"""Exercise the actual Bash deployment flow with isolated paths and fake Docker/HTTP."""
import json
import os
from pathlib import Path
import subprocess
import sys
import tempfile
import unittest

SCRIPT = Path(__file__).with_name('deploy.sh').read_text()

# External infrastructure is simulated; file replacement/configuration use real commands.
STUB = r'''
import json, os, pathlib, sys
name = pathlib.Path(sys.argv[0]).name
args = sys.argv[1:]
with open(os.environ['CALLS'], 'a') as log:
    log.write(json.dumps([name, *args]) + '\n')
if name == 'docker':
    if args[0] == 'inspect': print('a' * 40 if 'org.opencontainers.image.revision' in args[2] else 'old-image')
    if args[0] == 'exec' and args[1] == 'haji-server': print('HTTP/1.0 401 Unauthorized')
    if args[0] == 'compose' and 'up' in args and os.environ.get('FAIL_UP'): sys.exit(7)
if name == 'curl':
    if os.environ.get('FAIL_HTTP') or (os.environ.get('FAIL_H5') and args[-1].endswith('/haji/')): sys.exit(22)
    if '-o' in args:
        path = pathlib.Path(args[args.index('-o') + 1])
        if args[-1].endswith('/haji/'):
            path.write_text('<html>stale</html>' if os.environ.get('STALE_H5') else '<html>new</html>')
        else:
            path.write_text('{"code":1,"message":"unauthorized"}')
            print('401', end='')
    else: print('<html>new site</html>')
if name == 'sed':
    # macOS does not support the GNU sed -i syntax used by the Linux server.
    path = pathlib.Path(args[-1])
    key = args[1].split('^', 1)[1].split('=', 1)[0]
    path.write_text(''.join(line for line in path.read_text().splitlines(True) if not line.startswith(key + '=')))
'''


class LedgerDeploy(unittest.TestCase):
    def exercise(self, scope='all', failure=None, migration_changed=False):
        with tempfile.TemporaryDirectory(prefix='ledger-upload.', dir='/tmp') as temp:
            incoming = Path(temp)
            base = incoming / 'runtime'
            web = incoming / 'site'
            binaries = incoming / 'bin'
            for directory in (base, web, binaries):
                directory.mkdir()
            (base / 'release.env').write_text('HAJI_IMAGE=old-image\n')
            (base / 'compose.yaml').write_text('old compose')
            (base / 'migrations.sha256').write_text('old hash' if migration_changed else 'same hash')
            (web / 'index.html').write_text('<html>old</html>')
            (incoming / 'web').mkdir()
            (incoming / 'web/index.html').write_text('<html>new</html>')
            (incoming / 'compose.yaml').write_text('new compose')
            (incoming / 'migrations.sha256').write_text('same hash')
            for name in ('docker', 'curl', 'flock', 'sed'):
                stub = binaries / name
                stub.write_text('#!' + sys.executable + '\n' + STUB)
                stub.chmod(0o755)
            script = incoming / 'deploy.sh'
            script.write_text(SCRIPT.replace('base=/home/hahaen/ledger-runtime', 'base=' + str(base))
                              .replace('web_root=/home/hahaen/nginx/html/haji', 'web_root=' + str(web)))
            calls_file = incoming / 'calls.jsonl'
            env = {**os.environ, 'PATH': str(binaries) + os.pathsep + os.environ['PATH'], 'CALLS': str(calls_file)}
            if failure:
                env[failure] = '1'
            result = subprocess.run(['bash', str(script), 'ledger', scope, 'a' * 40, str(incoming)],
                                    env=env, capture_output=True, text=True)
            calls = [json.loads(line) for line in calls_file.read_text().splitlines()]
            result.backend_published = (base / 'backend-release.txt').exists()
            result.h5_published = (base / 'h5-release.txt').exists()
            self.assertFalse(list(incoming.glob('previous*')))
            self.assertFalse(list(incoming.glob('site.incoming.*')))
            return (result, calls, (base / 'release.env').read_text(),
                    (web / 'index.html').read_text(), (base / 'latest-release.txt').exists())

    def test_all_replaces_site_and_cleans_old_image_without_backup(self):
        result, calls, config, page, marker = self.exercise()
        self.assertEqual(result.returncode, 0, result.stderr)
        self.assertIn('HAJI_IMAGE=hahaen/haji-server:', config)
        self.assertIn('new', page)
        self.assertTrue(marker)
        self.assertTrue(result.backend_published)
        self.assertTrue(result.h5_published)
        self.assertIn('Published ledger/backend', result.stdout)
        self.assertIn('Published ledger/h5', result.stdout)
        self.assertIn(['docker', 'image', 'rm', 'old-image'], calls)
        self.assertEqual(sum(c[0] == 'docker' and c[1] == 'compose' and 'up' in c for c in calls), 1)

    def test_failed_replacement_does_not_restore_old_container(self):
        result, calls, config, page, marker = self.exercise(failure='FAIL_UP')
        self.assertNotEqual(result.returncode, 0)
        self.assertIn('no previous version was backed up or restored', result.stderr)
        self.assertEqual(sum(c[0] == 'docker' and c[1] == 'compose' and 'up' in c for c in calls), 1)
        self.assertIn('HAJI_IMAGE=hahaen/haji-server:', config)
        self.assertIn('old', page)  # Backend failure stops before H5 replacement.
        self.assertFalse(marker)

    def test_failed_http_does_not_restore_old_site(self):
        result, calls, config, page, marker = self.exercise(scope='h5', failure='FAIL_HTTP')
        self.assertNotEqual(result.returncode, 0)
        self.assertIn('new', page)
        self.assertEqual(config, 'HAJI_IMAGE=old-image\n')
        self.assertFalse(marker)
        self.assertFalse(any(c[0] == 'docker' and c[1] == 'compose' and 'up' in c for c in calls))

    def test_partial_success_is_reported_per_component(self):
        result, calls, config, page, marker = self.exercise(failure='FAIL_H5')
        self.assertNotEqual(result.returncode, 0)
        self.assertTrue(result.backend_published)
        self.assertFalse(result.h5_published)
        self.assertIn('Published ledger/backend', result.stdout)
        self.assertNotIn('Published ledger/h5', result.stdout)
        self.assertFalse(marker)
        self.assertIn(['docker', 'image', 'rm', 'old-image'], calls)

    def test_stale_site_cannot_be_reported_as_published(self):
        result, calls, config, page, marker = self.exercise(scope='h5', failure='STALE_H5')
        self.assertNotEqual(result.returncode, 0)
        self.assertFalse(result.h5_published)
        self.assertFalse(marker)

    def test_migration_change_stops_before_replacement(self):
        result, calls, config, page, marker = self.exercise(migration_changed=True)
        self.assertNotEqual(result.returncode, 0)
        self.assertIn('Database migrations changed', result.stderr)
        self.assertEqual(config, 'HAJI_IMAGE=old-image\n')
        self.assertIn('old', page)
        self.assertFalse(marker)
        self.assertFalse(any(c[0] == 'docker' and c[1] == 'build' for c in calls))


if __name__ == '__main__':
    unittest.main()

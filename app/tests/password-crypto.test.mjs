import assert from 'node:assert/strict'
import { generateKeyPairSync, privateDecrypt, randomBytes } from 'node:crypto'
import { createRequire } from 'node:module'
import { readFile } from 'node:fs/promises'
import { test } from 'node:test'
import ts from 'typescript'

const require = createRequire(import.meta.url)
const source = await readFile(new URL('../src/utils/passwordCryptoMp.ts', import.meta.url), 'utf8')
const compiled = ts.transpileModule(source, {
  compilerOptions: { target: ts.ScriptTarget.ESNext, module: ts.ModuleKind.CommonJS },
}).outputText

function loadModule() {
  const exports = {}
  new Function('require', 'exports', compiled)(require, exports)
  return exports
}

test('微信小程序密码兼容路径生成可由服务端 RSA-OAEP SHA-256 解密的密文', async () => {
  const keyPair = generateKeyPairSync('rsa', { modulusLength: 2048, publicExponent: 0x10001 })
  const publicKeyBase64 = keyPair.publicKey.export({ type: 'spki', format: 'der' }).toString('base64')
  const originalWx = globalThis.wx
  let requestedLength = 0
  globalThis.wx = {
    getRandomValues(options) {
      requestedLength = options.length
      const bytes = randomBytes(options.length)
      options.success({ randomValues: bytes.buffer.slice(bytes.byteOffset, bytes.byteOffset + bytes.byteLength) })
    },
  }

  try {
    const { encryptMiniProgramPassword } = loadModule()
    const encrypted = await encryptMiniProgramPassword('测试密码123456', publicKeyBase64)
    const decrypted = privateDecrypt({
      key: keyPair.privateKey,
      oaepHash: 'sha256',
      padding: require('node:crypto').constants.RSA_PKCS1_OAEP_PADDING,
    }, Buffer.from(encrypted, 'base64'))

    assert.equal(requestedLength, 32)
    assert.equal(decrypted.toString('utf8'), '测试密码123456')
  } finally {
    globalThis.wx = originalWx
  }
})

test('微信安全随机数接口失败时不降级使用弱随机数', async () => {
  const keyPair = generateKeyPairSync('rsa', { modulusLength: 2048, publicExponent: 0x10001 })
  const publicKeyBase64 = keyPair.publicKey.export({ type: 'spki', format: 'der' }).toString('base64')
  const originalWx = globalThis.wx
  globalThis.wx = { getRandomValues: options => options.fail() }

  try {
    const { encryptMiniProgramPassword } = loadModule()
    await assert.rejects(
      encryptMiniProgramPassword('测试密码123456', publicKeyBase64),
      /安全随机数生成失败/,
    )
  } finally {
    globalThis.wx = originalWx
  }
})

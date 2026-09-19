const STATIC_RESOURCE_BASE_URL = 'https://hahaen.xyz/minio-api/haji/wx'

export function staticResource(path: string): string {
  return `${STATIC_RESOURCE_BASE_URL}/${path.replace(/^\/+/, '')}`
}

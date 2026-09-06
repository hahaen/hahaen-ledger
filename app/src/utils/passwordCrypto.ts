import { request } from './api'

type PasswordKeyVO = { publicKey: string }

let cachedPublicKey = ''

function decodeBase64(value: string): ArrayBuffer {
  const binary = atob(value)
  const bytes = new Uint8Array(binary.length)
  for (let index = 0; index < binary.length; index += 1) bytes[index] = binary.charCodeAt(index)
  return bytes.buffer as ArrayBuffer
}

function encodeBase64(value: ArrayBuffer): string {
  const bytes = new Uint8Array(value)
  let binary = ''
  for (const byte of bytes) binary += String.fromCharCode(byte)
  return btoa(binary)
}

async function loadPublicKey(): Promise<string> {
  if (cachedPublicKey) return cachedPublicKey
  const result = await request<PasswordKeyVO>('/api/app/auth/password-key')
  if (!result.publicKey) throw new Error('密码安全配置不可用，请刷新页面后重试')
  cachedPublicKey = result.publicKey
  return cachedPublicKey
}

export async function encryptPassword(password: string): Promise<string> {
  const cryptoApi = globalThis.crypto
  if (!cryptoApi?.subtle || typeof TextEncoder === 'undefined' || typeof atob === 'undefined' || typeof btoa === 'undefined') {
    throw new Error('当前环境不支持安全密码加密，请使用 HTTPS 浏览器重试')
  }

  const publicKey = await cryptoApi.subtle.importKey(
    'spki',
    decodeBase64(await loadPublicKey()),
    { name: 'RSA-OAEP', hash: 'SHA-256' },
    false,
    ['encrypt'],
  )
  const encrypted = await cryptoApi.subtle.encrypt(
    { name: 'RSA-OAEP' },
    publicKey,
    new TextEncoder().encode(password),
  )
  return encodeBase64(encrypted)
}

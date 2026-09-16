import { request } from './api'

type UploadUrl = { fileId: string; uploadUrl: string; expiresInSeconds: number; status: string }
export type FileViewUrl = { fileId: string; viewUrl: string; expiresInSeconds: number; objectKey: string }
type AvatarUploadMetadata = { originalName: string; contentType: string; fileSize: number; fileHash: string }

function newIdempotencyKey(): string {
  const cryptoApi = globalThis.crypto as Crypto & { randomUUID?: () => string } | undefined
  return cryptoApi?.randomUUID?.() || `${Date.now()}-${Math.random().toString(36).slice(2)}`
}

async function sha256(file: File): Promise<string> {
  const subtle = globalThis.crypto?.subtle
  if (!subtle) throw new Error('当前浏览器不支持文件校验，请升级后重试')
  const digest = await subtle.digest('SHA-256', await file.arrayBuffer())
  return Array.from(new Uint8Array(digest), byte => byte.toString(16).padStart(2, '0')).join('')
}

async function actualImageContentType(file: File): Promise<string> {
  return imageContentTypeFromHeader(new Uint8Array(await file.slice(0, 12).arrayBuffer()))
}

function imageContentTypeFromHeader(header: Uint8Array): string {
  const isJpeg = header.length >= 3 && header[0] === 0xff && header[1] === 0xd8 && header[2] === 0xff
  if (isJpeg) return 'image/jpeg'
  const isPng = header.length >= 8 && header[0] === 0x89 && header[1] === 0x50 && header[2] === 0x4e
    && header[3] === 0x47 && header[4] === 0x0d && header[5] === 0x0a && header[6] === 0x1a && header[7] === 0x0a
  if (isPng) return 'image/png'
  const isGif = header.length >= 6 && header[0] === 0x47 && header[1] === 0x49 && header[2] === 0x46
    && header[3] === 0x38 && (header[4] === 0x37 || header[4] === 0x39) && header[5] === 0x61
  if (isGif) return 'image/gif'
  const isWebp = header.length >= 12 && header[0] === 0x52 && header[1] === 0x49 && header[2] === 0x46 && header[3] === 0x46
    && header[8] === 0x57 && header[9] === 0x45 && header[10] === 0x42 && header[11] === 0x50
  if (isWebp) return 'image/webp'
  throw new Error('头像文件不是支持的图片格式')
}

function miniFileError(error: unknown, fallback: string): Error {
  if (error instanceof Error) return error
  const message = typeof error === 'object' && error !== null && 'errMsg' in error ? String(error.errMsg || '') : ''
  if (message.includes('not in domain list')) return new Error(`${fallback}：上传地址不在小程序合法域名中`)
  if (/timeout|超时/i.test(message)) return new Error(`${fallback}：网络请求超时`)
  if (/ssl|certificate|证书/i.test(message)) return new Error(`${fallback}：HTTPS 证书异常`)
  return new Error(fallback)
}

const SHA256_K = [
  0x428a2f98, 0x71374491, 0xb5c0fbcf, 0xe9b5dba5, 0x3956c25b, 0x59f111f1, 0x923f82a4, 0xab1c5ed5,
  0xd807aa98, 0x12835b01, 0x243185be, 0x550c7dc3, 0x72be5d74, 0x80deb1fe, 0x9bdc06a7, 0xc19bf174,
  0xe49b69c1, 0xefbe4786, 0x0fc19dc6, 0x240ca1cc, 0x2de92c6f, 0x4a7484aa, 0x5cb0a9dc, 0x76f988da,
  0x983e5152, 0xa831c66d, 0xb00327c8, 0xbf597fc7, 0xc6e00bf3, 0xd5a79147, 0x06ca6351, 0x14292967,
  0x27b70a85, 0x2e1b2138, 0x4d2c6dfc, 0x53380d13, 0x650a7354, 0x766a0abb, 0x81c2c92e, 0x92722c85,
  0xa2bfe8a1, 0xa81a664b, 0xc24b8b70, 0xc76c51a3, 0xd192e819, 0xd6990624, 0xf40e3585, 0x106aa070,
  0x19a4c116, 0x1e376c08, 0x2748774c, 0x34b0bcb5, 0x391c0cb3, 0x4ed8aa4a, 0x5b9cca4f, 0x682e6ff3,
  0x748f82ee, 0x78a5636f, 0x84c87814, 0x8cc70208, 0x90befffa, 0xa4506ceb, 0xbef9a3f7, 0xc67178f2,
]

function rotateRight(value: number, bits: number): number {
  return (value >>> bits) | (value << (32 - bits))
}

function sha256Bytes(input: Uint8Array): string {
  const bitLength = input.length * 8
  const paddedLength = Math.ceil((input.length + 9) / 64) * 64
  const bytes = new Uint8Array(paddedLength)
  bytes.set(input)
  bytes[input.length] = 0x80
  const highLength = Math.floor(bitLength / 0x100000000)
  const lowLength = bitLength >>> 0
  bytes[paddedLength - 8] = highLength >>> 24
  bytes[paddedLength - 7] = highLength >>> 16
  bytes[paddedLength - 6] = highLength >>> 8
  bytes[paddedLength - 5] = highLength
  bytes[paddedLength - 4] = lowLength >>> 24
  bytes[paddedLength - 3] = lowLength >>> 16
  bytes[paddedLength - 2] = lowLength >>> 8
  bytes[paddedLength - 1] = lowLength

  let h0 = 0x6a09e667
  let h1 = 0xbb67ae85
  let h2 = 0x3c6ef372
  let h3 = 0xa54ff53a
  let h4 = 0x510e527f
  let h5 = 0x9b05688c
  let h6 = 0x1f83d9ab
  let h7 = 0x5be0cd19

  for (let offset = 0; offset < bytes.length; offset += 64) {
    const words = new Uint32Array(64)
    for (let index = 0; index < 16; index++) {
      const position = offset + index * 4
      words[index] = (bytes[position] << 24) | (bytes[position + 1] << 16) | (bytes[position + 2] << 8) | bytes[position + 3]
    }
    for (let index = 16; index < 64; index++) {
      const first = rotateRight(words[index - 15], 7) ^ rotateRight(words[index - 15], 18) ^ (words[index - 15] >>> 3)
      const second = rotateRight(words[index - 2], 17) ^ rotateRight(words[index - 2], 19) ^ (words[index - 2] >>> 10)
      words[index] = (words[index - 16] + first + words[index - 7] + second) >>> 0
    }

    let a = h0
    let b = h1
    let c = h2
    let d = h3
    let e = h4
    let f = h5
    let g = h6
    let h = h7
    for (let index = 0; index < 64; index++) {
      const choice = (e & f) ^ (~e & g)
      const majority = (a & b) ^ (a & c) ^ (b & c)
      const first = rotateRight(e, 6) ^ rotateRight(e, 11) ^ rotateRight(e, 25)
      const second = rotateRight(a, 2) ^ rotateRight(a, 13) ^ rotateRight(a, 22)
      const temp1 = (h + first + choice + SHA256_K[index] + words[index]) >>> 0
      const temp2 = (second + majority) >>> 0
      h = g
      g = f
      f = e
      e = (d + temp1) >>> 0
      d = c
      c = b
      b = a
      a = (temp1 + temp2) >>> 0
    }
    h0 = (h0 + a) >>> 0
    h1 = (h1 + b) >>> 0
    h2 = (h2 + c) >>> 0
    h3 = (h3 + d) >>> 0
    h4 = (h4 + e) >>> 0
    h5 = (h5 + f) >>> 0
    h6 = (h6 + g) >>> 0
    h7 = (h7 + h) >>> 0
  }

  return [h0, h1, h2, h3, h4, h5, h6, h7].map(value => value.toString(16).padStart(8, '0')).join('')
}

async function sha256ArrayBuffer(data: ArrayBuffer): Promise<string> {
  const subtle = globalThis.crypto?.subtle
  if (subtle) {
    const digest = await subtle.digest('SHA-256', data)
    return Array.from(new Uint8Array(digest), byte => byte.toString(16).padStart(2, '0')).join('')
  }
  return sha256Bytes(new Uint8Array(data))
}

async function finishAvatarUpload(metadata: AvatarUploadMetadata, putFile: (uploadUrl: string, contentType: string) => Promise<void>): Promise<FileViewUrl> {
  const upload = await request<UploadUrl>('/api/app/files/upload-url', {
    method: 'POST',
    data: {
      businessType: 'AVATAR',
      originalName: metadata.originalName,
      contentType: metadata.contentType,
      fileSize: metadata.fileSize,
      fileHash: metadata.fileHash,
      idempotencyKey: newIdempotencyKey(),
    },
  })
  if (upload.status === 'READY') return request<FileViewUrl>(`/api/app/files/${upload.fileId}/view-url`)
  if (upload.status !== 'UPLOADING') throw new Error('文件上传状态异常，请重试')
  await putFile(upload.uploadUrl, metadata.contentType)
  await request(`/api/app/files/${upload.fileId}/complete`, { method: 'POST' })
  return request<FileViewUrl>(`/api/app/files/${upload.fileId}/view-url`)
}

export async function uploadAvatar(file: File): Promise<FileViewUrl> {
  if (file.size <= 0 || file.size > 10 * 1024 * 1024) throw new Error('头像大小需大于 0 且不超过 10MB')
  const contentType = await actualImageContentType(file)
  const metadata = { originalName: file.name, contentType, fileSize: file.size, fileHash: await sha256(file) }
  return finishAvatarUpload(metadata, async uploadUrl => {
    const response = await fetch(uploadUrl, { method: 'PUT', headers: { 'Content-Type': contentType }, body: file })
    if (!response.ok) throw new Error('文件上传失败，请重试')
  })
}

function readMiniFile(filePath: string, position?: number, length?: number): Promise<ArrayBuffer> {
  return new Promise((resolve, reject) => {
    uni.getFileSystemManager().readFile({
      filePath,
      ...(position === undefined ? {} : { position }),
      ...(length === undefined ? {} : { length }),
      success: result => {
        if (typeof result.data === 'string') reject(new Error('头像文件读取失败，请重试'))
        else resolve(result.data)
      },
      fail: error => reject(miniFileError(error, '头像文件读取失败，请重试')),
    })
  })
}

function getMiniFileInfo(filePath: string): Promise<{ size: number }> {
  return new Promise((resolve, reject) => {
    uni.getFileSystemManager().getFileInfo({
      filePath,
      success: result => resolve({ size: result.size }),
      fail: error => reject(miniFileError(error, '头像文件校验失败，请重试')),
    })
  })
}

function putMiniFile(uploadUrl: string, contentType: string, data: ArrayBuffer): Promise<void> {
  return new Promise((resolve, reject) => {
    uni.request({
      url: uploadUrl,
      method: 'PUT',
      header: { 'Content-Type': contentType },
      dataType: 'text',
      data,
      success: response => response.statusCode >= 200 && response.statusCode < 300
        ? resolve()
        : reject(new Error(`头像上传失败（HTTP ${response.statusCode}），请重试`)),
      fail: error => reject(miniFileError(error, '头像上传失败，请重试')),
    })
  })
}

export async function uploadAvatarFromMiniPath(filePath: string, originalName = 'avatar.jpg'): Promise<FileViewUrl> {
  try {
    const fileInfo = await getMiniFileInfo(filePath)
    const data = await readMiniFile(filePath)
    const fileSize = fileInfo.size
    if (fileSize <= 0 || fileSize > 10 * 1024 * 1024) throw new Error('头像大小需大于 0 且不超过 10MB')
    const contentType = imageContentTypeFromHeader(new Uint8Array(data, 0, Math.min(12, data.byteLength)))
    const fileHash = await sha256ArrayBuffer(data)
    return await finishAvatarUpload({ originalName, contentType, fileSize, fileHash }, uploadUrl => putMiniFile(uploadUrl, contentType, data))
  } catch (error) {
    throw miniFileError(error, '头像上传失败，请重试')
  }
}

export function currentAvatar(): Promise<FileViewUrl | null> {
  return request<FileViewUrl | null>('/api/app/files/avatar/view-url')
}

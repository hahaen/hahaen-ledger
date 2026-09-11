import { request } from './api'

type UploadUrl = { fileId: string; uploadUrl: string; expiresInSeconds: number; status: string }
export type FileViewUrl = { fileId: string; viewUrl: string; expiresInSeconds: number; objectKey: string }

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
  const header = new Uint8Array(await file.slice(0, 12).arrayBuffer())
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

export async function uploadAvatar(file: File): Promise<FileViewUrl> {
  if (file.size <= 0 || file.size > 10 * 1024 * 1024) throw new Error('头像大小需大于 0 且不超过 10MB')
  const contentType = await actualImageContentType(file)
  const fileHash = await sha256(file)
  const upload = await request<UploadUrl>('/api/app/files/upload-url', {
    method: 'POST',
    data: {
      businessType: 'AVATAR',
      originalName: file.name,
      contentType,
      fileSize: file.size,
      fileHash,
      idempotencyKey: newIdempotencyKey(),
    },
  })
  if (upload.status === 'READY') return request<FileViewUrl>(`/api/app/files/${upload.fileId}/view-url`)
  if (upload.status !== 'UPLOADING') throw new Error('文件上传状态异常，请重试')
  const response = await fetch(upload.uploadUrl, { method: 'PUT', headers: { 'Content-Type': contentType }, body: file })
  if (!response.ok) throw new Error('文件上传失败，请重试')
  await request(`/api/app/files/${upload.fileId}/complete`, { method: 'POST' })
  return request<FileViewUrl>(`/api/app/files/${upload.fileId}/view-url`)
}

export function currentAvatar(): Promise<FileViewUrl | null> {
  return request<FileViewUrl | null>('/api/app/files/avatar/view-url')
}

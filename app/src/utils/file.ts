import { request } from './api'

type UploadUrl = { fileId: number; uploadUrl: string; expiresInSeconds: number; status: string }
export type FileViewUrl = { fileId: number; viewUrl: string; expiresInSeconds: number }

function newIdempotencyKey(): string {
  const cryptoApi = globalThis.crypto as Crypto & { randomUUID?: () => string } | undefined
  return cryptoApi?.randomUUID?.() || `${Date.now()}-${Math.random().toString(36).slice(2)}`
}

export async function uploadAvatar(file: File): Promise<FileViewUrl> {
  const contentType = file.type.toLowerCase()
  if (!['image/jpeg', 'image/png', 'image/webp', 'image/gif'].includes(contentType)) {
    throw new Error('头像仅支持 JPG、PNG、WEBP 或 GIF')
  }
  if (file.size <= 0 || file.size > 10 * 1024 * 1024) throw new Error('头像大小需大于 0 且不超过 10MB')
  const upload = await request<UploadUrl>('/api/app/files/upload-url', {
    method: 'POST',
    data: {
      businessType: 'AVATAR',
      originalName: file.name,
      contentType,
      fileSize: file.size,
      idempotencyKey: newIdempotencyKey(),
    },
  })
  if (upload.status !== 'UPLOADING') throw new Error('文件上传状态异常，请重试')
  const response = await fetch(upload.uploadUrl, { method: 'PUT', headers: { 'Content-Type': contentType }, body: file })
  if (!response.ok) throw new Error('文件上传失败，请重试')
  await request(`/api/app/files/${upload.fileId}/complete`, { method: 'POST' })
  return request<FileViewUrl>(`/api/app/files/${upload.fileId}/view-url`)
}

export function currentAvatar(): Promise<FileViewUrl | null> {
  return request<FileViewUrl | null>('/api/app/files/avatar/view-url')
}

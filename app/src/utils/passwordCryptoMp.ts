import * as forge from 'node-forge'

declare const wx: {
  getRandomValues(options: {
    length: number
    success: (result: { randomValues: ArrayBuffer }) => void
    fail: () => void
  }): void
}

function secureRandomBytes(length: number): Promise<string> {
  return new Promise((resolve, reject) => {
    if (typeof wx === 'undefined' || typeof wx.getRandomValues !== 'function') {
      reject(new Error('当前微信版本不支持安全密码加密，请升级微信后重试'))
      return
    }
    wx.getRandomValues({
      length,
      success: result => {
        const bytes = new Uint8Array(result.randomValues)
        let binary = ''
        for (const byte of bytes) binary += String.fromCharCode(byte)
        resolve(binary)
      },
      fail: () => reject(new Error('安全随机数生成失败，请重试')),
    })
  })
}

export async function encryptMiniProgramPassword(password: string, publicKeyBase64: string): Promise<string> {
  const publicKey = forge.pki.publicKeyFromAsn1(forge.asn1.fromDer(forge.util.decode64(publicKeyBase64)))
  const encrypted = publicKey.encrypt(forge.util.encodeUtf8(password), 'RSA-OAEP', {
    md: forge.md.sha256.create(),
    mgf1: { md: forge.md.sha256.create() },
    seed: await secureRandomBytes(32),
  })
  return forge.util.encode64(encrypted)
}

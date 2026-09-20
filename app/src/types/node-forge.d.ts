declare module 'node-forge' {
  namespace asn1 {
    interface Asn1Object {}
    function fromDer(bytes: string): Asn1Object
  }

  namespace md {
    interface MessageDigest {}
    namespace sha256 {
      function create(): MessageDigest
    }
  }

  namespace pki {
    interface RsaPublicKey {
      encrypt(data: string, scheme: 'RSA-OAEP', options: {
        md: md.MessageDigest
        mgf1: { md: md.MessageDigest }
        seed: string
      }): string
    }
    function publicKeyFromAsn1(value: asn1.Asn1Object): RsaPublicKey
  }

  namespace util {
    function decode64(value: string): string
    function encode64(value: string): string
    function encodeUtf8(value: string): string
  }
}

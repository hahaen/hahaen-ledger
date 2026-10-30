declare const __APP_ENV__: 'h5' | 'mp-weixin'
declare const __H5_BUILD_VERSION__: string
declare const uni: any
declare namespace UniApp { interface RequestOptions { url?: string; method?: string; data?: any; header?: Record<string, string> } }
interface ImportMetaEnv {
  readonly BASE_URL: string
  readonly MODE: string
  readonly VITE_APP_ENV?: 'development' | 'production'
  readonly VITE_API_BASE_URL?: string
}
interface ImportMeta { readonly env: ImportMetaEnv }

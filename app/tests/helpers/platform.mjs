import { createRequire } from 'node:module'

const require = createRequire(import.meta.url)
const { initPreContext, preJs, preHtml, preJson, preCss } = require('@dcloudio/uni-cli-shared/dist/preprocess/index.js')

// 使用项目编译器处理条件编译，避免直接 JSON.parse 将 pages.json 当作严格 JSON。
export function preprocessPlatform(source, platform, kind = 'js') {
  initPreContext(platform)
  return { js: preJs, html: preHtml, json: preJson, css: preCss }[kind](source, 'platform-test')
}

export function parsePages(source, platform = 'mp-weixin') {
  return JSON.parse(preprocessPlatform(source, platform, 'json'))
}

export function preprocessSfc(source, platform) {
  return preprocessPlatform(source, platform, 'html').replace(/(<script\b[^>]*>)([\s\S]*?)(<\/script>)/g,
    (_, start, script, end) => start + preprocessPlatform(script, platform) + end)
}

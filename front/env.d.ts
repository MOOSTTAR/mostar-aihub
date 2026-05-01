/// <reference types="vite/client" />

interface Window {
  MathJax?: {
    tex?: {
      inlineMath?: string[][]
      displayMath?: string[][]
      processEscapes?: boolean
      packages?: string[]
    }
    svg?: {
      fontCache?: string
    }
    startup?: {
      pageReady?: () => Promise<void>
      defaultPageReady?: () => Promise<void>
    }
    typesetPromise?: (elements?: HTMLElement[]) => Promise<void>
  }
}

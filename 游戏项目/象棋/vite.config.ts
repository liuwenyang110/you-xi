import { defineConfig } from 'vite'
import react from '@vitejs/plugin-react'
import { viteSingleFile } from "vite-plugin-singlefile"

// https://vite.dev/config/
export default defineConfig({
  plugins: [react(), viteSingleFile()],
  base: './', // 使用相对路径，方便直接打开或放在子目录下
  build: {
    outDir: 'dist',
    emptyOutDir: true,
  }
})

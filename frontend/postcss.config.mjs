/** @type {import('postcss-load-config').Config} */
// PostCSS processes Tailwind and adds CSS prefixes required for browser compatibility.
const config = {
  plugins: {
    tailwindcss: {},
    autoprefixer: {},
  },
}

export default config

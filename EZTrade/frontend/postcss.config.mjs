/** @type {import('postcss-load-config').Config} */
// PostCSS procesa Tailwind y anade prefijos CSS necesarios para compatibilidad de navegadores.
const config = {
  plugins: {
    tailwindcss: {},
    autoprefixer: {},
  },
}

export default config

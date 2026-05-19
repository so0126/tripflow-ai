const { defineConfig } = require('@vue/cli-service')
module.exports = defineConfig({
  transpileDependencies: true,
  devServer: {
      port: 80
    },
    css: {
    loaderOptions: {
      sass: {
        sassOptions: {
          // Silence specific deprecation warnings
          // 'import': Suppresses "@import rules are deprecated"
          // 'global-builtin': Suppresses "Global built-in functions are deprecated" (e.g., lighten, red)
          // 'color-functions': Suppresses color function warnings from Bootstrap
          silenceDeprecations: ['import', 'global-builtin', 'color-functions'],
        },
      },
    },
  },
});
module.exports = {
  pluginOptions: {
    quasar: {
      importStrategy: 'kebab',
      rtlSupport: false
    }
  },
  css: {
    loaderOptions: {
      sass: {
        prependData: `@import "@/styles/app_variables.scss";`
      }
    }
  },
  transpileDependencies: [
    'quasar'
  ]
}

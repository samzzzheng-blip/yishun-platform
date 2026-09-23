
const path = require('path')
const webpack = require('webpack')
const merge = require('webpack-merge')
const webpackConfigBase = require('./webpack.base.config')
const OpenBrowserPlugin = require('open-browser-webpack-plugin')
const HtmlWebpackPlugin = require('html-webpack-plugin')
const PORT = 3333

function resolve(relatedPath) {
  return path.join(__dirname, relatedPath)
}
const webpackConfigDev = {
  plugins: [
    // 定义环境变量为开发环境
    new webpack.DefinePlugin({
      'process.env.NODE_ENV': JSON.stringify('development'),
      IS_DEVELOPMETN: true,
    }),
    // 将打包后的资源注入到html文件内
    new HtmlWebpackPlugin({
      template: resolve('../app/index.html'),
      // mapConfig:'http://41.196.99.30/tgram-pgisbase/config/qdkjdsj_map_config.js'
    }),
    // new OpenBrowserPlugin({
    //   url: `http://localhost:${PORT}/#/login`,
    // }),
    new webpack.HotModuleReplacementPlugin(), //热加载插件
  ],
  devtool: 'source-map',
  devServer: {
    contentBase: resolve('../app'), // 根目录位置，访问index.html
    historyApiFallback: true, // 在开发单页应用时非常有用，它依赖于HTML5 history API，如果设置为true，所有的跳转将指向index.html
    hot: true,
    host: 'localhost',
    port: PORT,
    proxy: {
      '/upload': {
        target: "http://127.0.0.1:2222/",
      },
      '/api': {
        target: "http://localhost:8081/",
      }
    },
  },
}

module.exports = merge(webpackConfigBase, webpackConfigDev)

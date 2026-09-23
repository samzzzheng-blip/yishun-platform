import 'babel-polyfill'
import React from 'react'
import ReactDOM from 'react-dom'
import { Provider } from 'react-redux'
import Routes from '@configs/router.config'
import configure from '@middleware/configureStore'

if (typeof window !== 'undefined') {
  const hash = window.location.hash;
  if (hash.startsWith('#/')) {
    const hashPath = hash.slice(2);
    const [path, query] = hashPath.split('?');
    const newUrl = `${window.location.origin}/${path}${query ? `?${query}` : ''}`;
    window.location.replace(newUrl);
  }
}

const store = configure({ })
ReactDOM.render(
  <Provider store={store}>
    <Routes />
  </Provider>,
  document.getElementById('root'),
)

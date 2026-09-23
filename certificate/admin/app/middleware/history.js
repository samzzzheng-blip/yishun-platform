import { useRouterHistory } from 'react-router'
import createBrowserHistory from 'history/lib/createBrowserHistory'

// export default useRouterHistory(createHashHistory)()
const history = useRouterHistory(createBrowserHistory)({})
export default history

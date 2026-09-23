function hasUpdatePermission(menuKey) {
  const updateListStr = sessionStorage.getItem('gUpdateMenuList')
  return updateListStr.indexOf(`"${menuKey}"`) > -1
}

function hasDeletePermission(menuKey) {
  const deleteListStr = sessionStorage.getItem('gDeleteMenuList')
  return deleteListStr.indexOf(`"${menuKey}"`) > -1
}

function hasAddPermission(menuKey) {
  const deleteListStr = sessionStorage.getItem('gAddMenuList')
  return deleteListStr.indexOf(`"${menuKey}"`) > -1
}

function hasExportPermission(menuKey) {
  const deleteListStr = sessionStorage.getItem('gExportMenuList')
  return deleteListStr.indexOf(`"${menuKey}"`) > -1
}

export default {
  hasUpdatePermission,
  hasDeletePermission,
  hasAddPermission,
  hasExportPermission,
}

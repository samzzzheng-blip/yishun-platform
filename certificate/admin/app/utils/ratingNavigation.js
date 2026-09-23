// Workflow views inherit the existing rateManage permission; no new database role grants.
const keyOf = item => String(item.resKey || '').replace(/^\//, '')
function hasRatingMenu(items) {
  return (items || []).some(item => keyOf(item) === 'rateManage' || hasRatingMenu(item.children))
}
function canEditRating(storage) {
  return ['gAddMenuList','gUpdateMenuList'].some(key => {
    try { return hasRatingMenu(JSON.parse(storage.getItem(key) || '[]')) } catch(e) { return false }
  })
}
function canDeleteRating(storage) {
  try{return hasRatingMenu(JSON.parse(storage.getItem('gDeleteMenuList')||'[]'))}catch(e){return false}
}
function augmentRatingMenus(items, editable) {
  const keys = new Set()
  const collect = rows => (rows || []).forEach(item => {keys.add(keyOf(item));collect(item.children)})
  collect(items)
  const walk = rows => (rows || []).reduce((out,item) => {
    const copy = {...item}
    if(item.children) copy.children = walk(item.children)
    out.push(copy)
    if(keyOf(item) === 'rateManage') {
      if(!keys.has('rateTemporary')) {out.push({...item,resKey:'rateTemporary',resName:'临时',resIcon:'inbox',children:undefined});keys.add('rateTemporary')}
      if(editable && !keys.has('rateIntake')) {out.push({...item,resKey:'rateIntake',resName:'收卡建档',resIcon:'camera',children:undefined});keys.add('rateIntake')}
      if(!keys.has('rateWorkflow')) {out.push({...item,resKey:'rateWorkflow',resName:'评级流程',resIcon:'edit',children:undefined});keys.add('rateWorkflow')}
    }
    return out
  },[])
  return walk(items)
}
function safeRatingReturn(value) {
  return typeof value === 'string' && /^\/(rateManage|rateIntake|rateWorkflow|rateTemporary)(\?|$)/.test(value) ? value : '/manage'
}
module.exports = {hasRatingMenu, canEditRating, canDeleteRating, augmentRatingMenus, safeRatingReturn}

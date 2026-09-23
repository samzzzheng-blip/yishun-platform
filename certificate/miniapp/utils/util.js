const formatTime = date => {
  const year = date.getFullYear()
  const month = date.getMonth() + 1
  const day = date.getDate()
  const hour = date.getHours()
  const minute = date.getMinutes()
  const second = date.getSeconds()

  return [year, month, day].map(formatNumber).join('/') + ' ' + [hour, minute, second].map(formatNumber).join(':')
}

const formatNumber = n => {
  n = n.toString()
  return n[1] ? n : '0' + n
}

/**
 * 中文转为unicode 编码
 */
const encodeUnicode = str => {
  var res = [];
  for (var i = 0; i < str.length; i++) {
    res[i] = ("00" + str.charCodeAt(i).toString(16)).slice(-4);
  }
  return "\\u" + res.join("\\u");
}
/**
 * unicode编码解码为中文
 */
const decodeUnicode = str => {
  str = str.replace(/\\u/gi, '%u');
  return unescape(str);
}
/**
 * unicode编码解码为中文--特殊处理
 */
const decodeUnicodeBySpecial = str => {
  str = str.split('u');
  let list = []
  let regu = new RegExp('^[ ]+$'); 
  if(!regu.test(str)){
    str.forEach(function(item, index) {
      if(index == 0) {
        list.push(item);
      } else {
        if(item.length == 2) {
          item = 'u00' + item;
          list.push(item)
        } else if(item.length == 3){
          item = 'u00' + item;
          list.push(item)
        } else if(item.length == 4 && index!=str.length -1) {
          item = 'u0' + item;
          list.push(item)
        } else {
          item = 'u' + item;
          list.push(item)
        }
      }
      if(index == str.length -1) {
        str = list.join('')
        str = str.replace(/\\u/gi, '%u');
      }
    })
  }
  return unescape(str);
}

/** 替换emoji表情（四个字符） */
const filterEmoji = (name,str) => {
  var str = name.replace(/[\uD83C|\uD83D|\uD83E][\uDC00-\uDFFF][\u200D|\uFE0F]|[\uD83C|\uD83D|\uD83E][\uDC00-\uDFFF]|[0-9|*|#]\uFE0F\u20E3|[0-9|#]\u20E3|[\u203C-\u3299]\uFE0F\u200D|[\u203C-\u3299]\uFE0F|[\u2122-\u2B55]|\u303D|[\A9|\AE]\u3030|\uA9|\uAE|\u3030/ig, str);
  return str;
}

/**
 * 获取字符串长度
 */
const getStrLength = str => {
  var str = new String(str);
  var bytesCount = 0;
  for (var i = 0, n = str.length; i < n; i++) {
    var c = str.charCodeAt(i);
    if ((c >= 0x0001 && c <= 0x007e) || (0xff60 <= c && c <= 0xff9f)) {
      bytesCount += 1;
    } else {
      bytesCount += 2;
    }
  }
  return bytesCount;
}
/**
 * 深拷贝
 */
function copyobj(a){
  var c={};
  c=JSON.parse(JSON.stringify(a));
  return c;
}

function isEmpty(a) {
  return a == null || a == '' || a == 'undefined'
}


function getHttpsUrl(url) {
  if (url.indexOf('https:') == -1) {
    url = url.replace('http:', 'https:')
  }
  return url
}

module.exports = {
  formatTime: formatTime,
  encodeUnicode: encodeUnicode,
  decodeUnicode: decodeUnicode,
  filterEmoji: filterEmoji,
  getStrLength: getStrLength,
  decodeUnicodeBySpecial: decodeUnicodeBySpecial,
  copyobj: copyobj,
  isEmpty: isEmpty,
  getHttpsUrl: getHttpsUrl
}
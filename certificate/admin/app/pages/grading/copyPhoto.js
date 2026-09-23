// Clipboard images use PNG; the server's original photo is never changed.
export function clipboardPng(file) {
  if(file.type==='image/png')return Promise.resolve(file)
  return new Promise((resolve,reject)=>{
    const url=URL.createObjectURL(file),image=new Image()
    const finish=(error,blob)=>{clearTimeout(timer);image.onload=null;image.onerror=null;URL.revokeObjectURL(url);error?reject(error):resolve(blob)}
    const timer=setTimeout(()=>finish(new Error('图片处理超时，请重试或下载图片。')),20000)
    image.onerror=()=>finish(new Error('无法读取图片，请重新上传或下载图片。'))
    image.onload=()=>{
      try {
        if(!image.naturalWidth||!image.naturalHeight||image.naturalWidth*image.naturalHeight>40000000)throw new Error('图片尺寸过大或无效，请下载图片后使用。')
        const canvas=document.createElement('canvas');canvas.width=image.naturalWidth;canvas.height=image.naturalHeight
        const context=canvas.getContext('2d');if(!context)throw new Error('浏览器无法处理图片，请下载图片后使用。')
        context.drawImage(image,0,0)
        canvas.toBlob(blob=>finish(blob?null:new Error('图片转换失败，请下载图片后使用。'),blob),'image/png')
      }catch(e){finish(e)}
    }
    image.src=url
  })
}

export function copyPhoto(read) {
  if(!window.isSecureContext||!navigator.clipboard||!navigator.clipboard.write||typeof ClipboardItem==='undefined')return Promise.reject(new Error('当前浏览器不支持复制图片，请使用 HTTPS 页面或下载图片后粘贴。'))
  // Invoke write during the click, before awaiting the authenticated image request.
  const png=Promise.resolve().then(read).then(clipboardPng)
  png.catch(()=>{})
  try{return navigator.clipboard.write([new ClipboardItem({'image/png':png})])}catch(e){return Promise.reject(e)}
}

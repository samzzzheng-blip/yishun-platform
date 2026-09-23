export function base64ToBlob(base64, mime) {
  const byteString = atob(base64.split(',')[1]);
  const ab = new ArrayBuffer(byteString.length);
  const ia = new Uint8Array(ab);
  for (let i = 0; i < byteString.length; i++) {
    ia[i] = byteString.charCodeAt(i);
  }
  return new Blob([ab], { type: mime });
}

export function base64ToFile(base64, filename = 'warped.png') {
  const arr = base64.split(',');
  const mime = arr[0].match(/:(.*?);/)[1];
  const blob = base64ToBlob(base64, mime);
  return new File([blob], filename, { type: mime });
}

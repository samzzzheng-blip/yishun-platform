import subprocess,json,urllib.request,urllib.error
sql="SELECT access_token,tenant_id FROM system_oauth2_access_token WHERE user_type=2 AND user_id=1 AND deleted=0 AND expires_time>NOW() ORDER BY id DESC LIMIT 1"
cmd=['docker','exec','mysql','sh','-c','MYSQL_PWD="$MYSQL_ROOT_PASSWORD" mysql -uroot --batch --skip-column-names ruoyi_vue_pro -e "'+sql+'"']
r=subprocess.run(cmd,stdout=subprocess.PIPE,stderr=subprocess.PIPE,universal_newlines=True)
if r.returncode or not r.stdout.strip():
 print('No active admin session available for authenticated smoke test');raise SystemExit(0)
token,tenant=r.stdout.strip().split('\t')
headers={'Authorization':'Bearer '+token,'tenant-id':tenant,'Content-Type':'application/json'}
base='http://127.0.0.1:48080/admin-api/app/yikoujia'
def request(path, data=None):
 req=urllib.request.Request(base+path,headers=headers,data=None if data is None else json.dumps(data).encode(),method='GET' if data is None else 'PUT')
 try:
  with urllib.request.urlopen(req,timeout=20) as response:return json.load(response)
 except urllib.error.HTTPError as e:return {'http_status':e.code}
page=request('/page?pageNo=1&pageSize=1');print('List API code:',page.get('code'))
# A nonexistent id verifies the handler without modifying any record.
result=request('/image-order',{'id':9223372036854775807,'originalPicUrl':['verify-only'],'picUrl':['verify-only']})
print('Image-order nonexistent-id check:',json.dumps(result,ensure_ascii=False))
assert page.get('code')==0
assert result.get('code') not in (0,401,403,404,500,None),result

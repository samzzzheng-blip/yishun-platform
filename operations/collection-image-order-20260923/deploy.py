import json,subprocess,time,socket,http.client,os,shutil,urllib.request
from pathlib import Path
p=Path('/work/backups/image-order-20260923'); old=json.loads((p/'container.json').read_text())[0]
def run(*a):return subprocess.run(a,check=True,stdout=subprocess.PIPE,stderr=subprocess.PIPE,universal_newlines=True).stdout
class Docker(http.client.HTTPConnection):
 def __init__(self):super().__init__('localhost')
 def connect(self):self.sock=socket.socket(socket.AF_UNIX,socket.SOCK_STREAM);self.sock.connect('/var/run/docker.sock')
def api(path,data):
 c=Docker();c.request('POST',path,json.dumps(data),{'Content-Type':'application/json'});r=c.getresponse();b=r.read();assert r.status<300,(r.status,b);return json.loads(b) if b else None
backup='yudao-server-before-image-order-20260923'
networks=old['NetworkSettings']['Networks']
assert len(networks)==1
network,endpoint=next(iter(networks.items()));ip=endpoint['IPAddress']
config=dict(old['Config']);config['Image']='yudao-server:20260923-image-order';config['Hostname']=''
config['HostConfig']=old['HostConfig']
config['NetworkingConfig']={'EndpointsConfig':{network:{'IPAMConfig':{'IPv4Address':ip},'Aliases':['yudao-server']}}}
static=Path('/work/nginx/html/yudao-ui-admin');stage=Path('/work/nginx/html/yudao-ui-admin-image-order-stage')
if not stage.exists():shutil.copytree(static,stage)
run('tar','-xzf',str(p/'admin-image-order.tar.gz'),'-C',str(stage))
run('docker','stop','-t','30','yudao-server');run('docker','rename','yudao-server',backup);run('docker','network','disconnect',network,backup)
created=False;swapped=False
try:
 api('/containers/create?name=yudao-server',config);created=True
 run('docker','start','yudao-server');print('BACKEND_STARTED',flush=True)
 ready=False
 for i in range(90):
  time.sleep(2)
  logs=run('docker','logs','--tail','1000','yudao-server')
  if 'Started YudaoServerApplication' in logs or 'Started OnebookServerApplication' in logs:
   ready=True;break
  if i%10==0:print('WAITING_FOR_APPLICATION',i*2,flush=True)
 if not ready:raise RuntimeError('Startup success not observed')
 run('docker','exec','nginx','nginx','-t')
 static.rename('/work/nginx/html/yudao-ui-admin-before-image-order-20260923');stage.rename(static);swapped=True
 run('docker','exec','nginx','nginx','-s','reload')
 shutil.copy2(p/'app.jar','/work/projects/yudao-server/onebook-server.jar')
 print('DEPLOYED',flush=True)
except Exception:
 if created:subprocess.run(['docker','rm','-f','yudao-server'],stdout=subprocess.PIPE,stderr=subprocess.PIPE)
 run('docker','rename',backup,'yudao-server');run('docker','network','connect','--ip',ip,network,'yudao-server');run('docker','start','yudao-server')
 if swapped:
  static.rename('/work/nginx/html/yudao-ui-admin-failed-image-order-20260923');Path('/work/nginx/html/yudao-ui-admin-before-image-order-20260923').rename(static)
 run('docker','exec','nginx','nginx','-s','reload');print('ROLLED_BACK',flush=True);raise

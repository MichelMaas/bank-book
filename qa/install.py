#!/usr/bin/env python3
"""Real dpkg/debconf in private mounts; systemd/account operations are mocked."""
import json,os,pathlib,shutil,subprocess,sys,tempfile
B=pathlib.Path(__file__).resolve().parents[1];P=pathlib.Path
HELPER='''#!/usr/bin/python3
import os,pathlib,sys,json
p=pathlib.Path(os.environ['BANKBOOK_QA_STATE']);name=pathlib.Path(sys.argv[0]).name;a=sys.argv[1:]
with (p/'actions').open('a') as f:f.write(json.dumps([name]+a)+'\\n')
if name=='adduser':
 with open('/etc/passwd','a') as f:f.write('bankbook:x:0:0:QA:/var/lib/bankbook:/usr/sbin/nologin\\n')
 with open('/etc/group','a') as f:f.write('bankbook:x:0:\\n')
if name=='deb-systemd-invoke':
 if a[0]=='start':(p/'active').write_text('1')
 if a[0]=='stop':(p/'active').write_text('0')
'''
def inner(w):
 checks=[];dist=B/'target/debs'
 def run(*args,stdin=None):
  p=subprocess.run(args,input=stdin,capture_output=True,text=True)
  if p.returncode:raise RuntimeError(' '.join(args)+'\n'+p.stdout+p.stderr)
  return p.stdout
 def dpkg(*args):return run('dpkg','--force-depends','--force-confold',*args)
 def check(label,ok=True):
  assert ok,label;checks.append(label);print('PASS',label,flush=True)
 server=next(dist.glob('bankbook-server_*.deb'));client=next(dist.glob('bankbook-client_*.deb'));standalone=next(dist.glob('bankbook-standalone_*.deb'))
 run('debconf-set-selections',stdin='bankbook-client bankbook-client/server-url string https://bankbook.example:8443\n')
 dpkg('--install',str(server),str(client),str(standalone))
 check('echte dpkg-installatie drie pakketten met nagebootste systeemhelpers')
 check('debconf installatieadres opgeslagen',P('/etc/bankbook/client.properties').read_text().strip()=='server.url=https://bankbook.example:8443')
 check('servergegevensmap afgeschermd',(P('/var/lib/bankbook').stat().st_mode&0o777)==0o700)
 check('poort 80 en minimaal bindrecht ingesteld','BANKBOOK_PORT=80' in P('/etc/default/bankbook-server').read_text() and 'AmbientCapabilities=CAP_NET_BIND_SERVICE' in P('/usr/lib/systemd/system/bankbook-server.service').read_text())
 check('servicestart bij eerste installatie',(w/'state/active').read_text()=='1')
 cfg=P('/etc/default/bankbook-server');cfg.write_text(cfg.read_text()+'\n# QA local override\n');before=cfg.read_bytes()
 P('/var/lib/bankbook/qa-data').write_text('synthetic')
 dpkg('--install',str(server));check('conffile behouden bij herinstallatie',cfg.read_bytes()==before)
 (w/'state/active').write_text('0');dpkg('--install',str(server))
 check('gestopte service blijft gestopt bij herinstallatie',(w/'state/active').read_text()=='0')
 check('serverrestart via try-restart aangevraagd','try-restart' in (w/'state/actions').read_text())
 # A client reconfiguration changes the system default, not per-user choices.
 run('debconf-set-selections',stdin='bankbook-client bankbook-client/server-url string http://127.0.0.1\n')
 run('/var/lib/dpkg/info/bankbook-client.postinst','configure')
 check('adres later wijzigbaar',P('/etc/bankbook/client.properties').read_text().strip()=='server.url=http://127.0.0.1')
 invalid=subprocess.run(['/usr/bin/python3','/usr/lib/bankbook/url_config.py'],input='file:///etc/passwd',text=True,capture_output=True)
 check('ongeldige installatie-URL geweigerd',invalid.returncode!=0 and '127.0.0.1' in P('/etc/bankbook/client.properties').read_text())
 dpkg('--remove','bankbook-standalone','bankbook-client','bankbook-server')
 check('remove bewaart gegevens en serverconfig',P('/var/lib/bankbook/qa-data').read_text()=='synthetic' and cfg.read_bytes()==before)
 dpkg('--purge','bankbook-standalone','bankbook-client','bankbook-server')
 check('purge bewaart financiele gegevens en clientadres',P('/var/lib/bankbook/qa-data').read_text()=='synthetic' and P('/etc/bankbook/client.properties').exists())
 check('purge verwijdert serverconffile',not cfg.exists())
 (w/'results.json').write_text(json.dumps(checks,indent=2))
def outer():
 with tempfile.TemporaryDirectory(prefix='bankbook-install-') as td:
  w=P(td);(w/'state').mkdir();(w/'tools').mkdir();(w/'dpkg/info').mkdir(parents=True);(w/'dpkg/available').touch()
  for source,name,private in [('/usr/bin','usrbin',[]),('/usr/lib','usrlib',['systemd']),('/usr/share','usrshare',['doc','pixmaps','applications','metainfo'])]:
   dest=w/name;dest.mkdir()
   for entry in P(source).iterdir():
    if entry.name in private:(dest/entry.name).mkdir()
    else:(dest/entry.name).symlink_to('/mnt/'+source.removeprefix('/usr/')+'/'+entry.name)
  (w/'usrlib/systemd/system').mkdir()
  for name in ['adduser','deb-systemd-helper','deb-systemd-invoke','systemctl','ldconfig']:
   p=w/'tools'/name;p.write_text(HELPER);p.chmod(0o755)
  (w/'passwd').write_text('root:x:0:0:root:/root:/bin/bash\n');(w/'group').write_text('root:x:0:\n');(w/'nsswitch').write_text('passwd: files\ngroup: files\nhosts: files\n')
  (w/'dpkg/status').write_text('Package: bankbook-qa-base\nStatus: install ok installed\nArchitecture: all\nVersion: 1\nMaintainer: QA <qa@localhost>\nDescription: private base\n\n')
  dirs=['/.','/usr','/usr/bin','/usr/lib','/usr/lib/systemd','/usr/lib/systemd/system','/usr/share','/usr/share/doc','/usr/share/applications','/usr/share/pixmaps','/usr/share/metainfo','/etc','/etc/default','/var','/var/lib']
  (w/'dpkg/info/bankbook-qa-base.list').write_text('\n'.join(dirs)+'\n')
  cmd=['bwrap','--ro-bind','/','/','--ro-bind','/usr','/mnt','--unshare-user','--uid','0','--gid','0','--unshare-net','--tmpfs','/tmp','--tmpfs','/var/tmp','--ro-bind',str(B),str(B),'--bind',td,td,'--bind',str(w/'usrbin'),'/usr/bin','--bind',str(w/'usrlib'),'/usr/lib','--bind',str(w/'usrshare'),'/usr/share','--tmpfs','/etc','--bind',str(w/'passwd'),'/etc/passwd','--bind',str(w/'group'),'/etc/group','--ro-bind',str(w/'nsswitch'),'/etc/nsswitch.conf','--ro-bind','/etc/debconf.conf','/etc/debconf.conf','--tmpfs','/var/lib','--bind',str(w/'dpkg'),'/var/lib/dpkg','--tmpfs','/var/cache/debconf','--tmpfs','/run','--dir','/run/systemd/system','--proc','/proc','--dev','/dev','--die-with-parent','/usr/bin/python3',str(P(__file__).resolve()),'--inner',td]
  result=subprocess.run(cmd,env=dict(os.environ,PATH=str(w/'tools')+':/usr/bin:/usr/sbin:/bin:/sbin',DEBIAN_FRONTEND='noninteractive',BANKBOOK_QA_STATE=str(w/'state')),capture_output=True,text=True,timeout=150)
  (B/'qa/install.log').write_text(result.stdout+result.stderr)
  if result.returncode:raise RuntimeError((result.stdout+result.stderr)[-5000:])
  shutil.copyfile(w/'results.json',B/'qa/install-results.json');print(result.stdout)
if __name__=='__main__':
 if len(sys.argv)>1:inner(P(sys.argv[2]))
 else:outer()

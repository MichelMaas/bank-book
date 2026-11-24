#!/usr/bin/env python3
import hashlib,json,pathlib,subprocess,tarfile,io,tempfile,shutil
B=pathlib.Path(__file__).resolve().parents[1]
checks=[];info={}
unit=(B/'packaging/bankbook-server.service').read_text()
assert 'User=bankbook' in unit and 'AmbientCapabilities=CAP_NET_BIND_SERVICE' in unit and 'CapabilityBoundingSet=CAP_NET_BIND_SERVICE' in unit
for p in sorted((B/'target/debs').glob('*.deb')):
 raw=subprocess.check_output(['dpkg-deb','--fsys-tarfile',str(p)])
 with tarfile.open(fileobj=io.BytesIO(raw)) as t:
  members=t.getmembers()
  assert all(m.uid==0 and m.gid==0 for m in members)
  for member in members:
   if member.name.endswith('/client/client.jar'):
    expected=next((B/'bankbook-client/target').glob('bankbook-client-*.jar'))
    assert hashlib.sha256(t.extractfile(member).read()).digest()==hashlib.sha256(expected.read_bytes()).digest()
   if member.name.endswith('/bankbook/server.jar'):
    expected=next((B/'bankbook-frontend/target').glob('*-exec.jar'))
    assert hashlib.sha256(t.extractfile(member).read()).digest()==hashlib.sha256(expected.read_bytes()).digest()
 meta=subprocess.check_output(['dpkg-deb','-f',str(p),'Package','Version','Architecture','Depends'],text=True)
 info[p.name]={'sha256':hashlib.sha256(p.read_bytes()).hexdigest(),'size':p.stat().st_size,'control':meta}
 checks.append(p.name+': metadata en root-eigendom gecontroleerd')
assert len(info)==3
for p in (B/'packaging').iterdir():
 if p.suffix in ('.postinst','.postrm','.prerm','.config') or p.name=='client-launch':subprocess.run(['sh','-n',str(p)],check=True)
subprocess.run(['desktop-file-validate',str(B/'packaging/client.desktop')],check=True)
subprocess.run(['appstreamcli','validate','--no-net',str(B/'packaging/bankbook.metainfo.xml')],check=True)
with tempfile.TemporaryDirectory(prefix='bankbook-unit-') as tmp:
 unit=pathlib.Path(tmp)/'bankbook-server.service'
 # This build machine uses SDKMAN, not an APT-installed /usr/bin/java.
 unit.write_text((B/'packaging/bankbook-server.service').read_text().replace('/usr/bin/java',str(pathlib.Path(shutil.which('java')).resolve())))
 result=subprocess.run(['systemd-analyze','verify','--man=no',str(unit)],capture_output=True,text=True)
if result.returncode:raise RuntimeError(result.stdout+result.stderr)
checks.append('shellscripts, desktop-item, AppStream en systemd-unit gecontroleerd')
(B/'qa/package-results.json').write_text(json.dumps({'checks':checks,'packages':info},indent=2)+'\n')
for c in checks:print('PASS',c)

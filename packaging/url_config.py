"""Write a validated debconf value; no shell evaluation or credential storage."""
import os,pathlib,sys,tempfile,urllib.parse
s=sys.stdin.read(8193).strip()
p=urllib.parse.urlsplit(s)
if len(s)>4096 or p.scheme not in ('http','https') or not p.hostname or p.username is not None or p.password is not None or p.query or p.fragment or any(ord(c)<33 or ord(c)==127 for c in s) or (p.port is not None and not 0<p.port<=65535):raise ValueError('Ongeldig serveradres')
root=pathlib.Path('/etc/bankbook');root.mkdir(mode=0o755,exist_ok=True)
fd,name=tempfile.mkstemp(prefix='.client-',dir=root)
try:
 with os.fdopen(fd,'w') as f:f.write('server.url='+s+'\n')
 os.chmod(name,0o644);os.replace(name,root/'client.properties')
finally:
 if os.path.exists(name):os.unlink(name)

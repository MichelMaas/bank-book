#!/usr/bin/env python3
"""Real packaged JVM and HTTP smoke test using private data and network."""
import json,os,pathlib,shutil,subprocess,sys,tempfile,time,urllib.request,urllib.error,signal
B=pathlib.Path(__file__).resolve().parents[1]
def inner(work):
    root=work/'root';root.mkdir();deb=next((B/'target/debs').glob('bankbook-server_*.deb'))
    subprocess.run(['dpkg-deb','-x',str(deb),str(root)],check=True)
    subprocess.run(['dpkg-deb','-x',str(next((B/'target/debs').glob('bankbook-client_*.deb'))),str(root)],check=True)
    if os.environ.get('BANKBOOK_QA_USE_BUILD')=='1':
        shutil.copyfile(next((B/'bankbook-client/target').glob('bankbook-client-*.jar')),root/'usr/share/bankbook/client/client.jar')
    data=work/'data';home=work/'home';data.mkdir();home.mkdir()
    runtime=home/'.local/share/bankbook/jcef'
    shutil.copytree(work/'jcef-runtime',runtime,symlinks=True)
    opener=urllib.request.build_opener(urllib.request.ProxyHandler({}))
    jar=root/'usr/share/bankbook/server.jar';checks=[]
    def start():
        logfile=(work/'server.log').open('ab')
        p=subprocess.Popen([os.environ['BANKBOOK_QA_JAVA'],'--add-opens=java.base/java.time=ALL-UNNAMED','--add-opens=java.base/java.lang=ALL-UNNAMED','-Xmx512m','-Djava.awt.headless=true','-Duser.home='+str(home),'-Dbankbook.dataDir='+str(data),'-jar',str(jar),'--server.port=38081'],cwd=work,stdout=logfile,stderr=logfile,env=dict(os.environ,HOME=str(home)))
        logfile.close()
        end=time.monotonic()+55
        while time.monotonic()<end:
            if p.poll() is not None:raise RuntimeError('Server stopte: '+(work/'server.log').read_text(errors='replace')[-6500:])
            try:
                with opener.open('http://127.0.0.1:38081/',timeout=3) as response:
                    body=response.read()
                    assert response.status==200 and b'<html' in body.lower()
                    return p,body
            except (OSError,urllib.error.HTTPError):time.sleep(.3)
        p.terminate();p.wait(timeout=15);raise RuntimeError('Server niet gereed: '+(work/'server.log').read_text(errors='replace')[-6500:])
    processes=[]
    try:
        p,body=start();processes.append(p);checks.append('ingepakte server start met Java 21 zonder desktop en levert HTML')
        assert (data/'bankbook.ico').exists() and (data/'bankbook.png').exists();checks.append('iconen uit executable JAR correct uitgepakt in private datamap')
        (data/'qa-marker.txt').write_text('synthetic data retained')
        p.terminate();p.wait(timeout=15)
        p,body=start();processes.append(p)
        assert (data/'qa-marker.txt').read_text()=='synthetic data retained';checks.append('gegevensmap behouden na volledige JVM-herstart')
        assert not (home/'.bankbook').exists();checks.append('instelbare datamap gebruikt; geen impliciete persoonlijke opslag')
        java=pathlib.Path(os.environ['BANKBOOK_QA_JAVA']);javac=java.with_name('javac')
        cp=str(root/'usr/share/bankbook/client/client.jar')+':'+str(root/'usr/share/bankbook/client/lib/*')
        subprocess.run([str(javac),'-cp',cp,'-d',str(work),str(B/'qa/SettingsProbe.java'),str(B/'qa/GuiProbe.java')],check=True)
        qaenv=dict(os.environ,HOME=str(home),XDG_CONFIG_HOME=str(home/'config'),GSETTINGS_BACKEND='memory')
        subprocess.run([str(java),'-Duser.home='+str(home),'-Dbankbook.clientConfig='+str(work/'no-system-config'),'-cp',str(work)+':'+cp,'SettingsProbe'],env=qaenv,check=True)
        checks.append('client URL-validatie en opslag getest met tijdelijke configuratie')
        xv=pathlib.Path(os.environ['BANKBOOK_QA_XVFB'])
        pathlib.Path('/tmp/.X11-unix').mkdir(mode=0o1777,exist_ok=True)
        xvlog=(work/'xvfb.log').open('wb')
        xe=dict(qaenv,LD_LIBRARY_PATH=os.environ.get('BANKBOOK_QA_XLIB',''),LIBGL_ALWAYS_SOFTWARE='1',__EGL_VENDOR_LIBRARY_FILENAMES='/usr/share/glvnd/egl_vendor.d/50_mesa.json')
        x=subprocess.Popen([str(xv),':87','-screen','0','1280x900x24','-nolisten','tcp','-ac'],stdout=xvlog,stderr=xvlog,env=xe)
        processes.append(x);xvlog.close();time.sleep(.5)
        wm=subprocess.Popen([os.environ['BANKBOOK_QA_WM'],'--sm-disable'],env=dict(qaenv,DISPLAY=':87',LD_LIBRARY_PATH='/tmp/bankbook-display-tools/root/usr/lib/x86_64-linux-gnu',XDG_DATA_DIRS='/tmp/bankbook-display-tools/root/usr/share:/usr/share',XDG_CONFIG_DIRS='/tmp/bankbook-display-tools/root/etc/xdg:/etc/xdg'),stdout=subprocess.DEVNULL,stderr=subprocess.DEVNULL)
        processes.append(wm);time.sleep(.7)
        comp=subprocess.Popen([os.environ['BANKBOOK_QA_COMP'],'-a'],env=dict(qaenv,DISPLAY=':87'),stdout=subprocess.DEVNULL,stderr=subprocess.DEVNULL)
        processes.append(comp);time.sleep(.5)
        command=[str(java),'--add-exports=java.desktop/sun.awt=ALL-UNNAMED','--add-exports=java.desktop/sun.java2d=ALL-UNNAMED','-Duser.home='+str(home),'-Dbankbook.serverUrl=http://127.0.0.1:38081','-Dbankbook.qa.screenshot='+str(work/'client.png'),'-cp',str(work)+':'+cp,'GuiProbe']
        with (work/'client.log').open('w') as out:
            native=subprocess.Popen(command,env=dict(qaenv,DISPLAY=':87',LIBGL_ALWAYS_SOFTWARE='1',__EGL_VENDOR_LIBRARY_FILENAMES='/usr/share/glvnd/egl_vendor.d/50_mesa.json'),stdout=out,stderr=out,start_new_session=True)
            try:
                code=native.wait(timeout=65)
            except subprocess.TimeoutExpired:
                code=124
            finally:
                try:os.killpg(native.pid,signal.SIGKILL)
                except ProcessLookupError:pass
                native.wait(timeout=10)
        if code:raise RuntimeError('Clientproef: '+(work/'client.log').read_text(errors='replace')[-8000:])
        assert 'NotSerializableException' not in (work/'server.log').read_text(errors='replace')
        checks.append('gedeelde CEFViewer toont echte importpagina zonder paginaserialisatiefouten')
        for c in checks:print('PASS',c,flush=True)
        (work/'result.json').write_text(json.dumps(checks,indent=2))
    finally:
        for p in processes:
            if p.poll() is None:p.terminate();p.wait(timeout=20)

def outer():
    java=str(pathlib.Path(shutil.which('java')).resolve())
    with tempfile.TemporaryDirectory(prefix='bankbook-runtime-') as tmp:
        w=pathlib.Path(tmp)
        cached=pathlib.Path(os.environ.get('BANKBOOK_QA_JCEF','/home/michel/.local/share/filerenamer/jcef'))
        # Copy runtime binaries only; never copy another app's browser profile/cache.
        shutil.copytree(cached,w/'jcef-runtime',ignore=shutil.ignore_patterns('cache','Cache','GPUCache','Code Cache','*.log','*.dmp'),symlinks=True)
        xv=shutil.which('Xvfb') or '/tmp/uw2-test-runtime/root/usr/bin/Xvfb'
        extra=[]
        if xv.startswith('/tmp/'):extra=['--ro-bind','/tmp/uw2-test-runtime/root','/tmp/uw2-test-runtime/root']
        if pathlib.Path('/tmp/bankbook-display-tools/root').exists():extra += ['--ro-bind','/tmp/bankbook-display-tools/root','/tmp/bankbook-display-tools/root']
        cmd=['bwrap','--ro-bind','/','/','--unshare-user','--unshare-net','--tmpfs','/tmp','--tmpfs','/var/tmp','--ro-bind',str(B),str(B),'--bind',tmp,tmp,*extra,'--proc','/proc','--dev','/dev','--die-with-parent',sys.executable,str(pathlib.Path(__file__).resolve()),'--inner',tmp]
        p=subprocess.run(cmd,env=dict(os.environ,BANKBOOK_QA_JAVA=java,BANKBOOK_QA_XVFB=xv,BANKBOOK_QA_WM=shutil.which('openbox') or '/tmp/bankbook-display-tools/root/usr/bin/openbox',BANKBOOK_QA_COMP=shutil.which('xcompmgr') or '/tmp/bankbook-display-tools/root/usr/bin/xcompmgr',BANKBOOK_QA_XLIB='/tmp/uw2-test-runtime/root/usr/lib/x86_64-linux-gnu' if xv.startswith('/tmp/') else '',HOME=tmp,TMPDIR=tmp),capture_output=True,text=True,timeout=180)
        (B/'qa/runtime.log').write_text(p.stdout+p.stderr)
        if (w/'server.log').exists():shutil.copyfile(w/'server.log',B/'qa/server.log')
        for name in ['client.png','client.log','xvfb.log']:
            if (w/name).exists():shutil.copyfile(w/name,B/'qa'/name)
        if p.returncode:raise RuntimeError((p.stdout+p.stderr)[-7500:])
        shutil.copyfile(w/'result.json',B/'qa/runtime-results.json');print(p.stdout)
if __name__=='__main__':
    if len(sys.argv)>1:inner(pathlib.Path(sys.argv[2]))
    else:outer()

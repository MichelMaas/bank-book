#!/usr/bin/env python3
"""Build Linux packages after all application modules; never uploads or installs."""
import argparse, hashlib, os, pathlib, re, shutil, subprocess, tempfile
BASE=pathlib.Path(__file__).resolve().parent.parent

def write(root,path,text,mode=0o644):
    p=root/path.lstrip('/');p.parent.mkdir(parents=True,exist_ok=True);p.write_text(text);p.chmod(mode)
def copy(root,source,dest):
    p=root/dest.lstrip('/');p.parent.mkdir(parents=True,exist_ok=True);shutil.copyfile(source,p);p.chmod(0o644)
def build(version,output,revision=None):
    revision = revision or (BASE/"packaging/revision").read_text().strip()
    if not re.fullmatch(r"[1-9][0-9]*",revision):raise ValueError("Ongeldige pakketrevision")
    if not re.fullmatch(r'\d[0-9A-Za-z.+~-]*',version):raise ValueError('Ongeldige Maven/pakketversie')
    debversion=version.removesuffix('-SNAPSHOT')+('~snapshot' if version.endswith('-SNAPSHOT') else '')+'-'+revision
    if subprocess.check_output(['dpkg','--print-architecture'],text=True).strip()!='amd64':raise ValueError('De desktop-DEB wordt voorlopig alleen op Linux amd64 gebouwd.')
    server=BASE/f'bankbook-frontend/target/bankbook-frontend-{version}-exec.jar'
    client=BASE/f'bankbook-client/target/bankbook-client-{version}.jar'
    libs=BASE/'bankbook-client/target/lib'
    if not server.is_file() or not client.is_file() or not list(libs.glob('jcefmaven-*.jar')):raise ValueError('Bouw eerst alle modules inclusief client-runtime.')
    output.mkdir(parents=True,exist_ok=True)
    with tempfile.TemporaryDirectory(prefix='bankbook-deb-') as td:
        for name,arch,depends,description in [
          ('bankbook-server','all','openjdk-21-jre-headless, adduser, init-system-helpers (>= 1.54~)','Bankbook headless server'),
          ('bankbook-client','amd64','openjdk-21-jre, debconf (>= 0.5) | debconf-2.0, python3, libgtk-3-0, libxtst6, libxi6, libxrender1, libgl1, libasound2t64 | libasound2','Bankbook desktopclient'),
          ('bankbook-standalone','all',f'bankbook-server (= {debversion}), bankbook-client (= {debversion})','Bankbook server en client op een computer')]:
            root=pathlib.Path(td)/name;root.mkdir()
            write(root,'DEBIAN/control',f'Package: {name}\nVersion: {debversion}\nSection: misc\nPriority: optional\nArchitecture: {arch}\nMaintainer: Michel Maas <michel@localhost>\nDepends: {depends}\nDescription: {description}\n Bankbook met Maven-release en blijvende instellingen en gegevens.\n')
            write(root,f'usr/share/doc/{name}/README', (BASE/'README.md').read_text())
            write(root,f'usr/share/doc/{name}/copyright','Bankbook: private project by Michel Maas; no public license declared.\nBundled dependencies retain their own licenses. See JAR META-INF and legal notices.\n')
            if name=='bankbook-server':
                copy(root,server,'usr/share/bankbook/server.jar')
                for src,dst in [('bankbook-server.service','usr/lib/systemd/system/bankbook-server.service'),('server.defaults','etc/default/bankbook-server')]:copy(root,BASE/'packaging'/src,dst)
                write(root,'DEBIAN/conffiles','/etc/default/bankbook-server\n')
                for script in ['postinst','prerm','postrm']:copy(root,BASE/'packaging'/f'server.{script}',f'DEBIAN/{script}');(root/'DEBIAN'/script).chmod(0o755)
            elif name=='bankbook-client':
                copy(root,client,'usr/share/bankbook/client/client.jar')
                for lib in libs.glob('*.jar'):copy(root,lib,'usr/share/bankbook/client/lib/'+lib.name)
                copy(root,BASE/'bankbook-frontend/src/main/resources/open/images/bankbook.png','usr/share/pixmaps/bankbook.png')
                copy(root,BASE/'packaging/bankbook.metainfo.xml','usr/share/metainfo/nl.maas.Bankbook.metainfo.xml')
                for src,dst,mode in [('client-launch','usr/bin/bankbook',0o755),('client.desktop','usr/share/applications/bankbook.desktop',0o644),('client-settings.desktop','usr/share/applications/bankbook-settings.desktop',0o644),('client.templates','DEBIAN/templates',0o644),('client.config','DEBIAN/config',0o755),('client.postinst','DEBIAN/postinst',0o755),('client.postrm','DEBIAN/postrm',0o755),('url_config.py','usr/lib/bankbook/url_config.py',0o644)]:
                    copy(root,BASE/'packaging'/src,dst);(root/dst).chmod(mode)
            # Publish only a complete package and never replace the same version with different bytes.
            epoch=int(os.environ.get('SOURCE_DATE_EPOCH','1700000000'))
            for item in sorted(root.rglob('*'), reverse=True):os.utime(item,(epoch,epoch))
            os.utime(root,(epoch,epoch))
            tmp=pathlib.Path(td)/(name+'.deb')
            subprocess.run(['dpkg-deb','--root-owner-group','-Zxz','-z3','--threads-max=2','--build',str(root),str(tmp)],check=True)
            dst=output/f'{name}_{debversion}_{arch}.deb'
            if dst.exists() and dst.read_bytes()!=tmp.read_bytes():
                # Snapshot builds are intentionally replaceable only in the build output.
                if not version.endswith('-SNAPSHOT'):raise ValueError('Bestaande release-DEB wijkt af; verhoog de pakketrevision: '+str(dst))
            shutil.copyfile(tmp,dst)
            print(str(dst),flush=True)
    hashes=''.join(f'{hashlib.sha256(p.read_bytes()).hexdigest()}  {p.name}\n' for p in sorted(output.glob(f'*_{debversion}_*.deb')))
    (output/'SHA256SUMS').write_text(hashes)
if __name__=='__main__':
    p=argparse.ArgumentParser();p.add_argument('--version',required=True);p.add_argument('--output',type=pathlib.Path,required=True);p.add_argument('--revision');a=p.parse_args();build(a.version,a.output,a.revision)

import nl.maas.bankbook.client.ClientSettings;
import java.nio.file.*;
public class SettingsProbe {
 public static void main(String[] args) throws Exception {
  for(String s:new String[]{"file:///etc/passwd","javascript:alert(1)","http://admin:secret@server/","http://server:0","http://server/#test","http://server/?x=1"}) {
   try {ClientSettings.INSTANCE.validate(s);throw new AssertionError("URL geaccepteerd: "+s);}catch(IllegalArgumentException expected){}
  }
  String url="https://bankbook.example:8443/app";
  ClientSettings.INSTANCE.save(url);
  if(!ClientSettings.INSTANCE.load().equals(url))throw new AssertionError("Gebruikerskeuze niet bewaard");
  System.setProperty("bankbook.serverUrl","http://127.0.0.1");
  if(!ClientSettings.INSTANCE.load().equals("http://127.0.0.1"))throw new AssertionError("Voorrang override");
  System.out.println("PASS URL-validatie, blijvende gebruikerskeuze en voorrang instellingen");
 }
}

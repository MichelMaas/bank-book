# Bankbook: server, client en stand-alone

Java 21, gedeelde parent `nl.maas:parent:2026.04`, easy-frameworks 2026.08.
Maven-artifacts gaan via de geërfde distributionManagement naar Reposilite;
DEBs blijven losse installatiebestanden. Geen credentials in deze broncode.

## Bouwen en releasen

```
mvn clean verify
mvn clean package -Ppackage-linux-deb
```

DEBs en SHA256SUMS staan in `target/debs`. Linux amd64, Python 3 en dpkg-deb
zijn nodig op de bouwcomputer. Java wordt op de doelcomputer via APT geïnstalleerd.
De server bevat een executable Spring Boot JAR, de client dezelfde `Viewers.viewer` uit easy-wicket-framework-lib als FileRenamer.
De DEBs gebruiken eigen installatiescripts voor systemd en de serveradresvraag;
ze hebben geen jpackage-runtime nodig. Maven publiceert ze niet naar Reposilite.

Na controle en commit van alle wijzigingen, en met een schone Git-werkmap:

```
mvn release:prepare
mvn release:perform -Ppackage-linux-deb
```

De release-build staat onder `target/checkout`; de DEBs daaronder in `target/debs`.
`deployAtEnd` voorkomt Maven-upload zolang een modulebuild faalt, maar maakt de
netwerkpublicatie niet atomair. Geen automatische Git-push/release door de packager.
Bij een gewijzigde publieke release verhoog je de versie; publiceer geen andere
inhoud met dezelfde versie. Snapshot-DEBs zijn uitsluitend om lokaal te testen.

## Installatie

Server (CT of desktop): `sudo apt install ./bankbook-server_*.deb`
Client: `sudo apt install ./bankbook-client_*.deb`
Stand-alone: installeer de drie bij elkaar horende DEBs in één APT-opdracht,
of via de eigen APT-repository `sudo apt install bankbook-standalone`.
Het stand-alone pakket trekt de exacte server- en clientversie binnen.
De server blijft als systeemservice draaien wanneer het clientvenster sluit.

De client vraagt tijdens installatie het serveradres via debconf. Bij installatie
zonder vragen geldt `http://127.0.0.1`. Achteraf wijzigen:
`sudo dpkg-reconfigure bankbook-client`, of per gebruiker via **Bankbook server instellen** in het startmenu
(of `bankbook --configure`). Heropen de client na een wijziging. De per-gebruiker instelling heeft voorrang.
Systeemdefault: `/etc/bankbook/client.properties` (gegenereerd, bewaard bij verwijderen).
Gebruikerskeuze: `$XDG_CONFIG_HOME/bankbook/client.properties`, standaard
`~/.config/bankbook/client.properties`. Omgevingsvariabele `BANKBOOK_SERVER_URL`
of Java-property `bankbook.serverUrl` gaat voor beide bestanden.
Het clientpakket installeert geen lokale server en bewaart geen financiële gegevens.

De server luistert standaard alleen op `127.0.0.1:80`. Voor een aparte server:
wijzig `/etc/default/bankbook-server` naar `BANKBOOK_BIND=0.0.0.0`, herstart met
`sudo systemctl restart bankbook-server`, en gebruik bijvoorbeeld
`http://bankbook.mnw` in de client. De bestaande app heeft geen ingebouwde
gebruikersauthenticatie; stel financiële gegevens alleen beschikbaar binnen een
passend afgeschermd netwerk of via een beveiligde toegangspoort.

## Gegevens en updates

De service draait als `bankbook`, met gegevens in `/var/lib/bankbook` (0700).
Bestaande persoonlijke gegevens in `~/.bankbook` worden niet automatisch verplaatst.
Voor migratie: stop de service, maak een backup van beide locaties, kopieer zelf
de gewenste bestanden en geef de servicegebruiker toegang. Er is geen wijziging
van de financiële bestandsformaten bedoeld. Probeer migratie eerst met een kopie.

`/etc/default/bankbook-server` is een dpkg-conffile. Updates herstarten een actieve
service; een gestopte service blijft gestopt. Remove/purge bewaren de financiële
gegevens en de serviceaccount. Logs: `journalctl -u bankbook-server`.
Een backup omvat `/var/lib/bankbook` en `/etc/default/bankbook-server`, terwijl
de service gestopt is. Een downgrade van alleen software herstelt geen gegevens.

## Testen en beperkingen

`mvn clean verify` voert de bestaande parser-, CSV-, voorspelling- en tuple-tests uit
met synthetische gegevens. Er wordt geen persoonlijk CSV-bestand ingelezen.
`python3 qa/runtime.py` test de ingepakte JVM, de echte HTTP-pagina, herstarten,
clientconfiguratie en framework-viewer-paginaweergave op Xvfb. `python3 qa/install.py` test
echte dpkg/debconf-acties in een eigen tijdelijk bestandssysteem. Voor deze twee
proeven zijn bubblewrap, een JDK 21, Xvfb, openbox en xcompmgr op de bouwcomputer nodig. Op deze
werkcomputer is Xvfb uit een bestaande geïsoleerde testruntime gebruikt.
Systemd en accountaanmaak worden nagebootst; er is geen echte CT-installatie
of test met persoonlijke financiële bestanden uitgevoerd.

Online automatische vertaling staat standaard uit, zodat de pagina ook zonder
internet opent. Bestaande lokale vertalingen blijven beschikbaar; optioneel kan
`--bankbook.translation.remote-enabled=true` online vertaling activeren.

De client gebruikt exact de bestaande CEF-viewer en runtimevoorbereiding van
FileRenamer. Bij de eerste start bereidt de framework-lib de JCEF-runtime voor
onder `~/.local/share/bankbook/jcef`; daarbij is internet nodig. Bestaande runtimes
van andere apps worden niet gewijzigd. Java-libraries behouden hun
licentievermeldingen in de JARs. De gemeenschappelijke
parent bevat nog oudere expliciet vastgezette libraries; deze wijziging is geen
volledige audit van alle transitieve dependencies.

De SPA bewaart maximaal vijf live Wicket-pagina's per HTTP-sessie, zonder
paginaserialisatie naar schijf. Daardoor blokkeren niet-serialiseerbare callbacks
uit de bestaande frameworkbibliotheek het AJAX-scherm niet. Browser-terug geeft
geen historische momentopname van een gewijzigde pagina; gebruik de navigatie
van de app. Er is geen clustering/session-replicatie ingericht. Bij herstart
open je een nieuwe sessie; de apart opgeslagen bankgegevens blijven bestaan.

## Wijziging naar poort 80 (pakketrevision 2)

Nieuwe installaties gebruiken poort 80. De dienst draait als `bankbook` en krijgt
via systemd uitsluitend `CAP_NET_BIND_SERVICE`; Java zelf krijgt geen globaal
setcap-recht. Het standaardclientadres is `http://127.0.0.1`.

Bij een bestaande installatie bewaart dpkg lokale conffile-aanpassingen of stelt
het een configuratievraag. Een ongewijzigde oude standaardconfiguratie kan naar
poort 80 worden bijgewerkt. Eerder gekozen client-URLs blijven staan. Controleer
daarom na een upgrade beide instellingen. Zet zo nodig zelf `BANKBOOK_PORT=80` in `/etc/default/bankbook-server`,
herstart de service en pas het clientadres aan. Voor bereikbaarheid vanaf een
andere computer blijft `BANKBOOK_BIND=0.0.0.0` nodig. Een eerdere expliciete
clientkeuze wordt niet stilzwijgend veranderd.

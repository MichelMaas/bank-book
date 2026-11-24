# Bankbook — verificatie 24 november 2026

Geteste proefpakketten: `2026.01~snapshot-2`. Bronversie: `2026.01-SNAPSHOT`.
Parent `nl.maas:parent:2026.04`; easy-wicket-framework-lib `2026.08`.
De Git-werkmap is niet gecommit, getagd of gepubliceerd.

- Maven-build geslaagd; tien tests zonder fouten of overgeslagen tests. Machineafhankelijke
  CSV- en ongeldige oude testdata vervangen door synthetische fixtures. Opslagtest controleert
  ook het schrijven en teruglezen van een transactie met datum en bedrag onder Java 21.
- Echte JVM uit het serverpakket gestart in een privé-netwerknamespace. HTML-pagina,
  resource-extractie, instelbare datamap en volledige procesherstart gecontroleerd.
- Client gebruikt daadwerkelijk `Viewers.viewer` / `CEFViewer` uit de bestaande framework-lib.
  Native CEF-browser met de echte Bankbook-importpagina getest; importformulier verschijnt
  en het laadscherm verdwijnt. Venster visueel bekeken; afbeelding: `qa/client.png`.
- De native clientproef gebruikte een tijdelijke runtimekopie zonder de browsercache of
  het profiel van filerenamer. Alleen voor de proef zijn softwareweergave en een browserflag
  voor de buitenste geïsoleerde omgeving gebruikt; die flags staan niet in de launcher.
- Dertien controles met echte dpkg/debconf: installatie van alle drie pakketten, serveradres
  via preseed, adreswijziging, ongeldige URL geweigerd, private datamap, herinstallatie,
  conffile-behoud, behoud van gestopte service en gegevensbehoud bij remove/purge.
  Account- en systemd-opdrachten zijn daarbij nagebootst, niet op de host uitgevoerd.
- Shellscripts, desktop-item, AppStream, root-eigendom en afhankelijkheden gecontroleerd.
  Systemd-analyze gebruikte uitsluitend voor de test het lokale SDKMAN-Java-pad; op de
  doelcomputer levert de APT-afhankelijkheid `/usr/bin/java`.
- APT-installatiesimulatie op Ubuntu 24.04 vond Java 21 en de overige afhankelijkheden.
- De JARs in de definitieve DEBs zijn bytegelijk aan de geteste builds. SHA-256 is ook na
  kopiëren naar `/home/michel/shares/downloads/Debs` gecontroleerd.

Geen echte CT-installatie, eigen financiële bestanden of volledige interactieve
boekhoudregressie getest. CEF/OpenGL meldt waarschuwingen in de virtuele softwareomgeving;
de echte importpagina is zichtbaar en de serverlog bevat geen paginaserialisatiefouten.
De eerste runtime-download is niet opnieuw getest; de bestaande runtimevoorbereiding
van de framework-lib wordt ongewijzigd gebruikt.

Logs en controlebestanden staan onder `qa`. Pakketten en SHA256SUMS staan onder
`target/debs`. Alleen de drie DEBs zijn extra naar de gedeelde Debs-map gekopieerd.
Een reservekopie van de eerder aanwezige gewijzigde bestanden staat in
`/home/michel/Documenten/ChatGPT/Bash enzo/bankbook-backup-20261124-184820`.
Bestaande persoonlijke gegevens in `~/.bankbook` zijn niet verplaatst of gewijzigd.

## Revision 2: poort 80 en Kotlin-client

Nieuwe installaties gebruiken poort 80, standaardclientadres `http://127.0.0.1`.
De service houdt User=bankbook en krijgt uitsluitend CAP_NET_BIND_SERVICE via
AmbientCapabilities en CapabilityBoundingSet; geen globaal setcap op Java.
Installatieproef en systemd-unitcontrole bevestigen de nieuwe configuratie.

De testomgeving weigert een bind op poort 80, ook in een eigen netwerknamespace
met uitsluitend CAP_NET_BIND_SERVICE (capabilitymasker 0x400). Daarom gebruikt
de daadwerkelijke JVM-/HTTP-/CEF-proef tijdelijk poort 38081. Een echte start
op poort 80 onder systemd in de Proxmox-CT blijft nog te bevestigen.

De client is inmiddels Kotlin. De schone build en alle tien Maven-tests slagen;
ook URL-opslag, de echte gedeelde CEFViewer en alle dertien dpkg/debconf-controles
slagen. De omzetting van het instellingenscherm is hersteld, inclusief afsluiten
na `--configure`. De definitieve pakket-JARs zijn bytegelijk aan de geteste builds.

De vijf POMs en opgeloste dependency trees zijn afzonderlijk onderzocht. Er
blijven versie-inconsistenties via parent/framework-lib; zie VERSIECONTROLE.md.
Dit is geen verklaring dat alle dependencies nu consistent zijn. De gedeelde
parent en framework-lib zijn bij deze wijziging niet aangepast of gepubliceerd.

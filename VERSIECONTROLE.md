# Versiecontrole Bankbook — 24 november 2026

Gecontroleerd: de vijf lokale POMs, de werkelijk gebruikte parent
`nl.maas:parent:2026.04`, de bron van parent `2026.05-SNAPSHOT` en de verbose
Maven dependency trees van backend, frontend en client. De bomen staan per
module onder `target/dependency-tree.txt`; bouwlog: `qa/dependency-audit.log`.

## Conclusie

Niet alle versies zijn consistent. De build kan slagen omdat Maven één versie
kiest; dat bewijst niet dat iedere combinatie van bibliotheek-API's passend is.
`omitted for conflict` betekent juist dat die kandidaat niet wordt ingeladen.
Een afwijkende transitieve aanvraag is dus niet automatisch een runtimefout.

| Onderdeel | Werkelijk gekozen | Herkomst / beoordeling |
| --- | --- | --- |
| Coroutines | core 1.11.0, core-jvm 1.10.2 | Parent declareert core 1.11.0; dependency management kiest voor de JVM-implementatie 1.10.2. Afstemmen als één familie. |
| Jackson 2 | module-kotlin 2.12.5, databind/core 2.21.5 | Expliciete oude moduleversie in parent tegenover nieuwere beheerde kernversies. Deze familie moet worden afgestemd. |
| Commons Text | backend 1.13.1; frontend/client 1.9 | Lokale backenddeclaratie tegenover dependency management uit parent. De frontend krijgt daardoor een andere versie dan de backend zelf. |
| Commons CSV / Validator | backend/frontend 1.14.0 / 1.9.0; client 1.9.0 / 1.6 | Client erft oudere transitieven via framework-lib. Per proces maar één versie; wel verschil tussen modules. |
| Commons IO | backend/frontend 2.18.0; client 2.21.0 | Verschillende dependency-paden; Wicket vraagt 2.21.0, maar de server kiest de dichterbij gelegen 2.18.0. API-compatibiliteit nagaan. |
| ChartJS-wrapper | wicket-chartjs 9.0.0-M2 met geselecteerd Wicket 10.8.0 | Wrapper vraagt Wicket 9.0.0-M5, dat wordt weggemedieerd. Dit vraagt een gerichte grafiekproef of een aantoonbaar passende wrapper; het versienummer alleen bewijst geen fout. |

Kotlin stdlib en reflect komen beide uit op 2.4.20. De actieve Spring Boot-
modules komen uit op 4.0.8, Spring Framework op 7.0.9 en Wicket op 10.8.0.
De oude transitieve aanvragen daarnaar staan wel in de boom, maar zijn niet
allemaal aparte JARs in de uiteindelijke classpath.

Jackson 3 (`tools.jackson`, 3.1.5) is daarnaast aanwezig via Spring Boot 4.
Dat is een andere namespace dan Jackson 2 (`com.fasterxml.jackson`). Het
naast elkaar bestaan is geen gewone dubbele versie van hetzelfde artifact.
Forceer daarom niet alles naar Jackson 2 of alles naar Jackson 3.

## Plaats om op te lossen

In `/home/michel/projects/parent/pom.xml` staan ook in 2026.05-SNAPSHOT nog
`jackson-module-kotlin.version=2.12.5`, `commons-text.version=1.9` en de
oude ChartJS-wrapperpin. Bankbook gebruikt momenteel de gepubliceerde 2026.04;
alleen een wijziging aan de 2026.05-SNAPSHOT-bron verandert die build dus niet.

Stem de families centraal af in dependencyManagement (eventueel met een BOM)
en voorkom losse afwijkende versies in kinderen. Bij geïmporteerde BOMs is
alleen een losse projectproperty verhogen niet altijd genoeg: controleer de
opgeloste tree, met name core-jvm. Verander een reeds gepubliceerde parentversie
niet achteraf van inhoud; publiceer een nieuwe parent en pas de projectreferentie aan.

Deze controle heeft de gedeelde parent en framework-lib niet gewijzigd.
De poort-80-aanpassing en Kotlin-omzettingsreparaties zijn aparte wijzigingen
in Bankbook; er zijn geen brede dependency-upgrades doorgevoerd of gepubliceerd.

## Hercontrole met parent 2026.05

De actuele Bankbook-POM verwijst nu naar de gepubliceerde parent 2026.05.
De bron van de gedeelde parent staat inmiddels op 2026.06-SNAPSHOT.
De dependency-tree-opdracht is opnieuw geslaagd voor alle modules; dit was
een versiecontrole, geen nieuwe volledige testbuild of DEB-aflevering.

| Onderdeel | Backend, frontend én client | Status |
| --- | --- | --- |
| Commons Text | 1.15.0 | Nu consistent; het eerdere verschil is opgelost. |
| Jackson 2 | Kotlin-module 2.21.7; databind/core 2.21.5 | Nog niet afgestemd; dependency management verlaagt de kernversies. |
| Coroutines | core 1.11.0; core-jvm 1.10.2 | Nog niet afgestemd; dependency management verlaagt core-jvm. |

De parent-properties zijn dus goed verhoogd, maar voor de laatste twee families
moet ook de geïmporteerde versiesturing worden aangepast. Importeer de
Coroutines BOM 1.11.0 en Jackson 2 BOM 2.21.7 vóór de Spring Boot BOM in
dependencyManagement; laat expliciete familiepins daarmee overeenkomen.
Spring Boots afzonderlijke Jackson 3-familie blijft door Boot beheerd.

Nieuwe bomen: `target/dependency-recheck.txt` per module.
Logboek: `qa/versions-recheck.log`. Eerdere tabellen hierboven beschrijven
de vorige controle met parent 2026.04; deze hercontrole is de actuele stand.

## Hercontrole met parent 2026.06 — BOMs actief

Dit is de actuele stand en vervangt voor de drie besproken families de eerdere
bevindingen hierboven. Alle drie appmodules lossen nu op naar:

- Coroutines core én core-jvm: 1.11.0.
- Jackson 2 Kotlin-module, core en databind: 2.21.7.
- Commons Text: 1.15.0.

De drie modules zijn daarna schoon gebouwd. Alle tien bestaande tests slagen.
De client-runtime bevat na de schone bouw geen oude versies uit de vorige build.
Jackson 3 blijft afzonderlijk door Spring Boot beheerd.

Bewijs: `qa/dependency-versions.json`, `qa/versions-recheck.log`,
`qa/bom-verification.log` en de nieuwe dependency trees per module.
Dit bevestigt de drie besproken families; de andere aandachtspunten uit het
oorspronkelijke onderzoek zijn hiermee niet automatisch afgedaan.
Er zijn bij deze hercontrole geen nieuwe DEBs gemaakt of afgeleverd.

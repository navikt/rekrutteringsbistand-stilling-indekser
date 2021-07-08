# rekrutteringsbistand-kandidat-indekser

Applikasjon i GCP som henter stillinger fra Kafka og indekserer dem i ElasticSearch.

## Kjøre lokalt
Start Elastic Search i Docker:
```shell
chmod +x start-elastic-search.sh
./start-elastic-search.sh
```
Kjør prosjekt lokalt: Høyreklikk på `LokalApp.kt` og vel `Run`


# Hvordan fungerer appen

Applikasjonen lytter på endringer på stillinger fra Arbeidsplassen via en Kafka-topic. For hver stilling hentes ekstra stillingsinfo via et API-kall til rekrutteringsbistand-stilling-api. Til slutt indekseres stillingen i Elastic Search.

## Reindeksering
Ved å inkrementere miljøvariabelen `INDEKS_VERSJON` i `nais.yaml` og redeploye applikasjonen vil man hente alle stillinger fra start (1. januar 2020) og legge dem inn på nytt i en ny Elastic Search-indeks. Dette vil man typisk gjøre hvis man har endra hvordan stillingene lagres i indeksen.

Når applikasjonen starter opp ved en reindeksering vil to Kafka-konsumenter starte opp. Den ene vil fortsette å indeksere indeksen som er i bruk i produksjon, slik at dataen der er oppdatert til enhver tid.
Den andre Kafka-konsumenten vil hente alle stillinger fra start, og indeksere disse stillingene i en ny indeks på nytt format.

Hvordan vet den koden som leser fra Elasticsearch sin indeks at den skal lese fra en ny (versjon av) indeks? Koden som leser fra indeks bruker et alias istedenfor å referere til en konkret indeks. Dette aliaset er mulig å endre slik at aliasnavnet mapperet til nytt indeksnavn (som er dete samme som indeks versjon). Etter reindeksering - dvs. når ny indeks er populert med nyeste stillinger og inneholder samme stillinger som den gamle indeksen - så kan man sende en HTTP GET-request til `/internal/byttIndeks`-endepunktet til appen. Da vil aliaset som peker på indeksen som skal brukes i produksjon byttes over til ny indeks, og brukerne vil kunne hente data på nytt format.

## Slette gamle indekser
Det er ingen automatikk for sletting av gamle indekser som ikke er i bruk lengre. Man må da manuelt gjøre REST-kall direkte mot Elastic Search for å liste opp og slette gamle indekser.


# Spørringer mot Elastic Search

## Koble til
Lokal Elastic Search: `localhost:9200/`
URLer til Elastic Search i dev-gcp og prod-gcp ligger i filene `nais-dev.json` og `nais-prod.json`.
Spørringer må gjøres inne i podden med WGET.

Brukernavn og passord til Elastic Search finnes i Kubernetes-podden:
```shell
kubectl exec -it <pod> -n <namespace> -- /bin/sh
echo $ES_USERNAME
echo $ES_PASSWORD
```

## Spørringer
Vi har et alias `stilling` som peker på riktig indeks.

Liste indekser:
```sh
wget <url>/_cat/indices --user <brukernavn> --password <passord>
```

Slette indeks:
```sh
wget --method=DELETE <url>/<indeksnavn> --user <brukernavn> --password <passord>
```

Hente ut dokument med UUID:
```sh
wget <url>/stilling/_doc/<uuid på stilling> --user <brukernavn> --password <passord>
```

# Henvendelser

## For Nav-ansatte
* Dette Git-repositoriet eies av [Team inkludering i Produktområde arbeidsgiver](https://navno.sharepoint.com/sites/intranett-prosjekter-og-utvikling/SitePages/Produktomr%C3%A5de-arbeidsgiver.aspx).
* Slack-kanaler:
  * [#inkludering-utvikling](https://nav-it.slack.com/archives/CQZU35J6A)
  * [#arbeidsgiver-utvikling](https://nav-it.slack.com/archives/CD4MES6BB)
  * [#arbeidsgiver-general](https://nav-it.slack.com/archives/CCM649PDH)

## For folk utenfor Nav
* Opprett gjerne en issue i Github for alle typer spørsmål
* IT-utviklerne i Github-teamet https://github.com/orgs/navikt/teams/arbeidsgiver
* IT-avdelingen i [Arbeids- og velferdsdirektoratet](https://www.nav.no/no/NAV+og+samfunn/Kontakt+NAV/Relatert+informasjon/arbeids-og-velferdsdirektoratet-kontorinformasjon)

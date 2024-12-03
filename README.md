# rekrutteringsbistand-stilling-indekser

Applikasjon i GCP som henter stillinger fra Kafka og indekserer dem i OpenSearch.

## Kjøre lokalt

Start Open Search i Docker:

```shell
chmod +x start-open-search.sh
./start-open-search.sh
```

Kjør prosjekt lokalt: Høyreklikk på `LokalApp.kt` og vel `Run`

# Hvordan fungerer appen

Applikasjonen lytter på endringer på stillinger fra Arbeidsplassen via en Kafka-topic. For hver stilling hentes ekstra stillingsinfo via et API-kall til rekrutteringsbistand-stilling-api. Til slutt indekseres stillingen i Open Search.

## Reindeksering

Ved å inkrementere miljøvariabelen `INDEKS_VERSJON` i `nais.yaml` og redeploye applikasjonen vil man hente alle stillinger fra start (1. januar 2020) og legge dem inn på nytt i en ny Open Search-indeks. Dette vil man typisk gjøre hvis man har endra hvordan stillingene lagres i indeksen.

Når applikasjonen starter opp ved en reindeksering vil to Kafka-konsumenter starte opp. Den ene vil fortsette å indeksere indeksen som er i bruk i produksjon, slik at dataen der er oppdatert til enhver tid.
Den andre Kafka-konsumenten vil hente alle stillinger fra start, og indeksere disse stillingene i en ny indeks på nytt format.

Hvordan vet den koden som leser fra Opensearch sin indeks at den skal lese fra en ny (versjon av) indeks? Koden som leser fra indeks bruker et alias istedenfor å referere til en konkret indeks. Dette aliaset er mulig å endre slik at aliasnavnet mapperet til nytt indeksnavn (som er dete samme som indeks versjon). Etter reindeksering - dvs. når ny indeks er populert med nyeste stillinger og inneholder samme stillinger som den gamle indeksen - så kan man sende en HTTP GET-request til `/internal/byttIndeks`-endepunktet til appen. Da vil aliaset som peker på indeksen som skal brukes i produksjon byttes over til ny indeks, og brukerne vil kunne hente data på nytt format.

Vær oppmerksom på at gammel indekser pr 3/12/2024 stanser å indeksere under reindeksering, slik at forespørsel-api kan feile, og brukere ikke finner nye stillinger under "mine stilliger" før alias er byttet. Bør derfor gjøres på kveldstid, eventuelt med nedetid på frontend,  frem til dette er fikset.


## Slette gamle indekser

Det er ingen automatikk for sletting av gamle indekser som ikke er i bruk lengre. Man må da manuelt gjøre REST-kall direkte mot Open Search for å liste opp og slette gamle indekser.

# Spørringer mot Open Search

## Koble til

Lokal Open Search: `localhost:9200/`
Spørringer må gjøres inne i podden med WGET.

Brukernavn og passord til Open Search finnes i Kubernetes-podden:

```shell
kubectl exec -it <pod> -n <namespace> -- /bin/sh
echo $OPEN_SEARCH_USERNAME
echo $OPEN_SEARCH_PASSWORD
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

- Dette Git-repositoriet eies
  av [Team tiltak og inkludering (TOI) i Produktområde arbeidsgiver](https://teamkatalog.nais.adeo.no/team/0150fd7c-df30-43ee-944e-b152d74c64d6)
  .
- Slack-kanaler:
  - [#arbeidsgiver-toi-dev](https://nav-it.slack.com/archives/C02HTU8DBSR)
  - [#arbeidsgiver-utvikling](https://nav-it.slack.com/archives/CD4MES6BB)

## For folk utenfor Nav

- Opprett gjerne en issue i Github for alle typer spørsmål
- IT-utviklerne i Github-teamet https://github.com/orgs/navikt/teams/toi
- IT-avdelingen
  i [Arbeids- og velferdsdirektoratet](https://www.nav.no/no/NAV+og+samfunn/Kontakt+NAV/Relatert+informasjon/arbeids-og-velferdsdirektoratet-kontorinformasjon)

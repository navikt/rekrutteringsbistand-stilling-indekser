## rekrutteringsbistand-kandidat-indekser

Henter stillinger fra Kafka og indekserer dem i ElasticSearch.

Start Elastic Search i Docker:
```shell
chmod +x start-elastic-search.sh
./start-elastic-search.sh
```
Kjør prosjekt lokalt: Høyreklikk på `LokalApp.kt` og vel `Run`


# Spørringer mot Elastic Search

## Koble til
Lokal Elastic Search: `localhost:9200/`
URLer til Elastic Search i dev-gcp og prod-gcp ligger i filene `nais-dev.json` og `nais-prod.json`.
Spørringer må gjøres inne i podden med WGET.

Brukernavn og passord til Elastic Search finnes i Kubernetes-podden:
```shell
kubectl exec -it <pod> -- /bin/sh
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

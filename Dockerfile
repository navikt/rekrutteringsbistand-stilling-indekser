FROM ghcr.io/navikt/baseimages/temurin:18
COPY ./build/libs/rekrutteringsbistand-stilling-indekser-all.jar app.jar

EXPOSE 8222

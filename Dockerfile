FROM navikt/java:18
COPY ./build/libs/rekrutteringsbistand-stilling-indekser-all.jar app.jar

EXPOSE 8222

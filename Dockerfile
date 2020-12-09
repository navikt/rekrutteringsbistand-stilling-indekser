FROM navikt/java:15
COPY ./build/libs/rekrutteringsbistand-stilling-indekser-all.jar app.jar

EXPOSE 8222

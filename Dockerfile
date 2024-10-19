FROM gcr.io/distroless/java17-debian12:nonroot
ADD build/distributions/rekrutteringsbistand-stilling-indekser.tar /
ENTRYPOINT ["java", "-cp", "/rekrutteringsbistand-stilling-indekser/lib/*", "rekrutteringsbistand.stilling.indekser.AppKt"]
EXPOSE 8222

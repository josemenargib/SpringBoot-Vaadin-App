FROM openjdk:21-jdk-buster as build
RUN apt-get install -y git
RUN git clone https://git.primefactorsolutions.com/PFS/pfs-intra.git
RUN cd pfs-intra && ./mvnw clean package -Pproduction

FROM build
COPY --from=0 pfs-intra/target/*.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "/app.jar"]
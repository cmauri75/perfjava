FROM amazoncorretto:20-alpine as builder

COPY gradle gradle
COPY build.gradle .
COPY settings.gradle .
COPY gradlew .
COPY src src

RUN ./gradlew build -x test

FROM amazoncorretto:20-alpine
EXPOSE 8081

ENV JAVA_OPTS=${JAVA_OPTS:-""}
WORKDIR /app

COPY --from=builder ./build/libs/perfjava-*-SNAPSHOT.jar /app/app.jar

CMD java ${JAVA_OPTS} -Djava.security.egd=file:/dev/./urandom --enable-preview -jar /app/app.jar

FROM amazoncorretto:20-alpine as builder

COPY gradle gradle
COPY build.gradle .
COPY settings.gradle .
COPY gradlew .
COPY src src

RUN ./gradlew build -x test

FROM amazoncorretto:20-alpine as corretto-deps

COPY --from=builder ./build/libs/perfjava-*-SNAPSHOT.jar /app/app.jar

RUN unzip /app/app.jar -d temp &&  \
    jdeps  \
      --print-module-deps \
      --ignore-missing-deps \
      --recursive \
      --multi-release 20 \
      --class-path="./temp/BOOT-INF/lib/*" \
      --module-path="./temp/BOOT-INF/lib/*" \
      /app/app.jar > /modules.txt

FROM amazoncorretto:20-alpine as corretto-jdk

COPY --from=corretto-deps /modules.txt /modules.txt

#always include crypto modules otherwise ssl calls will fail (jdeps does not notice it presence because reflection hides it
RUN apk add --no-cache binutils && \
    jlink \
     --verbose \
     --add-modules "$(cat /modules.txt),jdk.crypto.ec,jdk.crypto.cryptoki" \
     --strip-debug \
     --no-man-pages \
     --no-header-files \
     --compress=2 \
     --output /jre

FROM alpine:latest
ENV JAVA_OPTS=${JAVA_OPTS:-""}
ENV JAVA_HOME=/jre
ENV PATH="${JAVA_HOME}/bin:${PATH}"

COPY --from=corretto-jdk /jre $JAVA_HOME

EXPOSE 8080
COPY --from=builder ./build/libs/perfjava-*-SNAPSHOT.jar /app/app.jar
WORKDIR /app

CMD java ${JAVA_OPTS} -Djava.security.egd=file:/dev/./urandom --enable-preview -jar /app/app.jar

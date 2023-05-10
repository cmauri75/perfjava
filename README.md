# Getting Started

## Core jar
```shell
./gradlew bootRun
ls -lh build/libs/*.jar | awk '{print $5, $9}'
```
Jar size is: 18 Mb

## Dockerfile
```shell
./gradlew build

docker build . -t lightjava:1.0.0 -f Dockerfile.plain
docker run -p 8081:8080 lightjava:1.0.0

docker build . -t lightjava:2.0.0 -f Dockerfile.slim
docker run -p 8082:8080 lightjava:2.0.0

docker image ls | grep light
```

Original size: 356 Mb

Slim size: 83 Mb


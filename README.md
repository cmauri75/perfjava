# Project 
This repo contains various hints and scenario to optimize spring boot java development:
1. Create a size optimized docker image
2. Usage of structured concurrency
3. Test of virtual threads in terms of performance and load management

# Getting Started

## Prerequisites
To run examples you need:
* Docker
* JDK 20 distribution

# Size-optimized Dockerization
Dockerization of images can be optimized using custom-made JDK containing only used packages.

It's an application undependent solution. Docker run exposes a port just for showing app is working fine.

## Core jar
```shell
./gradlew build
ls -lh build/libs/*.jar | awk '{print $5, $9}'
```
Jar size of opplication is: 26 Mb

## Dockerfile
```shell
./gradlew build

docker build . -t perfjava:1.0.0 -f docker/standard/Dockerfile
docker run -p 8081:8080 perfjava:1.0.0
curl http://localhost:8081 

docker build . -t perfjava:2.0.0 -f docker/slim/Dockerfile
docker run -p 8082:8080 perfjava:2.0.0
curl http://localhost:8082

docker image ls | grep perfjava
```

Results:
* Original size: 331 Mb
* Slim size: 92 Mb

NB: Use corretto for build in order to:
1. avoid problem on cloud deploy (for example if you store secrets on vault and need to inject in you application runtime)
2. use alpine machine for running, for example jre obtained from eclipse:temurin are not compatible so you need other bigger running machine to use that, debian:bookworm-slim is a candidate 

## Structured concurrency
SC has been introduced in Java19: 
>Enhance the maintainability, reliability, and observability of multithreaded code by adopting a concurrent programming style that reduces the likelihood of thread leaks and cancellation delays, which are common risks associated with cancellation and shutdown.

In example a tipical use case:
* A task can be split in 2 parts. If I do in parallel using ScheduledThreadPoolExecutor if first fail before second has finished, second finish it's work in any case (or viceversa).
* Using StructuredTaskScope.ShutdownOnFailure() as soon as first fails, second is stopped because no need to waste resources completing it. This is the case **I need both to be completed**
* Using StructuredTaskScope.ShutdownOnSuccess() as soon as first finish, second is stopped because no need to waste resources completing it. This is the case **I just need one of them to be completed, for example if I have multiple provider and ask data from all of them at once**

Example execution:
```shell
 ./gradlew compilejava
 
 java -cp ./build/classes/java/main --enable-preview --add-modules jdk.incubator.concurrent net.perfjava.virtualthreads.MainExecutor HOC
 java -cp ./build/classes/java/main --enable-preview --add-modules jdk.incubator.concurrent net.perfjava.virtualthreads.MainExecutor HOSC
```


## Virtual Threads
### Out of memory test
In this case we start infinite number of no-terminating threads and wait until system crashes.
  * Starts one thread after another, till system goes out of memory. In M1 system just over 4,000 threads are supported
  * Same as before, but virtualthread are started. Not 2,800,000 threads are started, system does not crash but slows a lot. 700x load improvement is achieved.

```shell
 ./gradlew compilejava
 
  java -cp ./build/classes/java/main --enable-preview net.perfjava.virtualthreads.MainExecutor C-T
  java -cp ./build/classes/java/main --enable-preview net.perfjava.virtualthreads.MainExecutor C-VT
```

Results:

|Technology|Max thread to crash|
|:----------------|--------------------:|
|Threads|4,000|
|Virtual Threads|2,800,000|

### Throughput test:
In this use-case we start a fixed number of threads that finishs after a while and check how much time and resources the code takes to finish all jobs.

  * Run N thread each executing same job (200ms sleep) and check spent time. 
  * Same as before with virtualthread.

Execution code (replace XXXXXX with the number of threads)
```shell
 ./gradlew compilejava
 
 time java -cp ./build/classes/java/main --enable-preview net.perfjava.virtualthreads.MainExecutor T-T XXXXXX
 time java -cp ./build/classes/java/main --enable-preview net.perfjava.virtualthreads.MainExecutor T-VT XXXXXX
```

|Technology|Num of threads call|User Time [s]|System time [s]|cpu|Total time [s]|
|:----------------|--------------------:|---------------|-----------------|------|----------------|
|Threads|1,000|0.12|0.21|96%|0.343|
|Threads|10,000|0.87|1.66|165%|1.277|
|Threads|100,000|5.48|13.82|201%|10.01|
|Threads|1,000,000|56.04|134.35|199%|1:35.92|
|Virtual Threads|1,000|0.11|0.04|46%|0.326|
|Virtual Threads|10,000|0.97|0.27|266%|0.46|
|Virtual Threads|100,000|2.77|2.8|700%|1,029|
|Virtual Threads|1,000,000|10.19|3.75|423%|3,2|

Evidence says: **As load grows VT is much faster with better CPU utilization (and much less memory needs)**

In case of huge load BT vantage is more than one order of magnitude (30x)

## Virtual Threads in rest service

See [Virtual Thread in Rest Service](README_VTRS.md)

## Bonus: AspectJ PerformanceMonitorInterceptor

See [AspectJ Monitor Interceptor](README_AJMI.md)

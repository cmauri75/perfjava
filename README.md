# Getting Started
Perfjava uses jdk20 with preview and incubator feature


## Dockerization
Dockerization of images can be optimized using custom made JDK containing only used packages

### Core jar
```shell
./gradlew build
ls -lh build/libs/*.jar | awk '{print $5, $9}'
```
Jar size of opplication is: 26 Mb

### Dockerfile
```shell
./gradlew build

docker build . -t perfjava:1.0.0 -f Dockerfile.plain
docker run -p 8081:8080 perfjava:1.0.0

docker build . -t perfjava:2.0.0 -f Dockerfile.slim
docker run -p 8082:8080 perfjava:2.0.0

docker image ls | grep perfjava
```

Results:
* Original size: 331 Mb
* Slim size: 92 Mb

NB: Use corretto for build in order to:
1. avoid problem on cloud deploy (for example if you store secrets on vault and need to inject in you application runtime)
2. use alpine machine for running, for example jre obtained from eclipse:temurin are not compatible so you need other bigger running machine to use that, debian:bookworm-slim is a candidate 

## Virtual Threads
```shell
 ./gradlew compilejava
 
 java -cp ./build/classes/java/main --enable-preview net.decathlon.VirtualThreads C-T
 java -cp ./build/classes/java/main --enable-preview net.decathlon.VirtualThreads C-VT
 time java -cp ./build/classes/java/main --enable-preview net.decathlon.VirtualThreads T-T 10000
 time java -cp ./build/classes/java/main --enable-preview net.decathlon.VirtualThreads T-VT 10000
 java -cp ./build/classes/java/main --enable-preview --add-modules jdk.incubator.concurrent net.decathlon.VirtualThreads HOC
 java -cp ./build/classes/java/main --enable-preview --add-modules jdk.incubator.concurrent net.decathlon.VirtualThreads HOSC
```

### Out of Memory Results

Go up invocking thread till system crashes

| Technology      |           Max thread to crash |
|:----------------|------------------------------:|
| Threads         |                         4.067 |
| Virtual Threads |                     2.931.139 |


### Througput load test

One of the most common concurrency use cases is serving requests over the wire using a server. 
For this, the preferred approach is the thread-per-request model, where a separate thread handles each request. 

Throughput of such systems can be explained using Little’s law, which states that in a stable system, 
the average concurrency (number of requests concurrently processed by the server), L, is equal to the throughput (average rate of requests), λ, times the latency (average duration of processing each request), W. 

With this, you can derive that throughput equals average concurrency divided by latency (λ = L/W).


| Technology      | Num of threads call | User Time [s] | System time [s] | cpu  | Total  |
|:----------------|--------------------:|---------------|-----------------|------|--------|
| Threads         |               1.000 | 0.13          | 0.13            | 78%  | 0.329  |
| Threads         |              10.000 | 0.87          | 1.66            | 198% | 1.277  |
| Threads         |             100.000 | 6.58          | 16.51s          | 216% | 10.688 |
| Threads         |           1.000.000 | 66.04         | 165.35s         | 218% | 105.92 |
| Virtual Threads         |               1.000 | 0.21          | 0.06            | 78%  | 0.347  |
| Virtual Threads         |              10.000 | 1.30          | 0.31            | 298% | 0.542  |
| Virtual Threads         |             100.000 | 5.33          | 2.66s           | 325% | 2.460  |
| Virtual Threads         |           1.000.000 | 26.09         | 19.86s          | 260% | 17.667 |


### Structured Concurrency

We want updateInventory() and updateOrder() subtasks to be executed concurrently.
Each of those can succeed or fail independently. Ideally, the handleOrder() method should fail if any subtask fails. However, if a failure occurs in one subtask, things get messy.

* If updateInventory fails, inventory.get fails so I've to rollback updateOrder if it has finished ok
* If order fast fails, inventory (if it's long running) waste lot of time, handleOrder should interrupt other job when one fails
* If handleOrder is interrupted, the event is not propagated to subtasks

handleOrderStructuredConcurrency solves isses:
* Error handling with short-circuiting — If either the updateInventory() or updateOrder() fails, the other is canceled unless its already completed. This is managed by the cancellation policy implemented by ShutdownOnFailure(); other policies are possible.
* Cancellation propagation — If the thread running handleOrder() is interrupted before or during the call to join(), both forks are canceled automatically when the thread exits the scope.
* Observability — A thread dump would clearly display the task hierarchy, with the threads running updateInventory() and updateOrder() shown as children of the scope.



# Virtual Threads in rest service use case

# Use case description
In typical use case we've a rest service that calls other low performance service, so lot of time is spent in I/O wait.

User request --> BE (ask external service to send message, locks, returns)

Lot of requests means lot of threads in I/O wait lock.

## Solution
Typical solution is to use webflux that has fantastic performances, but:
* Functional code may be difficult
* Difficult to debug
* Ugly stack trace
* Clients/libs should be async/reactive as well

## Alternative solution

Virtual Threads are threads managed by VM instead of linked to OS threads, very cheap to create and manage.

Planned to be included in September's JDK 21 and also available in the upcoming Spring Framework 6.1 (M4 now).

It would be a replacement for Spring WebFlux (the most popular efficient reactive framework) for writing high-throughput concurrent applications with minimal code changes.

We can enable it overriding default TomcatProtocolHandlerCustomizer method, changing executor.

Starting with virtualthreads
```shell
./gradlew bootrun -PjvmArgs="-Dapp.useVirtualThread=true"
```

Testing is made using autocannon, 3 case:
* /: Simple rest controller (not tested)
* /threading: a controller with I/O delay
* /fluxthreading: a reactive controller with I/O delay

```shell
npm install -g autocannon
autocannon -c 5000 -d 5 http://localhost:8080
autocannon -c 5000 -d 5 http://localhost:8080/threading
autocannon -c 5000 -d 5 http://localhost:8080/fluxthreading
```
## Results

Tables shows results, first row is standard mode, second one is virtualthread one, third is reactive standard and forth reactive with virtual thread.

<pre>
┌─────────────────┬────────┬─────────┬─────────┬─────────┬────────────┬───────────┬─────────┐
│ Stat            │ 2.5%   │ 50%     │ 97.5%   │ 99%     │ Avg        │ Stdev     │ Max     │
├─────────────────┼────────┼─────────┼─────────┼─────────┼────────────┼───────────┼─────────┤
│ Latency         │ 596 ms │ 1931 ms │ 4987 ms │ 5057 ms │ 2392.24 ms │ 1347.5 ms │ 5144 ms │
├─────────────────┼────────┼─────────┼─────────┼─────────┼────────────┼───────────┼─────────┤
│ Latency Flux    │ 925 ms │ 2842 ms │ 5043 ms │ 5136 ms │ 2877.05 ms │ 1284.5 ms │ 5184 ms │
├─────────────────┼────────┼─────────┼─────────┼─────────┼────────────┼───────────┼─────────┤
│ Latency VT      │ 529 ms │ 1078 ms │ 1951 ms │ 2107 ms │ 1099.33 ms │ 375.55 ms │ 2344 ms │
├─────────────────┼────────┼─────────┼─────────┼─────────┼────────────┼───────────┼─────────┤
│ Latency Flux VT │ 531 ms │ 1080 ms │ 1886 ms │ 2017 ms │ 1095.99 ms │ 367 ms    │ 2328 ms │
└─────────────────┴────────┴─────────┴─────────┴─────────┴────────────┴───────────┴─────────┘
┌──────────────────┬─────────┬─────────┬─────────┬─────────┬─────────┬─────────┬─────────┐
│ Stat             │ 1%      │ 2.5%    │ 50%     │ 97.5%   │ Avg     │ Stdev   │ Min     │
├──────────────────┼─────────┼─────────┼─────────┼─────────┼─────────┼─────────┼─────────┤
│ Req/Sec          │ 52      │ 52      │ 198     │ 205     │ 169.2   │ 58.9    │ 52      │
├──────────────────┼─────────┼─────────┼─────────┼─────────┼─────────┼─────────┼─────────┤
│ Req/Sec Flux     │ 95      │ 95      │ 195     │ 201     │ 175.6   │ 40.5    │ 95      │
├──────────────────┼─────────┼─────────┼─────────┼─────────┼─────────┼─────────┼─────────┤
│ Req/Sec VT       │ 836     │ 836     │ 4955    │ 5019    │ 4147.2  │ 1655.93 │ 836     │
├──────────────────┼─────────┼─────────┼─────────┼─────────┼─────────┼─────────┼─────────┤
│ Req/Sec Flux VT  │ 736     │ 736     │ 4999    │ 5083    │ 4157.61 │ 1711.21 │ 736     │
├──────────────────┼─────────┼─────────┼─────────┼─────────┼─────────┼─────────┼─────────┤
│ Bytes/Sec        │ 11.4 kB │ 11.4 kB │ 43.4 kB │ 45 kB   │ 37.1 kB │ 12.9 kB │ 11.4 kB │
├──────────────────┼─────────┼─────────┼─────────┼─────────┼─────────┼─────────┼─────────┤
│ Bytes/Sec Flux   │ 21.6 kB │ 21.6 kB │ 44.3 kB │ 45.7 kB │ 39.9 kB │ 9.2 kB  │ 21.6 kB │
├──────────────────┼─────────┼─────────┼─────────┼─────────┼─────────┼─────────┼─────────┤
│ Bytes/Sec VT     │ 194 kB  │ 194 kB  │ 1.16 MB │ 1.17 MB │ 964 kB  │ 385 kB  │ 194 kB  │
├──────────────────┼─────────┼─────────┼─────────┼─────────┼─────────┼─────────┼─────────┤
│ Bytes/Sec Flux VT│ 176 kB  │ 176 kB  │ 1.2 MB  │ 1.21 MB │ 996 kB  │ 410 kB  │ 176 kB  │
└──────────────────┴─────────┴─────────┴─────────┴─────────┴─────────┴─────────┴─────────┘
</pre>

Average request per seconds and throughput are 25x using virtual threads and latency is half.

Reactive offers better performance but not so important as virtual thread introduction does.

Results are obtained with no need of code changes.

# No I/O case

In case of no I/O improvements are present but less visible, these are results with no-reactive mode without and with virtualthreads.

<pre>
┌─────────┬───────┬───────┬────────┬─────────┬───────────┬───────────┬─────────┐
│ Stat    │ 2.5%  │ 50%   │ 97.5%  │ 99%     │ Avg       │ Stdev     │ Max     │
├─────────┼───────┼───────┼────────┼─────────┼───────────┼───────────┼─────────┤
│ Latency │ 44 ms │ 66 ms │ 353 ms │ 2278 ms │ 135.05 ms │ 401.22 ms │ 5562 ms │
├─────────┼───────┼───────┼────────┼─────────┼───────────┼───────────┼─────────┤
│ Latency │ 57 ms │ 87 ms │ 261 ms │ 1082 ms │ 130.62 ms │ 265.67 ms │ 3953 ms │
└─────────┴───────┴───────┴────────┴─────────┴───────────┴───────────┴─────────┘
┌───────────┬─────────┬─────────┬────────┬───────┬─────────┬──────────┬─────────┐
│ Stat      │ 1%      │ 2.5%    │ 50%    │ 97.5% │ Avg     │ Stdev    │ Min     │
├───────────┼─────────┼─────────┼────────┼───────┼─────────┼──────────┼─────────┤
│ Req/Sec   │ 32751   │ 32751   │ 40159  │ 78079 │ 50818   │ 17207.42 │ 32741   │
├───────────┼─────────┼─────────┼────────┼───────┼─────────┼──────────┼─────────┤
│ Req/Sec   │ 36351   │ 36351   │ 90751  │ 98303 │ 80900   │ 25913.11 │ 36327   │
├───────────┼─────────┼─────────┼────────┼───────┼─────────┼──────────┼─────────┤
│ Bytes/Sec │ 7.96 MB │ 7.96 MB │ 9.8 MB │ 19 MB │ 12.4 MB │ 4.19 MB  │ 7.96 MB │
├───────────┼─────────┼─────────┼────────┼───────┼─────────┼──────────┼─────────┤
│ Bytes/Sec │ 8.78 MB │ 8.78 MB │ 22 MB  │ 24 MB │ 19.6 MB │ 6.31 MB  │ 8.78 MB │
└───────────┴─────────┴─────────┴────────┴───────┴─────────┴──────────┴─────────┘
</pre>

# More realistic use case
In this case first microservice calls second one that via a reactive implementation returns a stream of data. Every slot is sent in a random from zero to one second delay.

Server can be run via:
```shell
docker build . -f docker/slowserver/Dockerfile -t slowserver
docker run -p:8000:8000 slowserver
```
or
```shell
./gradlew  slowserver:bootrun
```

Tests are executing via:

```shell
autocannon -c 500 -d 10 http://localhost:8080/threading2
autocannon -c 500 -d 10 http://localhost:8080/fluxthreading2
```

## Results

### Blocking, no VirtualThreads:

<pre>
┌─────────┬────────┬─────────┬─────────┬─────────┬────────────┬────────────┬─────────┐
│ Stat    │ 2.5%   │ 50%     │ 97.5%   │ 99%     │ Avg        │ Stdev      │ Max     │
├─────────┼────────┼─────────┼─────────┼─────────┼────────────┼────────────┼─────────┤
│ Latency │ 519 ms │ 4602 ms │ 8579 ms │ 8785 ms │ 4588.98 ms │ 2081.24 ms │ 9100 ms │
└─────────┴────────┴─────────┴─────────┴─────────┴────────────┴────────────┴─────────┘
┌───────────┬─────────┬─────────┬─────────┬─────────┬─────────┬─────────┬─────────┐
│ Stat      │ 1%      │ 2.5%    │ 50%     │ 97.5%   │ Avg     │ Stdev   │ Min     │
├───────────┼─────────┼─────────┼─────────┼─────────┼─────────┼─────────┼─────────┤
│ Req/Sec   │ 44      │ 44      │ 74      │ 109     │ 74.2    │ 17.7    │ 44      │
├───────────┼─────────┼─────────┼─────────┼─────────┼─────────┼─────────┼─────────┤
│ Bytes/Sec │ 10.3 kB │ 10.3 kB │ 17.4 kB │ 25.6 kB │ 17.4 kB │ 4.16 kB │ 10.3 kB │
└───────────┴─────────┴─────────┴─────────┴─────────┴─────────┴─────────┴─────────┘

Req/Bytes counts sampled once per second.
# of samples: 10

1k requests in 10.06s, 174 kB read
</pre>

### Reactive call, no VirtualThreads:
<pre>
┌─────────┬────────┬─────────┬─────────┬─────────┬────────────┬────────────┬─────────┐
│ Stat    │ 2.5%   │ 50%     │ 97.5%   │ 99%     │ Avg        │ Stdev      │ Max     │
├─────────┼────────┼─────────┼─────────┼─────────┼────────────┼────────────┼─────────┤
│ Latency │ 132 ms │ 2294 ms │ 4866 ms │ 4986 ms │ 2355.15 ms │ 1434.38 ms │ 5142 ms │
└─────────┴────────┴─────────┴─────────┴─────────┴────────────┴────────────┴─────────┘
┌───────────┬─────────┬─────────┬─────────┬─────────┬─────────┬─────────┬─────────┐
│ Stat      │ 1%      │ 2.5%    │ 50%     │ 97.5%   │ Avg     │ Stdev   │ Min     │
├───────────┼─────────┼─────────┼─────────┼─────────┼─────────┼─────────┼─────────┤
│ Req/Sec   │ 97      │ 97      │ 182     │ 228     │ 175.9   │ 38.44   │ 97      │
├───────────┼─────────┼─────────┼─────────┼─────────┼─────────┼─────────┼─────────┤
│ Bytes/Sec │ 24.6 kB │ 24.6 kB │ 46.2 kB │ 57.9 kB │ 44.7 kB │ 9.76 kB │ 24.6 kB │
└───────────┴─────────┴─────────┴─────────┴─────────┴─────────┴─────────┴─────────┘

Req/Bytes counts sampled once per second.
# of samples: 10

2k requests in 10.06s, 447 kB read
</pre>
Now improvements are visible more than double requests are made.

### Blocking, VirtualThreads enabled:

<pre>
┌─────────┬────────┬─────────┬─────────┬─────────┬────────────┬────────────┬─────────┐
│ Stat    │ 2.5%   │ 50%     │ 97.5%   │ 99%     │ Avg        │ Stdev      │ Max     │
├─────────┼────────┼─────────┼─────────┼─────────┼────────────┼────────────┼─────────┤
│ Latency │ 141 ms │ 2272 ms │ 4973 ms │ 5201 ms │ 2378.43 ms │ 1430.76 ms │ 5421 ms │
└─────────┴────────┴─────────┴─────────┴─────────┴────────────┴────────────┴─────────┘
┌───────────┬─────────┬─────────┬─────────┬─────────┬─────────┬─────────┬─────────┐
│ Stat      │ 1%      │ 2.5%    │ 50%     │ 97.5%   │ Avg     │ Stdev   │ Min     │
├───────────┼─────────┼─────────┼─────────┼─────────┼─────────┼─────────┼─────────┤
│ Req/Sec   │ 66      │ 66      │ 180     │ 215     │ 176.1   │ 43.42   │ 66      │
├───────────┼─────────┼─────────┼─────────┼─────────┼─────────┼─────────┼─────────┤
│ Bytes/Sec │ 15.5 kB │ 15.5 kB │ 42.3 kB │ 50.5 kB │ 41.4 kB │ 10.2 kB │ 15.5 kB │
└───────────┴─────────┴─────────┴─────────┴─────────┴─────────┴─────────┴─────────┘

Req/Bytes counts sampled once per second.
# of samples: 10

2k requests in 10.05s, 414 kB read
</pre>

Performances increased much, reaching flux-like one.

### Reactive call, VirtualThreads enabled:

<pre>
┌─────────┬────────┬─────────┬─────────┬─────────┬────────────┬────────────┬─────────┐
│ Stat    │ 2.5%   │ 50%     │ 97.5%   │ 99%     │ Avg        │ Stdev      │ Max     │
├─────────┼────────┼─────────┼─────────┼─────────┼────────────┼────────────┼─────────┤
│ Latency │ 133 ms │ 2135 ms │ 4909 ms │ 4986 ms │ 2272.22 ms │ 1413.18 ms │ 5179 ms │
└─────────┴────────┴─────────┴─────────┴─────────┴────────────┴────────────┴─────────┘
┌───────────┬─────────┬─────────┬─────────┬─────────┬─────────┬─────────┬─────────┐
│ Stat      │ 1%      │ 2.5%    │ 50%     │ 97.5%   │ Avg     │ Stdev   │ Min     │
├───────────┼─────────┼─────────┼─────────┼─────────┼─────────┼─────────┼─────────┤
│ Req/Sec   │ 84      │ 84      │ 195     │ 227     │ 183.9   │ 42.4    │ 84      │
├───────────┼─────────┼─────────┼─────────┼─────────┼─────────┼─────────┼─────────┤
│ Bytes/Sec │ 21.3 kB │ 21.3 kB │ 49.5 kB │ 57.7 kB │ 46.7 kB │ 10.8 kB │ 21.3 kB │
└───────────┴─────────┴─────────┴─────────┴─────────┴─────────┴─────────┴─────────┘

Req/Bytes counts sampled once per second.
# of samples: 10

2k requests in 10.06s, 467 kB read
</pre>

Flux get a little improvments, too

## Conclusion
* Virtual Threads gives an incredible boost to java applications under stress load, legacy code will reach reactive performance with minimal code changes
* Reactive programming + Virtual Threads is the fastest solution available. % of increase respect blocking code is worth in old JVM, but project loom reduces the gap up to 10%, so drawbacks of reactive code may be more important than performance gain.

# Use case

User request --> BE (ask external service to send message) - lock - return

Lot of requests, lot of threads in I/O wait lock.

Virtual Threads are threads managed by VM instead of linked to OS threads, very cheap to create and manage.

Planned to be included in September's JDK 21 and also available in the upcoming Spring Framework 6.1 (M1 now).

It would be a replacement for Spring WebFlux (the most popular efficient reactive framework) for writing high-throughput concurrent applications with minimal code changes.

WebFlux is great, the performance is fantastic, but:
* Functional code may be difficult
* Difficult to debug
* Ugly stack trace
* Clients/libs should be async/reactive as well

Testing:
```shell
npm install -g autocannon
autocannon -c 5000 -d 5 http://localhost:8080/threading
```

Standard results:
<pre>
┌─────────┬────────┬─────────┬─────────┬─────────┬────────────┬───────────┬─────────┐
│ Stat    │ 2.5%   │ 50%     │ 97.5%   │ 99%     │ Avg        │ Stdev     │ Max     │
├─────────┼────────┼─────────┼─────────┼─────────┼────────────┼───────────┼─────────┤
│ Latency │ 596 ms │ 1931 ms │ 4987 ms │ 5057 ms │ 2392.24 ms │ 1347.5 ms │ 5144 ms │
└─────────┴────────┴─────────┴─────────┴─────────┴────────────┴───────────┴─────────┘
┌───────────┬─────────┬─────────┬─────────┬───────┬─────────┬─────────┬─────────┐
│ Stat      │ 1%      │ 2.5%    │ 50%     │ 97.5% │ Avg     │ Stdev   │ Min     │
├───────────┼─────────┼─────────┼─────────┼───────┼─────────┼─────────┼─────────┤
│ Req/Sec   │ 52      │ 52      │ 198     │ 205   │ 169.2   │ 58.9    │ 52      │
├───────────┼─────────┼─────────┼─────────┼───────┼─────────┼─────────┼─────────┤
│ Bytes/Sec │ 11.4 kB │ 11.4 kB │ 43.4 kB │ 45 kB │ 37.1 kB │ 12.9 kB │ 11.4 kB │
└───────────┴─────────┴─────────┴─────────┴───────┴─────────┴─────────┴─────────┘
</pre>

Results with virtual threads:

<pre>
┌─────────┬────────┬─────────┬─────────┬─────────┬────────────┬───────────┬─────────┐
│ Stat    │ 2.5%   │ 50%     │ 97.5%   │ 99%     │ Avg        │ Stdev     │ Max     │
├─────────┼────────┼─────────┼─────────┼─────────┼────────────┼───────────┼─────────┤
│ Latency │ 529 ms │ 1078 ms │ 1951 ms │ 2107 ms │ 1099.33 ms │ 375.55 ms │ 2344 ms │
└─────────┴────────┴─────────┴─────────┴─────────┴────────────┴───────────┴─────────┘
┌───────────┬────────┬────────┬─────────┬─────────┬────────┬─────────┬────────┐
│ Stat      │ 1%     │ 2.5%   │ 50%     │ 97.5%   │ Avg    │ Stdev   │ Min    │
├───────────┼────────┼────────┼─────────┼─────────┼────────┼─────────┼────────┤
│ Req/Sec   │ 836    │ 836    │ 4955    │ 5019    │ 4147.2 │ 1655.93 │ 836    │
├───────────┼────────┼────────┼─────────┼─────────┼────────┼─────────┼────────┤
│ Bytes/Sec │ 194 kB │ 194 kB │ 1.16 MB │ 1.17 MB │ 964 kB │ 385 kB  │ 194 kB │
└───────────┴────────┴────────┴─────────┴─────────┴────────┴─────────┴────────┘
</pre>

Average requeste per seconds are 20x using virtual threads

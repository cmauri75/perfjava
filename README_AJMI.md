# AspectJ Performance Monitor Interceptor

## Use case description
In some use cases you need to check code execution performance, in order to early found bottleneck or to signal malfunctionality.

## Solution
AspectJ helps us to inject interceptor with no code changes for performance monitor or also custom monitoring, like checking when a method exceed a threeshold

* AopConfiguration.PerformanceMonitorInterceptor can set a default interceptor that traces in logs every execution time
* or you can use a customPerformanceMonitorInterceptor that is more flexible to your need

In this example I check all methods in chain and write a warn log if one is found.

NB: trace level must be configured if you want to use the provided PerformanceMonitorInterceptor, it logs traces. Be aware of flood of logs!

/* CMauri - Italy - 2023 */
package net.perfjava.aopmonitor;

import java.util.concurrent.atomic.AtomicInteger;
import org.aopalliance.intercept.MethodInvocation;
import org.apache.commons.logging.Log;
import org.springframework.aop.interceptor.AbstractMonitoringInterceptor;

public class CustomPerformanceIntercepter extends AbstractMonitoringInterceptor {

  private AtomicInteger slowTransactionCount = new AtomicInteger(0);

  public CustomPerformanceIntercepter(boolean useDynamicLogger) {
    setUseDynamicLogger(useDynamicLogger);
  }

  @Override
  protected Object invokeUnderTrace(MethodInvocation invocation, Log log) throws Throwable {
    String name = createInvocationTraceName(invocation);
    long start = System.currentTimeMillis();
    try {
      return invocation.proceed();
    } finally {
      long spent = System.currentTimeMillis() - start;
      if (spent > 400) {
        log.warn(
            String.format(
                "Method %s took %d ms to execute!, Total Slow Transaction count: %s",
                name, spent, slowTransactionCount.incrementAndGet()));
      }
    }
  }
}

/* CMauri - Italy - 2023 */
package net.perfjava.aopmonitor;

import org.aspectj.lang.annotation.Pointcut;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.aop.Advisor;
import org.springframework.aop.aspectj.AspectJExpressionPointcut;
import org.springframework.aop.support.DefaultPointcutAdvisor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.EnableAspectJAutoProxy;

@Configuration
@EnableAspectJAutoProxy
public class AopConfiguration {
  private static final Logger log = LoggerFactory.getLogger(AopConfiguration.class);

  @Pointcut("execution(* net.perfjava.aopmonitor.application..*(..))")
  public void monitorCriticalMethods() {}

  /**
   * Create a custom performance interceptor. If jsut logging execution time is enought you can just
   * use: return new PerformanceMonitorInterceptor(true); no other code is need
   *
   * @return
   */
  @Bean
  public CustomPerformanceIntercepter customPerformanceMonitorInterceptor() {
    return new CustomPerformanceIntercepter(true);
  }

  @Bean
  public Advisor performanceMonitorAdvisor() {
    log.info("performanceMonitorAdvisor configured");
    AspectJExpressionPointcut pointcut = new AspectJExpressionPointcut();
    pointcut.setExpression("net.perfjava.aopmonitor.AopConfiguration.monitorCriticalMethods()");
    return new DefaultPointcutAdvisor(pointcut, customPerformanceMonitorInterceptor());
  }
}

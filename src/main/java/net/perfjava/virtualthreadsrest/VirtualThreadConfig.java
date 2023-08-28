/* CMauri - Italy - 2023 */
package net.perfjava.virtualthreadsrest;

import java.util.concurrent.Executors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.web.embedded.tomcat.TomcatProtocolHandlerCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class VirtualThreadConfig {
  private static final Logger log = LoggerFactory.getLogger(VirtualThreadConfig.class);

  // Enables virtualthread management
  @Bean
  @ConditionalOnProperty(
      value = "app.useVirtualThread",
      havingValue = "true",
      matchIfMissing = false)
  TomcatProtocolHandlerCustomizer<?> protocolHandlerVirtualThreadExecutorCustomizer() {
    log.info("Enabling virtual threads");
    return protocolHandler ->
        protocolHandler.setExecutor(Executors.newVirtualThreadPerTaskExecutor());
  }
}

/* CMauri - Italy - 2023 */
package net.perfjava.virtualthreadsrest;

import java.util.Random;
import java.util.concurrent.TimeUnit;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class ExternalServiceGateway {
  private static final Logger log = LoggerFactory.getLogger(ExternalServiceGateway.class);
  public static final Random rand = new Random();

  public String getResponse() {
    // Call external service
    int sleepTime = rand.nextInt(1000) + 500;

    try {
      TimeUnit.MILLISECONDS.sleep(sleepTime);
    } catch (InterruptedException e) {
      log.error("Unaccepted interrupt", e);
    }
    return "Current Thread: " + Thread.currentThread();
  }
}

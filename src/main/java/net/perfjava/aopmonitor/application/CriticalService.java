/* CMauri - Italy - 2023 */
package net.perfjava.aopmonitor.application;

import java.util.concurrent.ThreadLocalRandom;
import org.springframework.stereotype.Service;

@Service
public class CriticalService {
  public int getComputeResultA() throws InterruptedException {
    int wait = ThreadLocalRandom.current().nextInt(500);
    Thread.sleep(wait);
    return wait;
  }

  public int getComputeResultB() throws InterruptedException {
    int wait = ThreadLocalRandom.current().nextInt(500);
    Thread.sleep(wait);
    return wait;
  }
}

/* Decathlon Italy - Tacos Team(C) 2023 */
package net.decathlon.service;

import java.util.Random;
import java.util.concurrent.TimeUnit;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class ExternalServiceGateway {
    private static final Logger LOGGER = LoggerFactory.getLogger(ExternalServiceGateway.class);

    public String getResponse() {
        // Call external service
        int sleepTime = new Random().nextInt(1000) + 500;

        try {
            TimeUnit.MILLISECONDS.sleep(sleepTime);
        } catch (InterruptedException e) {
            LOGGER.error(e.getMessage());
        }
        return "Current Thread: " + Thread.currentThread();
    }
}

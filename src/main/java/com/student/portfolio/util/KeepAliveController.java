package com.student.portfolio.util;

import org.springframework.context.annotation.Profile;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

@RestController
@Profile("prod")
public class KeepAliveController {

    private final RestTemplate restTemplate = new RestTemplate();

    /**
     * This endpoint is used for keeping the service alive.
     * It can be called manually or automatically by the scheduled method below.
     */
    @GetMapping("/keep-alive")
    public String keepAlive() {
        return "Service is alive at " + java.time.LocalDateTime.now();
    }

    /**
     * Scheduled task to call this service every 3 minutes.
     * Useful for deployment platforms like Render that stop the service
     * after 10 minutes of inactivity. This ensures the service stays up
     * and improves user experience.
     */
    @Scheduled(fixedRate = 180000) // 3 minutes = 180000 ms
    public void callSelf() {
        try {
            String url = "https://portfolio-backend-20s0.onrender.com/keep-alive"; // replace with public URL if deployed
            String response = restTemplate.getForObject(url, String.class);
            System.out.println("KeepAlive ping: " + response);
        } catch (Exception e) {
            System.err.println("Failed to ping self: " + e.getMessage());
        }
    }
}

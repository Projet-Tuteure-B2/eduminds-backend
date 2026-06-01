package com.eduminds.backend.controller;

import org.springframework.web.bind.annotation.*;
import java.time.LocalDateTime;
import java.util.Map;

@RestController
@RequestMapping("/api")
public class HealthController {

    @GetMapping("/health")
    public Map<String, Object> health() {
        return Map.of(
                "status",  "OK",
                "app",     "Synapz Backend",
                "version", "1.0.0",
                "time",    LocalDateTime.now().toString()
        );
    }
}

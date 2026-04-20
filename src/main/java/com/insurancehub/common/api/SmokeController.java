package com.insurancehub.common.api;

import java.util.Map;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
public class SmokeController {

    @GetMapping("/smoke")
    public ApiResponse<Map<String, String>> smoke() {
        return ApiResponse.success("Phase 0 smoke test passed", Map.of(
                "application", "Insurance Interface Hub",
                "phase", "0",
                "status", "ready"
        ));
    }
}

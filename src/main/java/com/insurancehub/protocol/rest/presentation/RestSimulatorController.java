package com.insurancehub.protocol.rest.presentation;

import java.time.LocalDateTime;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/simulator/rest")
public class RestSimulatorController {

    @PostMapping("/premium/calculate")
    public ResponseEntity<Map<String, Object>> calculatePremium(@RequestBody(required = false) String requestBody) {
        if (containsFail(requestBody)) {
            return simulatorFailure("PREMIUM_RULE_REJECTED", "Premium simulator rejected the request payload.");
        }
        return ResponseEntity.ok(Map.of(
                "status", "SUCCESS",
                "premiumAmount", 125000,
                "currency", "KRW",
                "message", "Premium calculated by local REST simulator.",
                "processedAt", LocalDateTime.now().toString()
        ));
    }

    @GetMapping("/policy/{policyNo}")
    public ResponseEntity<Map<String, Object>> getPolicy(@PathVariable String policyNo) {
        if (containsFail(policyNo)) {
            return simulatorFailure("POLICY_NOT_FOUND", "Policy simulator could not find the requested policy.");
        }
        return ResponseEntity.ok(Map.of(
                "status", "SUCCESS",
                "policyNo", policyNo,
                "policyStatus", "NORMAL",
                "holderName", "Local Demo Customer",
                "message", "Policy found by local REST simulator."
        ));
    }

    @PostMapping("/claim/register")
    public ResponseEntity<Map<String, Object>> registerClaim(@RequestBody(required = false) String requestBody) {
        if (containsFail(requestBody)) {
            return simulatorFailure("CLAIM_VALIDATION_FAILED", "Claim simulator rejected the request payload.");
        }
        return ResponseEntity.ok(Map.of(
                "status", "SUCCESS",
                "claimNo", "CLM-DEMO-20260421-001",
                "claimStatus", "REGISTERED",
                "message", "Claim registered by local REST simulator.",
                "processedAt", LocalDateTime.now().toString()
        ));
    }

    private ResponseEntity<Map<String, Object>> simulatorFailure(String code, String message) {
        return ResponseEntity.status(HttpStatus.UNPROCESSABLE_ENTITY).body(Map.of(
                "status", "FAILED",
                "code", code,
                "message", message,
                "processedAt", LocalDateTime.now().toString()
        ));
    }

    private boolean containsFail(String value) {
        return StringUtils.hasText(value) && value.toUpperCase().contains("FAIL");
    }
}

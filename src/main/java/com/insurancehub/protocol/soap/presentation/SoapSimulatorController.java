package com.insurancehub.protocol.soap.presentation;

import java.time.LocalDateTime;

import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/simulator/soap")
public class SoapSimulatorController {

    @PostMapping(
            value = "/policy-inquiry",
            consumes = {MediaType.TEXT_XML_VALUE, MediaType.APPLICATION_XML_VALUE, MediaType.ALL_VALUE},
            produces = MediaType.TEXT_XML_VALUE
    )
    public ResponseEntity<String> policyInquiry(
            @RequestBody(required = false) String requestXml,
            @RequestHeader(value = "SOAPAction", required = false) String soapAction
    ) {
        if (containsFail(requestXml)) {
            return soapFault("POLICY_INQUIRY_FAILED", "Policy inquiry simulator received FAIL.");
        }
        return ResponseEntity.ok(successEnvelope(
                "PolicyInquiryResponse",
                """
                        <pol:policyNo>POL-001</pol:policyNo>
                        <pol:policyStatus>NORMAL</pol:policyStatus>
                        <pol:message>Policy inquiry completed by local SOAP simulator.</pol:message>
                        """,
                soapAction
        ));
    }

    @PostMapping(
            value = "/claim-status",
            consumes = {MediaType.TEXT_XML_VALUE, MediaType.APPLICATION_XML_VALUE, MediaType.ALL_VALUE},
            produces = MediaType.TEXT_XML_VALUE
    )
    public ResponseEntity<String> claimStatus(@RequestBody(required = false) String requestXml) {
        if (containsFail(requestXml)) {
            return soapFault("CLAIM_STATUS_FAILED", "Claim status simulator received FAIL.");
        }
        return ResponseEntity.ok(successEnvelope(
                "ClaimStatusResponse",
                """
                        <pol:claimNo>CLM-DEMO-001</pol:claimNo>
                        <pol:claimStatus>RECEIVED</pol:claimStatus>
                        <pol:message>Claim status returned by local SOAP simulator.</pol:message>
                        """,
                "urn:ClaimStatusInquiry"
        ));
    }

    @PostMapping(
            value = "/premium-confirmation",
            consumes = {MediaType.TEXT_XML_VALUE, MediaType.APPLICATION_XML_VALUE, MediaType.ALL_VALUE},
            produces = MediaType.TEXT_XML_VALUE
    )
    public ResponseEntity<String> premiumConfirmation(@RequestBody(required = false) String requestXml) {
        if (containsFail(requestXml)) {
            return soapFault("PREMIUM_CONFIRMATION_FAILED", "Premium confirmation simulator received FAIL.");
        }
        return ResponseEntity.ok(successEnvelope(
                "PremiumConfirmationResponse",
                """
                        <pol:confirmed>true</pol:confirmed>
                        <pol:premiumAmount>125000</pol:premiumAmount>
                        <pol:currency>KRW</pol:currency>
                        """,
                "urn:PremiumConfirmation"
        ));
    }

    private ResponseEntity<String> soapFault(String code, String message) {
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .contentType(MediaType.TEXT_XML)
                .body("""
                        <soapenv:Envelope xmlns:soapenv="http://schemas.xmlsoap.org/soap/envelope/">
                          <soapenv:Body>
                            <soapenv:Fault>
                              <faultcode>%s</faultcode>
                              <faultstring>%s</faultstring>
                              <detail>
                                <processedAt>%s</processedAt>
                              </detail>
                            </soapenv:Fault>
                          </soapenv:Body>
                        </soapenv:Envelope>
                        """.formatted(code, message, LocalDateTime.now()));
    }

    private String successEnvelope(String responseName, String bodyXml, String soapAction) {
        return """
                <soapenv:Envelope xmlns:soapenv="http://schemas.xmlsoap.org/soap/envelope/" xmlns:pol="http://insurancehub.local/soap/policy">
                  <soapenv:Header/>
                  <soapenv:Body>
                    <pol:%s>
                      <pol:status>SUCCESS</pol:status>
                      <pol:soapAction>%s</pol:soapAction>
                      %s
                      <pol:processedAt>%s</pol:processedAt>
                    </pol:%s>
                  </soapenv:Body>
                </soapenv:Envelope>
                """.formatted(responseName, escapeXml(soapAction), bodyXml, LocalDateTime.now(), responseName);
    }

    private boolean containsFail(String value) {
        return StringUtils.hasText(value) && value.toUpperCase().contains("FAIL");
    }

    private String escapeXml(String value) {
        if (value == null) {
            return "";
        }
        return value
                .replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;")
                .replace("\"", "&quot;")
                .replace("'", "&apos;");
    }
}

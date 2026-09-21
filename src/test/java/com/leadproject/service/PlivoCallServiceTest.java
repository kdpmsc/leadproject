package com.leadproject.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.header;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

import java.util.Map;

import org.junit.jupiter.api.Test;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.HttpMethod;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.test.web.client.MockRestServiceServer;

class PlivoCallServiceTest {

    @Test
    void placeCallShouldPostToPlivoApi() {
        PlivoCallService service = new PlivoCallService(new RestTemplateBuilder(), "auth-id", "auth-token");
        ReflectionTestUtils.setField(service, "plivoPhoneNumber", "+971500000000");
        ReflectionTestUtils.setField(service, "appBaseUrl", "http://localhost:8080");

        MockRestServiceServer server = MockRestServiceServer.bindTo(service.getRestTemplate())
                .ignoreExpectOrder(true)
                .build();

        server.expect(requestTo("https://api.plivo.com/v1/Account/auth-id/Call/"))
                .andExpect(method(HttpMethod.POST))
                .andExpect(header("Authorization", "Basic YXV0aC1pZDphdXRoLXRva2Vu"))
                .andRespond(withSuccess("{\"request_uuid\":\"call-123\"}", org.springframework.http.MediaType.APPLICATION_JSON));

        Map<String, Object> result = service.placeCall("+971555123456", "Aisha", 42L);

        assertEquals("call-123", result.get("callId"));
        assertEquals("+971500000000", result.get("fromPhone"));
        server.verify();
    }
}

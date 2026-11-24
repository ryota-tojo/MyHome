package com.example.home_api.web;

import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockFilterChain;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import static org.assertj.core.api.Assertions.assertThat;

class ReportApiKeyFilterTest {
    private static final String VALID_KEY = "0123456789abcdef0123456789abcdef";

    @Test
    void rejectsRequestWhenApiKeyIsNotConfigured() throws Exception {
        var filter = new ReportApiKeyFilter("");
        var request = new MockHttpServletRequest("GET", "/api/reports/groups");
        var response = new MockHttpServletResponse();

        filter.doFilter(request, response, new MockFilterChain());

        assertThat(response.getStatus()).isEqualTo(503);
        assertThat(response.getContentAsString()).contains("report_api_not_configured");
    }

    @Test
    void rejectsInvalidApiKey() throws Exception {
        var filter = new ReportApiKeyFilter(VALID_KEY);
        var request = new MockHttpServletRequest("GET", "/api/reports/groups");
        request.addHeader(ReportApiKeyFilter.HEADER_NAME, "wrong-key");
        var response = new MockHttpServletResponse();

        filter.doFilter(request, response, new MockFilterChain());

        assertThat(response.getStatus()).isEqualTo(401);
        assertThat(response.getContentAsString()).contains("invalid_api_key");
    }

    @Test
    void acceptsConfiguredApiKey() throws Exception {
        var filter = new ReportApiKeyFilter(VALID_KEY);
        var request = new MockHttpServletRequest("GET", "/api/reports/groups");
        request.addHeader(ReportApiKeyFilter.HEADER_NAME, VALID_KEY);
        var response = new MockHttpServletResponse();
        var chain = new MockFilterChain();

        filter.doFilter(request, response, chain);

        assertThat(chain.getRequest()).isSameAs(request);
        assertThat(response.getStatus()).isEqualTo(200);
        assertThat(response.getHeader("Cache-Control")).isEqualTo("no-store");
    }

    @Test
    void ignoresNonReportEndpoints() throws Exception {
        var filter = new ReportApiKeyFilter(VALID_KEY);
        var request = new MockHttpServletRequest("GET", "/login");
        var response = new MockHttpServletResponse();
        var chain = new MockFilterChain();

        filter.doFilter(request, response, chain);

        assertThat(chain.getRequest()).isSameAs(request);
    }
}

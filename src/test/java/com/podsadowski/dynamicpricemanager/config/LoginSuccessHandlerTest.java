package com.podsadowski.dynamicpricemanager.config;

import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class LoginSuccessHandlerTest {

    private final LoginSuccessHandler handler = new LoginSuccessHandler();

    @Test
    void redirectsAdminToAdminPanel() throws Exception {
        HttpServletRequest request = new MockHttpServletRequest();
        MockHttpServletResponse response = new MockHttpServletResponse();
        var auth = new UsernamePasswordAuthenticationToken(
                "admin@system.com",
                "x",
                List.of(new SimpleGrantedAuthority("ROLE_ADMIN")));
        handler.onAuthenticationSuccess(request, response, auth);
        assertThat(response.getRedirectedUrl()).isEqualTo("/admin");
    }

    @Test
    void redirectsClientToClientPanel() throws Exception {
        MockHttpServletResponse response = new MockHttpServletResponse();
        var auth = new UsernamePasswordAuthenticationToken(
                "c@test.local",
                "x",
                List.of(new SimpleGrantedAuthority("ROLE_CLIENT")));
        handler.onAuthenticationSuccess(new MockHttpServletRequest(), response, auth);
        assertThat(response.getRedirectedUrl()).isEqualTo("/client");
    }
}

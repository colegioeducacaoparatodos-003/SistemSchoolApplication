package com.SistemSchool.config;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockFilterChain;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import com.SistemSchool.dto.UserDTO;
import com.SistemSchool.io.Perfil;

class AuthorizationFilterTest {

    private final AuthorizationFilter filter = new AuthorizationFilter();

    @Test
    void allowsLoginSignupAndPasswordRecoveryWithoutAuthentication() throws Exception {
        MockHttpServletResponse loginResponse = doFilter("/login.xhtml");
        MockHttpServletResponse signupResponse = doFilter("/components/dashboard/sign_in.xhtml");
        MockHttpServletResponse recoveryResponse = doFilter("/components/dashboard/recuperar_senha.xhtml");
        MockHttpServletResponse verificationResponse = doFilter("/verify-email.xhtml");
        MockHttpServletResponse protectedViewResponse = doFilter("/dashboard.xhtml");

        assertEquals(200, loginResponse.getStatus());
        assertEquals(200, signupResponse.getStatus());
        assertEquals(200, recoveryResponse.getStatus());
        assertEquals(200, verificationResponse.getStatus());
        assertEquals(302, protectedViewResponse.getStatus());
        assertEquals("/login.xhtml", protectedViewResponse.getRedirectedUrl());
    }

    @Test
    void pagesOutsidePublicRoutesRequireAuthentication() throws Exception {
        MockHttpServletResponse dashboardResponse = doFilter("/dashboard.xhtml");

        assertEquals("/login.xhtml", dashboardResponse.getRedirectedUrl());
    }

    @Test
    void allowsAuthenticatedUserToOpenProtectedView() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/dashboard.xhtml");
        request.setContextPath("");
        UserDTO.UserResponseDTO loggedUser = new UserDTO.UserResponseDTO();
        loggedUser.setPerfil(Perfil.ADMIN);
        request.getSession().setAttribute("loggedUser", loggedUser);

        MockHttpServletResponse response = new MockHttpServletResponse();
        MockFilterChain chain = new MockFilterChain();
        filter.doFilter(request, response, chain);

        assertEquals(request, chain.getRequest());
        assertFalse(response.isCommitted());
        assertEquals(200, response.getStatus());
    }

    private MockHttpServletResponse doFilter(String path) throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest("GET", path);
        request.setContextPath("");

        MockHttpServletResponse response = new MockHttpServletResponse();
        filter.doFilter(request, response, new MockFilterChain());
        return response;
    }
}

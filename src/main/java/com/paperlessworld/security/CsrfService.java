package com.paperlessworld.security;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;

import java.security.SecureRandom;
import java.util.HexFormat;

public final class CsrfService {
    public static final String CSRF_SESSION_ATTR = "CSRF_TOKEN";
    public static final String CSRF_PARAM_NAME = "_csrf";
    private static final SecureRandom SECURE_RANDOM = new SecureRandom();

    private CsrfService() {}

    public static String getOrCreateToken(HttpSession session) {
        String token = (String) session.getAttribute(CSRF_SESSION_ATTR);
        if (token == null) {
            byte[] bytes = new byte[32];
            SECURE_RANDOM.nextBytes(bytes);
            token = HexFormat.of().formatHex(bytes);
            session.setAttribute(CSRF_SESSION_ATTR, token);
        }
        return token;
    }

    public static boolean isValidToken(HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        if (session == null) {
            return false;
        }
        String sessionToken = (String) session.getAttribute(CSRF_SESSION_ATTR);
        String requestToken = request.getParameter(CSRF_PARAM_NAME);
        if (requestToken == null) {
            requestToken = request.getHeader("X-CSRF-TOKEN");
        }
        return sessionToken != null && sessionToken.equals(requestToken);
    }
}

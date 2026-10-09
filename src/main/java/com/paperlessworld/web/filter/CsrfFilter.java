package com.paperlessworld.web.filter;

import com.paperlessworld.security.CsrfService;
import jakarta.servlet.*;
import jakarta.servlet.annotation.WebFilter;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;

/**
 * Filter 3 in chain: verifies CSRF token for all state-changing POST requests (PRD Section 14.2).
 */
@WebFilter("/*")
public class CsrfFilter implements Filter {

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        HttpServletRequest req = (HttpServletRequest) request;
        HttpServletResponse resp = (HttpServletResponse) response;

        if ("POST".equalsIgnoreCase(req.getMethod())) {
            // Allow login and register POST requests without existing session CSRF token
            String path = req.getServletPath();
            if (!path.startsWith("/login") && !path.startsWith("/register")) {
                if (!CsrfService.isValidToken(req)) {
                    resp.sendError(HttpServletResponse.SC_FORBIDDEN, "Invalid or missing CSRF token");
                    return;
                }
            }
        }

        chain.doFilter(request, response);
    }
}

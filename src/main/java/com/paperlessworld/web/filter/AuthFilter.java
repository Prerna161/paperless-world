package com.paperlessworld.web.filter;

import com.paperlessworld.util.SessionUser;
import jakarta.servlet.*;
import jakarta.servlet.annotation.WebFilter;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.IOException;

/**
 * Filter 2 in chain: enforces session authentication (PRD Section 14.2).
 */
@WebFilter("/*")
public class AuthFilter implements Filter {

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        HttpServletRequest req = (HttpServletRequest) request;
        HttpServletResponse resp = (HttpServletResponse) response;

        String path = req.getServletPath();

        // Allow public pages and static assets
        if (path.startsWith("/login") || path.startsWith("/register") || 
            path.startsWith("/static/") || path.endsWith(".css") || 
            path.endsWith(".js") || path.startsWith("/WEB-INF/")) {
            chain.doFilter(request, response);
            return;
        }

        HttpSession session = req.getSession(false);
        if (session == null || session.getAttribute(SessionUser.USER_ID_ATTR) == null) {
            resp.sendRedirect(req.getContextPath() + "/login");
            return;
        }

        chain.doFilter(request, response);
    }
}

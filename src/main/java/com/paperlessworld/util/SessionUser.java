package com.paperlessworld.util;

import com.paperlessworld.exception.NotFoundException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;

public final class SessionUser {
    public static final String USER_ID_ATTR = "user_id";

    private SessionUser() {}

    public static long require(HttpServletRequest req) {
        HttpSession session = req.getSession(false);
        if (session == null || session.getAttribute(USER_ID_ATTR) == null) {
            throw new NotFoundException("User session expired or unauthorized");
        }
        return (Long) session.getAttribute(USER_ID_ATTR);
    }
}

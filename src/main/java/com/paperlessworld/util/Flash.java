package com.paperlessworld.util;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;

public final class Flash {
    public static final String FLASH_SUCCESS = "FLASH_SUCCESS";
    public static final String FLASH_ERROR = "FLASH_ERROR";

    private Flash() {}

    public static void success(HttpServletRequest req, String message) {
        HttpSession session = req.getSession(true);
        session.setAttribute(FLASH_SUCCESS, message);
    }

    public static void error(HttpServletRequest req, String message) {
        HttpSession session = req.getSession(true);
        session.setAttribute(FLASH_ERROR, message);
    }

    public static String consumeSuccess(HttpServletRequest req) {
        HttpSession session = req.getSession(false);
        if (session == null) return null;
        String msg = (String) session.getAttribute(FLASH_SUCCESS);
        session.removeAttribute(FLASH_SUCCESS);
        return msg;
    }

    public static String consumeError(HttpServletRequest req) {
        HttpSession session = req.getSession(false);
        if (session == null) return null;
        String msg = (String) session.getAttribute(FLASH_ERROR);
        session.removeAttribute(FLASH_ERROR);
        return msg;
    }
}

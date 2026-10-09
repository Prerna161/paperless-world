package com.paperlessworld.web.servlet;

import com.paperlessworld.service.resurfacing.ResurfacingEngine;
import com.paperlessworld.util.Flash;
import com.paperlessworld.util.SessionUser;
import com.paperlessworld.util.TimeSource;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.IOException;

@WebServlet("/demo/run")
public class DemoRunServlet extends HttpServlet {
    private ResurfacingEngine resurfacingEngine;

    @Override
    public void init() {
        resurfacingEngine = (ResurfacingEngine) getServletContext().getAttribute("resurfacingEngine");
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        long userId = SessionUser.require(req);
        HttpSession session = req.getSession(false);

        int offsetDays = session.getAttribute("demo_offset_days") != null 
            ? (Integer) session.getAttribute("demo_offset_days") : 0;
        TimeSource timeSource = TimeSource.withOffsetDays(offsetDays);

        int count = resurfacingEngine.runNow(userId, timeSource);
        Flash.success(req, "Resurfacing engine evaluated: " + count + " thought(s) surfaced and recorded.");

        String redirect = req.getParameter("redirect");
        resp.sendRedirect(redirect != null && !redirect.isBlank() ? redirect : req.getContextPath() + "/home");
    }
}

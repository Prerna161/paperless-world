package com.paperlessworld.web.servlet;

import com.paperlessworld.service.UserService;
import com.paperlessworld.util.Flash;
import com.paperlessworld.util.SessionUser;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.IOException;

@WebServlet("/demo/clock")
public class DemoClockServlet extends HttpServlet {
    private UserService userService;

    @Override
    public void init() {
        userService = (UserService) getServletContext().getAttribute("userService");
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        long userId = SessionUser.require(req);
        HttpSession session = req.getSession(false);

        int currentOffset = session.getAttribute("demo_offset_days") != null 
            ? (Integer) session.getAttribute("demo_offset_days") : 0;

        String action = req.getParameter("action");
        if ("reset".equalsIgnoreCase(action)) {
            currentOffset = 0;
            Flash.success(req, "Demo clock reset to current time.");
        } else {
            String daysParam = req.getParameter("days");
            int days = 15; // Default +15 days as in Section 8 wow demo
            if (daysParam != null) {
                try {
                    days = Integer.parseInt(daysParam.trim());
                } catch (NumberFormatException ignored) {}
            }
            currentOffset += days;
            Flash.success(req, "Demo clock advanced by +" + days + " days (total offset: +" + currentOffset + " days).");
        }

        session.setAttribute("demo_offset_days", currentOffset);
        userService.updateDemoOffset(userId, currentOffset);

        String redirect = req.getParameter("redirect");
        resp.sendRedirect(redirect != null && !redirect.isBlank() ? redirect : req.getContextPath() + "/home");
    }
}

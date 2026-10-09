package com.paperlessworld.web.servlet;

import com.paperlessworld.service.ThoughtService;
import com.paperlessworld.util.Flash;
import com.paperlessworld.util.SessionUser;
import com.paperlessworld.util.TimeSource;
import com.paperlessworld.util.Validators;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.IOException;

@WebServlet("/thoughts/reopen")
public class ThoughtReopenServlet extends HttpServlet {
    private ThoughtService thoughtService;

    @Override
    public void init() {
        thoughtService = (ThoughtService) getServletContext().getAttribute("thoughtService");
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        long userId = SessionUser.require(req);
        HttpSession session = req.getSession(false);
        int offsetDays = session.getAttribute("demo_offset_days") != null 
            ? (Integer) session.getAttribute("demo_offset_days") : 0;
        TimeSource timeSource = TimeSource.withOffsetDays(offsetDays);

        try {
            long id = Validators.requireLong(req.getParameter("id"));
            thoughtService.reopen(userId, id, timeSource);
            Flash.success(req, "Reopened loop into active Open set.");
        } catch (Exception e) {
            Flash.error(req, e.getMessage());
        }

        String redirect = req.getParameter("redirect");
        resp.sendRedirect(redirect != null && !redirect.isBlank() ? redirect : req.getContextPath() + "/thoughts?state=CLOSED");
    }
}

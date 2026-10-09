package com.paperlessworld.web.servlet;

import com.paperlessworld.exception.StaleThoughtException;
import com.paperlessworld.model.Thought;
import com.paperlessworld.security.CsrfService;
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

@WebServlet("/thoughts/edit")
public class ThoughtEditServlet extends HttpServlet {
    private ThoughtService thoughtService;

    @Override
    public void init() {
        thoughtService = (ThoughtService) getServletContext().getAttribute("thoughtService");
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        long userId = SessionUser.require(req);
        long thoughtId = Validators.requireLong(req.getParameter("id"));
        Thought thought = thoughtService.getThought(userId, thoughtId);

        req.setAttribute("thought", thought);
        req.setAttribute("csrfToken", CsrfService.getOrCreateToken(req.getSession(true)));
        req.setAttribute("flashError", Flash.consumeError(req));
        req.getRequestDispatcher("/WEB-INF/views/edit.jsp").forward(req, resp);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        long userId = SessionUser.require(req);
        HttpSession session = req.getSession(false);

        int offsetDays = session.getAttribute("demo_offset_days") != null 
            ? (Integer) session.getAttribute("demo_offset_days") : 0;
        TimeSource timeSource = TimeSource.withOffsetDays(offsetDays);

        long thoughtId = Validators.requireLong(req.getParameter("id"));
        long versionNo = Validators.requireLong(req.getParameter("version_no"));
        String title = Validators.requireNonBlank(req.getParameter("title"), "Title");
        String body = req.getParameter("body");
        int importance = 3;
        try {
            importance = Integer.parseInt(req.getParameter("importance"));
        } catch (Exception ignored) {}

        try {
            thoughtService.update(userId, thoughtId, title, body, importance, versionNo, timeSource);
            Flash.success(req, "Thought updated successfully.");
            resp.sendRedirect(req.getContextPath() + "/thoughts/view?id=" + thoughtId);
        } catch (StaleThoughtException e) {
            Flash.error(req, e.getMessage());
            resp.sendRedirect(req.getContextPath() + "/thoughts/edit?id=" + thoughtId);
        }
    }
}

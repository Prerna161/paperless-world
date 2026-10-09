package com.paperlessworld.web.servlet;

import com.paperlessworld.model.Thought;
import com.paperlessworld.model.ThoughtState;
import com.paperlessworld.security.CsrfService;
import com.paperlessworld.service.ThoughtService;
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
import java.util.List;

@WebServlet("/inbox")
public class InboxServlet extends HttpServlet {
    private ThoughtService thoughtService;

    @Override
    public void init() {
        thoughtService = (ThoughtService) getServletContext().getAttribute("thoughtService");
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        long userId = SessionUser.require(req);
        HttpSession session = req.getSession(false);

        int offsetDays = session.getAttribute("demo_offset_days") != null 
            ? (Integer) session.getAttribute("demo_offset_days") : 0;
        TimeSource timeSource = TimeSource.withOffsetDays(offsetDays);

        List<Thought> thoughts = thoughtService.listByState(userId, ThoughtState.INBOX);

        int inboxCount = thoughtService.countByState(userId, ThoughtState.INBOX);
        int openCount = thoughtService.countByState(userId, ThoughtState.OPEN);
        int closedCount = thoughtService.countByState(userId, ThoughtState.CLOSED);

        req.setAttribute("thoughts", thoughts);
        req.setAttribute("currentView", "INBOX");
        req.setAttribute("inboxCount", inboxCount);
        req.setAttribute("openCount", openCount);
        req.setAttribute("closedCount", closedCount);
        req.setAttribute("virtualToday", timeSource.today());
        req.setAttribute("offsetDays", offsetDays);
        req.setAttribute("csrfToken", CsrfService.getOrCreateToken(session));
        req.setAttribute("flashSuccess", Flash.consumeSuccess(req));
        req.setAttribute("flashError", Flash.consumeError(req));

        req.getRequestDispatcher("/WEB-INF/views/list.jsp").forward(req, resp);
    }
}

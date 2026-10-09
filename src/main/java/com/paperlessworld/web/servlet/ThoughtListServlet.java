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

@WebServlet("/thoughts")
public class ThoughtListServlet extends HttpServlet {
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

        String stateParam = req.getParameter("state");
        String query = req.getParameter("q");

        List<Thought> thoughts;
        String view = "OPEN";

        if (query != null && !query.isBlank()) {
            thoughts = thoughtService.search(userId, query.trim());
            view = "SEARCH";
            req.setAttribute("searchQuery", query.trim());
        } else if ("CLOSED".equalsIgnoreCase(stateParam)) {
            thoughts = thoughtService.listByState(userId, ThoughtState.CLOSED);
            view = "CLOSED";
        } else {
            thoughts = thoughtService.listByState(userId, ThoughtState.OPEN);
            view = "OPEN";
        }

        int inboxCount = thoughtService.countByState(userId, ThoughtState.INBOX);
        int openCount = thoughtService.countByState(userId, ThoughtState.OPEN);
        int closedCount = thoughtService.countByState(userId, ThoughtState.CLOSED);

        req.setAttribute("thoughts", thoughts);
        req.setAttribute("currentView", view);
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

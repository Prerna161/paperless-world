package com.paperlessworld.web.servlet;

import com.paperlessworld.dao.EventDao;
import com.paperlessworld.model.Thought;
import com.paperlessworld.model.ThoughtEvent;
import com.paperlessworld.security.CsrfService;
import com.paperlessworld.service.ThoughtService;
import com.paperlessworld.service.TransactionManager;
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
import java.sql.SQLException;
import java.util.List;

@WebServlet("/thoughts/view")
public class ThoughtViewServlet extends HttpServlet {
    private ThoughtService thoughtService;
    private EventDao eventDao;
    private TransactionManager txManager;

    @Override
    public void init() {
        thoughtService = (ThoughtService) getServletContext().getAttribute("thoughtService");
        eventDao = (EventDao) getServletContext().getAttribute("eventDao");
        txManager = (TransactionManager) getServletContext().getAttribute("txManager");
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        long userId = SessionUser.require(req);
        HttpSession session = req.getSession(false);

        int offsetDays = session.getAttribute("demo_offset_days") != null 
            ? (Integer) session.getAttribute("demo_offset_days") : 0;
        TimeSource timeSource = TimeSource.withOffsetDays(offsetDays);

        long thoughtId = Validators.requireLong(req.getParameter("id"));
        Thought thought = thoughtService.getThought(userId, thoughtId);

        List<ThoughtEvent> events = txManager.inTransaction(conn -> {
            try {
                return eventDao.findByThoughtId(conn, thoughtId);
            } catch (SQLException e) {
                throw new RuntimeException("Failed to query events", e);
            }
        });

        int inboxCount = thoughtService.countByState(userId, com.paperlessworld.model.ThoughtState.INBOX);
        int openCount = thoughtService.countByState(userId, com.paperlessworld.model.ThoughtState.OPEN);
        int closedCount = thoughtService.countByState(userId, com.paperlessworld.model.ThoughtState.CLOSED);

        req.setAttribute("thought", thought);
        req.setAttribute("events", events);
        req.setAttribute("inboxCount", inboxCount);
        req.setAttribute("openCount", openCount);
        req.setAttribute("closedCount", closedCount);
        req.setAttribute("virtualToday", timeSource.today());
        req.setAttribute("offsetDays", offsetDays);
        req.setAttribute("csrfToken", CsrfService.getOrCreateToken(session));
        req.setAttribute("flashSuccess", Flash.consumeSuccess(req));
        req.setAttribute("flashError", Flash.consumeError(req));

        req.getRequestDispatcher("/WEB-INF/views/view.jsp").forward(req, resp);
    }
}

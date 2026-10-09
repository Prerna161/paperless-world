package com.paperlessworld.web.servlet;

import com.paperlessworld.exception.InvalidClosureException;
import com.paperlessworld.exception.ValidationException;
import com.paperlessworld.model.ClosureReason;
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

@WebServlet("/thoughts/close")
public class ThoughtCloseServlet extends HttpServlet {
    private ThoughtService service;

    @Override
    public void init() {
        service = (ThoughtService) getServletContext().getAttribute("thoughtService");
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException, ServletException {
        long userId = SessionUser.require(req);
        HttpSession session = req.getSession(false);
        int offsetDays = session.getAttribute("demo_offset_days") != null 
            ? (Integer) session.getAttribute("demo_offset_days") : 0;
        TimeSource timeSource = TimeSource.withOffsetDays(offsetDays);

        String idParam = req.getParameter("id");
        try {
            long id = Validators.requireLong(idParam);
            ClosureReason reason = Validators.requireEnum(ClosureReason.class, req.getParameter("reason"));
            service.close(userId, id, reason, timeSource);
            Flash.success(req, "Thought closed with reason: " + reason);
            resp.sendRedirect(req.getContextPath() + "/home");
        } catch (ValidationException | InvalidClosureException e) {
            Flash.error(req, e.getMessage());
            resp.sendRedirect(req.getContextPath() + (idParam != null ? "/thoughts/view?id=" + idParam : "/home"));
        }
    }
}

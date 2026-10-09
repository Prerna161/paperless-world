package com.paperlessworld.web.servlet;

import com.paperlessworld.model.Thought;
import com.paperlessworld.model.ThoughtType;
import com.paperlessworld.service.ThoughtService;
import com.paperlessworld.util.*;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.IOException;

@WebServlet("/thoughts/new")
public class ThoughtCaptureServlet extends HttpServlet {
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

        String text = req.getParameter("text");
        String clientToken = req.getParameter("client_token");

        if (text == null || text.isBlank()) {
            Flash.error(req, "Thought cannot be empty");
            resp.sendRedirect(req.getContextPath() + "/home");
            return;
        }

        String trimmed = text.trim();
        String title;
        String body = null;
        if (trimmed.contains("\n")) {
            String[] lines = trimmed.split("\n", 2);
            title = lines[0].trim();
            body = lines[1].trim();
        } else {
            title = trimmed.length() > 200 ? trimmed.substring(0, 197) + "..." : trimmed;
            if (trimmed.length() > 200) {
                body = trimmed;
            }
        }

        // Suggest type via ShapeSuggester (PRD Section 10.4)
        ThoughtType suggestedType = ShapeSuggester.suggestType(trimmed);

        String typeParam = req.getParameter("type");
        if (typeParam != null && !typeParam.isBlank()) {
            try {
                suggestedType = ThoughtType.valueOf(typeParam.trim().toUpperCase());
            } catch (IllegalArgumentException ignored) {}
        }

        Thought t = thoughtService.capture(userId, title, body, suggestedType, 3, clientToken, timeSource);
        Flash.success(req, "Captured to Inbox: \"" + t.getTitle() + "\" (" + t.type() + ")");
        resp.sendRedirect(req.getContextPath() + "/inbox");
    }
}

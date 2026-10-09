package com.paperlessworld.web.servlet;

import com.paperlessworld.exception.ValidationException;
import com.paperlessworld.model.User;
import com.paperlessworld.security.CsrfService;
import com.paperlessworld.service.UserService;
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

@WebServlet("/login")
public class LoginServlet extends HttpServlet {
    private UserService userService;

    @Override
    public void init() {
        userService = (UserService) getServletContext().getAttribute("userService");
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        CsrfService.getOrCreateToken(req.getSession(true));
        req.setAttribute("flashError", Flash.consumeError(req));
        req.setAttribute("flashSuccess", Flash.consumeSuccess(req));
        req.getRequestDispatcher("/WEB-INF/views/login.jsp").forward(req, resp);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String email = req.getParameter("email");
        String password = req.getParameter("password");

        try {
            User user = userService.authenticate(email, password, TimeSource.system());

            // Invalidate old session and create new session (Section 14.4 session fixation defense)
            HttpSession oldSession = req.getSession(false);
            if (oldSession != null) oldSession.invalidate();

            HttpSession session = req.getSession(true);
            session.setAttribute(SessionUser.USER_ID_ATTR, user.getUserId());
            session.setAttribute("user_email", user.getEmail());
            session.setAttribute("display_name", user.getDisplayName());
            session.setAttribute("demo_offset_days", user.getDemoOffsetDays());
            CsrfService.getOrCreateToken(session);

            Flash.success(req, "Welcome back, " + user.getDisplayName());
            resp.sendRedirect(req.getContextPath() + "/home");
        } catch (ValidationException e) {
            Flash.error(req, e.getMessage());
            resp.sendRedirect(req.getContextPath() + "/login");
        }
    }
}

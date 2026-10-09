package com.paperlessworld.web.servlet;

import com.paperlessworld.exception.ValidationException;
import com.paperlessworld.model.User;
import com.paperlessworld.security.CsrfService;
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

@WebServlet("/register")
public class RegisterServlet extends HttpServlet {
    private UserService userService;

    @Override
    public void init() {
        userService = (UserService) getServletContext().getAttribute("userService");
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        CsrfService.getOrCreateToken(req.getSession(true));
        req.setAttribute("flashError", Flash.consumeError(req));
        req.getRequestDispatcher("/WEB-INF/views/register.jsp").forward(req, resp);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String email = req.getParameter("email");
        String displayName = req.getParameter("display_name");
        String password = req.getParameter("password");

        try {
            User user = userService.register(email, displayName, password);

            HttpSession session = req.getSession(true);
            session.setAttribute(SessionUser.USER_ID_ATTR, user.getUserId());
            session.setAttribute("user_email", user.getEmail());
            session.setAttribute("display_name", user.getDisplayName());
            session.setAttribute("demo_offset_days", 0);
            CsrfService.getOrCreateToken(session);

            Flash.success(req, "Account created successfully!");
            resp.sendRedirect(req.getContextPath() + "/home");
        } catch (ValidationException e) {
            Flash.error(req, e.getMessage());
            resp.sendRedirect(req.getContextPath() + "/register");
        }
    }
}

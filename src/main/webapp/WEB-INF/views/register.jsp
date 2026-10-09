<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <title>Register - Paperless World</title>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/static/style.css">
</head>
<body style="display:flex; align-items:center; justify-content:center; min-height:100vh;">

<div style="background:var(--bg-card); border:1px solid var(--border-color); border-radius:var(--radius-lg); padding:36px; width:100%; max-width:400px; box-shadow:var(--shadow-md);">
    <div style="text-align:center; margin-bottom:28px;">
        <div class="brand-icon" style="margin:0 auto 12px auto;">∞</div>
        <h2 style="font-size:1.5rem; font-weight:700;">Create Account</h2>
        <p style="font-size:0.85rem; color:var(--text-secondary); margin-top:4px;">Join Paperless World</p>
    </div>

    <c:if test="${not empty flashError}">
        <div class="alert-error" style="margin-bottom:18px; font-size:0.85rem;">
            <c:out value="${flashError}"/>
        </div>
    </c:if>

    <form action="${pageContext.request.contextPath}/register" method="post">
        <div style="margin-bottom:14px;">
            <label style="display:block; font-size:0.8rem; font-weight:600; color:var(--text-secondary); margin-bottom:6px;">Display Name</label>
            <input type="text" name="display_name" required placeholder="Your Name" style="width:100%; background:rgba(15,23,42,0.8); border:1px solid var(--border-color); padding:10px 14px; border-radius:var(--radius-sm); color:#fff;">
        </div>

        <div style="margin-bottom:14px;">
            <label style="display:block; font-size:0.8rem; font-weight:600; color:var(--text-secondary); margin-bottom:6px;">Email Address</label>
            <input type="email" name="email" required placeholder="you@university.edu" style="width:100%; background:rgba(15,23,42,0.8); border:1px solid var(--border-color); padding:10px 14px; border-radius:var(--radius-sm); color:#fff;">
        </div>

        <div style="margin-bottom:24px;">
            <label style="display:block; font-size:0.8rem; font-weight:600; color:var(--text-secondary); margin-bottom:6px;">Password</label>
            <input type="password" name="password" required placeholder="••••••••" style="width:100%; background:rgba(15,23,42,0.8); border:1px solid var(--border-color); padding:10px 14px; border-radius:var(--radius-sm); color:#fff;">
        </div>

        <button type="submit" class="btn-primary" style="width:100%; padding:11px;">Register</button>
    </form>

    <div style="text-align:center; margin-top:20px; font-size:0.85rem; color:var(--text-muted);">
        Already have an account? <a href="${pageContext.request.contextPath}/login" style="color:var(--accent-indigo); text-decoration:none; font-weight:600;">Sign in here</a>
    </div>
</div>

</body>
</html>

<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ include file="header.jspf" %>

<div class="section-header" style="margin-top:10px;">
    <div>
        <h2 class="section-title">📜 Lifecycle Audit Trail (Thought #${thoughtId})</h2>
        <p style="font-size:0.85rem; color:var(--text-secondary); margin-top:2px;">
            Complete state transition event log showing virtual time stamps and reasons.
        </p>
    </div>
    <a href="${pageContext.request.contextPath}/" class="btn-sm" style="color:var(--text-secondary); text-decoration:none;">← Back to Home</a>
</div>

<div style="background:var(--bg-card); border:1px solid var(--border-color); border-radius:var(--radius-md); padding:24px; margin-top:20px;">
    <c:choose>
        <c:when test="${empty events}">
            <p style="color:var(--text-muted); font-size:0.9rem;">No audit events recorded for this thought.</p>
        </c:when>
        <c:otherwise>
            <table style="width:100%; border-collapse:collapse; font-size:0.9rem;">
                <thead>
                    <tr style="border-bottom:1px solid var(--border-color); text-align:left; color:var(--text-muted); font-size:0.8rem; text-transform:uppercase;">
                        <th style="padding:10px;">Event Type</th>
                        <th style="padding:10px;">Details</th>
                        <th style="padding:10px;">Virtual Timestamp</th>
                        <th style="padding:10px;">Physical Logged At</th>
                    </tr>
                </thead>
                <tbody>
                    <c:forEach var="event" items="${events}">
                        <tr style="border-bottom:1px solid rgba(255,255,255,0.04);">
                            <td style="padding:12px 10px;">
                                <span style="font-weight:700; color:#38bdf8; background:rgba(56,189,248,0.1); padding:3px 8px; border-radius:4px; font-size:0.75rem;">
                                    ${event.eventType}
                                </span>
                            </td>
                            <td style="padding:12px 10px; color:var(--text-primary);">${event.details}</td>
                            <td style="padding:12px 10px; font-family:'JetBrains Mono',monospace; font-size:0.8rem; color:#fde68a;">
                                ${event.virtualTimestamp}
                            </td>
                            <td style="padding:12px 10px; font-family:'JetBrains Mono',monospace; font-size:0.8rem; color:var(--text-muted);">
                                ${event.createdAt}
                            </td>
                        </tr>
                    </c:forEach>
                </tbody>
            </table>
        </c:otherwise>
    </c:choose>
</div>

<%@ include file="footer.jspf" %>

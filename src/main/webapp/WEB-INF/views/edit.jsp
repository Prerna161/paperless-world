<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ include file="header.jspf" %>

<div style="max-width:680px; margin:0 auto;">
    <div style="margin-bottom:20px;">
        <a href="${pageContext.request.contextPath}/thoughts/view?id=${thought.id}" style="color:var(--text-secondary); text-decoration:none; font-size:0.9rem;">← Cancel and Return</a>
    </div>

    <div style="background:var(--bg-card); border:1px solid var(--border-color); border-radius:var(--radius-lg); padding:32px; box-shadow:var(--shadow-md);">
        <h2 style="font-size:1.4rem; font-weight:700; color:#fff; margin-bottom:6px;">Edit Thought #${thought.id}</h2>
        <p style="font-size:0.85rem; color:var(--text-secondary); margin-bottom:24px;">
            Optimistic locking active (Version #${thought.versionNo}). Concurrent modifications will be detected.
        </p>

        <form action="${pageContext.request.contextPath}/thoughts/edit" method="post">
            <input type="hidden" name="_csrf" value="${csrfToken}">
            <input type="hidden" name="id" value="${thought.id}">
            <!-- Hidden version_no for optimistic concurrency checking (PRD Section 11.2, 13.3) -->
            <input type="hidden" name="version_no" value="${thought.versionNo}">

            <div style="margin-bottom:18px;">
                <label style="display:block; font-size:0.85rem; font-weight:600; color:var(--text-secondary); margin-bottom:6px;">Title</label>
                <input type="text" name="title" required value="<c:out value="${thought.title}"/>" style="width:100%; background:rgba(15,23,42,0.8); border:1px solid var(--border-color); padding:12px; border-radius:var(--radius-sm); color:#fff; font-size:1rem;">
            </div>

            <div style="margin-bottom:18px;">
                <label style="display:block; font-size:0.85rem; font-weight:600; color:var(--text-secondary); margin-bottom:6px;">Body / Details</label>
                <textarea name="body" rows="6" style="width:100%; background:rgba(15,23,42,0.8); border:1px solid var(--border-color); padding:12px; border-radius:var(--radius-sm); color:#fff; font-size:0.95rem; line-height:1.6;"><c:out value="${thought.body}"/></textarea>
            </div>

            <div style="margin-bottom:28px;">
                <label style="display:block; font-size:0.85rem; font-weight:600; color:var(--text-secondary); margin-bottom:6px;">Importance (1 to 5)</label>
                <select name="importance" style="background:rgba(15,23,42,0.8); border:1px solid var(--border-color); padding:8px 14px; border-radius:var(--radius-sm); color:#fff;">
                    <c:forEach var="i" begin="1" end="5">
                        <option value="${i}" ${thought.importance == i ? 'selected' : ''}>${i} ${i == 5 ? '(Highest)' : (i == 1 ? '(Lowest)' : '')}</option>
                    </c:forEach>
                </select>
            </div>

            <div style="display:flex; justify-content:flex-end; gap:12px;">
                <a href="${pageContext.request.contextPath}/thoughts/view?id=${thought.id}" class="btn-sm btn-close" style="background:transparent; border:1px solid var(--border-color); color:var(--text-secondary); padding:8px 16px;">Cancel</a>
                <button type="submit" class="btn-primary" style="padding:8px 24px;">Save Changes</button>
            </div>
        </form>
    </div>
</div>

<%@ include file="footer.jspf" %>

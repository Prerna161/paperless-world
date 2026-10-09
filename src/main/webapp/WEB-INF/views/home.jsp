<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ include file="header.jspf" %>

<!-- 1. Capture Box (PRD Section 16.1) -->
<div class="capture-card">
    <form action="${pageContext.request.contextPath}/thoughts/new" method="post" style="display:flex; gap:12px; align-items:center;">
        <input type="hidden" name="_csrf" value="${csrfToken}">
        <input type="hidden" name="client_token" value="<%= java.util.UUID.randomUUID().toString() %>">
        <input type="text" name="text" required autofocus placeholder="What is on your mind?" style="flex:1; background:rgba(15,23,42,0.8); border:1px solid var(--border-color); padding:14px 18px; border-radius:var(--radius-md); font-size:1.05rem; color:#fff;">
        <button type="submit" class="btn-primary" style="padding:14px 28px; font-size:1rem;">Add</button>
    </form>
</div>

<!-- 2. BACK FOR ANOTHER LOOK (PRD Section 16.1) -->
<div class="resurface-tray">
    <div class="resurface-badge-header">BACK FOR ANOTHER LOOK</div>
    <div class="section-header">
        <div>
            <h2 class="section-title">Neglected Loops & Pending Commitments</h2>
            <p style="font-size:0.85rem; color:var(--text-secondary); margin-top:2px;">
                Deterministically resurfaced by the rule engine with stored reasons.
            </p>
        </div>
        <span class="pill-count resurfaced">${resurfaced.size()}</span>
    </div>

    <c:choose>
        <c:when test="${empty resurfaced}">
            <div style="background:rgba(255,255,255,0.02); padding:32px; border-radius:var(--radius-md); text-align:center; border:1px dashed var(--border-color);">
                <p style="color:var(--text-muted); font-size:0.95rem;">
                    No loops require review right now. Click <strong>+15 Days</strong> and <strong>Run Resurfacing Now</strong> on the top banner to test the engine.
                </p>
            </div>
        </c:when>
        <c:otherwise>
            <div class="cards-grid">
                <c:forEach var="thought" items="${resurfaced}">
                    <div class="thought-card resurfaced-highlight">
                        <div>
                            <div class="thought-top">
                                <span class="type-badge badge-${thought.type()}">${thought.type()}</span>
                                <span style="font-size:0.75rem; color:var(--text-muted);">Importance: ${thought.importance}/5</span>
                            </div>

                            <h3 class="thought-title">
                                <a href="${pageContext.request.contextPath}/thoughts/view?id=${thought.id}" style="color:#fff; text-decoration:none;">
                                    <c:out value="${thought.title}"/>
                                </a>
                            </h3>
                            <c:if test="${not empty thought.body}">
                                <p class="thought-content"><c:out value="${thought.body}"/></p>
                            </c:if>

                            <!-- Plain-text Stored Reason (PRD Section 10.3) -->
                            <div class="resurface-reason-box">
                                <strong>Why this resurfaced:</strong> <c:out value="${thought.resurfacedReason.text()}"/>
                            </div>

                            <!-- Attached Actions -->
                            <c:if test="${not empty thought.actions}">
                                <div class="actions-section">
                                    <div style="font-size:0.75rem; font-weight:700; color:var(--text-muted); margin-bottom:6px; text-transform:uppercase;">Actions</div>
                                    <c:forEach var="act" items="${thought.actions}">
                                        <div class="action-row">
                                            <span class="action-label ${act.done ? 'done' : ''}">
                                                <c:choose>
                                                    <c:when test="${act.done}">✓</c:when>
                                                    <c:otherwise>○</c:otherwise>
                                                </c:choose>
                                                <c:out value="${act.description}"/>
                                            </span>
                                            <c:if test="${not empty act.dueDate}">
                                                <span class="action-due">${act.dueDate}</span>
                                            </c:if>
                                        </div>
                                    </c:forEach>
                                </div>
                            </c:if>
                        </div>

                        <!-- Action Buttons (PRD Section 16.1: [Edit] [Add action] [Close v]) -->
                        <div class="card-footer">
                            <div style="display:flex; gap:6px; align-items:center;">
                                <a href="${pageContext.request.contextPath}/thoughts/edit?id=${thought.id}" class="btn-sm" style="background:rgba(255,255,255,0.06); color:#fff; border:1px solid var(--border-color);">Edit</a>
                                <button type="button" onclick="openActionModal(${thought.id})" class="btn-sm" style="background:rgba(255,255,255,0.06); color:#fff; border:1px solid var(--border-color);">Add action</button>
                                <button type="button" onclick="openCloseModal(${thought.id})" class="btn-sm btn-close">Close ▾</button>
                            </div>
                            <a href="${pageContext.request.contextPath}/thoughts/view?id=${thought.id}" style="color:var(--text-muted); font-size:0.75rem; text-decoration:none;">View ↗</a>
                        </div>
                    </div>
                </c:forEach>
            </div>
        </c:otherwise>
    </c:choose>
</div>

<!-- 3. RECENTLY CAPTURED (PRD Section 16.1) -->
<div style="margin-top:24px;">
    <div class="section-header">
        <div>
            <h2 class="section-title">Recently Captured (Inbox)</h2>
            <p style="font-size:0.85rem; color:var(--text-secondary); margin-top:2px;">
                Fresh captures awaiting triage into Open or deliberate Closure.
            </p>
        </div>
        <a href="${pageContext.request.contextPath}/inbox" style="color:var(--accent-indigo); text-decoration:none; font-size:0.9rem; font-weight:600;">View Inbox (${inboxCount}) →</a>
    </div>

    <c:choose>
        <c:when test="${empty recentInbox}">
            <div style="background:rgba(255,255,255,0.02); padding:20px; border-radius:var(--radius-md); border:1px dashed var(--border-color); color:var(--text-muted); font-size:0.9rem;">
                Inbox is clean. Capture any thought above!
            </div>
        </c:when>
        <c:otherwise>
            <div class="cards-grid">
                <c:forEach var="t" items="${recentInbox}">
                    <div class="thought-card">
                        <div>
                            <div class="thought-top">
                                <span class="type-badge badge-${t.type()}">${t.type()}</span>
                                <span style="font-size:0.75rem; color:var(--text-muted);">${t.state}</span>
                            </div>
                            <h3 class="thought-title"><c:out value="${t.title}"/></h3>
                            <c:if test="${not empty t.body}">
                                <p class="thought-content"><c:out value="${t.body}"/></p>
                            </c:if>
                        </div>
                        <div class="card-footer">
                            <div style="display:flex; gap:6px;">
                                <form action="${pageContext.request.contextPath}/thoughts/keep" method="post" style="display:inline; margin:0;">
                                    <input type="hidden" name="_csrf" value="${csrfToken}">
                                    <input type="hidden" name="id" value="${t.id}">
                                    <button type="submit" class="btn-sm btn-keep">Keep</button>
                                </form>
                                <button type="button" onclick="openActionModal(${t.id})" class="btn-sm" style="background:rgba(255,255,255,0.06); color:#fff; border:1px solid var(--border-color);">Add action</button>
                                <button type="button" onclick="openCloseModal(${t.id})" class="btn-sm btn-close">Close ▾</button>
                            </div>
                        </div>
                    </div>
                </c:forEach>
            </div>
        </c:otherwise>
    </c:choose>
</div>

<%@ include file="footer.jspf" %>

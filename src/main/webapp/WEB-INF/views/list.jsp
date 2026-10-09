<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ include file="header.jspf" %>

<div class="section-header" style="margin-top:10px;">
    <div>
        <h2 class="section-title">
            <c:choose>
                <c:when test="${currentView == 'INBOX'}">📥 Inbox (Triage)</c:when>
                <c:when test="${currentView == 'OPEN'}">🔄 Open Loops</c:when>
                <c:when test="${currentView == 'CLOSED'}">📦 Closed Archive</c:when>
                <c:when test="${currentView == 'SEARCH'}">🔍 Search Results for "<c:out value="${searchQuery}"/>"</c:when>
            </c:choose>
        </h2>
        <p style="font-size:0.85rem; color:var(--text-secondary); margin-top:2px;">
            <c:choose>
                <c:when test="${currentView == 'INBOX'}">Unsorted captures. Triage each item: Keep in Open, Attach an Action, or Close with a Reason.</c:when>
                <c:when test="${currentView == 'OPEN'}">Thoughts currently open. Touching them or adding actions resets their cooling period.</c:when>
                <c:when test="${currentView == 'CLOSED'}">Deliberately closed thoughts. Kept out of active views; can be reopened anytime.</c:when>
                <c:when test="${currentView == 'SEARCH'}">Full-text query matches across titles and body contents.</c:when>
            </c:choose>
        </p>
    </div>
    <span class="pill-count">${thoughts.size()} Items</span>
</div>

<c:choose>
    <c:when test="${empty thoughts}">
        <div style="background:rgba(255,255,255,0.02); padding:40px; border-radius:var(--radius-md); text-align:center; border:1px dashed var(--border-color); margin-top:20px;">
            <p style="color:var(--text-muted); font-size:1rem;">No thoughts currently in ${currentView}.</p>
        </div>
    </c:when>
    <c:otherwise>
        <div class="cards-grid" style="margin-top:20px;">
            <c:forEach var="thought" items="${thoughts}">
                <div class="thought-card">
                    <div>
                        <div class="thought-top">
                            <span class="type-badge badge-${thought.type()}">${thought.type()}</span>
                            <c:if test="${thought.state == 'CLOSED'}">
                                <span style="font-size:0.75rem; font-weight:700; color:#cbd5e1; background:rgba(255,255,255,0.1); padding:2px 6px; border-radius:4px;">
                                    ${thought.closureReason}
                                </span>
                            </c:if>
                            <span style="font-size:0.75rem; color:var(--text-muted); margin-left:auto;">Imp: ${thought.importance}/5</span>
                        </div>

                        <h3 class="thought-title">
                            <a href="${pageContext.request.contextPath}/thoughts/view?id=${thought.id}" style="color:#fff; text-decoration:none;">
                                <c:out value="${thought.title}"/>
                            </a>
                        </h3>
                        <c:if test="${not empty thought.body}">
                            <p class="thought-content"><c:out value="${thought.body}"/></p>
                        </c:if>

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

                    <!-- Journey Buttons (PRD Section 7 Journey 2 and Journey 4) -->
                    <div class="card-footer">
                        <div style="display:flex; gap:6px; align-items:center;">
                            <c:if test="${thought.state == 'INBOX'}">
                                <form action="${pageContext.request.contextPath}/thoughts/keep" method="post" style="display:inline; margin:0;">
                                    <input type="hidden" name="_csrf" value="${csrfToken}">
                                    <input type="hidden" name="id" value="${thought.id}">
                                    <input type="hidden" name="redirect" value="${pageContext.request.requestURI}">
                                    <button type="submit" class="btn-sm btn-keep">Keep</button>
                                </form>
                            </c:if>

                            <c:if test="${thought.state != 'CLOSED'}">
                                <button type="button" onclick="openActionModal(${thought.id})" class="btn-sm" style="background:rgba(255,255,255,0.06); color:#fff; border:1px solid var(--border-color);">Add action</button>
                                <button type="button" onclick="openCloseModal(${thought.id})" class="btn-sm btn-close">Close ▾</button>
                            </c:if>

                            <c:if test="${thought.state == 'CLOSED'}">
                                <form action="${pageContext.request.contextPath}/thoughts/reopen" method="post" style="display:inline; margin:0;">
                                    <input type="hidden" name="_csrf" value="${csrfToken}">
                                    <input type="hidden" name="id" value="${thought.id}">
                                    <input type="hidden" name="redirect" value="${pageContext.request.requestURI}">
                                    <button type="submit" class="btn-sm btn-reopen">Reopen Loop</button>
                                </form>
                            </c:if>
                        </div>
                        <a href="${pageContext.request.contextPath}/thoughts/view?id=${thought.id}" style="color:var(--text-muted); font-size:0.75rem; text-decoration:none;">View ↗</a>
                    </div>
                </div>
            </c:forEach>
        </div>
    </c:otherwise>
</c:choose>

<%@ include file="footer.jspf" %>

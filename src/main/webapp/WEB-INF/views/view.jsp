<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ include file="header.jspf" %>

<div style="max-width:800px; margin:0 auto;">
    <div style="display:flex; justify-content:space-between; align-items:center; margin-bottom:20px;">
        <a href="${pageContext.request.contextPath}/home" style="color:var(--text-secondary); text-decoration:none; font-size:0.9rem;">← Back to Home</a>
        <div style="display:flex; gap:8px;">
            <a href="${pageContext.request.contextPath}/thoughts/edit?id=${thought.id}" class="btn-sm" style="background:rgba(255,255,255,0.08); color:#fff; border:1px solid var(--border-color);">Edit Thought</a>
            <c:if test="${thought.state != 'CLOSED'}">
                <button type="button" onclick="openCloseModal(${thought.id})" class="btn-sm btn-close">Close Loop ▾</button>
            </c:if>
            <c:if test="${thought.state == 'CLOSED'}">
                <form action="${pageContext.request.contextPath}/thoughts/reopen" method="post" style="display:inline; margin:0;">
                    <input type="hidden" name="_csrf" value="${csrfToken}">
                    <input type="hidden" name="id" value="${thought.id}">
                    <button type="submit" class="btn-sm btn-reopen">Reopen Loop</button>
                </form>
            </c:if>
        </div>
    </div>

    <!-- Main Detail Card -->
    <div style="background:var(--bg-card); border:1px solid var(--border-color); border-radius:var(--radius-lg); padding:32px; box-shadow:var(--shadow-md); margin-bottom:32px;">
        <div style="display:flex; gap:10px; align-items:center; margin-bottom:14px;">
            <span class="type-badge badge-${thought.type()}">${thought.type()}</span>
            <span style="font-size:0.8rem; font-weight:700; color:#38bdf8; background:rgba(56,189,248,0.1); padding:2px 8px; border-radius:4px;">${thought.state}</span>
            <c:if test="${thought.state == 'CLOSED'}">
                <span style="font-size:0.8rem; font-weight:700; color:#cbd5e1; background:rgba(255,255,255,0.1); padding:2px 8px; border-radius:4px;">${thought.closureReason}</span>
            </c:if>
            <span style="font-size:0.8rem; color:var(--text-muted); margin-left:auto;">Version: #${thought.versionNo}</span>
        </div>

        <h1 style="font-size:1.7rem; font-weight:700; margin-bottom:14px; color:#fff;"><c:out value="${thought.title}"/></h1>

        <c:if test="${not empty thought.body}">
            <div style="color:var(--text-secondary); line-height:1.7; white-space:pre-wrap; margin-bottom:24px; font-size:1rem;">
                <c:out value="${thought.body}"/>
            </div>
        </c:if>

        <div style="font-size:0.8rem; color:var(--text-muted); border-top:1px solid var(--border-color); padding-top:14px; display:flex; gap:20px;">
            <span>Cooling Threshold: ${thought.coolingThreshold().toDays()} days</span>
            <span>Created: ${thought.createdAt}</span>
            <span>Last Touched: ${thought.lastTouchedAt}</span>
        </div>
    </div>

    <!-- Attached Actions -->
    <div style="background:var(--bg-card); border:1px solid var(--border-color); border-radius:var(--radius-md); padding:24px; margin-bottom:32px;">
        <div style="display:flex; justify-content:space-between; align-items:center; margin-bottom:16px;">
            <h3 style="font-size:1.1rem; color:#fff;">Actions & Commitments</h3>
            <button type="button" onclick="openActionModal(${thought.id})" class="btn-sm" style="background:rgba(255,255,255,0.06); color:#fff; border:1px solid var(--border-color);">+ Add Action</button>
        </div>

        <c:choose>
            <c:when test="${empty thought.actions}">
                <p style="color:var(--text-muted); font-size:0.85rem;">No actions attached yet.</p>
            </c:when>
            <c:otherwise>
                <div style="display:flex; flex-direction:column; gap:8px;">
                    <c:forEach var="act" items="${thought.actions}">
                        <div style="display:flex; justify-content:space-between; align-items:center; background:rgba(15,23,42,0.4); padding:10px 14px; border-radius:var(--radius-sm); border:1px solid var(--border-color);">
                            <span class="${act.done ? 'action-label done' : 'action-label'}">
                                <c:choose>
                                    <c:when test="${act.done}">✓</c:when>
                                    <c:otherwise>○</c:otherwise>
                                </c:choose>
                                <c:out value="${act.description}"/>
                            </span>
                            <div style="display:flex; align-items:center; gap:12px;">
                                <c:if test="${not empty act.dueDate}">
                                    <span class="action-due">${act.dueDate}</span>
                                </c:if>
                                <c:if test="${!act.done}">
                                    <form action="${pageContext.request.contextPath}/actions/done" method="post" style="display:inline; margin:0;">
                                        <input type="hidden" name="_csrf" value="${csrfToken}">
                                        <input type="hidden" name="action_id" value="${act.actionId}">
                                        <input type="hidden" name="thought_id" value="${thought.id}">
                                        <input type="hidden" name="redirect" value="${pageContext.request.requestURI}?id=${thought.id}">
                                        <button type="submit" class="btn-sm btn-reopen" style="font-size:0.75rem;">Done</button>
                                    </form>
                                </c:if>
                            </div>
                        </div>
                    </c:forEach>
                </div>
            </c:otherwise>
        </c:choose>
    </div>

    <!-- Event Audit History -->
    <div style="background:var(--bg-card); border:1px solid var(--border-color); border-radius:var(--radius-md); padding:24px;">
        <h3 style="font-size:1.1rem; color:#fff; margin-bottom:16px;">📜 Lifecycle Event History</h3>
        <c:choose>
            <c:when test="${empty events}">
                <p style="color:var(--text-muted); font-size:0.85rem;">No events recorded.</p>
            </c:when>
            <c:otherwise>
                <table style="width:100%; border-collapse:collapse; font-size:0.85rem;">
                    <thead>
                        <tr style="border-bottom:1px solid var(--border-color); text-align:left; color:var(--text-muted);">
                            <th style="padding:8px;">Event</th>
                            <th style="padding:8px;">Details</th>
                            <th style="padding:8px;">Timestamp</th>
                        </tr>
                    </thead>
                    <tbody>
                        <c:forEach var="ev" items="${events}">
                            <tr style="border-bottom:1px solid rgba(255,255,255,0.04);">
                                <td style="padding:10px 8px; font-weight:700; color:#38bdf8;">${ev.eventType}</td>
                                <td style="padding:10px 8px;"><c:out value="${ev.details}"/></td>
                                <td style="padding:10px 8px; font-family:'JetBrains Mono',monospace; color:var(--text-muted);">${ev.virtualTimestamp}</td>
                            </tr>
                        </c:forEach>
                    </tbody>
                </table>
            </c:otherwise>
        </c:choose>
    </div>
</div>

<%@ include file="footer.jspf" %>

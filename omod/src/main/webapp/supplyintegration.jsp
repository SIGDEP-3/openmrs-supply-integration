<%@ include file="/WEB-INF/template/include.jsp"%>

<%@ include file="/WEB-INF/template/header.jsp"%>

<h2><spring:message code="supplyintegration.title" /></h2>

<br/>
<h3><spring:message code="supplyintegration.formTitle" /></h3>
<table>
  <tr>
   <th>User Id</th>
   <th>Username</th>
  </tr>
  <c:forEach var="user" items="${users}">
      <tr>
        <td>${user.userId}</td>
        <td>${user.systemId}</td>
      </tr>
  </c:forEach>
</table>

<%@ include file="/WEB-INF/template/footer.jsp"%>

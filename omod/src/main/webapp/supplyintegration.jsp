<%@ include file="/WEB-INF/template/include.jsp"%>

<%@ include file="/WEB-INF/template/header.jsp"%>

<h2><spring:message code="supplyintegration.title" /></h2>

<br/>
<h3><spring:message code="supplyintegration.formTitle" /></h3>
<br/>
<div style="background-color:  ${connexionStatus ? 'darkgreen' : 'darkred'}; color: azure; padding: 5px">
    <center>${connexionMessage}</center>
</div>
<br/>
<form:form modelAttribute="connexionForm" method="post" action="" id="form">
<table>
  <tr>
   <td><label for="url">URL : </label></td>
   <td>
       <form:input path="url" cssClass="form-control form-control-sm picker" size="100" id="url" />
       <form:errors path="url" cssClass="error"/>
<%--       <input id="url" required type="url" name="url" size="100" placeholder="https://" value="${connectionForm.url}" />--%>
   </td>
  </tr>
  <tr>
   <td><label for="username">Nom d'utilisateur : </label></td>
   <td>
       <form:input path="username" cssClass="form-control form-control-sm picker" size="40" id="username" />
       <form:errors path="username" cssClass="error"/>
<%--       <input id="username" required type="text" name="username" size="40" placeholder="Nom utilisateur" value="${connectionForm.username}"  />--%>
   </td>
  </tr>
  <tr>
   <td><label for="password">Mot de passe : </label></td>
   <td>
       <form:password path="password" cssClass="form-control form-control-sm picker" size="40" id="password" />
       <form:errors path="password" cssClass="error"/>
<%--       <input id="password" required type="password" name="password" placeholder="Mot de passe" size="40" value="${connectionForm.password}"  />--%>
   </td>
  </tr>
    <tr><td colspan="2">&nbsp;</td></tr>
    <tr>
        <td colspan="2">
            <button type="submit" name="">Enregistrer</button>
        </td>
    </tr>
</table>
</form:form>

<%@ include file="/WEB-INF/template/footer.jsp"%>

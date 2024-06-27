<%@ include file="/WEB-INF/template/include.jsp"%>

<%@ include file="/WEB-INF/template/header.jsp"%>

<h2><spring:message code="supplyintegration.title" /></h2>

<br/>
<h3><spring:message code="supplyintegration.formTitle" /></h3>
<br/>
<form method="post" action="">
<table>
  <tr>
   <td><label for="url">URL : </label></td>
   <td><input id="url" required type="url" name="url" size="100" placeholder="https://" /></td>
  </tr>
  <tr>
   <td><label for="username">Nom d'utilisateur : </label></td>
   <td><input id="username" required type="text" name="username" size="40" placeholder="Nom utilisateur" /></td>
  </tr>
  <tr>
   <td><label for="password">Mot de passe : </label></td>
   <td><input id="password" required type="password" name="password" placeholder="Mot de passe" size="40" /></td>
  </tr>
    <tr><td colspan="2">&nbsp;</td></tr>
    <tr>
        <td colspan="2">
            <button type="submit" name="">Submit</button>
        </td>
    </tr>

</table>
</form>

<%@ include file="/WEB-INF/template/footer.jsp"%>

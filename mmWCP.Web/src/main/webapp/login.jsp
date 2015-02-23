<%@ page language="java" contentType="text/html; charset=ISO-8859-1"
    pageEncoding="ISO-8859-1"%>

<%@page import="javax.servlet.http.*" %>
    
<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN">
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=ISO-8859-1">
<title>Login</title>
</head>
<body>
<%
	wiki.WikiContext wCtx= wiki.WikiFilter.wikiContext();
	String username=request.getParameter("username");
	try {
	if ( username != null) {
		if (wiki.WikiMeta.checkUsernameForLogin(username)) {
			Cookie userCookie= new Cookie("username",username);
			response.addCookie(userCookie);
			//wCtx.setUser(username);
		}
	}
	} catch(java.sql.SQLException ex) {throw new wiki.WikiException(ex);}
%>

	<form name=login action="login.jsp" method=post>
	<table width=80%>
		<tr>
		<td align="right" > Your wiki name: </td>
		<td align=left width=50% ><%="<input type=\"text\" name=\"username\" maxlength=\"100\" value=\""+ (wCtx.user() != null ? wCtx.user() : "")+ "\" />" %></td>
		<td align="left" > <input type="submit" value="submit" name="submit" /> </td>
		</tr>
		</table>
	</form>

</body>
</html>
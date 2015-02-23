<%@ page language="java" contentType="text/html; charset=ISO-8859-1"
    pageEncoding="ISO-8859-1"%>
<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN">
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=ISO-8859-1">
<%
	wiki.WikiContext wCtx= wiki.WikiFilter.wikiContext();
	String teamName= wCtx.team();
	sexpr.SExpr teamList= wiki.WikiMeta.teamMembership(teamName);;
	if (request.getParameter("submit") != null) {  //process the submit form
		sexpr.SExpr new_members= new sexpr.RRepresentation().readFrom( request.getParameter("team_membership"));
		wiki.WikiMeta.processTeamUpdate(wCtx.user(),teamName,teamList,new_members);
		teamList =  new_members;
	} 
%>

<title>Team <%= teamName %></title>
</head>
<body>

	<form name=login action="team.jsp" method=post>
	<table width=80%>
		<tr>
		<td align="left" > Team: <b><%=teamName %></b> </td>
		</tr>  
		<tr>
		<td align=right width=80% >
		<textarea name="team_membership" rows=20 cols=78 wrap=virtual ><%=
			teamList.toString()
		%></textarea>
		</td>
		<tr>
		</tr>
		<tr>
		<td align="left" > 
			<input type="submit" value="submit" name="submit" /> 
			<%= "<input type=hidden value=" + teamName+" name='team' />\n" %> 
			
		</td>
		</tr>
	</table>
	</form>

</body>
</html>
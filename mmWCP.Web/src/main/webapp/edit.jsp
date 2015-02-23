<%@page import="java.util.function.Function"%>
<%@page import="me.mywiki.j2ee.J2EEWebInit"%>
<%@page import="me.mywiki.WikiApp"%>
<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"
    import="wiki.*"
    import="home.costin.util.*" 
%>
<!doctype html>

<%!
	WikiApp theApp;
	
	public void jspInit() {
    	this.theApp= J2EEWebInit.theApp(this.getServletContext());
	}
%>

<%
	
	WikiContext wCtx = WikiFilter.wikiContext();
	String pageName= wCtx.page();
	Integer version= WikiMeta.versionForEdit(wCtx.page(),wCtx.user(),wCtx.team());
	ByteSource bs= null;
	if (version != null) { 
		bs=theApp.wikiStorage().open(pageName,"src",wCtx.team()+"."+version);
	}
%>
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=ISO-8859-1">
<title>Edit <%=pageName%>></title>

</head>
<body>
<h1><%=pageName%> </h1>
<!--  p -->
<form action="save" method=post>
	<table width=80%>
		<tr>
		<td align=right width=100% >
		<textarea name="textSource" rows=20 cols=78 wrap=virtual ><%
			if (bs !=null) {
				bs.transferAsASCII(new wiki.HtmlEscapedWriter(out));
			}
		%></textarea> 
		</td>
	<tr>
		<td align=right>	
		<input type=submit name="Save" value="Save" label="Save">
		<%="<input type=hidden name=page value="+sexpr.SUtils.escapeString(wCtx.page())+ " >"%>
		<%="<input type=hidden name=team value=\""+ wCtx.team()+ "\" >"%>
		<%="<input type=hidden name=version value=\""+ version + "\" >"%>
		</td>
	</tr>
	</table>
</form>
<!--  /p -->
</body>
</html>
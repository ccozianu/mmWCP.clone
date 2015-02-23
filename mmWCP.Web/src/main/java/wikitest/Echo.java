package wikitest;

import home.costin.util.StringConstructor;

import java.io.IOException;
import java.util.Enumeration;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import sexpr.SUtils;


import wiki.DefaultHtmlRenderer;

public class Echo extends HttpServlet {
	
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		process(request,response);
	}  	
	

	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		process(request,response);
	}  
	
	private void process(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		StringConstructor output=new StringConstructor();
		output.append("`( h 1 ").append(SUtils.escapeString("Echo World")).append(')');
		output.append("`hr ");
		output.append("`(h 2 ").append(SUtils.escapeString("Headers")).append(')').append('\n');
		Enumeration e= request.getHeaderNames();
		while (e.hasMoreElements()) {
			String name = (String) e.nextElement();
			output.append(name).append(" = ").append(request.getHeader(name));
			output.append("\n");
		}
		output.append("`hr ");
		output.append("`(h 2 ").append(SUtils.escapeString("Parameters")).append(')').append('\n');
		Enumeration e1= request.getParameterNames();
		while (e1.hasMoreElements()) {
			String name = (String) e1.nextElement();
			output.append(name).append(" = ").append(request.getParameter(name));
			output.append("\n");
		}
		output.append("`hr ");
		output.append("Test ''link'' to self `(a Echo?x=1 \"Echo Again\" )");

		DefaultHtmlRenderer.render("Echo Servlet",output.toString(),response.getOutputStream());
	}	

}

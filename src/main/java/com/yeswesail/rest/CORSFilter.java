package com.yeswesail.rest;

import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;

import java.io.IOException;

public class CORSFilter implements Filter {
	private final Logger log = (Logger) Logger.getLogger(getClass());

	@Override
	public void init(FilterConfig filterConfig) throws ServletException 
	{
		;
	}

	public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain chain) 
			throws IOException, ServletException 
	{
		HttpServletRequest request = (HttpServletRequest) servletRequest;
		log.debug("Request: " + request.getMethod());

		HttpServletResponse resp = (HttpServletResponse) servletResponse;
		resp.addHeader("Access-Control-Allow-Origin","*");
		resp.addHeader("Access-Control-Allow-Methods","GET,POST");
		resp.addHeader("Access-Control-Allow-Headers","Origin, X-Requested-With, Content-Type, Accept");

		// Just ACCEPT and REPLY OK if OPTIONS
		if ( request.getMethod().equals("OPTIONS") ) {
			resp.setStatus(HttpServletResponse.SC_OK);
			return;
		}
		chain.doFilter(request, servletResponse);
	}

	@Override
	public void destroy() 
	{
		;
	}
}
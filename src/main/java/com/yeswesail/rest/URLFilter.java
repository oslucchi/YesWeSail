package com.yeswesail.rest;

import java.io.IOException;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;

import org.apache.log4j.Logger;

public class URLFilter implements Filter{
	private final Logger log = (Logger) Logger.getLogger(getClass());
	private final ApplicationProperties prop = ApplicationProperties.getInstance();
	@Override
	public void init(FilterConfig filterConfig) throws ServletException 
	{
		;
	}

	public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain chain) 
			throws IOException, ServletException 
	{
		String path = ((HttpServletRequest) servletRequest).getServletPath();
		if (path.startsWith("/rest"))
		{
			log.debug("trapped " + path + " call from " + 
					 ((HttpServletRequest) servletRequest).getRemoteAddr() + ", forward it to proper servlet");
			servletRequest.getRequestDispatcher("/restcall" + path.substring(5))
					.forward(servletRequest, servletResponse);
			return;
		}
		else
		{
			for(String checkPath : prop.getURLFilterFiles())
			{
				if (path.equals(checkPath))
				{
					chain.doFilter(servletRequest, servletResponse);
					return;
				}
			}
			for(String checkPath : prop.getURLFilterFolders())
			{
				if (path.startsWith(checkPath))
				{
					chain.doFilter(servletRequest, servletResponse);
					return;
				}
			}
		}		
		log.debug("request " + path + " from " + 
				  ((HttpServletRequest) servletRequest).getRemoteAddr() + " must be handled. Forwarding it to index.html");
		servletRequest.getRequestDispatcher("/index.html").forward(servletRequest, servletResponse);
	}

	@Override
	public void destroy() 
	{
		;
	}

}

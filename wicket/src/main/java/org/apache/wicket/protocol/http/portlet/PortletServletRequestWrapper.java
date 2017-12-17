/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.wicket.protocol.http.portlet;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.UnsupportedEncodingException;
import java.util.*;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;
import javax.servlet.http.HttpSession;

/**
 * Wraps servlet request object with Portlet specific functionality by overriding the
 * {@link HttpServletRequestWrapper} retrieval of the context path, path info, request URI etc... to
 * return the portal specific translations.
 * 
 * FIXME javadoc
 * 
 * @author Ate Douma
 */
public class PortletServletRequestWrapper extends HttpServletRequestWrapper
{
	private static final Logger log = LoggerFactory.getLogger(PortletServletRequestWrapper.class);
	/**
	 * Context path.
	 */
	private String contextPath;
	/**
	 * Servlet path.
	 */
	private final String servletPath;
	/**
	 * Path info - the url relative to the context and filter path.
	 */
	private String pathInfo;
	/**
	 * Request URI.
	 */
	private String requestURI;
	/**
	 * Query string.
	 */
	private String queryString;
	/**
	 * HTTP session.
	 */
	private HttpSession session;

	private Map parameterMap;

	/**
	 * Converts from a filterPath (path with a trailing slash), to a servletPath (path with a
	 * leading slash).
	 * 
	 * @param filterPath
	 * @return the filterPath prefixed with a leading slash and with the trailing slash removed
	 */
	private static String makeServletPath(String filterPath)
	{
		return "/" + filterPath.substring(0, filterPath.length() - 1);
	}

	/**
	 * Package private constructor which is called from either of the two public constructors - sets
	 * up the various portlet specific versions of the context path, servlet path, request URI
	 * etc...
	 * 
	 * @param context
	 * @param proxiedSession
	 * @param request
	 * @param filterPath
	 */
	protected PortletServletRequestWrapper(ServletContext context, HttpSession proxiedSession,
		HttpServletRequest request, String filterPath)
	{
		super(request);
		session = proxiedSession;
		if (proxiedSession == null)
		{
			session = request.getSession(false);
		}

		servletPath = makeServletPath(filterPath);
		// retrieve the correct contextPath, requestURI and queryString
		// if request is an include
		if ((contextPath = (String)request.getAttribute("javax.servlet.include.context_path")) != null)
		{
			requestURI = (String)request.getAttribute("javax.servlet.include.request_uri");
			queryString = (String)request.getAttribute("javax.servlet.include.query_string");
			// Arena 4.0
			fixContextPath();
			fixParameterMap(request);
		}
		// else if request is a forward
		else if ((contextPath = (String)request.getAttribute("javax.servlet.forward.context_path")) != null)
		{
			requestURI = (String)request.getAttribute("javax.servlet.forward.request_uri");
			queryString = (String)request.getAttribute("javax.servlet.forward.query_string");
			// Arena 4.0
			fixContextPath();
			fixParameterMap(request);
		}
		// else it is a normal request
		else
		{
			contextPath = super.getContextPath();
			requestURI = super.getRequestURI();
			queryString = super.getQueryString();
			parameterMap = super.getParameterMap();
		}
	}

	private static HttpServletRequest getOriginalRequest(final HttpServletRequest request) {
		HttpServletRequest originalRequest = request;
		while (originalRequest instanceof HttpServletRequestWrapper && ((HttpServletRequestWrapper) originalRequest).getRequest() != null) {
			originalRequest = (HttpServletRequest) ((HttpServletRequestWrapper) originalRequest).getRequest();
		}
		return originalRequest;
	}

	// Arena 4.0
	private void fixContextPath() {
		if (contextPath != null && requestURI != null) {
			int idx = requestURI.indexOf(contextPath);
			if (idx > 0) {
				contextPath = requestURI.substring(0, idx) + contextPath;
			}
		}
	}

	private static boolean isForm(final HttpServletRequest request) {
		return "post".equalsIgnoreCase(request.getMethod()) && "application/x-www-form-urlencoded".equalsIgnoreCase(request.getContentType());
	}

	private void fixParameterMap(final HttpServletRequest request) {
		if (isForm(request)) {
			final HttpServletRequest originalRequest=getOriginalRequest(request);
			Map parameterMap=new HashMap();
			parameterMap.putAll(originalRequest.getParameterMap());
			parameterMap.putAll(super.getParameterMap());
			this.parameterMap=Collections.unmodifiableMap(parameterMap);
		}
		else {
			parameterMap=super.getParameterMap();
		}
	}

	@Override
	public Map getParameterMap()  {
		return parameterMap;
	}

	@Override
	public String getParameter(String name)
	{
		Object value = parameterMap.get(name);
		if (value==null) {
			return null;
		}
		if (value instanceof String) {
			return String.class.cast(value);
		}
		else if (value instanceof String[]) {
		   String[] values=String[].class.cast(value);
		   return values.length>0 ? values[0] : null;
		}
		else {
			throw new IllegalArgumentException("Unexpected class: " + value.getClass().getName()+ " of parameter:"+name);
		}
	}

	@Override
	public Enumeration getParameterNames()
	{
		return Collections.enumeration(parameterMap.keySet());
	}

	@Override
	public String[] getParameterValues(String name) {
		Object value = parameterMap.get(name);
		if (value==null) {
			return null;
		}
		if (value instanceof String) {
			return new String[] {String.class.cast(value)};
		}
		else if (value instanceof String[]) {
			return String[].class.cast(value);
		}
		else {
			throw new IllegalArgumentException("Unexpected class: " + value.getClass().getName()+ " of parameter:"+name);
		}
	}

	/**
	 * FIXME javadoc
	 * 
	 * <p>
	 * Public constructor which internally builds the path info from request URI, instead of
	 * deriving it.
	 * 
	 * @param context
	 * @param request
	 * @param proxiedSession
	 * @param filterPath
	 */
	public PortletServletRequestWrapper(ServletContext context, HttpServletRequest request,
		HttpSession proxiedSession, String filterPath)
	{
		this(context, proxiedSession, request, filterPath);

		// Liferay sometimes gives an incorrect requestURI
		int pathInfoBegin = contextPath.length() + filterPath.length();
		String pathInfo = pathInfoBegin >= requestURI.length() ? null
			: requestURI.substring(pathInfoBegin);
		this.pathInfo = pathInfo == null || pathInfo.length() < 2 ? null : pathInfo;
	}

	/**
	 * FIXME javadoc
	 * 
	 * <p>
	 * Public constructor called when not running in a portlet environment, which is passed in the
	 * path info instead of deriving it. It overrides the generated request URI from the internal
	 * constructor.
	 * 
	 * @param context
	 * @param request
	 * @param proxiedSession
	 * @param filterPath
	 *            ???
	 * @param pathInfo
	 *            ???
	 */
	public PortletServletRequestWrapper(ServletContext context, HttpServletRequest request,
		HttpSession proxiedSession, String filterPath, String pathInfo)
	{
		this(context, proxiedSession, request, filterPath);

		this.pathInfo = pathInfo;
		// override requestURI which is setup in the protected constructor
		requestURI = contextPath + servletPath + (pathInfo != null ? pathInfo : "");
	}

	/**
	 * @see javax.servlet.ServletRequestWrapper#setCharacterEncoding(java.lang.String)
	 */
	@Override
	public void setCharacterEncoding(String enc) throws UnsupportedEncodingException
	{
		try
		{
			super.setCharacterEncoding(enc);
		}
		catch (UnsupportedEncodingException uex)
		{
			// TODO
			// SUN OpenPortal Portlet Container 2.0_01 BUG which only allows setting an encoding as
			// provided by underlying request (then: what's the use?)
			// and throws UnsupportedEncodingException even when that one == null :(
			// ... so, ignoring for now
		}
	}

	@Override
	public String getContextPath()
	{
		return contextPath;
	}

	@Override
	public String getServletPath()
	{
		return servletPath;
	}

	@Override
	public String getPathInfo()
	{
		return pathInfo;
	}

	@Override
	public String getRequestURI()
	{
		return requestURI;
	}

	@Override
	public String getQueryString()
	{
		return queryString;
	}

	@Override
	public HttpSession getSession()
	{
		return getSession(true);
	}

	@Override
	public HttpSession getSession(boolean create)
	{
		return session != null ? session : super.getSession(create);
	}

	@Override
	public Object getAttribute(String name)
	{
		// TODO: check if these can possibly be set/handled
		// nullifying these for now to prevent Wicket
		// ServletWebRequest.getRelativePathPrefixToWicketHandler() going the wrong route
		if ("javax.servlet.error.request_uri".equals(name) ||
			"javax.servlet.forward.servlet_path".equals(name))
		{
			return null;
		}
		return super.getAttribute(name);
	}
}

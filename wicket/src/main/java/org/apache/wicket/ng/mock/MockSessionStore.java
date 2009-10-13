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
package org.apache.wicket.ng.mock;

import java.io.Serializable;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.CopyOnWriteArraySet;

import org.apache.wicket.IPageMap;
import org.apache.wicket.Page;
import org.apache.wicket.Session;
import org.apache.wicket.ng.request.Request;
import org.apache.wicket.session.ISessionStore;
import org.apache.wicket.version.IPageVersionManager;

/**
 * Session store that holds one session.
 * 
 * @author Matej Knopp
 */
public class MockSessionStore implements ISessionStore
{

	public MockSessionStore()
	{

	}

	private String sessionId;
	private final Map<String, Serializable> attributes = new HashMap<String, Serializable>();
	private Session session;

	public void bind(Request request, Session newSession)
	{
		session = newSession;
	}

	public void destroy()
	{
		cleanup();
	}

	public Serializable getAttribute(Request request, String name)
	{
		return attributes.get(name);
	}

	public Set<String> getAttributeNames(Request request)
	{
		return Collections.unmodifiableSet(attributes.keySet());
	}

	public String getSessionId(Request request, boolean create)
	{
		if (create && sessionId == null)
		{
			sessionId = UUID.randomUUID().toString();
		}
		return sessionId;
	}

	private void cleanup()
	{
		sessionId = null;
		attributes.clear();
		session = null;
	}

	public void invalidate(Request request)
	{
		for (UnboundListener l : unboundListeners)
		{
			l.sessionUnbound(sessionId);
		}

		cleanup();
	}

	public Session lookup(Request request)
	{
		return session;
	}

	private final Set<UnboundListener> unboundListeners = new CopyOnWriteArraySet<UnboundListener>();

	public void registerUnboundListener(UnboundListener listener)
	{
		unboundListeners.add(listener);
	}

	public void removeAttribute(Request request, String name)
	{
		attributes.remove(name);
	}

	public void setAttribute(Request request, String name, Serializable value)
	{
		attributes.put(name, value);
	}

	public void unregisterUnboundListener(UnboundListener listener)
	{
		unboundListeners.remove(listener);
	}

	// EVERYTHING BELOW WAS AUTOMATICALLY STUBBED OUT////////////////////////////////

	public void bind(org.apache.wicket.Request request, Session newSession)
	{
	}

	public IPageMap createPageMap(String name)
	{
		return null;
	}

	public Serializable getAttribute(org.apache.wicket.Request request, String name)
	{
		return null;
	}

	public List<String> getAttributeNames(org.apache.wicket.Request request)
	{
		return null;
	}

	public String getSessionId(org.apache.wicket.Request request, boolean create)
	{
		return null;
	}

	public Collection<UnboundListener> getUnboundListeners()
	{
		return null;
	}

	public void invalidate(org.apache.wicket.Request request)
	{
	}

	public Session lookup(org.apache.wicket.Request request)
	{
		return null;
	}

	public IPageVersionManager newVersionManager(Page page)
	{
		return null;
	}

	public void onBeginRequest(org.apache.wicket.Request request)
	{
	}

	public void onEndRequest(org.apache.wicket.Request request)
	{
	}

	public void removeAttribute(org.apache.wicket.Request request, String name)
	{
	}

	public void setAttribute(org.apache.wicket.Request request, String name, Serializable value)
	{
	}

	public void unbind(String sessionId)
	{
	}


}

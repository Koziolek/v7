/*
 * Copyright (C) 2013 David Sowerby
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on
 * an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations under the License.
 */
package uk.co.q3c.v7.base.shiro;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.apache.shiro.subject.Subject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uk.co.q3c.v7.base.guice.uiscope.UIScoped;
import uk.co.q3c.v7.base.navigate.V7Navigator;
import uk.co.q3c.v7.base.ui.ScopedUI;
import uk.co.q3c.v7.base.view.component.UserNavigationTree;

import com.google.inject.Inject;
import com.vaadin.server.VaadinSession;
import com.vaadin.ui.UI;

/**
 * See {@link LoginStatusHandler} for description.
 * <p>
 * There is generally no need to call {@link #removeListener(LoginStatusListener)} because instances of this class have
 * the same scope as the UI they belong to.
 * <p>
 * When the listeners are fired, the navigator is the last one to be notified of the login status change. This is to
 * ensure that components such as the {@link UserNavigationTree} are updated with the correct content before attempting
 * to navigate to a page which may not have been available before. See https://github.com/davidsowerby/v7/issues/225
 * 
 * @author David Sowerby 16 Sep 2013
 * 
 */
@UIScoped
public class DefaultLoginStatusHandler implements LoginStatusHandler {
	private static Logger log = LoggerFactory.getLogger(DefaultLoginStatusHandler.class);
	private final List<LoginStatusListener> listeners = new ArrayList<>();
	private final VaadinSessionProvider sessionProvider;
	private final SubjectIdentifier subjectIdentifier;

	private final SubjectProvider subjectProvider;
	private final V7Navigator navigator;

	@Inject
	protected DefaultLoginStatusHandler(VaadinSessionProvider sessionProvider, SubjectIdentifier subjectIdentifier,
			SubjectProvider subjectProvider, V7Navigator navigator) {
		super();
		this.sessionProvider = sessionProvider;
		this.subjectIdentifier = subjectIdentifier;
		this.subjectProvider = subjectProvider;
		this.navigator = navigator;
	}

	@Override
	public void addListener(LoginStatusListener listener) {
		listeners.add(listener);
	}

	@Override
	public void removeListener(LoginStatusListener listener) {
		listeners.remove(listener);
	}

	/**
	 * Fires each of the listeners, and then notifies the navigator (which must always be last)
	 */
	private void fireListeners() {
		Subject subject = subjectProvider.get();
		log.debug("firing login status change listeners");
		for (LoginStatusListener listener : listeners) {
			listener.loginStatusChange(subject.isAuthenticated(), subject);
		}
		navigator.loginStatusChange(subject.isAuthenticated(), subject);
	}

	@Override
	public void initiateStatusChange() {
		log.debug("starting login status change");
		VaadinSession session = sessionProvider.get();
		Collection<UI> uIs = session.getUIs();

		for (UI ui : uIs) {
			ScopedUI sui = (ScopedUI) ui;
			sui.getLoginStatusHandler().respondToStatusChange();
		}
	}

	@Override
	public void respondToStatusChange() {
		fireListeners();
	}

	@Override
	public boolean subjectIsAuthenticated() {
		Subject subject = subjectProvider.get();
		boolean authenticated = subject.isAuthenticated();
		return authenticated;
	}

	@Override
	public String subjectName() {
		return subjectIdentifier.subjectName();
	}
}

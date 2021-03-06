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
package uk.co.q3c.v7.base.navigate;

import static com.google.common.base.Preconditions.checkNotNull;

import java.util.LinkedList;
import java.util.List;

import org.apache.shiro.authz.AuthorizationException;
import org.apache.shiro.authz.UnauthorizedException;
import org.apache.shiro.subject.Subject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uk.co.q3c.v7.base.navigate.sitemap.Sitemap;
import uk.co.q3c.v7.base.navigate.sitemap.SitemapException;
import uk.co.q3c.v7.base.navigate.sitemap.SitemapNode;
import uk.co.q3c.v7.base.navigate.sitemap.SitemapService;
import uk.co.q3c.v7.base.shiro.PageAccessController;
import uk.co.q3c.v7.base.shiro.SubjectProvider;
import uk.co.q3c.v7.base.shiro.UnauthorizedExceptionHandler;
import uk.co.q3c.v7.base.ui.ScopedUI;
import uk.co.q3c.v7.base.ui.ScopedUIProvider;
import uk.co.q3c.v7.base.user.status.UserStatus;
import uk.co.q3c.v7.base.view.DefaultViewFactory;
import uk.co.q3c.v7.base.view.ErrorView;
import uk.co.q3c.v7.base.view.V7View;
import uk.co.q3c.v7.base.view.V7ViewChangeEvent;
import uk.co.q3c.v7.base.view.V7ViewChangeListener;

import com.google.inject.Inject;
import com.google.inject.Provider;
import com.vaadin.navigator.ViewChangeListener;
import com.vaadin.navigator.ViewChangeListener.ViewChangeEvent;
import com.vaadin.server.Page;
import com.vaadin.server.Page.UriFragmentChangedEvent;

/**
 * There is no need to register as a listener with {@link UserStatus}, the navigator is always called after all
 * other listeners - this is so that navigation components are set up before the navigator moves to a page (which might
 * not be displayed in a navigation component if it is not up to date)
 * 
 * @author David Sowerby
 * @date 18 Apr 2014
 */
public class DefaultV7Navigator implements V7Navigator {
	private static Logger log = LoggerFactory.getLogger(DefaultV7Navigator.class);

	private final List<V7ViewChangeListener> viewChangeListeners = new LinkedList<V7ViewChangeListener>();
	private final URIFragmentHandler uriHandler;

	private NavigationState currentNavigationState;
	private NavigationState previousNavigationState;

	private final Sitemap sitemap;
	private final Provider<Subject> subjectProvider;
	private V7View currentView = null;

	private final PageAccessController pageAccessController;

	private final ScopedUIProvider uiProvider;

	private final DefaultViewFactory viewFactory;

	@Inject
	public DefaultV7Navigator(URIFragmentHandler uriHandler, SitemapService sitemapService,
			SubjectProvider subjectProvider, PageAccessController pageAccessController, ScopedUIProvider uiProvider,
			DefaultViewFactory viewFactory) {
		super();
		this.uriHandler = uriHandler;
		this.uiProvider = uiProvider;

		this.subjectProvider = subjectProvider;
		this.pageAccessController = pageAccessController;
		this.viewFactory = viewFactory;

		try {
			sitemapService.start();
			sitemap = sitemapService.getSitemap();
		} catch (Exception e) {
			String msg = "Sitemap service failed to start, application will have no pages";
			log.error(msg);
			throw new IllegalStateException(msg, e);
		}
	}

	/**
	 * Takes a URI fragment, checks for any redirects defined by the {@link Sitemap}, then calls
	 * {@link #navigateTo(V7View, String, String)} to change the view
	 * 
	 * @see uk.co.q3c.v7.base.navigate.V7Navigator#navigateTo(java.lang.String)
	 */
	@Override
	public void navigateTo(String fragment) {
		log.debug("Navigating to fragment: {}", fragment);

		// set up the navigation state
		NavigationState navigationState = uriHandler.navigationState(fragment);
		navigateTo(navigationState);
	}

	/**
	 * Checks {@code fragment} to see whether the {@link Sitemap} defines this as a page which should be redirected. If
	 * it is, the full fragment is returned, but modified for the redirected page. If not, the {@code fragment} is
	 * returned unchanged.
	 * 
	 * @param fragment
	 * @return
	 */
	private NavigationState redirectIfNeeded(NavigationState navigationState) {

		String page = navigationState.getVirtualPage();
		String redirection = sitemap.getRedirectPageFor(page);
		// if no redirect found, page is returned
		if (redirection == page) {
			return navigationState;
		} else {
			navigationState.setVirtualPage(redirection);
			navigationState.setFragment(uriHandler.fragment(navigationState));
			return navigationState;
		}
	}

	/**
	 * Navigates to a the location represented by {@code navigationState}. If the {@link Sitemap} holds a redirect for
	 * the URI represented by {@code navigationState}, navigation will be directed to the redirect target. An
	 * unrecognised URI will throw a {@link SitemapException}. If the view for the URI is found, the user's
	 * authorisation is checked. If the user is not authorised, a {@link AuthorizationException} is thrown. This would
	 * be caught by the the implementation bound to {@link UnauthorizedExceptionHandler}. If the user is authorised, the
	 * View is instantiated, and made the current view in the UI via {@link ScopedUI#changeView(V7View)}.<br>
	 * <br>
	 * Events are fired before and after the view change, to the {@link #viewChangeListeners}. Listeners have the option
	 * to block the view change by returning false (see {@link #fireBeforeViewChange(V7ViewChangeEvent)}
	 * <p>
	 * 
	 * @param navigationState
	 *            The navigationState to navigate to. May not be null.
	 */
	@Override
	public void navigateTo(NavigationState navigationState) {
		checkNotNull(navigationState);
		redirectIfNeeded(navigationState);

		// stop unnecessary changes, but also to prevent navigation aware
		// components from causing a loop by responding to a change of URI (they should suppress events when they do,
		// but may not)
		if (navigationState == currentNavigationState) {
			log.debug("fragment unchanged, no navigation required");
			return;
		}

		// https://sites.google.com/site/q3cjava/sitemap#emptyURI
		if (navigationState.getVirtualPage().isEmpty()) {
			navigationState.setVirtualPage(sitemap.standardPageURI(StandardPageKey.Public_Home));
			uriHandler.updateFragment(navigationState);
		}

		log.debug("obtaining view for '{}'", navigationState.getVirtualPage());

		SitemapNode node = sitemap.nodeFor(navigationState);
		if (node == null) {
			throw new InvalidURIException("URI not found: " + navigationState.getVirtualPage());
		}

		Subject subject = subjectProvider.get();
		boolean authorised = pageAccessController.isAuthorised(subject, node);
		if (authorised) {

			// need this in case the change is blocked by a listener
			NavigationState previousPreviousNavigationState = previousNavigationState;
			previousNavigationState = currentNavigationState;
			currentNavigationState = navigationState;

			V7ViewChangeEvent event = new V7ViewChangeEvent(navigationState);
			// if change is blocked revert to previous state
			if (!fireBeforeViewChange(event)) {
				currentNavigationState = previousNavigationState;
				previousNavigationState = previousPreviousNavigationState;
				return;
			}

			// make sure the page uri is updated if necessary, but do not fire any change events
			// as we have already responded to the change
			ScopedUI ui = uiProvider.get();
			Page page = ui.getPage();
			if (!navigationState.getFragment().equals(page.getUriFragment())) {
				page.setUriFragment(navigationState.getFragment(), false);
			}
			// now change the view
			V7View view = viewFactory.get(node.getViewClass());
			changeView(view, event);
			// and tell listeners its changed
			fireAfterViewChange(event);
		} else {
			throw new UnauthorizedException(navigationState.getVirtualPage());
		}

	}

	protected void changeView(V7View view, V7ViewChangeEvent event) {
		ScopedUI ui = uiProvider.get();
		ui.changeView(view);
		view.enter(event);
		currentView = view;
	}

	/**
	 * Fires an event before an imminent view change.
	 * <p>
	 * Listeners are called in registration order. If any listener returns <code>false</code>, the rest of the listeners
	 * are not called and the view change is blocked.
	 * <p>
	 * The view change listeners may also e.g. open a warning or question dialog and save the parameters to re-initiate
	 * the navigation operation upon user action.
	 * 
	 * @param event
	 *            view change event (not null, view change not yet performed)
	 * @return true if the view change should be allowed, false to silently block the navigation operation
	 */
	protected boolean fireBeforeViewChange(V7ViewChangeEvent event) {
		for (V7ViewChangeListener l : viewChangeListeners) {
			if (!l.beforeViewChange(event)) {
				return false;
			}
		}
		return true;
	}

	/**
	 * Fires an event after the current view has changed.
	 * <p>
	 * Listeners are called in registration order.
	 * 
	 * @param event
	 *            view change event (not null)
	 */
	protected void fireAfterViewChange(V7ViewChangeEvent event) {
		for (V7ViewChangeListener l : viewChangeListeners) {
			l.afterViewChange(event);
		}
	}

	/**
	 * Listen to changes of the active view.
	 * <p>
	 * Registered listeners are invoked in registration order before (
	 * {@link ViewChangeListener#beforeViewChange(ViewChangeEvent) beforeViewChange()}) and after (
	 * {@link ViewChangeListener#afterViewChange(ViewChangeEvent) afterViewChange()}) a view change occurs.
	 * 
	 * @param listener
	 *            Listener to invoke during a view change.
	 */
	@Override
	public void addViewChangeListener(V7ViewChangeListener listener) {
		viewChangeListeners.add(listener);
	}

	/**
	 * Removes a view change listener.
	 * 
	 * @param listener
	 *            Listener to remove.
	 */
	@Override
	public void removeViewChangeListener(V7ViewChangeListener listener) {
		viewChangeListeners.remove(listener);
	}

	@Override
	public void uriFragmentChanged(UriFragmentChangedEvent event) {
		navigateTo(event.getUriFragment());
	}

	@Override
	public NavigationState getCurrentNavigationState() {
		return currentNavigationState;
	}

	@Override
	public List<String> getNavigationParams() {
		return currentNavigationState.getParameterList();
	}

	@Override
	public V7View getCurrentView() {
		return currentView;
	}

	@Override
	public void navigateTo(StandardPageKey pageKey) {
		navigateTo(sitemap.standardPageURI(pageKey));
	}

	/**
	 * Returns the NavigationState representing the previous position of the navigator
	 * 
	 * @return
	 */
	public NavigationState getPreviousNavigationState() {
		return previousNavigationState;
	}

	@Override
	public void clearHistory() {
		previousNavigationState = null;
	}

	@Override
	public void error(Throwable error) {
		NavigationState navigationState = uriHandler.navigationState("error");
		V7ViewChangeEvent event = new V7ViewChangeEvent(navigationState);
		V7View view = viewFactory.get(ErrorView.class);
		changeView(view, event);
	}

	/**
	 * Navigates to a the location represented by {@code node}
	 */
	@Override
	public void navigateTo(SitemapNode node) {
		navigateTo(sitemap.uri(node));
	}

	@Override
	public SitemapNode getCurrentNode() {
		return sitemap.nodeFor(currentNavigationState);
	}

	/**
	 * When a user has successfully logged in, they are routed back to the page they were on before going to the login
	 * page. If they have gone straight to the login page (maybe they bookmarked it), or they were on the logout page,
	 * they will be routed to the 'private home page' (the StandardPage for StandardPageKey_Private_Home)
	 * 
	 */
	@Override
	public void userStatusChanged() {
		log.debug("user logged in successfully, navigating to appropriate view");

		if (subjectProvider.get().isAuthenticated()) {
			// they have logged in
			SitemapNode previousNode = sitemap.nodeFor(previousNavigationState);
			if (previousNode != null && previousNode != sitemap.standardPageNode(StandardPageKey.Logout)) {
				navigateTo(previousNavigationState);
			} else {
				navigateTo(StandardPageKey.Private_Home);
			}
		} else {
			// they have logged out
			navigateTo(StandardPageKey.Logout);
		}
	}

}

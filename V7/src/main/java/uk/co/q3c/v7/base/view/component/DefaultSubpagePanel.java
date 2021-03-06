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
package uk.co.q3c.v7.base.view.component;

import java.util.List;

import uk.co.q3c.v7.base.guice.uiscope.UIScoped;
import uk.co.q3c.v7.base.navigate.V7Navigator;
import uk.co.q3c.v7.base.navigate.sitemap.Sitemap;
import uk.co.q3c.v7.base.navigate.sitemap.SitemapNode;
import uk.co.q3c.v7.base.shiro.PageAccessController;
import uk.co.q3c.v7.base.shiro.SubjectProvider;
import uk.co.q3c.v7.base.user.opt.UserOption;
import uk.co.q3c.v7.base.user.status.UserStatus;
import uk.co.q3c.v7.base.user.status.UserStatusListener;
import uk.co.q3c.v7.i18n.CurrentLocale;
import uk.co.q3c.v7.i18n.Translate;

import com.google.inject.Inject;

@UIScoped
public class DefaultSubpagePanel extends NavigationButtonPanel implements SubpagePanel, UserStatusListener {

	private final PageAccessController pageAccessController;
	private final SubjectProvider subjectProvider;

	@Inject
	protected DefaultSubpagePanel(V7Navigator navigator, Sitemap sitemap, CurrentLocale currentLocale,
			Translate translate, PageAccessController pageAccessController, SubjectProvider subjectProvider,
			UserStatus userStatus, UserOption userOption) {
		super(navigator, sitemap, currentLocale, translate, userOption);
		this.pageAccessController = pageAccessController;
		this.subjectProvider = subjectProvider;
		userStatus.addListener(this);
		userStatusChanged();
	}

	@Override
	protected void moveToNavigationState() {
		List<SitemapNode> authorisedSubNodes = pageAccessController.authorisedChildNodes(subjectProvider.get(),
				getNavigator().getCurrentNode());
		organiseButtons(authorisedSubNodes);
	}

	@Override
	public void userStatusChanged() {
		moveToNavigationState();

	}

}

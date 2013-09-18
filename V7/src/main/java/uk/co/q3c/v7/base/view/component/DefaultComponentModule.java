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

import com.google.inject.AbstractModule;

public class DefaultComponentModule extends AbstractModule {

	@Override
	protected void configure() {
		bindLoginStatusPanel();
		bindApplicationMenu();
		bindSubpagePanel();
		bindMessageStatusPanel();
		bindApplicationLogo();
		bindApplicationHeader();
	}

	private void bindMessageStatusPanel() {
		bind(MessageStatusPanel.class).to(DefaultMessageStatusPanel.class);
	}

	private void bindApplicationHeader() {
		bind(ApplicationHeader.class).to(DefaultApplicationHeader.class);
	}

	private void bindApplicationLogo() {
		bind(ApplicationLogo.class).to(DefaultApplicationLogo.class);
	}

	private void bindSubpagePanel() {
		bind(SubpagePanel.class).to(DefaultSubpagePanel.class);
	}

	private void bindLoginStatusPanel() {
		bind(LoginStatusPanel.class).to(DefaultLoginStatusPanel.class);
	}

	private void bindApplicationMenu() {
		bind(ApplicationMenu.class).to(DefaultApplicationMenu.class);
	}

}

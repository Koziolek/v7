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
package uk.co.q3c.v7.base.config;

import javax.inject.Singleton;

import org.apache.shiro.subject.Subject;

import uk.co.q3c.v7.base.navigate.Sitemap;
import uk.co.q3c.v7.base.navigate.SitemapProvider;
import uk.co.q3c.v7.base.navigate.TextReaderSitemapProvider;
import uk.co.q3c.v7.base.shiro.SubjectProvider;

import com.google.inject.AbstractModule;

public class IniModule extends AbstractModule {

	@Override
	protected void configure() {
		bind(V7Ini.class).toProvider(V7IniProvider.class).in(Singleton.class);
		bind(SitemapProvider.class).to(TextReaderSitemapProvider.class);
		bind(Sitemap.class).toProvider(SitemapProvider.class).in(Singleton.class);
		bindSubjectProvider();
	}

	protected void bindSubjectProvider() {
		bind(Subject.class).toProvider(SubjectProvider.class);
	}

}

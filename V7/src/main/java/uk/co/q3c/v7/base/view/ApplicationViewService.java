package uk.co.q3c.v7.base.view;

import java.util.HashSet;
import java.util.Set;

import javax.inject.Inject;
import javax.inject.Singleton;

import uk.co.q3c.v7.base.guice.services.Service;
import uk.co.q3c.v7.base.navigate.sitemap.Sitemap;

import com.google.inject.Provider;

@Singleton
public class ApplicationViewService implements Service {

	private final Provider<Sitemap> sitemapPro;

	// the sitemap will be created right after the injector, this way many
	// errors could be seen earlier than first use
	@Inject
	public ApplicationViewService(Provider<Sitemap> sitemapPro) {
		this.sitemapPro = sitemapPro;
	}

	@Override
	public Status start() {
		// the sitemap will be created right after the injector, this way many
		// errors could be seen earlier than first use
		sitemapPro.get();
		return Status.STARTED;
	}

	@Override
	public Status stop() {
		return Status.STOPPED;
	}

	@Override
	public String serviceId() {
		return "Application View";
	}

	@Override
	public String getName() {
		return "Appplication View Service";
	}

	@Override
	public Set<Class<? extends Service>> getDependencies() {
		return new HashSet<Class<? extends Service>>();
	}
}
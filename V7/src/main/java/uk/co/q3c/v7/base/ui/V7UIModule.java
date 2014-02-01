package uk.co.q3c.v7.base.ui;

import uk.co.q3c.v7.base.data.V7DefaultConverterFactory;
import uk.co.q3c.v7.base.guice.V7Widgetset;
import uk.co.q3c.v7.base.navigate.DefaultV7Navigator;
import uk.co.q3c.v7.base.navigate.StrictURIFragmentHandler;
import uk.co.q3c.v7.base.navigate.URIFragmentHandler;
import uk.co.q3c.v7.base.navigate.V7Navigator;
import uk.co.q3c.v7.base.view.component.DefaultLoginStatusPanel;
import uk.co.q3c.v7.base.view.component.LoginStatusPanel;

import com.google.inject.AbstractModule;
import com.google.inject.multibindings.MapBinder;
import com.vaadin.data.util.converter.ConverterFactory;
import com.vaadin.server.WebBrowser;
import com.vaadin.ui.UI;

public abstract class V7UIModule extends AbstractModule {

	@Override
	protected void configure() {
		MapBinder<String, UI> mapbinder = MapBinder.newMapBinder(binder(), String.class, UI.class);

		bind(WebBrowser.class).toProvider(BrowserProvider.class);

		bindUIProvider();
		addUIBindings(mapbinder);
		bindNavigator();
		bindURIHandler();
		bindConverterFactory();
		bindLoginStatusMonitor();
		bind(String.class).annotatedWith(V7Widgetset.class).toInstance(widgetset());
	}

	/**
	 * Override this with the package qualified name of the widgetset to use, for example
	 * "uk.co.q3c.v7.demo.widgetset.V7demoWidgetset";
	 * <p>
	 * If the value returned from the method is "default", the default Vaadin widgetset is used.
	 * 
	 * @return
	 */
	protected String widgetset() {
		return "default";
	}

	private void bindConverterFactory() {
		bind(ConverterFactory.class).to(V7DefaultConverterFactory.class);
	}

	/**
	 * Override to bind your choice of LoginStatusMonitor
	 */
	protected void bindLoginStatusMonitor() {
		bind(LoginStatusPanel.class).to(DefaultLoginStatusPanel.class);
	}

	/**
	 * Override to bind your ScopedUIProvider implementation
	 */
	protected abstract void bindUIProvider();

	/**
	 * Override to bind your choice of URI handler
	 */
	protected void bindURIHandler() {
		bind(URIFragmentHandler.class).to(StrictURIFragmentHandler.class);
	}

	protected void bindNavigator() {
		bind(V7Navigator.class).to(DefaultV7Navigator.class);
	}

	/**
	 * Override with your UI bindings
	 * 
	 * @param mapbinder
	 */
	protected void addUIBindings(MapBinder<String, UI> mapbinder) {
		mapbinder.addBinding(BasicUI.class.getName()).to(BasicUI.class);
	}

}

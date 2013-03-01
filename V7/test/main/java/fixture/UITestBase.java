package fixture;

import static org.mockito.Mockito.*;

import javax.inject.Inject;
import javax.inject.Provider;

import org.junit.After;
import org.junit.Before;
import org.junit.runner.RunWith;

import uk.co.q3c.v7.base.config.V7Ini;
import uk.co.q3c.v7.base.config.V7IniProvider;
import uk.co.q3c.v7.base.guice.BaseModule;
import uk.co.q3c.v7.base.guice.uiscope.TestUI;
import uk.co.q3c.v7.base.guice.uiscope.UIKey;
import uk.co.q3c.v7.base.guice.uiscope.UIScope;
import uk.co.q3c.v7.base.navigate.V7Navigator;
import uk.co.q3c.v7.base.shiro.ShiroIntegrationTestBase;
import uk.co.q3c.v7.base.ui.BasicUI;
import uk.co.q3c.v7.base.ui.ScopedUI;
import uk.co.q3c.v7.base.view.V7View;
import uk.co.q3c.v7.base.view.V7ViewChangeEvent;
import uk.co.q3c.v7.base.view.V7ViewChangeListener;
import uk.co.q3c.v7.base.view.components.HeaderBar;
import uk.co.q3c.v7.demo.ui.DemoUIProvider;
import uk.co.q3c.v7.util.A;

import com.google.inject.Injector;
import com.google.inject.name.Named;
import com.mycila.testing.junit.MycilaJunitRunner;
import com.mycila.testing.plugin.guice.GuiceContext;
import com.vaadin.server.VaadinRequest;
import com.vaadin.ui.UI;
import com.vaadin.util.CurrentInstance;

/**
 * Extend this class to test anything related to a Vaadin UI (or in the case of V7, as {@link ScopedUI}. Note that the
 * {@link UIScope} is not prepared until the {@link #uiSetup()} method is called, so subclasses must use providers if
 * they want to inject UIScoped objects - otherwise the injection happens before the UIScope context is ready.
 * <p>
 * A number of providers are made available by the class
 * 
 * @author David Sowerby 18 Jan 2013
 * 
 */
@RunWith(MycilaJunitRunner.class)
@GuiceContext({ BaseModule.class })
public abstract class UITestBase extends ShiroIntegrationTestBase implements V7ViewChangeListener {

	@Inject
	@Named(A.baseUri)
	protected String baseUri;

	VaadinRequest mockedRequest = mock(VaadinRequest.class);

	protected V7View currentView;

	@Inject
	protected DemoUIProvider provider;

	@Inject
	Injector injector;

	protected HeaderBar headerBar;

	protected ScopedUI ui;

	@Inject
	protected Provider<V7Navigator> navigatorPro;

	@Inject
	private V7IniProvider iniPro;

	protected V7Ini ini;

	protected static Class<? extends ScopedUI> uiClass;

	@Before
	public void setup() {
		createUI(uiClass);
		headerBar = injector.getInstance(HeaderBar.class);

	}

	@After
	public void teardown() {
		currentView = null;
	}

	@Override
	public boolean beforeViewChange(V7ViewChangeEvent event) {
		return true;
	}

	@Override
	public void afterViewChange(V7ViewChangeEvent event) {
		currentView = event.getNewView();
	}

	/**
	 * Use this method to create TestUI instances, rather than the UIProvider It simulates the creation of a new
	 * CurrentInstance (which happens for each request)
	 * 
	 * @return
	 */
	protected TestUI createTestUI() {
		return (TestUI) createUI(TestUI.class);
	}

	/**
	 * Use this method to create BasicUI instances, rather than the UIProvider It simulates the creation of a new
	 * CurrentInstance (which happens for each request)
	 * 
	 * @return
	 */
	protected BasicUI createBasicUI() {
		return (BasicUI) createUI(BasicUI.class);
	}

	private ScopedUI createUI(Class<? extends ScopedUI> clazz) {
		CurrentInstance.set(UI.class, null);
		CurrentInstance.set(UIKey.class, null);
		ui = (ScopedUI) provider.createInstance(clazz);
		CurrentInstance.set(UI.class, ui);
		when(mockedRequest.getParameter("v-loc")).thenReturn(baseUri + "/");
		ui.getV7Navigator().addViewChangeListener(this);
		ui.doInit(mockedRequest, 1);
		ini = iniPro.get();
		return ui;
	}

}

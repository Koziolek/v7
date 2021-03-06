package uk.co.q3c.v7.base.ui;

import uk.co.q3c.v7.base.navigate.V7Navigator;
import uk.co.q3c.v7.base.push.Broadcaster;
import uk.co.q3c.v7.base.push.PushMessageRouter;
import uk.co.q3c.v7.base.view.component.DefaultLoginStatusPanel;
import uk.co.q3c.v7.i18n.Translate;

import com.google.inject.Inject;
import com.vaadin.data.util.converter.ConverterFactory;
import com.vaadin.server.ErrorHandler;
import com.vaadin.ui.AbstractOrderedLayout;
import com.vaadin.ui.VerticalLayout;

public class TestUI extends ScopedUI {

	@Inject
	private DefaultLoginStatusPanel panel1;

	@Inject
	private DefaultLoginStatusPanel panel2;

	@Inject
	public TestUI(V7Navigator navigator, ErrorHandler errorHandler, ConverterFactory converterFactory,
			Broadcaster broadcaster, PushMessageRouter pushMessageRouter, ApplicationTitle applicationTitle,
			Translate translate) {
		super(navigator, errorHandler, converterFactory, broadcaster, pushMessageRouter, applicationTitle, translate);

	}

	public DefaultLoginStatusPanel getPanel2() {
		return panel2;
	}

	@Override
	protected AbstractOrderedLayout screenLayout() {
		return new VerticalLayout(getViewDisplayPanel());
	}

	@Override
	protected String pageTitle() {
		return "TestUI";
	}

	public DefaultLoginStatusPanel getPanel1() {
		return panel1;
	}

	@Override
	protected void processBroadcastMessage(String group, String message) {
		// TODO Auto-generated method stub

	}

}

package uk.co.q3c.v7.base.services;

import com.google.inject.Provider;

/**
 * Implement this interface to provide a Service. A Service is typically something which is wired up using Guice
 * modules, but requires logic to get fully up and running, or consumes external resources - database connections, web
 * services etc, and is based on the recommendations of the Guice team. (see
 * https://code.google.com/p/google-guice/wiki/ModulesShouldBeFastAndSideEffectFree).
 * <p>
 * A {@link Service} can however be used for anything you feel appropriate, which could benefit from having a two stage
 * creation cycle - the initial configuration through Guice modules, followed by a controlled start to activate /
 * consume resources.
 * <p>
 * The easiest way is to create an implementation is to sub-class either {@link AbstractService} or
 * {@link AbstractServiceI18N}. Sub-classing one of these, combined with the {@link Dependency} annotation, will also
 * provide some service management functionality (see the {@link AbstractService} javadoc.
 * <p>
 * When an instance of a {@link Service} implementation is created through Guice, it is automatically registered with
 * the {@link ServicesMonitor}. (This is done through a Guice listener in the {@link ServicesMonitorModule}).
 * <p>
 * The AOP code in the ServicesMonitorModule also intercepts the finalize() method, and calls the stop() method to
 * ensure a service is stopped before being finalized.
 * <p>
 * A service should have the following characteristics:
 * <ol>
 * <li>All Services must be instantiated through Guice
 * <li>Other {@link Service} instances which your Service depends on, must be injected through the constructor
 * <li>The constructor must be lightweight and must not require that its dependencies are already started at the time of
 * injection.
 * <li>There are some limitations with injecting {@link Provider}s of services - but if the dependency's constructor is
 * lightweight as it should be, it should also be unnecessary to inject a Provider
 * </ol>
 * 
 * 
 * @author David Sowerby
 * 
 */
public interface Service {

	public enum Status {
		INITIAL, STARTED, FAILED, STOPPED, FAILED_TO_START, FAILED_TO_STOP, DEPENDENCY_FAILED
	}

	/**
	 * You will only need to implement this if you are not using a sub-class of {@link AbstractService}. When you do
	 * sub-class {@link AbstractService}, override {@link AbstractService#doStart()}
	 * 
	 */
	Status start() throws Exception;

	/**
	 * You will only need to implement this if you are not using a sub-class of {@link AbstractService}. When you do
	 * sub-class {@link AbstractService}, override {@link AbstractService#doStop()}
	 * 
	 */
	Status stop() throws Exception;

	/**
	 * The name for this service. The implementation may wish to include an instance identifier if it is not of
	 * Singleton scope, but this is not essential; the name is not used for anything except as a label. You may also
	 * choose to implement by sub-classing {@link AbstractServiceI18N}, which will handle I18N keys and translation
	 * 
	 * @return
	 */
	String getName();

	/**
	 * The name description for this service. You may also choose to implement by sub-classing
	 * {@link AbstractServiceI18N}, which will handle I18N keys and translation
	 * 
	 * @return
	 */
	String getDescription();

	/**
	 * returns the Status value for this service instance
	 * 
	 * @return
	 */
	Status getStatus();

	/**
	 * Returns true if and only if status == Service.Status.STARTED)
	 * 
	 * @return
	 */
	boolean isStarted();

	/**
	 * Adds a listener which will be notified when any change of service status occurs
	 * 
	 * @param listener
	 */
	void addChangeListener(ServiceChangeListener listener);

	void removeChangeListener(ServiceChangeListener listener);

	/**
	 * Adds a listener which will be notified when a service is stopped or fails. Specifically, this occurs when the
	 * service changes state from {@link Status#STARTED} to {@link Status#FAILED}, {@link Status#DEPENDENCY_FAILED} or
	 * {@link Status#STOPPED}
	 */
	void addStopListener(ServiceStopListener listener);

	void removeStopListener(ServiceStopListener listener);

	/**
	 * Adds a listener which is notified when a service changes from any state to {@link Status#STARTED}
	 * 
	 * @param listener
	 */
	void addStartListener(ServiceStartListener listener);

	void removeStartListener(ServiceStartListener listener);

	/**
	 * The service is in a stopped state (stopped, failed or dependency failed)
	 * 
	 * @return
	 */
	boolean isStopped();

}

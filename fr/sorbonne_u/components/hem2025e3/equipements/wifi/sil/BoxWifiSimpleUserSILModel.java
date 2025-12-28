package fr.sorbonne_u.components.hem2025e3.equipements.wifi.sil;


//Copyright Jacques Malenfant, Sorbonne Universite.
//Jacques.Malenfant@lip6.fr
//
//This software is a computer program whose purpose is to implement a mock-up
//of household energy management system.
//
//This software is governed by the CeCILL-C license under French law and
//abiding by the rules of distribution of free software.  You can use,
//modify and/ or redistribute the software under the terms of the
//CeCILL-C license as circulated by CEA, CNRS and INRIA at the following
//URL "http://www.cecill.info".
//
//As a counterpart to the access to the source code and  rights to copy,
//modify and redistribute granted by the license, users are provided only
//with a limited warranty  and the software's author,  the holder of the
//economic rights,  and the successive licensors  have only  limited
//liability. 
//
//In this respect, the user's attention is drawn to the risks associated
//with loading,  using,  modifying and/or developing or reproducing the
//software by the user in light of its specific status of free software,
//that may mean  that it is complicated to manipulate,  and  that  also
//therefore means  that it is reserved for developers  and  experienced
//professionals having in-depth computer knowledge. Users are therefore
//encouraged to load and test the software's suitability as regards their
//requirements in conditions enabling the security of their systems and/or 
//data to be ensured and,  more generally, to use and operate it in the 
//same conditions as regards security. 
//
//The fact that you are presently reading this means that you have had
//knowledge of the CeCILL-C license and that you accept its terms.

import java.util.ArrayList; 
import java.util.Map;
import java.util.concurrent.TimeUnit;
import org.apache.commons.math3.random.RandomDataGenerator;

import fr.sorbonne_u.components.cyphy.plugins.devs.AtomicSimulatorPlugin;
import fr.sorbonne_u.components.hem2025e2.equipments.BoxWifi.mil.events.ActivateWifiBoxWifi;
import fr.sorbonne_u.components.hem2025e2.equipments.BoxWifi.mil.events.DeactivateWifiBoxWifi;
import fr.sorbonne_u.components.hem2025e2.equipments.BoxWifi.mil.events.SwitchOffBoxWifi;
import fr.sorbonne_u.components.hem2025e2.equipments.BoxWifi.mil.events.SwitchOnBoxWifi;
import fr.sorbonne_u.devs_simulation.es.events.ES_EventI;
import fr.sorbonne_u.devs_simulation.es.models.AtomicES_Model;
import fr.sorbonne_u.devs_simulation.exceptions.MissingRunParameterException;
import fr.sorbonne_u.devs_simulation.exceptions.NeoSim4JavaException;
import fr.sorbonne_u.devs_simulation.models.annotations.ModelExternalEvents;
import fr.sorbonne_u.devs_simulation.models.events.EventI;
import fr.sorbonne_u.devs_simulation.models.interfaces.ModelI;
import fr.sorbonne_u.devs_simulation.models.time.Duration;
import fr.sorbonne_u.devs_simulation.models.time.Time;
import fr.sorbonne_u.devs_simulation.simulators.interfaces.AtomicSimulatorI;
import fr.sorbonne_u.devs_simulation.simulators.interfaces.SimulationReportI;
import fr.sorbonne_u.devs_simulation.utils.StandardLogger;
import fr.sorbonne_u.exceptions.AssertionChecking;

//-----------------------------------------------------------------------------
/**
* The class <code>BoxWifiSimpleUserSILModel</code> defines a very simple user
* model for the box WiFi simulator.
*
* <p><strong>Description</strong></p>
* 
* <p>
* This model is meant to illustrate how to program MIL models simulating user
* actions by sending events to other models.
* </p>
* <p>
* Here, we use an event scheduling atomic model to output events at random
* time intervals in a predefined cycle to test all of the different modes in
* the box WiFi. Note that the exported events are indeed subclasses of
* event scheduling events {@code ES_Event}. Hence, this example also shows
* how to program this type of event scheduling simulation models.
* </p>
* <p>
* Event scheduling models are constructed around an event list that contains
* events scheduled to be executed at future time in the simulation time. The
* class {@code AtomicES_Model} hence defines the main methods to execute
* the transitions. Internal transitions simply occurs at the time of the next
* event in the event list and then, if the event is internal, execute that
* event on the model (by calling the method {@code executeOn} defined on the
* event. If the next event is external, it must be emitted towards other
* models. This is performed by the method {@code AtomicES_Model#output}
* </p>
* 
* <ul>
* <li>Imported events: none</li>
* <li>Exported events:
*   {@code SwitchOnBoxWifi},
*   {@code SwitchOffBoxWifi},
*   {@code ActivateWifiBoxWifi},
*   {@code DeactivateWifiBoxWifi}</li>
* </ul>
* 
* <p><strong>Implementation Invariants</strong></p>
* 
* <pre>
* invariant	{@code BOX_ON_DURATION > 0.0}
* invariant	{@code WIFI_ACTIVATION_DURATION > 0.0}
* invariant	{@code BOX_OFF_DURATION > 0.0}
* invariant	{@code rg != null}
* </pre>
* 
* <p><strong>Invariants</strong></p>
* 
* <pre>
* invariant	{@code URI != null && !URI.isEmpty()}
* invariant	{@code BOX_ON_DURATION_RPNAME != null && !BOX_ON_DURATION_RPNAME.isEmpty()}
* invariant	{@code WIFI_ACTIVATION_RPNAME != null && !WIFI_ACTIVATION_RPNAME.isEmpty()}
* invariant	{@code BOX_OFF_DURATION_RPNAME != null && !BOX_OFF_DURATION_RPNAME.isEmpty()}
* </pre>
* 
* <p>Created on : 2025-01-15</p>
* 
* @author	<a href="mailto:Jacques.Malenfant@lip6.fr">Jacques Malenfant</a>
*/
//-----------------------------------------------------------------------------
@ModelExternalEvents(exported = {SwitchOnBoxWifi.class,
								 SwitchOffBoxWifi.class,
								 ActivateWifiBoxWifi.class,
								 DeactivateWifiBoxWifi.class})
//-----------------------------------------------------------------------------
public class			BoxWifiSimpleUserSILModel
extends		AtomicES_Model
{
	// -------------------------------------------------------------------------
	// Constants and variables
	// -------------------------------------------------------------------------

	private static final long	serialVersionUID = 1L;
	/** URI for an instance model; works as long as only one instance is
	 *  created.															*/
	public static final String	URI = BoxWifiSimpleUserSILModel.class.
																getSimpleName();

	/** when true, leaves a trace of the execution of the model.			*/
	public static boolean		VERBOSE = true;
	/** when true, leaves a debugging trace of the execution of the model.	*/
	public static boolean		DEBUG = false;

	/** mean duration for which the box stays ON (WiFi disabled) in hours.	*/
	protected static double		BOX_ON_DURATION = 1.0; // 1 hours
	/** mean duration for which WiFi stays activated in hours.				*/
	protected static double		WIFI_ACTIVATION_DURATION = 2.0; // 2 hour
	/** mean duration for which the box stays OFF between usages in hours.	*/
	protected static double		BOX_OFF_DURATION = 8.0; // 8 hours

	/**	the random number generator from common math library.				*/
	protected final RandomDataGenerator	rg ;

	// -------------------------------------------------------------------------
	// Invariants
	// -------------------------------------------------------------------------

	/**
	 * return true if the static implementation invariants are observed, false
	 * otherwise.
	 * 
	 * <p><strong>Contract</strong></p>
	 * 
	 * <pre>
	 * pre	{@code true}	// no precondition.
	 * post	{@code true}	// no postcondition.
	 * </pre>
	 *
	 * @return			true if the static implementation invariants are observed, false otherwise.
	 */
	protected static boolean	staticImplementationInvariants()
	{
		boolean ret = true;
		ret &= AssertionChecking.checkStaticImplementationInvariant(
				BOX_ON_DURATION > 0.0,
				BoxWifiSimpleUserSILModel.class,
				"BOX_ON_DURATION > 0.0");
		ret &= AssertionChecking.checkStaticImplementationInvariant(
				WIFI_ACTIVATION_DURATION > 0.0,
				BoxWifiSimpleUserSILModel.class,
				"WIFI_ACTIVATION_DURATION > 0.0");
		ret &= AssertionChecking.checkStaticImplementationInvariant(
				BOX_OFF_DURATION > 0.0,
				BoxWifiSimpleUserSILModel.class,
				"BOX_OFF_DURATION > 0.0");
		return ret;
	}

	/**
	 * return true if the implementation invariants are observed, false
	 * otherwise.
	 * 
	 * <p><strong>Contract</strong></p>
	 * 
	 * <pre>
	 * pre	{@code instance != null}
	 * post	{@code true}	// no postcondition.
	 * </pre>
	 *
	 * @param instance	instance to be tested.
	 * @return			true if the implementation invariants are observed, false otherwise.
	 */
	protected static boolean	implementationInvariants(
		BoxWifiSimpleUserSILModel instance
		)
	{
		assert	instance != null :
				new NeoSim4JavaException("Precondition violation: "
						+ "instance != null");

		boolean ret = true;
		ret &= staticImplementationInvariants();
		ret &= AssertionChecking.checkImplementationInvariant(
				instance.rg != null,
				BoxWifiSimpleUserSILModel.class,
				instance,
				"rg != null");
		return ret;
	}

	/**
	 * return true if the static invariants are observed, false otherwise.
	 * 
	 * <p><strong>Contract</strong></p>
	 * 
	 * <pre>
	 * pre	{@code true}	// no precondition.
	 * post	{@code true}	// no postcondition.
	 * </pre>
	 *
	 * @return	true if the black-box invariants are observed, false otherwise.
	 */
	protected static boolean	staticInvariants()
	{
		boolean ret = true;
		ret &= AssertionChecking.checkStaticInvariant(
				URI != null && !URI.isEmpty(),
				BoxWifiSimpleUserSILModel.class,
				"URI != null && !URI.isEmpty()");
		ret &= AssertionChecking.checkStaticInvariant(
				BOX_ON_DURATION_RPNAME != null && !BOX_ON_DURATION_RPNAME.isEmpty(),
				BoxWifiSimpleUserSILModel.class,
				"BOX_ON_DURATION_RPNAME != null && !BOX_ON_DURATION_RPNAME.isEmpty()");
		ret &= AssertionChecking.checkStaticInvariant(
				WIFI_ACTIVATION_RPNAME != null && !WIFI_ACTIVATION_RPNAME.isEmpty(),
				BoxWifiSimpleUserSILModel.class,
				"WIFI_ACTIVATION_RPNAME != null && !WIFI_ACTIVATION_RPNAME.isEmpty()");
		ret &= AssertionChecking.checkStaticInvariant(
				BOX_OFF_DURATION_RPNAME != null && !BOX_OFF_DURATION_RPNAME.isEmpty(),
				BoxWifiSimpleUserSILModel.class,
				"BOX_OFF_DURATION_RPNAME != null && !BOX_OFF_DURATION_RPNAME.isEmpty()");
		return ret;
	}

	/**
	 * return true if the invariants are observed, false otherwise.
	 * 
	 * <p><strong>Contract</strong></p>
	 * 
	 * <pre>
	 * pre	{@code instance != null}
	 * post	{@code true}	// no postcondition.
	 * </pre>
	 *
	 * @param instance	instance to be tested.
	 * @return			true if the black-box invariants are observed, false otherwise.
	 */
	protected static boolean	invariants(BoxWifiSimpleUserSILModel instance)
	{
		assert	instance != null :
				new NeoSim4JavaException("Precondition violation: "
						+ "instance != null");

		boolean ret = true;
		ret &= staticInvariants();
		return ret;
	}

	// -------------------------------------------------------------------------
	// Constructors
	// -------------------------------------------------------------------------

	/**
	 * create a box WiFi tester SIL model instance.
	 * 
	 * <p><strong>Contract</strong></p>
	 * 
	 * <pre>
	 * pre	{@code uri == null || !uri.isEmpty()}
	 * pre	{@code simulatedTimeUnit != null}
	 * pre	{@code simulationEngine != null && !simulationEngine.isModelSet()}
	 * pre	{@code simulationEngine instanceof AtomicEngine}
	 * post	{@code !isDebugModeOn()}
	 * post	{@code getURI() != null && !getURI().isEmpty()}
	 * post	{@code uri == null || getURI().equals(uri)}
	 * post	{@code getSimulatedTimeUnit().equals(simulatedTimeUnit)}
	 * post	{@code getSimulationEngine().equals(simulationEngine)}
	 * </pre>
	 *
	 * @param uri				URI of the model.
	 * @param simulatedTimeUnit	time unit used for the simulation time.
	 * @param simulationEngine	simulation engine to which the model is attached.
	 * @throws Exception		<i>to do.</i>
	 */
	public				BoxWifiSimpleUserSILModel(
		String uri,
		TimeUnit simulatedTimeUnit,
		AtomicSimulatorI simulationEngine
		) throws Exception
	{
		super(uri, simulatedTimeUnit, simulationEngine);

		this.rg = new RandomDataGenerator();
		// set the logger to a standard simulation logger
		this.getSimulationEngine().setLogger(new StandardLogger());

		assert	BoxWifiSimpleUserSILModel.implementationInvariants(this) :
				new NeoSim4JavaException(
						"BoxWifiSimpleUserSILModel.implementationInvariants(this)");
		assert	BoxWifiSimpleUserSILModel.invariants(this) :
				new NeoSim4JavaException("BoxWifiSimpleUserSILModel.invariants(this)");
	}

	// -------------------------------------------------------------------------
	// Methods
	// -------------------------------------------------------------------------

	/**
	 * generate the next event in the test scenario; current implementation
	 * cycles through {@code SwitchOnBoxWifi}, {@code ActivateWifiBoxWifi},
	 * {@code DeactivateWifiBoxWifi} and {@code SwitchOffBoxWifi} in this order
	 * at random time intervals following gaussian distributions.
	 * 
	 * <p><strong>Contract</strong></p>
	 * 
	 * <pre>
	 * pre	{@code eventList.peek() != null}
	 * post	{@code eventList.peek() != null}
	 * </pre>
	 */
	protected void		generateNextEvent()
	{
		EventI current = this.eventList.peek();
		// compute the next event type given the current event
		ES_EventI nextEvent = null;
		
		if (current instanceof SwitchOffBoxWifi) {
			// compute the time of occurrence for the next box WiFi usage
			Time t2 = this.computeTimeOfNextUsage(current.getTimeOfOccurrence());
			// after switching off the box, the next event must be a switch on
			nextEvent = new SwitchOnBoxWifi(t2);
		} else if (current instanceof SwitchOnBoxWifi) {
			// compute the time of occurrence for WiFi activation
			Time t = this.computeTimeForWifiActivation(current.getTimeOfOccurrence());
			// after switching on (box only mode), activate WiFi
			nextEvent = new ActivateWifiBoxWifi(t);
		} else if (current instanceof ActivateWifiBoxWifi) {
			// compute the time of occurrence for WiFi deactivation
			Time t = this.computeTimeForWifiDeactivation(current.getTimeOfOccurrence());
			// after activating WiFi, deactivate it
			nextEvent = new DeactivateWifiBoxWifi(t);
		} else if (current instanceof DeactivateWifiBoxWifi) {
			// compute the time of occurrence for switching off
			Time t = this.computeTimeForSwitchOff(current.getTimeOfOccurrence());
			// after deactivating WiFi, switch off the box
			nextEvent = new SwitchOffBoxWifi(t);
		}
		
		// schedule the event to be executed by this model
		if (nextEvent != null) {
			this.scheduleEvent(nextEvent);
		}
	}

	/**
	 * compute the time for WiFi activation, adding a random delay to
	 * {@code from}.
	 * 
	 * <p><strong>Contract</strong></p>
	 * 
	 * <pre>
	 * pre	{@code from != null}
	 * post	{@code true}	// no postcondition.
	 * </pre>
	 *
	 * @param from	time from which a delay will be added.
	 * @return		the time for WiFi activation.
	 */
	protected Time		computeTimeForWifiActivation(Time from)
	{
		assert	from != null;

		// generate randomly the delay but force it to be greater than 0
		double delay = Math.max(this.rg.nextGaussian(BOX_ON_DURATION/4.0,
													 BOX_ON_DURATION/20.0),
								0.01); // at least 0.01 hour (36 seconds)
		// compute the new time by adding the delay to from
		Time t = from.add(new Duration(delay, this.getSimulatedTimeUnit()));
		return t;
	}

	/**
	 * compute the time for WiFi deactivation, adding a random delay to
	 * {@code from}.
	 * 
	 * <p><strong>Contract</strong></p>
	 * 
	 * <pre>
	 * pre	{@code from != null}
	 * post	{@code true}	// no postcondition.
	 * </pre>
	 *
	 * @param from	time from which a delay will be added.
	 * @return		the time for WiFi deactivation.
	 */
	protected Time		computeTimeForWifiDeactivation(Time from)
	{
		assert	from != null;

		// generate randomly the delay but force it to be greater than 0
		double delay = Math.max(this.rg.nextGaussian(WIFI_ACTIVATION_DURATION,
													 WIFI_ACTIVATION_DURATION/5.0),
								0.01); // at least 0.01 hour
		// compute the new time by adding the delay to from
		Time t = from.add(new Duration(delay, this.getSimulatedTimeUnit()));
		return t;
	}

	/**
	 * compute the time for switching off the box, adding a random delay to
	 * {@code from}.
	 * 
	 * <p><strong>Contract</strong></p>
	 * 
	 * <pre>
	 * pre	{@code from != null}
	 * post	{@code true}	// no postcondition.
	 * </pre>
	 *
	 * @param from	time from which a delay will be added.
	 * @return		the time for switching off.
	 */
	protected Time		computeTimeForSwitchOff(Time from)
	{
		assert	from != null;

		// generate randomly the delay but force it to be greater than 0
		double delay = Math.max(this.rg.nextGaussian(BOX_ON_DURATION/2.0,
													 BOX_ON_DURATION/10.0),
								0.01); // at least 0.01 hour
		// compute the new time by adding the delay to from
		Time t = from.add(new Duration(delay, this.getSimulatedTimeUnit()));
		return t;
	}

	/**
	 * compute the time of the next box WiFi usage, adding a random delay to
	 * {@code from}.
	 * 
	 * <p><strong>Contract</strong></p>
	 * 
	 * <pre>
	 * pre	{@code from != null}
	 * post	{@code true}	// no postcondition.
	 * </pre>
	 *
	 * @param from	time from which a delay will be added.
	 * @return		the time of the next box WiFi usage.
	 */
	protected Time		computeTimeOfNextUsage(Time from)
	{
		assert	from != null;

		// generate randomly the next time interval but force it to be
		// greater than 0
		double delay = Math.max(this.rg.nextGaussian(BOX_OFF_DURATION,
													 BOX_OFF_DURATION/4.0),
								0.1); // at least 0.1 hour (6 minutes)
		// compute the new time by adding the delay to from
		Time t = from.add(new Duration(delay, this.getSimulatedTimeUnit()));
		return t;
	}

	// -------------------------------------------------------------------------
	// DEVS simulation protocol
	// -------------------------------------------------------------------------

	/**
	 * @see fr.sorbonne_u.devs_simulation.es.models.AtomicES_Model#initialiseState(fr.sorbonne_u.devs_simulation.models.time.Time)
	 */
	@Override
	public void			initialiseState(Time initialTime)
	{
		super.initialiseState(initialTime);

		// reinitialise the random number generator to get good samples
		this.rg.reSeedSecure();

		// compute the time of occurrence for the first event
		Time t = this.computeTimeOfNextUsage(this.getCurrentStateTime());
		// schedule the first event (start with box ON, WiFi disabled)
		this.scheduleEvent(new SwitchOnBoxWifi(t));
		// re-initialisation of the time of occurrence of the next event
		// required here after adding a new event in the schedule.
		this.nextTimeAdvance = this.timeAdvance();
		this.timeOfNextEvent =
				this.getCurrentStateTime().add(this.getNextTimeAdvance());

		if (VERBOSE) {
			this.logMessage("Box WiFi user simulation begins.");
			this.logMessage("First event: SwitchOnBoxWifi at " + t.getSimulatedTime() + " " + this.getSimulatedTimeUnit());
		}
	}

	/**
	 * @see fr.sorbonne_u.devs_simulation.es.models.AtomicES_Model#output()
	 */
	@Override
	public ArrayList<EventI>	output()
	{
		// generate and schedule the next event
		if (this.eventList.peek() != null) {
			this.generateNextEvent();
		}
		// this will extract the next event from the event list and emit it
		return super.output();
	}

	/**
	 * @see fr.sorbonne_u.devs_simulation.models.interfaces.ModelI#endSimulation(fr.sorbonne_u.devs_simulation.models.time.Time)
	 */
	@Override
	public void			endSimulation(Time endTime)
	{
		if (VERBOSE) {
			this.logMessage("Box WiFi user simulation ends.");
		}
		super.endSimulation(endTime);
	}

	// -------------------------------------------------------------------------
	// Optional DEVS simulation protocol: simulation run parameters
	// -------------------------------------------------------------------------

	/** run parameter name for {@code BOX_ON_DURATION}.						*/
	public static final String		BOX_ON_DURATION_RPNAME = "BOX_ON_DURATION";
	/** run parameter name for {@code WIFI_ACTIVATION_DURATION}.				*/
	public static final String		WIFI_ACTIVATION_RPNAME = "WIFI_ACTIVATION_DURATION";
	/** run parameter name for {@code BOX_OFF_DURATION}.					*/
	public static final String		BOX_OFF_DURATION_RPNAME = "BOX_OFF_DURATION";

	/**
	 * @see fr.sorbonne_u.devs_simulation.models.interfaces.ModelI#setSimulationRunParameters(Map)
	 */
	@Override
	public void			setSimulationRunParameters(
		Map<String, Object> simParams
		) throws MissingRunParameterException
	{
		super.setSimulationRunParameters(simParams);

		// this gets the reference on the owner component which is required
		// to have simulation models able to make the component perform some
		// operations or tasks or to get the value of variables held by the
		// component when necessary.
		if (simParams.containsKey(
						AtomicSimulatorPlugin.OWNER_RUNTIME_PARAMETER_NAME)) {
			// by the following, all of the logging will appear in the owner
			// component logger
			this.getSimulationEngine().setLogger(
						AtomicSimulatorPlugin.createComponentLogger(simParams));
		}

		String boxOnName =
				ModelI.createRunParameterName(getURI(), BOX_ON_DURATION_RPNAME);
		if (simParams.containsKey(boxOnName)) {
			BOX_ON_DURATION = (double) simParams.get(boxOnName);
		}
		
		String wifiActivationName =
				ModelI.createRunParameterName(getURI(), WIFI_ACTIVATION_RPNAME);
		if (simParams.containsKey(wifiActivationName)) {
			WIFI_ACTIVATION_DURATION = (double) simParams.get(wifiActivationName);
		}
		
		String boxOffName =
				ModelI.createRunParameterName(getURI(), BOX_OFF_DURATION_RPNAME);
		if (simParams.containsKey(boxOffName)) {
			BOX_OFF_DURATION = (double) simParams.get(boxOffName);
		}
		
		if (VERBOSE) {
			this.logMessage("Run parameters set:");
			this.logMessage("  BOX_ON_DURATION: " + BOX_ON_DURATION + " hours");
			this.logMessage("  WIFI_ACTIVATION_DURATION: " + WIFI_ACTIVATION_DURATION + " hours");
			this.logMessage("  BOX_OFF_DURATION: " + BOX_OFF_DURATION + " hours");
		}
	}

	// -------------------------------------------------------------------------
	// Optional DEVS simulation protocol: simulation report
	// -------------------------------------------------------------------------

	/**
	 * @see fr.sorbonne_u.devs_simulation.models.interfaces.ModelI#getFinalReport()
	 */
	@Override
	public SimulationReportI	getFinalReport()
	{
		return null;
	}
}
//-----------------------------------------------------------------------------
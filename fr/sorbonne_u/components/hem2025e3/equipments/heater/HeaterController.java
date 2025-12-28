package fr.sorbonne_u.components.hem2025e3.equipments.heater;

// Copyright Jacques Malenfant, Sorbonne Universite.
// Jacques.Malenfant@lip6.fr
//
// This software is a computer program whose purpose is to provide a
// new implementation of the DEVS simulation standard for Java.
//
// This software is governed by the CeCILL-C license under French law and
// abiding by the rules of distribution of free software.  You can use,
// modify and/ or redistribute the software under the terms of the
// CeCILL-C license as circulated by CEA, CNRS and INRIA at the following
// URL "http://www.cecill.info".
//
// As a counterpart to the access to the source code and  rights to copy,
// modify and redistribute granted by the license, users are provided only
// with a limited warranty  and the software's author,  the holder of the
// economic rights,  and the successive licensors  have only  limited
// liability. 
//
// In this respect, the user's attention is drawn to the risks associated
// with loading,  using,  modifying and/or developing or reproducing the
// software by the user in light of its specific status of free software,
// that may mean  that it is complicated to manipulate,  and  that  also
// therefore means  that it is reserved for developers  and  experienced
// professionals having in-depth computer knowledge. Users are therefore
// encouraged to load and test the software's suitability as regards their
// requirements in conditions enabling the security of their systems and/or 
// data to be ensured and,  more generally, to use and operate it in the 
// same conditions as regards security. 
//
// The fact that you are presently reading this means that you have had
// knowledge of the CeCILL-C license and that you accept its terms.

import java.util.concurrent.TimeUnit;
import fr.sorbonne_u.components.AbstractComponent;
import fr.sorbonne_u.components.annotations.OfferedInterfaces;
import fr.sorbonne_u.components.annotations.RequiredInterfaces;
import fr.sorbonne_u.components.cyphy.ExecutionMode;
import fr.sorbonne_u.components.cyphy.utils.aclocks.ClocksServerWithSimulationCI;
import fr.sorbonne_u.components.exceptions.ComponentShutdownException;
import fr.sorbonne_u.components.exceptions.ComponentStartException;
import fr.sorbonne_u.components.hem2025e3.equipments.heater.HeaterCyPhy.HeaterState;
import fr.sorbonne_u.components.hem2025e3.equipments.heater.HeaterSensorDataCI.HeaterSensorRequiredPullCI;
import fr.sorbonne_u.components.hem2025e3.equipments.heater.connections.HeaterActuatorConnector;
import fr.sorbonne_u.components.hem2025e3.equipments.heater.connections.HeaterActuatorOutboundPort;
import fr.sorbonne_u.components.hem2025e3.equipments.heater.connections.HeaterSensorDataConnector;
import fr.sorbonne_u.components.hem2025e3.equipments.heater.connections.HeaterSensorDataOutboundPort;
import fr.sorbonne_u.components.hem2025e3.equipments.heater.sensor_data.HeaterTemperaturesSensorData;
import fr.sorbonne_u.components.hem2025e3.equipments.heater.sensor_data.TemperatureSensorData;
import fr.sorbonne_u.components.interfaces.DataRequiredCI;
import fr.sorbonne_u.exceptions.ImplementationInvariantException;
import fr.sorbonne_u.exceptions.AssertionChecking;
import fr.sorbonne_u.exceptions.InvariantException;
import fr.sorbonne_u.exceptions.PreconditionException;

// -----------------------------------------------------------------------------
/**
 * The class <code>ThermostatedHeaterController</code> implements a controller
 * component for the thermostated heater.
 *
 * <p><strong>Description</strong></p>
 * 
 * <p>
 * The controller is a simple fixed period threshold-based controller with
 * hysteresis. It has two control modes. In a pull mode, it calls the pull
 * sensors of the heater to get the target and current temperatures. In the
 * push mode, it sets the period for the heater to push the temperatures data
 * towards it and perform once its control decision upon each reception. It
 * also uses a push pattern to receive changes in the state of the heater. For
 * example, when the heater is switched on, it sends a state data telling the
 * controller that it is now on so that the controller can begins its control
 * until the heater is switched off.
 * </p>
 * 
 * <p><strong>Glass-box Invariants</strong></p>
 * 
 * <pre>
 * invariant	{@code hysteresis > 0.0}
 * invariant	{@code controlPeriod > 0}
 * invariant	{@code executionMode.isStandard() || clockURI != null && !clockURI.isEmpty()}
 * invariant	{@code sensorIBP_URI != null && !sensorIBP_URI.isEmpty()}
 * invariant	{@code actuatorIBPURI != null && !actuatorIBPURI.isEmpty()}
 * </pre>
 * 
 * <p><strong>Black-box Invariants</strong></p>
 * 
 * <pre>
 * invariant	{@code STANDARD_HYSTERESIS > 0.0}
 * invariant	{@code STANDARD_CONTROL_PERIOD > 0}
 * </pre>
 * 
 * <p>Created on : 2022-10-27</p>
 * 
 * @author	<a href="mailto:Jacques.Malenfant@lip6.fr">Jacques Malenfant</a>
 */
//-----------------------------------------------------------------------------
@RequiredInterfaces(required={HeaterSensorRequiredPullCI.class,
							  HeaterActuatorCI.class,
							  ClocksServerWithSimulationCI.class})
@OfferedInterfaces(offered={DataRequiredCI.PushCI.class})
//-----------------------------------------------------------------------------
public class			HeaterController
extends		AbstractComponent
implements	HeaterPushImplementationI
{
	// -------------------------------------------------------------------------
	// Inner types and classes
	// -------------------------------------------------------------------------

	public static enum	ControlMode {
		PULL,
		PUSH
	}

	// -------------------------------------------------------------------------
	// Constants and variables
	// -------------------------------------------------------------------------

	/** when tracing, x coordinate of the window relative position.			*/
	public static int			X_RELATIVE_POSITION = 0;
	/** when tracing, y coordinate of the window relative position.			*/
	public static int			Y_RELATIVE_POSITION = 0;
	/** when true, some methods trace their actions.						*/
	public static boolean		VERBOSE = true;
	/** when true, some methods trace their actions.						*/
	public static boolean		DEBUG = true;

	/** the standard hysteresis used by the controller.						*/
	public static final double	STANDARD_HYSTERESIS = 0.1;
	/** standard control period in seconds.									*/
	public static final double	STANDARD_CONTROL_PERIOD = 60.0;

	/** URI of the sensor inbound port on the {@code ThermostatedHeater}.	*/
	protected String							sensorIBP_URI;
	/** URI of the actuator inbound port on the {@code ThermostatedHeater}.	*/
	protected String							actuatorIBPURI;
	/** sensor data outbound port connected to the {@code Heater}.			*/
	protected HeaterSensorDataOutboundPort		sensorOutboundPort;
	/** actuator outbound port connected to the {@code Heater}.				*/
	protected HeaterActuatorOutboundPort		actuatorOutboundPort;

	/** the actual hysteresis used in the control loop.						*/
	protected double							hysteresis;
	/* user set control period in seconds.									*/
	protected double							controlPeriod;
	/** control mode (push or pull) for the current execution.				*/
	protected ControlMode						controlMode;
	/* actual control period, either in pure real time (not under test)
	 * or in accelerated time (under test), expressed in nanoseconds;
	 * used for scheduling the control task.								*/
	protected long								actualControlPeriod;
	/** the current state of the heater as perceived through the sensor
	 *  data received from the {@code ThermostatedHeater}.					*/
	protected HeaterState						currentState;
	/** lock controlling the access to {@code currentState}.				*/
	protected Object							stateLock;

	/** the current execution mode of the component: standard, test or
	 *  test with simulation SIL or HIL.									*/
	protected ExecutionMode						executionMode;

	// -------------------------------------------------------------------------
	// Invariants
	// -------------------------------------------------------------------------

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
		HeaterController instance
		)
	{
		assert	instance != null :
				new PreconditionException("instance != null");

		boolean ret = true;
		ret &= AssertionChecking.checkImplementationInvariant(
					instance.hysteresis > 0.0,
					HeaterController.class,
					instance,
					"hysteresis > 0.0");
		ret &= AssertionChecking.checkImplementationInvariant(
					instance.controlPeriod > 0,
					HeaterController.class,
					instance,
					"controlPeriod > 0");
		ret &= AssertionChecking.checkImplementationInvariant(
					instance.sensorIBP_URI != null &&
											!instance.sensorIBP_URI.isEmpty(),
					HeaterController.class, instance,
					"sensorIBP_URI != null && !sensorIBP_URI.isEmpty()");
		ret &= AssertionChecking.checkImplementationInvariant(
					instance.actuatorIBPURI != null &&
											!instance.actuatorIBPURI.isEmpty(),
					HeaterController.class, instance,
					"actuatorIBPURI != null && !actuatorIBPURI.isEmpty()");
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
	 * @return			true if the invariants are observed, false otherwise.
	 */
	protected static boolean	invariants(HeaterController instance)
	{
		assert	instance != null :
				new PreconditionException("instance != null");

		boolean ret = true;
		ret &= AssertionChecking.checkInvariant(
					X_RELATIVE_POSITION >= 0,
					HeaterController.class, instance,
					"X_RELATIVE_POSITION >= 0");
		ret &= AssertionChecking.checkInvariant(
					Y_RELATIVE_POSITION >= 0,
					HeaterController.class, instance,
					"Y_RELATIVE_POSITION >= 0");
		ret &= AssertionChecking.checkInvariant(
					STANDARD_HYSTERESIS > 0.0,
					HeaterController.class,
					instance,
					"STANDARD_HYSTERESIS > 0.0");
		ret &= AssertionChecking.checkInvariant(
					STANDARD_CONTROL_PERIOD > 0,
					HeaterController.class,
					instance,
					"STANDARD_CONTROL_PERIOD > 0");
		return ret;
	}

	// -------------------------------------------------------------------------
	// Constructors
	// -------------------------------------------------------------------------

	// Standard execution

	/**
	 * create the controller component for standard executions.
	 * 
	 * <p><strong>Contract</strong></p>
	 * 
	 * <pre>
	 * pre	{@code !(this instanceof ComponentInterface)}
	 * pre	{@code sensorIBP_URI != null && !sensorIBP_URI.isEmpty()}
	 * pre	{@code actuatorIBP_URI != null && !actuatorIBP_URI.isEmpty()}
	 * post	{@code true}	// no postcondition.
	 * </pre>
	 *
	 * @param sensorIBP_URI		URI of the heater sensor inbound port.
	 * @param actuatorIBP_URI	URI of the heater actuator inbound port.
	 * @throws Exception		<i>to do</i>.
	 */
	protected			HeaterController(
		String sensorIBP_URI,
		String actuatorIBP_URI
		) throws Exception
	{
		this(sensorIBP_URI, actuatorIBP_URI,
			 STANDARD_HYSTERESIS, STANDARD_CONTROL_PERIOD,
			 ControlMode.PULL);
	}

	/**
	 * create the controller component for standard executions with the given
	 * control parameters.
	 * 
	 * <p><strong>Contract</strong></p>
	 * 
	 * <pre>
	 * pre	{@code !(this instanceof ComponentInterface)}
	 * pre	{@code sensorIBP_URI != null && !sensorIBP_URI.isEmpty()}
	 * pre	{@code actuatorIBP_URI != null && !actuatorIBP_URI.isEmpty()}
	 * pre	{@code hysteresis > 0.0}
	 * pre	{@code controlPeriod > 0}
	 * pre	{@code controlMode != null}
	 * post	{@code true}	// no postcondition.
	 * </pre>
	 *
	 * @param sensorIBP_URI		URI of the heater sensor inbound port.
	 * @param actuatorIBP_URI	URI of the heater actuator inbound port.
	 * @param hysteresis		control hysteresis around the target temperature.
	 * @param controlPeriod		control period in seconds.
	 * @param controlMode		control mode: {@code PULL} or {@code PUSH}.
	 * @throws Exception 		<i>to do</i>.
	 */
	protected			HeaterController(
		String sensorIBP_URI,
		String actuatorIBP_URI,
		double hysteresis,
		double controlPeriod,
		ControlMode controlMode
		) throws Exception
	{
		// two standard threads in case the thread that runs the method execute
		// can be prevented to run by the thread running receiveRunningState
		// the schedulable thread pool is used to run the control task
		super(3, 1);

		this.initialise(sensorIBP_URI, actuatorIBP_URI, hysteresis,
						controlPeriod, controlMode);

		this.executionMode = ExecutionMode.STANDARD;
		this.actualControlPeriod =
				(long)((this.controlPeriod * TimeUnit.SECONDS.toNanos(1)));
		// sanity checking, the standard Java scheduler has a
		// precision no less than 10 milliseconds...
		if (this.actualControlPeriod <
								TimeUnit.MILLISECONDS.toNanos(10)) {
			System.out.println(
					"Warning: accelerated control period is "
							+ "too small ("
							+ this.actualControlPeriod +
							"), unexpected scheduling problems may"
							+ " occur!");
		}
	}

	// Test executions, with or without simulation

	/**
	 * create the controller component with the given parameters.
	 * 
	 * <p><strong>Contract</strong></p>
	 * 
	 * <pre>
	 * pre	{@code !(this instanceof ComponentInterface)}
	 * pre	{@code sensorIBP_URI != null && !sensorIBP_URI.isEmpty()}
	 * pre	{@code actuatorIBP_URI != null && !actuatorIBP_URI.isEmpty()}
	 * pre	{@code hysteresis > 0.0}
	 * pre	{@code controlPeriod > 0}
	 * pre	{@code controlMode != null}
	 * pre	{@code executionMode.isSimulationTest()}
	 * pre	{@code accelerationFactor > 0.0}
	 * post	{@code getExecutionMode().equals(executionMode)}
	 * </pre>
	 *
	 * @param sensorIBP_URI			URI of the heater sensor inbound port.
	 * @param actuatorIBP_URI		URI of the heater actuator inbound port.
	 * @param hysteresis			control hysteresis around the target temperature.
	 * @param controlPeriod			control period in seconds.
	 * @param controlMode			control mode: {@code PULL} or {@code PUSH}.
	 * @param executionMode			execution mode for the next run.
	 * @param accelerationFactor	acceleration factor for the simulation.
	 * @throws Exception 			<i>to do</i>.
	 */
	protected			HeaterController(
		String sensorIBP_URI,
		String actuatorIBP_URI,
		double hysteresis,
		double controlPeriod,
		ControlMode controlMode,
		ExecutionMode executionMode,
		double accelerationFactor
		) throws Exception
	{
		// two standard threads in case the thread that runs the method execute
		// can be prevented to run by the thread running receiveRunningState
		// the schedulable thread pool is used to run the control task
		super(3, 1);

		assert	sensorIBP_URI != null && !sensorIBP_URI.isEmpty() :
				new PreconditionException(
						"sensorIBP_URI != null && !sensorIBP_URI.isEmpty()");
		assert	actuatorIBP_URI != null && !actuatorIBP_URI.isEmpty() :
				new PreconditionException(
					"actuatorIBP_URI != null && !actuatorIBP_URI.isEmpty()");
		assert	hysteresis > 0.0 :
				new PreconditionException("hysteresis > 0.0");
		assert	controlPeriod > 0 :
				new PreconditionException("controlPeriod > 0");
		assert	controlMode != null :
				new PreconditionException("controlMode != null");
		assert	executionMode.isSimulationTest() :
				new PreconditionException("executionMode.isSimulationTest()");
		assert	accelerationFactor > 0.0 :
				new PreconditionException("accelerationFactor > 0.0");

		this.initialise(sensorIBP_URI, actuatorIBP_URI, hysteresis,
						controlPeriod, controlMode);

		this.executionMode = executionMode;
		// the accelerated period is in nanoseconds, hence first convert
		// the period to nanoseconds, perform the division and then
		// convert to long (hence providing a better precision than
		// first dividing and then converting to nanoseconds...)
		this.actualControlPeriod =
			(long)((this.controlPeriod * TimeUnit.SECONDS.toNanos(1))/
														accelerationFactor);
		// sanity checking, the standard Java scheduler has a
		// precision no less than 10 milliseconds...
		if (this.actualControlPeriod <
								TimeUnit.MILLISECONDS.toNanos(10)) {
			System.out.println(
					"Warning: accelerated control period is "
							+ "too small ("
							+ this.actualControlPeriod +
							"), unexpected scheduling problems may"
							+ " occur!");
		}

		if (VERBOSE || DEBUG) {
			this.tracer.get().setTitle("Heater controller component");
			this.tracer.get().setRelativePosition(X_RELATIVE_POSITION,
												  Y_RELATIVE_POSITION);
			this.toggleTracing();
		}

		// Invariant checking
		assert	HeaterController.implementationInvariants(this) :
				new ImplementationInvariantException(
						"HeaterController.implementationInvariants(this)");
		assert	HeaterController.invariants(this) :
				new InvariantException("HeaterController.invariants(this)");
	}

	/**
	 * initialise the controller component with the given parameters.
	 * 
	 * <p><strong>Contract</strong></p>
	 * 
	 * <pre>
	 * pre	{@code sensorIBP_URI != null && !sensorIBP_URI.isEmpty()}
	 * pre	{@code actuatorIBP_URI != null && !actuatorIBP_URI.isEmpty()}
	 * pre	{@code hysteresis > 0.0}
	 * pre	{@code controlPeriod > 0}
	 * post	{@code true}	// no postcondition.
	 * </pre>
	 *
	 * @param sensorIBP_URI		URI of the heater sensor inbound port.
	 * @param actuatorIBP_URI	URI of the heater actuator inbound port.
	 * @param hysteresis		control hysteresis around the target temperature.
	 * @param controlPeriod		control period in seconds.
	 * @param controlMode		control mode: {@code PULL} or {@code PUSH}.
	 * @throws Exception 		<i>to do</i>.
	 */
	protected void		initialise(
		String sensorIBP_URI,
		String actuatorIBP_URI,
		double hysteresis,
		double controlPeriod,
		ControlMode controlMode
		) throws Exception
	{
		this.sensorIBP_URI = sensorIBP_URI;
		this.actuatorIBPURI = actuatorIBP_URI;
		this.hysteresis = hysteresis;
		this.controlPeriod = controlPeriod;
		this.controlMode = controlMode;
		this.stateLock = new Object();

		this.sensorOutboundPort = new HeaterSensorDataOutboundPort(this);
		this.sensorOutboundPort.publishPort();
		this.actuatorOutboundPort = new HeaterActuatorOutboundPort(this);
		this.actuatorOutboundPort.publishPort();
	}

	// -------------------------------------------------------------------------
	// Component life-cycle
	// -------------------------------------------------------------------------

	/**
	 * @see fr.sorbonne_u.components.AbstractComponent#start()
	 */
	@Override
	public synchronized void	start() throws ComponentStartException
	{
		super.start();

		try {
			this.doPortConnection(
					this.sensorOutboundPort.getPortURI(),
					sensorIBP_URI,
					HeaterSensorDataConnector.class.getCanonicalName());
			this.doPortConnection(
					this.actuatorOutboundPort.getPortURI(),
					this.actuatorIBPURI,
					HeaterActuatorConnector.class.getCanonicalName());

			synchronized (this.stateLock) {
				this.currentState = HeaterState.OFF;
			}

			if (VERBOSE) {
				this.traceMessage("Heater controller starts.\n");
			}
		} catch (Exception e) {
			throw new ComponentStartException(e);
		}
	}

	/**
	 * @see fr.sorbonne_u.components.AbstractComponent#finalise()
	 */
	@Override
	public synchronized void	finalise() throws Exception
	{
		if (VERBOSE) {
			this.traceMessage("Heater controller ends.\n");
		}
		this.doPortDisconnection(this.sensorOutboundPort.getPortURI());
		this.doPortDisconnection(this.actuatorOutboundPort.getPortURI());
		super.finalise();
	}

	/**
	 * @see fr.sorbonne_u.components.AbstractComponent#shutdown()
	 */
	@Override
	public synchronized void	shutdown() throws ComponentShutdownException
	{
		try {
			this.sensorOutboundPort.unpublishPort();
			this.actuatorOutboundPort.unpublishPort();
		} catch (Exception e) {
			throw new ComponentShutdownException(e) ;
		}
		super.shutdown();
	}

	// -------------------------------------------------------------------------
	// Component internal methods
	// -------------------------------------------------------------------------

	/**
	 * receive and process the state data coming from the heater component,
	 * starting the control loop if the state has changed from {@code OFF} to
	 * {@code ON}.
	 * 
	 * <p><strong>Contract</strong></p>
	 * 
	 * <pre>
	 * pre	{@code heaterState != null}
	 * post	{@code true}	// no postcondition.
	 * </pre>
	 *
	 * @param heaterState	state data received from the heater component.
	 */
	@Override
	public void			processHeaterState(HeaterState heaterState)
	{
		assert	heaterState != null :
				new PreconditionException("heaterState != null");

		if (DEBUG) {
			this.traceMessage("receives heater state: " + heaterState + ".\n");
		}

		// the current state is always updated, but only in the case
		// when the heater is switched on that the controller begins to
		// perform the temperature control
		synchronized (this.stateLock) {
			HeaterState oldState = this.currentState;
			this.currentState = heaterState;	

			if (heaterState != HeaterState.OFF && oldState == HeaterState.OFF) {
				if (this.controlMode == ControlMode.PULL) {
					if (VERBOSE) {
						this.traceMessage("start pull control.\n");
					}
					if (this.executionMode.isStandard() ||
								this.executionMode.isTestWithoutSimulation()) {
						this.pullControLoop();
					} else {
						// if a state change has been detected from OFF to ON,
						// schedule a first execution of the control loop, which
						// in turn will schedule its next execution if needed
						this.scheduleTaskOnComponent(
								new AbstractComponent.AbstractTask() {
									@Override
									public void run() {
										((HeaterController)
												this.getTaskOwner()).
															pullControLoop();
										}
									},
								this.actualControlPeriod, 
								TimeUnit.NANOSECONDS);
					}
				} else {
					if (VERBOSE) {
						this.traceMessage("start push control.\n");
					}
					long cp = (long) (TimeUnit.SECONDS.toMillis(1)
														* this.controlPeriod);
					try {
						this.sensorOutboundPort.
								startTemperaturesPushSensor(
													cp, TimeUnit.MILLISECONDS);
					} catch (Exception e) {
						throw new RuntimeException(e) ;
					}
				}
			}
		}
	}

	/**
	 * implement the push control loop.
	 * 
	 * @see fr.sorbonne_u.components.hem2025e3.equipments.heater.HeaterPushImplementationI#processTemperatures(fr.sorbonne_u.components.hem2025e3.equipments.heater.sensor_data.TemperatureSensorData, fr.sorbonne_u.components.hem2025e3.equipments.heater.sensor_data.TemperatureSensorData)
	 */
	public void			processTemperatures(
		TemperatureSensorData target,
		TemperatureSensorData current
		)
	{
		assert	target != null : new PreconditionException("target != null");
		assert	current != null : new PreconditionException("current != null");

		try {
			// execute the control only of the heater is still ON
			HeaterState s = HeaterState.OFF;
			synchronized (this.stateLock) {
				s = this.currentState;
			}
			if (s != HeaterState.OFF) {
				this.oneControlStep(target, current, s);
			} else {
				// when the heater is OFF, exit the control loop
				if (VERBOSE) {
					this.traceMessage("control is off.\n");
				}
			}
		} catch (Exception e) {
			throw new RuntimeException(e) ;
		}
	}

	/**
	 * 
	 * 
	 * <p><strong>Contract</strong></p>
	 * 
	 * <pre>
	 * pre	{@code true}	// no precondition.
	 * post	{@code true}	// no postcondition.
	 * </pre>
	 *
	 * @param target
	 * @param current
	 * @param priorState
	 * @throws Exception
	 */
	protected void		oneControlStep(
		TemperatureSensorData target,
		TemperatureSensorData current,
		HeaterState priorState
		) throws Exception
	{
		// tracing
		StringBuffer sb = new StringBuffer();
		double c = current.getMeasure().getData();
		double t = target.getMeasure().getData();
		if (c < t - this.hysteresis) {
			// the current room temperature is too low, start heating
			if (HeaterState.HEATING != priorState) {
				// actuate
				this.actuatorOutboundPort.startHeating();
				// update state
				this.currentState = HeaterState.HEATING;
			}
			if (VERBOSE) {
				StringBuffer temp = new StringBuffer();
				temp.append(current);
				temp.append(" < ");
				temp.append(target);
				if (HeaterState.HEATING != priorState) {
					sb.append("start heating with ");
					sb.append(temp);
					sb.append(" - ");
				} else {
					sb.append("still heating with ");
					sb.append(temp);
					sb.append(" + ");
				}
				sb.append(this.hysteresis);
				sb.append(" at ");
				sb.append(target.getTimestamp());
				sb.append(".\n");
				this.traceMessage(sb.toString());
			}
		} else if (c > t + this.hysteresis) {
			// the current room temperature is high enough, stop heating
			// tracing
			if (HeaterState.HEATING == priorState) {
				// actuate
				this.actuatorOutboundPort.stopHeating();;
				// update state
				this.currentState = HeaterState.ON;
			}
			StringBuffer temp = new StringBuffer();
			temp.append(current);
			temp.append(" > ");
			temp.append(target);
			if (VERBOSE) {
				if (HeaterState.HEATING == priorState) {
					sb.append("stop heating with ");
					sb.append(temp);
					sb.append(" + ");
				} else {
					sb.append("still not heating with ");
					sb.append(temp);
					sb.append(" - ");
				}
				sb.append(this.hysteresis);
				sb.append(" at ");
				sb.append(target.getTimestamp());
				sb.append(".\n");
				this.traceMessage(sb.toString());
			}
		} else {
			if (VERBOSE) {
				if (HeaterState.HEATING == priorState) {
					sb.append("still heating with ");
					sb.append(current);
					sb.append(" < ");
					sb.append(target);
					sb.append(" + ");
				} else {
					sb.append("still not heating with ");
					sb.append(current);
					sb.append(" > ");
					sb.append(target);
					sb.append(" - ");
			}
			sb.append(this.hysteresis);
			sb.append(" at ");
			sb.append(target.getTimestamp());
			sb.append(".\n");
				this.traceMessage(sb.toString());
			}
		}
	}

	/**
	 * implement the pull control loop.
	 * 
	 * <p><strong>Contract</strong></p>
	 * 
	 * <pre>
	 * pre	{@code true}	// no precondition.
	 * post	{@code true}	// no postcondition.
	 * </pre>
	 *
	 */
	protected void		pullControLoop()
	{
		try {
			// execute the control as long as the heater is ON
			HeaterState priorState = HeaterState.OFF;
			synchronized (this.stateLock) {
				priorState = this.currentState;
			}
			if (priorState != HeaterState.OFF) {
				// get the temperature data from the heater
				HeaterTemperaturesSensorData td =
						(HeaterTemperaturesSensorData)
										this.sensorOutboundPort.request();

				if (DEBUG) {
					this.traceMessage(
							"executes a new pull control step on " + td + "\n");
				}

				this.oneControlStep(td.getTargetTemperature(),
									td.getCurrentTemperature(),
									priorState);

				// schedule the next execution of the loop only if the
				// current execution is standard or if it is a real time
				// simulation with code execution i.e., SIL or HIL
				// otherwise, perform only one call to pull sensors to
				// test the functionality
				this.scheduleTask(
						o -> ((HeaterController)o).pullControLoop(),
						this.actualControlPeriod, 
						TimeUnit.NANOSECONDS);
			} else {
				// when the heater is OFF, exit the control loop
				if (VERBOSE) {
					this.traceMessage("exit the control.\n");
				}
			}
		} catch (Exception e) {
			throw new RuntimeException(e) ;
		}
	}
}
// -----------------------------------------------------------------------------

package fr.sorbonne_u.components.hem2025e3.equipments.heater;

// Copyright Jacques Malenfant, Sorbonne Universite.
// Jacques.Malenfant@lip6.fr
//
// This software is a computer program whose purpose is to implement a mock-up
// of household energy management system.
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

import fr.sorbonne_u.components.AbstractComponent;
import fr.sorbonne_u.components.AbstractPort;
import fr.sorbonne_u.components.annotations.OfferedInterfaces;
import fr.sorbonne_u.components.cyphy.AbstractCyPhyComponent;
import fr.sorbonne_u.components.cyphy.ExecutionMode;
import fr.sorbonne_u.components.cyphy.annotations.LocalArchitecture;
import fr.sorbonne_u.components.cyphy.annotations.SIL_Simulation_Architectures;
import fr.sorbonne_u.components.cyphy.interfaces.CyPhyReflectionCI;
import fr.sorbonne_u.components.cyphy.interfaces.ModelStateAccessI.VariableValue;
import fr.sorbonne_u.components.cyphy.plugins.devs.AtomicSimulatorPlugin;
import fr.sorbonne_u.components.cyphy.plugins.devs.RTAtomicSimulatorPlugin;
import fr.sorbonne_u.components.cyphy.utils.aclocks.ClocksServerWithSimulation;
import fr.sorbonne_u.components.cyphy.utils.tests.TestScenarioWithSimulation;
import fr.sorbonne_u.components.exceptions.BCMException;
import fr.sorbonne_u.components.exceptions.BCMRuntimeException;
import fr.sorbonne_u.components.exceptions.ComponentShutdownException;
import fr.sorbonne_u.components.exceptions.ComponentStartException;
import fr.sorbonne_u.components.hem2025e1.equipments.heater.HeaterExternalControlI;
import fr.sorbonne_u.components.hem2025e1.equipments.heater.HeaterExternalControlJava4CI;
import fr.sorbonne_u.components.hem2025e1.equipments.heater.HeaterInternalControlCI;
import fr.sorbonne_u.components.hem2025e1.equipments.heater.HeaterInternalControlI;
import fr.sorbonne_u.components.hem2025e1.equipments.heater.HeaterTemperatureI;
import fr.sorbonne_u.components.hem2025e1.equipments.heater.HeaterUserI;
import fr.sorbonne_u.components.hem2025e1.equipments.heater.HeaterUserJava4CI;
import fr.sorbonne_u.components.hem2025e1.equipments.heater.connections.HeaterExternalControlJava4InboundPort;
import fr.sorbonne_u.components.hem2025e1.equipments.heater.connections.HeaterInternalControlInboundPort;
import fr.sorbonne_u.components.hem2025e1.equipments.heater.connections.HeaterUserJava4InboundPort;
import fr.sorbonne_u.components.hem2025e2.equipments.heater.mil.events.DoNotHeat;
import fr.sorbonne_u.components.hem2025e2.equipments.heater.mil.events.Heat;
import fr.sorbonne_u.components.hem2025e2.equipments.heater.mil.events.SwitchOffHeater;
import fr.sorbonne_u.components.hem2025e2.equipments.heater.mil.events.SwitchOnHeater;
import fr.sorbonne_u.components.hem2025e3.equipments.heater.connections.HeaterActuatorInboundPort;
import fr.sorbonne_u.components.hem2025e3.equipments.heater.connections.HeaterSensorDataInboundPort;
import fr.sorbonne_u.components.hem2025e3.equipments.heater.sensor_data.HeaterStateSensorData;
import fr.sorbonne_u.components.hem2025e3.equipments.heater.sensor_data.HeaterTemperaturesSensorData;
import fr.sorbonne_u.components.hem2025e3.equipments.heater.sensor_data.HeatingSensorData;
import fr.sorbonne_u.components.hem2025e3.equipments.heater.sensor_data.TemperatureSensorData;
import fr.sorbonne_u.components.hem2025e3.equipments.heater.sil.HeaterStateSILModel;
import fr.sorbonne_u.components.hem2025e3.equipments.heater.sil.HeaterTemperatureSILModel;
import fr.sorbonne_u.components.hem2025e3.equipments.heater.sil.Local_SIL_SimulationArchitectures;
import fr.sorbonne_u.components.hem2025e3.equipments.heater.sil.events.SIL_SetPowerHeater;
import fr.sorbonne_u.components.hem2025e3.equipments.heater.sil.events.SIL_SetPowerHeater.PowerValue;
import fr.sorbonne_u.components.utils.tests.TestScenario;
import fr.sorbonne_u.devs_simulation.architectures.RTArchitecture;
import fr.sorbonne_u.devs_simulation.models.annotations.ModelExternalEvents;
import fr.sorbonne_u.exceptions.ImplementationInvariantException;
import fr.sorbonne_u.exceptions.AssertionChecking;
import fr.sorbonne_u.exceptions.InvariantException;
import fr.sorbonne_u.exceptions.PostconditionException;
import fr.sorbonne_u.exceptions.PreconditionException;
import fr.sorbonne_u.utils.aclocks.AcceleratedClock;
import fr.sorbonne_u.utils.aclocks.ClocksServer;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;
import fr.sorbonne_u.alasca.physical_data.Measure;
import fr.sorbonne_u.alasca.physical_data.MeasureI;
import fr.sorbonne_u.alasca.physical_data.SignalData;
import fr.sorbonne_u.alasca.physical_data.TimedMeasure;

// -----------------------------------------------------------------------------
/**
 * The class <code>Heater</code> implements a heater component.
 *
 * <p><strong>Description</strong></p>
 * 
 * <p><strong>Implementation Invariants</strong></p>
 * 
 * <pre>
 * invariant	{@code currentState != null}
 * invariant	{@code targetTemperature == null || targetTemperature.getMeasurementUnit().equals(TEMPERATURE_UNIT)}
 * invariant	{@code targetTemperature == null || targetTemperature.getData() >= MIN_TARGET_TEMPERATURE.getData() && targetTemperature.getData() <= MAX_TARGET_TEMPERATURE.getData()}
 * invariant	{@code currentPowerLevel == null || currentPowerLevel.getMeasurementUnit().equals(POWER_UNIT)}
 * invariant	{@code currentPowerLevel == null || currentPowerLevel.getData() >= 0.0 && currentPowerLevel.getData() <= MAX_POWER_LEVEL.getData()}
 * </pre>
 * 
 * <p><strong>Invariants</strong></p>
 * 
 * <pre>
 * invariant	{@code REFLECTION_INBOUND_PORT_URI != null && !REFLECTION_INBOUND_PORT_URI.isEmpty()}
 * invariant	{@code USER_INBOUND_PORT_URI != null && !USER_INBOUND_PORT_URI.isEmpty()}
 * invariant	{@code INTERNAL_CONTROL_INBOUND_PORT_URI != null && !INTERNAL_CONTROL_INBOUND_PORT_URI.isEmpty()}
 * invariant	{@code EXTERNAL_CONTROL_INBOUND_PORT_URI != null && !EXTERNAL_CONTROL_INBOUND_PORT_URI.isEmpty()}
 * invariant	{@code X_RELATIVE_POSITION >= 0}
 * invariant	{@code Y_RELATIVE_POSITION >= 0}
 * </pre>
 * 
 * <p>Created on : 2023-09-18</p>
 * 
 * @author	<a href="mailto:Jacques.Malenfant@lip6.fr">Jacques Malenfant</a>
 */
//-----------------------------------------------------------------------------
@SIL_Simulation_Architectures({
	// fragile: fields in annotations cannot be defined by a class constant due
	// to Java annotations field initialisers limited to static values only
	@LocalArchitecture(
		// must be equal to UNIT_TEST_ARCHITECTURE_URI
		uri = "silUnitTests",
		// must be equal to the URI of the instance of HeaterCoupledModel
		rootModelURI = "HeaterCoupledModel",
		// next fields must be the same as the values used in the local
		// architecture
		simulatedTimeUnit = TimeUnit.HOURS,
		externalEvents = @ModelExternalEvents()
		),
	@LocalArchitecture(
		// must be equal to INTEGRATION_TEST_ARCHITECTURE_URI
		uri = "silIntegrationTests",
		// must be equal to the URI of the instance of HairDryerStateModel
		rootModelURI = "HeaterCoupledModel",
		// next fields must be the same as the values used in the local
		// architecture
		simulatedTimeUnit = TimeUnit.HOURS,
		externalEvents =
			@ModelExternalEvents(
				exported = {SwitchOnHeater.class,
							SIL_SetPowerHeater.class,
							SwitchOffHeater.class,
							Heat.class,
							DoNotHeat.class}
			)
		)
})
//-----------------------------------------------------------------------------
@OfferedInterfaces(offered={HeaterUserJava4CI.class,
							HeaterInternalControlCI.class,
							HeaterExternalControlJava4CI.class,
							HeaterSensorDataCI.HeaterSensorOfferedPullCI.class,
							HeaterActuatorCI.class})
//-----------------------------------------------------------------------------
public class			HeaterCyPhy
extends		AbstractCyPhyComponent
implements	HeaterUserI,
			HeaterInternalControlI
{
	// -------------------------------------------------------------------------
	// Inner interfaces and types
	// -------------------------------------------------------------------------

	/**
	 * The enumeration <code>HeaterState</code> describes the operation
	 * states of the heater.
	 *
	 * <p><strong>Description</strong></p>
	 * 
	 * <p>Created on : 2021-09-10</p>
	 * 
	 * @author	<a href="mailto:Jacques.Malenfant@lip6.fr">Jacques Malenfant</a>
	 */
	public static enum	HeaterState
	{
		/** heater is on, but not heating.									*/
		ON,
		/** heater is heating.												*/
		HEATING,
		/** heater is off.													*/
		OFF
	}

	// -------------------------------------------------------------------------
	// Constants and variables
	// -------------------------------------------------------------------------

	// BCM4Java information

	/** URI of the heater inbound port used in tests.						*/
	public static final String		REFLECTION_INBOUND_PORT_URI =
															"Heater-RIP-URI";	

	/** URI of the heater port for user interactions.						*/
	public static final String		USER_INBOUND_PORT_URI =
												"HEATER-USER-INBOUND-PORT-URI";
	/** URI of the heater port for internal control.						*/
	public static final String		INTERNAL_CONTROL_INBOUND_PORT_URI =
									"HEATER-INTERNAL-CONTROL-INBOUND-PORT-URI";
	/** URI of the heater port for internal control.						*/
	public static final String		EXTERNAL_CONTROL_INBOUND_PORT_URI =
									"HEATER-EXTERNAL-CONTROL-INBOUND-PORT-URI";
	/** URI of the heater sensor data inbound port.							*/
	public static final String		SENSOR_INBOUND_PORT_URI =
											"HEATER-SENSOR-INBOUND-PORT-URI";
	/** URI of the heater actuator inbound port.							*/
	public static final String		ACTUATOR_INBOUND_PORT_URI =
											"HEATER-ACTUATOR-INBOUND-PORT-URI";


	/** inbound port offering the <code>HeaterUserCI</code> interface.		*/
	protected HeaterUserJava4InboundPort			hip;
	/** inbound port offering the <code>HeaterInternalControlCI</code>
	 *  interface.															*/
	protected HeaterInternalControlInboundPort		hicip;
	/** inbound port offering the <code>HeaterExternalControlCI</code>
	 *  interface.															*/
	protected HeaterExternalControlJava4InboundPort	hecip;

	// Appliance information
	
	/** standard target temperature for the heater in celsius.				*/
	protected static final Measure<Double>	STANDARD_TARGET_TEMPERATURE =
												new Measure<>(
														19.0,
														TEMPERATURE_UNIT);
	/** fake current temperature, used when testing without simulation. 	*/
	public static final SignalData<Double>	FAKE_CURRENT_TEMPERATURE =
												new SignalData<>(
													new Measure<>(
															10.0,
															TEMPERATURE_UNIT));

	/** current state (on, off) of the heater.								*/
	protected HeaterState			currentState;
	/**	current power level of the heater.									*/
	protected TimedMeasure<Double>	currentPowerLevel;
	/** target temperature for the heating.									*/
	protected TimedMeasure<Double>	targetTemperature;

	// Sensors/actuators

	/** the inbound port through which the sensors are called.				*/
	protected HeaterSensorDataInboundPort	sensorInboundPort;
	/** the inbound port through which the actuators are called.			*/
	protected HeaterActuatorInboundPort		actuatorInboundPort;

	// Execution/Simulation

	/** when true, methods trace their actions.								*/
	public static boolean			VERBOSE = true;
	/** when true, methods provides debugging traces of their actions.		*/
	public static boolean			DEBUG = false;
	/** when tracing, x coordinate of the window relative position.			*/
	public static int				X_RELATIVE_POSITION = 0;
	/** when tracing, y coordinate of the window relative position.			*/
	public static int				Y_RELATIVE_POSITION = 0;

	/** one thread for the method execute, which starts the local SIL
	 *  simulator and wait until the end of the simulation to get the
	 *  simulation report, and one to answer the calls to the component
	 *  services.															*/
	protected static int			NUMBER_OF_STANDARD_THREADS = 2;
	/** no need for statically defined schedulable threads.					*/
	protected static int			NUMBER_OF_SCHEDULABLE_THREADS = 0;

	/** URI of the local simulation architecture for SIL unit tests.		*/
	public static final String		UNIT_TEST_ARCHITECTURE_URI =
														"silUnitTests";
	/** URI of the local simulation architecture for SIL unit tests.		*/
	public static final String		INTEGRATION_TEST_ARCHITECTURE_URI =
														"silIntegrationTests";
	/** name used to access the value of the current temperature in the
	 *  the simulator when executing software-in-the-loop test.				*/
	protected static final String	CURRENT_TEMPERATURE_NAME =
														"currentTemperature";

	/** plug-in holding the local simulation architecture and simulators.	*/
	protected AtomicSimulatorPlugin	asp;
	/** URI of the local simulation architecture used to compose the global
	 *  simulation architecture or the empty string if the component does
	 *  not execute as a simulation.										*/
	protected final String			localArchitectureURI;
	/** acceleration factor to be used when running the real time
	 *  simulation.															*/
	protected final double			accelerationFactor;

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
	 * pre	{@code h != null}
	 * post	{@code true}	// no postcondition.
	 * </pre>
	 *
	 * @param h	instance to be tested.
	 * @return	true if the implementation invariants are observed, false otherwise.
	 */
	protected static boolean	implementationInvariants(HeaterCyPhy h)
	{
		assert	h != null : new PreconditionException("h != null");

		boolean ret = true;
		ret &= AssertionChecking.checkImplementationInvariant(
				h.currentState != null,
				HeaterCyPhy.class, h,
				"h.currentState != null");
		ret &= AssertionChecking.checkImplementationInvariant(
				h.targetTemperature == null ||
					h.targetTemperature.getData() >=
							MIN_TARGET_TEMPERATURE.getData() &&
					h.targetTemperature.getData() <=
								MAX_TARGET_TEMPERATURE.getData(),
				HeaterCyPhy.class, h,
				"targetTemperature == null || targetTemperature.getData() >= "
				+ "MIN_TARGET_TEMPERATURE.getData() && "
				+ "targetTemperature.getData() <= MIN_TARGET_TEMPERATURE.getData()");
		ret &= AssertionChecking.checkImplementationInvariant(
				h.currentPowerLevel == null ||
					h.currentPowerLevel.getData() >= 0.0 &&
							h.currentPowerLevel.getData() <=
													MAX_POWER_LEVEL.getData(),
				HeaterCyPhy.class, h,
				"currentPowerLevel == null || currentPowerLevel.getData() >= 0.0"
				+ " && currentPowerLevel.getData() <= MAX_POWER_LEVEL.getData()");
		return ret;
	}

	/**
	 * return true if the static invariants are observed, false otherwise.
	 * 
	 * <p><strong>Contract</strong></p>
	 * 
	 * <pre>
	 * pre	{@code h != null}
	 * post	{@code true}	// no postcondition.
	 * </pre>
	 *
	 * @return	true if the invariants are observed, false otherwise.
	 */
	public static boolean	staticInvariants()
	{
		boolean ret = true;
		ret &= HeaterTemperatureI.staticInvariants();
		ret &= HeaterExternalControlI.staticInvariants();
		ret &= AssertionChecking.checkStaticInvariant(
				REFLECTION_INBOUND_PORT_URI != null &&
									!REFLECTION_INBOUND_PORT_URI.isEmpty(),
				HeaterCyPhy.class,
				"REFLECTION_INBOUND_PORT_URI != null && "
								+ "!REFLECTION_INBOUND_PORT_URI.isEmpty()");
		ret &= AssertionChecking.checkStaticInvariant(
				USER_INBOUND_PORT_URI != null && !USER_INBOUND_PORT_URI.isEmpty(),
				HeaterCyPhy.class,
				"USER_INBOUND_PORT_URI != null && !USER_INBOUND_PORT_URI.isEmpty()");
		ret &= AssertionChecking.checkStaticInvariant(
				INTERNAL_CONTROL_INBOUND_PORT_URI != null &&
								!INTERNAL_CONTROL_INBOUND_PORT_URI.isEmpty(),
				HeaterCyPhy.class,
				"INTERNAL_CONTROL_INBOUND_PORT_URI != null && "
							+ "!INTERNAL_CONTROL_INBOUND_PORT_URI.isEmpty()");
		ret &= AssertionChecking.checkStaticInvariant(
				EXTERNAL_CONTROL_INBOUND_PORT_URI != null &&
								!EXTERNAL_CONTROL_INBOUND_PORT_URI.isEmpty(),
				HeaterCyPhy.class,
				"EXTERNAL_CONTROL_INBOUND_PORT_URI != null &&"
							+ "!EXTERNAL_CONTROL_INBOUND_PORT_URI.isEmpty()");
		ret &= AssertionChecking.checkStaticInvariant(
				X_RELATIVE_POSITION >= 0,
				HeaterCyPhy.class,
				"X_RELATIVE_POSITION >= 0");
		ret &= AssertionChecking.checkStaticInvariant(
				Y_RELATIVE_POSITION >= 0,
				HeaterCyPhy.class,
				"Y_RELATIVE_POSITION >= 0");
		return ret;
	}

	/**
	 * return true if the invariants are observed, false otherwise.
	 * 
	 * <p><strong>Contract</strong></p>
	 * 
	 * <pre>
	 * pre	{@code h != null}
	 * post	{@code true}	// no postcondition.
	 * </pre>
	 *
	 * @param h	instance to be tested.
	 * @return	true if the invariants are observed, false otherwise.
	 */
	protected static boolean	invariants(HeaterCyPhy h)
	{
		assert	h != null : new PreconditionException("h != null");

		boolean ret = true;
		ret &= staticInvariants();
		return ret;
	}

	// -------------------------------------------------------------------------
	// Constructors
	// -------------------------------------------------------------------------

	// Standard execution

	/**
	 * create a new heater.
	 * 
	 * <p><strong>Contract</strong></p>
	 * 
	 * <pre>
	 * pre	{@code !(this instanceof ComponentInterface)}
	 * post	{@code !on()}
	 * post	{@code isCurrentPowerLevel(HeaterExternalControlI.MAX_POWER_LEVEL)}
	 * post	{@code getTargetTemperature().equals(STANDARD_TARGET_TEMPERATURE)}
	 * post	{@code getExecutionMode().isStandard()}
	 * </pre>
	 * 
	 * @throws Exception <i>to do</i>.
	 */
	protected			HeaterCyPhy() throws Exception
	{
		this(USER_INBOUND_PORT_URI, INTERNAL_CONTROL_INBOUND_PORT_URI,
			 EXTERNAL_CONTROL_INBOUND_PORT_URI, SENSOR_INBOUND_PORT_URI,
			 ACTUATOR_INBOUND_PORT_URI);
	}

	/**
	 * create a new heater.
	 * 
	 * <p><strong>Contract</strong></p>
	 * 
	 * <pre>
	 * pre	{@code !(this instanceof ComponentInterface)}
	 * pre	{@code heaterUserInboundPortURI != null && !heaterUserInboundPortURI.isEmpty()}
	 * pre	{@code heaterInternalControlInboundPortURI != null && !heaterInternalControlInboundPortURI.isEmpty()}
	 * pre	{@code heaterExternalControlInboundPortURI != null && !heaterExternalControlInboundPortURI.isEmpty()}
	 * pre	{@code heaterSensorInboundPortURI != null && !heaterSensorInboundPortURI.isEmpty()}
	 * pre	{@code heaterActuatorInboundPortURI != null && !heaterActuatorInboundPortURI.isEmpty()}
	 * post	{@code !on()}
	 * post	{@code isCurrentPowerLevel(HeaterExternalControlI.MAX_POWER_LEVEL)}
	 * post	{@code getTargetTemperature().equals(STANDARD_TARGET_TEMPERATURE)}
	 * post	{@code getExecutionMode().isStandard()}
	 * </pre>
	 * 
	 * @param heaterUserInboundPortURI				URI of the inbound port to call the heater component for user interactions.
	 * @param heaterInternalControlInboundPortURI	URI of the inbound port to call the heater component for internal control.
	 * @param heaterExternalControlInboundPortURI	URI of the inbound port to call the heater component for external control.
	 * @param heaterSensorInboundPortURI			URI of the inbound port to call the heater component sensors.
	 * @param heaterActuatorInboundPortURI			URI of the inbound port to call the heater component actuators.
	 * @throws Exception							<i>to do</i>.
	 */
	protected			HeaterCyPhy(
		String heaterUserInboundPortURI,
		String heaterInternalControlInboundPortURI,
		String heaterExternalControlInboundPortURI,
		String heaterSensorInboundPortURI,
		String heaterActuatorInboundPortURI
		) throws Exception
	{
		this(AbstractPort.generatePortURI(CyPhyReflectionCI.class),
			 heaterUserInboundPortURI,
			 heaterInternalControlInboundPortURI,
			 heaterExternalControlInboundPortURI,
			 heaterSensorInboundPortURI,
			 heaterActuatorInboundPortURI
			 );
	}

	/**
	 * create a new heater.
	 * 
	 * <p><strong>Contract</strong></p>
	 * 
	 * <pre>
	 * pre	{@code !(this instanceof ComponentInterface)}
	 * pre	{@code reflectionInboundPortURI != null && !reflectionInboundPortURI.isEmpty()}
	 * pre	{@code heaterUserInboundPortURI != null && !heaterUserInboundPortURI.isEmpty()}
	 * pre	{@code heaterInternalControlInboundPortURI != null && !heaterInternalControlInboundPortURI.isEmpty()}
	 * pre	{@code heaterExternalControlInboundPortURI != null && !heaterExternalControlInboundPortURI.isEmpty()}
	 * pre	{@code heaterSensorInboundPortURI != null && !heaterSensorInboundPortURI.isEmpty()}
	 * pre	{@code heaterActuatorInboundPortURI != null && !heaterActuatorInboundPortURI.isEmpty()}
	 * post	{@code !on()}
	 * post	{@code isCurrentPowerLevel(HeaterExternalControlI.MAX_POWER_LEVEL)}
	 * post	{@code getTargetTemperature().getMeasure().equals(STANDARD_TARGET_TEMPERATURE)}
	 * post	{@code getExecutionMode().isStandard()}
	 * </pre>
	 * 
	 * @param reflectionInboundPortURI				URI of the reflection inbound port of the component.
	 * @param heaterUserInboundPortURI				URI of the inbound port to call the heater component for user interactions.
	 * @param heaterInternalControlInboundPortURI	URI of the inbound port to call the heater component for internal control.
	 * @param heaterExternalControlInboundPortURI	URI of the inbound port to call the heater component for external control.
	 * @param heaterSensorInboundPortURI			URI of the inbound port to call the heater component sensors.
	 * @param heaterActuatorInboundPortURI			URI of the inbound port to call the heater component actuators.
	 * @throws Exception							<i>to do</i>.
	 */
	protected			HeaterCyPhy(
		String reflectionInboundPortURI,
		String heaterUserInboundPortURI,
		String heaterInternalControlInboundPortURI,
		String heaterExternalControlInboundPortURI,
		String heaterSensorInboundPortURI,
		String heaterActuatorInboundPortURI
		) throws Exception
	{
		super(reflectionInboundPortURI,
			  NUMBER_OF_STANDARD_THREADS,
			  NUMBER_OF_SCHEDULABLE_THREADS);

		this.localArchitectureURI = null;
		this.accelerationFactor = 0.0;

		this.initialise(heaterUserInboundPortURI,
						heaterInternalControlInboundPortURI,
						heaterExternalControlInboundPortURI,
						heaterSensorInboundPortURI,
						heaterActuatorInboundPortURI);

//		assert	isCurrentPowerLevel(HeaterExternalControlI.MAX_POWER_LEVEL) :
//				new PostconditionException(
//						"isCurrentPowerLevel("
//						+ "HeaterExternalControlI.MAX_POWER_LEVEL)");
//		assert	getTargetTemperature().getMeasure().equals(
//												STANDARD_TARGET_TEMPERATURE) :
//				new PostconditionException(
//						"getTargetTemperature().getMeasure().equals("
//						+ "STANDARD_TARGET_TEMPERATURE)");

		assert	HeaterCyPhy.implementationInvariants(this) :
				new ImplementationInvariantException(
						"HeaterCyPhy.implementationInvariants(this)");
		assert	HeaterCyPhy.invariants(this) :
				new InvariantException("HeaterCyPhy.invariants(this)");
	}

	// Tests without simulation execution

	/**
	 * create a new heater.
	 * 
	 * <p><strong>Contract</strong></p>
	 * 
	 * <pre>
	 * pre	{@code !(this instanceof ComponentInterface)}
	 * pre	{@code executionMode != null && executionMode.isTestWithoutSimulation()}
	 * pre	{@code clockURI != null && !clockURI.isEmpty()}
	 * post	{@code !on()}
	 * post	{@code isCurrentPowerLevel(HeaterExternalControlI.MAX_POWER_LEVEL)}
	 * post	{@code getTargetTemperature().equals(STANDARD_TARGET_TEMPERATURE)}
	 * post	{@code getExecutionMode().equals(executionMode)}
	 * </pre>
	 * 
	 * @param executionMode	execution mode for the next run.
	 * @param clockURI		URI of a clock used to synchronise components.
	 * @throws Exception 	<i>to do</i>.
	 */
	protected			HeaterCyPhy(
		ExecutionMode executionMode,
		String clockURI
		) throws Exception
	{
		this(USER_INBOUND_PORT_URI, INTERNAL_CONTROL_INBOUND_PORT_URI,
			 EXTERNAL_CONTROL_INBOUND_PORT_URI, SENSOR_INBOUND_PORT_URI,
			 ACTUATOR_INBOUND_PORT_URI, executionMode, clockURI);
	}

	/**
	 * create a new heater for test executions without simulation with the given
	 * inbound port URIs.
	 * 
	 * <p><strong>Contract</strong></p>
	 * 
	 * <pre>
	 * pre	{@code !(this instanceof ComponentInterface)}
	 * pre	{@code heaterUserInboundPortURI != null && !heaterUserInboundPortURI.isEmpty()}
	 * pre	{@code heaterInternalControlInboundPortURI != null && !heaterInternalControlInboundPortURI.isEmpty()}
	 * pre	{@code heaterExternalControlInboundPortURI != null && !heaterExternalControlInboundPortURI.isEmpty()}
	 * pre	{@code heaterSensorInboundPortURI != null && !heaterSensorInboundPortURI.isEmpty()}
	 * pre	{@code heaterActuatorInboundPortURI != null && !heaterActuatorInboundPortURI.isEmpty()}
	 * pre	{@code executionMode != null && executionMode.isTestWithoutSimulation()}
	 * pre	{@code clockURI != null && !clockURI.isEmpty()}
	 * post	{@code !on()}
	 * post	{@code isCurrentPowerLevel(HeaterExternalControlI.MAX_POWER_LEVEL)}
	 * post	{@code getTargetTemperature().equals(STANDARD_TARGET_TEMPERATURE)}
	 * post	{@code getExecutionMode().equals(executionMode)}
	 * </pre>
	 * 
	 * @param heaterUserInboundPortURI				URI of the inbound port to call the heater component for user interactions.
	 * @param heaterInternalControlInboundPortURI	URI of the inbound port to call the heater component for internal control.
	 * @param heaterExternalControlInboundPortURI	URI of the inbound port to call the heater component for external control.
	 * @param heaterSensorInboundPortURI			URI of the inbound port to call the heater component sensors.
	 * @param heaterActuatorInboundPortURI			URI of the inbound port to call the heater component actuators.
	 * @param executionMode							execution mode for the next run.
	 * @param clockURI								URI of a clock used to synchronise components.
	 * @throws Exception							<i>to do</i>.
	 */
	protected			HeaterCyPhy(
		String heaterUserInboundPortURI,
		String heaterInternalControlInboundPortURI,
		String heaterExternalControlInboundPortURI,
		String heaterSensorInboundPortURI,
		String heaterActuatorInboundPortURI,
		ExecutionMode executionMode,
		String clockURI
		) throws Exception
	{
		this(AbstractPort.generatePortURI(CyPhyReflectionCI.class),
			 heaterUserInboundPortURI,
			 heaterInternalControlInboundPortURI,
			 heaterExternalControlInboundPortURI,
			 heaterSensorInboundPortURI,
			 heaterActuatorInboundPortURI,
			 executionMode,
			 clockURI);
	}

	/**
	 * create a new heater for test executions without simulation with the given
	 * inbound port URIs.
	 * 
	 * <p><strong>Contract</strong></p>
	 * 
	 * <pre>
	 * pre	{@code !(this instanceof ComponentInterface)}
	 * pre	{@code reflectionInboundPortURI != null && !reflectionInboundPortURI.isEmpty()}
	 * pre	{@code heaterUserInboundPortURI != null && !heaterUserInboundPortURI.isEmpty()}
	 * pre	{@code heaterInternalControlInboundPortURI != null && !heaterInternalControlInboundPortURI.isEmpty()}
	 * pre	{@code heaterExternalControlInboundPortURI != null && !heaterExternalControlInboundPortURI.isEmpty()}
	 * pre	{@code heaterSensorInboundPortURI != null && !heaterSensorInboundPortURI.isEmpty()}
	 * pre	{@code heaterActuatorInboundPortURI != null && !heaterActuatorInboundPortURI.isEmpty()}
	 * pre	{@code executionMode != null && executionMode.isTestWithoutSimulation()}
	 * pre	{@code clockURI != null && !clockURI.isEmpty()}
	 * post	{@code !on()}
	 * post	{@code isCurrentPowerLevel(HeaterExternalControlI.MAX_POWER_LEVEL)}
	 * post	{@code getTargetTemperature().getMeasure().equals(STANDARD_TARGET_TEMPERATURE)}
	 * post	{@code getExecutionMode().equals(executionMode)}
	 * </pre>
	 * 
	 * @param reflectionInboundPortURI				URI of the reflection inbound port of the component.
	 * @param heaterUserInboundPortURI				URI of the inbound port to call the heater component for user interactions.
	 * @param heaterInternalControlInboundPortURI	URI of the inbound port to call the heater component for internal control.
	 * @param heaterExternalControlInboundPortURI	URI of the inbound port to call the heater component for external control.
	 * @param heaterSensorInboundPortURI			URI of the inbound port to call the heater component sensors.
	 * @param heaterActuatorInboundPortURI			URI of the inbound port to call the heater component actuators.
	 * @param executionMode							execution mode for the next run.
	 * @param clockURI								URI of a clock used to synchronise components.
	 * @throws Exception							<i>to do</i>.
	 */
	protected			HeaterCyPhy(
		String reflectionInboundPortURI,
		String heaterUserInboundPortURI,
		String heaterInternalControlInboundPortURI,
		String heaterExternalControlInboundPortURI,
		String heaterSensorInboundPortURI,
		String heaterActuatorInboundPortURI,
		ExecutionMode executionMode,
		String clockURI
		) throws Exception
	{
		super(reflectionInboundPortURI,
			  NUMBER_OF_STANDARD_THREADS,
			  NUMBER_OF_SCHEDULABLE_THREADS,
			  executionMode,
			  clockURI,
			  null);

		assert	executionMode != null &&
									executionMode.isTestWithoutSimulation() :
				new PreconditionException(
						"executionMode != null && executionMode."
						+ "isTestWithoutSimulation()");

		this.localArchitectureURI = null;
		this.accelerationFactor = 0.0;

		this.initialise(heaterUserInboundPortURI,
						heaterInternalControlInboundPortURI,
						heaterExternalControlInboundPortURI,
						heaterSensorInboundPortURI,
						heaterActuatorInboundPortURI);

//		assert	isCurrentPowerLevel(HeaterExternalControlI.MAX_POWER_LEVEL) :
//				new PostconditionException(
//						"isCurrentPowerLevel("
//						+ "HeaterExternalControlI.MAX_POWER_LEVEL)");
//		assert	getTargetTemperature().getMeasure().equals(
//												STANDARD_TARGET_TEMPERATURE) :
//				new PostconditionException(
//						"getTargetTemperature().equals("
//						+ "STANDARD_TARGET_TEMPERATURE)");

		assert	HeaterCyPhy.implementationInvariants(this) :
				new ImplementationInvariantException(
						"HeaterCyPhy.implementationInvariants(this)");
		assert	HeaterCyPhy.invariants(this) :
				new InvariantException("HeaterCyPhy.invariants(this)");
	}

	// Tests with simulation

	/**
	 * create a new heater for test executions with simulation .
	 * 
	 * <p><strong>Contract</strong></p>
	 * 
	 * <pre>
	 * pre	{@code !(this instanceof ComponentInterface)}
	 * pre	{@code reflectionInboundPortURI != null && !reflectionInboundPortURI.isEmpty()}
	 * pre	{@code heaterUserInboundPortURI != null && !heaterUserInboundPortURI.isEmpty()}
	 * pre	{@code heaterInternalControlInboundPortURI != null && !heaterInternalControlInboundPortURI.isEmpty()}
	 * pre	{@code heaterExternalControlInboundPortURI != null && !heaterExternalControlInboundPortURI.isEmpty()}
	 * pre	{@code heaterSensorInboundPortURI != null && !heaterSensorInboundPortURI.isEmpty()}
	 * pre	{@code heaterActuatorInboundPortURI != null && !heaterActuatorInboundPortURI.isEmpty()}
	 * pre	{@code executionMode != null && executionMode.isSimulationTest()}
	 * pre	{@code testScenario == null}
	 * pre	{@code localArchitectureURI != null && !localArchitectureURI.isEmpty()}
	 * pre	{@code accelerationFactor > 0.0}
	 * post	{@code !on()}
	 * post	{@code isCurrentPowerLevel(HeaterExternalControlI.MAX_POWER_LEVEL)}
	 * post	{@code getTargetTemperature().getMeasure().equals(STANDARD_TARGET_TEMPERATURE)}
	 * post	{@code getExecutionMode().equals(executionMode)}
	 * </pre>
	 * 
	 * @param reflectionInboundPortURI				URI of the reflection inbound port of the component.
	 * @param heaterUserInboundPortURI				URI of the inbound port to call the heater component for user interactions.
	 * @param heaterInternalControlInboundPortURI	URI of the inbound port to call the heater component for internal control.
	 * @param heaterExternalControlInboundPortURI	URI of the inbound port to call the heater component for external control.
	 * @param heaterSensorInboundPortURI			URI of the inbound port to call the heater component sensors.
	 * @param heaterActuatorInboundPortURI			URI of the inbound port to call the heater component actuators.
	 * @param executionMode							execution mode for the next run.
	 * @param testScenario							test scenario to be executed with this component.
	 * @param localArchitectureURI					URI of the local simulation architecture to be used in composing the global simulation architecture.
	 * @param accelerationFactor					acceleration factor for the simulation.
	 * @throws Exception							<i>to do</i>.
	 */
	protected			HeaterCyPhy(
		String reflectionInboundPortURI,
		String heaterUserInboundPortURI,
		String heaterInternalControlInboundPortURI,
		String heaterExternalControlInboundPortURI,
		String heaterSensorInboundPortURI,
		String heaterActuatorInboundPortURI,
		ExecutionMode executionMode,
		TestScenario testScenario,
		String localArchitectureURI,
		double accelerationFactor
		) throws Exception
	{
		super(reflectionInboundPortURI,
			  NUMBER_OF_STANDARD_THREADS,
			  NUMBER_OF_SCHEDULABLE_THREADS,
			  executionMode,
			  AssertionChecking.assertTrueAndReturnOrThrow(
				testScenario != null,
				testScenario.getClockURI(),
				() -> new PreconditionException("testScenario != null")),
			  testScenario,
			  ((Supplier<Set<String>>)() ->
			  		{ HashSet<String> hs = new HashSet<>();
			  		  hs.add(UNIT_TEST_ARCHITECTURE_URI);
			  		  hs.add(INTEGRATION_TEST_ARCHITECTURE_URI);
			  		  return hs;
			  		}).get(),
			  accelerationFactor
			 );

		assert	executionMode != null && executionMode.isSimulationTest() :
				new PreconditionException(
						"executionMode != null && "
						+ "executionMode.isSimulationTest()");

		this.localArchitectureURI = localArchitectureURI;
		this.accelerationFactor = accelerationFactor;

		this.initialise(heaterUserInboundPortURI,
						heaterInternalControlInboundPortURI,
						heaterExternalControlInboundPortURI,
						heaterSensorInboundPortURI,
						heaterActuatorInboundPortURI);

		if (DEBUG) {
			this.logMessage("HeaterCyPhy local simulation architectures: "
							+ this.localSimulationArchitectures);
		}

//		assert	isCurrentPowerLevel(HeaterExternalControlI.MAX_POWER_LEVEL) :
//				new PostconditionException(
//						"isCurrentPowerLevel("
//						+ "HeaterExternalControlI.MAX_POWER_LEVEL)");
//		assert	getTargetTemperature().getMeasure().equals(
//												STANDARD_TARGET_TEMPERATURE) :
//				new PostconditionException(
//						"getTargetTemperature().getMeasure().equals("
//						+ "STANDARD_TARGET_TEMPERATURE)");

		assert	HeaterCyPhy.implementationInvariants(this) :
				new ImplementationInvariantException(
						"HeaterCyPhy.implementationInvariants(this)");
		assert	HeaterCyPhy.invariants(this) :
				new InvariantException("HeaterCyPhy.invariants(this)");
	}

	// -------------------------------------------------------------------------
	// Initialisation methods
	// -------------------------------------------------------------------------

	/**
	 * initialise a new thermostated heater.
	 * 
	 * <p><strong>Contract</strong></p>
	 * 
	 * <pre>
	 * pre	{@code heaterUserInboundPortURI != null && !heaterUserInboundPortURI.isEmpty()}
	 * pre	{@code heaterInternalControlInboundPortURI != null && !heaterInternalControlInboundPortURI.isEmpty()}
	 * pre	{@code heaterExternalControlInboundPortURI != null && !heaterExternalControlInboundPortURI.isEmpty()}
	 * post	{@code true}	// no postcondition.
	 * </pre>
	 *
	 * @param heaterUserInboundPortURI				URI of the inbound port to call the heater component for user interactions.
	 * @param heaterInternalControlInboundPortURI	URI of the inbound port to call the heater component for internal control.
	 * @param heaterExternalControlInboundPortURI	URI of the inbound port to call the heater component for external control.
	 * @param heaterSensorInboundPortURI			URI of the inbound port to call the heater component sensors.
	 * @param heaterActuatorInboundPortURI			URI of the inbound port to call the heater component actuators.
	 * @throws Exception							<i>to do</i>.
	 */
	protected void		initialise(
		String heaterUserInboundPortURI,
		String heaterInternalControlInboundPortURI,
		String heaterExternalControlInboundPortURI,
		String heaterSensorInboundPortURI,
		String heaterActuatorInboundPortURI
		) throws Exception
	{
		this.currentState = HeaterState.OFF;

		this.hip = new HeaterUserJava4InboundPort(heaterUserInboundPortURI, this);
		this.hip.publishPort();
		this.hicip = new HeaterInternalControlInboundPort(
									heaterInternalControlInboundPortURI, this);
		this.hicip.publishPort();
		this.hecip = new HeaterExternalControlJava4InboundPort(
									heaterExternalControlInboundPortURI, this);
		this.hecip.publishPort();
		this.sensorInboundPort = new HeaterSensorDataInboundPort(
											heaterSensorInboundPortURI, this);
		this.sensorInboundPort.publishPort();
		this.actuatorInboundPort = new HeaterActuatorInboundPort(
											heaterActuatorInboundPortURI, this);
		this.actuatorInboundPort.publishPort();

		if (VERBOSE) {
			this.tracer.get().setTitle("Heater component");
			this.tracer.get().setRelativePosition(X_RELATIVE_POSITION,
												  Y_RELATIVE_POSITION);
			this.toggleTracing();		
		}

		assert	HeaterCyPhy.implementationInvariants(this) :
				new ImplementationInvariantException(
						"Heater.implementationInvariants(this)");
		assert	HeaterCyPhy.invariants(this) :
				new InvariantException("Heater.invariants(this)");
	}

	/**
	 * @see fr.sorbonne_u.components.cyphy.AbstractCyPhyComponent#createLocalSimulationArchitecture(java.lang.String, java.lang.String, java.util.concurrent.TimeUnit, double)
	 */
	@Override
	protected RTArchitecture	createLocalSimulationArchitecture(
		String architectureURI,
		String rootModelURI,
		TimeUnit simulatedTimeUnit,
		double accelerationFactor
		) throws Exception
	{
		// Preconditions checking
		assert	architectureURI != null && !architectureURI.isEmpty() :
				new PreconditionException(
						"architectureURI != null && !architectureURI.isEmpty()");
		assert	rootModelURI != null && !rootModelURI.isEmpty() :
				new PreconditionException(
						"rootModelURI != null && !rootModelURI.isEmpty()");
		assert	simulatedTimeUnit != null :
				new PreconditionException("simulatedTimeUnit != null");
		assert	accelerationFactor > 0.0 :
				new PreconditionException("accelerationFactor > 0.0");

		RTArchitecture ret = null;
		if (architectureURI.equals(UNIT_TEST_ARCHITECTURE_URI)) {
			ret = Local_SIL_SimulationArchitectures.
						createHeaterSIL_Architecture4UnitTest(
									architectureURI,
									rootModelURI,
									simulatedTimeUnit,
									accelerationFactor);
		} else if (architectureURI.equals(INTEGRATION_TEST_ARCHITECTURE_URI)) {
			ret = Local_SIL_SimulationArchitectures.
						createHeater_SIL_LocalArchitecture4IntegrationTest(
									architectureURI,
									rootModelURI,
									simulatedTimeUnit,
									accelerationFactor);
		} else {
			throw new BCMException("Unknown local simulation architecture "
								   + "URI: " + architectureURI);
		}
		
		return ret;
	}

	// -------------------------------------------------------------------------
	// Component internal methods
	// -------------------------------------------------------------------------

	/**
	 * return true if the current power level is equal to {@code powerLevel},
	 * otherwise false.
	 * 
	 * <p><strong>Contract</strong></p>
	 * 
	 * <pre>
	 * pre	{@code powerLevel != null}
	 * post	{@code true}	// no postcondition.
	 * </pre>
	 *
	 * @param powerLevel	a power level to be tested.
	 * @return				true if the current power level is equal to {@code powerLevel}, otherwise false.
	 */
	public boolean		isCurrentPowerLevel(MeasureI<Double> powerLevel)
	{
		assert	powerLevel != null :
				new PreconditionException("powerLevel != null");

		return this.currentPowerLevel.equals(powerLevel);
	}

	/**
	 * return true if the current temperature is equal to {@code temperature},
	 * otherwise false.
	 * 
	 * <p><strong>Contract</strong></p>
	 * 
	 * <pre>
	 * pre	{@code temperature != null}
	 * post	{@code true}	// no postcondition.
	 * </pre>
	 *
	 * @param temperature	a temperature to be tested.
	 * @return				true if the current temperature is equal to {@code temperature}, otherwise false.
	 */
	public boolean		isCurrentTemperature(Measure<Double> temperature)
	{
		assert	temperature != null :
				new PreconditionException("temperature != null");

		try {
			return this.getCurrentTemperature().getMeasure().equals(temperature);
		} catch (Exception e) {
			throw new BCMRuntimeException(e) ;
		}
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

		assert	HeaterCyPhy.implementationInvariants(this) :
				new ImplementationInvariantException(
						"HeaterCyPhy.implementationInvariants(this)");
		assert	HeaterCyPhy.invariants(this) :
				new InvariantException("HeaterCyPhy.invariants(this)");

		// create the simulation plug-in given the current type of simulation
		// and its local architecture i.e., for the current execution
		try {
			switch (this.getExecutionMode()) {
			case STANDARD:
			case UNIT_TEST:
			case INTEGRATION_TEST:
				break;
			case UNIT_TEST_WITH_SIL_SIMULATION:
			case INTEGRATION_TEST_WITH_SIL_SIMULATION:
				RTArchitecture architecture =
					(RTArchitecture) this.localSimulationArchitectures.
												get(this.localArchitectureURI);
				// add to the standard plug-in the method getModelStateValue
				// from the interface ModelStateAccessI used to get the
				// current temperature from the simulator
				this.asp = new RTAtomicSimulatorPlugin() {
					private static final long serialVersionUID = 1L;
					/**
					 * @see fr.sorbonne_u.components.cyphy.plugins.devs.AtomicSimulatorPlugin#getModelStateValue(java.lang.String, java.lang.String)
					 */
					@SuppressWarnings("unchecked")
					@Override
					public VariableValue<Double>	getModelVariableValue(
						String modelURI,
						String name
						) throws Exception
					{
						assert	modelURI.equals(HeaterTemperatureSILModel.URI);
						assert	name.equals(CURRENT_TEMPERATURE_NAME);

						return ((HeaterTemperatureSILModel)
										this.atomicSimulators.get(modelURI).
												getSimulatedModel()).
														getCurrentTemperature();
					}
				};
				((RTAtomicSimulatorPlugin)this.asp).
								setPluginURI(architecture.getRootModelURI());
				((RTAtomicSimulatorPlugin)this.asp).
										setSimulationArchitecture(architecture);
				this.installPlugin(this.asp);
				// the simulator inside the plug-in is created
				this.asp.createSimulator();
				// to prepare for the run, set the run parameters
				this.asp.setSimulationRunParameters(
								(TestScenarioWithSimulation)this.testScenario,
								new HashMap<>());
				break;
			case UNIT_TEST_WITH_HIL_SIMULATION:
			case INTEGRATION_TEST_WITH_HIL_SIMULATION:
				throw new BCMException("HIL simulation not implemented yet!");
			default:
			}		
		} catch (Exception e) {
			throw new ComponentStartException(e) ;
		}		

		assert	HeaterCyPhy.implementationInvariants(this) :
				new ImplementationInvariantException(
						"HeaterCyPhy.implementationInvariants(this)");
		assert	HeaterCyPhy.invariants(this) :
				new InvariantException("HeaterCyPhyr.invariants(this)");
	}

	/**
	 * @see fr.sorbonne_u.components.AbstractComponent#execute()
	 */
	@Override
	public void			execute() throws Exception
	{
		this.traceMessage("Heater CyPhy executes.\n");

		assert	HeaterCyPhy.implementationInvariants(this) :
				new ImplementationInvariantException(
						"HeaterCyPhy.implementationInvariants(this)");
		assert	HeaterCyPhy.invariants(this) :
				new InvariantException("HeaterCyPhyr.invariants(this)");

		switch (this.getExecutionMode()) {
		case STANDARD:
			this.currentPowerLevel =
					new TimedMeasure<Double>(
							MAX_POWER_LEVEL.getData(),
							MAX_POWER_LEVEL.getMeasurementUnit());
			this.targetTemperature =
					new TimedMeasure<Double>(
							STANDARD_TARGET_TEMPERATURE.getData(),
							STANDARD_TARGET_TEMPERATURE.getMeasurementUnit());
			break;
		case UNIT_TEST:
		case INTEGRATION_TEST:
			this.initialiseClock(
					ClocksServer.STANDARD_INBOUNDPORT_URI,
					this.clockURI);
			this.currentPowerLevel =
					new TimedMeasure<Double>(
							MAX_POWER_LEVEL.getData(),
							MAX_POWER_LEVEL.getMeasurementUnit(),
							this.getClock(),
							this.getClock().getStartInstant());
			this.targetTemperature =
					new TimedMeasure<Double>(
							STANDARD_TARGET_TEMPERATURE.getData(),
							STANDARD_TARGET_TEMPERATURE.getMeasurementUnit(),
							this.getClock(),
							this.getClock().getStartInstant());
			break;
		case UNIT_TEST_WITH_SIL_SIMULATION:
			// First, the component must synchronise with other components
			// to start the execution of the test scenario; we use a
			// time-triggered synchronisation scheme with the accelerated clock
			this.initialiseClock4Simulation(
					ClocksServerWithSimulation.STANDARD_INBOUNDPORT_URI,
					this.clockURI);
			this.asp.initialiseSimulation(
					this.getClock4Simulation().getSimulatedStartTime(),
					this.getClock4Simulation().getSimulatedDuration());
			// schedule the start of the SIL (real time) simulation
			this.asp.startRTSimulation(
					TimeUnit.NANOSECONDS.toMillis(
							this.getClock4Simulation().getStartEpochNanos()),
					this.getClock4Simulation().getSimulatedStartTime().
														getSimulatedTime(),
					this.getClock4Simulation().getSimulatedDuration().
														getSimulatedDuration());
			this.currentPowerLevel =
					new TimedMeasure<Double>(
							MAX_POWER_LEVEL.getData(),
							MAX_POWER_LEVEL.getMeasurementUnit(),
							this.getClock4Simulation(),
							this.getClock4Simulation().getStartInstant());
			this.targetTemperature =
					new TimedMeasure<Double>(
							STANDARD_TARGET_TEMPERATURE.getData(),
							STANDARD_TARGET_TEMPERATURE.getMeasurementUnit(),
							this.getClock4Simulation(),
							this.getClock4Simulation().getStartInstant());
			// wait until the simulation ends
			this.getClock4Simulation().waitUntilEnd();
			// give some time for the end of simulation catering tasks
			Thread.sleep(200L);
			// get and print the simulation report
			this.logMessage(this.asp.getFinalReport().toString());
			break;
		case INTEGRATION_TEST_WITH_SIL_SIMULATION:
			// First, the component must synchronise with other components
			// to start the execution of the test scenario; we use a
			// time-triggered synchronisation scheme with the accelerated clock
			this.initialiseClock4Simulation(
					ClocksServerWithSimulation.STANDARD_INBOUNDPORT_URI,
					this.clockURI);
			this.currentPowerLevel =
					new TimedMeasure<Double>(
							MAX_POWER_LEVEL.getData(),
							MAX_POWER_LEVEL.getMeasurementUnit(),
							this.getClock4Simulation(),
							this.getClock4Simulation().getStartInstant());
			this.targetTemperature =
					new TimedMeasure<Double>(
							STANDARD_TARGET_TEMPERATURE.getData(),
							STANDARD_TARGET_TEMPERATURE.getMeasurementUnit(),
							this.getClock4Simulation(),
							this.getClock4Simulation().getStartInstant());
			break;
		case UNIT_TEST_WITH_HIL_SIMULATION:
		case INTEGRATION_TEST_WITH_HIL_SIMULATION:
				throw new BCMException("HIL simulation not implemented yet!");
		default:
		}		
	}

	/**
	 * @see fr.sorbonne_u.components.AbstractComponent#shutdown()
	 */
	@Override
	public synchronized void	shutdown() throws ComponentShutdownException
	{
		try {
			this.hip.unpublishPort();
			this.hicip.unpublishPort();
			this.hecip.unpublishPort();
			this.sensorInboundPort.unpublishPort();
			this.actuatorInboundPort.unpublishPort();
		} catch (Throwable e) {
			throw new ComponentShutdownException(e) ;
		}
		super.shutdown();
	}

	// -------------------------------------------------------------------------
	// Component services implementation
	// -------------------------------------------------------------------------

	/**
	 * @see fr.sorbonne_u.components.hem2025e1.equipments.heater.HeaterUserI#on()
	 */
	@Override
	public boolean		on() throws Exception
	{
		if (HeaterCyPhy.VERBOSE) {
			this.traceMessage("Heater returns its state: " +
											this.currentState + ".\n");
		}
		return this.currentState == HeaterState.ON ||
									this.currentState == HeaterState.HEATING;
	}

	/**
	 * @see fr.sorbonne_u.components.hem2025e1.equipments.heater.HeaterUserI#switchOn()
	 */
	@Override
	public void			switchOn() throws Exception
	{
		if (HeaterCyPhy.VERBOSE) {
			this.traceMessage("Heater switches on.\n");
		}

		assert	!this.on() : new PreconditionException("!on()");

		this.currentState = HeaterState.ON;

		if (this.getExecutionMode().isSILTest()) {
			// For SIL simulation, an operation done in the component code
			// must be reflected in the simulation; to do so, the component
			// code triggers an external event sent to the HairDryerStateModel
			// to make it change its state to on.
			((RTAtomicSimulatorPlugin)this.asp).triggerExternalEvent(
												HeaterStateSILModel.URI,
												t -> new SwitchOnHeater(t));
			// this will send to the heater internal controller the signal that the
			// heater is now on, hence starting the control loop
			this.sensorInboundPort.send(
								new HeaterStateSensorData(this.currentState));
		}

		// this will send to the heater internal controller the signal that the
		// heater is now on, hence starting the control loop
		// Not implemented yet when the heater is not simulated
//		this.sensorInboundPort.send(
//							new HeaterStateSensorData(this.currentState));

		assert	 this.on() : new PostconditionException("on()");
	}

	/**
	 * @see fr.sorbonne_u.components.hem2025e1.equipments.heater.HeaterUserI#switchOff()
	 */
	@Override
	public void			switchOff() throws Exception
	{
		if (HeaterCyPhy.VERBOSE) {
			this.traceMessage("Heater switches off.\n");
		}

		assert	this.on() : new PreconditionException("on()");

		// this will send to the heater internal controller the signal that the
		// heater is now off, hence stopping the control loop
		// Not implemented yet when the heater is not simulated
//		this.sensorInboundPort.send(
//							new HeaterStateSensorData(this.currentState));

		if (this.getExecutionMode().isSILTest()) {
			// this will send to the heater internal controller the signal that
			// the heater is now off, hence stopping the control loop
			this.sensorInboundPort.send(
								new HeaterStateSensorData(HeaterState.OFF));

			// For SIL simulation, an operation done in the component code
			// must be reflected in the simulation; to do so, the component
			// code triggers an external event sent to the HairDryerStateModel
			// to make it change its state to on.
			((RTAtomicSimulatorPlugin)this.asp).triggerExternalEvent(
												HeaterStateSILModel.URI,
												t -> new SwitchOffHeater(t));
		}

		this.currentState = HeaterState.OFF;

		assert	 !this.on() : new PostconditionException("!on()");
	}

	/**
	 * @see fr.sorbonne_u.components.hem2025e1.equipments.heater.HeaterUserI#setTargetTemperature(fr.sorbonne_u.alasca.physical_data.Measure)
	 */
	@Override
	public void			setTargetTemperature(Measure<Double> target)
	throws Exception
	{
		if (HeaterCyPhy.VERBOSE) {
			this.traceMessage("Heater sets a new target "
										+ "temperature: " + target + ".\n");
		}

		assert	target != null &&
						TEMPERATURE_UNIT.equals(target.getMeasurementUnit()) :
				new PreconditionException(
						"target != null && TEMPERATURE_UNIT.equals("
						+ "target.getMeasurementUnit())");
		assert	target.getData() >= MIN_TARGET_TEMPERATURE.getData() &&
						target.getData() <= MAX_TARGET_TEMPERATURE.getData() :
				new PreconditionException(
						"target.getData() >= MIN_TARGET_TEMPERATURE.getData() "
						+ "&& target.getData() <= MAX_TARGET_TEMPERATURE.getData()");

		if (this.executionMode.isStandard() ||
								this.executionMode.isTestWithoutSimulation()) {
			this.targetTemperature =
					new TimedMeasure<Double>(target.getData(),
											 target.getMeasurementUnit());
		} else {
			assert	this.executionMode.isSimulationTest() :
					new BCMException("executionMode.isSimulationTest()");

			this.targetTemperature =
					new TimedMeasure<Double>(target.getData(),
											 target.getMeasurementUnit(),
											 this.getClock4Simulation());
		}

		assert	getTargetTemperature().getMeasure().equals(target) :
				new PostconditionException(
						"getTargetTemperature().getMeasure().equals(target)");
	}

	/**
	 * @see fr.sorbonne_u.components.hem2025e1.equipments.heater.HeaterTemperatureI#getTargetTemperature()
	 */
	@Override
	public SignalData<Double>	getTargetTemperature() throws Exception
	{
		if (HeaterCyPhy.VERBOSE) {
			this.traceMessage("Heater returns its target temperature "
							  + this.targetTemperature + ".\n");
		}

		SignalData<Double> ret = null;
		if (this.getExecutionMode().isStandard()) {
			ret = new SignalData<Double>(this.targetTemperature);
		} else  if (this.getExecutionMode().isTestWithoutSimulation()) {
			assert	this.getClock() != null;
			ret = new SignalData<Double>(this.getClock(),
										 this.targetTemperature);
		} else {
			ret = new SignalData<Double>(this.getClock4Simulation(),
										 this.targetTemperature);
		}

		assert	ret != null && TEMPERATURE_UNIT.equals(
									ret.getMeasure().getMeasurementUnit()) :
				new PostconditionException(
						"return != null && TEMPERATURE_UNIT.equals("
						+ "return.getMeasure().getMeasurementUnit())");
		assert	ret.getMeasure().getData() >= MIN_TARGET_TEMPERATURE.getData() &&
					ret.getMeasure().getData() <= MAX_TARGET_TEMPERATURE.getData() :
				new PostconditionException(
						"return.getMeasure().getData() >= "
						+ "MIN_TARGET_TEMPERATURE.getData() "
						+ "&& return.getMeasure().getData() <= "
						+ "MAX_TARGET_TEMPERATURE.getData()");

		return ret;
	}

	/**
	 * @see fr.sorbonne_u.components.hem2025e1.equipments.heater.HeaterTemperatureI#getCurrentTemperature()
	 */
	@Override
	public SignalData<Double>	getCurrentTemperature() throws Exception
	{
		if (HeaterCyPhy.VERBOSE) {
			this.traceMessage("Heater returns its current temperature.\n");
		}

		assert	this.on() : new PreconditionException("on()");

		SignalData<Double> currentTemperature = null;
		if (this.executionMode.isSILTest()) {
			// retrieve the current temperature from the simulator
			VariableValue<Double> v = this.computeCurrentTemperature();
			currentTemperature =
				new SignalData<>(
					this.getClock4Simulation(),
					new TimedMeasure<Double>(
						v.getValue(),
						TEMPERATURE_UNIT,
						this.getClock4Simulation(),
						this.getClock4Simulation().
										instantOfSimulatedTime(v.getTime())));
		} else {
			assert	this.executionMode.isStandard() ||
					this.executionMode.isTestWithoutSimulation();

			// Temporary implementation; would need a temperature sensor.
			currentTemperature = FAKE_CURRENT_TEMPERATURE;

			if (HeaterCyPhy.VERBOSE) {
				this.traceMessage("Heater returns the current"
							+ " temperature " + currentTemperature + ".\n");
			}
		}

		return  currentTemperature;
	}

	/**
	 * return the current temperature.
	 * 
	 * <p><strong>Contract</strong></p>
	 * 
	 * <pre>
	 * pre	{@code true}	// no precondition.
	 * post	{@code true}	// no postcondition.
	 * </pre>
	 *
	 * @return				the current temperature.
	 * @throws Exception	<i>to do</i>.
	 */
	protected VariableValue<Double>	computeCurrentTemperature() throws Exception
	{
		return this.asp.getModelVariableValue(
							HeaterTemperatureSILModel.URI,
							CURRENT_TEMPERATURE_NAME);
	}

	/**
	 * @see fr.sorbonne_u.components.hem2025e1.equipments.heater.HeaterInternalControlI#heating()
	 */
	@Override
	public boolean		heating() throws Exception
	{
		if (HeaterCyPhy.VERBOSE) {
			this.traceMessage("Heater returns its heating status " + 
						(this.currentState == HeaterState.HEATING) + ".\n");
		}

		assert	this.on() : new PreconditionException("on()");

		return this.currentState == HeaterState.HEATING;
	}

	/**
	 * @see fr.sorbonne_u.components.hem2025e1.equipments.heater.HeaterInternalControlI#startHeating()
	 */
	@Override
	public void			startHeating() throws Exception
	{
		if (HeaterCyPhy.VERBOSE) {
			this.traceMessage("Heater starts heating.\n");
		}
		assert	this.on() : new PreconditionException("on()");
		assert	!this.heating() : new PreconditionException("!heating()");

		this.currentState = HeaterState.HEATING;

		if (this.getExecutionMode().isSILTest()) {
			// For SIL simulation, an operation done in the component code
			// must be reflected in the simulation; to do so, the component
			// code triggers an external event sent to the HairDryerStateModel
			// to make it change its state to on.
			((RTAtomicSimulatorPlugin)this.asp).triggerExternalEvent(
													HeaterStateSILModel.URI,
													t -> new Heat(t));
		}

		assert	this.heating() : new PostconditionException("heating()");
	}

	/**
	 * @see fr.sorbonne_u.components.hem2025e1.equipments.heater.HeaterInternalControlI#stopHeating()
	 */
	@Override
	public void			stopHeating() throws Exception
	{
		if (HeaterCyPhy.VERBOSE) {
			this.traceMessage("Heater stops heating.\n");
		}
		assert	this.on() : new PreconditionException("on()");
		assert	this.heating() : new PreconditionException("heating()");

		this.currentState = HeaterState.ON;

		if (this.getExecutionMode().isSILTest()) {
			// For SIL simulation, an operation done in the component code
			// must be reflected in the simulation; to do so, the component
			// code triggers an external event sent to the HairDryerStateModel
			// to make it change its state to on.
			((RTAtomicSimulatorPlugin)this.asp).triggerExternalEvent(
													HeaterStateSILModel.URI,
													t -> new DoNotHeat(t));
		}

		assert	!this.heating() : new PostconditionException("!heating()");
	}

	/**
	 * @see fr.sorbonne_u.components.hem2025e1.equipments.heater.HeaterExternalControlI#getMaxPowerLevel()
	 */
	@Override
	public Measure<Double>	getMaxPowerLevel() throws Exception
	{
		if (HeaterCyPhy.VERBOSE) {
			this.traceMessage("Heater returns its max power level " + 
					MAX_POWER_LEVEL + ".\n");
		}

		return MAX_POWER_LEVEL;
	}

	/**
	 * @see fr.sorbonne_u.components.hem2025e1.equipments.heater.HeaterExternalControlI#setCurrentPowerLevel(fr.sorbonne_u.alasca.physical_data.Measure)
	 */
	@Override
	public void			setCurrentPowerLevel(Measure<Double> powerLevel)
	throws Exception
	{
		if (HeaterCyPhy.VERBOSE) {
			this.traceMessage("Heater sets its power level to " + 
														powerLevel + ".\n");
		}

		assert	this.on() : new PreconditionException("on()");
		assert	powerLevel != null && powerLevel.getData() >= 0.0 &&
							powerLevel.getMeasurementUnit().equals(POWER_UNIT) :
				new PreconditionException(
						"powerLevel != null && powerLevel.getData() >= 0.0 && "
						+ "powerLevel.getMeasurementUnit().equals(POWER_UNIT)");

		if (powerLevel.getData() <= getMaxPowerLevel().getData()) {
			this.currentPowerLevel = new TimedMeasure<Double>(
											powerLevel.getData(),
											powerLevel.getMeasurementUnit());
		} else {
			this.currentPowerLevel = new TimedMeasure<Double>(
										MAX_POWER_LEVEL.getData(),
										MAX_POWER_LEVEL.getMeasurementUnit());
		}

		if (this.getExecutionMode().isSILTest()) {
			// For SIL simulation, an operation done in the component code
			// must be reflected in the simulation; to do so, the component
			// code triggers an external event sent to the HairDryerStateModel
			// to make it change its state to on.
			((RTAtomicSimulatorPlugin)this.asp).triggerExternalEvent(
				HeaterStateSILModel.URI,
				t -> new SIL_SetPowerHeater(t,
										new PowerValue(powerLevel.getData())));
		}

		assert	powerLevel.getData() > getMaxPowerLevel().getData() ||
						getCurrentPowerLevel().getMeasure().getData() ==
														powerLevel.getData() :
				new PostconditionException(
						"powerLevel.getData() > getMaxPowerLevel().getData() "
						+ "|| getCurrentPowerLevel().getData() == "
						+ "powerLevel.getData()");
	}

	/**
	 * @see fr.sorbonne_u.components.hem2025e1.equipments.heater.HeaterExternalControlI#getCurrentPowerLevel()
	 */
	@Override
	public SignalData<Double>	getCurrentPowerLevel() throws Exception
	{
		if (HeaterCyPhy.VERBOSE) {
			this.traceMessage("Heater returns its current power level " + 
					this.currentPowerLevel + ".\n");
		}

		assert	this.on() : new PreconditionException("on()");

		SignalData<Double> ret = new SignalData<Double>(this.currentPowerLevel);

		assert	ret != null && ret.getMeasure().getMeasurementUnit().
															equals(POWER_UNIT) :
				new PreconditionException(
						"return != null && return.getMeasure()."
						+ "getMeasurementUnit().equals(POWER_UNIT)");
		assert	ret.getMeasure().getData() >= 0.0 &&
					ret.getMeasure().getData() <= getMaxPowerLevel().getData() :
				new PostconditionException(
							"return.getMeasure().getData() >= 0.0 && "
							+ "return.getMeasure().getData() <= "
							+ "getMaxPowerLevel().getData()");

		return ret;
	}

	// -------------------------------------------------------------------------
	// Component sensors
	// -------------------------------------------------------------------------

	/**
	 * return the heating status of the heater as a sensor data; the heating
	 * status is not considered as a timed value in this component, hence the
	 * time associated with .
	 * 
	 * <p><strong>Contract</strong></p>
	 * 
	 * <pre>
	 * pre	{@code on()}
	 * post	{@code true}	// no postcondition.
	 * </pre>
	 *
	 * @return				the heating status of the heater as a sensor data.
	 * @throws Exception	<i>to do</i>.
	 */
	public HeatingSensorData	heatingPullSensor()
	throws Exception
	{
		return new HeatingSensorData(this.heating());
	}

	/**
	 * return the target temperature as a sensor data.
	 * 
	 * <p><strong>Contract</strong></p>
	 * 
	 * <pre>
	 * pre	{@code true}	// no precondition.
	 * post	{@code true}	// no postcondition.
	 * </pre>
	 *
	 * @return				the target temperature as a sensor data.
	 * @throws Exception	<i>to do</i>.
	 */
	public TemperatureSensorData		targetTemperaturePullSensor()
	throws Exception
	{
		TemperatureSensorData ret = null;
		switch (this.getExecutionMode()) {
		case STANDARD:
			ret = new TemperatureSensorData(this.targetTemperature);
			break;
		case UNIT_TEST:
		case INTEGRATION_TEST:
			ret = new TemperatureSensorData(this.getClock(),
											 this.targetTemperature);
			break;
		case UNIT_TEST_WITH_SIL_SIMULATION:
		case UNIT_TEST_WITH_HIL_SIMULATION:
		case INTEGRATION_TEST_WITH_SIL_SIMULATION:
		case INTEGRATION_TEST_WITH_HIL_SIMULATION:
			ret = new TemperatureSensorData(this.getClock4Simulation(),
											 this.targetTemperature);
		}
		return ret;
	}

	/**
	 * return the current temperature as a sensor data.
	 * 
	 * <p><strong>Contract</strong></p>
	 * 
	 * <pre>
	 * pre	{@code on()}
	 * post	{@code true}	// no postcondition.
	 * </pre>
	 *
	 * @return				the current temperature as a sensor data.
	 * @throws Exception	<i>to do</i>.
	 */
	public TemperatureSensorData	currentTemperaturePullSensor()
	throws Exception
	{
		TemperatureSensorData ret = null;
		switch (this.getExecutionMode()) {
		case STANDARD:
		case UNIT_TEST:
		case INTEGRATION_TEST:
			ret = new TemperatureSensorData(
						new TimedMeasure<Double>(
							FAKE_CURRENT_TEMPERATURE.getMeasure().getData(),
							FAKE_CURRENT_TEMPERATURE.getMeasure().
														getMeasurementUnit()));
			break;
		case UNIT_TEST_WITH_SIL_SIMULATION:
		case UNIT_TEST_WITH_HIL_SIMULATION:
		case INTEGRATION_TEST_WITH_SIL_SIMULATION:
		case INTEGRATION_TEST_WITH_HIL_SIMULATION:
			VariableValue<Double> v = this.computeCurrentTemperature();
			ret = new TemperatureSensorData(
						this.getClock4Simulation(),
						new TimedMeasure<Double>(
							v.getValue(),
							TEMPERATURE_UNIT,
							this.getClock4Simulation(),
							this.getClock4Simulation().
										instantOfSimulatedTime(v.getTime())));
		}
		return ret;
	}

	/**
	 * sends the compound measure of the target and the current temperatures
	 * through the push sensor interface.
	 * 
	 * <p><strong>Contract</strong></p>
	 * 
	 * <pre>
	 * pre	{@code true}	// no precondition.
	 * post	{@code true}	// no postcondition.
	 * </pre>
	 *
	 * @throws Exception	<i>to do</i>.
	 */
	public HeaterTemperaturesSensorData	temperaturesSensor()
	throws Exception
	{
		HeaterTemperaturesSensorData ret = null;
		switch (this.getExecutionMode()) {
		case STANDARD:
			ret = new HeaterTemperaturesSensorData(
								this.targetTemperaturePullSensor(),
								this.currentTemperaturePullSensor());
			break;
		case UNIT_TEST:
		case INTEGRATION_TEST:
		case UNIT_TEST_WITH_SIL_SIMULATION:
		case UNIT_TEST_WITH_HIL_SIMULATION:
		case INTEGRATION_TEST_WITH_SIL_SIMULATION:
		case INTEGRATION_TEST_WITH_HIL_SIMULATION:
			ret = new HeaterTemperaturesSensorData(
								this.targetTemperaturePullSensor(),
								this.currentTemperaturePullSensor(),
								this.getClock());
			break;
		}

		return ret;
	}

	/**
	 * start a sequence of temperatures pushes with the given period.
	 * 
	 * <p><strong>Contract</strong></p>
	 * 
	 * <pre>
	 * pre	{@code controlPeriod > 0}
	 * pre	{@code tu != null}
	 * post	{@code true}	// no postcondition.
	 * </pre>
	 *
	 * @param controlPeriod	period at which the pushes must be made.
	 * @param tu			time unit in which {@code controlPeriod} is expressed.
	 * @throws Exception	<i>to do</i>.
	 */
	public void			startTemperaturesPushSensor(
		long controlPeriod,
		TimeUnit tu
		) throws Exception
	{
		long actualControlPeriod = -1L;
		if (this.executionMode.isStandard()) {
			actualControlPeriod = (long)(controlPeriod * tu.toNanos(1));
		} else {
			// this will synchronise the start of the push sensor with the
			// availability of the clock, required to compute the actual push
			// period with the correct acceleration factor
			AcceleratedClock ac = this.clock.get();
			// the accelerated period is in nanoseconds, hence first convert
			// the period to nanoseconds, perform the division and then
			// convert to long (hence providing a better precision than
			// first dividing and then converting to nanoseconds...)
			actualControlPeriod =
					(long)((controlPeriod * tu.toNanos(1))/
											ac.getAccelerationFactor());
			// sanity checking, the standard Java scheduler has a
			// precision no less than 10 milliseconds...
			if (actualControlPeriod < TimeUnit.MILLISECONDS.toNanos(10)) {
				System.out.println(
					"Warning: accelerated control period is "
							+ "too small ("
							+ actualControlPeriod +
							"), unexpected scheduling problems may"
							+ " occur!");
			}
		}
		this.temperaturesPushSensorTask(actualControlPeriod);
	}

	/**
	 * if the heater is not off, perform one push and schedule the next.
	 * 
	 * <p><strong>Contract</strong></p>
	 * 
	 * <pre>
	 * pre	{@code actualControlPeriod > 0}
	 * post	{@code true}	// no postcondition.
	 * </pre>
	 *
	 * @param actualControlPeriod	period at which the push sensor must be triggered.
	 * @throws Exception			<i>to do</i>.
	 */
	protected void		temperaturesPushSensorTask(long actualControlPeriod)
	throws Exception
	{
		assert	actualControlPeriod > 0 :
				new PreconditionException("actualControlPeriod > 0");

		if (this.currentState != HeaterState.OFF) {
			this.traceMessage("Heater performs a new temperatures push.\n");
			this.temperaturesPushSensor();
			if (this.executionMode.isStandard()
							|| this.executionMode.isSILTest()
											|| this.executionMode.isHILTest()) {
				// schedule the next execution of the loop only if the
				// current execution is standard or if it is a real time
				// simulation with code execution i.e., SIL or HIL
				// otherwise, perform only one call to push sensors to
				// test the functionality
				this.scheduleTaskOnComponent(
					new AbstractComponent.AbstractTask() {
						@Override
						public void run() {
							try {
								temperaturesPushSensorTask(actualControlPeriod);
							} catch (Exception e) {
								e.printStackTrace();
							}
						}
					},
					actualControlPeriod,
					TimeUnit.NANOSECONDS);
			}
		}
	}

	/**
	 * sends the compound measure of the target and the current temperatures
	 * through the push sensor interface.
	 * 
	 * <p><strong>Contract</strong></p>
	 * 
	 * <pre>
	 * pre	{@code true}	// no precondition.
	 * post	{@code true}	// no postcondition.
	 * </pre>
	 *
	 * @throws Exception	<i>to do</i>.
	 */
	protected void		temperaturesPushSensor() throws Exception
	{
		this.sensorInboundPort.send(this.temperaturesSensor());
	}
}
// -----------------------------------------------------------------------------

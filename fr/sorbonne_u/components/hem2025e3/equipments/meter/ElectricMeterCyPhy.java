package fr.sorbonne_u.components.hem2025e3.equipments.meter;

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

import fr.sorbonne_u.components.annotations.OfferedInterfaces;
import fr.sorbonne_u.components.cyphy.AbstractCyPhyComponent;
import fr.sorbonne_u.components.cyphy.ExecutionMode;
import fr.sorbonne_u.components.cyphy.annotations.LocalArchitecture;
import fr.sorbonne_u.components.cyphy.annotations.SIL_Simulation_Architectures;
import fr.sorbonne_u.components.cyphy.plugins.devs.AtomicSimulatorPlugin;
import fr.sorbonne_u.components.cyphy.plugins.devs.RTAtomicSimulatorPlugin;
import fr.sorbonne_u.components.cyphy.utils.aclocks.ClocksServerWithSimulation;
import fr.sorbonne_u.components.cyphy.utils.tests.TestScenarioWithSimulation;
import fr.sorbonne_u.components.exceptions.BCMException;
import fr.sorbonne_u.components.exceptions.ComponentShutdownException;
import fr.sorbonne_u.components.exceptions.ComponentStartException;
import fr.sorbonne_u.components.hem2025e1.equipments.meter.ElectricMeterCI;
import fr.sorbonne_u.components.hem2025e1.equipments.meter.ElectricMeterImplementationI;
import fr.sorbonne_u.components.hem2025e1.equipments.meter.connections.ElectricMeterInboundPort;
import fr.sorbonne_u.components.hem2025e2.equipments.hairdryer.mil.events.SetHighHairDryer;
import fr.sorbonne_u.components.hem2025e2.equipments.hairdryer.mil.events.SetLowHairDryer;
import fr.sorbonne_u.components.hem2025e2.equipments.hairdryer.mil.events.SwitchOffHairDryer;
import fr.sorbonne_u.components.hem2025e2.equipments.hairdryer.mil.events.SwitchOnHairDryer;
import fr.sorbonne_u.components.hem2025e2.equipments.heater.mil.events.DoNotHeat;
import fr.sorbonne_u.components.hem2025e2.equipments.heater.mil.events.Heat;
import fr.sorbonne_u.components.hem2025e2.equipments.heater.mil.events.SwitchOffHeater;
import fr.sorbonne_u.components.hem2025e2.equipments.heater.mil.events.SwitchOnHeater;
import fr.sorbonne_u.components.hem2025e3.equipments.heater.sil.events.SIL_SetPowerHeater;
import fr.sorbonne_u.components.hem2025e3.equipments.meter.sil.LocalSimulationArchitectures;
import fr.sorbonne_u.components.utils.tests.TestScenario;
import fr.sorbonne_u.devs_simulation.architectures.RTArchitecture;
import fr.sorbonne_u.devs_simulation.models.annotations.ModelExternalEvents;
import fr.sorbonne_u.devs_simulation.models.time.Time;
import fr.sorbonne_u.exceptions.ImplementationInvariantException;
import fr.sorbonne_u.exceptions.AssertionChecking;
import fr.sorbonne_u.exceptions.PostconditionException;
import fr.sorbonne_u.exceptions.PreconditionException;
import fr.sorbonne_u.utils.aclocks.ClocksServer;
import fr.sorbonne_u.alasca.physical_data.Measure;
import fr.sorbonne_u.alasca.physical_data.SignalData;
import fr.sorbonne_u.alasca.physical_data.TimedMeasure;
import java.time.Instant;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Supplier;

// -----------------------------------------------------------------------------
/**
 * The class <code>ElectricMeter</code> implements a simplified electric meter
 * component.
 *
 * <p><strong>Description</strong></p>
 * 
 * <p>
 * The electric meter gathers the electric power consumption of all appliances
 * and the electric power production of all production units to provide these
 * metrics to the household energy manager.
 * </p>
 * <p>
 * This implementation of the electric meter provides two possible execution
 * modes:
 * </p>
 * <ol>
 * <li>{@code STANDARD} means that the component would execute in normal
 *   operational conditions, on the field (which is not used in this project
 *   but would be in an industrial project).</li>
 * <li>{@code UNIT_TEST} means that the component executes in unit tests where
 *   it is the sole appliance but cooperates with the {@code HairDryerUser}
 *   component.</li>
 * <li>{@code INTEGRATION_TEST} means that the component executes in integration
 *   tests where other appliances coexist and where it must cooperates with the
 *   {@code HairDryerUser} and also the {@code ElectricMeter} components.</li>
 * </ol>
 * <p>
 * There are also four distinct types of simulations defined by
 * {@code SimulationType}:
 * </p>
 * <ol>
 * <li>{@code NO_SIMULATION} means that the component does not execute a
 *   simulator, a type necessarily used in {@code STANDARD} executions but also
 *   for {@code UNIT_TEST} with no simulation.</li>
 * <li>{@code MIL_SIMULATION} means that only MIL simulators will run; it is
 *   meant to be used early stages of a project in {@code UNIT_TEST} and
 *   {@code INTEGRATION_TEST} before implementing the code of the components.
 *   </li>
 * <li>{@code MIL_RT_SIMULATION} is similar to {@code MIL_SIMULATION} but
 *   simulates in real time or an accelerated real time; it is more a step
 *   towards SIL simulations than an actual interesting simulation type
 *   in an industrial project.</li>
 * <li>{@code SIL_SIMULATION} means that the simulators are implemented so
 *   that they can execute with the component software in software-in-the-loop
 *   simulations; it can be used in {@code UNIT_TEST} executions to test
 *   the component software in isolation and then in {@code INTEGRATION_TEST}
 *   executions to test the entire application at once.</li>
 * </ol>
 * <p>
 * In this implementation of the {@code ElectricMeter} component, the standard
 * execution type is not really implemented as the software is not embedded in
 * any real appliances. Unit tests are relatively meaningless for the
 * {@code ElectricMeter} component as it is only useful when appliances and
 * production units are present in the execution. In integration tests with no
 * simulation, the component is totally passive as its methods are called by the
 * {@code HEM} component. In MIL, MIL real time and SIL simulations, the
 * component simply creates and installs its local simulation architecture,
 * which execution will be started by a supervisor component.
 * </p>
 * <p>
 * For SIL simulations in integration tests, the {@code ElectricMeter} component
 * creates the following local component simulation architecture:
 * </p>
 * <p><img src="../../../../../../../images/hem-2024-e3/ElectricMeterIntegrationTestComponentArchitecture.png"/></p>
 * <p>
 * Every electricity model of every appliance and production unit will have to
 * added to the local SIL simulation architecture of the {@code ElectricMeter},
 * which will therefore also receive all of the relevant events coming from the
 * local architectures of the appliances and production unit to propagate them
 * to the appropriate electricity models.
 * </p>
 * 
 * <p><strong>Implementation Invariants</strong></p>
 * 
 * <pre>
 * invariant	{@code currentPowerConsumption.get() == null || currentPowerConsumption.get().getMeasure().getData() >= 0.0 && currentPowerConsumption.get().getMeasure().getMeasurementUnit().equals(POWER_UNIT)}
 * invariant	{@code currentPowerProduction.get() == null || currentPowerProduction.get().getMeasure().getData() >= 0.0 && currentPowerProduction.get().getMeasure().getMeasurementUnit().equals(POWER_UNIT)}
 * invariant	{@code !getExecutionMode().isSimulationTest() || (localArchitectureURI != null && !localArchitectureURI.isEmpty())}
 * invariant	{@code !getExecutionMode().isSimulationTest() || accFactor > 0.0}
 * </pre>
 * 
 * <p><strong>Invariant</strong></p>
 * 
 * <pre>
 * invariant	{@code REFLECTION_INBOUND_PORT_URI != null && !REFLECTION_INBOUND_PORT_URI.isEmpty()}
 * invariant	{@code ELECTRIC_METER_INBOUND_PORT_URI != null && !ELECTRIC_METER_INBOUND_PORT_URI.isEmpty()}
 * invariant	{@code TENSION != null}
 * invariant	{@code TENSION.getData() > 0.0}
 * invariant	{@code TENSION.getMeasurementUnit().equals(TENSION_UNIT)}
 * invariant	{@code X_RELATIVE_POSITION >= 0}
 * invariant	{@code Y_RELATIVE_POSITION >= 0}
 * invariant	{@code NUMBER_OF_STANDARD_THREADS >= 0}
 * invariant	{@code NUMBER_OF_SCHEDULABLE_THREADS >= 0}
 * </pre>
 * 
 * <p>Created on : 2023-09-19</p>
 * 
 * @author	<a href="mailto:Jacques.Malenfant@lip6.fr">Jacques Malenfant</a>
 */
// -----------------------------------------------------------------------------
@SIL_Simulation_Architectures({
	// fragile: fields in annotations cannot be defined by a class constant due
	// to Java annotations field initialisers limited to static values only
	@LocalArchitecture(
		// must be equal to LOCAL_ARCHITECTURE_URI
		uri = "silIntegrationTests",
		// must be equal to the URI of the instance of
		// ElectricMeterCoupledModel
		rootModelURI = "ElectricMeterCoupledModel",
		// next fields must be the same as the values used in the local
		// architecture
		simulatedTimeUnit = TimeUnit.HOURS,
		externalEvents = @ModelExternalEvents(
			imported = {SwitchOnHairDryer.class,	// HairDryer events
						SwitchOffHairDryer.class,
						SetLowHairDryer.class,
						SetHighHairDryer.class,
						SIL_SetPowerHeater.class,		// Heater events
						SwitchOnHeater.class,
						SwitchOffHeater.class,
						Heat.class,
						DoNotHeat.class}
		)
	)
})
// -----------------------------------------------------------------------------
@OfferedInterfaces(offered={ElectricMeterCI.class})
// -----------------------------------------------------------------------------
public class			ElectricMeterCyPhy
extends		AbstractCyPhyComponent
implements	ElectricMeterImplementationI
{
	// -------------------------------------------------------------------------
	// Constants and variables
	// -------------------------------------------------------------------------

	/** tolerance applied when comparing double values for equality.		*/
	protected static double			TOLERANCE = 1e-8;
	/** URI of the hair dryer inbound port used in tests.					*/
	public static final String		REFLECTION_INBOUND_PORT_URI =
													"ELECTRIC-METER-RIP-URI";	
	/** URI of the electric meter inbound port used in tests.				*/
	public static final String		ELECTRIC_METER_INBOUND_PORT_URI =
															"ELECTRIC-METER";

	/**	the tension in the electric circuits of this meter.					*/
	public static Measure<Double>	TENSION = new Measure<Double>(
														220.0,
														TENSION_UNIT);

	/** inbound port offering the <code>ElectricMeterCI</code> interface.	*/
	protected ElectricMeterInboundPort		emip;

	/** current total electric power consumption measured at the electric
	 *  meter in the power unit of the meter.								*/
	protected AtomicReference<SignalData<Double>>	currentPowerConsumption;
	/** current total electric power production measured at the electric
	 *  meter in the power unit of the meter.								*/
	protected AtomicReference<SignalData<Double>>	currentPowerProduction;

	// Execution/Simulation

	/** when true, methods trace their actions.								*/
	public static boolean		VERBOSE = true;
	/** when true, methods provides debugging traces of their actions.		*/
	public static boolean		DEBUG = false;
	/** when tracing, x coordinate of the window relative position.			*/
	public static int			X_RELATIVE_POSITION = 0;
	/** when tracing, y coordinate of the window relative position.			*/
	public static int			Y_RELATIVE_POSITION = 0;

	/** one thread for the method execute, which starts the local SIL
	 *  simulator and wait until the end of the simulation to get the
	 *  simulation report, and one to answer the calls to the component
	 *  services.															*/
	protected static int		NUMBER_OF_STANDARD_THREADS = 2;
	/** no need for statically defined schedulable threads.					*/
	protected static int		NUMBER_OF_SCHEDULABLE_THREADS = 0;

	/** URI of the local simulation architecture for SIL unit tests.		*/
	public static final String	LOCAL_ARCHITECTURE_URI = "silIntegrationTests";

	/** plug-in holding the local simulation architecture and simulators.	*/
	protected AtomicSimulatorPlugin	asp;
	/** URI of the local simulator used to compose the global simulation
	 *  architecture.														*/
	protected final String			localArchitectureURI;
	/** acceleration factor to be used when running the real time
	 *  simulation.															*/
	protected double				accFactor;

	// -------------------------------------------------------------------------
	// Invariants
	// -------------------------------------------------------------------------

	/**
	 * return true if the implementation invariants are observed, false otherwise.
	 * 
	 * <p><strong>Contract</strong></p>
	 * 
	 * <pre>
	 * pre	{@code instance != null}
	 * post	{@code true}	// no postcondition.
	 * </pre>
	 *
	 * @param instance	instance to be tested.
	 * @return			true if the glass-box invariants are observed, false otherwise.
	 */
	protected static boolean	implementationInvariants(
		ElectricMeterCyPhy instance)
	{
		assert 	instance != null : new PreconditionException("instance != null");

		boolean ret = true;
		ret &= AssertionChecking.checkImplementationInvariant(
				instance.currentPowerConsumption.get() == null ||
					instance.currentPowerConsumption.get().getMeasure().
															getData() >= 0.0
					&& instance.currentPowerConsumption.get().getMeasure().
										getMeasurementUnit().equals(POWER_UNIT),
				ElectricMeterCyPhy.class, instance,
				"currentPowerConsumption.get() == null || "
				+ "currentPowerConsumption.get().getMeasure().getData() >= 0.0 "
				+ "&& currentPowerConsumption.get().getMeasure()."
				+ "getMeasurementUnit().equals(POWER_UNIT)");
		ret &= AssertionChecking.checkImplementationInvariant(
				instance.currentPowerProduction.get() == null ||
					instance.currentPowerProduction.get().getMeasure().
															getData() >= 0.0
					&& instance.currentPowerProduction.get().getMeasure().
										getMeasurementUnit().equals(POWER_UNIT),
				ElectricMeterCyPhy.class, instance,
				"currentPowerProduction.get() == null || "
				+ "currentPowerProduction.get().getMeasure().getData() >= 0.0 "
				+ "&& currentPowerProduction.get().getMeasure()."
				+ "getMeasurementUnit().equals(POWER_UNIT)");
		ret &= AssertionChecking.checkImplementationInvariant(
				!instance.getExecutionMode().isSimulationTest() ||
					(instance.localArchitectureURI != null &&
									!instance.localArchitectureURI.isEmpty()),
				ElectricMeterCyPhy.class, instance,
				"!getExecutionMode().isSimulationTest() || "
				+ "(localArchitectureURI != null && "
				+ "!localArchitectureURI.isEmpty())");
		ret &= AssertionChecking.checkImplementationInvariant(
				!instance.getExecutionMode().isSimulationTest() ||
													instance.accFactor > 0.0,
				ElectricMeterCyPhy.class, instance,
				"!getExecutionMode().isSimulationTest() || accFactor > 0.0");
		return ret;
	}

	/**
	 * return true if the static invariants are observed, false otherwise.
	 * 
	 * <p><strong>Contract</strong></p>
	 * 
	 * <pre>
	 * post	{@code true}	// no postcondition.
	 * </pre>
	 *
	 * @return	true if the static invariants are observed, false otherwise.
	 */
	public static boolean	staticInvariants()
	{
		boolean ret = true;
		ret &= AssertionChecking.checkStaticInvariant(
				REFLECTION_INBOUND_PORT_URI != null && !REFLECTION_INBOUND_PORT_URI.isEmpty(),
				ElectricMeterCyPhy.class,
				"REFLECTION_INBOUND_PORT_URI != null && !REFLECTION_INBOUND_PORT_URI.isEmpty()");
		ret &= AssertionChecking.checkStaticInvariant(
				ELECTRIC_METER_INBOUND_PORT_URI != null &&
								!ELECTRIC_METER_INBOUND_PORT_URI.isEmpty(),
				ElectricMeterCyPhy.class,
				"ELECTRIC_METER_INBOUND_PORT_URI != null &&"
							+ "!ELECTRIC_METER_INBOUND_PORT_URI.isEmpty()");
		ret &= AssertionChecking.checkStaticInvariant(
				TENSION != null,
				ElectricMeterCyPhy.class,
				"TENSION != null");
		ret &= AssertionChecking.checkStaticInvariant(
				TENSION.getData() > 0.0,
				ElectricMeterCyPhy.class,
				"TENSION.getData() > 0.0");
		ret &= AssertionChecking.checkStaticInvariant(
				TENSION.getMeasurementUnit().equals(TENSION_UNIT),
				ElectricMeterCyPhy.class,
				"TENSION.getMeasurementUnit().equals(TENSION_UNIT)");
		ret &= AssertionChecking.checkStaticInvariant(
				X_RELATIVE_POSITION >= 0,
				ElectricMeterCyPhy.class,
				"X_RELATIVE_POSITION >= 0");
		ret &= AssertionChecking.checkStaticInvariant(
				Y_RELATIVE_POSITION >= 0,
				ElectricMeterCyPhy.class,
				"Y_RELATIVE_POSITION >= 0");
		return ret;
	}

	/**
	 * return true if the invariants are observed, false otherwise.
	 * 
	 * <p><strong>Contract</strong></p>
	 * 
	 * <pre>
	 * pre	{@code em != null}
	 * post	{@code true}	// no postcondition.
	 * </pre>
	 *
	 * @param em	instance to be tested.
	 * @return	true if the invariants are observed, false otherwise.
	 */
	protected static boolean	invariants(ElectricMeterCyPhy em)
	{
		assert	em != null : new PreconditionException("em != null");

		boolean ret = true;
		ret &= staticInvariants();
		return ret;
	}

	// -------------------------------------------------------------------------
	// Constructors
	// -------------------------------------------------------------------------

	// Standard execution

	/**
	 * create an electric meter component for standard executions.
	 * 
	 * <p><strong>Contract</strong></p>
	 * 
	 * <pre>
	 * pre	{@code !(this instanceof ComponentInterface)}
	 * post	{@code getExecutionMode().isStandard()}
	 * </pre>
	 * 
	 * @throws Exception	<i>to do</i>.
	 */
	protected			ElectricMeterCyPhy() throws Exception
	{
		this(ELECTRIC_METER_INBOUND_PORT_URI);
	}

	/**
	 * create an electric meter component for standard executions.
	 * 
	 * <p><strong>Contract</strong></p>
	 * 
	 * <pre>
	 * pre	{@code !(this instanceof ComponentInterface)}
	 * pre	{@code electricMeterInboundPortURI != null && !electricMeterInboundPortURI.isEmpty()}
	 * post	{@code getExecutionMode().isStandard()}
	 * </pre>
	 *
	 * @param electricMeterInboundPortURI	URI of the electric meter inbound port.
	 * @throws Exception					<i>to do</i>.
	 */
	protected			ElectricMeterCyPhy(
		String electricMeterInboundPortURI
		) throws Exception
	{
		this(REFLECTION_INBOUND_PORT_URI, electricMeterInboundPortURI);
	}

	/**
	 * create an electric meter component for standard executions.
	 * 
	 * <p><strong>Contract</strong></p>
	 * 
	 * <pre>
	 * pre	{@code !(this instanceof ComponentInterface)}
	 * pre	{@code reflectionInboundPortURI != null && !reflectionInboundPortURI.isEmpty()}
	 * pre	{@code electricMeterInboundPortURI != null && !electricMeterInboundPortURI.isEmpty()}
	 * post	{@code getExecutionMode().isStandard()}
	 * </pre>
	 *
	 * @param reflectionInboundPortURI		URI of the reflection innbound port of the component.
	 * @param electricMeterInboundPortURI	URI of the electric meter inbound port.
	 * @throws Exception					<i>to do</i>.
	 */
	protected			ElectricMeterCyPhy(
		String reflectionInboundPortURI,
		String electricMeterInboundPortURI
		) throws Exception
	{
		super(reflectionInboundPortURI,
			  NUMBER_OF_STANDARD_THREADS,
			  NUMBER_OF_SCHEDULABLE_THREADS);

		this.initialise(electricMeterInboundPortURI);

		this.localArchitectureURI = null;

		assert	ElectricMeterCyPhy.implementationInvariants(this) :
				new ImplementationInvariantException(
						"ElectricMeterCyPhy.implementationInvariants(this)");
		assert	ElectricMeterCyPhy.invariants(this) :
				new ImplementationInvariantException(
						"ElectricMeterCyPhy.invariants(this)");
	}

	// Tests without simulation execution

	/**
	 * create an electric meter component for test executions without simulation.
	 * 
	 * <p><strong>Contract</strong></p>
	 * 
	 * <pre>
	 * pre	{@code !(this instanceof ComponentInterface)}
	 * pre	{@code executionMode != null && executionMode.isTestWithoutSimulation()}
	 * pre	{@code clockURI != null && !clockURI.isEmpty()}
	 * post	{@code getExecutionMode().equals(executionMode)}
	 * </pre>
	 *
	 * @param executionMode	execution mode for the next run.
	 * @param clockURI		URI of a clock used to synchronise components.
	 * @throws Exception	<i>to do</i>.
	 */
	protected			ElectricMeterCyPhy(
		ExecutionMode executionMode,
		String clockURI
		) throws Exception
	{
		this(ELECTRIC_METER_INBOUND_PORT_URI,
			 executionMode, clockURI);
	}

	/**
	 * create an electric meter component for test executions without simulation.
	 * 
	 * <p><strong>Contract</strong></p>
	 * 
	 * <pre>
	 * pre	{@code !(this instanceof ComponentInterface)}
	 * pre	{@code electricMeterInboundPortURI != null && !electricMeterInboundPortURI.isEmpty()}
	 * pre	{@code executionMode != null && executionMode.isTestWithoutSimulation()}
	 * pre	{@code clockURI != null && !clockURI.isEmpty()}
	 * post	{@code getExecutionMode().equals(executionMode)}
	 * </pre>
	 *
	 * @param electricMeterInboundPortURI	URI of the electric meter inbound port.
	 * @param executionMode					execution mode for the next run.
	 * @param clockURI						URI of a clock used to synchronise components.
	 * @throws Exception					<i>to do</i>.
	 */
	protected			ElectricMeterCyPhy(
		String electricMeterInboundPortURI,
		ExecutionMode executionMode,
		String clockURI
		) throws Exception
	{
		this(REFLECTION_INBOUND_PORT_URI,
			 electricMeterInboundPortURI,
			 executionMode, clockURI);
	}

	/**
	 * create an electric meter component for test executions without simulation.
	 * 
	 * <p><strong>Contract</strong></p>
	 * 
	 * <pre>
	 * pre	{@code !(this instanceof ComponentInterface)}
	 * pre	{@code reflectionInboundPortURI != null && !reflectionInboundPortURI.isEmpty()}
	 * pre	{@code electricMeterInboundPortURI != null && !electricMeterInboundPortURI.isEmpty()}
	 * pre	{@code executionMode != null && executionMode.isTestWithoutSimulation()}
	 * pre	{@code clockURI != null && !clockURI.isEmpty()}
	 * post	{@code getExecutionMode().equals(executionMode)}
	 * </pre>
	 *
	 * @param reflectionInboundPortURI		URI of the reflection innbound port of the component.
	 * @param electricMeterInboundPortURI	URI of the electric meter inbound port.
	 * @param executionMode					execution mode for the next run.
	 * @param clockURI						URI of a clock used to synchronise components.
	 * @throws Exception					<i>to do</i>.
	 */
	protected			ElectricMeterCyPhy(
		String reflectionInboundPortURI,
		String electricMeterInboundPortURI,
		ExecutionMode executionMode,
		String clockURI
		) throws Exception
	{
		super(reflectionInboundPortURI,
			  NUMBER_OF_STANDARD_THREADS,
			  NUMBER_OF_SCHEDULABLE_THREADS,
			  AssertionChecking.assertTrueAndReturnOrThrow(
				executionMode != null && executionMode.isTestWithoutSimulation(),
				executionMode,
				() -> new PreconditionException(
							"executionMode != null && "
							+ "executionMode.isTestWithoutSimulation()")),
			  AssertionChecking.assertTrueAndReturnOrThrow(
				clockURI != null && !clockURI.isEmpty(),
				clockURI,
				() -> new PreconditionException(
							"clockURI != null && !clockURI.isEmpty()")),
			  null);

		assert	executionMode != null &&
									executionMode.isTestWithoutSimulation() :
				new PreconditionException(
						"executionMode != null && "
						+ "executionMode.isTestWithoutSimulation()");

		this.initialise(electricMeterInboundPortURI);

		this.localArchitectureURI = null;

		assert	ElectricMeterCyPhy.implementationInvariants(this) :
				new ImplementationInvariantException(
						"ElectricMeterCyPhy.implementationInvariants(this)");
		assert	ElectricMeterCyPhy.invariants(this) :
				new ImplementationInvariantException(
						"ElectricMeterCyPhy.invariants(this)");
	}

	// Tests with simulation

	/**
	 * create an electric meter component.
	 * 
	 * <p><strong>Contract</strong></p>
	 * 
	 * <pre>
	 * pre	{@code !(this instanceof ComponentInterface)}
	 * pre	{@code reflectionInboundPortURI != null && !reflectionInboundPortURI.isEmpty()}
	 * pre	{@code electricMeterInboundPortURI != null && !electricMeterInboundPortURI.isEmpty()}
	 * pre	{@code executionMode != null && executionMode.isSimulationTest()}
	 * pre	{@code testScenario == null}
	 * pre	{@code localArchitectureURI != null && !localArchitectureURI.isEmpty()}
	 * pre	{@code accelerationFactor > 0.0}
	 * post	{@code getExecutionMode().equals(executionMode)}
	 * </pre>
	 *
	 * @param reflectionInboundPortURI		URI of the reflection innbound port of the component.
	 * @param electricMeterInboundPortURI	URI of the electric meter inbound port.
	 * @param executionMode					execution mode for the next run.
	 * @param testScenario					test scenario to be executed with this component.
	 * @param localArchitectureURI			URI of the local simulation architecture to be used in composing the global simulation architecture.
	 * @param accelerationFactor			acceleration factor for the simulation.
	 * @throws Exception					<i>to do</i>.
	 */
	protected			ElectricMeterCyPhy(
		String reflectionInboundPortURI,
		String electricMeterInboundPortURI,
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
			  		  hs.add(LOCAL_ARCHITECTURE_URI);
			  		  return hs;
			  		}).get(),
			 accelerationFactor );

		assert	electricMeterInboundPortURI != null &&
										!electricMeterInboundPortURI.isEmpty() :
				new PreconditionException(
						"electricMeterInboundPortURI != null && "
						+ "!electricMeterInboundPortURI.isEmpty()");
		assert	accelerationFactor > 0.0 :
				new PreconditionException("accelerationFactor > 0.0");

		this.localArchitectureURI = localArchitectureURI;
		this.accFactor = accelerationFactor;

		this.initialise(electricMeterInboundPortURI);

		assert	ElectricMeterCyPhy.implementationInvariants(this) :
				new ImplementationInvariantException(
						"ElectricMeterCyPhy.implementationInvariants(this)");
		assert	ElectricMeterCyPhy.invariants(this) :
				new ImplementationInvariantException(
						"ElectricMeterCyPhy.invariants(this)");
	}

	// -------------------------------------------------------------------------
	// Initialisation methods
	// -------------------------------------------------------------------------

	/**
	 * initialise an electric meter component.
	 * 
	 * <p><strong>Contract</strong></p>
	 * 
	 * <pre>
	 * pre	{@code electricMeterInboundPortURI != null && !electricMeterInboundPortURI.isEmpty()}
	 * post	{@code true}	// no postcondition.
	 * </pre>
	 *
	 * @param electricMeterInboundPortURI	URI of the electric meter inbound port.
	 * @throws Exception					<i>to do</i>.
	 */
	protected void		initialise(String electricMeterInboundPortURI)
	throws Exception
	{
		assert	electricMeterInboundPortURI != null &&
										!electricMeterInboundPortURI.isEmpty() :
				new PreconditionException(
						"electricMeterInboundPortURI != null && "
						+ "!electricMeterInboundPortURI.isEmpty()");

		this.emip =
				new ElectricMeterInboundPort(electricMeterInboundPortURI, this);
		this.emip.publishPort();

		this.currentPowerProduction = new AtomicReference<>();
		this.currentPowerConsumption = new AtomicReference<>();

		if (VERBOSE) {
			this.tracer.get().setTitle("Electric meter component");
			this.tracer.get().setRelativePosition(X_RELATIVE_POSITION,
												  Y_RELATIVE_POSITION);
			this.toggleTracing();
		}
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
		if (architectureURI.equals(LOCAL_ARCHITECTURE_URI)) {
			ret = LocalSimulationArchitectures.
					createElectricMeterSILArchitecture(
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
	 * set the current power consumption, a method that is meant to be called
	 * only by the simulator in SIL runs, otherwise a hardware sensor would be
	 * used in standard executions.
	 * 
	 * <p><strong>Contract</strong></p>
	 * 
	 * <pre>
	 * pre	{@code getExecutionMode().isSimulationTest()}
	 * pre	{@code power >= 0.0}
	 * pre	{@code t != null}
	 * post	{@code true}	// no postcondition.
	 * </pre>
	 *
	 * @param power		electric power consumption in {@code POWER_UNIT}.
	 * @param t			simulated time at which the power has been measured.
	 */
	public void			setCurrentPowerConsumption(double power, Time t)
	{
		assert	getExecutionMode().isSimulationTest() :
				new PreconditionException(
						"getExecutionMode().isSimulationTest()");
		assert	power >= 0.0 : new PreconditionException("power >= 0.0");
		assert	t != null : new PreconditionException("t != null");

		Instant currentInstant = this.getClock4Simulation().currentInstant();
		TimedMeasure<Double> measuredPowerConsumption =
				new TimedMeasure<Double>(
						power,
						POWER_UNIT,
						this.getClock4Simulation(),
						this.getClock4Simulation().instantOfSimulatedTime(t));
		SignalData<Double> powerConsumptionSignal =
				new SignalData<>(
						this.getClock4Simulation(),
						measuredPowerConsumption,
						currentInstant);

		SignalData<Double> oldSignalData =
				this.currentPowerConsumption.getAndSet(powerConsumptionSignal);
		if (oldSignalData != null && VERBOSE) {
			double old = oldSignalData.getMeasure().getData();
			if (Math.abs(old - power) > TOLERANCE) {
				this.traceMessage(
					"Electric meter sets its current consumption with new value "
					+ measuredPowerConsumption + " at " + currentInstant + ".\n");
			}
		}
	}

	/**
	 * set the current power consumption, a method that is meant to be called
	 * only by the simulator in SIL runs, otherwise a hardware sensor would be
	 * used in standard executions.
	 * 
	 * <p><strong>Contract</strong></p>
	 * 
	 * <pre>
	 * pre	{@code getExecutionMode().isSimulationTest()}
	 * pre	{@code power >= 0.0}
	 * pre	{@code t != null}
	 * post	{@code true}	// no postcondition.
	 * </pre>
	 *
	 * @param power		electric power consumption in {@code POWER_UNIT}.
	 * @param t			simulated time at which the power has been measured.
	 */
	public void			setCurrentPowerProduction(double power, Time t)
	{
		assert	getExecutionMode().isSimulationTest() :
				new PreconditionException(
						"getExecutionMode().isSimulationTest()");
		assert	power >= 0.0 : new PreconditionException("power >= 0.0");
		assert	t != null : new PreconditionException("t != null");

		Instant currentInstant = this.getClock4Simulation().currentInstant();
		TimedMeasure<Double> measuredPowerProduction =
				new TimedMeasure<Double>(
						power,
						POWER_UNIT,
						this.getClock4Simulation(),
						this.getClock4Simulation().instantOfSimulatedTime(t));
		SignalData<Double> powerProductionSignal =
				new SignalData<>(
						this.getClock4Simulation(),
						measuredPowerProduction,
						currentInstant);

		SignalData<Double> oldSignalData =
			this.currentPowerProduction.getAndSet(powerProductionSignal);
		if (oldSignalData != null && VERBOSE) {
			double old = oldSignalData.getMeasure().getData();
			if (Math.abs(old - power) > TOLERANCE) {
				this.traceMessage(
					"Electric meter sets its current production with new value "
					+ measuredPowerProduction + " at " + currentInstant + ".\n");
			}
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

		try {
			switch (this.getExecutionMode()) {
			case STANDARD:
			case UNIT_TEST:
			case INTEGRATION_TEST:
				break;
			case UNIT_TEST_WITH_SIL_SIMULATION:
			case INTEGRATION_TEST_WITH_SIL_SIMULATION:
				// create the standard real-time atomic simulator plug-in
				this.asp = new RTAtomicSimulatorPlugin();
				// get the local architecture chosen for this execution
				RTArchitecture architecture =
					(RTArchitecture) this.localSimulationArchitectures.get(
													this.localArchitectureURI);
				// set the URI of the plug-in to the root model URI
				this.asp.setPluginURI(architecture.getRootModelURI());
				// set the architecture for the plug-in
				this.asp.setSimulationArchitecture(architecture);
				// install the plug-in on the component
				this.installPlugin(this.asp);
				// the simulator inside the plug-in is created
				this.asp.createSimulator();
				// to prepare for the run, set the run parameters
				this.asp.setSimulationRunParameters(
						(TestScenarioWithSimulation) this.testScenario,
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

		assert	ElectricMeterCyPhy.implementationInvariants(this) :
				new ImplementationInvariantException(
						"ElectricMeterCyPhy.implementationInvariants(this)");
		assert	ElectricMeterCyPhy.invariants(this) :
				new ImplementationInvariantException(
						"ElectricMeterCyPhy.invariants(this)");
	}

	/**
	 * @see fr.sorbonne_u.components.AbstractComponent#execute()
	 */
	@Override
	public void			execute() throws Exception
	{
		if (VERBOSE) {
			this.traceMessage("Electric meter begins its execution.\n");
		}

		switch (this.getExecutionMode()) {
		case STANDARD:
			TimedMeasure<Double> measuredPowerConsumption =
					new TimedMeasure<Double>(0.0, POWER_UNIT);
			SignalData<Double> powerConsumptionSignal =
					new SignalData<>(measuredPowerConsumption);
			this.currentPowerConsumption.set(powerConsumptionSignal);
			TimedMeasure<Double> measuredPowerProduction =
					new TimedMeasure<Double>(0.0, POWER_UNIT);
			SignalData<Double> powerProductionSignal =
					new SignalData<>(measuredPowerProduction);
			this.currentPowerProduction.set(powerProductionSignal);
			break;
		case UNIT_TEST:
		case INTEGRATION_TEST:
			this.initialiseClock(
					ClocksServer.STANDARD_INBOUNDPORT_URI,
					this.clockURI);
			measuredPowerConsumption =
					new TimedMeasure<Double>(
							0.0,
							POWER_UNIT,
							this.getClock(),
							this.getClock().getStartInstant());
			powerConsumptionSignal =
					new SignalData<>(
							this.getClock(),
							measuredPowerConsumption,
							this.getClock().getStartInstant());
			this.currentPowerConsumption.set(powerConsumptionSignal);
			measuredPowerProduction =
					new TimedMeasure<Double>(
							0.0,
							POWER_UNIT,
							this.getClock(),
							this.getClock().getStartInstant());
			powerProductionSignal =
					new SignalData<>(
							this.getClock(),
							measuredPowerProduction,
							this.getClock().getStartInstant());
			this.currentPowerProduction.set(powerProductionSignal);
			break;
		case UNIT_TEST_WITH_SIL_SIMULATION:
			this.initialiseClock4Simulation(
					ClocksServerWithSimulation.STANDARD_INBOUNDPORT_URI,
					this.clockURI);
			// initialise the simulation before starting it
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

			measuredPowerConsumption =
					new TimedMeasure<Double>(
							0.0,
							POWER_UNIT,
							this.getClock4Simulation(),
							this.getClock4Simulation().getStartInstant());
			powerConsumptionSignal =
					new SignalData<>(
							this.getClock4Simulation(),
							measuredPowerConsumption,
							this.getClock4Simulation().getStartInstant());
			this.currentPowerConsumption.set(powerConsumptionSignal);
			measuredPowerProduction =
					new TimedMeasure<Double>(
							0.0,
							POWER_UNIT,
							this.getClock4Simulation(),
							this.getClock().getStartInstant());
			powerProductionSignal =
					new SignalData<>(
							this.getClock4Simulation(),
							measuredPowerProduction,
							this.getClock4Simulation().getStartInstant());
			this.currentPowerProduction.set(powerProductionSignal);
			// wait until the simulation ends
			this.getClock4Simulation().waitUntilEnd();
			// give some time for the end of simulation catering tasks
			Thread.sleep(200L);
			// get and print the simulation report
			this.logMessage(this.asp.getFinalReport().toString());
			break;
		case INTEGRATION_TEST_WITH_SIL_SIMULATION:
			this.initialiseClock4Simulation(
					ClocksServerWithSimulation.STANDARD_INBOUNDPORT_URI,
					this.clockURI);
			measuredPowerConsumption =
					new TimedMeasure<Double>(
							0.0,
							POWER_UNIT,
							this.getClock4Simulation(),
							this.getClock4Simulation().getStartInstant());
			powerConsumptionSignal =
					new SignalData<>(
							this.getClock4Simulation(),
							measuredPowerConsumption,
							this.getClock4Simulation().getStartInstant());
			this.currentPowerConsumption.set(powerConsumptionSignal);
			measuredPowerProduction =
					new TimedMeasure<Double>(
							0.0,
							POWER_UNIT,
							this.getClock4Simulation(),
							this.getClock().getStartInstant());
			powerProductionSignal =
					new SignalData<>(
							this.getClock4Simulation(),
							measuredPowerProduction,
							this.getClock4Simulation().getStartInstant());
			this.currentPowerProduction.set(powerProductionSignal);
			break;
		case UNIT_TEST_WITH_HIL_SIMULATION:
		case INTEGRATION_TEST_WITH_HIL_SIMULATION:
			throw new BCMException("HIL simulation not implemented yet!");
		default:
		}		

		if (VERBOSE) {
			this.traceMessage("Electric meter execution ends.\n");
		}
	}

	/**
	 * @see fr.sorbonne_u.components.AbstractComponent#shutdown()
	 */
	@Override
	public synchronized void	shutdown() throws ComponentShutdownException
	{
		try {
			this.emip.unpublishPort();
		} catch (Exception e) {
			throw new ComponentShutdownException(e) ;
		}
		super.shutdown();
	}

	// -------------------------------------------------------------------------
	// Component services implementation
	// -------------------------------------------------------------------------

	/**
	 * @see fr.sorbonne_u.components.hem2025e1.equipments.meter.ElectricMeterImplementationI#getTension()
	 */
	@Override
	public Measure<Double>		getTension() throws Exception
	{
		if (VERBOSE) {
			this.traceMessage("Electric meter returns its tension.\n");
		}

		return TENSION;
	}

	/**
	 * @see fr.sorbonne_u.components.hem2025e1.equipments.meter.ElectricMeterImplementationI#getCurrentConsumption()
	 */
	@Override
	public SignalData<Double>	getCurrentConsumption() throws Exception
	{
		if (VERBOSE) {
			this.traceMessage("Electric meter returns its current consumption.\n");
		}

		SignalData<Double> ret = null;
		if (this.getExecutionMode().isSimulationTest()) {
			ret = this.currentPowerConsumption.get();
		} else {
			TimedMeasure<Double> measuredPowerProduction = null;
			if (this.getExecutionMode().isTestWithoutSimulation()) {
				measuredPowerProduction =
						new TimedMeasure<Double>(
								0.0,
								POWER_UNIT,
								this.getClock());
				ret = new SignalData<>(this.getClock(),
									   measuredPowerProduction);
			} else {
				measuredPowerProduction =
						new TimedMeasure<Double>(0.0, POWER_UNIT);
				ret = new SignalData<>(this.getClock4Simulation(),
									   measuredPowerProduction);
			}
		}

		assert	ret != null : new PostconditionException("return != null");
		assert	ret.isSingle() : new PostconditionException("return.isSingle()");
		assert	ret.getMeasure().getData() >= 0.0 :
				new PostconditionException("return.getMeasure().getData() >= 0.0");
		assert	ret.getMeasure().getMeasurementUnit().equals(
									ElectricMeterImplementationI.POWER_UNIT) :
				new PostconditionException(
						"return.getMeasure().getMeasurementUnit().equals("
						+ "ElectricMeterImplementationI.POWER_UNIT)");

		return ret;
	}

	/**
	 * @see fr.sorbonne_u.components.hem2025e1.equipments.meter.ElectricMeterImplementationI#getCurrentProduction()
	 */
	@Override
	public SignalData<Double>	getCurrentProduction() throws Exception
	{
		if (VERBOSE) {
			this.traceMessage("Electric meter returns its current production.\n");
		}


		SignalData<Double> ret = null;
		if (this.getExecutionMode().isSimulationTest()) {
			ret = this.currentPowerProduction.get();
		} else {
			TimedMeasure<Double> measuredPowerProduction = null;
			if (this.getExecutionMode().isTestWithoutSimulation()) {
				measuredPowerProduction =
						new TimedMeasure<Double>(
								0.0,
								POWER_UNIT,
								this.getClock());
				ret = new SignalData<>(this.getClock(),
									   measuredPowerProduction);
			} else {
				measuredPowerProduction =
						new TimedMeasure<Double>(0.0, POWER_UNIT);
				ret = new SignalData<>(this.getClock4Simulation(),
									   measuredPowerProduction);
			}
		}

		assert	ret != null : new PostconditionException("return != null");
		assert	ret.isSingle() : new PostconditionException("return.isSingle()");
		assert	ret.getMeasure().getData() >= 0.0 :
				new PostconditionException("return.getMeasure().getData() >= 0.0");
		assert	ret.getMeasure().getMeasurementUnit().equals(
									ElectricMeterImplementationI.POWER_UNIT) :
				new PostconditionException(
						"return.getMeasure().getMeasurementUnit().equals("
						+ "ElectricMeterImplementationI.POWER_UNIT)");

		return ret;
	}
}
// -----------------------------------------------------------------------------

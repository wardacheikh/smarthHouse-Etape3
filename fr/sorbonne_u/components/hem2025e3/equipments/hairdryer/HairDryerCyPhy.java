package fr.sorbonne_u.components.hem2025e3.equipments.hairdryer;

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
import fr.sorbonne_u.components.cyphy.interfaces.CyPhyReflectionCI;
import fr.sorbonne_u.components.cyphy.plugins.devs.AtomicSimulatorPlugin;
import fr.sorbonne_u.components.cyphy.plugins.devs.RTAtomicSimulatorPlugin;
import fr.sorbonne_u.components.cyphy.utils.aclocks.ClocksServerWithSimulation;
import fr.sorbonne_u.components.cyphy.utils.tests.TestScenarioWithSimulation;
import fr.sorbonne_u.components.exceptions.BCMException;
import fr.sorbonne_u.components.exceptions.ComponentShutdownException;
import fr.sorbonne_u.components.exceptions.ComponentStartException;
import fr.sorbonne_u.components.hem2025e3.equipments.hairdryer.sil.HairDryerStateSILModel;
import fr.sorbonne_u.components.hem2025e3.equipments.hairdryer.sil.Local_SIL_SimulationArchitectures;
import fr.sorbonne_u.components.utils.tests.TestScenario;
import fr.sorbonne_u.devs_simulation.architectures.RTArchitecture;
import fr.sorbonne_u.devs_simulation.models.annotations.ModelExternalEvents;
import fr.sorbonne_u.components.hem2025e1.equipments.hairdryer.HairDryerImplementationI;
import fr.sorbonne_u.components.hem2025e1.equipments.hairdryer.HairDryerUserCI;
import fr.sorbonne_u.components.hem2025e1.equipments.hairdryer.connections.HairDryerInboundPort;
import fr.sorbonne_u.components.hem2025e2.equipments.hairdryer.mil.events.SetHighHairDryer;
import fr.sorbonne_u.components.hem2025e2.equipments.hairdryer.mil.events.SetLowHairDryer;
import fr.sorbonne_u.components.hem2025e2.equipments.hairdryer.mil.events.SwitchOffHairDryer;
import fr.sorbonne_u.components.hem2025e2.equipments.hairdryer.mil.events.SwitchOnHairDryer;
import fr.sorbonne_u.exceptions.AssertionChecking;
import fr.sorbonne_u.exceptions.ImplementationInvariantException;
import fr.sorbonne_u.exceptions.InvariantException;
import fr.sorbonne_u.exceptions.PostconditionException;
import fr.sorbonne_u.exceptions.PreconditionException;
import java.util.HashMap;
import java.util.concurrent.TimeUnit;
import java.util.Set;
import java.util.HashSet;
import java.util.function.Supplier;
import fr.sorbonne_u.alasca.physical_data.Measure;
import fr.sorbonne_u.components.AbstractPort;

// -----------------------------------------------------------------------------
/**
 * The class <code>HairDryerCyPhy</code> implements the cyber-physical
 * component version of the hair dryer.
 *
 * <p><strong>Description</strong></p>
 * 
 * <p>
 * The hair dryer is an uncontrollable appliance, hence it does not connect
 * with the household energy manager. However, it will connect later to the
 * electric panel to take its (simulated) electricity consumption into account.
 * </p>
 * <p>
 * This implementation of the hair dryer is complicated by the objective to show
 * the entire spectrum of possible execution and simulation modes. There are
 * three execution types defined by {@code ExecutionMode}. Here, the component
 * implements the following modes:
 * </p>
 * <ol>
 * <li>{@code STANDARD}/{@code UNIT_TEST}/{@code INTEGRATION_TEST}: the
 *   component would execute in normal operational conditions, on the field
 *   (currently, however, there is no physical device).</li>
 * <li>{@code UNIT_TEST_WITH_SIL_SIMULATION}/{@code INTEGRATION_TEST_WITH_SIL_SIMULATION}:
 *   the component creates a local simulation architecture that will simulate
 *   the electricity power consumption of the hair dryer; in unit test, the
 *   simulation architecture operates alone while in integration test, it is
 *   meant to be composed with other local simulators of other components
 *   into an application-wide simulator. The annotation
 *   {@code SIL_Simulation_Architectures} declares the local simulation
 *   architectures that are provided by the component.</li>
 * </ol>
 * <p>
 * In this implementation of the {@code HairDryer} component, the standard
 * execution mode is not really implemented as the software is not embedded in
 * any real appliance. In unit tests with no simulation, the component is
 * totally passive as its methods are called by the {@code HairDryerUser}
 * component.
 * </p>
 * <p>
 * For SIL simulations in unit tests, the component presents a rather special
 * case as it is the only component that runs a simulator, hence there is no
 * global component simulation architecture and therefore no need for
 * supervisor and coordinator components. Hence, the local SIL simulation
 * architecture uses only the models pertaining to the hair dryer itself:
 * </p>
 * <p><img src="../../../../../../../images/hem-2025-e3/HairDryerUnitTestLocalArchitecture.png"/></p>
 * <p>
 * After creating (in {@code initialise}) its local SIL simulation architecture
 * and installing the local simulation plug-in (in {@code start}), the component
 * also creates, initialises and triggers the execution of the simulator in the
 * method {@code execute}.
 * </p>
 * <p>
 * For SIL simulations in integration tests, the {@code HairDryerElectricityModel}
 * cannot share its continuous variable {@code currentIntensity} with the
 * {@code ElectricMeterElectricityModel} across component borders. Hence, the
 * {@code HairDryerElectricityModel} is rather moved to the {@code ElectricMeter}
 * component simulator, to co-localise it with the
 * {@code ElectricMeterElectricityModel}, hence the {@code HairDryerStateModel}
 * remaining in the {@code HairDryer} component simulator will emits the hair
 * dryer events to the {@code HairDryerElectricityModel} across the border of
 * the {@code HairDryer} and {@code ElectricMeter} components.
 * </p>
 * 
 * <p><strong>Implementation Invariants</strong></p>
 * 
 * <pre>
 * invariant	{@code INITIAL_STATE != null}
 * invariant	{@code INITIAL_MODE != null}
 * invariant	{@code currentState != null}
 * invariant	{@code currentMode != null}
 * invariant	{@code NUMBER_OF_STANDARD_THREADS >= 0}
 * invariant	{@code NUMBER_OF_SCHEDULABLE_THREADS >= 0}
 * invariant	{@code localArchitectureURI == null || !localArchitectureURI.isEmpty() && accelerationFactor > 0.0}
 * invariant	{@code asp == null || localArchitectureURI != null}
 * </pre>
 * 
 * <p><strong>Invariants</strong></p>
 * 
 * <pre>
 * invariant	{@code REFLECTION_INBOUND_PORT_URI != null && !REFLECTION_INBOUND_PORT_URI.isEmpty()}
 * invariant	{@code INBOUND_PORT_URI != null && !INBOUND_PORT_URI.isEmpty()}
 * invariant	{@code UNIT_TEST_ARCHITECTURE_URI != null && !UNIT_TEST_ARCHITECTURE_URI.isEmpty()}
 * invariant	{@code INTEGRATION_TEST_ARCHITECTURE_URI != null && !INTEGRATION_TEST_ARCHITECTURE_URI.isEmpty()}
 * invariant	{@code HIGH_POWER_IN_WATTS != null && HIGH_POWER_IN_WATTS.getData() > 0.0 && HIGH_POWER_IN_WATTS.getMeasurementUnit().equals(POWER_UNIT)}
 * invariant	{@code LOW_POWER_IN_WATTS != null && LOW_POWER_IN_WATTS.getData() > 0.0 && LOW_POWER_IN_WATTS.getMeasurementUnit().equals(POWER_UNIT)}
 * invariant	{@code TENSION != null && (TENSION.getData() == 110.0 || TENSION.getData() == 220.0) && TENSION.getMeasurementUnit().equals(TENSION_UNIT)}
 * invariant	{@code INITIAL_STATE != null && INITIAL_MODE != null}
 * invariant	{@code X_RELATIVE_POSITION >= 0}
 * invariant	{@code Y_RELATIVE_POSITION >= 0}
 * </pre>
 * 
 * <p>Created on : 2023-09-19</p>
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
		// must be equal to the URI of the instance of HairDryerCoupledModel
		rootModelURI = "HairDryerCoupledModel",
		// next fields must be the same as the values used in the local
		// architecture
		simulatedTimeUnit = TimeUnit.HOURS,
		externalEvents = @ModelExternalEvents()
		),
	@LocalArchitecture(
		// must be equal to INTEGRATION_TEST_ARCHITECTURE_URI
		uri = "silIntegrationTests",
		// must be equal to the URI of the instance of HairDryerStateModel
		rootModelURI = "HairDryerStateSILModel",
		// next fields must be the same as the values used in the local
		// architecture
		simulatedTimeUnit = TimeUnit.HOURS,
		externalEvents =
			@ModelExternalEvents(
				imported = {},
				exported = {SwitchOnHairDryer.class,
							SwitchOffHairDryer.class,
							SetHighHairDryer.class,
							SetLowHairDryer.class}
				)
		)
	})
//-----------------------------------------------------------------------------
@OfferedInterfaces(offered = {HairDryerUserCI.class})
//-----------------------------------------------------------------------------
public class			HairDryerCyPhy
extends		AbstractCyPhyComponent
implements	HairDryerImplementationI
{
	// -------------------------------------------------------------------------
	// Constants and variables
	// -------------------------------------------------------------------------

	/** standard URI of the hair dryer reflection inbound port.				*/
	public static final String		REFLECTION_INBOUND_PORT_URI =
														"HAIR-DRYER-RIP-URI";	
	/** URI of the hair dryer inbound port used in tests.					*/
	public static final String		INBOUND_PORT_URI =
												"HAIR-DRYER-INBOUND-PORT-URI";
	/** URI of the local simulation architecture for SIL unit tests.		*/
	public static final String		UNIT_TEST_ARCHITECTURE_URI =
														"silUnitTests";
	/** URI of the local simulation architecture for SIL unit tests.		*/
	public static final String		INTEGRATION_TEST_ARCHITECTURE_URI =
														"silIntegrationTests";
	// Configuration

	/** power consumption when in mode HIGH in the power unit used by
	 *  the hair dryer.														*/
	public static final Measure<Double>	HIGH_POWER = new Measure<Double>(
														1100.0,
														POWER_UNIT);
	/** power consumption when in mode LOW in the power unit used by
	 *  the hair dryer.														*/
	public static final Measure<Double>	LOW_POWER = new Measure<Double>(
														660.0,
														POWER_UNIT);
	/** tension required by the hair dryer in the power unit used by
	 *  the hair dryer.														*/
	public static final Measure<Double>	TENSION = new Measure<Double>(
														220.0,
														TENSION_UNIT);

	// Internal component state variables

	/** initial state of the hair dryer.									*/
	public static final HairDryerState	INITIAL_STATE = HairDryerState.OFF;
	/** initial mode of the hair dryer.										*/
	public static final HairDryerMode	INITIAL_MODE = HairDryerMode.LOW;

	/** current state (on, off) of the hair dryer.							*/
	protected HairDryerState			currentState;
	/** current mode of operation (low, high) of the hair dryer.			*/
	protected HairDryerMode				currentMode;

	/** inbound port offering the <code>HairDryerCI</code> interface.		*/
	protected HairDryerInboundPort		hdip;

	// Execution/Simulation

	/** when true, methods trace their actions.								*/
	public static boolean				VERBOSE = false;
	/** when true, methods provides debugging traces of their actions.		*/
	public static boolean				DEBUG = false;
	/** when tracing, x coordinate of the window relative position.			*/
	public static int					X_RELATIVE_POSITION = 0;
	/** when tracing, y coordinate of the window relative position.			*/
	public static int					Y_RELATIVE_POSITION = 0;

	/** one thread for the method execute, which starts the local SIL
	 *  simulator, and one to answer the calls to the component services.	*/
	protected static int				NUMBER_OF_STANDARD_THREADS = 2;
	/** no need for statically defined schedulable threads.					*/
	protected static int				NUMBER_OF_SCHEDULABLE_THREADS = 0;

	/** plug-in holding the local simulation architecture and simulators.	*/
	protected AtomicSimulatorPlugin		asp;
	/** URI of the local simulation architecture used to compose the global
	 *  simulation architecture or the empty string if the component does
	 *  not execute as a simulation.										*/
	protected final String				localArchitectureURI;
	/** acceleration factor to be used when running the real time
	 *  simulation.															*/
	protected final double				accelerationFactor;

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
	 * post	{@code true}	// no postcondition.
	 * </pre>
	 *
	 * @return	true if the static invariants are observed, false otherwise.
	 */
	public static boolean	staticImplementationInvariants()
	{
		boolean ret = true;
		ret &= AssertionChecking.checkStaticImplementationInvariant(
				INITIAL_STATE != null,
				HairDryerCyPhy.class,
				"INITIAL_STATE != null");
		ret &= AssertionChecking.checkStaticImplementationInvariant(
				INITIAL_MODE != null,
				HairDryerCyPhy.class,
				"INITIAL_MODE != null");
		ret &= AssertionChecking.checkStaticImplementationInvariant(
				NUMBER_OF_STANDARD_THREADS >= 0,
				HairDryerCyPhy.class,
				"NUMBER_OF_STANDARD_THREADS >= 0");
		ret &= AssertionChecking.checkStaticImplementationInvariant(
				NUMBER_OF_SCHEDULABLE_THREADS >= 0,
				HairDryerCyPhy.class,
				"NUMBER_OF_SCHEDULABLE_THREADS");
		return ret;
	}

	/**
	 * return true if the implementation invariants are observed, false otherwise.
	 * 
	 * <p><strong>Contract</strong></p>
	 * 
	 * <pre>
	 * pre	{@code hd != null}
	 * post	{@code true}	// no postcondition.
	 * </pre>
	 *
	 * @param hd	instance to be tested.
	 * @return		true if the implementation invariants are observed, false otherwise.
	 */
	protected static boolean	implementationInvariants(HairDryerCyPhy hd)
	{
		assert	hd != null : new PreconditionException("hd != null");

		boolean ret = true;
		ret &= staticImplementationInvariants();
		ret &= AssertionChecking.checkInvariant(
				hd.currentState != null,
				HairDryerCyPhy.class, hd,
				"currentState != null");
		ret &= AssertionChecking.checkInvariant(
				hd.currentMode != null,
				HairDryerCyPhy.class, hd,
				"currentMode != null");
		ret &= AssertionChecking.checkInvariant(
				hd.localArchitectureURI == null ||
						!hd.localArchitectureURI.isEmpty() &&
								hd.accelerationFactor > 0.0,
				HairDryerCyPhy.class, hd,
				"localArchitectureURI == null || !localArchitectureURI.isEmpty()"
				+ " && accelerationFactor > 0.0");
		ret &= AssertionChecking.checkInvariant(
				hd.asp == null || hd.localArchitectureURI != null,
				HairDryerCyPhy.class, hd,
				"asp == null || localArchitectureURI != null");
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
				REFLECTION_INBOUND_PORT_URI != null &&
									!REFLECTION_INBOUND_PORT_URI.isEmpty(),
				HairDryerCyPhy.class,
				"REFLECTION_INBOUND_PORT_URI != null && "
								+ "!REFLECTION_INBOUND_PORT_URI.isEmpty()");
		ret &= AssertionChecking.checkStaticInvariant(
				INBOUND_PORT_URI != null && !INBOUND_PORT_URI.isEmpty(),
				HairDryerCyPhy.class,
				"INBOUND_PORT_URI != null && !INBOUND_PORT_URI.isEmpty()");
		ret &= AssertionChecking.checkStaticInvariant(
				UNIT_TEST_ARCHITECTURE_URI != null &&
										!UNIT_TEST_ARCHITECTURE_URI.isEmpty(),
				HairDryerCyPhy.class,
				"UNIT_TEST_ARCHITECTURE_URI != null && "
				+ "!UNIT_TEST_ARCHITECTURE_URI.isEmpty()");
		ret &= AssertionChecking.checkStaticInvariant(
				INTEGRATION_TEST_ARCHITECTURE_URI != null &&
								!INTEGRATION_TEST_ARCHITECTURE_URI.isEmpty(),
				HairDryerCyPhy.class,
				"INTEGRATION_TEST_ARCHITECTURE_URI != null && "
				+ "!INTEGRATION_TEST_ARCHITECTURE_URI.isEmpty()");
		ret &= AssertionChecking.checkStaticInvariant(
				HIGH_POWER != null &&
					HIGH_POWER.getData() > 0.0 &&
					HIGH_POWER.getMeasurementUnit().equals(POWER_UNIT),
				HairDryerCyPhy.class,
				"HIGH_POWER_IN_WATTS != null && HIGH_POWER_IN_WATTS.getData()"
				+ " > 0.0 && HIGH_POWER_IN_WATTS.getMeasurementUnit().equals("
				+ "POWER_UNIT)");
		ret &= AssertionChecking.checkStaticInvariant(
				LOW_POWER != null &&
					LOW_POWER.getData() > 0.0 &&
					LOW_POWER.getMeasurementUnit().equals(POWER_UNIT),
				HairDryerCyPhy.class,
				"LOW_POWER_IN_WATTS != null && LOW_POWER_IN_WATTS.getData() >"
				+ " 0.0 && LOW_POWER_IN_WATTS.getMeasurementUnit().equals("
				+ "POWER_UNIT)");
		ret &= AssertionChecking.checkStaticInvariant(
				TENSION != null &&
					(TENSION.getData() == 110.0 || TENSION.getData() == 220.0) &&
					TENSION.getMeasurementUnit().equals(TENSION_UNIT),
				HairDryerCyPhy.class,
				"TENSION != null && (TENSION.getData() == 110.0 || TENSION."
				+ "getData() == 220.0) && TENSION.getMeasurementUnit().equals("
				+ "TENSION_UNIT)");
		ret &= AssertionChecking.checkStaticInvariant(
				INITIAL_STATE != null && INITIAL_MODE != null,
				HairDryerCyPhy.class,
				"INITIAL_STATE != null && INITIAL_MODE != null");
		ret &= AssertionChecking.checkStaticInvariant(
				X_RELATIVE_POSITION >= 0,
				HairDryerCyPhy.class,
				"X_RELATIVE_POSITION >= 0");
		ret &= AssertionChecking.checkStaticInvariant(
				Y_RELATIVE_POSITION >= 0,
				HairDryerCyPhy.class,
				"Y_RELATIVE_POSITION >= 0");
		return ret;
	}

	/**
	 * return true if the invariants are observed, false otherwise.
	 * 
	 * <p><strong>Contract</strong></p>
	 * 
	 * <pre>
	 * pre	{@code hd != null}
	 * post	{@code true}	// no postcondition.
	 * </pre>
	 *
	 * @param hd	instance to be tested.
	 * @return		true if the invariants are observed, false otherwise.
	 */
	protected static boolean	invariants(HairDryerCyPhy hd)
	{
		assert	hd != null : new PreconditionException("hd != null");

		boolean ret = true;
		ret &= staticInvariants();
		return ret;
	}

	// -------------------------------------------------------------------------
	// Constructors
	// -------------------------------------------------------------------------

	// Standard execution

	/**
	 * create a hair dryer component for standard execution.
	 * 
	 * <p><strong>Contract</strong></p>
	 * 
	 * <pre>
	 * pre	{@code !(this instanceof ComponentInterface)}
	 * post	{@code getState() == INITIAL_STATE}
	 * post	{@code getMode() == INITIAL_MODE}
	 * post	{@code getExecutionMode().isStandard()}
	 * </pre>
	 * 
	 * @throws Exception	<i>to do</i>.
	 */
	protected			HairDryerCyPhy() throws Exception
	{
		this(INBOUND_PORT_URI);
	}

	/**
	 * create a hair dryer component for standard execution with the given
	 * inbound port URI.
	 * 
	 * <p><strong>Contract</strong></p>
	 * 
	 * <pre>
	 * pre	{@code !(this instanceof ComponentInterface)}
	 * pre	{@code hairDryerInboundPortURI != null && !hairDryerInboundPortURI.isEmpty()}
	 * post	{@code getState() == INITIAL_STATE}
	 * post	{@code getMode() == INITIAL_MODE}
	 * post	{@code getExecutionMode().isStandard()}
	 * </pre>
	 * 
	 * @param hairDryerInboundPortURI	URI of the hair dryer inbound port.
	 * @throws Exception				<i>to do</i>.
	 */
	protected			HairDryerCyPhy(String hairDryerInboundPortURI)
	throws Exception
	{
		this(AbstractPort.generatePortURI(CyPhyReflectionCI.class),
			 hairDryerInboundPortURI);
	}

	/**
	 * create a hair dryer component for standard execution with the given
	 * reflection inbound port URI and inbound port URI.
	 * 
	 * <p><strong>Contract</strong></p>
	 * 
	 * <pre>
	 * pre	{@code !(this instanceof ComponentInterface)}
	 * pre	{@code reflectionInboundPortURI != null && !reflectionInboundPortURI.isEmpty()}
	 * pre	{@code hairDryerInboundPortURI != null && !hairDryerInboundPortURI.isEmpty()}
	 * post	{@code getState() == INITIAL_STATE}
	 * post	{@code getMode() == INITIAL_MODE}
	 * post	{@code getExecutionMode().isStandard()}
	 * </pre>
	 *
	 * @param reflectionInboundPortURI	URI of the reflection innbound port of the component.
	 * @param hairDryerInboundPortURI	URI of the hair dryer inbound port.
	 * @throws Exception				<i>to do</i>.
	 */
	protected			HairDryerCyPhy(
		String reflectionInboundPortURI,
		String hairDryerInboundPortURI
		) throws Exception
	{
		super(reflectionInboundPortURI,
			  NUMBER_OF_STANDARD_THREADS,
			  NUMBER_OF_SCHEDULABLE_THREADS);

		this.localArchitectureURI = null;
		this.accelerationFactor = 0.0;

		this.initialise(hairDryerInboundPortURI);

		assert	HairDryerCyPhy.implementationInvariants(this) :
				new ImplementationInvariantException(
						"HairDryerCyPhy.implementationInvariants(this)");
		assert	HairDryerCyPhy.invariants(this) :
				new InvariantException("HairDryerCyPhy.invariants(this)");
	}

	// Tests without simulation execution

	/**
	 * create a hair dryer component for test executions without simulation.
	 * 
	 * <p><strong>Contract</strong></p>
	 * 
	 * <pre>
	 * pre	{@code !(this instanceof ComponentInterface)}
	 * pre	{@code executionMode != null && executionMode.isTestWithoutSimulation()}
	 * post	{@code getState() == INITIAL_STATE}
	 * post	{@code getMode() == INITIAL_MODE}
	 * post	{@code getExecutionMode().equals(executionMode)}
	 * </pre>
	 * 
	 * @param executionMode	execution mode for the next run.
	 * @throws Exception	<i>to do</i>.
	 */
	protected			HairDryerCyPhy(
		ExecutionMode executionMode
		) throws Exception
	{
		this(REFLECTION_INBOUND_PORT_URI, INBOUND_PORT_URI,
			 AssertionChecking.assertTrueAndReturnOrThrow(
					 executionMode != null
									&& executionMode.isTestWithoutSimulation(),
									executionMode,
					() -> { return new PreconditionException(
											"executionMode != null && "
											+ "executionMode."
											+ "isTestWithoutSimulation()");
						  }));
	}

	/**
	 * create a hair dryer component for test executions without simulation with
	 * the given inbound port URI.
	 * 
	 * <p><strong>Contract</strong></p>
	 * 
	 * <pre>
	 * pre	{@code !(this instanceof ComponentInterface)}
	 * pre	{@code hairDryerInboundPortURI != null && !hairDryerInboundPortURI.isEmpty()}
	 * pre	{@code executionMode != null && executionMode.isTestWithoutSimulation()}
	 * post	{@code getState() == INITIAL_STATE}
	 * post	{@code getMode() == INITIAL_MODE}
	 * post	{@code getExecutionMode().equals(executionMode)}
	 * </pre>
	 * 
	 * @param hairDryerInboundPortURI	URI of the hair dryer inbound port.
	 * @param executionMode		execution mode for the next run.
	 * @throws Exception				<i>to do</i>.
	 */
	protected			HairDryerCyPhy(
		String hairDryerInboundPortURI,
		ExecutionMode executionMode
		) throws Exception
	{
		this(REFLECTION_INBOUND_PORT_URI, hairDryerInboundPortURI,
			 AssertionChecking.assertTrueAndReturnOrThrow(
					executionMode != null
							&& executionMode.isTestWithoutSimulation(),
					executionMode,
					() -> { return new PreconditionException(
											"executionType != null && "
											+ "executionType."
											+ "isTestWithoutSimulation()");
						  }));
	}

	/**
	 * create a hair dryer component for test executions without simulation with
	 * the given reflection inbound port URI and inbound port URI.
	 * 
	 * <p><strong>Contract</strong></p>
	 * 
	 * <pre>
	 * pre	{@code !(this instanceof ComponentInterface)}
	 * pre	{@code reflectionInboundPortURI != null && !reflectionInboundPortURI.isEmpty()}
	 * pre	{@code hairDryerInboundPortURI != null && !hairDryerInboundPortURI.isEmpty()}
	 * pre	{@code executionMode != null && executionMode.isTestWithoutSimulation()}
	 * post	{@code getState() == INITIAL_STATE}
	 * post	{@code getMode() == INITIAL_MODE}
	 * post	{@code getExecutionMode().equals(executionMode)}
	 * </pre>
	 *
	 * @param reflectionInboundPortURI	URI of the reflection innbound port of the component.
	 * @param hairDryerInboundPortURI	URI of the hair dryer inbound port.
	 * @param executionMode				execution mode for the next run.
	 * @throws Exception				<i>to do</i>.
	 */
	protected			HairDryerCyPhy(
		String reflectionInboundPortURI,
		String hairDryerInboundPortURI,
		ExecutionMode executionMode
		) throws Exception
	{
		super(reflectionInboundPortURI,
			  NUMBER_OF_STANDARD_THREADS,
			  NUMBER_OF_SCHEDULABLE_THREADS,
			  executionMode,
			  "fake-clock",		// passive component, do not need a clock
			  null);

		assert	executionMode != null &&
									executionMode.isTestWithoutSimulation() :
				new PreconditionException(
						"executionMode != null && executionMode."
						+ "isTestWithoutSimulation()");

		this.localArchitectureURI = null;
		this.accelerationFactor = 0.0;

		this.initialise(hairDryerInboundPortURI);

		assert	HairDryerCyPhy.implementationInvariants(this) :
				new ImplementationInvariantException(
						"HairDryerCyPhy.implementationInvariants(this)");
		assert	HairDryerCyPhy.invariants(this) :
				new InvariantException("HairDryerCyPhy.invariants(this)");
	}

	// Tests with simulation

	/**
	 * create a hair dryer component for test executions with simulation.
	 * 
	 * <p><strong>Contract</strong></p>
	 * 
	 * <pre>
	 * pre	{@code !(this instanceof ComponentInterface)}
	 * pre	{@code reflectionInboundPortURI != null && !reflectionInboundPortURI.isEmpty()}
	 * pre	{@code hairDryerInboundPortURI != null && !hairDryerInboundPortURI.isEmpty()}
	 * pre	{@code executionMode != null && executionMode.isSimulationTest()}
	 * pre	{@code testScenario == null}
	 * pre	{@code localArchitectureURI != null && !localArchitectureURI.isEmpty()}
	 * pre	{@code accelerationFactor > 0.0}
	 * post	{@code getState() == INITIAL_STATE}
	 * post	{@code getMode() == INITIAL_MODE}
	 * post	{@code getExecutionMode().equals(executionMode)}
	 * </pre>
	 *
	 * @param reflectionInboundPortURI	URI of the reflection inbound port of the component.
	 * @param hairDryerInboundPortURI	URI of the hair dryer inbound port.
	 * @param executionMode				execution type for the next run.
	 * @param testScenario				test scenario to be executed with this component.
	 * @param localArchitectureURI		URI of the local simulation architecture to be used in composing the global simulation architecture.
	 * @param accelerationFactor		acceleration factor for the simulation.
	 * @throws Exception				<i>to do</i>.
	 */
	protected			HairDryerCyPhy(
		String reflectionInboundPortURI,
		String hairDryerInboundPortURI,
		ExecutionMode executionMode,
		TestScenario testScenario,
		String localArchitectureURI,
		double accelerationFactor
		) throws Exception
	{
		// one thread for the method execute and one to answer the calls to
		// the component services
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
			  accelerationFactor);

		assert	hairDryerInboundPortURI != null &&
											!hairDryerInboundPortURI.isEmpty() :
				new PreconditionException(
						"hairDryerInboundPortURI != null && "
						+ "!hairDryerInboundPortURI.isEmpty()");

		this.localArchitectureURI = localArchitectureURI;
		this.accelerationFactor = accelerationFactor;

		this.initialise(hairDryerInboundPortURI);

		if (DEBUG) {
			this.logMessage("HairDryerCyPhy local simulation architectures: "
							+ this.localSimulationArchitectures);
		}

		assert	HairDryerCyPhy.implementationInvariants(this) :
				new ImplementationInvariantException(
						"HairDryerCyPhy.implementationInvariants(this)");
		assert	HairDryerCyPhy.invariants(this) :
				new InvariantException("HairDryerCyPhy.invariants(this)");
	}

	// -------------------------------------------------------------------------
	// Initialisation methods
	// -------------------------------------------------------------------------

	/**
	 * initialise the hair dryer component.
	 * 
	 * <p><strong>Contract</strong></p>
	 * 
	 * <pre>
	 * pre	{@code hairDryerInboundPortURI != null && !hairDryerInboundPortURI.isEmpty()}
	 * post	{@code getState() == HairDryerState.OFF}
	 * post	{@code getMode() == HairDryerMode.LOW}
	 * </pre>
	 * 
	 * @param hairDryerInboundPortURI	URI of the hair dryer inbound port.
	 * @throws Exception				<i>to do</i>.
	 */
	protected void		initialise(String hairDryerInboundPortURI)
	throws Exception
	{
		assert	hairDryerInboundPortURI != null :
					new PreconditionException(
										"hairDryerInboundPortURI != null");
		assert	!hairDryerInboundPortURI.isEmpty() :
					new PreconditionException(
										"!hairDryerInboundPortURI.isEmpty()");

		this.currentState = INITIAL_STATE;
		this.currentMode = INITIAL_MODE;
		this.hdip = new HairDryerInboundPort(hairDryerInboundPortURI, this);
		this.hdip.publishPort();

		if (HairDryerCyPhy.VERBOSE || HairDryerCyPhy.DEBUG) {
			this.tracer.get().setTitle("Hair dryer component");
			this.tracer.get().setRelativePosition(X_RELATIVE_POSITION,
												  Y_RELATIVE_POSITION);
			this.toggleTracing();
		}

		assert	HairDryerCyPhy.implementationInvariants(this) :
				new ImplementationInvariantException(
						"HairDryer.implementationInvariants(this)");
		assert	HairDryerCyPhy.invariants(this) :
				new InvariantException("HairDryer.invariants(this)");
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
						createHairDryerSIL_Architecture4UnitTest(
									architectureURI,
									rootModelURI,
									simulatedTimeUnit,
									accelerationFactor);
		} else if (architectureURI.equals(INTEGRATION_TEST_ARCHITECTURE_URI)) {
			ret = Local_SIL_SimulationArchitectures.
						createHairDryerSIL_Architecture4IntegrationTest(
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
	// Component life-cycle
	// -------------------------------------------------------------------------

	/**
	 * @see fr.sorbonne_u.components.AbstractComponent#start()
	 */
	@Override
	public synchronized void	start() throws ComponentStartException
	{
		super.start();

		assert	HairDryerCyPhy.implementationInvariants(this) :
				new ImplementationInvariantException(
						"HairDryer.implementationInvariants(this)");
		assert	HairDryerCyPhy.invariants(this) :
				new InvariantException("HairDryer.invariants(this)");

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
				// for the HairDryer, real time MIL and SIL use the same
				// simulation models
				RTArchitecture architecture =
					(RTArchitecture) this.localSimulationArchitectures.
												get(this.localArchitectureURI);
				this.asp = new RTAtomicSimulatorPlugin();
				((RTAtomicSimulatorPlugin)this.asp).
								setPluginURI(architecture.getRootModelURI());
				((RTAtomicSimulatorPlugin)this.asp).
										setSimulationArchitecture(architecture);
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

		assert	HairDryerCyPhy.implementationInvariants(this) :
				new ImplementationInvariantException(
						"HairDryer.implementationInvariants(this)");
		assert	HairDryerCyPhy.invariants(this) :
				new InvariantException("HairDryer.invariants(this)");
	}

	/**
	 * @see fr.sorbonne_u.components.AbstractComponent#execute()
	 */
	@Override
	public void			execute() throws Exception
	{
		this.traceMessage("Hair Dryer CyPhy executes.\n");

		assert	HairDryerCyPhy.implementationInvariants(this) :
				new ImplementationInvariantException(
						"HairDryer.implementationInvariants(this)");
		assert	HairDryerCyPhy.invariants(this) :
				new InvariantException("HairDryer.invariants(this)");

		switch (this.getExecutionMode()) {
		case UNIT_TEST:
		case INTEGRATION_TEST:
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
			// wait until the simulation ends
			this.getClock4Simulation().waitUntilEnd();
			// give some time for the end of simulation catering tasks
			Thread.sleep(200L);
			// get and print the simulation report
			this.logMessage(this.asp.getFinalReport().toString());
			break;
		case INTEGRATION_TEST_WITH_SIL_SIMULATION:
			break;
		case UNIT_TEST_WITH_HIL_SIMULATION:
		case INTEGRATION_TEST_WITH_HIL_SIMULATION:
				throw new BCMException("HIL simulation not implemented yet!");
		case STANDARD:
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
			this.hdip.unpublishPort();
		} catch (Throwable e) {
			throw new ComponentShutdownException(e) ;
		}
		super.shutdown();
	}

	// -------------------------------------------------------------------------
	// Component services implementation
	// -------------------------------------------------------------------------

	/**
	 * @see fr.sorbonne_u.components.hem2025e1.equipments.hairdryer.HairDryerImplementationI#getState()
	 */
	@Override
	public HairDryerState	getState() throws Exception
	{
		if (HairDryerCyPhy.VERBOSE) {
			this.traceMessage("Hair dryer returns its state : " +
													this.currentState + ".\n");
		}

		return this.currentState;
	}

	/**
	 * @see fr.sorbonne_u.components.hem2025e1.equipments.hairdryer.HairDryerImplementationI#getMode()
	 */
	@Override
	public HairDryerMode	getMode() throws Exception
	{
		if (HairDryerCyPhy.VERBOSE) {
			this.traceMessage("Hair dryer returns its mode : " +
													this.currentMode + ".\n");
		}

		return this.currentMode;
	}

	/**
	 * @see fr.sorbonne_u.components.hem2025e1.equipments.hairdryer.HairDryerImplementationI#turnOn()
	 * 
	 * 
	 * 
	 *  Contient le plugin de simulation (AtomicSimulatorPlugin asp)
        ✅ Envoie des événements au simulateur quand on change de mode
        ✅ Lit des valeurs depuis le simulateur (puissance consommée)
	 * 
	 * 
	 * 
	 */
	@Override
	public void			turnOn() throws Exception
	{
		if (HairDryerCyPhy.VERBOSE) {
			this.traceMessage("Hair dryer is turned on.\n");
		}

		assert	this.getState() == HairDryerState.OFF :
				new PreconditionException("getState() == HairDryerState.OFF");

		this.currentState = HairDryerState.ON;
		this.currentMode = HairDryerCyPhy.INITIAL_MODE;

		assert	this.getState() == HairDryerState.ON :
				new PostconditionException("getState() == HairDryerState.ON");
		assert	this.getMode() == HairDryerCyPhy.INITIAL_MODE :
				new PostconditionException(
						"getMode() == HairDryerCyPhy.INITIAL_MODE");

		if (this.getExecutionMode().isSILTest()) {
			// For SIL simulation, an operation done in the component code
			// must be reflected in the simulation; to do so, the component
			// code triggers an external event sent to the HairDryerStateModel
			// to make it change its state to on.
			((RTAtomicSimulatorPlugin)this.asp).triggerExternalEvent(
												HairDryerStateSILModel.URI,
												t -> new SwitchOnHairDryer(t));
		}
	}

	/**
	 * @see fr.sorbonne_u.components.hem2025e1.equipments.hairdryer.HairDryerImplementationI#turnOff()
	 */
	@Override
	public void			turnOff() throws Exception
	{
		if (HairDryerCyPhy.VERBOSE) {
			this.traceMessage("Hair dryer is turned off.\n");
		}

		assert	this.getState() == HairDryerState.ON :
				new PreconditionException("getState() == HairDryerState.ON");

		this.currentState = HairDryerState.OFF;
		this.currentMode = HairDryerCyPhy.INITIAL_MODE;

		assert	this.getState() == HairDryerState.OFF :
				new PostconditionException("getState() == HairDryerState.OFF");
		assert	this.getMode() == HairDryerCyPhy.INITIAL_MODE :
				new PostconditionException(
						"getMode() == HairDryerCyPhy.INITIAL_MODE");

		if (this.getExecutionMode().isSILTest()) {
			// For SIL simulation, an operation done in the component code
			// must be reflected in the simulation; to do so, the component
			// code triggers an external event sent to the HairDryerStateModel
			// to make it change its state to off.
			((RTAtomicSimulatorPlugin)this.asp).triggerExternalEvent(
												HairDryerStateSILModel.URI,
												t -> new SwitchOffHairDryer(t));
		}
	}

	/**
	 * @see fr.sorbonne_u.components.hem2025e1.equipments.hairdryer.HairDryerImplementationI#setHigh()
	 */
	@Override
	public void			setHigh() throws Exception
	{
		if (HairDryerCyPhy.VERBOSE) {
			this.traceMessage("Hair dryer is set high.\n");
		}

		assert	this.getState() == HairDryerState.ON :
				new PreconditionException("getState() == HairDryerState.ON");
		assert	this.getMode() == HairDryerMode.LOW :
				new PreconditionException("getMode() == HairDryerMode.LOW");

		this.currentMode = HairDryerMode.HIGH;

		assert	this.getState() == HairDryerState.ON :
				new PostconditionException("getState() == HairDryerState.ON");
		assert	this.getMode() == HairDryerMode.HIGH :
				new PostconditionException("getMode() == HairDryerMode.HIGH");

		if (this.getExecutionMode().isSILTest()) {
			// For SIL simulation, an operation done in the component code
			// must be reflected in the simulation; to do so, the component
			// code triggers an external event sent to the HairDryerStateModel
			// to make it change its mode to high.
			((RTAtomicSimulatorPlugin)this.asp).triggerExternalEvent(
												HairDryerStateSILModel.URI,
												t -> new SetHighHairDryer(t));
		}
	}

	/**
	 * @see fr.sorbonne_u.components.hem2025e1.equipments.hairdryer.HairDryerImplementationI#setLow()
	 */
	@Override
	public void			setLow() throws Exception
	{
		if (HairDryerCyPhy.VERBOSE) {
			this.traceMessage("Hair dryer is set low.\n");
		}

		assert	this.getState() == HairDryerState.ON :
				new PreconditionException("getState() == HairDryerState.ON");
		assert	this.getMode() == HairDryerMode.HIGH :
				new PreconditionException("getMode() == HairDryerMode.HIGH");

		this.currentMode = HairDryerMode.LOW;

		assert	this.getState() == HairDryerState.ON :
				new PostconditionException("getState() == HairDryerState.ON");
		assert	this.getMode() == HairDryerMode.LOW :
				new PostconditionException("getMode() == HairDryerMode.LOW");

		if (this.getExecutionMode().isSILTest()) {
			// For SIL simulation, an operation done in the component code
			// must be reflected in the simulation; to do so, the component
			// code triggers an external event sent to the HairDryerStateModel
			// to make it change its mode to low.
			((RTAtomicSimulatorPlugin)this.asp).triggerExternalEvent(
												HairDryerStateSILModel.URI,
												t -> new SetLowHairDryer(t));
		}
	}
}
// -----------------------------------------------------------------------------

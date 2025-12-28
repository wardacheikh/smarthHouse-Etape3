package fr.sorbonne_u.components.hem2025e3;

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

import fr.sorbonne_u.components.cvm.AbstractCVM; 
import fr.sorbonne_u.components.cyphy.ExecutionMode;
import fr.sorbonne_u.components.cyphy.utils.aclocks.ClocksServerWithSimulation;
import fr.sorbonne_u.components.cyphy.utils.tests.TestScenarioWithSimulation;
import fr.sorbonne_u.components.exceptions.BCMRuntimeException;
import fr.sorbonne_u.components.hem2025e3.equipements.wifi.BoxWifiCyPhy;
import fr.sorbonne_u.components.hem2025e3.equipements.wifi.BoxWifiTesterCyPhy;
import fr.sorbonne_u.components.hem2025e3.equipments.hairdryer.HairDryerCyPhy;
import fr.sorbonne_u.components.hem2025e3.equipments.hairdryer.HairDryerTesterCyPhy;
import fr.sorbonne_u.components.hem2025e3.equipments.heater.HeaterController;
import fr.sorbonne_u.components.hem2025e3.equipments.heater.HeaterCyPhy;
import fr.sorbonne_u.components.hem2025e3.equipments.heater.HeaterTesterCyPhy;
import fr.sorbonne_u.components.hem2025e3.equipments.heater.HeaterController.ControlMode;
import fr.sorbonne_u.components.hem2025e3.equipments.hem.HEMCyPhy;
import fr.sorbonne_u.components.hem2025e3.equipments.meter.ElectricMeterCyPhy;



import fr.sorbonne_u.components.utils.tests.TestScenario;
import fr.sorbonne_u.components.utils.tests.TestStep;
import fr.sorbonne_u.components.utils.tests.TestStepI;
import fr.sorbonne_u.devs_simulation.models.time.Duration;
import fr.sorbonne_u.devs_simulation.models.time.Time;
import fr.sorbonne_u.devs_simulation.models.time.TimeUtils;
import fr.sorbonne_u.exceptions.AssertionChecking;
import fr.sorbonne_u.exceptions.InvariantException;
import fr.sorbonne_u.exceptions.PreconditionException;
import fr.sorbonne_u.exceptions.VerboseException;
import fr.sorbonne_u.utils.aclocks.ClocksServer;
import java.time.Instant;
import java.util.concurrent.TimeUnit;
import fr.sorbonne_u.components.AbstractComponent;

// -----------------------------------------------------------------------------
/**
 * The class <code>CVMIntegrationTest</code> defines the integration test
 * for the household energy management example.
 *
 * <p><strong>Description</strong></p>
 * 
 * <p><strong>Implementation Invariants</strong></p>
 * 
 * <pre>
 * invariant	{@code true}	// no more invariant
 * </pre>
 * 
 * <p><strong>Invariants</strong></p>
 * 
 * <pre>
 * invariant	{@code CLOCK_URI != null && !CLOCK_URI.isEmpty()}
 * invariant	{@code DELAY_TO_START_IN_MILLIS >= 0}
 * invariant	{@code ACCELERATION_FACTOR > 0.0}
 * invariant	{@code START_INSTANT != null}
 * </pre>
 * 
 * <p>Created on : 2021-09-10</p>
 * 
 * @author	<a href="mailto:Jacques.Malenfant@lip6.fr">Jacques Malenfant</a>
 */
public class			CVMIntegrationTest
extends		AbstractCVM
{
	/** delay before starting the test scenarios, leaving time to build
	 *  and initialise the components and their simulators; this delay is
	 *  estimated given the complexity of the initialisation (including the
	 *  creation of the application simulator if simulation is used). It
	 *  could need to be revised if the computer on which the application
	 *  is run is less powerful.											*/
	public static long			DELAY_TO_START = 5000L;
	/** duration of the sleep at the end of the execution before exiting
	 *  the JVM.															*/
	public static long			END_SLEEP_DURATION = 100000L;

	/** time unit in which {@code SIMULATION_DURATION} is expressed.		*/
	public static TimeUnit		SIMULATION_TIME_UNIT = TimeUnit.HOURS;
	/** start time of the simulation, in simulated logical time, if
	 *  relevant.															*/
	public static Time 			SIMULATION_START_TIME =
										new Time(0.0, SIMULATION_TIME_UNIT);
	/** duration  of the simulation, in simulated time.						*/
	public static Duration		SIMULATION_DURATION =
										new Duration(6.0, SIMULATION_TIME_UNIT);
	/** for real time simulations, the acceleration factor applied to the
	 *  the simulated time to get the execution time of the simulations. 	*/
	public static double		ACCELERATION_FACTOR = 360.0;
	/** duration of the execution.											*/
	public static long			EXECUTION_DURATION =
			DELAY_TO_START +
				TimeUnit.NANOSECONDS.toMillis(
						TimeUtils.toNanos(
								SIMULATION_DURATION.getSimulatedDuration()/
													ACCELERATION_FACTOR,
								SIMULATION_DURATION.getTimeUnit()));

	public static ExecutionMode	GLOBAL_EXECUTION_MODE =
//						ExecutionMode.INTEGRATION_TEST;
						ExecutionMode.INTEGRATION_TEST_WITH_SIL_SIMULATION;

	/** for unit tests and SIL simulation unit tests, a {@code Clock} is
	 *  used to get a time-triggered synchronisation of the actions of
	 *  the components in the test scenarios.								*/
	public static String		CLOCK_URI = "integration-test-clock";
	/** start instant in test scenarios, as a string to be parsed.			*/
	public static Instant		START_INSTANT =
									Instant.parse("2025-12-02T06:00:00.00Z");

	// Solar panel constants

	/** number of square meters in the test solar panel.					*/
	public static final int		NB_OF_SQUARE_METERS = 10;

	// -------------------------------------------------------------------------
	// Invariants
	// -------------------------------------------------------------------------

	/**
	 * return true if the implementation invariants are observed, false otherwise.
	 * 
	 * <p><strong>Contract</strong></p>
	 * 
	 * <pre>
	 * pre	{@code cvm != null}
	 * post	{@code true}	// no postcondition.
	 * </pre>
	 *
	 * @param cvm	instance to be tested.
	 * @return		true if the implementation invariants are observed, false otherwise.
	 */
	protected static boolean	implementationInvariants(CVMIntegrationTest cvm)
	{
		assert	cvm != null : new PreconditionException("cvm != null");

		boolean ret = true;
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
				CLOCK_URI != null && !CLOCK_URI.isEmpty(),
				CVMIntegrationTest.class,
				"CLOCK_URI != null && !CLOCK_URI.isEmpty()");
		ret &= AssertionChecking.checkStaticInvariant(
				DELAY_TO_START >= 0,
				CVMIntegrationTest.class,
				"DELAY_TO_START >= 0");
		ret &= AssertionChecking.checkStaticInvariant(
				ACCELERATION_FACTOR > 0.0,
				CVMIntegrationTest.class,
				"ACCELERATION_FACTOR > 0.0");
		ret &= AssertionChecking.checkStaticInvariant(
				START_INSTANT != null,
				CVMIntegrationTest.class,
				"START_INSTANT != null");
		return ret;
	}

	/**
	 * return true if the invariants are observed, false otherwise.
	 * 
	 * <p><strong>Contract</strong></p>
	 * 
	 * <pre>
	 * pre	{@code cvm != null}
	 * post	{@code true}	// no postcondition.
	 * </pre>
	 *
	 * @param cvm	instance to be tested.
	 * @return	true if the invariants are observed, false otherwise.
	 */
	protected static boolean	invariants(CVMIntegrationTest cvm)
	{
		assert	cvm != null : new PreconditionException("cvm != null");

		boolean ret = true;
		ret &= staticInvariants();
		return ret;
	}

	// -------------------------------------------------------------------------
	// Constructors
	// -------------------------------------------------------------------------

	public				CVMIntegrationTest() throws Exception
	{
		// Trace and trace window positions
		ClocksServer.VERBOSE = true;
		ClocksServer.X_RELATIVE_POSITION = 0;
		ClocksServer.Y_RELATIVE_POSITION = 0;
		HEMCyPhy.VERBOSE = true;
		HEMCyPhy.X_RELATIVE_POSITION = 0;
		HEMCyPhy.Y_RELATIVE_POSITION = 1;
		ElectricMeterCyPhy.VERBOSE = true;
		ElectricMeterCyPhy.X_RELATIVE_POSITION = 1;
		ElectricMeterCyPhy.Y_RELATIVE_POSITION = 0;
		
		HairDryerTesterCyPhy.VERBOSE = true;
		HairDryerTesterCyPhy.X_RELATIVE_POSITION = 0;
		HairDryerTesterCyPhy.Y_RELATIVE_POSITION = 2;
		HairDryerCyPhy.VERBOSE = true;
		HairDryerCyPhy.X_RELATIVE_POSITION = 1;
		HairDryerCyPhy.Y_RELATIVE_POSITION = 2;
		HeaterTesterCyPhy.VERBOSE = true;
		HeaterTesterCyPhy.X_RELATIVE_POSITION = 0;
		HeaterTesterCyPhy.Y_RELATIVE_POSITION = 3;
		HeaterCyPhy.VERBOSE = true;
		HeaterCyPhy.X_RELATIVE_POSITION = 1;
		HeaterCyPhy.Y_RELATIVE_POSITION = 3;
		HeaterController.VERBOSE = true;
		HeaterController.X_RELATIVE_POSITION = 2;
		HeaterController.Y_RELATIVE_POSITION = 3;
		
		// Configuration pour Box WiFi
		BoxWifiTesterCyPhy.VERBOSE = true;
		BoxWifiTesterCyPhy.X_RELATIVE_POSITION = 0;
		BoxWifiTesterCyPhy.Y_RELATIVE_POSITION = 4;
		
		BoxWifiCyPhy.VERBOSE = true;
		BoxWifiCyPhy.X_RELATIVE_POSITION = 1;
		BoxWifiCyPhy.Y_RELATIVE_POSITION = 4;

		assert	CVMIntegrationTest.implementationInvariants(this) :
				new InvariantException(
						"CVMIntegrationTest.glassBoxInvariants(this)");
		assert	CVMIntegrationTest.invariants(this) :
				new InvariantException(
						"CVMIntegrationTest.blackBoxInvariants(this)");
	}

	/**
	 * @see fr.sorbonne_u.components.cvm.AbstractCVM#deploy()
	 */
	@Override
	public void			deploy() throws Exception
	{
		TestScenario testScenario;

		if (ExecutionMode.INTEGRATION_TEST.equals(GLOBAL_EXECUTION_MODE)) {

			testScenario = integrationWithoutSimulation();
			// start time in Unix epoch time in nanoseconds.
			long unixEpochStartTimeInMillis = 
								System.currentTimeMillis() + DELAY_TO_START;

			AbstractComponent.createComponent(
				ClocksServer.class.getCanonicalName(),
				new Object[]{
						// URI of the clock to retrieve it
						CLOCK_URI,
						// start time in Unix epoch time
						TimeUnit.MILLISECONDS.toNanos(
										 		unixEpochStartTimeInMillis),
						START_INSTANT,
						ACCELERATION_FACTOR});

			AbstractComponent.createComponent(
				ElectricMeterCyPhy.class.getCanonicalName(),
				new Object[]{
						ExecutionMode.INTEGRATION_TEST,
						CLOCK_URI
				});

			AbstractComponent.createComponent(
				HairDryerCyPhy.class.getCanonicalName(),
				new Object[]{ExecutionMode.INTEGRATION_TEST});
			AbstractComponent.createComponent(
				HairDryerTesterCyPhy.class.getCanonicalName(),
				new Object[]{
						HairDryerCyPhy.INBOUND_PORT_URI,
						ExecutionMode.INTEGRATION_TEST,
						testScenario
				});

			AbstractComponent.createComponent(
				HeaterCyPhy.class.getCanonicalName(),
				new Object[]{
						ExecutionMode.INTEGRATION_TEST,
						testScenario.getClockURI()
				});
			AbstractComponent.createComponent(
				HeaterTesterCyPhy.class.getCanonicalName(),
				new Object[]{
						HeaterCyPhy.USER_INBOUND_PORT_URI,
						HeaterCyPhy.INTERNAL_CONTROL_INBOUND_PORT_URI,
						HeaterCyPhy.EXTERNAL_CONTROL_INBOUND_PORT_URI,
						ExecutionMode.INTEGRATION_TEST,
						testScenario
				});

			// Création du composant Box WiFi
			AbstractComponent.createComponent(
				BoxWifiCyPhy.class.getCanonicalName(),
				new Object[]{ExecutionMode.INTEGRATION_TEST});
			
			AbstractComponent.createComponent(
				BoxWifiTesterCyPhy.class.getCanonicalName(),
				new Object[]{
						BoxWifiCyPhy.INBOUND_PORT_URI,
						ExecutionMode.INTEGRATION_TEST,
						testScenario
				});

			AbstractComponent.createComponent(
				HEMCyPhy.class.getCanonicalName(),
				new Object[]{
						ExecutionMode.INTEGRATION_TEST,
						testScenario
				});

		} else if (ExecutionMode.INTEGRATION_TEST_WITH_SIL_SIMULATION.equals(
													GLOBAL_EXECUTION_MODE)) {

			testScenario = integrationWithSimulation();
			// start time in Unix epoch time in nanoseconds.
			long unixEpochStartTimeInMillis = 
								System.currentTimeMillis() + DELAY_TO_START;

			AbstractComponent.createComponent(
				ClocksServerWithSimulation.class.getCanonicalName(),
				new Object[]{
						// URI of the clock to retrieve it
						CLOCK_URI,
						// start time in Unix epoch time
						TimeUnit.MILLISECONDS.toNanos(
										 		unixEpochStartTimeInMillis),
						START_INSTANT,
						ACCELERATION_FACTOR,
						DELAY_TO_START,
						SIMULATION_START_TIME,
						SIMULATION_DURATION});

			AbstractComponent.createComponent(
				GlobalSupervisor.class.getCanonicalName(),
				new Object[]{
						testScenario,
						GlobalSupervisor.SIL_SIM_ARCHITECTURE_URI
				});
			AbstractComponent.createComponent(
					CoordinatorComponent.class.getCanonicalName(),
					new Object[]{});

			AbstractComponent.createComponent(
				HEMCyPhy.class.getCanonicalName(),
				new Object[]{
						ExecutionMode.INTEGRATION_TEST_WITH_SIL_SIMULATION,
						testScenario
				});
			AbstractComponent.createComponent(
				ElectricMeterCyPhy.class.getCanonicalName(),
				new Object[]{
						ElectricMeterCyPhy.REFLECTION_INBOUND_PORT_URI,
						ElectricMeterCyPhy.ELECTRIC_METER_INBOUND_PORT_URI,
						ExecutionMode.INTEGRATION_TEST_WITH_SIL_SIMULATION,
						testScenario,
						ElectricMeterCyPhy.LOCAL_ARCHITECTURE_URI,
						ACCELERATION_FACTOR
				});

			AbstractComponent.createComponent(
				HairDryerCyPhy.class.getCanonicalName(),
				new Object[]{
						HairDryerCyPhy.REFLECTION_INBOUND_PORT_URI,
						HairDryerCyPhy.INBOUND_PORT_URI,
						ExecutionMode.INTEGRATION_TEST_WITH_SIL_SIMULATION,
						testScenario,
						HairDryerCyPhy.INTEGRATION_TEST_ARCHITECTURE_URI,
						ACCELERATION_FACTOR
				});
			AbstractComponent.createComponent(
				HairDryerTesterCyPhy.class.getCanonicalName(),
				new Object[]{
						HairDryerCyPhy.INBOUND_PORT_URI,
						ExecutionMode.INTEGRATION_TEST,
						testScenario
				});

			AbstractComponent.createComponent(
				HeaterCyPhy.class.getCanonicalName(),
				new Object[]{
						HeaterCyPhy.REFLECTION_INBOUND_PORT_URI,
						HeaterCyPhy.USER_INBOUND_PORT_URI,
						HeaterCyPhy.INTERNAL_CONTROL_INBOUND_PORT_URI,
						HeaterCyPhy.EXTERNAL_CONTROL_INBOUND_PORT_URI,
						HeaterCyPhy.SENSOR_INBOUND_PORT_URI,
						HeaterCyPhy.ACTUATOR_INBOUND_PORT_URI,
						ExecutionMode.INTEGRATION_TEST_WITH_SIL_SIMULATION,
						testScenario,
						HeaterCyPhy.INTEGRATION_TEST_ARCHITECTURE_URI,
						ACCELERATION_FACTOR
				});
			AbstractComponent.createComponent(
				HeaterController.class.getCanonicalName(),
				new Object[]{
						HeaterCyPhy.SENSOR_INBOUND_PORT_URI,
						HeaterCyPhy.ACTUATOR_INBOUND_PORT_URI,
						HeaterController.STANDARD_HYSTERESIS,
						HeaterController.STANDARD_CONTROL_PERIOD,
						ControlMode.PULL,
						ExecutionMode.INTEGRATION_TEST_WITH_SIL_SIMULATION,
						ACCELERATION_FACTOR
				});
			AbstractComponent.createComponent(
				HeaterTesterCyPhy.class.getCanonicalName(),
				new Object[]{
						HeaterCyPhy.USER_INBOUND_PORT_URI,
						HeaterCyPhy.INTERNAL_CONTROL_INBOUND_PORT_URI,
						HeaterCyPhy.EXTERNAL_CONTROL_INBOUND_PORT_URI,
						ExecutionMode.INTEGRATION_TEST,
						testScenario
				});

			// Création du composant Box WiFi avec simulation
			AbstractComponent.createComponent(
				BoxWifiCyPhy.class.getCanonicalName(),
				new Object[]{
						BoxWifiCyPhy.REFLECTION_INBOUND_PORT_URI,
						BoxWifiCyPhy.INBOUND_PORT_URI,
						ExecutionMode.INTEGRATION_TEST_WITH_SIL_SIMULATION,
						testScenario,
						BoxWifiCyPhy.INTEGRATION_TEST_ARCHITECTURE_URI,
						ACCELERATION_FACTOR
				});
			
			AbstractComponent.createComponent(
				BoxWifiTesterCyPhy.class.getCanonicalName(),
				new Object[]{
						BoxWifiCyPhy.INBOUND_PORT_URI,
						ExecutionMode.INTEGRATION_TEST,
						testScenario
				});

		}

		super.deploy();
	}

	// -------------------------------------------------------------------------
	// Executing
	// -------------------------------------------------------------------------

	public static void	main(String[] args)
	{
		VerboseException.VERBOSE = true;
		VerboseException.PRINT_STACK_TRACE = true;
		try {
			CVMIntegrationTest cvm = new CVMIntegrationTest();
			cvm.startStandardLifeCycle(EXECUTION_DURATION);
			Thread.sleep(END_SLEEP_DURATION);
			System.exit(0);
		} catch (Throwable e) {
			e.printStackTrace();
		}
	}

	// -------------------------------------------------------------------------
	// Test scenarios
	// -------------------------------------------------------------------------

	/**
	 * return a test scenario for the integration testing without simulation of
	 * the HEM application.
	 * 
	 * <p><strong>Description</strong></p>
	 * 
	 * <p>
	 * 
	 * </p>
	 * 
	 * <p><strong>Contract</strong></p>
	 * 
	 * <pre>
	 * pre	{@code true}	// no precondition.
	 * post	{@code return != null}
	 * </pre>
	 *
	 * @return				a test scenario for the integration testing of the HEM application.
	 * @throws Exception	<i>to do</i>.
	 */
	public static TestScenario	integrationWithoutSimulation()
	throws Exception
	{
		long d = TimeUnit.NANOSECONDS.toSeconds(
							TimeUtils.toNanos(SIMULATION_DURATION));
		Instant endInstant = START_INSTANT.plusSeconds(d);

		Instant heaterSwitchOn = START_INSTANT.plusSeconds(60);

		Instant hemTestMeter = START_INSTANT.plusSeconds(120);

		Instant hairDryerTurnOn = START_INSTANT.plusSeconds(600);
		Instant hairDryerSetHigh = START_INSTANT.plusSeconds(660);
		Instant hairDryerSetLow = START_INSTANT.plusSeconds(900);
		Instant hairDryerTurnOff = START_INSTANT.plusSeconds(1200);

		Instant hemTestHeater = START_INSTANT.plusSeconds(1500);

		// Instants pour Box WiFi
		Instant boxWifiSwitchOn = START_INSTANT.plusSeconds(1800);
		Instant boxWifiActivateWifi = START_INSTANT.plusSeconds(1860);
		Instant boxWifiDeactivateWifi = START_INSTANT.plusSeconds(1920);
		Instant boxWifiSwitchOff = START_INSTANT.plusSeconds(1980);
		
		Instant hemTestBoxWifi = START_INSTANT.plusSeconds(2100);

		Instant heaterSwitchOff = START_INSTANT.plusSeconds(d - 60);

		return new TestScenario(
			CLOCK_URI,
			START_INSTANT,
			endInstant,
			new TestStepI[] {
				new TestStep(
					CLOCK_URI,
					HeaterTesterCyPhy.REFLECTION_INBOUND_PORT_URI,
					heaterSwitchOn,
					owner ->  {
						try {
							((HeaterTesterCyPhy)owner).getHop().switchOn();
						} catch (Exception e) {
							throw new BCMRuntimeException(e) ;
						}
					}),

				// HEM test the meter
				new TestStep(
					CLOCK_URI,
					HEMCyPhy.REFLECTION_INBOUND_PORT_URI,
					hemTestMeter,
						owner ->  {
							try {
								((HEMCyPhy)owner).testMeter();
							} catch (Exception e) {
								throw new BCMRuntimeException(e) ;
							}
						}),

				// Hair dryer test steps
				new TestStep(
					CLOCK_URI,
					HairDryerTesterCyPhy.REFLECTION_INBOUND_PORT_URI,
					hairDryerTurnOn,
					owner ->  {
						try {
							((HairDryerTesterCyPhy)owner).turnOnHairDryer();
						} catch (Exception e) {
							throw new BCMRuntimeException(e) ;
						}
					}),
				new TestStep(
					CLOCK_URI,
					HairDryerTesterCyPhy.REFLECTION_INBOUND_PORT_URI,
					hairDryerSetHigh,
					owner ->  {
						try {
							((HairDryerTesterCyPhy)owner).setHighHairDryer();
						} catch (Exception e) {
							throw new BCMRuntimeException(e) ;
						}
					}),
				new TestStep(
					CLOCK_URI,
					HairDryerTesterCyPhy.REFLECTION_INBOUND_PORT_URI,
					hairDryerSetLow,
					owner ->  {
						try {
							((HairDryerTesterCyPhy)owner).setLowHairDryer();
						} catch (Exception e) {
							throw new BCMRuntimeException(e) ;
						}
					}),
				new TestStep(
					CLOCK_URI,
					HairDryerTesterCyPhy.REFLECTION_INBOUND_PORT_URI,
					hairDryerTurnOff,
					owner ->  {
						try {
							((HairDryerTesterCyPhy)owner).turnOffHairDryer();
						} catch (Exception e) {
							throw new BCMRuntimeException(e) ;
						}
					}),

				// HEM test the heater
				new TestStep(
					CLOCK_URI,
					HEMCyPhy.REFLECTION_INBOUND_PORT_URI,
					hemTestHeater,
					owner ->  {
						try {
							((HEMCyPhy)owner).testHeater();
						} catch (Exception e) {
							throw new BCMRuntimeException(e) ;
						}
					}),

				// Box WiFi test steps
				new TestStep(
					CLOCK_URI,
					BoxWifiTesterCyPhy.REFLECTION_INBOUND_PORT_URI,
					boxWifiSwitchOn,
					owner ->  {
						try {
							((BoxWifiTesterCyPhy)owner).turnOnBoxWifi();
						} catch (Exception e) {
							throw new BCMRuntimeException(e) ;
						}
					}),
				new TestStep(
					CLOCK_URI,
					BoxWifiTesterCyPhy.REFLECTION_INBOUND_PORT_URI,
					boxWifiActivateWifi,
					owner ->  {
						try {
							((BoxWifiTesterCyPhy)owner).activateWifiBoxWifi();
						} catch (Exception e) {
							throw new BCMRuntimeException(e) ;
						}
					}),
				new TestStep(
					CLOCK_URI,
					BoxWifiTesterCyPhy.REFLECTION_INBOUND_PORT_URI,
					boxWifiDeactivateWifi,
					owner ->  {
						try {
							((BoxWifiTesterCyPhy)owner).deactivateWifiBoxWifi();
						} catch (Exception e) {
							throw new BCMRuntimeException(e) ;
						}
					}),
				new TestStep(
					CLOCK_URI,
					BoxWifiTesterCyPhy.REFLECTION_INBOUND_PORT_URI,
					boxWifiSwitchOff,
					owner ->  {
						try {
							((BoxWifiTesterCyPhy)owner).turnOffBoxWifi();
						} catch (Exception e) {
							throw new BCMRuntimeException(e) ;
						}
					}),

				

				new TestStep(
					CLOCK_URI,
					HeaterTesterCyPhy.REFLECTION_INBOUND_PORT_URI,
					heaterSwitchOff,
					owner ->  {
						try {
							((HeaterTesterCyPhy)owner).getHop().switchOff();
						} catch (Exception e) {
							throw new BCMRuntimeException(e) ;
						}
					})
			});
	}

	/**
	 * return a test scenario for the integration testing with simulation of the
	 * HEM application.
	 * 
	 * <p><strong>Description</strong></p>
	 * 
	 * <p>
	 * 
	 * </p>
	 * 
	 * <p><strong>Contract</strong></p>
	 * 
	 * <pre>
	 * pre	{@code true}	// no precondition.
	 * post	{@code return != null}
	 * </pre>
	 *
	 * @return				a test scenario for the integration testing with simulation of the HEM application.
	 * @throws Exception	<i>to do</i>.
	 */
	public static TestScenarioWithSimulation	integrationWithSimulation()
	throws Exception
	{
		// START_INSTANT = "2025-12-02T06:00:00.00Z"
		long d = TimeUnit.NANOSECONDS.toSeconds(
									TimeUtils.toNanos(SIMULATION_DURATION));
		Instant endInstant = START_INSTANT.plusSeconds(d);

		Instant heaterSwitchOn = START_INSTANT.plusSeconds(60);
		Instant heaterSwitchOff = START_INSTANT.plusSeconds(d - 60);

		Instant hairDryerTurnOn1 = Instant.parse("2025-12-02T07:15:00.00Z");
		Instant hairDryerSetHigh1 = Instant.parse("2025-12-02T07:15:20.00Z");
		Instant hairDryerSetLow1 = Instant.parse("2025-12-02T07:20:00.00Z");
		Instant hairDryerTurnOff1 = Instant.parse("2025-12-02T07:25:00.00Z");
		Instant hairDryerTurnOn2 = Instant.parse("2025-12-02T08:15:00.00Z");
		Instant hairDryerSetHigh2 = Instant.parse("2025-12-02T08:15:20.00Z");
		Instant hairDryerSetLow2 = Instant.parse("2025-12-02T08:20:00.00Z");
		Instant hairDryerTurnOff2 = Instant.parse("2025-12-02T08:25:00.00Z");

		// Instants pour Box WiFi
		Instant boxWifiSwitchOn = Instant.parse("2025-12-02T09:00:00.00Z");
		Instant boxWifiActivateWifi = Instant.parse("2025-12-02T09:30:00.00Z");
		Instant boxWifiDeactivateWifi = Instant.parse("2025-12-02T10:00:00.00Z");
		Instant boxWifiSwitchOff = Instant.parse("2025-12-02T10:30:00.00Z");

		return new TestScenarioWithSimulation(
			CLOCK_URI,
			START_INSTANT,
			endInstant,
			GlobalSupervisor.SIL_SIM_ARCHITECTURE_URI,
			new Time(0.0, TimeUnit.HOURS),
			(ts, simParams) -> { },
			new TestStepI[] {
				new TestStep(
					CLOCK_URI,
					HeaterTesterCyPhy.REFLECTION_INBOUND_PORT_URI,
					heaterSwitchOn,
					owner ->  {
						try {
							((HeaterTesterCyPhy)owner).getHop().switchOn();
						} catch (Exception e) {
							throw new BCMRuntimeException(e) ;
						}
					}),

				new TestStep(
					CLOCK_URI,
					HairDryerTesterCyPhy.REFLECTION_INBOUND_PORT_URI,
					hairDryerTurnOn1,
					owner ->  {
						try {
							((HairDryerTesterCyPhy)owner).turnOnHairDryer();
						} catch (Exception e) {
							throw new BCMRuntimeException(e) ;
						}
					}),
				new TestStep(
					CLOCK_URI,
					HairDryerTesterCyPhy.REFLECTION_INBOUND_PORT_URI,
					hairDryerSetHigh1,
					owner ->  {
						try {
							((HairDryerTesterCyPhy)owner).setHighHairDryer();
						} catch (Exception e) {
							throw new BCMRuntimeException(e) ;
						}
					}),
				new TestStep(
					CLOCK_URI,
					HairDryerTesterCyPhy.REFLECTION_INBOUND_PORT_URI,
					hairDryerSetLow1,
					owner ->  {
						try {
							((HairDryerTesterCyPhy)owner).setLowHairDryer();
						} catch (Exception e) {
							throw new BCMRuntimeException(e) ;
						}
					}),
				new TestStep(
					CLOCK_URI,
					HairDryerTesterCyPhy.REFLECTION_INBOUND_PORT_URI,
					hairDryerTurnOff1,
					owner ->  {
						try {
							((HairDryerTesterCyPhy)owner).turnOffHairDryer();
						} catch (Exception e) {
							throw new BCMRuntimeException(e) ;
						}
					}),
				new TestStep(
					CLOCK_URI,
					HairDryerTesterCyPhy.REFLECTION_INBOUND_PORT_URI,
					hairDryerTurnOn2,
					owner ->  {
						try {
							((HairDryerTesterCyPhy)owner).turnOnHairDryer();
						} catch (Exception e) {
							throw new BCMRuntimeException(e) ;
						}
					}),
				new TestStep(
					CLOCK_URI,
					HairDryerTesterCyPhy.REFLECTION_INBOUND_PORT_URI,
					hairDryerSetHigh2,
					owner ->  {
						try {
							((HairDryerTesterCyPhy)owner).setHighHairDryer();
						} catch (Exception e) {
							throw new BCMRuntimeException(e) ;
						}
					}),
				new TestStep(
					CLOCK_URI,
					HairDryerTesterCyPhy.REFLECTION_INBOUND_PORT_URI,
					hairDryerSetLow2,
					owner ->  {
						try {
							((HairDryerTesterCyPhy)owner).setLowHairDryer();
						} catch (Exception e) {
							throw new BCMRuntimeException(e) ;
						}
					}),
				new TestStep(
					CLOCK_URI,
					HairDryerTesterCyPhy.REFLECTION_INBOUND_PORT_URI,
					hairDryerTurnOff2,
					owner ->  {
						try {
							((HairDryerTesterCyPhy)owner).turnOffHairDryer();
						} catch (Exception e) {
							throw new BCMRuntimeException(e) ;
						}
					}),

				// Box WiFi test steps
				new TestStep(
					CLOCK_URI,
					BoxWifiTesterCyPhy.REFLECTION_INBOUND_PORT_URI,
					boxWifiSwitchOn,
					owner ->  {
						try {
							((BoxWifiTesterCyPhy)owner).turnOnBoxWifi();
						} catch (Exception e) {
							throw new BCMRuntimeException(e) ;
						}
					}),
				new TestStep(
					CLOCK_URI,
					BoxWifiTesterCyPhy.REFLECTION_INBOUND_PORT_URI,
					boxWifiActivateWifi,
					owner ->  {
						try {
							((BoxWifiTesterCyPhy)owner).activateWifiBoxWifi();
						} catch (Exception e) {
							throw new BCMRuntimeException(e) ;
						}
					}),
				new TestStep(
					CLOCK_URI,
					BoxWifiTesterCyPhy.REFLECTION_INBOUND_PORT_URI,
					boxWifiDeactivateWifi,
					owner ->  {
						try {
							((BoxWifiTesterCyPhy)owner).deactivateWifiBoxWifi();
						} catch (Exception e) {
							throw new BCMRuntimeException(e) ;
						}
					}),
				new TestStep(
					CLOCK_URI,
					BoxWifiTesterCyPhy.REFLECTION_INBOUND_PORT_URI,
					boxWifiSwitchOff,
					owner ->  {
						try {
							((BoxWifiTesterCyPhy)owner).turnOffBoxWifi();
						} catch (Exception e) {
							throw new BCMRuntimeException(e) ;
						}
					}),

				new TestStep(
					CLOCK_URI,
					HeaterTesterCyPhy.REFLECTION_INBOUND_PORT_URI,
					heaterSwitchOff,
					owner ->  {
						try {
							((HeaterTesterCyPhy)owner).getHop().switchOff();
						} catch (Exception e) {
							throw new BCMRuntimeException(e) ;
						}
					})
			});
	}
}
// -----------------------------------------------------------------------------
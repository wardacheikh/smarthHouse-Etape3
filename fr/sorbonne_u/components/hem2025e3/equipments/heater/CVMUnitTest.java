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

import fr.sorbonne_u.components.cvm.AbstractCVM;
import fr.sorbonne_u.components.cyphy.ExecutionMode;
import fr.sorbonne_u.components.cyphy.utils.aclocks.ClocksServerWithSimulation;
import fr.sorbonne_u.components.cyphy.utils.tests.TestScenarioWithSimulation;
import fr.sorbonne_u.components.exceptions.BCMRuntimeException;
import fr.sorbonne_u.components.hem2025e1.equipments.heater.HeaterExternalControlI;
import fr.sorbonne_u.components.hem2025e1.equipments.heater.HeaterTemperatureI;
import fr.sorbonne_u.components.hem2025e3.equipments.heater.HeaterController.ControlMode;
import fr.sorbonne_u.components.utils.tests.TestScenario;
import fr.sorbonne_u.components.utils.tests.TestStep;
import fr.sorbonne_u.components.utils.tests.TestStepI;
import fr.sorbonne_u.devs_simulation.models.time.Duration;
import fr.sorbonne_u.devs_simulation.models.time.Time;
import fr.sorbonne_u.devs_simulation.models.time.TimeUtils;
import fr.sorbonne_u.exceptions.VerboseException;
import fr.sorbonne_u.utils.aclocks.ClocksServer;
import java.time.Instant;
import java.util.concurrent.TimeUnit;
import fr.sorbonne_u.alasca.physical_data.Measure;
import fr.sorbonne_u.components.AbstractComponent;

// -----------------------------------------------------------------------------
/**
 * The class <code>CVMUnitTest</code> performs unit tests for the thermostated
 * heater component.
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
 * invariant	{@code true}	// no more invariant
 * </pre>
 * 
 * <p>Created on : 2021-09-13</p>
 * 
 * @author	<a href="mailto:Jacques.Malenfant@lip6.fr">Jacques Malenfant</a>
 */
public class			CVMUnitTest
extends		AbstractCVM
{
	// -------------------------------------------------------------------------
	// Constants and variables
	// -------------------------------------------------------------------------

	/** delay before starting the test scenarios, leaving time to build
	 *  and initialise the components and their simulators; this delay is
	 *  estimated given the complexity of the initialisation (including the
	 *  creation of the application simulator if simulation is used). It
	 *  could need to be revised if the computer on which the application
	 *  is run is less powerful.											*/
	public static long				DELAY_TO_START = 3000L;
	/** duration of the sleep at the end of the execution before exiting
	 *  the JVM.															*/
	public static long				END_SLEEP_DURATION = 1000000L;

	/** time unit in which {@code SIMULATION_DURATION} is expressed.		*/
	public static TimeUnit			SIMULATION_TIME_UNIT = TimeUnit.HOURS;
	/** start time of the simulation, in simulated logical time, if
	 *  relevant.															*/
	public static Time 				SIMULATION_START_TIME =
										new Time(0.0, SIMULATION_TIME_UNIT);
	/** duration  of the simulation, in simulated time.						*/
	public static Duration			SIMULATION_DURATION =
										new Duration(6.0, SIMULATION_TIME_UNIT);
	/** for real time simulations, the acceleration factor applied to the
	 *  the simulated time to get the execution time of the simulations. 	*/
	public static double			ACCELERATION_FACTOR = 1200.0;
	/** duration of the execution.											*/
	public static long				EXECUTION_DURATION =
			DELAY_TO_START +
				TimeUnit.NANOSECONDS.toMillis(
						TimeUtils.toNanos(
								SIMULATION_DURATION.getSimulatedDuration()/
													ACCELERATION_FACTOR,
								SIMULATION_DURATION.getTimeUnit()));

	/** the execution mode for the hair dryer component, to select among
	 *  the values of the enumeration {@code ExecutionMode}.				*/
	public static ExecutionMode		HEATER_EXECUTION_MODE =
//											ExecutionMode.STANDARD;
//											ExecutionMode.UNIT_TEST;
											ExecutionMode.
												UNIT_TEST_WITH_SIL_SIMULATION;

	/** the execution mode for the hair dryer tester component, to select
	 *  among the values of the enumeration {@code ExecutionMode}.			*/
	public static ExecutionMode		HEATER_TESTER_EXECUTION_MODE =
//											ExecutionMode.STANDARD;
											ExecutionMode.UNIT_TEST;

	/** for unit tests and SIL simulation unit tests, a {@code Clock} is
	 *  used to get a time-triggered synchronisation of the actions of
	 *  the components in the test scenarios.								*/
	public static String			CLOCK_URI = "heater-test-clock";
	/** start instant in test scenarios, as a string to be parsed.			*/
	public static String			START_INSTANT = "2025-11-22T08:00:00.00Z";


	// -------------------------------------------------------------------------
	// Constructors
	// -------------------------------------------------------------------------

	public				CVMUnitTest() throws Exception
	{
		HeaterTesterCyPhy.VERBOSE = true;
		HeaterTesterCyPhy.X_RELATIVE_POSITION = 0;
		HeaterTesterCyPhy.Y_RELATIVE_POSITION = 1;
		HeaterCyPhy.VERBOSE = true;
		HeaterCyPhy.X_RELATIVE_POSITION = 1;
		HeaterCyPhy.Y_RELATIVE_POSITION = 1;
		HeaterController.VERBOSE = true;
		HeaterController.X_RELATIVE_POSITION = 2;
		HeaterController.Y_RELATIVE_POSITION = 1;
	}

	// -------------------------------------------------------------------------
	// CVM life-cycle
	// -------------------------------------------------------------------------

	/**
	 * @see fr.sorbonne_u.components.cvm.AbstractCVM#deploy()
	 */
	@Override
	public void			deploy() throws Exception
	{
		if (HEATER_EXECUTION_MODE.isStandard()) {

			// Nota: the heater controller cannot run in standard mode as the
			// heater does not have the required physical sensors

			AbstractComponent.createComponent(
					HeaterCyPhy.class.getCanonicalName(),
					new Object[]{});

			AbstractComponent.createComponent(
					HeaterTesterCyPhy.class.getCanonicalName(),
					new Object[]{
						HeaterCyPhy.USER_INBOUND_PORT_URI,
						HeaterCyPhy.INTERNAL_CONTROL_INBOUND_PORT_URI,
						HeaterCyPhy.EXTERNAL_CONTROL_INBOUND_PORT_URI
						});

		} else if (HEATER_EXECUTION_MODE.isTestWithoutSimulation()) {

			// Nota: the heater controller cannot run in test without simulation
			// mode as the heater does not have the required physical sensors

			long current = System.currentTimeMillis();
			// start time of the components in Unix epoch time in milliseconds.
			long unixEpochStartTimeInMillis = current + DELAY_TO_START;
			// start instant used for time-triggered synchronisation in unit tests
			// and SIL simulation runs.
			Instant	startInstant = Instant.parse(START_INSTANT);
			// test scenario to be executed for unit tests with simulation
			TestScenario testScenario = unitTestScenario();

			AbstractComponent.createComponent(
					HeaterCyPhy.class.getCanonicalName(),
					new Object[]{
						HEATER_EXECUTION_MODE,
						testScenario.getClockURI()
						});

			AbstractComponent.createComponent(
					HeaterTesterCyPhy.class.getCanonicalName(),
					new Object[]{
						HeaterCyPhy.USER_INBOUND_PORT_URI,
						HeaterCyPhy.INTERNAL_CONTROL_INBOUND_PORT_URI,
						HeaterCyPhy.EXTERNAL_CONTROL_INBOUND_PORT_URI,
						HEATER_TESTER_EXECUTION_MODE,
						testScenario
						});

			AbstractComponent.createComponent(
					ClocksServer.class.getCanonicalName(),
					new Object[]{
							// URI of the clock to retrieve it
							CLOCK_URI,
							// start time in Unix epoch time
							TimeUnit.MILLISECONDS.toNanos(
										 		unixEpochStartTimeInMillis),
							// start instant synchronised with the start time
							startInstant,
							ACCELERATION_FACTOR
					});

		} else {
			assert	HEATER_EXECUTION_MODE.isSimulationTest();

			long current = System.currentTimeMillis();
			// start time of the components in Unix epoch time in milliseconds.
			long unixEpochStartTimeInMillis = current + DELAY_TO_START;
			// start instant used for time-triggered synchronisation in unit
			// tests and SIL simulation runs.
			Instant	startInstant = Instant.parse(START_INSTANT);
			// test scenario to be executed for unit tests with simulation
			TestScenario testScenario = unitTestScenarioWithSimulation();

			AbstractComponent.createComponent(
					HeaterCyPhy.class.getCanonicalName(),
					new Object[]{
						HeaterCyPhy.REFLECTION_INBOUND_PORT_URI,
						HeaterCyPhy.USER_INBOUND_PORT_URI,
						HeaterCyPhy.INTERNAL_CONTROL_INBOUND_PORT_URI,
						HeaterCyPhy.EXTERNAL_CONTROL_INBOUND_PORT_URI,
						HeaterCyPhy.SENSOR_INBOUND_PORT_URI,
						HeaterCyPhy.ACTUATOR_INBOUND_PORT_URI,
						HEATER_EXECUTION_MODE,
						testScenario,
						HeaterCyPhy.UNIT_TEST_ARCHITECTURE_URI,
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
						HEATER_EXECUTION_MODE,
						ACCELERATION_FACTOR
						});

			AbstractComponent.createComponent(
					HeaterTesterCyPhy.class.getCanonicalName(),
					new Object[]{
						HeaterCyPhy.USER_INBOUND_PORT_URI,
						HeaterCyPhy.INTERNAL_CONTROL_INBOUND_PORT_URI,
						HeaterCyPhy.EXTERNAL_CONTROL_INBOUND_PORT_URI,
						HEATER_TESTER_EXECUTION_MODE,
						testScenario
						});

			AbstractComponent.createComponent(
					ClocksServerWithSimulation.class.getCanonicalName(),
					new Object[]{
							// URI of the clock to retrieve it
							CLOCK_URI,
							// start time in Unix epoch time
							TimeUnit.MILLISECONDS.toNanos(
											 		unixEpochStartTimeInMillis),
							// start instant synchronised with the start time
							startInstant,
							ACCELERATION_FACTOR,
							DELAY_TO_START,
							SIMULATION_START_TIME,
							SIMULATION_DURATION});
		}

		super.deploy();
	}

	public static void	main(String[] args)
	{
		try {
			VerboseException.VERBOSE = true;
			VerboseException.PRINT_STACK_TRACE = true;

			CVMUnitTest cvm = new CVMUnitTest();
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
	 * return a test scenario without simulation for testing the heater
	 * component.
	 * 
	 * <p><strong>Description</strong></p>
	 * 
	 * <p>
	 * The test includes four steps to be executed by the hair dryer unit tester
	 * component: switching on the hair dryer, setting it in high mode, setting
	 * it in low mode and then switching it off.
	 * </p>
	 * 
	 * <p><strong>Contract</strong></p>
	 * 
	 * <pre>
	 * pre	{@code true}	// no precondition.
	 * post	{@code return != null}
	 * </pre>
	 *
	 * @return	a test scenario for the unit testing of the hair dryer component.
	 * @throws VerboseException	<i>to do</i>.
	 */
	public static TestScenario	unitTestScenario() throws VerboseException
	{
		Instant startInstant = Instant.parse(START_INSTANT);
		long d = TimeUnit.NANOSECONDS.toSeconds(
							TimeUtils.toNanos(SIMULATION_DURATION));
		Instant endInstant = startInstant.plusSeconds(d);

		Instant switchOnInstant = startInstant.plusSeconds(60);
		Instant startHeatingInstant1 = startInstant.plusSeconds(6900);
		Instant setTargetTemperatureInstant = startInstant.plusSeconds(7200);
		Instant stopHeatingInstant1 = startInstant.plusSeconds(7500);
		Instant startHeatingInstant2 = startInstant.plusSeconds(10500);
		Instant setCurrentPowerLevelInstant = startInstant.plusSeconds(10800);
		Instant stopHeatingInstant2 = startInstant.plusSeconds(11100);
		Instant switchOffInstant = startInstant.plusSeconds(d - 60);

		return new TestScenario(
			CLOCK_URI,
			startInstant,
			endInstant,
			new TestStepI[] {
				new TestStep(
					CLOCK_URI,
					HeaterTesterCyPhy.REFLECTION_INBOUND_PORT_URI,
					switchOnInstant,
					owner ->  {
						try {
							((HeaterTesterCyPhy)owner).getHop().switchOn();
						} catch (Exception e) {
							throw new BCMRuntimeException(e) ;
						}
					}),
				new TestStep(
					CLOCK_URI,
					HeaterTesterCyPhy.REFLECTION_INBOUND_PORT_URI,
					startHeatingInstant1,
					owner ->  {
						try {
							((HeaterTesterCyPhy)owner).getHicop().
																startHeating();
						} catch (Exception e) {
							throw new BCMRuntimeException(e) ;
						}
					}),
				new TestStep(
					CLOCK_URI,
					HeaterTesterCyPhy.REFLECTION_INBOUND_PORT_URI,
					setTargetTemperatureInstant,
					owner ->  {
						try {
							((HeaterTesterCyPhy)owner).getHop().
								setTargetTemperature(
									new Measure<Double>(
											21.0,
											HeaterTemperatureI.TEMPERATURE_UNIT));
						} catch (Exception e) {
							throw new BCMRuntimeException(e) ;
						}
					}),
				new TestStep(
					CLOCK_URI,
					HeaterTesterCyPhy.REFLECTION_INBOUND_PORT_URI,
					stopHeatingInstant1,
					owner ->  {
						try {
							((HeaterTesterCyPhy)owner).getHicop().
																stopHeating();
						} catch (Exception e) {
							throw new BCMRuntimeException(e) ;
						}
					}),
				new TestStep(
					CLOCK_URI,
					HeaterTesterCyPhy.REFLECTION_INBOUND_PORT_URI,
					startHeatingInstant2,
					owner ->  {
						try {
							((HeaterTesterCyPhy)owner).getHicop().
																startHeating();
						} catch (Exception e) {
							throw new BCMRuntimeException(e) ;
						}
					}),
				new TestStep(
					CLOCK_URI,
					HeaterTesterCyPhy.REFLECTION_INBOUND_PORT_URI,
					setCurrentPowerLevelInstant,
					owner ->  {
						try {
							((HeaterTesterCyPhy)owner).getHop().
								setCurrentPowerLevel(
									new Measure<>(
											1100.0,
											HeaterExternalControlI.POWER_UNIT));
						} catch (Exception e) {
							throw new BCMRuntimeException(e) ;
						}
					}),
				new TestStep(
					CLOCK_URI,
					HeaterTesterCyPhy.REFLECTION_INBOUND_PORT_URI,
					stopHeatingInstant2,
					owner ->  {
						try {
							((HeaterTesterCyPhy)owner).getHicop().
																stopHeating();
						} catch (Exception e) {
							throw new BCMRuntimeException(e) ;
						}
					}),
				new TestStep(
					CLOCK_URI,
					HeaterTesterCyPhy.REFLECTION_INBOUND_PORT_URI,
					switchOffInstant,
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
	 * return a test scenario for testing with SIL simulation the hair dryer
	 * component.
	 * 
	 * <p><strong>Description</strong></p>
	 * 
	 * <p>
	 * The test includes four steps to be executed by the hair dryer unit tester
	 * component: switching on the hair dryer, setting it in high mode, setting
	 * it in low mode and then switching it off.
	 * </p>
	 * 
	 * <p><strong>Contract</strong></p>
	 * 
	 * <pre>
	 * pre	{@code true}	// no precondition.
	 * post	{@code return != null}
	 * </pre>
	 *
	 * @return	a test scenario for the unit testing of the hair dryer component.
	 * @throws VerboseException	<i>to do</i>.
	 */
	public static TestScenarioWithSimulation	unitTestScenarioWithSimulation()
	throws VerboseException
	{
		Instant startInstant = Instant.parse(START_INSTANT);
		long d = TimeUnit.NANOSECONDS.toSeconds(
							TimeUtils.toNanos(SIMULATION_DURATION));
		Instant endInstant = startInstant.plusSeconds(d);

		Instant switchOnInstant = startInstant.plusSeconds(60);
		Instant switchOffInstant = startInstant.plusSeconds(d - 60);

		return new TestScenarioWithSimulation(
			CLOCK_URI,
			startInstant,
			endInstant,
			"global-archi", // no global archi in fact
			SIMULATION_START_TIME,
			(ts, simParams) -> { },
			new TestStepI[] {
				new TestStep(
					CLOCK_URI,
					HeaterTesterCyPhy.REFLECTION_INBOUND_PORT_URI,
					switchOnInstant,
					owner ->  {
						try {
							((HeaterTesterCyPhy)owner).getHop().switchOn();
						} catch (Exception e) {
							throw new BCMRuntimeException(e) ;
						}
					}),
				new TestStep(
					CLOCK_URI,
					HeaterTesterCyPhy.REFLECTION_INBOUND_PORT_URI,
					switchOffInstant,
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

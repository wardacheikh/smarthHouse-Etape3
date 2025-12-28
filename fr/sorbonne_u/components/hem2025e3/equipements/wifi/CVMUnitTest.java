package fr.sorbonne_u.components.hem2025e3.equipements.wifi;


import fr.sorbonne_u.components.AbstractComponent;
import fr.sorbonne_u.components.cvm.AbstractCVM;
import fr.sorbonne_u.components.cyphy.ExecutionMode;
import fr.sorbonne_u.components.cyphy.utils.aclocks.ClocksServerWithSimulation;
import fr.sorbonne_u.components.cyphy.utils.tests.TestScenarioWithSimulation;
import fr.sorbonne_u.components.exceptions.BCMException;
import fr.sorbonne_u.components.exceptions.BCMRuntimeException;
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

/**
 * The class <code>CVMUnitTest</code> performs unit tests on the box wifi
 * component.
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
 * <p>Created on : 2025-01-15</p>
 * 
 * @author	<a href="mailto:Jacques.Malenfant@lip6.fr">Jacques Malenfant</a>
 */
public class CVMUnitTest extends AbstractCVM {
    
    // -------------------------------------------------------------------------
    // Constants and variables
    // -------------------------------------------------------------------------

    /** delay before starting the test scenarios, leaving time to build
     *  and initialise the components and their simulators. */
    public static long DELAY_TO_START = 3000L;
    
    /** duration of the sleep at the end of the execution before exiting
     *  the JVM. */
    public static long END_SLEEP_DURATION = 10000L;

    /** time unit in which {@code SIMULATION_DURATION} is expressed. */
    public static TimeUnit SIMULATION_TIME_UNIT = TimeUnit.HOURS;
    
    /** start time of the simulation, in simulated logical time, if
     *  relevant. */
    public static Time SIMULATION_START_TIME = new Time(0.0, SIMULATION_TIME_UNIT);
    
    /** duration of the simulation, in simulated time. */
    public static Duration SIMULATION_DURATION = new Duration(1.0, SIMULATION_TIME_UNIT);
    
    /** for real time simulations, the acceleration factor applied to the
     *  the simulated time to get the execution time of the simulations. */
    public static double ACCELERATION_FACTOR = 360.0;
    
    /** duration of the execution. */
    public static long EXECUTION_DURATION = DELAY_TO_START +
            TimeUnit.NANOSECONDS.toMillis(
                    TimeUtils.toNanos(
                            SIMULATION_DURATION.getSimulatedDuration() / ACCELERATION_FACTOR,
                            SIMULATION_DURATION.getTimeUnit()));

    /** the execution mode for the box wifi component. */
    public static ExecutionMode BOX_WIFI_EXECUTION_MODE =
//            ExecutionMode.STANDARD;
//            ExecutionMode.UNIT_TEST;
            ExecutionMode.UNIT_TEST_WITH_SIL_SIMULATION;

    /** the execution mode for the box wifi tester component. */
    public static ExecutionMode BOX_WIFI_TESTER_EXECUTION_MODE =
//            ExecutionMode.STANDARD;
            ExecutionMode.UNIT_TEST;

    /** for unit tests and SIL simulation unit tests, a {@code Clock} is
     *  used to get a time-triggered synchronisation. */
    public static String CLOCK_URI = "box-wifi-test-clock";
    
    /** start instant in test scenarios, as a string to be parsed. */
    public static String START_INSTANT = "2025-01-15T08:00:00.00Z";

    // -------------------------------------------------------------------------
    // Constructors
    // -------------------------------------------------------------------------

    public CVMUnitTest() throws Exception {
        BoxWifiTesterCyPhy.VERBOSE = true;
        BoxWifiTesterCyPhy.X_RELATIVE_POSITION = 0;
        BoxWifiTesterCyPhy.Y_RELATIVE_POSITION = 1;
        BoxWifiCyPhy.VERBOSE = true;
        BoxWifiCyPhy.X_RELATIVE_POSITION = 1;
        BoxWifiCyPhy.Y_RELATIVE_POSITION = 1;
    }

    // -------------------------------------------------------------------------
    // CVM life-cycle
    // -------------------------------------------------------------------------

    @Override
    public void deploy() throws Exception {
        if (BOX_WIFI_EXECUTION_MODE.isStandard()) {
            // Standard execution without tests or simulation
            AbstractComponent.createComponent(
                    BoxWifiCyPhy.class.getCanonicalName(),
                    new Object[]{
                        BoxWifiCyPhy.REFLECTION_INBOUND_PORT_URI,
                        BoxWifiCyPhy.INBOUND_PORT_URI
                    });

            AbstractComponent.createComponent(
                    BoxWifiTesterCyPhy.class.getCanonicalName(),
                    new Object[]{
                        BoxWifiCyPhy.INBOUND_PORT_URI
                    });

        } else if (BOX_WIFI_EXECUTION_MODE.isTestWithoutSimulation()) {
            // Unit tests without simulation
            long current = System.currentTimeMillis();
            long unixEpochStartTimeInMillis = current + DELAY_TO_START;
            Instant startInstant = Instant.parse(START_INSTANT);
            TestScenario testScenario = unitTestScenario();

            AbstractComponent.createComponent(
                    BoxWifiCyPhy.class.getCanonicalName(),
                    new Object[]{
                        BoxWifiCyPhy.REFLECTION_INBOUND_PORT_URI,
                        BoxWifiCyPhy.INBOUND_PORT_URI,
                        BOX_WIFI_EXECUTION_MODE
                    });

            AbstractComponent.createComponent(
                    BoxWifiTesterCyPhy.class.getCanonicalName(),
                    new Object[]{
                        BoxWifiCyPhy.INBOUND_PORT_URI,
                        BOX_WIFI_TESTER_EXECUTION_MODE,
                        testScenario
                    });

            AbstractComponent.createComponent(
                    ClocksServer.class.getCanonicalName(),
                    new Object[]{
                        CLOCK_URI,
                        TimeUnit.MILLISECONDS.toNanos(unixEpochStartTimeInMillis),
                        startInstant,
                        ACCELERATION_FACTOR
                    });

        } else if (BOX_WIFI_EXECUTION_MODE.isSILTest()) {
            // Unit tests with SIL simulation
            long current = System.currentTimeMillis();
            long unixEpochStartTimeInMillis = current + DELAY_TO_START;
            Instant startInstant = Instant.parse(START_INSTANT);
            TestScenario testScenario = unitTestScenarioWithSimulation();

            AbstractComponent.createComponent(
                    BoxWifiCyPhy.class.getCanonicalName(),
                    new Object[]{
                        BoxWifiCyPhy.REFLECTION_INBOUND_PORT_URI,
                        BoxWifiCyPhy.INBOUND_PORT_URI,
                        BOX_WIFI_EXECUTION_MODE,
                        testScenario,
                        BoxWifiCyPhy.UNIT_TEST_ARCHITECTURE_URI,
                        ACCELERATION_FACTOR
                    });

            AbstractComponent.createComponent(
                    BoxWifiTesterCyPhy.class.getCanonicalName(),
                    new Object[]{
                        BoxWifiCyPhy.INBOUND_PORT_URI,
                        BOX_WIFI_TESTER_EXECUTION_MODE,
                        testScenario
                    });

            AbstractComponent.createComponent(
                    ClocksServerWithSimulation.class.getCanonicalName(),
                    new Object[]{
                        CLOCK_URI,
                        TimeUnit.MILLISECONDS.toNanos(unixEpochStartTimeInMillis),
                        startInstant,
                        ACCELERATION_FACTOR,
                        DELAY_TO_START,
                        SIMULATION_START_TIME,
                        SIMULATION_DURATION
                    });
        }

        super.deploy();
    }

    // -------------------------------------------------------------------------
    // Executing
    // -------------------------------------------------------------------------

    public static void main(String[] args) {
        BCMException.VERBOSE = true;
        try {
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
     * return a test scenario without simulation for testing the box wifi
     * component.
     * 
     * <p><strong>Description</strong></p>
     * 
     * <p>
     * The test includes four steps to be executed by the box wifi unit tester
     * component: turning on the box wifi, activating wifi, deactivating wifi,
     * and then turning it off.
     * </p>
     * 
     * <p><strong>Contract</strong></p>
     * 
     * <pre>
     * pre	{@code true}	// no precondition.
     * post	{@code return != null}
     * </pre>
     *
     * @return a test scenario for the unit testing of the box wifi component.
     * @throws VerboseException <i>to do</i>.
     */
    public static TestScenario unitTestScenario() throws VerboseException {
        Instant startInstant = Instant.parse(START_INSTANT);
        long d = TimeUnit.NANOSECONDS.toSeconds(
                        TimeUtils.toNanos(SIMULATION_DURATION));
        Instant endInstant = startInstant.plusSeconds(d);

        // Define test steps timing (in seconds from start)
        Instant turnOnInstant = startInstant.plusSeconds(300);      // 5 minutes
        Instant activateWifiInstant = startInstant.plusSeconds(600); // 10 minutes
        Instant deactivateWifiInstant = startInstant.plusSeconds(900); // 15 minutes
        Instant turnOffInstant = startInstant.plusSeconds(1200);    // 20 minutes

        return new TestScenario(
            CLOCK_URI,
            startInstant,
            endInstant,
            new TestStepI[] {
                new TestStep(
                    CLOCK_URI,
                    BoxWifiTesterCyPhy.REFLECTION_INBOUND_PORT_URI,
                    turnOnInstant,
                    owner -> {
                        try {
                            ((BoxWifiTesterCyPhy)owner).turnOnBoxWifi();
                        } catch (Exception e) {
                            throw new BCMRuntimeException(e);
                        }
                    }),
                new TestStep(
                    CLOCK_URI,
                    BoxWifiTesterCyPhy.REFLECTION_INBOUND_PORT_URI,
                    activateWifiInstant,
                    owner -> {
                        try {
                            ((BoxWifiTesterCyPhy)owner).activateWifiBoxWifi();
                        } catch (Exception e) {
                            throw new BCMRuntimeException(e);
                        }
                    }),
                new TestStep(
                    CLOCK_URI,
                    BoxWifiTesterCyPhy.REFLECTION_INBOUND_PORT_URI,
                    deactivateWifiInstant,
                    owner -> {
                        try {
                            ((BoxWifiTesterCyPhy)owner).deactivateWifiBoxWifi();
                        } catch (Exception e) {
                            throw new BCMRuntimeException(e);
                        }
                    }),
                new TestStep(
                    CLOCK_URI,
                    BoxWifiTesterCyPhy.REFLECTION_INBOUND_PORT_URI,
                    turnOffInstant,
                    owner -> {
                        try {
                            ((BoxWifiTesterCyPhy)owner).turnOffBoxWifi();
                        } catch (Exception e) {
                            throw new BCMRuntimeException(e);
                        }
                    })
            });
    }

    /**
     * return a test scenario for testing with SIL simulation the box wifi
     * component.
     * 
     * <p><strong>Description</strong></p>
     * 
     * <p>
     * The test includes four steps to be executed by the box wifi unit tester
     * component: turning on the box wifi, activating wifi, deactivating wifi,
     * and then turning it off.
     * </p>
     * 
     * <p><strong>Contract</strong></p>
     * 
     * <pre>
     * pre	{@code true}	// no precondition.
     * post	{@code return != null}
     * </pre>
     *
     * @return a test scenario for the unit testing of the box wifi component.
     * @throws VerboseException <i>to do</i>.
     */
    public static TestScenarioWithSimulation unitTestScenarioWithSimulation() throws VerboseException {
        Instant startInstant = Instant.parse(START_INSTANT);
        long d = TimeUnit.NANOSECONDS.toSeconds(
                        TimeUtils.toNanos(SIMULATION_DURATION));
        Instant endInstant = startInstant.plusSeconds(d);

        // Define test steps timing (in seconds from start)
        Instant turnOnInstant = startInstant.plusSeconds(300);      // 5 minutes
        Instant activateWifiInstant = startInstant.plusSeconds(600); // 10 minutes
        Instant deactivateWifiInstant = startInstant.plusSeconds(900); // 15 minutes
        Instant turnOffInstant = startInstant.plusSeconds(1200);    // 20 minutes

        return new TestScenarioWithSimulation(
            CLOCK_URI,
            startInstant,
            endInstant,
            "global-architecture", // no global architecture in fact for unit tests
            SIMULATION_START_TIME,
            (ts, simParams) -> {
                // Simulation parameters initialization if needed
            },
            new TestStepI[] {
                new TestStep(
                    CLOCK_URI,
                    BoxWifiTesterCyPhy.REFLECTION_INBOUND_PORT_URI,
                    turnOnInstant,
                    owner -> {
                        try {
                            ((BoxWifiTesterCyPhy)owner).turnOnBoxWifi();
                        } catch (Exception e) {
                            throw new BCMRuntimeException(e);
                        }
                    }),
                new TestStep(
                    CLOCK_URI,
                    BoxWifiTesterCyPhy.REFLECTION_INBOUND_PORT_URI,
                    activateWifiInstant,
                    owner -> {
                        try {
                            ((BoxWifiTesterCyPhy)owner).activateWifiBoxWifi();
                        } catch (Exception e) {
                            throw new BCMRuntimeException(e);
                        }
                    }),
                new TestStep(
                    CLOCK_URI,
                    BoxWifiTesterCyPhy.REFLECTION_INBOUND_PORT_URI,
                    deactivateWifiInstant,
                    owner -> {
                        try {
                            ((BoxWifiTesterCyPhy)owner).deactivateWifiBoxWifi();
                        } catch (Exception e) {
                            throw new BCMRuntimeException(e);
                        }
                    }),
                new TestStep(
                    CLOCK_URI,
                    BoxWifiTesterCyPhy.REFLECTION_INBOUND_PORT_URI,
                    turnOffInstant,
                    owner -> {
                        try {
                            ((BoxWifiTesterCyPhy)owner).turnOffBoxWifi();
                        } catch (Exception e) {
                            throw new BCMRuntimeException(e);
                        }
                    })
            });
    }

    // -------------------------------------------------------------------------
    // Additional test scenarios for different use cases
    // -------------------------------------------------------------------------

    /**
     * Create a test scenario for intensive wifi usage.
     */
    public static TestScenario intensiveUsageScenario() throws VerboseException {
        Instant startInstant = Instant.parse(START_INSTANT);
        Instant endInstant = startInstant.plusSeconds(3600); // 1 hour

        return new TestScenario(
            CLOCK_URI,
            startInstant,
            endInstant,
            new TestStepI[] {
                new TestStep(
                    CLOCK_URI,
                    BoxWifiTesterCyPhy.REFLECTION_INBOUND_PORT_URI,
                    startInstant.plusSeconds(60), // 1 minute
                    owner -> {
                        try {
                            ((BoxWifiTesterCyPhy)owner).turnOnBoxWifi();
                        } catch (Exception e) {
                            throw new BCMRuntimeException(e);
                        }
                    }),
                new TestStep(
                    CLOCK_URI,
                    BoxWifiTesterCyPhy.REFLECTION_INBOUND_PORT_URI,
                    startInstant.plusSeconds(120), // 2 minutes
                    owner -> {
                        try {
                            ((BoxWifiTesterCyPhy)owner).activateWifiBoxWifi();
                        } catch (Exception e) {
                            throw new BCMRuntimeException(e);
                        }
                    }),
                new TestStep(
                    CLOCK_URI,
                    BoxWifiTesterCyPhy.REFLECTION_INBOUND_PORT_URI,
                    startInstant.plusSeconds(1800), // 30 minutes
                    owner -> {
                        try {
                            ((BoxWifiTesterCyPhy)owner).deactivateWifiBoxWifi();
                        } catch (Exception e) {
                            throw new BCMRuntimeException(e);
                        }
                    }),
                new TestStep(
                    CLOCK_URI,
                    BoxWifiTesterCyPhy.REFLECTION_INBOUND_PORT_URI,
                    startInstant.plusSeconds(2100), // 35 minutes
                    owner -> {
                        try {
                            ((BoxWifiTesterCyPhy)owner).activateWifiBoxWifi();
                        } catch (Exception e) {
                            throw new BCMRuntimeException(e);
                        }
                    }),
                new TestStep(
                    CLOCK_URI,
                    BoxWifiTesterCyPhy.REFLECTION_INBOUND_PORT_URI,
                    startInstant.plusSeconds(3300), // 55 minutes
                    owner -> {
                        try {
                            ((BoxWifiTesterCyPhy)owner).turnOffBoxWifi();
                        } catch (Exception e) {
                            throw new BCMRuntimeException(e);
                        }
                    })
            });
    }

    /**
     * Create a test scenario for minimal usage (box only, no wifi).
     */
    public static TestScenario minimalUsageScenario() throws VerboseException {
        Instant startInstant = Instant.parse(START_INSTANT);
        Instant endInstant = startInstant.plusSeconds(1800); // 30 minutes

        return new TestScenario(
            CLOCK_URI,
            startInstant,
            endInstant,
            new TestStepI[] {
                new TestStep(
                    CLOCK_URI,
                    BoxWifiTesterCyPhy.REFLECTION_INBOUND_PORT_URI,
                    startInstant.plusSeconds(300), // 5 minutes
                    owner -> {
                        try {
                            ((BoxWifiTesterCyPhy)owner).turnOnBoxWifi();
                        } catch (Exception e) {
                            throw new BCMRuntimeException(e);
                        }
                    }),
                new TestStep(
                    CLOCK_URI,
                    BoxWifiTesterCyPhy.REFLECTION_INBOUND_PORT_URI,
                    startInstant.plusSeconds(1500), // 25 minutes
                    owner -> {
                        try {
                            ((BoxWifiTesterCyPhy)owner).turnOffBoxWifi();
                        } catch (Exception e) {
                            throw new BCMRuntimeException(e);
                        }
                    })
            });
    }
}
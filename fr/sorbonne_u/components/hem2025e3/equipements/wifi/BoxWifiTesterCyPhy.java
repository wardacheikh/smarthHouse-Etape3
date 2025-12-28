package fr.sorbonne_u.components.hem2025e3.equipements.wifi;


import fr.sorbonne_u.components.annotations.RequiredInterfaces; 
import fr.sorbonne_u.components.cyphy.AbstractCyPhyComponent;
import fr.sorbonne_u.components.cyphy.ExecutionMode;
import fr.sorbonne_u.components.cyphy.utils.aclocks.ClocksServerWithSimulation;
import fr.sorbonne_u.components.exceptions.BCMException;
import fr.sorbonne_u.components.exceptions.ComponentShutdownException;
import fr.sorbonne_u.components.exceptions.ComponentStartException;
import fr.sorbonne_u.components.hem2025e1.equipement.Wifi.BoxWifiConnector;
import fr.sorbonne_u.components.hem2025e1.equipement.Wifi.BoxWifiImplementationI.BoxWifiMode;
import fr.sorbonne_u.components.hem2025e1.equipement.Wifi.BoxWifiOutboundPort;
import fr.sorbonne_u.components.utils.tests.TestScenario;
import fr.sorbonne_u.components.utils.tests.TestsStatistics;
import fr.sorbonne_u.exceptions.ImplementationInvariantException;
import fr.sorbonne_u.exceptions.AssertionChecking;
import fr.sorbonne_u.exceptions.InvariantException;
import fr.sorbonne_u.exceptions.PreconditionException;
import fr.sorbonne_u.utils.aclocks.ClocksServer;
import static org.junit.jupiter.api.Assertions.assertTrue;
import fr.sorbonne_u.components.hem2025e1.equipement.Wifi.BoxWifiUserCI;

@RequiredInterfaces(required = {BoxWifiUserCI.class})
public class BoxWifiTesterCyPhy extends AbstractCyPhyComponent {
    
    // -------------------------------------------------------------------------
    // Constants and variables
    // -------------------------------------------------------------------------

    /** when true, methods trace their actions. */
    public static boolean VERBOSE = false;
    
    /** when tracing, x coordinate of the window relative position. */
    public static int X_RELATIVE_POSITION = 0;
    
    /** when tracing, y coordinate of the window relative position. */
    public static int Y_RELATIVE_POSITION = 0;

    /** standard reflection, inbound port URI for the {@code BoxWifiTesterCyPhy} component. */
    public static final String REFLECTION_INBOUND_PORT_URI = "box-wifi-unit-tester-RIP-URI";

    /** outbound port connecting to the box wifi component. */
    protected BoxWifiOutboundPort bwop;
    
    /** URI of the box wifi inbound port to connect to. */
    protected String boxWifiInboundPortURI;

    // Execution/Simulation

    /** one thread for the method execute. */
    protected static int NUMBER_OF_STANDARD_THREADS = 1;
    
    /** one thread to schedule this component test actions. */
    protected static int NUMBER_OF_SCHEDULABLE_THREADS = 1;

    /** collector of test statistics. */
    protected TestsStatistics statistics;

    // -------------------------------------------------------------------------
    // Invariants
    // -------------------------------------------------------------------------

    /**
     * return true if the implementation invariants are observed, false otherwise.
     */
    protected static boolean implementationInvariants(BoxWifiTesterCyPhy bwt) {
        assert bwt != null : new PreconditionException("bwt != null");

        boolean ret = true;
        ret &= AssertionChecking.checkImplementationInvariant(
                bwt.boxWifiInboundPortURI != null &&
                                        !bwt.boxWifiInboundPortURI.isEmpty(),
                BoxWifiTesterCyPhy.class, bwt,
                "bwt.boxWifiInboundPortURI != null && "
                                + "!bwt.boxWifiInboundPortURI.isEmpty()");
        return ret;
    }

    /**
     * return true if the static invariants are observed, false otherwise.
     */
    public static boolean staticInvariants() {
        boolean ret = true;
        ret &= AssertionChecking.checkStaticInvariant(
                REFLECTION_INBOUND_PORT_URI != null &&
                                !REFLECTION_INBOUND_PORT_URI.isEmpty(),
                BoxWifiTesterCyPhy.class,
                "REFLECTION_INBOUND_PORT_URI != null && "
                          + "!REFLECTION_INBOUND_PORT_URI.isEmpty()");
        ret &= AssertionChecking.checkStaticInvariant(
                X_RELATIVE_POSITION >= 0,
                BoxWifiTesterCyPhy.class,
                "X_RELATIVE_POSITION >= 0");
        ret &= AssertionChecking.checkStaticInvariant(
                Y_RELATIVE_POSITION >= 0,
                BoxWifiTesterCyPhy.class,
                "Y_RELATIVE_POSITION >= 0");
        return ret;
    }

    /**
     * return true if the invariants are observed, false otherwise.
     */
    protected static boolean invariants(BoxWifiTesterCyPhy bwt) {
        assert bwt != null : new PreconditionException("bwt != null");

        boolean ret = true;
        ret &= staticInvariants();
        return ret;
    }

    // -------------------------------------------------------------------------
    // Constructors
    // -------------------------------------------------------------------------

    // Standard execution for manual tests (no test scenario and no simulation)

    /**
     * create a box wifi tester component manual tests without test scenario
     * or simulation.
     */
    protected BoxWifiTesterCyPhy(String boxWifiInboundPortURI) throws Exception {
        super(REFLECTION_INBOUND_PORT_URI,
              NUMBER_OF_STANDARD_THREADS,
              NUMBER_OF_SCHEDULABLE_THREADS);

        this.initialise(boxWifiInboundPortURI);
    }

    // Test execution with test scenario

    /**
     * create a box wifi tester component for tests (unit or integration)
     * with a test scenario but no simulation.
     */
    protected BoxWifiTesterCyPhy(
        String boxWifiInboundPortURI,
        ExecutionMode executionMode,
        TestScenario testScenario
    ) throws Exception {
        super(REFLECTION_INBOUND_PORT_URI,
              NUMBER_OF_STANDARD_THREADS,
              NUMBER_OF_SCHEDULABLE_THREADS,
              AssertionChecking.assertTrueAndReturnOrThrow(
                executionMode != null && !executionMode.isStandard(),
                executionMode,
                () -> new PreconditionException(
                                "currentExecutionMode != null && "
                                + "!currentExecutionMode.isStandard()")),
              AssertionChecking.assertTrueAndReturnOrThrow(
                testScenario != null,
                testScenario.getClockURI(),
                () -> new PreconditionException("testScenario != null")),
              testScenario);

        this.initialise(boxWifiInboundPortURI);
    }

    /**
     * initialise a box wifi tester component.
     */
    protected void initialise(String boxWifiInboundPortURI) throws Exception {
        this.boxWifiInboundPortURI = boxWifiInboundPortURI;
        this.bwop = new BoxWifiOutboundPort(this);
        this.bwop.publishPort();

        if (VERBOSE) {
            this.tracer.get().setTitle("Box WiFi tester component");
            this.tracer.get().setRelativePosition(X_RELATIVE_POSITION,
                                                  Y_RELATIVE_POSITION);
            this.toggleTracing();
        }

        if (this.getExecutionMode().isTestWithoutSimulation()) {
            this.statistics = new TestsStatistics();
        }

        assert BoxWifiTesterCyPhy.implementationInvariants(this) :
                new ImplementationInvariantException("BoxWifiTester.implementationInvariants(this)");
        assert BoxWifiTesterCyPhy.invariants(this) :
                new InvariantException("BoxWifiTester.invariants(this)");
    }

    // -------------------------------------------------------------------------
    // Test action methods
    // -------------------------------------------------------------------------

    /**
     * turn on the box wifi; method to be used in test scenario.
     */
    public void turnOnBoxWifi() throws Exception {
        this.bwop.turnOn();
    }

    /**
     * turn off the box wifi; method to be used in test scenario.
     */
    public void turnOffBoxWifi() throws Exception {
        this.bwop.turnOff();
    }

    /**
     * activate wifi; method to be used in test scenario.
     */
    public void activateWifiBoxWifi() throws Exception {
        this.bwop.activateWifi();
    }

    /**
     * deactivate wifi; method to be used in test scenario.
     */
    public void deactivateWifiBoxWifi() throws Exception {
        this.bwop.deactivateWifi();
    }

    // -------------------------------------------------------------------------
    // Tests implementations
    // -------------------------------------------------------------------------

    /**
     * test of the {@code getMode} method when the box wifi is off.
     * 
     * <p>Gherkin specification:</p>
     * <pre>
     * Feature: Getting the mode of the box wifi
     * 
     *   Scenario: getting the mode when off
     *     Given the box wifi is off
     *     When I test the mode of the box wifi
     *     Then the box wifi is in its initial mode
     * </pre>
     */
    public void testGetMode() {
        this.logMessage("Feature: Getting the mode of the box wifi");
        this.logMessage("  Scenario: getting the mode when off");
        this.logMessage("    Given the box wifi is off");
        BoxWifiMode result = null;
        try {
            this.logMessage("    When I test the mode of the box wifi");
            result = this.bwop.getMode();
            this.logMessage("    Then the box wifi is in its initial mode");
            if (!BoxWifiCyPhy.INITIAL_MODE.equals(result)) {
                this.statistics.incorrectResult();
                this.logMessage("     but was: " + result);
            }
        } catch (Throwable e) {
            this.statistics.incorrectResult();
            this.logMessage("     but the exception " + e + " has been raised");
        }

        this.statistics.updateStatistics();
    }

    /**
     * test of the {@code isOn} method.
     * 
     * <p>Gherkin specification:</p>
     * <pre>
     * Feature: Checking if the box wifi is on
     * 
     *   Scenario: checking when off
     *     Given the box wifi is off
     *     When I check if the box wifi is on
     *     Then the box wifi is reported as off
     * 
     *   Scenario: checking when on
     *     Given the box wifi is on
     *     When I check if the box wifi is on
     *     Then the box wifi is reported as on
     * </pre>
     */
    public void testIsOn() {
        this.logMessage("Feature: Checking if the box wifi is on");
        
        // Scenario 1: checking when off
        this.logMessage("  Scenario: checking when off");
        this.logMessage("    Given the box wifi is off");
        try {
            this.logMessage("    When I check if the box wifi is on");
            boolean result = this.bwop.isOn();
            this.logMessage("    Then the box wifi is reported as off");
            if (result != false) {
                this.statistics.incorrectResult();
                this.logMessage("     but was: " + result);
            }
        } catch (Throwable e) {
            this.statistics.incorrectResult();
            this.logMessage("     but the exception " + e + " has been raised");
        }

        this.statistics.updateStatistics();

        // Scenario 2: checking when on
        this.logMessage("  Scenario: checking when on");
        this.logMessage("    Given the box wifi is on");
        try {
            this.bwop.turnOn();
            this.logMessage("    When I check if the box wifi is on");
            boolean result = this.bwop.isOn();
            this.logMessage("    Then the box wifi is reported as on");
            if (result != true) {
                this.statistics.incorrectResult();
                this.logMessage("     but was: " + result);
            }
            // Clean up
            this.bwop.turnOff();
        } catch (Throwable e) {
            this.statistics.incorrectResult();
            this.logMessage("     but the exception " + e + " has been raised");
        }

        this.statistics.updateStatistics();
    }

    /**
     * test turning on and off the box wifi.
     * 
     * <p>Gherkin specification:</p>
     * <pre>
     * Feature: turning the box wifi on and off
     * 
     *   Scenario: turning on when off
     *     Given the box wifi is off
     *     When the box wifi is turned on
     *     Then the box wifi is on
     *     And the box wifi is in BOX_ONLY mode
     * 
     *   Scenario: turning on when on
     *     Given the box wifi is on
     *     When the box wifi is turned on
     *     Then a precondition exception is thrown
     * 
     *   Scenario: turning off when on
     *     Given the box wifi is on
     *     When the box wifi is turned off
     *     Then the box wifi is off
     * 
     *   Scenario: turning off when off
     *     Given the box wifi is off
     *     When the box wifi is turned off
     *     Then a precondition exception is thrown
     * </pre>
     */
    public void testTurnOnOff() {
        this.logMessage("Feature: turning the box wifi on and off");
        
        // Scenario 1: turning on when off
        this.logMessage("  Scenario: turning on when off");
        BoxWifiMode resultMode = null;
        try {
            this.logMessage("    Given the box wifi is off");
            resultMode = this.bwop.getMode();
            if (!BoxWifiMode.OFF.equals(resultMode)) {
                this.logMessage("     but was: " + resultMode);
                this.statistics.failedCondition();
            }
            this.logMessage("    When the box wifi is turned on");
            this.bwop.turnOn();
            this.logMessage("    Then the box wifi is on");
            boolean isOnResult = this.bwop.isOn();
            if (!isOnResult) {
                this.logMessage("     but was: off");
                this.statistics.incorrectResult();
            }
            this.logMessage("    And the box wifi is in BOX_ONLY mode");
            resultMode = this.bwop.getMode();
            if (!BoxWifiMode.BOX_ONLY.equals(resultMode)) {
                this.logMessage("     but was: " + resultMode);
                this.statistics.incorrectResult();
            }
        } catch (Throwable e) {
            this.statistics.incorrectResult();
            this.logMessage("     but the exception " + e + " has been raised");
        }

        this.statistics.updateStatistics();

        // Scenario 2: turning on when on
        this.logMessage("  Scenario: turning on when on");
        this.logMessage("    Given the box wifi is on");
        try {
            boolean isOnResult = this.bwop.isOn();
            if (!isOnResult) {
                this.logMessage("     but was: off");
                this.statistics.failedCondition();
            }
        } catch (Throwable e) {
            this.statistics.failedCondition();
            this.logMessage("     but the exception " + e + " has been raised");
        }
        this.logMessage("    When the box wifi is turned on");
        this.logMessage("    Then a precondition exception is thrown");
        boolean old = BCMException.VERBOSE;
        try {
            BCMException.VERBOSE = false;
            this.bwop.turnOn();
            this.logMessage("     but it was not thrown");
            this.statistics.incorrectResult();
        } catch(Throwable e) {
            // Expected exception
        } finally {
            BCMException.VERBOSE = old;
        }

        this.statistics.updateStatistics();

        // Scenario 3: turning off when on
        this.logMessage("  Scenario: turning off when on");
        this.logMessage("    Given the box wifi is on");
        try {
            boolean isOnResult = this.bwop.isOn();
            if (!isOnResult) {
                this.logMessage("     but was: off");
                this.statistics.failedCondition();
            }
        } catch (Throwable e) {
            this.statistics.failedCondition();
            this.logMessage("     but the exception " + e + " has been raised");
        }
        this.logMessage("    When the box wifi is turned off");
        try {
            this.bwop.turnOff();
            this.logMessage("    Then the box wifi is off");
            boolean isOnResult = this.bwop.isOn();
            if (isOnResult) {
                this.logMessage("     but was: on");
                this.statistics.incorrectResult();
            }
        } catch (Throwable e) {
            this.statistics.incorrectResult();
            this.logMessage("     but the exception " + e + " has been raised");
        }

        this.statistics.updateStatistics();

        // Scenario 4: turning off when off
        this.logMessage("  Scenario: turning off when off");
        this.logMessage("    Given the box wifi is off");
        try {
            boolean isOnResult = this.bwop.isOn();
            if (isOnResult) {
                this.logMessage("     but was: on");
                this.statistics.failedCondition();
            }
        } catch (Throwable e) {
            this.statistics.failedCondition();
            this.logMessage("     but the exception " + e + " has been raised");
        }
        this.logMessage("    When the box wifi is turned off");
        this.logMessage("    Then a precondition exception is thrown");
        old = BCMException.VERBOSE;
        try {
            BCMException.VERBOSE = false;
            this.bwop.turnOff();
            this.logMessage("     but the precondition exception was not thrown");
            this.statistics.incorrectResult();
        } catch (Throwable e) {
            // Expected exception
        } finally {
            BCMException.VERBOSE = old;
        }

        this.statistics.updateStatistics();
    }

    /**
     * test activating and deactivating wifi.
     * 
     * <p>Gherkin specification:</p>
     * <pre>
     * Feature: activating and deactivating wifi
     * 
     *   Scenario: activate wifi from BOX_ONLY mode
     *     Given the box wifi is on in BOX_ONLY mode
     *     When wifi is activated
     *     Then the box wifi is in FULL_ON mode
     * 
     *   Scenario: activate wifi when off
     *     Given the box wifi is off
     *     When wifi is activated
     *     Then nothing happens (box stays off)
     * 
     *   Scenario: deactivate wifi from FULL_ON mode
     *     Given the box wifi is on in FULL_ON mode
     *     When wifi is deactivated
     *     Then the box wifi is in BOX_ONLY mode
     * 
     *   Scenario: deactivate wifi when not in FULL_ON mode
     *     Given the box wifi is not in FULL_ON mode
     *     When wifi is deactivated
     *     Then nothing happens
     * </pre>
     */
    public void testActivateDeactivateWifi() {
        this.logMessage("Feature: activating and deactivating wifi");
        
        // Scenario 1: activate wifi from BOX_ONLY mode
        this.logMessage("  Scenario: activate wifi from BOX_ONLY mode");
        try {
            this.logMessage("    Given the box wifi is on in BOX_ONLY mode");
            this.bwop.turnOn();
            BoxWifiMode initialMode = this.bwop.getMode();
            if (!BoxWifiMode.BOX_ONLY.equals(initialMode)) {
                this.logMessage("     but was: " + initialMode);
                this.statistics.failedCondition();
            }
            this.logMessage("    When wifi is activated");
            this.bwop.activateWifi();
            this.logMessage("    Then the box wifi is in FULL_ON mode");
            BoxWifiMode resultMode = this.bwop.getMode();
            if (!BoxWifiMode.FULL_ON.equals(resultMode)) {
                this.logMessage("     but was: " + resultMode);
                this.statistics.incorrectResult();
            }
            // Clean up
            this.bwop.turnOff();
        } catch (Throwable e) {
            this.statistics.incorrectResult();
            this.logMessage("     but the exception " + e + " has been raised");
        }

        this.statistics.updateStatistics();

        // Scenario 2: activate wifi when off
        this.logMessage("  Scenario: activate wifi when off");
        try {
            this.logMessage("    Given the box wifi is off");
            this.bwop.turnOff();
            BoxWifiMode initialMode = this.bwop.getMode();
            if (!BoxWifiMode.OFF.equals(initialMode)) {
                this.logMessage("     but was: " + initialMode);
                this.statistics.failedCondition();
            }
            this.logMessage("    When wifi is activated");
            this.bwop.activateWifi();
            this.logMessage("    Then nothing happens (box stays off)");
            BoxWifiMode resultMode = this.bwop.getMode();
            if (!BoxWifiMode.OFF.equals(resultMode)) {
                this.logMessage("     but changed to: " + resultMode);
                this.statistics.incorrectResult();
            }
        } catch (Throwable e) {
            this.statistics.incorrectResult();
            this.logMessage("     but the exception " + e + " has been raised");
        }

        this.statistics.updateStatistics();

        // Scenario 3: deactivate wifi from FULL_ON mode
        this.logMessage("  Scenario: deactivate wifi from FULL_ON mode");
        try {
            this.logMessage("    Given the box wifi is on in FULL_ON mode");
            this.bwop.turnOn();
            this.bwop.activateWifi();
            BoxWifiMode initialMode = this.bwop.getMode();
            if (!BoxWifiMode.FULL_ON.equals(initialMode)) {
                this.logMessage("     but was: " + initialMode);
                this.statistics.failedCondition();
            }
            this.logMessage("    When wifi is deactivated");
            this.bwop.deactivateWifi();
            this.logMessage("    Then the box wifi is in BOX_ONLY mode");
            BoxWifiMode resultMode = this.bwop.getMode();
            if (!BoxWifiMode.BOX_ONLY.equals(resultMode)) {
                this.logMessage("     but was: " + resultMode);
                this.statistics.incorrectResult();
            }
            // Clean up
            this.bwop.turnOff();
        } catch (Throwable e) {
            this.statistics.incorrectResult();
            this.logMessage("     but the exception " + e + " has been raised");
        }

        this.statistics.updateStatistics();

        // Scenario 4: deactivate wifi when not in FULL_ON mode
        this.logMessage("  Scenario: deactivate wifi when not in FULL_ON mode");
        try {
            this.logMessage("    Given the box wifi is not in FULL_ON mode");
            this.bwop.turnOn(); // Now in BOX_ONLY mode
            BoxWifiMode initialMode = this.bwop.getMode();
            if (BoxWifiMode.FULL_ON.equals(initialMode)) {
                this.logMessage("     but was in FULL_ON mode");
                this.statistics.failedCondition();
            }
            this.logMessage("    When wifi is deactivated");
            this.bwop.deactivateWifi();
            this.logMessage("    Then nothing happens");
            BoxWifiMode resultMode = this.bwop.getMode();
            if (!BoxWifiMode.BOX_ONLY.equals(resultMode)) {
                this.logMessage("     but changed to: " + resultMode);
                this.statistics.incorrectResult();
            }
            // Clean up
            this.bwop.turnOff();
        } catch (Throwable e) {
            this.statistics.incorrectResult();
            this.logMessage("     but the exception " + e + " has been raised");
        }

        this.statistics.updateStatistics();
    }

    /**
     * run all unit tests.
     * 
     * <p>The tests are run in the following order:</p>
     * <ol>
     * <li>{@code testGetMode}</li>
     * <li>{@code testIsOn}</li>
     * <li>{@code testTurnOnOff}</li>
     * <li>{@code testActivateDeactivateWifi}</li>
     * </ol>
     */
    protected void runAllUnitTests() {
        this.testGetMode();
        this.testIsOn();
        this.testTurnOnOff();
        this.testActivateDeactivateWifi();

        this.statistics.statisticsReport(this);
    }

    // -------------------------------------------------------------------------
    // Component life-cycle
    // -------------------------------------------------------------------------

    @Override
    public synchronized void start() throws ComponentStartException {
        super.start();

        try {
            this.doPortConnection(
                            this.bwop.getPortURI(),
                            boxWifiInboundPortURI,
                            BoxWifiConnector.class.getCanonicalName());
        } catch (Throwable e) {
            throw new ComponentStartException(e);
        }
    }

    @Override
    public synchronized void execute() throws Exception {
        this.traceMessage("Box WiFi Unit Tester begins execution.\n");

        switch (this.getExecutionMode()) {
            case UNIT_TEST:
            case INTEGRATION_TEST:
                this.initialiseClock(
                        ClocksServer.STANDARD_INBOUNDPORT_URI,
                        this.clockURI);
                this.executeTestScenario(testScenario);
                break;
            case UNIT_TEST_WITH_SIL_SIMULATION:
            case INTEGRATION_TEST_WITH_SIL_SIMULATION:
                this.initialiseClock4Simulation(
                        ClocksServerWithSimulation.STANDARD_INBOUNDPORT_URI,
                        this.clockURI);
                this.executeTestScenario(testScenario);
                break;
            case INTEGRATION_TEST_WITH_HIL_SIMULATION:
            case UNIT_TEST_WITH_HIL_SIMULATION:
                throw new BCMException("HIL simulation not implemented yet!");
            case STANDARD:
                this.statistics = new TestsStatistics();
                this.traceMessage("Box WiFi Unit Tester starts the tests.\n");
                this.runAllUnitTests();
                this.traceMessage("Box WiFi Unit Tester ends.\n");
                break;
            default:
        }
        this.traceMessage("Box WiFi Unit Tester ends execution.\n");
    }

    @Override
    public synchronized void finalise() throws Exception {
        this.doPortDisconnection(this.bwop.getPortURI());
        super.finalise();
    }

    @Override
    public synchronized void shutdown() throws ComponentShutdownException {
        try {
            this.bwop.unpublishPort();
        } catch (Throwable e) {
            throw new ComponentShutdownException(e);
        }
        super.shutdown();
    }
}
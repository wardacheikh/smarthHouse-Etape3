package fr.sorbonne_u.components.hem2025e3.equipements.wifi;

import java.util.HashMap;
import java.util.concurrent.TimeUnit;
import java.util.Set;
import java.util.HashSet;
import java.util.function.Supplier;

import fr.sorbonne_u.alasca.physical_data.Measure;
import fr.sorbonne_u.components.AbstractComponent;
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
import fr.sorbonne_u.components.hem2025e1.equipement.Wifi.BoxWifiImplementationI;
import fr.sorbonne_u.components.hem2025e1.equipement.Wifi.BoxWifiUserCI;
import fr.sorbonne_u.components.hem2025e1.equipement.Wifi.BoxWifiInboundPort;
import fr.sorbonne_u.components.hem2025e2.equipments.BoxWifi.mil.events.ActivateWifiBoxWifi;
import fr.sorbonne_u.components.hem2025e2.equipments.BoxWifi.mil.events.DeactivateWifiBoxWifi;
import fr.sorbonne_u.components.hem2025e2.equipments.BoxWifi.mil.events.SwitchOffBoxWifi;
import fr.sorbonne_u.components.hem2025e2.equipments.BoxWifi.mil.events.SwitchOnBoxWifi;
import fr.sorbonne_u.components.hem2025e3.equipements.wifi.sil.BoxWifiStateSILModel;
import fr.sorbonne_u.components.hem2025e3.equipements.wifi.sil.Local_SIL_SimulationArchitectures;
import fr.sorbonne_u.components.utils.tests.TestScenario;
import fr.sorbonne_u.devs_simulation.architectures.RTArchitecture;
import fr.sorbonne_u.devs_simulation.models.annotations.ModelExternalEvents;
import fr.sorbonne_u.exceptions.AssertionChecking;
import fr.sorbonne_u.exceptions.ImplementationInvariantException;
import fr.sorbonne_u.exceptions.InvariantException;
import fr.sorbonne_u.exceptions.PostconditionException;
import fr.sorbonne_u.exceptions.PreconditionException;

@SIL_Simulation_Architectures({
    @LocalArchitecture(
        uri = "silUnitTests",
        rootModelURI = "BoxWifiCoupledModel",
        simulatedTimeUnit = TimeUnit.HOURS,
        externalEvents = @ModelExternalEvents()
    ),
    @LocalArchitecture(
        uri = "silIntegrationTests",
        rootModelURI = "BoxWifiStateSILModel",
        simulatedTimeUnit = TimeUnit.HOURS,
        externalEvents = @ModelExternalEvents(
            imported = {},
            exported = {SwitchOnBoxWifi.class, 
                       SwitchOffBoxWifi.class,
                       ActivateWifiBoxWifi.class,
                       DeactivateWifiBoxWifi.class}
        )
    )
})
@OfferedInterfaces(offered = {BoxWifiUserCI.class})
public class BoxWifiCyPhy extends AbstractCyPhyComponent implements BoxWifiImplementationI {
    
    // -------------------------------------------------------------------------
    // Constants and variables
    // -------------------------------------------------------------------------

    /** standard URI of the box wifi reflection inbound port. */
    public static final String REFLECTION_INBOUND_PORT_URI = "BOX-WIFI-RIP-URI";    
    /** URI of the box wifi inbound port used in tests. */
    public static final String INBOUND_PORT_URI = "BOX-WIFI-INBOUND-PORT-URI";
    /** URI of the local simulation architecture for SIL unit tests. */
    public static final String UNIT_TEST_ARCHITECTURE_URI = "silUnitTests";
    /** URI of the local simulation architecture for SIL unit tests. */
    public static final String INTEGRATION_TEST_ARCHITECTURE_URI = "silIntegrationTests";

    // Configuration
    
    /** power consumption when in mode BOX_ONLY */
    public static final Measure<Double> BOX_ONLY_POWER = new Measure<Double>(15.0, POWER_UNIT);
    /** power consumption when in mode FULL_ON */
    public static final Measure<Double> FULL_ON_POWER = new Measure<Double>(25.0, POWER_UNIT);
    /** tension required by the box wifi */
    public static final Measure<Double> TENSION = new Measure<Double>(220.0, TENSION_UNIT);

    // Internal component state variables
    public static final BoxWifiMode INITIAL_MODE = BoxWifiMode.OFF;
    protected BoxWifiMode currentMode;

    protected BoxWifiInboundPort bwip;

    // Execution/Simulation
    public static boolean VERBOSE = true;
    public static boolean DEBUG = false;
    public static int X_RELATIVE_POSITION = 2;
    public static int Y_RELATIVE_POSITION = 2;

    protected static int NUMBER_OF_STANDARD_THREADS = 2;
    protected static int NUMBER_OF_SCHEDULABLE_THREADS = 0;

    protected AtomicSimulatorPlugin asp;
    protected final String localArchitectureURI;
    protected final double accelerationFactor;

    // -------------------------------------------------------------------------
    // Invariants
    // -------------------------------------------------------------------------

    public static boolean staticImplementationInvariants() {
        boolean ret = true;
        ret &= AssertionChecking.checkStaticImplementationInvariant(
                INITIAL_MODE != null,
                BoxWifiCyPhy.class,
                "INITIAL_MODE != null");
        ret &= AssertionChecking.checkStaticImplementationInvariant(
                NUMBER_OF_STANDARD_THREADS >= 0,
                BoxWifiCyPhy.class,
                "NUMBER_OF_STANDARD_THREADS >= 0");
        ret &= AssertionChecking.checkStaticImplementationInvariant(
                NUMBER_OF_SCHEDULABLE_THREADS >= 0,
                BoxWifiCyPhy.class,
                "NUMBER_OF_SCHEDULABLE_THREADS >= 0");
        return ret;
    }

    protected static boolean implementationInvariants(BoxWifiCyPhy box) {
        assert box != null : new PreconditionException("box != null");

        boolean ret = true;
        ret &= staticImplementationInvariants();
        ret &= AssertionChecking.checkInvariant(
                box.currentMode != null,
                BoxWifiCyPhy.class, box,
                "currentMode != null");
        ret &= AssertionChecking.checkInvariant(
                box.localArchitectureURI == null ||
                        !box.localArchitectureURI.isEmpty() &&
                                box.accelerationFactor > 0.0,
                BoxWifiCyPhy.class, box,
                "localArchitectureURI == null || !localArchitectureURI.isEmpty()"
                + " && accelerationFactor > 0.0");
        ret &= AssertionChecking.checkInvariant(
                box.asp == null || box.localArchitectureURI != null,
                BoxWifiCyPhy.class, box,
                "asp == null || localArchitectureURI != null");
        return ret;
    }

    public static boolean staticInvariants() {
        boolean ret = true;
        ret &= AssertionChecking.checkStaticInvariant(
                REFLECTION_INBOUND_PORT_URI != null && !REFLECTION_INBOUND_PORT_URI.isEmpty(),
                BoxWifiCyPhy.class,
                "REFLECTION_INBOUND_PORT_URI != null && !REFLECTION_INBOUND_PORT_URI.isEmpty()");
        ret &= AssertionChecking.checkStaticInvariant(
                INBOUND_PORT_URI != null && !INBOUND_PORT_URI.isEmpty(),
                BoxWifiCyPhy.class,
                "INBOUND_PORT_URI != null && !INBOUND_PORT_URI.isEmpty()");
        ret &= AssertionChecking.checkStaticInvariant(
                UNIT_TEST_ARCHITECTURE_URI != null && !UNIT_TEST_ARCHITECTURE_URI.isEmpty(),
                BoxWifiCyPhy.class,
                "UNIT_TEST_ARCHITECTURE_URI != null && !UNIT_TEST_ARCHITECTURE_URI.isEmpty()");
        ret &= AssertionChecking.checkStaticInvariant(
                INTEGRATION_TEST_ARCHITECTURE_URI != null && !INTEGRATION_TEST_ARCHITECTURE_URI.isEmpty(),
                BoxWifiCyPhy.class,
                "INTEGRATION_TEST_ARCHITECTURE_URI != null && !INTEGRATION_TEST_ARCHITECTURE_URI.isEmpty()");
        ret &= AssertionChecking.checkStaticInvariant(
                BOX_ONLY_POWER != null && BOX_ONLY_POWER.getData() > 0.0 && 
                BOX_ONLY_POWER.getMeasurementUnit().equals(POWER_UNIT),
                BoxWifiCyPhy.class,
                "BOX_ONLY_POWER != null && BOX_ONLY_POWER.getData() > 0.0 && BOX_ONLY_POWER.getMeasurementUnit().equals(POWER_UNIT)");
        ret &= AssertionChecking.checkStaticInvariant(
                FULL_ON_POWER != null && FULL_ON_POWER.getData() > 0.0 && 
                FULL_ON_POWER.getMeasurementUnit().equals(POWER_UNIT),
                BoxWifiCyPhy.class,
                "FULL_ON_POWER != null && FULL_ON_POWER.getData() > 0.0 && FULL_ON_POWER.getMeasurementUnit().equals(POWER_UNIT)");
        ret &= AssertionChecking.checkStaticInvariant(
                FULL_ON_POWER.getData() > BOX_ONLY_POWER.getData(),
                BoxWifiCyPhy.class,
                "FULL_ON_POWER.getData() > BOX_ONLY_POWER.getData()");
        ret &= AssertionChecking.checkStaticInvariant(
                TENSION != null && (TENSION.getData() == 110.0 || TENSION.getData() == 220.0) && 
                TENSION.getMeasurementUnit().equals(TENSION_UNIT),
                BoxWifiCyPhy.class,
                "TENSION != null && (TENSION.getData() == 110.0 || TENSION.getData() == 220.0) && TENSION.getMeasurementUnit().equals(TENSION_UNIT)");
        ret &= AssertionChecking.checkStaticInvariant(
                INITIAL_MODE != null,
                BoxWifiCyPhy.class,
                "INITIAL_MODE != null");
        ret &= AssertionChecking.checkStaticInvariant(
                X_RELATIVE_POSITION >= 0,
                BoxWifiCyPhy.class,
                "X_RELATIVE_POSITION >= 0");
        ret &= AssertionChecking.checkStaticInvariant(
                Y_RELATIVE_POSITION >= 0,
                BoxWifiCyPhy.class,
                "Y_RELATIVE_POSITION >= 0");
        return ret;
    }

    protected static boolean invariants(BoxWifiCyPhy box) {
        assert box != null : new PreconditionException("box != null");

        boolean ret = true;
        ret &= staticInvariants();
        ret &= implementationInvariants(box);
        return ret;
    }

    // -------------------------------------------------------------------------
    // Constructors (simplifiés comme HairDryerCyPhy)
    // -------------------------------------------------------------------------

    // Standard execution
    protected BoxWifiCyPhy() throws Exception {
        this(INBOUND_PORT_URI);
    }

    protected BoxWifiCyPhy(String boxWifiInboundPortURI) throws Exception {
        super(REFLECTION_INBOUND_PORT_URI, NUMBER_OF_STANDARD_THREADS, NUMBER_OF_SCHEDULABLE_THREADS);
        
        this.localArchitectureURI = null;
        this.accelerationFactor = 0.0;
        
        this.initialise(boxWifiInboundPortURI);
        
        assert BoxWifiCyPhy.implementationInvariants(this) :
                new ImplementationInvariantException("BoxWifiCyPhy.implementationInvariants(this)");
        assert BoxWifiCyPhy.invariants(this) :
                new InvariantException("BoxWifiCyPhy.invariants(this)");
    }

    // Tests without simulation
    protected BoxWifiCyPhy(ExecutionMode executionMode) throws Exception {
        this(REFLECTION_INBOUND_PORT_URI, INBOUND_PORT_URI,
             AssertionChecking.assertTrueAndReturnOrThrow(
                     executionMode != null && executionMode.isTestWithoutSimulation(),
                     executionMode,
                     () -> new PreconditionException("executionMode != null && executionMode.isTestWithoutSimulation()")));
    }

    protected BoxWifiCyPhy(String boxWifiInboundPortURI, ExecutionMode executionMode) throws Exception {
        this(REFLECTION_INBOUND_PORT_URI, boxWifiInboundPortURI,
             AssertionChecking.assertTrueAndReturnOrThrow(
                     executionMode != null && executionMode.isTestWithoutSimulation(),
                     executionMode,
                     () -> new PreconditionException("executionMode != null && executionMode.isTestWithoutSimulation()")));
    }

    protected BoxWifiCyPhy(String reflectionInboundPortURI, String boxWifiInboundPortURI, 
                          ExecutionMode executionMode) throws Exception {
        super(reflectionInboundPortURI, NUMBER_OF_STANDARD_THREADS, NUMBER_OF_SCHEDULABLE_THREADS,
              executionMode, "fake-clock", null);

        assert executionMode != null && executionMode.isTestWithoutSimulation() :
                new PreconditionException("executionMode != null && executionMode.isTestWithoutSimulation()");

        this.localArchitectureURI = null;
        this.accelerationFactor = 0.0;

        this.initialise(boxWifiInboundPortURI);

        assert BoxWifiCyPhy.implementationInvariants(this) :
                new ImplementationInvariantException("BoxWifiCyPhy.implementationInvariants(this)");
        assert BoxWifiCyPhy.invariants(this) :
                new InvariantException("BoxWifiCyPhy.invariants(this)");
    }

    // Tests with simulation (comme HairDryerCyPhy)
    protected BoxWifiCyPhy(
        String reflectionInboundPortURI,
        String boxWifiInboundPortURI,
        ExecutionMode executionMode,
        TestScenario testScenario,
        String localArchitectureURI,
        double accelerationFactor
    ) throws Exception {
        super(reflectionInboundPortURI,
              NUMBER_OF_STANDARD_THREADS, 
              NUMBER_OF_SCHEDULABLE_THREADS,
              executionMode,
              AssertionChecking.assertTrueAndReturnOrThrow(
                testScenario != null,
                testScenario.getClockURI(),
                () -> new PreconditionException("testScenario != null")),
              testScenario,
              ((Supplier<Set<String>>)() -> {
                   HashSet<String> hs = new HashSet<>();
                   hs.add(UNIT_TEST_ARCHITECTURE_URI);
                   hs.add(INTEGRATION_TEST_ARCHITECTURE_URI);
                   return hs;
              }).get(),
              accelerationFactor);

        assert boxWifiInboundPortURI != null && !boxWifiInboundPortURI.isEmpty() :
                new PreconditionException("boxWifiInboundPortURI != null && !boxWifiInboundPortURI.isEmpty()");

        this.localArchitectureURI = localArchitectureURI;
        this.accelerationFactor = accelerationFactor;

        this.initialise(boxWifiInboundPortURI);

        if (DEBUG) {
            this.logMessage("BoxWifiCyPhy local simulation architectures: " + this.localSimulationArchitectures);
        }

        assert BoxWifiCyPhy.implementationInvariants(this) :
                new ImplementationInvariantException("BoxWifiCyPhy.implementationInvariants(this)");
        assert BoxWifiCyPhy.invariants(this) :
                new InvariantException("BoxWifiCyPhy.invariants(this)");
    }

    // -------------------------------------------------------------------------
    // Initialisation methods
    // -------------------------------------------------------------------------

    protected void initialise(String boxWifiInboundPortURI) throws Exception {
        assert boxWifiInboundPortURI != null :
                new PreconditionException("boxWifiInboundPortURI != null");
        assert !boxWifiInboundPortURI.isEmpty() :
                new PreconditionException("!boxWifiInboundPortURI.isEmpty()");

        this.currentMode = INITIAL_MODE;
        this.bwip = new BoxWifiInboundPort(boxWifiInboundPortURI, this);
        this.bwip.publishPort();

        if (BoxWifiCyPhy.VERBOSE || BoxWifiCyPhy.DEBUG) {
            this.tracer.get().setTitle("Box WiFi component");
            this.tracer.get().setRelativePosition(X_RELATIVE_POSITION, Y_RELATIVE_POSITION);
            this.toggleTracing();
        }

        assert BoxWifiCyPhy.implementationInvariants(this) :
                new ImplementationInvariantException("BoxWifiCyPhy.implementationInvariants(this)");
        assert BoxWifiCyPhy.invariants(this) :
                new InvariantException("BoxWifiCyPhy.invariants(this)");
    }

    @Override
    protected RTArchitecture createLocalSimulationArchitecture(
        String architectureURI,
        String rootModelURI,
        TimeUnit simulatedTimeUnit,
        double accelerationFactor
    ) throws Exception {
        assert architectureURI != null && !architectureURI.isEmpty() :
                new PreconditionException("architectureURI != null && !architectureURI.isEmpty()");
        assert rootModelURI != null && !rootModelURI.isEmpty() :
                new PreconditionException("rootModelURI != null && !rootModelURI.isEmpty()");
        assert simulatedTimeUnit != null :
                new PreconditionException("simulatedTimeUnit != null");
        assert accelerationFactor > 0.0 :
                new PreconditionException("accelerationFactor > 0.0");

        RTArchitecture ret = null;
        if (architectureURI.equals(UNIT_TEST_ARCHITECTURE_URI)) {
            ret = Local_SIL_SimulationArchitectures.
                    createBoxWifiSIL_Architecture4UnitTest(
                            architectureURI,
                            rootModelURI,
                            simulatedTimeUnit,
                            accelerationFactor);
        } else if (architectureURI.equals(INTEGRATION_TEST_ARCHITECTURE_URI)) {
            ret = Local_SIL_SimulationArchitectures.
                    createBoxWifiSIL_Architecture4IntegrationTest(
                            architectureURI,
                            rootModelURI,
                            simulatedTimeUnit,
                            accelerationFactor);
        } else {
            throw new BCMException("Unknown local simulation architecture URI: " + architectureURI);
        }
        
        return ret;
    }

    // -------------------------------------------------------------------------
    // Component life-cycle (comme HairDryerCyPhy)
    // -------------------------------------------------------------------------

    @Override
    public synchronized void start() throws ComponentStartException {
        super.start();

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
                this.asp = new RTAtomicSimulatorPlugin();
                ((RTAtomicSimulatorPlugin)this.asp).
                                setPluginURI(architecture.getRootModelURI());
                ((RTAtomicSimulatorPlugin)this.asp).
                                        setSimulationArchitecture(architecture);
                this.installPlugin(this.asp);
                this.asp.createSimulator();
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
            throw new ComponentStartException(e);
        }

        assert BoxWifiCyPhy.implementationInvariants(this) :
                new ImplementationInvariantException("BoxWifiCyPhy.implementationInvariants(this)");
        assert BoxWifiCyPhy.invariants(this) :
                new InvariantException("BoxWifiCyPhy.invariants(this)");
    }

    @Override
    public void execute() throws Exception {
        this.traceMessage("Box WiFi CyPhy executes.\n");

        assert BoxWifiCyPhy.implementationInvariants(this) :
                new ImplementationInvariantException("BoxWifiCyPhy.implementationInvariants(this)");
        assert BoxWifiCyPhy.invariants(this) :
                new InvariantException("BoxWifiCyPhy.invariants(this)");

        switch (this.getExecutionMode()) {
            case UNIT_TEST:
            case INTEGRATION_TEST:
                break;
            case UNIT_TEST_WITH_SIL_SIMULATION:
                this.initialiseClock4Simulation(
                        ClocksServerWithSimulation.STANDARD_INBOUNDPORT_URI,
                        this.clockURI);
                this.asp.initialiseSimulation(
                        this.getClock4Simulation().getSimulatedStartTime(),
                        this.getClock4Simulation().getSimulatedDuration());
                this.asp.startRTSimulation(
                        TimeUnit.NANOSECONDS.toMillis(
                                this.getClock4Simulation().getStartEpochNanos()),
                        this.getClock4Simulation().getSimulatedStartTime().getSimulatedTime(),
                        this.getClock4Simulation().getSimulatedDuration().getSimulatedDuration());
                this.getClock4Simulation().waitUntilEnd();
                Thread.sleep(200L);
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

    @Override
    public synchronized void shutdown() throws ComponentShutdownException {
        try {
            this.bwip.unpublishPort();
        } catch (Throwable e) {
            throw new ComponentShutdownException(e);
        }
        super.shutdown();
    }

    // -------------------------------------------------------------------------
    // Services implementation (adapté à la logique WiFi)
    // -------------------------------------------------------------------------

    @Override
    public BoxWifiMode getMode() throws Exception {
        if (BoxWifiCyPhy.VERBOSE) {
            this.traceMessage("Box WiFi returns its mode : " + this.currentMode + ".\n");
        }

        return this.currentMode;
    }

    @Override
    public boolean isOn() throws Exception {
        boolean isOn = this.currentMode != BoxWifiMode.OFF;
        if (BoxWifiCyPhy.VERBOSE) {
            this.traceMessage("Box WiFi is " + (isOn ? "ON" : "OFF") + ".\n");
        }
        return isOn;
    }

    @Override
    public void turnOn() throws Exception {
        if (BoxWifiCyPhy.VERBOSE) {
            this.traceMessage("Box WiFi is turned on (WiFi deactivated).\n");
        }

        assert this.getMode() == BoxWifiMode.OFF :
                new PreconditionException("getMode() == BoxWifiMode.OFF");

        this.currentMode = BoxWifiMode.BOX_ONLY;

        assert this.getMode() == BoxWifiMode.BOX_ONLY :
                new PostconditionException("getMode() == BoxWifiMode.BOX_ONLY");

        if (this.getExecutionMode().isSILTest()) {
            ((RTAtomicSimulatorPlugin)this.asp).triggerExternalEvent(
                    BoxWifiStateSILModel.URI,
                    t -> new SwitchOnBoxWifi(t));
        }
    }

    @Override
    public void turnOff() throws Exception {
        if (BoxWifiCyPhy.VERBOSE) {
            this.traceMessage("Box WiFi is turned off.\n");
        }

        assert this.getMode() != BoxWifiMode.OFF :
                new PreconditionException("getMode() != BoxWifiMode.OFF");

        this.currentMode = BoxWifiMode.OFF;

        assert this.getMode() == BoxWifiMode.OFF :
                new PostconditionException("getMode() == BoxWifiMode.OFF");

        if (this.getExecutionMode().isSILTest()) {
            ((RTAtomicSimulatorPlugin)this.asp).triggerExternalEvent(
                    BoxWifiStateSILModel.URI,
                    t -> new SwitchOffBoxWifi(t));
        }
    }

    @Override
    public void activateWifi() throws Exception {
        if (BoxWifiCyPhy.VERBOSE) {
            this.traceMessage("Box WiFi WiFi is activated.\n");
        }

        assert this.getMode() == BoxWifiMode.BOX_ONLY :
                new PreconditionException("getMode() == BoxWifiMode.BOX_ONLY");

        this.currentMode = BoxWifiMode.FULL_ON;

        assert this.getMode() == BoxWifiMode.FULL_ON :
                new PostconditionException("getMode() == BoxWifiMode.FULL_ON");

        if (this.getExecutionMode().isSILTest()) {
            ((RTAtomicSimulatorPlugin)this.asp).triggerExternalEvent(
                    BoxWifiStateSILModel.URI,
                    t -> new ActivateWifiBoxWifi(t));
        }
    }

    @Override
    public void deactivateWifi() throws Exception {
        if (BoxWifiCyPhy.VERBOSE) {
            this.traceMessage("Box WiFi WiFi is deactivated.\n");
        }

        assert this.getMode() == BoxWifiMode.FULL_ON :
                new PreconditionException("getMode() == BoxWifiMode.FULL_ON");

        this.currentMode = BoxWifiMode.BOX_ONLY;

        assert this.getMode() == BoxWifiMode.BOX_ONLY :
                new PostconditionException("getMode() == BoxWifiMode.BOX_ONLY");

        if (this.getExecutionMode().isSILTest()) {
            ((RTAtomicSimulatorPlugin)this.asp).triggerExternalEvent(
                    BoxWifiStateSILModel.URI,
                    t -> new DeactivateWifiBoxWifi(t));
        }
    }
}
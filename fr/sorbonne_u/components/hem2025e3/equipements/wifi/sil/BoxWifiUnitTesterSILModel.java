package fr.sorbonne_u.components.hem2025e3.equipements.wifi.sil;

import java.util.concurrent.TimeUnit; 
import java.util.Map;

import fr.sorbonne_u.components.cyphy.plugins.devs.AtomicSimulatorPlugin;
import fr.sorbonne_u.components.hem2025e2.equipments.BoxWifi.mil.BoxWifiTesterModel;
import fr.sorbonne_u.components.hem2025e2.equipments.BoxWifi.mil.BoxWifiUnitTesterModel;
import fr.sorbonne_u.components.hem2025e2.equipments.BoxWifi.mil.events.ActivateWifiBoxWifi;
import fr.sorbonne_u.components.hem2025e2.equipments.BoxWifi.mil.events.DeactivateWifiBoxWifi;
import fr.sorbonne_u.components.hem2025e2.equipments.BoxWifi.mil.events.SwitchOffBoxWifi;
import fr.sorbonne_u.components.hem2025e2.equipments.BoxWifi.mil.events.SwitchOnBoxWifi;
import fr.sorbonne_u.devs_simulation.exceptions.MissingRunParameterException;
import fr.sorbonne_u.devs_simulation.exceptions.NeoSim4JavaException;
import fr.sorbonne_u.devs_simulation.models.annotations.ModelExternalEvents;
import fr.sorbonne_u.devs_simulation.simulators.interfaces.AtomicSimulatorI;
import fr.sorbonne_u.devs_simulation.utils.StandardLogger;

@ModelExternalEvents(exported = {SwitchOnBoxWifi.class,
                                 SwitchOffBoxWifi.class,
                                 ActivateWifiBoxWifi.class,
                                 DeactivateWifiBoxWifi.class})
public class BoxWifiUnitTesterSILModel extends BoxWifiUnitTesterModel {
    
    // -------------------------------------------------------------------------
    // Constants and variables
    // -------------------------------------------------------------------------

    private static final long serialVersionUID = 1L;
    
    /** when true, leaves a trace of the execution of the model. */
    public static boolean VERBOSE = false;
    
    /** when true, leaves a debugging trace of the execution of the model. */
    public static boolean DEBUG = false;
    
    /** URI for an instance model; works as long as only one instance is created. */
    public static final String URI = BoxWifiUnitTesterSILModel.class.getSimpleName();

    public static final String	TEST_SCENARIO_RP_NAME = "TEST_SCENARIO";    
    // Commented out constants from original, kept for reference
//    /** URI for an instance model in MIL simulations; works as long as only one instance is created. */
//    public static final String MIL_URI = URI + "-MIL";
//    /** URI for an instance model in MIL real time simulations; works as long as only one instance is created. */
//    public static final String MIL_RT_URI = URI + "-MIL_RT";
//    /** URI for an instance model in SIL simulations; works as long as only one instance is created. */
//    public static final String SIL_URI = URI + "-SIL";
//    /** name of the run parameter used to provide the test scenario. */
//    public static final String TEST_SCENARIO_RP_NAME = "TEST_SCENARIO";

    // -------------------------------------------------------------------------
    // Constructors
    // -------------------------------------------------------------------------

    /**
     * create an atomic model with the given URI (if null, one will be
     * generated) and to be run by the given simulator using the given time unit
     * for its clock.
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
     * @param uri               unique identifier of the model.
     * @param simulatedTimeUnit time unit used for the simulation clock.
     * @param simulationEngine  simulation engine enacting the model.
     * @throws Exception 
     */
    public BoxWifiUnitTesterSILModel(
        String uri,
        TimeUnit simulatedTimeUnit,
        AtomicSimulatorI simulationEngine
    ) throws Exception {
        super(uri, simulatedTimeUnit, simulationEngine);

        if (VERBOSE || DEBUG) {
            // set the logger to a standard simulation logger
            this.getSimulationEngine().setLogger(new StandardLogger());
        }

        // Invariant checking
        assert BoxWifiUnitTesterSILModel.implementationInvariants(this) :
                new NeoSim4JavaException(
                        "Implementation Invariants violation: "
                        + "BoxWifiUnitTesterModel."
                        + "implementationInvariants(this)");
        assert BoxWifiUnitTesterSILModel.invariants(this) :
                new NeoSim4JavaException(
                        "Invariants violation: BoxWifiUnitTesterModel."
                        + "invariants(this)");
    }

    // -------------------------------------------------------------------------
    // Invariants
    // -------------------------------------------------------------------------

    /**
     * Check implementation invariants.
     */
    protected static boolean implementationInvariants(BoxWifiUnitTesterSILModel instance) {
        assert instance != null : new NeoSim4JavaException("Precondition violation: instance != null");
        
        boolean ret = true;
        ret &= instance.getURI() != null && !instance.getURI().isEmpty();
        ret &= instance.getSimulatedTimeUnit() != null;
        ret &= instance.getSimulationEngine() != null;
        return ret;
    }

    /**
     * Check static invariants.
     */
    public static boolean staticInvariants() {
        boolean ret = true;
        ret &= URI != null && !URI.isEmpty();
        ret &= BoxWifiUnitTesterModel.TEST_SCENARIO_RP_NAME != null && 
               !BoxWifiUnitTesterModel.TEST_SCENARIO_RP_NAME.isEmpty();
        return ret;
    }

    /**
     * Check all invariants.
     */
    protected static boolean invariants(BoxWifiUnitTesterSILModel instance) {
        assert instance != null : new NeoSim4JavaException("Precondition violation: instance != null");
        
        boolean ret = true;
        ret &= staticInvariants();
        ret &= implementationInvariants(instance);
        return ret;
    }

    // -------------------------------------------------------------------------
    // Methods
    // -------------------------------------------------------------------------

    /**
     * @see fr.sorbonne_u.devs_simulation.models.interfaces.ModelI#setSimulationRunParameters(java.util.Map)
     */
    @Override
    public void setSimulationRunParameters(
        Map<String, Object> simParams
    ) throws MissingRunParameterException {
        // this gets the reference on the owner component which is required
        // to have simulation models able to make the component perform some
        // operations or tasks or to get the value of variables held by the
        // component when necessary.
        if (simParams.containsKey(AtomicSimulatorPlugin.OWNER_RUNTIME_PARAMETER_NAME)) {
            // by the following, all of the logging will appear in the owner
            // component logger
            this.getSimulationEngine().setLogger(
                    AtomicSimulatorPlugin.createComponentLogger(simParams));
        }
        super.setSimulationRunParameters(simParams);
    }
}
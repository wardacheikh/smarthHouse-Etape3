package fr.sorbonne_u.components.hem2025e3.equipements.wifi.sil;


import java.util.ArrayList; 
import java.util.Map;
import java.util.concurrent.TimeUnit;

import fr.sorbonne_u.alasca.physical_data.MeasurementUnit;
import fr.sorbonne_u.components.cyphy.plugins.devs.AtomicSimulatorPlugin;
import fr.sorbonne_u.components.hem2025e1.equipement.Wifi.BoxWifiImplementationI.BoxWifiMode;
import fr.sorbonne_u.components.hem2025e1.equipments.meter.ElectricMeterImplementationI;
import fr.sorbonne_u.components.hem2025e2.GlobalReportI;
import fr.sorbonne_u.components.hem2025e2.equipments.BoxWifi.mil.events.AbstractBoxWifiEvent;
import fr.sorbonne_u.components.hem2025e2.equipments.BoxWifi.mil.events.ActivateWifiBoxWifi;
import fr.sorbonne_u.components.hem2025e2.equipments.BoxWifi.mil.events.DeactivateWifiBoxWifi;
import fr.sorbonne_u.components.hem2025e2.equipments.BoxWifi.mil.events.SwitchOffBoxWifi;
import fr.sorbonne_u.components.hem2025e2.equipments.BoxWifi.mil.events.SwitchOnBoxWifi;
import fr.sorbonne_u.components.hem2025e2.utils.Electricity;
import fr.sorbonne_u.components.hem2025e3.equipements.wifi.BoxWifiCyPhy;
import fr.sorbonne_u.devs_simulation.exceptions.MissingRunParameterException;
import fr.sorbonne_u.devs_simulation.exceptions.NeoSim4JavaException;
import fr.sorbonne_u.devs_simulation.hioa.annotations.ExportedVariable;
import fr.sorbonne_u.devs_simulation.hioa.annotations.ModelExportedVariable;
import fr.sorbonne_u.devs_simulation.hioa.models.AtomicHIOA;
import fr.sorbonne_u.devs_simulation.hioa.models.vars.Value;
import fr.sorbonne_u.devs_simulation.models.annotations.ModelExternalEvents;
import fr.sorbonne_u.devs_simulation.models.events.Event;
import fr.sorbonne_u.devs_simulation.models.events.EventI;
import fr.sorbonne_u.devs_simulation.models.interfaces.ModelI;
import fr.sorbonne_u.devs_simulation.models.time.Duration;
import fr.sorbonne_u.devs_simulation.models.time.Time;
import fr.sorbonne_u.devs_simulation.simulators.interfaces.AtomicSimulatorI;
import fr.sorbonne_u.devs_simulation.simulators.interfaces.SimulationReportI;
import fr.sorbonne_u.devs_simulation.utils.StandardLogger;
import fr.sorbonne_u.exceptions.AssertionChecking;

@ModelExternalEvents(imported = {SwitchOnBoxWifi.class, SwitchOffBoxWifi.class,
		ActivateWifiBoxWifi.class, DeactivateWifiBoxWifi.class})
@ModelExportedVariable(name = "currentIntensity", type = Double.class)
public class BoxWifiElectricitySILModel extends AtomicHIOA {

    private static final long serialVersionUID = 1L;
    
    /** when true, leaves a trace of the execution of the model. */
    public static boolean VERBOSE = true;
    
    /** when true, leaves a debugging trace of the execution of the model. */
    public static boolean DEBUG = false;

    /** URI for an instance model; works as long as only one instance is created. */
    public static final String URI = BoxWifiElectricitySILModel.class.getSimpleName();

    /** Current mode of the box wifi. */
    protected BoxWifiMode currentMode = BoxWifiCyPhy.INITIAL_MODE;
    
    /** true when the electricity consumption has changed after executing an external event. */
    protected boolean consumptionHasChanged = false;

    /** Power consumption in BOX_ONLY mode. */
    protected double boxOnlyConsumption;
    
    /** Power consumption in FULL_ON mode. */
    protected double fullOnConsumption;
    
    /** Tension. */
    protected double tension;

    /** Total consumption of the box wifi during the simulation in kwh. */
    protected double totalConsumption;

    // -------------------------------------------------------------------------
    // HIOA model variables
    // -------------------------------------------------------------------------

    /** Current intensity in the power unit defined by the electric meter. */
    @ExportedVariable(type = Double.class)
    protected final Value<Double> currentIntensity = new Value<Double>(this);

    // -------------------------------------------------------------------------
    // Invariants
    // -------------------------------------------------------------------------

    protected static boolean implementationInvariants(BoxWifiElectricitySILModel instance) {
        assert instance != null : new NeoSim4JavaException("Precondition violation: instance != null");

        boolean ret = true;
        ret &= AssertionChecking.checkImplementationInvariant(
                instance.boxOnlyConsumption > 0.0,
                BoxWifiElectricitySILModel.class,
                instance,
                "boxOnlyConsumption > 0.0");
        ret &= AssertionChecking.checkImplementationInvariant(
                instance.fullOnConsumption > instance.boxOnlyConsumption,
                BoxWifiElectricitySILModel.class,
                instance,
                "fullOnConsumption > boxOnlyConsumption");
        ret &= AssertionChecking.checkImplementationInvariant(
                instance.totalConsumption >= 0.0,
                BoxWifiElectricitySILModel.class,
                instance,
                "totalConsumption >= 0.0");
        ret &= AssertionChecking.checkImplementationInvariant(
                instance.currentMode != null,
                BoxWifiElectricitySILModel.class,
                instance,
                "currentMode != null");
        ret &= AssertionChecking.checkImplementationInvariant(
                !instance.currentIntensity.isInitialised() ||
                        instance.currentIntensity.getValue() >= 0.0,
                BoxWifiElectricitySILModel.class,
                instance,
                "!currentIntensity.isInitialised() || currentIntensity.getValue() >= 0.0");
        return ret;
    }

    public static boolean staticInvariants() {
        boolean ret = true;
        ret &= BoxWifiCyPhy.staticInvariants();
        ret &= AssertionChecking.checkStaticInvariant(
                URI != null && !URI.isEmpty(),
                BoxWifiElectricitySILModel.class,
                "URI != null && !URI.isEmpty()");
        ret &= AssertionChecking.checkStaticInvariant(
                BOX_ONLY_CONSUMPTION_RPNAME != null && !BOX_ONLY_CONSUMPTION_RPNAME.isEmpty(),
                BoxWifiElectricitySILModel.class,
                "BOX_ONLY_CONSUMPTION_RPNAME != null && !BOX_ONLY_CONSUMPTION_RPNAME.isEmpty()");
        ret &= AssertionChecking.checkStaticInvariant(
                FULL_ON_CONSUMPTION_RPNAME != null && !FULL_ON_CONSUMPTION_RPNAME.isEmpty(),
                BoxWifiElectricitySILModel.class,
                "FULL_ON_CONSUMPTION_RPNAME != null && !FULL_ON_CONSUMPTION_RPNAME.isEmpty()");
        ret &= AssertionChecking.checkStaticInvariant(
                TENSION_RPNAME != null && !TENSION_RPNAME.isEmpty(),
                BoxWifiElectricitySILModel.class,
                "TENSION_RPNAME != null && !TENSION_RPNAME.isEmpty()");
        return ret;
    }

    protected static boolean invariants(BoxWifiElectricitySILModel instance) {
        assert instance != null : new NeoSim4JavaException("Precondition violation: instance != null");

        boolean ret = true;
        ret &= staticInvariants();
        return ret;
    }

    // -------------------------------------------------------------------------
    // Constructors
    // -------------------------------------------------------------------------

    public BoxWifiElectricitySILModel(
        String uri,
        TimeUnit simulatedTimeUnit,
        AtomicSimulatorI simulationEngine
    ) throws Exception {
        super(uri, simulatedTimeUnit, simulationEngine);

        this.boxOnlyConsumption = BoxWifiCyPhy.BOX_ONLY_POWER.getData();
        this.fullOnConsumption = BoxWifiCyPhy.FULL_ON_POWER.getData();
        this.tension = BoxWifiCyPhy.TENSION.getData();

        if (VERBOSE || DEBUG) {
            this.getSimulationEngine().setLogger(new StandardLogger());
        }

        assert BoxWifiElectricitySILModel.implementationInvariants(this) :
                new NeoSim4JavaException("BoxWifiElectricitySILModel.implementationInvariants(this)");
        assert BoxWifiElectricitySILModel.invariants(this) :
                new NeoSim4JavaException("BoxWifiElectricitySILModel.invariants(this)");
    }

    // -------------------------------------------------------------------------
    // Methods for BoxWifi operations
    // -------------------------------------------------------------------------

    /**
     * Turn on the box (WiFi disabled).
     */
    public void turnOn() {
        if (this.currentMode == BoxWifiMode.OFF) {
            this.currentMode = BoxWifiMode.BOX_ONLY;
            this.toggleConsumptionHasChanged();
            if (VERBOSE) {
                this.logMessage("Box turned on (WiFi disabled)");
            }
        }
    }

    /**
     * Turn off the box.
     */
    public void turnOff() {
        if (this.currentMode != BoxWifiMode.OFF) {
            this.currentMode = BoxWifiMode.OFF;
            this.toggleConsumptionHasChanged();
            if (VERBOSE) {
                this.logMessage("Box turned off");
            }
        }
    }

    /**
     * Activate WiFi.
     */
    public void activateWifi() {
        if (this.currentMode == BoxWifiMode.BOX_ONLY) {
            this.currentMode = BoxWifiMode.FULL_ON;
            this.toggleConsumptionHasChanged();
            if (VERBOSE) {
                this.logMessage("WiFi activated");
            }
        } else if (this.currentMode == BoxWifiMode.OFF) {
            if (VERBOSE) {
                this.logMessage("Cannot activate WiFi: box is off");
            }
        }
    }

    /**
     * Deactivate WiFi.
     */
    public void deactivateWifi() {
        if (this.currentMode == BoxWifiMode.FULL_ON) {
            this.currentMode = BoxWifiMode.BOX_ONLY;
            this.toggleConsumptionHasChanged();
            if (VERBOSE) {
                this.logMessage("WiFi deactivated");
            }
        }
    }

    /**
     * Get the current mode.
     */
    public BoxWifiMode getCurrentMode() {
        return this.currentMode;
    }

    /**
     * Toggle the value of consumptionHasChanged.
     */
    public void toggleConsumptionHasChanged() {
        this.consumptionHasChanged = !this.consumptionHasChanged;
    }

    // -------------------------------------------------------------------------
    // DEVS simulation protocol
    // -------------------------------------------------------------------------

    @Override
    public void initialiseState(Time startTime) {
        super.initialiseState(startTime);

        this.currentMode = BoxWifiCyPhy.INITIAL_MODE;
        this.consumptionHasChanged = false;
        this.totalConsumption = 0.0;

        if (VERBOSE) {
            this.logMessage("Box WiFi electricity simulation begins. Initial mode: " + this.currentMode);
        }

        assert BoxWifiElectricitySILModel.implementationInvariants(this) :
                new NeoSim4JavaException("BoxWifiElectricitySILModel.implementationInvariants(this)");
        assert BoxWifiElectricitySILModel.invariants(this) :
                new NeoSim4JavaException("BoxWifiElectricitySILModel.invariants(this)");
    }

    @Override
    public void initialiseVariables() {
        super.initialiseVariables();

        this.currentIntensity.initialise(0.0);

        assert BoxWifiElectricitySILModel.implementationInvariants(this) :
                new NeoSim4JavaException("BoxWifiElectricitySILModel.implementationInvariants(this)");
        assert BoxWifiElectricitySILModel.invariants(this) :
                new NeoSim4JavaException("BoxWifiElectricitySILModel.invariants(this)");
    }

    @Override
    public ArrayList<EventI> output() {
        return null; // This model does not export events
    }

    @Override
    public Duration timeAdvance() {
        Duration ret;
        if (this.consumptionHasChanged) {
            this.toggleConsumptionHasChanged();
            ret = new Duration(0.0, this.getSimulatedTimeUnit());
        } else {
            ret = Duration.INFINITY;
        }

        assert BoxWifiElectricitySILModel.implementationInvariants(this) :
                new NeoSim4JavaException("BoxWifiElectricitySILModel.implementationInvariants(this)");
        assert BoxWifiElectricitySILModel.invariants(this) :
                new NeoSim4JavaException("BoxWifiElectricitySILModel.invariants(this)");

        return ret;
    }

    @Override
    public void userDefinedInternalTransition(Duration elapsedTime) {
        super.userDefinedInternalTransition(elapsedTime);

        Time t = this.getCurrentStateTime();
        double newIntensity = 0.0;

        switch (this.currentMode) {
            case OFF:
                newIntensity = 0.0;
                break;
            case BOX_ONLY:
                newIntensity = this.boxOnlyConsumption / this.tension;
                break;
            case FULL_ON:
                newIntensity = this.fullOnConsumption / this.tension;
                break;
        }

        this.currentIntensity.setNewValue(newIntensity, t);

        if (VERBOSE) {
            StringBuilder message = new StringBuilder("Executes internal transition ");
            message.append("with current consumption ");
            message.append(this.currentIntensity.getValue());
            message.append(" A ");
            message.append("(").append(this.currentIntensity.getValue() * this.tension).append(" W)");
            message.append(" at ");
            message.append(this.currentIntensity.getTime());
            message.append(" [Mode: ").append(this.currentMode).append("]");
            this.logMessage(message.toString());
        }

        assert BoxWifiElectricitySILModel.implementationInvariants(this) :
                new NeoSim4JavaException("BoxWifiElectricitySILModel.implementationInvariants(this)");
        assert BoxWifiElectricitySILModel.invariants(this) :
                new NeoSim4JavaException("BoxWifiElectricitySILModel.invariants(this)");
    }

    @Override
    public void userDefinedExternalTransition(Duration elapsedTime) {
        super.userDefinedExternalTransition(elapsedTime);

        ArrayList<EventI> currentEvents = this.getStoredEventAndReset();
        
        if (currentEvents != null && !currentEvents.isEmpty()) {
            EventI ce = currentEvents.get(0);

            // Compute total consumption for simulation report
            if (ElectricMeterImplementationI.POWER_UNIT.equals(MeasurementUnit.WATTS)) {
                this.totalConsumption += Electricity.computeConsumption(
                        elapsedTime,
                        this.currentIntensity.getValue() * this.tension);
            } else {
                this.totalConsumption += Electricity.computeConsumption(
                        elapsedTime,
                        this.tension * this.currentIntensity.getValue());
            }

            if (VERBOSE) {
                StringBuilder message = new StringBuilder("Executes external transition ");
                message.append("(").append(ce.getClass().getSimpleName()).append(")");
                message.append(" [Previous mode: ").append(this.currentMode).append("]");
                this.logMessage(message.toString());
            }

            // Gérer directement les événements par leur type
            String eventClassName = ce.getClass().getSimpleName();
            
            switch (eventClassName) {
                case "SwitchOnBoxWifi":
                    this.turnOn();
                    break;
                case "SwitchOffBoxWifi":
                    this.turnOff();
                    break;
                case "ActivateWifiBoxWifi":
                    this.activateWifi();
                    break;
                case "DeactivateWifiBoxWifi":
                    this.deactivateWifi();
                    break;
                default:
                    throw new RuntimeException("Unknown event: " + eventClassName);
            }

            if (VERBOSE) {
                this.logMessage("New mode after external transition: " + this.currentMode);
            }
        }

        assert BoxWifiElectricitySILModel.implementationInvariants(this) :
                new NeoSim4JavaException("BoxWifiElectricitySILModel.implementationInvariants(this)");
        assert BoxWifiElectricitySILModel.invariants(this) :
                new NeoSim4JavaException("BoxWifiElectricitySILModel.invariants(this)");
    }
    
    @Override
    public void endSimulation(Time endTime) {
        Duration d = endTime.subtract(this.getCurrentStateTime());
        if (ElectricMeterImplementationI.POWER_UNIT.equals(MeasurementUnit.WATTS)) {
            this.totalConsumption += Electricity.computeConsumption(
                    d,
                    this.currentIntensity.getValue() * this.tension);
        } else {
            this.totalConsumption += Electricity.computeConsumption(
                    d,
                    this.tension * this.currentIntensity.getValue());
        }

        if (VERBOSE) {
            this.logMessage("Box WiFi electricity simulation ends.");
            this.logMessage("Final mode: " + this.currentMode);
            this.logMessage("Final consumption: " + this.currentIntensity.getValue() + " A");
            this.logMessage("Total consumption: " + this.totalConsumption + " kWh");
        }
        
        super.endSimulation(endTime);
    }

    // -------------------------------------------------------------------------
    // Optional DEVS simulation protocol: simulation run parameters
    // -------------------------------------------------------------------------

    /** Run parameter name for BOX_ONLY_CONSUMPTION. */
    public static final String BOX_ONLY_CONSUMPTION_RPNAME = "BOX_ONLY_CONSUMPTION";
    
    /** Run parameter name for FULL_ON_CONSUMPTION. */
    public static final String FULL_ON_CONSUMPTION_RPNAME = "FULL_ON_CONSUMPTION";
    
    /** Run parameter name for TENSION. */
    public static final String TENSION_RPNAME = "TENSION";

    @Override
    public void setSimulationRunParameters(Map<String, Object> simParams) throws MissingRunParameterException {
        super.setSimulationRunParameters(simParams);

        String boxOnlyName = ModelI.createRunParameterName(this.getURI(), BOX_ONLY_CONSUMPTION_RPNAME);
        if (simParams.containsKey(boxOnlyName)) {
            this.boxOnlyConsumption = (double) simParams.get(boxOnlyName);
        }
        
        String fullOnName = ModelI.createRunParameterName(this.getURI(), FULL_ON_CONSUMPTION_RPNAME);
        if (simParams.containsKey(fullOnName)) {
            this.fullOnConsumption = (double) simParams.get(fullOnName);
        }
        
        String tensionName = ModelI.createRunParameterName(getURI(), TENSION_RPNAME);
        if (simParams.containsKey(tensionName)) {
            this.tension = (double) simParams.get(tensionName);
        }

        if (simParams.containsKey(AtomicSimulatorPlugin.OWNER_RUNTIME_PARAMETER_NAME)) {
            this.getSimulationEngine().setLogger(AtomicSimulatorPlugin.createComponentLogger(simParams));
        }

        if (VERBOSE) {
            this.logMessage("Run parameters set:");
            this.logMessage("  Box only consumption: " + this.boxOnlyConsumption + " W");
            this.logMessage("  Full on consumption: " + this.fullOnConsumption + " W");
            this.logMessage("  Tension: " + this.tension + " V");
        }

        assert BoxWifiElectricitySILModel.implementationInvariants(this) :
                new NeoSim4JavaException("BoxWifiElectricitySILModel.implementationInvariants(this)");
        assert BoxWifiElectricitySILModel.invariants(this) :
                new NeoSim4JavaException("BoxWifiElectricitySILModel.invariants(this)");
    }

    // -------------------------------------------------------------------------
    // Optional DEVS simulation protocol: simulation report
    // -------------------------------------------------------------------------

    public static class BoxWifiElectricityReport implements SimulationReportI, GlobalReportI {
        private static final long serialVersionUID = 1L;
        protected String modelURI;
        protected double totalConsumption; // in kwh
        protected BoxWifiMode finalMode;

        public BoxWifiElectricityReport(String modelURI, double totalConsumption, BoxWifiMode finalMode) {
            super();
            this.modelURI = modelURI;
            this.totalConsumption = totalConsumption;
            this.finalMode = finalMode;
        }

        @Override
        public String getModelURI() {
            return this.modelURI;
        }

        @Override
        public String printout(String indent) {
            StringBuilder ret = new StringBuilder(indent);
            ret.append("---\n");
            ret.append(indent).append('|').append(this.modelURI).append(" report\n");
            ret.append(indent).append('|').append("Final mode: ").append(this.finalMode).append("\n");
            ret.append(indent).append('|').append("Total consumption: ").append(this.totalConsumption).append(" kWh\n");
            ret.append(indent).append("---\n");
            return ret.toString();
        }

        @Override
        public String toString() {
            return this.printout("");
        }
    }

    @Override
    public SimulationReportI getFinalReport() {
        return new BoxWifiElectricityReport(this.getURI(), this.totalConsumption, this.currentMode);
    }
    
    // -------------------------------------------------------------------------
    // Additional helper methods
    // -------------------------------------------------------------------------
    
    /**
     * Get the current power consumption in watts.
     */
    public double getCurrentPowerInWatts() {
        return this.currentIntensity.getValue() * this.tension;
    }
    
    /**
     * Get the current intensity in amperes.
     */
    public double getCurrentIntensity() {
        return this.currentIntensity.getValue();
    }
    
    /**
     * Check if the box is currently consuming power.
     */
    public boolean isConsumingPower() {
        return this.currentMode != BoxWifiMode.OFF;
    }
}
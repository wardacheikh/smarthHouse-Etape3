package fr.sorbonne_u.components.hem2025e3.equipements.wifi.sil;

import java.util.ArrayList;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import fr.sorbonne_u.components.cyphy.plugins.devs.AtomicSimulatorPlugin;
import fr.sorbonne_u.components.hem2025e1.equipement.Wifi.BoxWifiImplementationI.BoxWifiMode;
import fr.sorbonne_u.components.hem2025e2.equipments.BoxWifi.mil.events.AbstractBoxWifiEvent;
import fr.sorbonne_u.components.hem2025e2.equipments.BoxWifi.mil.events.ActivateWifiBoxWifi;
import fr.sorbonne_u.components.hem2025e2.equipments.BoxWifi.mil.events.DeactivateWifiBoxWifi;
import fr.sorbonne_u.components.hem2025e2.equipments.BoxWifi.mil.events.SwitchOffBoxWifi;
import fr.sorbonne_u.components.hem2025e2.equipments.BoxWifi.mil.events.SwitchOnBoxWifi;
import fr.sorbonne_u.components.hem2025e3.equipements.wifi.BoxWifiCyPhy;
import fr.sorbonne_u.devs_simulation.exceptions.MissingRunParameterException;
import fr.sorbonne_u.devs_simulation.models.AtomicModel;
import fr.sorbonne_u.devs_simulation.models.annotations.ModelExternalEvents;
import fr.sorbonne_u.devs_simulation.models.events.EventI;
import fr.sorbonne_u.devs_simulation.models.time.Duration;
import fr.sorbonne_u.devs_simulation.models.time.Time;
import fr.sorbonne_u.devs_simulation.simulators.interfaces.AtomicSimulatorI;
import fr.sorbonne_u.devs_simulation.utils.StandardLogger;

@ModelExternalEvents(
    imported = {SwitchOnBoxWifi.class, SwitchOffBoxWifi.class,
    		ActivateWifiBoxWifi.class, DeactivateWifiBoxWifi.class},
    exported = {SwitchOnBoxWifi.class, SwitchOffBoxWifi.class,
    		ActivateWifiBoxWifi.class, DeactivateWifiBoxWifi.class}
)
public class BoxWifiStateSILModel extends AtomicModel {
	



	private static final long serialVersionUID = 1L;
    
    /** when true, leaves a trace of the execution of the model. */
    public static boolean VERBOSE = true;
    
    /** when true, leaves a debugging trace of the execution of the model. */
    public static boolean DEBUG = false;

    /** URI for an instance model; works as long as only one instance is created. */
    public static final String URI = BoxWifiStateSILModel.class.getSimpleName();


    /** Current mode of the box WiFi. */
    protected BoxWifiMode currentMode = BoxWifiCyPhy.INITIAL_MODE;
    
    /** Last received event or null if none. */
    protected AbstractBoxWifiEvent lastReceived;

    // -------------------------------------------------------------------------
    // Constructors
    // -------------------------------------------------------------------------

    public BoxWifiStateSILModel(
      String uri,
        TimeUnit simulatedTimeUnit,
        AtomicSimulatorI simulationEngine
    ) throws Exception {
        super(uri, simulatedTimeUnit, simulationEngine);

        if (VERBOSE || DEBUG) {
            // set the logger to a standard simulation logger
            this.getSimulationEngine().setLogger(new StandardLogger());
        }
        System.out.println("=== CONSTRUCTEUR BoxWifiStateSILModel ===");
        System.out.println("URI: " + uri);
        System.out.println("simulatedTimeUnit: " + simulatedTimeUnit);
        System.out.println("simulationEngine: " + (simulationEngine != null ? "non null" : "null"));
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

    // -------------------------------------------------------------------------
    // DEVS simulation protocol
    // -------------------------------------------------------------------------

    @Override
    public void initialiseState(Time initialTime) {
        super.initialiseState(initialTime);

        this.lastReceived = null;
        this.currentMode = BoxWifiCyPhy.INITIAL_MODE;

        // tracing
        if (VERBOSE) {
            this.logMessage("Box WiFi simulation begins. Initial mode: " + this.currentMode);
        }
    }

    
    @Override
    public ArrayList<EventI> output() {
        assert this.lastReceived != null;
        
        ArrayList<EventI> ret = new ArrayList<EventI>();
        ret.add(this.lastReceived);  // ← Retourner l'événement directement
        this.lastReceived = null;
        return ret;
    }

    @Override
    public Duration timeAdvance() {
        if (this.lastReceived != null) {
            // trigger an immediate internal transition
            return Duration.zero(this.getSimulatedTimeUnit());
        } else {
            // wait until the next external event
            return Duration.INFINITY;
        }
    }

    @Override
    public void userDefinedExternalTransition(Duration elapsedTime) {
        super.userDefinedExternalTransition(elapsedTime);

        // get the vector of current external events
        ArrayList<EventI> currentEvents = this.getStoredEventAndReset();
        
        if (currentEvents != null && !currentEvents.isEmpty()) {
            // For the box wifi model, there will be exactly one event by construction
            this.lastReceived = (AbstractBoxWifiEvent) currentEvents.get(0);

            // Execute the corresponding operation based on the event type
            if (this.lastReceived instanceof SwitchOnBoxWifi) {
                this.turnOn();
            } else if (this.lastReceived instanceof SwitchOffBoxWifi) {
                this.turnOff();
            } else if (this.lastReceived instanceof ActivateWifiBoxWifi) {
                this.activateWifi();
            } else if (this.lastReceived instanceof DeactivateWifiBoxWifi) {
                this.deactivateWifi();
            }

            // tracing
            if (VERBOSE) {
                StringBuilder message = new StringBuilder(this.uri);
                message.append(" executes external event: ");
                message.append(this.lastReceived.getClass().getSimpleName());
                message.append(" -> New mode: ");
                message.append(this.currentMode);
                this.logMessage(message.toString());
            }
        }
    }

    @Override
    public void userDefinedInternalTransition(Duration elapsedTime) {
        super.userDefinedInternalTransition(elapsedTime);

        
        // After output, reset lastReceived (already done in output() method)
        // This transition doesn't change the state, just prepares for next event
        
        if (VERBOSE && this.lastReceived == null) {
            this.logMessage("Internal transition: ready for next event");
        }   
        this.lastReceived = null; // Reset après output

    }

    @Override
    public void endSimulation(Time endTime) {
        // tracing
        if (VERBOSE) {
            this.logMessage("Box WiFi simulation ends. Final mode: " + this.currentMode);
        }
        super.endSimulation(endTime);
    }

    // -------------------------------------------------------------------------
    // Optional DEVS simulation protocol: simulation run parameters
    // -------------------------------------------------------------------------

    @Override
    public void setSimulationRunParameters(
        Map<String, Object> simParams
    ) throws MissingRunParameterException {
        super.setSimulationRunParameters(simParams);

        // Get the reference on the owner component
        if (simParams.containsKey(AtomicSimulatorPlugin.OWNER_RUNTIME_PARAMETER_NAME)) {
            // All logging will appear in the owner component logger
            this.getSimulationEngine().setLogger(
                    AtomicSimulatorPlugin.createComponentLogger(simParams));
        }
        
        // You can also get other simulation parameters if needed
        if (VERBOSE && simParams != null) {
            this.logMessage("Simulation parameters set: " + simParams.keySet());
        }
    }
    
    // -------------------------------------------------------------------------
    // Additional helper methods
    // -------------------------------------------------------------------------
    
    /**
     * Check if the box is currently on.
     */
    public boolean isBoxOn() {
        return this.currentMode != BoxWifiMode.OFF;
    }
    
    /**
     * Check if WiFi is currently active.
     */
    public boolean isWifiActive() {
        return this.currentMode == BoxWifiMode.FULL_ON;
    }
    
    /**
     * Get the current power consumption based on the mode.
     */
    public double getCurrentPowerConsumption() {
        switch (this.currentMode) {
            case OFF:
                return 0.0;
            case BOX_ONLY:
                return BoxWifiCyPhy.BOX_ONLY_POWER.getData();
            case FULL_ON:
                return BoxWifiCyPhy.FULL_ON_POWER.getData();
            default:
                return 0.0;
        }
    }
}
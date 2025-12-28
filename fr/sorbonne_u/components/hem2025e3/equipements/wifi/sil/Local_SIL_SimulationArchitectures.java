package fr.sorbonne_u.components.hem2025e3.equipements.wifi.sil;


import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import fr.sorbonne_u.components.hem2025e2.equipments.BoxWifi.mil.BoxWifiCoupledModel;
import fr.sorbonne_u.components.hem2025e2.equipments.BoxWifi.mil.events.ActivateWifiBoxWifi;
import fr.sorbonne_u.components.hem2025e2.equipments.BoxWifi.mil.events.DeactivateWifiBoxWifi;
import fr.sorbonne_u.components.hem2025e2.equipments.BoxWifi.mil.events.SwitchOffBoxWifi;
import fr.sorbonne_u.components.hem2025e2.equipments.BoxWifi.mil.events.SwitchOnBoxWifi;
import fr.sorbonne_u.devs_simulation.architectures.RTArchitecture;
import fr.sorbonne_u.devs_simulation.hioa.architectures.RTAtomicHIOA_Descriptor;
import fr.sorbonne_u.devs_simulation.models.architectures.AbstractAtomicModelDescriptor;
import fr.sorbonne_u.devs_simulation.models.architectures.CoupledModelDescriptor;
import fr.sorbonne_u.devs_simulation.models.architectures.RTAtomicModelDescriptor;
import fr.sorbonne_u.devs_simulation.models.architectures.RTCoupledModelDescriptor;
import fr.sorbonne_u.devs_simulation.models.events.EventSink;
import fr.sorbonne_u.devs_simulation.models.events.EventSource;
import fr.sorbonne_u.exceptions.PreconditionException;

/**
 * The class <code>Local_SIL_SimulationArchitectures</code> defines the local
 * software-in-the-loop simulation architectures pertaining to the BoxWifi appliance.
 *
 * <p><strong>Description</strong></p>
 * 
 * <p>
 * The class provides static methods that create the local software-in-the-loop
 * real time simulation architectures for the component {@code BoxWifiCyPhy}.
 * </p>
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
public abstract class Local_SIL_SimulationArchitectures {
    
    /**
     * Create the local software-in-the-loop real time simulation architecture
     * for the {@code BoxWifiCyPhy} component when performing SIL unit tests.
     * 
     * <p><strong>Description</strong></p>
     * 
     * <p>
     * In BoxWifi unit tests, the component architecture has two
     * components: {@code BoxWifiCyPhy} and {@code BoxWifiUnitTesterCyPhy}.
     * The local simulation architecture for the {@code BoxWifiCyPhy}
     * component contains two atomic simulation models:
     * </p>
     * <ol>
     * <li>The <code>BoxWifiStateSILModel</code> keeps track of the state
     *   (turned on, turned off, WiFi activated, etc.) of the box wifi. 
     *   The state changes are triggered by the reception of external events 
     *   that it imports; whenever a state change occurs, the triggering event 
     *   is emitted towards the <code>BoxWifiElectricitySILModel</code>, 
     *   hence it also exports them.
     * <li>The <code>BoxWifiElectricitySILModel</code> keeps track of the
     *   electric power consumed by the box wifi in a variable
     *   <code>currentIntensity</code> which is exported but not used in this
     *   simulation of the box wifi in isolation. It changes this power
     *   consumption upon the reception of box wifi events, which it
     *   imports.</li>
     * </ol>
     * <p>
     * The coupled model <code>BoxWifiCoupledModel</code> composes the two
     * atomic models so that the exported events emitted by the
     * <code>BoxWifiStateModel</code> are received by the
     * <code>BoxWifiElectricitySILModel</code> that imports them.
     * </p>
     * 
     * <p><strong>Contract</strong></p>
     * 
     * <pre>
     * pre	{@code architectureURI != null && !architectureURI.isEmpty()}
     * pre	{@code rootModelURI != null && !rootModelURI.isEmpty()}
     * pre	{@code simulatedTimeUnit != null}
     * pre	{@code accelerationFactor > 0.0}
     * post	{@code return != null}
     * post {@code return.getArchitectureURI().equals(architectureURI)}
     * post	{@code return.getRootModelURI().equals(rootModelURI)}
     * post	{@code return.getSimulationTimeUnit().equals(simulatedTimeUnit)}
     * </pre>
     *
     * @param architectureURI       URI to be given to the created simulation architecture.
     * @param rootModelURI          URI of the root model in the simulation architecture.
     * @param simulatedTimeUnit     simulated time unit used in the architecture.
     * @param accelerationFactor    acceleration factor used to execute in a logical time speeding up the real time.
     * @return                      the local software-in-the-loop real time simulation architecture for the unit test of the {@code BoxWifiCyPhy} component.
     * @throws Exception            <i>to do</i>.
     */
    public static RTArchitecture createBoxWifiSIL_Architecture4UnitTest(
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

        // Map that will contain the atomic model descriptors to construct
        // the simulation architecture
        Map<String, AbstractAtomicModelDescriptor> atomicModelDescriptors = new HashMap<>();

        // The box wifi model simulating its electricity consumption, an
        // atomic HIOA model hence we use an AtomicHIOA_Descriptor
        atomicModelDescriptors.put(
                BoxWifiElectricitySILModel.URI,
                RTAtomicHIOA_Descriptor.create(
                        BoxWifiElectricitySILModel.class,
                        BoxWifiElectricitySILModel.URI,
                        simulatedTimeUnit,
                        null,
                        accelerationFactor));
        
        // For atomic models, we use an AtomicModelDescriptor
        atomicModelDescriptors.put(
                BoxWifiStateSILModel.URI,
                RTAtomicModelDescriptor.create(
                        BoxWifiStateSILModel.class,
                        BoxWifiStateSILModel.URI,
                        simulatedTimeUnit,
                        null,
                        accelerationFactor));

        // Map that will contain the coupled model descriptors to construct
        // the simulation architecture
        Map<String, CoupledModelDescriptor> coupledModelDescriptors = new HashMap<>();

        // The set of submodels of the coupled model, given by their URIs
        Set<String> submodels = new HashSet<String>();
        submodels.add(BoxWifiElectricitySILModel.URI);
        submodels.add(BoxWifiStateSILModel.URI);

        // Event exchanging connections between exporting and importing models
        Map<EventSource, EventSink[]> connections = new HashMap<EventSource, EventSink[]>();

        // Connect TurnOnBoxWifi events
        connections.put(
            new EventSource(BoxWifiStateSILModel.URI, SwitchOnBoxWifi.class),
            new EventSink[] {
                new EventSink(BoxWifiElectricitySILModel.URI, SwitchOnBoxWifi.class)
            });
        
        // Connect TurnOffBoxWifi events
        connections.put(
            new EventSource(BoxWifiStateSILModel.URI, SwitchOffBoxWifi.class),
            new EventSink[] {
                new EventSink(BoxWifiElectricitySILModel.URI, SwitchOffBoxWifi.class)
            });
        
        // Connect ActivateWifi events
        connections.put(
            new EventSource(BoxWifiStateSILModel.URI, ActivateWifiBoxWifi.class),
            new EventSink[] {
                new EventSink(BoxWifiElectricitySILModel.URI, ActivateWifiBoxWifi.class)
            });
        
        // Connect DeactivateWifi events
        connections.put(
            new EventSource(BoxWifiStateSILModel.URI, DeactivateWifiBoxWifi.class),
            new EventSink[] {
                new EventSink(BoxWifiElectricitySILModel.URI, DeactivateWifiBoxWifi.class)
            });

        // Coupled model descriptor
        coupledModelDescriptors.put(
                rootModelURI,
                new RTCoupledModelDescriptor(
                        BoxWifiCoupledModel.class,
                        rootModelURI,
                        submodels,
                        null,
                        null,
                        connections,
                        null,
                        accelerationFactor));

        // Simulation architecture
        RTArchitecture architecture =
                new RTArchitecture(
                        architectureURI,
                        rootModelURI,
                        atomicModelDescriptors,
                        coupledModelDescriptors,
                        simulatedTimeUnit,
                        accelerationFactor);

        return architecture;
    }

    /**
     * Create the local software-in-the-loop real time simulation architecture
     * for the {@code BoxWifiCyPhy} component for integration tests.
     * 
     * <p><strong>Description</strong></p>
     * 
     * <p>
     * In integration tests under software-in-the-loop simulation, the
     * {@code BoxWifiCyPhy} component defines only one atomic model, the
     * {@code BoxWifiStateSILModel}, that makes the state and mode changes for
     * the box wifi simulator when receiving events from the
     * {@code BoxWifiCyPhy} component methods in SIL simulations, to turn on,
     * turn off, activate WiFi or deactivate WiFi. Hence, the created
     * architecture contains this sole atomic model.
     * </p>
     * <p>
     * In integration tests, the {@code BoxWifiElectricitySILModel}, to which
     * the {@code BoxWifiStateModel} resends the events to make it keep track
     * of the corresponding electric power consumption changes, is located in
     * the {@code ElectricMeter} component simulator. Hence, the events will
     * be reexported by the local simulator to the local simulator of
     * the {@code ElectricMeter} component that will have them received by
     * the {@code BoxWifiElectricitySILModel}.
     * </p>
     * 
     * <p><strong>Contract</strong></p>
     * 
     * <pre>
     * pre	{@code architectureURI != null && !architectureURI.isEmpty()}
     * pre	{@code rootModelURI != null && !rootModelURI.isEmpty()}
     * pre	{@code simulatedTimeUnit != null}
     * pre	{@code accelerationFactor > 0.0}
     * post	{@code return != null}
     * post {@code return.getArchitectureURI().equals(architectureURI)}
     * post	{@code return.getRootModelURI().equals(rootModelURI)}
     * post	{@code return.getSimulationTimeUnit().equals(simulatedTimeUnit)}
     * </pre>
     *
     * @param architectureURI       URI to be given to the created simulation architecture.
     * @param rootModelURI          URI of the root model in the simulation architecture.
     * @param simulatedTimeUnit     simulated time unit used in the architecture.
     * @param accelerationFactor    acceleration factor used in this run.
     * @return                      the local software-in-the-loop real time simulation architecture for the integration test of the {@code BoxWifiCyPhy} component.
     * @throws Exception            <i>to do</i>.
     */
    public static RTArchitecture createBoxWifiSIL_Architecture4IntegrationTest(
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

        // Map that will contain the atomic model descriptors to construct
        // the simulation architecture
        Map<String, AbstractAtomicModelDescriptor> atomicModelDescriptors = new HashMap<>();

        // Only the state model is included in the component for integration tests
        atomicModelDescriptors.put(
                rootModelURI,
                RTAtomicModelDescriptor.create(
                        BoxWifiStateSILModel.class,
                        rootModelURI,
                        simulatedTimeUnit,
                        null,
                        accelerationFactor));

        // Map that will contain the coupled model descriptors to construct
        // the simulation architecture
        Map<String, CoupledModelDescriptor> coupledModelDescriptors = new HashMap<>();

        // Simulation architecture
        RTArchitecture architecture =
                new RTArchitecture(
                        architectureURI,
                        rootModelURI,
                        atomicModelDescriptors,
                        coupledModelDescriptors,
                        simulatedTimeUnit,
                        accelerationFactor);

        return architecture;
    }
    
    /**
     * Create a simple local architecture for basic tests (optional).
     * This can be used for quick debugging without the electricity model.
     */
    public static RTArchitecture createSimpleBoxWifiSIL_Architecture(
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

        // Map that will contain the atomic model descriptors
        Map<String, AbstractAtomicModelDescriptor> atomicModelDescriptors = new HashMap<>();

        // Only the state model
        atomicModelDescriptors.put(
                rootModelURI,
                RTAtomicModelDescriptor.create(
                        BoxWifiStateSILModel.class,
                        rootModelURI,
                        simulatedTimeUnit,
                        null,
                        accelerationFactor));

        // Empty coupled model descriptors
        Map<String, CoupledModelDescriptor> coupledModelDescriptors = new HashMap<>();

        // Simulation architecture
        RTArchitecture architecture =
                new RTArchitecture(
                        architectureURI,
                        rootModelURI,
                        atomicModelDescriptors,
                        coupledModelDescriptors,
                        simulatedTimeUnit,
                        accelerationFactor);

        return architecture;
    }
}
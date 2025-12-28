package fr.sorbonne_u.components.hem2025e3.equipments.hairdryer.sil;

// Copyright Jacques Malenfant, Sorbonne Universite.
// Jacques.Malenfant@lip6.fr
//
// This software is a computer program whose purpose is to provide a
// basic component programming model to program with components
// real time distributed applications in the Java programming language.
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

import java.util.HashMap; 
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import fr.sorbonne_u.components.hem2025e2.equipments.hairdryer.mil.HairDryerCoupledModel;
import fr.sorbonne_u.components.hem2025e2.equipments.hairdryer.mil.events.SetHighHairDryer;
import fr.sorbonne_u.components.hem2025e2.equipments.hairdryer.mil.events.SetLowHairDryer;
import fr.sorbonne_u.components.hem2025e2.equipments.hairdryer.mil.events.SwitchOffHairDryer;
import fr.sorbonne_u.components.hem2025e2.equipments.hairdryer.mil.events.SwitchOnHairDryer;
import fr.sorbonne_u.devs_simulation.architectures.RTArchitecture;
import fr.sorbonne_u.devs_simulation.hioa.architectures.RTAtomicHIOA_Descriptor;
import fr.sorbonne_u.devs_simulation.models.architectures.AbstractAtomicModelDescriptor;
import fr.sorbonne_u.devs_simulation.models.architectures.CoupledModelDescriptor;
import fr.sorbonne_u.devs_simulation.models.architectures.RTAtomicModelDescriptor;
import fr.sorbonne_u.devs_simulation.models.architectures.RTCoupledModelDescriptor;
import fr.sorbonne_u.devs_simulation.models.events.EventSink;
import fr.sorbonne_u.devs_simulation.models.events.EventSource;
import fr.sorbonne_u.exceptions.PreconditionException;

// -----------------------------------------------------------------------------
/**
 * The class <code>Local_SIL_SimulationArchitectures</code> defines the local
 * software-in-the-loop simulation architectures pertaining to the hair dryer
 * appliance.
 *
 * <p><strong>Description</strong></p>
 * 
 * <p>
 * The class provides static methods that create the local software-in-the-loop
 * real time simulation architectures for the component {@code HairDryerCyPhy}.
 * The overall simulation architecture for the hair dryer can be seen as
 * follows:
 * </p>
 * <p><img src="../../../../../../../../images/hem-2025-e3/HairDryerMILModel.png"/></p> 
 * <p>
 * The simulation architectures created in this class are local to components
 * in the sense that they define the simulators that are created and run by
 * each component. The one for unit test is meant to be executed alone in the
 * {@code HairDryerCyPhy} component. The one for integration test is meant to
 * be executed within a larger simulator for the entire application component
 * architectures where they are seen as atomic models to be composed by a
 * coupled model that will reside in a coordinator component.
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
 * <p>Created on : 2023-11-13</p>
 * 
 * @author	<a href="mailto:Jacques.Malenfant@lip6.fr">Jacques Malenfant</a>
 */
public abstract class	Local_SIL_SimulationArchitectures
{
	/**
	 * create the local software-in-the-loop real time simulation architecture
	 * for the {@code HairDryerCyPhy} component when performing SIL unit tests.
	 * 
	 * <p><strong>Description</strong></p>
	 * 
	 * <p>
	 * In hair dryer unit tests, the component architecture has two
	 * components: {@code HairDryerCyPhy} and {@code HairDryerUnitTesterCyPhy}.
	 * The local simulation architecture for the {@code HairDryerCyPhy}
	 * component is as follows:
	 * </p>
	 * <p><img src="../../../../../../../../images/hem-2025-e3/HairDryerUnitTestLocalArchitecture.png"/></p>
	 * <p>
	 * There are two atomic simulation models: 
	 * </p>
	 * <ol>
	 * <li>The <code>HairDryerStateSILModel</code> keeps track of the state
	 *   (switched on, switched off, etc.) of the hair dryer. The state changes
	 *   are triggered by the reception of external events that it imports;
	 *   whenever a state change occurs, the triggering event is emitted towards
	 *   the <code>HairDryerElectricitySILModel</code></li>, hence it also
	 *   exports them.
	 * <li>The <code>HairDryerElectricitySILModel</code> keeps track of the
	 *   electric power consumed by the hair dryer in a variable
	 *   <code>currentIntensity</code> which is exported but not used in this
	 *   simulation of the hair dryer in isolation. It changes this power
	 *   consumption upon the reception of hair dryer events, which it
	 *   imports.</li>
	 * </ol>
	 * <p>
	 * The coupled model <code>HairDryerCoupledModel</code> composes the two
	 * atomic models so that the exported events emitted by the
	 * <code>HairDryerStateModel</code> are received by the
	 * <code>HairDryerElectricitySILModel</code> that imports them.
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
	 * @param architectureURI		URI to be given to the created simulation architecture.
	 * @param rootModelURI			URI of the root model in the simulation architecture.
	 * @param simulatedTimeUnit		simulated time unit used in the architecture.
	 * @param accelerationFactor	acceleration factor used to execute in a logical time speeding up the real time.
	 * @return						the local software-in-the-loop real time simulation architecture for the unit test of the {@code HairDryerCyPhy} component.
	 * @throws Exception			<i>to do</i>.
	 */
	public static RTArchitecture	createHairDryerSIL_Architecture4UnitTest(
		String architectureURI,
		String rootModelURI,
		TimeUnit simulatedTimeUnit,
		double accelerationFactor
		) throws Exception
	{
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

		// map that will contain the atomic model descriptors to construct
		// the simulation architecture
		Map<String,AbstractAtomicModelDescriptor> atomicModelDescriptors =
															new HashMap<>();

		// the hair dyer model simulating its electricity consumption, an
		// atomic HIOA model hence we use an AtomicHIOA_Descriptor
		atomicModelDescriptors.put(
				HairDryerElectricitySILModel.URI,
				RTAtomicHIOA_Descriptor.create(
						HairDryerElectricitySILModel.class,
						HairDryerElectricitySILModel.URI,
						simulatedTimeUnit,
						null,
						accelerationFactor));
		// for atomic models, we use an AtomicModelDescriptor
		atomicModelDescriptors.put(
				HairDryerStateSILModel.URI,
				RTAtomicModelDescriptor.create(
						HairDryerStateSILModel.class,
						HairDryerStateSILModel.URI,
						simulatedTimeUnit,
						null,
						accelerationFactor));

		// map that will contain the coupled model descriptors to construct
		// the simulation architecture
		Map<String,CoupledModelDescriptor> coupledModelDescriptors =
															new HashMap<>();

		// the set of submodels of the coupled model, given by their URIs
		Set<String> submodels = new HashSet<String>();
		submodels.add(HairDryerElectricitySILModel.URI);
		submodels.add(HairDryerStateSILModel.URI);

		// event exchanging connections between exporting and importing
		// models
		Map<EventSource,EventSink[]> connections =
									new HashMap<EventSource,EventSink[]>();

		connections.put(
			new EventSource(HairDryerStateSILModel.URI,
							SwitchOnHairDryer.class),
			new EventSink[] {
				new EventSink(HairDryerElectricitySILModel.URI,
							  SwitchOnHairDryer.class)
			});
		connections.put(
			new EventSource(HairDryerStateSILModel.URI,
							SwitchOffHairDryer.class),
			new EventSink[] {
				new EventSink(HairDryerElectricitySILModel.URI,
							  SwitchOffHairDryer.class)
			});
		connections.put(
			new EventSource(HairDryerStateSILModel.URI,
							SetHighHairDryer.class),
			new EventSink[] {
				new EventSink(HairDryerElectricitySILModel.URI,
							  SetHighHairDryer.class)
			});
		connections.put(
			new EventSource(HairDryerStateSILModel.URI,
							SetLowHairDryer.class),
			new EventSink[] {
				new EventSink(HairDryerElectricitySILModel.URI,
							  SetLowHairDryer.class)
			});

		// coupled model descriptor
		coupledModelDescriptors.put(
				rootModelURI,
				new RTCoupledModelDescriptor(
						HairDryerCoupledModel.class,
						rootModelURI,
						submodels,
						null,
						null,
						connections,
						null,
						accelerationFactor));

		// simulation architecture
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
	 * create the local software-in-the-loop real time simulation architecture
	 * for the {@code HairDryerCyPhy} component.
	 * 
	 * <p><strong>Description</strong></p>
	 * 
	 * <p>
	 * In integration tests under software-in-the-loop simulation, the
	 * {@code HairDryerCyPhy} component defines only one atomic model, the
	 * {@code HairDryerStateSILModel}, that makes the state and mode changes for
	 * the hair dryer simulator when receiving events from the
	 * {@code HairDryerCyPhy} component methods in SIL simulations, to turn on,
	 * turn off, set to high or set to low the hair dryer. Hence, the created
	 * architecture contains this sole atomic model.
	 * </p>
	 * <p>
	 * In integration tests, the {@code HairDryerElectricitySILModel}, to which
	 * the {@code HairDryerStateModel} resends the events to make it keep track
	 * of the corresponding electric power consumption changes, is located in
	 * the {@code ElectricMeter} component simulator. Hence, the events will
	 * be reexported by the local simulator to the local simulator of
	 * the {@code ElectricMeter} component that will have the received by
	 * the {@code HairDryerElectricitySILModel}.
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
	 * @param architectureURI		URI to be given to the created simulation architecture.
	 * @param rootModelURI			URI of the root model in the simulation architecture.
	 * @param simulatedTimeUnit		simulated time unit used in the architecture.
	 * @param accelerationFactor	acceleration factor used in this run.
	 * @return						the local software-in-the-loop real time simulation architecture for the integration test of the {@code HairDryerCyPhy} component.
	 * @throws Exception			<i>to do</i>.
	 */
	public static RTArchitecture	createHairDryerSIL_Architecture4IntegrationTest(
		String architectureURI,
		String rootModelURI,
		TimeUnit simulatedTimeUnit,
		double accelerationFactor
		) throws Exception
	{
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


		// map that will contain the atomic model descriptors to construct
		// the simulation architecture
		Map<String,AbstractAtomicModelDescriptor> atomicModelDescriptors =
															new HashMap<>();

		// the hair dyer model simulating its electricity consumption, an
		// atomic HIOA model hence we use an AtomicHIOA_Descriptor
		atomicModelDescriptors.put(
				rootModelURI,
				RTAtomicModelDescriptor.create(
						HairDryerStateSILModel.class,
						rootModelURI,
						simulatedTimeUnit,
						null,
						accelerationFactor));

		// map that will contain the coupled model descriptors to construct
		// the simulation architecture
		Map<String,CoupledModelDescriptor> coupledModelDescriptors =
															new HashMap<>();

		// simulation architecture
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
// -----------------------------------------------------------------------------

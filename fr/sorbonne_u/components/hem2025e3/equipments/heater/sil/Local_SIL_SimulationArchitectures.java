package fr.sorbonne_u.components.hem2025e3.equipments.heater.sil;

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
import fr.sorbonne_u.components.hem2025e2.equipments.heater.mil.HeaterCoupledModel;
import fr.sorbonne_u.components.hem2025e2.equipments.heater.mil.events.DoNotHeat;
import fr.sorbonne_u.components.hem2025e2.equipments.heater.mil.events.Heat;
import fr.sorbonne_u.components.hem2025e2.equipments.heater.mil.events.SwitchOffHeater;
import fr.sorbonne_u.components.hem2025e2.equipments.heater.mil.events.SwitchOnHeater;
import fr.sorbonne_u.components.hem2025e3.equipments.heater.sil.events.SIL_SetPowerHeater;
import fr.sorbonne_u.devs_simulation.architectures.RTArchitecture;
import fr.sorbonne_u.devs_simulation.hioa.architectures.RTAtomicHIOA_Descriptor;
import fr.sorbonne_u.devs_simulation.hioa.architectures.RTCoupledHIOA_Descriptor;
import fr.sorbonne_u.devs_simulation.hioa.models.vars.VariableSink;
import fr.sorbonne_u.devs_simulation.hioa.models.vars.VariableSource;
import fr.sorbonne_u.devs_simulation.models.architectures.AbstractAtomicModelDescriptor;
import fr.sorbonne_u.devs_simulation.models.architectures.CoupledModelDescriptor;
import fr.sorbonne_u.devs_simulation.models.architectures.RTAtomicModelDescriptor;
import fr.sorbonne_u.devs_simulation.models.events.EventI;
import fr.sorbonne_u.devs_simulation.models.events.EventSink;
import fr.sorbonne_u.devs_simulation.models.events.EventSource;
import fr.sorbonne_u.devs_simulation.models.events.ReexportedEvent;
import fr.sorbonne_u.exceptions.PreconditionException;

// -----------------------------------------------------------------------------
/**
 * The class <code>Local_SIL_SimulationArchitectures</code> defines the local
 * software-in-the-loop simulation architectures pertaining to the heater
 * appliance.
 *
 * <p><strong>Description</strong></p>
 * 
 * <p>
 * The class provides static methods that create the localsoftware-in-the-loop
 * real time simulation architectures for the {@code HeaterCyPhy} component. The
 * overall simulation architecture for the heater appliance can be seen as
 * follows:
 * </p>
 * <p><img src="../../../../../../../../images/hem-2025-e3/HeaterMILModel.png"/></p> 
 * <p>
 * The simulation architectures created in this class are local to components
 * in the sense that they define the simulators that are created and run by
 * each component. The one for unit test is meant to be executed alone in the
 * {@code HeaterCyPhy} component. The one for integration test is meant to
 * be executed within a larger simulator for the entire application component
 * architectures where they are seen as atomic models to be composed by a
 * coupled model that will reside in a coordinator component.
 * </p>
 * 
 * <p><strong>Implementation  Invariants</strong></p>
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
	 * create the local software-in-the-loop simulation architecture for the
	 * {@code HeaterCyPhy} component used in unit tests.
	 * 
	 * <p><strong>Description</strong></p>
	 * 
	 * <p><img src="../../../../../../../../images/hem-2025-e3/HeaterUnitTestLocalArchitecture.png"/></p> 
	 * <p>
	 * In this simulation architecture, the heater simulator consists of four
	 * atomic models:
	 * </p>
	 * <ol>
	 * <li>The {@code HeaterStateSILModel} keeps track of the state (switched
	 *   on, switched off, etc.) of the heater and its current power level. The
	 *   state changes are triggered by the reception of external events
	 *   directly received from the {@code HeaterCyPhy} component methods;
	 *   whenever a state change occurs, the triggering event is reemitted
	 *   towards the {@code HeaterElectricitySILModel} and the
	 *   {@code HeaterTemperatureSILModel} (except for {@code SwitchOnHeater}
	 *   that does not influence the temperature model).</li>
	 * <li>The {@code HeaterElectricitySILModel} keeps track of the electric
	 *   power consumed by the heater in a variable <code>currentIntensity</code>,
	 *   which is exported but not used in this simulation of the heater in
	 *   isolation.</li>
	 * <li>The {@code ExternalTemperatureModel} simulates the temperature
	 *   outside the room, a part of the environment. The simulated temperature
	 *   is put in an exported variable {@code externalTemperature} that is
	 *   imported with the same name by the {@code HeaterTemperatureSILModel}.</li>
	 * <li>The {@code HeaterTemperatureSILModel} simulates the temperature
	 *   inside the heated room, using the external temperature provided by the
	 *   {@code ExternalTemperatureModel} and the current power of the heater,
	 *   which it keeps track of through the {@code SetPowerHeater} and
	 *   {@code SwitchOffHeater} events. The evolution of the inside temperature
	 *   also obviously depends upon the fact that the heater actually is
	 *   heating or not, a state which is kept track of through the events
	 *   {@code Heat} and {@code DoNotHeat}.</li>
	 * </ol>
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
	 * @return						the local software-in-the-loop real time simulation architecture for the unit tests of the {@code Heater} component.
	 * @throws Exception			<i>to do</i>.
	 */
	public static RTArchitecture	createHeaterSIL_Architecture4UnitTest(
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

		// the heater state model only exchanges event, an atomic model
		// hence we use an AtomicModelDescriptor
		atomicModelDescriptors.put(
				HeaterStateSILModel.URI,
				RTAtomicModelDescriptor.create(
						HeaterStateSILModel.class,
						HeaterStateSILModel.URI,
						simulatedTimeUnit,
						null,
						accelerationFactor));
		// the heater models simulating its electricity consumption, its
		// temperatures and the external temperature are atomic HIOA models
		// hence we use an AtomicHIOA_Descriptor(s)
		atomicModelDescriptors.put(
				ExternalTemperatureSILModel.URI,
				RTAtomicHIOA_Descriptor.create(
						ExternalTemperatureSILModel.class,
						ExternalTemperatureSILModel.URI,
						simulatedTimeUnit,
						null,
						accelerationFactor));
		atomicModelDescriptors.put(
				HeaterTemperatureSILModel.URI,
				RTAtomicHIOA_Descriptor.create(
						HeaterTemperatureSILModel.class,
						HeaterTemperatureSILModel.URI,
						simulatedTimeUnit,
						null,
						accelerationFactor));
		atomicModelDescriptors.put(
				HeaterElectricitySILModel.URI,
				RTAtomicHIOA_Descriptor.create(
						HeaterElectricitySILModel.class,
						HeaterElectricitySILModel.URI,
						simulatedTimeUnit,
						null,
						accelerationFactor));

		// map that will contain the coupled model descriptors to construct
		// the simulation architecture
		Map<String,CoupledModelDescriptor> coupledModelDescriptors =
															new HashMap<>();

		// the set of submodels of the coupled model, given by their URIs
		Set<String> submodels = new HashSet<String>();
		submodels.add(HeaterStateSILModel.URI);
		submodels.add(ExternalTemperatureSILModel.URI);
		submodels.add(HeaterTemperatureSILModel.URI);
		submodels.add(HeaterElectricitySILModel.URI);

		// event exchanging connections between exporting and importing
		// models
		Map<EventSource,EventSink[]> connections =
									new HashMap<EventSource,EventSink[]>();

		connections.put(
				new EventSource(HeaterStateSILModel.URI,
								SwitchOnHeater.class),
				new EventSink[] {
						new EventSink(HeaterElectricitySILModel.URI,
									  SwitchOnHeater.class)
				});
		connections.put(
				new EventSource(HeaterStateSILModel.URI,
								SIL_SetPowerHeater.class),
				new EventSink[] {
						new EventSink(HeaterTemperatureSILModel.URI,
									  SIL_SetPowerHeater.class),
						new EventSink(HeaterElectricitySILModel.URI,
									  SIL_SetPowerHeater.class)
				});
		connections.put(
				new EventSource(HeaterStateSILModel.URI,
								SwitchOffHeater.class),
				new EventSink[] {
						new EventSink(HeaterTemperatureSILModel.URI,
									  SwitchOffHeater.class),
						new EventSink(HeaterElectricitySILModel.URI,
									  SwitchOffHeater.class)
				});
		connections.put(
				new EventSource(HeaterStateSILModel.URI, Heat.class),
				new EventSink[] {
						new EventSink(HeaterTemperatureSILModel.URI,
									  Heat.class),
						new EventSink(HeaterElectricitySILModel.URI,
									  Heat.class)
				});
		connections.put(
				new EventSource(HeaterStateSILModel.URI, DoNotHeat.class),
				new EventSink[] {
						new EventSink(HeaterTemperatureSILModel.URI,
									  DoNotHeat.class),
						new EventSink(HeaterElectricitySILModel.URI,
									  DoNotHeat.class)
				});

		// variable bindings between exporting and importing models
		Map<VariableSource,VariableSink[]> bindings =
							new HashMap<VariableSource,VariableSink[]>();

		bindings.put(new VariableSource("externalTemperature",
										Double.class,
										ExternalTemperatureSILModel.URI),
					 new VariableSink[] {
							 new VariableSink("externalTemperature",
									 		  Double.class,
									 		  HeaterTemperatureSILModel.URI)
					 });

		// coupled model descriptor
		coupledModelDescriptors.put(
				rootModelURI,
				new RTCoupledHIOA_Descriptor(
						HeaterCoupledModel.class,
						rootModelURI,
						submodels,
						null,
						null,
						connections,
						null,
						null,
						null,
						bindings,
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
	 * for the {@code HeaterCyPhy} component when used in integration tests.
	 * 
	 * <p><strong>Description</strong></p>
	 * 
	 * <p>
	 * The simulation architecture created for {@code HeaterCyPhy} real time
	 * integration tests is very similar to the one used for unit test, except
	 * for two points:
	 * </p>
	 * <ul>
	 * <li>The {@code HeaterElectricitySILModel} is moved to the local
	 *   simulator of the {@code ElectricMeterCyPhy} component to cater for
	 *   the binding of its exported variable {@code currentIntensity}
	 *   with the electricity model of the electric meter.</li>
	 * <li>Because of this move, the state changes in the heater triggered by
	 *   the events must be transmitted to {@code HeaterElectricitySILModel}
	 *   by making the coupled model reexporting them.</li>
	 * </ul>
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
	 * @param accelerationFactor	acceleration factor used to execute in a logical time speeding up the real time.
	 * @return						the local SIL real time simulation architecture for the unit tests of the {@code Heater} component.
	 * @throws Exception			<i>to do</i>.
	 */
	public static RTArchitecture	createHeater_SIL_LocalArchitecture4IntegrationTest(
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

		atomicModelDescriptors.put(
				HeaterStateSILModel.URI,
				RTAtomicModelDescriptor.create(
						HeaterStateSILModel.class,
						HeaterStateSILModel.URI,
						simulatedTimeUnit,
						null,
						accelerationFactor));
		// the heater models simulating its electricity consumption, its
		// temperatures and the external temperature are atomic HIOA models
		// hence we use an AtomicHIOA_Descriptor(s)
		atomicModelDescriptors.put(
				ExternalTemperatureSILModel.URI,
				RTAtomicHIOA_Descriptor.create(
						ExternalTemperatureSILModel.class,
						ExternalTemperatureSILModel.URI,
						simulatedTimeUnit,
						null,
						accelerationFactor));
		atomicModelDescriptors.put(
				HeaterTemperatureSILModel.URI,
				RTAtomicHIOA_Descriptor.create(
						HeaterTemperatureSILModel.class,
						HeaterTemperatureSILModel.URI,
						simulatedTimeUnit,
						null,
						accelerationFactor));

		// map that will contain the coupled model descriptors to construct
		// the simulation architecture
		Map<String,CoupledModelDescriptor> coupledModelDescriptors =
															new HashMap<>();

		// the set of submodels of the coupled model, given by their URIs
		Set<String> submodels = new HashSet<String>();
		submodels.add(HeaterStateSILModel.URI);
		submodels.add(ExternalTemperatureSILModel.URI);
		submodels.add(HeaterTemperatureSILModel.URI);

		// events emitted by submodels that are reexported towards other models
		Map<Class<? extends EventI>,ReexportedEvent> reexported =
				new HashMap<Class<? extends EventI>,ReexportedEvent>();

		reexported.put(
				SwitchOnHeater.class,
				new ReexportedEvent(HeaterStateSILModel.URI,
									SwitchOnHeater.class));
		reexported.put(
				SIL_SetPowerHeater.class,
				new ReexportedEvent(HeaterStateSILModel.URI,
									SIL_SetPowerHeater.class));
		reexported.put(
				SwitchOffHeater.class,
				new ReexportedEvent(HeaterStateSILModel.URI,
									SwitchOffHeater.class));
		reexported.put(
				Heat.class,
				new ReexportedEvent(HeaterStateSILModel.URI,
									Heat.class));
		reexported.put(
				DoNotHeat.class,
				new ReexportedEvent(HeaterStateSILModel.URI,
									DoNotHeat.class));

		// event exchanging connections between exporting and importing
		// models
		Map<EventSource,EventSink[]> connections =
									new HashMap<EventSource,EventSink[]>();

		connections.put(
				new EventSource(HeaterStateSILModel.URI,
								SIL_SetPowerHeater.class),
				new EventSink[] {
						new EventSink(HeaterTemperatureSILModel.URI,
									  SIL_SetPowerHeater.class)
				});
		connections.put(
				new EventSource(HeaterStateSILModel.URI,
								SwitchOffHeater.class),
				new EventSink[] {
						new EventSink(HeaterTemperatureSILModel.URI,
									  SwitchOffHeater.class)
				});
		connections.put(
				new EventSource(HeaterStateSILModel.URI, Heat.class),
				new EventSink[] {
						new EventSink(HeaterTemperatureSILModel.URI,
									  Heat.class)
				});
		connections.put(
				new EventSource(HeaterStateSILModel.URI, DoNotHeat.class),
				new EventSink[] {
						new EventSink(HeaterTemperatureSILModel.URI,
									  DoNotHeat.class)
				});

		// variable bindings between exporting and importing models
		Map<VariableSource,VariableSink[]> bindings =
							new HashMap<VariableSource,VariableSink[]>();

		bindings.put(new VariableSource("externalTemperature",
										Double.class,
										ExternalTemperatureSILModel.URI),
					 new VariableSink[] {
							 new VariableSink("externalTemperature",
									 		  Double.class,
									 		 HeaterTemperatureSILModel.URI)
					 });

		// coupled model descriptor
		coupledModelDescriptors.put(
				rootModelURI,
				new RTCoupledHIOA_Descriptor(
						HeaterCoupledModel.class,
						rootModelURI,
						submodels,
						null,
						reexported,
						connections,
						null,
						null,
						null,
						bindings,
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
}
// -----------------------------------------------------------------------------

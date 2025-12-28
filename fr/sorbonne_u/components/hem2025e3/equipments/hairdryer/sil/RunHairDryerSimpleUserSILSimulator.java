package fr.sorbonne_u.components.hem2025e3.equipments.hairdryer.sil;

// Copyright Jacques Malenfant, Sorbonne Universite.
// Jacques.Malenfant@lip6.fr
//
// This software is a computer program whose purpose is to provide a
// basic component programming model to program with components
// distributed applications in the Java programming language.
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
import fr.sorbonne_u.components.hem2025e2.equipments.hairdryer.mil.HairDryerCoupledModel;
import fr.sorbonne_u.components.hem2025e2.equipments.hairdryer.mil.HairDryerSimulationConfigurationI;
import fr.sorbonne_u.components.hem2025e2.equipments.hairdryer.mil.events.SetHighHairDryer;
import fr.sorbonne_u.components.hem2025e2.equipments.hairdryer.mil.events.SetLowHairDryer;
import fr.sorbonne_u.components.hem2025e2.equipments.hairdryer.mil.events.SwitchOffHairDryer;
import fr.sorbonne_u.components.hem2025e2.equipments.hairdryer.mil.events.SwitchOnHairDryer;
import fr.sorbonne_u.devs_simulation.architectures.ArchitectureI;
import fr.sorbonne_u.devs_simulation.architectures.RTArchitecture;
import fr.sorbonne_u.devs_simulation.hioa.architectures.RTAtomicHIOA_Descriptor;
import fr.sorbonne_u.devs_simulation.models.architectures.AbstractAtomicModelDescriptor;
import fr.sorbonne_u.devs_simulation.models.architectures.CoupledModelDescriptor;
import fr.sorbonne_u.devs_simulation.models.architectures.RTAtomicModelDescriptor;
import fr.sorbonne_u.devs_simulation.models.architectures.RTCoupledModelDescriptor;
import fr.sorbonne_u.devs_simulation.models.events.EventSink;
import fr.sorbonne_u.devs_simulation.models.events.EventSource;
import fr.sorbonne_u.devs_simulation.models.time.Duration;
import fr.sorbonne_u.devs_simulation.models.time.Time;
import fr.sorbonne_u.devs_simulation.simulators.SimulationEngine;
import fr.sorbonne_u.devs_simulation.simulators.interfaces.SimulationReportI;
import fr.sorbonne_u.devs_simulation.simulators.interfaces.SimulatorI;

// -----------------------------------------------------------------------------
/**
 * The class <code>RunHairDryerSimpleUserMILSimulation</code> is the main class
 * used to run a real time simulations on the software-in-the-loop models of
 * the hair dryer in isolation based on a user model.
 *
 * <p><strong>Description</strong></p>
 * 
 * <p>
 * The simulation architecture for the hair dryer contains only two atomic
 * models composed under a coupled model:
 * </p>
 * <p><img src="../../../../../../../../images/hem-2025-e2/HairDryerSimpleUserArchitecture.png"/></p> 
 * <p>
 * The code of the {@code main} methods shows how to use simulation model
 * descriptors to create the description of the above simulation architecture
 * and then create an instance of this architecture by instantiating and
 * connecting the model instances. Note how models are described by atomic model
 * descriptors and coupled model descriptors and then the connections between
 * coupled models and their submodels as well as exported events to imported
 * ones are described by different maps. In this example, only connections
 * between models within this architecture are necessary, but when creating
 * coupled models, they can also import and export events consumed and produced
 * by their submodels.
 * </p>
 * <p>
 * The architecture object is the root of this description and it provides
 * the method {@code constructSimulator} that instantiate the models and
 * connect them. This method returns the reference on the simulator attached
 * to the root coupled model in the architecture instance, which is then used
 * to perform simulation runs by calling the method
 * {@code doStandAloneSimulation}
 * </p>
 * <p>
 * The descriptors and maps can be viewed as kinds of nodes in the abstract
 * syntax tree of an architectural language that does not have a concrete
 * syntax yet.
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
 * invariant	{@code HairDryerSimulationConfigurationI.staticInvariants()}
 * </pre>
 * 
 * <p>Created on : 2025-10-28</p>
 * 
 * @author	<a href="mailto:Jacques.Malenfant@lip6.fr">Jacques Malenfant</a>
 */
public class			RunHairDryerSimpleUserSILSimulator
{
	// -------------------------------------------------------------------------
	// Constants and variables
	// -------------------------------------------------------------------------

	/** the acceleration factor used in the real time MIL simulations.	 	*/
	public static final double		ACCELERATION_FACTOR = 3600.0;

	public static final Time		START_TIME =
			new Time(0.0, HairDryerSimulationConfigurationI.TIME_UNIT);
	public static final Duration	SIMULATION_DURATION =
			new Duration(0.5, HairDryerSimulationConfigurationI.TIME_UNIT);

	// -------------------------------------------------------------------------
	// Invariants
	// -------------------------------------------------------------------------

	/**
	 * return true if the static invariants are observed, false otherwise.
	 * 
	 * <p><strong>Contract</strong></p>
	 * 
	 * <pre>
	 * pre	{@code instance != null}
	 * post	{@code true}	// no postcondition.
	 * </pre>
	 *
	 * @return	true if the invariants are observed, false otherwise.
	 */
	public static boolean	staticInvariants()
	{
		boolean ret = true;
		ret &= HairDryerSimulationConfigurationI.staticInvariants();
		return ret;
	}

	// -------------------------------------------------------------------------
	// Methods
	// -------------------------------------------------------------------------

	public static void	main(String[] args)
	{
		staticInvariants();
		Time.setPrintPrecision(4);
		Duration.setPrintPrecision(4);

		try {
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
							HairDryerSimulationConfigurationI.TIME_UNIT,
							null,
							ACCELERATION_FACTOR));
			// for atomic model, we use an AtomicModelDescriptor
			atomicModelDescriptors.put(
					HairDryerStateSILModel.URI,
					RTAtomicModelDescriptor.create(
							HairDryerStateSILModel.class,
							HairDryerStateSILModel.URI,
							HairDryerSimulationConfigurationI.TIME_UNIT,
							null,
							ACCELERATION_FACTOR));
			atomicModelDescriptors.put(
					HairDryerSimpleUserSILModel.URI,
					RTAtomicModelDescriptor.create(
							HairDryerSimpleUserSILModel.class,
							HairDryerSimpleUserSILModel.URI,
							HairDryerSimulationConfigurationI.TIME_UNIT,
							null,
							ACCELERATION_FACTOR));

			// map that will contain the coupled model descriptors to construct
			// the simulation architecture
			Map<String,CoupledModelDescriptor> coupledModelDescriptors =
																new HashMap<>();

			// the set of submodels of the coupled model, given by their URIs
			Set<String> submodels = new HashSet<String>();
			submodels.add(HairDryerStateSILModel.URI);
			submodels.add(HairDryerElectricitySILModel.URI);
			submodels.add(HairDryerSimpleUserSILModel.URI);

			// event exchanging connections between exporting and importing
			// models
			Map<EventSource,EventSink[]> connections =
										new HashMap<EventSource,EventSink[]>();

			connections.put(
				new EventSource(HairDryerSimpleUserSILModel.URI,
								SwitchOnHairDryer.class),
				new EventSink[] {
					new EventSink(HairDryerStateSILModel.URI,
								  SwitchOnHairDryer.class)
				});
			connections.put(
				new EventSource(HairDryerSimpleUserSILModel.URI,
								SwitchOffHairDryer.class),
				new EventSink[] {
					new EventSink(HairDryerStateSILModel.URI,
								  SwitchOffHairDryer.class)
				});
			connections.put(
				new EventSource(HairDryerSimpleUserSILModel.URI,
								SetHighHairDryer.class),
				new EventSink[] {
					new EventSink(HairDryerStateSILModel.URI,
								  SetHighHairDryer.class)
				});
			connections.put(
				new EventSource(HairDryerSimpleUserSILModel.URI,
								SetLowHairDryer.class),
				new EventSink[] {
					new EventSink(HairDryerStateSILModel.URI,
								  SetLowHairDryer.class)
				});

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
					HairDryerCoupledModel.URI,
					new RTCoupledModelDescriptor(
							HairDryerCoupledModel.class,
							HairDryerCoupledModel.URI,
							submodels,
							null,
							null,
							connections,
							null,
							ACCELERATION_FACTOR));

			// simulation architecture
			ArchitectureI architecture =
					new RTArchitecture(
							HairDryerCoupledModel.URI,
							atomicModelDescriptors,
							coupledModelDescriptors,
							HairDryerSimulationConfigurationI.TIME_UNIT);

			// create the simulator from the simulation architecture
			SimulatorI se = architecture.constructSimulator();
			// this add additional time at each simulation step in
			// standard simulations (useful when debugging)
			SimulationEngine.SIMULATION_STEP_SLEEP_TIME = 0L;

			System.out.println("test begins.");
			long realTimeStart = System.currentTimeMillis() + 200;
			se.startRTSimulation(realTimeStart,
								 START_TIME.getSimulatedTime(),
								 SIMULATION_DURATION.getSimulatedDuration());
			long executionDuration =					
				new Double(
						HairDryerSimulationConfigurationI.TIME_UNIT.toMillis(1)
							* (SIMULATION_DURATION.getSimulatedDuration()
										/ACCELERATION_FACTOR)).longValue();
			Thread.sleep(executionDuration + 2000L);
			SimulationReportI sr = se.getSimulatedModel().getFinalReport();
			System.out.println(sr);
			System.out.println("test ends.");
			System.exit(0);
		} catch (Exception e) {
			throw new RuntimeException(e) ;
		}
	}
}
// -----------------------------------------------------------------------------

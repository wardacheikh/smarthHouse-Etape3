package fr.sorbonne_u.components.hem2025e3.equipements.wifi.sil;


//Copyright Jacques Malenfant, Sorbonne Universite.
//Jacques.Malenfant@lip6.fr
//
//This software is a computer program whose purpose is to provide a
//basic component programming model to program with components
//distributed applications in the Java programming language.
//
//This software is governed by the CeCILL-C license under French law and
//abiding by the rules of distribution of free software.  You can use,
//modify and/ or redistribute the software under the terms of the
//CeCILL-C license as circulated by CEA, CNRS and INRIA at the following
//URL "http://www.cecill.info".
//
//As a counterpart to the access to the source code and  rights to copy,
//modify and redistribute granted by the license, users are provided only
//with a limited warranty  and the software's author,  the holder of the
//economic rights,  and the successive licensors  have only  limited
//liability. 
//
//In this respect, the user's attention is drawn to the risks associated
//with loading,  using,  modifying and/or developing or reproducing the
//software by the user in light of its specific status of free software,
//that may mean  that it is complicated to manipulate,  and  that  also
//therefore means  that it is reserved for developers  and  experienced
//professionals having in-depth computer knowledge. Users are therefore
//encouraged to load and test the software's suitability as regards their
//requirements in conditions enabling the security of their systems and/or 
//data to be ensured and,  more generally, to use and operate it in the 
//same conditions as regards security. 
//
//The fact that you are presently reading this means that you have had
//knowledge of the CeCILL-C license and that you accept its terms.

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import fr.sorbonne_u.components.hem2025e2.equipments.BoxWifi.mil.BoxWifiCoupledModel;
import fr.sorbonne_u.components.hem2025e2.equipments.BoxWifi.mil.BoxWifiSimulationConfigurationI;
import fr.sorbonne_u.components.hem2025e2.equipments.BoxWifi.mil.events.ActivateWifiBoxWifi;
import fr.sorbonne_u.components.hem2025e2.equipments.BoxWifi.mil.events.DeactivateWifiBoxWifi;
import fr.sorbonne_u.components.hem2025e2.equipments.BoxWifi.mil.events.SwitchOffBoxWifi;
import fr.sorbonne_u.components.hem2025e2.equipments.BoxWifi.mil.events.SwitchOnBoxWifi;
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

//-----------------------------------------------------------------------------
/**
* The class <code>RunBoxWifiSimpleUserSILSimulator</code> is the main class
* used to run a real time simulations on the software-in-the-loop models of
* the box WiFi in isolation based on a user model.
*
* <p><strong>Description</strong></p>
* 
* <p>
* The simulation architecture for the box WiFi contains three atomic
* models composed under a coupled model:
* </p>
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
* invariant	{@code BoxWifiSimulationConfigurationI.staticInvariants()}
* </pre>
* 
* <p>Created on : 2025-01-15</p>
* 
* @author	<a href="mailto:Jacques.Malenfant@lip6.fr">Jacques Malenfant</a>
*/
public class			RunBoxWifiSimpleUserSILSimulator
{
	// -------------------------------------------------------------------------
	// Constants and variables
	// -------------------------------------------------------------------------

	/** the acceleration factor used in the real time SIL simulations.	 	*/
	public static final double		ACCELERATION_FACTOR = 3600.0;

	public static final Time		START_TIME =
			new Time(0.0, BoxWifiSimulationConfigurationI.TIME_UNIT);
	public static final Duration	SIMULATION_DURATION =
			new Duration(24.0, BoxWifiSimulationConfigurationI.TIME_UNIT); // 24 hours

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
		ret &= BoxWifiSimulationConfigurationI.staticInvariants();
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

			// the box WiFi model simulating its electricity consumption, an
			// atomic HIOA model hence we use an AtomicHIOA_Descriptor
			atomicModelDescriptors.put(
					BoxWifiElectricitySILModel.URI,
					RTAtomicHIOA_Descriptor.create(
							BoxWifiElectricitySILModel.class,
							BoxWifiElectricitySILModel.URI,
							BoxWifiSimulationConfigurationI.TIME_UNIT,
							null,
							ACCELERATION_FACTOR));
			
			// the box WiFi state model, an atomic model
			atomicModelDescriptors.put(
					BoxWifiStateSILModel.URI,
					RTAtomicModelDescriptor.create(
							BoxWifiStateSILModel.class,
							BoxWifiStateSILModel.URI,
							BoxWifiSimulationConfigurationI.TIME_UNIT,
							null,
							ACCELERATION_FACTOR));
			
			// the box WiFi simple user model
			atomicModelDescriptors.put(
					BoxWifiSimpleUserSILModel.URI,
					RTAtomicModelDescriptor.create(
							BoxWifiSimpleUserSILModel.class,
							BoxWifiSimpleUserSILModel.URI,
							BoxWifiSimulationConfigurationI.TIME_UNIT,
							null,
							ACCELERATION_FACTOR));

			// map that will contain the coupled model descriptors to construct
			// the simulation architecture
			Map<String,CoupledModelDescriptor> coupledModelDescriptors =
																new HashMap<>();

			// the set of submodels of the coupled model, given by their URIs
			Set<String> submodels = new HashSet<String>();
			submodels.add(BoxWifiStateSILModel.URI);
			submodels.add(BoxWifiElectricitySILModel.URI);
			submodels.add(BoxWifiSimpleUserSILModel.URI);

			// event exchanging connections between exporting and importing
			// models
			Map<EventSource,EventSink[]> connections =
										new HashMap<EventSource,EventSink[]>();

			// Connections from user model to state model
			connections.put(
				new EventSource(BoxWifiSimpleUserSILModel.URI,
								SwitchOnBoxWifi.class),
				new EventSink[] {
					new EventSink(BoxWifiStateSILModel.URI,
								  SwitchOnBoxWifi.class)
				});
			connections.put(
				new EventSource(BoxWifiSimpleUserSILModel.URI,
								SwitchOffBoxWifi.class),
				new EventSink[] {
					new EventSink(BoxWifiStateSILModel.URI,
								  SwitchOffBoxWifi.class)
				});
			connections.put(
				new EventSource(BoxWifiSimpleUserSILModel.URI,
								ActivateWifiBoxWifi.class),
				new EventSink[] {
					new EventSink(BoxWifiStateSILModel.URI,
								  ActivateWifiBoxWifi.class)
				});
			connections.put(
				new EventSource(BoxWifiSimpleUserSILModel.URI,
								DeactivateWifiBoxWifi.class),
				new EventSink[] {
					new EventSink(BoxWifiStateSILModel.URI,
								  DeactivateWifiBoxWifi.class)
				});

			// Connections from state model to electricity model
			connections.put(
				new EventSource(BoxWifiStateSILModel.URI,
								SwitchOnBoxWifi.class),
				new EventSink[] {
					new EventSink(BoxWifiElectricitySILModel.URI,
								  SwitchOnBoxWifi.class)
				});
			connections.put(
				new EventSource(BoxWifiStateSILModel.URI,
								SwitchOffBoxWifi.class),
				new EventSink[] {
					new EventSink(BoxWifiElectricitySILModel.URI,
								  SwitchOffBoxWifi.class)
				});
			connections.put(
				new EventSource(BoxWifiStateSILModel.URI,
								ActivateWifiBoxWifi.class),
				new EventSink[] {
						new EventSink(BoxWifiElectricitySILModel.URI,
									  ActivateWifiBoxWifi.class)
				});
			connections.put(
				new EventSource(BoxWifiStateSILModel.URI,
								DeactivateWifiBoxWifi.class),
				new EventSink[] {
					new EventSink(BoxWifiElectricitySILModel.URI,
								  DeactivateWifiBoxWifi.class)
				});

			// coupled model descriptor
			coupledModelDescriptors.put(
					BoxWifiCoupledModel.URI,
					new RTCoupledModelDescriptor(
							BoxWifiCoupledModel.class,
							BoxWifiCoupledModel.URI,
							submodels,
							null,
							null,
							connections,
							null,
							ACCELERATION_FACTOR));

			// simulation architecture
			ArchitectureI architecture =
					new RTArchitecture(
							BoxWifiCoupledModel.URI,
							atomicModelDescriptors,
							coupledModelDescriptors,
							BoxWifiSimulationConfigurationI.TIME_UNIT);

			// create the simulator from the simulation architecture
			SimulatorI se = architecture.constructSimulator();
			// this add additional time at each simulation step in
			// standard simulations (useful when debugging)
			SimulationEngine.SIMULATION_STEP_SLEEP_TIME = 0L;

			System.out.println("==================================================");
			System.out.println("Box WiFi Simple User SIL Simulation");
			System.out.println("==================================================");
			System.out.println("Simulation parameters:");
			System.out.println("  Start time: " + START_TIME.getSimulatedTime() + " " + BoxWifiSimulationConfigurationI.TIME_UNIT);
			System.out.println("  Duration: " + SIMULATION_DURATION.getSimulatedDuration() + " " + BoxWifiSimulationConfigurationI.TIME_UNIT);
			System.out.println("  Acceleration factor: " + ACCELERATION_FACTOR + "x");
			System.out.println("  Real time duration: " + 
				(BoxWifiSimulationConfigurationI.TIME_UNIT.toMillis(1) * 
				 SIMULATION_DURATION.getSimulatedDuration() / ACCELERATION_FACTOR / 1000.0) + 
				" seconds");
			System.out.println("==================================================");
			
			System.out.println("Simulation begins...");
			long realTimeStart = System.currentTimeMillis() + 200;
			se.startRTSimulation(realTimeStart,
								 START_TIME.getSimulatedTime(),
								 SIMULATION_DURATION.getSimulatedDuration());
			
			// Calculate and wait for the real-time duration of the simulation
			long executionDuration =					
				new Double(
						BoxWifiSimulationConfigurationI.TIME_UNIT.toMillis(1)
							* (SIMULATION_DURATION.getSimulatedDuration()
										/ACCELERATION_FACTOR)).longValue();
			
			System.out.println("Waiting " + (executionDuration/1000.0) + " seconds for simulation to complete...");
			Thread.sleep(executionDuration + 2000L); // Add 2 seconds margin
			
			SimulationReportI sr = se.getSimulatedModel().getFinalReport();
			System.out.println("==================================================");
			System.out.println("Simulation Report:");
			System.out.println("==================================================");
			System.out.println(sr);
			System.out.println("==================================================");
			System.out.println("Simulation ends.");
			System.out.println("==================================================");
			
			System.exit(0);
		} catch (Exception e) {
			System.err.println("==================================================");
			System.err.println("Simulation failed with error:");
			System.err.println("==================================================");
			e.printStackTrace();
			System.err.println("==================================================");
			System.exit(1);
		}
	}
	
	// -------------------------------------------------------------------------
	// Helper methods for different simulation scenarios
	// -------------------------------------------------------------------------
	
	/**
	 * Run a quick test simulation (1 hour simulated time).
	 */
	public static void runQuickTest() throws Exception {
		Duration quickDuration = new Duration(1.0, BoxWifiSimulationConfigurationI.TIME_UNIT);
		runSimulation(quickDuration, 3600.0, "Quick Test");
	}
	
	/**
	 * Run a daily simulation (24 hours simulated time).
	 */
	public static void runDailySimulation() throws Exception {
		Duration dailyDuration = new Duration(24.0, BoxWifiSimulationConfigurationI.TIME_UNIT);
		runSimulation(dailyDuration, 3600.0, "Daily Simulation");
	}
	
	/**
	 * Run a weekly simulation (168 hours simulated time).
	 */
	public static void runWeeklySimulation() throws Exception {
		Duration weeklyDuration = new Duration(168.0, BoxWifiSimulationConfigurationI.TIME_UNIT);
		runSimulation(weeklyDuration, 7200.0, "Weekly Simulation"); // Faster acceleration for weekly
	}
	
	/**
	 * Generic method to run a simulation with given parameters.
	 */
	private static void runSimulation(Duration duration, double accelerationFactor, String scenarioName) throws Exception {
		System.out.println("==================================================");
		System.out.println("Box WiFi " + scenarioName);
		System.out.println("==================================================");
		
		Time.setPrintPrecision(4);
		Duration.setPrintPrecision(4);
		
		// Build architecture
		ArchitectureI architecture = buildArchitecture(accelerationFactor);
		
		// Create simulator
		SimulatorI se = architecture.constructSimulator();
		SimulationEngine.SIMULATION_STEP_SLEEP_TIME = 0L;
		
		System.out.println("Scenario: " + scenarioName);
		System.out.println("Duration: " + duration.getSimulatedDuration() + " hours");
		System.out.println("Acceleration: " + accelerationFactor + "x");
		
		long realTimeStart = System.currentTimeMillis() + 200;
		se.startRTSimulation(realTimeStart,
							 START_TIME.getSimulatedTime(),
							 duration.getSimulatedDuration());
		
		long executionDuration = new Double(
				TimeUnit.HOURS.toMillis(1) * (duration.getSimulatedDuration() / accelerationFactor)).longValue();
		
		System.out.println("Real time to wait: " + (executionDuration/1000.0) + " seconds");
		Thread.sleep(executionDuration + 2000L);
		
		SimulationReportI sr = se.getSimulatedModel().getFinalReport();
		System.out.println("Simulation Report:");
		System.out.println(sr);
		
		System.out.println(scenarioName + " completed.");
	}
	
	/**
	 * Build the simulation architecture with given acceleration factor.
	 */
	private static ArchitectureI buildArchitecture(double accelerationFactor) throws Exception {
		Map<String,AbstractAtomicModelDescriptor> atomicModelDescriptors = new HashMap<>();
		
		atomicModelDescriptors.put(
				BoxWifiElectricitySILModel.URI,
				RTAtomicHIOA_Descriptor.create(
						BoxWifiElectricitySILModel.class,
						BoxWifiElectricitySILModel.URI,
						BoxWifiSimulationConfigurationI.TIME_UNIT,
						null,
						accelerationFactor));
		
		atomicModelDescriptors.put(
				BoxWifiStateSILModel.URI,
				RTAtomicModelDescriptor.create(
						BoxWifiStateSILModel.class,
						BoxWifiStateSILModel.URI,
						BoxWifiSimulationConfigurationI.TIME_UNIT,
						null,
						accelerationFactor));
		
		atomicModelDescriptors.put(
				BoxWifiSimpleUserSILModel.URI,
				RTAtomicModelDescriptor.create(
						BoxWifiSimpleUserSILModel.class,
						BoxWifiSimpleUserSILModel.URI,
						BoxWifiSimulationConfigurationI.TIME_UNIT,
						null,
						accelerationFactor));
		
		Map<String,CoupledModelDescriptor> coupledModelDescriptors = new HashMap<>();
		Set<String> submodels = new HashSet<String>();
		submodels.add(BoxWifiStateSILModel.URI);
		submodels.add(BoxWifiElectricitySILModel.URI);
		submodels.add(BoxWifiSimpleUserSILModel.URI);
		
		Map<EventSource,EventSink[]> connections = new HashMap<EventSource,EventSink[]>();
		
		// User -> State connections
		connections.put(
			new EventSource(BoxWifiSimpleUserSILModel.URI, SwitchOnBoxWifi.class),
			new EventSink[] { new EventSink(BoxWifiStateSILModel.URI, SwitchOnBoxWifi.class) });
		connections.put(
			new EventSource(BoxWifiSimpleUserSILModel.URI, SwitchOffBoxWifi.class),
			new EventSink[] { new EventSink(BoxWifiStateSILModel.URI, SwitchOffBoxWifi.class) });
		connections.put(
			new EventSource(BoxWifiSimpleUserSILModel.URI, ActivateWifiBoxWifi.class),
			new EventSink[] { new EventSink(BoxWifiStateSILModel.URI, ActivateWifiBoxWifi.class) });
		connections.put(
			new EventSource(BoxWifiSimpleUserSILModel.URI, DeactivateWifiBoxWifi.class),
			new EventSink[] { new EventSink(BoxWifiStateSILModel.URI, DeactivateWifiBoxWifi.class) });
		
		// State -> Electricity connections
		connections.put(
			new EventSource(BoxWifiStateSILModel.URI, SwitchOnBoxWifi.class),
			new EventSink[] { new EventSink(BoxWifiElectricitySILModel.URI, SwitchOnBoxWifi.class) });
		connections.put(
			new EventSource(BoxWifiStateSILModel.URI, SwitchOffBoxWifi.class),
			new EventSink[] { new EventSink(BoxWifiElectricitySILModel.URI, SwitchOffBoxWifi.class) });
		connections.put(
			new EventSource(BoxWifiStateSILModel.URI, ActivateWifiBoxWifi.class),
			new EventSink[] { new EventSink(BoxWifiElectricitySILModel.URI, ActivateWifiBoxWifi.class) });
		connections.put(
			new EventSource(BoxWifiStateSILModel.URI, DeactivateWifiBoxWifi.class),
			new EventSink[] { new EventSink(BoxWifiElectricitySILModel.URI, DeactivateWifiBoxWifi.class) });
		
		coupledModelDescriptors.put(
				BoxWifiCoupledModel.URI,
				new RTCoupledModelDescriptor(
						BoxWifiCoupledModel.class,
						BoxWifiCoupledModel.URI,
						submodels,
						null,
						null,
						connections,
						null,
						accelerationFactor));
		
		return new RTArchitecture(
				BoxWifiCoupledModel.URI,
				atomicModelDescriptors,
				coupledModelDescriptors,
				BoxWifiSimulationConfigurationI.TIME_UNIT);
	}
}
//-----------------------------------------------------------------------------
package fr.sorbonne_u.components.hem2025e3.equipements.wifi.sil;

// Copyright Jacques Malenfant, Sorbonne Universite.
// Jacques.Malenfant@lip6.fr
//
// This software is a computer program whose purpose is to implement a mock-up
// of household energy management system.
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
import fr.sorbonne_u.components.cyphy.utils.tests.SimulationTestStep;
import fr.sorbonne_u.components.cyphy.utils.tests.TestScenarioWithSimulation;
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
import fr.sorbonne_u.devs_simulation.models.events.EventI;
import fr.sorbonne_u.devs_simulation.models.events.EventSink;
import fr.sorbonne_u.devs_simulation.models.events.EventSource;
import fr.sorbonne_u.devs_simulation.models.interfaces.ModelI;
import fr.sorbonne_u.devs_simulation.models.time.Duration;
import fr.sorbonne_u.devs_simulation.models.time.Time;
import fr.sorbonne_u.devs_simulation.simulators.SimulationEngine;
import fr.sorbonne_u.devs_simulation.simulators.interfaces.SimulationReportI;
import fr.sorbonne_u.devs_simulation.simulators.interfaces.SimulatorI;
import fr.sorbonne_u.exceptions.VerboseException;
import java.time.Instant;
import java.util.ArrayList;

// -----------------------------------------------------------------------------
/**
 * The class <code>RunBoxWifiUnitarySILSimulator</code> is the main class
 * used to run real time simulations on the software-in-the-loop models of the
 * box WiFi in isolation based on test scenarios.
 *
 * <p><strong>Description</strong></p>
 * 
 * <p>
 * This simulation execution class creates the following simulation architecture
 * and then executes it for one run:
 * </p>
 * <p>
 * In this simulation architecture, the box WiFi simulator consists of three
 * atomic models:
 * </p>
 * <ol>
 * <li>The <code>BoxWifiStateSILModel</code> keeps track of the state (switched
 *   on, switched off, WiFi activated, etc.) of the box WiFi. The state changes
 *   are triggered by the reception of external events; whenever a state change
 *   occurs, the triggering event is emitted towards the
 *   <code>BoxWifiElectricitySILModel</code></li>.
 * <li>The <code>BoxWifiElectricitySILModel</code> keeps track of the electric
 *   power consumed by the box WiFi in a variable
 *   <code>currentIntensity</code> which is exported but not used in this
 *   simulation of the box WiFi in isolation.</li>
 * <li>The <code>BoxWifiUnitTesterSILModel</code> simulates a box WiFi tester
 *   by emitting state changing events towards the
 *   <code>BoxWifiStateSILModel.</code></li>
 * </ol>
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
public class			RunBoxWifiUnitarySILSimulator
{
	// -------------------------------------------------------------------------
	// Constants and variables
	// -------------------------------------------------------------------------

	/** the acceleration factor used in the real time SIL simulations.	 	*/
	public static final double		ACCELERATION_FACTOR = 3600.0;

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
			// for atomic model, we use an AtomicModelDescriptor
			atomicModelDescriptors.put(
					BoxWifiStateSILModel.URI,
					RTAtomicModelDescriptor.create(
							BoxWifiStateSILModel.class,
							BoxWifiStateSILModel.URI,
							BoxWifiSimulationConfigurationI.TIME_UNIT,
							null,
							ACCELERATION_FACTOR));
			atomicModelDescriptors.put(
					BoxWifiUnitTesterSILModel.URI,
					RTAtomicModelDescriptor.create(
							BoxWifiUnitTesterSILModel.class,
							BoxWifiUnitTesterSILModel.URI,
							BoxWifiSimulationConfigurationI.TIME_UNIT,
							null,
							ACCELERATION_FACTOR));

			// map that will contain the coupled model descriptors to construct
			// the simulation architecture
			Map<String,CoupledModelDescriptor> coupledModelDescriptors =
																new HashMap<>();

			// the set of submodels of the coupled model, given by their URIs
			Set<String> submodels = new HashSet<String>();
			submodels.add(BoxWifiElectricitySILModel.URI);
			submodels.add(BoxWifiStateSILModel.URI);
			submodels.add(BoxWifiUnitTesterSILModel.URI);

			// event exchanging connections between exporting and importing
			// models
			Map<EventSource,EventSink[]> connections =
										new HashMap<EventSource,EventSink[]>();

			// Connections from unit tester to state model
			connections.put(
					new EventSource(BoxWifiUnitTesterSILModel.URI,
									SwitchOnBoxWifi.class),
					new EventSink[] {
							new EventSink(BoxWifiStateSILModel.URI,
										  SwitchOnBoxWifi.class)
					});
			connections.put(
					new EventSource(BoxWifiUnitTesterSILModel.URI,
									SwitchOffBoxWifi.class),
					new EventSink[] {
							new EventSink(BoxWifiStateSILModel.URI,
										  SwitchOffBoxWifi.class)
					});
			connections.put(
					new EventSource(BoxWifiUnitTesterSILModel.URI,
									ActivateWifiBoxWifi.class),
					new EventSink[] {
							new EventSink(BoxWifiStateSILModel.URI,
										  ActivateWifiBoxWifi.class)
					});
			connections.put(
					new EventSource(BoxWifiUnitTesterSILModel.URI,
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

			// Simulation run configuration

			// this add additional time at each simulation step in
			// standard simulations (useful when debugging)
			SimulationEngine.SIMULATION_STEP_SLEEP_TIME = 0L;

			// Set verbose mode for all models
			BoxWifiElectricitySILModel.VERBOSE = true;
			BoxWifiElectricitySILModel.DEBUG = false;
			BoxWifiStateSILModel.VERBOSE = true;
			BoxWifiStateSILModel.DEBUG = false;
			BoxWifiUnitTesterSILModel.VERBOSE = true;
			BoxWifiUnitTesterSILModel.DEBUG = false;

			// create the simulator from the simulation architecture
			SimulatorI se = architecture.constructSimulator();

			// run a CLASSICAL test scenario
			TestScenarioWithSimulation classical = classical();
			System.out.println(classical.beginMessage());
			Map<String, Object> classicalRunParameters =
												new HashMap<String, Object>();
			classical.addToRunParameters(classicalRunParameters);
			se.setSimulationRunParameters(classicalRunParameters);
			Time startTime = classical.getStartTime();
			Duration d = classical.getEndTime().subtract(startTime);
			long realTimeStart = System.currentTimeMillis() + 200;
			se.startRTSimulation(realTimeStart,
								 startTime.getSimulatedTime(),
								 d.getSimulatedDuration());
			long executionDuration =					
				new Double(
						BoxWifiSimulationConfigurationI.TIME_UNIT.toMillis(1)
							* (d.getSimulatedDuration()/ACCELERATION_FACTOR)).
																	longValue();
			Thread.sleep(executionDuration + 2000L);
			SimulationReportI sr = se.getSimulatedModel().getFinalReport();
			System.out.println(sr);
			System.out.println(classical.endMessage());
			System.exit(0);
		} catch (Throwable e) {
			throw new RuntimeException(e) ;
		}
	}

	// -------------------------------------------------------------------------
	// Test scenarios
	// -------------------------------------------------------------------------

	/** the start instant used in the test scenarios.						*/
	protected static Instant	START_INSTANT =
									Instant.parse("2025-01-20T12:00:00.00Z");
	/** the end instant used in the test scenarios.							*/
	protected static Instant	END_INSTANT =
									Instant.parse("2025-01-20T18:00:00.00Z");
	/** the start time in simulated time, corresponding to
	 *  {@code START_INSTANT}.												*/
	protected static Time		START_TIME =
									new Time(0.0, TimeUnit.HOURS);

	/** standard test scenario, see Gherkin specification.				 	
	 * @throws VerboseException */
	protected static TestScenarioWithSimulation	classical()
	throws VerboseException
	{
		return new TestScenarioWithSimulation(
			"=====================================================\n" +
			"Box WiFi Classical Test Scenario\n\n" +
			"  Gherkin specification\n\n" +
			"    Feature: Box WiFi operation\n\n" +
			"      Scenario: Box WiFi switched on (WiFi disabled)\n" +
			"        Given a box WiFi that is off\n" +
			"        When it is switched on\n" +
			"        Then it is on and in BOX_ONLY mode (WiFi disabled)\n\n" +
			"      Scenario: WiFi activated\n" +
			"        Given a box WiFi that is on in BOX_ONLY mode\n" +
			"        When WiFi is activated\n" +
			"        Then it is on and in FULL_ON mode (WiFi enabled)\n\n" +
			"      Scenario: WiFi deactivated\n" +
			"        Given a box WiFi that is on in FULL_ON mode\n" +
			"        When WiFi is deactivated\n" +
			"        Then it is on and in BOX_ONLY mode (WiFi disabled)\n\n" +
			"      Scenario: Box WiFi switched off\n" +
			"        Given a box WiFi that is on\n" +
			"        When it is switched off\n" +
			"        Then it is off\n" +
			"=====================================================\n",
			"\n=====================================================\n" +
			"End Box WiFi Classical Test\n" +
			"=====================================================",
			"fake-clock-URI",	// no clock needed for purely simulation test scenario
			START_INSTANT,
			END_INSTANT,
			BoxWifiCoupledModel.URI,	// no real global architecture in this
										// scenario, use the root model URI
			START_TIME,
			(ts, simParams) -> {
				// CORRECTION CRITIQUE : Ne pas passer le TestScenarioWithSimulation
				// à BoxWifiUnitTesterSILModel qui attend un TestScenario différent
				// On passe un simple marqueur à la place
			    simParams.put(
		                ModelI.createRunParameterName(
		                    BoxWifiUnitTesterSILModel.URI,
		                    BoxWifiUnitTesterSILModel.TEST_SCENARIO_RP_NAME),
		                ts);  // ts est le TestScenarioWithSimulation
		        },
			new SimulationTestStep[]{
				new SimulationTestStep(
					BoxWifiUnitTesterSILModel.URI,
					Instant.parse("2025-01-20T13:00:00.00Z"),
					(m, t) -> {
						ArrayList<EventI> ret = new ArrayList<>();
						ret.add(new SwitchOnBoxWifi(t));
						return ret;
					},
					(m, t) -> {
						System.out.println("Step 1: Box switched ON (WiFi disabled) at " + t.getSimulatedTime() + " hours");
					}),
				new SimulationTestStep(
						BoxWifiUnitTesterSILModel.URI,
						Instant.parse("2025-01-20T14:00:00.00Z"),
						(m, t) -> {
							ArrayList<EventI> ret = new ArrayList<>();
							ret.add(new ActivateWifiBoxWifi(t));
							return ret;
						},
						(m, t) -> {
							System.out.println("Step 2: WiFi ACTIVATED at " + t.getSimulatedTime() + " hours");
						}),
				new SimulationTestStep(
						BoxWifiUnitTesterSILModel.URI,
						Instant.parse("2025-01-20T15:00:00.00Z"),
						(m, t) -> {
							ArrayList<EventI> ret = new ArrayList<>();
							ret.add(new DeactivateWifiBoxWifi(t));
							return ret;
						},
						(m, t) -> {
							System.out.println("Step 3: WiFi DEACTIVATED at " + t.getSimulatedTime() + " hours");
						}),
				new SimulationTestStep(
						BoxWifiUnitTesterSILModel.URI,
						Instant.parse("2025-01-20T16:00:00.00Z"),
						(m, t) -> {
							ArrayList<EventI> ret = new ArrayList<>();
							ret.add(new SwitchOffBoxWifi(t));
							return ret;
						},
						(m, t) -> {
							System.out.println("Step 4: Box switched OFF at " + t.getSimulatedTime() + " hours");
						})
			});
	}
}
// -----------------------------------------------------------------------------
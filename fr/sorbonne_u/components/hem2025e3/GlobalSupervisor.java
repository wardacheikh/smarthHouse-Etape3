package fr.sorbonne_u.components.hem2025e3;

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
import java.util.Map;
import java.util.concurrent.TimeUnit;
import fr.sorbonne_u.components.AbstractComponent;
import fr.sorbonne_u.components.cyphy.plugins.devs.SupervisorPlugin;
import fr.sorbonne_u.components.cyphy.plugins.devs.architectures.ComponentModelArchitecture;
import fr.sorbonne_u.components.cyphy.utils.aclocks.AcceleratedAndSimulationClock;
import fr.sorbonne_u.components.cyphy.utils.tests.TestScenarioWithSimulation;
import fr.sorbonne_u.exceptions.PreconditionException;
import fr.sorbonne_u.utils.aclocks.ClocksServer;

// -----------------------------------------------------------------------------
/**
 * The class <code>GlobalSupervisor</code> implements the supervisor component
 * for simulated runs of the HEM project.
 *
 * <p><strong>Description</strong></p>
 * 
 * <p>
 * In BCM4Java-CyPhy, simulated runs execute both the components and their
 * DEVS simulators. In this case, the supervisor component is responsible for
 * the creation, initialisation and start of execution of the global component
 * simulation architecture using models disseminated into the different
 * application components.
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
public class			GlobalSupervisor
extends		AbstractComponent
{
	// -------------------------------------------------------------------------
	// Constants and variables
	// -------------------------------------------------------------------------

	/** when true, methods trace their actions.								*/
	public static boolean		VERBOSE = false;
	/** when tracing, x coordinate of the window relative position.			*/
	public static int			X_RELATIVE_POSITION = 0;
	/** when tracing, y coordinate of the window relative position.			*/
	public static int			Y_RELATIVE_POSITION = 0;

	/** URI of the simulation architecture when a SIL simulation is
	 *  executed.															*/
	public static final String	SIL_SIM_ARCHITECTURE_URI = "hem-sil-simulator";

	// Execution/Simulation

	/** one thread for execute and one for report reception.				*/
	protected static int		NUMBER_OF_STANDARD_THREADS = 2;
	/** no need for statically defined schedulable threads.					*/
	protected static int		NUMBER_OF_SCHEDULABLE_THREADS = 0;

	/** URI of the simulation architecture to be created or the empty string
	 *  if the component does not execute as a SIL simulation.				*/
	protected final String					simArchitectureURI;
	protected TestScenarioWithSimulation	testScenario;

	// -------------------------------------------------------------------------
	// Constructors
	// -------------------------------------------------------------------------

	/**
	 * create a supervisor component.
	 * 
	 * <p><strong>Contract</strong></p>
	 * 
	 * <pre>
	 * pre	{@code simArchitectureURI != null && !simArchitectureURI.isEmpty()}
	 * post	{@code getExecutionMode().equals(ExecutionMode.INTEGRATION_TEST_WITH_SIL_SIMULATION)}
	 * </pre>
	 *
	 * @param simArchitectureURI	URI of the simulation architecture to be created or the empty string if the component does not execute as a simulation.
	 * @throws Exception			<i>to do</i>.
	 */
	protected			GlobalSupervisor(
		TestScenarioWithSimulation testScenario,
		String simArchitectureURI
		) throws Exception
	{
		super(NUMBER_OF_STANDARD_THREADS,
			  NUMBER_OF_SCHEDULABLE_THREADS);
		
		assert	simArchitectureURI != null && !simArchitectureURI.isEmpty() :
				new PreconditionException(
						"currentExecutionType.isSimulated() ||  "
						+ "(simArchitectureURI != null && "
						+ "!simArchitectureURI.isEmpty())");

		this.testScenario = testScenario;
		this.simArchitectureURI = simArchitectureURI;

		if (VERBOSE) {
			this.tracer.get().setTitle("Global supervisor");
			this.tracer.get().setRelativePosition(X_RELATIVE_POSITION,
												  Y_RELATIVE_POSITION);
			this.toggleTracing();
		}
	}

	// -------------------------------------------------------------------------
	// Component life-cycle
	// -------------------------------------------------------------------------

	/**
	 * @see fr.sorbonne_u.components.AbstractComponent#execute()
	 */
	@Override
	public void			execute() throws Exception
	{
		this.logMessage("Global superisor execution begins.");

		// Get the clock
		this.initialiseClock(ClocksServer.STANDARD_INBOUNDPORT_URI,
							 this.testScenario.getClockURI());
		AcceleratedAndSimulationClock ac =
							(AcceleratedAndSimulationClock) this.getClock();

		// Create the simulation architecture
		ComponentModelArchitecture cma =
				ComponentSimulationArchitectures.
						createComponentSimulationArchitectures(
											this.simArchitectureURI,
											GlobalCoupledModel.URI,
											ac.getSimulatedTimeUnit(),
											ac.getAccelerationFactor());
		// Create the simulation supervision plug-in and install it
		SupervisorPlugin sp = new SupervisorPlugin(cma);
		sp.setPluginURI(GlobalSupervisor.SIL_SIM_ARCHITECTURE_URI);
		this.installPlugin(sp);
		this.logMessage("plug-in installed.");
		// Construct the simulator from the architecture
		sp.constructSimulator();
		this.logMessage("simulator constructed.");
		Map<String, Object> simParams = new HashMap<>();
		this.testScenario.addToRunParameters(simParams);
		sp.setSimulationRunParameters(simParams);
		this.logMessage("run parameters set, simulation begins.");
		
		sp.startRTSimulation(
					TimeUnit.NANOSECONDS.toMillis(ac.getStartEpochNanos()),
					ac.getSimulatedStartTime().getSimulatedTime(),
					ac.getSimulatedDuration().getSimulatedDuration());

		// wait for the end of the simulation
		ac.waitUntilEnd();
		// leave some time for the simulators end of simulation catering
		// tasks
		Thread.sleep(250L);
		this.logMessage(sp.getFinalReport().toString());

		this.logMessage("Global superisor execution ends.");
	}
}
// -----------------------------------------------------------------------------

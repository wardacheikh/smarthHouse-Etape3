package fr.sorbonne_u.components.hem2025e3.equipments.hairdryer;

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

import fr.sorbonne_u.components.annotations.RequiredInterfaces; 
import fr.sorbonne_u.components.cyphy.AbstractCyPhyComponent;
import fr.sorbonne_u.components.cyphy.ExecutionMode;
import fr.sorbonne_u.components.cyphy.utils.aclocks.ClocksServerWithSimulation;
import fr.sorbonne_u.components.exceptions.BCMException;
import fr.sorbonne_u.components.exceptions.ComponentShutdownException;
import fr.sorbonne_u.components.exceptions.ComponentStartException;
import fr.sorbonne_u.components.hem2025e1.equipments.hairdryer.HairDryerImplementationI.HairDryerMode;
import fr.sorbonne_u.components.hem2025e1.equipments.hairdryer.HairDryerImplementationI.HairDryerState;
import fr.sorbonne_u.components.hem2025e1.equipments.hairdryer.connections.HairDryerConnector;
import fr.sorbonne_u.components.hem2025e1.equipments.hairdryer.connections.HairDryerOutboundPort;
import fr.sorbonne_u.components.utils.tests.TestScenario;
import fr.sorbonne_u.components.utils.tests.TestsStatistics;
import fr.sorbonne_u.exceptions.ImplementationInvariantException;
import fr.sorbonne_u.exceptions.AssertionChecking;
import fr.sorbonne_u.exceptions.InvariantException;
import fr.sorbonne_u.exceptions.PreconditionException;
import fr.sorbonne_u.utils.aclocks.ClocksServer;
import static org.junit.jupiter.api.Assertions.assertTrue;
import fr.sorbonne_u.components.hem2025e1.equipments.hairdryer.HairDryerUserCI;

// -----------------------------------------------------------------------------
/**
 * The class <code>HairDryerTesterCyPhy</code> implements the cyber-physical
 * component performing tests for the class <code>HairDryerCyPhy</code> as
 * a BCM4Java-CyPhy component.
 *
 * <p><strong>Description</strong></p>
 * 
 * <p>
 * The har dryer tester component is meant to make sure that the component
 * methods work according to its expectation. It implements the following
 * execution mode:
 * </p>
 * <ul>
 * <li></li>
 * <li></li>
 * </ul>
 * 
 * <p><strong>Implementation Invariants</strong></p>
 * 
 * <pre>
 * invariant	{@code hairDryerInboundPortURI != null && !hairDryerInboundPortURI.isEmpty()}
 * </pre>
 * 
 * <p><strong>Invariants</strong></p>
 * 
 * <pre>
 * invariant	{@code REFLECTION_INBOUND_PORT_URI != null && !REFLECTION_INBOUND_PORT_URI.isEmpty()}
 * invariant	{@code X_RELATIVE_POSITION >= 0}
 * invariant	{@code Y_RELATIVE_POSITION >= 0}
 * </pre>
 * 
 * <p>Created on : 2023-09-19</p>
 * 
 * @author	<a href="mailto:Jacques.Malenfant@lip6.fr">Jacques Malenfant</a>
 */
@RequiredInterfaces(required = {HairDryerUserCI.class})
public class			HairDryerTesterCyPhy
extends		AbstractCyPhyComponent
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

	/** standard reflection, inbound port URI for the
	 *  {@code HairDryerUnitTesterCyPhy} component.							*/
	public static final String	REFLECTION_INBOUND_PORT_URI =
											"hair-dryer-unit-tester-RIP-URI";

	/** outbound port connecting to the hair dryer component.				*/
	protected HairDryerOutboundPort		hdop;
	/** URI of the hair dryer inbound port to connect to.					*/
	protected String					hairDryerInboundPortURI;

	// Execution/Simulation

	/** one thread for the method execute.									*/
	protected static int		NUMBER_OF_STANDARD_THREADS = 1;
	/** one thread to schedule this component test actions.					*/
	protected static int		NUMBER_OF_SCHEDULABLE_THREADS = 1;

	/** collector of test statistics.										*/
	protected TestsStatistics	statistics;

	// -------------------------------------------------------------------------
	// Invariants
	// -------------------------------------------------------------------------

	/**
	 * return true if the implementation invariants are observed, false otherwise.
	 * 
	 * <p><strong>Contract</strong></p>
	 * 
	 * <pre>
	 * pre	{@code hdt != null}
	 * post	{@code true}	// no postcondition.
	 * </pre>
	 *
	 * @param hdt	instance to be tested.
	 * @return		true if the implementation invariants are observed, false otherwise.
	 */
	protected static boolean	implementationInvariants(
		HairDryerTesterCyPhy hdt
		)
	{
		assert	hdt != null : new PreconditionException("hdt != null");

		boolean ret = true;
		ret &= AssertionChecking.checkImplementationInvariant(
				hdt.hairDryerInboundPortURI != null &&
										!hdt.hairDryerInboundPortURI.isEmpty(),
				HairDryerTesterCyPhy.class, hdt,
				"hdt.hairDryerInboundPortURI != null && "
								+ "!hdt.hairDryerInboundPortURI.isEmpty()");
		return ret;
	}

	/**
	 * return true if the static invariants are observed, false otherwise.
	 * 
	 * <p><strong>Contract</strong></p>
	 * 
	 * <pre>
	 * post	{@code true}	// no postcondition.
	 * </pre>
	 *
	 * @return	true if the static invariants are observed, false otherwise.
	 */
	public static boolean	staticInvariants()
	{
		boolean ret = true;
		ret &= AssertionChecking.checkStaticInvariant(
				REFLECTION_INBOUND_PORT_URI != null &&
								!REFLECTION_INBOUND_PORT_URI.isEmpty(),
				HairDryerTesterCyPhy.class,
				"REFLECTION_INBOUND_PORT_URI != null && "
							+ "!REFLECTION_INBOUND_PORT_URI.isEmpty()");
		ret &= AssertionChecking.checkStaticInvariant(
				X_RELATIVE_POSITION >= 0,
				HairDryerTesterCyPhy.class,
				"X_RELATIVE_POSITION >= 0");
		ret &= AssertionChecking.checkStaticInvariant(
				Y_RELATIVE_POSITION >= 0,
				HairDryerTesterCyPhy.class,
				"Y_RELATIVE_POSITION >= 0");
		return ret;
	}

	/**
	 * return true if the invariants are observed, false otherwise.
	 * 
	 * <p><strong>Contract</strong></p>
	 * 
	 * <pre>
	 * pre	{@code hdt != null}
	 * post	{@code true}	// no postcondition.
	 * </pre>
	 *
	 * @param hdt	instance to be tested.
	 * @return		true if the invariants are observed, false otherwise.
	 */
	protected static boolean	invariants(HairDryerTesterCyPhy hdt)
	{
		assert	hdt != null : new PreconditionException("hdt != null");

		boolean ret = true;
		ret &= staticInvariants();
		return ret;
	}

	// -------------------------------------------------------------------------
	// Constructors
	// -------------------------------------------------------------------------

	// Standard execution for manual tests (no test scenario and no simulation)

	/**
	 * create a hair dryer tester component manual tests without test scenario
	 * or simulation.
	 * 
	 * <p><strong>Contract</strong></p>
	 * 
	 * <pre>
	 * pre	{@code !(this instanceof ComponentInterface)}
	 * pre	{@code hairDryerInboundPortURI != null && !hairDryerInboundPortURI.isEmpty()}
	 * post	{@code getCurrentExecutionMode().isStandard()}
	 * </pre>
	 *
	 * @param hairDryerInboundPortURI	URI of the hair dryer inbound port to connect to.
	 * @throws Exception				<i>to do</i>.
	 */
	protected			HairDryerTesterCyPhy(
		String hairDryerInboundPortURI
		) throws Exception
	{
		super(REFLECTION_INBOUND_PORT_URI,
			  NUMBER_OF_STANDARD_THREADS,
			  NUMBER_OF_SCHEDULABLE_THREADS);

		this.initialise(hairDryerInboundPortURI);
	}

	// Test execution with test scenario

	/**
	 * create a hair dryer tester component for tests (unit or integration)
	 * with a test scenario but no simulation.
	 * 
	 * <p><strong>Contract</strong></p>
	 * 
	 * <pre>
	 * pre	{@code !(this instanceof ComponentInterface)}
	 * pre	{@code hairDryerInboundPortURI != null && !hairDryerInboundPortURI.isEmpty()}
	 * pre	{@code executionMode != null && !executionMode.isStandard()}
	 * pre	{@code testScenario != null}
	 * post	{@code getExecutionMode().equals(executionMode)}
	 * </pre>
	 *
	 * @param hairDryerInboundPortURI	URI of the hair dryer inbound port to connect to.
	 * @param executionMode				execution mode for the next run.
	 * @param testScenario				test scenario to be executed.
	 * @throws Exception				<i>to do</i>.
	 */
	protected			HairDryerTesterCyPhy(
		String hairDryerInboundPortURI,
		ExecutionMode executionMode,
		TestScenario testScenario
		) throws Exception
	{
		super(REFLECTION_INBOUND_PORT_URI,
			  NUMBER_OF_STANDARD_THREADS,
			  NUMBER_OF_SCHEDULABLE_THREADS,
			  AssertionChecking.assertTrueAndReturnOrThrow(
				executionMode != null && !executionMode.isStandard(),
				executionMode,
				() -> new PreconditionException(
								"currentExecutionMode != null && "
								+ "!currentExecutionMode.isStandard()")),
			  AssertionChecking.assertTrueAndReturnOrThrow(
				testScenario != null,
				testScenario.getClockURI(),
				() -> new PreconditionException("testScenario != null")),
			  testScenario);

		this.initialise(hairDryerInboundPortURI);
	}

	/**
	 * initialise a hair dryer tester component.
	 * 
	 * <p><strong>Contract</strong></p>
	 * 
	 * <pre>
	 * pre	{@code hairDryerInboundPortURI != null && !hairDryerInboundPortURI.isEmpty()}
	 * post	{@code true}	// no postcondition.
	 * </pre>
	 *
	 * @param hairDryerInboundPortURI	URI of the hair dryer inbound port to connect to.
	 * @throws Exception				<i>to do</i>.
	 */
	protected void		initialise(
		String hairDryerInboundPortURI
		) throws Exception
	{
		this.hairDryerInboundPortURI = hairDryerInboundPortURI;
		this.hdop = new HairDryerOutboundPort(this);
		this.hdop.publishPort();

		if (VERBOSE) {
			this.tracer.get().setTitle("Hair dryer tester component");
			this.tracer.get().setRelativePosition(X_RELATIVE_POSITION,
												  Y_RELATIVE_POSITION);
			this.toggleTracing();
		}

		if (this.getExecutionMode().isTestWithoutSimulation()) {
			this.statistics = new TestsStatistics();
		}

		assert	HairDryerTesterCyPhy.implementationInvariants(this) :
				new ImplementationInvariantException(
						"HairDryerTester.implementationInvariants(this)");
		assert	HairDryerTesterCyPhy.invariants(this) :
				new InvariantException("HairDryerTester.invariants(this)");
	}

	// -------------------------------------------------------------------------
	// Test action methods
	// -------------------------------------------------------------------------

	/**
	 * turn on the hair dryer; method to be used in test scenario.
	 * 
	 * <p><strong>Contract</strong></p>
	 * 
	 * <pre>
	 * pre	{@code true}	// no precondition.
	 * post	{@code true}	// no postcondition.
	 * </pre>
	 *
	 * @throws Exception	<i>to do</i>.
	 */
	public void		turnOnHairDryer() throws Exception
	{
		this.hdop.turnOn();
	}

	/**
	 * turn off the hair dryer; method to be used in test scenario.
	 * 
	 * <p><strong>Contract</strong></p>
	 * 
	 * <pre>
	 * pre	{@code true}	// no precondition.
	 * post	{@code true}	// no postcondition.
	 * </pre>
	 *
	 * @throws Exception	<i>to do</i>.
	 */
	public void		turnOffHairDryer() throws Exception
	{
		this.hdop.turnOff();
	}

	/**
	 * set the hair dryer low; method to be used in test scenario.
	 * 
	 * <p><strong>Contract</strong></p>
	 * 
	 * <pre>
	 * pre	{@code true}	// no precondition.
	 * post	{@code true}	// no postcondition.
	 * </pre>
	 *
	 * @throws Exception	<i>to do</i>.
	 */
	public void		setLowHairDryer() throws Exception
	{
		this.hdop.setLow();
	}

	/**
	 * set the hair dryer high; method to be used in test scenario.
	 * 
	 * <p><strong>Contract</strong></p>
	 * 
	 * <pre>
	 * pre	{@code true}	// no precondition.
	 * post	{@code true}	// no postcondition.
	 * </pre>
	 *
	 * @throws Exception	<i>to do</i>.
	 */
	public void		setHighHairDryer() throws Exception
	{
		this.hdop.setHigh();
	}

	// -------------------------------------------------------------------------
	// Tests implementations
	// -------------------------------------------------------------------------

	/**
	 * test of the {@code getState} method when the hair dryer is off.
	 * 
	 * <p><strong>Description</strong></p>
	 * 
	 * <p>Gherkin specification:</p>
	 * <pre>
	 * Feature: Getting the state of the hair dryer
	 * 
	 *   Scenario: getting the state when off
	 *     Given the hair dryer has not been used yet
	 *     When I test the state of the hair dryer
	 *     Then the hair dryer is in its initial state
	 * </pre>
	 * 
	 * <p><strong>Contract</strong></p>
	 * 
	 * <pre>
	 * pre	{@code true}	// no precondition.
	 * post	{@code true}	// no postcondition.
	 * </pre>
	 *
	 */
	public void			testGetState()
	{
		this.logMessage("Feature: Getting the state of the hair dryer");
		this.logMessage("  Scenario: getting the state when off");
		this.logMessage("    Given the hair dryer has not been used yet");
		HairDryerState result = null;
		try {
			this.logMessage("    When I test the state of the hair dryer");
			result = this.hdop.getState();
			this.logMessage("    Then the hair dryer is in its initial state");
			if (!HairDryerCyPhy.INITIAL_STATE.equals(result)) {
				this.statistics.incorrectResult();
				this.logMessage("     but was: " + result);
			}
		} catch (Throwable e) {
			this.statistics.incorrectResult();
			this.logMessage("     but the exception " + e + " has been raised");
		}

		this.statistics.updateStatistics();
	}

	/**
	 * test of the {@code getMode} method when the hair dryer is off.
	 * 
	 * <p><strong>Description</strong></p>
	 * 
	 * <p>Gherkin specification:</p>
	 * <pre>
	 * Feature: Getting the mode of the hair dryer
	 * 
	 *   Scenario: getting the state when off
	 *     Given the hair dryer is off
	 *     When I test the mode of the hair dryer
	 *     Then the hair dryer is in its initial mode
	 * </pre>
	 * 
	 * <p><strong>Contract</strong></p>
	 * 
	 * <pre>
	 * pre	{@code true}	// no precondition.
	 * post	{@code true}	// no postcondition.
	 * </pre>
	 *
	 */
	public void			testGetMode()
	{
		this.logMessage("Feature: Getting the mode of the hair dryer");
		this.logMessage("  Scenario: getting the mode when off");
		this.logMessage("    Given the hair dryer is off");
		HairDryerState sResult = null;
		try {
			sResult = this.hdop.getState();
			if (!HairDryerState.OFF.equals(sResult)) {
				this.statistics.failedCondition();
				this.logMessage("     but was: " + sResult);
			}
		} catch (Throwable e) {
			this.statistics.failedCondition();
			this.logMessage("     but the exception " + e + " has been raised");
		}
		HairDryerMode mResult = null;
		try {
			this.logMessage("    When I test the mode of the hair dryer");
			mResult = this.hdop.getMode();
			this.logMessage("    Then the hair dryer is in its initial mode");
			if (!HairDryerCyPhy.INITIAL_MODE.equals(mResult)) {
				this.logMessage("     but was: " + mResult);	
				this.statistics.incorrectResult();
			}
		} catch (Throwable e) {
			this.statistics.incorrectResult();
			this.logMessage("     but the exception " + e + " has been raised");
		}

		this.statistics.updateStatistics();
	}

	/**
	 * test turning on and off the heir dryer.
	 * 
	 * <p><strong>Description</strong></p>
	 * 
	 * <p>Gherkin specification:</p>
	 * <pre>
	 * Feature: turning the hair dryer on and off
	 * 
	 *   Scenario: turning on when off
	 *     Given the hair dryer is off
	 *     When the hair dryer is turned on
	 *     Then the hair dryer is on
	 *     And the hair dryer is low
	 * 
	 *   Scenario: turning on when on
	 *     Given the hair dryer is on
	 *     When the hair dryer is turned on
	 *     Then a precondition exception is thrown
	 * 
	 *   Scenario: turning off when on
	 *     Given the hair dryer is on
	 *     When the hair dryer is turned off
	 *     Then the hair dryer is off
	 * 
	 *   Scenario: turning off when off
	 *     Given the hair dryer is off
	 *     When the hair dryer is turned off
	 *     Then a precondition exception is thrown
	 * </pre>
	 * 
	 * <p><strong>Contract</strong></p>
	 * 
	 * <pre>
	 * pre	{@code true}	// no precondition.
	 * post	{@code true}	// no postcondition.
	 * </pre>
	 *
	 */
	public void			testTurnOnOff()
	{
		this.logMessage("Feature: turning the hair dryer on and off");
		this.logMessage("  Scenario: turning on when off");
		HairDryerState resultState = null;
		HairDryerMode resultMode = null;
		try {
			this.logMessage("    Given the hair dryer is off");
			resultState = this.hdop.getState();
			if (!HairDryerState.OFF.equals(resultState)) {
				this.logMessage("     but was: " + resultState);
				this.statistics.failedCondition();
			}
			this.logMessage("    When the hair dryer is turned on");
			this.hdop.turnOn();
			this.logMessage("    Then the hair dryer is on");
			resultState = this.hdop.getState();
			if (!HairDryerState.ON.equals(resultState)) {
				this.logMessage("     but was: " + resultState);
				this.statistics.incorrectResult();
			}
			this.logMessage("    And the hair dryer is in mode low");
			resultMode = this.hdop.getMode();
			if (!HairDryerMode.LOW.equals(resultMode)) {
				this.logMessage("     but was: " + resultState);
				this.statistics.incorrectResult();
			}
		} catch (Throwable e) {
			this.statistics.incorrectResult();
			this.logMessage("     but the exception " + e + " has been raised");
		}

		this.statistics.updateStatistics();

		this.logMessage("  Scenario: turning on when on");
		this.logMessage("    Given the hair dryer is on");
		try {
			resultState = this.hdop.getState();
			if (!HairDryerState.ON.equals(resultState)) {
				this.logMessage("     but was: " + resultState);
				this.statistics.failedCondition();
			}
		} catch (Throwable e) {
			this.statistics.failedCondition();
			this.logMessage("     but the exception " + e + " has been raised");
		}
		this.logMessage("    When the hair dryer is turned on");
		this.logMessage("    Then a precondition exception is thrown");
		boolean old = BCMException.VERBOSE;
		try {
			BCMException.VERBOSE = false;
			this.hdop.turnOn();
			this.logMessage("     but it was not thrown");
			this.statistics.incorrectResult();
		} catch(Throwable e) {
			
		} finally {
			BCMException.VERBOSE = old;
		}

		this.statistics.updateStatistics();

		this.logMessage("  Scenario: turning off when on");
		this.logMessage("    Given the hair dryer is on");
		try {
			resultState = this.hdop.getState();
			if (!HairDryerState.ON.equals(resultState)) {
				this.logMessage("     but was: " + resultState);
				this.statistics.failedCondition();
			}
		} catch (Throwable e) {
			this.statistics.failedCondition();
			this.logMessage("     but the exception " + e + " has been raised");
		}
		this.logMessage("    When the hair dryer is turned off");
		try {
			this.hdop.turnOff();
			this.logMessage("    Then the hair dryer is off");
			resultState = this.hdop.getState();
			if (!HairDryerState.OFF.equals(resultState)) {
				this.logMessage("     but was: " + resultState);
				this.statistics.incorrectResult();
			}
		} catch (Throwable e) {
			this.statistics.incorrectResult();
			this.logMessage("     but the exception " + e + " has been raised");
		}

		this.statistics.updateStatistics();

		this.logMessage("  Scenario: turning off when off");
		this.logMessage("    Given the hair dryer is off");
		try {
			resultState = this.hdop.getState();
			if (!HairDryerState.OFF.equals(resultState)) {
				this.logMessage("     but was: " + resultState);
				this.statistics.failedCondition();
			}
		} catch (Throwable e) {
			this.statistics.failedCondition();
			this.logMessage("     but the exception " + e + " has been raised");
		}
		this.logMessage("    When the hair dryer is turned off");
		this.logMessage("    Then a precondition exception is thrown");
		old = BCMException.VERBOSE;
		try {
			BCMException.VERBOSE = false;
			this.hdop.turnOff();
			this.logMessage("     but the precondition exception was not thrown");
			this.statistics.incorrectResult();
		} catch (Throwable e) {
			
		} finally {
			BCMException.VERBOSE = old;
		}

		this.statistics.updateStatistics();
	}

	/**
	 * test switching mode of the hair dryer.
	 * 
	 * <p><strong>Description</strong></p>
	 * 
	 * <p>Gherkin specification:</p>
	 * <pre>
	 * Feature: switching the hair dryer low and high.
	 * 
	 *   Scenario: set the hair dryer high from low
	 *     Given the hair dryer is on
	 *     And the hair dryer is low
	 *     When the hair dryer is set high
	 *     Then the hair dryer is on
	 *     And  the hair dryer is high
	 * 
	 *   Scenario: set the hair dryer high from high
	 *     Given the hair dryer is on
	 *     And the hair dryer is high
	 *     When the hair dryer is set high
	 *     Then an exception is thrown
	 * 
	 *   Scenario: set the hair dryer low from high
	 *     Given the hair dryer is on
	 *     And the hair dryer is high
	 *     When the hair dryer is set low
	 *     Then the hair dryer is on
	 *     And the hair dryer is low
	 * 
	 *   Scenario: set the hair dryer low from low
	 *     Given the hair dryer is on
	 *     And the hair dryer is low
	 *     When the hair dryer is set low
	 *     Then an exception is thrown
	 * </pre>
	 * 
	 * <p><strong>Contract</strong></p>
	 * 
	 * <pre>
	 * pre	{@code true}	// no precondition.
	 * post	{@code true}	// no postcondition.
	 * </pre>
	 *
	 */
	public void			testSetLowHigh()
	{
		this.logMessage("Feature: switching the hair dryer low and high.");
		this.logMessage("  Scenario: set the hair dryer high from low");
		this.logMessage("    Given the hair dryer is on");
		HairDryerState resultState = null;
		HairDryerMode resultMode = null;
		try {
			this.hdop.turnOn();
			resultState = this.hdop.getState();
			if (!HairDryerState.ON.equals(resultState)) {
				this.logMessage("     but was: " + resultState);
				this.statistics.failedCondition();
			}
		} catch (Throwable e) {
			this.statistics.failedCondition();
			this.logMessage("     but the exception " + e + " has been raised");
		}
		try {
			this.logMessage("    And the hair dryer is low");
			resultMode = this.hdop.getMode();
			if (!HairDryerMode.LOW.equals(resultMode)) {
				this.logMessage("     but was: " + resultMode);
				this.statistics.failedCondition();
			}
		} catch (Throwable e) {
			this.statistics.failedCondition();
			this.logMessage("     but the exception " + e + " has been raised");
		}
		try {
			this.logMessage("    When the hair dryer is set high");
			this.logMessage("    Then the hair dryer is on");
			this.hdop.setHigh();
			resultState = this.hdop.getState();
			if (!HairDryerState.ON.equals(resultState)) {
				this.logMessage("     but was: " + resultState);
				this.statistics.incorrectResult();
			}
		} catch (Throwable e) {
			this.statistics.incorrectResult();
			this.logMessage("     but the exception " + e + " has been raised");
		}
		try {
			this.logMessage("    And  the hair dryer is high");
			resultMode = this.hdop.getMode();
			if (!HairDryerMode.HIGH.equals(resultMode)) {
				this.logMessage("     but was: " + resultMode);
				this.statistics.incorrectResult();
			}
		} catch (Throwable e) {
			this.statistics.incorrectResult();
			this.logMessage("     but the exception " + e + " has been raised");
		}

		this.statistics.updateStatistics();

		this.logMessage("  Scenario: set the hair dryer high from high");
		this.logMessage("    Given the hair dryer is on");
		this.logMessage("    And the hair dryer is high");
		this.logMessage("    When the hair dryer is set high");
		this.logMessage("    Then a precondition exception is thrown");
		boolean old = BCMException.VERBOSE;
		try {
			BCMException.VERBOSE = false;
			this.hdop.setHigh();
			this.logMessage("     but it was not thrown");
			this.statistics.incorrectResult();
		} catch (Throwable e) {
			
		} finally {
			BCMException.VERBOSE = old;
		}

		this.statistics.updateStatistics();

		this.logMessage("  Scenario: set the hair dryer low from high");
		this.logMessage("    Given the hair dryer is on");
		this.logMessage("    And the hair dryer is high");
		this.logMessage("    When the hair dryer is set low");
		try {
			this.hdop.setLow();
			this.logMessage("    Then the hair dryer is on");
			resultState = this.hdop.getState();
			if (!HairDryerState.ON.equals(resultState)) {
				this.logMessage("     but was: " + resultState);
				this.statistics.incorrectResult();
			}
		} catch (Throwable e) {
			this.statistics.incorrectResult();
			this.logMessage("     but the exception " + e + " has been raised");
		}
		try {
			this.logMessage("    And the hair dryer is low");
			resultMode = this.hdop.getMode();
			if (!HairDryerMode.LOW.equals(resultMode)) {
				this.logMessage("     but was: " + resultMode);
				this.statistics.incorrectResult();
			}
		} catch (Throwable e) {
			this.statistics.incorrectResult();
			this.logMessage("     but the exception " + e + " has been raised");
		}

		this.statistics.updateStatistics();

		this.logMessage("  Scenario: set the hair dryer low from low");
		this.logMessage("    Given the hair dryer is on");
		this.logMessage("    And the hair dryer is low");
		this.logMessage("    When the hair dryer is set low");
		this.logMessage("    Then a precondition exception is thrown");
		old = BCMException.VERBOSE;
		try {
			BCMException.VERBOSE = false;
			this.hdop.setLow();
			this.logMessage("     but it was not thrown");
			this.statistics.incorrectResult();
		} catch (Throwable e) {
			
		} finally {
			BCMException.VERBOSE = old;
		}

		this.statistics.updateStatistics();

		// turn off at the end of the tests
		try {
			this.hdop.turnOff();
		} catch (Throwable e) {
			assertTrue(false);
		}
	}

	/**
	 * run all unit tests.
	 * 
	 * <p><strong>Description</strong></p>
	 * 
	 * <p>The tests are run in the following order:</p>
	 * <ol>
	 * <li>{@code testGetState}</li>
	 * <li>{@code testGetMode}</li>
	 * <li>{@code testTurnOnOff(}</li>
	 * <li>{@code testSetLowHigh}</li>
	 * </ol>
	 * 
	 * <p><strong>Contract</strong></p>
	 * 
	 * <pre>
	 * pre	{@code true}	// no precondition.
	 * post	{@code true}	// no postcondition.
	 * </pre>
	 */
	protected void			runAllUnitTests()
	{
		this.testGetState();
		this.testGetMode();
		this.testTurnOnOff();
		this.testSetLowHigh();

		this.statistics.statisticsReport(this);
	}

	// -------------------------------------------------------------------------
	// Component life-cycle
	// -------------------------------------------------------------------------

	/**
	 * @see fr.sorbonne_u.components.AbstractComponent#start()
	 */
	@Override
	public synchronized void	start()
	throws ComponentStartException
	{
		super.start();

		try {
			this.doPortConnection(
							this.hdop.getPortURI(),
							hairDryerInboundPortURI,
							HairDryerConnector.class.getCanonicalName());
		} catch (Throwable e) {
			throw new ComponentStartException(e) ;
		}
	}

	/**
	 * @see fr.sorbonne_u.components.AbstractComponent#execute()
	 */
	@Override
	public synchronized void execute() throws Exception
	{
		this.traceMessage("Hair Dryer Unit Tester begins execution.\n");

		switch (this.getExecutionMode()) {
		case UNIT_TEST:
		case INTEGRATION_TEST:
			this.initialiseClock(
					ClocksServer.STANDARD_INBOUNDPORT_URI,
					this.clockURI);
			this.executeTestScenario(testScenario);
			break;
		case UNIT_TEST_WITH_SIL_SIMULATION:
		case INTEGRATION_TEST_WITH_SIL_SIMULATION:
			this.initialiseClock4Simulation(
					ClocksServerWithSimulation.STANDARD_INBOUNDPORT_URI,
					this.clockURI);
			this.executeTestScenario(testScenario);
			break;
		case INTEGRATION_TEST_WITH_HIL_SIMULATION:
		case UNIT_TEST_WITH_HIL_SIMULATION:
			throw new BCMException("HIL simulation not implemented yet!");
		case STANDARD:
			this.statistics = new TestsStatistics();
			this.traceMessage("Hair Dryer Unit Tester starts the tests.\n");
			this.runAllUnitTests();
			this.traceMessage("Hair Dryer Unit Tester ends.\n");
			break;
		default:
		}
		this.traceMessage("Hair Dryer Unit Tester ends execution.\n");
	}

	/**
	 * @see fr.sorbonne_u.components.AbstractComponent#finalise()
	 */
	@Override
	public synchronized void	finalise() throws Exception
	{
		this.doPortDisconnection(this.hdop.getPortURI());
		super.finalise();
	}

	/**
	 * @see fr.sorbonne_u.components.AbstractComponent#shutdown()
	 */
	@Override
	public synchronized void	shutdown() throws ComponentShutdownException
	{
		try {
			this.hdop.unpublishPort();
		} catch (Throwable e) {
			throw new ComponentShutdownException(e) ;
		}
		super.shutdown();
	}
}
// -----------------------------------------------------------------------------

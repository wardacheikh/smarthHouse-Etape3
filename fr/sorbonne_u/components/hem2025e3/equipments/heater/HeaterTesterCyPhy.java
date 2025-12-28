package fr.sorbonne_u.components.hem2025e3.equipments.heater;

// Copyright Jacques Malenfant, Sorbonne Universite.
// Jacques.Malenfant@lip6.fr
//
// This software is a computer program whose purpose is to provide a basic
// household management systems as an example of a cyber-physical system.
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
import fr.sorbonne_u.components.exceptions.BCMException;
import fr.sorbonne_u.components.exceptions.ComponentShutdownException;
import fr.sorbonne_u.components.exceptions.ComponentStartException;
import fr.sorbonne_u.components.hem2025e1.equipments.heater.HeaterUserCI;
import fr.sorbonne_u.components.hem2025e1.equipments.heater.HeaterExternalControlCI;
import fr.sorbonne_u.components.hem2025e1.equipments.heater.HeaterInternalControlCI;
import fr.sorbonne_u.components.hem2025e1.equipments.heater.connections.HeaterExternalControlConnector;
import fr.sorbonne_u.components.hem2025e1.equipments.heater.connections.HeaterExternalControlOutboundPort;
import fr.sorbonne_u.components.hem2025e1.equipments.heater.connections.HeaterInternalControlConnector;
import fr.sorbonne_u.components.hem2025e1.equipments.heater.connections.HeaterInternalControlOutboundPort;
import fr.sorbonne_u.components.hem2025e1.equipments.heater.connections.HeaterUserConnector;
import fr.sorbonne_u.components.hem2025e1.equipments.heater.connections.HeaterUserOutboundPort;
import fr.sorbonne_u.components.utils.tests.TestScenario;
import fr.sorbonne_u.components.utils.tests.TestsStatistics;
import fr.sorbonne_u.exceptions.ImplementationInvariantException;
import fr.sorbonne_u.exceptions.AssertionChecking;
import fr.sorbonne_u.exceptions.InvariantException;
import fr.sorbonne_u.exceptions.PreconditionException;
import fr.sorbonne_u.utils.aclocks.ClocksServer;
import fr.sorbonne_u.alasca.physical_data.Measure;
import fr.sorbonne_u.alasca.physical_data.SignalData;

// -----------------------------------------------------------------------------
/**
 * The class <code>HeaterTesterCyPhy</code> implements a component performing 
 * tests for the class <code>HeaterCyPhy</code> as a BCM component.
 *
 * <p><strong>Description</strong></p>
 * 
 * <p><strong>Implementation Invariants</strong></p>
 * 
 * <pre>
 * invariant	{@code heaterUserInboundPortURI != null && !heaterUserInboundPortURI.isEmpty()}
 * invariant	{@code heaterInternalControlInboundPortURI != null && !heaterInternalControlInboundPortURI.isEmpty()}
 * invariant	{@code heaterExternalControlInboundPortURI != null && !heaterExternalControlInboundPortURI.isEmpty()}
 * </pre>
 * 
 * <p><strong>Invariants</strong></p>
 * 
 * <pre>
 * invariant	{@code X_RELATIVE_POSITION >= 0}
 * invariant	{@code Y_RELATIVE_POSITION >= 0}
 * </pre>
 * 
 * <p>Created on : 2021-09-13</p>
 * 
 * @author	<a href="mailto:Jacques.Malenfant@lip6.fr">Jacques Malenfant</a>
 */
@RequiredInterfaces(required={HeaterUserCI.class,
							  HeaterInternalControlCI.class,
							  HeaterExternalControlCI.class})
public class			HeaterTesterCyPhy
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
											"heater-unit-tester-RIP-URI";

	/** URI of the user component interface inbound port.					*/
	protected String			heaterUserInboundPortURI;
	/** URI of the internal control component interface inbound port.		*/
	protected String			heaterInternalControlInboundPortURI;
	/** URI of the external control component interface inbound port.		*/
	protected String			heaterExternalControlInboundPortURI;

	/** user component interface inbound port.								*/
	protected HeaterUserOutboundPort			hop;
	/** internal control component interface inbound port.					*/
	protected HeaterInternalControlOutboundPort	hicop;
	/** external control component interface inbound port.					*/
	protected HeaterExternalControlOutboundPort	hecop;

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
	 * return true if the implementation invariants are observed, false
	 * otherwise.
	 * 
	 * <p><strong>Contract</strong></p>
	 * 
	 * <pre>
	 * pre	{@code ht != null}
	 * post	{@code true}	// no postcondition.
	 * </pre>
	 *
	 * @param ht	instance to be tested.
	 * @return		true if the implementation invariants are observed, false otherwise.
	 */
	protected static boolean	implementationInvariants(HeaterTesterCyPhy ht)
	{
		assert	ht != null : new PreconditionException("ht != null");

		boolean ret = true;
		ret &= AssertionChecking.checkImplementationInvariant(
				ht.heaterUserInboundPortURI != null &&
									!ht.heaterUserInboundPortURI.isEmpty(),
				HeaterTesterCyPhy.class, ht,
				"ht.heaterUserInboundPortURI != null && "
							+ "!ht.heaterUserInboundPortURI.isEmpty()");
		ret &= AssertionChecking.checkImplementationInvariant(
				ht.heaterInternalControlInboundPortURI != null &&
							!ht.heaterInternalControlInboundPortURI.isEmpty(),
				HeaterTesterCyPhy.class, ht,
				"ht.heaterInternalControlInboundPortURI != null && "
						+ "!ht.heaterInternalControlInboundPortURI.isEmpty()");
		ret &= AssertionChecking.checkImplementationInvariant(
				ht.heaterExternalControlInboundPortURI != null &&
							!ht.heaterExternalControlInboundPortURI.isEmpty(),
				HeaterTesterCyPhy.class, ht,
				"ht.heaterExternalControlInboundPortURI != null &&"
						+ "!ht.heaterExternalControlInboundPortURI.isEmpty()");
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
				X_RELATIVE_POSITION >= 0,
				HeaterTesterCyPhy.class,
				"X_RELATIVE_POSITION >= 0");
		ret &= AssertionChecking.checkStaticInvariant(
				Y_RELATIVE_POSITION >= 0,
				HeaterTesterCyPhy.class,
				"Y_RELATIVE_POSITION >= 0");
		return ret;
	}

	/**
	 * return true if the invariants is observed, false otherwise.
	 * 
	 * <p><strong>Contract</strong></p>
	 * 
	 * <pre>
	 * pre	{@code ht != null}
	 * post	{@code true}	// no postcondition.
	 * </pre>
	 *
	 * @param ht	instance to be tested.
	 * @return		true if the invariants are observed, false otherwise.
	 */
	protected static boolean	invariants(HeaterTesterCyPhy ht)
	{
		assert	ht != null : new PreconditionException("ht != null");

		boolean ret = true;
		ret &= staticInvariants();
		return ret;
	}

	// -------------------------------------------------------------------------
	// Constructors
	// -------------------------------------------------------------------------

	// Standard execution for manual tests (no test scenario and no simulation)

	/**
	 * create a heater unit tester component manual tests without  test scenario
	 * or simulation.
	 * 
	 * <p><strong>Contract</strong></p>
	 * 
	 * <pre>
	 * pre	{@code !(this instanceof ComponentInterface)}
	 * post	{@code getCurrentExecutionMode().isStandard()}
	 * </pre>
	 *
	 * @throws Exception	<i>to do</i>.
	 */
	protected			HeaterTesterCyPhy(
		String heaterUserInboundPortURI,
		String heaterInternalControlInboundPortURI,
		String heaterExternalControlInboundPortURI
		) throws Exception
	{
		super(REFLECTION_INBOUND_PORT_URI,
			  NUMBER_OF_STANDARD_THREADS,
			  NUMBER_OF_SCHEDULABLE_THREADS);
	
		this.initialise(heaterUserInboundPortURI,
						heaterInternalControlInboundPortURI,
						heaterExternalControlInboundPortURI);
	}

	// Test execution with test scenario but no simulation

	/**
	 * create a heater unit tester component manual tests with test scenario but
	 * no simulation (for this component).
	 * 
	 * <p><strong>Contract</strong></p>
	 * 
	 * <pre>
	 * pre	{@code !(this instanceof ComponentInterface)}
	 * pre	{@code heaterUserInboundPortURI != null && !heaterUserInboundPortURI.isEmpty()}
	 * pre	{@code heaterInternalControlInboundPortURI != null && !heaterInternalControlInboundPortURI.isEmpty()}
	 * pre	{@code heaterExternalControlInboundPortURI != null && !heaterExternalControlInboundPortURI.isEmpty()}
	 * pre	{@code executionMode != null && !executionMode.isStandard()}
	 * pre	{@code testScenario != null}
	 * post	{@code getExecutionMode().equals(executionMode)}
	 * </pre>
	 *
	 * @param heaterUserInboundPortURI				URI of the user component interface inbound port.
	 * @param heaterInternalControlInboundPortURI	URI of the internal control component interface inbound port.
	 * @param heaterExternalControlInboundPortURI	URI of the external control component interface inbound port.
	 * @throws Exception							<i>to do</i>.
	 */
	protected			HeaterTesterCyPhy(
		String heaterUserInboundPortURI,
		String heaterInternalControlInboundPortURI,
		String heaterExternalControlInboundPortURI,
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
		
		this.initialise(heaterUserInboundPortURI,
						heaterInternalControlInboundPortURI,
						heaterExternalControlInboundPortURI);
	}

	/**
	 * initialise a heater test component.
	 * 
	 * <p><strong>Contract</strong></p>
	 * 
	 * <pre>
	 * pre	{@code heaterUserInboundPortURI != null && !heaterUserInboundPortURI.isEmpty()}
	 * pre	{@code heaterInternalControlInboundPortURI != null && !heaterInternalControlInboundPortURI.isEmpty()}
	 * pre	{@code heaterExternalControlInboundPortURI != null && !heaterExternalControlInboundPortURI.isEmpty()}
	 * post	{@code true}	// no postcondition.
	 * </pre>
	 *
	 * @param heaterUserInboundPortURI				URI of the user component interface inbound port.
	 * @param heaterInternalControlInboundPortURI	URI of the internal control component interface inbound port.
	 * @param heaterExternalControlInboundPortURI	URI of the external control component interface inbound port.
	 * @throws Exception							<i>to do</i>.
	 */
	protected void		initialise(
		String heaterUserInboundPortURI,
		String heaterInternalControlInboundPortURI,
		String heaterExternalControlInboundPortURI
		) throws Exception
	{
		this.heaterUserInboundPortURI = heaterUserInboundPortURI;
		this.hop = new HeaterUserOutboundPort(this);
		this.hop.publishPort();
		this.heaterInternalControlInboundPortURI =
									heaterInternalControlInboundPortURI;
		this.hicop = new HeaterInternalControlOutboundPort(this);
		this.hicop.publishPort();
		this.heaterExternalControlInboundPortURI =
									heaterExternalControlInboundPortURI;
		this.hecop = new HeaterExternalControlOutboundPort(this);
		this.hecop.publishPort();

		if (VERBOSE) {
			this.tracer.get().setTitle("Heater tester component");
			this.tracer.get().setRelativePosition(X_RELATIVE_POSITION,
												  Y_RELATIVE_POSITION);
			this.toggleTracing();
		}

		this.statistics = new TestsStatistics();

		assert	HeaterTesterCyPhy.implementationInvariants(this) :
				new ImplementationInvariantException(
						"HeaterTester.implementationInvariants(this)");
		assert	HeaterTesterCyPhy.invariants(this) :
				new InvariantException("HeaterTester.invariants(this)");
	}

	// -------------------------------------------------------------------------
	// Test action helper methods
	// -------------------------------------------------------------------------

	/**
	 * return the heater user outbound port.
	 * 
	 * <p><strong>Contract</strong></p>
	 * 
	 * <pre>
	 * pre	{@code true}	// no precondition.
	 * post	{@code return != null && return.isConnected()}
	 * </pre>
	 *
	 * @return	the heater user outbound port.
	 */
	public HeaterUserOutboundPort	getHop()
	{
		return this.hop;
	}

	/**
	 * return the heater internal control outbound port.
	 * 
	 * <p><strong>Contract</strong></p>
	 * 
	 * <pre>
	 * pre	{@code true}	// no precondition.
	 * post	{@code return != null && return.isConnected()}
	 * </pre>
	 *
	 * @return	the heater internal control outbound port.
	 */
	public HeaterInternalControlOutboundPort	getHicop()
	{
		return this.hicop;
	}

	/**
	 * return the heater external control outbound port.
	 * 
	 * <p><strong>Contract</strong></p>
	 * 
	 * <pre>
	 * pre	{@code true}	// no precondition.
	 * post	{@code return != null && return.isConnected()}
	 * </pre>
	 *
	 * @return	the heater external control outbound port.
	 */
	public HeaterExternalControlOutboundPort	getHecop()
	{
		return this.hecop;
	}

	// -------------------------------------------------------------------------
	// Tests implementations
	// -------------------------------------------------------------------------

	/**
	 * test getting the state of the heater.
	 * 
	 * <p><strong>Description</strong></p>
	 * 
	 * <p>Gherkin specification</p>
	 * <p></p>
	 * <pre>
	 * Feature: getting the state of the heater
	 *   Scenario: getting the state of the heater when off
	 *     Given the heater is initialised
	 *     And the heater has not been used yet
	 *     When I test the state of the heater
	 *     Then the state of the heater is off
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
	protected void		testOff()
	{
		this.logMessage("Feature: getting the state of the heater");
		this.logMessage("  Scenario: getting the state of the heater when off");
		this.logMessage("    Given the heater is initialised");
		this.logMessage("    And the heater has not been used yet");
		try {
			this.logMessage("    When I test the state of the heater");
			boolean result = !this.hop.on();
			if (result) {
				this.logMessage("    Then the state of the heater is off");
			} else {
				this.logMessage("     but was: on");
				this.statistics.incorrectResult();
			}
		} catch (Throwable e) {
			this.statistics.incorrectResult();
			this.logMessage("     but the exception " + e + " has been raised");
		}

		this.statistics.updateStatistics();
	}

	/**
	 * test switching on and off the heater.
	 * 
	 * <p><strong>Description</strong></p>
	 * 
	 * <p>Gherkin specification</p>
	 * <p></p>
	 * <pre>
	 * Feature: switching on and off the heater
	 *   Scenario: switching on the heater when off
	 *     Given the heater is initialised
	 *     And the heater has not been used yet
	 *     When I switch on the heater
	 *     Then the state of the heater is on
	 *   Scenario: switching off the heater when on
	 *     Given the heater is initialised
	 *     And the heater is on
	 *     When I switch off the heater
	 *     Then the state of the heater is off
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
	protected void		testSwitchOnSwitchOff()
	{
		this.logMessage("Feature: switching on and off the heater");

		this.logMessage("  Scenario: switching on the heater when off");
		this.logMessage("    Given the heater is initialised");
		this.logMessage("    And the heater has not been used yet");
		boolean result;
		try {
			this.logMessage("    When I switch on the heater");
			this.hop.switchOn();
			result = this.hop.on();
			if (result) {
				this.logMessage("    Then the state of the heater is on");
			} else {
				this.logMessage("     but was: off");
				this.statistics.incorrectResult();
			}
		} catch (Throwable e) {
			this.statistics.incorrectResult();
			this.logMessage("     but the exception " + e + " has been raised");
		}

		this.statistics.updateStatistics();

		this.logMessage("  Scenario: switching off the heater when on");
		this.logMessage("    Given the heater is initialised");
		this.logMessage("    And the heater is on");
		try {
			this.logMessage("    When I switch off the heater");
			this.hop.switchOff();
			result = !this.hop.on();
			if (result) {
				this.logMessage("    Then the state of the heater is off");
			} else {
				this.logMessage("     but was: on");
				this.statistics.incorrectResult();
			}
		} catch (Throwable e) {
			this.statistics.incorrectResult();
			this.logMessage("     but the exception " + e + " has been raised");
		}

		this.statistics.updateStatistics();
	}

	/**
	 * test getting and setting the target temperature of the heater.
	 * 
	 * <p><strong>Description</strong></p>
	 * 
	 * <p>Gherkin specification</p>
	 * <p></p>
	 * <pre>
	 * Feature: getting and setting the target temperature of the heater");
	 *   Scenario: getting the target temperature through the user interface when just initialised
	 *     Given the heater is initialised
	 *     And the heater has not been used yet
	 *     And the heater is on
	 *     When I get the target temperature through the user interface
	 *     Then the target temperature of the heater is the heater standard target temperature
	 *   Scenario: getting the target temperature through the internal control interface when just initialised
	 *     Given the heater is initialised
	 *     And the heater has not been used yet
	 *     And the heater is on
	 *     When I get the target temperature through the internal control interface
	 *     Then the target temperature of the heater is the heater standard target temperature
	 *   Scenario: setting the target temperature of the heater when on
	 *     Given the heater is initialised
	 *     And the heater is on
	 *     When I set the temperature at any given temperature between -50 and 50 Celsius inclusive
	 *     Then the target temperature of the heater is the given temperature
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
	protected void		testTargetTemperature()
	{
		this.logMessage("Feature: getting and setting the target temperature"
						+ " of the heater");

		this.logMessage("  Scenario: getting the target temperature through the"
						+ " user interface when just initialised");
		this.logMessage("    Given the heater is initialised");
		this.logMessage("    And the heater has not been used yet");
		this.logMessage("    And the heater is on");
		boolean result;
		SignalData<Double> temperature = null;
		try {
			this.hop.switchOn();
			result = this.hop.on();
			if (!result) {
				this.logMessage("     but was: off");
				this.statistics.failedCondition();
			}
			this.logMessage("    When I get the target temperature through the "
							+ "user interface");
			temperature = this.hop.getTargetTemperature();
			if (temperature.getMeasure().equals(
									HeaterCyPhy.STANDARD_TARGET_TEMPERATURE)) {
				this.logMessage("    Then the target temperature of the heater"
								+ " is the heater standard target temperature");
			} else {
				this.logMessage("     but was: " + temperature);
				this.statistics.incorrectResult();
			}
		} catch (Throwable e) {
			this.statistics.incorrectResult();
			this.logMessage("     but the exception " + e + " has been raised");
		}

		this.statistics.updateStatistics();

		this.logMessage("  Scenario: getting the target temperature through the internal control interface when just initialised");
		this.logMessage("    Given the heater is initialised");
		this.logMessage("    And the heater has not been used yet");
		this.logMessage("    And the heater is on");
		try {
			this.logMessage("    When I get the target temperature through the internal control interface");
			temperature = this.hicop.getTargetTemperature();
			if (temperature.getMeasure().equals(
									HeaterCyPhy.STANDARD_TARGET_TEMPERATURE)) {
				this.logMessage("    Then the target temperature of the heater"
								+ " is the heater standard target temperature");
			} else {
				this.logMessage("     but was: " + temperature);
				this.statistics.incorrectResult();
			}
		} catch (Throwable e) {
			this.statistics.incorrectResult();
			this.logMessage("     but the exception " + e + " has been raised");
		}

		this.statistics.updateStatistics();

		this.logMessage("  Scenario: setting the target temperature of the "
						+ "heater when on");
		this.logMessage("    Given the heater is initialised");
		this.logMessage("    And the heater is on");
		try {
			result = this.hop.on();
			if (!result) {
				this.logMessage("     but was: off");
				this.statistics.failedCondition();
			}
			this.logMessage("    When I set the temperature at any given "
							+ "temperature between -50 and 50 Celsius inclusive");
			this.hop.setTargetTemperature(
					new Measure<Double>(21.0, HeaterCyPhy.TEMPERATURE_UNIT));
			temperature = this.hop.getTargetTemperature();
			if (temperature.getMeasure().getData() == 21.0 &&
				temperature.getMeasure().getMeasurementUnit().equals(
											HeaterCyPhy.TEMPERATURE_UNIT)) {
				this.logMessage("    Then the target temperature of the heater"
								+ " is the given temperature");
			} else {
				this.statistics.incorrectResult();
				this.logMessage("     but was not: " + temperature);
			}
			this.hop.switchOff();
		} catch (Throwable e) {
			this.statistics.incorrectResult();
			this.logMessage("     but the exception " + e + " has been raised");
		}

		this.statistics.updateStatistics();
	}

	/**
	 * test getting the current temperature in the room of the heater.
	 * 
	 * <p><strong>Description</strong></p>
	 * 
	 * <p>Gherkin specification</p>
	 * <p></p>
	 * <pre>
	 * Feature: getting the current temperature in the room of the heater");
	 *   Scenario: getting the current temperature when on");
	 *     Given the heater is initialised");
	 *     And the heater has not been used yet");
	 *     And the heater is on");
	 *     When I get the current temperature of the heater");
	 *     Then the current temperature is the heater standard current temperature");
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
	protected void		testCurrentTemperature()
	{
		this.logMessage("Feature: getting the current temperature"
						+ " in the room of the heater");

		this.logMessage("  Scenario: getting the current temperature through "
						+ "the user interface when on");
		this.logMessage("    Given the heater is initialised");
		this.logMessage("    And the heater has not been used yet");
		this.logMessage("    And the heater is on");
		boolean result;
		SignalData<Double> temperature = null;
		try {
			this.hop.switchOn();
			result = this.hop.on();
			if (!result) {
				this.logMessage("     but was: off");
				this.statistics.failedCondition();
			}
			this.logMessage("    When I get the current temperature of the "
							+ "heater through the user interface");
			temperature = this.hop.getCurrentTemperature();
			if (temperature.getMeasure().getData() == 
					HeaterCyPhy.FAKE_CURRENT_TEMPERATURE.getMeasure().getData() &&
				temperature.getMeasure().getMeasurementUnit().equals(
					HeaterCyPhy.FAKE_CURRENT_TEMPERATURE.getMeasure().
														getMeasurementUnit())) {
				this.logMessage("    Then the current temperature is the heater"
								+ " standard current temperature");
			} else {
				this.logMessage("     but was: " + temperature.getMeasure().getData());
				this.statistics.incorrectResult();
			}
		} catch (Throwable e) {
			this.statistics.incorrectResult();
			this.logMessage("     but the exception " + e + " has been raised");
		}

		this.statistics.updateStatistics();

		this.logMessage("  Scenario: getting the current temperature through "
						+ "the internal control interface when on");
		this.logMessage("    Given the heater is initialised");
		this.logMessage("    And the heater has not been used yet");
		this.logMessage("    And the heater is on");
		try {
			this.logMessage("    When I get the current temperature of the "
							+ "heater through the user interface");
			temperature = this.hicop.getCurrentTemperature();
			if (temperature.getMeasure().getData() == 
					HeaterCyPhy.FAKE_CURRENT_TEMPERATURE.getMeasure().getData() &&
				temperature.getMeasure().getMeasurementUnit().equals(
					HeaterCyPhy.FAKE_CURRENT_TEMPERATURE.getMeasure().
														getMeasurementUnit())) {
				this.logMessage("    Then the current temperature is the heater"
								+ " standard current temperature");
			} else {
				this.statistics.incorrectResult();
				this.logMessage("     but was: " +
										temperature.getMeasure().getData());
			}
			this.hop.switchOff();
		} catch (Throwable e) {
			this.statistics.incorrectResult();
			this.logMessage("     but the exception " + e + " has been raised");
		}

		this.statistics.updateStatistics();
	}

	/**
	 * test getting and setting the power level of the heater.
	 * 
	 * <p><strong>Description</strong></p>
	 * 
	 * <p>Gherkin specification</p>
	 * <p></p>
	 * <pre>
	 * Feature: getting and setting the power level of the heater
	 *   Scenario: getting the maximum power level through the user interface
	 *     Given the heater is initialised
	 *     When I get the maximum power level through the user interface
	 *     Then the result is the heater maximum power level
	 *   Scenario: getting the maximum power level through the external control interface
	 *     Given the heater is initialised
	 *     When I get the maximum power level through the external control interface
	 *     Then the result is the heater maximum power level
	 *   Scenario: getting the current power level through the user interface when just initialised
	 *     Given the heater is initialised
	 *     And the heater has not been used yet
	 *     And the heater is on
	 *     When I get the current power level through the user interface
	 *     Then the result is the heater maximum power level
	 *   Scenario: getting the current power level through the external control interface when just initialised
	 *     Given the heater is initialised
	 *     And the heater has not been used yet
	 *     And the heater is on
	 *     When I get the current power level through the external control interface
	 *     Then the result is the heater maximum power level
	 *   Scenario: setting the power level to a given level between 0 and the maximum power level through the user interface
	 *     Given the heater is initialised
	 *     And the heater is on
	 *     When I set the current power level through the user interface to a given level between 0 and the maximum power level
	 *     Then the current power level is the given power level
	 *   Scenario: setting the power level to a given level over the maximum power level through the user interface
	 *     Given the heater is initialised
	 *     And the heater is on
	 *     When I set the current power level through the user interface to a given level bover the maximum power level
	 *     Then the current power level is the maximum power level
	 *   Scenario: setting the power level to a given level between 0 and the maximum power level through the external control interface
	 *     Given the heater is initialised
	 *     And the heater is on
	 *     When I set the current power level through the external control interface to a given level between 0 and the maximum power level
	 *     Then the current power level is the given power level
	 *   Scenario: setting the power level to a given level over the maximum power level through the external control interface
	 *     Given the heater is initialised
	 *     And the heater is on
	 *     When I set the current power level through the external control interface to a given level over the maximum power level
	 *     Then the current power level is the maximum power level
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
	protected void		testPowerLevel()
	{
		this.logMessage("Feature: getting and setting the power level of the"
						+ " heater");

		this.logMessage("  Scenario: getting the maximum power level through "
						+ "the user interface");
		this.logMessage("    Given the heater is initialised");
		Measure<Double> powerLevel = null;
		try {
			this.logMessage("    When I get the maximum power level through the"
							+ " user interface");
			powerLevel = this.hop.getMaxPowerLevel();
			if (powerLevel.getData() == HeaterCyPhy.MAX_POWER_LEVEL.getData() &&
				powerLevel.getMeasurementUnit().equals(
							HeaterCyPhy.MAX_POWER_LEVEL.getMeasurementUnit())) {
				this.logMessage("    Then the result is the heater maximum "
								+ "power level");
			} else {
				this.statistics.incorrectResult();
				this.logMessage("     but was: " + powerLevel.getData());
			}
		} catch (Throwable e) {
			this.statistics.incorrectResult();
			this.logMessage("     but the exception " + e + " has been raised");
		}

		this.statistics.updateStatistics();

		this.logMessage("  Scenario: getting the maximum power level through the"
						+ " external control interface");
		this.logMessage("    Given the heater is initialised");
		try {
			this.logMessage("    When I get the maximum power level through the"
							+ " external control interface");
			powerLevel = this.hecop.getMaxPowerLevel();
			if (powerLevel.getData() == HeaterCyPhy.MAX_POWER_LEVEL.getData() &&
				powerLevel.getMeasurementUnit().equals(
							HeaterCyPhy.MAX_POWER_LEVEL.getMeasurementUnit())) {
				this.logMessage("    Then the result is the heater maximum "
								+ "power level");
			} else {
				this.statistics.incorrectResult();
				this.logMessage("     but was: " + powerLevel.getData());
			}
		} catch (Throwable e) {
			this.statistics.incorrectResult();
			this.logMessage("     but the exception " + e + " has been raised");
		}

		this.statistics.updateStatistics();

		this.logMessage("  Scenario: getting the current power level through "
						+ "the user interface when just initialised");
		this.logMessage("    Given the heater is initialised");
		this.logMessage("    And the heater has not been used yet");
		this.logMessage("    And the heater is on");
		boolean result;
		SignalData<Double> powerLevelSignal = null;
		try {
			this.hop.switchOn();
			result = this.hop.on();
			if (!result) {
				this.logMessage("     but was: off");
				this.statistics.failedCondition();
			}
			this.logMessage("    When I get the current power level through the"
							+ " user interface");
			powerLevelSignal =  this.hop.getCurrentPowerLevel();
			if (powerLevelSignal.getMeasure().getData() == 
											HeaterCyPhy.MAX_POWER_LEVEL.getData() &&
				powerLevelSignal.getMeasure().getMeasurementUnit().equals(
								HeaterCyPhy.MAX_POWER_LEVEL.getMeasurementUnit())) {
				this.logMessage("    Then the result is the heater maximum "
								+ "power level");
			} else {
				this.logMessage("     but was: " +
									powerLevelSignal.getMeasure().getData());
				this.statistics.incorrectResult();
			}
		} catch (Throwable e) {
			this.statistics.incorrectResult();
			this.logMessage("     but the exception " + e + " has been raised");
		}

		this.statistics.updateStatistics();

		this.logMessage("  Scenario: getting the current power level through "
						+ "the external control interface when just initialised");
		this.logMessage("    Given the heater is initialised");
		this.logMessage("    And the heater has not been used yet");
		this.logMessage("    And the heater is on");
		try {
			this.logMessage("    When I get the current power level through the"
							+ " external control interface");
			powerLevelSignal =  this.hecop.getCurrentPowerLevel();
			if (powerLevelSignal.getMeasure().getData() == 
									HeaterCyPhy.MAX_POWER_LEVEL.getData() &&
				powerLevelSignal.getMeasure().getMeasurementUnit().equals(
								HeaterCyPhy.MAX_POWER_LEVEL.getMeasurementUnit())) {
				this.logMessage("    Then the result is the heater maximum "
								+ "power level");
			} else {
				this.statistics.incorrectResult();
				this.logMessage("     but was: " +
									powerLevelSignal.getMeasure().getData());
			}
		} catch (Throwable e) {
			this.statistics.incorrectResult();
			this.logMessage("     but the exception " + e + " has been raised");
		}

		this.statistics.updateStatistics();

		this.logMessage("  Scenario: setting the power level through the user "
						+ "interface to a given level between 0 and the maximum"
						+ " power level");
		this.logMessage("    Given the heater is initialised");
		this.logMessage("    And the heater is on");
		try {
			this.logMessage("    When I set the current power level through the"
							+ " user interface to a given level between 0 and"
							+ " the maximum power level");
			this.hop.setCurrentPowerLevel(
					new Measure<Double>(HeaterCyPhy.MAX_POWER_LEVEL.getData()/2.0,
										HeaterCyPhy.POWER_UNIT));
			powerLevelSignal = this.hop.getCurrentPowerLevel();
			if (powerLevelSignal.getMeasure().getData() ==
										HeaterCyPhy.MAX_POWER_LEVEL.getData()/2.0 &&
				powerLevelSignal.getMeasure().getMeasurementUnit().equals(
								HeaterCyPhy.MAX_POWER_LEVEL.getMeasurementUnit())) {
				this.logMessage("    Then the current power level is the given"
								+ " level");
			} else {
				this.statistics.incorrectResult();
				this.logMessage("     but was: " +
									powerLevelSignal.getMeasure().getData());
			}
		} catch (Throwable e) {
			this.statistics.incorrectResult();
			this.logMessage("     but the exception " + e + " has been raised");
		}

		this.statistics.updateStatistics();

		this.logMessage("  Scenario: setting the power level through the user "
						+ "interface to a given level over the maximum"
						+ " power level");
		this.logMessage("    Given the heater is initialised");
		this.logMessage("    And the heater is on");
		try {
			this.logMessage("    When I set the current power level through the"
							+ " user interface to a given level over the maximum"
							+ " power level");
			this.hop.setCurrentPowerLevel(
					new Measure<Double>(HeaterCyPhy.MAX_POWER_LEVEL.getData() + 1.0,
										HeaterCyPhy.POWER_UNIT));
			powerLevelSignal = this.hop.getCurrentPowerLevel();
			if (powerLevelSignal.getMeasure().getData() ==
											HeaterCyPhy.MAX_POWER_LEVEL.getData() &&
				powerLevelSignal.getMeasure().getMeasurementUnit().equals(
								HeaterCyPhy.MAX_POWER_LEVEL.getMeasurementUnit())) {
				this.logMessage("    Then the current power level is the maximum"
								+ " power level");
			} else {
				this.logMessage("     but was: " +
									powerLevelSignal.getMeasure().getData());
				this.statistics.incorrectResult();
			}
		} catch (Throwable e) {
			this.statistics.incorrectResult();
			this.logMessage("     but the exception " + e + " has been raised");
		}

		this.statistics.updateStatistics();

		this.logMessage("  Scenario: setting the power level through the "
						+ "external control interface to a given level between "
						+ "0 and the maximum power level");
		this.logMessage("    Given the heater is initialised");
		this.logMessage("    And the heater is on");
		try {
			this.logMessage("    When I set the current power level through the"
							+ " external control interface to a given level "
							+ "between 0 and the maximum power level");
			this.hop.setCurrentPowerLevel(
					new Measure<Double>(HeaterCyPhy.MAX_POWER_LEVEL.getData()/2.0,
										HeaterCyPhy.POWER_UNIT));
			powerLevelSignal = this.hecop.getCurrentPowerLevel();
			if (powerLevelSignal.getMeasure().getData() ==
										HeaterCyPhy.MAX_POWER_LEVEL.getData()/2.0 &&
					powerLevelSignal.getMeasure().getMeasurementUnit().equals(
								HeaterCyPhy.MAX_POWER_LEVEL.getMeasurementUnit())) {
				this.logMessage("    Then the current power level is the given"
								+ " level");
			} else {
				this.statistics.incorrectResult();
				this.logMessage("     but was: " +
									powerLevelSignal.getMeasure().getData());
			}
		} catch (Throwable e) {
			this.statistics.incorrectResult();
			this.logMessage("     but the exception " + e + " has been raised");
		}

		this.statistics.updateStatistics();

		this.logMessage("  Scenario: setting the power level through the "
						+ "external control interface to a given level over the"
						+ " maximum power level");
		this.logMessage("    Given the heater is initialised");
		this.logMessage("    And the heater is on");
		try {
			this.logMessage("    When I set the current power level through the"
							+ " external control interface to a given level over"
							+ " the maximum power level");
			this.hop.setCurrentPowerLevel(
					new Measure<Double>(HeaterCyPhy.MAX_POWER_LEVEL.getData() + 1.0,
										HeaterCyPhy.POWER_UNIT));
			powerLevelSignal = this.hecop.getCurrentPowerLevel();
			if (powerLevelSignal.getMeasure().getData() ==
											HeaterCyPhy.MAX_POWER_LEVEL.getData() &&
				powerLevelSignal.getMeasure().getMeasurementUnit().equals(
								HeaterCyPhy.MAX_POWER_LEVEL.getMeasurementUnit())) {
				this.logMessage("    Then the current power level is the maximum"
								+ " power level");
			} else {
				this.logMessage("     but was: " +
									powerLevelSignal.getMeasure().getData());
				this.statistics.incorrectResult();
			}
		} catch (Throwable e) {
			this.statistics.incorrectResult();
			this.logMessage("     but the exception " + e + " has been raised");
		}

		this.statistics.updateStatistics();
	}

	/**
	 * run all unit tests.
	 * 
	 * <p><strong>Contract</strong></p>
	 * 
	 * <pre>
	 * pre	{@code true}	// no precondition.
	 * post	{@code true}	// no postcondition.
	 * </pre>
	 *
	 */
	protected void		runAllUnitTests()
	{
		this.testOff();
		this.testSwitchOnSwitchOff();
		this.testTargetTemperature();
		this.testCurrentTemperature();
		this.testPowerLevel();

		this.statistics.statisticsReport(this);
	}

	// -------------------------------------------------------------------------
	// Component life-cycle
	// -------------------------------------------------------------------------

	/**
	 * @see fr.sorbonne_u.components.AbstractComponent#start()
	 */
	@Override
	public synchronized void	start() throws ComponentStartException
	{
		super.start();

		try {
			this.doPortConnection(
					this.hop.getPortURI(),
					this.heaterUserInboundPortURI,
					HeaterUserConnector.class.getCanonicalName());
			this.doPortConnection(
					this.hicop.getPortURI(),
					heaterInternalControlInboundPortURI,
					HeaterInternalControlConnector.class.getCanonicalName());
			this.doPortConnection(
					this.hecop.getPortURI(),
					heaterExternalControlInboundPortURI,
					HeaterExternalControlConnector.class.getCanonicalName());
		} catch (Throwable e) {
			throw new ComponentStartException(e) ;
		}
	}

	/**
	 * @see fr.sorbonne_u.components.AbstractComponent#execute()
	 */
	@Override
	public synchronized void	execute() throws Exception
	{
		this.traceMessage("Heater Unit Tester begins execution.\n");

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
					ClocksServer.STANDARD_INBOUNDPORT_URI,
					this.clockURI);
			this.executeTestScenario(testScenario);
			break;
		case INTEGRATION_TEST_WITH_HIL_SIMULATION:
		case UNIT_TEST_WITH_HIL_SIMULATION:
			throw new BCMException("HIL simulation not implemented yet!");
		case STANDARD:
			this.statistics = new TestsStatistics();
			this.traceMessage("Heater Unit Tester starts the tests.\n");
			this.runAllUnitTests();
			this.traceMessage("Heater Unit Tester ends.\n");
			break;
		default:
		}

		this.traceMessage("Heater Unit Tester ends execution.\n");
	}

	/**
	 * @see fr.sorbonne_u.components.AbstractComponent#finalise()
	 */
	@Override
	public synchronized void	finalise() throws Exception
	{
		this.doPortDisconnection(this.hop.getPortURI());
		this.doPortDisconnection(this.hicop.getPortURI());
		this.doPortDisconnection(this.hecop.getPortURI());
		super.finalise();
	}

	/**
	 * @see fr.sorbonne_u.components.AbstractComponent#shutdown()
	 */
	@Override
	public synchronized void	shutdown() throws ComponentShutdownException
	{
		try {
			this.hop.unpublishPort();
			this.hicop.unpublishPort();
			this.hecop.unpublishPort();
		} catch (Throwable e) {
			throw new ComponentShutdownException(e) ;
		}
		super.shutdown();
	}
}
// -----------------------------------------------------------------------------

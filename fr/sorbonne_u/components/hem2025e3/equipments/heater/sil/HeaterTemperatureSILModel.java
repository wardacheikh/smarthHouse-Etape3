package fr.sorbonne_u.components.hem2025e3.equipments.heater.sil;

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

import java.util.ArrayList;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import fr.sorbonne_u.components.hem2025e1.equipments.heater.Heater.HeaterState;
import fr.sorbonne_u.components.cyphy.interfaces.ModelStateAccessI.VariableValue;
import fr.sorbonne_u.components.cyphy.plugins.devs.AtomicSimulatorPlugin;
import fr.sorbonne_u.components.hem2025e1.equipments.heater.HeaterExternalControlI;
import fr.sorbonne_u.components.hem2025e2.GlobalReportI;
import fr.sorbonne_u.components.hem2025e2.equipments.heater.mil.HeaterSimulationConfigurationI;
import fr.sorbonne_u.components.hem2025e2.equipments.heater.mil.events.DoNotHeat;
import fr.sorbonne_u.components.hem2025e2.equipments.heater.mil.events.Heat;
import fr.sorbonne_u.components.hem2025e2.equipments.heater.mil.events.HeaterEventI;
import fr.sorbonne_u.components.hem2025e2.equipments.heater.mil.events.SwitchOffHeater;
import fr.sorbonne_u.components.hem2025e3.equipments.heater.sil.events.SIL_SetPowerHeater;
import fr.sorbonne_u.devs_simulation.exceptions.MissingRunParameterException;
import fr.sorbonne_u.devs_simulation.exceptions.NeoSim4JavaException;
import fr.sorbonne_u.devs_simulation.hioa.annotations.ImportedVariable;
import fr.sorbonne_u.devs_simulation.hioa.annotations.InternalVariable;
import fr.sorbonne_u.devs_simulation.hioa.annotations.ModelImportedVariable;
import fr.sorbonne_u.devs_simulation.hioa.models.AtomicHIOA;
import fr.sorbonne_u.devs_simulation.hioa.models.vars.DerivableValue;
import fr.sorbonne_u.devs_simulation.hioa.models.vars.Value;
import fr.sorbonne_u.devs_simulation.models.annotations.ModelExternalEvents;
import fr.sorbonne_u.devs_simulation.models.events.Event;
import fr.sorbonne_u.devs_simulation.models.events.EventI;
import fr.sorbonne_u.devs_simulation.models.time.Duration;
import fr.sorbonne_u.devs_simulation.models.time.Time;
import fr.sorbonne_u.devs_simulation.simulators.interfaces.AtomicSimulatorI;
import fr.sorbonne_u.devs_simulation.simulators.interfaces.SimulationReportI;
import fr.sorbonne_u.devs_simulation.utils.Pair;
import fr.sorbonne_u.devs_simulation.utils.StandardLogger;
import fr.sorbonne_u.devs_simulation.utils.AssertionChecking;

// -----------------------------------------------------------------------------
/**
 * The class <code>HeaterTemperatureSILModel</code> defines a simulation model
 * for the temperature inside a room equipped with a heater.
 *
 * <p><strong>Description</strong></p>
 * 
 * <p>
 * The model is implemented as an atomic HIOA model. A differential equation
 * defines the temperature variation over time. It uses a very simple
 * mathematical model where the derivative is proportional to the difference
 * between the current temperature and the temperature that influences the
 * current one. In fact, there are two temperatures that influences the current
 * temperature of the room:
 * </p>
 * <ol>
 * <li>the temperature outside the house (room) where the coefficient
 *   applied to the difference between the outside temperature and the
 *   current temperature models the thermal insulation of the walls
 *   ({@code INSULATION_TRANSFER_CONSTANT});</li>
 * <li>the temperature of the heater when it heats where the coefficient
 *   applied to the difference between the heater temperature
 *   ({@code STANDARD_HEATING_TEMP}) and the current temperature models the
 *   heat diffusion over the house (room)
 *   ({@code HEATING_TRANSFER_CONSTANT}); the heat diffusion is not constant
 *   but rather proportional to the current power level of the heater.</li>
 * </ol>
 * <p>
 * The resulting differential equation is integrated using the Euler method
 * with a predefined integration step. The initial state of the model is
 * a state not heating and the initial temperature given by
 * {@code INITIAL_TEMPERATURE}.
 * </p>
 * <p>
 * Whether the current temperature evolves under the influence of the outside
 * temperature only or also the heating temperature depends upon the state,
 * which in turn is modified through the reception of imported events
 * {@code Heat} and {@code DoNotHeat}. The external temperature is imported
 * from another model simulating the environment. The current temperature is
 * exported to be used by other models.
 * </p>
 * 
 * <ul>
 * <li>Imported events:
 *   {@code SwitchOffHeater},
 *   {@code Heat},
 *   {@code DoNotHeat}</li>
 * <li>Exported events: none</li>
 * <li>Imported variables: none</li>
 * <li>Exported variables:
 *   name = {@code externalTemperature}, type = {@code Double}</li>
 * </ul>
 * 
 * <p><strong>Implementation Invariants</strong></p>
 * 
 * <pre>
 * invariant	{@code TEMPERATURE_UPDATE_TOLERANCE >= 0.0}
 * invariant	{@code POWER_HEAT_TRANSFER_TOLERANCE >= 0.0}
 * invariant	{@code INSULATION_TRANSFER_CONSTANT > 0.0}
 * invariant	{@code MIN_HEATING_TRANSFER_CONSTANT > 0.0}
 * invariant	{@code STEP > 0.0}
 * invariant	{@code currentState != null}
 * invariant	{@code integrationStep.getSimulatedDuration() > 0.0}
 * invariant	{@code !isStateInitialised() || start != null}
 * invariant	{@code currentHeatingPower >= 0.0}
 * invariant	{@code currentTemperature != null}
 * </pre>
 * 
 * <p><strong>Invariants</strong></p>
 * 
 * <pre>
 * invariant	{@code URI != null && !URI.isEmpty()}
 * </pre>
 * 
 * <p>Created on : 2023-09-29</p>
 * 
 * @author	<a href="mailto:Jacques.Malenfant@lip6.fr">Jacques Malenfant</a>
 */
@ModelExternalEvents(imported = {SwitchOffHeater.class,
		 						 SIL_SetPowerHeater.class,
		 						 Heat.class,
		 						 DoNotHeat.class})
@ModelImportedVariable(name = "externalTemperature", type = Double.class)
// -----------------------------------------------------------------------------
public class			HeaterTemperatureSILModel
extends		AtomicHIOA
implements	SIL_HeaterOperationI
{
	// -------------------------------------------------------------------------
	// Constants and variables
	// -------------------------------------------------------------------------

	private static final long		serialVersionUID = 1L;

	// The following variables should be considered constant but can be changed
	// before the first model instance is created to adapt the simulation
	// scenario.

	/** URI for a model; works when only one instance is created.			*/
	public static String		URI = HeaterTemperatureSILModel.class.
															getSimpleName();
	/** when true, leaves a trace of the execution of the model.			*/
	public static boolean		VERBOSE = true;
	/** when true, leaves a debugging trace of the execution of the model.	*/
	public static boolean		DEBUG = false;

	// TODO: define as simulation run parameters
	/** temperature of the room (house) when the simulation begins.			*/
	public static double		INITIAL_TEMPERATURE = 19.005;
	/** wall insulation heat transfer constant in the differential equation.*/
	protected static double 	INSULATION_TRANSFER_CONSTANT = 12.5;
	/** heating transfer constant in the differential equation when the
	 *  heating power is maximal.											*/
	protected static double		MIN_HEATING_TRANSFER_CONSTANT = 40.0;
	/** temperature of the heating plate in the heater.						*/
	protected static double		STANDARD_HEATING_TEMP = 300.0;
	/** update tolerance for the temperature <i>i.e.</i>, shortest elapsed
	 *  time since the last update under which the temperature is not
	 *  changed by the update to avoid too large computation errors.		*/
	protected static double		TEMPERATURE_UPDATE_TOLERANCE = 0.0001;
	/** the minimal power under which the temperature derivative must be 0.	*/
	protected static double		POWER_HEAT_TRANSFER_TOLERANCE = 0.0001;
	/** integration step for the differential equation(assumed in hours).	*/
	protected static double		STEP = 60.0/3600.0;	// 60 seconds

	/** current state of the heater; for the temperature model, only two
	 *  states are relevant: heating, which is {@code HeaterState.HEATING},
	 *  or notheating, which assimilates to on but not heating <i>i.e.</i>,
	 *  {@code HeaterState.ON}.												*/
	protected HeaterState		currentState = HeaterState.ON;
	/** the current heating power between 0 and
	 *  {@code HeaterElectricityModel.MAX_HEATING_POWER}.					*/
	protected double			currentHeatingPower;

	// Simulation run variables

	/** integration step as a duration, including the time unit.			*/
	protected final Duration	integrationStep;
	/** accumulator to compute the mean external temperature for the
	 *  simulation report.													*/
	protected double			temperatureAcc;
	/** the simulation time of start used to compute the mean temperature.	*/
	protected Time				start;
	/** the mean temperature over the simulation duration for the simulation
	 *  report.																*/
	protected double			meanTemperature;

	// -------------------------------------------------------------------------
	// HIOA model variables
	// -------------------------------------------------------------------------

	/** current external temperature in Celsius.							*/
	@ImportedVariable(type = Double.class)
	protected Value<Double>					externalTemperature;
	/** current temperature in the room.									*/
	@InternalVariable(type = Double.class)
	protected final DerivableValue<Double>	currentTemperature =
												new DerivableValue<Double>(this);

	// -------------------------------------------------------------------------
	// Invariants
	// -------------------------------------------------------------------------

	/**
	 * return true if the static implementation invariants are observed, false
	 * otherwise.
	 * 
	 * <p><strong>Contract</strong></p>
	 * 
	 * <pre>
	 * pre	{@code true}	// no precondition.
	 * post	{@code true}	// no postcondition.
	 * </pre>
	 *
	 * @return	true if the static implementation invariants are observed, false otherwise.
	 */
	protected static boolean	staticImplementationInvariants()
	{
		boolean ret = true;
		ret &= AssertionChecking.checkStaticImplementationInvariant(
				TEMPERATURE_UPDATE_TOLERANCE >= 0.0,
				HeaterTemperatureSILModel.class,
				"TEMPERATURE_UPDATE_TOLERANCE >= 0.0");
		ret &= AssertionChecking.checkStaticImplementationInvariant(
				POWER_HEAT_TRANSFER_TOLERANCE >= 0.0,
				HeaterTemperatureSILModel.class,
				"POWER_HEAT_TRANSFER_TOLERANCE >= 0.0");
		ret &= AssertionChecking.checkStaticImplementationInvariant(
				INSULATION_TRANSFER_CONSTANT > 0.0,
				HeaterTemperatureSILModel.class,
				"INSULATION_TRANSFER_CONSTANT > 0.0");
		ret &= AssertionChecking.checkStaticImplementationInvariant(
				MIN_HEATING_TRANSFER_CONSTANT > 0.0,
				HeaterTemperatureSILModel.class,
				"MIN_HEATING_TRANSFER_CONSTANT > 0.0");
		ret &= AssertionChecking.checkStaticImplementationInvariant(
				STEP > 0.0,
				HeaterTemperatureSILModel.class,
				"STEP > 0.0");
		return ret;
	}

	/**
	 * return true if the implementation invariants are observed, false
	 * otherwise.
	 * 
	 * <p><strong>Contract</strong></p>
	 * 
	 * <pre>
	 * pre	{@code instance != null}
	 * post	{@code true}	// no postcondition.
	 * </pre>
	 *
	 * @param instance	instance to be tested.
	 * @return			true if the implementation invariants are observed, false otherwise.
	 */
	protected static boolean	implementationInvariants(
		HeaterTemperatureSILModel instance
		)
	{
		assert	instance != null :
				new NeoSim4JavaException("Precondition violation: "
						+ "instance != null");

		boolean ret = true;
		ret &= staticImplementationInvariants();
		ret &= AssertionChecking.checkImplementationInvariant(
				instance.currentState != null,
				HeaterTemperatureSILModel.class,
				instance,
				"currentState != null");
		ret &= AssertionChecking.checkImplementationInvariant(
				instance.integrationStep.getSimulatedDuration() > 0.0,
				HeaterTemperatureSILModel.class,
				instance,
				"integrationStep.getSimulatedDuration() > 0.0");
		ret &= AssertionChecking.checkImplementationInvariant(
				!instance.isStateInitialised() || instance.start != null,
				HeaterTemperatureSILModel.class,
				instance,
				"!isStateInitialised() || start != null");
		ret &= AssertionChecking.checkImplementationInvariant(
				instance.currentHeatingPower >= 0.0,
				HeaterTemperatureSILModel.class,
				instance,
				"currentHeatingPower >= 0.0");
		ret &= AssertionChecking.checkImplementationInvariant(
				instance.currentTemperature != null,
				HeaterTemperatureSILModel.class,
				instance,
				"currentTemperature != null");
		return ret;
	}

	/**
	 * return true if the static invariants are observed, false otherwise.
	 * 
	 * <p><strong>Contract</strong></p>
	 * 
	 * <pre>
	 * pre	{@code true}	// no precondition.
	 * post	{@code true}	// no postcondition.
	 * </pre>
	 *
	 * @return	true if the static invariants are observed, false otherwise.
	 */
	public static boolean	staticInvariants()
	{
		boolean ret = true;
		ret &= HeaterSimulationConfigurationI.staticInvariants();
		ret &= AssertionChecking.checkStaticInvariant(
				URI != null && !URI.isEmpty(),
				HeaterTemperatureSILModel.class,
				"URI != null && !URI.isEmpty()");
		return ret;
	}

	/**
	 * return true if the invariants are observed, false otherwise.
	 * 
	 * <p><strong>Contract</strong></p>
	 * 
	 * <pre>
	 * pre	{@code instance != null}
	 * post	{@code true}	// no postcondition.
	 * </pre>
	 *
	 * @param instance	instance to be tested.
	 * @return			true if the invariants are observed, false otherwise.
	 */
	protected static boolean	invariants(
		HeaterTemperatureSILModel instance
		)
	{
		assert	instance != null :
				new NeoSim4JavaException(
						"Precondition violation: instance != null");

		boolean ret = true;
		ret &= staticInvariants();
		return ret;
	}

	// -------------------------------------------------------------------------
	// Constructors
	// -------------------------------------------------------------------------

	/**
	 * create a <code>HeaterTemperatureModel</code> instance.
	 * 
	 * <p><strong>Contract</strong></p>
	 * 
	 * <pre>
	 * pre	{@code uri == null || !uri.isEmpty()}
	 * pre	{@code simulatedTimeUnit != null}
	 * pre	{@code simulationEngine != null && !simulationEngine.isModelSet()}
	 * pre	{@code simulationEngine instanceof AtomicEngine}
	 * post	{@code !isDebugModeOn()}
	 * post	{@code getURI() != null && !getURI().isEmpty()}
	 * post	{@code uri == null || getURI().equals(uri)}
	 * post	{@code getSimulatedTimeUnit().equals(simulatedTimeUnit)}
	 * post	{@code getSimulationEngine().equals(simulationEngine)}
	 * </pre>
	 *
	 * @param uri				URI of the model.
	 * @param simulatedTimeUnit	time unit used for the simulation time.
	 * @param simulationEngine	simulation engine to which the model is attached.
	 * @throws Exception		<i>to do</i>.
	 */
	public				HeaterTemperatureSILModel(
		String uri,
		TimeUnit simulatedTimeUnit,
		AtomicSimulatorI simulationEngine
		) throws Exception
	{
		super(uri, simulatedTimeUnit, simulationEngine);

		this.integrationStep = new Duration(STEP, simulatedTimeUnit);
		this.getSimulationEngine().setLogger(new StandardLogger());

		assert	HeaterTemperatureSILModel.implementationInvariants(this) :
				new NeoSim4JavaException(
						"HeaterTemperatureModel.implementationInvariants(this)");
		assert	HeaterTemperatureSILModel.invariants(this) :
				new NeoSim4JavaException(
						"HeaterTemperatureModel.implementationInvariants(this)");
	}

	// -------------------------------------------------------------------------
	// Methods
	// -------------------------------------------------------------------------

	/**
	 * @see fr.sorbonne_u.components.hem2025e3.equipments.heater.sil.SIL_HeaterOperationI#setState(fr.sorbonne_u.components.hem2025e1.equipments.heater.Heater.HeaterState)
	 */
	@Override
	public void			setState(HeaterState s)
	{
		this.currentState = s;

		assert	HeaterTemperatureSILModel.implementationInvariants(this) :
				new NeoSim4JavaException(
						"HeaterTemperatureModel.implementationInvariants(this)");
		assert	HeaterTemperatureSILModel.invariants(this) :
				new NeoSim4JavaException(
						"HeaterTemperatureModel.implementationInvariants(this)");
	}

	/**
	 * @see fr.sorbonne_u.components.hem2025e3.equipments.heater.sil.SIL_HeaterOperationI#getState()
	 */
	@Override
	public HeaterState	getState()
	{
		return this.currentState;
	}

	/**
	 * For software-in-the-loop tests with simulation, return the current value
	 * of the {@code currentTemperature} variable.
	 * 
	 * <p><strong>Contract</strong></p>
	 * 
	 * <pre>
	 * pre	{@code true}	// no precondition.
	 * post	{@code true}	// no postcondition.
	 * </pre>
	 *
	 * @return	the current value of the {@code currentTemperature} variable.
	 */
	public VariableValue<Double>	getCurrentTemperature()
	{
		return new VariableValue<Double>(
							this.currentTemperature.getValue(),
							this.currentTemperature.getTime());
	}

	/**
	 * @see fr.sorbonne_u.components.hem2025e3.equipments.heater.sil.SIL_HeaterOperationI#setCurrentHeatingPower(double, fr.sorbonne_u.devs_simulation.models.time.Time)
	 */
	@Override
	public void			setCurrentHeatingPower(double newPower, Time t)
	{
		assert	newPower >= 0.0 &&
					newPower <= HeaterExternalControlI.MAX_POWER_LEVEL.getData() :
				new NeoSim4JavaException(
					"Precondition violation: newPower >= 0.0 && "
					+ "newPower <= HeaterElectricityModel.MAX_HEATING_POWER,"
					+ " but newPower = " + newPower);

		this.currentHeatingPower = newPower;

		assert	HeaterStateSILModel.implementationInvariants(this) :
				new NeoSim4JavaException(
						"HeaterStateModel.implementationInvariants(this)");
		assert	HeaterStateSILModel.invariants(this) :
				new NeoSim4JavaException("HeaterStateModel.invariants(this)");
	}

	/**
	 * compute the current heat transfer constant given the current heating
	 * power of the heater.
	 * 
	 * <p><strong>Contract</strong></p>
	 * 
	 * <pre>
	 * pre	{@code true}	// no precondition.
	 * post	{@code true}	// no postcondition.
	 * </pre>
	 *
	 * @return	the current heat transfer constant.
	 */
	protected double	currentHeatTransfertConstant()
	{
		// the following formula is just a mathematical trick to get a heat
		// transfer constant that grows as the power gets lower, hence the
		// derivative given by the differential equation will be lower when
		// the power gets lower, what is physically awaited.
		double c = 1.0/(MIN_HEATING_TRANSFER_CONSTANT *
							HeaterExternalControlI.MAX_POWER_LEVEL.getData());
		return 1.0/(c*this.currentHeatingPower);
	}

	/**
	 * compute the current derivative of the room temperature.
	 * 
	 * <p><strong>Contract</strong></p>
	 * 
	 * <pre>
	 * pre	{@code true}	// no precondition.
	 * post	{@code true}	// no postcondition.
	 * </pre>
	 *
	 * @param current	current temperature of the room.
	 * @return			the current derivative.
	 */
	protected double	computeDerivatives(Double current)
	{
		double currentTempDerivative = 0.0;
		if (this.currentState == HeaterState.HEATING) {
			// the heating contribution: temperature difference between the
			// heating temperature and the room temperature divided by the
			// heat transfer constant taking into account the size of the
			// room
			if (this.currentHeatingPower > POWER_HEAT_TRANSFER_TOLERANCE) {
				currentTempDerivative =
						(STANDARD_HEATING_TEMP - current)/
											this.currentHeatTransfertConstant();
			}
		}

		// the cooling contribution: difference between the external temperature
		// and the temperature of the room divided by the insulation transfer
		// constant taking into account the surface of the walls.
		Time t = this.getCurrentStateTime();
		currentTempDerivative +=
				(this.externalTemperature.evaluateAt(t) - current)/
												INSULATION_TRANSFER_CONSTANT;
		return currentTempDerivative;
	}

	/**
	 * compute the current temperature given that a duration of {@code deltaT}
	 * has elapsed since the last update.
	 * 
	 * <p><strong>Contract</strong></p>
	 * 
	 * <pre>
	 * pre	{@code deltaT >= 0.0}
	 * post	{@code true}	// no postcondition.
	 * </pre>
	 *
	 * @param deltaT	the duration of the step since the last update.
	 * @return			the new temperature in celsius.
	 */
	protected double	computeNewTemperature(double deltaT)
	{
		Time t = this.currentTemperature.getTime();
		double oldTemp = this.currentTemperature.evaluateAt(t);
		double newTemp;

		if (deltaT > TEMPERATURE_UPDATE_TOLERANCE) {
			// update the room temperature using the Euler integration of the
			// differential equation
			double derivative = this.currentTemperature.getFirstDerivative();
			newTemp = oldTemp + derivative*deltaT;
		} else {
			newTemp = oldTemp;
		}

		// accumulate the temperature*time to compute the mean temperature
		this.temperatureAcc += ((oldTemp + newTemp)/2.0) * deltaT;
		return newTemp;
	}

	// -------------------------------------------------------------------------
	// DEVS simulation protocol
	// -------------------------------------------------------------------------

	/**
	 * @see fr.sorbonne_u.devs_simulation.models.interfaces.ModelI#setSimulationRunParameters(Map)
	 */
	@Override
	public void			setSimulationRunParameters(
		Map<String, Object> simParams
		) throws MissingRunParameterException
	{
		super.setSimulationRunParameters(simParams);

		// this gets the reference on the owner component which is required
		// to have simulation models able to make the component perform some
		// operations or tasks or to get the value of variables held by the
		// component when necessary.
		if (simParams.containsKey(
						AtomicSimulatorPlugin.OWNER_RUNTIME_PARAMETER_NAME)) {
			// by the following, all of the logging will appear in the owner
			// component logger
			this.getSimulationEngine().setLogger(
						AtomicSimulatorPlugin.createComponentLogger(simParams));
		}
	}

	/**
	 * 
	 * @see fr.sorbonne_u.devs_simulation.hioa.models.AtomicHIOA#initialiseState(fr.sorbonne_u.devs_simulation.models.time.Time)
	 */
	@Override
	public void			initialiseState(Time initialTime)
	{
		this.temperatureAcc = 0.0;
		this.start = initialTime;
		this.currentHeatingPower =
				HeaterExternalControlI.MAX_POWER_LEVEL.getData();

		if (VERBOSE) {
			this.logMessage("simulation begins.");
		}

		super.initialiseState(initialTime);

		assert	HeaterTemperatureSILModel.implementationInvariants(this) :
				new NeoSim4JavaException(
						"HeaterTemperatureModel.implementationInvariants(this)");
		assert	HeaterTemperatureSILModel.invariants(this) :
				new NeoSim4JavaException(
						"HeaterTemperatureModel.implementationInvariants(this)");
	}

	/**
	 * @see fr.sorbonne_u.devs_simulation.hioa.models.interfaces.VariableInitialisationI#useFixpointInitialiseVariables()
	 */
	@Override
	public boolean		useFixpointInitialiseVariables()
	{
		return true;
	}

	/**
	 * @see fr.sorbonne_u.devs_simulation.hioa.models.interfaces.VariableInitialisationI#fixpointInitialiseVariables()
	 */
	@Override
	public Pair<Integer, Integer>	fixpointInitialiseVariables()
	{
		int justInitialised = 0;
		int notInitialisedYet = 0;

		// Only one variable must be initialised, the current temperature, and
		// it depends upon only one variable, the external temperature.
		if (!this.currentTemperature.isInitialised() &&
									this.externalTemperature.isInitialised()) {
			// If the current temperature is not initialised yet but the
			// external temperature is, then initialise the current temperature
			// and say one more variable is initialised at this execution.
			double derivative = this.computeDerivatives(INITIAL_TEMPERATURE);
			this.currentTemperature.initialise(INITIAL_TEMPERATURE, derivative);
			justInitialised++;
		} else if (!this.currentTemperature.isInitialised()) {
			// If the external temperature is not initialised and the current
			// temperature either, then say one more variable has not been
			// initialised yet at this execution, forcing another execution
			// to reach the fix point.
			notInitialisedYet++;
		}

		assert	HeaterTemperatureSILModel.implementationInvariants(this) :
				new NeoSim4JavaException(
						"HeaterTemperatureModel.implementationInvariants(this)");
		assert	HeaterTemperatureSILModel.invariants(this) :
				new NeoSim4JavaException(
						"HeaterTemperatureModel.implementationInvariants(this)");

		return new Pair<>(justInitialised, notInitialisedYet);
	}

	/**
	 * @see fr.sorbonne_u.devs_simulation.models.interfaces.AtomicModelI#output()
	 */
	@Override
	public ArrayList<EventI>	output()
	{
		return null;
	}

	/**
	 * @see fr.sorbonne_u.devs_simulation.models.interfaces.ModelI#timeAdvance()
	 */
	@Override
	public Duration		timeAdvance()
	{
		return this.integrationStep;
	}

	/**
	 * @see fr.sorbonne_u.devs_simulation.models.AtomicModel#userDefinedInternalTransition(fr.sorbonne_u.devs_simulation.models.time.Duration)
	 */
	@Override
	public void			userDefinedInternalTransition(Duration elapsedTime)
	{
		// First, update the temperature (i.e., the value of the continuous
		// variable) until the current time.
		double newTemp =
				this.computeNewTemperature(elapsedTime.getSimulatedDuration());
		// Next, compute the new derivative
		double newDerivative = this.computeDerivatives(newTemp);
		// Finally, set the new temperature value and derivative
		this.currentTemperature.setNewValue(
						newTemp,
						newDerivative,
						new Time(this.getCurrentStateTime().getSimulatedTime(),
								 this.getSimulatedTimeUnit()));

		// Tracing
		if (VERBOSE) {
			String mark = this.currentState == HeaterState.HEATING ? " (h)" : " (-)";
			StringBuffer message = new StringBuffer();
			message.append(this.currentTemperature.getTime().getSimulatedTime());
			message.append(mark);
			message.append(" : ");
			message.append(this.currentTemperature.getValue());
			this.logMessage(message.toString());
		}

		super.userDefinedInternalTransition(elapsedTime);

		assert	HeaterTemperatureSILModel.implementationInvariants(this) :
				new NeoSim4JavaException(
						"HeaterTemperatureModel.implementationInvariants(this)");
		assert	HeaterTemperatureSILModel.invariants(this) :
				new NeoSim4JavaException(
						"HeaterTemperatureModel.implementationInvariants(this)");
	}

	/**
	 * @see fr.sorbonne_u.devs_simulation.models.AtomicModel#userDefinedExternalTransition(fr.sorbonne_u.devs_simulation.models.time.Duration)
	 */
	@Override
	public void			userDefinedExternalTransition(Duration elapsedTime)
	{
		// get the vector of current external events
		ArrayList<EventI> currentEvents = this.getStoredEventAndReset();
		// when this method is called, there is at least one external event,
		// and for the heater model, there will be exactly one by
		// construction.
		assert	currentEvents != null && currentEvents.size() == 1;

		Event ce = (Event) currentEvents.get(0);
		assert	ce instanceof HeaterEventI;

		if (VERBOSE) {
			StringBuffer sb = new StringBuffer("executing the external event: ");
			sb.append(ce.eventAsString());
			sb.append(".");
			this.logMessage(sb.toString());
		}

		// First, update the temperature (i.e., the value of the continuous
		// variable) until the current time.
		double newTemp =
				this.computeNewTemperature(elapsedTime.getSimulatedDuration());
		// Then, update the current state of the heater.
		ce.executeOn(this);
		// Next, compute the new derivative
		double newDerivative = this.computeDerivatives(newTemp);

		if (elapsedTime.getSimulatedDuration() > TEMPERATURE_UPDATE_TOLERANCE) {
			// Finally, set the new temperature value and derivative
			this.currentTemperature.setNewValue(
					newTemp,
					newDerivative,
					new Time(this.getCurrentStateTime().getSimulatedTime()
										+ elapsedTime.getSimulatedDuration(),
							 this.getSimulatedTimeUnit()));
		}

		super.userDefinedExternalTransition(elapsedTime);

		assert	HeaterTemperatureSILModel.implementationInvariants(this) :
				new NeoSim4JavaException(
						"HeaterTemperatureModel.implementationInvariants(this)");
		assert	HeaterTemperatureSILModel.invariants(this) :
				new NeoSim4JavaException(
						"HeaterTemperatureModel.implementationInvariants(this)");
	}

	/**
	 * @see fr.sorbonne_u.devs_simulation.hioa.models.AtomicHIOA#endSimulation(fr.sorbonne_u.devs_simulation.models.time.Time)
	 */
	@Override
	public void			endSimulation(Time endTime)
	{
		this.meanTemperature =
				this.temperatureAcc/
						endTime.subtract(this.start).getSimulatedDuration();

		if (VERBOSE) {
			this.logMessage("simulation ends.");
		}
		super.endSimulation(endTime);
	}

	// -------------------------------------------------------------------------
	// Optional DEVS simulation protocol: simulation report
	// -------------------------------------------------------------------------

	/**
	 * The class <code>HeaterTemperatureReport</code> implements the
	 * simulation report for the <code>HeaterTemperatureModel</code>.
	 *
	 * <p><strong>Description</strong></p>
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
	 * <p>Created on : 2023-09-29</p>
	 * 
	 * @author	<a href="mailto:Jacques.Malenfant@lip6.fr">Jacques Malenfant</a>
	 */
	public static class		HeaterTemperatureReport
	implements	SimulationReportI, GlobalReportI
	{
		private static final long serialVersionUID = 1L;
		protected String	modelURI;
		protected double	meanTemperature;

		public			HeaterTemperatureReport(
			String modelURI,
			double meanTemperature
			)
		{
			super();
			this.modelURI = modelURI;
			this.meanTemperature = meanTemperature;
		}

		@Override
		public String	getModelURI()
		{
			return this.modelURI;
		}

		@Override
		public String	printout(String indent)
		{
			StringBuffer ret = new StringBuffer(indent);
			ret.append("---\n");
			ret.append(indent);
			ret.append('|');
			ret.append(this.modelURI);
			ret.append(" report\n");
			ret.append(indent);
			ret.append('|');
			ret.append("mean temperature = ");
			ret.append(this.meanTemperature);
			ret.append(".\n");
			ret.append(indent);
			ret.append("---\n");
			return ret.toString();
		}
	}

	/**
	 * @see fr.sorbonne_u.devs_simulation.models.interfaces.ModelI#getFinalReport()
	 */
	@Override
	public SimulationReportI	getFinalReport()
	{
		return new HeaterTemperatureReport(this.getURI(), this.meanTemperature);
	}
}
// -----------------------------------------------------------------------------

package fr.sorbonne_u.components.hem2025e3.equipments.meter.sil;

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
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import fr.sorbonne_u.components.cyphy.plugins.devs.AtomicSimulatorPlugin;
import fr.sorbonne_u.components.hem2025e1.equipments.meter.ElectricMeterImplementationI;
import fr.sorbonne_u.components.hem2025e2.GlobalReportI;
import fr.sorbonne_u.components.hem2025e2.GlobalSimulationConfigurationI;
import fr.sorbonne_u.components.hem2025e2.equipments.batteries.mil.events.BatteriesRequiredPowerChanged;
import fr.sorbonne_u.components.hem2025e2.equipments.generator.mil.events.GeneratorRequiredPowerChanged;
import fr.sorbonne_u.components.hem2025e2.utils.Electricity;
import fr.sorbonne_u.components.hem2025e3.equipments.meter.ElectricMeterCyPhy;
import fr.sorbonne_u.devs_simulation.exceptions.MissingRunParameterException;
import fr.sorbonne_u.devs_simulation.exceptions.NeoSim4JavaException;
import fr.sorbonne_u.devs_simulation.hioa.annotations.ImportedVariable;
import fr.sorbonne_u.devs_simulation.hioa.annotations.InternalVariable;
import fr.sorbonne_u.devs_simulation.hioa.annotations.ModelImportedVariable;
import fr.sorbonne_u.devs_simulation.hioa.annotations.ModelImportedVariables;
import fr.sorbonne_u.devs_simulation.hioa.models.AtomicHIOA;
import fr.sorbonne_u.devs_simulation.hioa.models.vars.Value;
import fr.sorbonne_u.devs_simulation.models.events.EventI;
import fr.sorbonne_u.devs_simulation.models.time.Duration;
import fr.sorbonne_u.devs_simulation.models.time.Time;
import fr.sorbonne_u.devs_simulation.simulators.interfaces.AtomicSimulatorI;
import fr.sorbonne_u.devs_simulation.simulators.interfaces.SimulationReportI;
import fr.sorbonne_u.devs_simulation.utils.Pair;
import fr.sorbonne_u.devs_simulation.utils.StandardLogger;
import fr.sorbonne_u.exceptions.PreconditionException;
import fr.sorbonne_u.devs_simulation.utils.AssertionChecking;
import java.text.NumberFormat;

// -----------------------------------------------------------------------------
/**
 * The class <code>ElectricMeterElectricityModel</code> defines the simulation
 * model for the electric meter electricity consumption.
 *
 * <p><strong>Description</strong></p>
 * 
 * <p>
 * This model is an HIOA model that imports variables, hence shows how this kind
 * of models are programmed.
 * </p>
 * 
 * <ul>
 * <li>Imported events: none</li>
 * <li>Exported events: none</li>
 * <li>Imported variables:
 *   <ul>
 *   <i>name = {@code currentHeaterIntensity}, type = {@code Double}</li>
 *   <i>name = {@code currentHairDryerIntensity}, type = {@code Double}</li>
 *   <i>name = {@code solarPanelOutputPower}, type = {@code Double}</li>
 *   <i>name = {@code batteriesInputPower}, type = {@code Double}</li>
 *   <i>name = {@code batteriesOutputPower}, type = {@code Double}</li>
 *   <i>name = {@code generatorOutputPower}, type = {@code Double}</li>
 *   </ul>
 * </li>
 * <li>Exported variables:
 *   <ul>
 *   <i>name = {@code batteriesRequiredPower}, type = {@code Double}</li>
 *   <i>name = {@code generatorRequiredPower}, type = {@code Double}</li>
 *   </ul>
 * </li>
 * </ul>
 * 
 * <p><strong>Implementation Invariants</strong></p>
 * 
 * <pre>
 * invariant	{@code STEP > 0.0}
 * invariant	{@code evaluationStep.getSimulatedDuration() > 0.0}
 * invariant	{@code solarPanelOutputPower == null || !solarPanelOutputPower.isInitialised() || solarPanelOutputPower.getValue() >= 0.0}
 * invariant	{@code currentHeaterIntensity == null || !currentHeaterIntensity.isInitialised() || currentHeaterIntensity.getValue() >= 0.0}
 * invariant	{@code currentHairDryerIntensity == null || !currentHairDryerIntensity.isInitialised() || currentHairDryerIntensity.getValue() >= 0.0}
 * invariant	{@code currentIntensity != null && (!currentIntensity.isInitialised() || currentIntensity.getValue() >= 0.0)}
 * invariant	{@code cumulativeConsumption != null && (!cumulativeConsumption.isInitialised() || cumulativeConsumption.getValue() >= 0.0)}
 * invariant	{@code powerProduction != null && (!powerProduction.isInitialised() || powerProduction.getValue() >= 0.0)}
 * </pre>
 * 
 * <p><strong>Invariants</strong></p>
 * 
 * <pre>
 * invariant	{@code URI != null && !URI.isEmpty()}
 * </pre>
 * 
 * <p>Created on : 2023-10-02</p>
 * 
 * @author	<a href="mailto:Jacques.Malenfant@lip6.fr">Jacques Malenfant</a>
 */
// -----------------------------------------------------------------------------
//@ModelExternalEvents(
//	exported = {BatteriesRequiredPowerChanged.class,
//				GeneratorRequiredPowerChanged.class}
//	)
@ModelImportedVariables(
	{@ModelImportedVariable(name = "currentHeaterIntensity",
							type = Double.class),
	 @ModelImportedVariable(name = "currentHairDryerIntensity",
	 						type = Double.class)
//	 @ModelImportedVariable(name = "solarPanelOutputPower",
//	 						type = Double.class),
//	 @ModelImportedVariable(name = "batteriesInputPower",
//	 						type = Double.class),
//	 @ModelImportedVariable(name = "batteriesOutputPower",
//	 						type = Double.class),
//	 @ModelImportedVariable(name = "generatorOutputPower",
//	 						type = Double.class)
	})
//@ModelExportedVariables(
//	{@ModelExportedVariable(name = "batteriesRequiredPower",
//							type = Double.class),
//	 @ModelExportedVariable(name = "generatorRequiredPower",
//	 						type = Double.class)
//	})
// -----------------------------------------------------------------------------
public class			ElectricMeterElectricitySILModel
extends		AtomicHIOA
{
	// -------------------------------------------------------------------------
	// Constants and variables
	// -------------------------------------------------------------------------

	private static final long	serialVersionUID = 1L;
	/** URI for a model; works when only one instance is created.			*/
	public static final String	URI = ElectricMeterElectricitySILModel.class.
																getSimpleName();
	/** when true, leaves a trace of the execution of the model.			*/
	public static boolean		VERBOSE = true;
	/** when true, leaves a debugging trace of the execution of the model.	*/
	public static boolean		DEBUG = false;
	/** when comparing floating point values, use this tolerance to get
	 *  the result of the comparison.										*/
	protected static final double	TOLERANCE  = 1.0e-08;

	/** evaluation step for the equation (assumed in hours).				*/
	protected static final double	STEP = 60.0/3600.0;	// 60 seconds

	/** evaluation step as a duration, including the time unit.				*/
	protected final Duration	evaluationStep;
	/** when true, the generator power model must be notified of a change
	 *  in the required power at the next output.						 	*/
	protected boolean			generatorToBeNotified;
	/** when true, the batteries power model must be notified of a change
	 *  in the required power at the next output.						 	*/
	protected boolean			batteriesToBeNotified;

	/** the component that owns and run this simulation model.				*/
	protected ElectricMeterCyPhy				ownerComponent;
	/** final report of the simulation run.									*/
	protected ElectricMeterElectricityReport	finalReport;

	// -------------------------------------------------------------------------
	// HIOA model variables
	// -------------------------------------------------------------------------

//	/** power consumed from the electric circuit to charge the batteries
//	 *  in {@code MeasurementUnit.AMPERES}.									*/
//	@ImportedVariable(type = Double.class)
//	protected Value<Double>			batteriesInputPower;
//	/** power required by the electric circuit from the batteries in
//	 *  {@code MeasurementUnit.AMPERES}.									*/
//	@ExportedVariable(type = Double.class)
//	protected Value<Double>			batteriesRequiredPower = new Value<>(this);
//	/** power delivered to the electric circuit by the batteries in
//	 *  {@code MeasurementUnit.AMPERES}.									*/
//	@ImportedVariable(type = Double.class)
//	protected Value<Double>			batteriesOutputPower;

//	/** current power production of the solar panel in amperes.				*/
//	@ImportedVariable(type = Double.class)
//	protected Value<Double>			solarPanelOutputPower;

//	/** current power production of the generator in amperes.				*/
//	@ImportedVariable(type = Double.class)
//	protected Value<Double>			generatorOutputPower;
//	/** current power required from the generator.							*/
//	@ExportedVariable(type = Double.class)
//	protected Value<Double>			generatorRequiredPower =
//												new Value<Double>(this);

	/** current intensity of the heater in amperes.							*/
	@ImportedVariable(type = Double.class)
	protected Value<Double>			currentHeaterIntensity;
	/** current intensity of the hair dryer in amperes.						*/
	@ImportedVariable(type = Double.class)
	protected Value<Double>			currentHairDryerIntensity;

//	/** current total power production of the house in the power unit
//	 *  defined by the electric meter.										*/
//	@InternalVariable(type = Double.class)
//	protected final Value<Double>	powerProduction =
//												new Value<Double>(this);
	/** current total consumed intensity of the house in the power unit
	 *  defined by the electric meter.										*/
	@InternalVariable(type = Double.class)
	protected final Value<Double>	currentIntensity =
												new Value<Double>(this);
	/** current total consumption of the house in kwh.						*/
	@InternalVariable(type = Double.class)
	protected final Value<Double>	cumulativeConsumption =
												new Value<Double>(this);
	/** largest negative power balance during the simulation.				*/
	@InternalVariable(type = Double.class)
	protected Value<Double>			largestPowerDebt = new Value<>(this);
	/** largest positive power margin during the simulation.				*/
	@InternalVariable(type = Double.class)
	protected Value<Double>			largestPowerMargin = new Value<>(this);

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
	 * pre	{@code instance != null}
	 * post	{@code true}	// no postcondition.
	 * </pre>
	 *
	 * @param instance	instance to be tested.
	 * @return			true if the implementation invariants are observed, false otherwise.
	 */
	protected static boolean	implementationInvariants(
		ElectricMeterElectricitySILModel instance
		)
	{
		assert	instance != null :
				new NeoSim4JavaException("Precondition violation: "
						+ "instance != null");

		boolean ret = true;
		ret &= AssertionChecking.checkImplementationInvariant(
				STEP > 0.0,
				ElectricMeterElectricitySILModel.class,
				instance,
				"STEP > 0.0");
		ret &= AssertionChecking.checkImplementationInvariant(
				instance.evaluationStep.getSimulatedDuration() > 0.0,
				ElectricMeterElectricitySILModel.class,
				instance,
				"evaluationStep.getSimulatedDuration() > 0.0");
//		ret &= AssertionChecking.checkImplementationInvariant(
//				instance.solarPanelOutputPower == null ||
//					!instance.solarPanelOutputPower.isInitialised() ||
//						instance.solarPanelOutputPower.getValue() >= 0.0,
//				ElectricMeterElectricitySILModel.class,
//				instance,
//				"solarPanelOutputPower == null || "
//				+ "!solarPanelOutputPower.isInitialised() || "
//				+ "solarPanelOutputPower.getValue() >= 0.0");
		ret &= AssertionChecking.checkImplementationInvariant(
				instance.currentHeaterIntensity == null ||
					!instance.currentHeaterIntensity.isInitialised() ||
						instance.currentHeaterIntensity.getValue() >= 0.0,
				ElectricMeterElectricitySILModel.class,
				instance,
				"currentHeaterIntensity == null || "
				+ "!currentHeaterIntensity.isInitialised() || "
				+ "currentHeaterIntensity.getValue() >= 0.0");
		ret &= AssertionChecking.checkImplementationInvariant(
				instance.currentHairDryerIntensity == null ||
					!instance.currentHairDryerIntensity.isInitialised() ||
						instance.currentHairDryerIntensity.getValue() >= 0.0,
				ElectricMeterElectricitySILModel.class,
				instance,
				"currentHairDryerIntensity == null || !i "
				+ "currentHairDryerIntensity.isInitialised() || "
				+ "currentHairDryerIntensity.getValue() >= 0.0");
		ret &= AssertionChecking.checkImplementationInvariant(
				instance.currentIntensity != null &&
					(!instance.currentIntensity.isInitialised() ||
								instance.currentIntensity.getValue() >= 0.0),
				ElectricMeterElectricitySILModel.class,
				instance,
				"currentIntensity != null && "
				+ "(!currentIntensity.isInitialised() || "
				+ "currentIntensity.getValue() >= 0.0)");
		ret &= AssertionChecking.checkImplementationInvariant(
				instance.cumulativeConsumption != null &&
					(!instance.cumulativeConsumption.isInitialised() ||
								instance.cumulativeConsumption.getValue() >= 0.0),
				ElectricMeterElectricitySILModel.class,
				instance,
				"cumulativeConsumption != null && "
				+ "(!cumulativeConsumption.isInitialised() || "
				+ "cumulativeConsumption.getValue() >= 0.0)");
//		ret &= AssertionChecking.checkImplementationInvariant(
//				instance.powerProduction != null &&
//					(!instance.powerProduction.isInitialised() ||
//								instance.powerProduction.getValue() >= 0.0),
//				ElectricMeterElectricitySILModel.class,
//				instance,
//				"powerProduction != null && "
//				+ "(!powerProduction.isInitialised() || "
//				+ "powerProduction.getValue() >= 0.0)");
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
	 * @return			true if the invariants are observed, false otherwise.
	 */
	protected static boolean	staticInvariants()
	{
		boolean ret = true;
		ret &= GlobalSimulationConfigurationI.staticInvariants();
		ret &= AssertionChecking.checkStaticInvariant(
				URI != null && !URI.isEmpty(),
				ElectricMeterElectricitySILModel.class,
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
		ElectricMeterElectricitySILModel instance
		)
	{
		assert	instance != null :
				new NeoSim4JavaException("Precondition violation: "
						+ "instance != null");

		boolean ret = true;
		ret &= staticInvariants();
		return ret;
	}

	// -------------------------------------------------------------------------
	// Constructors
	// -------------------------------------------------------------------------

	/**
	 * create an <code>ElectricMeterElectricityModel</code> instance.
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
	public				ElectricMeterElectricitySILModel(
		String uri,
		TimeUnit simulatedTimeUnit,
		AtomicSimulatorI simulationEngine
		) throws Exception
	{
		super(uri, simulatedTimeUnit, simulationEngine);

		this.evaluationStep = new Duration(STEP, this.getSimulatedTimeUnit());
		this.getSimulationEngine().setLogger(new StandardLogger());

		assert	ElectricMeterElectricitySILModel.implementationInvariants(this) :
				new NeoSim4JavaException(
						"ElectricMeterElectricityModel."
						+ "implementationInvariants(this)");
		assert	ElectricMeterElectricitySILModel.invariants(this) :
				new NeoSim4JavaException(
						"ElectricMeterElectricityModel.invariants(this)");
	}

	// -------------------------------------------------------------------------
	// Methods
	// -------------------------------------------------------------------------

	/**
	 * update the cumulative electricity consumption in kwh given the current
	 * intensity has been constant for the duration {@code d}.
	 * 
	 * <p><strong>Contract</strong></p>
	 * 
	 * <pre>
	 * pre	{@code d != null}
	 * post	{@code true}	// no postcondition.
	 * </pre>
	 *
	 * @param d	duration for which the intensity has been maintained.
	 */
	protected void		updateCumulativeConsumption(Duration d)
	{
		double c = this.cumulativeConsumption.getValue();
		c += Electricity.computeConsumption(
				d,
				ElectricMeterCyPhy.TENSION.getData() *
											this.currentIntensity.getValue());
		Time t = this.cumulativeConsumption.getTime().add(d);
		this.cumulativeConsumption.setNewValue(c, t);

		assert	ElectricMeterElectricitySILModel.implementationInvariants(this) :
				new NeoSim4JavaException(
						"ElectricMeterElectricityModel."
						+ "implementationInvariants(this)");
		assert	ElectricMeterElectricitySILModel.invariants(this) :
				new NeoSim4JavaException(
						"ElectricMeterElectricityModel.invariants(this)");
	}

	/**
	 * compute the current total intensity.
	 * 
	 * <p><strong>Contract</strong></p>
	 * 
	 * <pre>
	 * pre	{@code true}	// no precondition.
	 * post	{@code true}	// no postcondition.
	 * </pre>
	 *
	 * @return the current total intensity of electric consumption.
	 */
	protected double	computeTotalIntensity()
	{
		// simple sum of all incoming intensities
		return this.currentHairDryerIntensity.getValue()
					+ this.currentHeaterIntensity.getValue()
//					+ this.batteriesInputPower.getValue()
					;
	}

	/**
	 * compute the current total power production.
	 * 
	 * <p><strong>Contract</strong></p>
	 * 
	 * <pre>
	 * pre	{@code true}	// no precondition.
	 * post	{@code true}	// no postcondition.
	 * </pre>
	 *
	 * @return	the current total power production.
	 */
//	protected double	computeTotalPowerProduction()
//	{
//		return this.solarPanelOutputPower.getValue() +
//			   this.generatorOutputPower.getValue() +
//			   this.batteriesOutputPower.getValue();
//	}

	// -------------------------------------------------------------------------
	// DEVS simulation protocol
	// -------------------------------------------------------------------------

	/**
	 * @see fr.sorbonne_u.devs_simulation.hioa.models.interfaces.VariableInitialisationI#useFixpointInitialiseVariables()
	 */
	@Override
	public boolean		useFixpointInitialiseVariables()
	{
		return true;
	}

	/**
	 * @see fr.sorbonne_u.devs_simulation.hioa.models.AtomicHIOA#initialiseState(fr.sorbonne_u.devs_simulation.models.time.Time)
	 */
	@Override
	public void			initialiseState(Time initialTime)
	{
		this.generatorToBeNotified = false;
		this.batteriesToBeNotified = false;
		
		super.initialiseState(initialTime);
	}

	/**
	 * @see fr.sorbonne_u.devs_simulation.hioa.models.interfaces.VariableInitialisationI#fixpointInitialiseVariables()
	 */
	@Override
	public Pair<Integer, Integer>	fixpointInitialiseVariables()
	{
		int justInitialised = 0;
		int notInitialisedYet = 0;

		// the variable batteriesRequiredPower is exported and does not depend
		// upon any other variable, hence it can be immediately initialised
//		if (!this.batteriesRequiredPower.isInitialised()) {
//			this.batteriesRequiredPower.initialise(0.0);
//			justInitialised++;
//			if (DEBUG) {
//				this.logMessage(
//						"fixpointInitialiseVariables batteriesRequiredPower = "
//						+ this.batteriesRequiredPower.getValue());
//			}
//		}

		// the variable generatorRequiredPower is exported and does not depend
		// upon any other variable, hence it can be immediately initialised
//		if (!this.generatorRequiredPower.isInitialised()) {
//			this.generatorRequiredPower.initialise(0.0);
//			justInitialised++;
//			if (DEBUG) {
//				this.logMessage(
//						"fixpointInitialiseVariables generatorRequiredPower = "
//						+ this.generatorRequiredPower.getValue());
//			}
//		}

		if (!this.currentIntensity.isInitialised()
//				&& this.batteriesInputPower.isInitialised()
				&& this.currentHairDryerIntensity.isInitialised()
				&& this.currentHeaterIntensity.isInitialised()) {
			double i = this.computeTotalIntensity();
			this.currentIntensity.initialise(i);
			this.cumulativeConsumption.initialise(0.0);
			this.largestPowerDebt.initialise(0.0);
			this.largestPowerMargin.initialise(0.0);
			justInitialised += 4;
		} else if (!this.currentIntensity.isInitialised()) {
			notInitialisedYet += 4;
		}

//		if (!this.powerProduction.isInitialised()
//				&& this.solarPanelOutputPower.isInitialised()
//				&& this.generatorOutputPower.isInitialised()
//				&& this.batteriesOutputPower.isInitialised()) {
//			double p = this.computeTotalPowerProduction();
//			this.powerProduction.initialise(p);
//			justInitialised++;
//		} else if (!this.powerProduction.isInitialised()) {
//			notInitialisedYet++;
//		}

		assert	ElectricMeterElectricitySILModel.implementationInvariants(this) :
				new NeoSim4JavaException(
						"ElectricMeterElectricityModel."
						+ "implementationInvariants(this)");
		assert	ElectricMeterElectricitySILModel.invariants(this) :
				new NeoSim4JavaException(
						"ElectricMeterElectricityModel.invariants(this)");

		return new Pair<>(justInitialised, notInitialisedYet);
	}

	/**
	 * @see fr.sorbonne_u.devs_simulation.models.interfaces.AtomicModelI#output()
	 */
	@Override
	public ArrayList<EventI>	output()
	{
		ArrayList<EventI> ret = null;
		if (this.generatorToBeNotified) {
			ret = new ArrayList<>();
			ret.add(new GeneratorRequiredPowerChanged(
												this.getCurrentStateTime()));
		}
		if (this.batteriesToBeNotified) {
			if (ret == null) {
				ret = new ArrayList<>();
			}
			ret.add(new BatteriesRequiredPowerChanged(
												this.getCurrentStateTime()));
		}
		return ret;
	}

	/**
	 * @see fr.sorbonne_u.devs_simulation.models.interfaces.ModelI#timeAdvance()
	 */
	@Override
	public Duration		timeAdvance()
	{
		if (this.generatorToBeNotified || this.batteriesToBeNotified) {
			return Duration.zero(this.getSimulatedTimeUnit());
		} else {
			// trigger a new internal transition at each evaluation step duration
			return this.evaluationStep;
		}
	}

	/**
	 * @see fr.sorbonne_u.devs_simulation.models.AtomicModel#userDefinedInternalTransition(fr.sorbonne_u.devs_simulation.models.time.Duration)
	 */
	@Override
	public void			userDefinedInternalTransition(Duration elapsedTime)
	{
		super.userDefinedInternalTransition(elapsedTime);

		// update the current consumption since the last consumption update.
		// must be done before recomputing the instantaneous intensity.
		this.updateCumulativeConsumption(elapsedTime);

		boolean somethingHasChanged = false;
		// recompute the current power production
//		double totalProduction = this.computeTotalPowerProduction();
//		if (Math.abs(totalProduction - this.powerProduction.getValue())
//																>= TOLERANCE) {
//			somethingHasChanged = true;
//		}
//		this.powerProduction.setNewValue(totalProduction,
//										 this.getCurrentStateTime());
		// recompute the current total intensity
		double totalConsumption = this.computeTotalIntensity();
		if (Math.abs(totalConsumption - this.currentIntensity.getValue())
																>= TOLERANCE) {
			somethingHasChanged = true;
			this.ownerComponent.setCurrentPowerConsumption(
												totalConsumption,
												this.getCurrentStateTime());
		}
		this.currentIntensity.setNewValue(totalConsumption,
										  this.getCurrentStateTime());

		if (somethingHasChanged) {
			// set the power to be taken from the generator
//			double oldGeneratorRequiredPower =
//										this.generatorRequiredPower.getValue();
//			double newGeneratorRequiredPower =
//					totalConsumption - this.solarPanelOutputPower.getValue();
//			if (newGeneratorRequiredPower < 0.0) {
//				newGeneratorRequiredPower = 0.0;
//			}
//			if (Math.abs(newGeneratorRequiredPower - oldGeneratorRequiredPower)
//																> TOLERANCE) {
//				// the production is under the consumption in a sensible way
//				// try to activate the generator i.e., if it is running
//				this.generatorRequiredPower.setNewValue(
//													newGeneratorRequiredPower,
//													this.getCurrentStateTime());
//			}
//			if (!this.generatorToBeNotified) {
//				if (Math.abs(newGeneratorRequiredPower
//									- oldGeneratorRequiredPower) > TOLERANCE) {
//					this.generatorToBeNotified = true;
//				} else {
//					this.generatorToBeNotified = false;
//				}
//			} else {
//				this.generatorToBeNotified = false;
//			}

			// set the power to be taken from the batteries
//			double oldBatteriesRequiredPower =
//										this.batteriesRequiredPower.getValue();
//			double newBatteriesRequiredPower =
//					totalConsumption -
//						(this.solarPanelOutputPower.getValue()
//								+ this.generatorOutputPower.getValue());
//			if (newBatteriesRequiredPower < 0.0) {
//				newBatteriesRequiredPower = 0.0;
//			}
//			if (Math.abs(newBatteriesRequiredPower - oldBatteriesRequiredPower)
//																>= TOLERANCE) {
//				this.batteriesRequiredPower.setNewValue(
//													newBatteriesRequiredPower,
//													this.getCurrentStateTime());
//			}
//			if (!this.batteriesToBeNotified) {
//				if (Math.abs(newBatteriesRequiredPower
//								- oldBatteriesRequiredPower) > TOLERANCE) {
//					this.batteriesToBeNotified = true;
//				} else {
//					this.batteriesToBeNotified = false;
//				}
//			} else {
//				this.batteriesToBeNotified = false;
//			}
		} else {
			this.generatorToBeNotified = false;
			this.batteriesToBeNotified = false;
		}

		// Report statistics
		double powerBalance =
//				this.powerProduction.getValue() -
									this.currentIntensity.getValue();
		if (powerBalance < 0 && powerBalance < this.largestPowerDebt.getValue()) {
			this.largestPowerDebt.setNewValue(powerBalance,
											  this.getCurrentStateTime());
		} else if (powerBalance > 0 && powerBalance > this.largestPowerMargin.getValue()) {
			this.largestPowerMargin.setNewValue(powerBalance,
					this.getCurrentStateTime());
}

		// Tracing
		NumberFormat nf = NumberFormat.getInstance(Locale.US);
		nf.setGroupingUsed(false);
		nf.setMaximumFractionDigits(2);
		if (// this.powerProduction.isInitialised() &&
								this.currentIntensity.isInitialised()) {
			StringBuffer message =
					new StringBuffer("current power balance: ");
			message.append(nf.format(powerBalance));
			if (DEBUG) {
//				message.append(", solar panel production: ");
//				message.append(nf.format(this.solarPanelOutputPower.getValue()));
//				message.append(", generator required power: ");
//				message.append(nf.format(this.generatorRequiredPower.getValue()));
//				message.append(", generator production: ");
//				message.append(nf.format(this.generatorOutputPower.getValue()));
//				message.append(", batteries required power: ");
//				message.append(nf.format(this.batteriesRequiredPower.getValue()));
//				message.append(", batteries production: ");
//				message.append(nf.format(this.batteriesOutputPower.getValue()));
				message.append(", current total consumption: ");
				message.append(nf.format(this.currentIntensity.getValue()));
			} else if (VERBOSE) {
				message.append(" ");
				message.append(ElectricMeterImplementationI.POWER_UNIT);
			}
			message.append(" at ");
			message.append(this.getCurrentStateTime());
			this.logMessage(message.toString());
		}

		assert	ElectricMeterElectricitySILModel.implementationInvariants(this) :
				new NeoSim4JavaException(
						"ElectricMeterElectricityModel."
						+ "implementationInvariants(this)");
		assert	ElectricMeterElectricitySILModel.invariants(this) :
				new NeoSim4JavaException(
						"ElectricMeterElectricityModel.invariants(this)");
	}

	/**
	 * @see fr.sorbonne_u.devs_simulation.hioa.models.AtomicHIOA#endSimulation(fr.sorbonne_u.devs_simulation.models.time.Time)
	 */
	@Override
	public void			endSimulation(Time endTime)
	{
		this.updateCumulativeConsumption(
						endTime.subtract(this.cumulativeConsumption.getTime()));

		// must capture the current consumption before the finalisation
		// reinitialise the internal model variable.
		this.finalReport = new ElectricMeterElectricityReport(
										URI,
										this.cumulativeConsumption.getValue(),
										this.largestPowerDebt.getValue(),
										this.largestPowerDebt.getTime(),
										this.largestPowerMargin.getValue(),
										this.largestPowerMargin.getTime());

		if (VERBOSE) {
			this.logMessage("simulation ends.");
		}

		super.endSimulation(endTime);
	}

	// -------------------------------------------------------------------------
	// Optional DEVS simulation protocol: simulation run parameters
	// -------------------------------------------------------------------------

	/**
	 * @see fr.sorbonne_u.devs_simulation.models.Model#setSimulationRunParameters(java.util.Map)
	 */
	@Override
	public void			setSimulationRunParameters(
		Map<String, Object> simParams
		) throws MissingRunParameterException
	{
		super.setSimulationRunParameters(simParams);

		assert	simParams != null && !simParams.isEmpty() :
				new PreconditionException(
								"simParams != null && !simParams.isEmpty()");

		if (simParams.containsKey(
						AtomicSimulatorPlugin.OWNER_RUNTIME_PARAMETER_NAME)) {
			this.ownerComponent = 
				(ElectricMeterCyPhy) simParams.get(
						AtomicSimulatorPlugin.OWNER_RUNTIME_PARAMETER_NAME);
			this.getSimulationEngine().setLogger(
						AtomicSimulatorPlugin.createComponentLogger(simParams));
		}
	}

	// -------------------------------------------------------------------------
	// Optional DEVS simulation protocol: simulation report
	// -------------------------------------------------------------------------

	/**
	 * The class <code>ElectricMeterElectricityReport</code> implements the
	 * simulation report for the <code>ElectricMeterElectricityModel</code>.
	 *
	 * <p><strong>Description</strong></p>
	 * 
	 * <p><strong>Invariant</strong></p>
	 * 
	 * <pre>
	 * invariant	{@code true}	// no invariant
	 * </pre>
	 * 
	 * <p>Created on : 2021-10-01</p>
	 * 
	 * @author	<a href="mailto:Jacques.Malenfant@lip6.fr">Jacques Malenfant</a>
	 */
	public static class		ElectricMeterElectricityReport
	implements	SimulationReportI, GlobalReportI
	{
		private static final long serialVersionUID = 1L;
		protected String	modelURI;
		protected double	totalConsumption; // in kwh
		protected double	largestPowerDebt; // in amperes
		protected Time		largestPowerDebtTime;
		protected double	largestPowerMargin; // in amperes
		protected Time 		largestPowerMarginTime;

		public			ElectricMeterElectricityReport(
			String modelURI,
			double totalConsumption,
			double largestPowerDebt,
			Time largestPowerDebtTime,
			double largestPowerMargin,
			Time largestPowerMarginTime
			)
		{
			super();
			this.modelURI = modelURI;
			this.totalConsumption = totalConsumption;
			this.largestPowerDebt = largestPowerDebt;
			this.largestPowerDebtTime = largestPowerDebtTime;
			this.largestPowerMargin = largestPowerMargin;
			this.largestPowerMarginTime = largestPowerMarginTime;
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
			ret.append("total consumption in kwh = ");
			ret.append(this.totalConsumption);
			ret.append(".\n");
			ret.append(indent);
			ret.append('|');
			ret.append("largest power debt ");
			ret.append(this.largestPowerDebt);
			ret.append(" ");
			ret.append(ElectricMeterImplementationI.POWER_UNIT);
			ret.append(" at ");
			ret.append(this.largestPowerDebtTime);
			ret.append(".\n");
			ret.append(indent);
			ret.append('|');
			ret.append("largest power Margin ");
			ret.append(this.largestPowerMargin);
			ret.append(" ");
			ret.append(ElectricMeterImplementationI.POWER_UNIT);
			ret.append(" at ");
			ret.append(this.largestPowerMarginTime);
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
		return this.finalReport;
	}
}
// -----------------------------------------------------------------------------

package fr.sorbonne_u.components.hem2025e3.equipments.meter.sil;

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
import fr.sorbonne_u.components.hem2025e3.equipments.hairdryer.sil.HairDryerElectricitySILModel;
import fr.sorbonne_u.components.hem2025e2.equipments.hairdryer.mil.events.SetHighHairDryer;
import fr.sorbonne_u.components.hem2025e2.equipments.hairdryer.mil.events.SetLowHairDryer;
import fr.sorbonne_u.components.hem2025e2.equipments.hairdryer.mil.events.SwitchOffHairDryer;
import fr.sorbonne_u.components.hem2025e2.equipments.hairdryer.mil.events.SwitchOnHairDryer;
import fr.sorbonne_u.components.hem2025e3.equipments.heater.sil.HeaterElectricitySILModel;
import fr.sorbonne_u.components.hem2025e2.equipments.heater.mil.events.DoNotHeat;
import fr.sorbonne_u.components.hem2025e2.equipments.heater.mil.events.Heat;
import fr.sorbonne_u.components.hem2025e2.equipments.heater.mil.events.SwitchOffHeater;
import fr.sorbonne_u.components.hem2025e2.equipments.heater.mil.events.SwitchOnHeater;
import fr.sorbonne_u.components.hem2025e3.equipments.heater.sil.events.SIL_SetPowerHeater;
import fr.sorbonne_u.devs_simulation.architectures.RTArchitecture;
import fr.sorbonne_u.devs_simulation.hioa.architectures.HIOA_Composer;
import fr.sorbonne_u.devs_simulation.hioa.architectures.RTAtomicHIOA_Descriptor;
import fr.sorbonne_u.devs_simulation.hioa.architectures.RTCoupledHIOA_Descriptor;
import fr.sorbonne_u.devs_simulation.hioa.models.vars.VariableSink;
import fr.sorbonne_u.devs_simulation.hioa.models.vars.VariableSource;
import fr.sorbonne_u.devs_simulation.models.architectures.AbstractAtomicModelDescriptor;
import fr.sorbonne_u.devs_simulation.models.architectures.CoupledModelDescriptor;
import fr.sorbonne_u.devs_simulation.models.events.EventI;
import fr.sorbonne_u.devs_simulation.models.events.EventSink;
import fr.sorbonne_u.exceptions.PreconditionException;

// -----------------------------------------------------------------------------
/**
 * The class <code>MILSimulationArchitectures</code>  defines the local MIL
 * simulation architecture pertaining to the electric meter component.
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
 * <p>Created on : 2023-11-16</p>
 * 
 * @author	<a href="mailto:Jacques.Malenfant@lip6.fr">Jacques Malenfant</a>
 */
public abstract class	LocalSimulationArchitectures
{
	/**
	 * create the local SIL real time simulation architecture for the
	 * {@code ElectricMeter} component.
	 * 
	 * <p><strong>Contract</strong></p>
	 * 
	 * <pre>
	 * pre	{@code true}	// no precondition.
	 * post	{@code true}	// no postcondition.
	 * </pre>
	 *
	 * @param architectureURI		URI to be given to the created simulation architecture.
	 * @param rootModelURI			URI of the root model in the simulation architecture.
	 * @param simulatedTimeUnit		simulated time unit used in the architecture.
	 * @param accelerationFactor	acceleration factor used to execute in a logical time speeding up the real time.
	 * @return						the local SIL real time simulation architecture for the {@code ElectricMeter} component.
	 * @throws Exception			<i>to do</i>.
	 */
	public static RTArchitecture	createElectricMeterSILArchitecture(
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

		// the electric meter electricity model accumulates the electric
		// power consumption and production, an atomic HIOA model hence we use
		// a RTAtomicHIOA_Descriptor
		atomicModelDescriptors.put(
				ElectricMeterElectricitySILModel.URI,
				RTAtomicHIOA_Descriptor.create(
						ElectricMeterElectricitySILModel.class,
						ElectricMeterElectricitySILModel.URI,
						simulatedTimeUnit,
						null,
						accelerationFactor));
		// The electricity models of all appliances will need to be put within
		// the ElectricMeter simulator to be able to share the variables
		// containing their power consumptions.
		atomicModelDescriptors.put(
				HairDryerElectricitySILModel.URI,
				RTAtomicHIOA_Descriptor.create(
						HairDryerElectricitySILModel.class,
						HairDryerElectricitySILModel.URI,
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
		submodels.add(ElectricMeterElectricitySILModel.URI);
		submodels.add(HairDryerElectricitySILModel.URI);
		submodels.add(HeaterElectricitySILModel.URI);

		Map<Class<? extends EventI>,EventSink[]> imported = new HashMap<>();
		imported.put(
				SwitchOnHairDryer.class,
				new EventSink[] {
					new EventSink(HairDryerElectricitySILModel.URI,
								  SwitchOnHairDryer.class)
				});
		imported.put(
				SwitchOffHairDryer.class,
				new EventSink[] {
					new EventSink(HairDryerElectricitySILModel.URI,
								  SwitchOffHairDryer.class)
				});
		imported.put(
				SetLowHairDryer.class,
				new EventSink[] {
					new EventSink(HairDryerElectricitySILModel.URI,
								  SetLowHairDryer.class)
				});
		imported.put(
				SetHighHairDryer.class,
				new EventSink[] {
					new EventSink(HairDryerElectricitySILModel.URI,
								  SetHighHairDryer.class)
				});

		imported.put(
				SIL_SetPowerHeater.class,
				new EventSink[] {
						new EventSink(HeaterElectricitySILModel.URI,
									  SIL_SetPowerHeater.class)
				});
		imported.put(
				SwitchOnHeater.class,
				new EventSink[] {
						new EventSink(HeaterElectricitySILModel.URI,
									  SwitchOnHeater.class)
				});
		imported.put(
				SwitchOffHeater.class,
				new EventSink[] {
						new EventSink(HeaterElectricitySILModel.URI,
									  SwitchOffHeater.class)
				});
		imported.put(
				Heat.class,
				new EventSink[] {
						new EventSink(HeaterElectricitySILModel.URI,
									  Heat.class)
				});
		imported.put(
				DoNotHeat.class,
				new EventSink[] {
						new EventSink(HeaterElectricitySILModel.URI,
									  DoNotHeat.class)
				});

		// variable bindings between exporting and importing models
		Map<VariableSource,VariableSink[]> bindings =
								new HashMap<VariableSource,VariableSink[]>();
		bindings.put(
				new VariableSource("currentIntensity",
								   Double.class,
								   HairDryerElectricitySILModel.URI),
				new VariableSink[] {
					new VariableSink("currentHairDryerIntensity",
									 Double.class,
									 ElectricMeterElectricitySILModel.URI)
				});
		bindings.put(
				new VariableSource("currentIntensity",
								   Double.class,
								   HeaterElectricitySILModel.URI),
				new VariableSink[] {
					new VariableSink("currentHeaterIntensity",
									 Double.class,
									 ElectricMeterElectricitySILModel.URI)
				});

		coupledModelDescriptors.put(
				rootModelURI,
				new RTCoupledHIOA_Descriptor(
						ElectricMeterCoupledModel.class,
						rootModelURI,
						submodels,
						imported,
						null,
						null,
						null,
						null,
						null,
						bindings,
						new HIOA_Composer(),
						accelerationFactor));

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

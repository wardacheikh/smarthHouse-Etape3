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
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;
//Ajouter ces imports
import fr.sorbonne_u.components.hem2025e2.equipments.BoxWifi.mil.events.ActivateWifiBoxWifi;
import fr.sorbonne_u.components.hem2025e2.equipments.BoxWifi.mil.events.DeactivateWifiBoxWifi;
import fr.sorbonne_u.components.hem2025e2.equipments.BoxWifi.mil.events.SwitchOffBoxWifi;
import fr.sorbonne_u.components.hem2025e2.equipments.BoxWifi.mil.events.SwitchOnBoxWifi;

import fr.sorbonne_u.components.cyphy.plugins.devs.CoordinatorPlugin;
import fr.sorbonne_u.components.cyphy.plugins.devs.architectures.RTComponentAtomicModelDescriptor;
import fr.sorbonne_u.components.cyphy.plugins.devs.architectures.RTComponentCoupledModelDescriptor;
import fr.sorbonne_u.components.cyphy.plugins.devs.architectures.RTComponentModelArchitecture;
import fr.sorbonne_u.components.hem2025e2.equipments.hairdryer.mil.events.SetHighHairDryer;
import fr.sorbonne_u.components.hem2025e2.equipments.hairdryer.mil.events.SetLowHairDryer;
import fr.sorbonne_u.components.hem2025e2.equipments.hairdryer.mil.events.SwitchOffHairDryer;
import fr.sorbonne_u.components.hem2025e2.equipments.hairdryer.mil.events.SwitchOnHairDryer;
import fr.sorbonne_u.components.hem2025e2.equipments.heater.mil.HeaterCoupledModel;
import fr.sorbonne_u.components.hem2025e2.equipments.heater.mil.events.DoNotHeat;
import fr.sorbonne_u.components.hem2025e2.equipments.heater.mil.events.Heat;
import fr.sorbonne_u.components.hem2025e3.equipments.heater.sil.events.SIL_SetPowerHeater;
import fr.sorbonne_u.components.hem2025e2.equipments.heater.mil.events.SwitchOffHeater;
import fr.sorbonne_u.components.hem2025e2.equipments.heater.mil.events.SwitchOnHeater;
import fr.sorbonne_u.components.hem2025e3.equipements.wifi.BoxWifiCyPhy;
import fr.sorbonne_u.components.hem2025e3.equipements.wifi.sil.BoxWifiStateSILModel;
import fr.sorbonne_u.components.hem2025e3.equipments.hairdryer.HairDryerCyPhy;
import fr.sorbonne_u.components.hem2025e3.equipments.hairdryer.sil.HairDryerStateSILModel;
import fr.sorbonne_u.components.hem2025e3.equipments.heater.HeaterCyPhy;
import fr.sorbonne_u.components.hem2025e3.equipments.meter.ElectricMeterCyPhy;
import fr.sorbonne_u.components.hem2025e3.equipments.meter.sil.ElectricMeterCoupledModel;
import fr.sorbonne_u.devs_simulation.models.architectures.AbstractAtomicModelDescriptor;
import fr.sorbonne_u.devs_simulation.models.architectures.CoupledModelDescriptor;
import fr.sorbonne_u.devs_simulation.models.events.EventI;
import fr.sorbonne_u.devs_simulation.models.events.EventSink;
import fr.sorbonne_u.devs_simulation.models.events.EventSource;
import fr.sorbonne_u.exceptions.PreconditionException;

// -----------------------------------------------------------------------------
/**
 * The class <code>ComponentSimulationArchitectures</code> defines the global
 * component simulation architectures for the whole HEM application.
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
public abstract class	ComponentSimulationArchitectures
{
	/**
	 * create the global SIL real time component simulation architecture for the
	 * HEM application.
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
	 * @param architectureURI		URI of the component model architecture to be created.
	 * @param rootModelURI			URI of the root model in the simulation architecture.
	 * @param simulatedTimeUnit		simulated time unit used in the architecture.
	 * @param accelerationFactor	acceleration factor for this run.
	 * @return						the global SIL real time simulation  architecture for the HEM application.
	 * @throws Exception			<i>to do</i>.
	 */
	@SuppressWarnings("unchecked")
	public static RTComponentModelArchitecture
									createComponentSimulationArchitectures(
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

		// Currently, the HEM application has only two appliances: a hair dryer
		// and a heater.
		atomicModelDescriptors.put(
				HairDryerStateSILModel.URI,
				RTComponentAtomicModelDescriptor.create(
						HairDryerStateSILModel.URI,
						(Class<? extends EventI>[]) new Class<?>[]{},
						(Class<? extends EventI>[]) new Class<?>[]{
							SwitchOnHairDryer.class,	// notice that the
							SwitchOffHairDryer.class,	// exported events of
							SetLowHairDryer.class,		// the atomic model
							SetHighHairDryer.class},	// appear here
						simulatedTimeUnit,
						HairDryerCyPhy.REFLECTION_INBOUND_PORT_URI
						));
		
		// Après l'ajout du hair dryer, ajouter le Box WiFi
		atomicModelDescriptors.put(
		    BoxWifiStateSILModel.URI,
		    RTComponentAtomicModelDescriptor.create(
		        BoxWifiStateSILModel.URI,
		        (Class<? extends EventI>[]) new Class<?>[]{},
		        (Class<? extends EventI>[]) new Class<?>[]{
		            SwitchOnBoxWifi.class,
		            SwitchOffBoxWifi.class,
		            ActivateWifiBoxWifi.class,
		            DeactivateWifiBoxWifi.class},
		        simulatedTimeUnit,
		        BoxWifiCyPhy.REFLECTION_INBOUND_PORT_URI
		    ));
		atomicModelDescriptors.put(
				HeaterCoupledModel.URI,
				RTComponentAtomicModelDescriptor.create(
						HeaterCoupledModel.URI,
						(Class<? extends EventI>[]) new Class<?>[]{},
						(Class<? extends EventI>[]) new Class<?>[]{
							SIL_SetPowerHeater.class,		// notice that the
							SwitchOnHeater.class,		// reexported events of
							SwitchOffHeater.class,		// the coupled model
							Heat.class,					// appear here
							DoNotHeat.class},
						simulatedTimeUnit,
						HeaterCyPhy.REFLECTION_INBOUND_PORT_URI));

		// The electric meter also has a SIL simulation model
		// The electric meter also has a SIL simulation model
		atomicModelDescriptors.put(
		    ElectricMeterCoupledModel.URI,
		    RTComponentAtomicModelDescriptor.create(
		        ElectricMeterCoupledModel.URI,
		        (Class<? extends EventI>[]) new Class<?>[]{
		            SwitchOnHairDryer.class,
		            SwitchOffHairDryer.class,
		            SetLowHairDryer.class,
		            SetHighHairDryer.class,
		            SIL_SetPowerHeater.class,
		            SwitchOnHeater.class,
		            SwitchOffHeater.class,
		            Heat.class,
		            DoNotHeat.class,
		            // AJOUTER LES ÉVÉNEMENTS BOX WIFI ICI :
		            SwitchOnBoxWifi.class,
		            SwitchOffBoxWifi.class,
		            ActivateWifiBoxWifi.class,
		            DeactivateWifiBoxWifi.class},
		        (Class<? extends EventI>[]) new Class<?>[]{},
		        simulatedTimeUnit,
		        ElectricMeterCyPhy.REFLECTION_INBOUND_PORT_URI));
		// map that will contain the coupled model descriptors to construct
		// the simulation architecture
		Map<String,CoupledModelDescriptor> coupledModelDescriptors =
															new HashMap<>();

		// the set of submodels of the coupled model, given by their URIs
		Set<String> submodels = new HashSet<String>();
		submodels.add(HairDryerStateSILModel.URI);
		submodels.add(HeaterCoupledModel.URI);
		submodels.add(ElectricMeterCoupledModel.URI);
		submodels.add(BoxWifiStateSILModel.URI);
		// event exchanging connections between exporting and importing
		// models
		Map<EventSource,EventSink[]> connections =
									new HashMap<EventSource,EventSink[]>();

		// first, the events going from the hair dryer to the electric meter
		connections.put(
			new EventSource(HairDryerStateSILModel.URI,
							SwitchOnHairDryer.class),
			new EventSink[] {
				new EventSink(ElectricMeterCoupledModel.URI,
							  SwitchOnHairDryer.class)
			});
		connections.put(
			new EventSource(HairDryerStateSILModel.URI,
							SwitchOffHairDryer.class),
			new EventSink[] {
				new EventSink(ElectricMeterCoupledModel.URI,
							  SwitchOffHairDryer.class)
			});
		connections.put(
			new EventSource(HairDryerStateSILModel.URI,
							SetLowHairDryer.class),
			new EventSink[] {
				new EventSink(ElectricMeterCoupledModel.URI,
							  SetLowHairDryer.class)
			});
		connections.put(
			new EventSource(HairDryerStateSILModel.URI,
							SetHighHairDryer.class),
			new EventSink[] {
				new EventSink(ElectricMeterCoupledModel.URI,
							  SetHighHairDryer.class)
			});
		

		// second, the events going from the heater to the electric meter
		connections.put(
				new EventSource(HeaterCoupledModel.URI,
								SIL_SetPowerHeater.class),
				new EventSink[] {
					new EventSink(ElectricMeterCoupledModel.URI,
								  SIL_SetPowerHeater.class)
				});
		connections.put(
				new EventSource(HeaterCoupledModel.URI,
								SwitchOnHeater.class),
				new EventSink[] {
					new EventSink(ElectricMeterCoupledModel.URI,
								  SwitchOnHeater.class)
				});
		connections.put(
				new EventSource(HeaterCoupledModel.URI,
								SwitchOffHeater.class),
				new EventSink[] {
					new EventSink(ElectricMeterCoupledModel.URI,
								  SwitchOffHeater.class)
				});
		connections.put(
				new EventSource(HeaterCoupledModel.URI,
								Heat.class),
				new EventSink[] {
					new EventSink(ElectricMeterCoupledModel.URI,
								  Heat.class)
				});
		connections.put(
				new EventSource(HeaterCoupledModel.URI,
								DoNotHeat.class),
				new EventSink[] {
					new EventSink(ElectricMeterCoupledModel.URI,
								  DoNotHeat.class)
				});
		// Après les connections du heater, ajouter celles du Box WiFi
		connections.put(
		    new EventSource(BoxWifiStateSILModel.URI,
		                    SwitchOnBoxWifi.class),
		    new EventSink[] {
		        new EventSink(ElectricMeterCoupledModel.URI,
		                      SwitchOnBoxWifi.class)
		    });
		connections.put(
		    new EventSource(BoxWifiStateSILModel.URI,
		                    SwitchOffBoxWifi.class),
		    new EventSink[] {
		        new EventSink(ElectricMeterCoupledModel.URI,
		                      SwitchOffBoxWifi.class)
		    });
		connections.put(
		    new EventSource(BoxWifiStateSILModel.URI,
		                    ActivateWifiBoxWifi.class),
		    new EventSink[] {
		        new EventSink(ElectricMeterCoupledModel.URI,
		                      ActivateWifiBoxWifi.class)
		    });
		connections.put(
		    new EventSource(BoxWifiStateSILModel.URI,
		                    DeactivateWifiBoxWifi.class),
		    new EventSink[] {
		        new EventSink(ElectricMeterCoupledModel.URI,
		                      DeactivateWifiBoxWifi.class)
		    });

		// coupled model descriptor
		coupledModelDescriptors.put(
				rootModelURI,
				RTComponentCoupledModelDescriptor.create(
						GlobalCoupledModel.class,
						rootModelURI,
						submodels,
						null,
						null,
						connections,
						null,
						CoordinatorComponent.REFLECTION_INBOUND_PORT_URI,
						CoordinatorPlugin.class,
						null,
						accelerationFactor));

		RTComponentModelArchitecture architecture =
				new RTComponentModelArchitecture(
						architectureURI,
						rootModelURI,
						atomicModelDescriptors,
						coupledModelDescriptors,
						simulatedTimeUnit);

		return architecture;
	}
}
// -----------------------------------------------------------------------------

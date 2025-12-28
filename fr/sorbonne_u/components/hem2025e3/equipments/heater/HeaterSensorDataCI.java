package fr.sorbonne_u.components.hem2025e3.equipments.heater;

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

import java.util.concurrent.TimeUnit;
import fr.sorbonne_u.components.hem2025e3.equipments.heater.sensor_data.HeatingSensorData;
import fr.sorbonne_u.components.hem2025e3.equipments.heater.sensor_data.TemperatureSensorData;
import fr.sorbonne_u.components.interfaces.DataOfferedCI;
import fr.sorbonne_u.components.interfaces.DataRequiredCI;
import fr.sorbonne_u.components.interfaces.OfferedCI;
import fr.sorbonne_u.components.interfaces.RequiredCI;

// -----------------------------------------------------------------------------
/**
 * The component data interface <code>HeaterSensorCI</code> declares the pull
 * and the push interfaces to get and receive sensor data from the heater
 * component.
 *
 * <p><strong>Description</strong></p>
 * 
 * <p>
 * BCM4Java data oriented interfaces provide means to communicate between
 * components to exchange data rather than calling services. To achieve this,
 * the standard BCM4Java {@code DataOfferedCI} and {@code DataRequiredCI}
 * defines two protocols for the exchange of data: a pull protocol and a push
 * protocol. These interfaces assume that the data exchanges are performed
 * between a component that produces data, the producer, and another component
 * that consumes them, the consumer.
 * </p>
 * <p>
 * In the standard pull protocol, the consumer component (owning the outbound
 * port) has the initiative to call the consumer to get the data. To do so, it
 * calls the method {@code DataRequiredCI.PullCI::request} on its outbound port,
 * which in turn calls the same method on the connector, which finally calls the
 * method {@code DataOfferedCI.PullCI::get} on the inbound port that must be
 * implemented to call the producer component services to retrieve the data.
 * </p>
 * <p>
 * In the standard push protocol, the producer component (owning the inbound
 * port) has the initiative to call the producer to pass it the data. To do so,
 * it calls the method {@code DataOfferedCI.PushCI::send} on its inbound port,
 * which in turn calls the same method on the connector, which finally calls the
 * method {@code DataRequiredCI.PushCI::receive} on the outbound port that must
 * be implemented to call the consumer component services to pass it the data.
 * </p>
 * <p>
 * In the case of the {@code HeaterCyPhy} component (the producer) and its
 * {@code HeaterController} controller (the consumer), the two standard
 * protocols can be used: in the pull control mode, the controller uses the
 * standard pull protocol to get the target temperature and the current room
 * temperature from the heater, while in the push control mode, the heater
 * uses the standard push protocol to pass the temperatures to the controller,
 * hence triggering the execution of the control per se. The pull control is
 * started by the {@code HeaterCyPhy} component by pushing its state sensor
 * value through the standard push protocol when switched on, and its is stopped
 * also by passing its state when switched off. The controller schedules the
 * execution of the control as a repetitive task with a given control period.
 * The push control is also started and stopped when switching on and off the
 * heater, but the repetitive task is scheduled and executed on the heater to
 * push the data towards the controller upon a request made by the controller
 * through by calling the method
 * {@code HeaterSensorCI::startTemperaturesPushSensor}.
 * </p>
 * <p>
 * As the standard pull and push protocols are used to transmit the state of
 * the heater and the temperatures, the component interface
 * {@code HeaterSensorCI} adds method allowing to access other sensor data and
 * also other methods required to implement the cooperation between the two
 * components, like the method {@code HeaterSensorCI::startTemperaturesPushSensor}.
 * </p>
 * 
 * <p><strong>Invariants</strong></p>
 * 
 * <pre>
 * invariant	{@code true}	// no more invariant
 * </pre>
 * 
 * <p>Created on : 2023-11-27</p>
 * 
 * @author	<a href="mailto:Jacques.Malenfant@lip6.fr">Jacques Malenfant</a>
 */
public interface		HeaterSensorDataCI
extends		DataOfferedCI,
			DataRequiredCI
{
	// -------------------------------------------------------------------------
	// Inner interfaces and types
	// -------------------------------------------------------------------------

	/**
	 * The component interface <code>HeaterSensorCI</code> declares the common
	 * services used in pull mode to get the sensor data from the heater.
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
	 * <p>Created on : 2025-11-27</p>
	 * 
	 * @author	<a href="mailto:Jacques.Malenfant@lip6.fr">Jacques Malenfant</a>
	 */
	public static interface	HeaterSensorCI
	extends		OfferedCI,
				RequiredCI
	{
		// ---------------------------------------------------------------------
		// Methods
		// ---------------------------------------------------------------------

		/**
		 * return true if the heater is currently heating.
		 * 
		 * <p><strong>Contract</strong></p>
		 * 
		 * <pre>
		 * pre	{@code on()}
		 * post	{@code true}	// no postcondition.
		 * </pre>
		 *
		 * @return				true if the heater is currently heating.
		 * @throws Exception	<i>to do</i>.
		 */
		public HeatingSensorData	heatingPullSensor()
		throws Exception;

		/**
		 * get the current target temperature.
		 * 
		 * <p><strong>Contract</strong></p>
		 * 
		 * <pre>
		 * pre	{@code true}	// no precondition.
		 * post	{@code return >= -50.0 && return <= 50.0}
		 * </pre>
		 *
		 * @return				the current target temperature.
		 * @throws Exception	<i>to do</i>.
		 */
		public TemperatureSensorData	targetTemperaturePullSensor()
		throws Exception;

		/**
		 * return the current temperature measured by the thermostat.
		 * 
		 * <p><strong>Contract</strong></p>
		 * 
		 * <pre>
		 * pre	{@code on()}
		 * post	{@code true}	// no postcondition.
		 * </pre>
		 *
		 * @return				the current temperature measured by the thermostat.
		 * @throws Exception	<i>to do</i>.
		 */
		public TemperatureSensorData	currentTemperaturePullSensor()
		throws Exception;

		/**
		 * start a sequence of temperatures pushes with the given period.
		 * 
		 * <p><strong>Contract</strong></p>
		 * 
		 * <pre>
		 * pre	{@code controlPeriod > 0}
		 * pre	{@code tu != null}
		 * post	{@code true}	// no postcondition.
		 * </pre>
		 *
		 * @param controlPeriod	period at which the pushes must be made.
		 * @param tu			time unit in which {@code controlPeriod} is expressed.
		 * @throws Exception	<i>to do</i>.
		 */
		public void			startTemperaturesPushSensor(
			long controlPeriod,
			TimeUnit tu
			) throws Exception;
	}

	/**
	 * The interface <code>HeaterSensorRequiredPullCI</code> is the pull
	 * interface that a client component must require to call the outbound port.
	 *
	 * <p><strong>Description</strong></p>
	 * 
	 * <p><strong>Invariants</strong></p>
	 * 
	 * <pre>
	 * invariant	{@code true}	// no more invariant
	 * </pre>
	 * 
	 * <p>Created on : 2023-12-04</p>
	 * 
	 * @author	<a href="mailto:Jacques.Malenfant@lip6.fr">Jacques Malenfant</a>
	 */
	public static interface		HeaterSensorRequiredPullCI
	extends		DataRequiredCI.PullCI,
				HeaterSensorCI
	{
	}

	/**
	 * The interface <code>HeaterSensorOfferedPullCI</code> is the pull
	 * interface that a server component must offer to be called the inbound
	 * port.
	 *
	 * <p><strong>Description</strong></p>
	 * 
	 * <p><strong>Invariants</strong></p>
	 * 
	 * <pre>
	 * invariant	{@code true}	// no more invariant
	 * </pre>
	 * 
	 * <p>Created on : 2023-12-04</p>
	 * 
	 * @author	<a href="mailto:Jacques.Malenfant@lip6.fr">Jacques Malenfant</a>
	 */
	public static interface		HeaterSensorOfferedPullCI
	extends		DataOfferedCI.PullCI,
				HeaterSensorCI
	{
	}
}
// -----------------------------------------------------------------------------

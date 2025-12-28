package fr.sorbonne_u.components.hem2025e3.equipments.heater.sensor_data;

// Copyright Jacques Malenfant, Sorbonne Universite.
// Jacques.Malenfant@lip6.fr
//
// This software is a computer program whose purpose is to provide a
// basic component programming model to program with components
// distributed applications in the Java programming language.
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

import java.time.Instant;
import fr.sorbonne_u.alasca.physical_data.AbstractSignalData;
import fr.sorbonne_u.alasca.physical_data.ComposedSignalData;
import fr.sorbonne_u.exceptions.AssertionChecking;
import fr.sorbonne_u.utils.aclocks.AcceleratedClock;
import fr.sorbonne_u.exceptions.PreconditionException;

// -----------------------------------------------------------------------------
/**
 * The class <code>HeaterTemperaturesSensor</code> implements a composed
 * sensor data sent by the heater to the controller, which contains the target
 * temperature and the current room temperature measured by the heater
 * internal thermometer.
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
 * invariant	{@code size() == 2}
 * </pre>
 * 
 * <p>Created on : 2025-11-24</p>
 * 
 * @author	<a href="mailto:Jacques.Malenfant@lip6.fr">Jacques Malenfant</a>
 */
public class			HeaterTemperaturesSensorData
extends		ComposedSignalData
implements	HeaterSensorDataI
{
	// -------------------------------------------------------------------------
	// Constants and variables
	// -------------------------------------------------------------------------

	private static final long serialVersionUID = 1L;

	// -------------------------------------------------------------------------
	// Constructors
	// -------------------------------------------------------------------------

	/**
	 * create a new composed sensor data with the given target and current
	 * temperature at the current time under the hardware clock time reference.
	 * 
	 * <p><strong>Contract</strong></p>
	 * 
	 * <pre>
	 * pre	{@code target != null && current != null}
	 * post	{@code isComposed()}
	 * post	{@code size() == 2}
	 * post	{@code getTimestamp().equals(Instant.ofEpochMilli(System.currentTimeMillis())}
	 * post	{@code getTimeReference().equals(getStandardTimestamper())}
	 * </pre>
	 *
	 * @param target		target temperature of the thermostat.
	 * @param current		current room temperature.
	 * @throws Exception	<i>to do</i>.
	 */
	public				HeaterTemperaturesSensorData(
		TemperatureSensorData target,
		TemperatureSensorData current
		) throws Exception
	{
		super(AssertionChecking.assertTrueAndReturnOrThrow(
				target != null && current != null,
				new AbstractSignalData[]{target, current},
				() -> new PreconditionException(
						"target != null && current != null")));
	}

	/**
	 * create a new composed sensor data with the given target and current
	 * temperature at the given time under this host hardware clock time
	 * reference.
	 * 
	 * <p><strong>Contract</strong></p>
	 * 
	 * <pre>
	 * pre	{@code target != null && current != null}
	 * pre	{@code timestamp != null}
	 * post	{@code isComposed()}
	 * post	{@code size() == 2}
	 * post	{@code getTimestamp().equals(timestamp)}
	 * post	{@code getTimeReference().equals(getStandardTimestamper())}
	 * </pre>
	 *
	 * @param target		target temperature of the thermostat.
	 * @param current		current room temperature.
	 * @param timestamp		time stamp as a Java {@code Instant} object.
	 * @throws Exception	<i>to do</i>.
	 */
	public				HeaterTemperaturesSensorData(
		TemperatureSensorData target,
		TemperatureSensorData current,
		Instant timestamp
		) throws Exception
	{
		super(AssertionChecking.assertTrueAndReturnOrThrow(
				target != null && current != null,
				new AbstractSignalData[]{target, current},
				() -> new PreconditionException(
						"target != null && current != null")),
			  timestamp);
	}

	/**
	 * create a new composed sensor data with the given target and current
	 * temperature at the given time under the given host hardware clock time
	 * reference.
	 * 
	 * <p><strong>Contract</strong></p>
	 * 
	 * <pre>
	 * pre	{@code target != null && current != null}
	 * pre	{@code timestamp != null}
	 * pre	{@code timestamper != null && !timestamper.isEmpty()}
	 * post	{@code isComposed()}
	 * post	{@code size() == 2}
	 * post	{@code getTimestamp().equals(timestamp)}
	 * post	{@code getTimeReference().equals(getTimestamper())}
	 * </pre>
	 *
	 * @param target		target temperature of the thermostat.
	 * @param current		current room temperature.
	 * @param timestamp		time stamp as a Java {@code Instant} object.
	 * @throws Exception	<i>to do</i>.
	 */
	public				HeaterTemperaturesSensorData(
		TemperatureSensorData target,
		TemperatureSensorData current,
		Instant timestamp,
		String timestamper
		) throws Exception
	{
		super(AssertionChecking.assertTrueAndReturnOrThrow(
				target != null && current != null,
				new AbstractSignalData[]{target, current},
				() -> new PreconditionException(
						"target != null && current != null")),
			  timestamp,
			  timestamper);
	}

	/**
	 * create a new composed sensor data with the given target and current
	 * temperature at the current time under the given software clock time
	 * reference.
	 * 
	 * <p><strong>Contract</strong></p>
	 * 
	 * <pre>
	 * pre	{@code target != null && current != null}
	 * pre	{@code ac != null}
	 * post	{@code isComposed()}
	 * post	{@code size() == 2}
	 * post	{@code getTimestamp().equals(ac.currentInstant())}
	 * post	{@code getTimeReference().equals(((Supplier<String>) () -> { try { return ac.getTimeReferenceIdentity(); } catch (UnknownHostException e) { return UNKNOWN_TIMESTAMPER; }}).get())}
	 * </pre>
	 *
	 * @param target		target temperature of the thermostat.
	 * @param current		current room temperature.
	 * @param ac			an accelerated clock giving the time reference.
	 * @throws Exception	<i>to do</i>.
	 */
	public				HeaterTemperaturesSensorData(
		TemperatureSensorData target,
		TemperatureSensorData current,
		AcceleratedClock ac
		) throws Exception
	{
		super(AssertionChecking.assertTrueAndReturnOrThrow(
				target != null && current != null,
				new AbstractSignalData[]{target, current},
				() -> new PreconditionException(
						"target != null && current != null")),
			  ac);
	}

	/**
	 * create a new composed sensor data with the given target and current
	 * temperature at the given time under the given software clock time
	 * reference.
	 * 
	 * <p><strong>Contract</strong></p>
	 * 
	 * <pre>
	 * pre	{@code target != null && current != null}
	 * pre	{@code ac != null}
	 * pre	{@code timestamp != null}
	 * post	{@code isComposed()}
	 * post	{@code size() == 2}
	 * post	{@code getTimestamp().equals(timestamp)}
	 * post	{@code getTimeReference().equals(((Supplier<String>) () -> { try { return ac.getTimeReferenceIdentity(); } catch (UnknownHostException e) { return UNKNOWN_TIMESTAMPER; }}).get())}
	 * </pre>
	 *
	 * @param target		target temperature of the thermostat.
	 * @param current		current room temperature.
	 * @param ac			an accelerated clock giving the time reference.
	 * @param timestamp		time stamp as a Java {@code Instant} object.
	 * @throws Exception	<i>to do</i>.
	 */
	public				HeaterTemperaturesSensorData(
		TemperatureSensorData target,
		TemperatureSensorData current,
		AcceleratedClock ac,
		Instant timestamp
		) throws Exception
	{
		super(AssertionChecking.assertTrueAndReturnOrThrow(
				target != null && current != null,
				new AbstractSignalData[]{target, current},
				() -> new PreconditionException(
						"target != null && current != null")),
			  ac,
			  timestamp);
	}

	// -------------------------------------------------------------------------
	// Methods
	// -------------------------------------------------------------------------

	/**
	 * return the target temperature of the thermostat as a sensor data.
	 * 
	 * <p><strong>Contract</strong></p>
	 * 
	 * <pre>
	 * pre	{@code true}	// no precondition.
	 * post	{@code return != null}
	 * </pre>
	 *
	 * @return	the target temperature of the thermostat as a sensor data.
	 */
	public TemperatureSensorData	getTargetTemperature()
	{
		return (TemperatureSensorData) this.get(0);
	}

	/**
	 * return the current room temperature measured by the thermostat as a
	 * sensor data.
	 * 
	 * <p><strong>Contract</strong></p>
	 * 
	 * <pre>
	 * pre	{@code true}	// no precondition.
	 * post	{@code return != null}
	 * </pre>
	 *
	 * @return	the current room temperature measured by the thermostat as a sensor data.
	 */
	public TemperatureSensorData	getCurrentTemperature()
	{
		return (TemperatureSensorData) this.get(1);
	}
}
// -----------------------------------------------------------------------------

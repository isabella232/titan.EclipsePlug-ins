/******************************************************************************
 * Copyright (c) 2000-2017 Ericsson Telecom AB
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 ******************************************************************************/
package org.eclipse.titan.runtime.core;

import java.io.IOException;
import java.nio.channels.SelectableChannel;
import java.nio.channels.SelectionKey;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.Map;
import java.util.Set;

/**
 * The base class of test ports
 * 
 * TODO: lots to implement
 * 
 * @author Kristof Szabados
 */
public class TitanPort {
	// originally the list stored in list_head list_tail
	private static final LinkedList<TitanPort> PORTS = new LinkedList<TitanPort>();
	// originally the list stored in system_list_head and system_list_tail
	private static final LinkedList<TitanPort> SYSTEM_PORTS = new LinkedList<TitanPort>();

	private String portName;
	protected boolean is_active;
	protected boolean is_started;
	protected boolean is_halted;

	private ArrayList<String> systemMappings = new ArrayList<String>();

	public TitanPort(final String portName) {
		this.portName = portName;
		is_active = false;
		is_started = false;
	}

	protected TitanPort() {}

	public String getName() {
		return portName;
	}

	//originally PORT::add_to_list
	private void addToList(final boolean system) {
		if (system) {
			for (TitanPort port : SYSTEM_PORTS) {
				if (port == this) {
					return;
				}
				if (port.portName.equals(portName)) {
					throw new TtcnError(MessageFormat.format("Internal error: There are more than one ports with name {0}.", portName));
				}
			}

			SYSTEM_PORTS.add(this);
		} else {
			for (TitanPort port : PORTS) {
				if (port == this) {
					return;
				}
				if (port.portName.equals(portName)) {
					throw new TtcnError(MessageFormat.format("Internal error: There are more than one ports with name {0}.", portName));
				}
			}

			PORTS.add(this);
		}
	}

	//originally PORT::remove_from_list
	private void removeFromList(final boolean system) {
		if (system) {
			SYSTEM_PORTS.remove(this);
		} else {
			PORTS.remove(this);
		}
	}

	//originally PORT::lookup_by_name
	private static TitanPort lookupByName(final String parameterPortName, final boolean system) {
		if (system) {
			for (TitanPort port : SYSTEM_PORTS) {
				if (port.portName.equals(parameterPortName)) {
					return port;
				}
			}
		} else {
			for (TitanPort port : PORTS) {
				if (port.portName.equals(parameterPortName)) {
					return port;
				}
			}
		}

		return null;
	}

	//originally PORT::activate_port
	public void activatePort(final boolean system) {
		if (!is_active) {
			addToList(system);
			is_active = true;
			//FIXME add translation port support
		}
	}

	//originally PORT::deactivate_port
	public void deActivatePort(final boolean system) {
		if (is_active) {
			//FIXME implement
			removeFromList(system);
			is_active = false;
		}
	}

	// originally PORT::deactivate_all
	public static void deactivateAll() {
		for (TitanPort port : PORTS) {
			port.deActivatePort(false);
		}
		for (TitanPort port : SYSTEM_PORTS) {
			port.deActivatePort(true);
		}
	}

	public void start() {
		if (!is_active) {
			throw new TtcnError(MessageFormat.format("Internal error: Inactive port {0} cannot be started.", portName));
		}
		if (is_started) {
			TtcnError.TtcnWarning(MessageFormat.format("Performing start operation on port {0}, which is already started. The operation will clear the incoming queue.", portName));
			clearQueue();
		} else {
			if(is_halted) {
				
			// the queue might contain old messages which has to be discarded
			clearQueue();
			is_halted = false;
			}
			userStart();
			is_started = true;
		}
		//TODO: TTCN_Logger::log_port_state
	}

	public static void all_start() {
		for (TitanPort port : PORTS) {
			port.start();
		}
		for (TitanPort port : SYSTEM_PORTS) {
			port.start();
		}
	}

	public void stop() {
		if (!is_active) {
			throw new TtcnError(MessageFormat.format("Internal error: Inactive port {0} cannot be stopped.", portName));
		}
		//FIXME implement
		is_started = false;
		userStop();
	}

	public TitanAlt_Status receive(final TitanComponent_template sender_template, final TitanComponent sender_pointer) {
		// FIXME logging
		return TitanAlt_Status.ALT_NO;
	}

	//originally any_receive
	public static TitanAlt_Status any_receive(final TitanComponent_template sender_template, final TitanComponent sender_pointer) {
		if (PORTS.isEmpty()) {
			// FIXME log error
			return TitanAlt_Status.ALT_NO;
		}

		TitanAlt_Status returnValue = TitanAlt_Status.ALT_NO;
		for (TitanPort port : PORTS) {
			switch(port.receive(sender_template, sender_pointer)) {
			case ALT_YES:
				return TitanAlt_Status.ALT_YES;
			case ALT_MAYBE:
				returnValue = TitanAlt_Status.ALT_MAYBE;
			case ALT_NO:
				break;
			default:
				throw new TtcnError(MessageFormat.format("Internal error: Receive operation returned unexpected status code on port {0} while evaluating `any port.receive'.", port.portName));
			}
		}

		return returnValue;
	}

	public TitanAlt_Status check_receive(final TitanComponent_template sender_template, final TitanComponent sender_pointer) {
		// FIXME logging
		return TitanAlt_Status.ALT_NO;
	}

	//originally any_receive
	public static TitanAlt_Status any_check_receive(final TitanComponent_template sender_template, final TitanComponent sender_pointer) {
		if (PORTS.isEmpty()) {
			// FIXME log error
			return TitanAlt_Status.ALT_NO;
		}

		TitanAlt_Status returnValue = TitanAlt_Status.ALT_NO;
		for (TitanPort port : PORTS) {
			switch(port.check_receive(sender_template, sender_pointer)) {
			case ALT_YES:
				return TitanAlt_Status.ALT_YES;
			case ALT_MAYBE:
				returnValue = TitanAlt_Status.ALT_MAYBE;
			case ALT_NO:
				break;
			default:
				throw new TtcnError(MessageFormat.format("Internal error: Check-receive operation returned unexpected status code on port {0} while evaluating `any port.check(receive)'.", port.portName));
			}
		}

		return returnValue;
	}

	public TitanAlt_Status trigger(final TitanComponent_template sender_template, final TitanComponent sender_pointer) {
		// FIXME logging
		return TitanAlt_Status.ALT_NO;
	}

	//originally any_receive
	public static TitanAlt_Status any_trigger(final TitanComponent_template sender_template, final TitanComponent sender_pointer) {
		if (PORTS.isEmpty()) {
			// FIXME log error
			return TitanAlt_Status.ALT_NO;
		}

		TitanAlt_Status returnValue = TitanAlt_Status.ALT_NO;
		for (TitanPort port : PORTS) {
			switch(port.trigger(sender_template, sender_pointer)) {
			case ALT_YES:
				return TitanAlt_Status.ALT_YES;
			case ALT_MAYBE:
				returnValue = TitanAlt_Status.ALT_MAYBE;
			case ALT_NO:
				break;
			default:
				throw new TtcnError(MessageFormat.format("Internal error: Trigger operation returned unexpected status code on port {0} while evaluating `any port.trigger'.", port.portName));
			}
		}

		return returnValue;
	}

	public TitanAlt_Status getcall(final TitanComponent_template sender_template, final TitanComponent sender_pointer) {
		return TitanAlt_Status.ALT_NO;
	}

	//originally any_getcall
	public static TitanAlt_Status any_getcall(final TitanComponent_template sender_template, final TitanComponent sender_pointer) {
		if (PORTS.isEmpty()) {
			// FIXME log error
			return TitanAlt_Status.ALT_NO;
		}

		TitanAlt_Status returnValue = TitanAlt_Status.ALT_NO;
		for (TitanPort port : PORTS) {
			switch(port.getcall(sender_template, sender_pointer)) {
			case ALT_YES:
				return TitanAlt_Status.ALT_YES;
			case ALT_MAYBE:
				returnValue = TitanAlt_Status.ALT_MAYBE;
			case ALT_NO:
				break;
			default:
				throw new TtcnError(MessageFormat.format("Internal error: Getcall operation returned unexpected status code on port {0} while evaluating `any port.getcall'.", port.portName));
			}
		}

		return returnValue;
	}

	public TitanAlt_Status check_getcall(final TitanComponent_template sender_template, final TitanComponent sender_pointer) {
		return TitanAlt_Status.ALT_NO;
	}

	//originally any_check_getcall
	public static TitanAlt_Status any_check_getcall(final TitanComponent_template sender_template, final TitanComponent sender_pointer) {
		if (PORTS.isEmpty()) {
			// FIXME log error
			return TitanAlt_Status.ALT_NO;
		}

		TitanAlt_Status returnValue = TitanAlt_Status.ALT_NO;
		for (TitanPort port : PORTS) {
			switch(port.check_getcall(sender_template, sender_pointer)) {
			case ALT_YES:
				return TitanAlt_Status.ALT_YES;
			case ALT_MAYBE:
				returnValue = TitanAlt_Status.ALT_MAYBE;
			case ALT_NO:
				break;
			default:
				throw new TtcnError(MessageFormat.format("Internal error: Check-getcall operation returned unexpected status code on port {0} while evaluating `any port.check(getcall)'.", port.portName));
			}
		}

		return returnValue;
	}

	public TitanAlt_Status getreply(final TitanComponent_template sender_template, final TitanComponent sender_pointer) {
		return TitanAlt_Status.ALT_NO;
	}

	//originally any_getreply
	public static TitanAlt_Status any_getreply(final TitanComponent_template sender_template, final TitanComponent sender_pointer) {
		if (PORTS.isEmpty()) {
			// FIXME log error
			return TitanAlt_Status.ALT_NO;
		}

		TitanAlt_Status returnValue = TitanAlt_Status.ALT_NO;
		for (TitanPort port : PORTS) {
			switch(port.getreply(sender_template, sender_pointer)) {
			case ALT_YES:
				return TitanAlt_Status.ALT_YES;
			case ALT_MAYBE:
				returnValue = TitanAlt_Status.ALT_MAYBE;
			case ALT_NO:
				break;
			default:
				throw new TtcnError(MessageFormat.format("Internal error: Getreply operation returned unexpected status code on port {0} while evaluating `any port.getreply'.", port.portName));
			}
		}

		return returnValue;
	}

	public TitanAlt_Status check_getreply(final TitanComponent_template sender_template, final TitanComponent sender_pointer) {
		return TitanAlt_Status.ALT_NO;
	}

	//originally any_check_getreply
	public static TitanAlt_Status any_check_getreply(final TitanComponent_template sender_template, final TitanComponent sender_pointer) {
		if (PORTS.isEmpty()) {
			// FIXME log error
			return TitanAlt_Status.ALT_NO;
		}

		TitanAlt_Status returnValue = TitanAlt_Status.ALT_NO;
		for (TitanPort port : PORTS) {
			switch(port.check_getreply(sender_template, sender_pointer)) {
			case ALT_YES:
				return TitanAlt_Status.ALT_YES;
			case ALT_MAYBE:
				returnValue = TitanAlt_Status.ALT_MAYBE;
			case ALT_NO:
				break;
			default:
				throw new TtcnError(MessageFormat.format("Internal error: Check-getreply operation returned unexpected status code on port {0} while evaluating `any port.check(getreply)'.", port.portName));
			}
		}

		return returnValue;
	}

	public TitanAlt_Status get_exception(final TitanComponent_template sender_template, final TitanComponent sender_pointer) {
		return TitanAlt_Status.ALT_NO;
	}

	//originally any_catch
	public static TitanAlt_Status any_catch(final TitanComponent_template sender_template, final TitanComponent sender_pointer) {
		if (PORTS.isEmpty()) {
			// FIXME log error
			return TitanAlt_Status.ALT_NO;
		}

		TitanAlt_Status returnValue = TitanAlt_Status.ALT_NO;
		for (TitanPort port : PORTS) {
			switch(port.get_exception(sender_template, sender_pointer)) {
			case ALT_YES:
				return TitanAlt_Status.ALT_YES;
			case ALT_MAYBE:
				returnValue = TitanAlt_Status.ALT_MAYBE;
			case ALT_NO:
				break;
			default:
				throw new TtcnError(MessageFormat.format("Internal error: Catch operation returned unexpected status code on port {0} while evaluating `any port.catch'.", port.portName));
			}
		}

		return returnValue;
	}

	public TitanAlt_Status check_catch(final TitanComponent_template sender_template, final TitanComponent sender_pointer) {
		return TitanAlt_Status.ALT_NO;
	}

	//originally any_check_catch
	public static TitanAlt_Status any_check_catch(final TitanComponent_template sender_template, final TitanComponent sender_pointer) {
		if (PORTS.isEmpty()) {
			// FIXME log error
			return TitanAlt_Status.ALT_NO;
		}

		TitanAlt_Status returnValue = TitanAlt_Status.ALT_NO;
		for (TitanPort port : PORTS) {
			switch(port.check_getreply(sender_template, sender_pointer)) {
			case ALT_YES:
				return TitanAlt_Status.ALT_YES;
			case ALT_MAYBE:
				returnValue = TitanAlt_Status.ALT_MAYBE;
			case ALT_NO:
				break;
			default:
				throw new TtcnError(MessageFormat.format("Internal error: Check-catch operation returned unexpected status code on port {0} while evaluating `any port.check(catch)'.", port.portName));
			}
		}

		return returnValue;
	}

	public TitanAlt_Status check(final TitanComponent_template sender_template, final TitanComponent sender_pointer) {
		TitanAlt_Status returnValue = TitanAlt_Status.ALT_NO;
		// the procedure-based queue must have the higher priority
		switch(check_getcall(sender_template, sender_pointer)) {
		case ALT_YES:
			return TitanAlt_Status.ALT_YES;
		case ALT_MAYBE:
			returnValue = TitanAlt_Status.ALT_MAYBE;
		case ALT_NO:
			break;
		default:
			throw new TtcnError(MessageFormat.format("Internal error: Check-getcall operation returned unexpected status code on port {0}.", portName));
		}
		if (!TitanAlt_Status.ALT_MAYBE.equals(returnValue)) {
			// don't try getreply if the procedure-based queue is empty
			// (i.e. check_getcall() returned ALT_MAYBE)
			switch(check_getreply(sender_template, sender_pointer)) {
			case ALT_YES:
				return TitanAlt_Status.ALT_YES;
			case ALT_MAYBE:
				returnValue = TitanAlt_Status.ALT_MAYBE;
			case ALT_NO:
				break;
			default:
				throw new TtcnError(MessageFormat.format("Internal error: Check-getreply operation returned unexpected status code on port {0}.", portName));
			}
		}
		if (!TitanAlt_Status.ALT_MAYBE.equals(returnValue)) {
			// don't try catch if the procedure-based queue is empty
			// (i.e. check_getcall() or check_getreply() returned ALT_MAYBE)
			switch(check_catch(sender_template, sender_pointer)) {
			case ALT_YES:
				return TitanAlt_Status.ALT_YES;
			case ALT_MAYBE:
				returnValue = TitanAlt_Status.ALT_MAYBE;
			case ALT_NO:
				break;
			default:
				throw new TtcnError(MessageFormat.format("Internal error: Check-catch operation returned unexpected status code on port {0}.", portName));
			}
		}
		switch(check_receive(sender_template, sender_pointer)) {
		case ALT_YES:
			return TitanAlt_Status.ALT_YES;
		case ALT_MAYBE:
			returnValue = TitanAlt_Status.ALT_MAYBE;
		case ALT_NO:
			break;
		default:
			throw new TtcnError(MessageFormat.format("Internal error: Check-receive operation returned unexpected status code on port {0}.", portName));
		}

		return returnValue;
	}

	//originally any_check
	public static TitanAlt_Status any_check(final TitanComponent_template sender_template, final TitanComponent sender_pointer) {
		if (PORTS.isEmpty()) {
			// FIXME log error
			return TitanAlt_Status.ALT_NO;
		}

		TitanAlt_Status returnValue = TitanAlt_Status.ALT_NO;
		for (TitanPort port : PORTS) {
			switch(port.check(sender_template, sender_pointer)) {
			case ALT_YES:
				return TitanAlt_Status.ALT_YES;
			case ALT_MAYBE:
				returnValue = TitanAlt_Status.ALT_MAYBE;
			case ALT_NO:
				break;
			default:
				throw new TtcnError(MessageFormat.format("Internal error: Check operation returned unexpected status code on port {0} while evaluating `any port.check'.", port.portName));
			}
		}

		return returnValue;
	}

	protected void Install_Handler(final Set<SelectableChannel> readChannels, final Set<SelectableChannel> writeChannels, final double callInterval) throws IOException {
		if (!is_active) {
			throw new TtcnError(MessageFormat.format("Event handler cannot be installed for inactive port {0}.", portName));
		}

		//FIXME register handler
		if (readChannels != null) {
			for(SelectableChannel channel : readChannels) {
				channel.configureBlocking(false);
				TTCN_Snapshot.channelMap.put(channel, this);
				channel.register(TTCN_Snapshot.selector, SelectionKey.OP_READ);
			}
		}
	}

	protected void Uninstall_Handler() throws IOException {
		final ArrayList<SelectableChannel> tobeRemoved = new ArrayList<SelectableChannel>();
		for (Map.Entry<SelectableChannel, TitanPort> entry: TTCN_Snapshot.channelMap.entrySet()) {
			if (entry.getValue() == this) {
				tobeRemoved.add(entry.getKey());
			}
		}

		for (SelectableChannel channel : tobeRemoved) {
			channel.close();
			TTCN_Snapshot.channelMap.remove(channel);
		}
	}

	public void Handle_Event(final SelectableChannel channel, final boolean isReadable, final boolean isWriteable) {
		//FIXME implement default
	}

	protected void userMap(final String systemPort) {
		//default implementation is empty
	}

	protected void userUnmap(final String systemPort) {
		//default implementation is empty
	}

	protected void userStart(){
		//default implementation is empty
	}

	protected void userStop() {
		//default implementation is empty
	}

	protected void clearQueue() {
		//default implementation is empty
	}

	//originally get_default_destination
	protected int getDefaultDestination() {
		//FIXME implement connection checks
		if (systemMappings.size() > 1) {
			throw new TtcnError(MessageFormat.format("Port {0} has more than one mappings. Message cannot be sent on it to system.", portName));
		} else if (systemMappings.isEmpty()) {
			throw new TtcnError(MessageFormat.format("Port {0} has neither connections nor mappings. Message cannot be sent on it.", portName));
		}

		return TitanComponent.SYSTEM_COMPREF;
	}



	// FIXME handle translation ports
	private final void map(final String systemPort) {
		if (!is_active) {
			throw new TtcnError(MessageFormat.format("Inactive port {0} cannot be mapped.", portName));
		}

		for (int i = 0; i < systemMappings.size(); i++) {
			if (systemPort.equals(systemMappings.get(i))) {
				TtcnError.TtcnWarning(MessageFormat.format("Port {0} is already mapped to system:{1}.\n Map operation was ignored.",
						portName, systemPort));
				return;
			}
		}

		userMap(systemPort);

		// the mapping shall be registered in the table only if user_map() was successful
		systemMappings.add(systemPort);
		if (systemMappings.size() > 1) {
			TtcnError.TtcnWarning(MessageFormat.format("Port {0} has now more than one mappings."
					+ " Message cannot be sent on it to system even with explicit addressing.", portName));
		}
	}

	// FIXME handle translation ports
	private final void unmap(final String systemPort) {
		int deletionPosition;
		for (deletionPosition = 0; deletionPosition < systemMappings.size(); deletionPosition++) {
			if (systemPort.equals(systemMappings.get(deletionPosition))) {
				break;
			}
		}

		if (deletionPosition >= systemMappings.size()) {
			TtcnError.TtcnWarning(MessageFormat.format("Port {0} is not mapped to system:{1}. " + "Unmap operation was ignored.",
					portName, systemPort));
			return;
		}

		systemMappings.remove(deletionPosition);

		userUnmap(systemPort);

		// FIXME implement
	}

	public static void mapPort(final String componentPort, final String systemPort, final boolean translation) {
		//FIXME this is actually more complex
		final TitanPort port = lookupByName(componentPort, false);
		if (port == null) {
			throw new TtcnError(MessageFormat.format("Map operation refers to non-existent port {0}.", componentPort));
		}
		//FIXME add support for translation and single mode check
		port.map(componentPort);
	}

	public static void unmapPort(final String componentPort, final String systemPort, final boolean translation) {
		//FIXME this is actually more complex
		final TitanPort port = lookupByName(componentPort, false);
		if (port == null) {
			throw new TtcnError(MessageFormat.format("Unmap operation refers to non-existent port {0}.", componentPort));
		}
		//FIXME add support for translation and single mode check
		port.unmap(componentPort);
	}

	public void setName(final String name) {
		if(name == null) {
			throw new TtcnError("Internal error: Setting an invalid name for a single element of a port array.");
		}
		portName = name;
	}
	
	public void log() {
		TtcnLogger.log_event("port %s", portName);
	}
}

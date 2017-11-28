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

	protected String portName;
	protected int msg_head_count;
	protected int proc_head_count;
	//temporary variable
	protected int proc_tail_count;
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
			msg_head_count = 0;
			proc_head_count = 0;
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
		final LinkedList<TitanPort> temp = new LinkedList<TitanPort>(PORTS);
		for (TitanPort port : temp) {
			port.deActivatePort(false);
		}
		temp.clear();
		temp.addAll(SYSTEM_PORTS);
		for (TitanPort port : temp) {
			port.deActivatePort(true);
		}
	}

	public void clear() {
		if (!is_active) {
			throw new TtcnError(MessageFormat.format("Internal error: Inactive port {0} cannot be cleared.", portName));
		}
		if (!is_started && !is_halted) {
			TtcnError.TtcnWarning(MessageFormat.format("Performing clear operation on port {0}, which is already stopped. The operation has no effect.", portName));

		}
		clearQueue();
		//TODO: TTCN_Logger::log_port_misc
	}

	public static void all_clear() {
		for (TitanPort port : PORTS) {
			port.clear();
		}
		for (TitanPort port : SYSTEM_PORTS) {
			port.clear();
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
		TtcnLogger.log_port_state(TtcnLogger.Port_State_operation.STARTED, portName);
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
		if(is_started) {
			is_started = false;
			is_halted = false;
			userStop();
			// dropping all messages from the queue because they cannot be extracted by receiving operations anymore
			clearQueue();
		} else if(is_halted) {
			is_halted = false;
			clearQueue();
		} else {
			TtcnError.TtcnWarning(MessageFormat.format("Performing stop operation on port {0}, which is already stopped. The operation has no effect.", portName));
		}
		TtcnLogger.log_port_state(TtcnLogger.Port_State_operation.STOPPED, portName);
	}

	public static void all_stop() {
		for (TitanPort port : PORTS) {
			port.stop();
		}
		for (TitanPort port : SYSTEM_PORTS) {
			port.stop();
		}
	}

	public void halt() {
		if (!is_active) {
			throw new TtcnError(MessageFormat.format("Internal error: Inactive port {0} cannot be halted.", portName));
		}
		if(is_started) {
			is_started = false;
			is_halted = true;
			userStop();
			// keep the messages in the queue
		} else if(is_halted) {
			TtcnError.TtcnWarning(MessageFormat.format("Performing halt operation on port {0}, which is already halted. The operation has no effect.", portName));
		} else {
			TtcnError.TtcnWarning(MessageFormat.format("Performing halt operation on port {0}, which is already stopped. The operation has no effect.", portName));
		}
		TtcnLogger.log_port_state(TtcnLogger.Port_State_operation.HALTED, portName);
	}

	public static void all_halt() {
		for (TitanPort port : PORTS) {
			port.halt();
		}
		for (TitanPort port : SYSTEM_PORTS) {
			port.halt();
		}
	}

	//originally check_port_state
	public boolean checkPortState(final String type) {
		if ("Started".equals(type)) {
			return is_started;
		} else if ("Halted".equals(type)) {
			return is_halted;
		} else if ("Stopped".equals(type)) {
			return (!is_started && !is_halted);
		} else if ("Connected".equals(type)) {
			return false;//FIXME connection_list_head
		} else if ("Mapped".equals(type)) {
			return !systemMappings.isEmpty();
		} else if ("Linked".equals(type)) {
			return !systemMappings.isEmpty();//FIXME connection_list_head
		}
		throw new TtcnError(MessageFormat.format("{0} is not an allowed parameter of checkstate().", type));
	}

	//originally check_port_state
	public boolean checkPortState(final TitanCharString type) {
		return checkPortState(type.getValue().toString());
	}

	// originally any_check_port_state
	public static boolean any_checkPortState(final String type) {
		for (TitanPort port : PORTS) {
			if (port.checkPortState(type)) {
				return true;
			}
		}
		for (TitanPort port : SYSTEM_PORTS) {
			if (port.checkPortState(type)) {
				return true;
			}
		}

		return false;
	}

	// originally any_check_port_state
	public static boolean any_checkPortState(final TitanCharString type) {
		return any_checkPortState(type.getValue().toString());
	}

	//originally all_check_port_state
	public static boolean all_checkPortState(final String type) {
		for (TitanPort port : PORTS) {
			if (!port.checkPortState(type)) {
				return false;
			}
		}
		for (TitanPort port : SYSTEM_PORTS) {
			if (!port.checkPortState(type)) {
				return false;
			}
		}

		return true;
	}

	//originally all_check_port_state
	public static boolean all_checkPortState(final TitanCharString type) {
		return all_checkPortState(type.getValue().toString());
	}

	public TitanAlt_Status receive(final TitanComponent_template sender_template, final TitanComponent sender_pointer, final Index_Redirect index_redirect) {
		TtcnLogger.log_matching_problem(TtcnLogger.MatchingProblemType_reason.NO_INCOMING_TYPES, TtcnLogger.MatchingProblemType_operation.RECEIVE_, false, false, portName);
		return TitanAlt_Status.ALT_NO;
	}

	//originally any_receive
	public static TitanAlt_Status any_receive(final TitanComponent_template sender_template, final TitanComponent sender_pointer) {
		if (PORTS.isEmpty()) {
			TtcnLogger.log_matching_problem(TtcnLogger.MatchingProblemType_reason.COMPONENT_HAS_NO_PORTS, TtcnLogger.MatchingProblemType_operation.RECEIVE_, true, false, null);
			return TitanAlt_Status.ALT_NO;
		}

		TitanAlt_Status returnValue = TitanAlt_Status.ALT_NO;
		for (TitanPort port : PORTS) {
			switch(port.receive(sender_template, sender_pointer, null)) {
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

	public TitanAlt_Status check_receive(final TitanComponent_template sender_template, final TitanComponent sender_pointer, final Index_Redirect index_redirect) {
		TtcnLogger.log_matching_problem(TtcnLogger.MatchingProblemType_reason.NO_INCOMING_TYPES, TtcnLogger.MatchingProblemType_operation.RECEIVE_, false, true, portName);
		return TitanAlt_Status.ALT_NO;
	}

	//originally any_receive
	public static TitanAlt_Status any_check_receive(final TitanComponent_template sender_template, final TitanComponent sender_pointer) {
		if (PORTS.isEmpty()) {
		    TtcnLogger.log_matching_problem(TtcnLogger.MatchingProblemType_reason.COMPONENT_HAS_NO_PORTS, TtcnLogger.MatchingProblemType_operation.RECEIVE_, true, true, null);
			return TitanAlt_Status.ALT_NO;
		}

		TitanAlt_Status returnValue = TitanAlt_Status.ALT_NO;
		for (TitanPort port : PORTS) {
			switch(port.check_receive(sender_template, sender_pointer, null)) {
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

	public TitanAlt_Status trigger(final TitanComponent_template sender_template, final TitanComponent sender_pointer, final Index_Redirect index_redirect) {
	    TtcnLogger.log_matching_problem(TtcnLogger.MatchingProblemType_reason.NO_INCOMING_TYPES, TtcnLogger.MatchingProblemType_operation.TRIGGER_, false, false, portName);
		return TitanAlt_Status.ALT_NO;
	}

	//originally any_receive
	public static TitanAlt_Status any_trigger(final TitanComponent_template sender_template, final TitanComponent sender_pointer) {
		if (PORTS.isEmpty()) {
		    TtcnLogger.log_matching_problem(TtcnLogger.MatchingProblemType_reason.COMPONENT_HAS_NO_PORTS, TtcnLogger.MatchingProblemType_operation.TRIGGER_, true, false, null);
			return TitanAlt_Status.ALT_NO;
		}

		TitanAlt_Status returnValue = TitanAlt_Status.ALT_NO;
		for (TitanPort port : PORTS) {
			switch(port.trigger(sender_template, sender_pointer, null)) {
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

	public TitanAlt_Status getcall(final TitanComponent_template sender_template, final TitanComponent sender_pointer, final Index_Redirect index_redirect) {
		return TitanAlt_Status.ALT_NO;
	}

	//originally any_getcall
	public static TitanAlt_Status any_getcall(final TitanComponent_template sender_template, final TitanComponent sender_pointer) {
		if (PORTS.isEmpty()) {
		    TtcnLogger.log_matching_problem(TtcnLogger.MatchingProblemType_reason.COMPONENT_HAS_NO_PORTS, TtcnLogger.MatchingProblemType_operation.GETCALL_, true, false, null);
			return TitanAlt_Status.ALT_NO;
		}

		TitanAlt_Status returnValue = TitanAlt_Status.ALT_NO;
		for (TitanPort port : PORTS) {
			switch(port.getcall(sender_template, sender_pointer, null)) {
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

	public TitanAlt_Status check_getcall(final TitanComponent_template sender_template, final TitanComponent sender_pointer, final Index_Redirect index_redirect) {
		return TitanAlt_Status.ALT_NO;
	}

	//originally any_check_getcall
	public static TitanAlt_Status any_check_getcall(final TitanComponent_template sender_template, final TitanComponent sender_pointer) {
		if (PORTS.isEmpty()) {
		    TtcnLogger.log_matching_problem(TtcnLogger.MatchingProblemType_reason.COMPONENT_HAS_NO_PORTS, TtcnLogger.MatchingProblemType_operation.GETCALL_, true, true, null);
			return TitanAlt_Status.ALT_NO;
		}

		TitanAlt_Status returnValue = TitanAlt_Status.ALT_NO;
		for (TitanPort port : PORTS) {
			switch(port.check_getcall(sender_template, sender_pointer, null)) {
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

	public TitanAlt_Status getreply(final TitanComponent_template sender_template, final TitanComponent sender_pointer, final Index_Redirect index_redirect) {
		return TitanAlt_Status.ALT_NO;
	}

	//originally any_getreply
	public static TitanAlt_Status any_getreply(final TitanComponent_template sender_template, final TitanComponent sender_pointer) {
		if (PORTS.isEmpty()) {
		    TtcnLogger.log_matching_problem(TtcnLogger.MatchingProblemType_reason.COMPONENT_HAS_NO_PORTS, TtcnLogger.MatchingProblemType_operation.GETREPLY_, true, false, null);
			return TitanAlt_Status.ALT_NO;
		}

		TitanAlt_Status returnValue = TitanAlt_Status.ALT_NO;
		for (TitanPort port : PORTS) {
			switch(port.getreply(sender_template, sender_pointer, null)) {
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

	public TitanAlt_Status check_getreply(final TitanComponent_template sender_template, final TitanComponent sender_pointer, final Index_Redirect index_redirect) {
		return TitanAlt_Status.ALT_NO;
	}

	//originally any_check_getreply
	public static TitanAlt_Status any_check_getreply(final TitanComponent_template sender_template, final TitanComponent sender_pointer) {
		if (PORTS.isEmpty()) {
		    TtcnLogger.log_matching_problem(TtcnLogger.MatchingProblemType_reason.COMPONENT_HAS_NO_PORTS, TtcnLogger.MatchingProblemType_operation.GETREPLY_, true, true, null);
			return TitanAlt_Status.ALT_NO;
		}

		TitanAlt_Status returnValue = TitanAlt_Status.ALT_NO;
		for (TitanPort port : PORTS) {
			switch(port.check_getreply(sender_template, sender_pointer, null)) {
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

	public TitanAlt_Status get_exception(final TitanComponent_template sender_template, final TitanComponent sender_pointer, final Index_Redirect index_redirect) {
		return TitanAlt_Status.ALT_NO;
	}

	//originally any_catch
	public static TitanAlt_Status any_catch(final TitanComponent_template sender_template, final TitanComponent sender_pointer) {
		if (PORTS.isEmpty()) {
		    TtcnLogger.log_matching_problem(TtcnLogger.MatchingProblemType_reason.COMPONENT_HAS_NO_PORTS, TtcnLogger.MatchingProblemType_operation.CATCH_, true, false, null);
			return TitanAlt_Status.ALT_NO;
		}

		TitanAlt_Status returnValue = TitanAlt_Status.ALT_NO;
		for (TitanPort port : PORTS) {
			switch(port.get_exception(sender_template, sender_pointer, null)) {
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

	public TitanAlt_Status check_catch(final TitanComponent_template sender_template, final TitanComponent sender_pointer, final Index_Redirect index_redirect) {
		return TitanAlt_Status.ALT_NO;
	}

	//originally any_check_catch
	public static TitanAlt_Status any_check_catch(final TitanComponent_template sender_template, final TitanComponent sender_pointer) {
		if (PORTS.isEmpty()) {
		    TtcnLogger.log_matching_problem(TtcnLogger.MatchingProblemType_reason.COMPONENT_HAS_NO_PORTS, TtcnLogger.MatchingProblemType_operation.CATCH_, true, true, null);
			return TitanAlt_Status.ALT_NO;
		}

		TitanAlt_Status returnValue = TitanAlt_Status.ALT_NO;
		for (TitanPort port : PORTS) {
			switch(port.check_getreply(sender_template, sender_pointer, null)) {
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

	public TitanAlt_Status check(final TitanComponent_template sender_template, final TitanComponent sender_pointer, final Index_Redirect index_redirect) {
		TitanAlt_Status returnValue = TitanAlt_Status.ALT_NO;
		// the procedure-based queue must have the higher priority
		switch(check_getcall(sender_template, sender_pointer, null)) {
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
			switch(check_getreply(sender_template, sender_pointer, null)) {
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
			switch(check_catch(sender_template, sender_pointer, null)) {
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
		switch(check_receive(sender_template, sender_pointer, null)) {
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
			TtcnLogger.log_matching_problem(TtcnLogger.MatchingProblemType_reason.COMPONENT_HAS_NO_PORTS, TtcnLogger.MatchingProblemType_operation.CHECK_, true, false, null);
			return TitanAlt_Status.ALT_NO;
		}

		TitanAlt_Status returnValue = TitanAlt_Status.ALT_NO;
		for (TitanPort port : PORTS) {
			switch(port.check(sender_template, sender_pointer, null)) {
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

	protected void prepare_message(final Text_Buf outgoing_buf, final String message_type) {
		throw new TtcnError("prepare_message not yet implemented");
	}

	protected void prepare_call(final Text_Buf outgoing_buf, final String signature_name) {
		throw new TtcnError("prepare_call not yet implemented");
	}

	protected void prepare_reply(final Text_Buf outgoing_buf, final String signature_name) {
		throw new TtcnError("prepare_reply not yet implemented");
	}

	protected void prepare_exception(final Text_Buf outgoing_buf, final String signature_name) {
		throw new TtcnError("prepare_exception not yet implemented");
	}

	protected void send_data(final Text_Buf outgoing_buf, final TitanComponent destination_component) {
		throw new TtcnError(MessageFormat.format("Sending messages on port {0}, is not yet supported.", getName()));
	}

	protected boolean process_message(final String message_type, final Text_Buf incoming_buf, final int sender_component, final TitanOctetString slider) {
		return false;
	}

	protected boolean process_call(final String signature_name, final Text_Buf incoming_buf, final int sender_component) {
		return false;
	}

	protected boolean process_reply(final String signature_name, final Text_Buf incoming_buf, final int sender_component) {
		return false;
	}

	protected boolean process_exception(final String signature_name, final Text_Buf incoming_buf, final int sender_component) {
		return false;
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

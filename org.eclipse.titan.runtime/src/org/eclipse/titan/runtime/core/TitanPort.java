/******************************************************************************
 * Copyright (c) 2000-2018 Ericsson Telecom AB
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

import org.eclipse.titan.runtime.core.Event_Handler.Channel_And_Timeout_Event_Handler;

/**
 * The base class of test ports
 *
 * TODO: lots to implement
 *
 * @author Kristof Szabados
 */
public class TitanPort extends Channel_And_Timeout_Event_Handler {
	// originally the list stored in list_head list_tail
	private static final LinkedList<TitanPort> PORTS = new LinkedList<TitanPort>();
	// originally the list stored in system_list_head and system_list_tail
	private static final LinkedList<TitanPort> SYSTEM_PORTS = new LinkedList<TitanPort>();

	protected String port_name;
	protected int msg_head_count;
	protected int proc_head_count;
	//temporary variable
	protected int proc_tail_count;
	protected boolean is_active;
	protected boolean is_started;
	protected boolean is_halted;

	private ArrayList<String> system_mappings = new ArrayList<String>();

	public TitanPort(final String portName) {
		this.port_name = portName;
		is_active = false;
		is_started = false;
	}

	protected TitanPort() {}

	public String get_name() {
		return port_name;
	}

	//originally PORT::add_to_list
	private void add_to_list(final boolean system) {
		if (system) {
			for (final TitanPort port : SYSTEM_PORTS) {
				if (port == this) {
					return;
				}
				if (port.port_name.equals(port_name)) {
					throw new TtcnError(MessageFormat.format("Internal error: There are more than one ports with name {0}.", port_name));
				}
			}

			SYSTEM_PORTS.add(this);
		} else {
			for (final TitanPort port : PORTS) {
				if (port == this) {
					return;
				}
				if (port.port_name.equals(port_name)) {
					throw new TtcnError(MessageFormat.format("Internal error: There are more than one ports with name {0}.", port_name));
				}
			}

			PORTS.add(this);
		}
	}

	//originally PORT::remove_from_list
	private void remove_from_list(final boolean system) {
		if (system) {
			SYSTEM_PORTS.remove(this);
		} else {
			PORTS.remove(this);
		}
	}

	//originally PORT::lookup_by_name
	private static TitanPort lookup_by_name(final String parameter_port_name, final boolean system) {
		if (system) {
			for (final TitanPort port : SYSTEM_PORTS) {
				if (port.port_name.equals(parameter_port_name)) {
					return port;
				}
			}
		} else {
			for (final TitanPort port : PORTS) {
				if (port.port_name.equals(parameter_port_name)) {
					return port;
				}
			}
		}

		return null;
	}

	//originally PORT::activate_port
	public void activate_port(final boolean system) {
		if (!is_active) {
			add_to_list(system);
			is_active = true;
			msg_head_count = 0;
			proc_head_count = 0;
			//FIXME add translation port support
		}
	}

	//originally PORT::deactivate_port
	public void deactivate_port(final boolean system) {
		if (is_active) {
			//FIXME implement
			remove_from_list(system);
			is_active = false;
		}
	}

	// originally PORT::deactivate_all
	public static void deactivate_all() {
		final LinkedList<TitanPort> temp = new LinkedList<TitanPort>(PORTS);
		for (final TitanPort port : temp) {
			port.deactivate_port(false);
		}
		temp.clear();
		temp.addAll(SYSTEM_PORTS);
		for (final TitanPort port : temp) {
			port.deactivate_port(true);
		}
	}

	public void clear() {
		if (!is_active) {
			throw new TtcnError(MessageFormat.format("Internal error: Inactive port {0} cannot be cleared.", port_name));
		}
		if (!is_started && !is_halted) {
			TtcnError.TtcnWarning(MessageFormat.format("Performing clear operation on port {0}, which is already stopped. The operation has no effect.", port_name));

		}
		clear_queue();
		TtcnLogger.log_port_misc(TitanLoggerApi.Port__Misc_reason.enum_type.port__was__cleared, port_name, 0, "", "", 0, 0);
	}

	public static void all_clear() {
		for (final TitanPort port : PORTS) {
			port.clear();
		}
		for (final TitanPort port : SYSTEM_PORTS) {
			port.clear();
		}
	}

	public void start() {
		if (!is_active) {
			throw new TtcnError(MessageFormat.format("Internal error: Inactive port {0} cannot be started.", port_name));
		}
		if (is_started) {
			TtcnError.TtcnWarning(MessageFormat.format("Performing start operation on port {0}, which is already started. The operation will clear the incoming queue.", port_name));
			clear_queue();
		} else {
			if (is_halted) {
				// the queue might contain old messages which has to be discarded
				clear_queue();
				is_halted = false;
			}
			user_start();
			is_started = true;
		}
		TtcnLogger.log_port_state(TitanLoggerApi.Port__State_operation.enum_type.started, port_name);
	}

	public static void all_start() {
		for (final TitanPort port : PORTS) {
			port.start();
		}
		for (final TitanPort port : SYSTEM_PORTS) {
			port.start();
		}
	}

	public void stop() {
		if (!is_active) {
			throw new TtcnError(MessageFormat.format("Internal error: Inactive port {0} cannot be stopped.", port_name));
		}
		if (is_started) {
			is_started = false;
			is_halted = false;
			user_stop();
			// dropping all messages from the queue because they cannot be extracted by receiving operations anymore
			clear_queue();
		} else if (is_halted) {
			is_halted = false;
			clear_queue();
		} else {
			TtcnError.TtcnWarning(MessageFormat.format("Performing stop operation on port {0}, which is already stopped. The operation has no effect.", port_name));
		}
		TtcnLogger.log_port_state(TitanLoggerApi.Port__State_operation.enum_type.stopped, port_name);
	}

	public static void all_stop() {
		for (final TitanPort port : PORTS) {
			port.stop();
		}
		for (final TitanPort port : SYSTEM_PORTS) {
			port.stop();
		}
	}

	public void halt() {
		if (!is_active) {
			throw new TtcnError(MessageFormat.format("Internal error: Inactive port {0} cannot be halted.", port_name));
		}
		if (is_started) {
			is_started = false;
			is_halted = true;
			user_stop();
			// keep the messages in the queue
		} else if (is_halted) {
			TtcnError.TtcnWarning(MessageFormat.format("Performing halt operation on port {0}, which is already halted. The operation has no effect.", port_name));
		} else {
			TtcnError.TtcnWarning(MessageFormat.format("Performing halt operation on port {0}, which is already stopped. The operation has no effect.", port_name));
		}
		TtcnLogger.log_port_state(TitanLoggerApi.Port__State_operation.enum_type.halted, port_name);
	}

	public static void all_halt() {
		for (final TitanPort port : PORTS) {
			port.halt();
		}
		for (final TitanPort port : SYSTEM_PORTS) {
			port.halt();
		}
	}

	//originally check_port_state
	public boolean check_port_state(final String type) {
		if ("Started".equals(type)) {
			return is_started;
		} else if ("Halted".equals(type)) {
			return is_halted;
		} else if ("Stopped".equals(type)) {
			return (!is_started && !is_halted);
		} else if ("Connected".equals(type)) {
			return false;//FIXME connection_list_head
		} else if ("Mapped".equals(type)) {
			return !system_mappings.isEmpty();
		} else if ("Linked".equals(type)) {
			return !system_mappings.isEmpty();//FIXME connection_list_head
		}
		throw new TtcnError(MessageFormat.format("{0} is not an allowed parameter of checkstate().", type));
	}

	//originally check_port_state
	public boolean check_port_state(final TitanCharString type) {
		return check_port_state(type.getValue().toString());
	}

	// originally any_check_port_state
	public static boolean any_check_port_state(final String type) {
		for (final TitanPort port : PORTS) {
			if (port.check_port_state(type)) {
				return true;
			}
		}
		for (final TitanPort port : SYSTEM_PORTS) {
			if (port.check_port_state(type)) {
				return true;
			}
		}

		return false;
	}

	// originally any_check_port_state
	public static boolean any_check_port_state(final TitanCharString type) {
		return any_check_port_state(type.getValue().toString());
	}

	//originally all_check_port_state
	public static boolean all_check_port_state(final String type) {
		for (final TitanPort port : PORTS) {
			if (!port.check_port_state(type)) {
				return false;
			}
		}
		for (final TitanPort port : SYSTEM_PORTS) {
			if (!port.check_port_state(type)) {
				return false;
			}
		}

		return true;
	}

	//originally all_check_port_state
	public static boolean all_check_port_state(final TitanCharString type) {
		return all_check_port_state(type.getValue().toString());
	}

	public TitanAlt_Status receive(final TitanComponent_template sender_template, final TitanComponent sender_pointer, final Index_Redirect index_redirect) {
		TtcnLogger.log_matching_problem(TitanLoggerApi.MatchingProblemType_reason.enum_type.no__incoming__types, TitanLoggerApi.MatchingProblemType_operation.enum_type.receive__, false, false, port_name);
		return TitanAlt_Status.ALT_NO;
	}

	//originally any_receive
	public static TitanAlt_Status any_receive(final TitanComponent_template sender_template, final TitanComponent sender_pointer) {
		if (PORTS.isEmpty()) {
			TtcnLogger.log_matching_problem(TitanLoggerApi.MatchingProblemType_reason.enum_type.component__has__no__ports, TitanLoggerApi.MatchingProblemType_operation.enum_type.receive__, true, false, null);
			return TitanAlt_Status.ALT_NO;
		}

		TitanAlt_Status returnValue = TitanAlt_Status.ALT_NO;
		for (final TitanPort port : PORTS) {
			switch(port.receive(sender_template, sender_pointer, null)) {
			case ALT_YES:
				return TitanAlt_Status.ALT_YES;
			case ALT_MAYBE:
				returnValue = TitanAlt_Status.ALT_MAYBE;
			case ALT_NO:
				break;
			default:
				throw new TtcnError(MessageFormat.format("Internal error: Receive operation returned unexpected status code on port {0} while evaluating `any port.receive'.", port.port_name));
			}
		}

		return returnValue;
	}

	public TitanAlt_Status check_receive(final TitanComponent_template sender_template, final TitanComponent sender_pointer, final Index_Redirect index_redirect) {
		TtcnLogger.log_matching_problem(TitanLoggerApi.MatchingProblemType_reason.enum_type.no__incoming__types, TitanLoggerApi.MatchingProblemType_operation.enum_type.receive__, false, true, port_name);
		return TitanAlt_Status.ALT_NO;
	}

	//originally any_receive
	public static TitanAlt_Status any_check_receive(final TitanComponent_template sender_template, final TitanComponent sender_pointer) {
		if (PORTS.isEmpty()) {
			TtcnLogger.log_matching_problem(TitanLoggerApi.MatchingProblemType_reason.enum_type.component__has__no__ports, TitanLoggerApi.MatchingProblemType_operation.enum_type.receive__, true, true, null);
			return TitanAlt_Status.ALT_NO;
		}

		TitanAlt_Status returnValue = TitanAlt_Status.ALT_NO;
		for (final TitanPort port : PORTS) {
			switch (port.check_receive(sender_template, sender_pointer, null)) {
			case ALT_YES:
				return TitanAlt_Status.ALT_YES;
			case ALT_MAYBE:
				returnValue = TitanAlt_Status.ALT_MAYBE;
			case ALT_NO:
				break;
			default:
				throw new TtcnError(MessageFormat.format("Internal error: Check-receive operation returned unexpected status code on port {0} while evaluating `any port.check(receive)'.", port.port_name));
			}
		}

		return returnValue;
	}

	public TitanAlt_Status trigger(final TitanComponent_template sender_template, final TitanComponent sender_pointer, final Index_Redirect index_redirect) {
		TtcnLogger.log_matching_problem(TitanLoggerApi.MatchingProblemType_reason.enum_type.no__incoming__types, TitanLoggerApi.MatchingProblemType_operation.enum_type.trigger__, false, false, port_name);

		return TitanAlt_Status.ALT_NO;
	}

	//originally any_receive
	public static TitanAlt_Status any_trigger(final TitanComponent_template sender_template, final TitanComponent sender_pointer) {
		if (PORTS.isEmpty()) {
			TtcnLogger.log_matching_problem(TitanLoggerApi.MatchingProblemType_reason.enum_type.component__has__no__ports, TitanLoggerApi.MatchingProblemType_operation.enum_type.trigger__, true, false, null);
			return TitanAlt_Status.ALT_NO;
		}

		TitanAlt_Status returnValue = TitanAlt_Status.ALT_NO;
		for (final TitanPort port : PORTS) {
			switch (port.trigger(sender_template, sender_pointer, null)) {
			case ALT_YES:
				return TitanAlt_Status.ALT_YES;
			case ALT_MAYBE:
				returnValue = TitanAlt_Status.ALT_MAYBE;
			case ALT_NO:
				break;
			default:
				throw new TtcnError(MessageFormat.format("Internal error: Trigger operation returned unexpected status code on port {0} while evaluating `any port.trigger'.", port.port_name));
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
			TtcnLogger.log_matching_problem(TitanLoggerApi.MatchingProblemType_reason.enum_type.component__has__no__ports, TitanLoggerApi.MatchingProblemType_operation.enum_type.getcall__, true, false, null);
			return TitanAlt_Status.ALT_NO;
		}

		TitanAlt_Status returnValue = TitanAlt_Status.ALT_NO;
		for (final TitanPort port : PORTS) {
			switch (port.getcall(sender_template, sender_pointer, null)) {
			case ALT_YES:
				return TitanAlt_Status.ALT_YES;
			case ALT_MAYBE:
				returnValue = TitanAlt_Status.ALT_MAYBE;
			case ALT_NO:
				break;
			default:
				throw new TtcnError(MessageFormat.format("Internal error: Getcall operation returned unexpected status code on port {0} while evaluating `any port.getcall'.", port.port_name));
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
			TtcnLogger.log_matching_problem(TitanLoggerApi.MatchingProblemType_reason.enum_type.component__has__no__ports, TitanLoggerApi.MatchingProblemType_operation.enum_type.getcall__, true, true, null);
			return TitanAlt_Status.ALT_NO;
		}

		TitanAlt_Status returnValue = TitanAlt_Status.ALT_NO;
		for (final TitanPort port : PORTS) {
			switch (port.check_getcall(sender_template, sender_pointer, null)) {
			case ALT_YES:
				return TitanAlt_Status.ALT_YES;
			case ALT_MAYBE:
				returnValue = TitanAlt_Status.ALT_MAYBE;
			case ALT_NO:
				break;
			default:
				throw new TtcnError(MessageFormat.format("Internal error: Check-getcall operation returned unexpected status code on port {0} while evaluating `any port.check(getcall)'.", port.port_name));
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
			TtcnLogger.log_matching_problem(TitanLoggerApi.MatchingProblemType_reason.enum_type.component__has__no__ports, TitanLoggerApi.MatchingProblemType_operation.enum_type.getreply__, true, false, null);
			return TitanAlt_Status.ALT_NO;
		}

		TitanAlt_Status returnValue = TitanAlt_Status.ALT_NO;
		for (final TitanPort port : PORTS) {
			switch (port.getreply(sender_template, sender_pointer, null)) {
			case ALT_YES:
				return TitanAlt_Status.ALT_YES;
			case ALT_MAYBE:
				returnValue = TitanAlt_Status.ALT_MAYBE;
			case ALT_NO:
				break;
			default:
				throw new TtcnError(MessageFormat.format("Internal error: Getreply operation returned unexpected status code on port {0} while evaluating `any port.getreply'.", port.port_name));
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
			TtcnLogger.log_matching_problem(TitanLoggerApi.MatchingProblemType_reason.enum_type.component__has__no__ports, TitanLoggerApi.MatchingProblemType_operation.enum_type.getreply__, true, true, null);
			return TitanAlt_Status.ALT_NO;
		}

		TitanAlt_Status returnValue = TitanAlt_Status.ALT_NO;
		for (final TitanPort port : PORTS) {
			switch (port.check_getreply(sender_template, sender_pointer, null)) {
			case ALT_YES:
				return TitanAlt_Status.ALT_YES;
			case ALT_MAYBE:
				returnValue = TitanAlt_Status.ALT_MAYBE;
			case ALT_NO:
				break;
			default:
				throw new TtcnError(MessageFormat.format("Internal error: Check-getreply operation returned unexpected status code on port {0} while evaluating `any port.check(getreply)'.", port.port_name));
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
			TtcnLogger.log_matching_problem(TitanLoggerApi.MatchingProblemType_reason.enum_type.component__has__no__ports, TitanLoggerApi.MatchingProblemType_operation.enum_type.catch__, true, false, null);
			return TitanAlt_Status.ALT_NO;
		}

		TitanAlt_Status returnValue = TitanAlt_Status.ALT_NO;
		for (final TitanPort port : PORTS) {
			switch (port.get_exception(sender_template, sender_pointer, null)) {
			case ALT_YES:
				return TitanAlt_Status.ALT_YES;
			case ALT_MAYBE:
				returnValue = TitanAlt_Status.ALT_MAYBE;
			case ALT_NO:
				break;
			default:
				throw new TtcnError(MessageFormat.format("Internal error: Catch operation returned unexpected status code on port {0} while evaluating `any port.catch'.", port.port_name));
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
			TtcnLogger.log_matching_problem(TitanLoggerApi.MatchingProblemType_reason.enum_type.component__has__no__ports, TitanLoggerApi.MatchingProblemType_operation.enum_type.catch__, true, true, null);
			return TitanAlt_Status.ALT_NO;
		}

		TitanAlt_Status returnValue = TitanAlt_Status.ALT_NO;
		for (final TitanPort port : PORTS) {
			switch (port.check_getreply(sender_template, sender_pointer, null)) {
			case ALT_YES:
				return TitanAlt_Status.ALT_YES;
			case ALT_MAYBE:
				returnValue = TitanAlt_Status.ALT_MAYBE;
			case ALT_NO:
				break;
			default:
				throw new TtcnError(MessageFormat.format("Internal error: Check-catch operation returned unexpected status code on port {0} while evaluating `any port.check(catch)'.", port.port_name));
			}
		}

		return returnValue;
	}

	public TitanAlt_Status check(final TitanComponent_template sender_template, final TitanComponent sender_pointer, final Index_Redirect index_redirect) {
		TitanAlt_Status returnValue = TitanAlt_Status.ALT_NO;
		// the procedure-based queue must have the higher priority
		switch (check_getcall(sender_template, sender_pointer, null)) {
		case ALT_YES:
			return TitanAlt_Status.ALT_YES;
		case ALT_MAYBE:
			returnValue = TitanAlt_Status.ALT_MAYBE;
		case ALT_NO:
			break;
		default:
			throw new TtcnError(MessageFormat.format("Internal error: Check-getcall operation returned unexpected status code on port {0}.", port_name));
		}
		if (!TitanAlt_Status.ALT_MAYBE.equals(returnValue)) {
			// don't try getreply if the procedure-based queue is empty
			// (i.e. check_getcall() returned ALT_MAYBE)
			switch (check_getreply(sender_template, sender_pointer, null)) {
			case ALT_YES:
				return TitanAlt_Status.ALT_YES;
			case ALT_MAYBE:
				returnValue = TitanAlt_Status.ALT_MAYBE;
			case ALT_NO:
				break;
			default:
				throw new TtcnError(MessageFormat.format("Internal error: Check-getreply operation returned unexpected status code on port {0}.", port_name));
			}
		}
		if (!TitanAlt_Status.ALT_MAYBE.equals(returnValue)) {
			// don't try catch if the procedure-based queue is empty
			// (i.e. check_getcall() or check_getreply() returned ALT_MAYBE)
			switch (check_catch(sender_template, sender_pointer, null)) {
			case ALT_YES:
				return TitanAlt_Status.ALT_YES;
			case ALT_MAYBE:
				returnValue = TitanAlt_Status.ALT_MAYBE;
			case ALT_NO:
				break;
			default:
				throw new TtcnError(MessageFormat.format("Internal error: Check-catch operation returned unexpected status code on port {0}.", port_name));
			}
		}
		switch (check_receive(sender_template, sender_pointer, null)) {
		case ALT_YES:
			return TitanAlt_Status.ALT_YES;
		case ALT_MAYBE:
			returnValue = TitanAlt_Status.ALT_MAYBE;
		case ALT_NO:
			break;
		default:
			throw new TtcnError(MessageFormat.format("Internal error: Check-receive operation returned unexpected status code on port {0}.", port_name));
		}

		return returnValue;
	}

	//originally any_check
	public static TitanAlt_Status any_check(final TitanComponent_template sender_template, final TitanComponent sender_pointer) {
		if (PORTS.isEmpty()) {
			TtcnLogger.log_matching_problem(TitanLoggerApi.MatchingProblemType_reason.enum_type.component__has__no__ports, TitanLoggerApi.MatchingProblemType_operation.enum_type.check__, true, false, null);
			return TitanAlt_Status.ALT_NO;
		}

		TitanAlt_Status returnValue = TitanAlt_Status.ALT_NO;
		for (final TitanPort port : PORTS) {
			switch (port.check(sender_template, sender_pointer, null)) {
			case ALT_YES:
				return TitanAlt_Status.ALT_YES;
			case ALT_MAYBE:
				returnValue = TitanAlt_Status.ALT_MAYBE;
			case ALT_NO:
				break;
			default:
				throw new TtcnError(MessageFormat.format("Internal error: Check operation returned unexpected status code on port {0} while evaluating `any port.check'.", port.port_name));
			}
		}

		return returnValue;
	}

	protected void Install_Handler(final Set<SelectableChannel> read_channels, final Set<SelectableChannel> write_channels, final double call_interval) throws IOException {
		if (!is_active) {
			throw new TtcnError(MessageFormat.format("Event handler cannot be installed for inactive port {0}.", port_name));
		}

		//FIXME register handler
		if (read_channels != null) {
			for (final SelectableChannel channel : read_channels) {
				channel.configureBlocking(false);
				TTCN_Snapshot.channelMap.get().put(channel, this);
				channel.register(TTCN_Snapshot.selector.get(), SelectionKey.OP_READ);
			}
		}
	}

	protected void Uninstall_Handler() throws IOException {
		final ArrayList<SelectableChannel> tobeRemoved = new ArrayList<SelectableChannel>();
		for (final Map.Entry<SelectableChannel, Channel_And_Timeout_Event_Handler> entry: TTCN_Snapshot.channelMap.get().entrySet()) {
			if (entry.getValue() == this) {
				tobeRemoved.add(entry.getKey());
			}
		}

		for (final SelectableChannel channel : tobeRemoved) {
			channel.close();
			TTCN_Snapshot.channelMap.get().remove(channel);
		}
	}

	@Override
	public void Handle_Event(final SelectableChannel channel, final boolean is_readable, final boolean is_writeable) {
		//FIXME implement default
	}

	@Override
	public void Handle_Timeout(final double time_since_last_call) {
		//FIXME implement default
	}

	protected void user_map(final String system_port) {
		//default implementation is empty
	}

	protected void user_unmap(final String system_port) {
		//default implementation is empty
	}

	protected void user_start(){
		//default implementation is empty
	}

	protected void user_stop() {
		//default implementation is empty
	}

	protected void clear_queue() {
		//default implementation is empty
	}

	//originally get_default_destination
	protected int get_default_destination() {
		//FIXME implement connection checks
		if (system_mappings.size() > 1) {
			throw new TtcnError(MessageFormat.format("Port {0} has more than one mappings. Message cannot be sent on it to system.", port_name));
		} else if (system_mappings.isEmpty()) {
			throw new TtcnError(MessageFormat.format("Port {0} has neither connections nor mappings. Message cannot be sent on it.", port_name));
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
		throw new TtcnError(MessageFormat.format("Sending messages on port {0}, is not yet supported.", get_name()));
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
	private final void map(final String system_port, final boolean translation) {
		if (!is_active) {
			throw new TtcnError(MessageFormat.format("Inactive port {0} cannot be mapped.", port_name));
		}

		for (int i = 0; i < system_mappings.size(); i++) {
			if (system_port.equals(system_mappings.get(i))) {
				TtcnError.TtcnWarning(MessageFormat.format("Port {0} is already mapped to system:{1}.\n Map operation was ignored.",
						port_name, system_port));
				return;
			}
		}

		user_map(system_port);

		// the mapping shall be registered in the table only if user_map() was successful
		system_mappings.add(system_port);
		if (system_mappings.size() > 1) {
			TtcnError.TtcnWarning(MessageFormat.format("Port {0} has now more than one mappings."
					+ " Message cannot be sent on it to system even with explicit addressing.", port_name));
		}
	}

	// FIXME handle translation ports
	private final void unmap(final String system_port, final boolean translation) {
		int deletion_position;
		for (deletion_position = 0; deletion_position < system_mappings.size(); deletion_position++) {
			if (system_port.equals(system_mappings.get(deletion_position))) {
				break;
			}
		}

		if (deletion_position >= system_mappings.size()) {
			TtcnError.TtcnWarning(MessageFormat.format("Port {0} is not mapped to system:{1}. " + "Unmap operation was ignored.",
					port_name, system_port));
			return;
		}

		system_mappings.remove(deletion_position);

		user_unmap(system_port);

		// FIXME implement
	}

	public static void process_connect_listen(final String local_port, final int remote_component, final String remote_port, final int transport_type) {
		final TitanPort port = lookup_by_name(local_port, false);
		if (port == null) {
			//FIXME implement
			return;
		} else if (!port.is_active) {
			throw new TtcnError(MessageFormat.format("Internal error: Port {0} is inactive when trying to connect it to {1}:{2}.", local_port, remote_component, remote_port));
		}
		//FIXME implement the additional checks
		//FIXME implement additional connection types
		throw new TtcnError("connecting ports is not yet supported !");
	}

	public static void process_connect(final String local_port, final int remote_component, final String remote_port, final int transport_type, final Text_Buf text_buf) {
		final TitanPort port = lookup_by_name(local_port, false);
		if (port == null) {
			//FIXME implement
			return;
		} else if (!port.is_active) {
			throw new TtcnError(MessageFormat.format("Internal error: Port {0} is inactive when trying to connect it to {1}:{2}.", local_port, remote_component, remote_port));
		}
		//FIXME implement the additional checks
		//FIXME implement additional connection types
		throw new TtcnError("connecting ports is not yet supported !");
	}

	public static void map_port(final String component_port, final String system_port, final boolean translation) {
		final String port_name = translation ? system_port : component_port;
		final TitanPort port = lookup_by_name(port_name, translation);
		if (port == null) {
			throw new TtcnError(MessageFormat.format("Map operation refers to non-existent port {0}.", component_port));
		}
		//FIXME this is actually more complex
		if (translation) {
			port.map(component_port, translation);
		} else {
			port.map(system_port, translation);
		}
		if (!TTCN_Runtime.is_single()) {
			//FIXME add send_mapped
		}
	}

	public static void unmap_port(final String component_port, final String system_port, final boolean translation) {
		final String port_name = translation ? system_port : component_port;
		final TitanPort port = lookup_by_name(port_name, translation);
		if (port == null) {
			throw new TtcnError(MessageFormat.format("Unmap operation refers to non-existent port {0}.", component_port));
		}
		//FIXME this is actually more complex
		if (translation) {
			port.unmap(component_port, translation);
		} else {
			port.unmap(system_port, translation);
		}
		if (!TTCN_Runtime.is_single()) {
			//FIXME add send_unmapped
		}
	}

	public void setName(final String name) {
		if (name == null) {
			throw new TtcnError("Internal error: Setting an invalid name for a single element of a port array.");
		}
		port_name = name;
	}

	public void log() {
		TtcnLogger.log_event("port %s", port_name);
	}
}

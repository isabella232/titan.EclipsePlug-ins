/******************************************************************************
 * Copyright (c) 2000-2018 Ericsson Telecom AB
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/org/documents/epl-2.0/EPL-2.0.html
 ******************************************************************************/
package org.eclipse.titan.runtime.core;

import java.io.IOException;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.nio.ByteBuffer;
import java.nio.channels.SelectableChannel;
import java.nio.channels.SelectionKey;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

import org.eclipse.titan.runtime.core.Event_Handler.Channel_And_Timeout_Event_Handler;
import org.eclipse.titan.runtime.core.TTCN_Communication.transport_type_enum;

/**
 * The base class of test ports
 *
 * TODO: lots to implement
 *
 * @author Kristof Szabados
 */
public class TitanPort extends Channel_And_Timeout_Event_Handler {
	// originally the list stored in list_head list_tail
	private static final ThreadLocal<LinkedList<TitanPort>> PORTS = new ThreadLocal<LinkedList<TitanPort>>() {
		@Override
		protected LinkedList<TitanPort> initialValue() {
			return new LinkedList<TitanPort>();
		}
	};
	// originally the list stored in system_list_head and system_list_tail
	private static final ThreadLocal<LinkedList<TitanPort>> SYSTEM_PORTS = new ThreadLocal<LinkedList<TitanPort>>() {
		@Override
		protected LinkedList<TitanPort> initialValue() {
			return new LinkedList<TitanPort>();
		}
	};

	//FIXME implement the remaining features
	private static final class port_connection extends Channel_And_Timeout_Event_Handler {
		static enum connection_data_type_enum {CONN_DATA_LAST, CONN_DATA_MESSAGE, CONN_DATA_CALL, CONN_DATA_REPLY, CONN_DATA_EXCEPTION};
		static enum connection_state_enum {CONN_IDLE, CONN_LISTENING, CONN_CONNECTED, CONN_LAST_MSG_SENT, CONN_LAST_MSG_RCVD};

		private TitanPort owner_port;
		connection_state_enum connection_state;
		int remote_component;
		String remote_port;
		transport_type_enum transport_type;
		//only in case of local connection
		TitanPort local_port;
		//only in case inet connection
		SelectableChannel stream_socket;
		//only in case inet connection
		Text_Buf stream_incoming_buf;
		TitanOctetString sliding_buffer;

		@Override
		public void Handle_Event(final SelectableChannel channel, final boolean is_readable, final boolean is_writeable) {
			// FIXME for now only handle inet streams
			if (is_readable) {
				if (connection_state == connection_state_enum.CONN_LISTENING) {
					owner_port.handle_incoming_connection(this);
				} else if (is_readable) {
					owner_port.handle_incoming_data(this);
				}
			}
		}

		@Override
		public void Handle_Timeout(final double time_since_last_call) {
			// TODO Auto-generated method stub
		}
	}

	protected String port_name;
	protected int msg_head_count;
	protected int proc_head_count;
	//temporary variable
	protected int proc_tail_count;
	protected boolean is_active;
	protected boolean is_started;
	protected boolean is_halted;

	private ArrayList<String> system_mappings = new ArrayList<String>();
	private LinkedList<port_connection> connection_list = new LinkedList<TitanPort.port_connection>();

	public TitanPort(final String portName) {
		this.port_name = portName == null ? "<unknown>" : portName;
		is_active = false;
		is_started = false;
	}

	protected TitanPort() {
		port_name = "<unknown>";
		is_active = false;
		is_started = false;
		is_halted = false;
	}

	public String get_name() {
		return port_name;
	}

	//originally PORT::add_to_list
	private void add_to_list(final boolean system) {
		if (system) {
			for (final TitanPort port : SYSTEM_PORTS.get()) {
				if (port == this) {
					return;
				}
				if (port.port_name.equals(port_name)) {
					throw new TtcnError(MessageFormat.format("Internal error: There are more than one ports with name {0}.", port_name));
				}
			}

			SYSTEM_PORTS.get().add(this);
		} else {
			for (final TitanPort port : PORTS.get()) {
				if (port == this) {
					return;
				}
				if (port.port_name.equals(port_name)) {
					throw new TtcnError(MessageFormat.format("Internal error: There are more than one ports with name {0}.", port_name));
				}
			}

			PORTS.get().add(this);
		}
	}

	//originally PORT::remove_from_list
	private void remove_from_list(final boolean system) {
		if (system) {
			SYSTEM_PORTS.get().remove(this);
		} else {
			PORTS.get().remove(this);
		}
	}

	//originally PORT::lookup_by_name
	private static TitanPort lookup_by_name(final String parameter_port_name, final boolean system) {
		if (system) {
			for (final TitanPort port : SYSTEM_PORTS.get()) {
				if (port.port_name.equals(parameter_port_name)) {
					return port;
				}
			}
		} else {
			for (final TitanPort port : PORTS.get()) {
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

			if (system_mappings.size() == 0) {
				init_port_variables();
			}
		}
	}

	//originally PORT::deactivate_port
	public void deactivate_port(final boolean system) {
		if (is_active) {
			/* In order to proceed with the deactivation we must ignore the
			 * following errors:
			 * - errors in user code of Test Port (i.e. user_stop, user_unmap)
			 * - failures when sending messages to MC (the link may be down)
			 */
			final boolean is_parallel = !TTCN_Runtime.is_single();
			// terminate all connections
			while (!connection_list.isEmpty()) {
				final port_connection connection = connection_list.getFirst();
				TtcnLogger.log_port_misc(TitanLoggerApi.Port__Misc_reason.enum_type.removing__unterminated__connection, port_name, connection.remote_component, connection.remote_port, null, -1, 0);
				if (is_parallel) {
					try {
						TTCN_Communication.send_disconnected(port_name, connection.remote_component, connection.remote_port);
					} catch (final TtcnError e) {
						//intentionally empty
					}
				}
				remove_connection(connection);
			}
	
			// terminate all mappings
			while (!system_mappings.isEmpty()) {
				final String system_port = system_mappings.get(0);
				TtcnLogger.log_port_misc(TitanLoggerApi.Port__Misc_reason.enum_type.removing__unterminated__mapping, port_name, TitanComponent.NULL_COMPREF, system_port, null, -1, 0);
				try {
					unmap(system_port, system);
				} catch (final TtcnError e) {
					//intentionally empty
				}

				if (is_parallel) {
					try {
						TTCN_Communication.send_unmapped(port_name, system_port, system);
					} catch (final TtcnError e) {
						//intentionally empty
					}
				}
			}

			// the previous disconnect/unmap operations may generate incoming events
			// so we should stop and clear the queue after them
			if (is_started || is_halted) {
				try {
					stop();
				} catch (final TtcnError e) {
					//intentionally empty
				}
			}

			clear_queue();

			// deactivate all event handlers
			//FIXME implement
			remove_from_list(system);
			is_active = false;
		}
	}

	// originally PORT::deactivate_all
	public static void deactivate_all() {
		final LinkedList<TitanPort> temp = new LinkedList<TitanPort>(PORTS.get());
		for (final TitanPort port : temp) {
			port.deactivate_port(false);
		}
		temp.clear();
		temp.addAll(SYSTEM_PORTS.get());
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
		TtcnLogger.log_port_misc(TitanLoggerApi.Port__Misc_reason.enum_type.port__was__cleared, port_name, 0, "", "", -1, 0);
	}

	public static void all_clear() {
		for (final TitanPort port : PORTS.get()) {
			port.clear();
		}
		for (final TitanPort port : SYSTEM_PORTS.get()) {
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
		for (final TitanPort port : PORTS.get()) {
			port.start();
		}
		for (final TitanPort port : SYSTEM_PORTS.get()) {
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
		for (final TitanPort port : PORTS.get()) {
			port.stop();
		}
		for (final TitanPort port : SYSTEM_PORTS.get()) {
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
		for (final TitanPort port : PORTS.get()) {
			port.halt();
		}
		for (final TitanPort port : SYSTEM_PORTS.get()) {
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
			return !connection_list.isEmpty();
		} else if ("Mapped".equals(type)) {
			return !system_mappings.isEmpty();
		} else if ("Linked".equals(type)) {
			return !connection_list.isEmpty() || !system_mappings.isEmpty();
		}
		throw new TtcnError(MessageFormat.format("{0} is not an allowed parameter of checkstate().", type));
	}

	//originally check_port_state
	public boolean check_port_state(final TitanCharString type) {
		return check_port_state(type.getValue().toString());
	}

	// originally any_check_port_state
	public static boolean any_check_port_state(final String type) {
		for (final TitanPort port : PORTS.get()) {
			if (port.check_port_state(type)) {
				return true;
			}
		}
		for (final TitanPort port : SYSTEM_PORTS.get()) {
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
		for (final TitanPort port : PORTS.get()) {
			if (!port.check_port_state(type)) {
				return false;
			}
		}
		for (final TitanPort port : SYSTEM_PORTS.get()) {
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
		if (PORTS.get().isEmpty()) {
			TtcnLogger.log_matching_problem(TitanLoggerApi.MatchingProblemType_reason.enum_type.component__has__no__ports, TitanLoggerApi.MatchingProblemType_operation.enum_type.receive__, true, false, null);
			return TitanAlt_Status.ALT_NO;
		}

		TitanAlt_Status returnValue = TitanAlt_Status.ALT_NO;
		for (final TitanPort port : PORTS.get()) {
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
		if (PORTS.get().isEmpty()) {
			TtcnLogger.log_matching_problem(TitanLoggerApi.MatchingProblemType_reason.enum_type.component__has__no__ports, TitanLoggerApi.MatchingProblemType_operation.enum_type.receive__, true, true, null);
			return TitanAlt_Status.ALT_NO;
		}

		TitanAlt_Status returnValue = TitanAlt_Status.ALT_NO;
		for (final TitanPort port : PORTS.get()) {
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
		if (PORTS.get().isEmpty()) {
			TtcnLogger.log_matching_problem(TitanLoggerApi.MatchingProblemType_reason.enum_type.component__has__no__ports, TitanLoggerApi.MatchingProblemType_operation.enum_type.trigger__, true, false, null);
			return TitanAlt_Status.ALT_NO;
		}

		TitanAlt_Status returnValue = TitanAlt_Status.ALT_NO;
		for (final TitanPort port : PORTS.get()) {
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
		if (PORTS.get().isEmpty()) {
			TtcnLogger.log_matching_problem(TitanLoggerApi.MatchingProblemType_reason.enum_type.component__has__no__ports, TitanLoggerApi.MatchingProblemType_operation.enum_type.getcall__, true, false, null);
			return TitanAlt_Status.ALT_NO;
		}

		TitanAlt_Status returnValue = TitanAlt_Status.ALT_NO;
		for (final TitanPort port : PORTS.get()) {
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
		if (PORTS.get().isEmpty()) {
			TtcnLogger.log_matching_problem(TitanLoggerApi.MatchingProblemType_reason.enum_type.component__has__no__ports, TitanLoggerApi.MatchingProblemType_operation.enum_type.getcall__, true, true, null);
			return TitanAlt_Status.ALT_NO;
		}

		TitanAlt_Status returnValue = TitanAlt_Status.ALT_NO;
		for (final TitanPort port : PORTS.get()) {
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
		if (PORTS.get().isEmpty()) {
			TtcnLogger.log_matching_problem(TitanLoggerApi.MatchingProblemType_reason.enum_type.component__has__no__ports, TitanLoggerApi.MatchingProblemType_operation.enum_type.getreply__, true, false, null);
			return TitanAlt_Status.ALT_NO;
		}

		TitanAlt_Status returnValue = TitanAlt_Status.ALT_NO;
		for (final TitanPort port : PORTS.get()) {
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
		if (PORTS.get().isEmpty()) {
			TtcnLogger.log_matching_problem(TitanLoggerApi.MatchingProblemType_reason.enum_type.component__has__no__ports, TitanLoggerApi.MatchingProblemType_operation.enum_type.getreply__, true, true, null);
			return TitanAlt_Status.ALT_NO;
		}

		TitanAlt_Status returnValue = TitanAlt_Status.ALT_NO;
		for (final TitanPort port : PORTS.get()) {
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
		if (PORTS.get().isEmpty()) {
			TtcnLogger.log_matching_problem(TitanLoggerApi.MatchingProblemType_reason.enum_type.component__has__no__ports, TitanLoggerApi.MatchingProblemType_operation.enum_type.catch__, true, false, null);
			return TitanAlt_Status.ALT_NO;
		}

		TitanAlt_Status returnValue = TitanAlt_Status.ALT_NO;
		for (final TitanPort port : PORTS.get()) {
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
		if (PORTS.get().isEmpty()) {
			TtcnLogger.log_matching_problem(TitanLoggerApi.MatchingProblemType_reason.enum_type.component__has__no__ports, TitanLoggerApi.MatchingProblemType_operation.enum_type.catch__, true, true, null);
			return TitanAlt_Status.ALT_NO;
		}

		TitanAlt_Status returnValue = TitanAlt_Status.ALT_NO;
		for (final TitanPort port : PORTS.get()) {
			switch (port.check_catch(sender_template, sender_pointer, null)) {
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
		if (PORTS.get().isEmpty()) {
			TtcnLogger.log_matching_problem(TitanLoggerApi.MatchingProblemType_reason.enum_type.component__has__no__ports, TitanLoggerApi.MatchingProblemType_operation.enum_type.check__, true, false, null);
			return TitanAlt_Status.ALT_NO;
		}

		TitanAlt_Status returnValue = TitanAlt_Status.ALT_NO;
		for (final TitanPort port : PORTS.get()) {
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
		if (connection_list.isEmpty()) {
			if (system_mappings.size() > 1) {
				throw new TtcnError(MessageFormat.format("Port {0} has more than one mappings. Message cannot be sent on it to system.", port_name));
			} else if (system_mappings.isEmpty()) {
				throw new TtcnError(MessageFormat.format("Port {0} has neither connections nor mappings. Message cannot be sent on it.", port_name));
			}
		} else {
			if (system_mappings.size() > 0) {
				throw new TtcnError(MessageFormat.format("Port {0} has both connection(s) and mapping(s). Message can be sent on it only with explicit addressing.", port_name));
			} else if (connection_list.size() > 1) {
				throw new TtcnError(MessageFormat.format("Port {0} has more than one active connections. Message can be sent on it only with explicit addressing.", port_name));
			}

			return connection_list.getFirst().remote_component;
		}

		return TitanComponent.SYSTEM_COMPREF;
	}

	protected void prepare_message(final Text_Buf outgoing_buf, final String message_type) {
		outgoing_buf.push_int(port_connection.connection_data_type_enum.CONN_DATA_MESSAGE.ordinal());
		outgoing_buf.push_string(message_type);
	}

	protected void prepare_call(final Text_Buf outgoing_buf, final String signature_name) {
		outgoing_buf.push_int(port_connection.connection_data_type_enum.CONN_DATA_CALL.ordinal());
		outgoing_buf.push_string(signature_name);
	}

	protected void prepare_reply(final Text_Buf outgoing_buf, final String signature_name) {
		outgoing_buf.push_int(port_connection.connection_data_type_enum.CONN_DATA_REPLY.ordinal());
		outgoing_buf.push_string(signature_name);
	}

	protected void prepare_exception(final Text_Buf outgoing_buf, final String signature_name) {
		outgoing_buf.push_int(port_connection.connection_data_type_enum.CONN_DATA_EXCEPTION.ordinal());
		outgoing_buf.push_string(signature_name);
	}

	protected void send_data(final Text_Buf outgoing_buf, final TitanComponent destination_component) {
		if (!destination_component.isBound()) {
			throw new TtcnError(MessageFormat.format("Internal error: The destination component reference is unbound when sending data on port {0}.", port_name));
		}

		final int destination_compref = destination_component.componentValue;
		final AtomicBoolean is_unique = new AtomicBoolean();
		final port_connection connection = lookup_connection_to_compref(destination_compref, is_unique);
		if (connection == null) {
			throw new TtcnError(MessageFormat.format("Data cannot be sent on port {0} to component {1} because there is no connection towards component {1}.", port_name, destination_compref));
		} else if (!is_unique.get()) {
			throw new TtcnError(MessageFormat.format("Data cannot be sent on port {0} to component {1} because there are more than one connections towards component {1}.", port_name, destination_compref));
		} else if (connection.connection_state != port_connection.connection_state_enum.CONN_CONNECTED) {
			throw new TtcnError(MessageFormat.format("Data cannot be sent on port {0} to component {1} because the connection is not in active state.", port_name, destination_compref));
		}

		switch (connection.transport_type) {
		case TRANSPORT_LOCAL:
			send_data_local(connection, outgoing_buf);
			break;
		case TRANSPORT_INET_STREAM:
			send_data_stream(connection, outgoing_buf, false);
			break;
		default:
			throw new TtcnError(MessageFormat.format("Internal error: Invalid transport type ({0}) in port connection between {1} and {2}:{3}.", connection.transport_type, port_name, connection.remote_component, connection.remote_port));
		}
	}

	protected void process_data(final port_connection connection, final Text_Buf incoming_buf) {
		final int connection_int = incoming_buf.pull_int().getInt();
		final port_connection.connection_data_type_enum conn_data_type = port_connection.connection_data_type_enum.values()[connection_int];

		if (conn_data_type != port_connection.connection_data_type_enum.CONN_DATA_LAST) {
			switch (connection.connection_state) {
			case CONN_CONNECTED:
			case CONN_LAST_MSG_SENT:
				break;
			case CONN_LAST_MSG_RCVD:
			case CONN_IDLE:
				TtcnError.TtcnWarning(MessageFormat.format("Data arrived after the indication of connection termination on port {0} from {1}:{2}. Data is ignored.", port_name, connection.remote_component, connection.remote_port));
				return;
			default:
				throw new TtcnError(MessageFormat.format("Internal error: Connection of port {0} with {1}:{2} has invalid state ({3}).", port_name, connection.remote_component, connection.remote_port, connection.connection_state.ordinal()));
			}

			final String message_type = incoming_buf.pull_string();
			switch (conn_data_type) {
			case CONN_DATA_MESSAGE:
				if (!process_message(message_type, incoming_buf, connection.remote_component, connection.sliding_buffer)) {
					throw new TtcnError(MessageFormat.format("Port {0} does not support incoming message type {1}, which has arrived on the connection from {2}:{3}.", port_name, message_type, connection.remote_component, connection.remote_port));
				}
				break;
			case CONN_DATA_CALL:
				if (!process_call(message_type, incoming_buf, connection.remote_component)) {
					throw new TtcnError(MessageFormat.format("Port {0} does not support incoming call of signature {1}, which has arrived on the connection from {2}:{3}.", port_name, message_type, connection.remote_component, connection.remote_port));
				}
				break;
			case CONN_DATA_REPLY:
				if (!process_reply(message_type, incoming_buf, connection.remote_component)) {
					throw new TtcnError(MessageFormat.format("Port {0} does not support incoming reply of signature {1}, which has arrived on the connection from {2}:{3}.", port_name, message_type, connection.remote_component, connection.remote_port));
				}
				break;
			case CONN_DATA_EXCEPTION:
				if (!process_exception(message_type, incoming_buf, connection.remote_component)) {
					throw new TtcnError(MessageFormat.format("Port {0} does not support incoming exception of signature {1}, which has arrived on the connection from {2}:{3}.", port_name, message_type, connection.remote_component, connection.remote_port));
				}
				break;
			default:
				throw new TtcnError(MessageFormat.format("Internal error: Data with invalid selector ({0}) was received on port {1} from {2}:{3}.", conn_data_type.ordinal(), port_name, connection.remote_component, connection.remote_port));
			}
		} else {
			process_last_message(connection);
		}
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

	/**
	 * Resets the port type variables to null after unmap
	 * */
	protected void reset_port_variables() {
		// intentionally empty
	}

	/**
	 * Initializes the port variables after map
	 * */
	protected void init_port_variables() {
		// intentionally empty
	}

	private port_connection add_connection(final int remote_component, final String remote_port, final transport_type_enum transport_type) {
		int index = -1;
		int i = -1;
		for (final port_connection connection: connection_list) {
			i++;
			if (connection.remote_component == remote_component) {
				final int ret_val = connection.remote_port.compareTo(remote_port);
				if (ret_val == 0) {
					return connection;
				} else if (ret_val > 0) {
					index = i;
					break;
				}
			} else if (connection.remote_component > remote_component) {
				index = i;
				break;
			}
		}

		if (system_mappings.size() > 0) {
			throw new TtcnError(MessageFormat.format("Connect operation cannot be performed on a mapped port ({0}).", port_name));
		}

		final port_connection new_connection = new port_connection();
		new_connection.owner_port = this;
		new_connection.connection_state = port_connection.connection_state_enum.CONN_IDLE;
		new_connection.remote_component = remote_component;
		new_connection.remote_port = remote_port;
		new_connection.transport_type = transport_type;
		new_connection.local_port = null;
		new_connection.sliding_buffer = new TitanOctetString();
		new_connection.stream_socket = null;
		new_connection.stream_incoming_buf = null;

		if (index == -1) {
			// new_conn will be inserted to the end of the list
			connection_list.addLast(new_connection);
		} else {
			connection_list.add(index, new_connection);
		}

		return new_connection;
	}

	private void remove_connection(final port_connection connection) {
		switch (connection.transport_type) {
		case TRANSPORT_LOCAL:
			break;
		case TRANSPORT_INET_STREAM:
			TTCN_Snapshot.channelMap.get().remove(connection.stream_socket);
			try {
				connection.stream_socket.close();
			} catch (final IOException e) {
				throw new TtcnError(e);
			}

			connection.stream_socket = null;
			break;
		default:
			throw new TtcnError("Internal error: PORT::remove_connection(): invalid transport type.");
		}

		connection_list.remove(connection);
	}

	private port_connection lookup_connection_to_compref(final int remote_component, final AtomicBoolean is_unique) {
		port_connection result = null;
		for (final port_connection connection : connection_list) {
			if (connection.remote_component == remote_component) {
				if (is_unique != null) {
					if (result == null) {
						is_unique.set(true);
					} else {
						is_unique.set(false);

						return result;
					}
				}

				result = connection;
			} else if (connection.remote_component > remote_component) {
				break;
			}
		}

		return result;
	}

	private port_connection lookup_connection(final int remote_component, final String remote_port) {
		for (final port_connection connection : connection_list) {
			if (connection.remote_component == remote_component) {
				final int ret_val = connection.remote_port.compareTo(remote_port);
				if (ret_val == 0) {
					return connection;
				} else if (ret_val > 0) {
					break;
				}
			} else if (connection.remote_component > remote_component) {
				break;
			}
		}

		return null;
	}

	private void add_local_connection(final TitanPort other_endpoint) {
		final port_connection connection = add_connection(TitanComponent.self.get().componentValue, other_endpoint.port_name, transport_type_enum.TRANSPORT_LOCAL);
		connection.connection_state = port_connection.connection_state_enum.CONN_CONNECTED;
		connection.local_port = other_endpoint;

		TtcnLogger.log_port_misc(TitanLoggerApi.Port__Misc_reason.enum_type.local__connection__established, port_name, TitanComponent.NULL_COMPREF, other_endpoint.port_name, null, -1, 0);
	}

	private void remove_local_connection(final port_connection connection) {
		if(connection.transport_type != transport_type_enum.TRANSPORT_LOCAL) {
			throw new TtcnError(MessageFormat.format("Internal error: The transport type used by the connection between port {0} and {1}:{2} is not LOCAL.", port_name, connection.remote_component, connection.remote_port));
		}

		final TitanPort other_endpoint = connection.local_port;
		remove_connection(connection);
		TtcnLogger.log_port_misc(TitanLoggerApi.Port__Misc_reason.enum_type.local__connection__terminated, port_name, TitanComponent.NULL_COMPREF, other_endpoint.port_name, null, -1, 0);
	}

	private void connect_listen_inet_stream(final int remote_component, final String remote_port) {
		try {
			final ServerSocketChannel serverSocketChannel = ServerSocketChannel.open();
			final ServerSocket serverSocket =serverSocketChannel.socket();
			final InetSocketAddress local_addr = new InetSocketAddress(serverSocket.getInetAddress(), 0);
			serverSocket.bind(local_addr);
			final int local_port = serverSocketChannel.socket().getLocalPort();
			//FIXME implement rest
			final port_connection new_connection = add_connection(remote_component, remote_port, transport_type_enum.TRANSPORT_INET_STREAM);
			new_connection.connection_state = port_connection.connection_state_enum.CONN_LISTENING;
			new_connection.stream_socket = serverSocketChannel;

			serverSocketChannel.configureBlocking(false);
			TTCN_Snapshot.channelMap.get().put(serverSocketChannel, new_connection);
			serverSocketChannel.register(TTCN_Snapshot.selector.get(), SelectionKey.OP_ACCEPT);

			TTCN_Communication.send_connect_listen_ack_inet_stream(port_name, local_port, remote_component, remote_port, Inet4Address.getLocalHost());
			TtcnLogger.log_port_misc(TitanLoggerApi.Port__Misc_reason.enum_type.port__is__waiting__for__connection__tcp, port_name, remote_component, remote_port, "TCP", -1, 0);
		} catch (final IOException e) {
			throw new TtcnError(e);
		}
	}

	private void connect_local(final int remote_component, final String remote_port) {
		if (TitanComponent.self.get().componentValue != remote_component) {
			TTCN_Communication.send_connect_error(port_name, remote_component, remote_port, MessageFormat.format("Message CONNECT with transport type LOCAL refers to a port of another component ({0}).", remote_component));
			return;
		}

		final TitanPort remotePort = lookup_by_name(remote_port, false);
		if (remotePort == null) {
			TTCN_Communication.send_connect_error(port_name, remote_component, remote_port, MessageFormat.format("Port {0} does not exist.", remote_port));
			return;
		} else if (!remotePort.is_active) {
			throw new TtcnError(MessageFormat.format("Internal error: Port {0} is inactive when trying to connect it to local port {1}.", remote_port, port_name));
		}

		add_local_connection(remotePort);
		if (this != remotePort) {
			remotePort.add_local_connection(this);
		}
		TTCN_Communication.send_connected(port_name, remote_component, remote_port);
	}

	private void connect_stream(final int remote_component, final String remote_port, final transport_type_enum transport_type, final Text_Buf text_buf) {
		//FIXME implement properly (not even local address pulling is ok now)
		
		// family, port, addr, zero
		final byte family[] = new byte[2];
		text_buf.pull_raw(2, family);
		final byte port[] = new byte[2];
		text_buf.pull_raw(2, port);

		final byte addr[] = new byte[4];
		text_buf.pull_raw(4, addr);

		final byte zero[] = new byte[8];
		text_buf.pull_raw(8, zero);

		try {
			final InetAddress temp = Inet4Address.getByAddress(addr);
			int temp2 = (port[0]&0xFF) * 256;
			temp2 += (port[1]&0xFF);

			final InetSocketAddress address = new InetSocketAddress(temp, temp2);
			final SocketChannel socketChannel = SocketChannel.open();
			socketChannel.connect(address);

			final port_connection new_connection = add_connection(remote_component, remote_port, transport_type);
			new_connection.connection_state = port_connection.connection_state_enum.CONN_CONNECTED;
			new_connection.stream_socket = socketChannel;

			socketChannel.configureBlocking(false);
			TTCN_Snapshot.channelMap.get().put(socketChannel, new_connection);
			socketChannel.register(TTCN_Snapshot.selector.get(), SelectionKey.OP_READ);
		} catch (final IOException e) {
			throw new TtcnError(e);
		}

		TtcnLogger.log_port_misc(TitanLoggerApi.Port__Misc_reason.enum_type.connection__established, port_name, remote_component, remote_port, "TCP", -1, 0);
	}

	private void disconnect_local(final port_connection connection) {
		final TitanPort remotePort = connection.local_port;
		remove_local_connection(connection);
		if (this != remotePort) {
			final port_connection connection2 = remotePort.lookup_connection(TitanComponent.self.get().componentValue, port_name);
			if (connection2 == null) {
				throw new TtcnError(MessageFormat.format("Internal error: Port {0} is connected with local port {1}, but port {1} does not have a connection to {0}.", port_name, remotePort.port_name));
			} else {
				remotePort.remove_local_connection(connection2);
			}
		}

		TTCN_Communication.send_disconnected(port_name, TitanComponent.self.get().componentValue, remotePort.port_name);
	}

	private void disconnect_stream(final port_connection connection) {
		switch (connection.connection_state) {
		case CONN_LISTENING:
			TtcnLogger.log_port_misc(TitanLoggerApi.Port__Misc_reason.enum_type.destroying__unestablished__connection, port_name, connection.remote_component, connection.remote_port, null, -1, 0);
			remove_connection(connection);
			break;
		case CONN_CONNECTED: {
			TtcnLogger.log_port_misc(TitanLoggerApi.Port__Misc_reason.enum_type.terminating__connection, port_name, connection.remote_component, connection.remote_port, null, -1, 0);
			final Text_Buf outgoing_buf = new Text_Buf();
			outgoing_buf.push_int(port_connection.connection_data_type_enum.CONN_DATA_LAST.ordinal());
			if (send_data_stream(connection, outgoing_buf, true)) {
				//sending the last message was successful
				// waiting for confirmation from the peer
				connection.connection_state = port_connection.connection_state_enum.CONN_LAST_MSG_SENT;
			} else {
				TtcnLogger.log_port_misc(TitanLoggerApi.Port__Misc_reason.enum_type.sending__termination__request__failed, port_name, connection.remote_component, connection.remote_port, null, -1, 0);
				// send an acknowledgment to MC immediately to avoid deadlock
				// in case of communication failure
				TTCN_Communication.send_disconnected(port_name, connection.remote_component, connection.remote_port);
				TtcnError.TtcnWarning(MessageFormat.format("The last outgoing messages on port {0} may be lost.", port_name));
				//destroy the connection immediately
				remove_connection(connection);
			}
			break;
		}
		default:
			throw new TtcnError(MessageFormat.format("The connection of port {0} to {1}:{2} is in unexpected state when trying to terminate it.", port_name, connection.remote_component, connection.remote_port));
		}
	}

	private void send_data_local(final port_connection connection, final Text_Buf outgoing_data) {
		outgoing_data.rewind();
		final TitanPort destination_port = connection.local_port;
		if (this != destination_port) {
			final port_connection connection2 = destination_port.lookup_connection(TitanComponent.self.get().componentValue, port_name);
			if (connection2 == null) {
				throw new TtcnError(MessageFormat.format("Internal error: Port {0} is connected with local port {1}, but port {1} does not have a connection to {0}.", port_name, destination_port.port_name));
			} else {
				destination_port.process_data(connection2, outgoing_data);
			}
		} else {
			process_data(connection, outgoing_data);
		}
	}

	private boolean send_data_stream(final port_connection connection, final Text_Buf outgoing_data, final boolean ignore_peer_disconnect) {
		outgoing_data.calculate_length();
		final byte[] msg_ptr = outgoing_data.get_data();
		final int msg_len = outgoing_data.get_len();
		final ByteBuffer buffer = ByteBuffer.allocate(msg_len);
		buffer.clear();
		final byte[] temp_msg_ptr = new byte[msg_len];
		System.arraycopy(msg_ptr, outgoing_data.get_begin(), temp_msg_ptr, 0, msg_len);
		buffer.put(temp_msg_ptr);
		buffer.flip();
		while (buffer.hasRemaining()) {
			try {
				((SocketChannel)connection.stream_socket).write(buffer);
			} catch (final IOException e) {
				throw new TtcnError(e);
			}
		}
		//FIXME implement
		return true;
	}

	private void handle_incoming_connection(final port_connection connection) {
		final ServerSocketChannel serverSocketChannel = (ServerSocketChannel) connection.stream_socket;
		try {
			final SocketChannel com_channel = serverSocketChannel.accept();
			//FIXME only a prototype
			TTCN_Snapshot.channelMap.get().remove(serverSocketChannel);

			connection.connection_state = port_connection.connection_state_enum.CONN_CONNECTED;
			connection.stream_socket = com_channel;
			com_channel.configureBlocking(false);
			TTCN_Snapshot.channelMap.get().put(com_channel, connection);
			com_channel.register(TTCN_Snapshot.selector.get(), SelectionKey.OP_READ);

			serverSocketChannel.close();

			TTCN_Communication.send_connected(port_name, connection.remote_component, connection.remote_port);

			TtcnLogger.log_port_misc(TitanLoggerApi.Port__Misc_reason.enum_type.connection__accepted, port_name, connection.remote_component, connection.remote_port, null, -1, 0);
		} catch (final IOException e) {
			throw new TtcnError(e);
		}
	}

	private void handle_incoming_data(final port_connection connection) {
		if (connection.stream_incoming_buf == null) {
			connection.stream_incoming_buf = new Text_Buf();
		}

		final Text_Buf incoming_buffer = connection.stream_incoming_buf;
		final AtomicInteger end_index = new AtomicInteger();
		final AtomicInteger end_len = new AtomicInteger();
		incoming_buffer.get_end(end_index, end_len);
		final ByteBuffer buffer = ByteBuffer.allocate(1024);
		try {
			final int recv_len = ((SocketChannel)connection.stream_socket).read(buffer);
			if (recv_len < 0) {
				//the connection is closed
				//FIXME implement rest
				TTCN_Communication.send_disconnected(port_name, connection.remote_component, connection.remote_port);
				TtcnLogger.log_port_misc(TitanLoggerApi.Port__Misc_reason.enum_type.connection__reset__by__peer, port_name, connection.remote_component, connection.remote_port, null, -1, 0);
				TtcnError.TtcnWarning(MessageFormat.format("The last outgoing messages on port {0} may be lost.", port_name));
				connection.connection_state = port_connection.connection_state_enum.CONN_IDLE;
			} else if (recv_len > 0) {
				buffer.flip();
				incoming_buffer.increase_length(buffer.remaining());
				final byte[] data = incoming_buffer.get_data();
				final int remaining = buffer.remaining();
				final byte[] temp = new byte[remaining];
				buffer.get(temp);
				System.arraycopy(temp, 0, data, end_index.get(), remaining);

				while (incoming_buffer.is_message()) {
					incoming_buffer.pull_int(); // message_length
					process_data(connection, incoming_buffer);
					incoming_buffer.cut_message();
				}
			} else {
				// the connection was closed by the peer
				TTCN_Communication.send_disconnected(port_name, connection.remote_component, connection.remote_port);
				if (connection.connection_state == port_connection.connection_state_enum.CONN_LAST_MSG_RCVD) {
					TtcnLogger.log_port_misc(TitanLoggerApi.Port__Misc_reason.enum_type.connection__closed__by__peer, port_name, connection.remote_component, connection.remote_port, null, -1, 0);
				}
				// the connection can be removed
				connection.connection_state = port_connection.connection_state_enum.CONN_IDLE;
			}
		} catch (final IOException e) {
			throw new TtcnError(e);
		}

		if (connection.connection_state == port_connection.connection_state_enum.CONN_IDLE) {
			// terminating and removing connection
			final int msg_len = incoming_buffer.get_len();
			if (msg_len > 0) {
				TtcnError.TtcnWarningBegin(MessageFormat.format("Message fragment remained in the buffer of port connection between {0} and ", port_name));
				TitanComponent.log_component_reference(connection.remote_component);
				TtcnLogger.log_event_str(MessageFormat.format(":{0}: ", connection.remote_port));
				final byte[] msg = incoming_buffer.get_data();
				for (int i = 0; i < msg_len; i++) {
					TtcnLogger.log_octet((char)msg[i]);
				}
				TtcnError.TtcnWarningEnd();
			}

			TtcnLogger.log_port_misc(TitanLoggerApi.Port__Misc_reason.enum_type.port__disconnected, port_name, connection.remote_component, connection.remote_port, null, -1, 0);
			remove_connection(connection);
		}
	}

	private void process_last_message(final port_connection connection) {
		switch(connection.transport_type) {
		case TRANSPORT_INET_STREAM:
			break;
		default:
			throw new TtcnError(MessageFormat.format("Internal error: Connection termination request was received on the connection of port {0} with {1}:{2}, which has an invalid transport type ({3}).", port_name, connection.remote_component, connection.remote_port, connection.transport_type));
		}

		switch (connection.connection_state) {
		case CONN_CONNECTED: {
			TtcnLogger.log_port_misc(TitanLoggerApi.Port__Misc_reason.enum_type.termination__request__received, port_name, connection.remote_component, connection.remote_port, null, -1, 0);
			final Text_Buf outgoing_buf = new Text_Buf();
			outgoing_buf.push_int(port_connection.connection_data_type_enum.CONN_DATA_LAST.ordinal());
			if (send_data_stream(connection, outgoing_buf, true)) {
				// sending the last message was successful wait until the peer closes the transport connection
				connection.connection_state = port_connection.connection_state_enum.CONN_LAST_MSG_RCVD;
			} else {
				TtcnLogger.log_port_misc(TitanLoggerApi.Port__Misc_reason.enum_type.acknowledging__termination__request__failed, port_name, connection.remote_component, connection.remote_port, null, -1, 0);
				// send an acknowledgment to MC immediately to avoid deadlock in case of communication failure
				TTCN_Communication.send_disconnected(port_name, connection.remote_component, connection.remote_port);
				// the connection can be removed immediately
				TtcnError.TtcnWarning(MessageFormat.format("The last outgoing messages on port {0} may be lost.", port_name));
				connection.connection_state = port_connection.connection_state_enum.CONN_IDLE;
			}
			break;
		}
		case CONN_LAST_MSG_SENT:
			connection.connection_state = port_connection.connection_state_enum.CONN_IDLE;
			break;
		case CONN_LAST_MSG_RCVD:
		case CONN_IDLE:
			TtcnError.TtcnWarning(MessageFormat.format("Unexpected data arrived after the indication of connection termination on port {0} from {1}:{2}.", port_name, connection.remote_component, connection.remote_port));
			break;
		default:
			throw new TtcnError(MessageFormat.format("Internal error: Connection of port {0} with {1}:{2} has invalid state ({3}).", port_name, connection.remote_component, connection.remote_port, connection.connection_state.ordinal()));
		}
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

		if (system_mappings.size() == 0) {
			reset_port_variables();
		}
		// FIXME implement logging
	}

	public static void process_connect_listen(final String local_port, final int remote_component, final String remote_port, final transport_type_enum transport_type) {
		final TitanPort port = lookup_by_name(local_port, false);
		if (port == null) {
			TTCN_Communication.send_connect_error(local_port, remote_component, remote_port, MessageFormat.format("Port {0} does not exist.", local_port));

			return;
		} else if (!port.is_active) {
			throw new TtcnError(MessageFormat.format("Internal error: Port {0} is inactive when trying to connect it to {1}:{2}.", local_port, remote_component, remote_port));
		} else if (port.lookup_connection(remote_component, remote_port) != null) {
			TTCN_Communication.send_connect_error(local_port, remote_component, remote_port, MessageFormat.format("Port {0} already has a connection towards {1}:{2}.", local_port, remote_component, remote_port));

			return;
		} else if (port.lookup_connection_to_compref(remote_component, null) != null) {
			TtcnError.TtcnWarningBegin(MessageFormat.format("Port {0} will have more than one connections with ports of test component ", local_port));
			TitanComponent.log_component_reference(remote_component);
			TtcnLogger.log_event_str(". These connections cannot be used for sending even with explicit addressing.");
			TtcnError.TtcnWarningEnd();
		}

		switch (transport_type) {
		case TRANSPORT_LOCAL:
			TTCN_Communication.send_connect_error(local_port, remote_component, remote_port, "Message CONNECT_LISTEN cannot refer to transport type LOCAL.");

			break;
		case TRANSPORT_INET_STREAM:
			port.connect_listen_inet_stream(remote_component, remote_port);
			break;
		default:
			TTCN_Communication.send_connect_error(local_port, remote_component, remote_port, MessageFormat.format("Message CONNECT_LISTEN refers to invalid transport type ({0})", transport_type.ordinal()));
			break;
		}
	}

	public static void process_connect(final String local_port, final int remote_component, final String remote_port, final transport_type_enum transport_type, final Text_Buf text_buf) {
		final TitanPort port = lookup_by_name(local_port, false);
		if (port == null) {
			TTCN_Communication.send_connect_error(local_port, remote_component, remote_port, MessageFormat.format("Port {0} does not exist.", local_port));

			return;
		} else if (!port.is_active) {
			throw new TtcnError(MessageFormat.format("Internal error: Port {0} is inactive when trying to connect it to {1}:{2}.", local_port, remote_component, remote_port));
		} else if (port.lookup_connection(remote_component, remote_port) != null) {
			TTCN_Communication.send_connect_error(local_port, remote_component, remote_port, MessageFormat.format("Port {0} already has a connection towards {1}:{2}.", local_port, remote_component, remote_port));

			return;
		} else if (port.lookup_connection_to_compref(remote_component, null) != null) {
			TtcnError.TtcnWarningBegin(MessageFormat.format("Port {0} will have more than one connections with ports of test component ", local_port));
			TitanComponent.log_component_reference(remote_component);
			TtcnLogger.log_event_str(". These connections cannot be used for sending even with explicit addressing.");
			TtcnError.TtcnWarningEnd();
		}

		switch (transport_type) {
		case TRANSPORT_LOCAL:
			port.connect_local(remote_component, remote_port);
			break;
		case TRANSPORT_INET_STREAM:
			port.connect_stream(remote_component, remote_port, transport_type, text_buf);
			break;
		default:
			TTCN_Communication.send_connect_error(local_port, remote_component, remote_port, MessageFormat.format("Message CONNECT refers to invalid transport type ({0})", transport_type.ordinal()));
			break;
		}
	}

	public static void process_disconnect(final String local_port, final int remote_component, final String remote_port) {
		final TitanPort port = lookup_by_name(local_port, false);
		if (port == null) {
			TTCN_Communication.send_error(MessageFormat.format("Message DISCONNECT refers to non-existent local port {0}.", local_port));

			return;
		} else if (!port.is_active) {
			throw new TtcnError(MessageFormat.format("Internal error: Port {0} is inactive when trying to disconnect it from {1}:{2}.", local_port, remote_component, remote_port));
		}

		final port_connection connection = port.lookup_connection(remote_component, remote_port);
		if (connection == null) {
			//the connection does not exist
			if (TitanComponent.self.get().componentValue == remote_component && lookup_by_name(remote_port, false) == null) {
				TTCN_Communication.send_error(MessageFormat.format("Message DISCONNECT refers to non-existent port {0}.", remote_port));
			} else {
				TTCN_Communication.send_disconnected(local_port, remote_component, remote_port);
			}
			return;
		}

		switch (connection.transport_type) {
		case TRANSPORT_LOCAL:
			port.disconnect_local(connection);
			break;
		case TRANSPORT_INET_STREAM:
			port.disconnect_stream(connection);
			break;
		default:
			throw new TtcnError(MessageFormat.format("Internal error: The connection of port {0} to {1}:{2} has invalid transport type ({3}) when trying to terminate the connection.", local_port, remote_component, remote_port, connection.transport_type.ordinal()));
		}

	}

	public static void make_local_connection(final String source_port, final String destination_port) {
		final TitanPort sourcePort = lookup_by_name(source_port, false);
		if (sourcePort == null) {
			throw new TtcnError(MessageFormat.format("Connect operation refers to non-existent port {0}.", source_port));
		} else if (!sourcePort.is_active) {
			throw new TtcnError(MessageFormat.format("Internal error: Port {0} is inactive when trying to connect it with local port {1}.", source_port, destination_port));
		} else if (sourcePort.lookup_connection(TitanComponent.MTC_COMPREF, destination_port) != null) {
			TtcnError.TtcnWarning(MessageFormat.format("Port {0} is already connected with local port {1}. Connect operation had no effect.", source_port, destination_port));

			return;
		} else if (sourcePort.lookup_connection_to_compref(TitanComponent.MTC_COMPREF, null) != null) {
			TtcnError.TtcnWarning(MessageFormat.format("Port {0} will have more than one connections with local ports. These connections cannot be used for communication even with explicit addressing.", source_port));
		}

		final TitanPort destinationPort = lookup_by_name(destination_port, false);
		if (destinationPort == null) {
			throw new TtcnError(MessageFormat.format("Connect operation refers to non-existent port {0}.", destination_port));
		} else if (!destinationPort.is_active) {
			throw new TtcnError(MessageFormat.format("Internal error: Port {0} is inactive when trying to connect it with local port {1}.", destination_port, source_port));
		}

		sourcePort.add_local_connection(destinationPort);
		if (sourcePort != destinationPort) {
			destinationPort.add_local_connection(sourcePort);
		}
	}

	public static void terminate_local_connection(final String source_port, final String destination_port) {
		final TitanPort sourcePort = lookup_by_name(source_port, false);
		if (sourcePort == null) {
			throw new TtcnError(MessageFormat.format("Disconnect operation refers to non-existent port {0}.", source_port));
		} else if (!sourcePort.is_active) {
			throw new TtcnError(MessageFormat.format("Internal error: Port {0} is inactive when trying to disconnect it with local port {1}.", source_port, destination_port));
		}

		final port_connection connection = sourcePort.lookup_connection(TitanComponent.MTC_COMPREF, destination_port);
		if (connection == null) {
			final TitanPort destinationPort = lookup_by_name(destination_port, false);
			if (destinationPort == null) {
				throw new TtcnError(MessageFormat.format("Disconnect operation refers to non-existent port {0}.", destination_port));
			} else if (sourcePort != destinationPort) {
				if (!destinationPort.is_active) {
					throw new TtcnError(MessageFormat.format("Internal error: Port {0} is inactive when trying to disconnect it with local port {1}.", destination_port, source_port));
				} else if (destinationPort.lookup_connection(TitanComponent.MTC_COMPREF, source_port) != null) {
					throw new TtcnError(MessageFormat.format("Internal error: Port {0} is connected with local port {1}, but port {1} does not have a connection to {0}.", destination_port, source_port));
				}
			}
			//FIXME
		} else {
			final TitanPort destinationPort = lookup_by_name(destination_port, false);
			sourcePort.remove_local_connection(connection);
			if (sourcePort != destinationPort) {
				if (!destinationPort.is_active) {
					throw new TtcnError(MessageFormat.format("Internal error: Port {0} is inactive when trying to disconnect it with local port {1}.", destination_port, source_port));
				}

				final port_connection connection2 = destinationPort.lookup_connection(TitanComponent.MTC_COMPREF, source_port);
				if (connection2 == null) {
					throw new TtcnError(MessageFormat.format("Internal error: Port {0} is connected with local port {1}, but port {1} does not have a connection to {0}.", source_port, destination_port));
				} else {
					destinationPort.remove_local_connection(connection2);
				}
			}
		}
	}

	public static void map_port(final String component_port, final String system_port, final boolean translation) {
		final String port_name = translation ? system_port : component_port;
		final TitanPort port = lookup_by_name(port_name, translation);
		if (port == null) {
			throw new TtcnError(MessageFormat.format("Map operation refers to non-existent port {0}.", component_port));
		}
		if (!port.connection_list.isEmpty()) {
			throw new TtcnError(MessageFormat.format("Map operation is not allowed on a connected port ({0}).", port_name));
		}

		if (translation) {
			port.map(component_port, translation);
		} else {
			port.map(system_port, translation);
		}
		if (!TTCN_Runtime.is_single()) {
			if (translation) {
				TTCN_Communication.send_mapped(system_port, component_port, translation);
			} else {
				TTCN_Communication.send_mapped(component_port, system_port, translation);
			}
		}
	}

	public static void unmap_port(final String component_port, final String system_port, final boolean translation) {
		final String port_name = translation ? system_port : component_port;
		final TitanPort port = lookup_by_name(port_name, translation);
		if (port == null) {
			throw new TtcnError(MessageFormat.format("Unmap operation refers to non-existent port {0}.", component_port));
		}

		if (translation) {
			port.unmap(component_port, translation);
		} else {
			port.unmap(system_port, translation);
		}
		if (!TTCN_Runtime.is_single()) {
			TTCN_Communication.send_unmapped(component_port, system_port, translation);
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

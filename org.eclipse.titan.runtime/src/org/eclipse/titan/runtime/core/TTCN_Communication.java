/******************************************************************************
 * Copyright (c) 2000-2018 Ericsson Telecom AB
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 ******************************************************************************/
package org.eclipse.titan.runtime.core;

import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectableChannel;
import java.nio.channels.SelectionKey;
import java.nio.channels.SocketChannel;
import java.text.MessageFormat;
import java.util.concurrent.atomic.AtomicInteger;

import org.eclipse.titan.runtime.core.Event_Handler.Channel_And_Timeout_Event_Handler;
import org.eclipse.titan.runtime.core.TTCN_Runtime.executorStateEnum;
import org.eclipse.titan.runtime.core.TitanLoggerApi.ExecutorConfigdata_reason;
import org.eclipse.titan.runtime.core.TitanVerdictType.VerdictTypeEnum;
import org.eclipse.titan.runtime.core.TtcnLogger.Severity;

/**
 * The class handling internal communication.
 *
 * TODO: lots to implement
 *
 * @author Kristof Szabados
 */
public class TTCN_Communication {
	/* Any relation - any direction */

	private static final int MSG_ERROR = 0;

	/* Any relation - to MC (up) */

	private static final int MSG_LOG = 1;

	/* First messages - to MC (up) */

	/* from HCs */
	private static final int MSG_VERSION = 2;
	/* from MTC */
	private static final int MSG_MTC_CREATED = 3;
	/* from PTCs */
	private static final int MSG_PTC_CREATED = 4;

	/* Messages from MC to HC (down) */

	private static final int MSG_CREATE_MTC = 2;
	private static final int MSG_CREATE_PTC = 3;
	private static final int MSG_KILL_PROCESS = 4;
	private static final int MSG_EXIT_HC = 5;

	/* Messages from HC to MC (up) */

	private static final int MSG_CREATE_NAK = 4;
	private static final int MSG_HC_READY = 5;

	/* Messages from MC to TC (down) */

	private static final int MSG_CREATE_ACK = 1;
	private static final int MSG_START_ACK = 2;
	private static final int MSG_STOP = 3;
	private static final int MSG_STOP_ACK = 4;
	private static final int MSG_KILL_ACK = 5;
	private static final int MSG_RUNNING = 6;
	private static final int MSG_ALIVE = 7;
	private static final int MSG_DONE_ACK = 8;
	private static final int MSG_KILLED_ACK = 9;
	private static final int MSG_CANCEL_DONE = 10;
	private static final int MSG_COMPONENT_STATUS = 11;
	private static final int MSG_CONNECT_LISTEN = 12;
	private static final int MSG_CONNECT = 13;
	private static final int MSG_CONNECT_ACK = 14;
	private static final int MSG_DISCONNECT = 15;
	private static final int MSG_DISCONNECT_ACK = 16;
	private static final int MSG_MAP = 17;
	private static final int MSG_MAP_ACK = 18;
	private static final int MSG_UNMAP = 19;
	private static final int MSG_UNMAP_ACK = 20;

	/* Messages from MC to MTC (down) */

	private static final int MSG_EXECUTE_CONTROL = 21;
	private static final int MSG_EXECUTE_TESTCASE = 22;
	private static final int MSG_PTC_VERDICT = 23;
	private static final int MSG_CONTINUE = 24;
	private static final int MSG_EXIT_MTC = 25;

	/* Messages from MC to PTC (down) */

	private static final int MSG_START = 21;
	private static final int MSG_KILL = 22;

	/* Messages from TC to MC (up) */

	private static final int MSG_CREATE_REQ = 2;
	private static final int MSG_START_REQ = 3;
	private static final int MSG_STOP_REQ = 4;
	private static final int MSG_KILL_REQ = 5;
	private static final int MSG_IS_RUNNING = 6;
	private static final int MSG_IS_ALIVE = 7;
	private static final int MSG_DONE_REQ = 8;
	private static final int MSG_KILLED_REQ = 9;
	private static final int MSG_CANCEL_DONE_ACK = 10;
	private static final int MSG_CONNECT_REQ = 11;
	private static final int MSG_CONNECT_LISTEN_ACK = 12;
	private static final int MSG_CONNECTED = 13;
	private static final int MSG_CONNECT_ERROR = 14;
	private static final int MSG_DISCONNECT_REQ = 15;
	private static final int MSG_DISCONNECTED = 16;
	private static final int MSG_MAP_REQ = 17;
	private static final int MSG_MAPPED = 18;
	private static final int MSG_UNMAP_REQ = 19;
	private static final int MSG_UNMAPPED = 20;
	private static final int MSG_DEBUG_HALT_REQ = 101;
	private static final int MSG_DEBUG_CONTINUE_REQ = 102;
	private static final int MSG_DEBUG_BATCH = 103;

	/* Messages from MTC to MC (up) */

	private static final int MSG_TESTCASE_STARTED = 21;
	private static final int MSG_TESTCASE_FINISHED = 22;
	private static final int MSG_MTC_READY = 23;

	/* Messages from PTC to MC (up) */

	private static final int MSG_STOPPED = 21;
	private static final int MSG_STOPPED_KILLED = 22;
	private static final int MSG_KILLED = 23;

	/* Messages from MC to HC or TC (down) */

	private static final int MSG_DEBUG_COMMAND = 100;

	/* Messages from HC or TC to MC (up) */

	private static final int MSG_DEBUG_RETURN_VALUE = 100;

	/* Messages from MC to HC or MTC (down) */

	private static final int MSG_CONFIGURE = 200;

	/* Messages from HC or MTC to MC (up) */

	private static final int MSG_CONFIGURE_ACK = 200;
	private static final int MSG_CONFIGURE_NAK = 201;

	public static enum transport_type_enum {
		TRANSPORT_LOCAL,
		TRANSPORT_INET_STREAM,
		/* unsupported as it is not platform independent */
		@Deprecated
		TRANSPORT_UNIX_STREAM,
		/*unused marks the current number of enumerations */
		TRANSPORT_NUM
	}

	private static boolean mc_addr_set = false;
	private static ThreadLocal<Boolean> is_connected = new ThreadLocal<Boolean>() {
		@Override
		protected Boolean initialValue() {
			return false;
		}
	};

	private static String MC_host;
	private static int MC_port;
	//private static Socket mc_socket;
	private static ThreadLocal<SocketChannel> mc_socketchannel = new ThreadLocal<SocketChannel>() {
		@Override
		protected SocketChannel initialValue() {
			return null;
		}
	};
	//private static DataOutputStream mc_outputstream;
	private static ThreadLocal<Text_Buf> incoming_buf = new ThreadLocal<Text_Buf>() {
		@Override
		protected Text_Buf initialValue() {
			return new Text_Buf();
		}
	};

	static class MC_Connection extends Channel_And_Timeout_Event_Handler {
		final SocketChannel mc_channel;
		final Text_Buf incoming_buffer;

		MC_Connection(final SocketChannel channel, final Text_Buf buffer) {
			mc_channel = channel;
			incoming_buffer = buffer;
		}

		@Override
		public void Handle_Event(final SelectableChannel channel, final boolean is_readable, final boolean is_writeable) {
			if (!channel.equals(mc_channel)) {
				throw new TtcnError("MC_Connection: unexpected selectable channel.");
			}
			//FIXME implement
			if (is_readable) {
				final AtomicInteger buf_ptr = new AtomicInteger();
				final AtomicInteger buf_len = new AtomicInteger();
				incoming_buffer.get_end(buf_ptr, buf_len);

				final ByteBuffer tempbuffer = ByteBuffer.allocate(1024);
				int recv_len = 0;
				try {
					recv_len = mc_channel.read(tempbuffer);
				} catch (IOException e) {
					throw new TtcnError(e);
				}
				if (recv_len > 0) {
					//incoming_buf.increase_length(recv_len);
					incoming_buf.get().push_raw(recv_len, tempbuffer.array());

					if (!TTCN_Runtime.is_idle()) {
						process_all_messages_tc();
					}
				} else {
					close_mc_connection();
					if (recv_len == 0) {
						throw new TtcnError("Control connection was closed unexpectedly by MC.");
					} else {
						throw new TtcnError("Receiving data on the control connection from MC failed.");
					}
					//FIXME implement
				}
			}
		}
		//FIXME implement

		@Override
		public void Handle_Timeout(final double time_since_last_call) {
			if (TTCN_Runtime.get_state() == executorStateEnum.HC_OVERLOADED) {
				// indicate the timeout to be handled in process_all_messages_hc()
				TTCN_Runtime.set_state(executorStateEnum.HC_OVERLOADED_TIMEOUT);
			} else {
				TtcnError.TtcnWarning("Unexpected timeout occurred on the control connection to MC.");
				disable_periodic_call();
			}
		}
		
	}

	public static void set_mc_address(final String MC_host, final int MC_port) {
		if (mc_addr_set) {
			TtcnError.TtcnWarning("The address of MC has already been set.");
		}
		if (is_connected.get()) {
			throw new TtcnError("Trying to change the address of MC, but there is an existing connection.");
		}
		//FIXME implement
		TTCN_Communication.MC_host = MC_host;
		TTCN_Communication.MC_port = MC_port;
		mc_addr_set = true;
	}

	public static void connect_mc() {
		if (is_connected.get()) {
			throw new TtcnError("Trying to re-connect to MC, but there is an existing connection.");
		}
		if (!mc_addr_set) {
			throw new TtcnError("Trying to connect to MC, but the address of MC has not yet been set.");
		}

		try {
			mc_socketchannel.set(SocketChannel.open());
			mc_socketchannel.get().connect(new InetSocketAddress(MC_host, MC_port));
			//FIXME register
		} catch (IOException e) {
			throw new TtcnError(e);
		}
		//FIXME implement
		final MC_Connection mc_connection = new MC_Connection(mc_socketchannel.get(), incoming_buf.get());
		try {
			mc_socketchannel.get().configureBlocking(false);
			TTCN_Snapshot.channelMap.get().put(mc_socketchannel.get(), mc_connection);
			mc_socketchannel.get().register(TTCN_Snapshot.selector.get(), SelectionKey.OP_READ);
		} catch (IOException e) {
			throw new TtcnError(e);
		}

		TtcnLogger.log_executor_runtime(TitanLoggerApi.ExecutorRuntime_reason.enum_type.connected__to__mc);
		is_connected.set(true);;
	}

	public static void disconnect_mc() {
		if (is_connected.get()) {
			// TODO check if the missing part is needed
			close_mc_connection();
			TtcnLogger.log_executor_runtime(TitanLoggerApi.ExecutorRuntime_reason.enum_type.disconnected__from__mc);
		}
	}

	public static void close_mc_connection() {
		if (is_connected.get()) {
			is_connected.set(false);;
			incoming_buf.get().reset();
			try {
				mc_socketchannel.get().close();
			} catch (IOException e) {
				throw new TtcnError(e);
			}

			TTCN_Snapshot.channelMap.get().remove(mc_socketchannel);
			//FIXME implement
		}
	}

	public static void disable_periodic_call() {
		//FIXME implement
	}

	public static void process_all_messages_hc() {
		if (!TTCN_Runtime.is_hc()) {
			throw new TtcnError("Internal error: TTCN_Communication::process_all_messages_hc() was called in invalid state.");
		}

		TTCN_Runtime.wait_terminated_processes();
		boolean wait_flag = false;
		boolean check_overload = TTCN_Runtime.is_overloaded();
		while (incoming_buf.get().is_message()) {
			wait_flag = true;

			final int msg_len = incoming_buf.get().pull_int().getInt();
			final int msg_end = incoming_buf.get().get_pos() + msg_len;
			final int msg_type = incoming_buf.get().pull_int().getInt();

			switch (msg_type) {
			case MSG_ERROR:
				process_error();
				break;
			case MSG_CONFIGURE:
				process_configure(msg_end, false);
				break;
			case MSG_CREATE_MTC:
				process_create_mtc();
				TTCN_Runtime.wait_terminated_processes();
				wait_flag = false;
				check_overload = false;
				break;
			case MSG_CREATE_PTC:
				process_create_ptc();
				TTCN_Runtime.wait_terminated_processes();
				wait_flag = false;
				check_overload = false;
				break;
			case MSG_KILL_PROCESS:
				process_kill_process();
				TTCN_Runtime.wait_terminated_processes();
				wait_flag = false;
				break;
			case MSG_EXIT_HC:
				process_exit_hc();
				break;
			case MSG_DEBUG_COMMAND:
				process_debug_command();
				break;
			default:
				process_unsupported_message(msg_type, msg_end);
				break;
			}
		}
		if (wait_flag) {
			TTCN_Runtime.wait_terminated_processes();
		}
		if (check_overload && TTCN_Runtime.is_overloaded()) {
			//FIXME implement
		}
	}

	public static void process_all_messages_tc() {
		if (!TTCN_Runtime.is_tc()) {
			throw new TtcnError("Internal error: TTCN_Communication::process_all_messages_tc() was called in invalid state.");
		}

		while (incoming_buf.get().is_message()) {
			final int msg_len = incoming_buf.get().pull_int().getInt();
			final int msg_end = incoming_buf.get().get_pos() + msg_len;
			final int msg_type = incoming_buf.get().pull_int().getInt();

			// messages: MC -> TC
			switch (msg_type) {
			case MSG_ERROR:
				process_error();
				break;
			case MSG_CREATE_ACK:
				process_create_ack();
				break;
			case MSG_START_ACK:
				process_start_ack();
				break;
			case MSG_STOP:
				process_stop();
				break;
			case MSG_STOP_ACK:
				process_stop_ack();
				break;
			case MSG_KILL_ACK:
				process_kill_ack();
				break;
			case MSG_RUNNING:
				process_running();
				break;
			case MSG_ALIVE:
				process_alive();
				break;
			case MSG_DONE_ACK:
				process_done_ack(msg_end);
				break;
			case MSG_KILLED_ACK:
				process_killed_ack();
				break;
			case MSG_CANCEL_DONE:
				//FIXME
				throw new TtcnError("MSG_CANCEL_DONE received, but not yet supported!");
			case MSG_COMPONENT_STATUS:
				if (TTCN_Runtime.is_mtc()) {
					process_component_status_mtc(msg_end);
				} else {
					process_component_status_ptc(msg_end);
				}
				break;
			case MSG_CONNECT_LISTEN:
				process_connect_listen();
				break;
			case MSG_CONNECT:
				process_connect();
				break;
			case MSG_CONNECT_ACK:
				process_connect_ack();
				break;
			case MSG_DISCONNECT:
				process_disconnect();
				break;
			case MSG_DISCONNECT_ACK:
				process_disconnect_ack();
				break;
			case MSG_MAP:
				process_map();
				break;
			case MSG_MAP_ACK:
				process_map_ack();
				break;
			case MSG_UNMAP:
				process_unmap();
				break;
			case MSG_UNMAP_ACK:
				process_unmap_ack();
				break;
			case MSG_DEBUG_COMMAND:
				//FIXME process_debug_command();
				throw new TtcnError("MSG_DEBUG_COMMAND received, but not yet supported!");
			default:
				if (TTCN_Runtime.is_mtc()) {
					// messages: MC -> MTC
					switch(msg_type) {
					case MSG_EXECUTE_CONTROL:
						process_execute_control();
						break;
					case MSG_EXECUTE_TESTCASE:
						process_execute_testcase();
						break;
					case MSG_PTC_VERDICT:
						process_ptc_verdict();
						break;
					case MSG_CONTINUE:
						//FIXME process_continue();
						throw new TtcnError("MSG_CONTINUE received, but not yet supported!");
					case MSG_EXIT_MTC:
						process_exit_mtc();
						break;
					case MSG_CONFIGURE:
						//FIXME process_configure(msg_end, TRUE);
						throw new TtcnError("MSG_CONFIGURE received, but not yet supported!");
					default:
						process_unsupported_message(msg_type, msg_end);
						break;
					}
				} else {
					// messages: MC -> PTC
					switch (msg_type) {
					case MSG_START:
						process_start();
						break;
					case MSG_KILL:
						process_kill();
						break;
					default:
						process_unsupported_message(msg_type, msg_end);
					}
				}
			}
		}
	}

	public static void send_version() {
		//FIXME implement (only temporary values for now)

		final Text_Buf text_buf = new Text_Buf();
		text_buf.push_int(MSG_VERSION);
		//sending temporary data
		text_buf.push_int(TTCN_Runtime.TTCN3_MAJOR);
		text_buf.push_int(TTCN_Runtime.TTCN3_MINOR);
		text_buf.push_int(TTCN_Runtime.TTCN3_PATCHLEVEL);
		text_buf.push_int(TTCN_Runtime.TTCN3_BUILDNUMBER);

		text_buf.push_int(0);//for now send no module info
		//Module_List.push_version(text_buf);

		//FIXME fill with correct machine info
		text_buf.push_string("node");
		text_buf.push_string("machine");
		text_buf.push_string("sysname");
		text_buf.push_string("release");
		text_buf.push_string("version");

		text_buf.push_int(2);//nof supported transports
		text_buf.push_int(transport_type_enum.TRANSPORT_LOCAL.ordinal()); //TRANSPORT_LOCAL
		text_buf.push_int(transport_type_enum.TRANSPORT_INET_STREAM.ordinal()); //TRANSPORT_INET_STREAM

		send_message(text_buf);
	}

	public static void send_configure_ack() {
		final Text_Buf text_buf = new Text_Buf();
		text_buf.push_int(MSG_CONFIGURE_ACK);

		send_message(text_buf);
	}

	public static void send_configure_nak() {
		final Text_Buf text_buf = new Text_Buf();
		text_buf.push_int(MSG_CONFIGURE_NAK);

		send_message(text_buf);
	}

	public static void send_create_req(final String componentTypeModule, final String componentTypeName,
			final String componentName, final String componentLocation, final boolean is_alive) {
		final Text_Buf text_buf = new Text_Buf();
		text_buf.push_int(MSG_CREATE_REQ);
		text_buf.push_string(componentTypeModule);
		text_buf.push_string(componentTypeName);
		text_buf.push_string(componentName);
		text_buf.push_string(componentLocation);
		text_buf.push_int( is_alive ? 1 : 0);

		send_message(text_buf);
	}

	public static void prepare_start_req(final Text_Buf text_buf, final int component_reference, final String module_name, final String function_name) {
		text_buf.push_int(MSG_START_REQ);
		text_buf.push_int(component_reference);
		text_buf.push_string(module_name);
		text_buf.push_string(function_name);
	}

	public static void send_stop_req(final int componentReference) {
		final Text_Buf text_buf = new Text_Buf();
		text_buf.push_int(MSG_STOP_REQ);
		text_buf.push_int(componentReference);

		send_message(text_buf);
	}

	public static void send_kill_req(final int componentReference) {
		final Text_Buf text_buf = new Text_Buf();
		text_buf.push_int(MSG_KILL_REQ);
		text_buf.push_int(componentReference);

		send_message(text_buf);
	}

	public static void send_is_running(final int componentReference) {
		final Text_Buf text_buf = new Text_Buf();
		text_buf.push_int(MSG_IS_RUNNING);
		text_buf.push_int(componentReference);

		send_message(text_buf);
	}

	public static void send_is_alive(final int componentReference) {
		final Text_Buf text_buf = new Text_Buf();
		text_buf.push_int(MSG_IS_ALIVE);
		text_buf.push_int(componentReference);

		send_message(text_buf);
	}

	public static void send_done_req(final int componentReference) {
		final Text_Buf text_buf = new Text_Buf();
		text_buf.push_int(MSG_DONE_REQ);
		text_buf.push_int(componentReference);

		send_message(text_buf);
	}

	public static void send_killed_req(final int componentReference) {
		final Text_Buf text_buf = new Text_Buf();
		text_buf.push_int(MSG_KILLED_REQ);
		text_buf.push_int(componentReference);

		send_message(text_buf);
	}

	public static void send_connect_req(final int sourceComponent, final String sourcePort, final int destinationComponent, final String destinationPort) {
		final Text_Buf text_buf = new Text_Buf();
		text_buf.push_int(MSG_CONNECT_REQ);
		text_buf.push_int(sourceComponent);
		text_buf.push_string(sourcePort);
		text_buf.push_int(destinationComponent);
		text_buf.push_string(destinationPort);

		send_message(text_buf);
	}

	//FIXME extra local_port_number is not present in the core
	public static void send_connect_listen_ack_inet_stream(final String local_port, final int local_port_number, final int remote_component, final String remote_port, final InetAddress local_address) {
		final Text_Buf text_buf = new Text_Buf();
		text_buf.push_int(MSG_CONNECT_LISTEN_ACK);
		text_buf.push_string(local_port);
		text_buf.push_int(remote_component);
		text_buf.push_string(remote_port);
		text_buf.push_int(transport_type_enum.TRANSPORT_INET_STREAM.ordinal());

		final byte temp[] = local_address.getAddress();
		final byte temp2 = (byte) local_port_number;
		text_buf.push_raw(2, new byte[]{2, 0});
		text_buf.push_raw(2, new byte[]{(byte)(local_port_number/256), (byte)(local_port_number%256)});
		text_buf.push_raw(temp.length, temp);
		text_buf.push_raw(8, new byte[8]);
		//FIXME implement

		send_message(text_buf);
	}

	public static void send_connected(final String local_port, final int remote_component, final String remote_port){
		final Text_Buf text_buf = new Text_Buf();
		text_buf.push_int(MSG_CONNECTED);
		text_buf.push_string(local_port);
		text_buf.push_int(remote_component);
		text_buf.push_string(remote_port);

		send_message(text_buf);
	}

	// in the command line receives variable argument list
	public static void send_connect_error(final String local_port, final int remote_component, final String remote_port, final String message){
		final Text_Buf text_buf = new Text_Buf();
		text_buf.push_int(MSG_CONNECT_ERROR);
		text_buf.push_string(local_port);
		text_buf.push_int(remote_component);
		text_buf.push_string(remote_port);
		text_buf.push_string(message);

		send_message(text_buf);
	}

	public static void send_disconnect_req(final int sourceComponent, final String sourcePort, final int destinationComponent, final String destinationPort) {
		final Text_Buf text_buf = new Text_Buf();
		text_buf.push_int(MSG_DISCONNECT_REQ);
		text_buf.push_int(sourceComponent);
		text_buf.push_string(sourcePort);
		text_buf.push_int(destinationComponent);
		text_buf.push_string(destinationPort);

		send_message(text_buf);
	}

	public static void send_disconnected(final String localPort, final int remoteComponent, final String remotePort) {
		final Text_Buf text_buf = new Text_Buf();
		text_buf.push_int(MSG_DISCONNECTED);
		text_buf.push_string(localPort);
		text_buf.push_int(remoteComponent);
		text_buf.push_string(remotePort);

		send_message(text_buf);
	}

	public static void send_map_req(final int sourceComponent, final String sourcePort, final String systemPort, final boolean translation) {
		final Text_Buf text_buf = new Text_Buf();
		text_buf.push_int(MSG_MAP_REQ);
		text_buf.push_int(sourceComponent);
		text_buf.push_int(translation ? 1 : 0);
		text_buf.push_string(sourcePort);
		text_buf.push_string(systemPort);

		send_message(text_buf);
	}

	public static void send_mapped(final String localPort, final String systemPort, final boolean translation) {
		final Text_Buf text_buf = new Text_Buf();
		text_buf.push_int(MSG_MAPPED);
		text_buf.push_int(translation ? 1 : 0);
		text_buf.push_string(localPort);
		text_buf.push_string(systemPort);

		send_message(text_buf);
	}

	public static void send_unmap_req(final int sourceComponent, final String sourcePort, final String systemPort, final boolean translation) {
		final Text_Buf text_buf = new Text_Buf();
		text_buf.push_int(MSG_UNMAP_REQ);
		text_buf.push_int(sourceComponent);
		text_buf.push_int(translation ? 1 : 0);
		text_buf.push_string(sourcePort);
		text_buf.push_string(systemPort);

		send_message(text_buf);
	}

	public static void send_unmapped(final String localPort, final String systemPort, final boolean translation) {
		final Text_Buf text_buf = new Text_Buf();
		text_buf.push_int(MSG_UNMAPPED);
		text_buf.push_int(translation ? 1 : 0);
		text_buf.push_string(localPort);
		text_buf.push_string(systemPort);

		send_message(text_buf);
	}

	public static void send_mtc_created() {
		final Text_Buf text_buf = new Text_Buf();
		text_buf.push_int(MSG_MTC_CREATED);

		send_message(text_buf);
	}

	public static void send_testcase_started(final String testcaseModule, final String testcaseName, final String mtc_comptype_module,
			final String mtc_comptype_name, final String system_comptype_module, final String system_comptype_name) {
		final Text_Buf text_buf = new Text_Buf();
		text_buf.push_int(MSG_TESTCASE_STARTED);
		text_buf.push_string(testcaseModule);
		text_buf.push_string(testcaseName);
		text_buf.push_string(mtc_comptype_module);
		text_buf.push_string(mtc_comptype_name);
		text_buf.push_string(system_comptype_module);
		text_buf.push_string(system_comptype_name);

		send_message(text_buf);
	}

	public static void send_testcase_finished(final TitanVerdictType.VerdictTypeEnum finalVerdict, final String reason) {
		final Text_Buf text_buf = new Text_Buf();
		text_buf.push_int(MSG_TESTCASE_FINISHED);
		text_buf.push_int(finalVerdict.ordinal());
		text_buf.push_string(reason);

		send_message(text_buf);
	}

	public static void send_mtc_ready() {
		final Text_Buf text_buf = new Text_Buf();
		text_buf.push_int(MSG_MTC_READY);

		send_message(text_buf);
	}

	public static void send_ptc_created(final int component_reference) {
		final Text_Buf text_buf = new Text_Buf();
		text_buf.push_int(MSG_PTC_CREATED);
		text_buf.push_int(component_reference);

		send_message(text_buf);
	}

	public static void prepare_stopped(final Text_Buf text_buf, final TitanVerdictType.VerdictTypeEnum final_verdict, final String return_type, final String reason) {
		text_buf.push_int(MSG_STOPPED);
		text_buf.push_int(final_verdict.getValue());
		text_buf.push_string(reason);
		text_buf.push_string(return_type);
	}

	public static void send_stopped(final TitanVerdictType.VerdictTypeEnum final_verdict, final String reason) {
		final Text_Buf text_buf = new Text_Buf();
		text_buf.push_int(MSG_STOPPED);
		text_buf.push_int(final_verdict.ordinal());
		text_buf.push_string(reason);
		// add an empty return type
		text_buf.push_string(null);

		send_message(text_buf);
	}

	public static void prepare_stopped_killed(final Text_Buf text_buf, final TitanVerdictType.VerdictTypeEnum final_verdict, final String return_type, final String reason) {
		text_buf.push_int(MSG_STOPPED_KILLED);
		text_buf.push_int(final_verdict.getValue());
		text_buf.push_string(reason);
		text_buf.push_string(return_type);
	}

	public static void send_stopped_killed(final TitanVerdictType.VerdictTypeEnum final_verdict, final String reason) {
		final Text_Buf text_buf = new Text_Buf();
		text_buf.push_int(MSG_STOPPED_KILLED);
		text_buf.push_int(final_verdict.getValue());
		text_buf.push_string(reason);
		// add an empty return type
		text_buf.push_string(null);

		send_message(text_buf);
	}

	public static void send_killed(final TitanVerdictType.VerdictTypeEnum final_verdict, final String reason) {
		final Text_Buf text_buf = new Text_Buf();
		text_buf.push_int(MSG_KILLED);
		text_buf.push_int(final_verdict.getValue());
		text_buf.push_string(reason);
		// add an empty return type
		text_buf.push_string(null);

		send_message(text_buf);
	}

	public static void send_error(final String message) {
		final Text_Buf text_buf = new Text_Buf();
		text_buf.push_int(MSG_ERROR);
		text_buf.push_string(message);

		send_message(text_buf);
	}

	public static void send_message(final Text_Buf text_buf) {
		if (!is_connected.get()) {
			throw new TtcnError("Trying to send a message to MC, but the control connection is down.");
		}

		text_buf.calculate_length();
		final byte msg[] = text_buf.get_data();
		final ByteBuffer buffer = ByteBuffer.allocate(text_buf.get_len());
		final byte temp_msg[] = new byte[text_buf.get_len()];
		System.arraycopy(msg, text_buf.get_begin(), temp_msg, 0, text_buf.get_len());
		buffer.put(temp_msg);
		buffer.flip();

		try {
			while (buffer.hasRemaining()) {
				mc_socketchannel.get().write(buffer);
			}
		} catch (IOException e) {
			throw new TtcnError(e);
		}
		//FIXME implement
	}

	private static void process_configure(final int msg_end, final boolean to_mtc) {
		switch (TTCN_Runtime.get_state()) {
		case HC_IDLE:
		case HC_ACTIVE:
		case HC_OVERLOADED:
			if (!to_mtc) {
				break;
			}
		case MTC_IDLE:
			if (to_mtc) {
				break;
			}
		default:
			incoming_buf.get().cut_message();
			send_error("Message CONFIGURE arrived in invalid state.");
			return;
		}

		TTCN_Runtime.set_state(to_mtc ? executorStateEnum.MTC_CONFIGURING : executorStateEnum.HC_CONFIGURING);
		TtcnLogger.log_configdata(ExecutorConfigdata_reason.enum_type.received__from__mc, null);

		final Text_Buf temp_incoming_buf = incoming_buf.get();
		final int config_str_len = temp_incoming_buf.pull_int().getInt();
		final int config_str_begin = temp_incoming_buf.get_pos();
		if (config_str_begin + config_str_len != msg_end) {
			temp_incoming_buf.cut_message();
			send_error("Malformed message CONFIGURE was received.");
			return;
		}

		final String config_str;
		if (config_str_len == 0) {
			config_str = "";
		} else {
			final byte[] config_bytes = new byte[config_str_len];
			final byte[] incoming_data = temp_incoming_buf.get_data();
			System.arraycopy(incoming_data, temp_incoming_buf.get_begin() + config_str_begin, config_bytes, 0, config_str_len);
			config_str = new String(config_bytes);
		}
		//FIXME process config string
		// for now assume successful processing
		boolean success = true;
		if (success) {
			try {
				Module_List.post_init_modules();
			} catch (TtcnError error) {
				TtcnLogger.log_executor_runtime(TitanLoggerApi.ExecutorRuntime_reason.enum_type.initialization__of__modules__failed);
				success = false;
			}
			
		} else {
			TtcnLogger.log_configdata(ExecutorConfigdata_reason.enum_type.processing__failed, null);
		}
		if (success) {
			send_configure_ack();
			TTCN_Runtime.set_state(to_mtc ? executorStateEnum.MTC_IDLE : executorStateEnum.HC_ACTIVE);
			TtcnLogger.log_configdata(ExecutorConfigdata_reason.enum_type.processing__succeeded, null);
		} else {
			send_configure_nak();
			TTCN_Runtime.set_state(to_mtc ? executorStateEnum.MTC_IDLE : executorStateEnum.HC_IDLE);
		}

		temp_incoming_buf.cut_message();
	}

	private static void process_create_mtc() {
		incoming_buf.get().cut_message();
		TTCN_Runtime.process_create_mtc();
	}

	private static void process_create_ptc() {
		final Text_Buf temp_incoming_buf = incoming_buf.get();

		final int component_reference = temp_incoming_buf.pull_int().getInt();
		if (component_reference < TitanComponent.FIRST_PTC_COMPREF) {
			temp_incoming_buf.cut_message();
			send_error(MessageFormat.format("Message CREATE_PTC refers to invalid component reference {0}.", component_reference));
			return;
		}

		final String component_module_name = temp_incoming_buf.pull_string();
		final String component_definition_name = temp_incoming_buf.pull_string();
		final String system_module_name = temp_incoming_buf.pull_string();
		final String system_definition_name = temp_incoming_buf.pull_string();
		if (component_module_name == null || component_definition_name == null || system_module_name == null || system_definition_name == null) {
			send_error(MessageFormat.format("Message CREATE_PTC with component reference {0} contains an invalid component type or system type.", component_reference));
		}

		final String component_name = temp_incoming_buf.pull_string();
		final boolean is_alive = temp_incoming_buf.pull_int().getInt() == 0 ? false : true;
		final String testcase_module_name = temp_incoming_buf.pull_string();
		final String testcase_definition_name = temp_incoming_buf.pull_string();
		temp_incoming_buf.cut_message();

		TTCN_Runtime.process_create_ptc(component_reference, component_module_name, component_definition_name, system_module_name, system_definition_name, component_name, is_alive, testcase_module_name, testcase_definition_name);
	}

	private static void process_kill_process() {
		final int component_reference = incoming_buf.get().pull_int().getInt();
		incoming_buf.get().cut_message();

		TTCN_Runtime.process_kill_process(component_reference);
	}

	private static void process_exit_hc() {
		incoming_buf.get().cut_message();
		TtcnLogger.log_executor_runtime(TitanLoggerApi.ExecutorRuntime_reason.enum_type.exit__requested__from__mc__hc);
		TTCN_Runtime.set_state(executorStateEnum.HC_EXIT);
	}

	private static void process_create_ack() {
		final int component_reference = incoming_buf.get().pull_int().getInt();
		incoming_buf.get().cut_message();

		TTCN_Runtime.process_create_ack(component_reference);
	}

	private static void process_start_ack() {
		incoming_buf.get().cut_message();

		switch (TTCN_Runtime.get_state()) {
		case MTC_START:
			TTCN_Runtime.set_state(executorStateEnum.MTC_TESTCASE);
			break;
		case MTC_TERMINATING_TESTCASE:
			break;
		case PTC_START:
			TTCN_Runtime.set_state(executorStateEnum.PTC_FUNCTION);
			break;
		default:
			throw new TtcnError("Internal error: Message START_ACK arrived in invalid state.");
		}
	}

	private static void process_stop() {
		incoming_buf.get().cut_message();

		switch (TTCN_Runtime.get_state()) {
		case MTC_IDLE:
			TtcnLogger.log_executor_runtime(TitanLoggerApi.ExecutorRuntime_reason.enum_type.stop__was__requested__from__mc__ignored__on__idle__mtc);
			break;
		case MTC_PAUSED:
			TtcnLogger.log_executor_runtime(TitanLoggerApi.ExecutorRuntime_reason.enum_type.stop__was__requested__from__mc);
			TTCN_Runtime.set_state(executorStateEnum.MTC_TERMINATING_EXECUTION);
			break;
		case PTC_IDLE:
		case PTC_STOPPED:
			TtcnLogger.log_executor_runtime(TitanLoggerApi.ExecutorRuntime_reason.enum_type.stop__was__requested__from__mc__ignored__on__idle__ptc);
			break;
		case PTC_EXIT:
			break;
		default:
			TtcnLogger.log_executor_runtime(TitanLoggerApi.ExecutorRuntime_reason.enum_type.stop__was__requested__from__mc);;
			TTCN_Runtime.stop_execution();
			break;
		}
	}

	private static void process_stop_ack() {
		incoming_buf.get().cut_message();

		switch (TTCN_Runtime.get_state()) {
		case MTC_STOP:
			TTCN_Runtime.set_state(executorStateEnum.MTC_TESTCASE);
			break;
		case MTC_TERMINATING_TESTCASE:
			break;
		case PTC_STOP:
			TTCN_Runtime.set_state(executorStateEnum.PTC_FUNCTION);
			break;
		default:
			throw new TtcnError("Internal error: Message STOP_ACK arrived in invalid state.");
		}
	}

	private static void process_kill_ack() {
		incoming_buf.get().cut_message();

		switch (TTCN_Runtime.get_state()) {
		case MTC_KILL:
			TTCN_Runtime.set_state(executorStateEnum.MTC_TESTCASE);
			break;
		case MTC_TERMINATING_TESTCASE:
			break;
		case PTC_KILL:
			TTCN_Runtime.set_state(executorStateEnum.PTC_FUNCTION);
			break;
		default:
			throw new TtcnError("Internal error: Message KILL_ACK arrived in invalid state.");
		}
	}

	private static void process_running() {
		final Text_Buf temp_incoming_buf = incoming_buf.get();

		final boolean answer = temp_incoming_buf.pull_int().getInt() == 0 ? false: true;
		temp_incoming_buf.cut_message();

		TTCN_Runtime.process_running(answer);
	}

	private static void process_alive() {
		final Text_Buf temp_incoming_buf = incoming_buf.get();

		final boolean answer = temp_incoming_buf.pull_int().getInt() == 0 ? false: true;
		temp_incoming_buf.cut_message();

		TTCN_Runtime.process_alive(answer);
	}

	private static void process_done_ack(final int msg_end) {
		final Text_Buf temp_incoming_buf = incoming_buf.get();

		final boolean answer = temp_incoming_buf.pull_int().getInt() == 0 ? false: true;
		final int verdict_int = temp_incoming_buf.pull_int().getInt();
		final VerdictTypeEnum ptc_verdict = TitanVerdictType.VerdictTypeEnum.values()[verdict_int];
		final String return_type = temp_incoming_buf.pull_string();
		final int return_value_begin = temp_incoming_buf.get_pos();

		try {
			TTCN_Runtime.process_done_ack(answer, ptc_verdict, return_type, temp_incoming_buf.get_data(), msg_end - return_value_begin, temp_incoming_buf.get_begin(), return_value_begin);
		} finally {
			temp_incoming_buf.cut_message();
		}
	}

	private static void process_killed_ack() {
		final Text_Buf temp_incoming_buf = incoming_buf.get();

		final boolean answer = temp_incoming_buf.pull_int().getInt() == 0 ? false: true;
		temp_incoming_buf.cut_message();

		TTCN_Runtime.process_killed_ack(answer);
	}

	private static void process_component_status_mtc(final int msg_end) {
		final Text_Buf temp_incoming_buf = incoming_buf.get();

		final int component_reference = temp_incoming_buf.pull_int().getInt();
		final boolean is_done = temp_incoming_buf.pull_int().getInt() == 0 ? false: true;
		final boolean is_killed = temp_incoming_buf.pull_int().getInt() == 0 ? false: true;
		final boolean is_any_done = temp_incoming_buf.pull_int().getInt() == 0 ? false: true;
		final boolean is_all_done = temp_incoming_buf.pull_int().getInt() == 0 ? false: true;
		final boolean is_any_killed = temp_incoming_buf.pull_int().getInt() == 0 ? false: true;
		final boolean is_all_killed = temp_incoming_buf.pull_int().getInt() == 0 ? false: true;
		if (is_done) {
			// the return type and value are valid
			final int verdict_int = temp_incoming_buf.pull_int().getInt();
			final VerdictTypeEnum ptc_verdict = TitanVerdictType.VerdictTypeEnum.values()[verdict_int];
			final String return_type = temp_incoming_buf.pull_string();
			final int return_value_begin = temp_incoming_buf.get_pos();
			try {
				TTCN_Runtime.set_component_done(component_reference, ptc_verdict, return_type, temp_incoming_buf.get_data(), msg_end - return_value_begin, temp_incoming_buf.get_begin(), return_value_begin);
			} catch (TtcnError error) {
				temp_incoming_buf.cut_message();
				throw error;
			}
		}

		if (is_killed) {
			TTCN_Runtime.set_component_killed(component_reference);
		}
		if (is_any_done) {
			TTCN_Runtime.set_component_done(TitanComponent.ANY_COMPREF, VerdictTypeEnum.NONE, null, null, 0, 0, 0);
		}
		if (is_all_done) {
			TTCN_Runtime.set_component_done(TitanComponent.ALL_COMPREF, VerdictTypeEnum.NONE, null, null, 0, 0, 0);
		}
		if (is_any_killed) {
			TTCN_Runtime.set_component_killed(TitanComponent.ANY_COMPREF);
		}
		if (is_all_killed) {
			TTCN_Runtime.set_component_killed(TitanComponent.ALL_COMPREF);
		}

		temp_incoming_buf.cut_message();
		if (!is_done && !is_killed && (component_reference != TitanComponent.NULL_COMPREF ||
				(!is_any_done && !is_all_done && !is_any_killed && !is_all_killed))) {
			throw new TtcnError("Internal error: Malformed COMPONENT_STATUS message was received.");
		}
	}

	private static void process_component_status_ptc(final int msg_end) {
		final Text_Buf temp_incoming_buf = incoming_buf.get();

		final int component_reference = temp_incoming_buf.pull_int().getInt();
		final boolean is_done = temp_incoming_buf.pull_int().getInt() == 0 ? false: true;
		final boolean is_killed = temp_incoming_buf.pull_int().getInt() == 0 ? false: true;
		if (is_done) {
			// the return type and value are valid
			final int verdict_int = temp_incoming_buf.pull_int().getInt();
			final VerdictTypeEnum ptc_verdict = TitanVerdictType.VerdictTypeEnum.values()[verdict_int];
			final String return_type = temp_incoming_buf.pull_string();
			final int return_value_begin = temp_incoming_buf.get_pos();
			try {
				TTCN_Runtime.set_component_done(component_reference, ptc_verdict, return_type, temp_incoming_buf.get_data(), msg_end - return_value_begin, temp_incoming_buf.get_begin(), return_value_begin);
			} catch (TtcnError error) {
				temp_incoming_buf.cut_message();
				throw error;
			}
		}

		if (is_killed) {
			TTCN_Runtime.set_component_killed(component_reference);
		}

		temp_incoming_buf.cut_message();
		if (!is_done && !is_killed) {
			throw new TtcnError("Internal error: Malformed COMPONENT_STATUS message was received.");
		}
	}

	private static void process_connect_listen() {
		final Text_Buf temp_incoming_buf = incoming_buf.get();

		final String local_port = temp_incoming_buf.pull_string();
		final int remote_component = temp_incoming_buf.pull_int().getInt();
		final String remote_component_name = temp_incoming_buf.pull_string();
		final String remote_port = temp_incoming_buf.pull_string();
		final int temp_transport_type = temp_incoming_buf.pull_int().getInt();

		temp_incoming_buf.cut_message();

		if (remote_component != TitanComponent.MTC_COMPREF && TitanComponent.self.get().getComponent() != remote_component) {
			TitanComponent.register_component_name(remote_component, remote_component_name);
		}

		final transport_type_enum transport_type = transport_type_enum.values()[temp_transport_type];
		TitanPort.process_connect_listen(local_port, remote_component, remote_port, transport_type);

		temp_incoming_buf.cut_message();
	}

	private static void process_connect() {
		final Text_Buf temp_incoming_buf = incoming_buf.get();

		final String local_port = temp_incoming_buf.pull_string();
		final int remote_component = temp_incoming_buf.pull_int().getInt();
		final String remote_component_name = temp_incoming_buf.pull_string();
		final String remote_port = temp_incoming_buf.pull_string();
		final int temp_transport_type = temp_incoming_buf.pull_int().getInt();

		if (remote_component != TitanComponent.MTC_COMPREF && TitanComponent.self.get().getComponent() != remote_component) {
			TitanComponent.register_component_name(remote_component, remote_component_name);
		}

		final transport_type_enum transport_type = transport_type_enum.values()[temp_transport_type];
		TitanPort.process_connect(local_port, remote_component, remote_port, transport_type, incoming_buf.get());

		temp_incoming_buf.cut_message();
	}

	private static void process_connect_ack() {
		incoming_buf.get().cut_message();

		switch (TTCN_Runtime.get_state()) {
		case MTC_CONNECT:
			TTCN_Runtime.set_state(executorStateEnum.MTC_TESTCASE);
			break;
		case MTC_TERMINATING_TESTCASE:
			break;
		case PTC_CONNECT:
			TTCN_Runtime.set_state(executorStateEnum.PTC_FUNCTION);
			break;
		default:
			throw new TtcnError("Internal error: Message CONNECT_ACK arrived in invalid state.");
		}
	}

	private static void process_disconnect() {
		final Text_Buf temp_incoming_buf = incoming_buf.get();

		final String local_port = temp_incoming_buf.pull_string();
		final int remote_component = temp_incoming_buf.pull_int().getInt();
		final String remote_port = temp_incoming_buf.pull_string();

		temp_incoming_buf.cut_message();

		TitanPort.process_disconnect(local_port, remote_component, remote_port);
	}

	private static void process_disconnect_ack() {
		incoming_buf.get().cut_message();

		switch (TTCN_Runtime.get_state()) {
		case MTC_DISCONNECT:
			TTCN_Runtime.set_state(executorStateEnum.MTC_TESTCASE);
			break;
		case MTC_TERMINATING_TESTCASE:
			break;
		case PTC_DISCONNECT:
			TTCN_Runtime.set_state(executorStateEnum.PTC_FUNCTION);
			break;
		default:
			throw new TtcnError("Internal error: Message DISCONNECT_ACK arrived in invalid state.");
		}
	}

	private static void process_map() {
		final Text_Buf temp_incoming_buf = incoming_buf.get();

		final boolean translation = temp_incoming_buf.pull_int().getInt() == 0 ? false: true;
		final String local_port = temp_incoming_buf.pull_string();
		final String system_port = temp_incoming_buf.pull_string();

		temp_incoming_buf.cut_message();

		TitanPort.map_port(local_port, system_port, false);
		if (translation) {
			TitanPort.map_port(local_port, system_port, true);
		}
		if (!TTCN_Runtime.is_single()) {
			if (translation) {
				send_mapped(system_port, local_port, translation);
			} else {
				send_mapped(local_port, system_port, translation);
			}
		}
	}

	private static void process_map_ack() {
		incoming_buf.get().cut_message();

		switch (TTCN_Runtime.get_state()) {
		case MTC_MAP:
			TTCN_Runtime.set_state(executorStateEnum.MTC_TESTCASE);
			break;
		case MTC_TERMINATING_TESTCASE:
			break;
		case PTC_MAP:
			TTCN_Runtime.set_state(executorStateEnum.PTC_FUNCTION);
			break;
		default:
			throw new TtcnError("Internal error: Message MAP_ACK arrived in invalid state.");
		}
	}

	private static void process_unmap() {
		final Text_Buf temp_incoming_buf = incoming_buf.get();

		final boolean translation = temp_incoming_buf.pull_int().getInt() == 0 ? false: true;
		final String local_port = temp_incoming_buf.pull_string();
		final String system_port = temp_incoming_buf.pull_string();

		temp_incoming_buf.cut_message();

		TitanPort.unmap_port(local_port, system_port, false);
		if (translation) {
			TitanPort.unmap_port(local_port, system_port, true);
		}
		if (!TTCN_Runtime.is_single()) {
			if (translation) {
				send_unmapped(system_port, local_port, translation);
			} else {
				send_unmapped(local_port, system_port, translation);
			}
		}
	}

	private static void process_unmap_ack() {
		incoming_buf.get().cut_message();

		switch (TTCN_Runtime.get_state()) {
		case MTC_UNMAP:
			TTCN_Runtime.set_state(executorStateEnum.MTC_TESTCASE);
			break;
		case MTC_TERMINATING_TESTCASE:
			break;
		case PTC_UNMAP:
			TTCN_Runtime.set_state(executorStateEnum.PTC_FUNCTION);
			break;
		default:
			throw new TtcnError("Internal error: Message UNMAP_ACK arrived in invalid state.");
		}
	}

	private static void process_execute_control() {
		final String module_name = incoming_buf.get().pull_string();
		incoming_buf.get().cut_message();

		if (TTCN_Runtime.get_state() != executorStateEnum.MTC_IDLE) {
			throw new TtcnError("Internal error: Message EXECUTE_CONTROL arrived in invalid state.");
		}

		TtcnLogger.log(Severity.PARALLEL_UNQUALIFIED, MessageFormat.format("Executing control part of module {0}.", module_name));

		TTCN_Runtime.set_state(executorStateEnum.MTC_CONTROLPART);

		try {
			Module_List.execute_control(module_name);
		} catch (TC_End TC_end) {
			//no operation needed
		} catch (TtcnError error) {
			//no operation needed
		}

		if (is_connected.get()) {
			send_mtc_ready();
			TTCN_Runtime.set_state(executorStateEnum.MTC_IDLE);
		} else {
			TTCN_Runtime.set_state(executorStateEnum.MTC_EXIT);
		}
	}

	private static void process_execute_testcase() {
		final String module_name = incoming_buf.get().pull_string();
		final String testcase_name = incoming_buf.get().pull_string();
		incoming_buf.get().cut_message();

		if (TTCN_Runtime.get_state() != executorStateEnum.MTC_IDLE) {
			throw new TtcnError("Internal error: Message EXECUTE_TESTCASE arrived in invalid state."); 
		}

		TtcnLogger.log_testcase_exec(testcase_name, module_name);
		TTCN_Runtime.set_state(executorStateEnum.MTC_CONTROLPART);

		try {
			if (testcase_name != null && testcase_name.length() > 0) {
				Module_List.execute_testcase(module_name, testcase_name);
			} else {
				Module_List.execute_all_testcases(module_name);
			}
		} catch (TC_End TC_end) {
			//no operation needed
		} catch (TtcnError error) {
			//no operation needed
		}
	}

	private static void process_ptc_verdict() {
		TTCN_Runtime.process_ptc_verdict(incoming_buf.get());
		incoming_buf.get().cut_message();
	}

	private static void process_exit_mtc() {
		incoming_buf.get().cut_message();
		TTCN_Runtime.log_verdict_statistics();
		TtcnLogger.log_executor_runtime(TitanLoggerApi.ExecutorRuntime_reason.enum_type.exit__requested__from__mc__mtc);
		TTCN_Runtime.set_state(executorStateEnum.MTC_EXIT);
	}

	private static void process_start() {
		final String module_name = incoming_buf.get().pull_string();
		final String definition_name = incoming_buf.get().pull_string();

		if (module_name == null || definition_name == null) {
			incoming_buf.get().cut_message();

			throw new TtcnError("Internal error: Message START contains an invalid function name.");
		}

		TTCN_Runtime.start_function(module_name, definition_name, incoming_buf.get());
	}

	private static void process_kill() {
		incoming_buf.get().cut_message();

		TTCN_Runtime.process_kill();
	}

	private static void process_error() {
		final Text_Buf temp_incoming_buf = incoming_buf.get();

		final String error_string = temp_incoming_buf.pull_string();

		temp_incoming_buf.cut_message();

		throw new TtcnError("Error message was received from MC : " + error_string);
	}

	private static void process_unsupported_message(final int msg_type, final int msg_end) {
		final Text_Buf temp_incoming_buf = incoming_buf.get();

		TtcnLogger.begin_event(Severity.WARNING_UNQUALIFIED);
		TtcnLogger.log_event_str(MessageFormat.format("Unsupported message was received from MC: type (decimal): {0}, data (hexadecimal): ", msg_type));

		final byte[] data = temp_incoming_buf.get_data();
		final int begin = temp_incoming_buf.get_begin();
		for (int i = temp_incoming_buf.get_pos(); i < msg_end; i++) {
			TtcnLogger.log_octet((char)data[begin + i]);
		}
		TtcnLogger.end_event();
		temp_incoming_buf.cut_message();
	}

	private static void process_debug_command() {
		final Text_Buf temp_incoming_buf = incoming_buf.get();

		final int command = temp_incoming_buf.pull_int().getInt();
		final int argument_count = temp_incoming_buf.pull_int().getInt();
		//FIXME process the arguments properly
		if (argument_count > 0) {
			for (int i = 0; i < argument_count; i++) {
				temp_incoming_buf.pull_string();
			}
		}
		temp_incoming_buf.cut_message();
		//FIXME implement execute_command
	}
}

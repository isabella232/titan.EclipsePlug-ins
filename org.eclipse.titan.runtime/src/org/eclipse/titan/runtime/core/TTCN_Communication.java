/******************************************************************************
 * Copyright (c) 2000-2018 Ericsson Telecom AB
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 ******************************************************************************/
package org.eclipse.titan.runtime.core;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectableChannel;
import java.nio.channels.SelectionKey;
import java.nio.channels.SocketChannel;
import java.text.MessageFormat;
import java.util.concurrent.atomic.AtomicInteger;

import org.eclipse.titan.runtime.core.Event_Handler.Channel_And_Timeout_Event_Handler;
import org.eclipse.titan.runtime.core.TTCN_Runtime.executorStateEnum;
import org.eclipse.titan.runtime.core.TitanLoggerApi.ExecutorConfigdata_reason.enum_type;

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
				AtomicInteger buf_ptr = new AtomicInteger();
				AtomicInteger buf_len = new AtomicInteger();
				incoming_buffer.get_end(buf_ptr, buf_len);

				ByteBuffer tempbuffer = ByteBuffer.allocate(1024);
				int recv_len = 0;
				try {
					recv_len = mc_channel.read(tempbuffer);
				} catch (IOException e) {
					e.printStackTrace();
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
			//mc_socket = new Socket(MC_host, MC_port);
			//mc_outputstream = new DataOutputStream(mc_socket.getOutputStream());
			mc_socketchannel.set(SocketChannel.open());
			mc_socketchannel.get().connect(new InetSocketAddress(MC_host, MC_port));
			//FIXME register
		} catch (IOException e) {
			e.printStackTrace();
			return;
		}
		//FIXME implement
		MC_Connection mc_connection = new MC_Connection(mc_socketchannel.get(), incoming_buf.get());
		try {
			mc_socketchannel.get().configureBlocking(false);
			TTCN_Snapshot.channelMap.get().put(mc_socketchannel.get(), mc_connection);
			mc_socketchannel.get().register(TTCN_Snapshot.selector.get(), SelectionKey.OP_READ);
		} catch (IOException e) {
			e.printStackTrace();
			//FIXME implement
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
				e.printStackTrace();
				//FIXME
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

		//FIXME implement
		boolean wait_flag = false;
		boolean check_overload = TTCN_Runtime.is_overloaded();
		while (incoming_buf.get().is_message()) {
			wait_flag = true;

			int msg_len = incoming_buf.get().pull_int().getInt();
			int msg_end = incoming_buf.get().get_pos() + msg_len;
			int msg_type = incoming_buf.get().pull_int().getInt();

			System.out.println("received hc message type " + msg_type);//FIXME only debug printout
			switch (msg_type) {
			case MSG_ERROR:
				process_error();
				break;
			case MSG_CONFIGURE:
				process_configure(msg_end, false);
				break;
			case MSG_CREATE_MTC:
				process_create_mtc();
				//FIXME implement
				break;
			case MSG_CREATE_PTC:
				process_create_ptc();
				//FIXME implement
				break;
			case MSG_KILL_PROCESS:
				//FIXME implement
				break;
			case MSG_EXIT_HC:
				//FIXME process_exit_hc
				break;
			case MSG_DEBUG_COMMAND:
				process_debug_command();
				break;
			default:
				//FIXME process_unsupported_message
				break;
			}
		}
		if (wait_flag) {
			//FIXME implement TTCN_Runtime.wait_terminated_processes();
		}
		if (check_overload && TTCN_Runtime.is_overloaded()) {
			//FIXME implement
		}
	}

	public static void process_all_messages_tc() {
		if (!TTCN_Runtime.is_tc()) {
			throw new TtcnError("Internal error: TTCN_Communication::process_all_messages_tc() was called in invalid state.");
		}

		//FIXME implement
		while (incoming_buf.get().is_message()) {
			int msg_len = incoming_buf.get().pull_int().getInt();
			int msg_end = incoming_buf.get().get_pos() + msg_len;
			int msg_type = incoming_buf.get().pull_int().getInt();

			System.out.println("received tc message type " + msg_type);
			// messages: MC -> TC
			switch (msg_type) {
			case MSG_ERROR:
				process_error();
				break;
			case MSG_CREATE_ACK:
				//FIXME process_create_ack();
				break;
			case MSG_START_ACK:
				//FIXME process_start_ack();
				break;
			case MSG_STOP:
				//FIXME process_stop();
				break;
			case MSG_STOP_ACK:
				//FIXME process_stop_ack();
				break;
			case MSG_KILL_ACK:
				//FIXME process_kill_ack();
				break;
			case MSG_RUNNING:
				//FIXME process_running();
				break;
			case MSG_ALIVE:
				//FIXME process_alive();
				break;
			case MSG_DONE_ACK:
				//FIXME process_done_ack();
				break;
			case MSG_KILLED_ACK:
				//FIXME process_killed_ack();
				break;
			case MSG_CANCEL_DONE:
				//FIXME
				break;
			case MSG_COMPONENT_STATUS:
				//FIXME implment
				break;
			case MSG_CONNECT_LISTEN:
				//FIXME process_connect();
				break;
			case MSG_CONNECT:
				//FIXME process_connect();
				break;
			case MSG_CONNECT_ACK:
				//FIXME process_connecT_ack();
				break;
			case MSG_DISCONNECT:
				//FIXME process_disconnect();
				break;
			case MSG_DISCONNECT_ACK:
				//FIXME process_disconnect_ack();
				break;
			case MSG_MAP:
				//FIXME process_map();
				break;
			case MSG_MAP_ACK:
				//FIXME process_map_ack();
				break;
			case MSG_UNMAP:
				//FIXME process_unmap();
				break;
			case MSG_UNMAP_ACK:
				//FIXME process_unmap_ack();
				break;
			case MSG_DEBUG_COMMAND:
				//FIXME process_debug_command();
				break;
			default:
				if (TTCN_Runtime.is_mtc()) {
					// messages: MC -> MTC
					switch(msg_type) {
					case MSG_EXECUTE_CONTROL:
						//FIXME process_execute_control();
						break;
					case MSG_EXECUTE_TESTCASE:
						//FIXME process_execute_testcase();
						break;
					case MSG_PTC_VERDICT:
						//FIXME process_ptc_verdict();
						break;
					case MSG_CONTINUE:
						//FIXME process_continue();
						break;
					case MSG_EXIT_MTC:
						//FIXME process_exit_mtc();
						break;
					case MSG_CONFIGURE:
						//FIXME process_configure(msg_end, TRUE);
						break;
					default:
						//FIXME process_unsupported_message(msg_type, msg_end);
						break;
					}
				} else {
					// messages: MC -> PTC
					switch (msg_type) {
					case MSG_START:
						//FIXME process_start();
						break;
					case MSG_KILL:
						//FIXME process_kill();
						break;
					default:
						//FIXME process_unsupported_message(msg_type, msg_end);
					}
				}
			}
			//FIXME implement
		}
		//FIXME implement
	}

	public static void send_version() {
		//FIXME implement (only temporary values for now)

		Text_Buf text_buf = new Text_Buf();
		text_buf.push_int(MSG_VERSION);
		//sending temporary data
		text_buf.push_int(TTCN_Runtime.TTCN3_MAJOR);
		text_buf.push_int(TTCN_Runtime.TTCN3_MINOR);
		text_buf.push_int(TTCN_Runtime.TTCN3_PATCHLEVEL);
		text_buf.push_int(TTCN_Runtime.TTCN3_BUILDNUMBER);

		text_buf.push_int(0);//for now send no module info
		//Module_List.push_version(text_buf);

		text_buf.push_string("node");
		text_buf.push_string("machine");
		text_buf.push_string("sysname");
		text_buf.push_string("release");
		text_buf.push_string("version");

		text_buf.push_int(2);//nof supported transports
		text_buf.push_int(0); //TRANSPORT_LOCAL
		text_buf.push_int(1); //TRANSPORT_INET_STREAM

		send_message(text_buf);
	}

	public static void send_configure_ack() {
		Text_Buf text_buf = new Text_Buf();
		text_buf.push_int(MSG_CONFIGURE_ACK);

		send_message(text_buf);
	}

	public static void send_configure_nak() {
		Text_Buf text_buf = new Text_Buf();
		text_buf.push_int(MSG_CONFIGURE_NAK);

		send_message(text_buf);
	}

	public static void send_mtc_created() {
		Text_Buf text_buf = new Text_Buf();
		text_buf.push_int(MSG_MTC_CREATED);

		send_message(text_buf);
	}

	public static void send_error(final String message) {
		Text_Buf text_buf = new Text_Buf();
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
		ByteBuffer buffer = ByteBuffer.allocate(msg.length);
		buffer.put(msg);
		buffer.flip();

		try {
//			mc_outputstream.write(msg);
//			mc_outputstream.flush();
			while (buffer.hasRemaining()) {
				mc_socketchannel.get().write(buffer);
			}
		} catch (IOException e) {
			e.printStackTrace();
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
		TtcnLogger.log_configdata(enum_type.received__from__mc, null);

		final Text_Buf temp_incoming_buf = incoming_buf.get();
		int config_str_len = temp_incoming_buf.pull_int().getInt();
		int config_str_begin = temp_incoming_buf.get_pos();
		if (config_str_begin + config_str_len != msg_end) {
			temp_incoming_buf.cut_message();
			send_error("Malformed message CONFIGURE was received.");
			return;
		}

		final String config_str;
		if (config_str_len == 0) {
			config_str = "";
		} else {
			byte[] data = new byte[config_str_len];
			System.arraycopy(temp_incoming_buf.get_data(), config_str_begin, data, 0, config_str_len);
			config_str = new String(data);
		}
		//FIXME process config string
		// for now assume successful processing
		boolean success = true;
		if (success) {
			send_configure_ack();
			TTCN_Runtime.set_state(to_mtc ? executorStateEnum.MTC_IDLE : executorStateEnum.HC_ACTIVE);
			TtcnLogger.log_configdata(enum_type.processing__succeeded, null);
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

		int component_reference = temp_incoming_buf.pull_int().getInt();
		if (component_reference < TitanComponent.FIRST_PTC_COMPREF) {
			temp_incoming_buf.cut_message();
			send_error(MessageFormat.format("Message CREATE_PTC refers to invalid component reference {0}.", component_reference));
			return;
		}

		//FIXME temporary code for debugging purposes
		String temp1 = temp_incoming_buf.pull_string();
		String temp2 = temp_incoming_buf.pull_string();
	}

	private static void process_error() {
		final Text_Buf temp_incoming_buf = incoming_buf.get();

		final String error_string = temp_incoming_buf.pull_string();

		temp_incoming_buf.cut_message();

		System.out.println("Error message was received from MC : " + error_string);
	}

	private static void process_debug_command() {
		final Text_Buf temp_incoming_buf = incoming_buf.get();

		int command = temp_incoming_buf.pull_int().getInt();
		int argument_count = temp_incoming_buf.pull_int().getInt();
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

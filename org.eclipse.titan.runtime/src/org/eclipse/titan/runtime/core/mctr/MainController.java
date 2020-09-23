/******************************************************************************
 * Copyright (c) 2000-2020 Ericsson Telecom AB
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/org/documents/epl-2.0/EPL-2.0.html
 ******************************************************************************/
package org.eclipse.titan.runtime.core.mctr;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.net.Inet4Address;
import java.net.Inet6Address;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.net.StandardSocketOptions;
import java.net.UnknownHostException;
import java.nio.ByteBuffer;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.ReentrantLock;

import org.eclipse.titan.runtime.core.NetworkHandler;
import org.eclipse.titan.runtime.core.TTCN_Communication.transport_type_enum;
import org.eclipse.titan.runtime.core.TTCN_Logger;
import org.eclipse.titan.runtime.core.TTCN_Runtime;
import org.eclipse.titan.runtime.core.Text_Buf;
import org.eclipse.titan.runtime.core.TitanCharString;
import org.eclipse.titan.runtime.core.TitanComponent;
import org.eclipse.titan.runtime.core.TitanPort.Map_Params;
import org.eclipse.titan.runtime.core.TitanVerdictType;
import org.eclipse.titan.runtime.core.TitanVerdictType.VerdictTypeEnum;
import org.eclipse.titan.runtime.core.TtcnError;
import org.eclipse.titan.runtime.core.cfgparser.CfgAnalyzer;
import org.eclipse.titan.runtime.core.cfgparser.ExecuteSectionHandler.ExecuteItem;


/**
 * TODO: lots to implement
 *
 * @author Bianka Bekefi
 */

public class MainController {

	// TODO Use the messages from the TTCN_Communication class
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

	/** For representing the global state of MC */
	public static enum mcStateEnum {
		MC_INACTIVE, MC_LISTENING, MC_LISTENING_CONFIGURED, MC_HC_CONNECTED,
		MC_CONFIGURING, MC_ACTIVE, MC_SHUTDOWN, MC_CREATING_MTC, MC_READY,
		MC_TERMINATING_MTC, MC_EXECUTING_CONTROL, MC_EXECUTING_TESTCASE,
		MC_TERMINATING_TESTCASE, MC_PAUSED, MC_RECONFIGURING
	}

	/** Possible states of a HC */
	private static enum hc_state_enum { 
		HC_IDLE, HC_CONFIGURING, HC_ACTIVE, HC_OVERLOADED,
		HC_CONFIGURING_OVERLOADED, HC_EXITING, HC_DOWN 
	}

	/** Possible states of a port connection or mapping */
	private static enum conn_state_enum {
		CONN_LISTENING, CONN_CONNECTING, CONN_CONNECTED,
		CONN_DISCONNECTING, CONN_MAPPING, CONN_MAPPED, CONN_UNMAPPING 
	}

	/** Possible states of a TC (MTC or PTC) */
	public static enum tc_state_enum {
		TC_INITIAL, TC_IDLE, TC_CREATE, TC_START, TC_STOP, TC_KILL,
		TC_CONNECT, TC_DISCONNECT, TC_MAP, TC_UNMAP, TC_STOPPING, TC_EXITING, TC_EXITED,
		MTC_CONTROLPART, MTC_TESTCASE, MTC_ALL_COMPONENT_STOP,
		MTC_ALL_COMPONENT_KILL, MTC_TERMINATING_TESTCASE, MTC_PAUSED,
		PTC_FUNCTION, PTC_STARTING, PTC_STOPPED, PTC_KILLING, PTC_STOPPING_KILLING,
		PTC_STALE, TC_SYSTEM, MTC_CONFIGURING 
	}

	/** Possible reasons for waking up the MC thread from the main thread. */
	private static enum wakeup_reason_t {
		REASON_NOTHING, REASON_SHUTDOWN, REASON_MTC_KILL_TIMER
	}
	
	/** Data structure for representing a port connection */
	static class PortConnection {
		conn_state_enum conn_state;
		transport_type_enum transport_type;
		int comp_ref;
		String port_name;
		PortConnection next;
		PortConnection prev;
		ComponentStruct component;
		List<ComponentStruct> components;
		int headComp;
		String headPort;
		int tailComp;
		String tailPort;
		RequestorStruct requestors;
	}
	
	/** Data structure for describing the component location constraints */
	static class HostGroupStruct {
		String group_name;
		boolean has_all_hosts;
		boolean has_all_components;
		List<Host> host_members;
		List<ComponentStruct> assigned_components;
	}
	
	/** Data structure for each host (and the corresponding HC) */
	public static class Host {
		SocketChannel socket;
		SocketAddress address;
		public hc_state_enum hc_state;
		public List<ComponentStruct> components;
		boolean transport_supported[];
		public String hostname;
		public String hostname_local;
		String machine_type;
		public String system_name;
		public String system_release;
		public String system_version;

		Host(final SocketChannel sc) {
			socket = sc;
			components = new ArrayList<ComponentStruct>();
			transport_supported = new boolean[transport_type_enum.TRANSPORT_NUM.ordinal()];
		}

		void addComponent(final ComponentStruct comp) {
			components.add(comp);
		}

	}
	
	public static class QualifiedName {
		public String module_name;
		public String definition_name;

		public QualifiedName(final String module_name, final String definition_name) {
			this.module_name = module_name;
			this.definition_name = definition_name;
		}

	}
	
	/** Data structure for each TC */
	public static class ComponentStruct {
		public int comp_ref;
		public QualifiedName comp_type;
		public String comp_name;
		String log_source;
		Host comp_location;
		public tc_state_enum tc_state;
		public VerdictTypeEnum local_verdict;
		String verdict_reason;
		int tc_fd;
		Text_Buf text_buf;
		public QualifiedName tc_fn_name;
		String return_type;
		byte[] return_value;
		boolean is_alive;
		boolean stop_requested;
		boolean process_killed;
		// int arg;
		byte[] arg;
		ComponentStruct create_requestor;
		ComponentStruct start_requestor;
		RequestorStruct cancel_done_sent_to;
		RequestorStruct stop_requestors;
		RequestorStruct kill_requestors;
		List<PortConnection> conn_head_list;
		List<PortConnection> conn_tail_list;
		RequestorStruct done_requestors;
		RequestorStruct killed_requestors;
		RequestorStruct cancel_done_sent_for;

		ComponentStruct(final Text_Buf tb) {
			text_buf = tb;
		}

	}
	
	/** Structure for timers */
	static class TimerStruct {
		double expiration;
		ComponentStruct component;
	}

	/** Container of test components (when a pending operation can be requested by several components) */
	static class RequestorStruct {
		int n_components;
		ComponentStruct comp;
		List<ComponentStruct> components;
	}
	
	private static UserInterface ui;
	private static NetworkHandler nh = new NetworkHandler();

	private static mcStateEnum mc_state;
	private static String mc_hostname;
	
	/** Use ServerSocketChannel and Selector for non-blocking I/O instead of file descriptor*/
	private static ServerSocketChannel mc_channel; 
	private static Selector mc_selector;
	
	private static ThreadLocal<Text_Buf> incoming_buf = new ThreadLocal<Text_Buf>() {
		@Override
		protected Text_Buf initialValue() {
			return new Text_Buf();
		}
	};


	private static ThreadLocal<Integer> n_hosts = new ThreadLocal<Integer>() {
		@Override
		protected Integer initialValue() {
			return Integer.valueOf(0);
		}
	};

	private static ThreadLocal<String> config_str = new ThreadLocal<String>() {
		@Override
		protected String initialValue() {
			return "";
		};
	};

	private static ThreadLocal<Boolean> any_component_done_requested = new ThreadLocal<Boolean>() {
		@Override
		protected Boolean initialValue() {
			return false;
		}
	};

	private static ThreadLocal<Boolean> any_component_done_sent = new ThreadLocal<Boolean>() {
		@Override
		protected Boolean initialValue() {
			return false;
		}
	};

	private static ThreadLocal<Boolean> all_component_done_requested = new ThreadLocal<Boolean>() {
		@Override
		protected Boolean initialValue() {
			return false;
		}
	};

	private static ThreadLocal<Boolean> any_component_killed_requested = new ThreadLocal<Boolean>() {
		@Override
		protected Boolean initialValue() {
			return false;
		}
	};

	private static ThreadLocal<Boolean> all_component_killed_requested = new ThreadLocal<Boolean>() {
		@Override
		protected Boolean initialValue() {
			return false;
		}
	};

	private static ThreadLocal<Boolean> stop_requested = new ThreadLocal<Boolean>() {
		@Override
		protected Boolean initialValue() {
			return false;
		}
	};

	private static ThreadLocal<Boolean> stop_after_tc = new ThreadLocal<Boolean>() {
		@Override
		protected Boolean initialValue() {
			return false;
		}
	};

	private static int next_comp_ref;
	private static int tc_first_comp_ref;
	private static List<Host> hosts;
	private static List<HostGroupStruct> host_groups = new ArrayList<HostGroupStruct>();
	private static List<ExecuteItem> executeItems;
	

	private static ThreadLocal<CfgAnalyzer> cfgAnalyzer = new ThreadLocal<CfgAnalyzer>() {
		@Override
		protected CfgAnalyzer initialValue() {
			return null;
		}
	};

	private static Map<Integer, ComponentStruct> components;

	private static double kill_timer = 0.0;
	private static ReentrantLock mutex;

	private static ComponentStruct mtc;
	private static ComponentStruct ptc;
	private static ComponentStruct system;

	public static void initialize(final UserInterface par_ui, final int par_max_ptcs) {
		ui = par_ui;

		//max_ptcs = par_max_ptcs;

		mc_state = mcStateEnum.MC_INACTIVE;

		try {
			mc_hostname = InetAddress.getLocalHost().getHostName();
		} catch (UnknownHostException e) {
			throw new TtcnError(e);
		}
		mc_hostname = String.format("MC@%s", mc_hostname);

		host_groups = null;
		//all_components_assigned = false;

		hosts = null;
		config_str.set(null);

		//version_known = FALSE;
		//modules = NULL;

		//n_components = 0;
		//n_active_ptcs = 0;
		components = null;
		mtc = null;
		system = null;
		//debugger_active_tc = NULL;
		next_comp_ref = TitanComponent.FIRST_PTC_COMPREF;

		stop_after_tc.set(false);
		stop_requested.set(false);

		kill_timer = 10.0;
		mutex = new ReentrantLock();
	}

	public static void terminate() {
		//FIXME implement
	}

	private static void lock() {
		//TODO handle error
		mutex.lock();
	}

	private static void unlock() {
		//TODO handle error
		mutex.unlock();
	}

	private static void error(final String message) {
		unlock();
		ui.error(0, message);
		lock();
	}

	private static void notify(final String message) {
		final long timestamp = System.currentTimeMillis();
		notify(timestamp, mc_hostname,  TTCN_Logger.Severity.EXECUTOR_UNQUALIFIED.ordinal(), message);
	}

	private static void notify(final long timestamp, final String source, final int severity, final String message) {
		unlock();
		ui.notify(timestamp, source, severity, message);
		lock();
	}

	private static void status_change() {
		unlock();
		ui.status_change();
		lock();
	}

	private static void fatal_error(final String message) {
		//FIXME implement fatal_error
	}

	public static void add_host(final String group_name, final String host_name) {
		if (mc_state != mcStateEnum.MC_INACTIVE) {
			throw new TtcnError("MainController.add_host: called in wrong state.");
		}

		HostGroupStruct group = add_host_group(group_name);
		if (host_name != null) {
			if (group.has_all_hosts) {
				//System.err ?
				throw new TtcnError(MessageFormat.format("Redundant member `{0}' was ignored in host group `{1}'. All hosts (`*') are already the members of the group.", host_name, group_name));
			} else {
				/*if (group.host_members.contains()) {

				}*/
			}
		}

		//TODO: implement
	}

	private static HostGroupStruct add_host_group(final String group_name) {
		if (host_groups == null) {
			host_groups = new ArrayList<HostGroupStruct>();
			HostGroupStruct new_group = new HostGroupStruct();

			new_group.group_name = group_name;
			new_group.has_all_hosts = false;
			new_group.has_all_components = false;
			new_group.host_members = new ArrayList<Host>();

			host_groups.add(new_group);
			return new_group;
		} else {
			for (int i = 0; i < host_groups.size(); i++) {
				if (host_groups.get(i).group_name.equals(group_name)) {
					return host_groups.get(i);
				}
			}
			HostGroupStruct new_group = new HostGroupStruct();

			new_group.group_name = group_name;
			new_group.has_all_hosts = false;
			new_group.has_all_components = false;

			host_groups.add(new_group);
			return new_group;
		}
	}

	public static int start_session(final String local_address, int tcp_port) {
		lock();
		try {
			if (mc_state != mcStateEnum.MC_INACTIVE) {
				error("MainController.start_session: called in wrong state.");

				return 0;
			}

			nh.set_family(local_address, tcp_port);

			try {
				mc_channel = ServerSocketChannel.open();
			} catch (IOException e) {
				error(MessageFormat.format("Server socket creation failed: {0}\n", e.getMessage()));
				//clean up?
				return 0;
			}

			try {
				mc_channel.setOption(StandardSocketOptions.SO_REUSEADDR, true);
			} catch (IOException e) {
				error(MessageFormat.format("SO_REUSEADDR failed on server socket: {0}", e.getMessage()));
				//clean up?
				return 0;
			}

			//TCP_NODELAY not supported for server sockets in Java

			try {
				mc_channel.bind(nh.get_addr(), 10);
			} catch (IOException e) {
				if (local_address == null || local_address.isEmpty()) {
					error(MessageFormat.format("Binding server socket to TCP port {0,number,#} failed: {1}\n", tcp_port, e.getMessage()));
					//clean up?
					return 0;
				} else {
					error(MessageFormat.format("Binding server socket to IP address {0} and TCP port {1,number,#} failed: {2}\n", local_address, tcp_port, e.getMessage()));
					//clean up?
					return 0;
				}
			}
			//declare hosts list?

			hosts = new ArrayList<Host>();
			try {
				mc_state = mcStateEnum.MC_LISTENING;
				notify(MessageFormat.format("Listening on IP address {0} and TCP port {1,number,#}.\n",
						((InetSocketAddress)mc_channel.getLocalAddress()).getAddress().getHostAddress(), ((InetSocketAddress)mc_channel.getLocalAddress()).getPort()));
				tcp_port = ((InetSocketAddress)mc_channel.getLocalAddress()).getPort();
				SocketChannel sc = mc_channel.accept();
				Host host = new Host(sc);
				hosts.add(host);
				host.address = sc.getRemoteAddress();
				host.hc_state = hc_state_enum.HC_IDLE;

				process_version(host);
				notify(MessageFormat.format("New HC connected from {0} [{1}]. : {2} {3} on {4}.", host.hostname, host.socket.getRemoteAddress().toString(), host.system_name, host.system_release,
						host.machine_type));

				mc_state = mcStateEnum.MC_ACTIVE;

			} catch (IOException e) {
				if (local_address == null || local_address.isEmpty()) {
					error(MessageFormat.format("Listening on TCP port {0,number,#} failed: {1}\n", tcp_port, e.getMessage()));
					//clean up?
					return 0;
				} else {
					error(MessageFormat.format("Listening on IP address {0} and TCP port {1,number,#} failed: {2}\n", local_address, tcp_port, e.getMessage()));
					//clean up?
					return 0;
				}
			}

			//FIXME start background thread listening for connections and replies.
			//FIXME notification about localAddress
			ui.status_change();

			return tcp_port;
		} finally {
			unlock();
		}

	}

	public static void set_kill_timer(final Double timer_val) {
		switch (mc_state) {
		case MC_INACTIVE:
		case MC_LISTENING:
		case MC_HC_CONNECTED:
		case MC_RECONFIGURING:
			if (timer_val < 0.0) {
				System.err.println("MainController.set_kill_timer: setting a negative kill timer value.");
			} else {
				kill_timer = timer_val;
			}
			break;
		default:
			System.err.println("MainController.set_kill_timer: called in wrong state.");
			break;
		}
	}

	public static mcStateEnum get_state() {
		return mc_state;
	}

	public static boolean get_stop_after_testcase() {
		return stop_after_tc.get();
	}

	public static int get_nof_hosts() {
		return hosts.size();
	}

	public static Host get_host_data(final int host_index) {
		mutex.lock();
		return hosts.get(host_index);
	}

	public static void release_data() {
		mutex.unlock();
	}

	public static String get_mc_state_name(final mcStateEnum state) {
		switch (state) {
		case MC_INACTIVE:
			return "inactive";
		case MC_LISTENING:
			return "listening";
		case MC_LISTENING_CONFIGURED:
			return "listening (configured)";
		case MC_HC_CONNECTED:
			return "HC connected";
		case MC_CONFIGURING:
			return "configuring...";
		case MC_ACTIVE:
			return "active";
		case MC_CREATING_MTC:
			return "creating MTC...";
		case MC_TERMINATING_MTC:
			return "terminating MTC...";
		case MC_READY:
			return "ready";
		case MC_EXECUTING_CONTROL:
			return "executing control part";
		case MC_EXECUTING_TESTCASE:
			return "executing testcase";
		case MC_TERMINATING_TESTCASE:
			return "terminating testcase...";
		case MC_PAUSED:
			return "paused after testcase";
		case MC_SHUTDOWN:
			return "shutting down...";
		default:
			return "unknown/transient";
		}
	}

	public static String get_hc_state_name(final hc_state_enum state) {
		switch (state) {
		case HC_IDLE:
			return "not configured";
		case HC_CONFIGURING:
		case HC_CONFIGURING_OVERLOADED:
			return "being configured";
		case HC_ACTIVE:
			return "ready";
		case HC_OVERLOADED:
			return "overloaded";
		case HC_DOWN:
			return "down";
		default:
			return "unknown/transient";
		}
	}

	public static String get_tc_state_name(final tc_state_enum state) {
		switch (state) {
		case TC_INITIAL:
			return "being created";
		case TC_IDLE:
			return "inactive - waiting for start";
		case TC_CREATE:
			return "executing create operation";
		case TC_START:
			return "executing component start operation";
		case TC_STOP:
		case MTC_ALL_COMPONENT_STOP:
			return "executing component stop operation";
		case TC_KILL:
		case MTC_ALL_COMPONENT_KILL:
			return "executing kill operation";
		case TC_CONNECT:
			return "executing connect operation";
		case TC_DISCONNECT:
			return "executing disconnect operation";
		case TC_MAP:
			return "executing map operation";
		case TC_UNMAP:
			return "executing unmap operation";
		case TC_STOPPING:
			return "being stopped";
		case TC_EXITING:
			return "terminated";
		case TC_EXITED:
			return "exited";
		case MTC_CONTROLPART:
			return "executing control part";
		case MTC_TESTCASE:
			return "executing testcase";
		case MTC_TERMINATING_TESTCASE:
			return "terminating testcase";
		case MTC_PAUSED:
			return "paused";
		case PTC_FUNCTION:
			return "executing function";
		case PTC_STARTING:
			return "being started";
		case PTC_STOPPED:
			return "stopped - waiting for re-start";
		case PTC_KILLING:
		case PTC_STOPPING_KILLING:
			return "being killed";
		default:
			return "unknown/transient";
		}
	}

	//Temporary
	public static List<Host> get_hosts() {
		return hosts;
	}

	public static synchronized void create_mtc(final Host host) {
		mutex.lock();
		if (mc_state != mcStateEnum.MC_ACTIVE) {
			System.out.println("MainController.create_mtc: called in wrong state.");
			return;
		}

		switch (host.hc_state) {
		case HC_OVERLOADED:
			System.out.println("HC on host"+host.hostname+" reported overload. Trying to create MTC there anyway.");
		case HC_ACTIVE:
			break;
		default:
			// TODO error, message in MainController::create_mtc
			return;
		}

		System.out.println("Creating MTC on host "+host.hostname+".");
		send_create_mtc(host);

		mtc = new ComponentStruct(new Text_Buf());
		mtc.comp_ref = TitanComponent.MTC_COMPREF;
		mtc.comp_name = "MTC";
		mtc.tc_state = tc_state_enum.TC_INITIAL;
		mtc.local_verdict = VerdictTypeEnum.NONE;
		mtc.comp_location = host;
		host.addComponent(mtc);
		components = new HashMap<Integer, ComponentStruct>();
		components.put(mtc.comp_ref, mtc);
		mtc.done_requestors = init_requestors(null);
		mtc.killed_requestors = init_requestors(null);
		mtc.cancel_done_sent_for = init_requestors(null);
		mtc.conn_head_list = new ArrayList<PortConnection>();
		mtc.conn_tail_list = new ArrayList<PortConnection>();

		system = new ComponentStruct(new Text_Buf());
		system.comp_ref = TitanComponent.SYSTEM_COMPREF;
		system.comp_name = "SYSTEM";
		system.tc_state = tc_state_enum.TC_SYSTEM;
		system.local_verdict = VerdictTypeEnum.NONE;
		components.put(system.comp_ref, system);
		system.done_requestors = init_requestors(null);
		system.killed_requestors = init_requestors(null);
		system.cancel_done_sent_for = init_requestors(null);
		system.conn_head_list = new ArrayList<PortConnection>();
		system.conn_tail_list = new ArrayList<PortConnection>();

		mc_state = mcStateEnum.MC_CREATING_MTC;

		final Thread MTC = new Thread() {

			@Override
			public void run() {
				connect_mtc();
			}

		};

		MTC.start();
	}

	private synchronized static void connect_mtc() {
		SocketChannel sc;
		try {
			sc = mc_channel.accept();
			final Host mtcHost = new Host(sc);
			mtcHost.address = sc.getLocalAddress();
			setup_host(mtcHost);
			mtc.comp_location = mtcHost;
			handle_unknown_data(mtcHost);
		} catch (IOException e) {
			final StringWriter error = new StringWriter();
			e.printStackTrace(new PrintWriter(error));
			throw new TtcnError("Sending data on the control connection to HC failed.");
		}
	}


	private static void handle_unknown_data(final Host mtc) {
		final Text_Buf local_incoming_buf = incoming_buf.get();
		receiveMessage(mtc);

		do {
			final int msg_len = local_incoming_buf.pull_int().get_int();
			final int msg_type = local_incoming_buf.pull_int().get_int();
			boolean process_more_messages = false;
			switch (msg_type) {
			case MSG_ERROR:
				process_error(mtc);
				process_more_messages = true;
				break;
			case MSG_LOG:
				process_log(mtc);
				process_more_messages = true;
				break;
			case MSG_VERSION:
				process_version(mtc);
				break;
			case MSG_MTC_CREATED:
				process_mtc_created(mtc);
				break;
			case MSG_PTC_CREATED:
				process_ptc_created(mtc);
				break;
			default:
				// TODO error, message in MainController::handle_unknown_data
			}
			if (process_more_messages) {
				local_incoming_buf.cut_message();
				receiveMessage(mtc);
			} else {
				break;
			}
		} while (local_incoming_buf.is_message());
		// TODO
	}

	private static void process_error(final ComponentStruct tc) {
		final Text_Buf text_buf = incoming_buf.get();
		final String reason = text_buf.pull_string();
		text_buf.cut_message();
		if (tc.equals(mtc)) {
			// TODO error, message in MainController::process_error
		} else {
			System.out.println(MessageFormat.format("Error message was received from PTC {0} at {1} [{2}]: {3}",
					tc.comp_ref, tc.comp_location.hostname, tc.comp_location.address, reason));
		}
	}

	private static void process_error(final Host hc) {
		final Text_Buf text_buf = incoming_buf.get();
		final String reason = text_buf.pull_string();
		text_buf.cut_message();
		// TODO error, message in MainController::process_error
	}

	private static void connect_ptc() {
		SocketChannel sc;
		try {
			sc = mc_channel.accept();
			final Host ptcHost = new Host(sc);
			ptcHost.address = sc.getLocalAddress();
			setup_host(ptcHost);
			ptc.comp_location = ptcHost;
			handle_unknown_data(ptcHost);
		} catch (IOException e) {
			final StringWriter error = new StringWriter();
			e.printStackTrace(new PrintWriter(error));
			throw new TtcnError("Sending data on the control connection to HC failed.");
		}
	}

	private static void send_create_mtc(final Host host) {
		final Text_Buf text_buf = new Text_Buf();
		text_buf.push_int(MSG_CREATE_MTC);
		send_message(host, text_buf);
	}

	public static void printWelcome() {
		System.out.printf("\n"+
				"*************************************************************************\n"+
				"* TTCN-3 Test Executor - Main Controller 2                              *\n"+
				"* Version: %-40s                     *\n"+
				"* Copyright (c) 2000-2020 Ericsson Telecom AB                           *\n"+
				"* All rights reserved. This program and the accompanying materials      *\n"+
				"* are made available under the terms of the Eclipse Public License v2.0 *\n"+
				"* which accompanies this distribution, and is available at              *\n"+
				"* https://www.eclipse.org/org/documents/epl-2.0/EPL-2.0.html            *\n"+
				"*************************************************************************\n"+
				"\n", TTCN_Runtime.PRODUCT_NUMBER);
	}

	//original: Cli::printUsage
	//  This is the titan.core solution:
	//	public static void printUsage() {
	//		System.err.printf(
	//				"TTCN-3 Test Executor - Main Controller 2\n"+
	//			    "Version: " + TTCN_Runtime.PRODUCT_NUMBER + "\n\n"+
	//			    "usage: mctr_cli [configuration_file]\n" +
	//			    "where: the optional 'configuration_file' parameter specifies the name "+
	//			    "and\nlocation of the main controller configuration file"+
	//			    "\n");
	//	}

	//This is the temporal solution:
	public static void printUsage() {
		System.err.printf(
				"TTCN-3 Test Executor - Main Controller 2\n"+
						"Version: " + TTCN_Runtime.PRODUCT_NUMBER + "\n\n"+
						"usage: mctr_cli configuration_file\n" +
						"where: the 'configuration_file' parameter specifies the name and \n"+
				"location of the main controller configuration file\n");
	}

	private static void handle_hc_data(final Host hc) {
		final Text_Buf local_incoming_buf = incoming_buf.get();
		boolean error_flag = false;
		receiveMessage(hc);
		do {
			final int msg_len = local_incoming_buf.pull_int().get_int();
			final int msg_type = local_incoming_buf.pull_int().get_int();
			switch (msg_type) {
			case MSG_CONFIGURE_ACK:
				process_configure_ack(hc);
				break;
			case MSG_ERROR:
				process_error(hc);
				break;
			case MSG_CONFIGURE_NAK:
				process_configure_nak(hc);
				break;
			case MSG_CREATE_NAK:
				// TODO
				break;
			case MSG_LOG:
				process_log(hc);
				break;
			case MSG_HC_READY:
				process_hc_ready(hc);
				break;
			case MSG_DEBUG_RETURN_VALUE:
				// TODO
				break;
			default:
				// TODO error, message in MainController::handle_hc_data
				error_flag = true;
			}
			if (error_flag) {
				break;
			}
			local_incoming_buf.cut_message();
			if (msg_type != MSG_CONFIGURE_ACK) {
				receiveMessage(hc);
			}
		} while (local_incoming_buf.is_message());
		// TODO

	}

	private static void process_configure_nak(final Host hc) {
		incoming_buf.get().cut_message();
		switch(hc.hc_state) {
		case HC_CONFIGURING:
		case HC_CONFIGURING_OVERLOADED:
			hc.hc_state = hc_state_enum.HC_IDLE;
			break;
		default:
			send_error(hc, "Unexpected message CONFIGURE_NAK was received.");
			return;
		}

		if (mc_state == mcStateEnum.MC_CONFIGURING || mc_state == mcStateEnum.MC_RECONFIGURING) {
			check_all_hc_configured();
		} else {
			System.out.println("Processing of configuration file failed on host "+hc.hostname+".");
		}
	}

	private static void process_hc_ready(final Host hc) {
		switch(hc.hc_state) {
		case HC_OVERLOADED:
			hc.hc_state = hc_state_enum.HC_ACTIVE;
			break;
		case HC_CONFIGURING_OVERLOADED:
			hc.hc_state = hc_state_enum.HC_CONFIGURING;
			break;
		default:
			send_error(hc, "Unexpected message HC_READY was received.");
			return;
		}
		System.out.println("Host "+hc.hostname+" is no more overloaded.");
		incoming_buf.get().cut_message();
	}

	public static void send_error(final Host hc, final String reason) {
		final Text_Buf text_buf = new Text_Buf();
		text_buf.push_int(MSG_ERROR);
		text_buf.push_string(reason);
		send_message(hc, text_buf);
	}


	private static void process_create_req(final ComponentStruct tc) {
		if (!request_allowed(mtc, "CREATE_REQ")) {
			return;
		}

		final Text_Buf text_buf = incoming_buf.get();

		final String componentTypeModule = text_buf.pull_string();
		final String componentTypeName = text_buf.pull_string();
		final String componentName = text_buf.pull_string();
		final String componentLocation = text_buf.pull_string();
		final int isAlive = text_buf.pull_int().get_int();
		final int seconds = text_buf.pull_int().get_int();
		final int miliseconds = text_buf.pull_int().get_int();
		text_buf.cut_message();


		// FIXME choose location
		final Host ptcLoc = choose_ptc_location(componentTypeName, componentName, componentLocation);
		if (ptcLoc == null) {
			if (!is_hc_in_state(hc_state_enum.HC_ACTIVE)) {
				send_error(tc.comp_location, "There is no active HC connection. Create operation cannot be performed.");
			} else {
				String compData = "component type: "+componentTypeModule+"."+componentTypeName;
				if (componentName != null) {
					compData = compData+", name: "+componentName;
				}
				if (componentLocation != null) {
					compData = compData+", location: "+componentLocation;
				}
				send_error(tc.comp_location, MessageFormat.format("No suitable host was found to create a new PTC ({0}).", compData));
			}
			return;
		}

		tc.tc_state = tc_state_enum.TC_CREATE;

		ptc = new ComponentStruct(new Text_Buf());
		ptc.comp_ref = next_comp_ref++;
		ptc.comp_name = componentName;
		ptc.comp_location = ptcLoc;
		ptc.tc_state = tc_state_enum.TC_INITIAL;
		ptc.local_verdict = VerdictTypeEnum.NONE;
		ptc.is_alive = (isAlive == 1);
		ptc.create_requestor = tc;
		ptc.done_requestors = init_requestors(null);
		ptc.killed_requestors = init_requestors(null);
		ptc.cancel_done_sent_for = init_requestors(null);
		ptc.conn_head_list = new ArrayList<PortConnection>();
		ptc.conn_tail_list = new ArrayList<PortConnection>();

		components.put(ptc.comp_ref, ptc);
		ptcLoc.addComponent(ptc);

		send_create_ptc(ptcLoc, ptc.comp_ref, componentTypeModule, componentTypeName, system.comp_type,
				componentName, isAlive, mtc.tc_fn_name, seconds, miliseconds);

		final Thread PTC = new Thread() {

			@Override
			public void run() {
				connect_ptc();
			}

		};

		PTC.start();
	}


	// FIXME
	private static Host choose_ptc_location(final String componentTypeName, final String componentName, final String componentLocation) {
		final Host location = null;
		for (final Host h : hosts) {
			if (!h.equals(mtc.comp_location)) {
				return h;
			}
		}
		return null;
	}


	boolean set_has_string(final Set<String> set, final String str) {
		if (str == null) {
			return false;
		}
		for (final String element : set) {
			final int result = element.compareTo(str);
			if (result == 0) {
				return true;
			} else if (result > 0) {
				break;
			}
		}
		return false;
	}


	private static void send_create_ptc(final Host host, final int comp_ref, final String componentTypeModule, final String componentTypeName,
			final QualifiedName comp_type, final String componentName, final int isAlive, final QualifiedName tc_fn_name, final int seconds,
			final int miliseconds) {
		final Text_Buf text_buf = new Text_Buf();
		text_buf.push_int(MSG_CREATE_PTC);
		text_buf.push_int(comp_ref);
		text_buf.push_string(componentTypeModule);
		text_buf.push_string(componentTypeName);
		text_buf.push_string(comp_type.module_name);
		text_buf.push_string(comp_type.definition_name);
		text_buf.push_string(componentName);
		text_buf.push_int(isAlive);
		text_buf.push_string(mtc.tc_fn_name.module_name);
		text_buf.push_string(mtc.tc_fn_name.definition_name);
		text_buf.push_int(seconds);
		text_buf.push_int(miliseconds);

		send_message(host, text_buf);
	}

	private static void process_configure_ack(final Host hc) {
		switch (hc.hc_state) {
		case HC_CONFIGURING:
			hc.hc_state = hc_state_enum.HC_ACTIVE;
			break;
		case HC_CONFIGURING_OVERLOADED:
			hc.hc_state = hc_state_enum.HC_OVERLOADED;
			break;
		default:
			send_error(hc, "Unexpected message CONFIGURE_ACK was received.");
		}

		if (mc_state == mcStateEnum.MC_CONFIGURING || mc_state == mcStateEnum.MC_RECONFIGURING) {
			check_all_hc_configured();
		} else {
			System.out.println("Host "+hc.hostname+" was configured successfully.");
		}
		incoming_buf.get().cut_message();
	}

	private static void check_all_hc_configured() {
		final boolean reconf = (mc_state == mcStateEnum.MC_RECONFIGURING);
		if (is_hc_in_state(hc_state_enum.HC_CONFIGURING) ||
				is_hc_in_state(hc_state_enum.HC_CONFIGURING_OVERLOADED)) {
			return;
		}

		if (is_hc_in_state(hc_state_enum.HC_IDLE)) {
			mc_state = reconf ? mcStateEnum.MC_READY : mcStateEnum.MC_HC_CONNECTED;
			// TODO error , message in MainController::check_all_hc_configured
		} else if (is_hc_in_state(hc_state_enum.HC_ACTIVE) || is_hc_in_state(hc_state_enum.HC_OVERLOADED)) {
			System.out.println("Configuration file was processed on all HCs.");
			mc_state = reconf ? mcStateEnum.MC_READY : mcStateEnum.MC_ACTIVE;
		} else {
			mc_state = mcStateEnum.MC_LISTENING;
			//TODO error , message in MainController::check_all_hc_configured
		}

	}

	private static boolean is_hc_in_state(final hc_state_enum checked_state) {
		for (int i = 0; i < hosts.size(); i++) {
			if (hosts.get(i).hc_state == checked_state) {
				return true;
			}
		}
		return false;
	}

	public static void receiveMessage(final Host hc) {
		final Text_Buf local_incoming_buf = incoming_buf.get();
		final AtomicInteger buf_ptr = new AtomicInteger();
		final AtomicInteger buf_len = new AtomicInteger();
		local_incoming_buf.get_end(buf_ptr, buf_len);

		final ByteBuffer tempbuffer = ByteBuffer.allocate(1024);
		int recv_len = 0;
		try {
			recv_len = hc.socket.read(tempbuffer);
		} catch (IOException e) {
			e.printStackTrace();
			throw new TtcnError(e);
		}
		if (recv_len > 0) {
			local_incoming_buf.push_raw(recv_len, tempbuffer.array());
		}
	}

	public static void process_version(final Host hc) {
		receiveMessage(hc);
		final Text_Buf local_incoming_buf = incoming_buf.get();
		final int msg_len = local_incoming_buf.pull_int().get_int();
		final int msg_type = local_incoming_buf.pull_int().get_int();

		if (msg_type == MSG_VERSION) {
			for (int i = 0; i < 4; i++) {
				local_incoming_buf.pull_int();
			}

			final int modules_size = local_incoming_buf.pull_int().get_int();
			for (int i = 0; i < modules_size; i++) {
				local_incoming_buf.pull_string();
				final int value = local_incoming_buf.pull_int().get_int();
				final byte[] data = new byte[16];
				if (value == 16) {
					local_incoming_buf.pull_raw(16, data);
				}
			}

			add_new_host(hc);
			local_incoming_buf.cut_message();
		}
	}

	private static void add_new_host(final Host hc) {
		final Text_Buf text_buf = incoming_buf.get();
		hc.hostname_local = text_buf.pull_string();
		//FIXME hostname should be calculated
		hc.hostname = hc.hostname_local;
		hc.machine_type = text_buf.pull_string();
		hc.system_name = text_buf.pull_string();
		hc.system_release = text_buf.pull_string();
		hc.system_version = text_buf.pull_string();

		for (int i = 0; i < transport_type_enum.TRANSPORT_NUM.ordinal(); i++) {
			hc.transport_supported[i] = false;
		}

		final int n_supported_transports = text_buf.pull_int().get_int();
		for (int i = 0; i < n_supported_transports; i++) {
			final int transport_type = text_buf.pull_int().get_int();
			if (transport_type >= 0 && transport_type < transport_type_enum.TRANSPORT_NUM.ordinal()) {
				if (hc.transport_supported[transport_type]) {
					send_error(hc, MessageFormat.format("Malformed VERSION message was received: Transport type {0} "
							+ " was specified more than once.", transport_type_enum.values()[transport_type].toString()));
				} else {
					hc.transport_supported[transport_type] = true;
				}
			} else {
				send_error(hc, MessageFormat.format("Malformed VERSION message was received: Transport type code {0} "
						+ "is invalid.", transport_type));
			}
		}

		if (!hc.transport_supported[transport_type_enum.TRANSPORT_LOCAL.ordinal()]) {
			send_error(hc, MessageFormat.format("Malformed VERSION message was received: Transport type {0} "
					+ " must be supported anyway.", transport_type_enum.TRANSPORT_LOCAL.toString()));
		}
		if (!hc.transport_supported[transport_type_enum.TRANSPORT_INET_STREAM.ordinal()]) {
			send_error(hc, MessageFormat.format("Malformed VERSION message was received: Transport type {0} "
					+ " must be supported anyway.", transport_type_enum.TRANSPORT_INET_STREAM.toString()));
		}

		hc.hc_state = hc_state_enum.HC_IDLE;
		text_buf.cut_message();
	}

	private static void setup_host(final Host host) {
		host.transport_supported[transport_type_enum.TRANSPORT_LOCAL.ordinal()] = true;
		host.transport_supported[transport_type_enum.TRANSPORT_INET_STREAM.ordinal()] = true;

	}

	public static void configure(final String config_file) {
		lock();
		switch(mc_state) {
		case MC_HC_CONNECTED:
		case MC_ACTIVE:
			mc_state = mcStateEnum.MC_CONFIGURING;
			break;
		case MC_LISTENING:
		case MC_LISTENING_CONFIGURED:
			mc_state = mcStateEnum.MC_LISTENING_CONFIGURED;
			break;
		case MC_RECONFIGURING:
			break;
		default:
			//TODO error, message in MainController::configure
			unlock();
			return;
		}

		//FIXME needs to be processed somewhere
		config_str.set(config_file);

//		try {
//			Scanner sc = new Scanner(new File(config_file));
//			config_str.set(sc.useDelimiter("\\Z").next());
//			sc.close();
//		} catch (FileNotFoundException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}

		if (mc_state == mcStateEnum.MC_CONFIGURING || mc_state == mcStateEnum.MC_RECONFIGURING) {
			notify("Downloading configuration file to all HCs.");
			for (final Host host : hosts) {
				configure_host(host, false);
				handle_hc_data(host);//?
			}
		}

		if (mc_state == mcStateEnum.MC_RECONFIGURING) {
			notify("Downloading configuration file to the MTC.");
			configure_mtc();
		}

		status_change();
		unlock();
	}

	private static void configure_mtc() {
		if (config_str == null) {
			fatal_error("MainController.configure_mtc: no config file");
		}
		if (mtc.tc_state == tc_state_enum.TC_IDLE) {
			//TODO error, MainController::configure_mtc
		} else {
			mtc.tc_state = tc_state_enum.MTC_CONFIGURING;
			send_configure_mtc(config_str.get());
		}

	}

	private static void send_configure_mtc(final String config) {
		final Text_Buf text_buf = new Text_Buf();
		text_buf.push_int(MSG_CONFIGURE);
		text_buf.push_string(config);
		send_message(mtc.comp_location, text_buf);
	}

	private static void configure_host(final Host host, final boolean should_notify) {
		hc_state_enum next_state = hc_state_enum.HC_CONFIGURING;
		switch(host.hc_state) {
		case HC_CONFIGURING:
		case HC_CONFIGURING_OVERLOADED:
		case HC_EXITING:
			//TODO error, message in MainController::configure_host
			break;
		case HC_DOWN:
			break;
		case HC_OVERLOADED:
			next_state =hc_state_enum.HC_CONFIGURING_OVERLOADED;
			// no break
		default:
			host.hc_state = next_state;
			if (should_notify) {
				System.out.println("Downloading configuration file to HC on host "+host.hostname+".");
			}
			send_configure(host);

			if (mc_state != mcStateEnum.MC_RECONFIGURING) {
				// TODO send debug setup
			}
		}

	}

	private static void send_configure(final Host hc) {
		final Text_Buf text_buf = new Text_Buf();
		text_buf.push_int(MSG_CONFIGURE);
		text_buf.push_string(config_str.get());

		send_message(hc, text_buf);
	}

	private static void send_message(final Host hc, final Text_Buf text_buf) {
		text_buf.calculate_length();
		final byte[] msg_ptr = text_buf.get_data();
		final int msg_len = text_buf.get_len();
		final ByteBuffer buffer = ByteBuffer.wrap(msg_ptr, text_buf.get_begin(), msg_len);
		final SocketChannel localChannel = hc.socket;
		try {
			while (buffer.hasRemaining()) {
				localChannel.write(buffer);
			}
		} catch (IOException e) {
			close_connection(hc);
			final StringWriter error = new StringWriter();
			e.printStackTrace(new PrintWriter(error));
			throw new TtcnError("Sending data on the control connection to MC failed.");
		}
	}

	private static void process_mtc_created(final Host hc) {
		if (mc_state != mcStateEnum.MC_CREATING_MTC) {
			send_error(hc, "Message MTC_CREATED arrived in invalid state.");
			return;
		}
		if (mtc == null || mtc.tc_state != tc_state_enum.TC_INITIAL ) {
			//TODO error, message in MainController::process_mtc_created
			return;
		}

		mc_state = mcStateEnum.MC_READY;
		mtc.tc_state = tc_state_enum.TC_IDLE;

		System.out.println("MTC is created.");
		/*for (final ExecuteItem item : executeItems) {
			if (item.getTestcaseName() == null) {
				execute_control(hc, item.getModuleName());
			} else if (!"*".equals(item.getTestcaseName())) {
				execute_testcase(hc, item.getModuleName(), null);
			} else {
				execute_testcase(hc, item.getModuleName(), item.getTestcaseName());
			}*/

			handle_tc_data(mtc);
		//}
		ui.status_change();
		//FIXME these part should not be here !!!
/*		incoming_buf.get().cut_message();
		exit_mtc();

		if (mc_state != mcStateEnum.MC_TERMINATING_MTC) {
			System.out.println("The control connection to MTC is lost. Destroying all PTC connections.");
		}
		// TODO destroy_all_components();
		System.out.println("MTC terminated.");
		if (is_hc_in_state(hc_state_enum.HC_CONFIGURING)) {
			mc_state = mcStateEnum.MC_CONFIGURING;
		} else if (is_hc_in_state(hc_state_enum.HC_IDLE)) {
			mc_state = mcStateEnum.MC_HC_CONNECTED;
		} else if (is_hc_in_state(hc_state_enum.HC_ACTIVE) || is_hc_in_state(hc_state_enum.HC_OVERLOADED)) {
			mc_state = mcStateEnum.MC_ACTIVE;
		} else {
			mc_state = mcStateEnum.MC_LISTENING_CONFIGURED;
		}
		stop_requested.set(false);

		shutdown_session();
		System.out.println("Shutdown complete.");*/
	}


	private static synchronized void handle_tc_data(final ComponentStruct tc) {
		final Text_Buf local_incoming_buf = incoming_buf.get();

		receiveMessage(tc.comp_location);
		boolean close_connection = false;
		do {
			final int msg_len = local_incoming_buf.pull_int().get_int();
			final int msg_end = local_incoming_buf.get_pos() + msg_len;
			final int msg_type = local_incoming_buf.pull_int().get_int();
			switch (msg_type) {
			case MSG_ERROR:
				process_error(tc);
				break;
			case MSG_LOG:
				process_log(tc.comp_location);
				break;
			case MSG_CREATE_REQ:
				process_create_req(tc);
				break;
			case MSG_START_REQ:
				process_start_req(tc);
				break;
			case MSG_STOP_REQ:
				process_stop_req(tc);
				break;
			case MSG_KILL_REQ:
				process_kill_req(tc);
				break;
			case MSG_IS_RUNNING:
				process_is_running(tc);
				break;
			case MSG_IS_ALIVE:
				process_is_alive(tc);
				break;
			case MSG_DONE_REQ:
				process_done_req(tc);
				break;
			case MSG_KILLED_REQ:
				process_killed_req(tc);
				break;
			case MSG_CANCEL_DONE_ACK:
				process_cancel_done_ack(tc);
				break;
			case MSG_CONNECT_REQ:
				process_connect_req(tc);
				break;
			case MSG_CONNECT_LISTEN_ACK:
				process_connect_listen_ack(tc, msg_end);
				break;
			case MSG_CONNECTED:
				process_connected(tc);
				break;
			case MSG_CONNECT_ERROR:
				process_connect_error(tc);
				break;
			case MSG_DISCONNECT_REQ:
				process_disconnect_req(tc);
				break;
			case MSG_DISCONNECTED:
				process_disconnected(tc);
				break;
			case MSG_MAP_REQ:
				process_map_req(tc);
				break;
			case MSG_MAPPED:
				process_mapped(tc);
				break;
			case MSG_UNMAP_REQ:
				process_unmap_req(tc);
				break;
			case MSG_UNMAPPED:
				process_unmapped(tc);
				break;
			case MSG_DEBUG_RETURN_VALUE:
				// TODO
				break;
			case MSG_DEBUG_HALT_REQ:
				// TODO
				break;
			case MSG_DEBUG_CONTINUE_REQ:
				// TODO
				break;
			case MSG_DEBUG_BATCH:
				// TODO
				break;
			default:
				if (tc.equals(mtc)) {
					// these messages can be received only from the MTC
					switch (msg_type) {
					case MSG_TESTCASE_STARTED:
						process_testcase_started();
						break;
					case MSG_TESTCASE_FINISHED:
						process_testcase_finished();
						break;
					case MSG_MTC_READY:
						process_mtc_ready();
						break;
					case MSG_CONFIGURE_ACK:
						process_configure_ack_mtc();
						break;
					case MSG_CONFIGURE_NAK:
						process_configure_nak_mtc();
						break;
					default:
						// TODO error, message in MainController::handle_tc_data
						close_connection = true;
					}
				} else {
					// these messages can be received only from PTCs
					switch (msg_type) {
					case MSG_STOPPED:
						process_stopped(tc, msg_end);
						break;
					case MSG_STOPPED_KILLED:
						process_stopped_killed(tc, msg_end);
						break;
					case MSG_KILLED:
						process_killed(tc);
						break;
					default:
						System.out.println(MessageFormat.format("Invalid message type ({}) was received from PTC {1} "
								+ "at {2} [{3}].", msg_type, tc.comp_ref, tc.comp_location.hostname,
								tc.comp_location.address.toString()));
						close_connection = true;
						// TODO
					}
				}
			}

			if (close_connection) {
				break;
			}

			if (local_incoming_buf.get_len() == local_incoming_buf.get_pos()) {
				local_incoming_buf.cut_message();
				if (!(msg_type == MSG_MTC_READY && tc.equals(mtc))) {
					receiveMessage(tc.comp_location);
				}
			}
		} while (local_incoming_buf.is_message());
		// FIXME

		if (!tc.equals(mtc)) {
			if (tc.tc_state != tc_state_enum.TC_EXITING) {
				tc.local_verdict = VerdictTypeEnum.ERROR;
				component_terminated(tc);
			}
			tc.tc_state = tc_state_enum.TC_EXITED;
			if (mc_state == mcStateEnum.MC_TERMINATING_TESTCASE && ready_to_finish_testcase()) {
				finish_testcase();
			}
		}
	}

	private static boolean ready_to_finish_testcase() {
		for (int i = tc_first_comp_ref; i <= components.size(); i++) {
			final ComponentStruct comp = components.get(i);
			switch(comp.tc_state) {
			case TC_EXITED:
			case PTC_STALE:
				break;
			default:
				return false;
			}
		}
		return true;
	}

	private static void process_stopped(final ComponentStruct tc, final int msg_end) {
		switch(tc.tc_state) {
		case TC_STOPPING:
		case PTC_FUNCTION:
		case PTC_STOPPING_KILLING:
			if (tc.is_alive) {
				break;
			}
		default:
			send_error(tc.comp_location, "Unexpected message STOPPED was received.");
			return;
		}

		final Text_Buf text_buf = incoming_buf.get();
		tc.local_verdict = VerdictTypeEnum.values()[text_buf.pull_int().get_int()];
		tc.verdict_reason = text_buf.pull_string();
		tc.return_type = text_buf.pull_string();
		tc.return_value = new byte[text_buf.get_len() - text_buf.get_pos()];
		text_buf.pull_raw(tc.return_value.length, tc.return_value);
		text_buf.cut_message();

		component_stopped(tc);
	}

	private static void component_stopped(final ComponentStruct tc) {
		final tc_state_enum old_state = tc.tc_state;
		if (old_state == tc_state_enum.PTC_STOPPING_KILLING) {
			tc.tc_state = tc_state_enum.PTC_KILLING;
		} else {
			tc.tc_state = tc_state_enum.PTC_STOPPED;
			// TODO timer
		}

		switch(mc_state) {
		case MC_EXECUTING_TESTCASE:
			break;
		case MC_TERMINATING_TESTCASE:
			return;
		default:
			//TODO error, message in MainController::component_stopped
			return;
		}
		if (!tc.is_alive) {
			send_error(tc.comp_location, "Message STOPPED can only be sent by alive PTCs.");
			return;
		}
		boolean send_status_to_mtc = false;
		boolean send_done_to_mtc = false;
		for (int i = 0;; i++) {
			final ComponentStruct requestor = get_requestor(tc.done_requestors, i);
			if (requestor == null) {
				break;
			} else if (requestor.equals(mtc)) {
				send_status_to_mtc = true;
				send_done_to_mtc = true;
			} else {
				send_component_status_to_requestor(tc, requestor, true, false);
			}
		}
		if (any_component_done_requested.get()) {
			send_status_to_mtc = true;
		}
		boolean all_done_checked = false;
		boolean all_done_result = false;
		if (all_component_done_requested.get()) {
			all_done_checked = true;
			all_done_result = !is_any_component_running();
			if (all_done_result) {
				send_status_to_mtc = true;
			}
		}

		if (send_status_to_mtc) {
			if (!all_done_checked) {
				all_done_result = !is_any_component_running();
			}
			if (send_done_to_mtc) {
				send_component_status_mtc(tc.comp_ref, true, false, any_component_done_requested.get(), all_done_result,
						false, false, tc.local_verdict, tc.return_type, tc.return_value);
			} else {
				send_component_status_mtc(TitanComponent.NULL_COMPREF, false, false, any_component_done_requested.get(),
						all_done_result, false, false, VerdictTypeEnum.NONE, null, null);
			}
			if (any_component_done_requested.get()) {
				any_component_done_requested.set(false);
				any_component_done_sent.set(true);
			}
			if (all_done_result) {
				all_component_done_requested.set(false);
			}

		}
		if (old_state != tc_state_enum.PTC_FUNCTION) {
			if (mtc.tc_state == tc_state_enum.MTC_ALL_COMPONENT_KILL) {
				// do nothing
			} else if (mtc.tc_state == tc_state_enum.MTC_ALL_COMPONENT_STOP) {
				check_all_component_stop();
			} else {
				send_stop_ack_to_requestors(tc);
			}
		}
	}

	private static void process_unmapped(final ComponentStruct tc) {
		if (!message_expected(tc, "UNMAPPED")) {
			return;
		}

		final Text_Buf text_buf = incoming_buf.get();
		final boolean translation = text_buf.pull_int().get_int() == 0 ? false : true;
		final String sourcePort = text_buf.pull_string();
		final String systemPort = text_buf.pull_string();
		final int nof_params = text_buf.pull_int().get_int();
		final Map_Params params = new Map_Params(nof_params);
		for (int i = 0; i < nof_params; i++) {
			final String par = text_buf.pull_string();
			params.set_param(i, new TitanCharString(par));
		}
		text_buf.cut_message();

		PortConnection conn = null;
		if (!translation) {
			conn = find_connection(tc.comp_ref, sourcePort, TitanComponent.SYSTEM_COMPREF, systemPort);
		} else {
			conn = find_connection(TitanComponent.SYSTEM_COMPREF, sourcePort, tc.comp_ref, systemPort);
		}

		if (conn != null) {
			switch(conn.conn_state) {
			case CONN_MAPPING:
			case CONN_MAPPED:
			case CONN_UNMAPPING:
				destroy_mapping(conn, nof_params, params, null);
				break;
			default:
				send_error(tc.comp_location, MessageFormat.format("Unexpected MAPPED message was received for "
						+ "port mapping {0}:{1} - system:{2}.", tc.comp_ref, sourcePort, systemPort));
			}
		}

	}

	private static void process_unmap_req(final ComponentStruct tc) {
		if (!request_allowed(tc, "UNMAP_REQ")) {
			return;
		}

		final Text_Buf text_buf = incoming_buf.get();
		final int sourceComponent = text_buf.pull_int().get_int();
		final boolean translation = text_buf.pull_int().get_int() == 0 ? false : true;
		final String sourcePort = text_buf.pull_string();
		final String systemPort = text_buf.pull_string();
		final int nof_params = text_buf.pull_int().get_int();

		final Map_Params params = new Map_Params(nof_params);
		for (int i = 0; i < nof_params; i++) {
			final String par = text_buf.pull_string();
			params.set_param(i, new TitanCharString(par));
		}
		text_buf.cut_message();

		if (!valid_endpoint(sourceComponent, false, tc, "unmap")) {
			return;
		}

		final PortConnection conn = find_connection(sourceComponent, sourcePort, TitanComponent.SYSTEM_COMPREF, systemPort);
		if (conn == null) {
			send_unmap_ack(tc, nof_params, params);
		} else {
			switch (conn.conn_state) {
			case CONN_MAPPED:
				send_unmap(components.get(sourceComponent), sourcePort, systemPort, nof_params, params, translation);
				conn.conn_state = conn_state_enum.CONN_UNMAPPING;
			case CONN_UNMAPPING:
				add_requestor(conn.requestors, tc);
				tc.tc_state = tc_state_enum.TC_UNMAP;
				break;
			case CONN_MAPPING:
				send_error(tc.comp_location, MessageFormat.format("The port mapping {0}:{1} - system:{2} cannot be "
						+ "destroyed because a map operation is in progress on it.", sourceComponent, sourcePort, systemPort));
				break;
			default:
				send_error(tc.comp_location, MessageFormat.format("The port mapping {0}:{1} - system:{2} is in invalid "
						+ "state.", sourceComponent, sourcePort, systemPort));
			}
		}
	}

	private static void send_unmap(final ComponentStruct tc, final String sourcePort, final String systemPort,
			final int nof_params, final Map_Params params, final boolean translation) {

		final Text_Buf text_buf = new Text_Buf();
		text_buf.push_int(MSG_UNMAP);
		text_buf.push_int(translation ? 1 : 0);
		text_buf.push_string(sourcePort);
		text_buf.push_string(systemPort);
		text_buf.push_int(nof_params);
		for (int i = 0; i < nof_params; i++) {
			text_buf.push_string(params.get_param(i).get_value().toString());
		}

		send_message(tc.comp_location, text_buf);
	}

	private static void process_killed_req(final ComponentStruct tc) {
		if (!request_allowed(tc, "KILLED_REQ")) {
			return;
		}

		final Text_Buf text_buf = incoming_buf.get();
		final int component_reference = text_buf.pull_int().get_int();
		text_buf.cut_message();

		switch (component_reference) {
		case TitanComponent.NULL_COMPREF:
			send_error(tc.comp_location, "Killed operation was requested on the null component reference.");
			return;
		case TitanComponent.MTC_COMPREF:
			send_error(tc.comp_location, "Killed operation was requested on the component reference of the MTC.");
			return;
		case TitanComponent.SYSTEM_COMPREF:
			send_error(tc.comp_location, "Killed operation was requested on the component reference of the system.");
			return;
		case TitanComponent.ANY_COMPREF:
			if (tc.equals(mtc)) {
				final boolean answer = !is_all_component_alive();
				send_killed_ack(mtc, answer);
				if (!answer) {
					any_component_killed_requested.set(true);
				}
			} else {
				send_error(tc.comp_location, "Operation 'any component.killed' can only be performed on the MTC.");
			}
			return;
		case TitanComponent.ALL_COMPREF:
			if (tc == mtc) {
				final boolean answer = !is_any_component_alive();
				send_killed_ack(mtc, answer);
				if (!answer) {
					all_component_killed_requested.set(true);
				}
			} else {
				send_error(tc.comp_location, "Operation 'all component.killed' can only be performed on the MTC.");
			}
			return;
		default:
			break;
		}

		final ComponentStruct comp = components.get(component_reference);
		if (comp == null) {
			send_error(tc.comp_location, MessageFormat.format("The argument of killed operation is an invalid "
					+ "component reference: {0}.", component_reference));
			return;
		}
		switch (comp.tc_state) {
		case TC_EXITING:
		case TC_EXITED:
			send_killed_ack(tc, true);
			break;
		case TC_IDLE:
		case TC_CREATE:
		case TC_START:
		case TC_STOP:
		case TC_KILL:
		case TC_CONNECT:
		case TC_DISCONNECT:
		case TC_MAP:
		case TC_UNMAP:
		case TC_STOPPING:
		case PTC_FUNCTION:
		case PTC_STARTING:
		case PTC_STOPPED:
		case PTC_KILLING:
		case PTC_STOPPING_KILLING:
			send_killed_ack(tc, false);
			add_requestor(comp.killed_requestors, tc);
			break;
		case PTC_STALE:
			send_error(tc.comp_location, MessageFormat.format("The argument of killed operation ({0}) is a component "
					+ "reference that belongs to an earlier testcase.", component_reference));
			break;
		default:
			send_error(tc.comp_location, MessageFormat.format("The test component that the killed operation refers to "
					+ "({0}) is in invalid state.", component_reference));
		}
	}

	private static void send_killed_ack(final ComponentStruct tc, final boolean answer) {
		final Text_Buf text_buf = new Text_Buf();
		text_buf.push_int(MSG_KILLED_ACK);
		text_buf.push_int(answer ? 1 : 0);

		send_message(tc.comp_location, text_buf);
	}

	private static void process_done_req(final ComponentStruct tc) {
		if (!request_allowed(tc, "DONE_REQ")) {
			return;
		}

		final Text_Buf text_buf = incoming_buf.get();
		final int component_reference = text_buf.pull_int().get_int();
		text_buf.cut_message();

		switch (component_reference) {
		case TitanComponent.NULL_COMPREF:
			send_error(tc.comp_location, "Done operation was requested on the null component reference.");
			return;
		case TitanComponent.MTC_COMPREF:
			send_error(tc.comp_location, "Done operation was requested on the component reference of the MTC.");
			return;
		case TitanComponent.SYSTEM_COMPREF:
			send_error(tc.comp_location, "Done operation was requested on the component reference of the system.");
			return;
		case TitanComponent.ANY_COMPREF:
			if (tc.equals(mtc)) {
				final boolean answer = is_any_component_done();
				send_done_ack(mtc, answer, VerdictTypeEnum.NONE, null, null);
				if (answer) {
					any_component_done_sent.set(true);
				} else {
					any_component_done_requested.set(true);
				}
			} else {
				send_error(tc.comp_location, "Operation 'any component.done' can only be performed on the MTC.");
			}
			return;
		case TitanComponent.ALL_COMPREF:
			if (tc.equals(mtc)) {
				final boolean answer = !is_any_component_running();
				send_done_ack(mtc, answer, VerdictTypeEnum.NONE, null, null);
				if (!answer) {
					all_component_done_requested.set(true);
				}
			} else {
				send_error(tc.comp_location, "Operation 'all component.done' can only be performed on the MTC.");
			}
			return;
		default:
			break;
		}

		final ComponentStruct comp = components.get(component_reference);
		if (comp == null) {
			send_error(tc.comp_location, MessageFormat.format("The argument of done operation is an invalid component "
					+ "reference: {0}.", component_reference));
			return;
		}

		switch (comp.tc_state) {
		case PTC_STOPPED:
			// this answer has to be cancelled when the component is re-started
			add_requestor(comp.done_requestors, tc);
			// no break
		case TC_EXITING:
		case TC_EXITED:
		case PTC_KILLING:
			send_done_ack(tc, true, comp.local_verdict, comp.return_type, comp.return_value);
			break;
		case TC_IDLE:
		case TC_CREATE:
		case TC_START:
		case TC_STOP:
		case TC_KILL:
		case TC_CONNECT:
		case TC_DISCONNECT:
		case TC_MAP:
		case TC_UNMAP:
		case TC_STOPPING:
		case PTC_FUNCTION:
		case PTC_STARTING:
		case PTC_STOPPING_KILLING:
			send_done_ack(tc, false, VerdictTypeEnum.NONE, null, null);
			add_requestor(comp.done_requestors, tc);
			break;
		case PTC_STALE:
			send_error(tc.comp_location, MessageFormat.format("The argument of done operation ({0}) "
					+ "is a component reference that belongs to an earlier testcase.", component_reference));
			break;
		default:
			send_error(tc.comp_location, MessageFormat.format("The test component that the done operation refers to ({0}) "
					+ "is in invalid state.", component_reference));
		}
	}

	private static void send_done_ack(final ComponentStruct tc, final boolean answer, final VerdictTypeEnum verdict, final String return_type, final byte[] return_value) {
		final Text_Buf text_buf = new Text_Buf();
		text_buf.push_int(MSG_DONE_ACK);
		text_buf.push_int(answer ? 1 : 0);
		text_buf.push_int(verdict.getValue());
		text_buf.push_string(return_type);
		if (return_value != null) {
			text_buf.push_raw(return_value.length, return_value);
		}
		send_message(tc.comp_location, text_buf);
	}

	private static void process_cancel_done_ack(final ComponentStruct tc) {
		final Text_Buf text_buf = incoming_buf.get();
		final int component_reference = text_buf.pull_int().get_int();
		text_buf.cut_message();
		switch (component_reference) {
		case TitanComponent.NULL_COMPREF:
			send_error(tc.comp_location, "Message CANCEL_DONE_ACK refers to the null component reference.");
			return;
		case TitanComponent.MTC_COMPREF:
			send_error(tc.comp_location, "Message CANCEL_DONE_ACK refers to the component reference of the MTC.");
			return;
		case TitanComponent.SYSTEM_COMPREF:
			send_error(tc.comp_location, "Message CANCEL_DONE_ACK refers to the component reference of the system.");
			return;
		case TitanComponent.ANY_COMPREF:
			send_error(tc.comp_location, "Message CANCEL_DONE_ACK refers to 'any component'.");
			return;
		case TitanComponent.ALL_COMPREF:
			send_error(tc.comp_location, "Message CANCEL_DONE_ACK refers to 'all component'.");
			return;
		default:
			break;
		}
		final ComponentStruct started_tc = components.get(component_reference);

		if (started_tc == null) {
			send_error(tc.comp_location, MessageFormat.format("Message CANCEL_DONE_ACK refers to an invalid "
					+ "component reference: {0}.", component_reference));
			return;
		}
		done_cancelled(tc, started_tc);
		remove_requestor(tc.cancel_done_sent_for, started_tc);
	}

	private static void process_is_alive(final ComponentStruct tc) {
		if (!request_allowed(tc, "IS_ALIVE")) {
			return;
		}

		final Text_Buf text_buf = incoming_buf.get();
		final int component_reference = text_buf.pull_int().get_int();
		text_buf.cut_message();
		switch (component_reference) {
		case TitanComponent.NULL_COMPREF:
			send_error(tc.comp_location, "Alive operation was requested on the null component reference.");
			return;
		case TitanComponent.MTC_COMPREF:
			send_error(tc.comp_location, "Alive operation was requested on the component reference of the MTC.");
			return;
		case TitanComponent.SYSTEM_COMPREF:
			send_error(tc.comp_location, "Alive operation was requested on the component reference of the system.");
			return;
		case TitanComponent.ANY_COMPREF:
			if (tc.equals(mtc)) {
				send_alive(mtc, is_any_component_alive());
			} else {
				send_error(tc.comp_location, "Operation 'any component.alive' can only be performed on the MTC.");
			}
			return;
		case TitanComponent.ALL_COMPREF:
			if (tc.equals(mtc)) {
				send_alive(mtc, is_all_component_alive());
			} else {
				send_error(tc.comp_location, "Operation 'all component.alive' can only be performed on the MTC.");
			}
			return;
		default:
			break;
		}

		final ComponentStruct comp = components.get(component_reference);
		if (comp == null) {
			send_error(tc.comp_location, MessageFormat.format("The argument of alive operation is an invalid "
					+ "component reference: {0}.", component_reference));
			return;
		}

		switch (comp.tc_state) {
		case TC_IDLE:
		case TC_CREATE:
		case TC_START:
		case TC_STOP:
		case TC_KILL:
		case TC_CONNECT:
		case TC_DISCONNECT:
		case TC_MAP:
		case TC_UNMAP:
		case TC_STOPPING:
		case PTC_FUNCTION:
		case PTC_STARTING:
		case PTC_STOPPED:
		case PTC_KILLING:
		case PTC_STOPPING_KILLING:
			send_alive(tc, true);
			break;
		case TC_EXITING:
		case TC_EXITED:
			send_alive(tc, false);
			break;
		case PTC_STALE:
			send_error(tc.comp_location, MessageFormat.format("The argument of alive operation ({0}) is a "
					+ "component reference that belongs to an earlier testcase.", component_reference));
			break;
		default:
			send_error(tc.comp_location, MessageFormat.format("The test component that the alive operation referes to "
					+ "({0}) is in invalid state.", component_reference));
		}
	}

	private static boolean is_all_component_alive() {
		for (int i = tc_first_comp_ref; i <= components.size(); i++) {
			final ComponentStruct comp = components.get(i);
			if (!comp.is_alive) {
				return false;
			}
		}
		return true;
	}

	private static void send_alive(final ComponentStruct tc, final boolean answer) {
		final Text_Buf text_buf = new Text_Buf();
		text_buf.push_int(MSG_ALIVE);
		text_buf.push_int(answer ? 1 : 0);
		send_message(tc.comp_location, text_buf);
	}

	private static void process_is_running(final ComponentStruct tc) {
		if (!request_allowed(tc, "IS_RUNNING")) {
			return;
		}

		final Text_Buf text_buf = incoming_buf.get();
		final int component_reference = text_buf.pull_int().get_int();
		text_buf.cut_message();
		switch (component_reference) {
		case TitanComponent.NULL_COMPREF:
			send_error(tc.comp_location, "Running operation was requested on the null component reference.");
			return;
		case TitanComponent.MTC_COMPREF:
			send_error(tc.comp_location, "Running operation was requested on the component reference of the MTC.");
			return;
		case TitanComponent.SYSTEM_COMPREF:
			send_error(tc.comp_location, "Running operation was requested on the component reference of the system.");
			return;
		case TitanComponent.ANY_COMPREF:
			if (tc.equals(mtc)) {
				send_running(mtc, is_any_component_running());
			} else {
				send_error(tc.comp_location, "Operation 'any component.running' can only be performed on the MTC.");
			}
			return;
		case TitanComponent.ALL_COMPREF:
			if (tc.equals(mtc)) {
				send_running(mtc, is_all_component_running());
			} else {
				send_error(tc.comp_location, "Operation 'all component.running' can only be performed on the MTC.");
			}
			return;
		default:
			break;
		}

		final ComponentStruct comp = components.get(component_reference);
		if (comp == null) {
			send_error(tc.comp_location, MessageFormat.format("The argument of running operation is an invalid "
					+ "component reference: {0}.", component_reference));
			return;
		}

		switch (comp.tc_state) {
		case TC_CREATE:
		case TC_START:
		case TC_STOP:
		case TC_KILL:
		case TC_CONNECT:
		case TC_DISCONNECT:
		case TC_MAP:
		case TC_UNMAP:
		case TC_STOPPING:
		case PTC_FUNCTION:
		case PTC_STARTING:
		case PTC_STOPPING_KILLING:
			send_running(tc, true);
			break;
		case TC_IDLE:
		case TC_EXITING:
		case TC_EXITED:
		case PTC_STOPPED:
		case PTC_KILLING:
			send_running(tc, false);
			break;
		case PTC_STALE:
			send_error(tc.comp_location, MessageFormat.format("The argument of running operation ({0}) "
					+ "is a component reference that belongs to an earlier testcase.", component_reference));
			break;
		default:
			send_error(tc.comp_location, MessageFormat.format("The test component that the running operation refers "
					+ "to ({0}) is in invalid state.", component_reference));
		}
	}

	private static boolean is_all_component_running() {
		for (int i = tc_first_comp_ref; i <= components.size(); i++) {
			final ComponentStruct tc = components.get(i);
			if (tc.stop_requested) {
				continue;
			}
			switch(tc.tc_state) {
			case TC_EXITING:
			case TC_EXITED:
			case PTC_STOPPED:
				return false;
			default:
				break;
			}
		}
		return true;
	}

	private static void send_running(final ComponentStruct tc, final boolean answer) {
		final Text_Buf text_buf = new Text_Buf();
		text_buf.push_int(MSG_RUNNING);
		text_buf.push_int(answer ? 1 : 0);
		send_message(tc.comp_location, text_buf);
	}

	private static void process_mapped(final ComponentStruct tc) {
		if (!message_expected(tc, "MAPPED")) {
			return;
		}

		final Text_Buf text_buf = incoming_buf.get();
		final boolean translation = text_buf.pull_int().get_int() == 0 ? false : true;
		final String localPort = text_buf.pull_string();
		final String systemPort = text_buf.pull_string();
		final int nof_params = text_buf.pull_int().get_int();

		final Map_Params params = new Map_Params(nof_params);
		for (int i = 0; i < nof_params; i++) {
			final String par = text_buf.pull_string();
			params.set_param(i, new TitanCharString(par));
		}

		text_buf.cut_message();
		PortConnection conn = null;
		if (!translation) {
			conn = find_connection(tc.comp_ref, localPort, TitanComponent.SYSTEM_COMPREF, systemPort);
		} else {
			conn = find_connection(TitanComponent.SYSTEM_COMPREF, localPort, tc.comp_ref, systemPort);
		}
		if (conn == null) {
			send_error(tc.comp_location, MessageFormat.format("The MAPPED message refers to a non-existent "
					+ "port mapping {0}:{1} - system:{2}.", tc.comp_ref, localPort, systemPort));
		} else if (conn.conn_state != conn_state_enum.CONN_MAPPING && conn.conn_state != conn_state_enum.CONN_MAPPED
				&& translation) {
			send_error(tc.comp_location, MessageFormat.format("Unexpected MAPPED message was received for mapping "
					+ "{0}:{1} - system:{2}.", tc.comp_ref, localPort, systemPort));
		} else {
			for (int i = 0;; i++) {
				final ComponentStruct comp = get_requestor(conn.requestors, i);
				if (comp == null) {
					break;
				}
				if (comp.tc_state == tc_state_enum.TC_MAP) {
					send_map_ack(comp, nof_params, params);
					if (comp.equals(mtc)) {
						comp.tc_state = tc_state_enum.MTC_TESTCASE;
					} else {
						comp.tc_state = tc_state_enum.PTC_FUNCTION;
					}
				}
			}
			conn.conn_state = conn_state_enum.CONN_MAPPED;
		}
	}

	private static void process_killed(final ComponentStruct tc) {
		System.out.println("Process killed: "+tc.tc_state.toString());
		switch(tc.tc_state) {
		case TC_IDLE:
		case PTC_STOPPED:
		case PTC_KILLING:
			break;
		default:
			send_error(tc.comp_location, "Unexpected message KILLED was received.");
			System.out.println("Unexpected message KILLED was received from PTC "+tc.comp_ref+".");
			incoming_buf.get().cut_message();
			return;
		}

		final Text_Buf text_buf = incoming_buf.get();
		tc.local_verdict = VerdictTypeEnum.values()[text_buf.pull_int().get_int()];
		tc.verdict_reason = text_buf.pull_string();
		text_buf.cut_message();

		if (tc.tc_state != tc_state_enum.PTC_KILLING) {
			// TODO timer
		}
		component_terminated(tc);
	}

	private static void process_configure_nak_mtc() {
		if (mtc.tc_state != tc_state_enum.MTC_CONFIGURING) {
			send_error(mtc.comp_location, "Unexpected message CONFIGURE_NAK was received.");
			return;
		}
		mtc.tc_state = tc_state_enum.TC_IDLE;
		System.out.println("Processing of configuration file failed on the MTC.");
	}

	private static void process_configure_ack_mtc() {
		if (mtc.tc_state != tc_state_enum.MTC_CONFIGURING) {
			send_error(mtc.comp_location, "Unexpected message CONFIGURE_ACK was received.");
			return;
		}
		mtc.tc_state = tc_state_enum.TC_IDLE;
		System.out.println("Configuration file was processed on the MTC.");
	}

	private static void process_map_req(final ComponentStruct tc) {
		if (!request_allowed(tc, "MAP_REQ")) {
			return;
		}

		final Text_Buf text_buf = incoming_buf.get();
		final int sourceComponent = text_buf.pull_int().get_int();
		final boolean translation = text_buf.pull_int().get_int() == 0 ? false : true;
		final String sourcePort = text_buf.pull_string();
		final String systemPort = text_buf.pull_string();
		final int nof_params = text_buf.pull_int().get_int();

		if (!valid_endpoint(sourceComponent, true, tc, "map")) {
			text_buf.cut_message();
			return;
		}

		final Map_Params params = new Map_Params(nof_params);
		for (int i = 0; i < nof_params; i++) {
			final String par = text_buf.pull_string();
			params.set_param(i, new TitanCharString(par));
		}

		text_buf.cut_message();

		PortConnection conn = find_connection(sourceComponent, sourcePort, TitanComponent.SYSTEM_COMPREF, systemPort);
		if (conn == null) {
			send_map(components.get(sourceComponent), sourcePort, systemPort, nof_params, params, translation);
			conn = new PortConnection();
			conn.headComp = sourceComponent;
			conn.headPort = sourcePort;
			conn.tailComp = TitanComponent.SYSTEM_COMPREF;
			conn.tailPort = systemPort;
			conn.requestors = init_requestors(tc);
			add_connection(conn);
			tc.tc_state = tc_state_enum.TC_MAP;
		} else {
			switch(conn.conn_state) {
			case CONN_MAPPING:
				add_requestor(conn.requestors, tc);
				tc.tc_state = tc_state_enum.TC_MAP;
				break;
			case CONN_MAPPED:
				send_map_ack(tc, nof_params, params);
				break;
			case CONN_UNMAPPING:
				send_error(tc.comp_location, MessageFormat.format("The port mapping {0}:{1} - system:{2} cannot be "
						+ "established because an unmap operation is in progress on it.", sourceComponent, sourcePort, systemPort));
				break;
			default:
				send_error(tc.comp_location, MessageFormat.format("The port mapping {0}:{1} - system:{2} "
						+ "is in invalid state.", sourceComponent, sourcePort, systemPort));
			}
		}
	}

	private static void send_map_ack(final ComponentStruct tc, final int nof_params, final Map_Params params) {
		final Text_Buf text_buf = new Text_Buf();
		text_buf.push_int(MSG_MAP_ACK);
		text_buf.push_int(nof_params);
		for (int i = 0; i < nof_params; i++) {
			text_buf.push_string(params.get_param(i).get_value().toString());
		}

		send_message(tc.comp_location, text_buf);
	}

	private static void send_map(final ComponentStruct tc, final String sourcePort, final String systemPort, final int nof_params,
			final Map_Params params, final boolean translation) {
		final Text_Buf text_buf = new Text_Buf();
		text_buf.push_int(MSG_MAP);
		text_buf.push_int(translation ? 1 : 0);
		text_buf.push_string(sourcePort);
		text_buf.push_string(systemPort);
		text_buf.push_int(nof_params);
		for (int i = 0; i < nof_params; i++) {
			text_buf.push_string(params.get_param(i).get_value().toString());
		}

		send_message(tc.comp_location, text_buf);
	}

	private static void process_kill_req(final ComponentStruct tc) {
		if (!request_allowed(tc, "KILL_REQ")) {
			return;
		}

		final Text_Buf text_buf = incoming_buf.get();
		final int component_reference = text_buf.pull_int().get_int();
		text_buf.cut_message();
		switch(component_reference) {
		case TitanComponent.NULL_COMPREF:
			send_error(tc.comp_location, "Kill operation was requested on the null component reference.");
			return;
		case TitanComponent.MTC_COMPREF:
			send_error(tc.comp_location, "Kill operation was requested on the component reference of the MTC.");
			return;
		case TitanComponent.SYSTEM_COMPREF:
			send_error(tc.comp_location, "Kill operation was requested on the component reference of the system.");
			return;
		case TitanComponent.ANY_COMPREF:
			send_error(tc.comp_location, "Kill operation was requested on 'any component'.");
			return;
		case TitanComponent.ALL_COMPREF:
			if (tc.equals(mtc)) {
				if (kill_all_components(false)) {
					send_kill_ack(mtc);
				} else {
					mtc.tc_state = tc_state_enum.MTC_ALL_COMPONENT_KILL;
				}
			} else {
				send_error(tc.comp_location, "Operation 'all component.kill' can only be performed on the MTC.");
			}
			return;
		default:
			break;
		}

		final ComponentStruct target = components.get(component_reference);
		if (target == null) {
			send_error(tc.comp_location, MessageFormat.format("The argument of kill operation is an invalid "
					+ "component reference: {0}.", component_reference));
			return;
		} else if (target.equals(tc)) {
			send_error(tc.comp_location, "Kill operation was requested on the requestor component itself.");
			return;
		}

		boolean target_inactive = false;
		switch (target.tc_state) {
		case PTC_STOPPED:
			// the done status of this PTC is already sent out
			// and it will not be cancelled in the future
			target.done_requestors = new RequestorStruct();
			// no break
		case TC_IDLE:
			target_inactive = true;
			// no break
		case TC_CREATE:
		case TC_START:
		case TC_STOP:
		case TC_KILL:
		case TC_CONNECT:
		case TC_DISCONNECT:
		case TC_MAP:
		case TC_UNMAP:
		case PTC_FUNCTION:
			send_kill(target);
			if (target_inactive) {
				// the PTC was inactive
				target.tc_state = tc_state_enum.PTC_KILLING;
				if (!target.is_alive) {
					target.stop_requested = true;
				}
			} else {
				// the PTC was active
				target.tc_state = tc_state_enum.PTC_STOPPING_KILLING;
				target.stop_requested = true;
			}
			target.stop_requestors = init_requestors(null);
			target.kill_requestors = init_requestors(tc);
			// TODO timer
			tc.tc_state = tc_state_enum.TC_KILL;
			break;
		case TC_STOPPING:
			// the PTC is currently being stopped
			send_kill(target);
			target.tc_state = tc_state_enum.PTC_STOPPING_KILLING;
			// TODO timer
			// no break
		case PTC_KILLING:
		case PTC_STOPPING_KILLING:
			// the PTC is currently being terminated
			add_requestor(target.kill_requestors, tc);
			tc.tc_state = tc_state_enum.TC_KILL;
			break;
		case TC_EXITING:
		case TC_EXITED:
			// the PTC is already terminated
			send_kill_ack(tc);
			break;
		case PTC_STARTING:
			send_error(tc.comp_location, MessageFormat.format("PTC with component reference {0} cannot be killed "
					+ "because it is currently being started.", component_reference));
			break;
		case PTC_STALE:
			send_error(tc.comp_location, MessageFormat.format("The argument of kill operation ({0}) is a component "
					+ "reference that belongs to an earlier testcase.", component_reference));
			break;
		default:
			send_error(tc.comp_location, MessageFormat.format("The test component that the kill operation refers to "
					+ "({0}) is in invalid state.", component_reference));
		}
	}

	private static void process_stopped_killed(final ComponentStruct tc, final int msg_end) {
		switch (tc.tc_state) {
		case TC_CREATE:
		case TC_START:
		case TC_STOP:
		case TC_KILL:
		case TC_CONNECT:
		case TC_DISCONNECT:
		case TC_MAP:
		case TC_UNMAP:
		case TC_STOPPING:
		case PTC_FUNCTION:
		case PTC_STOPPING_KILLING:
			break;
		default:
			send_error(tc.comp_location, "Unexpected message STOPPED_KILLED was received.");
			System.out.println("Unexpected message STOPPED_KILLED was received from PTC "+tc.comp_ref+".");
			return;
		}

		final Text_Buf text_buf = incoming_buf.get();
		tc.local_verdict = TitanVerdictType.VerdictTypeEnum.values()[text_buf.pull_int().get_int()];
		tc.verdict_reason = text_buf.pull_string();
		tc.return_type = text_buf.pull_string();
		text_buf.cut_message();

		if (tc.tc_state != tc_state_enum.PTC_STOPPING_KILLING) {
			// TODO timer
		}
		component_terminated(tc);

	}

	private static void component_terminated(final ComponentStruct tc) {
		final tc_state_enum old_state = tc.tc_state;
		tc.tc_state = tc_state_enum.TC_EXITING;
		switch(mc_state) {
		case MC_EXECUTING_TESTCASE:
			break;
		case MC_TERMINATING_TESTCASE:
			return;
		default:
			// TODO error, message in MainController::component_terminated
			return;
		}

		boolean send_status_to_mtc = false;
		boolean send_done_to_mtc = true;
		for (int i = 0;; i++) {
			final ComponentStruct requestor = get_requestor(tc.done_requestors, i);
			if (requestor == null) {
				break;
			} else if (requestor.equals(mtc)) {
				send_status_to_mtc = true;
				send_done_to_mtc = true;
			} else {
				send_component_status_to_requestor(tc, requestor, true, true);
			}
		}
		for (int i = 0;; i++) {
			final ComponentStruct requestor = get_requestor(tc.killed_requestors, i);
			if (requestor == null) {
				break;
			} else if (requestor.equals(mtc)) {
				send_status_to_mtc = true;
			} else if (!has_requestor(tc.done_requestors, requestor)) {
				send_component_status_to_requestor(tc, requestor, false, true);
			}
		}

		tc.done_requestors = new RequestorStruct();
		tc.killed_requestors = new RequestorStruct();

		if (any_component_done_requested.get() || any_component_killed_requested.get()) {
			send_status_to_mtc = true;
		}
		boolean all_done_checked = false;
		boolean all_done_result = false;
		if (all_component_done_requested.get()) {
			all_done_checked = true;
			all_done_result = !is_any_component_running();
			if (all_done_result) {
				send_status_to_mtc = true;
			}
		}
		boolean all_killed_checked = false;
		boolean all_killed_result = false;
		if (all_component_killed_requested.get()) {
			all_killed_checked = true;
			all_killed_result = !is_any_component_alive();
			if (all_killed_result) {
				send_status_to_mtc = true;
			}
		}

		if (send_status_to_mtc) {
			if (!all_done_checked) {
				all_done_result = !is_any_component_running();
			}
			if (!all_killed_checked) {
				all_killed_result = !is_any_component_alive();
			}
			if (send_done_to_mtc) {
				send_component_status_mtc(tc.comp_ref, true, true, true, all_done_result, true, all_killed_result,
						tc.local_verdict, tc.return_type, tc.return_value);
			} else {
				send_component_status_mtc(tc.comp_ref, false, true, true, all_done_result, true, all_killed_result,
						VerdictTypeEnum.NONE, null, null);
			}
			any_component_done_requested.set(false);
			any_component_done_sent.set(true);
			any_component_killed_requested.set(false);
			if (all_done_result) {
				all_component_done_requested.set(false);
			}
			if (all_killed_result) {
				all_component_killed_requested.set(false);
			}
		}
		switch(old_state) {
		case TC_STOPPING:
		case PTC_STOPPING_KILLING:
		case PTC_KILLING:
			if (mtc.tc_state == tc_state_enum.MTC_ALL_COMPONENT_KILL) {
				check_all_component_kill();
			} else if (mtc.tc_state == tc_state_enum.MTC_ALL_COMPONENT_STOP) {
				check_all_component_stop();
			} else {
				send_stop_ack_to_requestors(tc);
				send_kill_ack_to_requestors(tc);
			}
		default:
			break;
		}
		for (int i = 0;; i++) {
			final ComponentStruct comp = get_requestor(tc.cancel_done_sent_for, i);
			if (comp == null) {
				break;
			}
			done_cancelled(tc, comp);
		}
		tc.cancel_done_sent_for = new RequestorStruct();

		Iterator<PortConnection> it = tc.conn_head_list.iterator();
		while (it.hasNext()) {
			final PortConnection conn = it.next();
			if (conn.tailComp == TitanComponent.SYSTEM_COMPREF) {
				destroy_mapping(conn, 0, null, it);
			} else {
				destroy_connection(conn, tc, it);
			}
			tc.conn_tail_list.remove(conn);
		}
		it = tc.conn_tail_list.iterator();
		while (it.hasNext()) {
			final PortConnection conn = it.next();
			if (conn.headComp == TitanComponent.SYSTEM_COMPREF) {
				destroy_mapping(conn, 0 , null, it);
			} else {
				destroy_connection(conn, tc, it);
			}
			tc.conn_head_list.remove(conn);
		}

		tc.tc_fn_name = new QualifiedName("", "");

	}

	private static void destroy_connection(final PortConnection conn, final ComponentStruct tc, final Iterator<PortConnection> it) {
		switch (conn.conn_state) {
		case CONN_LISTENING:
		case CONN_CONNECTING:
			if (conn.transport_type != transport_type_enum.TRANSPORT_LOCAL &&
			conn.headComp != tc.comp_ref) {
				// shut down the server side if the client has terminated
				send_disconnect_to_server(conn);
			}
			send_error_to_connect_requestors(conn, "test component "+tc.comp_ref+" has terminated during connection setup.");

			break;
		case CONN_CONNECTED:
			break;
		case CONN_DISCONNECTING:
			send_disconnect_ack_to_requestors(conn);
			break;
		default:
			// TODO error, message in MainController::destroy_connection
		}
		it.remove();
	}

	private static void destroy_mapping(final PortConnection conn, final int nof_params, final Map_Params params, final Iterator<PortConnection> it) {
		int tc_compref;
		String tc_port;
		String system_port;
		if (conn.comp_ref == TitanComponent.SYSTEM_COMPREF) {
			tc_compref = conn.tailComp;
			tc_port = conn.tailPort;
			system_port = conn.headPort;
		} else {
			tc_compref = conn.headComp;
			tc_port = conn.headPort;
			system_port = conn.tailPort;
		}
		switch(conn.conn_state) {
		case CONN_UNMAPPING:
			for (int i=0; ; i++) {
				final ComponentStruct comp = get_requestor(conn.requestors, i);
				if (comp == null) {
					break;
				}
				if (comp.tc_state == tc_state_enum.TC_UNMAP) {
					send_unmap_ack(comp, nof_params, params);
					if (comp.equals(mtc)) {
						comp.tc_state = tc_state_enum.MTC_TESTCASE;
					} else {
						comp.tc_state = tc_state_enum.PTC_FUNCTION;
					}
				}
			}
			break;
		case CONN_MAPPING:
			for (int i = 0;; i++) {
				final ComponentStruct comp = get_requestor(conn.requestors, i);
				if (comp == null) {
					break;
				}
				if (comp.tc_state == tc_state_enum.TC_MAP) {
					send_error(comp.comp_location, MessageFormat.format("Establishment of port mapping {0}:{1} - "
							+ "system:{2} failed because the test component endpoint has terminated.", tc_compref, tc_port, system_port));
					if (comp.equals(mtc)) {
						comp.tc_state = tc_state_enum.MTC_TESTCASE;
					} else {
						comp.tc_state = tc_state_enum.PTC_FUNCTION;
					}
				}
			}
		default:
			break;
		}
		if (it != null) {
			it.remove();
		} else {
			remove_connection(conn);
		}
	}

	private static void send_unmap_ack(final ComponentStruct tc, final int nof_params, final Map_Params params) {
		final Text_Buf text_buf = new Text_Buf();
		text_buf.push_int(MSG_UNMAP_ACK);
		text_buf.push_int(nof_params);
		for (int i = 0; i < nof_params; i++) {
			text_buf.push_string(params.get_param(i).get_value().toString());
		}
		send_message(tc.comp_location, text_buf);

	}

	private static void done_cancelled(final ComponentStruct from, final ComponentStruct started_tc) {
		if (started_tc.tc_state != tc_state_enum.PTC_STARTING) {
			return;
		}
		remove_requestor(started_tc.cancel_done_sent_to, from);
		if (get_requestor(started_tc.cancel_done_sent_to, 0) != null) {
			return;
		}

		send_start(started_tc, started_tc.tc_fn_name, started_tc.arg);
		final ComponentStruct start_requestor = started_tc.start_requestor;
		if (start_requestor.tc_state == tc_state_enum.TC_START) {
			send_start_ack(start_requestor);
			if (start_requestor.equals(mtc)) {
				start_requestor.tc_state = tc_state_enum.MTC_TESTCASE;
			} else {
				start_requestor.tc_state = tc_state_enum.PTC_FUNCTION;
			}
		}

		started_tc.cancel_done_sent_to = new RequestorStruct();
		started_tc.tc_state = tc_state_enum.PTC_FUNCTION;
	}

	private static void remove_requestor(final RequestorStruct reqs, final ComponentStruct tc) {
		switch (reqs.n_components) {
		case 0:
			break;
		case 1:
			if (reqs.comp.equals(tc)) {
				reqs.n_components = 0;
			}
			break;
		case 2:
			ComponentStruct tmp = null;
			if (reqs.components.get(0).equals(tc)) {
				tmp = reqs.components.get(1);
			} else if (reqs.components.get(1).equals(tc)) {
				tmp = reqs.components.get(0);
			}
			if (tmp != null) {
				reqs.n_components = 1;
				reqs.comp = tmp;
			}
			break;
		default:
			for (final ComponentStruct comp : reqs.components) {
				if (comp.equals(tc)) {
					reqs.n_components--;
					break;
				}
			}
		}
	}

	private static void send_kill_ack_to_requestors(final ComponentStruct tc) {
		for (int i = 0; ; i++) {
			final ComponentStruct requestor = get_requestor(tc.kill_requestors, i);
			if (requestor == null) {
				break;
			}
			if (requestor.tc_state == tc_state_enum.TC_KILL) {
				send_kill_ack(requestor);
				if (requestor.equals(mtc)) {
					requestor.tc_state = tc_state_enum.MTC_TESTCASE;
				} else {
					requestor.tc_state = tc_state_enum.PTC_FUNCTION;
				}
			}
		}
		tc.kill_requestors = new RequestorStruct();
	}

	private static void send_stop_ack_to_requestors(final ComponentStruct tc) {
		for (int i = 0; ; i++) {
			final ComponentStruct requestor = get_requestor(tc.stop_requestors, i);
			if (requestor == null) {
				break;
			}
			if (requestor.tc_state == tc_state_enum.TC_STOP) {
				send_stop_ack(requestor);
				if (requestor.equals(mtc)) {
					requestor.tc_state = tc_state_enum.MTC_TESTCASE;
				} else {
					requestor.tc_state = tc_state_enum.PTC_FUNCTION;
				}
			}
		}
		tc.stop_requestors = new RequestorStruct();
	}

	private static void check_all_component_stop() {
		boolean ready_for_ack = true;
		for (int i = tc_first_comp_ref; i <= components.size(); i++) {
			final ComponentStruct comp = components.get(i);

			switch (comp.tc_state) {
			case TC_INITIAL:
			case PTC_KILLING:
				if (!comp.is_alive) {
					ready_for_ack = false;
				}
				break;
			case TC_STOPPING:
			case PTC_STOPPING_KILLING:
				ready_for_ack = false;
				break;
			case TC_EXITING:
			case TC_EXITED:
			case PTC_STOPPED:
			case PTC_STALE:
				break;
			case TC_IDLE:
				// only alive components can be in idle state
				if (comp.is_alive) {
					break;
				}
			default:
				//TODO error, message in MainController::check_all_component_stop
			}
			if (!ready_for_ack) {
				break;
			}
		}
		if (ready_for_ack) {
			send_stop_ack(mtc);
			mtc.tc_state = tc_state_enum.MTC_TESTCASE;
		}
	}

	private static void check_all_component_kill() {
		boolean ready_for_ack = true;
		for (int i = tc_first_comp_ref; i <= components.size(); i++) {
			final ComponentStruct comp = components.get(i);
			switch (comp.tc_state) {
			case TC_INITIAL:
			case PTC_STOPPING_KILLING:
			case PTC_KILLING:
				ready_for_ack = false;
			case TC_EXITING:
			case TC_EXITED:
			case PTC_STALE:
				break;
			default:
				// TODO error, message in MainController::check_all_component_kill
			}
			if (!ready_for_ack) {
				break;
			}
		}
		if (ready_for_ack) {
			send_kill_ack(mtc);
			mtc.tc_state = tc_state_enum.MTC_TESTCASE;
		}
	}

	private static void send_kill_ack(final ComponentStruct tc) {
		final Text_Buf text_buf = new Text_Buf();
		text_buf.push_int(MSG_KILL_ACK);
		send_message(tc.comp_location, text_buf);
	}

	private static void send_component_status_mtc(final int comp_ref, final boolean is_done, final boolean is_killed, final boolean is_any_done,
			final boolean is_all_done, final boolean is_any_killed, final boolean is_all_killed, final VerdictTypeEnum local_verdict,
			final String return_type, final byte[] return_value) {

		final Text_Buf text_buf = new Text_Buf();
		text_buf.push_int(MSG_COMPONENT_STATUS);
		text_buf.push_int(comp_ref);
		text_buf.push_int(is_done ? 1 : 0);
		text_buf.push_int(is_killed ? 1 : 0);
		text_buf.push_int(is_any_done ? 1 : 0);
		text_buf.push_int(is_all_done ? 1 : 0);
		text_buf.push_int(is_any_killed ? 1 : 0);
		text_buf.push_int(is_all_killed ? 1 : 0);
		text_buf.push_int(local_verdict.getValue());
		text_buf.push_string(return_type);

		if (return_value != null) {
			text_buf.push_raw(return_value);
		}

		send_message(mtc.comp_location, text_buf);
	}

	private static boolean is_any_component_alive() {
		for (int i = tc_first_comp_ref; i <= components.size(); i++) {
			final ComponentStruct comp = components.get(i);
			if (component_is_alive(comp)) {
				return true;
			}
		}
		return false;
	}

	private static boolean component_is_alive(final ComponentStruct tc) {
		switch (tc.tc_state) {
		case TC_INITIAL:
		case TC_IDLE:
		case TC_CREATE:
		case TC_START:
		case TC_STOP:
		case TC_KILL:
		case TC_CONNECT:
		case TC_DISCONNECT:
		case TC_MAP:
		case TC_UNMAP:
		case TC_STOPPING:
		case PTC_FUNCTION:
		case PTC_STARTING:
		case PTC_STOPPED:
		case PTC_KILLING:
		case PTC_STOPPING_KILLING:
			return true;
		case TC_EXITING:
		case TC_EXITED:
			return false;
		default:
			// TODO error, message in MainController::component_is_alive
			return false;
		}
	}

	private static boolean is_any_component_running() {
		for (int i = tc_first_comp_ref; i <= components.size(); i++) {
			final ComponentStruct comp = components.get(i);
			if (component_is_running(comp)) {
				return true;
			}
		}
		return false;
	}

	private static boolean component_is_running(final ComponentStruct tc) {
		switch (tc.tc_state) {
		case TC_CREATE:
		case TC_START:
		case TC_STOP:
		case TC_KILL:
		case TC_CONNECT:
		case TC_DISCONNECT:
		case TC_MAP:
		case TC_UNMAP:
		case TC_STOPPING:
		case PTC_FUNCTION:
		case PTC_STARTING:
		case PTC_STOPPING_KILLING:
			return true;
		case TC_INITIAL:
		case TC_IDLE:
		case TC_EXITING:
		case TC_EXITED:
		case PTC_STOPPED:
		case PTC_KILLING:
			return false;
		default:
			// TODO error, message in MainController::component_is_running
			return false;
		}
	}

	private static void send_component_status_to_requestor(final ComponentStruct tc, final ComponentStruct requestor, final boolean done_status,
			final boolean killed_status) {
		switch (requestor.tc_state) {
		case PTC_FUNCTION:
		case TC_CREATE:
		case TC_START:
		case TC_STOP:
		case TC_KILL:
		case TC_CONNECT:
		case TC_DISCONNECT:
		case TC_MAP:
		case TC_UNMAP:
		case TC_STOPPING:
		case PTC_STOPPED:
		case PTC_STARTING:
			if (done_status) {
				send_component_status_ptc(requestor, tc.comp_ref, true, killed_status, tc.local_verdict,
						tc.return_type, tc.return_value);
			} else {
				send_component_status_ptc(requestor, tc.comp_ref, false,killed_status, tc.local_verdict, null, null);
			}
			break;
		case PTC_STOPPING_KILLING:
		case PTC_KILLING:
		case TC_EXITING:
		case TC_EXITED:
			// the PTC requestor is not interested in the component status anymore
			break;
		default:
			// TODO error, message in MainController::send_component_status_to_requestor
		}
	}

	private static void send_component_status_ptc(final ComponentStruct tc, final int comp_ref, final boolean is_done,
			final boolean is_killed, final VerdictTypeEnum local_verdict, final String return_type, final byte[] return_value) {
		final Text_Buf text_buf = new Text_Buf();
		text_buf.push_int(MSG_COMPONENT_STATUS);
		text_buf.push_int(comp_ref);
		text_buf.push_int(is_done ? 1 : 0);
		text_buf.push_int(is_killed ? 1 : 0);
		text_buf.push_int(local_verdict.getValue());
		text_buf.push_string(return_type);
		if (tc.return_value != null) {
			text_buf.push_raw(tc.return_value);
		}
		send_message(tc.comp_location, text_buf);
	}

	private static void process_stop_req(final ComponentStruct tc) {
		if (!request_allowed(tc, "STOP_REQ")) {
			return;
		}

		final Text_Buf text_buf = incoming_buf.get();
		final int component_reference = text_buf.pull_int().get_int();
		text_buf.cut_message();

		switch(component_reference) {
		case TitanComponent.NULL_COMPREF:
			send_error(tc.comp_location, "Stop operation was requested on the null component reference.");
			return;
		case TitanComponent.MTC_COMPREF:
			if (!tc.equals(mtc)) {
				if (!mtc.stop_requested) {
					send_stop(mtc);
					kill_all_components(true);
					mtc.stop_requested = true;
					// TODO timer
					System.out.println("Test Component "+tc.comp_ref+" had requested to stop MTC. Terminating current "
							+ "testcase execution.");
				}
			} else {
				send_error(tc.comp_location, "MTC has requested to stop itself.");
			}
			return;
		case TitanComponent.SYSTEM_COMPREF:
			send_error(tc.comp_location, "Stop operation was requested on the component reference of the system.");
			return;
		case TitanComponent.ANY_COMPREF:
			send_error(tc.comp_location, "Stop operation was requested on 'any component'.");
			return;
		case TitanComponent.ALL_COMPREF:
			if (tc.equals(mtc)) {
				if (stop_all_components()) {
					send_stop_ack(mtc);
				} else {
					mtc.tc_state = tc_state_enum.MTC_ALL_COMPONENT_STOP;
				}
			} else {
				send_error(tc.comp_location, "Operation 'all component.stop' can only be performed on the MTC.");
			}
			return;
		default:
			break;
		}

		final ComponentStruct target = components.get(component_reference);
		if (target == null) {
			send_error(tc.comp_location, MessageFormat.format("The argument of stop operation is an invalid component "
					+ "reference {0}.", component_reference));
			return;
		} else if (target.equals(tc)) {
			send_error(tc.comp_location, "Stop operation was requested on the requestor component itself.");
			return;
		}
		boolean target_inactive = false;
		switch(target.tc_state) {
		case PTC_STOPPED:
			if (!target.is_alive) {
				throw new TtcnError("PTC "+component_reference+" cannot be in state STOPPED because it is not an "
						+ "alive type PTC.");
			}
			// no break
		case TC_IDLE:
			target_inactive = true;
			// no break
		case TC_CREATE:
		case TC_START:
		case TC_STOP:
		case TC_KILL:
		case TC_CONNECT:
		case TC_DISCONNECT:
		case TC_MAP:
		case TC_UNMAP:
		case PTC_FUNCTION:
			if (target.is_alive) {
				if (target_inactive) {
					send_stop_ack(tc);
					break;
				} else {
					send_stop(target);
					target.tc_state = tc_state_enum.TC_STOPPING;
				}
			} else {
				send_kill(target);
				if (target_inactive) {
					target.tc_state = tc_state_enum.PTC_KILLING;
				} else {
					target.tc_state = tc_state_enum.PTC_STOPPING_KILLING;
				}
			}
			target.stop_requested = true;
			target.stop_requestors = init_requestors(tc);
			target.kill_requestors = init_requestors(null);
			// TODO timer
			tc.tc_state = tc_state_enum.TC_STOP;
			break;
		case PTC_KILLING:
			if (target.is_alive) {
				send_stop_ack(tc);
				break;
			}
			// no break
		case TC_STOPPING:
		case PTC_STOPPING_KILLING:
			add_requestor(target.stop_requestors, tc);
			tc.tc_state = tc_state_enum.TC_STOP;
			break;
		case TC_EXITING:
		case TC_EXITED:
			send_stop_ack(tc);
			break;
		case PTC_STARTING:
			send_error(tc.comp_location, MessageFormat.format("PTC with component reference {0} cannot be stopped "
					+ "because it is currently being started.", component_reference));
			break;
		case PTC_STALE:
			send_error(tc.comp_location, MessageFormat.format("The argument of stop operation ({0}) is a component "
					+ "reference that belongs to an earlier testcase.", component_reference));
			break;
		default:
			send_error(tc.comp_location, MessageFormat.format("The test component that the stop operation refers to "
					+ "({0}) is in invalid state.", component_reference));
		}
	}

	private static void send_stop_ack(final ComponentStruct tc) {
		final Text_Buf text_buf = new Text_Buf();
		text_buf.push_int(MSG_STOP_ACK);
		send_message(tc.comp_location, text_buf);
	}

	private static boolean stop_all_components() {
		boolean ready_for_ack = true;
		for (int i = tc_first_comp_ref; i <= components.size(); i++) {
			final ComponentStruct tc = components.get(i);
			switch(tc.tc_state) {
			case TC_INITIAL:
				if (!tc.is_alive) {
					ready_for_ack = false;
				}
				break;
			case TC_IDLE:
				if (!tc.is_alive) {
					send_kill(tc);
					tc.tc_state = tc_state_enum.PTC_KILLING;
					tc.stop_requested = true;
					tc.stop_requestors = init_requestors(null);
					tc.kill_requestors = init_requestors(null);
					ready_for_ack = false;
				}
				break;
			case TC_CREATE:
			case TC_START:
			case TC_STOP:
			case TC_KILL:
			case TC_CONNECT:
			case TC_DISCONNECT:
			case TC_MAP:
			case TC_UNMAP:
			case PTC_FUNCTION:
				if (tc.is_alive) {
					send_stop(tc);
					tc.tc_state = tc_state_enum.TC_STOPPING;
				} else {
					send_kill(tc);
					tc.tc_state = tc_state_enum.PTC_STOPPING_KILLING;
				}
				tc.stop_requested = true;
				tc.stop_requestors = init_requestors(null);
				tc.killed_requestors = init_requestors(null);
				// TODO timer
				ready_for_ack = false;
				break;
			case PTC_STARTING:
				tc.cancel_done_sent_to = new RequestorStruct();
				tc.tc_state = tc_state_enum.PTC_STOPPED;
				break;
			case TC_STOPPING:
			case PTC_STOPPING_KILLING:
				tc.stop_requestors = new RequestorStruct();
				tc.killed_requestors = new RequestorStruct();
				ready_for_ack = false;
				break;
			case PTC_KILLING:
				tc.stop_requestors = new RequestorStruct();
				tc.killed_requestors = new RequestorStruct();
				if (!tc.is_alive) {
					ready_for_ack = false;
				}
				break;
			case PTC_STOPPED:
			case TC_EXITING:
			case TC_EXITED:
			case PTC_STALE:
				break;
			default:
				throw new TtcnError("Test Component "+tc.comp_ref+" is in invalid state when stopping all components.");

			}
			final boolean mtc_requested_done = has_requestor(tc.done_requestors, mtc);
			tc.done_requestors = new RequestorStruct();
			if (mtc_requested_done) {
				add_requestor(tc.done_requestors, mtc);
			}
			final boolean mtc_requested_killed = has_requestor(tc.killed_requestors, mtc);
			tc.killed_requestors = new RequestorStruct();
			if (mtc_requested_killed) {
				add_requestor(tc.killed_requestors, mtc);
			}
			tc.cancel_done_sent_for = new RequestorStruct();
		}
		return ready_for_ack;
	}

	private static boolean kill_all_components(final boolean testcase_ends) {
		boolean ready_for_ack = true;
		for (int i = tc_first_comp_ref; i <= components.size(); i++) {
			final ComponentStruct tc = components.get(i);
			boolean is_inactive = false;
			switch(tc.tc_state) {
			case TC_INITIAL:
				ready_for_ack = false;
				break;
			case PTC_STARTING:
				tc.cancel_done_sent_to = new RequestorStruct();
				// no break
			case TC_IDLE:
			case PTC_STOPPED:
				is_inactive = true;
				// no break
			case TC_CREATE:
			case TC_START:
			case TC_STOP:
			case TC_KILL:
			case TC_CONNECT:
			case TC_DISCONNECT:
			case TC_MAP:
			case TC_UNMAP:
			case PTC_FUNCTION:
				send_kill(tc);
				if (is_inactive) {
					tc.tc_state = tc_state_enum.PTC_KILLING;
					if (!tc.is_alive) {
						tc.stop_requested = true;
					}
				} else {
					tc.tc_state = tc_state_enum.PTC_STOPPING_KILLING;
					tc.stop_requested = true;
				}
				tc.stop_requestors = init_requestors(null);
				tc.kill_requestors = init_requestors(null);
				// TODO timer
				ready_for_ack = false;
				break;
			case TC_STOPPING:
				send_kill(tc);
				tc.tc_state = tc_state_enum.PTC_STOPPING_KILLING;
				// TODO timer
				// no break
			case PTC_KILLING:
			case PTC_STOPPING_KILLING:
				tc.stop_requestors = new RequestorStruct();
				tc.kill_requestors = new RequestorStruct();
				ready_for_ack = false;
				break;
			case TC_EXITING:
				if (testcase_ends) {
					ready_for_ack = false;
				}
			case TC_EXITED:
			case PTC_STALE:
				break;
			default:
				// TODO error, message in MainController::kill_all_components
			}
			if (testcase_ends) {
				tc.done_requestors = new RequestorStruct();
				tc.killed_requestors = new RequestorStruct();
			} else {
				final boolean mtc_requested_done = has_requestor(tc.done_requestors, mtc);
				tc.done_requestors = new RequestorStruct();
				if (mtc_requested_done) {
					add_requestor(tc.done_requestors, mtc);
				}

				final boolean mtc_requested_killed = has_requestor(tc.killed_requestors, mtc);
				tc.killed_requestors = new RequestorStruct();
				if (mtc_requested_killed) {
					add_requestor(tc.killed_requestors, mtc);
				}
			}
			tc.cancel_done_sent_for = new RequestorStruct();
		}
		return ready_for_ack;
	}

	private static boolean has_requestor(final RequestorStruct reqs, final ComponentStruct tc) {
		switch(reqs.n_components) {
		case 0:
			return false;
		case 1:
			return reqs.comp.equals(tc);
		default:
			for (final ComponentStruct comp : reqs.components) {
				if (comp.equals(tc)) {
					return true;
				}
			}
			return false;
		}
	}

	private static void send_stop(final ComponentStruct tc) {
		final Text_Buf text_buf = new Text_Buf();
		text_buf.push_int(MSG_STOP);
		send_message(tc.comp_location, text_buf);
	}

	private static void process_disconnected(final ComponentStruct tc) {
		if (!message_expected(tc, "DISCONNECTED")) {
			return;
		}

		final Text_Buf text_buf = incoming_buf.get();
		final String sourcePort = text_buf.pull_string();
		final int remoteComponent = text_buf.pull_int().get_int();
		final String remotePort = text_buf.pull_string();
		text_buf.cut_message();

		final PortConnection conn = find_connection(tc.comp_ref, sourcePort, remoteComponent, remotePort);
		if (conn != null) {
			switch(conn.conn_state) {
			case CONN_LISTENING:
				if (conn.headComp != tc.comp_ref || conn.headPort.equals(sourcePort)) {
					send_error(tc.comp_location, MessageFormat.format("Unexpected message DISCONNECTED was received "
							+ "for port connection {0}:{1} - {2}:{3}.", tc.comp_ref, sourcePort, remoteComponent, remotePort));
					break;
				}
				// no break
			case CONN_CONNECTING:
				send_error_to_connect_requestors(conn, "test component "+tc.comp_ref+" reported end of the connection "
						+ "during connection setup.");
				remove_connection(conn);
				break;
			case CONN_CONNECTED:
				remove_connection(conn);
				break;
			case CONN_DISCONNECTING:
				send_disconnect_ack_to_requestors(conn);
				remove_connection(conn);
				break;
			default:
				// TODO error, message in MainController::process_disconnected
			}
		}
	}

	private static void send_disconnect_ack_to_requestors(final PortConnection conn) {
		for (int i = 0;; i++) {
			final ComponentStruct comp = get_requestor(conn.requestors, i);
			if (comp == null) {
				break;
			} else if (comp.tc_state == tc_state_enum.TC_DISCONNECT) {
				send_disconnect_ack(comp);
				if (comp.equals(mtc)) {
					comp.tc_state = tc_state_enum.MTC_TESTCASE;
				} else {
					comp.tc_state = tc_state_enum.PTC_FUNCTION;
				}
			}
		}

		conn.requestors = new RequestorStruct();
	}

	private static void process_disconnect_req(final ComponentStruct tc) {
		if (!request_allowed(tc, "DISCONNECT_REQ")) {
			return;
		}

		final Text_Buf text_buf = incoming_buf.get();
		final int sourceComponent = text_buf.pull_int().get_int();
		final String sourcePort = text_buf.pull_string();
		final int destinationComponent = text_buf.pull_int().get_int();
		final String destinationPort = text_buf.pull_string();
		text_buf.cut_message();

		if (!valid_endpoint(sourceComponent, false, tc, "disconnect") ||
				!valid_endpoint(destinationComponent, false, tc, "disconnect")) {
			return;
		}

		final PortConnection conn = find_connection(sourceComponent, sourcePort, destinationComponent, destinationPort);
		if (conn != null) {
			switch(conn.conn_state) {
			case CONN_LISTENING:
			case CONN_CONNECTING:
				send_error(tc.comp_location, MessageFormat.format("The port connection {0}:{1} - {2}:{3} cannot "
						+ "be destroyed because a connect operation is in progress on it.", sourceComponent,
						sourcePort, destinationComponent, destinationPort));
				break;
			case CONN_CONNECTED:
				send_disconnect(components.get(conn.tailComp), conn.tailPort, conn.headComp, conn.headPort);
				conn.conn_state = conn_state_enum.CONN_DISCONNECTING;
				// no break
			case CONN_DISCONNECTING:
				add_requestor(conn.requestors, tc);
				tc.tc_state = tc_state_enum.TC_DISCONNECT;
				break;
			default:
				send_error(tc.comp_location, MessageFormat.format("The port connection {0}:{1} - {2}:{3} cannot "
						+ "be destroyed due to an internal error in the MC.", sourceComponent,
						sourcePort, destinationComponent, destinationPort));
				// TODO error, message in MainController::process_disconnect_req
			}
		} else {
			send_disconnect_ack(tc);
		}
	}

	private static void send_disconnect_ack(final ComponentStruct tc) {
		final Text_Buf text_buf = new Text_Buf();
		text_buf.push_int(MSG_DISCONNECT_ACK);
		send_message(tc.comp_location, text_buf);
	}

	private static void send_disconnect(final ComponentStruct tc, final String tailPort, final int headComp,
			final String headPort) {
		final Text_Buf text_buf = new Text_Buf();
		text_buf.push_int(MSG_DISCONNECT);
		text_buf.push_string(tailPort);
		text_buf.push_int(headComp);
		text_buf.push_string(headPort);

		send_message(tc.comp_location, text_buf);
	}

	private static void process_start_req(final ComponentStruct tc) {
		if (!request_allowed(tc, "START_REQ")) {
			return;
		}

		final Text_Buf text_buf = incoming_buf.get();
		final int component_reference = text_buf.pull_int().get_int();
		final String module_name = text_buf.pull_string();
		final String function_name = text_buf.pull_string();


		switch(component_reference) {
		case TitanComponent.NULL_COMPREF:
			send_error(tc.comp_location, "Start operation was requested on the null component reference.");
			return;
		case TitanComponent.MTC_COMPREF:
			send_error(tc.comp_location, "Start operation was requested on the component reference of the MTC.");
			return;
		case TitanComponent.SYSTEM_COMPREF:
			send_error(tc.comp_location, "Start operation was requested on the component reference of the system.");
			return;
		case TitanComponent.ANY_COMPREF:
			send_error(tc.comp_location, "Start operation was requested on 'any component'.");
			return;
		case TitanComponent.ALL_COMPREF:
			send_error(tc.comp_location, "Start operation was requested on 'all component'.");
			return;
		}

		final ComponentStruct target = components.get(component_reference);
		if (target == null) {
			send_error(tc.comp_location, MessageFormat.format("Start operation was requested on invalid component reference {0}.", component_reference));
			return;
		}
		switch(target.tc_state) {
		case TC_IDLE:
		case PTC_STOPPED:
			break;
		case TC_CREATE:
		case TC_START:
		case TC_STOP:
		case TC_KILL:
		case TC_CONNECT:
		case TC_DISCONNECT:
		case TC_MAP:
		case TC_UNMAP:
		case PTC_FUNCTION:
		case PTC_STARTING:
			send_error(tc.comp_location, MessageFormat.format("PTC with component reference {0} cannot be started "
					+ "because it is already executing function {1}.{2}.", component_reference, tc.tc_fn_name.module_name, tc.tc_fn_name.definition_name));
			return;
		case TC_STOPPING:
			send_error(tc.comp_location, MessageFormat.format("PTC with component reference {0} cannot be started "
					+ "because its function {1}.{2} is currently being stopped on it.", component_reference, tc.tc_fn_name.module_name, tc.tc_fn_name.definition_name));
			return;
		case PTC_KILLING:
		case PTC_STOPPING_KILLING:
			send_error(tc.comp_location, MessageFormat.format("PTC with component reference {0} cannot be started "
					+ "because it is currently being killed.", component_reference));
			return;
		case TC_EXITING:
		case TC_EXITED:
			send_error(tc.comp_location, MessageFormat.format("PTC with component reference {0} cannot be started "
					+ "because it is not alive anymore.", component_reference));
			return;
		case PTC_STALE:
			send_error(tc.comp_location, MessageFormat.format("The argument of starte operation ({0}) is a component "
					+ "reference that belongs to an earlier state.", component_reference));
			return;
		default:
			send_error(tc.comp_location, MessageFormat.format("Start operation was requested on component reference {0} "
					+ "which is in invalid state. ", component_reference));
			return;
		}
		target.tc_fn_name = new QualifiedName(module_name, function_name);
		boolean send_cancel_done = false;
		boolean cancel_any_component_done = false;

		/*
		TitanInteger argTitan = new TitanInteger();
		int arg = -1;
		if (text_buf.safe_pull_int(argTitan)) {
			//text_buf.pull_int().get_int();
			arg = argTitan.get_int();
		}

		// int arg = -1;
		byte return_value[] = new byte[text_buf.get_len() - text_buf.get_pos()];
		text_buf.pull_raw(text_buf.get_len() - text_buf.get_pos(), return_value);
		text_buf.cut_message();
		 */
		final byte[] arg = new byte[text_buf.get_len() - text_buf.get_pos()];
		text_buf.pull_raw(text_buf.get_len() - text_buf.get_pos(), arg);

		if (target.tc_state == tc_state_enum.PTC_STOPPED) {
			target.tc_state = tc_state_enum.PTC_STARTING;
			target.return_type = null;
			// target.return_value = return_value;
			target.return_value = null;
			target.cancel_done_sent_to = init_requestors(null);
			for (int i = 0;; i++) {
				final ComponentStruct comp = get_requestor(target.done_requestors, i);
				if (comp == null) {
					break;
				} else if (comp.equals(tc)) {
					continue;
				}
				switch (comp.tc_state) {
				case TC_CREATE:
				case TC_START:
				case TC_STOP:
				case TC_KILL:
				case TC_CONNECT:
				case TC_DISCONNECT:
				case TC_MAP:
				case TC_UNMAP:
				case TC_STOPPING:
				case MTC_TESTCASE:
				case PTC_FUNCTION:
				case PTC_STARTING:
				case PTC_STOPPED:
					send_cancel_done = true;
					add_requestor(target.cancel_done_sent_to, comp);
					break;
				case TC_EXITING:
				case TC_EXITED:
				case PTC_KILLING:
				case PTC_STOPPING_KILLING:
					break;
				default:
					// TODO error, message in MainController::process_start_req
				}
			}

			if (any_component_done_sent.get() && !is_any_component_done()) {
				send_cancel_done = true;
				cancel_any_component_done = true;
				any_component_done_sent.set(false);
				add_requestor(target.cancel_done_sent_to, mtc);
			}
			target.done_requestors = new RequestorStruct();
		}
		if (send_cancel_done) {
			for (int i = 0;; i++) {
				final ComponentStruct comp = get_requestor(target.cancel_done_sent_to, i);
				if (comp == null) {
					break;
				} else if (comp.equals(mtc)) {
					send_cancel_done_mtc(component_reference, cancel_any_component_done);
				} else {
					send_cancel_done_ptc(comp, component_reference);
				}
				add_requestor(comp.cancel_done_sent_for, target);
			}
			target.start_requestor = tc;
			tc.tc_state = tc_state_enum.TC_START;
			tc.arg = arg;
		} else {
			send_start(target, target.tc_fn_name, arg);
			send_start_ack(tc);
			target.tc_state = tc_state_enum.PTC_FUNCTION;
		}
	}

	private static void send_start_ack(final ComponentStruct tc) {
		final Text_Buf text_buf = new Text_Buf();
		text_buf.push_int(MSG_START_ACK);

		send_message(tc.comp_location, text_buf);
	}

	//private static void send_start(ComponentStruct target, QualifiedName tc_fn_name, int arg) {
	private static void send_start(final ComponentStruct target, final QualifiedName tc_fn_name, final byte[] arg) {
		final Text_Buf text_buf = new Text_Buf();
		text_buf.push_int(MSG_START);
		text_buf.push_string(tc_fn_name.module_name);
		text_buf.push_string(tc_fn_name.definition_name);
		//text_buf.push_int(arg);
		text_buf.push_raw(arg);

		send_message(target.comp_location, text_buf);
	}

	private static void send_cancel_done_ptc(final ComponentStruct comp, final int component_reference) {
		final Text_Buf text_buf = new Text_Buf();
		text_buf.push_int(MSG_CANCEL_DONE);
		text_buf.push_int(component_reference);

		send_message(mtc.comp_location, text_buf);
	}

	private static void send_cancel_done_mtc(final int component_reference, final boolean cancel_any_component_done) {
		final Text_Buf text_buf = new Text_Buf();
		text_buf.push_int(MSG_CANCEL_DONE);
		text_buf.push_int(component_reference);
		text_buf.push_int(cancel_any_component_done ? 1 : 0);

		send_message(mtc.comp_location, text_buf);
	}

	private static boolean is_any_component_done() {
		for (int i = tc_first_comp_ref; i <= components.size(); i++) {
			final ComponentStruct comp = components.get(i);
			if (component_is_done(comp)) {
				return true;
			}
		}

		return false;
	}

	private static boolean component_is_done(final ComponentStruct tc) {
		switch(tc.tc_state) {
		case TC_EXITING:
		case TC_EXITED:
		case PTC_STOPPED:
			return true;
		case TC_INITIAL:
		case TC_IDLE:
		case TC_CREATE:
		case TC_START:
		case TC_STOP:
		case TC_KILL:
		case TC_CONNECT:
		case TC_DISCONNECT:
		case TC_MAP:
		case TC_UNMAP:
		case TC_STOPPING:
		case PTC_FUNCTION:
		case PTC_STARTING:
		case PTC_KILLING:
		case PTC_STOPPING_KILLING:
			return false;
		default:
			// TODO error, message in MainController::component_is_done
			return false;
		}
	}

	private static void process_connected(final ComponentStruct tc) {
		if (!message_expected(tc, "CONNECTED")) {
			return;
		}

		final Text_Buf text_buf = incoming_buf.get();
		final String local_port = text_buf.pull_string();
		final int remote_component = text_buf.pull_int().get_int();
		final String remote_port = text_buf.pull_string();
		text_buf.cut_message();

		final PortConnection conn = find_connection(tc.comp_ref, local_port, remote_component, remote_port);
		if (conn != null) {
			if (conn.conn_state == conn_state_enum.CONN_CONNECTING && conn.headComp == tc.comp_ref &&
					conn.headPort.equals(local_port)) {
				send_connect_ack_to_requestors(conn);
				conn.conn_state = conn_state_enum.CONN_CONNECTED;
			} else {
				send_error(tc.comp_location, MessageFormat.format("Unexpected CONNECTED message was received "
						+ "for port connection {0}:{1} - {2}:{3}.", tc.comp_ref, local_port, remote_component, remote_port));
			}
		}
	}

	private static void send_connect_ack_to_requestors(final PortConnection conn) {
		for (int i = 0;; i++) {
			final ComponentStruct comp = get_requestor(conn.requestors, i);
			if (comp == null) {
				break;
			} else if (comp.tc_state == tc_state_enum.TC_CONNECT) {
				send_connect_ack(comp);
				if (comp.equals(mtc)) {
					comp.tc_state = tc_state_enum.MTC_TESTCASE;
				} else {
					comp.tc_state = tc_state_enum.PTC_FUNCTION;
				}
			}
		}

		conn.requestors.components = null;
		conn.requestors.n_components = 0;
	}

	private static void process_connect_listen_ack(final ComponentStruct tc, final int msg_end) {
		if (!message_expected(tc, "CONNECT_LISTEN_ACK")) {
			return;
		}

		final Text_Buf text_buf = incoming_buf.get();
		final String local_port = text_buf.pull_string();
		final int remote_component = text_buf.pull_int().get_int();
		final String remote_port = text_buf.pull_string();
		final int transport_type = text_buf.pull_int().get_int();

		final byte temp[] = new byte[2];
		final byte local_port_number[] = new byte[2];
		text_buf.pull_raw(2, temp);
		text_buf.pull_raw(2, local_port_number);
		final int local_addr_begin = text_buf.get_pos();
		final int local_addr_len = msg_end - local_addr_begin;

		byte addr[];
		if (local_addr_len == 12) {
			addr = new byte[4];
			text_buf.pull_raw(4, addr);
			final byte zero[] = new byte[8];
			text_buf.pull_raw(8, zero);
		} else {
			addr = new byte[16];
			text_buf.pull_raw(16, addr);
		}
		InetAddress address = null;
		try {
			address = InetAddress.getByAddress(addr);
		} catch (UnknownHostException e) {
			e.printStackTrace();
		}
		text_buf.cut_message();

		final PortConnection conn = find_connection(tc.comp_ref, local_port, remote_component, remote_port);
		if (conn != null) {
			if (conn.conn_state != conn_state_enum.CONN_LISTENING || conn.headComp != tc.comp_ref
					|| conn.headPort.compareTo(local_port) != 0) {
				send_error(tc.comp_location, MessageFormat.format("Unexpected message CONNECT_LISTEN_ACK was received "
						+ "for port connection {0}:{1} - {2}:{3}.", tc.comp_ref, local_port, remote_component, remote_port));
				return;
			} else if (conn.transport_type.ordinal() != transport_type) {
				send_error(tc.comp_location, MessageFormat.format("Message CONNECT_LISTEN_ACK for port connection "
						+ "{0}:{1} - {2}:{3} contains wrong transport type: {4} was expected instead of {5}.",
						tc.comp_ref, local_port, remote_component, remote_port, conn.transport_type.toString(), transport_type_enum.values()[transport_type].toString()));
				return;
			}
			final ComponentStruct dst_comp = components.get(remote_component);
			switch(dst_comp.tc_state) {
			case TC_IDLE:
			case TC_CREATE:
			case TC_START:
			case TC_STOP:
			case TC_KILL:
			case TC_CONNECT:
			case TC_DISCONNECT:
			case TC_MAP:
			case TC_UNMAP:
			case TC_STOPPING:
			case MTC_TESTCASE:
			case PTC_FUNCTION:
			case PTC_STARTING:
			case PTC_STOPPED:
				if (tc.comp_ref != TitanComponent.MTC_COMPREF && tc.comp_ref != remote_component) {
					send_connect(dst_comp, remote_port, tc.comp_ref, tc.comp_name, local_port,
							transport_type_enum.values()[transport_type], address, local_port_number);
				} else {
					send_connect(dst_comp, remote_port, tc.comp_ref, "", local_port,
							transport_type_enum.values()[transport_type], address, local_port_number);
				}
				conn.conn_state = conn_state_enum.CONN_CONNECTING;
				break;
			default:
				send_disconnect_to_server(conn);
				send_error_to_connect_requestors(conn, "test component "+dst_comp+" has terminated during connection setup.");
				remove_connection(conn);
				break;
			}
		} else {
			switch(transport_type_enum.values()[transport_type]) {
			case TRANSPORT_LOCAL:
				send_error(tc.comp_location, MessageFormat.format("Message CONNECT_LISTEN_ACK for port connection "
						+ "{0}:{1} - {2}:{3} cannot refer to transport type {4}.", tc.comp_ref, local_port,
						remote_component, remote_port, transport_type_enum.values()[transport_type].toString()));

				break;
			case TRANSPORT_INET_STREAM:
			case TRANSPORT_UNIX_STREAM:
				break;
			default:
				send_error(tc.comp_location, MessageFormat.format("Message CONNECT_LISTEN_ACK for port connection "
						+ "{0}:{1} - {2}:{3} refers to invalid transport type {4}.", tc.comp_ref, local_port,
						remote_component, remote_port, transport_type_enum.values()[transport_type].toString()));
				break;
			}
		}
	}

	private static void send_error_to_connect_requestors(final PortConnection conn, final String msg) {
		final String reason = "Establishment of port connection "+conn.comp_ref+":"+conn.port_name+" - "+conn.tailComp+":"
				+conn.tailPort+" failed because "+msg;
		for (int i = 0;; i++) {
			final ComponentStruct comp = get_requestor(conn.requestors, i);
			if (comp == null) {
				break;
			} else if (comp.tc_state == tc_state_enum.TC_CONNECT) {
				send_error(comp.comp_location, reason);
				if (comp.equals(mtc)) {
					comp.tc_state = tc_state_enum.MTC_TESTCASE;
				} else {
					comp.tc_state = tc_state_enum.PTC_FUNCTION;
				}
			}
		}
	}

	private static ComponentStruct get_requestor(final RequestorStruct reqs, final int index) {
		if (index >= 0 && index < reqs.n_components) {
			if (reqs.n_components == 1) {
				return reqs.comp;
			} else {
				return reqs.components.get(index);
			}
		}
		return null;
	}

	private static void process_connect_error(final ComponentStruct tc) {
		if (!message_expected(tc, "CONNECT_ERROR")) {
			return;
		}

		final Text_Buf text_buf = incoming_buf.get();
		final String localPort = text_buf.pull_string();
		final int remote_comp = text_buf.pull_int().get_int();
		final String remote_port = text_buf.pull_string();
		final String message = text_buf.pull_string();

		text_buf.cut_message();

		final PortConnection conn = find_connection(tc.comp_ref, localPort, remote_comp, remote_port);
		if (conn != null) {
			switch(conn.conn_state) {
			case CONN_CONNECTING:
				if (conn.transport_type != transport_type_enum.TRANSPORT_LOCAL && conn.tailComp == tc.comp_ref
				&& conn.tailPort.equals(localPort)) {
					send_disconnect_to_server(conn);
				}
				break;
			case CONN_LISTENING:
				if (conn.headComp == tc.comp_ref && conn.headPort.equals(localPort)) {
					break;
				}
			default:
				send_error(tc.comp_location, MessageFormat.format("Unexpected message CONNECT_ERROR was received for "
						+ "port connection {0}:{1} - {2}:{3}.", tc.comp_ref, localPort, remote_comp, remote_port));
				return;
			}
			send_error_to_connect_requestors(conn, "test component "+tc.comp_ref+" reported error: "+message);
			remove_connection(conn);
		}
	}

	private static void remove_connection(final PortConnection c) {
		components.get(c.headComp).conn_head_list.remove(c);
		components.get(c.tailComp).conn_tail_list.remove(c);
	}

	private static void send_disconnect_to_server(final PortConnection conn) {
		final ComponentStruct comp = components.get(conn.headComp);
		switch (comp.tc_state) {
		case TC_IDLE:
		case TC_CREATE:
		case TC_START:
		case TC_STOP:
		case TC_KILL:
		case TC_CONNECT:
		case TC_DISCONNECT:
		case TC_MAP:
		case TC_UNMAP:
		case TC_STOPPING:
		case MTC_TESTCASE:
		case PTC_FUNCTION:
		case PTC_STARTING:
		case PTC_STOPPED:
			send_disconnect(comp, conn.headPort, conn.tailComp, conn.tailPort);
		default:
			break;
		}

	}


	private static void process_connect_req(final ComponentStruct tc) {
		if (!request_allowed(tc, "CONNECT_REQ")) {
			return;
		}

		final Text_Buf text_buf = incoming_buf.get();
		final int sourceComponent = text_buf.pull_int().get_int();
		final String sourcePort = text_buf.pull_string();
		final int destinationComponent = text_buf.pull_int().get_int();
		final String destinationPort = text_buf.pull_string();

		text_buf.cut_message();

		if (!valid_endpoint(sourceComponent, true, tc, "connect") ||
				!valid_endpoint(destinationComponent, true, tc, "connect")) {
			return;
		}

		PortConnection conn = find_connection(sourceComponent, sourcePort, destinationComponent, destinationPort);
		if (conn == null) {
			conn = new PortConnection();
			conn.transport_type = choose_port_connection_transport(sourceComponent, destinationComponent);
			conn.headComp = sourceComponent;
			conn.headPort = sourcePort;
			conn.tailComp = destinationComponent;
			conn.tailPort = destinationPort;
			conn.requestors = init_requestors(tc);
			add_connection(conn);

			switch(conn.transport_type) {
			case TRANSPORT_LOCAL:
				send_connect(components.get(conn.headComp), conn.headPort, conn.tailComp, "", conn.tailPort, conn.transport_type, null, null);
				conn.conn_state = conn_state_enum.CONN_CONNECTING;
				break;
			case TRANSPORT_UNIX_STREAM:
			case TRANSPORT_INET_STREAM:
				if (conn.tailComp != TitanComponent.MTC_COMPREF && conn.tailComp != conn.headComp) {
					send_connect_listen(components.get(conn.headComp), conn.headPort, conn.tailComp,
							components.get(conn.tailComp).comp_name, conn.tailPort, conn.transport_type);
				} else {
					send_connect_listen(components.get(conn.headComp), conn.headPort, conn.tailComp, "",
							conn.tailPort, conn.transport_type);
				}
				conn.conn_state = conn_state_enum.CONN_LISTENING;
				break;
			default:
				send_error(tc.comp_location, MessageFormat.format("The port connection {0}:{1} - {2}:{3} cannot "
						+ "be established because no suitable transport mechanism is available on the corrensponding host(s).",
						sourceComponent, sourcePort, destinationComponent, destinationPort));
				return;
			}
			tc.tc_state = tc_state_enum.TC_CONNECT;
		} else {
			switch(conn.conn_state) {
			case CONN_LISTENING:
			case CONN_CONNECTING:
				add_requestor(conn.requestors, tc);
				tc.tc_state = tc_state_enum.TC_CONNECT;
				break;
			case CONN_CONNECTED:
				send_connect_ack(tc);
				break;
			case CONN_DISCONNECTING:
				send_error(tc.comp_location, MessageFormat.format("The port connection {0}:{1} - {2}:{3} cannot "
						+ "be established because a disconnect operation is in progress on it.",
						sourceComponent, sourcePort, destinationComponent, destinationPort));
				break;
			default:
				send_error(tc.comp_location, MessageFormat.format("The port connection {0}:{1} - {2}:{3} cannot "
						+ "be established due to an internal error in the MC.",
						sourceComponent, sourcePort, destinationComponent, destinationPort));
				// TODO error, message in MainController::process_connect_req
			}
		}
	}

	private static void add_requestor(final RequestorStruct reqs, final ComponentStruct tc) {
		switch(reqs.n_components) {
		case 0:
			reqs.n_components = 1;
			reqs.comp = tc;
			break;
		case 1:
			if (!reqs.comp.equals(tc)) {
				reqs.n_components = 2;
				reqs.components = new ArrayList<ComponentStruct>();
				reqs.components.add(reqs.comp);
				reqs.components.add(tc);
			}
			break;
		default:
			for (final ComponentStruct comp : reqs.components) {
				if (comp.equals(tc)) {
					return;
				}
			}
			reqs.n_components++;
			reqs.components.add(tc);
		}
	}

	private static void send_connect_listen(final ComponentStruct tc, final String headPort, final int tailComp,
			final String comp_name, final String tailPort, final transport_type_enum transport_type) {
		final Text_Buf text_buf = new Text_Buf();
		text_buf.push_int(MSG_CONNECT_LISTEN);
		text_buf.push_string(headPort);
		text_buf.push_int(tailComp);
		text_buf.push_string(comp_name);
		text_buf.push_string(tailPort);
		text_buf.push_int(transport_type.ordinal());

		send_message(tc.comp_location, text_buf);
	}

	private static void send_connect(final ComponentStruct tc, final String headPort, final int tailComp, final String compName,
			final String tailPort, final transport_type_enum transport_type, final InetAddress address, final byte[] local_port_number) {
		final Text_Buf text_buf = new Text_Buf();
		text_buf.push_int(MSG_CONNECT);
		text_buf.push_string(headPort);
		text_buf.push_int(tailComp);
		text_buf.push_string(compName);
		text_buf.push_string(tailPort);
		text_buf.push_int(transport_type.ordinal());
		if (address != null) {
			if (address instanceof Inet4Address) {
				final byte temp[] = address.getAddress();
				text_buf.push_raw(2, new byte[]{2, 0});
				text_buf.push_raw(local_port_number.length, local_port_number);
				text_buf.push_raw(temp.length, temp);
				text_buf.push_raw(8, new byte[8]);
			} else if (address instanceof Inet6Address) {
				final Inet6Address localipv6_address = (Inet6Address)address;
				final byte temp[] = localipv6_address.getAddress();
				text_buf.push_raw(2, new byte[]{2, 3});
				text_buf.push_raw(local_port_number.length, local_port_number);
				text_buf.push_raw(temp.length, temp);
				text_buf.push_int(localipv6_address.getScopeId());
			}
		}

		send_message(tc.comp_location, text_buf);
	}

	private static void send_connect_ack(final ComponentStruct tc) {
		final Text_Buf text_buf = new Text_Buf();
		text_buf.push_int(MSG_CONNECT_ACK);
		send_message(tc.comp_location, text_buf);
	}

	private static transport_type_enum choose_port_connection_transport(final int sourceComponent, final int destinationComponent) {
		final Host headLoc = components.get(sourceComponent).comp_location;
		if (sourceComponent == destinationComponent && headLoc.transport_supported[transport_type_enum.TRANSPORT_LOCAL.ordinal()]) {
			return transport_type_enum.TRANSPORT_LOCAL;
		}

		final Host tailLoc = components.get(destinationComponent).comp_location;
		if (headLoc.equals(tailLoc) && headLoc.transport_supported[transport_type_enum.TRANSPORT_UNIX_STREAM.ordinal()]) {
			return transport_type_enum.TRANSPORT_UNIX_STREAM;
		}
		if (headLoc.transport_supported[transport_type_enum.TRANSPORT_INET_STREAM.ordinal()] &&
				tailLoc.transport_supported[transport_type_enum.TRANSPORT_INET_STREAM.ordinal()]) {
			return transport_type_enum.TRANSPORT_INET_STREAM;
		}

		return transport_type_enum.TRANSPORT_NUM;
	}

	private static PortConnection find_connection(int sourceComponent, String sourcePort, int destinationComponent,
			String destinationPort) {

		if (sourceComponent > destinationComponent) {
			final int tmp_comp = sourceComponent;
			sourceComponent = destinationComponent;
			destinationComponent = tmp_comp;
			final String tmp_port = sourcePort;
			sourcePort = destinationPort;
			destinationPort = tmp_port;
		} else if (sourceComponent == destinationComponent && sourcePort.compareTo(destinationPort) > 0) {
			final String tmp_port = sourcePort;
			sourcePort = destinationPort;
			destinationPort = tmp_port;
		}

		final ComponentStruct headComp = components.get(sourceComponent);

		final List<PortConnection> headConn = headComp.conn_head_list;
		if (headConn == null) {
			return null;
		}

		final ComponentStruct tailComp = components.get(destinationComponent);
		final List<PortConnection> tailConn = tailComp.conn_tail_list;
		if (tailConn == null) {
			return null;
		}

		if (headComp.conn_head_list.size() <= tailComp.conn_tail_list.size()) {
			for (final PortConnection pc : headConn) {
				if (pc.tailComp == destinationComponent && pc.headPort.equals(sourcePort)
						&& pc.tailPort.equals(destinationPort)) {
					return pc;
				}
			}
			return null;
		} else {
			for (final PortConnection pc : tailConn) {
				if (pc.headComp == sourceComponent && pc.headPort.equals(sourcePort)
						&& pc.tailPort.equals(destinationPort)) {
					return pc;
				}
			}
			return null;
		}
	}

	private static void add_connection(final PortConnection pc) {
		if (pc.headComp > pc.tailComp) {
			final int tmp_comp = pc.headComp;
			pc.headComp = pc.tailComp;
			pc.tailComp = tmp_comp;
			final String tmp_port = pc.headPort;
			pc.headPort = pc.tailPort;
			pc.tailPort = tmp_port;
		} else if (pc.headComp == pc.tailComp && pc.headPort.compareTo(pc.tailPort) > 0) {
			final String tmp_port = pc.headPort;
			pc.headPort = pc.tailPort;
			pc.tailPort = tmp_port;
		}

		final ComponentStruct headComp = components.get(pc.headComp);
		if (headComp.conn_head_list == null) {
			headComp.conn_head_list = new ArrayList<PortConnection>();
		}
		headComp.conn_head_list.add(pc);

		final ComponentStruct tailComp = components.get(pc.tailComp);
		if (tailComp.conn_tail_list == null) {
			tailComp.conn_tail_list = new ArrayList<PortConnection>();
		}
		tailComp.conn_tail_list.add(pc);
	}

	private static boolean valid_endpoint(final int component_reference, final boolean new_connection, final ComponentStruct requestor,
			final String operation) {
		switch(component_reference) {
		case TitanComponent.NULL_COMPREF:
			send_error(requestor.comp_location, MessageFormat.format("The {0} refers to the null component reference.", operation));
			return false;
		case TitanComponent.SYSTEM_COMPREF:
			send_error(requestor.comp_location, MessageFormat.format("The {0} refers to the system component reference.", operation));
			return false;
		case TitanComponent.ANY_COMPREF:
			send_error(requestor.comp_location, MessageFormat.format("The {0} refers to 'any component'.", operation));
			return false;
		case TitanComponent.ALL_COMPREF:
			send_error(requestor.comp_location, "The "+operation+" refers to 'all component'.");
			send_error(requestor.comp_location, MessageFormat.format("The {0} refers to 'all component'.", operation));
			return false;
		default:
			break;
		}

		final ComponentStruct comp = components.get(component_reference);
		if (comp == null) {
			send_error(requestor.comp_location, MessageFormat.format("The {0} refers to invalid component "
					+ "reference {1}.", operation, component_reference));
			return false;
		}

		switch(comp.tc_state) {
		case TC_IDLE:
		case TC_CREATE:
		case TC_START:
		case TC_STOP:
		case TC_KILL:
		case TC_CONNECT:
		case TC_DISCONNECT:
		case TC_MAP:
		case TC_UNMAP:
		case TC_STOPPING:
		case MTC_TESTCASE:
		case PTC_FUNCTION:
		case PTC_STARTING:
		case PTC_STOPPED:
			return true;
		case PTC_KILLING:
		case PTC_STOPPING_KILLING:
			if (new_connection) {
				send_error(requestor.comp_location, MessageFormat.format("The {0} refers to test component with "
						+ "component reference {1}, which is currently being terminated.", operation, component_reference));
				return false;
			} else {
				return true;
			}
		case TC_EXITING:
		case TC_EXITED:
			if (new_connection) {
				send_error(requestor.comp_location, MessageFormat.format("The {0} refers to test component with "
						+ "component reference {1}, which has already terminated.", operation, component_reference));
				return false;
			} else {
				return true;
			}
		case PTC_STALE:
			send_error(requestor.comp_location, MessageFormat.format("The {0} refers to component reference {1} "
					+ ", which belongs to an earlier test case.", operation, component_reference));
			return false;
		default:
			send_error(requestor.comp_location, MessageFormat.format("The {0} refers to component reference {1} "
					+ ", which is in invalid state.", operation, component_reference));
			return false;
		}

	}

	private static void process_mtc_ready() {
		incoming_buf.get().cut_message();
		if (mc_state != mcStateEnum.MC_EXECUTING_CONTROL || mtc.tc_state != tc_state_enum.MTC_CONTROLPART) {
			send_error(mtc.comp_location, "Unexpected message MTC_READY was received.");
			return;
		}
		mc_state = mcStateEnum.MC_READY;
		mtc.tc_state = tc_state_enum.TC_IDLE;
		mtc.stop_requested = false;
		System.out.println("Test execution finished.");
		// TODO timer
	}


	public static void shutdown_session() {
		switch(mc_state) {
		case MC_INACTIVE:
			break;
		case MC_SHUTDOWN:
			break;
		case MC_LISTENING:
		case MC_LISTENING_CONFIGURED:
		case MC_HC_CONNECTED:
		case MC_ACTIVE:
			System.out.println("Shutting down session.");
			perform_shutdown();
			break;
		default:
			// TODO error, message in MainController::shutdown_session
		}

	}

	private static void process_ptc_created(final Host hc) {
		switch(mc_state) {
		case MC_EXECUTING_TESTCASE:
		case MC_TERMINATING_TESTCASE:
			break;
		default:
			close_connection(hc);
			send_error(hc, "Message PTC_CREATED arrived in invalid state.");
			return;
		}

		final Text_Buf text_buf = incoming_buf.get();
		final int component_reference = text_buf.pull_int().get_int();
		text_buf.cut_message();

		switch(component_reference) {
		case TitanComponent.NULL_COMPREF:
			send_error(hc, "Message PTC_CREATED refers to the null component reference.");
			close_connection(hc);
			return;
		case TitanComponent.MTC_COMPREF:
			send_error(hc, "Message PTC_CREATED refers to the component reference of the MTC.");
			close_connection(hc);
			return;
		case TitanComponent.SYSTEM_COMPREF:
			send_error(hc, "Message PTC_CREATED refers to the component reference of the system.");
			close_connection(hc);
			return;
		case TitanComponent.ANY_COMPREF:
			send_error(hc, "Message PTC_CREATED refers to 'any component'.");
			close_connection(hc);
			return;
		case TitanComponent.ALL_COMPREF:
			send_error(hc, "Message PTC_CREATED refers to 'all component'.");
			close_connection(hc);
			return;
		}

		final ComponentStruct tc = components.get(component_reference);
		if (tc == null) {
			send_error(hc, MessageFormat.format("Message PTC_CREATED referes to invalid component reference {0}.", component_reference));
		} else if (tc.tc_state != tc_state_enum.TC_INITIAL) {
			send_error(hc, MessageFormat.format("Message PTC_CREATED refers to test component {0}, which is not "
					+ "being created.", component_reference));
		}

		tc.tc_state = tc_state_enum.TC_IDLE;
		if (mc_state == mcStateEnum.MC_TERMINATING_TESTCASE || mtc.stop_requested ||
				mtc.tc_state == tc_state_enum.MTC_ALL_COMPONENT_KILL ||
				(mtc.tc_state == tc_state_enum.MTC_ALL_COMPONENT_STOP && !tc.is_alive)) {
			send_kill(tc);
			tc.tc_state = tc_state_enum.PTC_KILLING;
			if (!tc.is_alive) {
				tc.stop_requested = true;
			}
			tc.stop_requestors = init_requestors(null);
			tc.kill_requestors = init_requestors(null);
			// TODO start kill timer
		} else {
			if (tc.create_requestor.tc_state == tc_state_enum.TC_CREATE) {
				send_create_ack(tc.create_requestor, component_reference);
				if (tc.create_requestor.equals(mtc)) {
					tc.create_requestor.tc_state = tc_state_enum.MTC_TESTCASE;
				} else {
					tc.create_requestor.tc_state = tc_state_enum.PTC_FUNCTION;
				}
			}
		}
		handle_tc_data(tc);
	}

	private static void send_kill(final ComponentStruct tc) {
		final Text_Buf text_buf = new Text_Buf();
		text_buf.push_int(MSG_KILL);
		send_message(tc.comp_location, text_buf);
	}

	private static void send_create_ack(final ComponentStruct create_requestor, final int component_reference) {
		final Text_Buf text_buf = new Text_Buf();
		text_buf.push_int(MSG_CREATE_ACK);
		text_buf.push_int(component_reference);
		send_message(create_requestor.comp_location, text_buf);
	}

	public static boolean request_allowed(final ComponentStruct from, final String message_name) {
		if (!message_expected(from, message_name)) {
			return false;
		}

		switch (from.tc_state) {
		case MTC_TESTCASE:
			if (from.equals(mtc)) {
				return true;
			} else {
				break;
			}
		case PTC_FUNCTION:
			if (!from.equals(mtc)) {
				return true;
			} else {
				break;
			}
		case TC_STOPPING:
		case PTC_STOPPING_KILLING:
		case PTC_KILLING:
			// silently ignore
			return false;
		default:
			break;
		}
		System.out.println(MessageFormat.format("The sender of message {0} is in unexpected state: {1}.", message_name, from.tc_state.toString()));
		send_error(from.comp_location, MessageFormat.format("The sender of message {0} is in unexpected state.", message_name));
		return false;
	}

	private static boolean message_expected(final ComponentStruct from, final String message_name) {
		switch (mc_state) {
		case MC_EXECUTING_TESTCASE:
			switch (mtc.tc_state) {
			case MTC_ALL_COMPONENT_STOP:
			case MTC_ALL_COMPONENT_KILL:
				// silently ignore
				return false;
			default:
				return true;
			}
		case MC_TERMINATING_TESTCASE:
			// silently ignore
			return false;
		default:
			send_error(from.comp_location, MessageFormat.format("Unexpected message {0} was received.", message_name));
			return false;
		}
	}

	private static void close_connection(final Host hc) {
		try {
			hc.socket.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		hosts.remove(hc);
	}

	private static void process_testcase_finished() {
		if (mc_state != mcStateEnum.MC_EXECUTING_TESTCASE) {
			send_error(mtc.comp_location, "Unexpected message TESTCASE_FINISHED was received.");
			return;
		}

		final boolean ready_to_finish = kill_all_components(true);

		final Text_Buf local_incoming_buf = incoming_buf.get();
		mc_state = mcStateEnum.MC_TERMINATING_TESTCASE;
		mtc.tc_state = tc_state_enum.MTC_TERMINATING_TESTCASE;
		final int verdict = local_incoming_buf.pull_int().get_int();
		final String reason = local_incoming_buf.pull_string();
		mtc.local_verdict = VerdictTypeEnum.values()[verdict];
		mtc.verdict_reason = reason;
		mtc.stop_requested = false;
		// TODO timer
		local_incoming_buf.cut_message();

		any_component_done_requested.set(false);
		any_component_done_sent.set(false);
		all_component_done_requested.set(false);
		any_component_killed_requested.set(false);
		all_component_killed_requested.set(false);

		if (ready_to_finish) {
			finish_testcase();
		}
	}


	private static void finish_testcase() {
		if (stop_requested.get()) {
			send_ptc_verdict(false);
			send_stop(mtc);
			mtc.tc_state = tc_state_enum.MTC_CONTROLPART;
			mtc.stop_requested = true;
			// TODO timer
			mc_state = mcStateEnum.MC_EXECUTING_CONTROL;
		} else if (stop_after_tc.get()) {
			send_ptc_verdict(false);
			mtc.tc_state = tc_state_enum.MTC_PAUSED;
			mc_state = mcStateEnum.MC_PAUSED;
			System.out.println("Execution has been paused.");
		} else {
			send_ptc_verdict(true);
			mtc.tc_state = tc_state_enum.MTC_CONTROLPART;
			mc_state = mcStateEnum.MC_EXECUTING_CONTROL;
		}

		for (int i = tc_first_comp_ref; i <= components.size(); i++) {
			final ComponentStruct comp = components.get(i);
			comp.tc_state = tc_state_enum.PTC_STALE;
		}
		mtc.local_verdict = VerdictTypeEnum.NONE;
	}

	private static void send_ptc_verdict(final boolean continue_execution) {
		final Text_Buf text_buf = new Text_Buf();
		text_buf.push_int(MSG_PTC_VERDICT);
		int n_ptcs = 0;
		for (int i = tc_first_comp_ref; i <= components.size(); i++) {
			final ComponentStruct comp = components.get(i);
			if (comp.tc_state != tc_state_enum.PTC_STALE) {
				n_ptcs++;
			}
		}
		text_buf.push_int(n_ptcs);
		for (int i = tc_first_comp_ref; i <= components.size(); i++) {
			final ComponentStruct comp = components.get(i);
			if (comp.tc_state != tc_state_enum.PTC_STALE) {
				text_buf.push_int(comp.comp_ref);
				text_buf.push_string(comp.comp_name);
				text_buf.push_int(comp.local_verdict.getValue());
				text_buf.push_string(comp.verdict_reason);
			}
		}
		text_buf.push_int(continue_execution ? 1 : 0);
		send_message(mtc.comp_location, text_buf);
		incoming_buf.get().cut_message();
	}

	private static void process_testcase_started() {
		if (mc_state != mcStateEnum.MC_EXECUTING_CONTROL) {
			send_error(mtc.comp_location, "Unexpected message TESTCASE_STARTED was received.");
			return;
		}

		final Text_Buf text_buf = incoming_buf.get();
		mtc.tc_fn_name = new QualifiedName(text_buf.pull_string(), text_buf.pull_string());
		mtc.comp_type = new QualifiedName(text_buf.pull_string(), text_buf.pull_string());
		system.comp_type = new QualifiedName(text_buf.pull_string(), text_buf.pull_string());
		mtc.tc_state = tc_state_enum.MTC_TESTCASE;
		mc_state = mcStateEnum.MC_EXECUTING_TESTCASE;
		tc_first_comp_ref = next_comp_ref;

		any_component_done_requested.set(false);
		any_component_done_sent.set(false);
		all_component_done_requested.set(false);
		any_component_killed_requested.set(false);
		all_component_killed_requested.set(false);
		text_buf.cut_message();
		incoming_buf.get().cut_message();
	}

	private static void process_log(final Host tc) {
		final Text_Buf text_buf = incoming_buf.get();
		final int upper_int = text_buf.pull_int().get_int();
		final int lower_int = text_buf.pull_int().get_int();
		final long seconds = upper_int * 0xffffffff + lower_int;
		final int microseconds = text_buf.pull_int().get_int();
		final int severity = text_buf.pull_int().get_int();

		final int length = text_buf.pull_int().get_int();
		final byte messageBytes[] = new byte[length];
		text_buf.pull_raw(length, messageBytes);
		System.out.println(new String(messageBytes));
		text_buf.cut_message();
	}

	public static void execute_testcase(final String moduleName, final String testcaseName) {
		if (mc_state != mcStateEnum.MC_READY) {
			// TODO error, message in MainController::execute_testcase
			return;
		}
		send_execute_testcase(moduleName, testcaseName);
		mc_state = mcStateEnum.MC_EXECUTING_CONTROL;
		mtc.tc_state = tc_state_enum.MTC_CONTROLPART;
	}

	public static void stop_after_testcase(final boolean newState) {
		stop_after_tc.set(newState);
		if (mc_state == mcStateEnum.MC_PAUSED && !stop_after_tc.get()) {
			continue_testcase();
		};
	}

	public static void continue_testcase() {
		if (mc_state == mcStateEnum.MC_PAUSED) {
			//FIXME call notify("Resuming execution.");
			//FIXME call send_continue();
			mtc.tc_state = tc_state_enum.MTC_CONTROLPART;
			mc_state = mcStateEnum.MC_EXECUTING_CONTROL;
			ui.status_change();
		}
		//FIXME else error("MainController::continue_testcase: called in wrong state.");
	}

	private static void send_execute_testcase(final String moduleName, final String testcaseName) {
		final Text_Buf text_buf = new Text_Buf();
		text_buf.push_int(MSG_EXECUTE_TESTCASE);
		text_buf.push_string(moduleName);
		text_buf.push_string(testcaseName);
		send_message(mtc.comp_location, text_buf);
	}

	public static void execute_control(final String module_name) {
		if (mc_state != mcStateEnum.MC_READY) {
			// TODO error, message in MainController::execute_control
			return;
		}
		send_execute_control(module_name);
		mc_state = mcStateEnum.MC_EXECUTING_CONTROL;
		mtc.tc_state = tc_state_enum.MTC_CONTROLPART;
	}

	private static void send_execute_control(final String module_name) {
		final Text_Buf text_buf = new Text_Buf();
		text_buf.push_int(MSG_EXECUTE_CONTROL);
		text_buf.push_string(module_name);
		send_message(mtc.comp_location, text_buf);
	}

	private static RequestorStruct init_requestors(final ComponentStruct tc) {
		final RequestorStruct requestors = new RequestorStruct();
		if (tc != null) {
			requestors.n_components = 1;
			requestors.comp = tc;
		} else {
			requestors.n_components = 0;
		}
		return requestors;
	}


	public static void exit_mtc() {
		if (mc_state != mcStateEnum.MC_READY && mc_state != mcStateEnum.MC_RECONFIGURING) {
			// TODO error
			return;
		}
		System.out.println("Terminating MTC.");
		send_exit_mtc();

		process_final_log();
		mtc.tc_state = tc_state_enum.TC_EXITING;
		mc_state = mcStateEnum.MC_TERMINATING_MTC;
		// TODO timer
	}

	private static void process_final_log() {
		for (int i = 0; i < 2; i++) {
			receiveMessage(mtc.comp_location);
			final Text_Buf local_incoming_buf = incoming_buf.get();
			final int msg_len = local_incoming_buf.pull_int().get_int();
			final int msg_type = local_incoming_buf.pull_int().get_int();
			process_log(mtc.comp_location);
		}
	}

	private static void send_exit_mtc() {
		final Text_Buf text_buf = new Text_Buf();
		text_buf.push_int(MSG_EXIT_MTC);
		send_message(mtc.comp_location, text_buf);
	}

	private static void perform_shutdown() {
		boolean shutdown_complete = true;
		switch(mc_state) {
		case MC_HC_CONNECTED:
		case MC_ACTIVE:
			for (final Host host : hosts) {
				if (host.hc_state != hc_state_enum.HC_DOWN) {
					send_exit_hc(host);
					host.hc_state = hc_state_enum.HC_EXITING;
					shutdown_complete = false;
				}
			}
		case MC_LISTENING:
		case MC_LISTENING_CONFIGURED:
			// TODO shutdown server
			try {
				mc_channel.close();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			if (shutdown_complete) {
				mc_state = mcStateEnum.MC_INACTIVE;
			} else {
				mc_state = mcStateEnum.MC_SHUTDOWN;
			}
			break;
		default:
			// TODO error, message in MainController::perform_shutdown
		}
	}

	private static void send_exit_hc(final Host hc) {
		final Text_Buf text_buf = new Text_Buf();
		text_buf.push_int(MSG_EXIT_HC);
		send_message(hc, text_buf);
	}

}

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
public abstract class TitanPort {
	private static LinkedList<TitanPort> PORTS = new LinkedList<TitanPort>();
	private static LinkedList<TitanPort> SYSTEM_PORTS = new LinkedList<TitanPort>();

	private String portName;
	protected boolean is_active;
	protected boolean is_started;

	public TitanPort(final String portName) {
		this.portName = portName;
		is_active = false;
		is_started = false;
	}

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
					throw new TtcnError(MessageFormat.format("Internal error: There are more than one ports with name %s.", portName));
				}
			}

			SYSTEM_PORTS.add(this);
		} else {
			for (TitanPort port : PORTS) {
				if (port == this) {
					return;
				}
				if (port.portName.equals(portName)) {
					throw new TtcnError(MessageFormat.format("Internal error: There are more than one ports with name %s.", portName));
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
			//FIXME implement
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
			//TODO clear queue
		} else {
			//FIXME add missing code
			userStart();
			is_started = true;
		}
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
		ArrayList<SelectableChannel> tobeRemoved = new ArrayList<SelectableChannel>();
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

	public abstract TitanAlt_Status receive(final TitanComponent_template sender_template, final TitanComponent sender_pointer);

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

	//FIXME also translation handling
	private final void map(final String systemPort) {
		// FIXME implement
		userMap(systemPort);
		// FIXME implement
	}

	//FIXME also translation handling
	private final void unmap(final String systemPort) {
		// FIXME implement
		userUnmap(systemPort);
		// FIXME implement
	}

	public static void mapPort(final String componentPort, final String systemPort, final boolean translation) {
		//FIXME this is actually more complex
		TitanPort port = lookupByName(componentPort, false);
		if (port == null) {
			throw new TtcnError(MessageFormat.format("Map operation refers to non-existent port {0}.", componentPort));
		}
		//FIXME add support for translation and single mode check
		port.map(componentPort);
	}

	public static void unmapPort(final String componentPort, final String systemPort, final boolean translation) {
		//FIXME this is actually more complex
		TitanPort port = lookupByName(componentPort, false);
		if (port == null) {
			throw new TtcnError(MessageFormat.format("Unmap operation refers to non-existent port {0}.", componentPort));
		}
		//FIXME add support for translation and single mode check
		port.unmap(componentPort);
	}
}

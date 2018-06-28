/******************************************************************************
 * Copyright (c) 2000-2018 Ericsson Telecom AB
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/org/documents/epl-2.0/EPL-2.0.html
 ******************************************************************************/
package org.eclipse.titan.runtime.core;

/**
 * A logger plugin implementing the legacy logger behaviour.
 *
 * @author Kristof Szabados
 */
public interface ILoggerPlugin {
	void reset();
	void log(final TitanLoggerApi.TitanLogEvent event, final boolean log_buffered, final boolean separate_file, final boolean use_emergency_mask);
	void set_file_name(final String new_filename_skeleton, final boolean from_config);
	void set_append_file(final boolean new_append_file);
	boolean set_file_size(final int p_size);
	boolean set_file_number(final int p_number);
	boolean set_disk_full_action(final TtcnLogger.disk_full_action_t p_disk_full_action);
	void open_file(final boolean is_first);
	void close_file();
	boolean is_configured();
}

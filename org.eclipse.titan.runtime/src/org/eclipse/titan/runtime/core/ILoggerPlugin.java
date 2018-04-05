/******************************************************************************
 * Copyright (c) 2000-2018 Ericsson Telecom AB
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 ******************************************************************************/
package org.eclipse.titan.runtime.core;

/**
 * A logger plugin implementing the legacy logger behaviour.
 *
 * @author Kristof Szabados
 */
public interface ILoggerPlugin {
	void log(final TitanLoggerApi.TitanLogEvent event, final boolean log_buffered, final boolean separate_file, final boolean use_emergency_mask);
}

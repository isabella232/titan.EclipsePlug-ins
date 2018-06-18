/******************************************************************************
 * Copyright (c) 2000-2018 Ericsson Telecom AB
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/org/documents/epl-2.0/EPL-2.0.html
 ******************************************************************************/
package org.eclipse.titan.designer.properties.data;

import org.eclipse.titan.common.logging.ErrorReporter;
import org.w3c.dom.DOMError;
import org.w3c.dom.DOMErrorHandler;

/**
 * Error handler class for the case, when an error is encountered by the XML
 * parser.
 * 
 * @author Kristof Szabados
 * */
public final class DOMErrorHandlerImpl implements DOMErrorHandler {
	private static final String ERROR = "Error Message:";

	@Override
	public boolean handleError(final DOMError error) {
		if (error.getSeverity() == DOMError.SEVERITY_WARNING) {
			ErrorReporter.logWarning(ERROR + error.getMessage());
			return true;
		}

		ErrorReporter.logError(ERROR + error.getMessage());
		return false;
	}
}

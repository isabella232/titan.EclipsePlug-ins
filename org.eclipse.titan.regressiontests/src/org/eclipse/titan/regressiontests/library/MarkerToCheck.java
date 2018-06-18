/******************************************************************************
 * Copyright (c) 2000-2018 Ericsson Telecom AB
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/org/documents/epl-2.0/EPL-2.0.html
 ******************************************************************************/
package org.eclipse.titan.regressiontests.library;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.core.resources.IMarker;

public class MarkerToCheck {
	private final HashMap<Object, Object> marker = new HashMap<Object, Object>();
	
	public MarkerToCheck(final String messageToCheck, final int lineNumber, final int messageSeverity)
	{
		marker.put(IMarker.MESSAGE, messageToCheck);
		marker.put(IMarker.LINE_NUMBER, lineNumber);
		marker.put(IMarker.SEVERITY, messageSeverity);
	}

	public Map<?, ?> getMarkerMap() {
	  return marker;	
	}
}

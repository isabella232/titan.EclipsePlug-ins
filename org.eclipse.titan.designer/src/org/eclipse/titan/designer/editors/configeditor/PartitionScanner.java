/******************************************************************************
 * Copyright (c) 2000-2021 Ericsson Telecom AB
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/org/documents/epl-2.0/EPL-2.0.html
 ******************************************************************************/
package org.eclipse.titan.designer.editors.configeditor;

import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.rules.IPredicateRule;
import org.eclipse.jface.text.rules.RuleBasedPartitionScanner;

/**
 * @author Kristof Szabados
 * */
public final class PartitionScanner extends RuleBasedPartitionScanner {
	public static final String CONFIG_PARTITIONING = "__config_partitioning";

	public static final String[] PARTITION_TYPES = new String[] { IDocument.DEFAULT_CONTENT_TYPE, };

	public PartitionScanner() {
		fRules = new IPredicateRule[0];
	}
}

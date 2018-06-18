/******************************************************************************
 * Copyright (c) 2000-2018 Ericsson Telecom AB
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/org/documents/epl-2.0/EPL-2.0.html
 ******************************************************************************/
package org.eclipse.titan.designer.AST.TTCN3.attributes;

import org.eclipse.titan.designer.AST.ASTVisitor;
import org.eclipse.titan.designer.AST.IVisitableNode;
import org.eclipse.titan.designer.parsers.CompilationTimeStamp;

/**
 * Represents the printing attribute on a JSON encoder external function, used to tell the
 * generator of the external function how JSON code should be printed.
 *
 * @author Botond Baranyi
 * */
public final class PrintingAttribute extends ExtensionAttribute implements IVisitableNode {

	private final PrintingType printingType;

	public PrintingAttribute(final PrintingType printingType) {
		this.printingType = printingType;
	}

	@Override
	/** {@inheritDoc} */
	public ExtensionAttribute_type getAttributeType() {
		return ExtensionAttribute_type.PRINTING;
	}

	public PrintingType getPrintingType() {
		return printingType;
	}

	/**
	 * Does the semantic checking of the printing attribute.
	 *
	 * @param timestamp
	 *                the time stamp of the actual semantic check cycle.
	 * */
	public void check(final CompilationTimeStamp timestamp) {
		if (lastTimeChecked != null && !lastTimeChecked.isLess(timestamp)) {
			return;
		}

		if (printingType != null) {
			printingType.check(timestamp);
		}

		lastTimeChecked = timestamp;
	}

	@Override
	/** {@inheritDoc} */
	public boolean accept(final ASTVisitor v) {
		switch (v.visit(this)) {
		case ASTVisitor.V_ABORT:
			return false;
		case ASTVisitor.V_SKIP:
			return true;
		}
		if (printingType != null && !printingType.accept(v)) {
			return false;
		}
		if (v.leave(this) == ASTVisitor.V_ABORT) {
			return false;
		}
		return true;
	}
}

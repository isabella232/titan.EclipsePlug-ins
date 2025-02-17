/******************************************************************************
 * Copyright (c) 2000-2021 Ericsson Telecom AB
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/org/documents/epl-2.0/EPL-2.0.html
 ******************************************************************************/
package org.eclipse.titan.designer.editors.configeditor;

import org.eclipse.jface.text.Position;
import org.eclipse.titan.common.parsers.Interval;
import org.eclipse.titan.designer.editors.FoldingSupport;
import org.eclipse.titan.designer.preferences.PreferenceConstants;
import org.eclipse.titan.designer.productUtilities.ProductConstants;

/**
 * @author Kristof Szabados
 * */
public final class ConfigFoldingSupport extends FoldingSupport {
	@Override
	protected void recursiveTokens(final Interval interval) {
		int endOffset = interval.getEndOffset();
		final int startOffset = interval.getStartOffset();
		if (documentText.length() <= endOffset || documentText.length() <= startOffset) {
			return;
		} else if (endOffset == -1) {
			endOffset = documentText.length() - 1;
		}

		final int distance = interval.getEndLine() - interval.getStartLine();
		if (distance >= foldingDistance) {
			final char temp = documentText.charAt(startOffset);
			switch (temp) {
			case '{':
			case '[':
				if (preferencesService.getBoolean(ProductConstants.PRODUCT_ID_DESIGNER, PreferenceConstants.FOLD_STATEMENT_BLOCKS,
						true, null)) {
					positions.add(new Position(startOffset, endOffset - startOffset));
				}
				break;
			case '(':
				if (preferencesService.getBoolean(ProductConstants.PRODUCT_ID_DESIGNER, PreferenceConstants.FOLD_PARENTHESIS, true,
						null)) {
					positions.add(new Position(startOffset, endOffset - startOffset));
				}
				break;
			case '/':
				if (preferencesService.getBoolean(ProductConstants.PRODUCT_ID_DESIGNER, PreferenceConstants.FOLD_COMMENTS, true,
						null)) {
					positions.add(new Position(startOffset, endOffset - startOffset));
				}
				break;
			default:
				break;
			}
		}
		for (final Interval subIntervall : interval.getSubIntervals()) {
			recursiveTokens(subIntervall);
		}
	}
}

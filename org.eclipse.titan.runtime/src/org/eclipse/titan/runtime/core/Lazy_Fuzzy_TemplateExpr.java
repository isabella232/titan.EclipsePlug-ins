/******************************************************************************
 * Copyright (c) 2000-2018 Ericsson Telecom AB
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/org/documents/epl-2.0/EPL-2.0.html
 ******************************************************************************/
package org.eclipse.titan.runtime.core;

/**
 * This class represents a lazy or fuzzy in template formal parameter.
 *
 * @author Kristof Szabados
 */
public class Lazy_Fuzzy_TemplateExpr<E extends Base_Template> {
	protected E expr_cache;
	protected boolean fuzzy;
	protected boolean expressionEvaluated;
	protected boolean oldExpressionEvaluated;

	public Lazy_Fuzzy_TemplateExpr(final boolean fuzzy) {
		this.fuzzy = fuzzy;
	}

	public Lazy_Fuzzy_TemplateExpr(final boolean fuzzy, final E cache) {
		this.fuzzy = fuzzy;
		this.expressionEvaluated = true;
		this.oldExpressionEvaluated = true;
		this.expr_cache = cache;
	}

	/**
	 * Internal evaluation of the expression.
	 * Can be overridden in child classes.
	 * */
	protected void evaluate_expression() {
		//Intentionally empty default implementation
	};

	/**
	 * This function is called whenever the fuzzy or lazy expression is referenced.
	 *
	 * @return the evaluated value.
	 * */
	public E evaluate() {
		if (fuzzy || !expressionEvaluated) {
			evaluate_expression();
			expressionEvaluated = true;
		}

		return expr_cache;
	}

	public void log() {
		if (!expressionEvaluated) {
			TTCN_Logger.log_event_str("<not evaluated>");
		} else {
			expr_cache.log();
		}
	}

	/** Changes the evaluation type (from lazy to fuzzy or from fuzzy to lazy). */
	public void change() {
		fuzzy = !fuzzy;
		if (!fuzzy) {
			oldExpressionEvaluated = expressionEvaluated;
			expressionEvaluated = false;
		}
	}

	/** Reverts the evaluation type back to its previous state. */
	public void revert() {
		fuzzy = !fuzzy;
		if (fuzzy) {
			expressionEvaluated = expressionEvaluated || oldExpressionEvaluated;
		}
	}
}

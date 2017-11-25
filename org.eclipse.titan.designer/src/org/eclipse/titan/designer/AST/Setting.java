/******************************************************************************
 * Copyright (c) 2000-2017 Ericsson Telecom AB
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 ******************************************************************************/
package org.eclipse.titan.designer.AST;

import org.eclipse.titan.common.logging.ErrorReporter;
import org.eclipse.titan.designer.AST.Module.module_type;
import org.eclipse.titan.designer.AST.ASN1.definitions.SpecialASN1Module;
import org.eclipse.titan.designer.parsers.CompilationTimeStamp;

/**
 * @author Kristof Szabados
 * */
public abstract class Setting extends ASTNode implements ISetting {
	/** indicates if this setting has already been found erroneous in the actual checking cycle. */
	protected boolean isErroneous;

	/** the name of the setting to be used in the code generator */
	protected String genName;

	/** the time when this setting was check the last time. */
	protected CompilationTimeStamp lastTimeChecked;

	/**
	 * The location of the whole setting.
	 * This location encloses the setting fully, as it is used to report errors to.
	 **/
	protected Location location;

	public Setting() {
		isErroneous = false;
		location = NULL_Location.INSTANCE;
	}

	@Override
	/** {@inheritDoc} */
	public final boolean getIsErroneous(final CompilationTimeStamp timestamp) {
		return isErroneous;
	}

	@Override
	/** {@inheritDoc} */
	public final void setIsErroneous(final boolean isErroneous) {
		this.isErroneous = isErroneous;
	}

	@Override
	/** {@inheritDoc} */
	public abstract Setting_type getSettingtype();

	@Override
	/** {@inheritDoc} */
	public void setLocation(final Location location) {
		this.location = location;
	}

	@Override
	/** {@inheritDoc} */
	public Location getLocation() {
		return location;
	}

	@Override
	/** {@inheritDoc} */
	public final boolean isAsn() {
		if (myScope == null) {
			return false;
		}

		return module_type.ASN_MODULE.equals(myScope.getModuleScope().getModuletype());
	}

	/**
	 * Set the generated name for this setting.
	 *
	 * @param genName the name to set.
	 * */
	public void setGenName(final String genName) {
		this.genName = genName;
	}

	/**
	 * Set the generated name for this setting,
	 *  as a concatenation of a prefix, an underscore and a suffix,
	 * unless the prefix already ends with, or the suffix already begins with
	 * precisely one underscore.
	 *
	 * @param prefix the prefix to use
	 * @param suffix the suffix to use.
	 * */
	public void setGenName(final String prefix, final String suffix) {
		if (prefix.length() == 0 || suffix.length() == 0) {
			ErrorReporter.INTERNAL_ERROR("FATAL ERROR while seting the generated name of setting `" + getFullName() + "''");
			genName = "<FATAL ERROR>";
			return;
		}

		if((!prefix.endsWith("_") || prefix.endsWith("__")) &&
				(!suffix.startsWith("_") || suffix.startsWith("__"))) {
			genName = prefix + "_" + suffix;
		} else {
			genName = prefix + suffix;
		}
	}

	/**
	 * Returns a Java reference that points to this setting from the local module.
	 *
	 * @return The name of the Java setting in the generated code.
	 */
	public String getGenNameOwn(){
		if (genName == null) {
			//fatal error
			//TODO implement the calculation of generated name, this is just temporary
			final String fullname = getFullName();
			return fullname.substring( fullname.lastIndexOf(".") + 1 );
		}

		return genName;
	}

	/**
	 * Returns a Java reference that points to this setting from the module of the parameter scope.
	 *
	 * @param scope the scope into which the name needs to be generated
	 * @return The name of the Java setting in the generated code.
	 */
	public String getGenNameOwn(final Scope scope) {
		if(myScope == null || scope == null) {
			ErrorReporter.INTERNAL_ERROR("Code generator reached erroneous setting `" + getFullName() + "''");
			return "FATAL_ERROR encountered";
		}

		final StringBuilder returnValue = new StringBuilder();
		final Module myModule = myScope.getModuleScope();//get_scope_mod_gen

		if(!myModule.equals(scope.getModuleScope()) && !SpecialASN1Module.isSpecAsss(myModule)) {
			//TODO properly prefix the setting with the module's Java reference
			returnValue.append(myModule.getName()).append('.');
		}

		returnValue.append( getGenNameOwn());

		return returnValue.toString();
	}
}

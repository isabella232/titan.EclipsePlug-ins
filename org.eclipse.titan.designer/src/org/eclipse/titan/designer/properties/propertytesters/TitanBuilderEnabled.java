/******************************************************************************
 * Copyright (c) 2000-2018 Ericsson Telecom AB
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/org/documents/epl-2.0/EPL-2.0.html
 ******************************************************************************/
package org.eclipse.titan.designer.properties.propertytesters;

import org.eclipse.core.expressions.PropertyTester;
import org.eclipse.core.resources.ICommand;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IProjectDescription;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.titan.common.logging.ErrorReporter;
import org.eclipse.titan.designer.core.TITANBuilder;
import org.eclipse.titan.designer.core.TITANJavaBuilder;

/**
 * @author Kristof Szabados
 * */
public class TitanBuilderEnabled extends PropertyTester {

	@Override
	public boolean test(Object receiver, String property, Object[] args, Object expectedValue) {
		final IProject project = ((IResource)receiver).getProject();
		if (!project.isAccessible()) {
			return false;
		}
		try {
			final IProjectDescription description = project.getDescription();
			final ICommand[] buildSpec = description.getBuildSpec();
			for (int i = 0; i < buildSpec.length; ++i) {
				final String builderName = buildSpec[i].getBuilderName();
				if (TITANBuilder.BUILDER_ID.equals(builderName) && "isTitanBuilder".equals(property)) {
					return true;
				} else if (TITANJavaBuilder.BUILDER_ID.equals(builderName) && "isTitanJavaBuilder".equals(property)) {
					return true;
				}
			}
		} catch (CoreException e) {
			ErrorReporter.logExceptionStackTrace(e);
		}
		// TODO Auto-generated method stub
		return false;
	}

}

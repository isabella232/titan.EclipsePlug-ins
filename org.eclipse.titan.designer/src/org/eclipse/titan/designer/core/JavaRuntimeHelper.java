/******************************************************************************
 * Copyright (c) 2000-2021 Ericsson Telecom AB
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/org/documents/epl-2.0/EPL-2.0.html
 ******************************************************************************/
package org.eclipse.titan.designer.core;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.jdt.launching.AbstractVMInstall;
import org.eclipse.jdt.launching.IVMInstall;
import org.eclipse.jdt.launching.JavaRuntime;
import org.eclipse.titan.common.logging.ErrorReporter;

/**
 * Provides backward compatibility for class path related code 
 * @author Adam Knapp
 * */
public final class JavaRuntimeHelper {

	/** @see org.eclipse.jdt.core.JavaCore#VERSION_1_8  */
	public static final String VERSION_1_8 = "1.8";
	/** @see org.eclipse.jdt.launching.IRuntimeClasspathEntry#MODULE_PATH */
	public static final int MODULE_PATH = 4;
	/** @see org.eclipse.jdt.launching.IRuntimeClasspathEntry#CLASS_PATH */
	public static final int CLASS_PATH = 5;

	/**
	 * Checks if configuration JRE is greater than 8.
	 * @param configuration the launch configuration
	 * @return boolean <code>true</code> if JRE used in configuration is greater than 8 else <code>false</code>
	 * @see org.eclipse.jdt.launching.JavaRuntime#isModularConfiguration
	 */
	public static boolean isModularConfiguration(ILaunchConfiguration configuration) {
		try {
			IVMInstall vm = JavaRuntime.computeVMInstall(configuration);
			if (vm instanceof AbstractVMInstall) {
				AbstractVMInstall install = (AbstractVMInstall) vm;
				String vmver = install.getJavaVersion();
				if (vmver == null) {
					return false;
				}
				// versionToJdkLevel only handles 3 char versions = 1.5, 1.6, 1.7, etc
				if (vmver.length() > 3) {
					vmver = vmver.substring(0, 3);
				}
				if (VERSION_1_8.compareTo(vmver) < 0) {
					return true;
				}
			}
		} catch (CoreException e) {
			ErrorReporter.logExceptionStackTrace(e);
		}
		return false;
	}
}

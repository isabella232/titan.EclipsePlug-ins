/******************************************************************************
 * Copyright (c) 2000-2017 Ericsson Telecom AB
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 ******************************************************************************/
package org.eclipse.titan.executor;

import org.eclipse.titan.executor.views.executormonitor.ExecutorMonitorView;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.osgi.framework.BundleContext;

/**
 * The activator class controls the plug-in life cycle.
 * 
 * @author Kristof Szabados
 */
public final class Activator extends AbstractUIPlugin {
	// The plug-in ID
	public static final String PLUGIN_ID = "org.eclipse.titan.executor";

	// The shared instance
	private static Activator plugin = null;
	private static ExecutorMonitorView mainView = null;

	public Activator() {
		setDefault(this);
	}

	@Override
	public void stop(final BundleContext context) throws Exception {
		setDefault(null);

		super.stop(context);
	}

	/**
	 * Sets the default singleton instance of this plug-in,
	 * that later can be used to access plug-in specific preference settings.
	 * 
	 * @param activator the single instance of this plug-in class.
	 * */
	private static void setDefault(final Activator activator) {
		if (plugin == null) {
			plugin = activator;
		}
	}

	/**
	 * Returns the shared instance.
	 *
	 * @return the shared instance
	 */
	public static Activator getDefault() {
		return plugin;
	}

	public static ExecutorMonitorView getMainView() {
		return mainView;
	}

	public static void setMainView(final ExecutorMonitorView view) {
		mainView = view;
	}
}

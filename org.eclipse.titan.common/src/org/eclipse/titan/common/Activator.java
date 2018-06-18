/******************************************************************************
 * Copyright (c) 2000-2018 Ericsson Telecom AB
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/org/documents/epl-2.0/EPL-2.0.html
 ******************************************************************************/
package org.eclipse.titan.common;

import org.eclipse.core.resources.WorkspaceJob;
import org.eclipse.titan.common.product.ProductConstants;
import org.eclipse.titan.common.usagestats.InstalledProductInfoCollector;
import org.eclipse.titan.common.usagestats.UsageStatSender;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.osgi.framework.BundleContext;

/**
 * The activator class controls the plug-in life cycle.
 *
 * @author Kristof Szabados
 */
public final class Activator extends AbstractUIPlugin {

	//** The plug-in ID */
	public static final String PLUGIN_ID = ProductConstants.PRODUCT_ID_COMMON;

	/** The shared instance */
	private static Activator plugin;
	private WorkspaceJob usageStatSenderJob;

	public Activator() {
		setDefault(this);
	}

	@Override
	public void start(final BundleContext context) throws Exception {
		super.start(context);
		if ( ProductConstants.USAGE_STAT_SENDING ) {
			usageStatSenderJob = new UsageStatSender(new InstalledProductInfoCollector()).sendAsync();
		}
	}

	@Override
	public void stop(final BundleContext context) throws Exception {
		setDefault(null);
		if (usageStatSenderJob != null) {
			usageStatSenderJob.cancel();
		}
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
}

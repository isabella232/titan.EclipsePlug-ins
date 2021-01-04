/******************************************************************************
 * Copyright (c) 2000-2021 Ericsson Telecom AB
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/org/documents/epl-2.0/EPL-2.0.html
 ******************************************************************************/
package org.eclipse.titan.executor.views.executormonitor;

import java.util.List;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.debug.core.ILaunch;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchesListener2;
import org.eclipse.titan.common.logging.ErrorReporter;
import org.eclipse.titan.executor.executors.ITreeLeaf;
import org.eclipse.titan.executor.executors.TreeBranch;
import org.eclipse.ui.PlatformUI;

/**
 * @author Kristof Szabados
 * */
public final class LaunchesListener implements ILaunchesListener2 {
	private final ExecutorMonitorView executorMonitorView;

	public LaunchesListener(final ExecutorMonitorView executorMonitorView) {
		this.executorMonitorView = executorMonitorView;
	}

	@Override
	public void launchesAdded(final ILaunch[] launches) {
		for (final ILaunch launch : launches) {
			final ILaunchConfiguration launchConfiguration = launch.getLaunchConfiguration();

			try {
				if (ExecutorMonitorView.isSupportedConfiguration(launchConfiguration)) {
					LaunchElement launchElement;

					if (LaunchStorage.getLaunchElementMap().containsKey(launch)) {
						launchElement = LaunchStorage.getLaunchElementMap().get(launch);
					} else {
						final String name = launchConfiguration.getName() + " [ " + launchConfiguration.getType().getName() + " ]";
						launchElement = new LaunchElement(name, launch);
						LaunchStorage.registerLaunchElement(launchElement);
						ExecutorStorage.registerExecutorStorage(launchElement);
					}

					executorMonitorView.getRoot().addChildToEnd(launchElement);
				}
			} catch (CoreException e) {
				ErrorReporter.logExceptionStackTrace(" While processing launch configuration " + launchConfiguration.getName(),e);
			}
		}
		PlatformUI.getWorkbench().getDisplay().syncExec(new Runnable() {
			@Override
			public void run() {
				executorMonitorView.getTreeViewer().refresh(executorMonitorView.getRoot());
			}
		});
		executorMonitorView.updateActions();
	}

	@Override
	public void launchesChanged(final ILaunch[] launches) {
		for (final ILaunch launched : launches) {
			final List<ITreeLeaf> children = executorMonitorView.getRoot().children();
			for (final ITreeLeaf leaf : children) {
				final LaunchElement launchElement = (LaunchElement) leaf;
				if (launched.equals(launchElement.launch())) {
					launchElement.changed();
					PlatformUI.getWorkbench().getDisplay().syncExec(new Runnable() {
						@Override
						public void run() {
							executorMonitorView.getTreeViewer().expandToLevel(launchElement, 3);
							executorMonitorView.getTreeViewer().refresh(launchElement);
						}
					});
				}
			}
		}
		executorMonitorView.updateActions();
	}

	@Override
	public void launchesRemoved(final ILaunch[] launches) {
		final TreeBranch root = executorMonitorView.getRoot();
		for (final ILaunch launched : launches) {
			for (int i = root.children().size() - 1; i >= 0; i--) {
				final ILaunch temporal = ((LaunchElement) root.children().get(i)).launch();
				if (launched.equals(temporal)) {
					root.children().get(i).dispose();
					root.children().remove(i);
				}
			}
		}
		PlatformUI.getWorkbench().getDisplay().syncExec(new Runnable() {
			@Override
			public void run() {
				executorMonitorView.getTreeViewer().refresh(executorMonitorView.getRoot());
			}
		});
		executorMonitorView.updateActions();
	}

	@Override
	public void launchesTerminated(final ILaunch[] launches) {
		for (final ILaunch launched : launches) {
			for (final ITreeLeaf element : executorMonitorView.getRoot().children()) {
				final LaunchElement launchElement = (LaunchElement) element;
				if (launched.equals(launchElement.launch())) {
					launchElement.changed();
					MainControllerElement mainController;
					if (1 == ((LaunchElement) element).children().size()) {
						mainController = (MainControllerElement) ((LaunchElement) element).children().get(0);
					} else {
						mainController = null;
					}
					if (null != mainController) {
						mainController.executor().terminate(true);
					}
					PlatformUI.getWorkbench().getDisplay().syncExec(new Runnable() {
						@Override
						public void run() {
							executorMonitorView.getTreeViewer().refresh(launchElement);
						}
					});
				}
			}
		}
		executorMonitorView.updateActions();
	}
}

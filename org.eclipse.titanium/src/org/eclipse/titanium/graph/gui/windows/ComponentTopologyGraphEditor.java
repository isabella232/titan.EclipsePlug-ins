/******************************************************************************
 * Copyright (c) 2000-2019 Ericsson Telecom AB
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/org/documents/epl-2.0/EPL-2.0.html
 ******************************************************************************/
package org.eclipse.titanium.graph.gui.windows;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.util.Collection;
import java.util.Deque;

import javax.swing.ButtonGroup;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JRadioButtonMenuItem;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.QualifiedName;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.titan.common.logging.ErrorReporter;
import org.eclipse.titan.designer.properties.data.ProjectBuildPropertyData;
import org.eclipse.titanium.graph.components.EdgeDescriptor;
import org.eclipse.titanium.graph.components.NodeColours;
import org.eclipse.titanium.graph.components.NodeDescriptor;
import org.eclipse.titanium.graph.gui.common.Layouts;
import org.eclipse.titanium.graph.gui.dialogs.ExportImagePreferencesDialog;
import org.eclipse.titanium.graph.gui.utils.LayoutEntry;
import org.eclipse.titanium.graph.utils.CheckParallelPaths;
import org.eclipse.titanium.graph.utils.CircleCheck;
import org.eclipse.titanium.graph.visualization.BadLayoutException;
import org.eclipse.titanium.graph.visualization.ErrorType;
import org.eclipse.titanium.graph.visualization.GraphHandler;
import org.eclipse.titanium.graph.visualization.GraphHandler.ImageExportType;
import org.eclipse.titanium.gui.FindWindow;

/**
 * This class is a subclass of {@link GraphEditor}. It implements the
 * specialties needed for component finder graph editor window
 *
 * @author Bianka Bekefi
 * @see GraphEditor
 */
public class ComponentTopologyGraphEditor extends GraphEditor {
	public static final String ID = "org.eclipse.titanium.graph.editors.ComponentTopologyGraphEditor";

	public ComponentTopologyGraphEditor() {
		super();
	}


	@Override
	protected void initWindow() {
		//super.initWindow();
		
		drawArea = new JPanel();
		window.add(drawArea, BorderLayout.CENTER);
		drawArea.setSize(windowSize.width, windowSize.height);
		drawArea.setPreferredSize(new Dimension(windowSize.width, windowSize.height));

		menuBar = new JMenuBar();
		window.add(menuBar, BorderLayout.NORTH);

		final JMenu mnFile = new JMenu("File");

		final ActionListener saveGraph = new ActionListener() {
			@Override
			public void actionPerformed(final ActionEvent e) {
				String path = "";
				try {
					path = project.getPersistentProperty(
							new QualifiedName(ProjectBuildPropertyData.QUALIFIER, "Graph_Save_Path"));
				} catch (CoreException exc) {
					errorHandler.reportException("Error while reading persistent property", exc);
				}
				final String oldPath = path;
				Display.getDefault().asyncExec(new Runnable() {
					@Override
					public void run() {
						FileDialog dialog = new FileDialog(editorComposite.getShell(), SWT.SAVE);
						dialog.setText("Save Pajek file");
						dialog.setFilterPath(oldPath);
						dialog.setFilterExtensions(new String[] { "*.net", "*.dot" });
						String graphFilePath = dialog.open();
						if (graphFilePath == null) {
							return;
						}
						String newPath = graphFilePath.substring(0, graphFilePath.lastIndexOf(File.separator) + 1);
						try {
							QualifiedName name = new QualifiedName(ProjectBuildPropertyData.QUALIFIER, "Graph_Save_Path");
							project.setPersistentProperty(name, newPath);

							if ("dot".equals(graphFilePath.substring(graphFilePath.lastIndexOf('.') + 1,
									graphFilePath.length()))) {
								GraphHandler.saveGraphToDot(graph, graphFilePath, project.getName());
							} else {
								GraphHandler.saveGraphToPajek(graph, graphFilePath);
							}

						} catch (BadLayoutException be) {
							ErrorReporter.logExceptionStackTrace("Error while saving image to " + newPath, be);
							errorHandler.reportErrorMessage("Bad layout\n\n" + be.getMessage());
						} catch (Exception ce) {
							ErrorReporter.logExceptionStackTrace("Error while saving image to " + newPath, ce);
							errorHandler.reportException("Error while setting persistent property", ce);
						}
					}
				});
			}
		};

		final JMenuItem mntmSave = new JMenuItem("Save (Ctrl+S)");
		mntmSave.addActionListener(saveGraph);
		mnFile.add(mntmSave);

		final ActionListener exportImage = new ActionListener() {
			@Override
			public void actionPerformed(final ActionEvent e) {
				String path = "";
				try {
					path = project.getPersistentProperty(
							new QualifiedName(ProjectBuildPropertyData.QUALIFIER, "Graph_Save_Path"));
				} catch (CoreException exc) {
					errorHandler.reportException("Error while reading persistent property", exc);
				}
				final String oldPath = path;
				Display.getDefault().asyncExec(new Runnable() {
					@Override
					public void run() {
						ExportImagePreferencesDialog prefDialog = new ExportImagePreferencesDialog(editorComposite.getShell());
						ImageExportType mode = prefDialog.open();

						FileDialog dialog = new FileDialog(editorComposite.getShell(), SWT.SAVE);
						dialog.setText("Export image");
						dialog.setFilterPath(oldPath);
						dialog.setFilterExtensions(new String[] { "*.png" });
						String graphFilePath = dialog.open();
						if (graphFilePath == null) {
							return;
						}
						String newPath = graphFilePath.substring(0, graphFilePath.lastIndexOf(File.separator) + 1);
						try {
							QualifiedName name = new QualifiedName(ProjectBuildPropertyData.QUALIFIER, "Graph_Save_Path");
							project.setPersistentProperty(name, newPath);
							handler.saveToImage(graphFilePath, mode);
						} catch (BadLayoutException be) {
							errorHandler.reportException("Error while saving image", be);
							errorHandler.reportErrorMessage(be.getMessage());
						} catch (CoreException ce) {
							errorHandler.reportException("Error while setting persistent property", ce);
						}
					}
				});
			}
		};

		final JMenuItem mntmExportToImage = new JMenuItem("Export to image file (Ctrl+E)");
		mntmExportToImage.addActionListener(exportImage);
		mnFile.add(mntmExportToImage);

		layoutMenu = new JMenu("Layout");
		layoutGroup = new ButtonGroup();

		layoutListener = new ActionListener() {
			@Override
			public void actionPerformed(final ActionEvent e) {
				final IProgressMonitor monitor = Job.getJobManager().createProgressGroup();
				monitor.beginTask("Change layout", 100);
				if (!(e.getSource() instanceof LayoutEntry)) {
					errorHandler.reportErrorMessage("Unexpected error\n\nAn unusual error has been logged" + LOGENTRYNOTE);
					ErrorReporter.logError("The layout changing event's source is not of type \"LayoutEntry\"!");
					return;
				}
				final LayoutEntry layout = (LayoutEntry) e.getSource();
				if (handler.getVisualizator() != null) {
					drawArea.remove(handler.getVisualizator());
				}
				try {
					handler.changeLayout(layout, windowSize);
					drawArea.add(handler.getVisualizator());
					if (satView != null) {
						satView.add(handler.getSatelliteViewer());
					}
					window.pack();
				} catch (BadLayoutException exc) {
					layout.setSelected(false);
					chosenLayout.setSelected(true);
					if (exc.getType() == ErrorType.EMPTY_GRAPH || exc.getType() == ErrorType.NO_OBJECT) {
						return;
					}
					try {
						handler.changeLayout(chosenLayout, windowSize);
						drawArea.add(handler.getVisualizator());
						if (satView != null) {
							satView.add(handler.getSatelliteViewer());
						}
						window.pack();
						monitor.done();
					} catch (BadLayoutException exc2) {
						monitor.done();
						if (exc2.getType() != ErrorType.CYCLIC_GRAPH && exc2.getType() != ErrorType.EMPTY_GRAPH) {
							errorHandler.reportException("Error while creating layout", exc2);
						} else {
							errorHandler.reportErrorMessage(exc2.getMessage());
						}
					} catch (IllegalStateException exc3) {
						monitor.done();
						errorHandler.reportException("Error while creating layout", exc3);
					}
					if (exc.getType() != ErrorType.CYCLIC_GRAPH && exc.getType() != ErrorType.EMPTY_GRAPH) {
						errorHandler.reportException("Error while creating layout", exc);
					} else {
						errorHandler.reportErrorMessage(exc.getMessage());
					}
				} catch (IllegalStateException exc) {
					layout.setSelected(false);
					chosenLayout.setSelected(true);
					try{
						handler.changeLayout(chosenLayout, windowSize);
						drawArea.add(handler.getVisualizator());
						if (satView != null) {
							satView.add(handler.getSatelliteViewer());
						}
						window.pack();
						monitor.done();
					} catch (BadLayoutException exc2) {
						monitor.done();
						if (exc2.getType() != ErrorType.CYCLIC_GRAPH && exc2.getType() != ErrorType.EMPTY_GRAPH) {
							errorHandler.reportException("Error while creating layout", exc2);
						} else {
							errorHandler.reportErrorMessage(exc2.getMessage());
						}
					} catch (IllegalStateException exc3) {
						monitor.done();
						errorHandler.reportException("Error while creating layout", exc3);
					}
					errorHandler.reportException("Error while creating layout", exc);
				}
				chosenLayout = layout.newInstance();
				monitor.done();
			}
		};

		final JMenu findMenu = new JMenu("Find");
		final JMenuItem nodeByName = new JMenuItem("Node by name (Ctrl+F)");

		final GraphEditor thisEditor = this;
		nodeByName.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(final ActionEvent e) {
				Display.getDefault().asyncExec(new Runnable() {
					@Override
					public void run() {
						if (wndFind != null) {
							wndFind.close();
						}
						try {
							wndFind = new FindWindow<NodeDescriptor>(editorComposite.getShell(), thisEditor, graph.getVertices());
							wndFind.open();
						} catch(IllegalArgumentException e) {
							errorHandler.reportException("", e);
						}
					}
				});
			}
		});

		findMenu.add(nodeByName);

		final JMenu tools = new JMenu("Tools");
		final JMenuItem findCircles = new JMenuItem("Show circles");
		final JMenuItem findPaths = new JMenuItem("Show parallel paths");
		final JMenuItem clearResults = new JMenuItem("Clear Results");

		findCircles.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(final ActionEvent ev) {
				final Job circlesJob = new Job("Searching for circles") {
					@Override
					protected IStatus run(final IProgressMonitor monitor) {
						if (graph == null) {
							return null;
						}
						CircleCheck<NodeDescriptor, EdgeDescriptor> checker =
								new CircleCheck<NodeDescriptor, EdgeDescriptor>(graph);
						if (checker.isCyclic()) {
							for (EdgeDescriptor e : graph.getEdges()) {
								e.setColour(Color.lightGray);
							}
							for (Deque<EdgeDescriptor> st : checker.getCircles()) {
								for (EdgeDescriptor e : st) {
									e.setColour(NodeColours.DARK_RED);
								}
							}
							refresh();
						} else {
							errorHandler.reportInformation("Result:\n\nThis graph is not cyclic!");
						}

						return Status.OK_STATUS;
					} // end run
				}; // end job
				circlesJob.schedule();
			} // end actionPerformed
		});

		findPaths.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(final ActionEvent ev) {
				final Job pathsJob = new Job("Searching for parallel paths") {
					@Override
					protected IStatus run(final IProgressMonitor monitor) {
						if (graph == null) {
							return null;
						}
						CheckParallelPaths<NodeDescriptor, EdgeDescriptor> checker = null;
						checker = new CheckParallelPaths<NodeDescriptor, EdgeDescriptor>(graph);
						if (checker.hasParallelPaths()) {
							for (EdgeDescriptor e : graph.getEdges()) {
								e.setColour(Color.lightGray);
							}
							for (Deque<EdgeDescriptor> list : checker.getPaths()) {
								for (EdgeDescriptor e : list) {
									e.setColour(NodeColours.DARK_RED);
								}
							}
							refresh();
						} else {
							errorHandler.reportInformation("Result:\n\nThere are no parallel paths in this graph!");
						}

						return Status.OK_STATUS;
					} // end run
				}; // end job
				pathsJob.schedule();
			} // end actionPerformed
		});

		clearResults.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(final ActionEvent ev) {
				for (final EdgeDescriptor e : graph.getEdges()) {
					e.setColour(Color.black);
				}
				refresh();
			}
		});

		tools.add(findCircles);
		tools.add(findPaths);
		tools.add(clearResults);

		menuBar.add(mnFile);
		menuBar.add(findMenu);
		menuBar.add(tools);
		menuBar.add(layoutMenu);

		handlerService.activateHandler(GRAPH_SEARCHCMD_ID, new AbstractHandler() {
			@Override
			public Object execute(final ExecutionEvent event) throws ExecutionException {
				nodeByName.getActionListeners()[0].actionPerformed(null);
				handlers.add(this);
				return null;
			}
		});

		handlerService.activateHandler(GRAPH_SAVECMD_ID, new AbstractHandler() {
			@Override
			public Object execute(final ExecutionEvent event) throws ExecutionException {
				mntmSave.getActionListeners()[0].actionPerformed(null);
				handlers.add(this);
				return null;
			}
		});

		handlerService.activateHandler(GRAPH_EXPORTCMD_ID, new AbstractHandler() {
			@Override
			public Object execute(final ExecutionEvent event) throws ExecutionException {
				mntmExportToImage.getActionListeners()[0].actionPerformed(null);
				handlers.add(this);
				return null;
			}
		});

		/*try {
			generator.generateGraph();
			setLabeller(generator.getLabeler());
			setGraph(generator.getGraph());
		} catch (InterruptedException ex) {
			errorHandler.reportException("Error while creating the graph", ex);
		}*/
		

		final JRadioButtonMenuItem isom = Layouts.LAYOUT_ISOM.newInstance();
		isom.addActionListener(layoutListener);
		layoutGroup.add(isom);
		layoutMenu.add(isom);

		final JMenu dagMenu = new JMenu("Directed layouts");
		layoutMenu.add(dagMenu);

		final JRadioButtonMenuItem tdag = Layouts.LAYOUT_TDAG.newInstance();
		tdag.setSelected(true);
		tdag.addActionListener(layoutListener);
		dagMenu.add(tdag);
		layoutGroup.add(tdag);

		final JRadioButtonMenuItem rtdag = Layouts.LAYOUT_RTDAG.newInstance();
		rtdag.addActionListener(layoutListener);
		dagMenu.add(rtdag);
		layoutGroup.add(rtdag);
	}

	@Override
	public void recolour(final Collection<NodeDescriptor> nodeSet) {
		for (final NodeDescriptor v : nodeSet) {
			v.setNodeColour(NodeColours.LIGHT_GREEN);
		}
	}

	@Override
	protected void initGeneratorAndHandler(final Composite parent) {
		handler = new GraphHandler();
		/*generator = new ComponentTopologyGraphGenerator(((IFileEditorInput) getEditorInput()).getFile(),
				((IFileEditorInput) getEditorInput()).getFile().getProject(),
				errorHandler);*/
	}

}

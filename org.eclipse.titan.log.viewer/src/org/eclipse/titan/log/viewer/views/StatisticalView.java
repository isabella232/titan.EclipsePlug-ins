/******************************************************************************
 * Copyright (c) 2000-2021 Ericsson Telecom AB
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/org/documents/epl-2.0/EPL-2.0.html
 ******************************************************************************/
package org.eclipse.titan.log.viewer.views;

import java.io.File;
import java.io.IOException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Vector;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.Path;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.ISelectionProvider;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.titan.common.logging.ErrorReporter;
import org.eclipse.titan.common.utils.IOUtils;
import org.eclipse.titan.log.viewer.Activator;
import org.eclipse.titan.log.viewer.actions.OpenMSCViewAction;
import org.eclipse.titan.log.viewer.actions.OpenTextTableStatisticalViewMenuAction;
import org.eclipse.titan.log.viewer.exceptions.TechnicalException;
import org.eclipse.titan.log.viewer.exceptions.TitanLogExceptionHandler;
import org.eclipse.titan.log.viewer.extractors.TestCaseExtractor;
import org.eclipse.titan.log.viewer.models.LogFileMetaData;
import org.eclipse.titan.log.viewer.parsers.data.LogRecord;
import org.eclipse.titan.log.viewer.parsers.data.TestCase;
import org.eclipse.titan.log.viewer.readers.CachedLogReader;
import org.eclipse.titan.log.viewer.readers.LogFileReader;
import org.eclipse.titan.log.viewer.utils.Constants;
import org.eclipse.titan.log.viewer.utils.LogFileCacheHandler;
import org.eclipse.titan.log.viewer.utils.Messages;
import org.eclipse.titan.log.viewer.views.details.StatisticalData;
import org.eclipse.titan.log.viewer.views.navigator.ProjectsViewerMenuListener;
import org.eclipse.titan.log.viewer.views.navigator.ProjectsViewerMenuManager;
import org.eclipse.ui.IActionBars;
import org.eclipse.ui.IMemento;
import org.eclipse.ui.IViewPart;
import org.eclipse.ui.IViewReference;
import org.eclipse.ui.IViewSite;
import org.eclipse.ui.IWorkbenchActionConstants;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.forms.events.ExpansionAdapter;
import org.eclipse.ui.forms.events.ExpansionEvent;
import org.eclipse.ui.forms.widgets.ExpandableComposite;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.eclipse.ui.forms.widgets.ScrolledForm;
import org.eclipse.ui.forms.widgets.Section;
import org.eclipse.ui.forms.widgets.TableWrapData;
import org.eclipse.ui.forms.widgets.TableWrapLayout;
import org.eclipse.ui.part.ViewPart;

/**
 * This class represents the statistical view
 */
public class StatisticalView extends ViewPart implements ISelectionProvider, ILogViewerView {

	private IMemento memento = null;
	private LogFileMetaData logFileMetaData;
	private Table amountTable = null;
	private Table errorTestCasesTable = null;
	private Table failTestCasesTable = null;
	private Table testCasesTable = null;
	private CachedLogReader reader = null;
	private static final int DEFAULT_COLUMN_WIDTH = 55;
	private static final int DEFAULT_AMOUNT_COLUMN_WIDTH = 75;
	private OpenMSCViewAction openMSCViewAction;
	private OpenTextTableStatisticalViewMenuAction openTextTableStatisticalViewMenuAction;
	private final List<ISelectionChangedListener> registeredListeners;
	private TestCase testcaseSelection = null;
	private ISelection eventSelection;

	private FormToolkit toolkit;
	private ScrolledForm form;
	private ExpandableComposite ecError;
	private ExpandableComposite ecFail;
	private ExpandableComposite ecTestCases;

	private final Map<String, Section> cachedSections;
	private List<StatisticalData> statisticalDataVector;

	/**
	 * The constructor.
	 */
	public StatisticalView() {
		registeredListeners = new ArrayList<ISelectionChangedListener>();
		cachedSections = new HashMap<String, Section>();
		statisticalDataVector = new Vector<StatisticalData>();
	}

	@Override
	public void init(final IViewSite site, final IMemento memento) throws PartInitException {
		init(site);
		this.memento = memento;
	}

	@Override
	public void saveState(final IMemento memento) {
		// do not save empty views
		if (this.reader == null) {
			return;
		}
		final IMemento tempMemento = memento.createChild("selection"); //$NON-NLS-1$
		try {
			final IMemento[] viewAttributes = new IMemento[this.statisticalDataVector.size()];
			for (int i = 0; i < this.statisticalDataVector.size(); i++) {
				final IMemento viewAttribute = tempMemento.createChild("viewAttributes"); //$NON-NLS-1$
				final StatisticalData statisticData = this.statisticalDataVector.get(i);
				final LogFileMetaData logFileMetaData =  statisticData.getLogFileMetaData();
				viewAttribute.putString("projectName", logFileMetaData.getProjectName()); //$NON-NLS-1$

				// save state about log file
				final Path filePath = new Path(logFileMetaData.getProjectRelativePath());
				final IFile logFile = ResourcesPlugin.getWorkspace().getRoot().getFile(filePath);
				if ((logFile != null) && logFile.exists()) {
					// add property file to the memento
					viewAttribute.putString("propertyFile", LogFileCacheHandler.getPropertyFileForLogFile(logFile).getAbsolutePath()); //$NON-NLS-1$
					final File aLogFile = logFile.getLocation().toFile();
					viewAttribute.putString("fileSize", String.valueOf(aLogFile.length())); //$NON-NLS-1$
					viewAttribute.putString("fileModification", String.valueOf(aLogFile.lastModified())); //$NON-NLS-1$
				}
				viewAttributes[i] = viewAttribute;
			}
		} catch (Exception e) {
			ErrorReporter.logExceptionStackTrace(e);
		}
	}

	/**
	 * Set data for Statistical View
	 * @param statisticalDataVector the new data
	 */
	public void setData(final List<StatisticalData> statisticalDataVector) {
		this.statisticalDataVector = statisticalDataVector;
		if (this.statisticalDataVector.size() > 1) {
			final Set<String> keys = cachedSections.keySet();
			for (final String currentKey : keys) {
				final Section tmpSection = cachedSections.get(currentKey);
				if (tmpSection != null && !tmpSection.isDisposed()) {
					tmpSection.dispose();
				}
			}
			cachedSections.clear();
		}

		for (final StatisticalData statisticalData : this.statisticalDataVector) {
			this.logFileMetaData = statisticalData.getLogFileMetaData();
			final List<TestCase> tmpTestCases = statisticalData.getTestCaseVector();
			this.reader = statisticalData.getCachedLogFileReader();


			final String projectRelativePath = this.logFileMetaData.getProjectRelativePath();
			final Section tmpSection = cachedSections.get(projectRelativePath);
			if (tmpSection == null) {
				createSection();
			}
			//Clear all tables before setting the data
			this.amountTable.removeAll();
			this.errorTestCasesTable.removeAll();
			this.failTestCasesTable.removeAll();
			this.testCasesTable.removeAll();
			int noOfPass = 0;
			int noOfFail = 0;
			int noOfInconc = 0;
			int noOfNone = 0;
			int noOfError = 0;
			int noOfCrash = 0;

			// If input is null
			if (tmpTestCases == null) {
				continue;
			}

			final int noTotal = tmpTestCases.size();

			for (final TestCase tc : tmpTestCases) {
				final TableItem tcItem = new TableItem(this.testCasesTable, SWT.BORDER);

				LogRecord record = getLogRecordAtRow(tc.getStartRecordNumber());
				final String start = record.getTimestamp();
				record = getLogRecordAtRow(tc.getEndRecordNumber());
				final String stop = record.getTimestamp();
				Image image;
				switch (tc.getVerdict()) {
				case Constants.VERDICT_PASS:
					image = Activator.getDefault().getIcon(Constants.ICONS_PASS);
					noOfPass++;
					break;
				case Constants.VERDICT_ERROR:{
					image = Activator.getDefault().getIcon(Constants.ICONS_ERROR);
					final TableItem tcErrorItem = new TableItem(this.errorTestCasesTable, SWT.BORDER);
					tcErrorItem.setImage(1, image);
					tcErrorItem.setText(2, tc.getTestCaseName());
					tcErrorItem.setText(3, start);
					tcErrorItem.setText(4, stop);
					tcErrorItem.setData(tc);
					noOfError++;
					break;
				}
				case Constants.VERDICT_FAIL:{
					image = Activator.getDefault().getIcon(Constants.ICONS_FAIL);
					final TableItem tcFailItem = new TableItem(this.failTestCasesTable, SWT.BORDER);
					tcFailItem.setImage(1, image);
					tcFailItem.setText(2, tc.getTestCaseName());
					tcFailItem.setText(3, start);
					tcFailItem.setText(4, stop);
					tcFailItem.setData(tc);

					noOfFail++;
					break;
				}
				case Constants.VERDICT_INCONCLUSIVE:
					image = Activator.getDefault().getIcon(Constants.ICONS_INCONCLUSIVE);
					noOfInconc++;
					break;
				case Constants.VERDICT_NONE:
					image = Activator.getDefault().getIcon(Constants.ICONS_NONE);
					noOfNone++;
					break;
				case Constants.VERDICT_CRASHED:
					image = Activator.getDefault().getIcon(Constants.ICONS_CRASHED);
					noOfCrash++;
					break;
				default:
					// Could not find image return null
					image = null;
					break;
				}

				tcItem.setImage(1, image);
				tcItem.setText(2, tc.getTestCaseName());
				tcItem.setText(3, start);
				tcItem.setText(4, stop);
				tcItem.setData(tc);

			}

			if (this.errorTestCasesTable.getItems().length < 1) {
				this.errorTestCasesTable.setLinesVisible(false);
			} else {
				this.errorTestCasesTable.redraw();
				ecError.setExpanded(true);
			}

			if (this.failTestCasesTable.getItems().length < 1) {
				this.failTestCasesTable.setLinesVisible(false);
			} else {
				this.failTestCasesTable.redraw();
				ecFail.setExpanded(true);
			}

			if (this.testCasesTable.getItems().length < 1) {
				this.testCasesTable.setLinesVisible(false);
			} else {
				this.testCasesTable.redraw();
				ecTestCases.setExpanded(true);
			}

			// Create the statistical row
			final TableItem item = new TableItem(this.amountTable, SWT.BORDER);
			item.setText(0, String.valueOf(noTotal));
			item.setText(1, String.valueOf(noOfPass + getPercent(noOfPass, noTotal)));
			item.setText(2, String.valueOf(noOfFail + getPercent(noOfFail, noTotal)));
			item.setText(3, String.valueOf(noOfInconc + getPercent(noOfInconc, noTotal)));
			item.setText(4, String.valueOf(noOfNone + getPercent(noOfNone, noTotal)));
			item.setText(5, String.valueOf(noOfError + getPercent(noOfError, noTotal)));
			item.setText(6, String.valueOf(noOfCrash + getPercent(noOfCrash, noTotal)));
		}

		if (statisticalDataVector.size() > 1) {
			setPartName("Statistics"); //$NON-NLS-1$
			setContentDescription(""); //$NON-NLS-1$
		} else if (this.logFileMetaData != null) {
			final File file = new File(this.logFileMetaData.getFilePath());
			final String fileName = file.getName();
			//Set the name of the part
			setPartName(fileName);
			setContentDescription(this.logFileMetaData.getProjectRelativePath());
		}

		// Finally redraw form
		form.reflow(true);
		form.setRedraw(true);
	}

	@Override
	public void dispose() {
		IOUtils.closeQuietly(reader);
		toolkit.dispose();
		super.dispose();
	}


	/**
	 * Called in the view life-cycle restore chain
	 * Reads back all view data if memento has been set
	 *
	 * The restore is very restricted an checks that the
	 * <li> Project still exists and is open
	 * <li> The file is within the project
	 * <li> The file size and file date has not changed
	 */
	private List<StatisticalData> restoreState() {
		if (this.memento == null) {
			return new ArrayList<StatisticalData>();
		}

		this.memento = this.memento.getChild("selection"); //$NON-NLS-1$
		if (this.memento == null) {
			return new ArrayList<StatisticalData>();
		}

		final List<StatisticalData> tmpStatisticalDataVector = new ArrayList<StatisticalData>();
		try {
			// get project
			final IMemento[] viewAttributes = this.memento.getChildren("viewAttributes"); //$NON-NLS-1$
			for (final IMemento viewAttribute : viewAttributes) {
				final String projectName = viewAttribute.getString("projectName"); //$NON-NLS-1$
				final IProject project = ResourcesPlugin.getWorkspace().getRoot().getProject(projectName);
				if ((project == null) || !project.exists() || !project.isOpen()) {
					return new ArrayList<StatisticalData>();
				}

				// retrieve log file meta data
				final String propertyFilePath = viewAttribute.getString("propertyFile"); //$NON-NLS-1$
				if (propertyFilePath == null) {
					return new ArrayList<StatisticalData>();
				}

				final File propertyFile = new File(propertyFilePath);
				if (!propertyFile.exists()) {
					return new ArrayList<StatisticalData>();
				}
				final LogFileMetaData tmpLogFileMetaData = LogFileCacheHandler.logFileMetaDataReader(propertyFile);

				// get log file
				final Path path = new Path(tmpLogFileMetaData.getProjectRelativePath());
				final IFile logFile = ResourcesPlugin.getWorkspace().getRoot().getFile(path);
				if ((logFile == null) || !logFile.exists() || !logFile.getProject().getName().equals(project.getName())) {
					return new ArrayList<StatisticalData>();
				}

				final File file = logFile.getLocation().toFile();

				// get file attributes to see if file has changed
				final String fileSizeString = viewAttribute.getString("fileSize"); //$NON-NLS-1$
				long fileSize = 0;
				if (fileSizeString != null) {
					fileSize = Long.parseLong(fileSizeString);
				}

				final String fileModificationString = viewAttribute.getString("fileModification");  //$NON-NLS-1$
				long fileModification = 0;
				if (fileModificationString != null) {
					fileModification = Long.valueOf(fileModificationString);
				}
				if ((file.lastModified() != fileModification) || (file.length() != fileSize)
						|| LogFileCacheHandler.hasLogFileChanged(logFile)) {
					return new ArrayList<StatisticalData>();
				}

				// create reader and set as input
				this.reader = new CachedLogReader(LogFileReader.getReaderForLogFile(logFile));
				final TestCaseExtractor extractor = new TestCaseExtractor();
				extractor.extractTestCasesFromIndexedLogFile(logFile);

				final StatisticalData statisticalData = new StatisticalData(tmpLogFileMetaData, extractor.getTestCases(), reader);
				tmpStatisticalDataVector.add(statisticalData);

			}
			return tmpStatisticalDataVector;

		} catch (Exception e) {
			ErrorReporter.logExceptionStackTrace(e);
		} finally {
			this.memento = null;
		}
		return new ArrayList<StatisticalData>();
	}

	/**
	 * Set the log file meta data
	 */
	public void setLogFileMetaData(final LogFileMetaData logFileMetaData) {
		this.logFileMetaData = logFileMetaData;
	}

	private String getPercent(final int noOf, final int noTotal) {
		int result = 0;

		if (noTotal > 0) {
			final float percent = ((float) noOf / (float) noTotal * 100);
			result = Math.round(percent);
		}
		if (result > 0) {
			return Messages.getString("StatisticalView.23")
					+ String.valueOf(result) + Messages.getString("StatisticalView.24");
		}
		return Messages.getString("StatisticalView.25");
	}

	private LogRecord getLogRecordAtRow(final int row) {
		LogRecord logRecord = null;
		try {
			logRecord = this.reader.getRecord(row);
		} catch (IOException e) {
			ErrorReporter.logExceptionStackTrace(e);
			TitanLogExceptionHandler.handleException(new TechnicalException(Messages.getString("TextTableModel.6") + e.getMessage()));  //$NON-NLS-1$
		} catch (ParseException e) {
			ErrorReporter.logExceptionStackTrace(e);
			TitanLogExceptionHandler.handleException(new TechnicalException(Messages.getString("TextTableModel.7") + e.getMessage()));  //$NON-NLS-1$
		}
		return logRecord;
	}

	private void createStatisticalViewContextMenuActions() {

		this.openMSCViewAction = new OpenMSCViewAction();
		this.openMSCViewAction.setEnabled(false);
		this.openMSCViewAction.setImageDescriptor(Activator.getDefault().getCachedImageDescriptor(Constants.ICONS_MSC_VIEW));
		this.addSelectionChangedListener(openMSCViewAction);

		this.openTextTableStatisticalViewMenuAction = new OpenTextTableStatisticalViewMenuAction(this);
		this.openTextTableStatisticalViewMenuAction.setEnabled(false);
		this.openTextTableStatisticalViewMenuAction.setImageDescriptor(Activator.getDefault().getCachedImageDescriptor(Constants.ICONS_TEXT_TABLE_VIEW));
	}

	/**
	 * Adds a menu to the selected row in the table
	 */
	private Menu hookStatisticalViewTableContextMenu(final Control control) {
		final ProjectsViewerMenuManager menuMgr = new ProjectsViewerMenuManager();
		menuMgr.setRemoveAllWhenShown(true);
		menuMgr.addMenuListener(new ProjectsViewerMenuListener() {
			@Override
			public void menuAboutToShow(final IMenuManager menuManager) {
				StatisticalView.this.fillStatisticalViewContextMenu(menuManager);
			}
		});
		return menuMgr.createContextMenu(control);
	}


	protected void fillStatisticalViewContextMenu(final IMenuManager menuManager) {
		// MB_ADDITIONS must be added to the menuMgr or platform will
		// throw a part initialize exception, this will fix this
		menuManager.add(new Separator(IWorkbenchActionConstants.MB_ADDITIONS));
		menuManager.add(this.openMSCViewAction);
		menuManager.add(this.openTextTableStatisticalViewMenuAction);
	}

	@Override
	public void addSelectionChangedListener(final ISelectionChangedListener listener) {
		if (!this.registeredListeners.contains(listener)) {
			this.registeredListeners.add(listener);
		}
	}

	@Override
	public ISelection getSelection() {
		return this.eventSelection;
	}

	@Override
	public void removeSelectionChangedListener(final ISelectionChangedListener listener) {
		if (this.registeredListeners.contains(listener)) {
			this.registeredListeners.remove(listener);
		}
	}

	@Override
	public void setSelection(final ISelection selection) {
		this.eventSelection = selection;
	}

	private void fireSelectionChangeEvent() {
		for (final ISelectionChangedListener listener : this.registeredListeners) {
			listener.selectionChanged(new SelectionChangedEvent(this, new StructuredSelection(this.testcaseSelection)));
		}
	}

	/**
	 * Create a close all action in the tool bar
	 */
	private void createToolbar() {

		final IActionBars actionBars = getViewSite().getActionBars();
		final IToolBarManager mgr = actionBars.getToolBarManager();

		final IAction closeAllAction = new Action() {
			@Override
			public void run() {

				final IWorkbenchPage activePage = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage();
				final IViewReference[] viewReferences = activePage.getViewReferences();

				for (final IViewReference reference : viewReferences) {
					final IViewPart view = reference.getView(false);

					// memento restored views that never have had focus are
					// null!!!
					if (view == null) {
						activePage.hideView(reference);
					} else if (view instanceof StatisticalView) {
						activePage.hideView(reference);
					}
				}
			}
		};

		closeAllAction.setImageDescriptor(ImageDescriptor.createFromImage(Activator.getDefault().getIcon(
				Constants.ICONS_MSC_DELETE)));
		closeAllAction.setId(Messages.getString("StatisticalView.27")); //$NON-NLS-1$
		closeAllAction.setToolTipText(Messages.getString("StatisticalView.26")); //$NON-NLS-1$
		closeAllAction.setEnabled(true);

		mgr.add(closeAllAction);
		actionBars.updateActionBars();
	}

	/**
	 * This is a callback that will allow us to create the viewer and
	 * initialize it.
	 */
	@Override
	public void createPartControl(final Composite parent) {
		final List<StatisticalData> statisticalData = restoreState();

		createToolbar();
		createStatisticalViewContextMenuActions();

		toolkit = new FormToolkit(parent.getDisplay());
		form = toolkit.createScrolledForm(parent);
		form.setText("Statistics"); //$NON-NLS-1$
		final TableWrapLayout layout = new TableWrapLayout();
		form.getBody().setLayout(layout);
		final TableWrapData td = new TableWrapData();
		td.colspan = 2;

		layout.numColumns = 2;

		toolkit.paintBordersFor(form.getBody());

		if (statisticalData != null) {
			setData(statisticalData);
		}
	}

	/**
	 * Passing the focus request to the form.
	 */
	@Override
	public void setFocus() {
		form.setFocus();
	}

	private Section createSection() {
		final Section section = toolkit.createSection(form.getBody(), Section.DESCRIPTION
				| ExpandableComposite.TWISTIE | ExpandableComposite.TITLE_BAR | ExpandableComposite.EXPANDED);
		final TableWrapData td = new TableWrapData(TableWrapData.FILL);
		td.colspan = 2;
		section.setLayoutData(td);
		section.addExpansionListener(new ExpansionAdapter() {
			@Override
			public void expansionStateChanged(final ExpansionEvent e) {
				form.reflow(true);
			}
		});

		final File file = new File(this.logFileMetaData.getFilePath());
		final Date date = new Date(file.lastModified());
		section.setText(file.getName());
		section.setData(this.logFileMetaData.getProjectRelativePath());
		section.setDescription(this.logFileMetaData.getProjectRelativePath() + " " + date.toString()); //$NON-NLS-1$
		final Composite sectionClient = toolkit.createComposite(section);
		sectionClient.setLayout(new GridLayout());
		createAmountTable(sectionClient);

		this.ecError = toolkit.createExpandableComposite(sectionClient, ExpandableComposite.TREE_NODE | ExpandableComposite.CLIENT_INDENT);
		ecError.setText("Error test cases"); //$NON-NLS-1$
		this.errorTestCasesTable = createTestCaseTable(ecError);
		ecError.setClient(this.errorTestCasesTable);
		ecError.addExpansionListener(new ExpansionAdapter() {
			@Override
			public void expansionStateChanged(final ExpansionEvent e) {
				form.reflow(true);
			}
		});

		this.ecFail = toolkit.createExpandableComposite(sectionClient, ExpandableComposite.TREE_NODE | ExpandableComposite.CLIENT_INDENT);
		ecFail.setText("Fail test cases"); //$NON-NLS-1$
		this.failTestCasesTable = createTestCaseTable(ecFail);
		ecFail.setClient(this.failTestCasesTable);

		ecFail.addExpansionListener(new ExpansionAdapter() {
			@Override
			public void expansionStateChanged(final ExpansionEvent e) {
				form.reflow(true);
			}
		});

		this.ecTestCases = toolkit.createExpandableComposite(sectionClient, ExpandableComposite.TREE_NODE | ExpandableComposite.CLIENT_INDENT);
		ecTestCases.setText("Test cases"); //$NON-NLS-1$
		this.testCasesTable = createTestCaseTable(ecTestCases);
		ecTestCases.setClient(this.testCasesTable);
		ecTestCases.addExpansionListener(new ExpansionAdapter() {
			@Override
			public void expansionStateChanged(final ExpansionEvent e) {
				form.reflow(true);
			}
		});
		section.setClient(sectionClient);

		cachedSections.put(this.logFileMetaData.getProjectRelativePath(), section);

		return section;

	}

	private Table createAmountTable(final Composite composite) {
		this.amountTable = toolkit.createTable(composite, SWT.NONE);
		amountTable.setBackgroundMode(SWT.INHERIT_DEFAULT);
		this.amountTable.setHeaderVisible(true);
		this.amountTable.setLinesVisible(true);

		createAmountColumn(Messages.getString("StatisticalView.1"));
		createAmountColumn(Messages.getString("StatisticalView.2"));
		createAmountColumn(Messages.getString("StatisticalView.3"));
		createAmountColumn(Messages.getString("StatisticalView.4"));
		createAmountColumn(Messages.getString("StatisticalView.5"));
		createAmountColumn(Messages.getString("StatisticalView.6"));
		createAmountColumn(Messages.getString("StatisticalView.7"));
		return amountTable;
	}

	private void createAmountColumn(final String title) {
		final TableColumn column = new TableColumn(this.amountTable, SWT.BORDER);
		column.setText(title);
		column.setResizable(true);
		column.setMoveable(true);
		column.setWidth(DEFAULT_AMOUNT_COLUMN_WIDTH);
	}

	private Table createTestCaseTable(final Composite composite) {

		final Table testCasesTable = toolkit.createTable(composite, SWT.H_SCROLL | SWT.V_SCROLL | SWT.FULL_SELECTION | SWT.VIRTUAL);
		testCasesTable.setHeaderVisible(true);
		testCasesTable.setLinesVisible(true);

		testCasesTable.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(final SelectionEvent e) {
				if (e.getSource() instanceof Table) {
					final Table table = (Table) e.getSource();

					final TableItem tableItem = table.getItem(table.getSelectionIndex());
					final Object data = tableItem.getData();
					if (data instanceof TestCase) {
						StatisticalView.this.testcaseSelection = (TestCase) data;
					} else {
						StatisticalView.this.testcaseSelection = null;
					}
					fireSelectionChangeEvent();
				}
			}
		});
		testCasesTable.setMenu(hookStatisticalViewTableContextMenu(testCasesTable));

		new TableColumn(testCasesTable, SWT.BORDER);
		createTestCasesColumn(testCasesTable, Messages.getString("StatisticalView.9"), DEFAULT_COLUMN_WIDTH);
		createTestCasesColumn(testCasesTable, Messages.getString("StatisticalView.10"), 5 * DEFAULT_COLUMN_WIDTH);
		createTestCasesColumn(testCasesTable, Messages.getString("StatisticalView.11"), 4* DEFAULT_COLUMN_WIDTH);
		createTestCasesColumn(testCasesTable, Messages.getString("StatisticalView.12"), 4* DEFAULT_COLUMN_WIDTH);
		testCasesTable.redraw();
		return testCasesTable;
	}

	private void createTestCasesColumn(final Table testCasesTable, final String title, final int width) {
		final TableColumn column = new TableColumn(testCasesTable, SWT.BORDER);
		column.setText(title);
		column.setResizable(true);
		column.setMoveable(true);
		column.setWidth(width);
	}

	@Override
	public LogFileMetaData getLogFileMetaData() {
		return logFileMetaData;
	}

	@Override
	public String getName() {
		return "Statistical View";
	}

}

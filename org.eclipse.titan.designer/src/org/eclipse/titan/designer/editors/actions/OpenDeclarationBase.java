package org.eclipse.titan.designer.editors.actions;

import java.text.MessageFormat;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.resources.IFile;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.text.TextSelection;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.titan.common.logging.ErrorReporter;
import org.eclipse.titan.designer.AST.Location;
import org.eclipse.titan.designer.commonFilters.ResourceExclusionHelper;
import org.eclipse.titan.designer.core.TITANNature;
import org.eclipse.ui.IEditorActionDelegate;
import org.eclipse.ui.IEditorDescriptor;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.part.FileEditorInput;
import org.eclipse.ui.texteditor.AbstractTextEditor;

/**
 * Serves as base class for OpenDeclaration subclasses
 * 
 * @author Adam Knapp
 * */

public abstract class OpenDeclarationBase extends AbstractHandler implements IEditorActionDelegate {
	public static final String FILENOTIDENTIFIABLE = "The file related to the editor could not be identified";
	public static final String EDITORNOTFOUND = "The {0} editor could not be found";
	
	protected IEditorPart targetEditor = null;
	protected ISelection selection = TextSelection.emptySelection();
	
	@Override
	public Object execute(final ExecutionEvent event) throws ExecutionException {
		targetEditor = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage().getActiveEditor();

		doOpenDeclaration();

		return null;
	}

	@Override
	public final void run(IAction action) {
		doOpenDeclaration();
	}

	@Override
	public final void selectionChanged(IAction action, ISelection selection) {
		this.selection = selection;
	}

	@Override
	public final void setActiveEditor(IAction action, IEditorPart targetEditor) {
		this.targetEditor = targetEditor;
	}

	/**
	 * Called by the {@link #run(IAction)} method
	 */
	protected abstract void doOpenDeclaration();
	
	/**
	 * Opens an editor for the provided declaration, and in this editor the
	 * location of the declaration is revealed and selected.
	 *
	 * @param declaration the declaration to reveal
	 * @param errorMesssage
	 * */
	protected void selectAndRevealDeclaration(final Location location, final String editorType) {
		final IEditorDescriptor desc = PlatformUI.getWorkbench().getEditorRegistry().getDefaultEditor(location.getFile().getName());
		if (desc == null) {
			targetEditor.getEditorSite().getActionBars().getStatusLineManager().setErrorMessage(MessageFormat.format(EDITORNOTFOUND, editorType));
			return;
		}

		try {
			final IWorkbenchPage page = targetEditor.getSite().getPage();
			final IEditorPart editorPart = page.openEditor(new FileEditorInput((IFile) location.getFile()), desc.getId());
			if (editorPart != null && (editorPart instanceof AbstractTextEditor)) {
				((AbstractTextEditor) editorPart).selectAndReveal(location.getOffset(),
						location.getEndOffset() - location.getOffset());
			}

		} catch (PartInitException e) {
			ErrorReporter.logExceptionStackTrace(e);
		}
	}
	
	/**
	 * Used in {@link #doOpenDeclaration()} to perform basic checks
	 */
	protected boolean check() {
		targetEditor.getEditorSite().getActionBars().getStatusLineManager().setErrorMessage(null);

		final IFile file = (IFile) targetEditor.getEditorInput().getAdapter(IFile.class);
		if (file == null) {
			targetEditor.getEditorSite().getActionBars().getStatusLineManager().setErrorMessage(FILENOTIDENTIFIABLE);
			return false;
		}

		if (!TITANNature.hasTITANNature(file.getProject())) {
			targetEditor.getEditorSite().getActionBars().getStatusLineManager().setErrorMessage(TITANNature.NO_TITAN_FILE_NATURE_FOUND);
			return false;
		}

		if (ResourceExclusionHelper.isExcluded(file)) {
			MessageDialog.openError(null, "Open Declaration does not work within excluded resources",
					"This module is excluded from build. To use the Open Declaration "
							+ "feature please click on the 'Toggle exclude from build state' in the context menu of the Project Explorer. ");
			return false;
		}
		
		return true;
	}
}

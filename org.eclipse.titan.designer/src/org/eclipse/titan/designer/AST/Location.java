/******************************************************************************
 * Copyright (c) 2000-2016 Ericsson Telecom AB
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 ******************************************************************************/
package org.eclipse.titan.designer.AST;

import java.util.HashMap;
import java.util.Map;

import org.antlr.v4.runtime.Token;
import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.IWorkspaceRunnable;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.titan.common.logging.ErrorReporter;
import org.eclipse.titan.designer.GeneralConstants;

/**
 * The Location class represents a location in the source code.
 * <p>
 * This class is mainly used to: locate language elements, build structures based on their textual positions.
 * This class is also used to report some kind of warning or error to a given location.
 * 
 * @author Kristof Szabados
 * @author Arpad Lovassy
 */
public class Location {

	/**
	 * File if the source code.
	 */
	private IResource file;

	/**
	 * Line number.
	 * Starts with 1, or -1 if undefined.
	 */
	private int line;

	/**
	 * Position before the first character of the location.
	 * Starts with 0, or -1 if undefined.
	 */
	private int offset;

	/**
	 * Position after the last character of the location. -1 if undefined.
	 * In other words: the start position of the last character + 1, because a character is 1 character long, so
	 * end position of a character == start position of a character + 1 
	 * <p>
	 * For example:
	 * <pre>
	 *   __apple__
	 *   012345678
	 * </pre>
	 *   The location of "apple" is: offset == 2, endOffset == 7
	 * <p>
	 * NOTE: org.antlr.v4.runtime.Token.getStopIndex() returns the start position of the last character,
	 *       so if Location is converted from Token, +1 must be added to getStopIndex(),
	 *       so Location.endOffset = Token.getStopIndex() + 1,
	 *       but Location.offset = Token.getStartIndex()
	 */
	private int endOffset;

	/**
	 * Copy constructor
	 * @param location original location object to copy
	 */
	public Location(final Location location) {
		setLocation(location);
	}

	public Location(final IResource file, final int line, final int offset, final int endOffset) {
		setLocation(file, line, offset, endOffset);
	}

	public Location(final IResource file, final int line) {
		this( file, line, -1, -1 );
	}

	public Location(final IResource file) {
		this( file, -1 );
	}

	/**
	 * Constructor.
	 * Default implementation for NULL_Location
	 */
	protected Location() {
		this( ( IResource )null );
	}

	/**
	 * Constructor for ANTLR v4 tokens
	 * @param aFile the parsed file
	 * @param aStartToken the 1st token, its line and start position will be used for the location
	 *                  NOTE: start position is the column index of the tokens 1st character.
	 *                        Column index starts with 0.
	 * @param aEndToken the last token, its end position will be used for the location.
	 *                  NOTE: end position is the column index after the token's last character.
	 */
	public Location( final IResource aFile, final Token aStartToken, final Token aEndToken ) {
		setLocation( aFile, aStartToken.getLine(), aStartToken.getStartIndex(),
					 aEndToken.getStopIndex() + 1 );
	}

	/**
	 * Sets the offset with an ANTLR v4 end token
	 * @param aEndToken the new end token
	 */
	public final void setOffset(final Token aToken) {
		this.setOffset( aToken.getStartIndex() );
	}

	public static Location interval(final Location startLoc, final Location endLoc) {
		return new Location(startLoc.getFile(), startLoc.getLine(), startLoc.getOffset(), endLoc.getEndOffset());
	}

	private final void setLocation(final Location location) {
		file = location.getFile();
		line = location.getLine();
		offset = location.getOffset();
		endOffset = location.getEndOffset();
	}

	private final void setLocation(final IResource file, final int line, final int offset, final int endOffset) {
		this.file = file;
		this.line = line;
		this.offset = offset;
		this.endOffset = endOffset;
	}

	public final IResource getFile() {
		return file;
	}

	public final int getLine() {
		return line;
	}

	public final void setLine(final int line) {
		this.line = line;
	}

	public final int getOffset() {
		return offset;
	}

	public final void setOffset(final int offset) {
		this.offset = offset;
	}

	public final int getEndOffset() {
		return endOffset;
	}

	public final void setEndOffset(final int endOffset) {
		this.endOffset = endOffset;
	}

	@Override
	public boolean equals(final Object object) {
		if (this == object) {
			return true;
		}
		
		if(object instanceof Location) {
			final Location other = (Location) object;
			
			// this order of checking is more likely to fail fast
			return line == other.line && offset == other.offset && endOffset == other.endOffset && file.equals(other.file);
		}

		return false;
	}

	/**
	 * Checks whether the given offset is inside this location.
	 * @param offset the offset
	 * @return true if the offset is inside this location
	 */
	public final boolean containsOffset(final int offset) {
		return this.offset <= offset && this.endOffset >= offset;
	}

	/**
	 * Reports a syntactic error to this location containing the supplied reason of error.
	 *
	 * @param reason the reason for the error.
	 * */
	public void reportSyntacticError(final String reason) {
		reportProblem(reason, IMarker.SEVERITY_ERROR, GeneralConstants.ONTHEFLY_SYNTACTIC_MARKER);
	}

	/**
	 * Reports a syntactic warning to this location containing the supplied reason of warning.
	 *
	 * @param reason the reason for the error.
	 * */
	public void reportSyntacticWarning(final String reason) {
		reportProblem(reason, IMarker.SEVERITY_WARNING, GeneralConstants.ONTHEFLY_SYNTACTIC_MARKER);
	}

	/**
	 * Reports a semantic problem to this location containing the supplied reason of problem according to a configuration.
	 * <p>
	 * As this method does not check the existence of a problem marker at this location, with this message this is very fast.
	 *
	 * @param option the way the problem should be reported.
	 * @param reason the reason for the problem.
	 * */
	public void reportConfigurableSemanticProblem(final String option, final String reason) {
		if (GeneralConstants.WARNING.equals(option)) {
			reportSemanticWarning(reason);
		} else if (GeneralConstants.ERROR.equals(option)) {
			reportSemanticError(reason);
		}
	}

	/**
	 * Reports a semantic error to this location containing the supplied reason of error.
	 * <p>
	 * As this method does not check the existence of an error marker at this location, with this message this is very fast.
	 *
	 * @param reason the reason for the error.
	 * */
	public void reportSemanticError(final String reason) {
		reportProblem(reason, IMarker.SEVERITY_ERROR, GeneralConstants.ONTHEFLY_SEMANTIC_MARKER);
	}

	/**
	 * Reports a semantic error to this location containing the supplied reason of error.
	 * <p>
	 * This method before reporting the error, checks if an error with the same reason was already reported to the same location.
	 * If such an error is found, the new one will not be reported.
	 * This way of reporting errors can be rather slow.
	 *
	 * @param reason the reason for the error.
	 * */
	public void reportSingularSemanticError(final String reason) {
		if (!alreadyExists(GeneralConstants.ONTHEFLY_SEMANTIC_MARKER, IMarker.SEVERITY_ERROR, reason)) {
			reportProblem(reason, IMarker.SEVERITY_ERROR, GeneralConstants.ONTHEFLY_SEMANTIC_MARKER);
		}
	}

	/**
	 * Reports a semantic warning to this location containing the supplied reason of warning.
	 * <p>
	 * As this method does not check the existence of an warning marker at this location, with this message this is very fast.
	 *
	 * @param reason the reason for the error.
	 * */
	public void reportSemanticWarning(final String reason) {
		reportProblem(reason, IMarker.SEVERITY_WARNING, GeneralConstants.ONTHEFLY_SEMANTIC_MARKER);
	}

	/**
	 * Reports a semantic warning to this location containing the supplied reason of warning.
	 * <p>
	 * This method before reporting the warning, checks if a warning with the same reason was already reported to the same location.
	 * If such a warning is found, the new one will not be reported.
	 * This way of reporting warning can be rather slow.
	 *
	 * @param reason the reason for the error.
	 * */
	public void reportSingularSemanticWarning(final String reason) {
		if (!alreadyExists(GeneralConstants.ONTHEFLY_SEMANTIC_MARKER, IMarker.SEVERITY_WARNING, reason)) {
			reportProblem(reason, IMarker.SEVERITY_WARNING, GeneralConstants.ONTHEFLY_SEMANTIC_MARKER);
		}
	}

	
	/**
	 * Reports a syntactic error in semantic check's time to this location containing the supplied reason of error.
	 * This is for extension attribute and ASN.1 parser
	 * @param reason the reason for the error.
	 * */
	public void reportMixedError(final String reason) {
		reportProblem(reason, IMarker.SEVERITY_ERROR, GeneralConstants.ONTHEFLY_MIXED_MARKER);
	}

	/**
	 * Reports a syntactic warning in semantic check's time to this location containing the supplied reason of warning.
	 * This is for extension attribute and ASN.1 parser
	 * @param reason the reason for the error.
	 * */
	public void reportMixedWarning(final String reason) {
		reportProblem(reason, IMarker.SEVERITY_WARNING, GeneralConstants.ONTHEFLY_MIXED_MARKER);
	}

	
	/**
	 * Checks if a marker with the same reason exist on the same location.
	 *
	 * @param markerTypeID the marker type to check for
	 * @param severity the severity of the marker to check for.
	 * @param reason the reason to check for.
	 *
	 * @return true if there is a problem marker on the same position with the same reason.
	 * */
	protected final boolean alreadyExists(final String markerTypeID, final int severity, final String reason) {
		if (file == null || !file.isAccessible()) {
			return false;
		}

		final IMarker marker = MarkerHandler.hasMarker(markerTypeID, file, line, offset, endOffset, severity, reason);
		if (marker == null) {
			return false;
		}

		return !MarkerHandler.isMarkerdForRemoval(markerTypeID, file, marker.getId());
	}

	public void reportExternalProblem(final String reason, final int severity, final String markerID) {
		reportProblem(reason, severity, markerID);
	}

	public void reportSingularExternalProblem(final String reason, final int severity, final String markerID) {
		if (!alreadyExists(markerID, severity, reason)) {
			reportProblem(reason, severity, markerID);
		}
	}

	public void reportExternalProblem(final String reason, final int severity, final int priority, final String markerID) {
		reportProblem(reason, severity, priority, markerID);
	}

	/**
	 * Does the actual of job of reporting the problem.
	 *
	 * @param reason the reason for the problem.
	 * @param severity the severity to report it with.
	 * @param markerID the identifier of the type of the marker.
	 * */
	protected void reportProblem(final String reason, final int severity, final String markerID) {
		reportProblem(reason, severity, IMarker.PRIORITY_HIGH, markerID);
	}
	
	protected void reportProblem(final String reason, final int severity, final int priority, final String markerID) {
		final Map<String, Object> markerProperties = new HashMap<String, Object>();

		final Integer lineNumber = Integer.valueOf(line);

		if (line != -1) {
			markerProperties.put(IMarker.LINE_NUMBER, lineNumber);
		}
		if (offset != -1) {
			markerProperties.put(IMarker.CHAR_START, Integer.valueOf(offset));
		}
		if (endOffset != -1) {
			markerProperties.put(IMarker.CHAR_END, Integer.valueOf(endOffset));
		}
		markerProperties.put(IMarker.SEVERITY, Integer.valueOf(severity));
		markerProperties.put(IMarker.PRIORITY, Integer.valueOf(priority));
		markerProperties.put(IMarker.MESSAGE, reason);
		markerProperties.put(IMarker.TRANSIENT, Boolean.TRUE);
		try {
			if (file != null && file.isAccessible()) {
				final IMarker marker = MarkerHandler.hasMarker(markerID, file, line, offset, endOffset, severity, reason);
				if (marker != null) {
					MarkerHandler.markUsed(markerID, file, marker.getId());
				} else {
					final MarkerCreator markerCreator = new MarkerCreator(markerID, markerProperties);
					file.getWorkspace().run(markerCreator, null, IWorkspace.AVOID_UPDATE, null);
					final IMarker createdMarker = markerCreator.getMarker();
					final long markerId = createdMarker.getId();
					MarkerHandler.addMarker(markerID, file, line, offset, endOffset, markerId);
				}
			}
		} catch (CoreException e) {
			ErrorReporter.logExceptionStackTrace("Error while creating marker", e);
		}
	}

	private final class MarkerCreator implements IWorkspaceRunnable {
		private IMarker marker;
		private final String markerID;
		private final Map<String, Object> markerProperties;

		public MarkerCreator(final String markerID, final Map<String, Object> markerProperties) {
			this.markerID = markerID;
			this.markerProperties = markerProperties;
		}

		@Override
		public void run(final IProgressMonitor monitor) throws CoreException {
			marker = file.createMarker(markerID);
			marker.setAttributes(markerProperties);
		}

		public IMarker getMarker() {
			return marker;
		}
	}
}

/******************************************************************************
 * Copyright (c) 2000-2019 Ericsson Telecom AB
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/org/documents/epl-2.0/EPL-2.0.html
 ******************************************************************************/
package org.eclipse.titan.common.log.format;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.titan.common.utils.StandardCharsets;
import org.eclipse.titan.common.utils.StringUtils;

/**
 * This class is responsible for formatting log files
 *
 * @author Kristof Szabados
 */
public class LogFormatter {

	private static final int DEFAULT_INDENTATION_SIZE = 100;

	/** The number of bytes represented by a tick in the progress report */
	public static final int TICK_SIZE = 65536;
	private static final int IN_BUFFER_SIZE = 8192;
	private static final int OUT_BUFFER_SIZE = 65536;

	/** indent with 4 spaces */
	private static final int INDENTATION_SIZE = 4;

	private static final byte[] NEWLINE = StringUtils.lineSeparator().getBytes(StandardCharsets.UTF8);

	private enum LastTokenTypes {
		OPEN_BRACE, CLOSE_BRACE, COMMA, WHITE_SPACE, OTHER
	}

	private static byte[] indentation;
	static {
		resizeIndentation(DEFAULT_INDENTATION_SIZE);
	}

	private LastTokenTypes lastToken;
	private int indentationLevel;
	private boolean insideString;

	public LogFormatter() {
	}

	public void format(final IProgressMonitor internalMonitor, final FileChannel sourceChannel, final FileChannel targetChannel) throws IOException {
		long nofProcessedBytes = 0;
		indentationLevel = 0;
		insideString = false;
		boolean cancelled = false;
		lastToken = LastTokenTypes.OTHER;
		final ByteBuffer sourceBuffer = ByteBuffer.allocateDirect(IN_BUFFER_SIZE);
		final ByteBuffer targetBuffer = ByteBuffer.allocate(OUT_BUFFER_SIZE);
		sourceBuffer.clear();
		while (!cancelled && sourceChannel.read(sourceBuffer) != -1) {
			if (internalMonitor.isCanceled()) {
				cancelled = true;
			}

			sourceBuffer.flip();

			processBuffer(sourceBuffer, targetBuffer, targetChannel);

			nofProcessedBytes += sourceBuffer.limit();
			sourceBuffer.flip();
			targetBuffer.flip();

			targetChannel.write(targetBuffer);
			targetBuffer.clear();

			if (nofProcessedBytes > TICK_SIZE) {
				internalMonitor.worked((int) nofProcessedBytes / TICK_SIZE);
				nofProcessedBytes %= TICK_SIZE;
			}
		}
	}

	private void processBuffer(final ByteBuffer source, final ByteBuffer targetBuffer, final FileChannel targetChannel) throws IOException {
		byte actualByte;
		while (source.hasRemaining()) {
			actualByte = source.get();
			if (targetBuffer.remaining() < indentationLevel * INDENTATION_SIZE + 1) {
				targetBuffer.flip();
				targetChannel.write(targetBuffer);
				targetBuffer.clear();
			}

			if (insideString) {
				processInsideString(source, targetBuffer, actualByte);
			} else {
				outsideString(source, targetBuffer, actualByte);
			}
		}
	}

	private void outsideString(final ByteBuffer source, final ByteBuffer target, final byte actualByte) {
		switch (actualByte) {
		case '{':
			if (indentationLevel > 0) {
				switch (lastToken) {
				case OPEN_BRACE:
				case COMMA:
					target.put(NEWLINE);
					indent(target, indentationLevel);
					break;
				default:
					target.put((byte) ' ');
					break;
				}
			}
			target.put(actualByte);
			indentationLevel += 1;
			lastToken = LastTokenTypes.OPEN_BRACE;
			break;
		case '}':
			if (indentationLevel > 0) {
				indentationLevel -= 1;
				if (LastTokenTypes.OPEN_BRACE.equals(lastToken)) {
					target.put((byte) ' ');
				} else {
					target.put(NEWLINE);
					indent(target, indentationLevel);
				}
				lastToken = LastTokenTypes.CLOSE_BRACE;
			}
			target.put(actualByte);
			break;
		case ',':
			target.put(actualByte);
			if (indentationLevel > 0) {
				lastToken = LastTokenTypes.COMMA;
			}
			break;
		case '\"':
			target.put(actualByte);
			insideString = true;
			break;
		case ' ':
		case '\t':
			if (indentationLevel > 0) {
				if (LastTokenTypes.OTHER.equals(lastToken)) {
					lastToken = LastTokenTypes.WHITE_SPACE;
				}
			} else {
				target.put(actualByte);
			}
			break;
		case '\n':
			if (indentationLevel > 0) {
				if (LastTokenTypes.OTHER.equals(lastToken)) {
					lastToken = LastTokenTypes.WHITE_SPACE;
				}
			} else {
				target.put(NEWLINE);
			}
			break;
		case '\r':
			if (source.remaining() > 0) {
				final byte temp2 = source.get();
				if ('\n' == temp2) {
					if (indentationLevel > 0) {
						if (LastTokenTypes.OTHER.equals(lastToken)) {
							lastToken = LastTokenTypes.WHITE_SPACE;
						}
					} else {
						target.put(NEWLINE);
					}
				} else {
					target.put(NEWLINE);
				}
			} else {
				target.put(actualByte);
			}
			break;
		default:
			if (indentationLevel > 0) {
				switch (lastToken) {
				case OPEN_BRACE:
				case COMMA:
					target.put(NEWLINE);
					indent(target, indentationLevel);
					break;
				case CLOSE_BRACE:
				case WHITE_SPACE:
					target.put((byte) ' ');
					break;
				default:
					break;
				}
				lastToken = LastTokenTypes.OTHER;
			}
			target.put(actualByte);
			break;
		}
	}

	private void processInsideString(final ByteBuffer source, final ByteBuffer target, final byte actualByte) {
		target.put(actualByte);
		switch (actualByte) {
		case '\"':
			insideString = false;
			break;
		case '\\':
			if (source.hasRemaining()) {
				final byte temp = source.get();
				target.put(temp);
			}
			break;
		default:
			break;
		}
	}

	private void indent(final ByteBuffer target, final int amount) {
		final int temp = amount * INDENTATION_SIZE;
		if (temp > indentation.length) {
			resizeIndentation(temp);
		}

		target.put(indentation, 0, temp);
	}

	private static void resizeIndentation(final int newSize) {
		indentation = new byte[newSize];
		for (int i = 0; i < newSize; i++) {
			indentation[i] = (byte) ' ';
		}
	}

}

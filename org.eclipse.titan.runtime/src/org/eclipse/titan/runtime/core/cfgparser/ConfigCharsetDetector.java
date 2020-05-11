/******************************************************************************
 * Copyright (c) 2000-2020 Ericsson Telecom AB
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/org/documents/epl-2.0/EPL-2.0.html
 ******************************************************************************/
package org.eclipse.titan.runtime.core.cfgparser;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.channels.FileChannel.MapMode;
/**
 * JDK 7 or above
 * import java.nio.file.Files;
 */

import org.eclipse.titan.runtime.core.TtcnError;

/**
 * Character set detector for guessing the encoding of the config file.
 * 
 * @author Gergo Ujhelyi
 *
 */
public final class ConfigCharsetDetector {

	private byte[] char_bytes;

	private static byte[] UTF8_BOM = { (byte) 0xEF, (byte) 0xBB, (byte) 0xBF };
	private static byte[] UTF16_LE_BOM = { (byte) 0xFF, (byte) 0xFE };
	private static byte[] UTF16_BE_BOM = { (byte) 0xFE, (byte) 0xFF };

	public ConfigCharsetDetector(final File p_file) {
		FileInputStream file_input = null;
		FileChannel file_channel = null;
		try {
			file_input = new FileInputStream(p_file);
			file_channel = file_input.getChannel();
			final int file_size = (int) (file_channel.size());
			final MappedByteBuffer buffer = file_channel.map(MapMode.READ_ONLY, 0, file_size);
			char_bytes = new byte[file_size];
			buffer.get(char_bytes);
			/**
			 * JDK 7 or above: can use this line only. char_bytes =
			 * Files.readAllBytes(p_file.toPath());
			 **/
		} catch (IOException e) {
			throw new TtcnError(e);
		} finally {
			try {
				if (file_input != null) {
					file_input.close();
				}
				if (file_channel != null) {
					file_channel.close();
				}
			} catch (IOException e) {
				throw new TtcnError(e);
			}
		}
	}

	public ConfigCharsetDetector(final byte[] p_bytes) {
		if (p_bytes != null) {
			char_bytes = p_bytes;
		}
	}

	public String detectCharSet() {
		// First, check ASCII encoding
		if (char_bytes == null) {
			return null;
		}
		for (int i = 0; i < char_bytes.length; i++) {
			if ((char_bytes[i] & 0x80) != 0) {
				break;
			}
			// Every bytes are under 0x80
			if (i == char_bytes.length - 1) {
				return "US-ASCII";
			}
		}

		final String detectedBom = detectBOM();
		if (detectedBom != null) {
			return detectedBom;
		}

		if (char_bytes.length < 3) {
			return null;
		}

		if (isUTF8()) {
			return "UTF-8";
		}

		// Can't guess the encoding -> using default.
		return null;
	}

	/**
	 * Checking the Byte Order Mark at the beginning of the file.
	 * 
	 * @return the detected BOM or null if it wasn't there.
	 */
	private String detectBOM() {
		if (char_bytes.length < 3) {
			return null;
		}
		if (char_bytes[0] == UTF8_BOM[0] && char_bytes[1] == UTF8_BOM[1] && char_bytes[2] == UTF8_BOM[2]) {
			return "UTF-8";
		} else if (char_bytes[0] == UTF16_LE_BOM[0] && char_bytes[1] == UTF16_LE_BOM[1]) {
			return "UTF-16LE";
		} else if (char_bytes[0] == UTF16_BE_BOM[0] && char_bytes[1] == UTF16_BE_BOM[1]) {
			return "UTF-16BE";
		}
		return null;
	}

	private boolean isUTF8() {
		int code_length = 0;
		int i = 0;
		final int data_length = char_bytes.length;
		int ch = 0;
		while (i != data_length) {
			final int temp_byte = char_bytes[i] & 0xFF;
			if (temp_byte <= 0x7F) {
				// 1 byte sequence : 0xxxxxxx
				i++;
				continue;
			}
			if (0xC2 <= temp_byte && temp_byte <= 0xDF) {
				// First byte of the 2 bytes sequence: 110xxxxx
				code_length = 2;
			} else if (0xE0 <= temp_byte && temp_byte <= 0xEF) {
				// First byte of the 3 bytes sequence: 1110xxxx
				code_length = 3;
			} else if (0xF0 <= temp_byte && temp_byte <= 0xF4) {
				// First byte of the 4 bytes sequence: 11110xxx
				code_length = 4;
			} else {
				return false;
			}
			if (i + (code_length - 1) >= data_length) {
				return false;
			}
			// bit 7 should be set, bit 6 should be unset
			for (int j = 1; j < code_length; j++) {
				if ((char_bytes[i + j] & 0xC0) != 0x80) {
					return false;
				}
			}
			if (code_length == 2) {
				// 2 bytes sequence: 110xxxxx 10xxxxxx
				ch = ((char_bytes[i] & 0x1F) << 6) + (char_bytes[i + 1] & 0x3F);
			} else if (code_length == 3) {
				// 3 bytes sequence: 1110xxxx 10xxxxxx 10xxxxxx
				ch = ((char_bytes[i] & 0x0F) << 12) + ((char_bytes[i + 1] & 0x3F) << 6) + (char_bytes[i + 2] & 0x3F);
				if (ch < 0x0800) {
					return false;
				}
				if ((ch >> 11) == 0x1B) {
					return false;
				}
			} else if (code_length == 4) {
				// 4 bytes sequence: 11110xxx 10xxxxxx 10xxxxxx 10xxxxxx
				ch = ((char_bytes[i] & 0x07) << 18) + ((char_bytes[i + 1] & 0x3F) << 12)
						+ ((char_bytes[i + 2] & 0x3F) << 6) + (char_bytes[i + 3] & 0x3F);
				if ((ch < 0x10000) || (ch > 0x10FFF)) {
					return false;
				}
			}
			i += code_length;
		}
		return true;
	}

}

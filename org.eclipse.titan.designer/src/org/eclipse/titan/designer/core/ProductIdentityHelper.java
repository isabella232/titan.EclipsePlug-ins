/******************************************************************************
 * Copyright (c) 2000-2021 Ericsson Telecom AB
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/org/documents/epl-2.0/EPL-2.0.html
 ******************************************************************************/
package org.eclipse.titan.designer.core;

import java.math.BigInteger;
import java.text.MessageFormat;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.eclipse.titan.common.product.ProductIdentity;
import org.eclipse.titan.designer.AST.Location;

/**
 * @author Kristof Szabados
 * @author Adam Knapp
 * */
public final class ProductIdentityHelper {

	private static final Pattern PRODUCT_PATTERN1 = Pattern.compile("^([A-Z]{3,5}[ ][0-9]{3}[ ][0-9]{2,4})/([0-9]+)[ ](R.+)$");
	private static final Matcher PRODUCT_PATTERN1_MATCHER = PRODUCT_PATTERN1.matcher("");
	private static final Pattern PRODUCT_PATTERN2 = Pattern.compile("^([0-9]+)/([A-Z]{3,5}[ ][0-9]{3}[ ][0-9]{2,4})[ ](R.+)$");
	private static final Matcher PRODUCT_PATTERN2_MATCHER = PRODUCT_PATTERN2.matcher("");
	private static final Pattern PRODUCT_PATTERN3 = Pattern.compile("^([A-Z]{3,5}[ ][0-9]{3}[ ][0-9]{2,4})[ ](R.+)$");
	private static final Matcher PRODUCT_PATTERN3_MATCHER = PRODUCT_PATTERN3.matcher("");
	private static final Pattern PRODUCT_PATTERN4 = Pattern.compile("^(R.+)$");
	private static final Matcher PRODUCT_PATTERN4_MATCHER = PRODUCT_PATTERN4.matcher("");
	private static final Pattern PRODUCT_PATTERN5 = Pattern.compile("^([0-9]+.[0-9]+.[0-9]+)");
	private static final Matcher PRODUCT_PATTERN5_MATCHER = PRODUCT_PATTERN5.matcher("");

	private static final Pattern RNUMBER_PATTERN1 = Pattern.compile("^R([0-9]+)([A-Z]+)([0-9]+)$");
	private static final Matcher RNUMBER_PATTERN1_MATCHER = RNUMBER_PATTERN1.matcher("");
	private static final Pattern RNUMBER_PATTERN2 = Pattern.compile("^R([0-9]+)([A-Z]+)$");
	private static final Matcher RNUMBER_PATTERN2_MATCHER = RNUMBER_PATTERN2.matcher("");
	private static final Pattern RNUMBER_PATTERN3 = Pattern.compile("^([0-9]+).([0-9]+).([0-9]+)");
	private static final Matcher RNUMBER_PATTERN3_MATCHER = RNUMBER_PATTERN3.matcher("");
	
	private static final String DEFAULT_SEMANTIC_ERROR_MESSAGE = "Wrong format for product version information: "
			+ "The accepted formats resemble CRL 113 200/1 R9A or 7/CAX 105 7730 R2A or 7.2.1";

	private ProductIdentityHelper() {
	}

	/**
	 * Validates the input and if found correctly returns a version number
	 * representing that version. Also if the location is set and the input
	 * is erroneous, the error is reported to that location.
	 * 
	 * @param versionString
	 *                the version string in Ericsson format.
	 * @param location
	 *                if set the possible errors will be reported here.
	 * @return the new version number, or null if the input is not correct.
	 * */
	public static synchronized ProductIdentity getProductIdentity(final String versionString, final Location location) {
		if (versionString == null) {
			return null;
		}

		if (versionString.contains("RnXnn")) {
			return new ProductIdentity();
		}

		String productNumber = null;
		String productNumberSuffix = null;
		String rNumber;
		// C[RN]L 113 200/7 R2A
		if (PRODUCT_PATTERN1_MATCHER.reset(versionString).matches()) {
			productNumber = PRODUCT_PATTERN1_MATCHER.group(1);
			productNumberSuffix = PRODUCT_PATTERN1_MATCHER.group(2);
			rNumber = PRODUCT_PATTERN1_MATCHER.group(3);
		// 7/CAX 105 7730 R2A
		} else if (PRODUCT_PATTERN2_MATCHER.reset(versionString).matches()) {
			productNumberSuffix = PRODUCT_PATTERN2_MATCHER.group(1);
			productNumber = PRODUCT_PATTERN2_MATCHER.group(2);
			rNumber = PRODUCT_PATTERN2_MATCHER.group(3);
		} else if (PRODUCT_PATTERN3_MATCHER.reset(versionString).matches()) {
			productNumber = PRODUCT_PATTERN3_MATCHER.group(1);
			rNumber = PRODUCT_PATTERN3_MATCHER.group(2);
		// R1A1
		} else if (PRODUCT_PATTERN4_MATCHER.reset(versionString).matches()) {
			rNumber = PRODUCT_PATTERN4_MATCHER.group(1);
		// 7.2.1
		} else if (PRODUCT_PATTERN5_MATCHER.reset(versionString).matches()) {
			productNumber = "";
			rNumber = PRODUCT_PATTERN5_MATCHER.group(1);
		} else {
			reportDefaultSemanticError(location);
			return null;
		}

		boolean isNumber = false;
		String revisionDigit = null;
		String revisionLetter = null;
		String verificationStep = null;
		if (RNUMBER_PATTERN1_MATCHER.reset(rNumber).matches()) {
			revisionDigit = RNUMBER_PATTERN1_MATCHER.group(1);
			revisionLetter = RNUMBER_PATTERN1_MATCHER.group(2);
			verificationStep = RNUMBER_PATTERN1_MATCHER.group(3);
		} else if (RNUMBER_PATTERN2_MATCHER.reset(rNumber).matches()) {
			revisionDigit = RNUMBER_PATTERN2_MATCHER.group(1);
			revisionLetter = RNUMBER_PATTERN2_MATCHER.group(2);
		} else if (RNUMBER_PATTERN3_MATCHER.reset(rNumber).matches()) {
			productNumberSuffix = RNUMBER_PATTERN3_MATCHER.group(1);
			revisionDigit = RNUMBER_PATTERN3_MATCHER.group(2);
			revisionLetter = RNUMBER_PATTERN3_MATCHER.group(3);
			isNumber = true;
		} else {
			reportSemanticError(location, "Wrong format for version information: The accepted formats resemble R2D02 or R2D or 7.2.1");
			return null;
		}

		int majorVersion = 0;
		if (productNumberSuffix != null) {
			try {
				final BigInteger temp = new BigInteger(productNumberSuffix);
				if (temp.compareTo(BigInteger.valueOf(Integer.MAX_VALUE)) >= 0) {
					reportSemanticError(location, MessageFormat
							.format("The major version number {0} is unexpectedly large, right now we can not handle such large numbers",
									temp));
					return null;
				}
				majorVersion = temp.intValue();
			} catch (NumberFormatException e) {
				reportSemanticError(location, MessageFormat.format(
						"Wrong format for version information: the major version must be a number instead of {0}",
						productNumberSuffix));
				return null;
			}
		}

		int minorVersion = 0;
		try {
			final BigInteger temp = new BigInteger(revisionDigit);
			if (temp.compareTo(BigInteger.valueOf(Integer.MAX_VALUE)) >= 0) {
				reportSemanticError(location, MessageFormat
						.format("The minor version number {0} is unexpectedly large, right now we can not handle such large numbers",
								temp));
				return null;
			}
			minorVersion = temp.intValue();
		} catch (NumberFormatException e) {
			reportSemanticError(location, MessageFormat.format(
					"Wrong format for version information: the minor version must be a number instead of {0}",
					revisionDigit));
			return null;
		}

		int patchVersion = 0;
		if (isNumber) {
			final BigInteger temp = new BigInteger(revisionLetter);
			if (temp.compareTo(BigInteger.valueOf(Integer.MAX_VALUE)) >= 0) {
				reportSemanticError(location, MessageFormat
						.format("The patch version number {0} is unexpectedly large, right now we can not handle such large numbers",
								temp));
				return null;
			}
			patchVersion = temp.intValue();
		} else {
			if (revisionLetter.length() > 2) {
				reportSemanticError(location, MessageFormat
						.format("More than one letter is not allowed as patch version: {0}", revisionLetter));
				return null;
			}
			final char c = revisionLetter.charAt(0);
			patchVersion = revisionLetter.codePointAt(0) - "A".codePointAt(0);
			switch (c) {
			case 'I':
			case 'O':
			case 'P':
			case 'Q':
			case 'R':
			case 'W':
				reportSemanticError(location, MessageFormat.format("Letter {0} is not allowed as patch version", c));
			default:
				break;
			}
		}

		int buildVersion = 0;
		if (verificationStep != null) {
			try {
				final BigInteger temp = new BigInteger(verificationStep);
				if (temp.compareTo(BigInteger.valueOf(Integer.MAX_VALUE)) >= 0) {
					reportSemanticError(location, MessageFormat
							.format("The build version number {0} is unexpectedly large, right now we can not handle such large numbers",
									temp));
					return null;
				}
				buildVersion = temp.intValue();
			} catch (NumberFormatException e) {
				reportSemanticError(location, MessageFormat.format(
						"Wrong format for version information: the build version must be a number instead of {0}",
						verificationStep));
				return null;
			}
		}

		if ("CNL 113 300".equals(productNumber) && majorVersion == 0) {
			// in previous TITAN versions this information was not stored.
			majorVersion = 1;
		}

		return ProductIdentity.getProductIdentity(productNumber, majorVersion, minorVersion, patchVersion, buildVersion);
	}

	/**
	 * Reports the default semantic error to the specified location
	 * @param location the location of error, where it is reported
	 */
	private static void reportDefaultSemanticError(final Location location) {
		reportSemanticError(location, DEFAULT_SEMANTIC_ERROR_MESSAGE);
	}

	/**
	 * Reports a semantic error to the specified location containing the supplied reason of error.
	 * <p>
	 * As this method does not check the existence of an error marker at this location, with this message this is very fast.
	 *
	 * @param location the location of error, where it is reported
	 * @param reason the reason for the error.
	 * */
	private static void reportSemanticError(final Location location, final String reason) {
		if (location == null)
			return;
		location.reportSemanticError(reason);
	}
}

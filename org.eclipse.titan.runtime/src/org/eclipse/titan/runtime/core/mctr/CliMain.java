/******************************************************************************
 * Copyright (c) 2000-2020 Ericsson Telecom AB
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/org/documents/epl-2.0/EPL-2.0.html
 ******************************************************************************/
package org.eclipse.titan.runtime.core.mctr;

public class CliMain {

	public static void main(String[] args) {
		Cli userInterface = new Cli();
		int max_ptcs = -1;

		final MainController mainController = new MainController();
		userInterface.setMainController(mainController);
		mainController.initialize(userInterface, max_ptcs);

		userInterface.enterLoop(args);
		mainController.terminate();
	}

}

package org.eclipse.titan.runtime.core.mctr;

import org.eclipse.equinox.app.IApplication;
import org.eclipse.equinox.app.IApplicationContext;

import java.util.Iterator;
import java.util.Map;

public class MctrCliHeadlessRunner implements IApplication {
	
	private MainController mainController;

	@Override
	public Object start(IApplicationContext context) throws Exception {
		Map<Object, Object> arguments = context.getArguments();
		String[] args = (String[]) arguments.get("application.args");
		final Cli userInterface = new Cli();
		final int max_ptcs = -1;

		mainController = new MainController();
		userInterface.setMainController(mainController);
		mainController.initialize(userInterface, max_ptcs);

		userInterface.enterLoop(args);
		
		return null;
	}

	@Override
	public void stop() {
		mainController.terminate();
	}

}

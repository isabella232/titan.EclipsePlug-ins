package org.eclipse.titan.regressiontests.designer.statictests.cfgFile;

import org.eclipse.titan.regressiontests.designer.statictests.cfgFile.define.define;
import org.eclipse.titan.regressiontests.designer.statictests.cfgFile.module_parameters.module_parameters;
import org.eclipse.titan.regressiontests.designer.statictests.cfgFile.ordered_include.ordered_include;
import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

@RunWith(Suite.class)
@SuiteClasses({ 
	define.class,
	module_parameters.class,
	ordered_include.class
})
public class cfgFile {

}

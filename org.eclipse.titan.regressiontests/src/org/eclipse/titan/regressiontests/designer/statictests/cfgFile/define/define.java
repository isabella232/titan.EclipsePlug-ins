package org.eclipse.titan.regressiontests.designer.statictests.cfgFile.define;

import org.eclipse.titan.regressiontests.designer.statictests.cfgFile.define.macro_reference.macro_reference;
import org.eclipse.titan.regressiontests.designer.statictests.cfgFile.define.structured.structured;
import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

@RunWith(Suite.class)
@SuiteClasses({ 
	structured.class,
	macro_reference.class
})
public class define {

}

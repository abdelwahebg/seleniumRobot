/**
 * Orignal work: Copyright 2015 www.seleniumtests.com
 * Modified work: Copyright 2016 www.infotel.com
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * 	http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.seleniumtests.it.stubclasses;

import org.apache.log4j.Logger;

import com.seleniumtests.core.runner.SeleniumTestPlan;
import com.seleniumtests.util.logging.SeleniumRobotLogger;

public class StubParentClass extends SeleniumTestPlan {
	
	protected static final Logger logger = SeleniumRobotLogger.getLogger(StubParentClass.class);
//
//	/**
//	 * Generate context to have logger correctly initialized
//	 * @param testContext
//	 */
//	@BeforeSuite(groups="stub")
//	public void initSuite(final ITestContext testContext) {
//
//        SeleniumTestsContextManager.initGlobalContext(testContext);
//        SeleniumTestsContextManager.initThreadContext(testContext, null);
//
//		SeleniumTestsContextManager.getThreadContext().setSoftAssertEnabled(false);
//		SeleniumTestsContextManager.getGlobalContext().setSoftAssertEnabled(false);
//        SeleniumRobotLogger.updateLogger(SeleniumTestsContextManager.getGlobalContext().getOutputDirectory(), SeleniumTestsContextManager.getGlobalContext().getDefaultOutputDirectory());
//	}
}

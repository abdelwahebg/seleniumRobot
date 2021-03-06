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
package com.seleniumtests.browserfactory;

import org.openqa.selenium.remote.DesiredCapabilities;

import com.seleniumtests.core.SeleniumTestsContextManager;
import com.seleniumtests.driver.DriverConfig;

public class SauceLabsCapabilitiesFactory extends ICapabilitiesFactory {
	
    public SauceLabsCapabilitiesFactory(DriverConfig webDriverConfig) {
		super(webDriverConfig);
	}

	@Override
    public DesiredCapabilities createCapabilities() {

        final DesiredCapabilities capabilities = new DesiredCapabilities();

        capabilities.setCapability("browserName", webDriverConfig.getBrowser());
        capabilities.setCapability("platform", webDriverConfig.getPlatform());
        capabilities.setCapability("version", webDriverConfig.getVersion());
        capabilities.setCapability("name", SeleniumTestsContextManager.getThreadContext().getTestMethodSignature());

        return capabilities;
    }
}

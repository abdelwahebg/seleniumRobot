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

import org.openqa.selenium.WebDriver;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.firefox.FirefoxOptions;

import com.seleniumtests.driver.DriverConfig;

public class FirefoxDriverFactory extends AbstractWebDriverFactory implements IWebDriverFactory {

    private static boolean marionetteMode = true;
    
    /**
     * @param  cfg  the configuration of the firefoxDriver
     */
    public FirefoxDriverFactory(final DriverConfig cfg) {
        super(cfg);
    }   

    /**
     * create native driver instance, designed for unit testing.
     *
     * @return	the driver
     */
    @Override
    protected WebDriver createNativeDriver() {
    	if (driverOptions.is(FirefoxDriver.MARIONETTE)) {
    		setMarionetteMode(true);
    	} else {
    		setMarionetteMode(false);
    	}
		return new FirefoxDriver((FirefoxOptions)driverOptions);
    }
    
	@Override
	protected ICapabilitiesFactory getCapabilitiesFactory() {
		return new FirefoxCapabilitiesFactory(webDriverConfig);
	}
//
//    @Override
//    public WebDriver createWebDriver() {
//        DriverConfig cfg = this.getWebDriverConfig();
//        driver = createWebDriverWithTimeout();
//
//        // Implicit Waits to handle dynamic element. The default value is 5
//        // seconds.
//        setImplicitWaitTimeout(cfg.getImplicitWaitTimeout());
//        if (cfg.getPageLoadTimeout() >= 0) {
//            setPageLoadTimeout(cfg.getPageLoadTimeout());
//        }
//
//        this.setWebDriver(driver);
//        return driver;
//    }
//
//    /**
//     * Create webDriver, capture socket customexception and retry with timeout.
//     *
//     * @return  WebDriver
//     */
//    protected WebDriver createWebDriverWithTimeout() {
//        long time = 0;
//        while (time < getTimeout()) {
//            try {
//                driver = createNativeDriver();
//                return driver;
//            } catch (WebDriverException ex) {
//                if (ex.getMessage().contains("SocketException")
//                        || ex.getMessage().contains("Failed to connect to binary FirefoxBinary")
//                        || ex.getMessage().contains("Unable to bind to locking port 7054 within 45000 ms")) {
//                    WaitHelper.waitForSeconds(1);
//
//                    time++;
//                } else {
//                    throw new DriverExceptions(ex.getMessage());
//                }
//            }
//        }
//
//        throw new DriverExceptions("Got customexception when creating webDriver with socket timeout 1 minute");
//    }

	public static boolean isMarionetteMode() {
		return marionetteMode;
	}

	private static void setMarionetteMode(boolean marionetteMode) {
		FirefoxDriverFactory.marionetteMode = marionetteMode;
	}
}

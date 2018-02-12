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
package com.seleniumtests.reporter.logger;

import org.json.JSONObject;


/**
 * This is an action made inside the test: click on an element, ...
 * @author behe
 *
 */
public class TestAction {
	protected String name;
	protected Boolean failed;
	protected Throwable actionException;
	
	public TestAction(String name, Boolean failed) {
		this.name = name;
		this.failed = failed;
	}

	public String getName() {
		return name;
	}

	public Boolean getFailed() {
		return failed;
	}

	public void setFailed(Boolean failed) {
		this.failed = failed;
	}
	
	public Throwable getActionException() {
		return actionException;
	}

	public void setActionException(Throwable actionException) {
		this.actionException = actionException;
	}
	
	@Override
	public String toString() {
		return name;
	}
	
	public JSONObject toJson() {
		JSONObject actionJson = new JSONObject();
		
		actionJson.put("type", "action");
		actionJson.put("name", name);
		actionJson.put("exception", actionException == null ? null: actionException.toString());
		actionJson.put("failed", failed);
		
		return actionJson;
	}

}
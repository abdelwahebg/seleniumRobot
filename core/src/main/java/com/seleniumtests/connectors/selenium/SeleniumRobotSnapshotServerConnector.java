package com.seleniumtests.connectors.selenium;

import java.io.File;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import org.json.JSONException;
import org.json.JSONObject;

import com.mashape.unirest.http.Unirest;
import com.mashape.unirest.http.exceptions.UnirestException;
import com.mashape.unirest.request.body.MultipartBody;
import com.seleniumtests.core.SeleniumTestsContextManager;
import com.seleniumtests.customexception.ConfigurationException;
import com.seleniumtests.customexception.SeleniumRobotServerException;
import com.seleniumtests.driver.BrowserType;

public class SeleniumRobotSnapshotServerConnector extends SeleniumRobotServerConnector {
	
	public static final String SESSION_API_URL = "/snapshot/api/session/";
	public static final String TESTCASEINSESSION_API_URL = "/snapshot/api/testcaseinsession/";
	public static final String TESTSTEP_API_URL = "/snapshot/api/teststep/";
	public static final String STEPRESULT_API_URL = "/snapshot/api/stepresult/";
	public static final String SNAPSHOT_API_URL = "/snapshot/upload/image";
	private Integer sessionId;
	private String sessionUUID;
	private Integer testCaseInSessionId;
	private Integer testStepId;
	private Integer stepResultId;
	private Integer snapshotId;

	public SeleniumRobotSnapshotServerConnector(final boolean useRequested, final String url) {
		super(useRequested, url);
		if (!active) {
			return;
		}
		active = isAlive();
		if (active) {
			getInfoFromServer(null);
		}
	}
	
	@Override
	public boolean isAlive() {
		return isAlive("/snapshot/");
	}
	
	public void createSession() {
		if (!active) {
			return;
		}
		if (environmentId == null) {
			createEnvironment();
		}
		if (versionId == null) {
			createVersion();
		}
		try {
			BrowserType browser = SeleniumTestsContextManager.getThreadContext().getBrowser();
			browser = browser == null ? BrowserType.NONE : browser;
			sessionUUID = UUID.randomUUID().toString();
			
			JSONObject sessionJson = getJSonResponse(Unirest.post(url + SESSION_API_URL)
					.field("sessionId", sessionUUID)
					.field("date", LocalDate.now().format(DateTimeFormatter.ISO_DATE))
					.field("browser", browser.getBrowserType())
					.field("environment", SeleniumTestsContextManager.getThreadContext().getTestEnv())
					.field("version", versionId)
					.field("compareSnapshot", SeleniumTestsContextManager.getThreadContext().getSeleniumRobotServerCompareSnapshot()));
			sessionId = sessionJson.getInt("id");
		} catch (UnirestException | JSONException e) {
			throw new SeleniumRobotServerException("cannot create session", e);
		}
	}
	
	/**
	 * Create link between test case and session
	 */
	public void createTestCaseInSession() {
		if (!active) {
			return;
		}
		if (sessionId == null) {
			createSession();
		}
		if (testCaseId == null) {
			throw new ConfigurationException("Test case must be previously defined");
		}
		try {
			JSONObject testInSessionJson = getJSonResponse(Unirest.post(url + TESTCASEINSESSION_API_URL)
					.field("testCase", testCaseId)
					.field("session", sessionId));
			testCaseInSessionId = testInSessionJson.getInt("id");
		} catch (UnirestException | JSONException e) {
			throw new SeleniumRobotServerException("cannot create test case", e);
		}
	}
	

	/**
	 * Create test step and add it to the current test case
	 */
	public void createTestStep(String testStep) {
		if (!active) {
			return;
		}
		try {
			JSONObject stepJson = getJSonResponse(Unirest.post(url + TESTSTEP_API_URL)
					.field("name", testStep));
			testStepId = stepJson.getInt("id");
			addCurrentTestStepToTestCase();
		} catch (UnirestException | JSONException e) {
			throw new SeleniumRobotServerException("cannot create test step", e);
		}
	}
	
	/**
	 * Create snapshot
	 */
	public void createSnapshot(File pictureFile) {
		if (!active) {
			return;
		}
		if (sessionId == null) {
			createSession();
		}
		if (testCaseInSessionId == null) {
			createTestCaseInSession();
		}
		if (stepResultId == null) {
			throw new ConfigurationException("Step result must be previously recorded");
		}
		try {
			snapshotId = null;
			getJSonResponse(Unirest.post(url + SNAPSHOT_API_URL)
					.field("stepResult", stepResultId)
					.field("sessionId", sessionUUID)
					.field("testCase", testCaseInSessionId)
					.field("image", pictureFile)
					);
			snapshotId = 0; // for test only
		} catch (UnirestException | JSONException e) {
			throw new SeleniumRobotServerException("cannot create test snapshot", e);
		}
	}
	
	/**
	 * Record step result
	 * @param result	step result (true of false)
	 * @param logs		step details
	 * @param duration	step duration in milliseconds
	 */
	public void recordStepResult(Boolean result, String logs, long duration) {
		if (!active) {
			return;
		}
		if (sessionId == null) {
			createSession();
		}
		if (testCaseInSessionId == null) {
			createTestCaseInSession();
		}
		if (testStepId == null) {
			throw new ConfigurationException("Test step and test case in session must be previously defined");
		}
		try {
			stepResultId = null;
			JSONObject resultJson = getJSonResponse(Unirest.post(url + STEPRESULT_API_URL)
					.field("step", testStepId)
					.field("testCase", testCaseInSessionId)
					.field("result", result)
					.field("duration", duration)
					.field("stacktrace", logs)
					);
			stepResultId = resultJson.getInt("id");
		} catch (UnirestException | JSONException e) {
			throw new SeleniumRobotServerException("cannot create test snapshot", e);
		}
	}

	/**
	 * Returns list of test steps in a test case
	 * @return
	 */
	public List<String> getStepListFromTestCase() {
		if (testCaseInSessionId == null) {
			return new ArrayList<>();
		}
		
		try {

			JSONObject sessionJson = getJSonResponse(Unirest.get(url + TESTCASEINSESSION_API_URL + testCaseInSessionId));
			return sessionJson.getJSONArray("testSteps")
					.toList()
					.stream()
					.map(Object::toString)
					.collect(Collectors.toList());

		} catch (UnirestException | JSONException e) {
			throw new SeleniumRobotServerException("cannot get test step list", e);
		}
	}
	
	/**
	 * Add the current test case (should have been previously created) to this test session
	 */
	public void addCurrentTestStepToTestCase() {
		if (testStepId == null || testCaseInSessionId == null) {
			throw new ConfigurationException("Test step and Test case in session must be previously created");
		}
		
		try {
			// get list of tests associated to this session
			List<String> testSteps = getStepListFromTestCase();
			if (!testSteps.contains(testStepId.toString())) {
				testSteps.add(testStepId.toString());
			}
			addTestStepsToTestCases(testSteps);
			
		} catch (UnirestException | JSONException e) {
			throw new SeleniumRobotServerException("cannot add test step to test case", e);
		}
	}
	
	public JSONObject addTestStepsToTestCases(List<String> testSteps) throws UnirestException {
		if (testSteps.isEmpty()) {
			return new JSONObject();
		}
		
		MultipartBody request = Unirest.patch(url + TESTCASEINSESSION_API_URL + testCaseInSessionId + "/").field("testSteps", testSteps.get(0));
		for (String tc: testSteps.subList(1, testSteps.size())) {
			request = request.field("testSteps", tc);
		}
		return getJSonResponse(request);
	}
	
	public void addLogsToTestCaseInSession(String logs) {
		if (testCaseInSessionId == null) {
			createTestCaseInSession();
		}
		
		try {
			getJSonResponse(Unirest.patch(url + TESTCASEINSESSION_API_URL + testCaseInSessionId + "/").field("stacktrace", logs));
		} catch (UnirestException e) {
			throw new SeleniumRobotServerException("cannot add logs to test case", e);
		}
	}
	public Integer getSessionId() {
		return sessionId;
	}

	public Integer getTestStepId() {
		return testStepId;
	}

	public Integer getSnapshotId() {
		return snapshotId;
	}

	public String getSessionUUID() {
		return sessionUUID;
	}

	public Integer getTestCaseInSessionId() {
		return testCaseInSessionId;
	}

	public Integer getStepResultId() {
		return stepResultId;
	}
}

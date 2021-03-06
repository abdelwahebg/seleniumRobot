package com.seleniumtests.reporter.logger;

import java.util.ArrayList;

import org.json.JSONObject;

/**
 * Class for storing values that can be reused in reports
 * For example store an amount during the test so that it can be compared by an external tool
 * @author s047432
 *
 */
public class TestValue extends TestAction {
	
	private String message;
	private String value;

	public TestValue(String id, String humanReadableMessage, String value) {
		super(id, false, new ArrayList<>());
		this.message = humanReadableMessage;
		this.value = value;
	}

	public String getMessage() {
		return message;
	}

	public String getValue() {
		return value;
	}
	
	public String format() {
		return String.format("%s %s", message, value);
	}

	@Override
	public JSONObject toJson() {
		JSONObject actionJson = new JSONObject();
		
		actionJson.put("type", "value");
		actionJson.put("message", encodeString(message, "json"));
		actionJson.put("id", encodeString(name, "json"));
		actionJson.put("value", encodeString(value, "json"));
		
		return actionJson;
	}
	
	@Override
	public TestValue encode(String format) {
		TestValue val =  new TestValue(encodeString(name, format), 
				encodeString(message, format),
				encodeString(value, format));
		val.encoded = true;
		return val;
	}
}

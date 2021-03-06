package com.seleniumtests.connectors.selenium;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.NotImplementedException;
import org.apache.http.HttpHost;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.entity.InputStreamEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.openqa.selenium.Capabilities;
import org.openqa.selenium.remote.DesiredCapabilities;

import com.google.common.net.HttpHeaders;
import com.google.common.net.MediaType;
import com.mashape.unirest.http.Unirest;
import com.mashape.unirest.http.exceptions.UnirestException;
import com.seleniumtests.customexception.ScenarioException;
import com.seleniumtests.customexception.SeleniumGridException;
import com.seleniumtests.util.FileUtility;

import io.appium.java_client.remote.MobileCapabilityType;

public class SeleniumRobotGridConnector extends SeleniumGridConnector {

	public static final String NODE_TASK_SERVLET = "/extra/NodeTaskServlet";
	
	public SeleniumRobotGridConnector(String url) {
		super(url);
	}
	
	/**
	 * In case an app is required on the node running the test, upload it to the grid hub
	 * This will then be made available through HTTP GET URL to the node (appium will receive an url instead of a file)
	 * 
	 */
	@Override
	public void uploadMobileApp(Capabilities caps) {
		
		String appPath = (String)caps.getCapability(MobileCapabilityType.APP);
		
		// check whether app is given and app path is a local file
		if (appPath != null && new File(appPath).isFile()) {
			
			try (CloseableHttpClient client = HttpClients.createDefault();) {
				// zip file
				List<File> appFiles = new ArrayList<>();
				appFiles.add(new File(appPath));
				File zipFile = FileUtility.createZipArchiveFromFiles(appFiles);
				
				HttpHost serverHost = new HttpHost(hubUrl.getHost(), hubUrl.getPort());
				URIBuilder builder = new URIBuilder();
	        	builder.setPath("/grid/admin/FileServlet/");
	        	builder.addParameter("output", "app");
	        	HttpPost httpPost = new HttpPost(builder.build());
		        httpPost.setHeader(HttpHeaders.CONTENT_TYPE, MediaType.OCTET_STREAM.toString());
		        FileInputStream fileInputStream = new FileInputStream(zipFile);
	            InputStreamEntity entity = new InputStreamEntity(fileInputStream);
	            httpPost.setEntity(entity);
		        
		        CloseableHttpResponse response = client.execute(serverHost, httpPost);
		        if (response.getStatusLine().getStatusCode() != 200) {
		        	throw new SeleniumGridException("could not upload application file: " + response.getStatusLine().getReasonPhrase());
		        } else {
		        	// set path to the mobile application as an URL on the grid hub
		        	((DesiredCapabilities)caps).setCapability(MobileCapabilityType.APP, IOUtils.toString(response.getEntity().getContent()) + "/" + appFiles.get(0).getName());
		        }
		        
			} catch (IOException | URISyntaxException e) {
				throw new SeleniumGridException("could not upload application file", e);
			}
		}
	}
	
	/**
	 * Upload a file given file path
	 * @param filePath
	 */
	public void uploadFile(String filePath) {
		try (CloseableHttpClient client = HttpClients.createDefault();) {
			// zip file
			List<File> appFiles = new ArrayList<>();
			appFiles.add(new File(filePath));
			File zipFile = FileUtility.createZipArchiveFromFiles(appFiles);
			
			HttpHost serverHost = new HttpHost(hubUrl.getHost(), hubUrl.getPort());
			URIBuilder builder = new URIBuilder();
        	builder.setPath("/grid/admin/FileServlet/");
        	builder.addParameter("output", "app");
        	HttpPost httpPost = new HttpPost(builder.build());
	        httpPost.setHeader(HttpHeaders.CONTENT_TYPE, MediaType.OCTET_STREAM.toString());
	        FileInputStream fileInputStream = new FileInputStream(zipFile);
            InputStreamEntity entity = new InputStreamEntity(fileInputStream);
            httpPost.setEntity(entity);
	        
	        CloseableHttpResponse response = client.execute(serverHost, httpPost);
	        if (response.getStatusLine().getStatusCode() != 200) {
	        	throw new SeleniumGridException("could not upload file: " + response.getStatusLine().getReasonPhrase());
	        } else {
	        	// TODO call remote API
	        	throw new NotImplementedException("call remote Robot to really upload file");
	        }
	        
		} catch (IOException | URISyntaxException e) {
			throw new SeleniumGridException("could not upload file", e);
		}
	}
	
	/**
	 * Take screenshot of the full desktop
	 * @return
	 */
	public BufferedImage captureDesktopToBuffer() {
		// TODO: call remote API to do capture and get content
		throw new NotImplementedException("call remote Robot to really upload file");
	}
	

	/**
	 * Kill process
	 * @param processName
	 */
	public void killProcess(String processName) {
		if (nodeUrl == null) {
			throw new ScenarioException("You cannot kill a remote process before driver has been created and corresponding node instanciated");
		}
		
		logger.info("killing process: " + processName);
		try {
			Unirest.post(String.format("%s%s", nodeUrl, NODE_TASK_SERVLET))
				.queryString("action", "kill")
				.queryString("process", processName).asString();
		} catch (UnirestException e) {
			logger.warn(String.format("Could not kill process %s: %s", processName, e.getMessage()));
		}
	}

}

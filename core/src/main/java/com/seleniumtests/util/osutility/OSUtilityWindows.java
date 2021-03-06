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
package com.seleniumtests.util.osutility;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.seleniumtests.browserfactory.BrowserInfo;
import com.seleniumtests.customexception.ConfigurationException;
import com.seleniumtests.customexception.DisabledConnector;
import com.seleniumtests.driver.BrowserType;
import com.sun.jna.platform.win32.Advapi32Util;
import com.sun.jna.platform.win32.Win32Exception;
import com.sun.jna.platform.win32.WinReg;

public class OSUtilityWindows extends OSUtility {
	
	Pattern versionPattern = Pattern.compile(".*?(\\d+\\.\\d+\\.\\d+).*?");
		
	@Override
	public int getIEVersion() {

        String output = OSCommand.executeCommandAndWait("reg query \"HKLM\\Software\\Microsoft\\Internet Explorer\" /v svcVersion");
        if (output.split("\n").length < 3) {
            output = OSCommand.executeCommandAndWait("reg query \"HKLM\\Software\\Microsoft\\Internet Explorer\" /v Version");
        }

        String internetExplorerValue = output.split("\n")[2];
        String version = internetExplorerValue.trim().split("   ")[2];
        version = version.trim().split("\\.")[0];
        return Integer.parseInt(version);
    }

	/**
     * @param fast : true gets only Image Name and pId of the process
     * @return list of ProcessInfo
     */
	@Override
    public List<ProcessInfo> getRunningProcessList(){
    	/*
    	 * Output command : Image name ;  PID ;  Session name ;  Session# ;  Mem Usage .
    	 * In Windows 7, windir = C:\Windows\ 
    	 * /NH will not display column headers
    	 * and /V displays also : Status ;  Username ;  CPU time ;  Windows title .
    	 * or /SVC displays only : Image name ;  PID ;  Services .
    	 */
    	String command = System.getenv("windir") + "\\system32\\" + "tasklist.exe /NH /SVC";
    	List<String> strProcessList = Arrays.asList(OSCommand.executeCommandAndWait(command).split("\n"));
    	
    	List<ProcessInfo> processInfoList = new ArrayList<>();
    	for (String sentence : strProcessList) {
    		sentence = sentence.toLowerCase();
    		
    		ProcessInfo processInfo = new ProcessInfo();
    		
			Pattern pTasklist = Pattern.compile("(system|.*\\.exe)\\s+(\\d+).*");
			Matcher m = pTasklist.matcher(sentence);
			
			if (m.matches()) {
				processInfo.setName(m.group(1).replaceAll(".exe", ""));
				processInfo.setPid(m.group(2));
				
				processInfoList.add(processInfo);
			}
    	}
    	return processInfoList;
    }
    
    /**
     * Terminate process from command line terminal.
     * @param process
     * @param force to kill the process
     * @return
     * @throws IOException
     */
    @Override
    public String killProcess(String pid, boolean force) {

    	if (force) {
    		return OSCommand.executeCommandAndWait("taskkill /F /PID " + pid);
    	} else {
    		return OSCommand.executeCommandAndWait("taskkill /PID " + pid);
    	}
    	
    }
    
    /**
     * Kill process by name, extension must not be given as it's added automatically
     * @param 	programName	name of the process without extension
     * @param	force		do we force program exit
     */
	@Override
	public String killProcessByName(String programName, boolean force) {
		if (force) {
    		return OSCommand.executeCommandAndWait("taskkill /F /IM " + programName + getProgramExtension());
    	} else {
    		return OSCommand.executeCommandAndWait("taskkill /IM " + programName + getProgramExtension());
    	}
	}

	@Override
	public String getProgramExtension() {
		return ".exe";
	}

	@Override
	public String getOSBuild() {
		String version = OSCommand.executeCommandAndWait("cmd /C ver").replace("\r", "").replace("\n", "").trim();
		Matcher versionMatcher = versionPattern.matcher(version);
		if (versionMatcher.matches()) {
			return versionMatcher.group(1);
		} else {
			logger.error("could not get Windows version");
			return "5000";
		}
	}
	
	private String getChromeVersionFromRegistry() {
		try {
			return Advapi32Util.registryGetStringValue(WinReg.HKEY_LOCAL_MACHINE, "Software\\Wow6432Node\\Microsoft\\Windows\\CurrentVersion\\Uninstall\\Google Chrome", "version");
		} catch (Win32Exception e) {
			return Advapi32Util.registryGetStringValue(WinReg.HKEY_LOCAL_MACHINE, "Software\\Google\\Chrome\\BLBeacon", "version");
		}
	}
	
	/**
	 * Search for a folder with version name where chrome.exe is located (e.g: 58.0.3029.81)
	 * @param chromePath
	 * @return
	 */
	private String getChromeVersionFromFolder(String chromePath) {
		if (!new File(chromePath).exists()) {
			throw new ConfigurationException("Chrome version could not be get from folder, chrome path does not exist");
		}
		for (File file: new File(chromePath.replace("chrome.exe", "")).listFiles()) {
			if (file.isDirectory() && file.getName().matches("^\\d+.*")) {
				return file.getName();
			}
		}
		throw new ConfigurationException("Chrome version could not be get from folder");
	}
	
	private String getIeVersionFromRegistry() {
		try {
			return Advapi32Util.registryGetStringValue(WinReg.HKEY_LOCAL_MACHINE, "Software\\Microsoft\\Internet Explorer", "svcVersion");
		} catch (Win32Exception e) {
			return Advapi32Util.registryGetStringValue(WinReg.HKEY_LOCAL_MACHINE, "Software\\Microsoft\\Internet Explorer", "version");
		}
	}
	
	private List<String> searchFirefoxVersions() {
		List<String> firefoxInstallations = new ArrayList<>();
		String out = OSCommand.executeCommandAndWait(new String[] {"REG", "QUERY", "HKCR",  "/f", "FirefoxHTML", "/k", "/c"});
		for (String line: out.split("\n")) {
			if (line.startsWith("HKEY_CLASSES_ROOT")) {
				String keyPath = line.replace("HKEY_CLASSES_ROOT\\", "").trim();
				try {
					String firefoxPath = Advapi32Util.registryGetStringValue(WinReg.HKEY_CLASSES_ROOT, keyPath + "\\shell\\open\\command", "");
					firefoxPath = firefoxPath.split(".exe\"")[0].replace("\"", "") + ".exe";
					firefoxInstallations.add(firefoxPath);
				} catch (Win32Exception e) {}
			}
		}
		return firefoxInstallations;
	}

	@Override
	public Map<BrowserType, List<BrowserInfo>> discoverInstalledBrowsersWithVersion() {
			
		Map<BrowserType, List<BrowserInfo>> browserList = new EnumMap<>(BrowserType.class);
		
		browserList.put(BrowserType.HTMLUNIT, Arrays.asList(new BrowserInfo(BrowserType.HTMLUNIT, BrowserInfo.LATEST_VERSION, null)));
		browserList.put(BrowserType.PHANTOMJS, Arrays.asList(new BrowserInfo(BrowserType.PHANTOMJS, BrowserInfo.LATEST_VERSION, null)));
		
		// look for Firefox
		try {
			browserList.put(BrowserType.FIREFOX, new ArrayList<>());
			
			for (String firefoxPath: searchFirefoxVersions()) {
				String version = getFirefoxVersion(firefoxPath);
				try {
					browserList.get(BrowserType.FIREFOX).add(new BrowserInfo(BrowserType.FIREFOX, extractFirefoxVersion(version), firefoxPath));
				} catch (ConfigurationException e) {
					continue;
				}
			}
		} catch (IndexOutOfBoundsException e) {}
		
		
		// look for chrome
		// TODO: handle multiple chrome versions
		try {
			browserList.put(BrowserType.CHROME, new ArrayList<>());
			
			String chromePath = Advapi32Util.registryGetStringValue(WinReg.HKEY_LOCAL_MACHINE, "Software\\Classes\\ChromeHTML\\shell\\open\\command", "");
			chromePath = chromePath.split(".exe\"")[0].replace("\"", "") + ".exe";
			String version;
			try {
				version = getChromeVersionFromRegistry();
			} catch (Win32Exception e) {
				version = getChromeVersionFromFolder(chromePath);
			}
			browserList.get(BrowserType.CHROME).add(new BrowserInfo(BrowserType.CHROME, extractChromeVersion("Google Chrome " + version), chromePath));
		} catch (Win32Exception | ConfigurationException e) {}
		
		// look for ie
		try {
			Advapi32Util.registryGetStringValue(WinReg.HKEY_LOCAL_MACHINE, "Software\\Microsoft\\Windows\\CurrentVersion\\App Paths\\IEXPLORE.EXE", "");
			String version = getIeVersionFromRegistry();
			
			browserList.put(BrowserType.INTERNET_EXPLORER, Arrays.asList(new BrowserInfo(BrowserType.INTERNET_EXPLORER, extractIEVersion(version), null)));
		} catch (Win32Exception | ConfigurationException e) {}
		
		// look for edge
		try {
			String version = Advapi32Util.registryGetStringValue(WinReg.HKEY_CURRENT_USER, "Software\\Microsoft\\MicrosoftEdge\\Main", "EdgeSwitchingOSBuildNumber");
			browserList.put(BrowserType.EDGE, Arrays.asList(new BrowserInfo(BrowserType.EDGE, extractEdgeVersion(version), null)));
		} catch (Win32Exception | ConfigurationException e) {}
		
		return browserList;
	}
	
	public String getSoapUiPath() {
		try {
			String[] jvms = Advapi32Util.registryGetKeys(WinReg.HKEY_CURRENT_USER, "Software\\ej-technologies\\exe4j\\jvms");
			
			for (String jvm: jvms) {
				if (jvm.toLowerCase().contains("soapui")) {
					return jvm.split("/jre/bin")[0];
				}
			}
		} catch (Exception e) {		}
		throw new DisabledConnector("SOAP UI is not installed (not found in registry). Install it and run it once (MANDATORY)");
	
	}

	@Override
	public List<Long> getChildProcessPid(Long parentProcess, String processName, List<Long> existingPids) throws IOException {
		Scanner scan = new Scanner(Runtime.getRuntime().exec(String.format("wmic process where (ParentProcessId=%d) get Caption,ProcessId", parentProcess)).getInputStream());
        scan.useDelimiter("\\A");
        String childProcessIds =  scan.hasNext() ? scan.next() : "";
        List<Long> namedSubprocesses = new ArrayList<>();
        String[] splited = childProcessIds.split("\\s+");
        for(int i =0 ; i<splited.length; i = i+2){
        	Long pid;
        	try {
        		pid = Long.parseLong(splited[i+1]);
        	} catch (NumberFormatException e) {
        		continue;
        	}
            if((processName == null || processName.equalsIgnoreCase(splited[i])) && !existingPids.contains(pid)) {
            	namedSubprocesses.add(pid);
            }
        }
       
        scan.close();
        
        return namedSubprocesses;
	}

	@Override
	public String getProgramNameFromPid(Long pid) {
		String[] processNames = OSCommand.executeCommandAndWait(String.format("wmic process where processId=%d get name", pid)).trim().split("\n");
		String processName = processNames[processNames.length - 1];
		return processName.endsWith(".exe") ? processName: "";
	}
}

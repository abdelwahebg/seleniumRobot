/*
 * Copyright 2016 www.infotel.com
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.seleniumtests.util;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

public class OSUtilityUnix extends OSUtility {

	/**
     * Ask console for every running process.
     * @return list of output command lines
     */
    public static List<String> getURunningProcessList(){
    	String command = "ps -e";
	    return Arrays.asList(executeCommand(command).split("\n"));
    }
    
    /**
     * Terminate process from command line terminal.
     * @param process
     * @param force to kill the process
     * @return
     * @throws IOException
     */
    protected static String ukillProcess(String process, boolean force) throws IOException {
    
		String pId = executeCommand("pidof " + process).split("\n")[0];
    	
    	if (force) {
    		return executeCommand("kill -SIGKILL " + pId);
    	} else {
    		return executeCommand("kill -SIGTERM " + pId);
    	}
    }
}
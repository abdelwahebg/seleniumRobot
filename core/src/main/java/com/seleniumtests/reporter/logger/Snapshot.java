package com.seleniumtests.reporter.logger;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.Arrays;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.StringEscapeUtils;

import com.seleniumtests.core.SeleniumTestsContextManager;
import com.seleniumtests.driver.screenshots.ScreenShot;
import com.seleniumtests.reporter.reporters.CommonReporter;
import com.seleniumtests.util.FileUtility;
import com.seleniumtests.util.StringUtility;

public class Snapshot extends TestAction {
	
	private ScreenShot screenshot;

	public static final String SNAPSHOT_PATTERN = "Application Snapshot";
	public static final String OUTPUT_PATTERN = "Output: ";

	public Snapshot(final ScreenShot screenshot) {
		super(screenshot.getTitle(), false, new ArrayList<>());
		this.screenshot = screenshot;
	}
	
    /**
     * Log Screenshot method
     * Return: screenshot message with links
     *
     * @param  screenShot
     * 
     * @return String
     */
    public String buildScreenshotLog() {

        StringBuilder sbMessage = new StringBuilder("");
        sbMessage.append(OUTPUT_PATTERN + StringEscapeUtils.escapeHtml4(screenshot.getTitle()) + ": ");
        
        if (screenshot.getLocation() != null) {
            sbMessage.append("<a href='" + screenshot.getLocation() + "' target=url>Application URL</a>");
        }

        if (screenshot.getHtmlSourcePath() != null) {
            sbMessage.append(" | <a href='" + screenshot.getHtmlSourcePath()
                    + "' target=html>Application HTML Source</a>");
        }

        if (screenshot.getImagePath() != null) {
            sbMessage.append(" | <a href='" + screenshot.getImagePath()
                    + "' class='lightbox'>" + SNAPSHOT_PATTERN + "</a>");
        }

        return sbMessage.toString();
    }

    /**
     * Rename HTML and PNG files so that they do not present an uuid
     * New name is <test_name>_<step_idx>_<snapshot_idx>_<step_name>_<uuid>
     * @param testStep
     * @param stepIdx	   	number of this step
     * @param snapshotIdx	number of this snapshot for this step
     * @param userGivenName	name specified by user, rename to this name
     */
    public void rename(final TestStep testStep, final int stepIdx, final int snapshotIdx, final String userGivenName) {
    	String newBaseName;
    	if (userGivenName == null) {
	    	newBaseName = String.format("%s_%d-%d_%s-", 
	    			StringUtility.replaceOddCharsFromFileName(CommonReporter.getTestName(testStep.getTestResult())),
	    			stepIdx, 
	    			snapshotIdx,
	    			StringUtility.replaceOddCharsFromFileName(testStep.getName()));
    	} else {
    		newBaseName = StringUtility.replaceOddCharsFromFileName(userGivenName);
    	}
    	
    	
    	if (screenshot.getHtmlSourcePath() != null) {
    		String oldFullPath = screenshot.getFullHtmlPath();
    		String oldPath = screenshot.getHtmlSourcePath();
    		File oldFile = new File(oldPath);
    		String folderName = "";
    		if (oldFile.getParent() != null) {
    			folderName = oldFile.getParent().replace(File.separator, "/") + "/";
    		}
    		
    		String newName = newBaseName + FilenameUtils.getBaseName(oldFile.getName());
    		newName = newName.substring(0, Math.min(50, newName.length())) + "." +  FilenameUtils.getExtension(oldFile.getName());
    		
    		// if file cannot be moved, go back to old name
    		try {
    			oldFile = new File(oldFullPath);
    			if (SeleniumTestsContextManager.getGlobalContext().getOptimizeReports()) {
    				screenshot.setHtmlSourcePath(folderName + newName + ".zip");
    				oldFile = FileUtility.createZipArchiveFromFiles(Arrays.asList(oldFile));
    			} else {
    				screenshot.setHtmlSourcePath(folderName + newName);
    			}
    			
				FileUtils.copyFile(oldFile, new File(screenshot.getFullHtmlPath()));
				new File(oldFullPath).delete();
				
			} catch (IOException e) {
				screenshot.setHtmlSourcePath(oldPath);
			}
    	}
    	if (screenshot.getImagePath() != null) {
    		String oldFullPath = screenshot.getFullImagePath();
    		String oldPath = screenshot.getImagePath();
    		File oldFile = new File(oldPath);
    		String folderName = "";
    		if (oldFile.getParent() != null) {
    			folderName = oldFile.getParent().replace(File.separator, "/") + "/";
    		}
    		
    		String newName = newBaseName + FilenameUtils.getBaseName(oldFile.getName());
    		newName = newName.substring(0, Math.min(50, newName.length())) + "." + FilenameUtils.getExtension(oldFile.getName());
    		screenshot.setImagePath(folderName + newName);
    		
    		// if file cannot be moved, go back to old name
    		try {
    			Files.move(Paths.get(oldFullPath), Paths.get(screenshot.getFullImagePath()), StandardCopyOption.REPLACE_EXISTING);
			} catch (IOException e) {
				screenshot.setImagePath(oldPath);
			}
    	}
    }
    
	public ScreenShot getScreenshot() {
		return screenshot;
	}

}

/*******************************************************************************
 * Copyright (c) 2014 SAP AG or an SAP affiliate company. All rights reserved.
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License. 
 *******************************************************************************/

package org.eclipse.dirigible.runtime.logging;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.URL;
import java.text.SimpleDateFormat;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class LoggingServlet extends HttpServlet {
	private static final String B_CLOSE = "</b>";
	private static final String B = "<b>";
	private static final String A_HREF_JAVASCRIPT_LOCATION_RELOAD_TRUE_REFRESH_A = "<a href=\"javascript:location.reload(true);\">Refresh</a>";
	private static final String LOG_FILES_A_CLOSE = "\">Log Files</a>";
	private static final String SMALL_CLOSE = "</small>";
	private static final String SMALL = "<small>";
	private static final String BR = "<br/>";
	private static final String HR = "<hr>";
	private static final String TD_TR = "</td></tr>";
	private static final String TD_TD = "</td><td>";
	private static final String TR_TD = "<tr><td>";
	private static final String A_HREF_END = "</a>";
	private static final String A_HREF_CLOSE = "\">";
	private static final String A_HREF = "<a href=\"";
	private static final String TR_TD_B_LOG_FILES_B_TD_TD_B_LAST_MODIFIED_B_TD_TR = "<tr><td><b>Log Files</b></td><td><b>Last modified</b></td></tr>";
	private static final String TABLE_CLOSE = "</table>";
	private static final String TABLE_OPEN = "<table  border=\"0\">";
	private static final String FONT_CLOSE = "</font>";
	private static final String FONT_ARIAL_OPEN = "<font face=\"arial\">";
	private static final String BAD_INITIAL_CONFIGURATION_PLEASE_SET_PROPERLY_LOGGING_DIRECTORY = "Bad initial configuration. Please set properly logging directory.";
	private static final String MM_DD_YYYY_HH_MM_SS = "MM/dd/yyyy HH:mm:ss";
	private static final String CONTENT_TYPE_TEXT_HTML = "text/html";
	private static final String LOG_FILE_S_DOSN_T_EXIST = "Log file '%s' dosn't exist!";
	private static final String LOG_PARAMETER = "log";
	private static final String EMPTY_STRING = "";
	private static final String DOT = ".";
	private static final String LOGGING_SERVLET_CLASS_IS_NOT_STORED_IN_A_FILE = "LoggingServlet class is not stored in a file.";
	private static final String LOGGING_SERVLET_CLASS = "LoggingServlet.class";
	private static final String FILE = "file";
	private static final String INIT_LOGGING_DIRECTORY = "initLoggingDirectory";
	private static final String HTML_START = "<!DOCTYPE html><html><body>";
	private static final String HTML_END = "</body></html>";
	private static final String LOGGING_FILES_LIST_LOCATION = "logging";
	private static final String LOGGING_FILE_LOCATION = LOGGING_FILES_LIST_LOCATION + "?log=";
	private static final long serialVersionUID = 1L;

	protected void doGet(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		String initLoggingDirectory = getInitParameter(INIT_LOGGING_DIRECTORY);
		String serverFileSystemPath = getServerFileSystemPath();

		File loggingDirectory = new File(serverFileSystemPath + initLoggingDirectory);
		if (!loggingDirectory.exists()) {
			response.sendError(500, BAD_INITIAL_CONFIGURATION_PLEASE_SET_PROPERLY_LOGGING_DIRECTORY);
		} else {
			File[] loggingFiles = loggingDirectory.listFiles();
			String logFile = request.getParameter(LOG_PARAMETER);
			if (logFile != null && !logFile.equals(EMPTY_STRING)) {
				printLogFile(response, loggingFiles, logFile);
			} else {
				printLogFilesList(response, loggingDirectory);
			}
		}
	}

	private void printLogFilesList(HttpServletResponse response, File loggingDirectory)
			throws IOException {
		SimpleDateFormat dateFormat = new SimpleDateFormat(MM_DD_YYYY_HH_MM_SS);

		StringBuilder fileLinks = new StringBuilder(HTML_START);
		fileLinks.append(FONT_ARIAL_OPEN);
		fileLinks.append(TABLE_OPEN);
		fileLinks.append(TR_TD_B_LOG_FILES_B_TD_TD_B_LAST_MODIFIED_B_TD_TR);

		for (File loggingFile : loggingDirectory.listFiles()) {
			String fileName = XSSUtils.stripXSS(loggingFile.getName());
			String lastModified = XSSUtils.stripXSS(dateFormat.format(loggingFile.lastModified()));
			String a = A_HREF + LOGGING_FILE_LOCATION + fileName + A_HREF_CLOSE + fileName + A_HREF_END;
			fileLinks.append(TR_TD + a + TD_TD + lastModified + TD_TR);
		}

		fileLinks.append(TABLE_CLOSE);
		fileLinks.append(FONT_CLOSE);
		fileLinks.append(HTML_END);

		PrintWriter writer = response.getWriter();
		writer.println(fileLinks.toString());
		writer.flush();
		writer.close();
	}

	private void printLogFile(HttpServletResponse response, File[] loggingFiles, String logFile)
			throws FileNotFoundException, IOException {
		response.setContentType(CONTENT_TYPE_TEXT_HTML);
		PrintWriter writer = response.getWriter();

		boolean found = false;

		for (File loggingFile : loggingFiles) {
			if (loggingFile.getName().equalsIgnoreCase(logFile)) {
				BufferedReader reader = new BufferedReader(new FileReader(loggingFile));
				String line;
				writer.print(HTML_START);
				writer.print(FONT_ARIAL_OPEN);
				printBeforeLogFile(writer);
				while ((line = reader.readLine()) != null) {
					writer.print(BR);
					writer.println(SMALL + XSSUtils.stripXSS(line) + SMALL_CLOSE);
				}
				printAfterLogFile(writer);
				writer.print(FONT_CLOSE);
				writer.print(HTML_END);
				writer.flush();
				reader.close();
				found = true;
				break;
			}
		}
		if (!found) {
			writer.println(String.format(LOG_FILE_S_DOSN_T_EXIST, logFile));
			writer.flush();
		}
		writer.close();
	}

	private void printBeforeLogFile(PrintWriter writer) {
		printScript(writer);
		writer.print(HR);
	}

	private void printAfterLogFile(PrintWriter writer) {
		writer.print(HR);
		printScript(writer);
	}

	private void printScript(PrintWriter writer) {
		writer.print(TABLE_OPEN);
		String linkLogList = A_HREF + LOGGING_FILES_LIST_LOCATION + LOG_FILES_A_CLOSE;
		String linkRefresh = A_HREF_JAVASCRIPT_LOCATION_RELOAD_TRUE_REFRESH_A;
		writer.print(TR_TD + B + linkLogList + B_CLOSE + TD_TD + B + linkRefresh + B_CLOSE + TD_TR);
		writer.print(TABLE_CLOSE);
		writer.flush();
	}

	private String getServerFileSystemPath() {
		URL servletURL = LoggingServlet.class.getResource(LOGGING_SERVLET_CLASS);
		if (!FILE.equalsIgnoreCase(servletURL.getProtocol())) {
			throw new IllegalStateException(LOGGING_SERVLET_CLASS_IS_NOT_STORED_IN_A_FILE);
		}
		String path = servletURL.getPath();
		String serverFileSystemPath = path.substring(0, path.indexOf(DOT));
		return serverFileSystemPath;
	}
}

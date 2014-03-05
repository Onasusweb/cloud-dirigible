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

package com.sap.dirigible.runtime.metrics;

import java.util.Date;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import com.sap.dirigible.runtime.registry.PathUtils;

public class AccessLogRecord {
	
	private String requestUri;
	
	private String remoteUser;
	
	private String remoteHost;
	
	private String method;
	
	private String userAgent;
	
	private Date timestamp;
	
	private Date period;
	
	private int responseStatus;
	
	private int responseTime;
	
	private String sessionId;
	
	private String pattern;
	
	private String projectName;
	
	public AccessLogRecord(HttpServletRequest request, String pattern) {
		this.requestUri = request.getRequestURI();
		this.remoteUser = request.getRemoteUser();
		this.remoteHost = request.getRemoteHost();
		HttpSession session = request.getSession(true);
		this.sessionId = session.getId();
		this.method = request.getMethod();
		this.userAgent = request.getHeader("User-Agent");
		if (this.userAgent != null
				&& this.userAgent.length() > 32) {
			this.userAgent = this.userAgent.substring(0,31);	
		}
		this.timestamp = new Date();
		this.period = TimeUtils.roundCeilingHour(this.timestamp);
		this.pattern = pattern;
		String pathInfo = PathUtils.extractPath(request);
		this.projectName = extractProjectName(pathInfo);
	}

	
	private String extractProjectName(String pathInfo) {
		if (pathInfo != null
				&& pathInfo.length() > 2
				&& pathInfo.charAt(0) == '/'
				&& pathInfo.substring(1).indexOf("/") > 0) {
			String project = pathInfo.substring(1);
			project = project.substring(0,project.indexOf("/"));
		}
		return "none";
	}


	public AccessLogRecord(String requestUri, String remoteUser,
			String remoteHost, String sessionId, String method, String userAgent, 
			int responseStatus, Date timestamp, Date period, String pattern, String projectName, int responseTime) {
		this.requestUri = requestUri;
		this.remoteUser = remoteUser;
		this.remoteHost = remoteHost;
		this.method = method;
		this.userAgent = userAgent;
		this.timestamp = timestamp;
		this.responseStatus = responseStatus;
		this.responseTime = responseTime;
		this.sessionId = sessionId;
		this.pattern = pattern;
		this.projectName = projectName;
	}


	public String getRequestUri() {
		return requestUri;
	}

	public String getRemoteUser() {
		return remoteUser;
	}

	public String getRemoteHost() {
		return remoteHost;
	}

	public String getSessionId() {
		return sessionId;
	}

	public String getMethod() {
		return method;
	}

	public String getUserAgent() {
		return userAgent;
	}

	public Date getTimestamp() {
		return timestamp;
	}

	public int getResponseStatus() {
		return responseStatus;
	}
	
	public void setResponseStatus(int responseStatus) {
		this.responseStatus = responseStatus;
	}

	public int getResponseTime() {
		return responseTime;
	}
	
	public void setResponseTime(int responseTime) {
		this.responseTime = responseTime;
	}

	public String getPattern() {
		return pattern;
	}
	
	public void setPattern(String pattern) {
		this.pattern = pattern;
	}
	
	public String getProjectName() {
		return projectName;
	}
	
	public void setProjectName(String projectName) {
		this.projectName = projectName;
	}
	
	public Date getPeriod() {
		return period;
	}
	
	public void setPeriod(Date period) {
		this.period = period;
	}
}

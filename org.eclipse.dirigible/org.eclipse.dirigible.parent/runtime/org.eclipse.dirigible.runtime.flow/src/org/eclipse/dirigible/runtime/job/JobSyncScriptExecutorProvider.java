/******************************************************************************* 
 * Copyright (c) 2015 SAP and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0 
 * which accompanies this distribution, and is available at 
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   SAP - initial API and implementation
 *******************************************************************************/

package org.eclipse.dirigible.runtime.job;

import java.io.IOException;

import javax.servlet.http.HttpServletRequest;

import org.eclipse.dirigible.repository.api.ICommonConstants;
import org.eclipse.dirigible.runtime.scripting.IScriptExecutor;
import org.eclipse.dirigible.runtime.scripting.IScriptExecutorProvider;

public class JobSyncScriptExecutorProvider implements
		IScriptExecutorProvider {

	@Override
	public String getType() {
		return ICommonConstants.ENGINE_TYPE.JOB;
	}
	
	@Override
	public String getAlias() {
		return ICommonConstants.ENGINE_ALIAS.JOB;
	}

	@Override
	public IScriptExecutor createExecutor(HttpServletRequest request) throws IOException {
		JobSyncServlet jobServlet = new JobSyncServlet();
		JobSyncExecutor jobSyncExecutor = jobServlet.createExecutor(request);
		return jobSyncExecutor;
	}

}

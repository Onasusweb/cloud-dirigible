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

package org.eclipse.dirigible.runtime.js.debug;

import java.io.IOException;

import javax.servlet.http.HttpServletRequest;

import org.eclipse.dirigible.repository.ext.debug.IDebugProtocol;
import org.eclipse.dirigible.repository.logging.Logger;
import org.eclipse.dirigible.runtime.js.JavaScriptExecutor;
import org.eclipse.dirigible.runtime.js.JavaScriptServlet;

/**
 * Servlet for JavaScript scripts execution
 */
public class JavaScriptDebugServlet extends JavaScriptServlet {

	private static final long serialVersionUID = -9115022531455267478L;

	private static final Logger logger = Logger.getLogger(JavaScriptDebugServlet.class);

	@Override
	public JavaScriptExecutor createExecutor(HttpServletRequest request) throws IOException {

		logger.debug("entering JavaScriptDebugServlet.createExecutor()");

		IDebugProtocol debugProtocol = DebugProtocolUtils.lookupDebugProtocol();

		String rootPath = getScriptingRegistryPath(request);
		logger.debug("rootPath=" + rootPath);
		JavaScriptDebuggingExecutor executor = new JavaScriptDebuggingExecutor(
				getRepository(request), rootPath, REGISTRY_SCRIPTING_DEPLOY_PATH, debugProtocol);

		logger.debug("exiting JavaScriptDebugServlet.createExecutor()");

		return executor;
	}

}

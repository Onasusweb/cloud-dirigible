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

package org.eclipse.dirigible.repository.ext.debug;

import java.util.ArrayList;
import java.util.List;

public class DebugSessionsMetadata {

	private List<DebugSessionMetadata> debugSessionsMetadata = new ArrayList<DebugSessionMetadata>();
	
	public List<DebugSessionMetadata> getDebugSessionsMetadata() {
		return debugSessionsMetadata;
	}
	
	public void setDebugSessionsMetadata(
			List<DebugSessionMetadata> debugSessionsMetadata) {
		this.debugSessionsMetadata = debugSessionsMetadata;
	}
	
}

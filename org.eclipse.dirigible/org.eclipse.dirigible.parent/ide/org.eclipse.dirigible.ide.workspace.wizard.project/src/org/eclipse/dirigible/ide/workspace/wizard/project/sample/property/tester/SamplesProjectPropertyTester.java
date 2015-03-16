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

package org.eclipse.dirigible.ide.workspace.wizard.project.sample.property.tester;

import org.eclipse.core.expressions.PropertyTester;

import org.eclipse.dirigible.ide.common.CommonParameters;

public class SamplesProjectPropertyTester extends PropertyTester {

	@Override
	public boolean test(Object receiver, String property, Object[] args, Object expectedValue) {
		boolean testResult = false;
		try {
			testResult = CommonParameters.isUserInRole(CommonParameters.ROLE_DEVELOPER);
		} catch (UnsupportedOperationException e) {
			testResult = false;
		}
		return testResult;
	}

}

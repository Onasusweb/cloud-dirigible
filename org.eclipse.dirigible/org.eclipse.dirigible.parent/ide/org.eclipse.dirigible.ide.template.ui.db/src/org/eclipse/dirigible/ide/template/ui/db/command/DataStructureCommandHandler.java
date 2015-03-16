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

package org.eclipse.dirigible.ide.template.ui.db.command;

import org.eclipse.core.resources.IResource;
import org.eclipse.jface.wizard.Wizard;

import org.eclipse.dirigible.ide.template.ui.common.TemplateCommandHandler;
import org.eclipse.dirigible.ide.template.ui.db.wizard.DataStructureTemplateWizard;

public class DataStructureCommandHandler extends TemplateCommandHandler {

	@Override
	protected Wizard getWizard(IResource resource) {
		return new DataStructureTemplateWizard(resource);
	}

}

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

package org.eclipse.dirigible.ide.template.ui.js.wizard;

import org.eclipse.core.resources.IResource;
import org.eclipse.jface.wizard.IWizardPage;

import org.eclipse.dirigible.ide.common.CommonParameters;
import org.eclipse.dirigible.ide.common.status.StatusLineManagerUtil;
import org.eclipse.dirigible.ide.template.ui.common.GenerationModel;
import org.eclipse.dirigible.ide.template.ui.common.TemplateGenerator;
import org.eclipse.dirigible.ide.template.ui.common.TemplateWizard;

public class JavascriptServiceTemplateWizard extends TemplateWizard {

	private static final String CREATE_SCRIPTING_SERVICE = Messages.JavascriptServiceTemplateWizard_CREATE_SCRIPTING_SERVICE;
	private final JavascriptServiceTemplateModel model;
	private final JavascriptServiceTemplateTypePage typesPage;
	private final TablesTemplateTablePage tablesTemplateTablePage;
	private final JavascriptServiceTemplateTargetLocationPage targetLocationPage;

	public JavascriptServiceTemplateWizard(IResource resource) {
		setWindowTitle(CREATE_SCRIPTING_SERVICE);

		model = new JavascriptServiceTemplateModel();
		model.setSourceResource(resource);
		typesPage = new JavascriptServiceTemplateTypePage(model);
		tablesTemplateTablePage = new TablesTemplateTablePage(model);
		targetLocationPage = new JavascriptServiceTemplateTargetLocationPage(
				model);
	}

	@Override
	public void addPages() {
		addPage(typesPage);
		addPage(tablesTemplateTablePage);
		addPage(targetLocationPage);
	}

	@Override
	public TemplateGenerator getTemplateGenerator() {
		JavascriptServiceTemplateGenerator generator = new JavascriptServiceTemplateGenerator(
				model);
		return generator;
	}

	@Override
	protected GenerationModel getModel() {
		return model;
	}

	@Override
	public IWizardPage getNextPage(IWizardPage page) {
		if (page instanceof JavascriptServiceTemplateTypePage) {
			if (("/org/eclipse/dirigible/ide/template/ui/js/templates/database-access.js" //$NON-NLS-1$
					.equals(model.getTemplate().getLocation()))) {
				return tablesTemplateTablePage;
			} else if (("/org/eclipse/dirigible/ide/template/ui/js/templates/database-crud.js" //$NON-NLS-1$
					.equals(model.getTemplate().getLocation()))) {
				return tablesTemplateTablePage;
			} else {
				return targetLocationPage;
			}
		}
		if (page instanceof TablesTemplateTablePage) {
			return targetLocationPage;
		}
		return super.getNextPage(page);
	}

	@Override
	public IWizardPage getPreviousPage(IWizardPage page) {
		if (page instanceof TablesTemplateTablePage) {
			return typesPage;
		}
		if (page instanceof JavascriptServiceTemplateTargetLocationPage) {
			if (("/org/eclipse/dirigible/ide/template/ui/js/templates/database-access.js" //$NON-NLS-1$
					.equals(model.getTemplate().getLocation()))) {
				return tablesTemplateTablePage;
			} else if (("/org/eclipse/dirigible/ide/template/ui/js/templates/database-crud.js" //$NON-NLS-1$
					.equals(model.getTemplate().getLocation()))) {
				return tablesTemplateTablePage;
			}
		}
		return super.getPreviousPage(page);
	}

	@Override
	protected String openEditorForFileWithExtension() {
		return CommonParameters.JAVASCRIPT_SERVICE_EXTENSION;
	}

	@Override
	public boolean performFinish() {
		boolean result = super.performFinish();
		if (result) {
			StatusLineManagerUtil.setInfoMessage(String.format(
					StatusLineManagerUtil.ARTIFACT_HAS_BEEN_CREATED,
					model.getFileName()));
		}
		return result;
	}

}

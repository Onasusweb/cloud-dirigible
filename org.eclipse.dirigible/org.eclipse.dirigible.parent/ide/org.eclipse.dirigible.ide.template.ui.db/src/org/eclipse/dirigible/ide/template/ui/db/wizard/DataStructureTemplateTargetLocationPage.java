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

package org.eclipse.dirigible.ide.template.ui.db.wizard;

import org.eclipse.dirigible.ide.common.CommonUtils;
import org.eclipse.dirigible.ide.template.ui.common.GenerationModel;
import org.eclipse.dirigible.ide.template.ui.common.TemplateTargetLocationPage;
import org.eclipse.dirigible.ide.ui.common.validation.IValidationStatus;
import org.eclipse.dirigible.repository.api.ICommonConstants;

public class DataStructureTemplateTargetLocationPage extends TemplateTargetLocationPage {

	private static final String DSV = ".dsv";

	private static final String VIEW_NAME_VIEW = "view_name.view";

	private static final String TABLE_NAME_TABLE = "table_name.table";

	private static final long serialVersionUID = -1678301320687605682L;

	private static final String SELECT_THE_TARGET_LOCATION_AND_THE_TARGET_FILE_NAME = Messages.DataStructureTemplateTargetLocationPage_SELECT_THE_TARGET_LOCATION_AND_THE_TARGET_FILE_NAME;

	private static final String TARGET_LOCATION = Messages.DataStructureTemplateTargetLocationPage_TARGET_LOCATION;

	private static final String PAGE_NAME = "org.eclipse.dirigible.ide.template.ui.db.wizard.DataStructureTemplateTargetLocationPage"; //$NON-NLS-1$

	private TableTemplateModel model;

	protected DataStructureTemplateTargetLocationPage(TableTemplateModel model) {
		super(PAGE_NAME);
		this.model = model;
		setTitle(TARGET_LOCATION);
		setDescription(SELECT_THE_TARGET_LOCATION_AND_THE_TARGET_FILE_NAME);
	}

	@Override
	protected void checkPageStatus() {
		if (getModel().getTargetLocation() == null || "".equals(getModel().getTargetLocation())) { //$NON-NLS-1$
			setPageComplete(false);
			return;
		}
		if (getModel().getFileName() == null || "".equals(getModel().getFileName())) { //$NON-NLS-1$
			setPageComplete(false);
			return;
		}
		IValidationStatus status = model.validateLocation();
		if (status.hasErrors()) {
			setErrorMessage(status.getMessage());
			setPageComplete(false);
		} else if (status.hasWarnings()) {
			setErrorMessage(status.getMessage());
			setPageComplete(true);
		} else {
			setErrorMessage(null);
			setPageComplete(true);
		}
	}

	@Override
	protected GenerationModel getModel() {
		return model;
	}

	@Override
	protected String getDefaultFileName(String preset) {
		String templateLocation = model.getTemplateLocation();
		String defaultName = null;
		if (templateLocation.equals(DataStructureTemplateLocations.TABLE)) {
			defaultName = (preset == null) ? TABLE_NAME_TABLE : CommonUtils.getFileNameNoExtension(preset) + ".table";
		} else if (templateLocation.equals(DataStructureTemplateLocations.VIEW)) {
			defaultName = (preset == null) ? VIEW_NAME_VIEW : CommonUtils.getFileNameNoExtension(preset) + ".view"; 
		} else if (templateLocation.equals(DataStructureTemplateLocations.DSV)) {
			String tableName = ((DataStructureTemplateModel) model).getTableName();
			defaultName = tableName.toLowerCase() + DSV;
		}
		return defaultName;
	}

	@Override
	protected boolean isForcedFileName() {
		return true;
	}

	@Override
	protected String getArtifactContainerName() {
		return ICommonConstants.ARTIFACT_TYPE.DATA_STRUCTURES;
	}

}

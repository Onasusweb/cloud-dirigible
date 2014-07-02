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

package com.sap.dirigible.ide.workspace.ui.commands;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Collection;

import org.apache.commons.io.IOUtils;
import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.MultiStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jface.wizard.Wizard;
import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.ui.handlers.HandlerUtil;

import com.sap.dirigible.ide.datasource.DataSourceFacade;
import com.sap.dirigible.ide.logging.Logger;
import com.sap.dirigible.repository.ext.db.DBTableDataInserter;

public class UploadDataHandler extends AbstractHandler {

	private static final String CANNOT_CLOSE_INPUT_STREAM_TO_AN_UPLOADED_FILE = Messages.UploadDataHandler_CANNOT_CLOSE_INPUT_STREAM_TO_AN_UPLOADED_FILE;
	private static final String UPLOADING = Messages.UploadDataHandler_UPLOADING;
	private static final String NO_FILES_SPECIFIED = Messages.UploadDataHandler_NO_FILES_SPECIFIED;
	private static final String PLUGIN_ID = "com.sap.dirigible.ide.workspace.ui"; //$NON-NLS-1$
	private static final String UPLOAD_DATA_RESULT = Messages.UploadDataHandler_UPLOAD_DATA_RESULT;
//	private static final String REASON = Messages.UploadDataHandler_REASON;
	private static final String CANNOT_STORE_DATA_FROM = Messages.UploadDataHandler_CANNOT_STORE_DATA_FROM;
	private static final String SUCCESSFULLY_IMPORTED_FILE = Messages.UploadDataHandler_SUCCESSFULLY_IMPORTED_FILE;
	private static final Logger logger = Logger
			.getLogger(UploadDataHandler.class);

	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		Wizard wizard = new UploadDataWizard(this);
		WizardDialog dialog = new WizardDialog(
				HandlerUtil.getActiveShell(event), wizard);
		dialog.open();
		return null;
	}

	public void insertIntoDbAsync(final Collection<String> fileNames) {
		Job job = new Job(UPLOADING) {
			@Override
			protected IStatus run(IProgressMonitor monitor) {
				return insertIntoDb(fileNames);
			}
		};
		job.setUser(true);
		job.schedule();
	}

	private IStatus insertIntoDb(Collection<String> fileNames) {
		if (fileNames == null || fileNames.isEmpty()) {
			return new Status(IStatus.ERROR, PLUGIN_ID, NO_FILES_SPECIFIED);
		}

		String fileName = null;
		MultiStatus multiStatus = new MultiStatus(PLUGIN_ID, IStatus.OK,
				UPLOAD_DATA_RESULT, null);
		for (String fullFileName : fileNames) {
			fileName = fullFileName.substring(fullFileName
					.lastIndexOf(File.separatorChar) + 1);
			InputStream in = null;
			try {
				in = new FileInputStream(fullFileName);
				byte[] data = IOUtils.toByteArray(in);
				DBTableDataInserter dataInserter = new DBTableDataInserter(DataSourceFacade.getInstance().getDataSource(),
						data, fileName);
				dataInserter.insert();
				multiStatus.add(new Status(IStatus.OK, PLUGIN_ID,
						SUCCESSFULLY_IMPORTED_FILE + fileName));
			} catch (Exception e) {
				logger.error(CANNOT_STORE_DATA_FROM + fileName, e);
				String errMessage = CANNOT_STORE_DATA_FROM + fileName;
				multiStatus.add(new Status(IStatus.ERROR, PLUGIN_ID,
						errMessage, e));
			} finally {
				if (in != null) {
					try {
						in.close();
					} catch (IOException e) {
						logger.warn(
								CANNOT_CLOSE_INPUT_STREAM_TO_AN_UPLOADED_FILE,
								e);
					}
				}
			}
		}

		if (multiStatus.getChildren().length == 1) {
			return multiStatus.getChildren()[0];
		} else {
			return multiStatus;
		}
	}

}

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

package com.sap.dirigible.ide.workspace.wizard.project.commands;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.zip.ZipInputStream;

import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.ui.handlers.HandlerUtil;

import com.sap.dirigible.ide.logging.Logger;
import com.sap.dirigible.ide.repository.RepositoryFacade;
import com.sap.dirigible.ide.workspace.RemoteResourcesPlugin;
import com.sap.dirigible.ide.workspace.impl.Workspace;
import com.sap.dirigible.repository.api.IRepository;

import com.sap.dirigible.ide.workspace.ui.commands.AbstractWorkspaceHandler;

public class UploadProjectHandler extends AbstractWorkspaceHandler {

	private static final String THIS_PROCESS_WILL_OVERRIDE_YOUR_EXISTING_PROJECTS_DO_YOU_WANT_TO_CONTINUE = Messages.UploadProjectHandler_THIS_PROCESS_WILL_OVERRIDE_YOUR_EXISTING_PROJECTS_DO_YOU_WANT_TO_CONTINUE;
	private static final String OVERRIDE_PROJECTS = Messages.UploadProjectHandler_OVERRIDE_PROJECTS;
	private static final String CANNOT_CLOSE_INPUT_STREAM_TO_AN_UPLOADED_FILE = Messages.UploadProjectHandler_CANNOT_CLOSE_INPUT_STREAM_TO_AN_UPLOADED_FILE;
	private static final String UPLOAD_ERROR = Messages.UploadProjectHandler_UPLOAD_ERROR;
	private static final String REASON = Messages.UploadProjectHandler_REASON;
	private static final String CANNOT_UPLOAD = Messages.UploadProjectHandler_CANNOT_UPLOAD;
	private static final String CANNOT_SAVE_UPLOADED_FILE = Messages.UploadProjectHandler_CANNOT_SAVE_UPLOADED_FILE;
	private static final String UPLOAD_PROJECT_ARCHIVE = Messages.UploadProjectHandler_UPLOAD_PROJECT_ARCHIVE;
	private static final Logger error = Logger.getLogger(UploadProjectHandler.class);

	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		FileDialog dlg = new FileDialog(HandlerUtil.getActiveShell(event), SWT.TITLE | SWT.MULTI);
//		dlg.setAutoUpload(true);
		dlg.setText(UPLOAD_PROJECT_ARCHIVE);
		dlg.setFilterExtensions(new String[] { "*.zip" }); //$NON-NLS-1$
		String projectPath = dlg.open();

		if (projectPath != null
				&& MessageDialog.openConfirm(null, OVERRIDE_PROJECTS,
						THIS_PROCESS_WILL_OVERRIDE_YOUR_EXISTING_PROJECTS_DO_YOU_WANT_TO_CONTINUE)) {
			String fileName = null;
			for (String fullFileName : dlg.getFileNames()) {
				fileName = fullFileName.substring(fullFileName.lastIndexOf(File.separatorChar) + 1);
				InputStream in = null;
				try {
					in = new FileInputStream(fullFileName);
					IRepository repository = RepositoryFacade.getInstance().getRepository();
					Workspace workspace = (Workspace) RemoteResourcesPlugin.getWorkspace();
					String root = workspace.getRepositoryPathForWorkspace(RemoteResourcesPlugin
							.getUserName());
					repository.importZip(new ZipInputStream(in), root);
					refreshWorkspace();
				} catch (Exception e) {
					error.error(CANNOT_SAVE_UPLOADED_FILE + fileName, e);
					MessageDialog.openError(null, UPLOAD_ERROR, CANNOT_UPLOAD + fileName + REASON
							+ e.getMessage());
				} finally {
					if (in != null) {
						try {
							in.close();
						} catch (IOException e) {
							error.warn(CANNOT_CLOSE_INPUT_STREAM_TO_AN_UPLOADED_FILE, e);
						}
					}
				}
			}
		}
		return null;
	}

}

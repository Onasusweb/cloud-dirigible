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

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.ui.handlers.HandlerUtil;

import com.sap.dirigible.ide.logging.Logger;

public class UploadHandler extends AbstractHandler {

	private static final String DO_YOU_WANT_TO_OVERRIDE_IT = Messages.UploadHandler_DO_YOU_WANT_TO_OVERRIDE_IT;
	private static final String FILE_S_ALREADY_EXISTS = Messages.UploadHandler_FILE_S_ALREADY_EXISTS;
	private static final String UPLOAD_FILE = Messages.UploadHandler_UPLOAD_FILE;
	private static final String CANNOT_DELETE_FILE = Messages.UploadHandler_CANNOT_DELETE_FILE;
	private static final String CANNOT_CLOSE_INPUT_STREAM_TO_AN_UPLOADED_FILE = Messages.UploadHandler_CANNOT_CLOSE_INPUT_STREAM_TO_AN_UPLOADED_FILE;
	private static final String UPLOAD_ERROR = Messages.UploadHandler_UPLOAD_ERROR;
	private static final String REASON = Messages.UploadHandler_REASON;
	private static final String CANNOT_UPLOAD = Messages.UploadHandler_CANNOT_UPLOAD;
	private static final String CANNOT_SAVE_UPLOADED_FILE = Messages.UploadHandler_CANNOT_SAVE_UPLOADED_FILE;
	private static final Logger logger = Logger.getLogger(UploadHandler.class);

	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		FileDialog dlg = new FileDialog(HandlerUtil.getActiveShell(event), SWT.TITLE | SWT.MULTI);
		dlg.setAutoUpload(true);
		dlg.setText(UPLOAD_FILE);
		dlg.open();

		IStructuredSelection selection = (IStructuredSelection) HandlerUtil
				.getCurrentSelection(event);
		Object firstElement = selection.getFirstElement();

		if (firstElement instanceof IFolder) {
			IFolder folder = (IFolder) firstElement;
			String fileName = null;
			for (String fullFileName : dlg.getFileNames()) {
				fileName = fullFileName.substring(fullFileName.lastIndexOf(File.separatorChar) + 1);
				IFile file = folder.getFile(fileName);
				InputStream in = null;
				try {
					in = new FileInputStream(fullFileName);
					if (file.exists()) {
						boolean overrideIt = MessageDialog.openConfirm(null,
								String.format(FILE_S_ALREADY_EXISTS, file.getName()),
								DO_YOU_WANT_TO_OVERRIDE_IT);
						if (overrideIt) {
							file.setContents(in, IResource.FORCE, null);
						}
					} else {
						file.create(in, false, null);
					}
				} catch (Exception e) {
					logger.error(CANNOT_SAVE_UPLOADED_FILE + fileName, e);
					MessageDialog.openError(null, UPLOAD_ERROR, CANNOT_UPLOAD + fileName + REASON
							+ e.getMessage());
					cleanUp(folder, fileName);
				} finally {
					if (in != null) {
						try {
							in.close();
						} catch (IOException e) {
							logger.warn(CANNOT_CLOSE_INPUT_STREAM_TO_AN_UPLOADED_FILE, e);
						}
					}
				}
			}
		}
		return null;
	}

	private void cleanUp(IFolder folder, String fileName) {
		IFile file = folder.getFile(fileName);
		try {
			file.delete(true, null);
		} catch (CoreException e) {
			logger.warn(CANNOT_DELETE_FILE, e);
		}
	}

}

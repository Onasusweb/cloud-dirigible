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

package com.sap.dirigible.ide.jgit.command;

import java.io.File;
import java.io.IOException;
import java.net.UnknownHostException;

import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.window.Window;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.api.errors.InvalidRemoteException;
import org.eclipse.jgit.api.errors.TransportException;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.handlers.HandlerUtil;

import com.sap.dirigible.ide.common.status.StatusLineManagerUtil;
import com.sap.dirigible.ide.jgit.command.ui.ShareCommandDialog;
import com.sap.dirigible.ide.jgit.connector.JGitConnector;
import com.sap.dirigible.ide.jgit.utils.CommandHandlerUtils;
import com.sap.dirigible.ide.jgit.utils.GitFileUtils;
import com.sap.dirigible.ide.jgit.utils.GitProjectProperties;
import com.sap.dirigible.ide.logging.Logger;
import com.sap.dirigible.ide.workspace.RemoteResourcesPlugin;
import com.sap.dirigible.ide.workspace.impl.DefaultProgressMonitor;
import com.sap.dirigible.ide.workspace.impl.Workspace;
import com.sap.dirigible.ide.workspace.ui.commands.AbstractWorkspaceHandler;
import com.sap.dirigible.repository.api.IRepository;

public class ShareCommandHandler extends AbstractWorkspaceHandler {
	private static final String TASK_SHARING_PROJECT = Messages.ShareCommandHandler_TASK_SHARING_PROJECT;
	private static final String PROJECT_S_SUCCESSFULY_SHARED = Messages.ShareCommandHandler_PROJECT_S_SUCCESSFULY_SHARED;
	private static final String MASTER = "master"; //$NON-NLS-1$
	private static final String DOT_GIT = ".git"; //$NON-NLS-1$
	private static final String SLASH = "/"; //$NON-NLS-1$
	private static final String INCORRECT_USERNAME_AND_OR_PASSWORD_OR_GIT_REPOSITORY_URI = Messages.ShareCommandHandler_INCORRECT_USERNAME_AND_OR_PASSWORD_OR_GIT_REPOSITORY_URI;
	private static final String PLEASE_CHECK_IF_PROXY_SETTINGS_ARE_SET_PROPERLY = Messages.ShareCommandHandler_PLEASE_CHECK_IF_PROXY_SETTINGS_ARE_SET_PROPERLY;
	private static final String PLEASE_SELECT_ONE = Messages.ShareCommandHandler_PLEASE_SELECT_ONE;
	private static final String ONLY_ONE_PROJECT_CAN_BE_SHARED_AT_A_TIME = Messages.ShareCommandHandler_ONLY_ONE_PROJECT_CAN_BE_SHARED_AT_A_TIME;
	private static final String NO_PROJECT_IS_SELECTED_FOR_SHARE = Messages.ShareCommandHandler_NO_PROJECT_IS_SELECTED_FOR_SHARE;
	private static final String ERROR_DURING_SHARE = Messages.ShareCommandHandler_ERROR_DURING_SHARE;
	private static final String WHILE_SHARING_PROJECT_ERROR_OCCURED = Messages.ShareCommandHandler_WHILE_SHARING_PROJECT_ERROR_OCCURED;

	private static final Logger logger = Logger.getLogger(ShareCommandHandler.class);

	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		final Shell parent = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell();
		final ISelection selection = HandlerUtil.getActiveMenuSelection(event);
		final IProject[] projects = CommandHandlerUtils.getProjects(selection, logger);

		if (projects.length == 0) {
			logger.warn(NO_PROJECT_IS_SELECTED_FOR_SHARE);
			StatusLineManagerUtil.setWarningMessage(NO_PROJECT_IS_SELECTED_FOR_SHARE);
			MessageDialog.openWarning(null, NO_PROJECT_IS_SELECTED_FOR_SHARE, PLEASE_SELECT_ONE);
			return null;
		} else if (projects.length > 1) {
			logger.warn(ONLY_ONE_PROJECT_CAN_BE_SHARED_AT_A_TIME);
			StatusLineManagerUtil.setWarningMessage(ONLY_ONE_PROJECT_CAN_BE_SHARED_AT_A_TIME);
			MessageDialog.openWarning(null, ONLY_ONE_PROJECT_CAN_BE_SHARED_AT_A_TIME,
					PLEASE_SELECT_ONE);
			return null;
		}

		DefaultProgressMonitor monitor = new DefaultProgressMonitor();
		monitor.beginTask(TASK_SHARING_PROJECT, IProgressMonitor.UNKNOWN);

		ShareCommandDialog shareCommandDialog = new ShareCommandDialog(parent);
		switch (shareCommandDialog.open()) {
		case Window.OK:
			String commitMessage = shareCommandDialog.getCommitMessage();
			String repositoryURI = shareCommandDialog.getGitRepositoryURI();
			String username = shareCommandDialog.getUsername();
			String email = shareCommandDialog.getEmail();
			String password = shareCommandDialog.getPassword();

			shareToGitRepository(projects[0], commitMessage, username, email, password,
					repositoryURI);
			break;
		}
		monitor.done();

		return null;
	}

	private void shareToGitRepository(IProject project, String commitMessage, String username,
			String email, String password, String repositoryURI) {
		final String errorMessage = String.format(WHILE_SHARING_PROJECT_ERROR_OCCURED,
				project.getName());
		File gitDirectory = null;
		try {
			final String repositoryName = repositoryURI.substring(
					repositoryURI.lastIndexOf(SLASH) + 1, repositoryURI.lastIndexOf(DOT_GIT));
			gitDirectory = GitFileUtils.createTempDirectory(JGitConnector.TEMP_DIRECTORY_PREFIX
					+ repositoryName);
			JGitConnector.cloneRepository(repositoryURI, gitDirectory);

			Repository repository = JGitConnector.getRepository(gitDirectory.getAbsolutePath());
			JGitConnector jgit = new JGitConnector(repository);

			GitFileUtils.copyProjectToDirectory(project, gitDirectory);
			jgit.add(JGitConnector.ADD_ALL_FILE_PATTERN);
			jgit.commit(commitMessage, username, email, true);
			jgit.push(username, password);

			Workspace workspace = (Workspace) RemoteResourcesPlugin.getWorkspace();
			IRepository dirigibleRepository = workspace.getRepository();

			String lastSHA = jgit.getLastSHAForBranch(MASTER);
			GitProjectProperties properties = new GitProjectProperties(repositoryURI, lastSHA);
			String user = RemoteResourcesPlugin.getUserName();

			GitFileUtils.saveGitPropertiesFile(dirigibleRepository, properties, user,
					project.getName());

			String message = String.format(PROJECT_S_SUCCESSFULY_SHARED, project.getName());
			StatusLineManagerUtil.setInfoMessage(message);
			refreshWorkspace();
		} catch (InvalidRemoteException e) {
			logger.error(errorMessage, e);
			MessageDialog.openError(null, ERROR_DURING_SHARE, errorMessage + "\n" + e.getMessage()); //$NON-NLS-1$
		} catch (TransportException e) {
			logger.error(errorMessage, e);
			Throwable rootCause = e.getCause();
			if (rootCause != null) {
				rootCause = rootCause.getCause();
				if (rootCause instanceof UnknownHostException) {
					MessageDialog.openError(null, errorMessage,
							PLEASE_CHECK_IF_PROXY_SETTINGS_ARE_SET_PROPERLY);
				} else {
					MessageDialog.openError(null, errorMessage, e.getCause().getMessage()
							+ INCORRECT_USERNAME_AND_OR_PASSWORD_OR_GIT_REPOSITORY_URI);
				}
			}
		} catch (GitAPIException e) {
			logger.error(errorMessage, e);
			MessageDialog.openError(null, ERROR_DURING_SHARE, errorMessage);
		} catch (IOException e) {
			logger.error(errorMessage, e);
			MessageDialog.openError(null, ERROR_DURING_SHARE, errorMessage);
		} catch (CoreException e) {
			logger.error(errorMessage, e);
			MessageDialog.openError(null, ERROR_DURING_SHARE, errorMessage);
		} finally {
			GitFileUtils.deleteDirectory(gitDirectory);
		}
	}
}

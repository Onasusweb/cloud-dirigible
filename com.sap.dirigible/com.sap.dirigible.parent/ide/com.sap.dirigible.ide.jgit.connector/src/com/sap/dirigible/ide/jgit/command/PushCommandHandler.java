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

import com.sap.dirigible.ide.common.CommonParameters;
import com.sap.dirigible.ide.common.status.DefaultProgressMonitor;
import com.sap.dirigible.ide.common.status.StatusLineManagerUtil;
import com.sap.dirigible.ide.jgit.command.ui.PushCommandDialog;
import com.sap.dirigible.ide.jgit.connector.JGitConnector;
import com.sap.dirigible.ide.jgit.utils.CommandHandlerUtils;
import com.sap.dirigible.ide.jgit.utils.GitFileUtils;
import com.sap.dirigible.ide.jgit.utils.GitProjectProperties;
import com.sap.dirigible.ide.logging.Logger;
import com.sap.dirigible.ide.repository.RepositoryFacade;
import com.sap.dirigible.ide.workspace.ui.commands.AbstractWorkspaceHandler;
import com.sap.dirigible.repository.api.IRepository;

public class PushCommandHandler extends AbstractWorkspaceHandler {
	private static final String TASK_PUSHING_TO_REMOTE_REPOSITORY = Messages.PushCommandHandler_TASK_PUSHING_TO_REMOTE_REPOSITORY;
	private static final String PLEASE_MERGE_TO_MASTER_AND_THEN_CONTINUE_WORKING_ON_PROJECT = Messages.PushCommandHandler_PLEASE_MERGE_TO_MASTER_AND_THEN_CONTINUE_WORKING_ON_PROJECT;
	private static final String PUSHED_TO_REMOTE_BRANCH_S = Messages.PushCommandHandler_PUSHED_TO_REMOTE_BRANCH_S;
	private static final String PROJECT_HAS_D_CONFILCTING_FILES = Messages.PushCommandHandler_PROJECT_HAS_D_CONFILCTING_FILES;
	private static final String CONFLICTING_FILES = Messages.PushCommandHandler_CONFLICTING_FILES;
	private static final String THIS_IS_NOT_A_GIT_PROJECT = Messages.PushCommandHandler_THIS_IS_NOT_A_GIT_PROJECT;
	private static final String CHANGES_BRANCH = "changes_branch_"; //$NON-NLS-1$
	private static final String SLASH = "/"; //$NON-NLS-1$
	private static final String DOT_GIT = ".git"; //$NON-NLS-1$
	private static final String MASTER = "master"; //$NON-NLS-1$
	private static final String INCORRECT_USERNAME_AND_OR_PASSWORD_OR_GIT_REPOSITORY_URI = Messages.PushCommandHandler_INCORRECT_USERNAME_AND_OR_PASSWORD_OR_GIT_REPOSITORY_URI;
	private static final String PLEASE_CHECK_IF_PROXY_SETTINGS_ARE_SET_PROPERLY = Messages.PushCommandHandler_PLEASE_CHECK_IF_PROXY_SETTINGS_ARE_SET_PROPERLY;
	private static final String PLEASE_SELECT_ONE = Messages.PushCommandHandler_PLEASE_SELECT_ONE;

	private static final String ONLY_ONE_PROJECT_CAN_BE_PUSHED_AT_A_TIME = Messages.PushCommandHandler_ONLY_ONE_PROJECT_CAN_BE_PUSHED_AT_A_TIME;
	private static final String ERROR_DURING_PUSH = Messages.PushCommandHandler_ERROR_DURING_PUSH;
	private static final String WHILE_PUSHING_PROJECT_ERROR_OCCURED = Messages.PushCommandHandler_WHILE_PUSHING_PROJECT_ERROR_OCCURED;
	private static final String PROJECT_HAS_BEEN_PUSHED_TO_REMOTE_REPOSITORY = Messages.PushCommandHandler_PROJECT_HAS_BEEN_PUSHED_TO_REMOTE_REPOSITORY;
	private static final String NO_PROJECT_IS_SELECTED_FOR_PUSH = Messages.PushCommandHandler_NO_PROJECT_IS_SELECTED_FOR_PUSH;
	private static final Logger logger = Logger.getLogger(PushCommandHandler.class);

	public PushCommandHandler() {
		super();
	}

	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		final ISelection selection = HandlerUtil.getActiveMenuSelection(event);
		if (selection.isEmpty()) {
			logger.warn(NO_PROJECT_IS_SELECTED_FOR_PUSH);
			StatusLineManagerUtil.setWarningMessage(NO_PROJECT_IS_SELECTED_FOR_PUSH);
			MessageDialog.openWarning(null, NO_PROJECT_IS_SELECTED_FOR_PUSH, PLEASE_SELECT_ONE);
			return null;
		}
		final IProject[] projects = CommandHandlerUtils.getProjects(selection, logger);
		if (projects.length == 0) {
			logger.warn(NO_PROJECT_IS_SELECTED_FOR_PUSH);
			StatusLineManagerUtil.setWarningMessage(NO_PROJECT_IS_SELECTED_FOR_PUSH);
			MessageDialog.openWarning(null, NO_PROJECT_IS_SELECTED_FOR_PUSH, PLEASE_SELECT_ONE);
			return null;
		} else if (projects.length > 1) {
			logger.warn(ONLY_ONE_PROJECT_CAN_BE_PUSHED_AT_A_TIME);
			StatusLineManagerUtil.setWarningMessage(ONLY_ONE_PROJECT_CAN_BE_PUSHED_AT_A_TIME);
			MessageDialog.openWarning(null, ONLY_ONE_PROJECT_CAN_BE_PUSHED_AT_A_TIME,
					PLEASE_SELECT_ONE);
			return null;
		}

		final IProject selectedProject = projects[0];

		DefaultProgressMonitor monitor = new DefaultProgressMonitor();
		monitor.beginTask(TASK_PUSHING_TO_REMOTE_REPOSITORY, IProgressMonitor.UNKNOWN);

		final Shell parent = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell();
		PushCommandDialog pushCommandDialog = new PushCommandDialog(parent);
		switch (pushCommandDialog.open()) {
		case Window.OK:
			final String commitMessage = pushCommandDialog.getCommitMessage();
			final String username = pushCommandDialog.getUsername();
			final String password = pushCommandDialog.getPassword();
			final String email = pushCommandDialog.getEmail();
			pushProjectToGitRepository(selectedProject, commitMessage, username, email, password);
			break;
		}

		monitor.done();
		return null;
	}

	private void pushProjectToGitRepository(final IProject selectedProject,
			final String commitMessage, final String username, final String email,
			final String password) {

		final String errorMessage = String.format(WHILE_PUSHING_PROJECT_ERROR_OCCURED,
				selectedProject.getName());
		GitProjectProperties gitProperties = null;
		try {
			gitProperties = GitFileUtils.getGitPropertiesForProject(selectedProject,
					CommonParameters.getUserName());
		} catch (IOException e) {
			MessageDialog.openError(null, THIS_IS_NOT_A_GIT_PROJECT, errorMessage);
			return;
		}
		File tempGitDirectory = null;
		try {
			String gitRepositoryURI = gitProperties.getURL();
			String repositoryName = gitRepositoryURI.substring(
					gitRepositoryURI.lastIndexOf(SLASH) + 1, gitRepositoryURI.lastIndexOf(DOT_GIT));
			tempGitDirectory = GitFileUtils.createTempDirectory(JGitConnector.TEMP_DIRECTORY_PREFIX
					+ repositoryName);
			JGitConnector.cloneRepository(tempGitDirectory, gitRepositoryURI, username, password);

			Repository repository = JGitConnector.getRepository(tempGitDirectory.toString());
			JGitConnector jgit = new JGitConnector(repository);
			String lastSHA = gitProperties.getSHA();

			final String changesBranch = CHANGES_BRANCH + CommonParameters.getUserName();
			jgit.checkout(lastSHA);
			jgit.createBranch(changesBranch, lastSHA);
			jgit.checkout(changesBranch);

			GitFileUtils.deleteProjectFolderFromDirectory(tempGitDirectory,
					selectedProject.getName());
			GitFileUtils.copyProjectToDirectory(selectedProject, tempGitDirectory);

			jgit.add(selectedProject.getName());
			jgit.commit(commitMessage, username, email, true);
			jgit.pull();
			int numberOfConflictingFiles = jgit.status().getConflicting().size();
			if (numberOfConflictingFiles == 0) {
				jgit.checkout(MASTER);
				jgit.rebase(changesBranch);
				jgit.push(username, password);

				String dirigibleUser = CommonParameters.getUserName();

				GitFileUtils.deleteDGBRepositoryProject(selectedProject, dirigibleUser);

				IRepository dirigibleRepository = RepositoryFacade.getInstance().getRepository();

				String workspacePath = String.format(
						GitProjectProperties.DB_DIRIGIBLE_USERS_S_WORKSPACE, dirigibleUser);

				String newLastSHA = jgit.getLastSHAForBranch(MASTER);
				gitProperties.setSHA(newLastSHA);

				GitFileUtils.importProject(tempGitDirectory, dirigibleRepository, workspacePath,
						dirigibleUser, gitProperties);

				refreshWorkspace();
				StatusLineManagerUtil.setInfoMessage(String.format(
						PROJECT_HAS_BEEN_PUSHED_TO_REMOTE_REPOSITORY, selectedProject.getName()));
			} else {
				jgit.hardReset();
				jgit.push(username, password);
				String statusLineMessage = String.format(PROJECT_HAS_D_CONFILCTING_FILES,
						numberOfConflictingFiles);
				StatusLineManagerUtil.setWarningMessage(statusLineMessage);
				String message = String.format(PROJECT_HAS_D_CONFILCTING_FILES
						+ PUSHED_TO_REMOTE_BRANCH_S
						+ PLEASE_MERGE_TO_MASTER_AND_THEN_CONTINUE_WORKING_ON_PROJECT,
						numberOfConflictingFiles, changesBranch);
				MessageDialog.openWarning(null, CONFLICTING_FILES, message);
			}
		} catch (IOException e) {
			logger.error(errorMessage, e);
			MessageDialog.openError(null, ERROR_DURING_PUSH, errorMessage);
		} catch (CoreException e) {
			logger.error(errorMessage, e);
			MessageDialog.openError(null, ERROR_DURING_PUSH, errorMessage);
		} catch (InvalidRemoteException e) {
			logger.error(errorMessage, e);
			MessageDialog.openError(null, ERROR_DURING_PUSH, errorMessage + "\n" + e.getMessage()); //$NON-NLS-1$
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
			MessageDialog.openError(null, ERROR_DURING_PUSH, errorMessage);
		} finally {
			GitFileUtils.deleteDirectory(tempGitDirectory);
		}
	}
}

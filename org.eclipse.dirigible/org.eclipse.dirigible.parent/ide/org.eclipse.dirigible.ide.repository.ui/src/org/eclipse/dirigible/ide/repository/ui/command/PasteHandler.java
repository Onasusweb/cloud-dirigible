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

package org.eclipse.dirigible.ide.repository.ui.command;

import java.io.IOException;
import java.util.Iterator;
import java.util.SortedSet;

import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.jface.dialogs.MessageDialog;

import org.eclipse.dirigible.ide.repository.RepositoryFacade;
import org.eclipse.dirigible.repository.api.IEntity;
import org.eclipse.dirigible.repository.api.IRepository;

public class PasteHandler extends AbstractClipboardHandler {

	private static final String SOME_OR_ALL_OF_THE_FILES_COULD_NOT_BE_PASTED = Messages.PasteHandler_SOME_OR_ALL_OF_THE_FILES_COULD_NOT_BE_PASTED;
	private static final String PASTE_ERROR = Messages.PasteHandler_PASTE_ERROR;

	protected void execute(ExecutionEvent event, SortedSet<IEntity> resources) {
		if (resources.size() == 0) {
			return;
		}

		IRepository repository = RepositoryFacade.getInstance().getRepository();

		String targetReposiotryPath = resources.first().getPath().toString();

		Clipboard clipboard = Clipboard.getInstance();

		String command = clipboard.getCommand();

		Throwable throwable = null;
		if (CUT.equals(command) || COPY.equals(command)) {

			for (Iterator<?> iterator = clipboard.iterator(); iterator.hasNext();) {
				IEntity resource = (IEntity) iterator.next();
				String sourceRepositoryPath = resource.getPath().toString();
				try {
					byte[] data = repository.exportZip(sourceRepositoryPath,
							true);
					repository.importZip(data, targetReposiotryPath);
				} catch (IOException e) {
					if (throwable == null) {
						throwable = e;
					}
				}
				if (CUT.equals(command)) {
					try {
						resource.delete();
					} catch (IOException e) {
						if (throwable == null) {
							throwable = e;
						}
					}
				}
			}
		}

		if (throwable != null) {
			MessageDialog.openWarning(null, PASTE_ERROR,
					SOME_OR_ALL_OF_THE_FILES_COULD_NOT_BE_PASTED);
		}

		RefreshHandler.refreshActivePart(event);

	}

}

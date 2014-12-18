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

package com.sap.dirigible.ide.publish;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyBoolean;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Answers;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

import com.sap.dirigible.ide.publish.AbstractPublisher;
import com.sap.dirigible.repository.api.ICollection;
import com.sap.dirigible.repository.api.IRepository;

public class PublisherTest {
	private static final String FILE_PATH = "/file_path";

	private static final String FILE_NAME = "file_name";

	private static final String GUEST_USER = "guest";

	@Mock(answer = Answers.CALLS_REAL_METHODS)
	private AbstractPublisher publisher;
	@Mock
	private IContainer source;
	@Mock
	private ICollection target;

	private IResource[] createFiles(int count) throws CoreException {
		List<IResource> files = new ArrayList<IResource>();

		for (int i = 0; i < count; i++) {
			IPath path = Mockito.mock(IPath.class);
			IWorkspaceRoot root = Mockito.mock(IWorkspaceRoot.class);
			IWorkspace workspace = Mockito.mock(IWorkspace.class);
			IFile file = Mockito.mock(IFile.class);
			InputStream is = Mockito.mock(InputStream.class);

			when(file.getName()).thenReturn(FILE_NAME);
			when(file.getWorkspace()).thenReturn(workspace);
			when(workspace.getRoot()).thenReturn(root);
			when(root.getRawLocation()).thenReturn(path);
			when(path.toString()).thenReturn(FILE_PATH);
			when(file.getFullPath()).thenReturn(path);
			when(file.getContents()).thenReturn(is);

			files.add(file);
		}
		return files.toArray(new IResource[0]);
	}

	private IResource[] createFolders(int count) throws CoreException {
		List<IResource> folders = new ArrayList<IResource>();

		for (int i = 0; i < count; i++) {
			IContainer folder = Mockito.mock(IFolder.class);

			when(folder.exists()).thenReturn(true);
			when(folder.members()).thenReturn(createFiles(i));

			folders.add(folder);
		}
		return folders.toArray(new IResource[0]);
	}

	@Before
	public void setUp() throws Exception {
		MockitoAnnotations.initMocks(this);
		doReturn(GUEST_USER).when(publisher).getUser();
	}

	@Test
	public void publishingOneFile() throws Exception {
		int oneFile = 1;

		com.sap.dirigible.repository.api.IResource fileResource = Mockito
				.mock(com.sap.dirigible.repository.api.IResource.class);
		IRepository repository = Mockito.mock(IRepository.class);
		IResource[] files = createFiles(oneFile);

		when(target.getResource(anyString())).thenReturn(fileResource);
		when(target.getRepository()).thenReturn(repository);
		when(target.getRepository().getResource(anyString())).thenReturn(fileResource);

		when(source.exists()).thenReturn(true);
		when(source.members()).thenReturn(files);

		publisher.copyAllFromTo(source, target);

		verify(publisher, times(oneFile)).copyFileInto(any(IFile.class), any(ICollection.class),
				anyString());
		verify(fileResource, times(oneFile)).setContent(any(byte[].class), any(boolean.class), any(String.class));
	}

	@Test
	public void publishingFiveBinaryFiles() throws Exception {
		int fiveFiles = 5;

		com.sap.dirigible.repository.api.IResource fileResource = Mockito
				.mock(com.sap.dirigible.repository.api.IResource.class);
		IRepository repository = Mockito.mock(IRepository.class);
		IResource[] files = createFiles(fiveFiles);

		when(target.getResource(anyString())).thenReturn(fileResource);
		when(target.getRepository()).thenReturn(repository);
		when(target.getRepository().getResource(anyString())).thenReturn(fileResource);
		when(fileResource.isBinary()).thenReturn(true);

		when(source.exists()).thenReturn(true);
		when(source.members()).thenReturn(files);

		publisher.copyAllFromTo(source, target);

		verify(publisher, times(fiveFiles)).copyFileInto(any(IFile.class), any(ICollection.class),
				anyString());
		verify(fileResource, times(fiveFiles)).setContent(any(byte[].class), anyBoolean(),
				anyString());
	}

	@Test
	public void publishingOneFolder() throws Exception {

		int oneFolder = 1;
		IResource[] folders = createFolders(oneFolder);
		ICollection targetFolder = Mockito.mock(ICollection.class);

		when(source.exists()).thenReturn(true);
		when(source.members()).thenReturn(folders);

		when(target.getCollection(anyString())).thenReturn(targetFolder);
		when(targetFolder.exists()).thenReturn(false);

		publisher.copyAllFromTo(source, target);

		verify(publisher, times(oneFolder)).copyFolderInto(any(IFolder.class),
				any(ICollection.class), anyString());
		verify(targetFolder, times(oneFolder)).create();
	}
}

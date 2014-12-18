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

package test.com.sap.dirigible.ide.template.ui.common;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import static org.mockito.Mockito.*;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Path;
import org.eclipse.rap.rwt.SingletonUtil;

import com.sap.dirigible.ide.template.ui.common.GenerationModel;
import com.sap.dirigible.ide.ui.common.validation.ValidationStatus;
import com.sap.dirigible.ide.workspace.RemoteResourcesPlugin;

/**
 * GenerationModelTest class tests GenerationModel
 */
public class GenerationModelTest {

	private static final String EMPTY_STRING = ""; //$NON-NLS-1$
	private static final String FILE_NAME_NO_EXTENSION = "fileName"; //$NON-NLS-1$
	private static final String FILE_NAME_WITH_EXTENSION = "fileName.txt"; //$NON-NLS-1$
	private static final String TEMPLATE_LOCATION = "Template Location"; //$NON-NLS-1$
	private static final String TARGET_LOCATION = "targetLocation/services"; //$NON-NLS-1$
	private static final String PROJECT_NAME = "targetLocation"; //$NON-NLS-1$
	private static final String TEMPLATE_LOCATION_IS_EMPTY = "Template location is empty"; //$NON-NLS-1$
	private static final String COULD_NOT_OPEN_INPUT_STREAM_FOR = "Could not open input stream for: %s"; //$NON-NLS-1$
	private static final String RESOURCE_ALREADY_EXISTS_IN_THE_WORKSPACE = "Resource already exists in the workspace %s"; //$NON-NLS-1$
	private static final String PATH_IS_NOT_VALID_FOR_A_RESOURCE_OF_THE_GIVEN_TYPE_S = "Path is not valid for a resource of the given type(s)"; //$NON-NLS-1$
	private static final String NAME_IS_NOT_VALID_FOR_A_RESOURCE_OF_THE_GIVEN_TYPE_S = "Name is not valid for a resource of the given type(s)"; //$NON-NLS-1$
	
	GenerationModel generationModel;
	ValidationStatus validationStatus;
	IWorkspace workspace;
	RemoteResourcesPlugin remoteResourcesPlugin;
	IStatus iStatus;
	SingletonUtil singletonUtil;
	IWorkspaceRoot root;
	IResource resource;
	 
	@Before
	public void setUp() {
		generationModel = Mockito.mock(GenerationModel.class, Mockito.CALLS_REAL_METHODS);
		remoteResourcesPlugin = Mockito.mock(RemoteResourcesPlugin.class);
		workspace = Mockito.mock(IWorkspace.class);
		iStatus = Mockito.mock(IStatus.class);
		root = Mockito.mock(IWorkspaceRoot.class);
	}
	/**
	 * Testing method validateTemplate() in class GenerationModel
	 */
	@Test
	public void testValidateTemplateWhenTemplateLocationIsNullOrEmtyString() {
		// when template location is null

		assertNull(generationModel.getTemplateLocation());
		generationModel.getTemplateLocation();
		String actual = generationModel.validateTemplate().getMessage();
		String expected = ValidationStatus.createError(
				TEMPLATE_LOCATION_IS_EMPTY).getMessage();
		assertEquals(actual, expected);

		// when template location is empty string

		Mockito.when(generationModel.getTemplateLocation()).thenReturn(
				EMPTY_STRING);
		String actualSecondCase = generationModel.validateTemplate()
				.getMessage();
		String expectedSecondCase = ValidationStatus.createError(
				TEMPLATE_LOCATION_IS_EMPTY).getMessage();
		assertEquals(actualSecondCase, expectedSecondCase);
	}

	@Test
	public void testValidateTemplateWhenInputStreamIsNull() {
		// valid templateLocation, but InputStream is null

		Mockito.when(generationModel.getTemplateLocation()).thenReturn(
				TEMPLATE_LOCATION);
		String actual = generationModel.validateTemplate().getMessage();
		String expected = ValidationStatus.createError(
				String.format(COULD_NOT_OPEN_INPUT_STREAM_FOR, TEMPLATE_LOCATION))
				.getMessage();
		assertEquals(actual, expected);
	}

	/**
	 * Testing method getFileNameNoExtension() in class GenerationModel
	 */
	@Test
	public void testGetFileNameNoExtension() {
		// case when file has not extension
		generationModel.setFileName(FILE_NAME_NO_EXTENSION);
		String actual = generationModel.getFileNameNoExtension();
		assertEquals(actual, FILE_NAME_NO_EXTENSION);

		// case when file has an extension
		generationModel.setFileName(FILE_NAME_WITH_EXTENSION);
		String actualSecondCase = generationModel.getFileNameNoExtension();
		assertEquals(actualSecondCase, FILE_NAME_NO_EXTENSION);
	}

	/**
	 * Testing method getProjectName() in class GenerationModel
	 */
	@Test 
	public void testgetProjectName() {
		generationModel.setTargetLocation(TARGET_LOCATION);
		generationModel.setFileName(FILE_NAME_WITH_EXTENSION);

//		IPath location = new Path(generationModel.getTargetLocation())
//				.append(generationModel.getFileName());
		assertEquals(generationModel.getProjectName(), PROJECT_NAME);
	}

	/**
	 * Testing method validateLocationGeneric() in class GenerationModel
	 */
	@Test
	public void testValidateLocationGenericPathNotIsValid() {
		//Path is not valid for a resource of the given type(s)
		
		generationModel.setTargetLocation(TARGET_LOCATION);

		Mockito.when(
				workspace.validatePath(generationModel.getTargetLocation(),
						IResource.FOLDER)).thenReturn(iStatus);
		Mockito.when(
				workspace.validatePath(generationModel.getTargetLocation(),
						IResource.PROJECT)).thenReturn(iStatus);

		String actual = generationModel.validateLocationGeneric(workspace)
				.getMessage();
		String expected = ValidationStatus.createError(
				PATH_IS_NOT_VALID_FOR_A_RESOURCE_OF_THE_GIVEN_TYPE_S)
				.getMessage();
		assertEquals(actual, expected);
	}

	@Test
	public void testValidateLocationGenericNameIsNotValid() {
		// Name is not valid for a resource of the given type(s)
		
		Mockito.when(iStatus.isOK()).thenReturn(true, false);

		Mockito.when(workspace.validatePath(anyString(), anyInt())).thenReturn(
				iStatus);

		Mockito.when(workspace.validateName(anyString(), anyInt())).thenReturn(
				iStatus);

		generationModel.setTargetLocation(TARGET_LOCATION);
		String actual = generationModel.validateLocationGeneric(workspace)
				.getMessage();
		String expected = ValidationStatus.createError(
				NAME_IS_NOT_VALID_FOR_A_RESOURCE_OF_THE_GIVEN_TYPE_S)
				.getMessage();
		assertEquals(actual, expected);
	}
	
	@Test
	public void testValidateLocationGenericResourceAlreadyExists() {
		//Resource already exists in the workspace
		resource = Mockito.mock(IResource.class);
		Mockito.when(iStatus.isOK()).thenReturn(true, true);

		Mockito.when(workspace.validatePath(anyString(), anyInt())).thenReturn(
				iStatus);

		Mockito.when(workspace.validateName(anyString(), anyInt())).thenReturn(
				iStatus);
		Mockito.when(workspace.getRoot()).thenReturn(root);
		Mockito.when(root.findMember(anyString())).thenReturn(resource);

		generationModel.setTargetLocation(TARGET_LOCATION);
		generationModel.setFileName(FILE_NAME_WITH_EXTENSION);

//		IPath location = new Path(generationModel.getTargetLocation())
//				.append(generationModel.getFileName());
		
		
		String actual = generationModel.validateLocationGeneric(workspace)
				.getMessage();
		String expected = ValidationStatus.createError(
				String.format(RESOURCE_ALREADY_EXISTS_IN_THE_WORKSPACE, (TARGET_LOCATION + "/" + FILE_NAME_WITH_EXTENSION))) //$NON-NLS-1$
				.getMessage();
		assertEquals(expected, actual);

	}
	 
	@Test
	public void testValidateLocationGenericStatusOk() {
		// Valid path, name and resource does not already exist in the workspace
		
		Mockito.when(iStatus.isOK()).thenReturn(true, true);

		Mockito.when(workspace.validatePath(anyString(), anyInt())).thenReturn(
				iStatus);

		Mockito.when(workspace.validateName(anyString(), anyInt())).thenReturn(
				iStatus);
		Mockito.when(workspace.getRoot()).thenReturn(root);
		Mockito.when(root.findMember(anyString())).thenReturn(resource);

		generationModel.setTargetLocation(TARGET_LOCATION);
		generationModel.setFileName(FILE_NAME_WITH_EXTENSION);

		IPath location = new Path(generationModel.getTargetLocation())
				.append(generationModel.getFileName());
		Mockito.when(location.toString()).thenReturn("/foo/bar.txt"); //$NON-NLS-1$
		
		String actual = generationModel.validateLocationGeneric(workspace)
				.getMessage();
		String expected = ValidationStatus.createOk()
				.getMessage();
		assertEquals(actual, expected);

	}
}

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

package com.sap.dirigible.ide.template.ui.common;

import static java.text.MessageFormat.format;

import java.io.IOException;
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExtension;
import org.eclipse.core.runtime.IExtensionPoint;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;

import com.sap.dirigible.ide.common.ExtensionPointUtils;
import com.sap.dirigible.ide.logging.Logger;
import com.sap.dirigible.ide.ui.common.validation.IValidationStatus;

public abstract class TemplateTypeWizardPage extends WizardPage {

	private static final String EXTENSION_POINT_0_COULD_NOT_BE_FOUND = Messages.TemplateTypeWizardPage_EXTENSION_POINT_0_COULD_NOT_BE_FOUND;
	private static final long serialVersionUID = -5435162447712125969L;
	private static final String EMPTY_STRING = ""; //$NON-NLS-1$
	private static final String ERROR_ON_LOADING_TEMPLATES_FOR_GENERATION = Messages.TemplateTypeWizardPage_ERROR_ON_LOADING_TEMPLATES_FOR_GENERATION;
	private static final String SELECT_TEMPLATE_TYPE_FORM_THE_LIST = Messages.TemplateTypeWizardPage_SELECT_TEMPLATE_TYPE_FORM_THE_LIST;
	private static final String AVAILABLE_TEMPLATES = Messages.TemplateTypeWizardPage_AVAILABLE_TEMPLATES;
	
	private static final String TEMPLATE_TYPE_EXTENSION_POINT_ID 	= "com.sap.dirigible.ide.template.type"; //$NON-NLS-1$
	private static final String TEMPLATE_TYPE_ELEMENT_NAME 			= "template"; //$NON-NLS-1$
	private static final String TEMPLATE_TYPE_TEXT_ATTRIBUTE 		= "text"; //$NON-NLS-1$
	private static final String TEMPLATE_TYPE_LOCATION_ATTRIBUTE 	= "location"; //$NON-NLS-1$
	private static final String TEMPLATE_TYPE_IMAGE_ATTRIBUTE 		= "image"; //$NON-NLS-1$
	private static final String TEMPLATE_TYPE_CATEGORY_ATTRIBUTE 	= "category"; //$NON-NLS-1$
	private static final String TEMPLATE_TYPE_PARAMETER_ATTRIBUTE 	= "parameter"; //$NON-NLS-1$
	private static final String TEMPLATE_TYPE_VALUE_ATTRIBUTE 		= "value"; //$NON-NLS-1$
	
	protected TemplateTypeWizardPage(String pageName) {
		super(pageName);
	}

	private static final Logger logger = Logger
			.getLogger(TemplateTypeWizardPage.class);

	protected abstract GenerationModel getModel();

	private TableViewer typeViewer;

	@Override
	public void createControl(Composite parent) {
		final Composite composite = new Composite(parent, SWT.NONE);
		setControl(composite);
		composite.setLayout(new GridLayout());
		createTypeField(composite);

		checkPageStatus();
	}

	private void createTypeField(Composite parent) {
		final Label label = new Label(parent, SWT.NONE);
		label.setText(AVAILABLE_TEMPLATES);
		label.setLayoutData(new GridData(SWT.LEFT, SWT.BOTTOM, false, false));

		typeViewer = new TableViewer(parent, SWT.SINGLE | SWT.BORDER
				| SWT.V_SCROLL);
		typeViewer.getControl().setLayoutData(
				new GridData(SWT.FILL, SWT.FILL, true, true));
		typeViewer.setContentProvider(new ArrayContentProvider());
		typeViewer.setLabelProvider(new TemplateTypePageLabelProvider());
		TemplateType[] templateTypes = createTemplateTypes();
		typeViewer.setInput(templateTypes);
		typeViewer.addSelectionChangedListener(new ISelectionChangedListener() {

			@Override
			public void selectionChanged(SelectionChangedEvent event) {
				IStructuredSelection selection = (IStructuredSelection) event
						.getSelection();
				if (selection.getFirstElement() == null
						|| !(selection.getFirstElement() instanceof TemplateType)) {
					setErrorMessage(SELECT_TEMPLATE_TYPE_FORM_THE_LIST);
				} else {
					setErrorMessage(null);
					TemplateType templateType = ((TemplateType) selection
							.getFirstElement());
					getModel().setTemplate(templateType);
				}
				checkPageStatus();
			}
		});

	}

	private TemplateType[] createTemplateTypes() {
		try {
			TemplateType[] templateTypes = prepareTemplateTypes();
			return templateTypes;
		} catch (IOException e) {
			logger.error(ERROR_ON_LOADING_TEMPLATES_FOR_GENERATION, e);
		}
		return null;
	}

//	protected abstract TemplateType[] prepareTemplateTypes() throws IOException;

	private void checkPageStatus() {
		if (getModel().getTemplateLocation() == null
				|| EMPTY_STRING.equals(getModel().getTemplateLocation())) {
			setPageComplete(false);
			return;
		}
		IValidationStatus status = getModel().validateTemplate();
		if (status.hasErrors()) {
			setPageComplete(false);
		} else if (status.hasWarnings()) {
			setPageComplete(true);
		} else {
			setPageComplete(true);
		}
	}
	
	public List<TemplateTypeDescriptor> getTemplateTypeDescriptors(String category) {

		List<TemplateTypeDescriptor> templateTypeDescriptors = new ArrayList<TemplateTypeDescriptor>();
		final IExtensionPoint extensionPoint = ExtensionPointUtils.getExtensionPoint(TEMPLATE_TYPE_EXTENSION_POINT_ID);
		if (extensionPoint == null) {
			throw new TemplateTypeException(format(
					EXTENSION_POINT_0_COULD_NOT_BE_FOUND,
					TEMPLATE_TYPE_EXTENSION_POINT_ID));
		}
		final IConfigurationElement[] templateTypeDescriptorElements = getTemplateElements(extensionPoint
				.getExtensions());

		for (int i = 0; i < templateTypeDescriptorElements.length; i++) {
			TemplateTypeDescriptor templateTypeDescriptor = createTemplateTypeDescriptor(category, templateTypeDescriptorElements[i]);
			if (templateTypeDescriptor != null) {
				templateTypeDescriptors.add(templateTypeDescriptor);
			}
		}

		return templateTypeDescriptors;
	}

	private IConfigurationElement[] getTemplateElements(
			IExtension[] extensions) {
		final List<IConfigurationElement> result = new ArrayList<IConfigurationElement>();
		for (IExtension extension : extensions) {
			for (IConfigurationElement element : extension
					.getConfigurationElements()) {
				if (TEMPLATE_TYPE_ELEMENT_NAME.equals(element.getName())) {
					result.add(element);
				}
			}
		}
		return result.toArray(new IConfigurationElement[0]);
	}
	
	private TemplateTypeDescriptor createTemplateTypeDescriptor(String category,
			IConfigurationElement templateTypeDescriptorElement) {
		
		
		if (category.equals(templateTypeDescriptorElement.getAttribute(TEMPLATE_TYPE_CATEGORY_ATTRIBUTE))) {
		
			TemplateTypeDescriptor templateTypeDescriptor = new TemplateTypeDescriptor();
			templateTypeDescriptor.setText(templateTypeDescriptorElement.getAttribute(TEMPLATE_TYPE_TEXT_ATTRIBUTE));
			templateTypeDescriptor.setLocation(templateTypeDescriptorElement.getAttribute(TEMPLATE_TYPE_LOCATION_ATTRIBUTE));
			templateTypeDescriptor.setImage(templateTypeDescriptorElement.getAttribute(TEMPLATE_TYPE_IMAGE_ATTRIBUTE));
			
			Set<String> parameters = new HashSet<String>(); 
			IConfigurationElement[] parameterElements = templateTypeDescriptorElement.getChildren(TEMPLATE_TYPE_PARAMETER_ATTRIBUTE);
			for (int i = 0; i < parameterElements.length; i++) {
				parameters.add(parameterElements[i].getAttribute(TEMPLATE_TYPE_VALUE_ATTRIBUTE));
			}
			
			templateTypeDescriptor.setParameters(parameters);
	//		templateTypeDescriptor.setPages(pages);
			
			return templateTypeDescriptor;
		}
		
		return null;
	}
	
	private TemplateType[] prepareTemplateTypes()
			throws MalformedURLException {
		List<TemplateTypeDescriptor> templateTypeDescriptors = getTemplateTypeDescriptors(getCategory());
		List<TemplateType> templateTypesList = new ArrayList<TemplateType>(); 
		for (TemplateTypeDescriptor templateTypeDescriptor : templateTypeDescriptors) {
			templateTypesList.add(TemplateType
						.createTemplateType(templateTypeDescriptor.getText(),
								templateTypeDescriptor.getLocation(),
								templateTypeDescriptor.getImage(),
								this.getClass(), templateTypeDescriptor.getParameters().toArray(new String[]{})));
		}
		
		TemplateType[] templateTypes = templateTypesList.toArray(new TemplateType[]{});
		return templateTypes;
	}

	protected abstract String getCategory();


}

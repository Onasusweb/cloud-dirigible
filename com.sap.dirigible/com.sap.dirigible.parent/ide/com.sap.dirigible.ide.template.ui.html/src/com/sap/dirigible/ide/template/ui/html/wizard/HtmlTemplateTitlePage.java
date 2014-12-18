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

package com.sap.dirigible.ide.template.ui.html.wizard;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;

import com.sap.dirigible.ide.workspace.ui.shared.FocusableWizardPage;

public class HtmlTemplateTitlePage extends FocusableWizardPage {

	private static final long serialVersionUID = -6815434141371322133L;

	private static final String INPUT_THE_PAGE_TITLE = Messages.HtmlTemplateTitlePage_INPUT_THE_PAGE_TITLE;

	private static final String PAGE_TITLE = Messages.HtmlTemplateTitlePage_PAGE_TITLE;

	private static final String SET_THE_PAGE_TITLE_WHICH_WILL_BE_USED_DURING_THE_GENERATION = Messages.HtmlTemplateTitlePage_SET_THE_PAGE_TITLE_WHICH_WILL_BE_USED_DURING_THE_GENERATION;

	private static final String PAGE_NAME = "com.sap.dirigible.ide.template.ui.html.wizard.HtmlTemplateTitlePage"; //$NON-NLS-1$

	private HtmlTemplateModel model;

	private Text pageTitleText;

	protected HtmlTemplateTitlePage(HtmlTemplateModel model) {
		super(PAGE_NAME);
		this.model = model;
		setTitle(PAGE_TITLE);
		setDescription(SET_THE_PAGE_TITLE_WHICH_WILL_BE_USED_DURING_THE_GENERATION);
	}

	@Override
	public void createControl(Composite parent) {
		final Composite composite = new Composite(parent, SWT.NONE);
		setControl(composite);
		composite.setLayout(new GridLayout(2, false));

		createPageTitleField(composite);
		checkPageStatus();
	}

	private void createPageTitleField(Composite parent) {
		final Label label = new Label(parent, SWT.NONE);
		label.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, false, false));
		label.setText(PAGE_TITLE);

		pageTitleText = new Text(parent, SWT.BORDER);
		pageTitleText.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true,
				false));
		pageTitleText.addModifyListener(new ModifyListener() {

			/**
			 * 
			 */
			private static final long serialVersionUID = 379844491503377428L;

			@Override
			public void modifyText(ModifyEvent event) {
				if (pageTitleText.getText() == null
						|| "".equals(pageTitleText.getText())) { //$NON-NLS-1$
					setErrorMessage(INPUT_THE_PAGE_TITLE);
				} else {
					setErrorMessage(null);
					model.setPageTitle(pageTitleText.getText());
				}
				checkPageStatus();
			}
		});
		setFocusable(pageTitleText);
	}

	private void checkPageStatus() {
		if (model.getPageTitle() == null || "".equals(model.getPageTitle())) { //$NON-NLS-1$
			setPageComplete(false);
			return;
		}
		setPageComplete(true);
	}

}

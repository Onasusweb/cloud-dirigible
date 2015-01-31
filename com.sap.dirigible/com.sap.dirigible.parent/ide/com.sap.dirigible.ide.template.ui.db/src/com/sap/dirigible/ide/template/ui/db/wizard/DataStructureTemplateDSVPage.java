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

package com.sap.dirigible.ide.template.ui.db.wizard;

import java.util.Random;

import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;

import com.sap.dirigible.ide.db.export.DataExportDialog;
import com.sap.dirigible.ide.db.export.DataFinder;
import com.sap.dirigible.ide.db.export.TableColumn;
import com.sap.dirigible.ide.db.export.TableName;
import com.sap.dirigible.repository.ext.db.DBSupportedTypesMap;

public class DataStructureTemplateDSVPage extends WizardPage {
	
	private static final long serialVersionUID = 7697608637259213988L;
	
	private static final String AVAILABLE_TABLES = Messages.DataStructureTemplateDSVPage_0;

	private static final String DSV = Messages.DataStructureTemplateDSVPage_1;

	private static final String PAGE_NAME = "com.sap.dirigible.ide.template.ui.db.wizard.DataStructureTemplateDSVPage"; //$NON-NLS-1$

	private static final String NO_TABLE_IS_SELECTED_PLEASE_SELECT_ONE = Messages.DataStructureTemplateDSVPage_2;

	private static final String GENERATE_DSV_SAMPLE_BASED_ON_TABLE = Messages.DataStructureTemplateDSVPage_3;

	private DataStructureTemplateModel model;
	
	private Label labelSelected;

	protected DataStructureTemplateDSVPage(DataStructureTemplateModel model) {
		super(PAGE_NAME);
		this.model = model;
		setTitle(DSV);
		setDescription(GENERATE_DSV_SAMPLE_BASED_ON_TABLE);
	}

	@Override
	public void createControl(Composite parent) {
		final Composite composite = new Composite(parent, SWT.NONE);
		setControl(composite);
		composite.setLayout(new GridLayout(1, false));

		createTablesList(composite);
		checkPageStatus();
	}

	private void createTablesList(Composite parent) {
		final Label label = new Label(parent, SWT.NONE);
		label.setText(AVAILABLE_TABLES);
		label.setLayoutData(new GridData(SWT.LEFT, SWT.BOTTOM, false, false));
		
		final TableViewer typeViewer = DataExportDialog.createTableList(parent);
		typeViewer.addSelectionChangedListener(new ISelectionChangedListener() {

			@Override
			public void selectionChanged(SelectionChangedEvent event) {
//				int selectionIndex = typeViewer.getTable().getSelectionIndex();
//				TableName[] tables = (TableName[]) typeViewer.getInput();
//				if (selectionIndex >= 0) {
//
//					final String selectedTable = tables[selectionIndex].getName();
//					model.setTableName(selectedTable);
//
//					DataFinder dataFinder = new DataFinder();
//					dataFinder.setTableName(selectedTable);
//					dataFinder.getTableData();
//
//					model.setDsvSampleRows(generateDsvSamplesRows(dataFinder.getTableColumns()));
//				}
//				
				
				
				if (typeViewer.getTable().getSelection() != null
						&& typeViewer.getTable().getSelection().length > 0) {
					TableName selectedTableName = (TableName) typeViewer.getTable().getSelection()[0].getData();
					if (selectedTableName != null) {
						model.setTableName(selectedTableName.getName());
						
						DataFinder dataFinder = new DataFinder();
						dataFinder.setTableName(selectedTableName.getName());
						dataFinder.getTableData();

						model.setDsvSampleRows(generateDsvSamplesRows(dataFinder.getTableColumns()));
						
						labelSelected.setText(selectedTableName.getName());
						labelSelected.pack();
					} else {
						model.setTableName(null);
						labelSelected.setText("");
						labelSelected.pack();
					}
				} else {
					model.setTableName(null);
				}
				
				checkPageStatus();
			}

			private String[] generateDsvSamplesRows(TableColumn[] tableColumns) {
				final String rowDelimiter = ";"; //$NON-NLS-1$
				final String dsvDelimiter = "|"; //$NON-NLS-1$
				StringBuilder dsvSample = new StringBuilder();
				int columnsCount = tableColumns.length;
				for (int i = 0; i < 3; i++) {
					for (int columnIndex = 0; columnIndex < columnsCount; columnIndex++) {
						TableColumn column = tableColumns[columnIndex];
						String sampleValue = getSampleValue(column);
						dsvSample.append(sampleValue);
						if (columnIndex < columnsCount - 1) {
							dsvSample.append(dsvDelimiter);
						}
					}
					dsvSample.append(rowDelimiter);
				}
				return dsvSample.toString().split(rowDelimiter);
			}

			private String getSampleValue(TableColumn column) {
				String type = DBSupportedTypesMap.getTypeName(column.getType());
				boolean numeric = type.equals(DBSupportedTypesMap.BIGINT)
						|| type.equals(DBSupportedTypesMap.SMALLINT)
						|| type.equals(DBSupportedTypesMap.BINARY)
						|| type.equals(DBSupportedTypesMap.BIT)
						|| type.equals(DBSupportedTypesMap.INTEGER)
						|| type.equals(DBSupportedTypesMap.NUMERIC)
						|| type.equals(DBSupportedTypesMap.TINYINT);
				boolean blob = type.equals(DBSupportedTypesMap.BLOB);
				boolean clob = type.equals(DBSupportedTypesMap.CLOB);
				boolean booleanType = type.equals(DBSupportedTypesMap.BOOLEAN);
				boolean textChar = type.equals(DBSupportedTypesMap.CHAR);
				boolean textVarchar = type.equals(DBSupportedTypesMap.NVARCHAR)
						|| type.equals(DBSupportedTypesMap.VARCHAR);
				boolean date = type.equals(DBSupportedTypesMap.DATE);

				boolean floatingPoint = type.equals(DBSupportedTypesMap.REAL)
						|| type.equals(DBSupportedTypesMap.DECIMAL)
						|| type.equals(DBSupportedTypesMap.DOUBLE)
						|| type.equals(DBSupportedTypesMap.FLOAT);
				boolean time = type.equals(DBSupportedTypesMap.TIME);
				boolean timeStamp = type.equals(DBSupportedTypesMap.TIMESTAMP);

				String value = null;
				Random rand = new Random();
				if (numeric) {
					value = Integer.toString(rand.nextInt(100) + 1);
				} else if (blob) {
					// TODO default value for BLOB
				} else if (clob) {
					// TODO default value for CLOB
				} else if (booleanType) {
					// TODO default value for Boolean
				} else if (time) {
					value = "10:30:45"; //$NON-NLS-1$
				} else if (timeStamp) {
					value = "2014-02-19 10:30:45"; //$NON-NLS-1$
				} else if (textChar) {
					value = "J"; //$NON-NLS-1$
				} else if (textVarchar) {
					value = "Test" + rand.nextInt(100); //$NON-NLS-1$
				} else if (date) {
					value = "2014-02-19"; //$NON-NLS-1$
				} else if (floatingPoint) {
					value = Float.toString(rand.nextFloat());
				} else {
					value = DBSupportedTypesMap.UNSUPPORTED_TYPE;
				}

				return value;
			}
		});
		labelSelected = new Label(parent, SWT.NONE);
		labelSelected.setText("");
		labelSelected.setLayoutData(new GridData(SWT.LEFT, SWT.BOTTOM, true, false));
	}

	private void checkPageStatus() {
		if (model.getTableName() == null || "".equals(model.getTableName())) { //$NON-NLS-1$
			setErrorMessage(NO_TABLE_IS_SELECTED_PLEASE_SELECT_ONE);
			setPageComplete(false);
			return;
		}
		setErrorMessage(null);
		setPageComplete(true);
	}

}

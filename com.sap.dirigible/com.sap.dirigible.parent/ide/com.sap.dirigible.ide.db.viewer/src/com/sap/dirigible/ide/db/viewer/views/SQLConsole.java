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

package com.sap.dirigible.ide.db.viewer.views;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.StringTokenizer;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IMenuListener;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.SashForm;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.Text;
import org.eclipse.swt.widgets.ToolBar;
import org.eclipse.swt.widgets.ToolItem;
import org.eclipse.ui.IWorkbenchActionConstants;
import org.eclipse.ui.part.ViewPart;

import com.sap.dirigible.ide.common.CommonParameters;
import com.sap.dirigible.ide.editor.js.EditorMode;
import com.sap.dirigible.ide.editor.js.EditorWidget;
import com.sap.dirigible.ide.logging.Logger;

public class SQLConsole extends ViewPart {

	private static final String EXECUTE_QUERY_STATEMENT = Messages.SQLConsole_EXECUTE_QUERY_STATEMENT;

	private static final String EXECUTE_QUERY = Messages.SQLConsole_EXECUTE_QUERY;

	private static final String EXECUTE_UPDATE_STATEMENT = Messages.SQLConsole_EXECUTE_UPDATE_STATEMENT;

	private static final String EXECUTE_UPDATE_TEXT = Messages.SQLConsole_EXECUTE_UPDATE_TEXT;

	private static final char SPACE = ' ';

	private static final char MINUS = '-';

	private static final String EMPTY = ""; //$NON-NLS-1$

	private static final String DOTS = "...\n"; //$NON-NLS-1$

	private static final String NULL = "NULL"; //$NON-NLS-1$

	private static final String ICON_EXECUTE_UPDATE_PNG = "icon-execute.png"; //$NON-NLS-1$

	private static final String ICON_EXECUTE_QUERY_PNG = "icon-execute.png"; //$NON-NLS-1$

	private static final String ICONS_SEGMENT = "/icons/"; //$NON-NLS-1$

	private static final String POPUP_MENU = "#PopupMenu"; //$NON-NLS-1$

	private static final String UPDATE_COUNT_S = Messages.SQLConsole_UPDATE_COUNT_S;

	private static final String EXECUTE_UPDATE = Messages.SQLConsole_EXECUTE_UPDATE;

	private static final String BINARY = "[BINARY]"; //$NON-NLS-1$

	private static final Logger logger = Logger.getLogger(SQLConsole.class);

	private static final String COLUMN_DELIMITER = "|"; //$NON-NLS-1$

	private static final String END_DELIMITER = "|\n"; //$NON-NLS-1$

	/**
	 * The ID of the view as specified by the extension.
	 */
	public static final String ID = "com.sap.dirigible.ide.db.viewer.views.SQLConsole"; //$NON-NLS-1$

	private EditorWidget scriptArea = null;
	private Text outputArea = null;

	private Action actionExecuteUpdate;
	private Action actionExecuteQuery;

	public static final String SCRIPT_DELIMITER = ";"; //$NON-NLS-1$

	public SQLConsole() {
	}

	@SuppressWarnings("unused")
	@Override
	public void createPartControl(Composite parent) {
		parent.setLayout(new GridLayout());

		ToolBar toolBar = new ToolBar(parent, SWT.FLAT | SWT.WRAP | SWT.RIGHT | SWT.BORDER
				| SWT.SHADOW_OUT);

		SashForm sashForm = new SashForm(parent, SWT.VERTICAL | SWT.BORDER);
		sashForm.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		scriptArea = new EditorWidget(sashForm);
		scriptArea.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		scriptArea.setText(EMPTY, getMode(), false, false, 0);

		outputArea = new Text(sashForm, SWT.MULTI | SWT.H_SCROLL | SWT.V_SCROLL | SWT.READ_ONLY);
		outputArea.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		outputArea.setText(EMPTY);
		outputArea.setFont(new Font(null, "Courier New", 12, SWT.NORMAL)); //$NON-NLS-1$

		ToolItem itemQuery = new ToolItem(toolBar, SWT.PUSH | SWT.SEPARATOR);
		itemQuery.setText(EXECUTE_QUERY);
		Image iconQuery = ImageDescriptor.createFromURL(
				SQLConsole.class.getResource(ICONS_SEGMENT + ICON_EXECUTE_QUERY_PNG)).createImage(); //$NON-NLS-1$
		itemQuery.setImage(iconQuery);
		itemQuery.addSelectionListener(new SelectionListener() {
			private static final long serialVersionUID = 1281159157504712273L;

			@Override
			public void widgetSelected(SelectionEvent e) {
				actionExecuteQuery.run();
			}

			@Override
			public void widgetDefaultSelected(SelectionEvent e) {
				//
			}
		});

		boolean isOperator = CommonParameters.isUserInRole(CommonParameters.ROLE_OPERATOR);
		if (isOperator) {
			new ToolItem(toolBar, SWT.SEPARATOR);
			ToolItem itemUpdate = new ToolItem(toolBar, SWT.PUSH);
			itemUpdate.setText(EXECUTE_UPDATE);
			Image iconUpdate = ImageDescriptor.createFromURL(
					SQLConsole.class.getResource(ICONS_SEGMENT + ICON_EXECUTE_UPDATE_PNG))
					.createImage(); //$NON-NLS-1$
			itemUpdate.setImage(iconUpdate);
			itemUpdate.addSelectionListener(new SelectionListener() {
				private static final long serialVersionUID = 1281159157504712273L;

				@Override
				public void widgetSelected(SelectionEvent e) {
					actionExecuteUpdate.run();
				}

				@Override
				public void widgetDefaultSelected(SelectionEvent e) {
					//
				}
			});
		}
		// Create the help context id for the viewer's control
		// PlatformUI.getWorkbench().getHelpSystem().setHelp(scriptArea,
		// "com.sap.dirigible.ide.db.viewer.views.SQLConsole");

		makeActions();
		hookContextMenu();
		// hookDoubleClickAction();
		// contributeToActionBars();

	}

	@Override
	public void setFocus() {
		scriptArea.setFocus();
	}

	private void makeActions() {
		actionExecuteQuery = new Action() {
			private static final long serialVersionUID = -4666336820729503841L;

			public void run() {
				executeStatement(true);
			}
		};
		actionExecuteQuery.setText(EXECUTE_QUERY);
		actionExecuteQuery.setToolTipText(EXECUTE_QUERY_STATEMENT);

		actionExecuteUpdate = new Action() {
			private static final long serialVersionUID = -4666336820729503841L;

			public void run() {
				executeStatement(false);
			}
		};
		actionExecuteUpdate.setText(EXECUTE_UPDATE_TEXT);
		actionExecuteUpdate.setToolTipText(EXECUTE_UPDATE_STATEMENT);
	}

	public void executeStatement(boolean isQuery) {

		String sql = scriptArea.getText();
		if (sql == null || sql.length() == 0) {
			return;
		}

		StringTokenizer tokenizer = new StringTokenizer(sql, SCRIPT_DELIMITER);
		while (tokenizer.hasMoreTokens()) {
			String line = tokenizer.nextToken();
			if (EMPTY.equals(line.trim())) {
				continue;
			}
			executeSingleStatement(line, isQuery);
		}

	}

	private void executeSingleStatement(String sql, boolean isQuery) {

		try {
			Connection connection = getConnection();
			try {

				PreparedStatement preparedStatement = connection.prepareStatement(sql);

				if (isQuery) {
					preparedStatement.executeQuery();
					ResultSet resultSet = preparedStatement.getResultSet();
					printResultSet(resultSet);
				} else {
					preparedStatement.executeUpdate();
					printUpdateCount(preparedStatement.getUpdateCount());
				}
			} finally {
				if (connection != null) {
					connection.close();
				}
			}
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
			outputArea.setText(e.getMessage());
		}
	}

	public Connection getConnection() throws Exception {
		return DatabaseViewer.getConnectionFromSelectedDatasource();
	}

	private void printResultSet(ResultSet resultSet) throws SQLException {
		StringBuffer buff = new StringBuffer();

		// header
		int headerLength = 0;
		ResultSetMetaData resultSetMetaData = resultSet.getMetaData();
		for (int i = 1; i <= resultSetMetaData.getColumnCount(); i++) {
			String columnLabel = resultSetMetaData.getColumnLabel(i);
			String columnLabelPrint = null;
			int columnType = resultSetMetaData.getColumnType(i);
			if (isBinaryType(columnType)) {
				columnLabelPrint = prepareStringForSize(columnLabel, BINARY.length(), SPACE);
			} else {
				columnLabelPrint = prepareStringForSize(columnLabel,
						resultSetMetaData.getColumnDisplaySize(i), SPACE);
			}
			buff.append(columnLabelPrint);
			headerLength += columnLabelPrint.length();
		}
		buff.append(END_DELIMITER);
		buff.append(prepareStringForSize(EMPTY, headerLength - 1, MINUS));
		buff.append(END_DELIMITER);

		// data
		int count = 0;
		while (resultSet.next()) {
			for (int i = 1; i <= resultSetMetaData.getColumnCount(); i++) {
				String data = null;
				int columnType = resultSetMetaData.getColumnType(i);
				if (isBinaryType(columnType)) {
					data = BINARY;
				} else {
					data = resultSet.getString(i);
				}
				String dataPrint = null;
				if (isBinaryType(columnType)) {
					dataPrint = prepareStringForSize(data, BINARY.length(), ' ');
				} else {
					dataPrint = prepareStringForSize(data,
							resultSetMetaData.getColumnDisplaySize(i), ' ');
				}
				buff.append(dataPrint);
			}
			buff.append(END_DELIMITER);
			if (++count > 100) {
				buff.append(DOTS);
				break;
			}
		}

		outputArea.setText(buff.toString());
	}

	private boolean isBinaryType(int columnType) {
		for (int c : CommonParameters.BINARY_TYPES) {
			if (columnType == c) {
				return true;
			}
		}
		return false;
	}

	private String prepareStringForSize(String columnLabel, int columnDisplaySize, char c) {
		String result;
		if (columnLabel == null) {
			columnLabel = NULL;
		}
		if (columnLabel.length() == columnDisplaySize) {
			result = columnLabel;
		} else if (columnLabel.length() > columnDisplaySize) {
			result = columnLabel.substring(0, columnDisplaySize);
		} else {
			StringBuffer buff = new StringBuffer();
			buff.append(columnLabel);
			for (int i = 0; i < columnDisplaySize - columnLabel.length(); i++) {
				buff.append(c);
			}
			result = buff.toString();
		}

		return COLUMN_DELIMITER + result;
	}

	private void printUpdateCount(int updateCount) {
		outputArea.setText(String.format(UPDATE_COUNT_S, updateCount));
	}

	private void hookContextMenu() {
		MenuManager menuMgr = new MenuManager(POPUP_MENU);
		menuMgr.setRemoveAllWhenShown(true);
		menuMgr.addMenuListener(new IMenuListener() {
			private static final long serialVersionUID = 7417283863427269417L;

			public void menuAboutToShow(IMenuManager manager) {
				SQLConsole.this.fillContextMenu(manager);
			}
		});
		Menu menu = menuMgr.createContextMenu(scriptArea);
		scriptArea.setMenu(menu);
	}

	private void fillContextMenu(IMenuManager manager) {
		manager.add(actionExecuteUpdate);
		manager.add(new Separator());
		// Other plug-ins can contribute there actions here
		manager.add(new Separator(IWorkbenchActionConstants.MB_ADDITIONS));
	}

	public void setQuery(String query) {
		scriptArea.setText(query, getMode(), false, false, 0);
	}

	private EditorMode getMode() {
		return EditorMode.SQL;
	}
}

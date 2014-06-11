package com.sap.dirigible.ide.db.preferences;

import org.eclipse.jface.preference.FieldEditorPreferencePage;
import org.eclipse.jface.preference.StringFieldEditor;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;

import com.sap.dirigible.ide.common.CommonParameters;

public class DatabaseAttributesPreferencePage extends FieldEditorPreferencePage implements IWorkbenchPreferencePage {

	private static final long serialVersionUID = -877187045002896492L;

	public DatabaseAttributesPreferencePage() {
		super(FieldEditorPreferencePage.GRID);
	}
	
	@Override
	protected void createFieldEditors() {
		Text text = null;
		
		StringFieldEditor databaseProductNameField = new StringFieldEditor(
				CommonParameters.DATABASE_PRODUCT_NAME,
				"&Product Name:",
		 		getFieldEditorParent());
		text = databaseProductNameField.getTextControl(getFieldEditorParent());
		text.setEditable(false);
		text.setText(CommonParameters.getDatabaseProductName() != null ? CommonParameters.getDatabaseProductName() : DatabasePreferencePage.N_A);
		addField(databaseProductNameField);
		
		StringFieldEditor databaseProductVersionField = new StringFieldEditor(
				CommonParameters.DATABASE_PRODUCT_VERSION,
				"&Product Version:",
		 		getFieldEditorParent());
		text = databaseProductVersionField.getTextControl(getFieldEditorParent());
		text.setEditable(false);
		text.setText(CommonParameters.getDatabaseProductVersion() != null ? CommonParameters.getDatabaseProductVersion() : DatabasePreferencePage.N_A);
		addField(databaseProductVersionField);
		
		StringFieldEditor databaseMinorVersionField = new StringFieldEditor(
				CommonParameters.DATABASE_MINOR_VERSION,
				"&Minor Version:",
		 		getFieldEditorParent());
		text = databaseMinorVersionField.getTextControl(getFieldEditorParent());
		text.setEditable(false);
		text.setText(CommonParameters.get(CommonParameters.DATABASE_MINOR_VERSION) != null ? CommonParameters.get(CommonParameters.DATABASE_MINOR_VERSION) : DatabasePreferencePage.N_A);
		addField(databaseMinorVersionField);
		
		StringFieldEditor databaseMajorVersionField = new StringFieldEditor(
				CommonParameters.DATABASE_MAJOR_VERSION,
				"&Major Version:",
		 		getFieldEditorParent());
		text = databaseMajorVersionField.getTextControl(getFieldEditorParent());
		text.setEditable(false);
		text.setText(CommonParameters.get(CommonParameters.DATABASE_MAJOR_VERSION) != null ? CommonParameters.get(CommonParameters.DATABASE_MAJOR_VERSION) : DatabasePreferencePage.N_A);
		addField(databaseMajorVersionField);

	}

	@Override
	public void init(IWorkbench workbench) {
		super.initialize();
	}

}

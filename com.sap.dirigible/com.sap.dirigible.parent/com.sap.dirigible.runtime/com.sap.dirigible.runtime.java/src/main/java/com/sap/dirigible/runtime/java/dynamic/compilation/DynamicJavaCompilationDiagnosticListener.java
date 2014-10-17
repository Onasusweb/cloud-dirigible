package com.sap.dirigible.runtime.java.dynamic.compilation;

import javax.tools.Diagnostic;
import javax.tools.DiagnosticListener;
import javax.tools.JavaFileObject;

public class DynamicJavaCompilationDiagnosticListener implements DiagnosticListener<JavaFileObject> {

	private static final String NEW_LINE = "\n"; //$NON-NLS-1$
	private static final String COLUMN = "Column: ";
	private static final String LINE = "Line: ";
	private StringBuilder errors;
	private StringBuilder warnings;

	public DynamicJavaCompilationDiagnosticListener() {
		this.errors = new StringBuilder();
		this.warnings = new StringBuilder();

	}

	public String getErrors() {
		return errors.toString();
	}

	public String getWarnings() {
		return warnings.toString();
	}

	@Override
	public void report(Diagnostic<? extends JavaFileObject> diagnostic) {
		switch (diagnostic.getKind()) {
		case ERROR:
			recordError(diagnostic);
		default:
			recordWarning(diagnostic);
		}
	}

	private void recordError(Diagnostic<? extends JavaFileObject> diagnostic) {
		errors.append(getMessage(diagnostic));
	}

	private void recordWarning(Diagnostic<? extends JavaFileObject> diagnostic) {
		warnings.append(getMessage(diagnostic));
	}

	private String getMessage(Diagnostic<? extends JavaFileObject> diagnostic) {
		StringBuilder message = new StringBuilder();
		message.append(NEW_LINE + diagnostic.getKind() + NEW_LINE);
		JavaFileObject javaFileObject = diagnostic.getSource();
		if (javaFileObject != null) {
			message.append(javaFileObject.getName() + NEW_LINE);
		}
		message.append(diagnostic.getMessage(null) + NEW_LINE);
		message.append(LINE + diagnostic.getLineNumber() + NEW_LINE);
		message.append(COLUMN + diagnostic.getColumnNumber() + NEW_LINE);
		return message.toString();
	}

}

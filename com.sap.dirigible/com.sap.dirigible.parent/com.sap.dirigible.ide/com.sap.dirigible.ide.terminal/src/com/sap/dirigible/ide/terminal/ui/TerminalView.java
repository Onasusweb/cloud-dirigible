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

package com.sap.dirigible.ide.terminal.ui;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.KeyListener;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.part.ViewPart;

import com.sap.dirigible.ide.logging.Logger;
import com.sap.dirigible.repository.ext.command.Piper;
import com.sap.dirigible.repository.ext.command.ProcessUtils;

public class TerminalView extends ViewPart {
	
	private static final Logger logger = Logger.getLogger(TerminalView.class);

	private Text commandLine;
	
	private Text commandHistory;

	public TerminalView() {
		super();
	}

	@Override
	public void createPartControl(Composite parent) {
		GridLayout layout = new GridLayout(1, false);
		
		parent.setLayout(layout);
		parent.setBackground(new Color(null, 0,0,0));
		parent.setForeground(new Color(null, 0,255,0));

		Font terminalFont = JFaceResources.getFont(JFaceResources.TEXT_FONT);
		
	    commandHistory = new Text(parent, SWT.MULTI | SWT.BORDER | SWT.H_SCROLL | SWT.V_SCROLL | SWT.READ_ONLY);
	    commandHistory.setLayoutData(new GridData(GridData.FILL_BOTH));
	    commandHistory.setBackground(new Color(null, 0,0,0));
	    commandHistory.setForeground(new Color(null, 0,255,0));
	    commandHistory.setFont(terminalFont);

	    commandLine = new Text(parent, SWT.BORDER);
	    commandLine.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
	    commandLine.setBackground(new Color(null, 0,0,0));
	    commandLine.setForeground(new Color(null, 0,255,0));
	    commandLine.setFont(terminalFont);
	    commandLine.addKeyListener(new KeyListener() {
			
			@Override
			public void keyReleased(KeyEvent e) {
				//
			}
			
			@Override
			public void keyPressed(KeyEvent e) {
				if (e.keyCode == SWT.CR) {
					//MessageDialog.openInformation(null, "Info", commandLine.getText());
					
					try {
						String result = executeCommand(ProcessUtils.translateCommandline(commandLine.getText()));
						commandHistory.setText(result);
					} catch (IOException ex) {
						commandHistory.setText(ex.getMessage());
					}
				}
				
			}
		});

	}

	@Override
	public void setFocus() {
		commandLine.setFocus();
	}

	private static String executeCommand(String[] args) throws IOException {
		if (args.length <= 0) {
			return "Need command to run";
		}
		
		IPreferenceStore preferenceStore = TerminalPreferencePage.getTerminalPreferenceStore();
		boolean limitEnabled = preferenceStore.getBoolean(TerminalPreferencePage.LIMIT_ENABLED);
		int limitTimeout = preferenceStore.getInt(TerminalPreferencePage.LIMIT_TIMEOUT);

		ProcessBuilder processBuilder = ProcessUtils.createProcess(args);
		ProcessUtils.addEnvironmentVariables(processBuilder, null);
		ProcessUtils.removeEnvironmentVariables(processBuilder, null);
		//processBuilder.directory(new File(workingDirectory));
		processBuilder.redirectErrorStream(true);
		
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		Process process = ProcessUtils.startProcess(args, processBuilder);
		Piper pipe = new Piper(process.getInputStream(), out);
        new Thread(pipe).start();
        try {
			//process.waitFor();
        	
        	int i=0;
            boolean deadYet = false;
            do {
                Thread.sleep(ProcessUtils.DEFAULT_WAIT_TIME);
                try {
                    process.exitValue();
                    deadYet = true;
                } catch (IllegalThreadStateException e) {
                	if (limitEnabled) {
	                    if (++i >= limitTimeout) {
	                    	process.destroy();
	                    	throw new RuntimeException("Exeeds timeout - " + ((ProcessUtils.DEFAULT_WAIT_TIME/1000) * ProcessUtils.DEFAULT_LOOP_COUNT));
	                    }
                	}
                }
            } while (!deadYet);
            
		} catch (Exception e) {
			logger.error(e.getMessage());
			return e.getMessage();
		}
		return new String(out.toByteArray());
	}
	
}

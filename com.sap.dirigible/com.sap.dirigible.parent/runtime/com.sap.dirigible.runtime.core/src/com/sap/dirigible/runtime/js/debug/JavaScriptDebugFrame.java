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

package com.sap.dirigible.runtime.js.debug;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.Stack;
import java.util.UUID;

import javax.servlet.http.HttpServletRequest;

import org.mozilla.javascript.BaseFunction;
import org.mozilla.javascript.Context;
import org.mozilla.javascript.NativeJavaObject;
import org.mozilla.javascript.Scriptable;
import org.mozilla.javascript.ScriptableObject;
import org.mozilla.javascript.Undefined;
import org.mozilla.javascript.debug.DebugFrame;
import org.mozilla.javascript.debug.DebuggableScript;

import com.google.gson.Gson;
import com.sap.dirigible.repository.ext.debug.BreakpointMetadata;
import com.sap.dirigible.repository.ext.debug.BreakpointsMetadata;
import com.sap.dirigible.repository.ext.debug.DebugConstants;
import com.sap.dirigible.repository.ext.debug.DebugSessionMetadata;
import com.sap.dirigible.repository.ext.debug.IDebugProtocol;
import com.sap.dirigible.repository.ext.debug.VariableValue;
import com.sap.dirigible.repository.ext.debug.VariableValuesMetadata;
import com.sap.dirigible.runtime.js.debug.IDebugCommands.DebugCommand;
import com.sap.dirigible.runtime.logger.Logger;

public class JavaScriptDebugFrame implements DebugFrame, PropertyChangeListener {
	private static final String NULL = "null";

	private static final String NATIVE = "native";

	private static final String FUNCTION = "function";

	private static final String UNDEFINED = "undefined";

	private static final Logger logger = Logger.getLogger(JavaScriptDebugFrame.class);

	private static final int SLEEP_TIME = 50;
	private DebuggerActionManager debuggerActionManager;
	private DebuggerActionCommander debuggerActionCommander;
	private Stack<DebuggableScript> scriptStack;
	private Stack<Scriptable> activationStack;
	private int stepOverLineNumber = 0;
	private int previousLineNumber = 0;
	private boolean stepOverFinished = true;
	private IDebugProtocol debugProtocol;
	private VariableValuesMetadata variableValuesMetadata;

	public JavaScriptDebugFrame(IDebugProtocol debugProtocol, HttpServletRequest request,
			JavaScriptDebugger javaScriptDebugger) {
		// get the instance of debugger action manager from the session

		logDebug("entering JavaScriptDebugFrame.constructor");

		this.debuggerActionManager = DebuggerActionManager.getInstance(request.getSession(true));

		// create a new instance of commander per frame
		String executionId = UUID.randomUUID().toString();
		String userId = request.getRemoteUser();
		this.debuggerActionCommander = new DebuggerActionCommander(this.debuggerActionManager,
				executionId, userId);

		this.debuggerActionCommander.init();
		this.debuggerActionCommander.setDebugFrame(this);
		this.debuggerActionCommander.setDebugger(javaScriptDebugger);

		this.debugProtocol = debugProtocol;
		this.debugProtocol.addPropertyChangeListener(this);

		this.scriptStack = new Stack<DebuggableScript>();
		this.activationStack = new Stack<Scriptable>();

		registerDebugFrame();

		logDebug("exiting JavaScriptDebugFrame.constructor");
	}

	private void registerDebugFrame() {
		logDebug("entering JavaScriptDebugFrame.registerDebugFrame");
		String commandBody = new Gson().toJson(new DebugSessionMetadata(
				getDebuggerActionCommander().getSessionId(), getDebuggerActionCommander()
						.getExecutionId(), getDebuggerActionCommander().getUserId()));
		send(DebugConstants.VIEW_REGISTER, commandBody);
		logDebug("exiting JavaScriptDebugFrame.registerDebugFrame");
	}

	@Override
	public void onEnter(Context context, Scriptable activation, Scriptable thisObj, Object[] args) {
		logDebug("entering JavaScriptDebugFrame.onEnter()");
		DebuggableScript script = (DebuggableScript) context.getDebuggerContextData();
		scriptStack.push(script);
		activationStack.push(activation);
		logDebug("exiting JavaScriptDebugFrame.onEnter()");
	}

	@Override
	public void onLineChange(Context context, int lineNumber) {
		blockExecution();
		processAction(lineNumber, getNextCommand());
	}

	@Override
	public void onExceptionThrown(Context context, Throwable ex) {
		logError("[debugger] onExceptionThrown()");
	}

	@Override
	public void onExit(Context context, boolean byThrow, Object resultOrException) {
		logDebug("entering JavaScriptDebugFrame.onExit()");
		scriptStack.pop();
		activationStack.pop();
		if (scriptStack.isEmpty()) {
			this.debugProtocol.removePropertyChangeListener(this);
			this.debuggerActionCommander.clean();
			DebuggerActionCommander commander = getDebuggerActionCommander();
			DebugSessionMetadata metadata = new DebugSessionMetadata(commander.getSessionId(),
					commander.getExecutionId(), commander.getUserId());
			String json = new Gson().toJson(metadata);
			send(DebugConstants.VIEW_FINISH, json);
		}
		logDebug("exiting JavaScriptDebugFrame.onExit()");
	}

	@Override
	public void onDebuggerStatement(Context context) {
		print(-1);
		if (debuggerActionCommander.isExecuting()) {
			debuggerActionCommander.pauseExecution();
		}
	}

	private void processAction(int lineNumber, DebugCommand nextCommand) {
		if (nextCommand != DebugCommand.SKIP_ALL_BREAKPOINTS) {
			if (isBreakpoint(lineNumber)) {
				hitBreakpoint(lineNumber);
			} else {
				switch (nextCommand) {
				case CONTINUE:
					break;
				case STEPINTO:
					stepInto(lineNumber);
					break;
				case STEPOVER:
					stepOver(lineNumber);
					break;
				case SKIP_ALL_BREAKPOINTS:
					break;
				default:
					break;
				}
			}
			previousLineNumber = lineNumber;
		}
	}

	private void hitBreakpoint(int lineNumber) {
		logDebug("entering JavaScriptDebugFrame.hitBreakpoint(): " + lineNumber);
		print(lineNumber);
		debuggerActionCommander.stepOver();
		debuggerActionCommander.pauseExecution();
		logDebug("exiting JavaScriptDebugFrame.hitBreakpoint()");
	}

	private void stepInto(int lineNumber) {
		logDebug("entering JavaScriptDebugFrame.stepInto(): " + lineNumber);
		print(lineNumber);
		debuggerActionCommander.pauseExecution();
		logDebug("exiting JavaScriptDebugFrame.stepInto()");
	}

	private void stepOver(int lineNumber) {
		logDebug("entering JavaScriptDebugFrame.stepOver(): " + lineNumber);
		if (stepOverFinished) {
			stepOverFinished = false;
			stepOverLineNumber = previousLineNumber;
		}
		if (stepOverLineNumber + 1 == lineNumber) {
			stepOverFinished = true;
			stepOverLineNumber = -1;
			stepInto(lineNumber);
		}
		logDebug("entering JavaScriptDebugFrame.stepOver()");
	}

	private void blockExecution() {
		logDebug("entering JavaScriptDebugFrame.blockExecution()");
		while (!debuggerActionCommander.isExecuting() && getNextCommand() != DebugCommand.CONTINUE
				&& getNextCommand() != DebugCommand.SKIP_ALL_BREAKPOINTS) {
			try {
				Thread.sleep(SLEEP_TIME);
			} catch (InterruptedException e) {
				// Ignore
			}
		}
		logDebug("exiting JavaScriptDebugFrame.blockExecution()");
	}

	private DebugCommand getNextCommand() {
		return debuggerActionCommander.getCommand();
	}

	private boolean isBreakpoint(int row) {
		String path = scriptStack.peek().getSourceName();
		DebuggerActionCommander commander = getDebuggerActionCommander();
		BreakpointMetadata breakpoint = new BreakpointMetadata(commander.getSessionId(),
				commander.getExecutionId(), commander.getUserId(), path, row);
		Set<BreakpointMetadata> breakpoints = debuggerActionCommander.getBreakpoints();
		return breakpoints.contains(breakpoint);
	}

	private void print(int row) {
		DebuggerActionCommander commander = getDebuggerActionCommander();
		DebuggableScript script = scriptStack.peek();
		Scriptable activation = activationStack.peek();
		List<VariableValue> variableValuesList = new ArrayList<VariableValue>();
		for (int i = 0; i < script.getParamAndVarCount(); i++) {
			String variable = script.getParamOrVarName(i);
			Object value = activation.get(variable, activation);

			if (variable != null && value != null) {
				String valueContent = parseValueToString(value);
				variableValuesList.add(new VariableValue(variable, valueContent));
			}
		}
		if (variableValuesMetadata == null) {
			variableValuesMetadata = new VariableValuesMetadata(commander.getSessionId(),
					commander.getExecutionId(), commander.getUserId(), variableValuesList);
		}
		variableValuesMetadata.setVariableValueList(variableValuesList);
		sendVariableValuesMetadata();
		sendOnBreakLineChange(script.getSourceName(), row);
	}

	private String parseValueToString(Object value) {
		String result = null;
		if (value instanceof Undefined) {
			result = UNDEFINED;
		} else if (value instanceof Boolean) {
			result = value.toString();
		} else if (value instanceof Number) {
			result = value.toString();
		} else if (value instanceof CharSequence) {
			result = value.toString();
		} else if (value instanceof BaseFunction) {
			result = FUNCTION;
		} else if (value instanceof NativeJavaObject) {
			result = NATIVE;
		} else if (value instanceof ScriptableObject) {
			try {
				result = new Gson().toJson(value);
			} catch (StackOverflowError error) {
				result = NATIVE;
			}
		} else {
			result = NULL;
		}
		return result;
	}

	@Override
	public void propertyChange(PropertyChangeEvent event) {
		String commandId = event.getPropertyName();
		String clientId = (String) event.getOldValue();
		String commandBody = (String) event.getNewValue();
		logDebug("JavaScriptDebugFrame propertyChange() command: " + commandId + ", clientId: "
				+ clientId + ", body: " + commandBody);

		if (clientId == null || !clientId.equals(getDebuggerActionCommander().getExecutionId())) {
			// skip as the command is not for the current frame
			return;
		}

		Gson gson = new Gson();
		if (commandId.startsWith(DebugConstants.DEBUG)) {
			if (commandId.equals(DebugConstants.DEBUG_REFRESH)) {
				sendBreakpointsMetadata();
				sendVariableValuesMetadata();
			} else if (commandId.equals(DebugConstants.DEBUG_STEP_INTO)) {
				debuggerActionCommander.stepInto();
				debuggerActionCommander.resumeExecution();
			} else if (commandId.equals(DebugConstants.DEBUG_STEP_OVER)) {
				debuggerActionCommander.stepOver();
				debuggerActionCommander.resumeExecution();
			} else if (commandId.equals(DebugConstants.DEBUG_CONTINUE)) {
				debuggerActionCommander.continueExecution();
				debuggerActionCommander.resumeExecution();
			} else if (commandId.equals(DebugConstants.DEBUG_SKIP_ALL_BREAKPOINTS)) {
				debuggerActionCommander.skipAllBreakpoints();
				debuggerActionCommander.resumeExecution();
			} else if (commandId.equals(DebugConstants.DEBUG_SET_BREAKPOINT)) {
				BreakpointMetadata breakpoint = gson
						.fromJson(commandBody, BreakpointMetadata.class);
				debuggerActionCommander.addBreakpoint(breakpoint);
				sendBreakpointsMetadata();
			} else if (commandId.equals(DebugConstants.DEBUG_CLEAR_BREAKPOINT)) {
				BreakpointMetadata breakpoint = gson
						.fromJson(commandBody, BreakpointMetadata.class);
				debuggerActionCommander.clearBreakpoint(breakpoint);
				sendBreakpointsMetadata();
			} else if (commandId.equals(DebugConstants.DEBUG_CLEAR_ALL_BREAKPOINTS)) {
				debuggerActionCommander.clearAllBreakpoints();
				sendBreakpointsMetadata();
			} else if (commandId.equals(DebugConstants.DEBUG_CLEAR_ALL_BREAKPOINTS_FOR_FILE)) {
				String path = commandBody;
				debuggerActionCommander.clearAllBreakpoints(path);
				sendBreakpointsMetadata();
			}
		}
	}

	private void sendVariableValuesMetadata() {
		if (variableValuesMetadata != null) {
			Gson gson = new Gson();
			String variableValuesJson = gson.toJson(variableValuesMetadata);
			send(DebugConstants.VIEW_VARIABLE_VALUES, variableValuesJson);
		}
	}

	private void sendOnBreakLineChange(String path, Integer row) {
		DebuggerActionCommander commander = getDebuggerActionCommander();
		BreakpointMetadata breakLine = new BreakpointMetadata(commander.getSessionId(),
				commander.getExecutionId(), commander.getUserId(), path, row);
		Gson gson = new Gson();
		String variableValuesJson = gson.toJson(breakLine);
		send(DebugConstants.VIEW_ON_LINE_CHANGE, variableValuesJson);
	}

	private void sendBreakpointsMetadata() {
		Set<BreakpointMetadata> breakpoints = debuggerActionCommander.getBreakpoints();
		DebuggerActionCommander commander = getDebuggerActionCommander();
		BreakpointsMetadata metadata = new BreakpointsMetadata(commander.getSessionId(),
				commander.getExecutionId(), commander.getUserId(), breakpoints);
		String json = new Gson().toJson(metadata);
		send(DebugConstants.VIEW_BREAKPOINT_METADATA, json);
	}

	public void send(String commandId, String commandBody) {
		logDebug("JavaScriptDebugFrame send() command: " + commandId + ", body: " + commandBody);
		DebugProtocolUtils.send(this.debugProtocol, commandId, getDebuggerActionCommander()
				.getExecutionId(), commandBody);
	}

	public DebuggerActionCommander getDebuggerActionCommander() {
		return debuggerActionCommander;
	}

	private void logError(String message) {
		logger.error(message);
	}

	private void logDebug(String message) {
		if (logger.isDebugEnabled()) {
			logger.debug(message);
		}
	}
}

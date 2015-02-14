package com.sap.dirigible.ide.publish.ui.command;

import java.util.Iterator;
import java.util.List;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IResourceChangeEvent;
import org.eclipse.core.resources.IResourceChangeListener;
import org.eclipse.core.resources.IResourceDelta;
import org.eclipse.core.resources.ISaveContext;
import org.eclipse.core.resources.ISaveParticipant;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.ui.PlatformUI;

import com.sap.dirigible.ide.publish.IPublisher;
import com.sap.dirigible.ide.publish.PublishException;
import com.sap.dirigible.ide.publish.PublishManager;
import com.sap.dirigible.ide.workspace.dual.WorkspaceLocator;
import com.sap.dirigible.ide.workspace.ui.view.WebViewerView;

public class AutoActivator implements //ISaveParticipant { 
	IResourceChangeListener {
	
	private static final String FAILED_TO_ACTIVATE_PROJECT = Messages.AutoActivateAction_FAILED_TO_ACTIVATE_PROJECT;
	private static final String FAILED_TO_ACTIVATE_FILE = Messages.AutoActivateAction_FAILED_TO_ACTIVATE_FILE;
	private static final String AUTO_ACTIVATION_FAILED = Messages.AutoActivateAction_AUTO_ACTIVATION_FAILED;

	
	public void registerListener() {
		WorkspaceLocator.getWorkspace().addResourceChangeListener(this);
//		try {
//			WorkspaceLocator.getWorkspace().addSaveParticipant("com.sap.dirigible.ide.publish.ui", this);
//		} catch (CoreException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
	}
	
	public void unregisterListener() {
		WorkspaceLocator.getWorkspace().removeResourceChangeListener(this);
//		WorkspaceLocator.getWorkspace().removeSaveParticipant("com.sap.dirigible.ide.publish.ui");
	}

	@Override
	public void resourceChanged(IResourceChangeEvent event) {
		
		if (!AutoActivateAction.isAutoActivateStrategy()) {
			return;
		}
		IResource delta = event.getResource();
		if (delta == null
				&& event.getDelta() != null) {
			 delta = locateResource(event);
		}
		if (delta != null) {
			if (delta instanceof IFile) {
				activateFile((IFile) delta);
			} else {
				if (delta.getProject() != null) {
					activate(delta.getProject());
				}
			}
		}
		
	}

	private IResource locateResource(IResourceChangeEvent event) {
		IResource resource = null;
		if (event.getDelta().getAffectedChildren().length > 0) {
			resource = locateResourceFromChild(event.getDelta().getAffectedChildren()[0]);
		}
		return resource;
	}
	
	private IResource locateResourceFromChild(IResourceDelta resourceDelta) {
		IResource resource = null;
		if (resourceDelta.getAffectedChildren().length > 0) {
			resource = locateResourceFromChild(resourceDelta.getAffectedChildren()[0]);
		} else {
			resource = resourceDelta.getResource();
		}
		return resource;
	}

	private void activate(IProject project) {
		try {
			PublishManager.activateProject(project);
		} catch (PublishException e) {
			MessageDialog.openError(
					PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell(),
					AUTO_ACTIVATION_FAILED, FAILED_TO_ACTIVATE_PROJECT + project.getName());
		}
		WebViewerView.refreshWebViewerViewIfVisible();
	}

	private void activateFile(IFile file) {
		try {
			final List<IPublisher> publishers = PublishManager.getPublishers();

			for (Iterator<IPublisher> iterator = publishers.iterator(); iterator.hasNext();) {
				IPublisher publisher = (IPublisher) iterator.next();
				if (publisher.isAutoActivationAllowed()) {
					publisher.activateFile(file);
				}
			}
		} catch (PublishException e) {
			MessageDialog.openError(
					PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell(),
					AUTO_ACTIVATION_FAILED, FAILED_TO_ACTIVATE_FILE + file.getName());
		}
		WebViewerView.refreshWebViewerViewIfVisible();
	}

//	@Override
//	public void doneSaving(ISaveContext saveContext) {
//		
//		if (!AutoActivateAction.isAutoActivateStrategy()) {
//			return;
//		}
//		
//		activate(saveContext.getProject());
//		
////		IResource delta = arg0.getResource();
////		if (delta != null) {
////			if (delta instanceof IFile) {
////				activateFile((IFile) delta);
////			} else {
////				activate(delta.getProject());
////			}
////		}
//		
//		
//	}
//
//	@Override
//	public void prepareToSave(ISaveContext arg0) throws CoreException {
//		// TODO Auto-generated method stub
//		
//	}
//
//	@Override
//	public void rollback(ISaveContext arg0) {
//		// TODO Auto-generated method stub
//		
//	}
//
//	@Override
//	public void saving(ISaveContext arg0) throws CoreException {
//		// TODO Auto-generated method stub
//		
//	}

}

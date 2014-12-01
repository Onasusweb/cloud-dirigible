package com.sap.dirigible.runtime.content;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

import javax.servlet.ServletException;

import org.apache.commons.io.IOUtils;

import com.sap.dirigible.repository.api.ContentTypeHelper;
import com.sap.dirigible.repository.api.ICollection;
import com.sap.dirigible.repository.api.IRepository;
import com.sap.dirigible.repository.api.IResource;
import com.sap.dirigible.runtime.logger.Logger;
import com.sap.dirigible.runtime.repository.RepositoryFacade;

public class ContentInitializer {
	
	private static final String INVALID_FILE_OR_FOLDER_LOCATION_DURING_CONTENT_INITIALIZATION = Messages
			.getString("ContentInitializerServlet.INVALID_FILE_OR_FOLDER_LOCATION_DURING_CONTENT_INITIALIZATION"); //$NON-NLS-1$
	private static final String CONTENT_INITIALIZATION_FAILED = Messages
			.getString("ContentInitializerServlet.CONTENT_INITIALIZATION_FAILED"); //$NON-NLS-1$
	private static final String COULD_NOT_INITIALIZE_REPOSITORY = Messages
			.getString("ContentInitializerServlet.COULD_NOT_INITIALIZE_REPOSITORY"); //$NON-NLS-1$
	
	private static final String REPOSITORY_ATTRIBUTE = "com.sap.dirigible.services.content.repository"; //$NON-NLS-1$

	private static final String PATH_REGISTY_ROOT_TARGET = "/db"; //$NON-NLS-1$

	private static final Logger logger = Logger.getLogger(ContentInitializer.class);

	private static final String SYSTEM_USER = "SYSTEM"; //$NON-NLS-1$

	
	public void initContent(File root) throws ServletException {
		logger.info("Content Servlet Init"); //$NON-NLS-1$
		initRepository();
		try {
			// 1. Import pre-delivered content
			checkAndImportRegistry(root, SYSTEM_USER);
			// 2. Post import actions
			ContentPostImportUpdater contentPostImportUpdater = new ContentPostImportUpdater(
					getRepository(SYSTEM_USER));
			contentPostImportUpdater.update();
		} catch (IOException e) {
			throw new ServletException(e);
		} catch (Exception e) {
			throw new ServletException(e);
		}
	}

	private IRepository initRepository() throws ServletException {
		try {
			IRepository repository = RepositoryFacade.getInstance().getRepository(null);
//			getServletContext().setAttribute(REPOSITORY_ATTRIBUTE, repository);
			return repository;
		} catch (Exception ex) {
			throw new ServletException(COULD_NOT_INITIALIZE_REPOSITORY, ex);
		}
	}

	private IRepository getRepository(String user) throws IOException {
		IRepository repository = null;
		
//		final IRepository repository = (IRepository) getServletContext().getAttribute(
//				REPOSITORY_ATTRIBUTE);
//		if (repository == null) {
			try {
				repository = initRepository();
			} catch (ServletException e) {
				throw new IOException(e);
			}
//		}
		return repository;
	}

	private void checkAndImportRegistry(File root, String user) throws IOException {
		logger.debug("root: " + root.getCanonicalPath().replace('\\', '/')); //$NON-NLS-1$
		if (root.exists() && root.isDirectory()) {
			File[] files = root.listFiles();
			if (files != null) {
				for (int i = 0; i < files.length; i++) {
					File folder = files[i];
					checkAndImportFileOrFolder(root.getCanonicalPath().length(), folder, user);
				}
			}
		} else {
			throw new IOException(CONTENT_INITIALIZATION_FAILED + root.getCanonicalPath());
		}
	}

	private void checkAndImportFileOrFolder(int rootLength, File fileOrFolder, String user)
			throws IOException {
		if (fileOrFolder.exists()) {
			if (fileOrFolder.isDirectory()) {
				String folderName = fileOrFolder.getCanonicalPath().substring(rootLength);
				folderName = folderName.replace('\\', '/');
				logger.debug(folderName + " source: " //$NON-NLS-1$
						+ fileOrFolder.getCanonicalPath().replace('\\', '/'));
				IRepository repository = getRepository(user);
				ICollection collection = repository.getCollection(PATH_REGISTY_ROOT_TARGET
						+ folderName);
				if (!collection.exists()) {
					collection.create();
					logger.info("Folder created from: " //$NON-NLS-1$
							+ fileOrFolder.getCanonicalPath().replace('\\', '/') + " to: " //$NON-NLS-1$
							+ collection.getPath());
				}
				File[] files = fileOrFolder.listFiles();
				if (files != null) {
					for (int i = 0; i < files.length; i++) {
						File folder = files[i];
						checkAndImportFileOrFolder(rootLength, folder, user);
					}
				}
			} else {
				String fileName = fileOrFolder.getCanonicalPath().substring(rootLength);
				fileName = fileName.replace('\\', '/');
				logger.debug(fileName + " source: " //$NON-NLS-1$
						+ fileOrFolder.getCanonicalPath());
				IRepository repository = getRepository(user);
				IResource resource = repository.getResource(PATH_REGISTY_ROOT_TARGET + fileName);
				if (resource.exists()) {
					resource.delete();
				}
				ByteArrayOutputStream baos = new ByteArrayOutputStream();
				IOUtils.copy(new FileInputStream(fileOrFolder), baos);
				String mimeType = null;
				String extension = ContentTypeHelper.getExtension(fileName);
				if ((mimeType = ContentTypeHelper.getContentType(extension)) != null) {
					repository.createResource(PATH_REGISTY_ROOT_TARGET + fileName,
							baos.toByteArray(), ContentTypeHelper.isBinary(mimeType), mimeType);
				} else {
					repository.createResource(PATH_REGISTY_ROOT_TARGET + fileName,
							baos.toByteArray());
				}
				logger.info("Resource initialized from: " //$NON-NLS-1$
						+ fileOrFolder.getCanonicalPath().replace('\\', '/')
						+ " to: " + resource.getPath()); //$NON-NLS-1$
				// logger.info(new String(baos.toByteArray()));
			}
		} else {
			throw new IOException(INVALID_FILE_OR_FOLDER_LOCATION_DURING_CONTENT_INITIALIZATION
					+ fileOrFolder.getCanonicalPath().replace('\\', '/'));
		}

	}


}

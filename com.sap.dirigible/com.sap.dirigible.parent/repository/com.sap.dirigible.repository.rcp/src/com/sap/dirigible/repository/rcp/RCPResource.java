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

package com.sap.dirigible.repository.rcp;

import static java.text.MessageFormat.format;

import java.io.IOException;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sap.dirigible.repository.api.IResource;
import com.sap.dirigible.repository.api.IResourceVersion;
import com.sap.dirigible.repository.api.RepositoryPath;

/**
 * The DB implementation of {@link IResource}
 * 
 */
public class RCPResource extends RCPEntity implements IResource {

	private static final String THERE_IS_NO_RESOURCE_AT_PATH_0 = Messages.getString("DBResource.THERE_IS_NO_RESOURCE_AT_PATH_0"); //$NON-NLS-1$
	private static final String COULD_NOT_UPDATE_DOCUMENT = Messages.getString("DBResource.COULD_NOT_UPDATE_DOCUMENT"); //$NON-NLS-1$
	private static final String COULD_NOT_READ_RESOURCE_CONTENT = Messages.getString("DBResource.COULD_NOT_READ_RESOURCE_CONTENT"); //$NON-NLS-1$
	private static final String NOT_IMPLEMENTED = Messages.getString("DBResource.NOT_IMPLEMENTED"); //$NON-NLS-1$
	private static final String COULD_NOT_DELETE_RESOURCE = Messages.getString("DBResource.COULD_NOT_DELETE_RESOURCE"); //$NON-NLS-1$
	private static final String COULD_NOT_RENAME_RESOURCE = Messages.getString("DBResource.COULD_NOT_RENAME_RESOURCE"); //$NON-NLS-1$

	private static final Logger logger = LoggerFactory
			.getLogger(RCPResource.class);

	private boolean binary = false;

	private String contentType;

	public RCPResource(RCPRepository repository, RepositoryPath path) {
		super(repository, path);
		try {
			RCPFile rcpFile = getDocument();
			if (rcpFile != null) {
				this.binary = rcpFile.isBinary();
				this.contentType = rcpFile.getContentType();

			}
		} catch (IOException e) {
			logger.error(e.getMessage(), e);
		}
	}

	@Override
	public void create() throws IOException {
		getParent().createResource(getName(), null, false, CONTENT_TYPE_DEFAULT);
	}

	@Override
	public void delete() throws IOException {
		final RCPFile document = getDocumentSafe();
		try {
			document.delete();
		} catch (RCPBaseException ex) {
			throw new IOException(COULD_NOT_DELETE_RESOURCE + this.getName(), ex);
		}
	}

	@Override
	public void renameTo(String name) throws IOException {
		 final RCPFile document = getDocumentSafe();
		 try {
			 document.rename(RepositoryPath.normalizePath(getParent().getPath(), name));
		 } catch (RCPBaseException ex) {
			 throw new IOException(COULD_NOT_RENAME_RESOURCE + this.getName(),
						ex);
		 }
	}

	@Override
	public void moveTo(String path) throws IOException {
		final RCPFile document = getDocumentSafe();
		 try {
			 document.rename(path);
		 } catch (RCPBaseException ex) {
			 throw new IOException(COULD_NOT_RENAME_RESOURCE + this.getName(),
						ex);
		 }
	}

	@Override
	public void copyTo(String path) throws IOException {
		// TODO Auto-generated method stub
		throw new IOException(NOT_IMPLEMENTED);
	}

	@Override
	public boolean exists() throws IOException {
		return (getDocument() != null);
	}

	@Override
	public boolean isEmpty() throws IOException {
		return (getContent().length == 0);
	}

	@Override
	public byte[] getContent() throws IOException {
		final RCPFile document = getDocumentSafe();
		try {
			byte[] bytes = document.getData();
			return bytes;
		} catch (RCPBaseException ex) {
			throw new IOException(COULD_NOT_READ_RESOURCE_CONTENT, ex);
		}
	}

	@Override
	public void setContent(byte[] content) throws IOException {

		if (this.contentType == null || "".equals(this.contentType)) { //$NON-NLS-1$
			this.contentType = IResource.CONTENT_TYPE_DEFAULT;
		}

		if (exists()) {
			final RCPFile document = getDocumentSafe();
			try {
				document.setData(content);
			} catch (RCPBaseException ex) {
				throw new IOException(COULD_NOT_UPDATE_DOCUMENT, ex);
			}
		} else {
			getParent().createResource(getName(), content, this.binary, this.contentType);
		}
	}

	@Override
	public boolean equals(Object obj) {
		if (obj == null) {
			return false;
		}
		if (obj == this) {
			return true;
		}
		if (!(obj instanceof RCPResource)) {
			return false;
		}
		final RCPResource other = (RCPResource) obj;
		return getPath().equals(other.getPath());
	}

	@Override
	public int hashCode() {
		return getPath().hashCode();
	}

	/**
	 * Returns the {@link RCPFile} object matching this {@link RCPResource}. If
	 * there is no such object, then <code>null</code> is returned.
	 */
	protected RCPFile getDocument() throws IOException {
		final RCPObject object = getRCPObject();
		if (object == null) {
			return null;
		}
		if (!(object instanceof RCPFile)) {
			return null;
		}
		return (RCPFile) object;
	}

	/**
	 * Returns the {@link RCPFile} object matching this {@link RCPResource}. If
	 * there is no such object, then an {@link IOException} is thrown.
	 */
	protected RCPFile getDocumentSafe() throws IOException {
		final RCPFile document = getDocument();
		if (document == null) {
			throw new IOException(format(THERE_IS_NO_RESOURCE_AT_PATH_0,
					getPath()));
		}
		return document;
	}

	@Override
	public boolean isBinary() {
		return binary;
	}

	@Override
	public String getContentType() {
		return contentType;
	}

	@Override
	public void setContent(byte[] content, boolean isBinary, String contentType)
			throws IOException {

		this.binary = isBinary;
		this.contentType = contentType;

		if (!isBinary) {
			setContent(content);
		}

		if (exists()) {
			final RCPFile document = getDocumentSafe();
			try {
				document.setData(content);
			} catch (RCPBaseException ex) {
				throw new IOException(COULD_NOT_UPDATE_DOCUMENT, ex);
			}
		} else {
			getParent().createResource(getName(), content, binary, contentType);
		}

	}

	@Override
	public List<IResourceVersion> getResourceVersions() throws IOException {
//		try {
//			return getRepository().getRepositoryDAO()
//					.getResourceVersionsByPath(getPath());
//		} catch (RCPBaseException ex) {
//			logger.error(ex.getMessage(), ex);
			return null;
//		}
	}

	@Override
	public IResourceVersion getResourceVersion(int version) throws IOException {
//		return new DBResourceVersion(getRepository(), new RepositoryPath(
//				getPath()), version);
		return null;
	}

}

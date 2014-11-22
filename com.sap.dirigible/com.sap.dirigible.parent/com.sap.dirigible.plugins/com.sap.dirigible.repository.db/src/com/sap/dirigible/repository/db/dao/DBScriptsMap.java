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

package com.sap.dirigible.repository.db.dao;

/**
 * The static map for the scripts locations Intentionally the active scripts are
 * registered in this class
 * 
 */
public class DBScriptsMap {

	public static final String SCRIPT_GET_SCHEMA_VERSION = "/com/sap/dirigible/repository/db/sql/get_schema_version.sql"; //$NON-NLS-1$

	public static final String SCRIPT_CREATE_SCHEMA_1 = "/com/sap/dirigible/repository/db/sql/create_schema_1.sql"; //$NON-NLS-1$
	public static final String SCRIPT_CREATE_SCHEMA_2 = "/com/sap/dirigible/repository/db/sql/create_schema_2.sql"; //$NON-NLS-1$
	public static final String SCRIPT_CREATE_SCHEMA_3 = "/com/sap/dirigible/repository/db/sql/create_schema_3.sql"; //$NON-NLS-1$
	public static final String SCRIPT_CREATE_SCHEMA_4 = "/com/sap/dirigible/repository/db/sql/create_schema_4.sql"; //$NON-NLS-1$
	public static final String SCRIPT_CREATE_SCHEMA_5 = "/com/sap/dirigible/repository/db/sql/create_schema_5.sql"; //$NON-NLS-1$
	public static final String SCRIPT_CREATE_SCHEMA_6 = "/com/sap/dirigible/repository/db/sql/create_schema_6.sql"; //$NON-NLS-1$

	public static final String SCRIPT_GET_BINARY = "/com/sap/dirigible/repository/db/sql/get_binary.sql"; //$NON-NLS-1$
	public static final String SCRIPT_GET_DOCUMENT = "/com/sap/dirigible/repository/db/sql/get_document.sql"; //$NON-NLS-1$
	public static final String SCRIPT_GET_FILES_BY_PATH = "/com/sap/dirigible/repository/db/sql/get_files_by_path.sql"; //$NON-NLS-1$
	public static final String SCRIPT_GET_FILE_BY_PATH = "/com/sap/dirigible/repository/db/sql/get_file_by_path.sql"; //$NON-NLS-1$

	public static final String SCRIPT_INSERT_BINARY = "/com/sap/dirigible/repository/db/sql/insert_binary.sql"; //$NON-NLS-1$
	public static final String SCRIPT_INSERT_DOCUMENT = "/com/sap/dirigible/repository/db/sql/insert_document.sql"; //$NON-NLS-1$
	public static final String SCRIPT_INSERT_FILE = "/com/sap/dirigible/repository/db/sql/insert_file.sql"; //$NON-NLS-1$

	public static final String SCRIPT_REMOVE_BINARY = "/com/sap/dirigible/repository/db/sql/remove_binary.sql"; //$NON-NLS-1$
	public static final String SCRIPT_REMOVE_BINS_CASCADE = "/com/sap/dirigible/repository/db/sql/remove_bins_cascade.sql"; //$NON-NLS-1$
	public static final String SCRIPT_REMOVE_DOCS_CASCADE = "/com/sap/dirigible/repository/db/sql/remove_docs_cascade.sql"; //$NON-NLS-1$
	public static final String SCRIPT_REMOVE_DOCUMENT = "/com/sap/dirigible/repository/db/sql/remove_document.sql"; //$NON-NLS-1$
	public static final String SCRIPT_REMOVE_FILE_BY_PATH = "/com/sap/dirigible/repository/db/sql/remove_file_by_path.sql"; //$NON-NLS-1$
	public static final String SCRIPT_REMOVE_FOLDER_BY_PATH = "/com/sap/dirigible/repository/db/sql/remove_folder_by_path.sql"; //$NON-NLS-1$
	public static final String SCRIPT_REMOVE_FOLDER_CASCADE = "/com/sap/dirigible/repository/db/sql/remove_folder_cascade.sql"; //$NON-NLS-1$

	public static final String SCRIPT_IS_FOLDER_EMPTY = "/com/sap/dirigible/repository/db/sql/is_folder_empty.sql"; //$NON-NLS-1$

	public static final String SCRIPT_SEARCH_NAME = "/com/sap/dirigible/repository/db/sql/search_name.sql"; //$NON-NLS-1$
	public static final String SCRIPT_SEARCH_NAME_SENSE = "/com/sap/dirigible/repository/db/sql/search_name_sense.sql"; //$NON-NLS-1$
	public static final String SCRIPT_SEARCH_NAME_UNDER_ROOT = "/com/sap/dirigible/repository/db/sql/search_name_under_root.sql"; //$NON-NLS-1$
	public static final String SCRIPT_SEARCH_NAME_UNDER_ROOT_SENSE = "/com/sap/dirigible/repository/db/sql/search_name_under_root_sense.sql"; //$NON-NLS-1$
	public static final String SCRIPT_SEARCH_TEXT = "/com/sap/dirigible/repository/db/sql/search_text.sql"; //$NON-NLS-1$
	public static final String SCRIPT_SEARCH_TEXT_SENSE = "/com/sap/dirigible/repository/db/sql/search_text_sense.sql"; //$NON-NLS-1$

	public static final String SCRIPT_GET_FILE_VERSION_BY_PATH = "/com/sap/dirigible/repository/db/sql/get_file_version_by_path.sql"; //$NON-NLS-1$
	public static final String SCRIPT_GET_FILE_VERSIONS_BY_PATH = "/com/sap/dirigible/repository/db/sql/get_file_versions_by_path.sql"; //$NON-NLS-1$
	public static final String SCRIPT_GET_NEXT_FILE_VERSION_BY_PATH = "/com/sap/dirigible/repository/db/sql/get_next_file_version_by_path.sql"; //$NON-NLS-1$
	public static final String SCRIPT_INSERT_FILE_VERSION = "/com/sap/dirigible/repository/db/sql/insert_file_version.sql"; //$NON-NLS-1$
	public static final String SCRIPT_REMOVE_ALL_FILE_VERSIONS = "/com/sap/dirigible/repository/db/sql/remove_all_file_versions_by_path.sql"; //$NON-NLS-1$
	public static final String SCRIPT_REMOVE_ALL_FILE_VERSIONS_BEFORE_DATE = "/com/sap/dirigible/repository/db/sql/remove_all_file_versions_before_date.sql"; //$NON-NLS-1$

	public static final String SCRIPT_GET_FILES_BY_PATH_CASCADE = "/com/sap/dirigible/repository/db/sql/get_files_by_path_cascade.sql"; //$NON-NLS-1$
	public static final String SCRIPT_GET_DOCUMENTS_BY_PATH_CASCADE = "/com/sap/dirigible/repository/db/sql/get_documents_by_path_cascade.sql"; //$NON-NLS-1$
	public static final String SCRIPT_RENAME_FILE = "/com/sap/dirigible/repository/db/sql/rename_file.sql"; //$NON-NLS-1$
	public static final String SCRIPT_RENAME_DOCUMENT = "/com/sap/dirigible/repository/db/sql/rename_document.sql"; //$NON-NLS-1$

	public static final String SCRIPT_SET_MODIFIED = "/com/sap/dirigible/repository/db/sql/set_modified.sql"; //$NON-NLS-1$
}

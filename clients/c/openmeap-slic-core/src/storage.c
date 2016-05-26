/*
 ###############################################################################
 #                                                                             #
 #    Copyright (C) 2011-2016 OpenMEAP, Inc.                                   #
 #    Credits to Jonathan Schang & Rob Thacher                                 #
 #                                                                             #
 #    Released under the LGPLv3                                                #
 #                                                                             #
 #    OpenMEAP is free software: you can redistribute it and/or modify         #
 #    it under the terms of the GNU Lesser General Public License as published #
 #    by the Free Software Foundation, either version 3 of the License, or     #
 #    (at your option) any later version.                                      #
 #                                                                             #
 #    OpenMEAP is distributed in the hope that it will be useful,              #
 #    but WITHOUT ANY WARRANTY; without even the implied warranty of           #
 #    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the            #
 #    GNU Lesser General Public License for more details.                      #
 #                                                                             #
 #    You should have received a copy of the GNU Lesser General Public License #
 #    along with OpenMEAP.  If not, see <http://www.gnu.org/licenses/>.        #
 #                                                                             #
 ###############################################################################
 */

#include <openmeap-slic-core.h>

om_file_input_stream_ptr om_storage_open_file_input_stream(om_storage_ptr storage, const char *filePath) {
	return storage->open_file_input_stream(storage,filePath);
}
om_file_output_stream_ptr om_storage_open_file_output_stream(om_storage_ptr storage, const char *filePath, om_bool truncate) {
	return storage->open_file_output_stream(storage,filePath,truncate);
}
om_uint32 om_storage_write(om_file_output_stream_ptr file, void *bytes, om_uint32 index, om_uint32 num_bytes) {
	return ((om_storage_ptr)(file->storage))->write(file,bytes,index,num_bytes);
}
om_uint32 om_storage_read(om_file_input_stream_ptr file, void *bytes, om_uint32 num_bytes) {
	return ((om_storage_ptr)(file->storage))->read(file,bytes,num_bytes);
}
void om_storage_close_file_input_stream(om_file_input_stream_ptr file) {
	((om_storage_ptr)(file->storage))->close_file_input_stream(file);
}
void om_storage_close_file_output_stream(om_file_output_stream_ptr file) {
	((om_storage_ptr)(file->storage))->close_file_output_stream(file); 
}
om_bool om_storage_delete_file(om_storage_ptr storage, const char *filePath) {
	return storage->delete_file(storage,filePath);
}
om_bool om_storage_create_directories_for_path(om_storage_ptr storage, char *path) {
	return storage->create_directories_for_path(storage,path);
}
om_file_input_stream_ptr om_storage_get_import_archive_input_stream(om_storage_ptr storage) {
	return storage->get_import_archive_input_stream(storage);
}
om_file_output_stream_ptr om_storage_get_import_archive_output_stream(om_storage_ptr storage) {
	return storage->get_import_archive_output_stream(storage);
}
om_bool om_storage_delete_import_archive(om_storage_ptr storage) {
	return storage->delete_import_archive(storage);
}
char * om_storage_get_current_location(om_storage_ptr storage) {
	return storage->get_current_location(storage);
}
om_bool om_storage_reset_storage(om_storage_ptr storage) {
	return storage->reset_storage(storage);
}
om_bool om_storage_delete_directory(om_storage_ptr storage, char *rootPath, om_bool include_root) {
	return storage->delete_directory(storage,rootPath,include_root);
}
char * om_storage_get_localstorage_path(om_storage_ptr storage) {
	return storage->get_localstorage_path(storage);
}
char * om_storage_get_current_assets_prefix(om_storage_ptr storage) {
	return storage->get_current_assets_prefix(storage);
}
char * om_storage_get_bundleresources_path(om_storage_ptr storage) {
	return storage->get_bundleresources_path(storage);
}
om_uint64 om_storage_get_bytes_free(om_storage_ptr storage) {
	return storage->get_bytes_free(storage);
}
char * om_storage_file_get_contents(om_storage_ptr storage, const char *fullFilePath) {
	return storage->file_get_contents(storage,fullFilePath);
}


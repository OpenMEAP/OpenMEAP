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

#ifndef __OPENMEAP_SLIC_CORE_STORAGE_DEFAULTS__
#define __OPENMEAP_SLIC_CORE_STORAGE_DEFAULTS__

/***************
 * DEFAULT IMPLEMENTATION DECLARATIONS BELOW
 ***************/

OM_EXPORT om_storage_ptr default_om_storage_alloc(om_config_ptr *cfg);
OM_EXPORT void default_om_storage_release(om_storage_ptr storage);

OM_EXPORT om_file_input_stream_ptr default_om_storage_open_file_input_stream(om_storage_ptr storage, const char *filePath);
OM_EXPORT om_file_output_stream_ptr default_om_storage_open_file_output_stream(om_storage_ptr storage, const char *filePath, om_bool truncate);
OM_EXPORT void default_om_storage_close_file_input_stream(om_file_input_stream_ptr file);
OM_EXPORT void default_om_storage_close_file_output_stream(om_file_output_stream_ptr file);

OM_EXPORT char * default_om_storage_file_get_contents(om_storage_ptr storage, const char *full_file_path);

OM_EXPORT om_uint32 default_om_storage_write(om_file_output_stream_ptr ostream, void *bytes, om_uint32 index, om_uint32 num_bytes);
OM_EXPORT om_uint32 default_om_storage_read(om_file_input_stream_ptr istream, void *bytes, om_uint32 num_bytes);

OM_EXPORT om_bool default_om_storage_delete_file(om_storage_ptr storage, const char *filePath);
OM_EXPORT om_bool default_om_storage_create_directories_for_path(om_storage_ptr storage, char *path);

OM_EXPORT om_file_input_stream_ptr default_om_storage_get_import_archive_input_stream(om_storage_ptr storage);
OM_EXPORT om_file_output_stream_ptr default_om_storage_get_import_archive_output_stream(om_storage_ptr storage);
OM_EXPORT om_bool default_om_storage_delete_import_archive(om_storage_ptr storage);

OM_EXPORT char * default_om_storage_get_current_location(om_storage_ptr storage);

OM_EXPORT om_bool default_om_storage_reset_storage(om_storage_ptr storage);

OM_EXPORT om_bool default_om_storage_delete_directory(om_storage_ptr storage, char *root_path, om_bool include_root);

OM_EXPORT om_uint64 default_om_storage_get_bytes_free(om_storage_ptr storage);

OM_EXPORT char * default_om_storage_get_localstorage_path(om_storage_ptr storage);
OM_EXPORT char * default_om_storage_get_bundleresources_path(om_storage_ptr storage);
OM_EXPORT char * default_om_storage_get_current_assets_prefix(om_storage_ptr storage);


#endif


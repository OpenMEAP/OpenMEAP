/*
 ###############################################################################
 #                                                                             #
 #    Copyright (C) 2011-2015 OpenMEAP, Inc.                                   #
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

#include "../storage-test.h"

om_storage_mock_vars_ptr om_storage_mock_vars_var = OM_NULL;

void om_storage_free_mock_vars(om_storage_mock_vars_ptr ptr) {
	if( om_storage_mock_vars_var->delete_file_arg!=OM_NULL ) {
		om_free(om_storage_mock_vars_var->delete_file_arg);
		om_storage_mock_vars_var->delete_file_arg = OM_NULL;
	}
	if( om_storage_mock_vars_var->open_file_output_stream_filePath!=OM_NULL ) {
		om_free(om_storage_mock_vars_var->open_file_output_stream_filePath);
		om_storage_mock_vars_var->open_file_output_stream_filePath = OM_NULL;
	}
	if( om_storage_mock_vars_var->open_file_input_stream_filePath!=OM_NULL ) {
		om_free(om_storage_mock_vars_var->open_file_input_stream_filePath);
		om_storage_mock_vars_var->open_file_input_stream_filePath = OM_NULL;
	}
}

void om_storage_set_mock_vars(om_storage_mock_vars_ptr ptr) {
	om_storage_mock_vars_var = ptr;
}

om_uint64 mock_om_storage_get_bytes_free(om_storage_ptr storage) {
	return om_storage_mock_vars_var->get_bytes_free_result;
}

om_bool mock_om_storage_delete_file(om_storage_ptr storage, const char *filePath) {
	om_storage_mock_vars_var->delete_file_arg = om_string_copy(filePath);
	return om_storage_mock_vars_var->delete_file_result;
}

om_file_input_stream_ptr mock_om_storage_open_file_input_stream(om_storage_ptr storage, const char *filePath) {
	om_storage_mock_vars_var->open_file_input_stream_filePath = om_string_copy(filePath);
	return om_storage_mock_vars_var->open_file_input_stream_result;
}

om_file_output_stream_ptr mock_om_storage_open_file_output_stream(om_storage_ptr storage, const char *filePath, om_bool truncate) {
	om_storage_mock_vars_var->open_file_output_stream_filePath = om_string_copy(filePath);
	om_storage_mock_vars_var->open_file_output_stream_truncate = truncate;
	return om_storage_mock_vars_var->open_file_output_stream_result;
}

void mock_om_storage_close_file_input_stream(om_file_input_stream_ptr file) {
	om_storage_mock_vars_var->close_file_input_stream_file=file;
	om_storage_mock_vars_var->close_file_input_stream_called=OM_TRUE;
}

void mock_om_storage_close_file_output_stream(om_file_output_stream_ptr file) {
	om_storage_mock_vars_var->close_file_output_stream_file=file;
	om_storage_mock_vars_var->close_file_output_stream_called=OM_TRUE;
}

void om_storage_release(om_storage_ptr storage) {
	om_free(storage);
}

om_storage_ptr om_storage_alloc(om_config_ptr *cfg) {
	om_storage_ptr stg = default_om_storage_alloc(cfg);
	if( stg==OM_NULL )
		return stg;
	stg->cfg=cfg;
	stg->get_bytes_free=mock_om_storage_get_bytes_free;
	stg->delete_file=mock_om_storage_delete_file;
	stg->open_file_input_stream=mock_om_storage_open_file_input_stream;
	stg->open_file_output_stream=mock_om_storage_open_file_output_stream;
	stg->close_file_input_stream=mock_om_storage_close_file_input_stream;
	stg->close_file_output_stream=mock_om_storage_close_file_output_stream;
	return stg;
}

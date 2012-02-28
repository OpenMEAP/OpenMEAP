/*
 ###############################################################################
 #                                                                             #
 #    Copyright (C) 2011-2012 OpenMEAP, Inc.                                   #
 #    Credits to Jonathan Schang & Robert Thacher                              #
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

#ifndef __STORAGE_TEST_H__
#define __STORAGE_TEST_H__

typedef struct om_storage_mock_vars_type {
	om_bool                   delete_file_result;
	char *                    delete_file_arg;
	
	om_file_input_stream_ptr  open_file_input_stream_result;
	char *                    open_file_input_stream_filePath;
	
	om_file_output_stream_ptr open_file_output_stream_result;
	char *                    open_file_output_stream_filePath;
	om_bool                   open_file_output_stream_truncate;
	
	uint32                    get_bytes_free_result;
	
	om_bool                   close_file_input_stream_called;
	om_file_input_stream_ptr  close_file_input_stream_file;
	
	om_bool                   close_file_output_stream_called;
	om_file_output_stream_ptr close_file_output_stream_file;
} om_storage_mock_vars, *om_storage_mock_vars_ptr;

void om_storage_set_mock_vars(om_storage_mock_vars_ptr ptr);
void run_storage_tests();

#endif

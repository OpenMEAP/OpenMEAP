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

#ifndef __OPENMEAP_ZIP_H__
#define __OPENMEAP_ZIP_H__

#include "unzip.h"

#define OM_ZIP_WRITEBUFFERSIZE (size_t)4096

#define OM_ZIP_OPTS_NOPATH    (1)
#define OM_ZIP_OPTS_OVERWRITE (2)

typedef struct om_unzip_archive_type {
	om_list_ptr error_log;
	unzFile file;
	unz_global_info * global_info_ptr;
} om_unzip_archive, *om_unzip_archive_ptr;

/*typedef struct om_unzip_entry_type {
	om_unzip_archive_ptr archive;
	unz_file_info64 file_info;
} om_unzip_entry, *om_unzip_entry_ptr;*/

/**
 * Opens the zip file pointed to by the full file path passed in.
 *
 * @param file_path The full file path on the system of the zip archive.
 * @return om_unzip_archive_ptr or OM_NULL
 */
OM_EXPORT om_unzip_archive_ptr om_unzip_open_archive(const char *file_path);

OM_EXPORT om_bool om_unzip_close_archive(om_unzip_archive_ptr archive);

/**
 * @return om_bool OM_TRUE if successful, else OM_FALSE and error is set
 */
OM_EXPORT om_bool om_unzip_archive_into_path(om_unzip_archive_ptr archive, const char *exportBasePath);

#endif

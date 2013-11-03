/*
 ###############################################################################
 #                                                                             #
 #    Copyright (C) 2011-2014 OpenMEAP, Inc.                                   #
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

#ifndef __OPENMEAP_SLIC_CORE__
#define __OPENMEAP_SLIC_CORE__
	
#ifndef NULL
	#define NULL  0
#endif
#define OM_NULL NULL

#ifndef TRUE
	#define TRUE  1
#endif
#define OM_TRUE TRUE

#ifndef FALSE
	#define FALSE 0
#endif
#define OM_FALSE FALSE

#define OM_ERROR (-1)

#ifndef uint32
	#define uint32 unsigned long
#endif
#define om_uint32 uint32

#ifndef uint64
	#define uint64 unsigned long long
#endif
#define om_uint64 uint64

#define OM_EXPORT extern 
#define OM_PRIVATE_FUNC

#define OM_DEBUG OM_TRUE

#define om_time(T) time(T)

#define OM_FS_FILE_SEP '/'
#define OM_FS_PATH_SEP ':'
#define OM_IMPORT_ARCHIVE_FILENAME "import.zip"

typedef int om_bool;

// a result that may be either OM_TRUE, OM_FALSE, or OM_ERROR
typedef int om_trires;

#include <openmeap/error.h>
#include <openmeap/mem.h>
#include <openmeap/string.h>
#include <openmeap/list.h>
#include <openmeap/config.h>
#include <openmeap/dict.h>
#include <openmeap/zip.h>
#include <openmeap/digest.h>
#include <openmeap/storage.h>
#include <openmeap/storage-default.h>

// The concrete implementation for the following will be different for each platform
#include <openmeap/props.h>
#include <openmeap/prefs.h>
#include <openmeap/network.h>

// update pretty much depends on everything else, so makes sense to come last
#include <openmeap/update.h>

OM_EXPORT void om_first_run_check(om_config_ptr cfg);

#endif

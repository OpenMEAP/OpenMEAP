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

#ifndef __OPENMEAP_SLIC_CORE_PROPERTIES__
#define __OPENMEAP_SLIC_CORE_PROPERTIES__

typedef struct om_props_type {
	void *device_data;
	char * (*get)(struct om_props_type *props, const char *key);
} om_props, *om_props_ptr;

/**
 * implemented in each specific device
 */
OM_EXPORT om_props_ptr om_props_acquire(const char *name);

/**
 * implemented in each specific device
 */
OM_EXPORT void om_props_release(om_props_ptr props);

/**
 * @return a new character array (must be freed by caller) containing the value of the property
 */
OM_EXPORT char * om_props_get(om_props_ptr props, const char *key);

#endif __OPENMEAP_SLIC_CORE_PROPERTIES__

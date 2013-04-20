/*
 ###############################################################################
 #                                                                             #
 #    Copyright (C) 2011-2013 OpenMEAP, Inc.                                   #
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

#ifndef __OPENMEAP_SLIC_CORE_STORAGE__
#define __OPENMEAP_SLIC_CORE_STORAGE__

// NOTE: unless otherwise stated, all functions are implemented openmeap-slic-core

/**
 * Represents a variety of stream types
 * Will likely be split, but for now all these types are, internally, the same.
 */
struct om_stream_type {
	void *storage;
	char *device_path;
	void *device_data;
};
typedef struct om_stream_type om_file_input_stream, om_file_output_stream, om_file_stream, 
*om_file_input_stream_ptr, *om_file_output_stream_ptr, *om_file_stream_ptr;

/**
 * Holds any information for device specific storage context
 * As well as OpenMEAP configuration.
 */
typedef struct om_storage_type {
    
	om_config_ptr cfg;
    
	om_file_input_stream_ptr (*open_file_input_stream)(struct om_storage_type * storage, const char *filePath);
	om_file_output_stream_ptr (*open_file_output_stream)(struct om_storage_type * storage, const char *filePath, om_bool truncate);
    void (*close_file_input_stream)(om_file_input_stream_ptr file);
	void (*close_file_output_stream)(om_file_output_stream_ptr file);
    
    char * (*file_get_contents)(struct om_storage_type * storage, const char *fullFilePath);
	om_uint32 (*write)(om_file_output_stream_ptr ostream, void *bytes, om_uint32 index, om_uint32 num_bytes);
	om_uint32 (*read)(om_file_input_stream_ptr istream, void *bytes, om_uint32 num_bytes);
	
	om_bool (*create_directories_for_path)(struct om_storage_type * storage, char *path);
    
	om_file_input_stream_ptr (*get_import_archive_input_stream)(struct om_storage_type * storage);
	om_file_output_stream_ptr (*get_import_archive_output_stream)(struct om_storage_type * storage);
	om_bool (*delete_import_archive)(struct om_storage_type * storage);
    
	char * (*get_current_location)(struct om_storage_type * storage);
    char * (*get_current_assets_prefix)(struct om_storage_type * storage);
    char * (*get_localstorage_path)(struct om_storage_type * storage);
	char * (*get_bundleresources_path)(struct om_storage_type * storage);
    
	om_bool (*reset_storage)(struct om_storage_type * storage);
	om_bool (*delete_directory)(struct om_storage_type * storage, char *rootPath, om_bool include_root);
    om_bool (*delete_file)(struct om_storage_type * storage, const char *filePath);
    
	om_uint64 (*get_bytes_free)(struct om_storage_type * storage);
    
} om_storage, *om_storage_ptr;

///////////////////////////
// INITIALIZATION - DEVICE LIBRARY SPECIFIC IMPLEMENTATIONS

/**
 * Allocate and initialize a new storage context
 * Must be implemented in device library
 */
OM_EXPORT om_storage_ptr om_storage_alloc(om_config_ptr *cfg);

/**
 * Release a storage context
 * Must be implemented in device library
 */
OM_EXPORT void om_storage_release(om_storage_ptr storage);


///////////////////////////
// DEVICE LIBRARY SPECIFIC IMPLEMENTATIONS

/**
 * @return A base path where we have read-write permissions.
 */
OM_EXPORT char * om_storage_get_localstorage_path(om_storage_ptr storage);

/**
 * @return The path where resources compiled into the application are available.  It may be read-only.
 */
OM_EXPORT char * om_storage_get_bundleresources_path(om_storage_ptr storage);

/**
 * Determine and return the number of bytes available for new storage.
 * Implemented in device library
 *
 * @return The number of bytes left for storage
 */
OM_EXPORT om_uint64 om_storage_get_bytes_free(om_storage_ptr storage);

/**
 * Returns the current prefix to search for bundled assets in.
 * This will be either the bundled assets, or the current unzipped archive location.
 */
OM_EXPORT char * om_storage_get_current_assets_prefix(om_storage_ptr storage);

/////////////////////////////
// IMPLEMENTATIONS THAT ARE IN CORE AND SHOULD BE REUSABLE

/**
 * Opens a file in the current storage location for reading.
 * Implemented in device library
 *
 * @param storage the storage context
 * @param filePath the path to the file to open
 */
OM_EXPORT om_file_input_stream_ptr om_storage_open_file_input_stream(om_storage_ptr storage, const char *filePath);

/**
 * Opens a file for writing.
 * Implemented in device library
 *
 * @param storage the storage context
 * @param filePath the path to the file to open
 * @param truncate truncate the file if it exists, else position to the end.
 * @return A pointer to the om_file_output_stream struct or OM_NULL.  Sets the error flag.
 */
OM_EXPORT om_file_output_stream_ptr om_storage_open_file_output_stream(om_storage_ptr storage, const char *filePath, om_bool truncate);

/**
 * Write num_bytes from bytes, starting at index, to the om_file_output_stream pointer passed in.
 *
 * @param ostream The om_file_output_stream pointer to write to
 * @param bytes The bytes to write to the output stream
 * @param index The index to start writing at
 * @param num_bytes The number of bytes to write
 */
OM_EXPORT om_uint32 om_storage_write(om_file_output_stream_ptr ostream, void *bytes, om_uint32 index, om_uint32 num_bytes);

/**
 * Read num_bytes into bytes from the om_file_input_stream pointer passed in
 *
 * @param istream The om_file_input_stream to read from
 * @param bytes A pointer to the pointer to allocate and pass back
 * @param num_bytes The number of bytes preferred to read
 * @return The number of bytes read.  Sets an error code on error
 */
OM_EXPORT om_uint32 om_storage_read(om_file_input_stream_ptr istream, void *bytes, om_uint32 num_bytes);

/**
 * Closes a file input stream and releases associated resources
 * Implemented in device library.
 *
 * @param storage
 * @param file
 */
OM_EXPORT void om_storage_close_file_input_stream(om_file_input_stream_ptr file);

/**
 * Closes a file output stream and releases associated resources
 * Implemented in device library.
 *
 * @param storage the storage context
 * @param file The file output stream to close and release resources for
 */
OM_EXPORT void om_storage_close_file_output_stream(om_file_output_stream_ptr file);

/**
 * Delete the file or directory passed in.  If directory, then it must be empty.
 * Implemented in device library
 */
OM_EXPORT om_bool om_storage_delete_file(om_storage_ptr storage, const char *filePath);

/**
 * Creates the directory structure for a path.  The segment after the last '/' is assumed
 * to be a file, and ignored.
 *
 * @param path The full path to be created.
 * @return om_bool true if successful, else false.
 */
OM_EXPORT om_bool om_storage_create_directories_for_path(om_storage_ptr storage, char *path);


///////////////////////////
// IMPORT ARCHIVE RELATED

/**
 * @return An input stream to use for reading the import archive.  Null if it does not exist.
 */
OM_EXPORT om_file_input_stream_ptr om_storage_get_import_archive_input_stream(om_storage_ptr storage);

/**
 * @return An output stream to the truncated import archive file
 */
OM_EXPORT om_file_output_stream_ptr om_storage_get_import_archive_output_stream(om_storage_ptr storage);

/**
 * Delete the import archive
 */
OM_EXPORT om_bool om_storage_delete_import_archive(om_storage_ptr storage);


///////////////////////////
// REGION RELATED

OM_EXPORT char * om_storage_get_current_location(om_storage_ptr storage);

/**
 * Deletes all files in the active storage regions
 *
 * @return true if successful, false if an error occurred.
 */
OM_EXPORT om_bool om_storage_reset_storage(om_storage_ptr storage);

/**
 * Recursively delete everything under a directory, including the directory
 */
OM_EXPORT om_bool om_storage_delete_directory(om_storage_ptr storage, char *rootPath, om_bool include_root);


#endif


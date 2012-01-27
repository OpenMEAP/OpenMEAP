/*
 ###############################################################################
 #                                                                             #
 #    Copyright (C) 2011 OpenMEAP, Inc.                                        #
 #    Credits to Jonathan Schang & Robert Thacher                              #
 #                                                                             #
 #    Released under the GPLv3                                                 #
 #                                                                             #
 #    OpenMEAP is free software: you can redistribute it and/or modify         #
 #    it under the terms of the GNU General Public License as published by     #
 #    the Free Software Foundation, either version 3 of the License, or        #
 #    (at your option) any later version.                                      #
 #                                                                             #
 #    OpenMEAP is distributed in the hope that it will be useful,              #
 #    but WITHOUT ANY WARRANTY; without even the implied warranty of           #
 #    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the            #
 #    GNU General Public License for more details.                             #
 #                                                                             #
 #    You should have received a copy of the GNU General Public License        #
 #    along with OpenMEAP.  If not, see <http://www.gnu.org/licenses/>.        #
 #                                                                             #
 ###############################################################################
 */

package com.openmeap.android;

import java.io.FileNotFoundException;
import java.io.File;
import java.lang.UnsupportedOperationException;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.database.Cursor;
import android.net.Uri;
import android.os.ParcelFileDescriptor;

/**
 * WebView does not support file loading. This class wraps a file load
 * with a content provider.  Making this ContentProvider a part of the
 * application gives us access to the internal storage and sql databases.
 */
public class FileContentProvider extends ContentProvider {
	
	/**
	 * Access to resources through this ContentProvider are addressed using this.
	 * If this were a provided Android ContentProvider, this would be the 
	 * CONTENT_URI.
	 */
    private static String BASE_URI = null;
    private static int BASE_URI_LEN = 0;
    private static String PROVIDER_AUTHORITY = null;
    
    static public void setProviderAuthority(String providerAuthority) {
    	BASE_URI = "content://"+providerAuthority;
    	BASE_URI_LEN = BASE_URI.length();
    	PROVIDER_AUTHORITY = providerAuthority;
    }
    static public String getProviderAuthority() {
    	return PROVIDER_AUTHORITY;
    }
    static public int getBaseUriLength() {
    	return BASE_URI_LEN;
    }
    static public String getBaseUri() {
    	return BASE_URI;
    }
    
    /**
     * Used extensively...having as a char saves on processing
     */
    private static final char FILE_SEP = System.getProperty("file.separator").charAt(0);
    
    /**
     * Converts a file resource to the actual path it will have in internal storage name.
     * 
     * The conversion of '/' to '.' in the fileName passed in is due to a limitation
     * on internal storage in Android.  Seems I cannot create sub-directories.
     * 
     * @param fileName
     * @return
     */
    static public String getInternalStorageFileName(String fileName) {
    	String path = System.getProperty("root.openmeap.internalStoragePrefix") + '.' + fileName.replace(FILE_SEP,'.');
    	return path;
    }
    
    static public String getInternalStorageFileName(String prefix, String fileName) {
    	String path = prefix + '.' + fileName.replace(FILE_SEP,'.');
    	return path;
    }
    
    @Override
    public String getType(Uri uri) {
        // If the mimetype is not appended to the uri, then return an empty string
        String mimetype = uri.getQuery();
        return mimetype == null ? "" : mimetype;
    }
    
    @Override
    public ParcelFileDescriptor openFile(Uri uri, String mode) throws FileNotFoundException {
        if (!"r".equals(mode)) {
            throw new FileNotFoundException("The SLIC FileContentProvider does not support mode \""+ mode + "\" for " + uri);
        }
        String rootOmPath = System.getProperty("root.openmeap.path");
        String filename = uri.toString().substring(BASE_URI_LEN);
        String relFN = filename.replace(rootOmPath,"");
        filename = rootOmPath + FILE_SEP + getInternalStorageFileName(relFN);
        File file = new File(filename);
        ParcelFileDescriptor toRet = ParcelFileDescriptor.open(file, ParcelFileDescriptor.MODE_READ_ONLY);
        return toRet;
    }
    
    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Uri insert(Uri uri, ContentValues values) {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean onCreate() {
        return true;
    }

    @Override
    public Cursor query(Uri uri, String[] projection, String selection,
            String[] selectionArgs, String sortOrder) {
        throw new UnsupportedOperationException();
    }

    @Override
    public int update(Uri uri, ContentValues values, String selection,
            String[] selectionArgs) {
        throw new UnsupportedOperationException();
    }    
}

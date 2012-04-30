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

package com.openmeap.file;

import java.io.BufferedOutputStream;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.zip.ZipFile;

import junit.framework.Assert;

import org.apache.commons.io.FileUtils;
import org.apache.commons.transaction.file.FileResourceManager;
import org.apache.log4j.Level;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.openmeap.constants.FormConstants;
import com.openmeap.util.SLF4JLoggerFacade;
import com.openmeap.util.Utils;

/**
 * Verify the functionality of the FileOperationManagerImpl.
 *  
 * @author schang
 */
public class FileOperationManagerImplTest {
	
	private Logger logger = LoggerFactory.getLogger(FileOperationManagerImplTest.class);
	private static FileOperationManagerImpl mgr;
	private static String STOREDIR = "FileOperationManagerTest-storeDir";
	private static String WORKDIR = "FileOperationManagerTest-workDir";
	private static String TEST_FILE = "test.txt";
	private static String TEST_TEXT = "This is test text.";
	
	/**
	 * Creates the working/store directories, sets up the logger, and creates the FileOperationManager. 
	 * @throws IOException
	 */
	@BeforeClass public static void setUp() throws IOException {
		org.apache.log4j.BasicConfigurator.configure();
		org.apache.log4j.Logger.getLogger("org.apache.commons").setLevel(Level.OFF);
		org.apache.log4j.Logger.getLogger("Locking").setLevel(Level.OFF);
		org.apache.log4j.Logger.getLogger("com.openmeap").setLevel(Level.TRACE);
		
		new File(STOREDIR).mkdir();
		new File(WORKDIR).mkdir();
		File f = new File(STOREDIR+File.separator+TEST_FILE);
		OutputStream stream = new BufferedOutputStream(new FileOutputStream(f));
		InputStream inputStream = new ByteArrayInputStream(TEST_TEXT.getBytes());
		try {
			Utils.pipeInputStreamIntoOutputStream(inputStream, stream);
		} finally {
			if(stream!=null) {
				stream.close();
			}
		}
		
		mgr = new FileOperationManagerImpl();
		FileResourceManager resMgr = new FileResourceManager(
				STOREDIR,
				WORKDIR, 
				false, 
				new SLF4JLoggerFacade(LoggerFactory.getLogger("org.apache.commons.transaction.file")));
		mgr.setFileResourceManager(resMgr);
	}
	
	/**
	 * Deletes the working and store directories
	 * @throws IOException
	 */
	@AfterClass public static void tearDown() throws IOException {
		FileUtils.deleteDirectory(new File(WORKDIR));
		FileUtils.deleteDirectory(new File(STOREDIR));
	}
	
	/**
	 * Verifies successful file copy.
	 * @throws FileOperationException
	 * @throws IOException
	 */
	@Test public void testCopy() throws FileOperationException, IOException {
		createSubDir();
		FileUtils.deleteDirectory(new File(STOREDIR+File.separator+"sub"));
	}
	
	/**
	 * Verifies directory deletion.
	 * @throws FileOperationException
	 * @throws IOException
	 */
	@Test public void testDeleteDir() throws FileOperationException, IOException {
		createSubDir();
		mgr.begin();
		mgr.deleteDir("sub");
		mgr.commit();
		File f = new File(STOREDIR+File.separator+"sub");
		Assert.assertTrue(!f.exists());
	}
	
	/**
	 * Validates that a zip file can be extracted and at a prefix location within the store.
	 * @throws FileOperationException
	 * @throws IOException
	 */
	@Test public void testUnzip() throws FileOperationException, IOException {
		mgr.begin();
		mgr.unzipFile(new ZipFile(this.getClass().getResource("zipped.zip").getFile()), "");
		mgr.commit();
		Assert.assertTrue(new File(STOREDIR+File.separator+"zipped/alpha.txt").exists());
		Assert.assertTrue(new File(STOREDIR+File.separator+"zipped/quick-fox.txt").exists());
		
		mgr.begin();
		mgr.unzipFile(new ZipFile(this.getClass().getResource("zipped.zip").getFile()), "prefix");
		mgr.commit();
		Assert.assertTrue(new File(STOREDIR+File.separator+"prefix/zipped/alpha.txt").exists());
		Assert.assertTrue(new File(STOREDIR+File.separator+"prefix/zipped/quick-fox.txt").exists());
	}
	
	private void createSubDir() throws FileOperationException, IOException {
		
		String[] targets = {
				"sub/test.txt",
				"sub/1/test.txt",
				"sub/1/2/test.txt",
				"sub/2/test.txt"
			};
		
		mgr.begin();
		
		for(String target:targets) {
			mgr.copy(TEST_FILE,target);
		}
		
		InputStream is = mgr.read(targets[0]);
		String expected = TEST_TEXT;
		String actual = Utils.readInputStream(is, FormConstants.CHAR_ENC_DEFAULT);
		
		Assert.assertEquals(expected, actual.trim());
		
		mgr.commit();
		
		for(String target:targets) {
			Assert.assertTrue(new File(STOREDIR+File.separator+target).exists());
		}
	}
}

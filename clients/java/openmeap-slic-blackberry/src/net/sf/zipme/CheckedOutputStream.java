/* CheckedOutputStream.java - Compute checksum of data being written.
   Copyright (C) 1999, 2000 Free Software Foundation, Inc.

This file is part of GNU Classpath.

GNU Classpath is free software; you can redistribute it and/or modify
it under the terms of the GNU General Public License as published by
the Free Software Foundation; either version 2, or (at your option)
any later version.
 
GNU Classpath is distributed in the hope that it will be useful, but
WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
General Public License for more details.

You should have received a copy of the GNU General Public License
along with GNU Classpath; see the file COPYING.  If not, write to the
Free Software Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA
02110-1301 USA.

Linking this library statically or dynamically with other modules is
making a combined work based on this library.  Thus, the terms and
conditions of the GNU General Public License cover the whole
combination.

As a special exception, the copyright holders of this library give you
permission to link this library with independent modules to produce an
executable, regardless of the license terms of these independent
modules, and to copy and distribute the resulting executable under
terms of your choice, provided that you also meet, for each linked
independent module, the terms and conditions of the license of that
module.  An independent module is a module which is not derived from
or based on this library.  If you modify this library, you may extend
this exception to your version of the library, but you are not
obligated to do so.  If you do not wish to do so, delete this
exception statement from your version. */


/* CheckedOutputStream.java
   Copyright (C) 2007 Akihiko Kusanagi

This file was modified by Akihiko Kusanagi for running on Java ME
platform. */


package net.sf.zipme;

import java.io.IOException;
import java.io.OutputStream;

/* Written using on-line Java Platform 1.2 API Specification
 * and JCL book.
 * Believed complete and correct.
 */

/**
 * OutputStream that computes a checksum of data being written using a
 * supplied Checksum object.
 *
 * @see Checksum
 *
 * @author Tom Tromey
 * @date May 17, 1999
 */
public class CheckedOutputStream extends OutputStream
{
 /**
    * This is the subordinate <code>OutputStream</code> that this class
    * redirects its method calls to.
    */
  protected OutputStream out;

  /**
   * Creates a new CheckInputStream on top of the supplied OutputStream
   * using the supplied Checksum.
   */
  public CheckedOutputStream (OutputStream out, Checksum cksum)
  {
    this.out = out;
    this.sum = cksum;
  }

  /**
   * Returns the Checksum object used. To get the data checksum computed so
   * far call <code>getChecksum.getValue()</code>.
   */
  public Checksum getChecksum ()
  {
    return sum;
  }

  /**
   * Writes one byte to the OutputStream and updates the Checksum.
   */
  public void write (int bval) throws IOException
  {
    out.write(bval);
    sum.update(bval);
  }

  /**
    * This method writes all the bytes in the specified array to the underlying
    * <code>OutputStream</code>.  It does this by calling the three parameter
    * version of this method - <code>write(byte[], int, int)</code> in this
    * class instead of writing to the underlying <code>OutputStream</code>
    * directly.  This allows most subclasses to avoid overriding this method.
    *
    * @param buf The byte array to write bytes from
    *
    * @exception IOException If an error occurs
    */
  public void write(byte[] buf) throws IOException
  {
    // Don't do checking here, per Java Lang Spec.
    write(buf, 0, buf.length);
  }

  /**
   * Writes the byte array to the OutputStream and updates the Checksum.
   */
  public void write (byte[] buf, int off, int len) throws IOException
  {
    out.write(buf, off, len);
    sum.update(buf, off, len);
  }

  /**
    * This method closes the underlying <code>OutputStream</code>.  Any
    * further attempts to write to this stream may throw an exception.
    *
    * @exception IOException If an error occurs
    */
  public void close() throws IOException
  {
    flush();
    out.close();
  }

  /**
    * This method attempt to flush all buffered output to be written to the
    * underlying output sink.
    *
    * @exception IOException If an error occurs
    */
  public void flush() throws IOException
  {
    out.flush();
  }

  /** The checksum object. */
  private Checksum sum;
}

/*******************************************************************************
 * Copyright (c) 2006-2018 Massachusetts General Hospital 
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. I2b2 is also distributed under
 * the terms of the Healthcare Disclaimer.
 ******************************************************************************/
/*

 * 
 * Contributors: 
 *     Rajesh Kuttan
 */
package edu.harvard.i2b2.fr.io;

import java.io.*;

/**
 * Copyright (c) 2001, 2002 by Pensamos Digital, All Rights Reserved.<p>
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Library General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 * <p>
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Library General Public License for more details.
 * <p>
 * You should have received a copy of the GNU Library General Public
 * License along with this library; if not, write to the Free
 * Software Foundation, Inc., 675 Mass Ave, Cambridge, MA 02139, USA.
 * <p>
 * This OutputStream discards all data written to it.
 *
 * @author Tim Macinta (twm@alum.mit.edu)
 **/

public class NullOutputStream extends OutputStream {

  private boolean closed = false;

  public NullOutputStream() {
  }

  @Override
public void close() {
    this.closed = true;
  }

  @Override
public void flush() throws IOException {
    if (this.closed) _throwClosed();
  }

  private void _throwClosed() throws IOException {
    throw new IOException("This OutputStream has been closed");
  }

  @Override
public void write(byte[] b) throws IOException {
    if (this.closed) _throwClosed();
  }

  @Override
public void write(byte[] b, int offset, int len) throws IOException {
    if (this.closed) _throwClosed();
  }

  @Override
public void write(int b) throws IOException {
    if (this.closed) _throwClosed();
  }

}

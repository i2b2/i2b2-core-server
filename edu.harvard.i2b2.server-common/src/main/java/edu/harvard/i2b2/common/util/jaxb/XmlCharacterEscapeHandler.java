/*******************************************************************************
 * Copyright (c) 2006-2018 Massachusetts General Hospital 
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. I2b2 is also distributed under
 * the terms of the Healthcare Disclaimer.
 ******************************************************************************/
package edu.harvard.i2b2.common.util.jaxb;

import java.io.IOException;
import java.io.Writer;

import org.apache.xml.utils.XML11Char;
//import org.eclipse.tags.shaded.org.apache.xml.utils.XML11Char;
import org.glassfish.jaxb.core.marshaller.CharacterEscapeHandler;

public class XmlCharacterEscapeHandler implements CharacterEscapeHandler {

	/**
	 * Escape characters inside the buffer and send the output to the writer.
	 *
	 * @exception IOException
	 *                if something goes wrong, IOException can be thrown to stop the
	 *                marshalling process.
	 */
	@Override
	public void escape(char[] buf, int start, int len, boolean isAttValue,
		Writer out) throws IOException {
		for (int i = start; i < (start + len); i++) {
			char ch = buf[i];

			//          you are supposed to do the standard XML character escapes
			// like & ... &amp;   < ... &lt;  etc
			if (ch == '&') {
				out.write("&amp;");

				continue;
			}

			if (ch == '<') {
				out.write("&lt;");
				continue;
			}

			if (ch == '>') {
				out.write("&gt;");
				continue;
			}

			if ((ch == '"') && isAttValue) {
				// isAttValue is set to true when the marshaller is processing
				// attribute values. Inside attribute values, there are more
				// things you need to escape, usually.
				out.write("&quot;");

				continue;
			}

			if ((ch == '\'') && isAttValue) {
				out.write("&apos;");

				continue;
			}

			// you should handle other characters like < or >
			if (ch > 0x7F) {
				// escape everything above ASCII to &#xXXXX;
				out.write("&#x");
				out.write(Integer.toHexString(ch));
				out.write(";");

				continue;
			}

			//use apache util to check for valid xml character
			if (XML11Char.isXML11Valid(ch)) {
				out.write(ch);
			}
		}
	}
}

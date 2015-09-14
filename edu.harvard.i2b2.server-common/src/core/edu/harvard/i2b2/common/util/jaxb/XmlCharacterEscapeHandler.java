package edu.harvard.i2b2.common.util.jaxb;

//import com.sun.org.apache.xerces.internal.util.XML11Char;

import com.sun.xml.bind.marshaller.CharacterEscapeHandler;

import java.io.IOException;
import java.io.Writer;

import org.apache.xml.utils.XML11Char;


public class XmlCharacterEscapeHandler implements CharacterEscapeHandler {
    /**
     * Escape characters inside the buffer and send the output to the writer.
     *
     * @exception IOException
     *    if something goes wrong, IOException can be thrown to stop the
     *    marshalling process.
     */
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

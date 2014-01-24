package de.uni_hildesheim.sse.monitoring.runtime.utils.xml;

/*
~ Copyright 2006-2007 Nicolas De Loof.
~
~ Licensed under the Apache License, Version 2.0 (the "License");
~ you may not use this file except in compliance with the License.
~ You may obtain a copy of the License at
~
~      http://www.apache.org/licenses/LICENSE-2.0
~
~ Unless required by applicable law or agreed to in writing, software
~ distributed under the License is distributed on an "AS IS" BASIS,
~ WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
~ See the License for the specific language governing permissions and
~ limitations under the License.
*/

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;

import de.uni_hildesheim.sse.monitoring.runtime.utils.HashMap;
import de.uni_hildesheim.sse.monitoring.runtime.utils.Stack;
import static de.uni_hildesheim.sse.monitoring.runtime.utils.
    StringUtils.*;

/**
 * A Quick and Dirty XML parser. This class is not thread-safe. Use this class
 * as follows:
 * <code>
 * Reader reader = new FileReader(...);
 * XmlQdParser parser = new XmlQdParser();
 * parser.parse(handler, reader, false);
 * </code>
 * for informing <code>handler</code> about XML events in the file represented
 * by <code>reader</code> and to avoid messages on XML text nodes 
 * (<code>false</code> as last parameter of <code>parse</code>).
 * 
 * @author Nicolas De Loof (taken from <code>http://www.koders.com/java</code>),
 *   Holger Eichelberger (adapted to this project)
 * @since 1.00
 * @version 1.00
 */
public class QdParser {

    /**
     * Defines some parsing modes (to be stored into the mode stack, avoids
     * additional Integer objects). 
     * 
     * @author Holger Eichelberger
     * @since 1.00
     * @version 1.00
     */
    private enum Mode {
    
        /** 
         * Text node.
         */
        TEXT,
    
        /** 
         * Entity node.
         */
        ENTITY,
    
        /** 
         * Opening tag.
         */
        OPEN_TAG,
    
        /** 
         * Closing tag. 
         */
        CLOSE_TAG,
    
        /** 
         * Start tag.
         */
        START_TAG,
    
        /** 
         * Attribute name.
         */
        ATTRIBUTE_LVALUE,
    
        /** 
         * Attribute declaration equal.
         */
        ATTRIBUTE_EQUAL,
    
        /** 
         * Attribute value.
         */
        ATTRIBUTE_RVALUE,
    
        /** 
         * Quote.
         */
        QUOTE,
    
        /** 
         * Inside a tag. 
         */
        IN_TAG,
    
        /** 
         * Single tag. 
         */
        SINGLE_TAG,
    
        /** 
         * Comment.
         */
        COMMENT,
    
        /** 
         * Document end.
         */
        DONE,
    
        /** 
         * Doctype declaration.
         */
        DOCTYPE,
    
        /** 
         * Before document root element. 
         */
        PRE,
    
        /** 
         * CDATA block. 
         */
        CDATA;
    
    }

    /** 
     * Parsed elements stack. 
     */
    private Stack<Mode> st = new Stack<Mode>();

    /** 
     * Depth in nested XML elements. 
     */
    private int depth = 0;

    /** 
     * Parsing mode. 
     */
    private Mode mode = Mode.PRE;

    /** 
     * Quote char (" or '). 
     */
    private int quotec = '"';

    /** 
     * Stores the line position in the document. 
     */
    private int line = 1;

    /** 
     * Stores the column position within {@link #line} in the document. 
     */
    private int col = 0;

    /** 
     * End of line? 
     */
    private boolean eol = false;

    /** 
     * Stores the current element being parsed. 
     */
    private StringBuilder sb = new StringBuilder();

    /** 
     * Stores the current end tag name.
     */
    private StringBuilder etag = new StringBuilder();

    /** 
     * Stores the current tag name. 
     */
    private String tagName = null;

    /** 
     * Stores the left value of an XML attribute.
     */
    private String lvalue = null;

    /** 
     * Stores the right value of an XML attribute. 
     */
    private String rvalue = null;

    /** 
     * Stores current element attributes. 
     */
    private HashMap<String, String> attrs = null;
    
    /**
     * Pop element from stack.
     *
     * @param st stack
     * @return element type
     */
    private static Mode popMode(Stack<Mode> st) {
        if (!st.empty() ) {
            return st.pop();
        }
        return Mode.PRE;
    }

    /**
     * Parse a document.
     *
     * @param doc handler for document
     * @param in the input stream containing the document
     * @param notifyText notify <code>doc</code> about text nodes at all
     * @throws QdParserException parse error
     * @throws IOException in case that reading errors occur
     */
    public void parse(DocHandler doc, InputStream in, 
        boolean notifyText) throws QdParserException, IOException {
        parse(doc, new InputStreamReader(in), notifyText);
    }
    
    /**
     * Parse a document.
     *
     * @param doc handler for document
     * @param reader input reader containing the document
     * @param notifyText notify <code>doc</code> about text nodes at all
     * @throws QdParserException parse error
     * @throws IOException in case that reading errors occur
     */
    public void parse(DocHandler doc, Reader reader, 
        boolean notifyText) throws QdParserException, IOException {
        doc.startDocument();
        int c = 0;
        while ((c = reader.read()) != -1) {
            // We need to map \r, \r\n, and \n to \n
            // See XML spec section 2.11
            if (c == '\n' && eol) {
                eol = false;
                continue;
            } else if (eol) {
                eol = false;
            } else if ( c == '\n' ) {
                line++;
                col = 0;
            } else if ( c == '\r' ) {
                eol = true;
                c = '\n';
                line++;
                col = 0;
            } else {
                col++;
            }
            if (mode == Mode.DONE) {
                doc.endDocument();
                return;

            } else if (mode == Mode.TEXT) {
                // We are between tags collecting text.
                textNode(doc, c, notifyText);
            } else if (mode == Mode.CLOSE_TAG) {
                // we are processing a closing tag: e.g. </foo>
                closeTag(doc, c);
            } else if (mode == Mode.CDATA) {
                // we are processing CDATA
                cdataSection(doc, c);
            } else if (mode == Mode.COMMENT) {
                // we are processing a comment. We are inside
                // the <!-- .... --> looking for the -->.
                comment(c);
            } else if (mode == Mode.PRE) {
                // We are outside the root tag element
                beforeRoot(c);
            } else if (mode == Mode.DOCTYPE) {
                // We are inside one of these <? ... ?>
                // or one of these <!DOCTYPE ... >
                doctype(c);
            } else if (mode == Mode.START_TAG) {
                // we have just seen a < and
                // are wondering what we are looking at
                // <foo>, </foo>, <!-- ... --->, etc.
                startTag(c);
            } else if (mode == Mode.ENTITY) {
                // we are processing an entity, e.g. &lt;, &#187;, etc.
                entityRef(c);
            } else if (mode == Mode.SINGLE_TAG) {
                // we have just seen something like this:
                // <foo a="b"/
                // and are looking for the final >.
                if (tagName == null) {
                    tagName = sb.toString();
                }
                if ( c != '>' ) {
                    exc("Expected > for tag: <" + tagName + "/>");
                }
                doc.startElement(tagName, attrs);
                doc.endElement(tagName);
                if (depth == 0) {
                    doc.endDocument();
                    return;
                }
                sb.setLength(0);
                attrs = new HashMap<String, String>();
                tagName = null;
                mode = popMode(st);

            } else if (mode == Mode.OPEN_TAG) {
                // we are processing something
                // like this <foo ... >. It could
                // still be a <!-- ... --> or something.
                openTag(doc, c);
            } else if (mode == Mode.QUOTE) {
                // We are processing the quoted right-hand side
                // of an element's attribute.
                quotedValue(c);
            } else if (mode == Mode.ATTRIBUTE_RVALUE) {
                attributeValue(c);
            } else if (mode == Mode.ATTRIBUTE_LVALUE) {
                attributeName(c);
            } else if (mode == Mode.ATTRIBUTE_EQUAL) {
                attributeEquals(c);
            } else if (mode == Mode.IN_TAG) {
                tagContent(doc, c);
            }
        }
        if (mode == Mode.DONE) {
            doc.endDocument();
        } else {
            exc("missing end tag");
        }
    }

    /**
     * Content off an XML element.
     *
     * @param doc document content handler
     * @param ch current char
     * @throws QdParserException parse error
     */
    private void tagContent(DocHandler doc, int ch) throws QdParserException {
        if (ch == '>') {
            mode = popMode(st);
            doc.startElement(tagName, attrs);
            depth++;
            tagName = null;
            attrs = new HashMap<String, String>();
        } else if (ch == '/') {
            mode = Mode.SINGLE_TAG;
        } else if ( !Character.isWhitespace((char) ch)) {
            mode = Mode.ATTRIBUTE_LVALUE;
            sb.append((char) ch);
        }
    }

    /**
     * "=" between attribute name and quoted value.
     *
     * @param ch current char
     * @throws QdParserException parse error
     */
    private void attributeEquals(int ch) throws QdParserException {
        if (ch == '=') {
            mode = Mode.ATTRIBUTE_RVALUE;
        } else if ( !Character.isWhitespace((char) ch )) {
            exc("Error in attribute processing.");
        }
    }

    /**
     * Part of the attribute name.
     *
     * @param ch current char
     */
    private void attributeName(int ch) {
        if ( Character.isWhitespace((char) ch)) {
            lvalue = sb.toString();
            sb.setLength(0);
            mode = Mode.ATTRIBUTE_EQUAL;
        } else if ( ch == '=' ) {
            lvalue = sb.toString();
            sb.setLength(0);
            mode = Mode.ATTRIBUTE_RVALUE;
        } else {
            sb.append((char) ch);
        }
    }

    /**
     * Part of the attribute value.
     *
     * @param ch current char
     * @throws QdParserException parse error
     */
    private void attributeValue(int ch) throws QdParserException {
        if (ch == '"' || ch == '\'') {
            quotec = ch;
            mode = Mode.QUOTE;
        } else if ( !Character.isWhitespace((char) ch)) {
            exc("Error in attribute processing");
        }
    }

    /**
     * Part of a quoted value.
     *
     * @param ch current char
     */
    private void quotedValue(int ch) {
        if (ch == quotec) {
            rvalue = sb.toString();
            sb.setLength(0);
            attrs.put(lvalue, rvalue);
            mode = Mode.IN_TAG;
            // See section the XML spec, section 3.3.3
            // on normalization processing.
        } else if (" \r\n\u0009".indexOf(ch) >= 0) {
            sb.append(' ');
        } else if (ch == '&') {
            st.push(mode);
            mode = Mode.ENTITY;
            etag.setLength(0);
        } else {
            sb.append((char) ch);
        }
    }

    /**
     * Inside an open tag (XML element started).
     *
     * @param doc document content handler
     * @param ch current char
     * @throws QdParserException parse error
     */
    private void openTag(DocHandler doc, int ch) throws QdParserException {
        if (ch == '>') {
            if (tagName == null) {
                tagName = sb.toString();
            }
            sb.setLength(0);
            depth++;
            doc.startElement(tagName, attrs);
            tagName = null;
            attrs = new HashMap<String, String>();
            mode = popMode(st);
        } else if ( ch == '/' ) {
            mode = Mode.SINGLE_TAG;
        } else if ( ch == '-' && same(sb, "!-" )) {
            mode = Mode.COMMENT;
        } else if ( ch == '[' && same(sb, "![CDATA")) {
            mode = Mode.CDATA;
            sb.setLength(0);
        } else if (ch == 'E' && same(sb, "!DOCTYP")) {
            sb.setLength(0);
            mode = Mode.DOCTYPE;
        } else if (Character.isWhitespace((char) ch)) {
            tagName = sb.toString();
            sb.setLength(0);
            mode = Mode.IN_TAG;
        } else {
            sb.append((char) ch);
        }
    }

    /**
     * Reference to an XML Entity.
     *
     * @param ch current char
     * @throws QdParserException parse error
     */
    private void entityRef(int ch) throws QdParserException {
        if (ch == ';') {
            mode = popMode(st);
            String cent = etag.toString();
            etag.setLength(0);
            if (cent.equals("lt")) {
                sb.append('<');
            } else if (cent.equals("gt")) {
                sb.append('>');
            } else if (cent.equals("amp")) {
                sb.append('&');
            } else if (cent.equals("quot")) {
                sb.append('"');
            } else if (cent.equals("apos")) {
                sb.append('\'');
            } else if (cent.startsWith("#")) {
                // Could parse hex entities if we wanted to
                // else if(cent.startsWith("#x"))
                // sb.append((char)Integer.parseInt(cent.substring(2),16));
                sb.append((char) Integer.parseInt(cent.substring(1)));
            } else {
                // Insert custom entity definitions here
                exc("Unknown entity: &" + cent + ";");
            }
        } else {
            etag.append((char) ch);
        }
    }

    /**
     * XML Element start tag.
     *
     * @param ch current char
     */
    private void startTag(int ch) {
        mode = popMode(st);
        if ( ch == '/' ) {
            st.push(mode);
            mode = Mode.CLOSE_TAG;
        } else if ( ch == '?' ) {
            mode = Mode.DOCTYPE;
        } else {
            st.push(mode);
            mode = Mode.OPEN_TAG;
            tagName = null;
            attrs = new HashMap<String, String>();
            sb.append((char) ch);
        }
    }

    /**
     * XML Doctype.
     *
     * @param ch current char
     */
    private void doctype(int ch) {
        if (ch == '>') {
            mode = popMode(st);
            if (mode == Mode.TEXT) {
                mode = Mode.PRE;
            }
        }
    }

    /**
     * Before parsing the XML root element.
     *
     * @param ch current char
     */
    private void beforeRoot(int ch) {
        if (ch == '<') {
            mode = Mode.TEXT;
            st.push(mode);
            mode = Mode.START_TAG;
        }
    }

    /**
     * XML comment.
     *
     * @param ch current char
     */
    private void comment(int ch) {
        if (ch == '>' && endsWith(sb, "--")) {
            sb.setLength(0);
            mode = popMode(st);
        } else {
            sb.append((char) ch);
        }
    }
    
    /**
     * Inside a [CDATA] section.
     *
     * @param doc document content handler
     * @param ch current char
     * @throws QdParserException parse error
     */
    private void cdataSection(DocHandler doc, int ch) throws QdParserException {
        if (ch == '>' && endsWith(sb, "]]")) {
            sb.setLength(sb.length() - 2);
            doc.text(sb.toString());
            sb.setLength(0);
            mode = popMode(st);
        } else {
            sb.append((char) ch);
        }
    }

    /**
     * XML Element closing tag.
     *
     * @param doc document content handler
     * @param ch current char
     * @throws QdParserException parse error
     */
    private void closeTag(DocHandler doc, int ch) throws QdParserException {
        if (ch == '>') {
            mode = popMode(st);
            tagName = sb.toString();
            sb.setLength(0);
            depth--;
            if (depth == 0) {
                mode = Mode.DONE;
            }
            doc.endElement(tagName);
        } else {
            sb.append((char) ch);
        }
    }

    /**
     * Text node.
     *
     * @param doc document content handler
     * @param ch current char
     * @param notify notify <code>doc</code> or not
     * @throws QdParserException parse error
     */
    private void textNode(DocHandler doc, int ch, boolean notify) 
        throws QdParserException {
        if (ch == '<') {
            st.push(mode);
            mode = Mode.START_TAG;
            if ( sb.length() > 0 ) {
                if (notify) {
                    doc.text(sb.toString());
                }
                sb.setLength(0);
            }
        } else if (ch == '&') {
            st.push(mode);
            mode = Mode.ENTITY;
            etag.setLength(0);
        } else {
            sb.append((char) ch);
        }
    }

    /**
     * Throws an exception with parse location.
     *
     * @param msg message
     * @throws QdParserException formatted exception
     */
    private void exc(String msg) throws QdParserException {
        throw new QdParserException(msg + " near line " + line 
            + ", column " + col);
    }
 
    
    /**
     * Just testing.
     * 
     * @param args ignored
     * 
     * @since 1.00
     */
    public static void main(String[] args) {
        try {
            Reader reader = new java.io.FileReader("src\\test\\TimerTest.xml");
            QdParser parser = new QdParser();
            parser.parse(new DocHandler() {
                
                @Override
                public void text(String str) throws QdParserException {
                    System.out.println("TEXT " + str);
                }
                
                @Override
                public void startElement(String tag, 
                    HashMap<String, String> atributes)
                    throws QdParserException {
                    System.out.println("END ELT " + tag + " " + atributes);
                }
                
                @Override
                public void startDocument() throws QdParserException {
                    System.out.println("START DOC ");                
                }
                
                @Override
                public void endElement(String tag) throws QdParserException {
                    System.out.println("END ELT " + tag);
                }
                
                @Override
                public void endDocument() throws QdParserException {
                    System.out.println("END DOC");
                }
            }, reader, false);
            reader.close();
        } catch (QdParserException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}

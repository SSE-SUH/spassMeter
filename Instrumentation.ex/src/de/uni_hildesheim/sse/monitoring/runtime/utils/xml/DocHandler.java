package de.uni_hildesheim.sse.monitoring.runtime.utils.xml;

import de.uni_hildesheim.sse.monitoring.runtime.utils.HashMap;

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

/**
* Defines an interface to link program specific actions with the 
* state of the {@link QdParser}.
* 
* @author Nicolas De Loof (taken from <code>http://www.koders.com/java</code>),
*   Holger Eichelberger (adaptation to conventions)
* @version 1.0
*/
public interface DocHandler {
    
   /**
    * Start of an XML Element.
    *
    * @param tag element name
    * @param attributes element attributes
    * @throws QdParserException parse error
    */
    void startElement(String tag, HashMap<String, String> attributes) 
        throws QdParserException;

   /**
    * End of an XML element.
    *
    * @param tag element name
    * @throws QdParserException parse error
    */
    void endElement(String tag) throws QdParserException;

   /**
    * Start of an XML Document.
    *
    * @throws QdParserException parse error
    */
    void startDocument() throws QdParserException;

   /**
    * End of an XML Document.
    *
    * @throws QdParserException parse error
    */
    void endDocument() throws QdParserException;

   /**
    * Text node content.
    *
    * @param str node content
    * @throws QdParserException parse error
    */
    void text(String str) throws QdParserException;
    
}
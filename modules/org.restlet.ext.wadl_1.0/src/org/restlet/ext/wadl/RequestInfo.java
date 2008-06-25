/*
 * Copyright 2005-2008 Noelios Consulting.
 * 
 * The contents of this file are subject to the terms of the Common Development
 * and Distribution License (the "License"). You may not use this file except in
 * compliance with the License.
 * 
 * You can obtain a copy of the license at
 * http://www.opensource.org/licenses/cddl1.txt See the License for the specific
 * language governing permissions and limitations under the License.
 * 
 * When distributing Covered Code, include this CDDL HEADER in each file and
 * include the License file at http://www.opensource.org/licenses/cddl1.txt If
 * applicable, add the following below this CDDL HEADER, with the fields
 * enclosed by brackets "[]" replaced with your own identifying information:
 * Portions Copyright [yyyy] [name of copyright owner]
 */

package org.restlet.ext.wadl;

import java.util.ArrayList;
import java.util.List;

import org.restlet.util.XmlWriter;
import org.xml.sax.SAXException;

/**
 * Describes the properties of a request associated to a parent method.
 * 
 * @author Jerome Louvel
 */
public class RequestInfo {

    /** Doc elements used to document that element. */
    private List<DocumentationInfo> documentations;

    /** List of parameters. */
    private List<ParameterInfo> parameters;

    /** List of supported input representations. */
    private List<RepresentationInfo> representations;

    /**
     * Returns the list of documentation elements.
     * 
     * @return The list of documentation elements.
     */
    public List<DocumentationInfo> getDocumentations() {
        // Lazy initialization with double-check.
        List<DocumentationInfo> d = this.documentations;
        if (d == null) {
            synchronized (this) {
                d = this.documentations;
                if (d == null)
                    this.documentations = d = new ArrayList<DocumentationInfo>();
            }
        }
        return d;
    }

    /**
     * Returns the list of parameters.
     * 
     * @return The list of parameters.
     */
    public List<ParameterInfo> getParameters() {
        // Lazy initialization with double-check.
        List<ParameterInfo> p = this.parameters;
        if (p == null) {
            synchronized (this) {
                p = this.parameters;
                if (p == null)
                    this.parameters = p = new ArrayList<ParameterInfo>();
            }
        }
        return p;
    }

    /**
     * Returns the list of supported input representations.
     * 
     * @return The list of supported input representations.
     */
    public List<RepresentationInfo> getRepresentations() {
        // Lazy initialization with double-check.
        List<RepresentationInfo> r = this.representations;
        if (r == null) {
            synchronized (this) {
                r = this.representations;
                if (r == null)
                    this.representations = r = new ArrayList<RepresentationInfo>();
            }
        }
        return r;
    }

    /**
     * Sets the list of documentation elements.
     * 
     * @param doc
     *                The list of documentation elements.
     */
    public void setDocumentations(List<DocumentationInfo> doc) {
        this.documentations = doc;
    }

    /**
     * Sets the list of parameters.
     * 
     * @param parameters
     *                The list of parameters.
     */
    public void setParameters(List<ParameterInfo> parameters) {
        this.parameters = parameters;
    }

    /**
     * Sets the list of supported input representations.
     * 
     * @param representations
     *                The list of supported input representations.
     */
    public void setRepresentations(List<RepresentationInfo> representations) {
        this.representations = representations;
    }

    /**
     * Writes the current object as an XML element using the given SAX writer.
     * 
     * @param writer
     *                The SAX writer.
     * @throws SAXException
     */
    public void writeElement(XmlWriter writer) throws SAXException {
        if (getDocumentations().isEmpty() && getParameters().isEmpty()
                && getRepresentations().isEmpty()) {
            writer.emptyElement("", "request");
        } else {
            writer.startElement("", "request");

            for (DocumentationInfo documentationInfo : getDocumentations()) {
                documentationInfo.writeElement(writer);
            }

            for (ParameterInfo parameterInfo : getParameters()) {
                parameterInfo.writeElement(writer);
            }

            for (RepresentationInfo representationInfo : getRepresentations()) {
                representationInfo.writeElement(writer);
            }

            writer.endElement("", "request");
        }
    }

}

/**
 * Copyright 2005-2014 Restlet
 * 
 * The contents of this file are subject to the terms of one of the following
 * open source licenses: Apache 2.0 or LGPL 3.0 or LGPL 2.1 or CDDL 1.0 or EPL
 * 1.0 (the "Licenses"). You can select the license that you prefer but you may
 * not use this file except in compliance with one of these Licenses.
 * 
 * You can obtain a copy of the Apache 2.0 license at
 * http://www.opensource.org/licenses/apache-2.0
 * 
 * You can obtain a copy of the LGPL 3.0 license at
 * http://www.opensource.org/licenses/lgpl-3.0
 * 
 * You can obtain a copy of the LGPL 2.1 license at
 * http://www.opensource.org/licenses/lgpl-2.1
 * 
 * You can obtain a copy of the CDDL 1.0 license at
 * http://www.opensource.org/licenses/cddl1
 * 
 * You can obtain a copy of the EPL 1.0 license at
 * http://www.opensource.org/licenses/eclipse-1.0
 * 
 * See the Licenses for the specific language governing permissions and
 * limitations under the Licenses.
 * 
 * Alternatively, you can obtain a royalty free commercial license with less
 * limitations, transferable or non-transferable, directly at
 * http://restlet.com/products/restlet-framework
 * 
 * Restlet is a registered trademark of Restlet S.A.S.
 */

package org.restlet.ext.jaxrs.internal.resteasy;

import java.io.IOException;
import java.io.OutputStream;

import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.NewCookie;

import org.jboss.resteasy.spi.HttpResponse;

/**
 * RESTEasy HTTP response wrapper for Restlet requests.
 * 
 * @author Jerome Louvel
 */
public class RestletHttpResponse implements HttpResponse {

    @Override
    public void addNewCookie(NewCookie arg0) {
        // TODO Auto-generated method stub

    }

    @Override
    public MultivaluedMap<String, Object> getOutputHeaders() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public OutputStream getOutputStream() throws IOException {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public int getStatus() {
        // TODO Auto-generated method stub
        return 0;
    }

    @Override
    public boolean isCommitted() {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public void reset() {
        // TODO Auto-generated method stub

    }

    @Override
    public void sendError(int arg0) throws IOException {
        // TODO Auto-generated method stub

    }

    @Override
    public void sendError(int arg0, String arg1) throws IOException {
        // TODO Auto-generated method stub

    }

    @Override
    public void setOutputStream(OutputStream arg0) {
        // TODO Auto-generated method stub

    }

    @Override
    public void setStatus(int arg0) {
        // TODO Auto-generated method stub

    }

}
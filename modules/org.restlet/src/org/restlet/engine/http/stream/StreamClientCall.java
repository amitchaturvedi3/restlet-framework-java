/**
 * Copyright 2005-2009 Noelios Technologies.
 * 
 * The contents of this file are subject to the terms of one of the following
 * open source licenses: LGPL 3.0 or LGPL 2.1 or CDDL 1.0 or EPL 1.0 (the
 * "Licenses"). You can select the license that you prefer but you may not use
 * this file except in compliance with one of these Licenses.
 * 
 * You can obtain a copy of the LGPL 3.0 license at
 * http://www.opensource.org/licenses/lgpl-3.0.html
 * 
 * You can obtain a copy of the LGPL 2.1 license at
 * http://www.opensource.org/licenses/lgpl-2.1.php
 * 
 * You can obtain a copy of the CDDL 1.0 license at
 * http://www.opensource.org/licenses/cddl1.php
 * 
 * You can obtain a copy of the EPL 1.0 license at
 * http://www.opensource.org/licenses/eclipse-1.0.php
 * 
 * See the Licenses for the specific language governing permissions and
 * limitations under the Licenses.
 * 
 * Alternatively, you can obtain a royalty free commercial license with less
 * limitations, transferable or non-transferable, directly at
 * http://www.noelios.com/products/restlet-engine
 * 
 * Restlet is a registered trademark of Noelios Technologies.
 */

package org.restlet.engine.http.stream;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.UnknownHostException;
import java.nio.channels.ReadableByteChannel;
import java.nio.channels.WritableByteChannel;
import java.util.logging.Level;

import javax.net.SocketFactory;

import org.restlet.Request;
import org.restlet.data.Method;
import org.restlet.data.Parameter;
import org.restlet.data.Reference;
import org.restlet.data.Status;
import org.restlet.engine.http.ClientCall;
import org.restlet.engine.http.header.HeaderUtils;
import org.restlet.engine.http.header.HeaderConstants;
import org.restlet.engine.http.io.ChunkedInputStream;
import org.restlet.engine.http.io.ChunkedOutputStream;
import org.restlet.engine.http.io.InputEntityStream;
import org.restlet.engine.http.io.KeepAliveOutputStream;
import org.restlet.engine.http.io.ClosingRepresentation;
import org.restlet.representation.Representation;

/**
 * HTTP client call based on streams.
 * 
 * @author Jerome Louvel
 */
public class StreamClientCall extends ClientCall {

    /**
     * Returns the absolute request URI.
     * 
     * @param resourceRef
     *            The resource reference.
     * @return The absolute request URI.
     */
    private static String getRequestUri(Reference resourceRef) {
        Reference absoluteRef = resourceRef.isAbsolute() ? resourceRef
                : resourceRef.getTargetRef();
        if (absoluteRef.hasQuery()) {
            return absoluteRef.getPath() + "?" + absoluteRef.getQuery();
        }

        return absoluteRef.getPath();
    }

    /** The socket factory. */
    private final SocketFactory factory;

    /** The request entity output stream. */
    private volatile OutputStream requestEntityStream;

    /** The request output stream. */
    private volatile OutputStream requestStream;

    /** The response input stream. */
    private volatile InputStream responseStream;

    /** The request socket */
    private volatile Socket socket;

    /**
     * Constructor.
     * 
     * @param helper
     *            The client connector helper.
     * @param request
     *            The request to send.
     */
    public StreamClientCall(StreamClientHelper helper, Request request,
            SocketFactory factory) {
        // The path of the request uri must not be empty.
        super(helper, request.getMethod().toString(), getRequestUri(request
                .getResourceRef()));

        // Set the HTTP version
        setVersion("HTTP/1.1");
        this.factory = factory;
    }

    /**
     * Creates the socket that will be used to send the request and get the
     * response.
     * 
     * @param hostDomain
     *            The target host domain name.
     * @param hostPort
     *            The target host port.
     * @return The created socket.
     * @throws UnknownHostException
     * @throws IOException
     */
    public Socket createSocket(String hostDomain, int hostPort)
            throws UnknownHostException, IOException {
        Socket result = null;

        if (factory != null) {
            result = factory.createSocket();
            InetSocketAddress address = new InetSocketAddress(hostDomain,
                    hostPort);
            result.connect(address, getHelper().getConnectTimeout());
        }

        return result;
    }

    @Override
    public StreamClientHelper getHelper() {
        return (StreamClientHelper) super.getHelper();
    }

    @Override
    protected Representation getRepresentation(InputStream stream) {
        Representation result = super.getRepresentation(stream);
        return new ClosingRepresentation(result, this.socket, getHelper()
                .getLogger());
    }

    @Override
    public WritableByteChannel getRequestEntityChannel() {
        return null;
    }

    @Override
    public OutputStream getRequestEntityStream() {
        if (this.requestEntityStream == null) {
            if (isRequestChunked()) {
                if (isKeepAlive()) {
                    this.requestEntityStream = new ChunkedOutputStream(
                            new KeepAliveOutputStream(getRequestHeadStream()));
                } else {
                    this.requestEntityStream = new ChunkedOutputStream(
                            getRequestHeadStream());
                }
            } else {
                this.requestEntityStream = new KeepAliveOutputStream(
                        getRequestHeadStream());
            }
        }

        return this.requestEntityStream;
    }

    @Override
    public OutputStream getRequestHeadStream() {
        return this.requestStream;
    }

    @Override
    public ReadableByteChannel getResponseEntityChannel(long size) {
        return null;
    }

    @Override
    public InputStream getResponseEntityStream(long size) {
        if (isResponseChunked()) {
            return new ChunkedInputStream(getResponseStream());
        } else if (size >= 0) {
            return new InputEntityStream(getResponseStream(), size);
        } else {
            return getResponseStream();
        }
    }

    /**
     * Returns the underlying HTTP response stream.
     * 
     * @return The underlying HTTP response stream.
     */
    private InputStream getResponseStream() {
        return this.responseStream;
    }

    @Override
    protected boolean isClientKeepAlive() {
        return false;
    }

    /**
     * Parses the HTTP response.
     * 
     * @throws IOException
     */
    protected void parseResponse() throws IOException {
        StringBuilder sb = new StringBuilder();

        // Parse the HTTP version
        int next = getResponseStream().read();
        while ((next != -1) && !HeaderUtils.isSpace(next)) {
            sb.append((char) next);
            next = getResponseStream().read();
        }

        if (next == -1) {
            throw new IOException(
                    "Unable to parse the response HTTP version. End of stream reached too early.");
        }

        setVersion(sb.toString());
        sb.delete(0, sb.length());

        // Parse the status code
        next = getResponseStream().read();
        while ((next != -1) && !HeaderUtils.isSpace(next)) {
            sb.append((char) next);
            next = getResponseStream().read();
        }

        if (next == -1) {
            throw new IOException(
                    "Unable to parse the response status. End of stream reached too early.");
        }

        setStatusCode(Integer.parseInt(sb.toString()));
        sb.delete(0, sb.length());

        // Parse the reason phrase
        next = getResponseStream().read();
        while ((next != -1) && !HeaderUtils.isCarriageReturn(next)) {
            sb.append((char) next);
            next = getResponseStream().read();
        }

        if (next == -1) {
            throw new IOException(
                    "Unable to parse the reason phrase. End of stream reached too early.");
        }

        next = getResponseStream().read();

        if (HeaderUtils.isLineFeed(next)) {
            setReasonPhrase(sb.toString());
            sb.delete(0, sb.length());

            // Parse the headers
            Parameter header = HeaderUtils.readHeader(getResponseStream(), sb);
            while (header != null) {
                getResponseHeaders().add(header);
                header = HeaderUtils.readHeader(getResponseStream(), sb);
            }
        } else {
            throw new IOException(
                    "Unable to parse the reason phrase. The carriage return must be followed by a line feed.");
        }
    }

    @Override
    public Status sendRequest(Request request) {
        Status result = null;

        try {
            // Resolve relative references
            Reference resourceRef = request.getResourceRef().isRelative() ? request
                    .getResourceRef().getTargetRef()
                    : request.getResourceRef();

            // Extract the host info
            String hostDomain = resourceRef.getHostDomain();
            int hostPort = resourceRef.getHostPort();

            if (hostPort == -1) {
                if (resourceRef.getSchemeProtocol() != null) {
                    hostPort = resourceRef.getSchemeProtocol().getDefaultPort();
                } else {
                    hostPort = getProtocol().getDefaultPort();
                }
            }

            // Create and connect the client socket
            this.socket = createSocket(hostDomain, hostPort);

            if (this.socket == null) {
                getHelper().getLogger().log(Level.SEVERE,
                        "Unable to create the client socket.");
                result = new Status(Status.CONNECTOR_ERROR_INTERNAL,
                        "Unable to create the client socket.");
            } else {
                this.socket.setTcpNoDelay(getHelper().getTcpNoDelay());

                this.requestStream = new BufferedOutputStream(this.socket
                        .getOutputStream());
                this.responseStream = new BufferedInputStream(this.socket
                        .getInputStream());

                // Write the request line
                getRequestHeadStream().write(getMethod().getBytes());
                getRequestHeadStream().write(' ');
                getRequestHeadStream().write(getRequestUri().getBytes());
                getRequestHeadStream().write(' ');
                getRequestHeadStream().write(getVersion().getBytes());
                HeaderUtils.writeCRLF(getRequestHeadStream());

                if (shouldRequestBeChunked(request)) {
                    getRequestHeaders().set(
                            HeaderConstants.HEADER_TRANSFER_ENCODING,
                            "chunked", true);
                }

                // We don't support persistent connections yet
                getRequestHeaders().set(HeaderConstants.HEADER_CONNECTION,
                        "close", isClientKeepAlive());

                // Prepare the host header
                String host = hostDomain;
                if (resourceRef.getHostPort() != -1) {
                    host += ":" + resourceRef.getHostPort();
                }
                getRequestHeaders()
                        .set(HeaderConstants.HEADER_HOST, host, true);

                // Write the request headers
                for (Parameter header : getRequestHeaders()) {
                    HeaderUtils.writeHeader(header, getRequestHeadStream());
                }

                // TODO may be replaced by an attribute on the Method class
                // telling that a method requires an entity.
                // Actually, since such classes are used in the context of
                // clients and servers, there could be two attributes
                if ((request.getEntity() == null
                        || !request.isEntityAvailable() || request.getEntity()
                        .getSize() == 0)
                        && (Method.POST.equals(request.getMethod()) || Method.PUT
                                .equals(request.getMethod()))) {
                    HeaderUtils.writeHeader(new Parameter(
                            HeaderConstants.HEADER_CONTENT_LENGTH, "0"),
                            getRequestHeadStream());
                }

                // Write the end of the headers section
                HeaderUtils.writeCRLF(getRequestHeadStream());
                getRequestHeadStream().flush();

                // Write the request body
                result = super.sendRequest(request);

                if (result.equals(Status.CONNECTOR_ERROR_COMMUNICATION)) {
                    return result;
                }

                // Parse the response
                parseResponse();

                // Build the result
                result = new Status(getStatusCode(), null, getReasonPhrase(),
                        null);
            }
        } catch (IOException ioe) {
            getHelper()
                    .getLogger()
                    .log(
                            Level.FINE,
                            "An error occured during the communication with the remote HTTP server.",
                            ioe);
            result = new Status(Status.CONNECTOR_ERROR_COMMUNICATION, ioe);
        }

        return result;
    }
}

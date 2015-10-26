package edu.ucsd.genie.connectionmanager;

import java.net.URI;

import org.apache.http.client.methods.HttpEntityEnclosingRequestBase;

/**
 * Http request that contains a delete method.
 */
class HttpDelete extends HttpEntityEnclosingRequestBase {
    public static final String METHOD_NAME = "DELETE";
    
    @Override
	public String getMethod() { return METHOD_NAME; }

    public HttpDelete(final String uri) {
        super();
        setURI(URI.create(uri));
    }
    public HttpDelete(final URI uri) {
        super();
        setURI(uri);
    }
    public HttpDelete() { super(); }
}

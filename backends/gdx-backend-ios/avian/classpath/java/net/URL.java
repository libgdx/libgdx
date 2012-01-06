/* Copyright (c) 2008-2009, Avian Contributors

   Permission to use, copy, modify, and/or distribute this software
   for any purpose with or without fee is hereby granted, provided
   that the above copyright notice and this permission notice appear
   in all copies.

   There is NO WARRANTY for this software.  See license.txt for
   details. */

package java.net;

import java.io.IOException;
import java.io.InputStream;

public final class URL {
  private final URLStreamHandler handler;
  private String protocol;
  private String host;
  private int port;
  private String file;
  private String path;
  private String query;
  private String ref;

  public URL(String s) throws MalformedURLException {
    int colon = s.indexOf(':');
    int slash = s.indexOf('/');
    if (colon > 0 && (slash < 0 || colon < slash)) {
      handler = findHandler(s.substring(0, colon));
      handler.parseURL(this, s, colon + 1, s.length());
    } else {
      throw new MalformedURLException(s);
    }
  }

  public String toString() {
    return handler.toExternalForm(this);
  }

  public String getProtocol() {
    return protocol;
  }

  public String getHost() {
    return host;
  }

  public int getPort() {
    return port;
  }

  public String getFile() {
    return file;
  }

  public String getRef() {
    return ref;
  }
  
  public String getPath() {
    return path;
  }
  
  public String getQuery() {
    return query;
  }

  public URLConnection openConnection() throws IOException {
    return handler.openConnection(this);
  }

  public InputStream openStream() throws IOException {
    return openConnection().getInputStream();
  }

  public Object getContent() throws IOException {
    return openStream();
  }

  private static URLStreamHandler findHandler(String protocol)
    throws MalformedURLException
  {
    if ("resource".equals(protocol)) {
      return new avian.resource.Handler();
    } else if ("file".equals(protocol)) {
      return new avian.file.Handler();
    } else if ("jar".equals(protocol)) {
      return new avian.jar.Handler();
    } else {
      throw new MalformedURLException("unknown protocol: " + protocol);
    }
  }

  public void set(String protocol, String host, int port, String file,
                  String ref)
  {
    this.protocol = protocol;
    this.host = host;
    this.port = port;
    this.file = file;
    this.ref = ref;
    
    int q = file.lastIndexOf('?');
    if (q != -1) {
      this.query = file.substring(q + 1);
      this.path = file.substring(0, q);
    } else {
      this.path = file;
    }
  }
}

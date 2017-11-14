/*
 * Copyright 2000-2016 Namics AG. All rights reserved.
 */

package com.namics.oss.java.binarystore;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * Binary representation, container to save binary content without metadata to a persistence store.
 *
 * @author aschaefer, Namics AG
 * @since 01.03.16 17:11
 */
public interface Binary {
	/**
	 * Id of the binary.
	 *
	 * @return id.
	 */
	String getId();

	/**
	 * Get an input stream to read content of binary.
	 * The <b>caller</b> is responsible for stream management.
	 * A new stream is return on each call to <code>getInputStream</code>
	 *
	 * @return a new input stream for this binary
	 * @throws IOException stream creation failed.
	 */
	InputStream getInputStream() throws IOException;

	/**
	 * Get an output stream to write content of binary.
	 * The <b>caller</b> is responsible for stream management.
	 * A new stream is return on each call to <code>getOutputStream</code>
	 *
	 * @return a new output stream for this binary.
	 * @throws IOException stream creation failed.
	 */
	OutputStream getOutputStream() throws IOException;
}

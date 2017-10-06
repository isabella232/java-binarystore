/*
 * Copyright 2000-2016 Namics AG. All rights reserved.
 */

package com.namics.oss.java.binarystore.filesystem;

import com.namics.oss.java.binarystore.Binary;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import static com.namics.commons.utils.Assert.notNull;

/**
 * File system stored binary.
 *
 * @author aschaefer, Namics AG
 * @since 01.03.16 17:33
 */
public class BinaryFilesystemImpl implements Binary {

	private final String id;
	private final File file;

	public BinaryFilesystemImpl(String id, File file) throws IOException {
		notNull(id);
		notNull(file);
		if (!file.exists()) {
			file.createNewFile();
		}
		this.id = id;
		this.file = file;
	}

	@Override
	public String getId() {
		return this.id;
	}

	@Override
	public InputStream getInputStream() throws FileNotFoundException {
		return new FileInputStream(file);
	}

	@Override
	public OutputStream getOutputStream() throws FileNotFoundException {
		return new FileOutputStream(file);
	}

	@Override
	public String toString() {
		return "BinaryFilesystemImpl{" +
		       "id='" + id + '\'' +
		       ", file=" + file +
		       '}';
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) {
			return true;
		}
		if (o == null || getClass() != o.getClass()) {
			return false;
		}

		BinaryFilesystemImpl that = (BinaryFilesystemImpl) o;

		return id != null ? id.equals(that.id) : that.id == null;

	}

	@Override
	public int hashCode() {
		return id != null ? id.hashCode() : 0;
	}
}

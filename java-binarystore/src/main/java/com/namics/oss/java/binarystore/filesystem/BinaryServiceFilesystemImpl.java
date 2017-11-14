/*
 * Copyright 2000-2016 Namics AG. All rights reserved.
 */

package com.namics.oss.java.binarystore.filesystem;

import com.namics.oss.java.binarystore.Binary;
import com.namics.oss.java.binarystore.BinaryService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;
import java.util.function.Supplier;

import static com.namics.oss.java.tools.utils.Assert.isTrue;
import static com.namics.oss.java.tools.utils.Assert.notNull;
import static java.util.Collections.emptySet;
import static java.util.stream.Collectors.toSet;

/**
 * BinaryService implementation that uses local files system for persistence.
 *
 * @author aschaefer, Namics AG
 * @since 01.03.16 17:18
 */
public class BinaryServiceFilesystemImpl implements BinaryService {

	private static final Logger LOG = LoggerFactory.getLogger(BinaryServiceFilesystemImpl.class);

	public static final int MIN_DEPTH = 0;
	public static final int DEFAULT_DEPTH = 0;

	public static final int DIR_CHAR_COUNT = 3;

	protected final File baseDirectory;
	protected final int directoryDepth;
	protected Supplier<String> idGenerator = () -> UUID.randomUUID().toString();

	public BinaryServiceFilesystemImpl(String baseDirectory) {
		this(new File(baseDirectory));
	}

	public BinaryServiceFilesystemImpl(File baseDirectory) {
		this(baseDirectory, DEFAULT_DEPTH);
	}

	public BinaryServiceFilesystemImpl(String baseDirectory, int directoryDepth) {
		this(new File(baseDirectory), directoryDepth);
	}

	public BinaryServiceFilesystemImpl(File baseDirectory, int directoryDepth) {
		LOG.info("Initialize with directoryDepth {} in base directory: {}", directoryDepth, baseDirectory);
		isTrue(directoryDepth >= MIN_DEPTH, "directoryDepth must be >= 0");
		notNull(baseDirectory);
		if (baseDirectory.exists()) {
			if (!baseDirectory.isDirectory()) {
				throw new IllegalArgumentException(baseDirectory + " is not a directory");
			}
		}
		if (!baseDirectory.isDirectory()) {
			boolean mkdirs = baseDirectory.mkdirs();
			if (!mkdirs) {
				throw new IllegalArgumentException(baseDirectory + " could not be created");
			}
		}
		if (!baseDirectory.canRead()) {
			throw new IllegalArgumentException(baseDirectory + " cannot be read");
		}

		if (!baseDirectory.canWrite()) {
			throw new IllegalArgumentException(baseDirectory + " cannot be written");
		}

		this.baseDirectory = baseDirectory;
		this.directoryDepth = directoryDepth;
	}

	@Override
	public Binary create() {
		String id = this.idGenerator.get();
		LOG.debug("Create file for id {}", id);
		return mapBinary(buildFile(id), id);
	}


	@Override
	public Binary get(String id) throws IllegalArgumentException {
		File file = buildFile(id);
		if (!file.exists()) {
			throw new IllegalArgumentException("Binary does not exist: " + id);
		}
		return mapBinary(file, id);
	}

	@Override
	public boolean exist(String id) {
		return buildFile(id).isFile();
	}

	@Override
	public boolean delete(String id) {
		File file = buildFile(id);
		return file.delete();
	}

	@Override
	public Set<Binary> findAll() {
		return findAllIds().stream()
		                   .map(this::get)
		                   .collect(toSet());
	}

	@Override
	public Set<String> findAllIds() {
		Path path = Paths.get(baseDirectory.getAbsolutePath());
		final Set<String> files = new HashSet<>();
		try {
			Files.walkFileTree(path, new SimpleFileVisitor<Path>() {
				@Override
				public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
					if (!attrs.isDirectory()) {
						String filename = file.getFileName().toString();
						if (exist(filename)) {
							files.add(filename);
						}
					}
					return FileVisitResult.CONTINUE;
				}
			});
			return files;
		} catch (IOException e) {
			LOG.error("Problem reading tree {} : {}", path, e, null);
			LOG.debug("Problem reading tree {} : {}", path, e, e);
		}
		return emptySet();
	}

	protected BinaryFilesystemImpl mapBinary(File file, String name) {
		try {
			return new BinaryFilesystemImpl(name, file);
		} catch (IOException e) {
			LOG.error("Failed to map binary name={} file={} : {}", name, file, e, null);
			LOG.debug("Failed to map binary name={} file={} : {}", name, file, e, e);
			return null;
		}
	}

	protected File buildFile(String id) {
		File directory = new File(baseDirectory + "/" + buildPath(id));
		if (!directory.isDirectory()) {
			LOG.debug("Create dirs {}", directory);
			directory.mkdirs();
		}
		return new File(directory, id);
	}


	protected String buildPath(String id) {

		//remove non word chars
		String sequence = id.replaceAll("\\W+", "");
		int length = sequence.length();

		StringBuilder path = new StringBuilder();

		int level = 0; // current dir level

		// iterate over chars and add / after each 3(DIR_CHAR_COUNT) chars, last char must always be /
		for (int charIndex = 0; charIndex < length; charIndex++) {
			if (level < directoryDepth) {

				path.append(sequence.charAt(charIndex)); // append current char

				if (dirNameComplete(charIndex)) {
					level++;
					path.append('/');

				} else if (lastChar(charIndex, length)) {
					path.append('/');
				}
			}
		}
		return path.toString();
	}

	protected boolean lastChar(int charIndex, int length) {
		return charIndex == length - 1;
	}

	protected boolean dirNameComplete(int charIndex) {
		return (charIndex + 1) % DIR_CHAR_COUNT == 0;
	}

	/**
	 * Use a custom id generator, default generator creates UUIDs.
	 *
	 * @param idGenerator generator to be used
	 */
	public void setIdGenerator(Supplier<String> idGenerator) {
		this.idGenerator = idGenerator;
	}

	/**
	 * Use a custom id generator, default generator creates UUIDs.
	 *
	 * @param idGenerator generator to be used
	 * @return this service instance to enable chained configuration.
	 */
	public BinaryServiceFilesystemImpl idGenerator(Supplier<String> idGenerator) {
		setIdGenerator(idGenerator);
		return this;
	}
}

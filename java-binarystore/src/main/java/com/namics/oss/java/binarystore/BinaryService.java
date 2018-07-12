/*
 * Copyright 2000-2016 Namics AG. All rights reserved.
 */

package com.namics.oss.java.binarystore;

import java.util.Set;

import static java.util.stream.Collectors.toSet;

/**
 * BinaryStore.
 *
 * @author aschaefer, Namics AG
 * @since 01.03.16 17:10
 */
public interface BinaryService {

	/**
	 * Create a new binary with a unique idGenerator, input and output stream ready to read/write.
	 *
	 * @return Binary
	 */
	Binary create();

	/**
	 * Create a new binary with a given id.
	 *
	 * @param id the given id
	 * @return the created binary
	 */
	Binary create(String id);

	/**
	 * Get an existing with by unique idGenerator, input and output stream ready to read/write.
	 *
	 * @param id of binary to get
	 * @return binary if exists, null if not found
	 */
	Binary get(String id) throws IllegalArgumentException;

	/**
	 * Check if binary with this idGenerator exists
	 *
	 * @param id check if binary with this idGenerator exists in store
	 * @return true if file exists
	 */
	boolean exist(String id);

	/**
	 * Delete binary with this idGenerator, this operation is silent, there is no feedback of error / success.
	 *
	 * @param id idGenerator of binary to be deleted
	 * @return true if file is deleted
	 */
	boolean delete(String id);

	/**
	 * Get a set of all available binaries.
	 *
	 * @return list of binaries.
	 */
	Set<Binary> findAll();

	/**
	 * Get a set of ids of all available binaries.
	 *
	 * @return list of ids.
	 */
	default Set<String> findAllIds() {
		return findAll().stream()
		                .map(Binary::getId)
		                .collect(toSet());
	}
}

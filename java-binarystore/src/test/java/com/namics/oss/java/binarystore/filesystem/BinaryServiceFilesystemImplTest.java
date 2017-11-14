/*
 * Copyright 2000-2016 Namics AG. All rights reserved.
 */

package com.namics.oss.java.binarystore.filesystem;

import com.namics.oss.java.binarystore.Binary;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.hamcrest.Matchers;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static com.google.common.collect.Iterables.toArray;
import static org.hamcrest.Matchers.*;
import static org.junit.Assert.assertThat;

/**
 * FilesystemBinaryStoreImplTest.
 *
 * @author aschaefer, Namics AG
 * @since 01.03.16 17:27
 */
public class BinaryServiceFilesystemImplTest {


    private static final Logger LOG = LoggerFactory.getLogger(BinaryServiceFilesystemImplTest.class);

    protected static final String DIR = System.getProperty("java.io.tmpdir") + "/" + BinaryServiceFilesystemImplTest.class.getSimpleName();
    protected static final File DIRECTORY = new File(DIR);

    BinaryServiceFilesystemImpl store = new BinaryServiceFilesystemImpl(new File(DIR));

    @BeforeClass
    public static void initTestDir() throws Exception {
        if (!DIRECTORY.isDirectory()) {
            LOG.info("Create working dir {}", DIRECTORY);
            DIRECTORY.mkdirs();
        }
    }

    @AfterClass
    public static void deleteTestDir() throws Exception {
        LOG.info("Delete working dir {}", DIRECTORY);
        FileUtils.deleteDirectory(DIRECTORY);
    }

    @Test
    public void testExistFalse() throws Exception {
        assertThat(store.exist("TEST"), is(false));
    }

    @Test
    public void testExistTrue() throws Exception {
        Binary binary = store.create();
        assertThat(store.exist(binary.getId()), is(true));
        store.delete(binary.getId());
    }

    @Test
    public void testDelete() throws Exception {
        Binary binary = store.create();
        assertThat(store.exist(binary.getId()), is(true));
        store.delete(binary.getId());
        assertThat(store.exist(binary.getId()), is(false));
    }

    @Test
    public void testCreateNew() throws Exception {
        Binary binary = store.create();
        String id = binary.getId();
        assertThat(binary.getId(), Matchers.notNullValue());
        assertThat(binary.getInputStream(), Matchers.notNullValue());
        assertThat(binary.getOutputStream(), Matchers.notNullValue());
        store.delete(id);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testGetBinaryNotFound() throws Exception {
        store.get("test");
    }

    @Test(expected = IllegalArgumentException.class)
    public void testGetBinary() throws Exception {
        Binary binary = store.create();
        String id = binary.getId();
        Binary read = store.get("test");
        assertThat(read.getId(), equalTo(id));
        assertThat(read.getInputStream(), Matchers.notNullValue());
        assertThat(read.getOutputStream(), Matchers.notNullValue());
        store.delete(id);
    }

    @Test
    public void testCrudDepth3() throws Exception {
        BinaryServiceFilesystemImpl service = new BinaryServiceFilesystemImpl(DIR, 3);
        Binary binary = service.create();
        String id = binary.getId();
        try (OutputStream outputStream = binary.getOutputStream()) {
            outputStream.write("TEST".getBytes("UTF-8"));
        }
        try (InputStream inputStream = binary.getInputStream()) {
            List<String> list = IOUtils.readLines(inputStream, "UTF-8");
            assertThat(list, contains(equalTo("TEST")));
        }
        binary = null;
        assertThat(service.exist(id), is(true));
        binary = service.get(id);
        try (InputStream inputStream = binary.getInputStream()) {
            List<String> list = IOUtils.readLines(inputStream, "UTF-8");
            assertThat(list, contains(equalTo("TEST")));
        }
        try (OutputStream outputStream = binary.getOutputStream()) {
            outputStream.write("CHANGED".getBytes("UTF-8"));
        }
        try (InputStream inputStream = binary.getInputStream()) {
            List<String> list = IOUtils.readLines(inputStream, "UTF-8");
            assertThat(list, contains(equalTo("CHANGED")));
        }
        assertThat(service.exist(id), is(true));
        service.delete(id);
        assertThat(service.exist(id), is(false));
    }

    @Test
    public void testListFilesDepth4() throws Exception {
        BinaryServiceFilesystemImpl service = new BinaryServiceFilesystemImpl(DIR, 4);

        Set<Binary> binaries = new HashSet<>();
        for (int i = 0; i < 5; i++) {
            binaries.add(service.create());
        }
        assertThat(binaries, hasSize(5));
        binaries.stream()
                .map(Binary::getId)
                .map(service::exist)
                .forEach(Assert::assertTrue);

        Set<Binary> all = service.findAll();
        LOG.info("{}", all.size());
        assertThat(all, containsInAnyOrder(toArray(binaries, Binary.class)));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testBuildPath() throws Exception {
        String filename = "105cbe4c-49f8-450c-9245-0b611d25d80c";

        assertThat(new BinaryServiceFilesystemImpl(DIR, 0).buildPath(filename), equalTo(""));
        assertThat(new BinaryServiceFilesystemImpl(DIR, 1).buildPath(filename), equalTo("105/"));
        assertThat(new BinaryServiceFilesystemImpl(DIR, 3).buildPath(filename), equalTo("105/cbe/4c4/"));
        assertThat(new BinaryServiceFilesystemImpl(DIR, 4).buildPath(filename), equalTo("105/cbe/4c4/9f8/"));
        assertThat(new BinaryServiceFilesystemImpl(DIR, 5).buildPath(filename), equalTo("105/cbe/4c4/9f8/450/"));
        assertThat(new BinaryServiceFilesystemImpl(DIR, 10).buildPath(filename), equalTo("105/cbe/4c4/9f8/450/c92/450/b61/1d2/5d8/"));
        assertThat(new BinaryServiceFilesystemImpl(DIR, 15).buildPath(filename), equalTo("105/cbe/4c4/9f8/450/c92/450/b61/1d2/5d8/0c/"));
        new BinaryServiceFilesystemImpl(DIR, -1);
    }
}
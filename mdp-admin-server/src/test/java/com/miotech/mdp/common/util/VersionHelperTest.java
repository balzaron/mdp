package com.miotech.mdp.common.util;

import org.junit.Test;

import static org.junit.Assert.*;

public class VersionHelperTest {
    @Test
    public void testCompare() throws Exception {
        assertEquals(1, VersionHelper.compare("1", "0.9"));
        assertEquals(1, VersionHelper.compare("0.0.0.2", "0.0.0.1"));
        assertEquals(1, VersionHelper.compare("1.0", "0.9"));
        assertEquals(1, VersionHelper.compare("2.0.1", "2.0.0"));
        assertEquals(1, VersionHelper.compare("2.0.1", "2.0"));
        assertEquals(1, VersionHelper.compare("2.0.1", "2"));
        assertEquals(1, VersionHelper.compare("0.9.1", "0.9.0"));
        assertEquals(1, VersionHelper.compare("0.9.2", "0.9.1"));
        assertEquals(1, VersionHelper.compare("0.9.11", "0.9.2"));
        assertEquals(1, VersionHelper.compare("0.9.12", "0.9.11"));
        assertEquals(1, VersionHelper.compare("0.10", "0.9"));
        assertEquals(0, VersionHelper.compare("0.10", "0.10"));
        assertEquals(-1, VersionHelper.compare("2.10", "2.10.1"));
        assertEquals(-1, VersionHelper.compare("0.0.0.2", "0.1"));
        assertEquals(1, VersionHelper.compare("1.0", "0.9.2"));
        assertEquals(1, VersionHelper.compare("1.10", "1.6"));
        assertEquals(0, VersionHelper.compare("1.10", "1.10.0.0.0.0"));
        assertEquals(1, VersionHelper.compare("1.10.0.0.0.1", "1.10"));
        assertEquals(0, VersionHelper.compare("1.10.0.0.0.0", "1.10"));
        assertEquals(1, VersionHelper.compare("1.10.0.0.0.1", "1.10"));
    }
}

// Copyright (c) 2011, Christopher Pavlina. All rights reserved.
//
// Default paths for executables and libraries

package me.pavlina.alco.compiler;

import java.util.Map;

/**
 * Listing of all default paths
 */
public class DefaultPaths
{

    // Ugh... formal documentation for simple method can be redundant :-(
    /**
     * Load all default paths into the given map
     * @param map Map to load paths into
     */
    public static void paths (Map<String, String> map)
    {
        map.put ("llc",         "/usr/bin/llc");
        map.put ("llvm-as",     "/usr/bin/llvm-as");
        map.put ("as",          "/usr/bin/as");
        map.put ("ld",          "/usr/bin/ld");
        map.put ("crt1-64",     "/usr/lib64/crt1.o");
        map.put ("crti-64",     "/usr/lib64/crti.o");
        map.put ("crtn-64",     "/usr/lib64/crtn.o");
        map.put ("ldso-64",     "/lib64/ld-linux-x86-64.so.2");
        map.put ("crt1-32",     "/usr/lib/crt1.o");
        map.put ("crti-32",     "/usr/lib/crti.o");
        map.put ("crtn-32",     "/usr/lib/crtn.o");
        map.put ("ldso-32",     "/lib/ld-linux.so.2");
        map.put ("runtime-64",  "/usr/lib64/alpha-runtime.o");
        map.put ("runtime-32",  "/usr/lib/alpha-runtime.o");
    }
}

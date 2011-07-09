// Copyright (c) 2011, Christopher Pavlina. All rights reserved.
//
// Configuration file reader

package me.pavlina.alco.compiler;

import java.io.*;
import java.util.Map;

/**
 * Configuration file reader.
 */
public class ConfigFile
{

    /**
     * Actual reader method. This reads a configuration file into a map.
     * @param file File name to read
     * @param paths Map to read into
     * @throws java.io.IOException on read/open error
     */
    private static void _read (File file, Map<String, String> paths)
            throws java.io.IOException
    {

        BufferedReader reader = new BufferedReader (new FileReader (file));
        String line;

        while ((line = reader.readLine ()) != null) {
            int idx;

            // Decomment
            idx = line.indexOf ('#');
            if (idx != -1) {
                line = line.substring (0, idx);
            }

            // Key and value
            idx = line.indexOf ('=');
            if (idx == -1) {
                continue;
            }

            String key = line.substring (0, idx).trim ();
            String val = line.substring (idx + 1).trim ();

            paths.put (key, val);
        }

        reader.close ();
    }

    /**
     * Reader method to call. This reads a configuration file into a map, and
     * prints a message on error.
     * @param file File name to read
     * @param paths Map to read into
     * @return nonzero on error
     */
    public static int read (File file, Map<String, String> paths)
    {
        try {
            ConfigFile._read (file, paths);
        } catch (java.io.IOException e) {
            System.err.println ("Error: cannot read config file "
                                + file.getPath ());
            return 1;
        }
        return 0;
    }

}

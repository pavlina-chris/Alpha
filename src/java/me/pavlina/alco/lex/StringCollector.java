// Copyright (c) 2011, Christopher Pavlina. All rights reserved.
//
// String collector - like StringBuilder, but only for appending, and may be
// efficiently reused.

package me.pavlina.alco.lex;

public class StringCollector
{
    char[] collector;
    int len;

    /**
     * Instantiate the StringCollector with the default initial capacity of 16.
     */
    public StringCollector () {
        this (16);
    }

    /**
     * Instantiate the StringCollector with the given initial capacity.
     */
    public StringCollector (int capacity) {
        collector = new char[capacity];
        len = 0;
    }

    /**
     * Add the character to the collector. */
    public void append (char c) {
        ensureSpace (1);
        collector[len++] = c;
    }

    /**
     * Add the String to the collector. */
    public void append (String s) {
        ensureSpace (s.length ());
        s.getChars (0, s.length (), collector, len);
        len += s.length ();
    }

    /**
     * Get the collector's string. */
    public String toString () {
        return new String (collector, 0, len);
    }

    /**
     * Clear the collector, but keep the allocated space */
    public void clear () {
        len = 0;
    }

    private void ensureSpace (int n) {
        // I'm not going to bother calculating a least power of two - doubling
        // increases space very quickly, so even if the desired space is many
        // times the capacity, we'll only iterate a few times
        int targetSz = collector.length;
        while (n + len > targetSz) {
            targetSz *= 2;
        }
        if (targetSz > collector.length) {
            collector = java.util.Arrays.copyOf (collector, targetSz);
        }
    }
}

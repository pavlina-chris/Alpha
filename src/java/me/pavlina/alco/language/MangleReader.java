// Copyright (c) 2011, Christopher Pavlina. All rights reserved.

package me.pavlina.alco.language;

/**
 * Scanner-like class which reads characters, strings or integers out of a
 * string. */
public class MangleReader
{
    char[] chs;
    int pos;

    public MangleReader (String str) {
        chs = str.toCharArray ();
        pos = 0;
    }

    /**
     * Return whether there are more characters */
    public boolean hasMore () {
        return pos < chs.length;
    }

    /**
     * Read and return a single character.
     * @throws IndexOutOfBoundsException if there are no more characters */
    public char nextChar () {
        if (pos >= chs.length)
            throw new IndexOutOfBoundsException ();
        return chs[pos++];
    }

    /**
     * Return a single character without advancing.
     * @throws IndexOutOfBoundsException if there are no more characters */
    public char peekChar () {
        if (pos >= chs.length)
            throw new IndexOutOfBoundsException ();
        return chs[pos];
    }

    /**
     * Read and return an integer.
     * @throws IndexOutOfBoundsException if there are no more characters
     * @throws NumberFormatException if the next character is not a digit */
    public int nextInt () {
        if (pos >= chs.length)
            throw new IndexOutOfBoundsException ();
        if (chs[pos] < '0' || chs[pos] > '9')
            throw new NumberFormatException ();
        int value = 0;
        for (; pos < chs.length; ++pos) {
            boolean breakFor = false;
            switch (chs[pos]) {
            case '0':
            case '1':
            case '2':
            case '3':
            case '4':
            case '5':
            case '6':
            case '7':
            case '8':
            case '9':
                value *= 10;
                value += (chs[pos] - '0');
                break;
            default:
                --pos;
                breakFor = true;
            }
            if (breakFor) break;
        }
        return value;
    }

    /**
     * Read and return a string.
     * @throws IndexOutOfBoundsException if there are no more characters
     */
    public String nextString (int length) {
        if (pos + length > chs.length)
            throw new IndexOutOfBoundsException ();
        String value = new String (chs, pos, length);
        pos += length;
        return value;
    }
}

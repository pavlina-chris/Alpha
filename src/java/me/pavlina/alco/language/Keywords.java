// Copyright (c) 2011, Christopher Pavlina. All rights reserved.
//
// List of keywords

package me.pavlina.alco.language;

/**
 * List of all Alpha keywords
 */
public class Keywords
{

    private Keywords () {}

    /**
     * List of all Alpha keywords, excluding type names
     */
    static final String[] KEYWORDS = {
        "class", "method", "extern",
        "let", "const", "static", "volatile", "threadlocal", "nomangle",
        "null", "allowconflict", "global",
        "record", "switch", "case", "default", "if", "else", "for",
        "foreach", "do", "while", "return", "as", "true", "false"};

    /**
     * List of all Alpha special type names
     */
    static final String[] TYPES = {
        "i8", "i16", "i32", "i64", "ssize", "int",
        "u8", "u16", "u32", "u64", "size", "unsigned",
        "float", "double", "var", "void", "bool"};

    /**
     * Check if a word is a keyword (excluding type names)
     * @param word Potential keyword to check
     * @return Whether it is a keyword
     */
    public static boolean isKeyword (String word)
    {
        return Keywords.isKeyword (word, false);
    }

    /**
     * Check if a word is a keyword
     * @param word Potential keyword to check
     * @param includeTypes Whether to include special type names as keywords
     * @return Whether it is a keyword
     */
    public static boolean isKeyword (String word, boolean includeTypes)
    {
        for (String i: Keywords.KEYWORDS) {
            if (i.equals (word)) return true;
        }
        if (includeTypes) {
            for (String i: Keywords.TYPES) {
                if (i.equals (word)) return true;
            }
        }
        return false;
    }
}

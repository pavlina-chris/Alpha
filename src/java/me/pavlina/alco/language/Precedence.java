// Copyright (c) 2011, Christopher Pavlina. All rights reserved.

package me.pavlina.alco.language;

/**
 * This class contains precedence constants for all operators. */
public class Precedence {
    private Precedence () {}

    // IF CHANGING, UPDATE "alpha.tex" (SEARCH FOR "PRECEDENCE")
    public static final int INDEX = 140;
    public static final int CALL = 140;
    public static final int MEMBER = 140;
    public static final int DEREF = 130;
    public static final int NEG = 130;
    public static final int NOT = 130;
    public static final int COMPL = 130;
    public static final int INCR = 130;
    public static final int CAST = 125;
    public static final int MUL = 120;
    public static final int ADD = 110;
    public static final int SHIFT = 100;
    public static final int BIT_AND = 96;
    public static final int BIT_XOR = 95;
    public static final int BIT_OR = 94;
    public static final int REL_INEQ = 90;
    public static final int REL_EQ = 80;
    public static final int LOG_AND = 40;
    public static final int LOG_OR = 30;
    public static final int COLON = 25;
    public static final int QUESTION = 25;
    public static final int ASSIGNMENT = 20;
    public static final int COMMA = 10;
}

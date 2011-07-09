// Copyright (c) 2011, Christopher Pavlina. All rights reserved.

package me.pavlina.alco.language;
import java.math.BigInteger;

// Of course, I have to use BigIntegers to fit the ranges of all possible
// integer types. No, Gosling, it's not your fault **cough*unsigned*cough**,
// since I can't fit both I64_MIN and U64_MAX in the same integer... :-(

/**
 * Limits of the Alpha integer types */
public class IntLimits
{
    private IntLimits () {}

    public static BigInteger I8_MIN, I8_MAX, I16_MIN, I16_MAX, I32_MIN,
        I32_MAX, INT_MIN, INT_MAX, I64_MIN, I64_MAX, U8_MIN, U8_MAX,
        U16_MIN, U16_MAX, U32_MIN, U32_MAX, UNSIGNED_MIN, UNSIGNED_MAX,
        U64_MIN, U64_MAX;

    static {
        I8_MIN  = new BigInteger ("-80", 16);
        I8_MAX  = new BigInteger ( "7f", 16);
        I16_MIN = new BigInteger ("-8000", 16);
        I16_MAX = new BigInteger ( "7fff", 16);
        I32_MIN = new BigInteger ("-80000000", 16);
        I32_MAX = new BigInteger ( "7fffffff", 16);
        I64_MIN = new BigInteger ("-8000000000000000", 16);
        I64_MAX = new BigInteger ( "7fffffffffffffff", 16);
        U8_MIN  = BigInteger.ZERO;
        U16_MIN = BigInteger.ZERO;
        U32_MIN = BigInteger.ZERO;
        U64_MIN = BigInteger.ZERO;
        U8_MAX  = new BigInteger ("ff", 16);
        U16_MAX = new BigInteger ("ffff", 16);
        U32_MAX = new BigInteger ("ffffffff", 16);
        U64_MAX = new BigInteger ("ffffffffffffffff", 16);
        INT_MIN = I32_MIN;
        INT_MAX = I32_MAX;
        UNSIGNED_MIN = U32_MIN;
        UNSIGNED_MAX = U32_MAX;
    }


}

// Copyright (c) 2011, Christopher Pavlina. All rights reserved.

package me.pavlina.alco.language;
import java.util.Map;
import java.util.HashMap;
import java.util.Collections;

/**
 * Operator information */
public class Operators
{

    private Operators () {}

    private static Map<String, String> _OPERATOR_TO_ID;
    private static Map<String, String> _ID_TO_OPERATOR;
    static {
        // Make sure these line up
        String[] operators = {
            "~", "*", "/", "%", "%%", "+", "-", "<<",
            ">>", "&", "^", "|", "<", "<=", ">", ">=",
            "==", "!=", "-=", "*=", "/=", "%=", "%%=", "+=",
            "<<=", ">>=", "&=", "^=", "|="};
        String[] ids = {
            "A", "C", "D", "E", "F", "G", "H", "I",
            "J", "K", "L", "M", "N", "O", "P", "Q",
            "R", "S", "h", "c", "d", "e", "f", "g",
            "i", "j", "k", "l", "m"};
        _OPERATOR_TO_ID = new HashMap<String, String> ();
        _ID_TO_OPERATOR = new HashMap<String, String> ();
        for (int i = 0; i < operators.length; ++i) {
            _OPERATOR_TO_ID.put (operators[i], ids[i]);
            _ID_TO_OPERATOR.put (ids[i], operators[i]);
        }
    }

    /**
     * Map linking operators to their mangled operator IDs */
    public static final Map<String, String> OPERATOR_TO_ID
        = Collections.unmodifiableMap (_OPERATOR_TO_ID);

    /**
     * Map linking mangled operator IDs to operators */
    public static final Map<String, String> ID_TO_OPERATOR
        = Collections.unmodifiableMap (_ID_TO_OPERATOR);

    /**
     * Check if an operator is overloadable */
    public static boolean isOverloadable (String oper)
    {
        return _OPERATOR_TO_ID.containsKey (oper);
    }
}

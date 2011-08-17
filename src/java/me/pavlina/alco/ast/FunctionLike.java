// Copyright (c) 2011, Christopher Pavlina. All rights reserved.

package me.pavlina.alco.ast;
import me.pavlina.alco.language.Type;
import me.pavlina.alco.language.Keywords;
import me.pavlina.alco.language.Operators;
import me.pavlina.alco.compiler.errors.*;
import me.pavlina.alco.compiler.Env;
import me.pavlina.alco.lex.TokenStream;
import me.pavlina.alco.lex.Token;
import me.pavlina.alco.parse.TypeParser;
import java.util.List;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Map;
import java.util.HashMap;

/**
 * AST function. Represents any function-like object, such as a method or an
 * extern declaration. */
public abstract class FunctionLike extends AST
{
    /**
     * Unmangled name of the function */
    protected String name;

    /**
     * Whether the name is an operator */
    protected boolean operator;

    /**
     * Return type of the function */
    protected Type type;

    /**
     * Return types of the function, for multiples */
    protected List<Type> types;

    /**
     * Whether the function was declared static */
    protected boolean _static;

    /**
     * Whether the function was declared nomangle */
    protected boolean nomangle;

    /**
     * Whether the function was declared allowconflict */
    protected boolean allowconflict;

    /**
     * Whether the function was declared global */
    protected boolean global;

    /**
     * Types of the function's arguments */
    protected List<Type> argtypes;

    /**
     * Names of the function's arguments. If unnamed arguments were allowed,
     * then this will contain an empty string for each unnamed argument. */
    protected List<String> argnames;

    /**
     * Function's package. A function must set this unless it is 'nomangle' */
    protected Package pkg;

    /**
     * Parse a function declaration.
     * @param allowStatic Whether to allow the "static" modifier
     * @param allowNomangle Whether to allow the "nomangle" modifier
     * @param allowAllowconflict Whether to allow the "allowconflict" modifier
     * @param allowGlobal Whether to allow the "global" modifier
     * @param allowMultRet Whether to allow multiple return types
     * @param nomangleRedundant Whether "nomangle" should trigger a
     *  "nomangle is redundant here" warning
     * @param allowUnnamed Whether to allow unnamed args
     * @param allowOperator Whether to allow this to be an operator overload
     *  declaration
     */
    protected void parse (TokenStream stream, Env env, boolean allowStatic,
                          boolean allowNomangle, boolean allowAllowconflict,
                          boolean allowGlobal, boolean allowMultRet,
                          boolean nomangleRedundant,
                          boolean allowUnnamed,
                          boolean allowOperator) throws CError {

        _static = nomangle = false;

        Token token = stream.peek ();

        // Static?
        if (token.is (Token.WORD, "static")) {
            if (allowStatic) {
                stream.next ();
                _static = true;
            } else {
                throw Unexpected.at ("type name", token);
            }
        }

        // Return type
        if (token.is (Token.WORD, "void")) {
            stream.next ();
            types = new ArrayList<Type> (0);
            type = Type.getNull ();
        } else if (token.is (Token.OPER, "(")) {
            if (!allowMultRet)
                throw CError.at ("multiple return types not allowed here",
                                 token);
            stream.next ();
            types = new ArrayList<Type> ();
            while (true) {
                Type t = TypeParser.parse (stream, env);
                types.add (t);
                token = stream.next ();
                if (token.is (Token.OPER, ")"))
                    break;
                else if (token.is (Token.NO_MORE))
                    throw UnexpectedEOF.after (") or ,", stream.last ());
                else if (!token.is (Token.OPER, ","))
                    throw Unexpected.after (") or ,", stream.last ());
            }
            type = types.get (0);
        } else {
            type = TypeParser.parse (stream, env);
            types = new ArrayList<Type> (1);
            types.add (type);
        }

        // Name
        token = stream.next ();
        operator = false;
        if (token.is (Token.EXTRA, "$$name")) {
            token = stream.next ();
            if (token.is (Token.NO_MORE))
                throw Unexpected.after ("name", stream.last ());
            name = token.value;
        } else if (token.is (Token.NO_MORE)) {
            throw Unexpected.after ("name", stream.last ());
        } else if (token.is (Token.OPER) && allowOperator &&
                   Operators.isOverloadable (token.value)) {
            operator = true;
        } else if (!token.is (Token.WORD) ||
                   Keywords.isKeyword (token.value, true)) {
            if (token.is (Token.OPER, "(") &&
                type.getEncoding () == Type.Encoding.POINTER) {
                CError e = Unexpected.at ("name", token);
                String note =
                    "Perhaps you meant to overload multiplication?\n"
                    + "(" + type.getSubtype ().toString () + ") * (...\n";
                e.setNote (note);
                throw e;
            }
            throw Unexpected.at ("name", token);
        }
        name = token.value;

        // Opening paren
        token = stream.next ();
        if (!token.is (Token.OPER, "("))
            throw Unexpected.after ("(", stream.last ());
        argtypes = new ArrayList<Type> ();
        argnames = new ArrayList<String> ();

        // No args?
        token = stream.peek ();
        boolean hasArgs;
        if (token.is (Token.OPER, ")")) {
            stream.next ();
            hasArgs = false;
        } else {
            hasArgs = true;
        }

        // Arguments
        while (hasArgs) {
            Type type;
            String name = "";
            type = TypeParser.parse (stream, env);
            token = stream.next ();
            if (token.is (Token.OPER, ",")) {
                if (! allowUnnamed)
                    throw Unexpected.after ("name", stream.last ());
                else
                    stream.putback (token);
            } else if (token.is (Token.OPER, ")") && allowUnnamed) {
                argtypes.add (type);
                argnames.add (name);
                break;
            } else if (token.is (Token.EXTRA, "$$name")) {
                token = stream.next ();
                if (token.is (Token.NO_MORE))
                    throw UnexpectedEOF.after ("name", stream.last ());
                name = token.value;
            } else if (!token.is (Token.WORD) || Keywords.isKeyword
                       (token.value, true)) {
                if (allowUnnamed)
                    throw Unexpected.after ("name or comma", stream.last ());
                else
                    throw Unexpected.after ("name", stream.last ());
            } else {
                name = token.value;
            }

            argtypes.add (type);
            argnames.add (name);

            token = stream.next ();
            if (token.is (Token.OPER, ")"))
                break;
            else if (!token.is (Token.OPER, ","))
                throw Unexpected.at (", or )", token);
        }

        // Attributes
        while (true) {
            token = stream.peek ();
            if (token.is (Token.WORD, "nomangle")) {
                if (! allowNomangle)
                    throw CError.at ("unexpected: nomangle", token);
                if (nomangleRedundant)
                    env.warning_at ("nomangle is redundant here", token);
                if (operator)
                    throw CError.at ("operators must be mangled", token);
                stream.next ();
                nomangle = true;

            } else if (token.is (Token.WORD, "allowconflict")) {
                if (! allowAllowconflict)
                    throw CError.at ("unexpected: allowconflict", token);
                stream.next ();
                allowconflict = true;

            } else if (token.is (Token.WORD, "global")) {
                if (! allowGlobal)
                    throw CError.at ("unexpected: global", token);
                stream.next ();
                global = true;

            }
            else break;
        }
    }

    /**
     * Get whether the function is mangled */
    public boolean isMangled () {
        return !nomangle;
    }

    /**
     * Get whether the function is conflict-allowed */
    public boolean isAllowConflict () {
        return allowconflict;
    }

    /**
     * Get the unmangled name of the method */
    public String getName () {
        return name;
    }

    /**
     * Get the mangled name of the method */
    public String getMangledName () {
        if (nomangle)
            return name;
        if (operator)
            return getMangledOperator ();
        StringBuilder sb = new StringBuilder ();
        if (global) {
            sb.append ("$G");
            sb.append (name.length ());
            sb.append (name);
        } else {
            sb.append ("$F");
            sb.append (pkg.getName ().length ());
            sb.append (pkg.getName ());
            sb.append (name.length ());
            sb.append (name);
        }
        for (Type i: types)
            sb.append (i.getEncodedName ());
        sb.append ('$');
        for (Type i: argtypes)
            sb.append (i.getEncodedName ());
        return sb.toString ();
    }

    /**
     * Get the mangled name of an operator method */
    private String getMangledOperator () {
        StringBuilder sb = new StringBuilder ();
        sb.append ("$O");
        sb.append (Operators.OPERATOR_TO_ID.get (name));
        for (Type i: types)
            sb.append (i.getEncodedName ());
        sb.append ('$');
        for (Type i: argtypes)
            sb.append (i.getEncodedName ());
        return sb.toString ();
    }

    /**
     * Get the return type */
    public Type getType () {
        return type;
    }

    /**
     * Get the return types */
    public List<Type> getTypes () {
        return Collections.unmodifiableList (types);
    }

    /**
     * Whether the method is declared static */
    public boolean isStatic () {
        return _static;
    }

    /**
     * Return a list of argument types */
    public List<Type> getArgTypes () {
        // FIXME: Changed for debugging
        return argtypes;
        // return Collections.unmodifiableList (argtypes);
    }

    /**
     * Return a list of argument names */
    public List<String> getArgNames () {
        return Collections.unmodifiableList (argnames);
    }

    /**
     * Return whether the declaration is equivalent to another, for purposes
     * of resolution. That is, if it has the same name, mangle status, and
     * argument types. Return type and static/instance are not considered. */
    public boolean equals (Object other) {
        if (!FunctionLike.class.isInstance (other))
            return false;
        FunctionLike fl = (FunctionLike) other;
        return (name.equals (fl.name) &&
                nomangle == fl.nomangle &&
                argtypes.equals (fl.argtypes));
    }

    /**
     * Dummy hashCode() */
    public int hashCode () {
        assert false : "No hashCode() written";
        return 1;
    }

    /**
     * Return a string version of the function declaration. This is used for
     * print() methods and for ambiguous-name errors (which list all
     * possibilities) */
    public String toString () {
        StringBuilder sb = new StringBuilder ();
        if (_static) sb.append ("static ");
        if (type.getEncoding () == Type.Encoding.NULL)
            sb.append ("void");
        else if (types.size () == 1) {
            // If name is *, we put the type in parentheses. Otherwise, the
            // parser would pick up the * as a pointer type qualifier and see
            // no name.
            if (name.equals ("*"))
                sb.append ("(");
            sb.append (type);
            if (name.equals ("*"))
                sb.append (")");
        } else {
            sb.append ("(");
            for (int i = 0; i < types.size (); ++i) {
                if (i != 0) sb.append (", ");
                sb.append (types.get (i));
            }
            sb.append (")");
        }
        sb.append (' ');
        sb.append (name);
        sb.append (" (");
        for (int i = 0; i < argtypes.size (); ++i) {
            if (i > 0) sb.append (", ");
            sb.append (argtypes.get (i));
            sb.append (' ');
            sb.append (argnames.get (i));
        }
        sb.append (')');
        if (nomangle) sb.append (" nomangle");
        return sb.toString ();
    }
}

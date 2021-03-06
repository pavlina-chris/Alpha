// Copyright (c) 2011, Christopher Pavlina. All rights reserved.
//
// Resolver - variable and method name resolver
package me.pavlina.alco.language;
// I tried to keep the AST package out of the Language package, but it just made
// more sense to allow these in.
import me.pavlina.alco.ast.FunctionLike;
import me.pavlina.alco.ast.Expression;
import me.pavlina.alco.lex.Token;
import me.pavlina.alco.compiler.errors.*;
import java.util.Map;
import java.util.HashMap;
import java.util.List;
import java.util.ArrayList;

/**
 * Variable and method name resolver. This stores names as found, and then
 * looks them up.
 */
public class Resolver
{
    Map<String, Integer> variableCounts;
    Map<String, Token> declared;
    Map<String, Variable> variables;
    List<FunctionLike> functions;
    boolean handler_oom, handler_oob;
    int[] globalCounter;

    /**
     * Create a brand new resolver, with no names at all */
    public Resolver () {
        declared = new HashMap<String, Token> ();
        variables = new HashMap<String, Variable> ();
        variableCounts = new HashMap<String, Integer> ();
        functions = new ArrayList<FunctionLike> ();
        globalCounter = new int[] {0};
    }

    /**
     * Create a new resolver from the given one. It will contain all the names
     * in the given resolver, but changes to it will not affect the given
     * resolver. This is used for descending into scopes. */
    public Resolver (Resolver other) {
        variableCounts = other.variableCounts;
        declared = new HashMap<String, Token> ();
        variables = new HashMap<String, Variable> (other.variables);
        functions = new ArrayList<FunctionLike> (other.functions);
        globalCounter = other.globalCounter;
        handler_oom = other.handler_oom;
        handler_oob = other.handler_oob;
    }

    /**
     * Clear the resolver's list of visible functions. This is done before
     * running on a method. */
    public void clear () {
        variableCounts.clear ();
    }

    /**
     * Return whether an OOM handler has been declared */
    public boolean getHandleOOM () {
        return handler_oom;
    }

    /**
     * Set whether an OOM handler has been declared */
    public void setHandleOOM (boolean b) {
        handler_oom = b;
    }

    /**
     * Return whether an OOB handler has been declared */
    public boolean getHandleOOB () {
        return handler_oob;
    }

    /**
     * Set whether an OOB handler has been declared */
    public void setHandleOOB (boolean b) {
        handler_oob = b;
    }

    /**
     * Add the variable to the resolver. */
    public Variable addVariable (String name, Type type, Token token)
        throws CError
    {
        Token lastDeclare = declared.get (name);
        if (lastDeclare != null) {
            throw CError.at
                (String.format ("variable '%s' already declared at %d:%d",
                                name, lastDeclare.line+1, lastDeclare.col+1),
                 token);
        } else {
            declared.put (name, token);
            Integer count = variableCounts.get (name);
            Variable var;
            if (count == null) {
                var = new Variable (name, 0, type);
                variableCounts.put (name, 1);
                variables.put (name, var);
            } else {
                var = new Variable (name, count.intValue (), type);
                variableCounts.put (name, count.intValue () + 1);
                variables.put (name, var);
            }
            return var;
        }
    }

    /**
     * Add a globally available local variable to the resolver. This is a
     * variable whose name is only resolvable locally, but whose pointer can
     * be accessed globally. It is technically a global with a numeric name.
     */
    public Variable addGlobalLocal (String name, Type type, Token token)
        throws CError
    {
        int num = globalCounter[0]++;
        Token lastDeclare = declared.get (name);
        if (lastDeclare != null) {
            throw CError.at
                (String.format ("variable '%s' already declared at %d:%d",
                                name, lastDeclare.line+1, lastDeclare.col+1),
                 token);
        } else {
            declared.put (name, token);
            Variable var = new Variable (Integer.toString (num), 0, type, "@");
            variables.put (name, var);
            return var;
        }
    }

    /**
     * Resolve the variable, returning an instance of Resolver.Variable */
    public Variable getVariable (String name, Token token) throws CError {
        Variable var = variables.get (name);
        if (var == null) throw CError.at ("cannot resolve name", token);
        return var;
    }

    /**
     * Add the function/method to the resolver. */
    public void addFunction (FunctionLike function, Token token) throws CError {
        if (function.isMangled ())
            for (FunctionLike i: functions) {
                if (i.equals (function)) {
                    CError err = CError.at
                        ("duplicate function declaration", token);
                    err.setNote ("Conflict:\n" + i.toString () + "\n");
                    throw err;
                }
                if (!i.isMangled () &&
                    i.getName ().equals (function.getName ())) {
                    if (!i.isAllowConflict () && !function.isAllowConflict ()) {
                        CError err = CError.at
                            ("duplicate function name", token);
                        err.setNote ("Conflict:\n" + i.toString () + "\n");
                        throw err;
                    }
                }
            }
        else
            for (FunctionLike i: functions) {
                if (i.equals (function)) {
                    CError err = CError.at
                        ("duplicate function declaration", token);
                    err.setNote ("Conflict:\n" + i.toString () + "\n");
                    throw err;
                }
                if (i.getName ().equals (function.getName ()) &&
                    !function.isAllowConflict ()) {
                    CError err = CError.at
                        ("duplicate function name", token);
                    err.setNote ("Conflict:\n" + i.toString () + "\n");
                    throw err;
                }
            }
        functions.add (function);
    }

    /**
     * Resolve the function call. */
    public FunctionLike getFunction (String name, List<Expression> args,
                                     Token token) throws CError
    {
        // Match levels:
        // 0: Nomangle: Function always matches 'nomangle'
        // 1: Perfect match. All types match exactly
        // 2: Types may require promotion
        // 3: Variadic with exact type match
        // 4: Variadic requiring promotion
        // 5: Types require promotion to dynamic
        // 6: Variadic requiring promotion to dynamic
        List<FunctionLike> matches = new ArrayList<FunctionLike> ();
        List<FunctionLike> candidates = new ArrayList<FunctionLike> ();
        int matchlvl = 7;

        List<Type> argTypes = new ArrayList<Type> (args.size ());
        for (Expression i: args)
            argTypes.add (i.getType ());

        for (FunctionLike i: functions) {
            List<Type> iArgs = i.getArgTypes ();
            if (! i.getName ().equals (name)) continue;
            candidates.add (i);

            // Level 0: Nomangle
            if (i.getName ().equals (name) && !i.isMangled ()) {
                candidates.clear ();
                candidates.add (i);
                if (args.size () != iArgs.size ()) {
                    matches.clear ();
                    break;
                }
                for (int arg = 0; arg < args.size (); ++arg) {
                    if (!Type.canCoerce (args.get (arg), iArgs.get (arg))) {
                        matches.clear ();
                        break;
                    }
                }
                if (matchlvl > 0) {
                    matchlvl = 0;
                    matches.clear ();
                }
                matches.add (i);
                continue;
            }

            // Level 1: Perfect
            if (matchlvl < 1) continue;
            if (iArgs.equals (argTypes)) {
                if (matchlvl > 1) {
                    matchlvl = 1;
                    matches.clear ();
                }
                matches.add (i);
                continue;
            }

            // Level 2: Promotion
            if (matchlvl < 2) continue;
            boolean matchWithPromotion = args.size () == iArgs.size ();
            for (int arg = 0; arg < args.size () && matchWithPromotion; ++arg) {
                if (!Type.canCoerce (args.get (arg), iArgs.get (arg)))
                    matchWithPromotion = false;
            }
            if (matchWithPromotion) {
                if (matchlvl > 2) {
                    matchlvl = 2;
                    matches.clear ();
                }
                matches.add (i);
                continue;
            }

            // No more levels have been implemented
        }

        if (matches.size () == 0) {
            StringBuilder sb = new StringBuilder ();
            sb.append ("cannot resolve call: ");
            sb.append (name).append (" (");
            boolean first = true;
            for (Expression i: args) {
                if (first) first = false;
                else sb.append (", ");
                sb.append (i.getType ());
            }
            sb.append (")");
            CError err = CError.at (sb.toString (), token);
            
            if (candidates.size () > 0) {
                StringBuilder sbNote = new StringBuilder ("Candidates were:\n");
                for (FunctionLike i: candidates) {
                    sbNote.append ("  ").append (i).append ("\n");
                }
                err.setNote (sbNote.toString ());
            }

            throw err;

        } else if (matches.size () > 1) {
            StringBuilder sb = new StringBuilder ();
            sb.append ("ambiguous call: ");
            sb.append (name).append (" (");
            boolean first = true;
            for (Expression i: args) {
                if (first) first = false;
                else sb.append (", ");
                sb.append (i.getType ());
            }
            sb.append (")");

            StringBuilder sbNote = new StringBuilder ("Matches were:\n");
            for (FunctionLike i: matches) {
                sbNote.append ("  ").append (i.toString ()).append ("\n");
            }

            CError err = CError.at (sb.toString (), token);
            err.setNote (sbNote.toString ());
            throw err;
        }
        return matches.get (0);
    }

    /**
     * This represents a variable reference. It holds the declared type and the
     * real name of the variable, and is returned from resolveVariable(). */
    public static class Variable {
        private String name;
        private int disambigCounter;
        private Type type;
        private String prefix;

        /**
         * Initialise the variable from a given name, counter, and type.
         * @param name This is the name (not "real" name, but declared name) of
         * the variable.
         * @param disambigCounter This is used to disambiguate variables with
         * the same name in nested scopes. If a scope and its child both declare
         * 'x', the first one will have real name 'x', and the second will have
         * real name 'x.1'.
         * @param type Declared type of the variable */
        public Variable (String name, int disambigCounter, Type type) {
            this.name = name;
            this.disambigCounter = disambigCounter;
            this.type = type;
            this.prefix = "%";
        }

        /**
         * Initialise the variable with a given prefix, other than "%".
         */
        public Variable (String name, int disambigCounter, Type type,
                         String prefix) {
            this.name = name;
            this.disambigCounter = disambigCounter;
            this.type = type;
            this.prefix = prefix;
        }

        /**
         * Return the real name of the variable */
        public String getName () {
            if (disambigCounter == 0)
                return prefix + name;
            else
                return prefix + name + "." + Integer.toString (disambigCounter);
        }

        /**
         * Return the disambiguation count of the variable */
        public int getCount () {
            return disambigCounter;
        }

        /**
         * Return the declared type of the variable */
        public Type getType () {
            return type;
        }
    }
}

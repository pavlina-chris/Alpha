// Copyright (c) 2011, Christopher Pavlina. All rights reserved.

package me.pavlina.alco.ast;
import me.pavlina.alco.compiler.Env;
import me.pavlina.alco.compiler.errors.*;
import me.pavlina.alco.lex.Token;
import me.pavlina.alco.lex.TokenStream;
import me.pavlina.alco.llvm.*;
import me.pavlina.alco.language.Type;
import me.pavlina.alco.language.HasType;
import me.pavlina.alco.language.Resolver;
import me.pavlina.alco.codegen.*;
import java.util.List;
import java.util.ArrayList;
import java.util.Arrays;

/**
 * Modulo assignment operator (%=). */
public class OpAssignMod extends Expression.Operator {
    Token token;
    Method method;
    Type type;
    Expression[] children;
    Instruction instruction;
    Overload overload;
    Cast cast;
    ModNum modnum;

    public static final Expression.OperatorCreator CREATOR;

    public OpAssignMod (Env env, TokenStream stream, Method method)
        throws CError
    {
        token = stream.next ();
        children = new Expression[2];
        this.method = method;
    }

    public int getPrecedence () {
        return me.pavlina.alco.language.Precedence.ASSIGNMENT;
    }

    public Expression.Associativity getAssociativity () {
        return Expression.Associativity.RIGHT;
    }

    public Expression.Arity getArity () {
        return Expression.Arity.BINARY;
    }

    public void setOperands (Expression dest, Expression value) {
        children[0] = dest;
        children[1] = value;
        dest.setParent (this);
        value.setParent (this);
    }

    public Instruction getInstruction () {
        return instruction;
    }

    public Type getType () {
        return type;
    }

    public void checkTypes (Env env, Resolver resolver) throws CError {
        children[0].checkTypes (env, resolver);
        children[0].checkPointer (true, token);
        children[1].checkTypes (env, resolver);

        try {
            checkTypes_ (env, resolver);
        } catch (CError e) {
            modnum = null;
            overload = new Overload (token, method);
            OpAddress addrOf = new OpAddress (token, children[0]);
            addrOf.checkTypes (env, resolver);
            overload.operator ("%=").children (addrOf, children[1]);
            if (!overload.find (env, resolver)) throw e;
            overload.checkTypes (env, resolver);
            type = overload.getType ();
        }
    }

    private void checkTypes_ (Env env, Resolver resolver) throws CError {
        Type.checkCoerce (children[1], children[0], token);
        cast = new Cast (token)
            .type (children[1].getType ())
            .dest (children[0].getType ());
        modnum = new ModNum (token)
            .type (children[0].getType ());
        modnum.checkTypes (env, resolver);
        type = children[0].getType ().getNormalised ();
    }

    public void genLLVM (Env env, Emitter emitter, Function function) {
        children[0].genLLVM (env, emitter, function);
        children[1].genLLVM (env, emitter, function);
        Instruction ptr = children[0].getPointer (env, emitter, function);
        
        if (modnum != null) {
            cast.value (children[1].getInstruction ());
            cast.genLLVM (env, emitter, function);
            modnum.lhs (children[0].getInstruction ());
            modnum.rhs (cast.getInstruction ());
            modnum.genLLVM (env, emitter, function);
            instruction = modnum.getInstruction ();
            function.add (new STORE ()
                          .pointer (ptr)
                          .value (instruction)
                          ._volatile (children[0].getType ().isVolatile ()));

        } else {
            overload.genLLVM (env, emitter, function);
            instruction = overload.getInstruction ();
        }
    }

    @SuppressWarnings("unchecked")
    public List<AST> getChildren () {
        return (List) Arrays.asList (children);
    }

    public Token getToken () {
        return token;
    }

    public void print (java.io.PrintStream out) {
        out.print ("(assign-mod");
        for (Expression i: children) {
            out.print (" ");
            i.print (out);
        }
        out.print (")");
    }

    public void checkPointer (boolean write, Token token) throws CError {
        throw CError.at ("cannot assign to assignment", token);
    }

    public Instruction getPointer (Env env, Emitter emitter, Function function) {
        return null;
    }

    static {
        CREATOR = new Expression.OperatorCreator () {
                public Operator create (Env env, TokenStream stream,
                                        Method method) throws CError {
                    return new OpAssignMod (env, stream, method);
                }
            };
    }
}

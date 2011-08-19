// Copyright (c) 2011, Christopher Pavlina. All rights reserved.

package me.pavlina.alco.ast;
import me.pavlina.alco.compiler.Env;
import me.pavlina.alco.compiler.errors.*;
import me.pavlina.alco.lex.Token;
import me.pavlina.alco.lex.TokenStream;
import me.pavlina.alco.language.Resolver;
import me.pavlina.alco.language.Type;
import static me.pavlina.alco.language.Type.Encoding;
import me.pavlina.alco.llvm.*;
import java.util.List;
import java.util.Arrays;

/**
 * Member access */
public class OpMember extends Expression.Operator {
    Type type;
    Token token;
    String name;
    Expression[] children;
    Instruction instruction;

    public static final Expression.OperatorCreator CREATOR;

    public OpMember (Env env, TokenStream stream, Method method) throws CError {
        token = stream.next ();
    }

    public int getPrecedence () {
        return me.pavlina.alco.language.Precedence.MEMBER;
    }

    public Expression.Associativity getAssociativity () {
        return Expression.Associativity.LEFT;
    }

    public Expression.Arity getArity () {
        return Expression.Arity.BINARY;
    }

    public void setOperands (Expression left, Expression right) {
        children = new Expression[] {left, right};
        left.setParent (this);
        right.setParent (this);
    }

    public void checkTypes (Env env, Resolver resolver) throws CError {
        children[0].checkTypes (env, resolver);
        if (children[0].getType ().getEncoding () == Encoding.ARRAY) {
            if (! NameValue.class.isInstance (children[1]))
                throw Unexpected.at ("name", children[1].getToken ());
            name = ((NameValue) children[1]).getName ();
            if (name.equals ("length")) {
                type = new Type (env, "size", null);
            } else {
                throw Unexpected.at ("'length'", children[1].getToken ());
            }
        } else {
            throw Unexpected.at ("array", children[0].getToken ());
        }
    }

    public Instruction getInstruction () {
        return instruction;
    }

    public Type getType () {
        return type;
    }

    public void genLLVM (Env env, Emitter emitter, Function function) {
        if (children[0].getType ().getEncoding () == Encoding.ARRAY) {
            if (name.equals ("length")) {
                // Array length is the first part of an array
                String size_t = "i" + env.getBits ();
                children[0].genLLVM (env, emitter, function);
                Instruction array = children[0].getInstruction ();
                Instruction sizep = new CONVERT ()
                    .op ("bitcast").stype ("%.nonprim").dtype (size_t + "*")
                    .value (array);
                instruction = new LOAD ().type (size_t).pointer (sizep);
                function.add (sizep);
                function.add (instruction);
            } else assert false;
        } else assert false;
    }

    @SuppressWarnings("unchecked")
    public List<AST> getChildren () {
        return (List) Arrays.asList (children);
    }
    
    public Token getToken () {
        return token;
    }

    public void print (java.io.PrintStream out) {
        out.print ("(. ");
        children[0].print (out);
        out.print (" ");
        children[1].print (out);
        out.print (")");
    }

    public void checkPointer (boolean write, Token token) throws CError {
        if (children[0].getType ().getEncoding () == Encoding.ARRAY) {
            if (name.equals ("length")) {
                throw CError.at ("cannot assign to array length", token);
            } else throw CError.at ("invalid member access", token);
        } else throw CError.at ("invalid member access", token);
    }

    public Instruction getPointer (Env env, Emitter emitter, Function function)
    {
        return null;
    }

    static {
        CREATOR = new Expression.OperatorCreator () {
                public Operator create (Env env, TokenStream stream,
                                        Method method) throws CError {
                    return new OpMember (env, stream, method);
                }
            };
    }
}

// Copyright (c) 2011, Christopher Pavlina. All rights reserved.

package me.pavlina.alco.ast;
import me.pavlina.alco.compiler.Env;
import me.pavlina.alco.compiler.errors.*;
import me.pavlina.alco.lex.Token;
import me.pavlina.alco.lex.TokenStream;
import me.pavlina.alco.language.Resolver;
import me.pavlina.alco.language.Type;
import me.pavlina.alco.language.HasType;
import me.pavlina.alco.llvm.*;
import me.pavlina.alco.codegen.*;
import java.util.List;
import java.util.Arrays;

/**
 * By-value not-equals operator.
 */
public class OpVNe extends Expression.Operator {
    Token token;
    Expression[] children;
    CmpNum cmpnum;
    Coerce coerce;
    Type type;
    Method method;
    Overload overload;
    Instruction instruction;

    public static final Expression.OperatorCreator CREATOR;

    public OpVNe (Env env, TokenStream stream, Method method) throws CError {
        token = stream.next ();
        children = new Expression[2];
        this.method = method;
    }

    public int getPrecedence () {
        return me.pavlina.alco.language.Precedence.REL_EQ;
    }

    public Expression.Associativity getAssociativity () {
        return Expression.Associativity.LEFT;
    }

    public Expression.Arity getArity () {
        return Expression.Arity.BINARY;
    }

    public void setOperands (Expression left, Expression right) {
        children[0] = left;
        children[1] = right;
        left.setParent (this);
        right.setParent (this);
    }

    public void checkTypes (Env env, Resolver resolver) throws CError {
        children[0].checkTypes (env, resolver);
        children[1].checkTypes (env, resolver);
        
        try {
            checkTypes_ (env, resolver);
        } catch (CError e) {
            cmpnum = null;
            overload = new Overload (token, method);
            overload.operator ("!=").children (children);
            if (!overload.find (env, resolver)) throw e;
            overload.checkTypes (env, resolver);
            type = overload.getType ();
        }
    }

    private void checkTypes_ (Env env, Resolver resolver) throws CError {
        coerce = new Coerce (token)
            .lhsT (children[0].getType ())
            .rhsT (children[1].getType ());
        coerce.checkTypes (env, resolver);
        type = new Type (env, "bool", null);
        cmpnum = new CmpNum (token)
            .type (coerce.getType ())
            .cmp ("ne");
        cmpnum.checkTypes (env, resolver);
    }

    public Instruction getInstruction () {
        return instruction;
    }

    public Type getType () {
        return type;
    }

    public void genLLVM (Env env, Emitter emitter, Function function)
    {
        children[0].genLLVM (env, emitter, function);
        children[1].genLLVM (env, emitter, function);

        if (cmpnum != null) {
            coerce.lhsV (children[0].getInstruction ());
            coerce.rhsV (children[1].getInstruction ());
            coerce.genLLVM (env, emitter, function);
            cmpnum.lhs (coerce.getInstructionL ());
            cmpnum.rhs (coerce.getInstructionR ());
            cmpnum.genLLVM (env, emitter, function);
            instruction = cmpnum.getInstruction ();
        } else {
            overload.genLLVM (env, emitter, function);
            instruction = overload.getInstruction ();
        }
    }

    @SuppressWarnings("unchecked")
    public List<AST> getChildren () {
        if (children == null)
            return null;
        else
            return (List) Arrays.asList (children);
    }

    public Token getToken () {
        return token;
    }

    public void print (java.io.PrintStream out) {
        out.print ("(vne? ");
        children[0].print (out);
        out.print (" ");
        children[1].print (out);
        out.print (")");
    }

    public void checkPointer (boolean write, Token token) throws CError {
        throw CError.at ("cannot assign to comparison", token);
    }

    public Instruction getPointer (Env env, Emitter emitter, Function function) {
        return null;
    }

    static {
        CREATOR = new Expression.OperatorCreator () {
                public Operator create (Env env, TokenStream stream,
                                        Method method) throws CError {
                    return new OpVNe (env, stream, method);
                }
            };
    }

}

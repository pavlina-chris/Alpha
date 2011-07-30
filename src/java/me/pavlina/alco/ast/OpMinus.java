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
 * Subtraction operator.
 */
public class OpMinus extends Expression.Operator {
    Token token;
    Method method;
    Expression[] children;
    Expression pointer, integer;
    SubNum subnum;
    Sub2Ptr sub2ptr;
    Sub1Ptr sub1ptr;
    Overload overload;
    Coerce coerce;
    Type type;
    String valueString;

    public static Expression.OperatorCreator CREATOR;

    public OpMinus (Env env, TokenStream stream, Method method) throws CError {
        token = stream.next ();
        children = new Expression[2];
        this.method = method;
    }

    public int getPrecedence () {
        return me.pavlina.alco.language.Precedence.ADD;
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
    }

    public void checkTypes (Env env, Resolver resolver) throws CError {
        children[0].checkTypes (env, resolver);
        children[1].checkTypes (env, resolver);

        try {
            checkTypes_ (env, resolver);
        } catch (CError e) {
            sub2ptr = null;
            sub1ptr = null;
            subnum = null;
            overload = new Overload (token, method);
            overload.operator ("-").children (children);
            if (!overload.find (env, resolver)) throw e;
            overload.checkTypes (env, resolver);
            type = overload.getType ();
        }
    }

    private void checkTypes_ (Env env, Resolver resolver) throws CError {
        Type.Encoding lhsE = children[0].getType ().getEncoding ();
        Type.Encoding rhsE = children[1].getType ().getEncoding ();

        if (lhsE == Type.Encoding.POINTER && rhsE == Type.Encoding.POINTER) {
            // pointer-pointer
            Type lhsT = children[0].getType ();
            Type rhsT = children[1].getType ();
            if (!lhsT.getSubtype ().equalsNoConst (rhsT.getSubtype ())) {
                throw CError.at ("subtracting pointers of different types",
                                 token);
            }
            sub2ptr = new Sub2Ptr (token)
                .type (lhsT);
            sub2ptr.checkTypes (env, resolver);
            type = sub2ptr.getType ();


        } else if (lhsE == Type.Encoding.POINTER &&
                   (rhsE == Type.Encoding.UINT ||
                    rhsE == Type.Encoding.SINT)) {
            // pointer-int
            pointer = children[0];
            integer = children[1];
            sub1ptr = new Sub1Ptr (token)
                .pointerT (pointer.getType ())
                .integerT (integer.getType ());
            sub1ptr.checkTypes (env, resolver);
            type = sub1ptr.getType ();

        } else {
            // int-int, float-float
            coerce = new Coerce (token)
                .lhsT (children[0].getType ())
                .rhsT (children[1].getType ());
            coerce.checkTypes (env, resolver);
            type = coerce.getType ();
            subnum = new SubNum (token)
                .type (type);
            subnum.checkTypes (env, resolver);
        }
    }

    public String getValueString () {
        return valueString;
    }

    public Type getType () {
        return type;
    }

    public void genLLVM (Env env, LLVMEmitter emitter, Function function) {
        children[0].genLLVM (env, emitter, function);
        children[1].genLLVM (env, emitter, function);

        if (sub2ptr != null) {
            sub2ptr.lhs (children[0].getValueString ());
            sub2ptr.rhs (children[1].getValueString ());
            sub2ptr.genLLVM (env, emitter, function);
            valueString = sub2ptr.getValueString ();

        } else if (sub1ptr != null) {
            sub1ptr.pointerV (pointer.getValueString ());
            sub1ptr.integerV (integer.getValueString ());
            sub1ptr.genLLVM (env, emitter, function);
            valueString = sub1ptr.getValueString ();

        } else if (subnum != null) {
            coerce.lhsV (children[0].getValueString ());
            coerce.rhsV (children[1].getValueString ());
            coerce.genLLVM (env, emitter, function);
            subnum.lhs (coerce.getValueStringL ());
            subnum.rhs (coerce.getValueStringR ());
            subnum.genLLVM (env, emitter, function);
            valueString = subnum.getValueString ();

        } else {
            overload.genLLVM (env, emitter, function);
            valueString = overload.getValueString ();
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
        out.print ("(subt");
        for (Expression i: children) {
            out.print (" ");
            i.print (out);
        }
        out.print (")");
    }

    public void checkPointer (boolean write, Token token) throws CError {
        throw CError.at ("cannot assign to subtraction", token);
    }

    public String getPointer (Env env, LLVMEmitter emitter, Function function) {
        return null;
    }

    static {
        CREATOR = new Expression.OperatorCreator () {
                public Operator create (Env env, TokenStream stream,
                                        Method method) throws CError {
                    return new OpMinus (env, stream, method);
                }
            };
    }
}

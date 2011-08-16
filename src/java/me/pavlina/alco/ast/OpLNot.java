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
 * Logical NOT operator.
 */
public class OpLNot extends Expression.Operator {
    Token token;
    Expression[] children;
    Type type;
    Instruction instruction;
    Cast cast;

    public static final Expression.OperatorCreator CREATOR;

    public OpLNot (Env env, TokenStream stream, Method method) throws CError {
        token = stream.next ();
    }

    public int getPrecedence () {
        return me.pavlina.alco.language.Precedence.NOT;
    }

    public Expression.Arity getArity () {
        return Expression.Arity.UNARY;
    }

    public Expression.Associativity getAssociativity () {
        return Expression.Associativity.RIGHT;
    }

    public void setOperands (Expression op, Expression ignore) {
        children = new Expression[] {op};
        op.setParent (this);
    }

    public void checkTypes (Env env, Resolver resolver) throws CError {
        children[0].checkTypes (env, resolver);

        type = new Type (env, "bool", null);
        Type.checkCoerce (children[0], type, token);
        cast = new Cast (token).type (children[0].getType ()).dest (type);
        cast.checkTypes (env, resolver);
    }

    public Instruction getInstruction () {
        return instruction;
    }

    public Type getType () {
        return type;
    }

    public void genLLVM (Env env, Emitter emitter, Function function) {
        children[0].genLLVM (env, emitter, function);
        cast.value (children[0].getInstruction ());
        cast.genLLVM (env, emitter, function);
        Instruction val = cast.getInstruction ();

        Instruction isTrue = new BINARY ()
            .op ("icmp ne").type ("i8")
            .lhs (val).rhs ("0");
        Instruction not = new BINARY ()
            .op ("xor").type ("i1").lhs (isTrue).rhs ("-1");
        instruction = new CONVERT ()
            .op ("sext").stype ("i1").dtype ("i8").value (not);
        function.add (isTrue);
        function.add (not);
        function.add (instruction);
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
        out.print ("(not");
        for (Expression i: children) {
            out.print (" ");
            i.print (out);
        }
        out.print (")");
    }

    public void checkPointer (boolean write, Token token) throws CError {
        throw CError.at ("cannot assign to logical operation", token);
    }

    public Instruction getPointer (Env env, Emitter emitter, Function function) {
        return null;
    }

    static {
        CREATOR = new Expression.OperatorCreator () {
                public Operator create (Env env, TokenStream stream,
                                        Method method) throws CError {
                    return new OpLNot (env, stream, method);
                }
            };
    }
}

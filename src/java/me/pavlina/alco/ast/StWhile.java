// Copyright (c) 2011, Christopher Pavlina. All rights reserved.

package me.pavlina.alco.ast;
import me.pavlina.alco.compiler.Env;
import me.pavlina.alco.compiler.errors.*;
import me.pavlina.alco.lex.TokenStream;
import me.pavlina.alco.lex.Token;
import me.pavlina.alco.language.Keywords;
import me.pavlina.alco.language.Type;
import me.pavlina.alco.language.HasType;
import me.pavlina.alco.language.Resolver;
import me.pavlina.alco.llvm.*;
import me.pavlina.alco.codegen.Cast;
import java.util.List;
import java.util.ArrayList;
import java.util.Arrays;

/**
 * While loop. Syntax:
 * while (expression) scope
 */
public class StWhile extends Statement
{
    Token token;
    AST[] values; // {condition, body}

    public StWhile (Env env, TokenStream stream, Method method) throws CError {
        token = stream.next ();
        if (!token.is (Token.WORD, "while"))
            throw new RuntimeException ("StWhile instantiated without kwd");

        values = new AST[2];

        // Condition
        Token temp = stream.next ();
        if (temp.is (Token.NO_MORE))
            throw UnexpectedEOF.after ("(", stream.last ());
        else if (!temp.is (Token.OPER, "("))
            throw Unexpected.after ("(", stream.last ());
        values[0] = (AST) Expression.parse (env, stream, method, ")");
        if (values[0] == null)
            throw Unexpected.after ("expression", temp);
        temp = stream.next ();
        if (temp.is (Token.NO_MORE))
            throw UnexpectedEOF.after (")", stream.last ());
        else if (!temp.is (Token.OPER, ")"))
            throw Unexpected.after (")", stream.last ());

        // Body
        if (stream.peek ().is (Token.NO_MORE))
            throw UnexpectedEOF.after ("body", stream.last ());

        values[1] = new Scope (env, stream, method);
    }

    public Token getToken () {
        return token;
    }

    public List<AST> getChildren () {
        return Arrays.asList (values);
    }

    public void checkTypes (Env env, Resolver resolver) throws CError {
        for (AST i: values)
            i.checkTypes (env, resolver);
        Type.checkCoerce ((Expression) values[0],
                          new Type (env, "bool", null), token);
    }

    public void genLLVM (Env env, LLVMEmitter emitter, Function function) {
        // br label %.Ltop
        // .Ltop:
        // %.cond = <cond>
        // %cond = icmp ne i8 %.cond, 0
        // br i1 %cond, label %.Lbody, label %.Lbot
        // .Lbody:
        // --body--
        // br label %.Ltop
        // .Lbot:

        String Ltop = ".L" + Integer.toString
            (emitter.getTemporary ("%.L"));
        String Lbody = ".L" + Integer.toString
            (emitter.getTemporary ("%.L"));
        String Lbot = ".L" + Integer.toString
            (emitter.getTemporary ("%.L"));

        
        new branch (emitter, function).ifTrue ("%" + Ltop).build ();
        new label (emitter, function).name (Ltop).build ();
        // Condition
        values[0].genLLVM (env, emitter, function);
        String bCond = ((Expression) values[0]).getValueString ();
        Cast c = new Cast (token)
            .value (bCond).type (((Expression) values[0]).getType ())
            .dest (new Type (env, "bool", null));
        c.genLLVM (env, emitter, function);


        String cond = new icmp (emitter, function)
            .comparison (icmp.Icmp.NE)
            .type ("i8").operands (c.getValueString (), "0")
            .build ();

        new branch (emitter, function)
            .ifTrue ("%" + Lbody).ifFalse ("%" + Lbot)
            .cond (cond).build ();
        new label (emitter, function).name (Lbody).build ();
        values[1].genLLVM (env, emitter, function);
        
        new branch (emitter, function).ifTrue ("%" + Ltop).build ();
        new label (emitter, function).name (Lbot).build ();
    }

    public void print (java.io.PrintStream out) {
        out.print ("(while ");
        values[0].print (out);
        values[1].print (out);
        out.println (")");
    }

    public static final Statement.StatementCreator CREATOR;
    static {
        CREATOR = new Statement.StatementCreator () {
                public Statement create (Env env, TokenStream stream,
                                         Method method) throws CError
                {
                    return new StWhile (env, stream, method);
                }
            };
    }
}

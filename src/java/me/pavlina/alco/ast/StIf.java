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
 * If (duh). Syntax:
 * if (expression) scope
 * else scope
 */
public class StIf extends Statement
{
    Token token;
    AST[] values; // {condition, ifTrue, ifFalse}

    public StIf (Env env, TokenStream stream, Method method) throws CError {
        token = stream.next ();
        if (!token.is (Token.WORD, "if"))
            throw new RuntimeException ("StIf instantiated without kwd");

        values = new AST[3];

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

        // Else?
        temp = stream.peek ();
        if (!temp.is (Token.WORD, "else")) {
            values[2] = null;
            return;
        }
        stream.next ();

        values[2] = new Scope (env, stream, method);
    }

    public Token getToken ()
    {
        return token;
    }

    public List<AST> getChildren ()
    {
        return Arrays.asList (values);
    }

    public void checkTypes (Env env, Resolver resolver) throws CError {
        for (AST i: values)
            if (i != null)
                i.checkTypes (env, resolver);
        Type.checkCoerce ((Expression) values[0],
                          new Type (env, "bool", null), token);
    }

    public void genLLVM (Env env, LLVMEmitter emitter, Function function) {
        String labelIfTrue = ".L" + Integer.toString
            (emitter.getTemporary ("%.L"));
        String labelIfFalse = ".L" + Integer.toString
            (emitter.getTemporary ("%.L"));
        String labelEnd;
        if (values[2] == null)
            labelEnd = labelIfFalse;
        else
            labelEnd = ".L" + Integer.toString
                (emitter.getTemporary ("%.L"));
        
        // Condition
        values[0].genLLVM (env, emitter, function);
        String boolCond = ((Expression) values[0]).getValueString ();
        Cast c = new Cast (token)
            .value (boolCond).type (((Expression) values[0]).getType ())
            .dest (new Type (env, "bool", null));
        c.genLLVM (env, emitter, function);

        String cond = new icmp (emitter, function)
            .comparison (icmp.Icmp.NE)
            .type ("i8").operands (c.getValueString (), "0")
            .build ();
        
        // Branch
        new branch (emitter, function)
            .ifTrue ("%" + labelIfTrue).ifFalse ("%" + labelIfFalse)
            .cond (cond).build ();

        // Case: true
        new label (emitter, function).name (labelIfTrue).build ();
        values[1].genLLVM (env, emitter, function);
        new branch (emitter, function).ifTrue ("%" + labelEnd).build ();

        // Case: false?
        if (values[2] != null) {
            new label (emitter, function).name (labelIfFalse).build ();
            values[2].genLLVM (env, emitter, function);
            new branch (emitter, function).ifTrue ("%" + labelEnd).build ();
        }

        new label (emitter, function).name (labelEnd).build ();
    }

    public void print (java.io.PrintStream out) {
        out.print ("(if ");
        values[0].print (out);
        out.println ();
        values[1].print (out, 2);
        out.println ();
        if (values[2] != null) {
            values[2].print (out, 2);
            out.println ();
        }
        out.println (")");
    }

    public static final Statement.StatementCreator CREATOR;
    static {
        CREATOR = new Statement.StatementCreator () {
                public Statement create (Env env, TokenStream stream,
                                         Method method) throws CError
                {
                    return new StIf (env, stream, method);
                }
            };
    }
}

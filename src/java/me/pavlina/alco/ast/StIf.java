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
import me.pavlina.alco.parse.ExpressionParser;
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
        values[0] = (AST) ExpressionParser.parse (env, stream, method, ")");
        if (values[0] == null)
            throw Unexpected.after ("expression", temp);
        values[0].setParent (this);
        temp = stream.next ();
        if (temp.is (Token.NO_MORE))
            throw UnexpectedEOF.after (")", stream.last ());
        else if (!temp.is (Token.OPER, ")"))
            throw Unexpected.after (")", stream.last ());

        // Body
        if (stream.peek ().is (Token.NO_MORE))
            throw UnexpectedEOF.after ("body", stream.last ());

        values[1] = new Scope (env, stream, method);
        values[1].setParent (this);

        // Else?
        temp = stream.peek ();
        if (!temp.is (Token.WORD, "else")) {
            values[2] = null;
            return;
        }
        stream.next ();

        values[2] = new Scope (env, stream, method);
        values[2].setParent (this);
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

    public void genLLVM (Env env, Emitter emitter, Function function) {
        Block labelIfTrue = new Block ();
        Block labelAfterTrue = new Block ();
        Block labelIfFalse = new Block ();
        Block labelEnd = null;
        if (values[2] == null)
            labelEnd = labelIfFalse;
        else
            labelEnd = new Block ();
        
        function.add (new COMMENT ().text ("IF, line " + (1+token.line)));

        // Condition
        values[0].genLLVM (env, emitter, function);
        Instruction boolCond = ((Expression) values[0]).getInstruction ();
        Cast c = new Cast (token)
            .value (boolCond).type (((Expression) values[0]).getType ())
            .dest (new Type (env, "bool", null));
        c.genLLVM (env, emitter, function);

        Instruction cond = new BINARY ()
            .op ("icmp ne").type ("i8").lhs (c.getInstruction ()).rhs ("0");
        function.add (cond);
        
        // Branch
        function.add (new BRANCH ().cond (cond).T (labelIfTrue)
                      .F (labelIfFalse));

        // Case: true
        function.add (labelIfTrue);
        values[1].genLLVM (env, emitter, function);
        function.add (new BRANCH ().dest (labelEnd));

        // Case: false?
        if (values[2] != null) {
            function.add (labelIfFalse);
            values[2].genLLVM (env, emitter, function);
            function.add (new BRANCH ().dest (labelEnd));
        }

        function.add (labelEnd);
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

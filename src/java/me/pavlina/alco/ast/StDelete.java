// Copyright (c) 2011, Christopher Pavlina. All rights reserved.

package me.pavlina.alco.ast;
import me.pavlina.alco.compiler.Env;
import me.pavlina.alco.compiler.errors.*;
import me.pavlina.alco.lex.TokenStream;
import me.pavlina.alco.lex.Token;
import me.pavlina.alco.language.Keywords;
import me.pavlina.alco.language.Type;
import static me.pavlina.alco.language.Type.Encoding;
import me.pavlina.alco.language.HasType;
import me.pavlina.alco.language.Resolver;
import me.pavlina.alco.parse.ExpressionParser;
import me.pavlina.alco.llvm.*;
import me.pavlina.alco.codegen.Cast;
import java.util.List;
import java.util.ArrayList;
import java.util.Arrays;

/**
 * Delete. Syntax:
 * delete var [{, var}] ;
 */
public class StDelete extends Statement
{
    Token token;
    List<Expression> values;

    public StDelete (Env env, TokenStream stream, Method method) throws CError {
        token = stream.next ();
        assert token.is (Token.WORD, "delete");

        values = new ArrayList<Expression> ();

        for (;;) {
            Expression exp = ExpressionParser.parse (env, stream, method, ";,");
            if (exp == null)
                throw Unexpected.after ("expression", stream.peek ());
            values.add (exp);
            Token temp = stream.next ();
            if (temp.is (Token.NO_MORE))
                throw UnexpectedEOF.after ("; or ,", stream.last ());
            else if (temp.is (Token.OPER, ";"))
                break;
            else if (!temp.is (Token.OPER, ","))
                throw UnexpectedEOF.after ("; or ,", stream.last ());
        }
    }

    public Token getToken () {
        return token;
    }

    @SuppressWarnings("unchecked")
    public List<AST> getChildren () {
        return (List) values;
    }

    public void checkTypes (Env env, Resolver resolver) throws CError {
        for (Expression i: values) {
            i.checkTypes (env, resolver);
            Encoding enc = i.getType ().getEncoding ();
            if (enc != Encoding.POINTER && enc != Encoding.ARRAY) {
                throw Unexpected.at ("array or pointer", i.getToken ());
            }
        }
    }

    public void genLLVM (Env env, Emitter emitter, Function function) {
        for (Expression i: values) {
            // The expression is either an array or a pointer. Either way,
            // as far as LLVM sees it's just a pointer. We bitcast it to the
            // right kind and toss it at free().
            i.genLLVM (env, emitter, function);
            Instruction bc = new CONVERT ()
                .op ("bitcast").stype (LLVMType.getLLVMName (i.getType ()))
                .dtype ("i8*").value (i.getInstruction ());
            Instruction call = new CALL ()
                .type ("void").fun ("@" + env.getFree ()).arg (bc);
            function.add (bc);
            function.add (call);
        }
    }

    public void print (java.io.PrintStream out) {
        out.print ("(delete");
        for (Expression i: values) {
            out.print (" ");
            i.print (out);
        }
        out.print (")");
    }

    public static final Statement.StatementCreator CREATOR;
    static {
        CREATOR = new Statement.StatementCreator () {
                public Statement create (Env env, TokenStream stream,
                                         Method method)
                    throws CError
                {
                    return new StDelete (env, stream, method);
                }
            };
    }
}

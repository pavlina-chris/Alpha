// Copyright (c) 2011, Christopher Pavlina. All rights reserved.

package me.pavlina.alco.ast;
import me.pavlina.alco.compiler.errors.*;
import me.pavlina.alco.compiler.Env;
import me.pavlina.alco.lex.TokenStream;
import me.pavlina.alco.lex.Token;
import me.pavlina.alco.language.Resolver;
import me.pavlina.alco.language.IntLimits;
import me.pavlina.alco.language.Type;
import static me.pavlina.alco.language.Type.Encoding;
import me.pavlina.alco.parse.TypeParser;
import me.pavlina.alco.codegen.Cast;
import me.pavlina.alco.codegen.NewArray;
import me.pavlina.alco.codegen.NewBackedArray;
import me.pavlina.alco.llvm.*;
import java.util.List;
import java.util.ArrayList;
import java.io.PrintStream;

/**
 * New */
public class NewValue extends Expression
{
    Token token;
    Type writtenType, type;
    Instruction instruction;
    List<Expression> arguments;
    Cast szCast;
    NewArray newarray;
    NewBackedArray newbackedarray;
    boolean handleOOM;

    /**
     * Create a NewValue from the stream */
    public NewValue (Env env, TokenStream stream) throws CError {
        token = stream.next ();
        assert token.is (Token.WORD, "new");
        writtenType = TypeParser.parse (stream, env);
    }

    public Instruction getInstruction () {
        return instruction;
    }

    public Token getToken () {
        return token;
    }

    @SuppressWarnings("unchecked")
    public List<AST> getChildren () {
        return (List) arguments;
    }

    /**
     * Set the arguments. (call (new ...)) is replaced using an AST
     * modification pass. */
    public void setArguments (List<Expression> args) {
        arguments = args;
        for (Expression i: args)
            i.setParent (this);
    }
    
    public void checkTypes (Env env, Resolver resolver) throws CError {
        // new primitive; (like "new int;") allocates a value of that type,
        //  returning primitive*.
        // new primitive (n) allocates 'n' of that type, returning
        //  primitive*.
        // new nonprimitive; allocates an object of that type, returning
        //  nonprimitive.
        // new T[] (n); allocates an array of 'n' of that type, returning
        //  T[].
        // new T[] (T* p, n); creates an array 'n' long, backed by 'p',
        //  returning T[].
        if (arguments == null) arguments = new ArrayList<Expression> ();
        for (Expression i: arguments)
            i.checkTypes (env, resolver);
        Encoding enc = writtenType.getEncoding ();
        switch (enc) {
        case UINT:
        case SINT:
        case FLOAT:
        case POINTER:
        case BOOL:
            type = writtenType.getPointer (env);
            if (arguments.size () == 1) {
                Type.checkCoerce (arguments.get (0),
                                  new Type (env, "size", null), token);
                szCast = new Cast (token).type (arguments.get (0).getType ())
                    .dest (new Type (env, "size", null));
                szCast.checkTypes (env, resolver);
            } else if (arguments.size () != 0) {
                throw Unexpected.at ("(size) or no arguments", token);
            }
            break;
        case ARRAY:
            type = writtenType;
            if (arguments.size () == 1) {
                newarray = new NewArray (token);
                newarray.type (type).lenty (arguments.get (0).getType ());
                newarray.checkTypes (env, resolver);
            } else if (arguments.size () == 2) {
                // Backing pointer must be of same subtype as array
                Type back_sub = arguments.get (0).getType ().getSubtype ();
                Type array_sub = writtenType.getSubtype ();
                Encoding back_enc = arguments.get (0).getType ().getEncoding ();
                if (!back_sub.equals (array_sub) ||
                    back_enc != Encoding.POINTER) {
                    throw Unexpected.at
                        ("(size) or (" + writtenType.getSubtype ()
                         + "*, size)", token);
                }
                newbackedarray = new NewBackedArray (token);
                newbackedarray.type (type).lenty (arguments.get (1).getType ());
                newbackedarray.checkTypes (env, resolver);
            } else {
                throw Unexpected.at ("(size) or (" + writtenType.getSubtype ()
                                     + "*, size)", token);
            }
            break;
        default:
            throw Unexpected.at ("array or primitive type", token);
        }
        handleOOM = resolver.getHandleOOM ();
    }

    public Type getType () {
        return type;
    }

    public void genLLVM (Env env, Emitter emitter, Function function) {
        if (newarray != null) {
            arguments.get (0).genLLVM (env, emitter, function);
            newarray.length (arguments.get (0).getInstruction ());
            newarray.genLLVM (env, emitter, function);
            instruction = newarray.getInstruction ();
            return;
        } else if (newbackedarray != null) {
            arguments.get (0).genLLVM (env, emitter, function);
            arguments.get (1).genLLVM (env, emitter, function);
            newbackedarray.backing (arguments.get (0).getInstruction ());
            newbackedarray.length (arguments.get (1).getInstruction ());
            newbackedarray.genLLVM (env, emitter, function);
            instruction = newbackedarray.getInstruction ();
            return;
        }
        Encoding enc = writtenType.getEncoding ();
        switch (enc) {
        case UINT:
        case SINT:
        case FLOAT:
        case POINTER:
        case BOOL: {
            Instruction number;
            if (arguments.size () == 0) {
                number = new Placeholder
                    (Integer.toString (writtenType.getSize ()),
                     "i" + env.getBits ());
            } else {
                arguments.get (0).genLLVM (env, emitter, function);
                szCast.value (arguments.get (0).getInstruction ());
                szCast.genLLVM (env, emitter, function);
                number = new BINARY ()
                    .op ("mul").type ("i" + env.getBits ())
                    .lhs (szCast.getInstruction ())
                    .rhs (Integer.toString (env.getBits () / 8));
                function.add (number);
            }
            Instruction call;
            if (env.getNullOOM ()) {
                call = new CALL ()
                    .type ("i8*").fun ("@" + env.getMalloc ()).arg (number);
            } else {
                call = new CALL ()
                    .type ("i8*").fun ("@$$new").arg (number)
                    .arg ("i32", Integer.toString (token.line + 1))
                    .arg ("i32", Integer.toString (token.col + 1))
                    .arg ("i8(i" + env.getBits () + ",i32,i32)*",
                          handleOOM ? "@$$oom" : "null")
                    .arg ("i8*(i" + env.getBits () + ")*",
                          "@" + env.getMalloc ());
            }
            function.add (call);
            Instruction bc = new CONVERT ()
                .op ("bitcast")
                .stype ("i8*").dtype (LLVMType.getLLVMName (type))
                .value (call);
            function.add (bc);
            instruction = bc;
        } break;
        default:
            assert false: enc;
            return;
        }
    }

    public void checkPointer (boolean write, Token token) throws CError {
        throw CError.at ("cannot assign to instantiation", token);
    }

    public Instruction getPointer (Env env, Emitter emitter, Function function)
    {
        return null;
    }

    public void print (PrintStream out) {
        out.print ("(new ");
        out.print (writtenType);
        out.print (")");
    }
}

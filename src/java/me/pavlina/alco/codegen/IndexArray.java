// Copyright (c) 2011, Christopher Pavlina. All rights reserved.

package me.pavlina.alco.codegen;
import me.pavlina.alco.compiler.Env;
import me.pavlina.alco.compiler.errors.*;
import me.pavlina.alco.lex.Token;
import me.pavlina.alco.llvm.*;
import me.pavlina.alco.language.Type;
import me.pavlina.alco.language.Resolver;
import java.util.List;
import java.util.ArrayList;
import java.util.Arrays;

/**
 * Array index */
public class IndexArray {

    Token token;
    Instruction arr, ind, instruction;
    Type arrType, indType;
    boolean handleOOB;

    public IndexArray (Token token) {
        this.token = token;
    }

    /**
     * Set type of left operand */
    public IndexArray ltype (Type t) { arrType = t; return this; }

    /**
     * Set type of non-unpacked right operand */
    public IndexArray rtype (Type t) { indType = t; return this; }

    /**
     * Set the array instruction */
    public IndexArray linst (Instruction i) { arr = i; return this; }

    /**
     * Set the index instruction */
    public IndexArray rinst (Instruction i) { ind = i; return this; }

    /**
     * Check types.
     * @pre lhs is checked. rhs is not OpComma and is checked. */
    public void checkTypes (Env env, Resolver resolver) throws CError {
        assert arrType.getEncoding () == Type.Encoding.ARRAY;
        Type.checkCoerce (indType, new Type (env, "size", null), token);
        handleOOB = resolver.getHandleOOB ();
    }

    public Instruction getPointer (Env env, Emitter emitter, Function function)
    {
        // Array:
        // +--------+---------+      +---+---+---+---+---+
        // | LENGTH | POINTER |  ->  |   |   |   |   |   |
        // +--------+---------+      +---+---+---+---+---+

        // Prepare information
        String size = "i" + env.getBits ();
        String sizep = size + "*";
        String elemp = LLVMType.getLLVMName (arrType.getSubtype ()) + "*";
        Instruction array = new CONVERT ()
            .op ("bitcast").stype ("i8*").dtype (sizep).value (arr);
        function.add (array);
        Cast index_ = new Cast (token)
            .value (ind).type (indType).dest (new Type (env, "size", null));
        index_.genLLVM (env, emitter, function);
        Instruction index = index_.getInstruction ();

        // Bounds-checking
        if (env.getBoundCheck ()) {
            Block check = new Block ();
            Block bad = new Block ();
            Block good = new Block ();
            Instruction isNull = new BINARY ()
                .op ("icmp eq").lhs (array).rhs ("null").type (sizep);
            Instruction nullBranch = new BRANCH ()
                .cond (isNull).T (bad).F (check);
            function.add (isNull);
            function.add (nullBranch);
            function.add (check);
            Instruction length = new LOAD ()
                .type (size).pointer (array);
            Instruction isGood = new BINARY ()
                .op ("icmp ult").lhs (index).rhs (length).type (size);
            Instruction lenBranch = new BRANCH ()
                .cond (isGood).T (good).F (bad);
            function.add (length);
            function.add (isGood);
            function.add (lenBranch);
            function.add (bad);
            Instruction bcCall = new CALL ()
                .type ("void").fun ("@$$oobmsg")
                .arg ("i32", Integer.toString (token.line + 1))
                .arg ("i32", Integer.toString (token.col + 1))
                .arg ("void(i32,i32)*",
                      handleOOB ? "@$$bounds" : "null");
            function.add (bcCall);
            function.add (new UNREACHABLE ());
            function.add (good);
        }

        Instruction ptrField = new GETELEMENTPTR ()
            .type (sizep).rtype (sizep).addIndex (1).value (array);
        Instruction ptr_ = new LOAD ()
            .type (size).pointer (ptrField);
        Instruction ptr = new CONVERT ()
            .op ("inttoptr").stype (size).dtype (elemp).value (ptr_);
        Instruction n = new GETELEMENTPTR ()
            .type (elemp).rtype (elemp).addIndex (index).value (ptr);
        function.add (ptrField);
        function.add (ptr_);
        function.add (ptr);
        function.add (n);
        return n;
    }

    public void genLLVM (Env env, Emitter emitter, Function function) {
        Instruction ptr = this.getPointer (env, emitter, function);
        Instruction val = new LOAD ()
            .type (LLVMType.getLLVMName (arrType.getSubtype ()))
            .pointer (ptr);
        function.add (val);
        instruction = val;
    }

    public Instruction getInstruction () {
        return instruction;
    }

}

// Copyright (c) 2011, Christopher Pavlina. All rights reserved.6

package me.pavlina.alco.codegen;
import me.pavlina.alco.ast.Expression;
import me.pavlina.alco.compiler.Env;
import me.pavlina.alco.compiler.errors.*;
import me.pavlina.alco.language.Type;
import static me.pavlina.alco.language.Type.Encoding.*;
import me.pavlina.alco.language.Resolver;
import me.pavlina.alco.llvm.*;
import me.pavlina.alco.lex.Token;

public class NewArray {
    Token token;
    Instruction instruction;
    Instruction length;
    Type type, lenty, size_t;
    boolean handleOOM;
    Cast szCast;

    public NewArray (Token token) {
        this.token = token;
    }

    /**
     * Set the array type */
    public NewArray type (Type type) {
        this.type = type;
        return this;
    }

    /**
     * Set the type of the specified length */
    public NewArray lenty (Type t) {
        lenty = t;
        return this;
    }

    /**
     * Set the array length */
    public NewArray length (Instruction length) {
        this.length = length;
        return this;
    }

    public void checkTypes (Env env, Resolver resolver) throws CError {
        size_t = new Type (env, "size", null);
        Type.checkCoerce (lenty, size_t, token);
        szCast = new Cast (token).type (lenty).dest (size_t);
        szCast.checkTypes (env, resolver);
        handleOOM = resolver.getHandleOOM ();
    }

    public void genLLVM (Env env, Emitter emitter, Function function) {
        // +--------+---------+      +---+---+---+---+---+
        // | LENGTH | POINTER |  ->  |   |   |   |   |   |
        // +--------+---------+      +---+---+---+---+---+

        String Lsize_t = "i" + env.getBits ();

        // Prepare the array length
        szCast.value (length);
        szCast.genLLVM (env, emitter, function);
        Instruction arrayLen = szCast.getInstruction ();
        Instruction numBytes = new BINARY ()
            .op ("mul").type (Lsize_t).lhs (arrayLen)
            .rhs (Integer.toString (type.getSubtype ().getSize ()));
        function.add (numBytes);

        // Calculate the total amount to allocate (we allocate both above
        // structures contiguously)
        // (env.getBits () / 4 is the size of two pointers, or a size and ptr)
        Instruction totalSize = new BINARY ()
            .op ("add").type (Lsize_t).lhs (numBytes)
            .rhs (Integer.toString (env.getBits () / 4));
        function.add (totalSize);

        // Allocate memory
        if (env.getNullOOM ()) {
            instruction = new CALL ()
                .type ("i8*").fun ("@" + env.getMalloc ()).arg (totalSize);
        } else {
            instruction = new CALL ()
                .type ("i8*").fun ("@$$new").arg (totalSize)
                .arg ("i32", Integer.toString (token.line + 1))
                .arg ("i32", Integer.toString (token.col + 1))
                .arg ("i8(i" + env.getBits () + ",i32,i32)*",
                      handleOOM ? "@$$oom" : "null")
                .arg ("i8*(i" + env.getBits () + ")*",
                      "@" + env.getMalloc ());
        }
        Block memGood = new Block ();
        Block bottom = new Block ();
        Instruction isNull = new BINARY ()
            .op ("icmp eq").type ("i8*").lhs (instruction).rhs ("null");
        Instruction branch = new BRANCH ()
            .cond (isNull).T (bottom).F (memGood);
        function.add (instruction);
        function.add (isNull);
        function.add (branch);
        function.add (memGood);
        Instruction alloc = new CONVERT ()
            .op ("bitcast").stype ("i8*").dtype (Lsize_t + "*")
            .value (instruction);
        function.add (alloc);

        // Store the array length
        function.add (new STORE ().pointer (alloc).value (arrayLen));
        
        // Store the pointer
        Instruction ptrField = new GETELEMENTPTR ()
            .type (Lsize_t + "*").rtype (Lsize_t + "*")
            .value (alloc).addIndex (1);
        Instruction _arrBegin = new GETELEMENTPTR ()
            .type (Lsize_t + "*").rtype (Lsize_t + "*")
            .value (alloc).addIndex (2);
        Instruction arrBegin = new CONVERT ()
            .op ("ptrtoint").stype (Lsize_t + "*").dtype (Lsize_t)
            .value (_arrBegin);
        function.add (ptrField);
        function.add (_arrBegin);
        function.add (arrBegin);
        function.add (new STORE ().pointer (ptrField).value (arrBegin));
        function.add (new BRANCH ().dest (bottom));
        function.add (bottom);
    }

    public Instruction getInstruction () {
        return instruction;
    }
}

// Copyright (c) 2011, Christopher Pavlina. All rights reserved.

package me.pavlina.alco.llvm;
import java.util.LinkedList;
import java.util.Iterator;

/**
 * LLVM basic block. This contains LLVM instructions, and ends with one
 * terminator instruction. */
public class Block {

    LinkedList<Instruction> instructions;
    String id;

    public Block () {
        instructions = new LinkedList<Instruction> ();
    }

    /**
     * Try to add the instruction to the block. If the last instruction added
     * was a terminator, no more instructions may be added, and add() will
     * return false. */
    public boolean add (Instruction i) {
        if (isTerminated ()) return false;
        instructions.add (i);
        return true;
    }

    /**
     * Return whether this block has been terminated yet. */
    public boolean isTerminated () {
        return !instructions.isEmpty ()
            && Terminator.class.isInstance (instructions.getLast ());
    }

    /**
     * Get the ID of this block. */
    public String getId () { return id; }

    /**
     * Set the ID of this block (as a label) */
    public void setId (String id) { this.id = id; }

    /**
     * Number all instructions in this block, and the block itself, from the
     * starting point. Return the next number after the end. Note that for the
     * first block, if unlabeled, use 0 for a start, because the block will
     * number itself. */
    public int number (int start) {
        if (id == null) {
            id = "%" + Integer.toString (start++);
        }
        for (Instruction i: instructions) {
            if (i.needsId ()) {
                i.setId ("%" + Integer.toString (start++));
            }
        }
        return start;
    }

    public String toString () {
        StringBuilder sb = new StringBuilder ();
        char ch = id.charAt (1);
        if (id.equals ("%0")) {
            // -1 first block - no comment
        } else if (ch >= '0' && ch <= '9') {
            // Numbered block - comment it
            sb.append ("; ").append (id).append (":\n");
        } else {
            // Label
            sb.append (id.substring (1)).append (":\n");
        }

        for (Instruction i: instructions) {
            sb.append ("  ").append (i);
        }
        return sb.toString ();
    }
}

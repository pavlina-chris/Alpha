// Copyright (c) 2011, Christopher Pavlina. All rights reserved.

package me.pavlina.alco.llvm;

/**
 * phi */
public class PHI implements Instruction {

    String type, value, label, id;
    Instruction iValue;
    Block bLabel;
    String[] values, labels;
    Instruction[] iValues;
    Block[] bLabels;

    public PHI () {}

    /**
     * Required: Set type */
    public PHI type (String t) { type = t; return this; }

    /**
     * Required: Set values and labels
     * @param value First value (String or Instruction)
     * @param label First label (String or Block)
     * @param pairs... Additional values and labels
     * @throws IllegalArgumentException If pairs has an odd number of elements
     * @throws ClassCastException If a type does not match */
    public PHI pairs (Object value, Object label, Object... pairs) {
        if (String.class.isInstance (value)) {
            this.value = (String) value;
        } else if (Instruction.class.isInstance (value)) {
            this.iValue = (Instruction) value;
        } else throw new ClassCastException ();
        if (String.class.isInstance (label)) {
            this.label = (String) label;
        } else if (Block.class.isInstance (value)) {
            this.bLabel = (Block) label;
        } else throw new ClassCastException ();
        if (pairs.length % 2 != 0)
            throw new IllegalArgumentException ();
        values = new String[pairs.length / 2];
        labels = new String[pairs.length / 2];
        iValues = new Instruction[pairs.length / 2];
        bLabels = new Block[pairs.length / 2];
        for (int i = 0; i < pairs.length; i += 2) {
            if (String.class.isInstance (pairs[i])) {
                values[i / 2] = (String) pairs[i];
            } else if (Instruction.class.isInstance (pairs[i])) {
                iValues[i / 2] = (Instruction) pairs[i];
            } else throw new ClassCastException ();
            if (String.class.isInstance (pairs[i + 1])) {
                labels[i / 2] = (String) pairs[i + 1];
            } else if (Block.class.isInstance (pairs[i])) {
                bLabels[i / 2] = (Block) pairs[i + 1];
            } else throw new ClassCastException ();
        }
        return this;
    }

    public String toString () {
        StringBuilder sb = new StringBuilder ();
        if (iValue != null) {
            type = iValue.getType ();
            value = iValue.getId ();
        }
        if (bLabel != null) {
            label = bLabel.getId ();
        }
        sb.append (id).append (" = phi ").append (type)
            .append (" [ ").append (value).append (", ").append (label)
            .append (" ]");
        for (int i = 0; i < values.length; ++i) {
            if (iValues[i] != null)
                values[i] = iValues[i].getId ();
            if (bLabels[i] != null)
                labels[i] = bLabels[i].getId ();
            sb.append (", [ ").append (values[i]).append (", ")
                .append (labels[i]).append ("]");
        }
        sb.append ('\n');
        return sb.toString ();
    }

    public boolean needsId () { return true; }
    public void setId (String id) { this.id = id; }
    public String getId () { return id; }
    public String getType () { return type + "*"; }
}

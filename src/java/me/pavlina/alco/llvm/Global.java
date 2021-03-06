// Copyright (c) 2011, Christopher Pavlina. All rights reserved.

package me.pavlina.alco.llvm;

/**
 * Global. Note that Instruction is implemented to allow using this
 * as a value. */
public class Global extends RootObject implements Instruction {

    String name, type, value, linkage;

    /**
     * Create a named global.
     * @param name Global name. Should start with @.
     * @param type Global type.
     * @param value Global value. Remember "zeroinitializer"
     * @param linkage Linkage type. */
    public Global (String name, String type, String value, String linkage) {
        this.name = name;
        this.type = type;
        this.value = value;
        this.linkage = linkage;
    }

    /**
     * Create an unnamed global.
     * @param type Global type.
     * @param value Global value. Remember "zeroinitializer"
     * @param linkage Linkage type. */
    public Global (String type, String value, String linkage) {
        this.type = type;
        this.value = value;
        this.linkage = linkage;
    }


    public int getLevel () { return RootObject.LEVEL_GLOBAL; }

    public boolean needsId () { return name == null; }

    public void setId (String id) { name = id; }
    
    public String getId () { return name; }

    public String getType () { return type + "*"; }

    public String toString () {
        return String.format ("%s = %s global %s %s\n",
                              name, linkage, type, value);
    }

}

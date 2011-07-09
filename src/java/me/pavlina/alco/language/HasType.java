// Copyright (c) 2011, Christopher Pavlina. All rights reserved.

package me.pavlina.alco.language;
import me.pavlina.alco.lex.Token;

public interface HasType {
    public Type getType ();
    public Token getToken ();
}

package vg.ritter.tattlefont.utility;

import java.io.Serializable;

public class Pair<A,B> implements Serializable {
    public A a;
    public B b;
    public Pair(A a, B b) {
        this.a = a;
        this.b = b;
    }
}

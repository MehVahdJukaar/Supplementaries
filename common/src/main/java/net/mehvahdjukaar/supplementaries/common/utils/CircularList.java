package net.mehvahdjukaar.supplementaries.common.utils;

import java.util.LinkedList;

//@Deprecated(forRemoval = true)
public class CircularList<T> extends LinkedList<T> {

    private final int size;

    public CircularList(int size) {
        super();
        this.size = size;
    }

    @Override
    public void addFirst(T t) {
        if (this.size() >= this.size) {
            this.removeLast();
        }
        super.addFirst(t);
    }

    @Override
    public void addLast(T t) {
        if (this.size() >= this.size) {
            this.removeFirst();
        }
        super.addLast(t);
    }
}

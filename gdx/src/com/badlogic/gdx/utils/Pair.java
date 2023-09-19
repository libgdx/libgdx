package com.badlogic.gdx.utils;

public class Pair<T, U> {

    public T first;
    public U second;

    public Pair(T first, U second) {
        this.first = first;
        this.second = second;
    }

    @Override
    public boolean equals(Object o) {
        if (o instanceof Pair) {
            Pair<?, ?> p = (Pair<?, ?>) o;
            return first.equals(p.first) && second.equals(p.second);
        }
        return false;
    }

    @Override
    public int hashCode() {
        return first.hashCode() + second.hashCode();
    }

    @Override
    public String toString() {
        return "(" + first.toString() + ", " + second.toString() + ")";
    }
}

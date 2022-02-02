package me.travja.performances.api.models;

import java.util.ArrayList;

public abstract class LazyList<E> extends ArrayList<E> {

    private boolean loaded = false;

    public void doLoad() {
        if (loaded)
            return;

        load();
        loaded = true;
    }

    protected abstract void load();

    @Override
    public int size() {
        doLoad();
        return super.size();
    }

    @Override
    public boolean contains(Object o) {
        doLoad();
        return super.contains(o);
    }

    @Override
    public E get(int index) {
        doLoad();
        return super.get(index);
    }

    @Override
    public boolean add(E e) {
        doLoad();
        return super.add(e);
    }

    @Override
    public boolean remove(Object o) {
        doLoad();
        return super.remove(o);
    }
}

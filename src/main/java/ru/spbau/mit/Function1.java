package ru.spbau.mit;

public abstract class Function1<T, R> {
    public abstract R apply(T x);

    public <E> Function1<T, E> compose(final Function1<? super R, E> g) {
        return new Function1<T, E>() {
            @Override
            public E apply(T x) {
                return g.apply(Function1.this.apply(x));
            }
        };
    }
}
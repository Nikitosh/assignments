package ru.spbau.mit;

public abstract class Function2<S, T, R> {
    public abstract R apply(S x, T y);

    public <E> Function2<S, T, E> compose(final Function1 <? super R, E> g) {
        return new Function2<S, T, E>() {
            @Override
            public E apply(S x, T y) {
                return g.apply(Function2.this.apply(x, y));
            }
        };
    }

    public <E extends S> Function1 <T, R> bind1(final E x) {
        return new Function1<T, R>() {
            @Override
            public R apply(T y) {
                return Function2.this.apply(x, y);
            }
        };
    }

    public <E extends T> Function1 <S, R> bind2(final E y) {
        return new Function1<S, R>() {
            @Override
            public R apply(S x) {
                return Function2.this.apply(x, y);
            }
        };
    }

    public Function1 <S, Function1 <T, R> > curry() {
        return new Function1<S, Function1<T, R>>() {
            @Override
            public Function1<T, R> apply(final S x) {
                return bind1(x);
            }
        };
    }
}
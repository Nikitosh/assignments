package ru.spbau.mit;

public abstract class Predicate<T> extends Function1<T, Boolean>{
    public <S extends T> Predicate<S> or(final Predicate<S> predicate) {
        return new Predicate<S>() {
            @Override
            public Boolean apply(final S x) {
                return Predicate.this.apply(x) || predicate.apply(x);
            }
        };
    }

    public <S extends T> Predicate<S> and(final Predicate<S> predicate) {
        return new Predicate<S>() {
            @Override
            public Boolean apply(final S x) {
                return Predicate.this.apply(x) && predicate.apply(x);
            }
        };
    }

    public Predicate<T> not() {
        return new Predicate<T>() {
            @Override
            public Boolean apply(T x) {
                return !Predicate.this.apply(x);
            }
        };
    }

    public static final Predicate ALWAYS_TRUE = new Predicate() {
        @Override
        public Boolean apply(Object x) {
            return true;
        }
    };

    public static final Predicate ALWAYS_FALSE = ALWAYS_TRUE.not();

}
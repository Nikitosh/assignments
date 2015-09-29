package ru.spbau.mit;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class Collections {
    public static <T, R> List<R> map(Function1<? super T, R> f, Iterable<T> collection) {
        List<R> resultCollection = new ArrayList<R>();
        for (T element : collection) {
            resultCollection.add(f.apply(element));
        }
        return resultCollection;
    }

    public static <T> List<T> filter(Predicate<? super T> f, Iterable<T> collection) {
        List<T> resultCollection = new ArrayList<T>();
        for (T element : collection) {
            if (f.apply(element)) {
                resultCollection.add(element);
            }
        }
        return resultCollection;
    }

    public static <T> List<T> takeWhile(Predicate<? super T> predicate, Iterable<T> collection) {
        List<T> resultCollection = new ArrayList<T>();
        for (T element : collection) {
            if (predicate.apply(element)) {
                resultCollection.add(element);
            } else {
                break;
            }
        }
        return resultCollection;
    }

    public static <T> List<T> takeUnless(Predicate<? super T> predicate, Iterable<T> collection) {
        return takeWhile(predicate.not(), collection);
    }

    public static <T, R> R foldl(Function2<? super R, ? super T, ? extends R> f, R value, Iterable<T> collection) {
        for (T element : collection) {
            value = f.apply(value, element);
        }
        return value;
    }

    private static <T, R> R foldr(Function2<? super T, ? super R, ? extends R> f, R value, Iterator<T> collectionIterator) {
        if (!collectionIterator.hasNext()) {
            return value;
        }
        return f.apply(collectionIterator.next(), foldr(f, value, collectionIterator));
    }

    public static <T, R> R foldr(Function2<? super T, ? super R, ? extends R> f, R startValue, Iterable<T> collection) {
        return foldr(f, startValue, collection.iterator());
    }
}
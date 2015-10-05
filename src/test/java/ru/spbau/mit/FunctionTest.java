package ru.spbau.mit;

import static org.junit.Assert.*;
import org.junit.Test;

import java.util.*;

public class FunctionTest {
    private static Function1<Integer, Integer> absFunction = new Function1<Integer, Integer>() {
        @Override
        public Integer apply(Integer x) {
            return Math.abs(x);
        }
    };

    private static Function1<Integer, Integer> succFunction = new Function1<Integer, Integer>() {
        @Override
        public Integer apply(Integer x) {
            return x + 1;
        }
    };

    private static Function2<Integer, Integer, Integer> plusFunction = new Function2<Integer, Integer, Integer>() {
        @Override
        public Integer apply(Integer x, Integer y) {
            return x + y;
        }
    };

    private static Function2<Integer, Integer, Integer> minusFunction = new Function2<Integer, Integer, Integer>() {
        @Override
        public Integer apply(Integer x, Integer y) {
            return x - y;
        }
    };

    private static Predicate<Integer> evenPredicate = new Predicate<Integer>() {
        @Override
        public Boolean apply(Integer x) {
            return x % 2 == 0;
        }
    };

    @Test
    public void testFunction1() {
        assertTrue(absFunction.apply(5) == 5);
        assertTrue(absFunction.apply(-5) == 5);

        Function1<Integer, Integer> succAbsFunction = absFunction.compose(succFunction);
        assertTrue(succAbsFunction.apply(-2) == 3);
        assertTrue(succAbsFunction.apply(0) == 1);
        assertTrue(succAbsFunction.apply(2) == 3);
    }

    @Test
    public void testFunction2() {
        assertTrue(plusFunction.compose(succFunction).apply(2, -5) == -2);

        Function1<Integer, Integer> bind1PlusFunction = plusFunction.bind1(5);
        assertTrue(bind1PlusFunction.apply(2) == 7);

        Function1<Integer, Integer> bind2PlusFunction = plusFunction.bind2(5);
        assertTrue(bind1PlusFunction.apply(2) == 7);

        Function1<Integer, Function1<Integer, Integer>> curryPlusFunction = plusFunction.curry();
        assertTrue(curryPlusFunction.apply(5).apply(2) == 7);
    }

    @Test
    public void testPredicate() {
        Predicate<Integer> positivePredicate = new Predicate<Integer>() {
            @Override
            public Boolean apply(Integer x) {
                return x > 0;
            }
        };

        assertTrue(evenPredicate.and(positivePredicate).apply(5) == false);
        assertTrue(evenPredicate.and(positivePredicate).apply(-5) == false);
        assertTrue(evenPredicate.and(positivePredicate).apply(4) == true);
        assertTrue(evenPredicate.and(positivePredicate).apply(-4) == false);

        assertTrue(evenPredicate.or(positivePredicate).apply(5) == true);
        assertTrue(evenPredicate.or(positivePredicate).apply(-5) == false);
        assertTrue(evenPredicate.or(positivePredicate).apply(4) == true);
        assertTrue(evenPredicate.or(positivePredicate).apply(-4) == true);

        Predicate<Integer> positiveLazyPredicate = new Predicate<Integer>() {
            @Override
            public Boolean apply(Integer x) {
                fail();
                return x > 0;
            }
        };

        assertTrue(evenPredicate.or(positiveLazyPredicate).apply(4) == true);
        assertTrue(evenPredicate.and(positiveLazyPredicate).apply(5) == false);

        assertTrue(evenPredicate.not().apply(5) == true);
        assertTrue(evenPredicate.not().apply(4) == false);

        assertTrue((boolean) Predicate.ALWAYS_TRUE.apply(5) == true);
        assertTrue((boolean) Predicate.ALWAYS_FALSE.apply(5) == false);
    }

    @Test
    public void testCollections() {
        ArrayList<Integer> arrayList = new ArrayList<>();
        arrayList.add(1);
        arrayList.add(3);
        arrayList.add(6);
        arrayList.add(10);

        List<Integer> mapArrayList = Collections.map(succFunction, arrayList);
        assertTrue(mapArrayList.size() == arrayList.size());
        for (int i = 0; i < arrayList.size(); i++) {
            assertTrue(mapArrayList.get(i) == arrayList.get(i) + 1);
        }

        List<Integer> filterArrayList = Collections.filter(evenPredicate, arrayList);
        assertTrue(filterArrayList.size() == 2);
        assertTrue(filterArrayList.get(0) == 6);
        assertTrue(filterArrayList.get(1) == 10);

        List<Integer> takeWhileArrayList = Collections.takeWhile(evenPredicate, arrayList);
        assertTrue(takeWhileArrayList.size() == 0);

        List<Integer> takeUnlessArrayList = Collections.takeUnless(evenPredicate, arrayList);
        assertTrue(takeUnlessArrayList.size() == 2);
        assertTrue(takeUnlessArrayList.get(0) == 1);
        assertTrue(takeUnlessArrayList.get(1) == 3);

        assert(Collections.foldl(minusFunction, 0, arrayList) == 0 - 1 - 3 - 6 - 10);
        assert(Collections.foldr(minusFunction, 0, arrayList) == 1 - (3 - (6 - (10 - 0))));
    }
}
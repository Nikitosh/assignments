package ru.spbau.mit;

import java.util.*;

public class TreeSetImpl<E> extends AbstractSet<E> {

    private static class Pair<S, T> {
        S first = null;
        T second = null;

        public Pair(S x, T y) {
            first = x;
            second = y;
        }

        public S first() {
            return first;
        }

        public T second() {
            return second;
        }
    }

    private static class Node<E> {
        private Node left = null, right = null;
        private static Random random = new Random();
        private int height = random.nextInt();
        private E value;
        private int treeSize = 1;

        public Node(E value) {
            this.value = value;
        }

        private void recalculateTreeSize() {
            treeSize = 1 + (left != null ? left.treeSize : 0) + (right != null ? right.treeSize : 0);
        }
    }

    private class TreapIterator implements Iterator {
        private Node<E> myNode = null;
        private Node<E> lastNode = null;

        public TreapIterator() {
            myNode = root;
            while (myNode != null && myNode.left != null) {
                myNode = myNode.left;
            }
        }

        private Node<E> getNextNode() {
            Node<E> curNode = root;
            Pair<Node, Node> pair = split(curNode, myNode.value, false);

            curNode = pair.second;
            while (curNode != null && curNode.left != null) {
                curNode = curNode.left;
            }
            merge(pair.first, pair.second);
            return curNode;
        }

        public E next() {
            if (myNode == null) {
                throw new NoSuchElementException();
            }
            lastNode = myNode;
            E value = myNode.value;
            myNode = getNextNode();
            return value;
        }

        public boolean hasNext() {
            if (myNode == null) {
                return false;
            }
            return true;
        }

        public void remove() {
            if (lastNode == null) {
                throw new IllegalStateException();
            }
            Pair<Node, Node> pair = split(root, lastNode.value, false);
            Pair<Node, Node> pair2 = split(pair.first, lastNode.value, true);
            Node curNode = pair2.first;
            while (curNode != null && curNode.right != null) {
                curNode = curNode.right;
            }
            lastNode = curNode;
            root = merge(pair2.first, pair.second);
        }
    }

    private Node merge(Node a, Node b) {
        if (a == null) {
            return b;
        }
        if (b == null) {
            return a;
        }
        if (a.height < b.height) {
            a.right = merge(a.right, b);
            a.recalculateTreeSize();
            return a;
        }
        else {
            b.left = merge(a, b.left);
            b.recalculateTreeSize();
            return b;
        }
    }

    private Pair<Node, Node> split(Node<E> curNode, E splitValue, boolean without) {
        if (curNode == null){
            return new Pair(null, null);
        }
        if (comparator.compare(splitValue, curNode.value) > 0 || comparator.compare(splitValue, curNode.value) == (without ? 1 : 0)) {
            Pair<Node, Node> pair = split(curNode.right, splitValue, without);
            curNode.right = pair.first;
            curNode.recalculateTreeSize();
            return new Pair(curNode, pair.second);
        }
        else {
            Pair<Node, Node> pair = split(curNode.left, splitValue, without);
            curNode.left = pair.second;
            curNode.recalculateTreeSize();
            return new Pair(pair.first, curNode);
        }
    }

    private Node<E> root = null;
    private Comparator<E> comparator;

    public TreeSetImpl(Comparator<E> comparator) {
        this.comparator = comparator;
    }

    public boolean contains(Object value) {
        Pair <Node, Node> pair = split(root, (E) value, false);
        Node<E> curNode = pair.first;
        while (curNode != null && curNode.right != null) {
            curNode = curNode.right;
        }
        if (curNode == null) {
            return false;
        }
        root = merge(pair.first, pair.second);
        return comparator.compare(curNode.value, (E) value) == 0;
    }

    public boolean add(E value) {
        if (contains(value)) {
            return false;
        }
        Pair<Node, Node> pair = split(root, value, false);
        root = merge(merge(pair.first, new Node(value)), pair.second);
        return true;
    }

    public boolean remove(Object value) {
        if (!contains(value)) {
            return false;
        }
        Pair <Node, Node> pair = split(root, (E) value, false);
        Node<E> curNode = pair.first;
        Pair <Node, Node> pair2 = split(curNode, (E) value, true);
        root = merge(pair2.first, pair.second);
        return true;
    }

    public int size() {
        return (root != null ? root.treeSize : 0);
    }

    public Iterator<E> iterator() {
        return new TreapIterator();
    }


}

package ru.spbau.mit;

import com.sun.org.apache.xpath.internal.SourceTree;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.util.LinkedList;
import java.util.Queue;

public class StringSetImpl implements StringSet, StreamSerializable {

    private static class TrieNode {
        private static final int ALPHABET_SIZE = 26;

        private TrieNode[] children = new TrieNode[2 * ALPHABET_SIZE];
        private boolean isTerminal;
        private int terminalDescendantNumber;
        private TrieNode parent;

        public TrieNode() {
            isTerminal = false;
            terminalDescendantNumber = 0;
            parent = null;
        }

        public TrieNode(TrieNode parent) {
            this();
            this.parent = parent;
        }

        static private int getCode(char c) {
            if (c >= 'a' && c <= 'z') {
                return c - 'a';
            }
            assert c >= 'A' && c <= 'Z';
            return c - 'A' + ALPHABET_SIZE;
        }

        public TrieNode getChild(char c) {
            return children[getCode(c)];
        }

        public void addChild(char c) {
            children[getCode(c)] = new TrieNode(this);
        }
    }

    private static final int GO_UP = 0;

    private TrieNode root = new TrieNode();
    private int size = 0;

    private TrieNode getNode(String element) {
        TrieNode currentNode = root;
        for (char c : element.toCharArray()) {
            TrieNode nextNode = currentNode.getChild(c);
            if (nextNode == null) {
                return null;
            }
            currentNode = nextNode;
        }
        return currentNode;
    }

    public boolean add(String element) {
        if (contains(element)) {
            return false;
        }
        TrieNode currentNode = root;
        for (char c : element.toCharArray()) {
            currentNode.terminalDescendantNumber++;
            if (currentNode.getChild(c) == null) {
                currentNode.addChild(c);
            }
            currentNode = currentNode.getChild(c);
        }
        currentNode.terminalDescendantNumber++;
        currentNode.isTerminal = true;
        size++;
        return true;
    }

    public boolean contains(String element) {
        TrieNode node = getNode(element);
        return node != null && node.isTerminal;
    }

    public boolean remove(String element) {
        TrieNode node = getNode(element);
        if (node == null) {
            return false;
        }
        node.isTerminal = false;
        while (node != null) {
            node.terminalDescendantNumber--;
            node = node.parent;
        }
        size--;
        return true;
    }

    public int size() {
        return size;
    }

    public int howManyStartsWithPrefix(String prefix) {
        TrieNode node = getNode(prefix);
        if (node == null) {
            return 0;
        }
        return node.terminalDescendantNumber;
    }

    private char getChar(int number) {
        if (number < TrieNode.ALPHABET_SIZE) {
            return (char) ('a' + number);
        }
        return (char) ('A' + number - TrieNode.ALPHABET_SIZE);
    }

    private void serializeDfs(TrieNode node, OutputStream out) throws IOException {
        out.write((byte) (node.isTerminal ? 1 : 0));
        for (int i = 0; i < 2 * TrieNode.ALPHABET_SIZE; i++) {
            char c = getChar(i);
            TrieNode sonNode = node.getChild(c);
            if (sonNode != null) {
                out.write(c);
                serializeDfs(sonNode, out);
                out.write(GO_UP);
            }
        }
    }

    public void serialize(OutputStream out) {
        try {
            serializeDfs(root, out);
        }
        catch (IOException exception) {
            throw new SerializationException();
        }
    }

    private void deserializeDfs(TrieNode node, InputStream in) throws IOException {
        node.isTerminal = in.read() != 0;
        node.terminalDescendantNumber = (int) (node.isTerminal ? 1 : 0);
        while (true) {
            int c = in.read();
            if (c == GO_UP || c == -1) {
                return;
            }
            node.addChild((char) c);
            deserializeDfs(node.getChild((char) c), in);
            node.terminalDescendantNumber += node.getChild((char) c).terminalDescendantNumber;
        }
    }

    public void deserialize(InputStream in) {
        try {
            root = new TrieNode();
            deserializeDfs(root, in);
            size = root.terminalDescendantNumber;
        }
        catch (IOException exception) {
            throw new SerializationException();
        }
    }
}

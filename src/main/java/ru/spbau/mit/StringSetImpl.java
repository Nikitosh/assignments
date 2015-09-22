package ru.spbau.mit;

import com.sun.org.apache.xpath.internal.SourceTree;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.util.LinkedList;
import java.util.Queue;

public class StringSetImpl implements StringSet, StreamSerializable {
    private TrieNode root;
    private int size;

    StringSetImpl() {
        root = new TrieNode();
        size = 0;
    }

    private TrieNode getNode(String element) {
        TrieNode currentNode = root;
        for (int i = 0; i < element.length(); i++) {
            TrieNode nextNode = currentNode.getChild(element.charAt(i));
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
        for (int i = 0; i < element.length(); i++) {
            currentNode.terminalDescendantNumber++;
            if (currentNode.getChild(element.charAt(i)) == null) {
                currentNode.addChild(element.charAt(i));
            }
            currentNode = currentNode.getChild(element.charAt(i));
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

    private void serializeDfs(TrieNode node, OutputStream out) throws IOException {
        out.write((byte) (node.isTerminal ? 1 : 0));
        for (int i = 0; i < 2 * TrieNode.ALPHABET_SIZE; i++) {
            char c = (char) ('a' + i);
            if (i >= TrieNode.ALPHABET_SIZE) {
                c = (char) ('A' + i - TrieNode.ALPHABET_SIZE);
            }
            TrieNode sonNode = node.getChild(c);
            if (sonNode != null) {
                out.write(c);
                serializeDfs(sonNode, out);
                out.write(0);
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
            if (c <= 0) {
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

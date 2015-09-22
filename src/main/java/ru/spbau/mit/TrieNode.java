package ru.spbau.mit;

import java.io.OutputStream;
import java.io.PrintWriter;
import java.util.HashMap;

public class TrieNode {
    public static final int ALPHABET_SIZE = 26;

    private TrieNode[] children;
    public boolean isTerminal;
    public int terminalDescendantNumber;
    public TrieNode parent;

    public TrieNode() {
        children = new TrieNode[2 * ALPHABET_SIZE];
        isTerminal = false;
        terminalDescendantNumber = 0;
        parent = null;
    }

    public TrieNode(TrieNode parent) {
        this();
        this.parent = parent;
    }

    private int getCode(char c) {
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


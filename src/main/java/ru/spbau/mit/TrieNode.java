package ru.spbau.mit;

import java.io.OutputStream;
import java.io.PrintWriter;
import java.util.HashMap;

public class TrieNode {
    public static final int ALPHABET_SIZE = 26;

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

    public boolean getIsTerminal() {
        return isTerminal;
    }

    public void setIsTerminal(boolean isTerminalValue) {
        isTerminal = isTerminalValue;
    }

    public int getTerminalDescendantNumber() {
        return terminalDescendantNumber;
    }

    public void setTerminalDescendantNumber(int terminalDescendantNumberValue) {
        terminalDescendantNumber = terminalDescendantNumberValue;
    }

    public void addToTerminalDescendantNumber(int delta) {
        terminalDescendantNumber += delta;
    }

    public TrieNode getParent() {
        return parent;
    }

    public void setParent(TrieNode parentValue) {
        parent = parentValue;
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


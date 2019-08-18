package com.example.cynosure_10;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Trie {

    private final Map<Character, Trie> children;
    private String value;
    private boolean terminal = false;

    public Trie() {
        this(null);
    }

    private Trie(String value) {
        this.value = value;
        children = new HashMap<Character, Trie>();
    }

    private void add(char c) {
        String val;
        if (this.value == null) {
            val = Character.toString(c);
        } else {
            val = this.value + c;
        }
        children.put(c, new Trie(val));
    }

    public void insert(String word) {
        if (word == null) {
            throw new IllegalArgumentException("Cannot add null to a Trie");
        }
        Trie node = this;
        for (char c : word.toCharArray()) {
            if (!node.children.containsKey(c)) {
                node.add(c);
            }
            node = node.children.get(c);
        }
        node.terminal = true;
    }

    public String find(String word) {
        Trie node = this;
        for (char c : word.toCharArray()) {
            if (!node.children.containsKey(c)) {
                return "";
            }
            node = node.children.get(c);
        }
        return node.value;
    }

    public Collection<String> autoComplete(String prefix) {
        Trie node = this;
        for (char c : prefix.toCharArray()) {
            if (!node.children.containsKey(c)) {
                return Collections.emptyList();
            }
            node = node.children.get(c);
        }
        return node.allPrefixes();
    }

    private Collection<String> allPrefixes() {
        List<String> results = new ArrayList<String>();
        if (this.terminal) {
            results.add(this.value);
        }
        for (Map.Entry<Character, Trie> entry : children.entrySet()) {
            Trie child = entry.getValue();
            Collection<String> childPrefixes = child.allPrefixes();
            results.addAll(childPrefixes);
        }
        return results;
    }
}

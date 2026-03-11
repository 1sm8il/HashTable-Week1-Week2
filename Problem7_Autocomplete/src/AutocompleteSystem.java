import java.util.HashMap;
import java.util.ArrayList;
import java.util.List;
import java.util.PriorityQueue;
import java.util.Collections;

class TrieNode {
    HashMap<Character, TrieNode> children;
    boolean isEndOfWord;
    int frequency;

    public TrieNode() {
        this.children = new HashMap<>();
        this.isEndOfWord = false;
        this.frequency = 0;
    }
}

public class AutocompleteSystem {
    private TrieNode root;
    private HashMap<String, Integer> queryFrequency;

    public AutocompleteSystem() {
        this.root = new TrieNode();
        this.queryFrequency = new HashMap<>();
    }

    public void addQuery(String query, int frequency) {
        queryFrequency.put(query, frequency);
        insertIntoTrie(query, frequency);
    }

    private void insertIntoTrie(String query, int frequency) {
        TrieNode node = root;
        for (char c : query.toCharArray()) {
            node.children.putIfAbsent(c, new TrieNode());
            node = node.children.get(c);
        }
        node.isEndOfWord = true;
        node.frequency = frequency;
    }

    public List<String> search(String prefix) {
        TrieNode node = root;

        // Navigate to the node representing the prefix
        for (char c : prefix.toCharArray()) {
            if (!node.children.containsKey(c)) {
                return new ArrayList<>();
            }
            node = node.children.get(c);
        }

        // Find all words with this prefix
        List<WordFreq> results = new ArrayList<>();
        findAllWords(node, prefix, results);

        // Sort by frequency and return top 10
        Collections.sort(results, (a, b) -> b.frequency - a.frequency);

        List<String> suggestions = new ArrayList<>();
        for (int i = 0; i < Math.min(10, results.size()); i++) {
            suggestions.add(results.get(i).word);
        }

        return suggestions;
    }

    private void findAllWords(TrieNode node, String prefix, List<WordFreq> results) {
        if (node.isEndOfWord) {
            results.add(new WordFreq(prefix, node.frequency));
        }

        for (char c : node.children.keySet()) {
            findAllWords(node.children.get(c), prefix + c, results);
        }
    }

    public void updateFrequency(String query) {
        int newFreq = queryFrequency.getOrDefault(query, 0) + 1;
        queryFrequency.put(query, newFreq);
        insertIntoTrie(query, newFreq);
        System.out.println("Updated frequency for \"" + query + "\": " + (newFreq - 1) + " → " + newFreq);
    }

    private class WordFreq {
        String word;
        int frequency;

        public WordFreq(String word, int frequency) {
            this.word = word;
            this.frequency = frequency;
        }
    }

    public static void main(String[] args) {
        AutocompleteSystem autocomplete = new AutocompleteSystem();

        // Add popular search queries
        autocomplete.addQuery("java tutorial", 1234567);
        autocomplete.addQuery("javascript", 987654);
        autocomplete.addQuery("java download", 456789);
        autocomplete.addQuery("java jobs", 345678);
        autocomplete.addQuery("java interview questions", 234567);
        autocomplete.addQuery("java projects", 123456);
        autocomplete.addQuery("java vs javascript", 111111);
        autocomplete.addQuery("java spring boot", 99999);
        autocomplete.addQuery("java programming", 88888);
        autocomplete.addQuery("java certification", 77777);

        // Search with prefix
        System.out.println("Search suggestions for \"jav\":");
        List<String> suggestions = autocomplete.search("jav");
        for (int i = 0; i < suggestions.size(); i++) {
            System.out.println((i + 1) + ". \"" + suggestions.get(i) + "\"");
        }

        System.out.println("\nSearch suggestions for \"java \":");
        suggestions = autocomplete.search("java ");
        for (int i = 0; i < suggestions.size(); i++) {
            System.out.println((i + 1) + ". \"" + suggestions.get(i) + "\"");
        }

        // Update frequency
        System.out.println("\nUpdating frequencies:");
        autocomplete.updateFrequency("java tutorial");
        autocomplete.updateFrequency("java tutorial");
        autocomplete.updateFrequency("java tutorial");
    }
}
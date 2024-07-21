import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.*;

class TrieNode {
    Map<Character, TrieNode> children;
    boolean isEndOfWord;

    public TrieNode() {
        children = new HashMap<>();
        isEndOfWord = false;
    }
}

class Trie {
    private final TrieNode root;

    public Trie() {
        root = new TrieNode();
    }

    public void insert(String word) {
        TrieNode node = root;
        for (char c : word.toCharArray()) {
            node = node.children.computeIfAbsent(c, k -> new TrieNode());
        }
        node.isEndOfWord = true;
    }

    public boolean search(String word) {
        TrieNode node = root;
        for (char c : word.toCharArray()) {
            node = node.children.get(c);
            if (node == null) {
                return false;
            }
        }
        return node.isEndOfWord;
    }

    public List<String> startsWith(String prefix) {
        TrieNode node = root;
        for (char c : prefix.toCharArray()) {
            node = node.children.get(c);
            if (node == null) {
                return Collections.emptyList();
            }
        }
        List<String> results = new ArrayList<>();
        findAllWords(node, new StringBuilder(prefix), results);
        return results;
    }

    private void findAllWords(TrieNode node, StringBuilder prefix, List<String> results) {
        if (node.isEndOfWord) {
            results.add(prefix.toString());
        }
        for (Map.Entry<Character, TrieNode> entry : node.children.entrySet()) {
            prefix.append(entry.getKey());
            findAllWords(entry.getValue(), prefix, results);
            prefix.deleteCharAt(prefix.length() - 1);
        }
    }
}

public class SpellChecker {
    private Trie dictionary;

    public SpellChecker() {
        dictionary = new Trie();
    }

    public void loadDictionary(List<String> dictionaryFiles) {
        for (String dictionaryFile : dictionaryFiles) {
            try (BufferedReader br = new BufferedReader(new FileReader(dictionaryFile))) {
                String word;
                while ((word = br.readLine()) != null) {
                    dictionary.insert(word.trim().toLowerCase());
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public List<String> getSuggestions(String input) {
        List<String> suggestions = dictionary.startsWith(input.toLowerCase());
        if (suggestions.isEmpty()) {
            return getClosestWords(input);
        }
        return suggestions;
    }

    public String checkSpelling(String input, Scanner scanner) {
        if (dictionary.search(input.toLowerCase())) {
            return input;
        }

        List<String> suggestions = getSuggestions(input);
        while (true) {
            if (suggestions.isEmpty()) {
                System.out.println("No close matches found.");
                System.out.println("Please enter the correct word:");
                input = scanner.nextLine().trim();
                if (dictionary.search(input.toLowerCase())) {
                    return input;
                }
                suggestions = getSuggestions(input);
            } else {
                System.out.println("Did you mean (type the number corresponding to your choice):");
                for (int i = 0; i < suggestions.size(); i++) {
                    System.out.println((i + 1) + ". " + suggestions.get(i));
                }
                System.out.println((suggestions.size() + 1) + ". None of the above");
                System.out.print("Please enter the number: ");

                String choiceStr = scanner.nextLine().trim();
                try {
                    int choice = Integer.parseInt(choiceStr);
                    if (choice > 0 && choice <= suggestions.size()) {
                        return suggestions.get(choice - 1);
                    } else if (choice == suggestions.size() + 1) {
                        System.out.println("Please enter the correct word:");
                        input = scanner.nextLine().trim();
                        if (dictionary.search(input.toLowerCase())) {
                            return input;
                        }
                        suggestions = getSuggestions(input);
                    } else {
                        System.out.println("Invalid choice. Please enter a number between 1 and " + (suggestions.size() + 1) + ".");
                    }
                } catch (NumberFormatException e) {
                    System.out.println("Invalid input. Please enter a number between 1 and " + (suggestions.size() + 1) + ".");
                }
            }
        }
    }

    private List<String> getClosestWords(String input) {
        List<String> closestWords = new ArrayList<>();
        int maxDistance = 3; // Maximum edit distance allowed
        for (String word : getAllWords()) {
            int distance = editDistance(input, word);
            if (distance <= maxDistance) {
                closestWords.add(word);
            }
        }
        return closestWords;
    }

    private List<String> getAllWords() {
        return dictionary.startsWith(""); // Get all words in the dictionary
    }

    private int editDistance(String a, String b) {
        int[][] dp = new int[a.length() + 1][b.length() + 1];
        for (int i = 0; i <= a.length(); i++) {
            for (int j = 0; j <= b.length(); j++) {
                if (i == 0) {
                    dp[i][j] = j;
                } else if (j == 0) {
                    dp[i][j] = i;
                } else {
                    dp[i][j] = Math.min(dp[i - 1][j - 1] + (a.charAt(i - 1) == b.charAt(j - 1) ? 0 : 1),
                            Math.min(dp[i - 1][j] + 1, dp[i][j - 1] + 1));
                }
            }
        }
        return dp[a.length()][b.length()];
    }
}

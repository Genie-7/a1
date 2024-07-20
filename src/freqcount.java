import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.*;

public class freqcount {

    // Method to parse the CSV file and update word frequencies in the map
    public static void parseCSV(String filePath, Map<String, Integer> wordCountMap) {
        try (BufferedReader br = new BufferedReader(new FileReader(filePath))) {
            String line;
            br.readLine(); // Skip the header line
            while ((line = br.readLine()) != null) {
                String[] columns = line.split(",");
                if (columns.length > 5) {
                    // Combine columns and split into words
                    String[] words = (columns[1] + " " + columns[2]).split("\\W+");
                    for (String word : words) {
                        word = word.toLowerCase(); // Normalize to lowercase
                        if (!word.isEmpty() && !isNumeric(word)) {
                            // Update word count
                            wordCountMap.put(word, wordCountMap.getOrDefault(word, 0) + 1);
                        }
                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace(); // Handle IO exception
        }
    }

    // Method to sort words by their frequency
    public static List<Map.Entry<String, Integer>> sortWordsByFrequency(Map<String, Integer> wordCountMap) {
        List<Map.Entry<String, Integer>> wordList = new ArrayList<>(wordCountMap.entrySet());
        Collections.sort(wordList, (e1, e2) -> e2.getValue().compareTo(e1.getValue()));
        return wordList;
    }

    // Method to display the top N most frequent words
    public static void displayTopNWords(List<Map.Entry<String, Integer>> sortedWords, int N) {
        System.out.println("Top " + N + " most frequent words:");
        for (int i = 0; i < N && i < sortedWords.size(); i++) {
            Map.Entry<String, Integer> entry = sortedWords.get(i);
            System.out.println(entry.getKey() + ": " + entry.getValue());
        }
    }

    // Helper method to check if a string is numeric
    private static boolean isNumeric(String str) {
        try {
            Double.parseDouble(str);
            return true;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    // Main method for testing purposes
    public static void main(String[] args) {
        Map<String, Integer> wordCountMap = new HashMap<>();
        parseCSV("remax_listings.csv", wordCountMap);
        List<Map.Entry<String, Integer>> sortedWords = sortWordsByFrequency(wordCountMap);
        displayTopNWords(sortedWords, 10);
    }
}

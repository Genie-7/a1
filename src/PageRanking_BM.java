import java.io.FileReader; 	// Used to read data from a CSV file
import java.io.IOException; // Used to catch potential exceptions during file reading
import java.util.*; 		// Imports various utility classes

import org.apache.commons.csv.CSVFormat; // Used to define the format of the CSV file
import org.apache.commons.csv.CSVParser; // Used to parse the CSV file data
import org.apache.commons.csv.CSVRecord; // Represents a single record (row) in the CSV file

public class PageRanking_BM {
    // Stores keyword frequencies for each property
    private Map<String, String> property_KeywordFrequencyMap = new HashMap<>();

    // Stores properties in a heap, ordered by their calculated rank
    private PriorityQueue<Map.Entry<String, Integer>> property_MaxHeap = new PriorityQueue<>(
            Comparator.comparingInt((Map.Entry<String, Integer> entry) -> entry.getValue()).reversed());

    // Path to the CSV file containing property data
    private String propertydata_CSVFilePath;

    // Constructor to set up the PageRanking object
    public PageRanking_BM(String propertydata_CSVFilePath, String[] search_Keywords) {
        this.propertydata_CSVFilePath = propertydata_CSVFilePath;
        parse_PropertyDataCSV(this.propertydata_CSVFilePath);
        calculate_PropertyRanks(search_Keywords);
    }

    // Reads property data (URLs, addresses, details) from the CSV file and builds the keyword frequency map
    private void parse_PropertyDataCSV(String propertydata_CSVFilePath) {
        // Use FileReader and CSVParser to read and parse the CSV file
        try (FileReader file_reader = new FileReader(propertydata_CSVFilePath);
             CSVParser csv_parser = new CSVParser(file_reader, CSVFormat.DEFAULT.withFirstRecordAsHeader())) {

            // Loop through each row in the CSV file
            for (CSVRecord csv_record : csv_parser) {
                // Get the URL for the property.
                String property_URL = csv_record.get("URL");

                // Get the property address and details and change them to lowercase.
                String property_Content = csv_record.get("Address").toLowerCase() + " " +
                        (csv_record.size() > 4 ? csv_record.get("Details").toLowerCase() : "");
                // Store the content in the map with initial frequency 0
                property_KeywordFrequencyMap.put(property_URL, property_Content);
            }
        } catch (IOException ex) {
            ex.printStackTrace(); // If there is an IOException, print the stack trace
        }
    }

    // Method for determining a property's page rank using search keywords entered by the user
    private void calculate_PropertyRanks(String[] search_Keywords) {
        // Iterate through each property in the map
        for (String property_URL : property_KeywordFrequencyMap.keySet()) {
            // Get the current property's content
            String property_Content = property_KeywordFrequencyMap.get(property_URL);
            int property_rank = 0; // Set the current property's rank initially

            // Iterate through each search keyword
            for (String search_Keyword : search_Keywords) {
                property_rank += countOccurrences(property_Content, search_Keyword); // Increment rank by keyword occurrences
            }

            // Construct the property URL and add it to the heap of rankings
            property_MaxHeap.add(new AbstractMap.SimpleEntry<>(property_URL, property_rank));
        }
    }

    // Outputs the first N properties with the highest rankings
    public void print_TopRankedProperties(int num) {
        System.out.println("Highest Ranked Properties:");
        int count = 0; // keeps track of how many properties are printed
        // Continue until either all of the properties are printed or the heap is empty
        while (!property_MaxHeap.isEmpty() && count < num) {
            Map.Entry<String, Integer> entry = property_MaxHeap.poll(); // Obtain the highest-ranked property
            System.out.println("URL: " + entry.getKey() + ", Rank: " + entry.getValue()); // Print property link and rank
            count++; // Increment counter
        }
    }

    // Method to count occurrences of a keyword in a text using the Boyer-Moore algorithm
    private int countOccurrences(String text, String keyword) {
        int count = 0;
        int m = keyword.length();
        int n = text.length();

        if (m == 0 || n == 0) {
            return count;
        }

        Map<Character, Integer> badChar = new HashMap<>();

        // Fill the actual value of last occurrence of a character
        for (int i = 0; i < m; i++) {
            badChar.put(keyword.charAt(i), i);
        }

        int s = 0; // s is shift of the pattern with respect to text

        while (s <= (n - m)) {
            int j = m - 1;

            // Keep reducing index j of pattern while characters of pattern and text are matching
            while (j >= 0 && keyword.charAt(j) == text.charAt(s + j)) {
                j--;
            }

            // If the pattern is present at current shift, then index j will become -1 after the above loop
            if (j < 0) {
                count++;
                s += (s + m < n) ? m - (badChar.getOrDefault(text.charAt(s + m), -1)) : 1;
            } else {
                s += Math.max(1, j - (badChar.getOrDefault(text.charAt(s + j), -1)));
            }
        }

        return count;
    }
}
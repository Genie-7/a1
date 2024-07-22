import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.*;

public class FrequencyCount {

    // Method to parse the CSV file and update word frequencies in the map
    public static void parseCSV(String filePath, Map<String, Integer> cityWordCountMap, Map<String, List<String[]>> cityListingsMap, Map<String, Integer> provinceWordCountMap, Map<String, List<String[]>> provinceListingsMap) {
        try (BufferedReader br = new BufferedReader(new FileReader(filePath))) {
            String line;
            br.readLine(); // Skip the header line
            while ((line = br.readLine()) != null) {
                String[] columns = parseCSVLine(line);
                if (columns.length >= 5) {
                    String city = columns[2].toLowerCase();
                    String province = columns[3].toLowerCase();

                    cityWordCountMap.put(city, cityWordCountMap.getOrDefault(city, 0) + 1);
                    cityListingsMap.computeIfAbsent(city, k -> new ArrayList<>()).add(columns);

                    provinceWordCountMap.put(province, provinceWordCountMap.getOrDefault(province, 0) + 1);
                    provinceListingsMap.computeIfAbsent(province, k -> new ArrayList<>()).add(columns);
                }
            }
        } catch (IOException e) {
            e.printStackTrace(); // Handle IO exception
        }
    }

    // Boyer-Moore substring search algorithm
    public static int boyerMooreSearch(String pattern, String text) {
        int n = text.length();
        int m = pattern.length();
        if (m == 0) return 0;

        int[] right = new int[256];
        Arrays.fill(right, -1);
        for (int j = 0; j < m; j++) {
            right[pattern.charAt(j)] = j;
        }

        int skip;
        for (int i = 0; i <= n - m; i += skip) {
            skip = 0;
            for (int j = m - 1; j >= 0; j--) {
                if (pattern.charAt(j) != text.charAt(i + j)) {
                    skip = Math.max(1, j - right[text.charAt(i + j)]);
                    break;
                }
            }
            if (skip == 0) return i; // found
        }
        return -1; // not found
    }

    // Method to parse a single CSV line
    private static String[] parseCSVLine(String line) {
        List<String> columns = new ArrayList<>();
        boolean inQuotes = false;
        StringBuilder sb = new StringBuilder();
        for (char ch : line.toCharArray()) {
            switch (ch) {
                case '"':
                    inQuotes = !inQuotes;
                    break;
                case ',':
                    if (inQuotes) {
                        sb.append(ch);
                    } else {
                        columns.add(sb.toString());
                        sb.setLength(0);
                    }
                    break;
                default:
                    sb.append(ch);
                    break;
            }
        }
        columns.add(sb.toString());
        return columns.toArray(new String[0]);
    }

    // Method to display the frequency of a specific word
    /*public static void displayWordFrequency(Map<String, Integer> wordCountMap, String word) {
        word = word.toLowerCase();
        int count = wordCountMap.getOrDefault(word, 0);
        System.out.println("Frequency of '" + word + "': " + count);
    }*/

    // Method to display the listings of a specific word
    public static void displayListings(Map<String, List<String[]>> listingsMap, String word) {
        word = word.toLowerCase();
        List<String[]> listings = listingsMap.getOrDefault(word, new ArrayList<>());

        if (listings.isEmpty()) {
            System.out.println("No listings found for '" + word + "'.");
        } else {
            System.out.println("Listings for '" + word + "':");
            for (String[] columns : listings) {
                System.out.println("Price: " + columns[0]);
                System.out.println("Address: " + columns[1]);
                System.out.println("City: " + columns[2]);
                System.out.println("Province: " + columns[3]);
                System.out.println("Details: " + columns[4]);
                System.out.println("URL: " + columns[5]);
                System.out.println();
            }
        }
    }

    // Method to filter listings by price range using Boyer-Moore algorithm
    public static List<String[]> filterByPrice(String filePath, double minPrice, double maxPrice) {
        List<String[]> filteredListings = new ArrayList<>();
        try (BufferedReader br = new BufferedReader(new FileReader(filePath))) {
            String line;
            br.readLine(); // Skip the header line
            while ((line = br.readLine()) != null) {
                String[] columns = parseCSVLine(line);
                if (columns.length >= 5) {
                    double price = Double.parseDouble(columns[0].replaceAll("[$,]", ""));
                    if (price >= minPrice && price <= maxPrice) {
                        filteredListings.add(columns);
                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace(); // Handle IO exception
        }
        return filteredListings;
    }
}

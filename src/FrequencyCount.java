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
    public static void displayWordFrequency(Map<String, Integer> wordCountMap, String word) {
        word = word.toLowerCase();
        int count = wordCountMap.getOrDefault(word, 0);
        System.out.println("Frequency of '" + word + "': " + count);
    }

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
                System.out.println("Image File: " + columns[6]);
                System.out.println();
            }
        }
    }

    // Method to filter listings by budget
    public static List<String[]> filterListingsByBudget(String filePath, int minBudget, int maxBudget) {
        List<String[]> filteredListings = new ArrayList<>();
        try (BufferedReader br = new BufferedReader(new FileReader(filePath))) {
            String line;
            br.readLine(); // Skip the header line
            while ((line = br.readLine()) != null) {
                String[] columns = parseCSVLine(line);
                if (columns.length >= 1) {
                    int price = parsePrice(columns[0].trim());
                    if (price >= minBudget && price <= maxBudget) {
                        filteredListings.add(columns);
                    }
                }
            }
        } catch (IOException e) {
            System.out.println("\033[1;31mError reading CSV file: " + e.getMessage() + "\033[0m");
        }
        return filteredListings;
    }

    // Method to display the filtered listings
    public static void displayListings(List<String[]> listings) {
        if (listings.isEmpty()) {
            System.out.println("\033[1;31mNo listings found in the specified budget range.\033[0m");
        } else {
            System.out.println("\033[1;32mListings in the specified budget range:\033[0m");
            for (String[] listing : listings) {
                System.out.println("Price: " + listing[0]);
                System.out.println("Address: " + listing[1]);
                System.out.println("City: " + listing[2]);
                System.out.println("Province: " + listing[3]);
                System.out.println("Details: " + listing[4]);
                System.out.println("URL: " + listing[5]);
                System.out.println("Image File: " + listing[6]);
                System.out.println();
            }
        }
    }

    // Method to parse a price string
    private static int parsePrice(String priceStr) throws NumberFormatException {
        String numericValue = priceStr.replaceAll("[^\\d]", "");
        return Integer.parseInt(numericValue);
    }
}
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.*;
import java.util.regex.Pattern;

public class FrequencyCount {
    private static final Pattern DETAILS_PATTERN = Pattern.compile("\\d+ bed \\d+ bath");
    private static final Pattern URL_PATTERN = Pattern.compile("https?://[\\w.-]+(/[-\\w./?%&=]*)?");

    // Method to parse the CSV file and update word frequencies in the map
    public static void parseCSV(String filePath, Map<String, Integer> cityWordCountMap, Map<String, List<String[]>> cityListingsMap, Map<String, Integer> provinceWordCountMap, Map<String, List<String[]>> provinceListingsMap) {
        try (BufferedReader br = new BufferedReader(new FileReader(filePath))) {
            String line;
            br.readLine(); // Skip the header line
            while ((line = br.readLine()) != null) {
                String[] columns = parseCSVLine(line);
                if (columns.length >= 7) {
                    String details = columns[4];
                    String url = columns[5];

                    if (!DETAILS_PATTERN.matcher(details).matches()) {
                        details = "Invalid details format";
                    }

                    if (!URL_PATTERN.matcher(url).matches()) {
                        url = "Invalid URL format";
                    }

                    columns[4] = details;
                    columns[5] = url;

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
                if (columns.length > 6) {
                    System.out.println("Image File: " + columns[6]);
                }
                System.out.println();
            }
        }
    }

    // Method to search for listings using Boyer-Moore algorithm
    public static void searchListings(Map<String, List<String[]>> listingsMap, String word) {
        word = word.toLowerCase();
        BoyerMoore bm = new BoyerMoore(word);

        List<String[]> results = new ArrayList<>();
        for (String key : listingsMap.keySet()) {
            if (bm.search(key) < key.length()) {
                results.addAll(listingsMap.get(key));
            }
        }

        if (results.isEmpty()) {
            System.out.println("No listings found for '" + word + "'.");
        } else {
            System.out.println("Listings for '" + word + "':");
            for (String[] columns : results) {
                System.out.println("Price: " + columns[0]);
                System.out.println("Address: " + columns[1]);
                System.out.println("City: " + columns[2]);
                System.out.println("Province: " + columns[3]);
                System.out.println("Details: " + columns[4]);
                System.out.println("URL: " + columns[5]);
                if (columns.length > 6) {
                    System.out.println("Image File: " + columns[6]);
                }
                System.out.println();
            }
        }
    }
}

import java.io.IOException;
import java.util.*;

public class Main {
    public static void main(String[] args) {
        RemaxWebScraper scraper = new RemaxWebScraper();
        Map<String, Integer> cityWordCountMap = new HashMap<>();
        Map<String, List<String[]>> cityListingsMap = new HashMap<>();
        Map<String, Integer> provinceWordCountMap = new HashMap<>();
        Map<String, List<String[]>> provinceListingsMap = new HashMap<>();

        SearchFrequencyTracker searchTracker = new SearchFrequencyTracker();
        SpellChecker spellChecker = new SpellChecker();

        // Load dictionary.txt into spell checker
        spellChecker.loadDictionary(Arrays.asList("resources/dictionary.txt"));

        Scanner scanner = new Scanner(System.in);
        System.out.println("Select an option:");
        System.out.println("1. Update CSV using web scraper");
        System.out.println("2. Search by province using existing CSV");
        System.out.println("3. Search by city using existing CSV");

        int choice = scanner.nextInt();
        scanner.nextLine(); // Consume newline

        switch (choice) {
            case 1:
                try {
                    scraper.scrape();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                break;
            case 2:
                FrequencyCount.parseCSV("remax_listings.csv", cityWordCountMap, cityListingsMap, provinceWordCountMap, provinceListingsMap);
                System.out.println("Enter the province name:");
                String province = scanner.nextLine().trim();
                String correctedProvince = spellChecker.checkSpelling(province, scanner);
                int provinceFrequency = searchTracker.search(correctedProvince);
                System.out.println(correctedProvince + " has been searched: " + provinceFrequency + " times.");
                FrequencyCount.displayWordFrequency(provinceWordCountMap, correctedProvince);
                FrequencyCount.displayListings(provinceListingsMap, correctedProvince);
                break;
            case 3:
                FrequencyCount.parseCSV("remax_listings.csv", cityWordCountMap, cityListingsMap, provinceWordCountMap, provinceListingsMap);
                System.out.println("Enter the city name:");
                String city = scanner.nextLine().trim();
                String correctedCity = spellChecker.checkSpelling(city, scanner);
                if (correctedCity.equalsIgnoreCase("None of the above")) {
                    System.out.println("Please enter the correct city name:");
                    correctedCity = scanner.nextLine().trim();
                }
                int cityFrequency = searchTracker.search(correctedCity);
                System.out.println(correctedCity + " has been searched: " + cityFrequency + " times.");
                FrequencyCount.displayWordFrequency(cityWordCountMap, correctedCity);
                FrequencyCount.displayListings(cityListingsMap, correctedCity);
                break;
            default:
                System.out.println("Invalid choice. Please enter 1, 2, or 3.");
                break;
        }

        searchTracker.saveFrequenciesToFile();
        scanner.close();
    }
}
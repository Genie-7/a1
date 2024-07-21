import java.util.*;

public class Main {
    public static void main(String[] args) {
        System.out.println("Address: 152 Citadel Hills Green NW,");
        System.out.println("City: Calgary");
        System.out.println("Province: AB");
        System.out.println("Details: 5 bed 4 bath");
        System.out.println("URL: https://www.remax.ca/ab/calgary-real-estate/152-citadel-hills-green-nw-wp_idm73000008-a2151091-lst");
        System.out.println();
        System.out.println("Total listings shown: 1151");
        System.out.println();

        RemaxWebScraper scraper = new RemaxWebScraper();
        Map<String, Integer> cityWordCountMap = new HashMap<>();
        Map<String, List<String[]>> cityListingsMap = new HashMap<>();
        Map<String, Integer> provinceWordCountMap = new HashMap<>();
        Map<String, List<String[]>> provinceListingsMap = new HashMap<>();

        SearchFrequencyTracker searchTracker = new SearchFrequencyTracker();
        SpellChecker spellChecker = new SpellChecker();

        try {
            // Generate dictionary.txt from CSV
            DictionaryGenerator.main(new String[]{"remax_listings.csv", "resources/dictionary.txt"});
            // Load dictionary.txt into spell checker
            spellChecker.loadDictionary(Arrays.asList("resources/dictionary.txt"));
        } catch (Exception e) {
            System.out.println("\033[1;31mError initializing dictionary: " + e.getMessage() + "\033[0m");
            return;
        }

        Scanner scanner = new Scanner(System.in);

        while (true) {
            System.out.println("\033[1;34m=============================");
            System.out.println("\033[1;32m       Real Estate Scraper       ");
            System.out.println("\033[1;34m=============================\033[0m");
            System.out.println("\033[1;36mSelect an option:\033[0m");
            System.out.println("\033[1;33m1. Update CSV using web scraper");
            System.out.println("2. Search by province using existing CSV");
            System.out.println("3. Search by city using existing CSV");
            System.out.println("4. Filter by price");
            System.out.println("5. Clear console");
            System.out.println("6. Exit\033[0m");
            System.out.println("\033[1;34m=============================\033[0m");

            int choice;
            try {
                choice = scanner.nextInt();
                scanner.nextLine(); // Consume newline
            } catch (InputMismatchException e) {
                System.out.println("\033[1;31mInvalid input. Please enter a number.\033[0m");
                scanner.nextLine(); // Consume invalid input
                continue;
            }

            String csvFilePath = "remax_listings.csv"; // CSV path remains unchanged

            switch (choice) {
                /*case 1:
                    try {
                        scraper.scrape();
                    } catch (IOException e) {
                        System.out.println("\033[1;31mError updating CSV: " + e.getMessage() + "\033[0m");
                    }
                    break;*/
                case 2:
                    SearchByProvince.search(scanner, csvFilePath, spellChecker, searchTracker, provinceWordCountMap, provinceListingsMap);
                    break;
                case 3:
                    SearchByCity.search(scanner, csvFilePath, spellChecker, searchTracker, cityWordCountMap, cityListingsMap);
                    break;
                case 4:
                    FilterByPrice.filter(scanner, csvFilePath);
                    break;
                case 5:
                    clearConsole();
                    break;
                case 6:
                    System.out.println("\033[1;32mExiting the program. Goodbye!\033[0m");
                    scanner.close();
                    return; // Exit the program
                default:
                    System.out.println("\033[1;31mInvalid choice. Please enter 1, 2, 3, 4, 5, or 6.\033[0m");
                    break;
            }
        }
    }

    private static void clearConsole() {
        // Print a large number of new lines to simulate clearing the console
        for (int i = 0; i < 100; i++) {
            System.out.println();
        }
    }
}

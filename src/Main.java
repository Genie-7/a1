import java.util.*;
import java.io.*;

public class Main {
    private static final Map<String, String> provinceCodes = new HashMap<>();
    private static Autocomplete autocomplete = new Autocomplete();

    static {
        provinceCodes.put("ON", "Ontario");
        provinceCodes.put("QC", "Quebec");
        provinceCodes.put("NS", "Nova Scotia");
        provinceCodes.put("NB", "New Brunswick");
        provinceCodes.put("MB", "Manitoba");
        provinceCodes.put("BC", "British Columbia");
        provinceCodes.put("PE", "Prince Edward Island");
        provinceCodes.put("SK", "Saskatchewan");
        provinceCodes.put("AB", "Alberta");
        provinceCodes.put("NL", "Newfoundland and Labrador");
        provinceCodes.put("NT", "Northwest Territories");
        provinceCodes.put("YT", "Yukon");
        provinceCodes.put("NU", "Nunavut");
    }

    public static void main(String[] args) {
        RemaxWebScraper scraper = new RemaxWebScraper();
        Map<String, Integer> cityWordCountMap = new HashMap<>();
        Map<String, List<String[]>> cityListingsMap = new HashMap<>();
        Map<String, Integer> provinceWordCountMap = new HashMap<>();
        Map<String, List<String[]>> provinceListingsMap = new HashMap<>();
        autocomplete.buildVocabularyFromRemaxFile("remax_listings.csv"); // Adjust path accordingly

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

        // Print the welcome message once
        printWelcomeMessage();

        while (true) {
            printMenu();
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
                    searchByProvince(scanner, csvFilePath, spellChecker, searchTracker, provinceWordCountMap, provinceListingsMap);
                    break;
                case 3:
                    searchByCity(scanner, csvFilePath, spellChecker, searchTracker, cityWordCountMap, cityListingsMap);
                    break;
                case 4:
                    FilterByPrice.filter(scanner, csvFilePath, "");
                    break;
                case 5:
                    FilterByBedBath.filter(scanner, csvFilePath);
                    break;
                case 6:
                    handleCitySuggestions(scanner, csvFilePath, spellChecker, searchTracker, cityWordCountMap, cityListingsMap);
                    break;
                case 7:
                    try {
                        handlePageRanking(scanner, csvFilePath);
                    } catch (Exception e) {
                        System.out.println("An error occurred during page ranking: " + e.getMessage());
                        e.printStackTrace();
                    }
                    break;
                case 8:
                    System.out.println("\033[1;32mClosing the app. Catch you later, property hunter! \033[0m");
                    scanner.close();
                    return; // Exit the program
                default:
                    System.out.println("\033[1;31mInvalid choice. Please enter a number between 1 and 8.\033[0m");
                    break;
            }
        }
    }

    private static void printWelcomeMessage() {
        System.out.println("\033[1;34m=============================");
        System.out.println("\033[1;32m");
        System.out.println("    ~6_.___,P_,P_,P_,P                                     |");
        System.out.println(" *  |___)  /^\\ /^\\ /^\\ /^\\      *         *   ,     *        \\   /");
        System.out.println("    ='='=    *                      *        )            --  *   --");
        System.out.println("                   *           .-----------. ((              /   \\");
        System.out.println("        *                *     )'''''( ||     *          |");
        System.out.println("                              /''''''\\||                     *");
        System.out.println("    *         *      *       /'''''''\\| *        *");
        System.out.println("                    ,,,,,,, /'''''''''\\      ,");
        System.out.println("       *           .-------.|");
        System.out.println("   *        *     / ,^, ,^, \\|  ,^^,   ,^^,  |  / \\ ((");
        System.out.println("                 /  |_| |_|  \\  |__|   |__|  | /   \\||   *");
        System.out.println("    *       *   /_____________\\ |  |   |  |  |/     \\|           *");
        System.out.println("                 |  __   __  |  '=='   '=='  /.......\\     *");
        System.out.println("  *     *        | (  ) (  ) |  //__\\\\  |,^, ,^,|     _    *");
        System.out.println("   ___,.,___     | |--| |--| |  ||(O)|(O)||  ||_| |_||   _|-|_");
        System.out.println("  (  ((''))  ) *  | |__| |__| |  || \" | \" ||  ||_| |_|| *  (\"')       *");
        System.out.println("   \\_('@')_/     |           |  ||   |   ||  |       |  --(_)--  *");
        System.out.println(" ***/_____\\******'==========='==''==='===''=='======='***(___)*****ldb");
        System.out.println("\033[0m");
        System.out.println("       Real Estate Scraper       ");
        System.out.println("=============================\033[0m");
    }

    private static void printMenu() {
        System.out.println("\033[1;36mSelect an option:\033[0m");
        System.out.println("\033[1;33m1. Update listings with the magic scraper");
        System.out.println("2. Explore properties by Province");
        System.out.println("3. Discover homes by City");
        System.out.println("4. Find your dream property within your Budget");
        System.out.println("5. Search cozy homes by bedrooms and bathrooms");
        System.out.println("6. City Name Autocomplete");
        System.out.println("7. Hunt for properties using keywords");
        System.out.println("8. Exit\033[0m");
        System.out.println("\033[1;34m=============================\033[0m");
    }

    private static void searchByProvince(Scanner scanner, String csvFilePath, SpellChecker spellChecker,
                                         SearchFrequencyTracker searchTracker, Map<String, Integer> provinceWordCountMap,
                                         Map<String, List<String[]>> provinceListingsMap) {
        FrequencyCount.parseCSV(csvFilePath, new HashMap<>(), new HashMap<>(), provinceWordCountMap, provinceListingsMap);
        displayProvinceCodes();
        while (true) {
            System.out.print("\033[1;36mEnter the province code (or type 'back' to return to the main menu, 'exit' to quit):\033[0m ");
            String provinceCode = scanner.nextLine().trim().toUpperCase();
            if (provinceCode.equalsIgnoreCase("back")) {
                return; // Go back to the main menu
            }
            if (provinceCode.equalsIgnoreCase("exit")) {
                System.out.println("\033[1;32mClosing the app. Catch you later, property hunter! \033[0m");
                System.exit(0);
            }
            if (!provinceCodes.containsKey(provinceCode)) {
                System.out.println("\033[1;31mInvalid province code. Please try again.\033[0m");
                displayProvinceCodes();
                continue;
            }
            int provinceFrequency = searchTracker.search(provinceCode);
            System.out.println("\033[1;32m" + provinceCodes.get(provinceCode) + " (" + provinceCode + ") has been searched: " + provinceFrequency + " times.\033[0m");
            FrequencyCount.displayWordFrequency(provinceWordCountMap, provinceCode);

            // Display search results for the province
            List<String[]> listings = provinceListingsMap.get(provinceCode.toLowerCase()); // Ensure case insensitivity
            if (listings != null && !listings.isEmpty()) {
                for (String[] listing : listings) {
                    System.out.println("Price: " + listing[0]);
                    System.out.println("Address: " + listing[1]);
                    System.out.println("City: " + listing[2]);
                    System.out.println("Province: " + listing[3]);
                    System.out.println("Details: " + listing[4]);
                    System.out.println("URL: " + listing[5]);
                    if (listing.length > 6) {
                        System.out.println("Image File: " + listing[6]);
                    }
                    System.out.println();
                }
            } else {
                System.out.println("No listings found for province: " + provinceCodes.get(provinceCode));
            }

            // Ask if user wants to apply the budget filter
            System.out.print("\033[1;36mDo you want to apply a budget filter? (yes/no):\033[0m ");
            String applyFilter = scanner.nextLine().trim().toLowerCase();
            if (applyFilter.equals("yes")) {
                FilterByPrice.filter(scanner, csvFilePath, provinceCode); // Pass the province code
            }
        }
    }




    private static void searchByCity(Scanner scanner, String csvFilePath, SpellChecker spellChecker,
                                     SearchFrequencyTracker searchTracker, Map<String, Integer> cityWordCountMap,
                                     Map<String, List<String[]>> cityListingsMap) {
        FrequencyCount.parseCSV(csvFilePath, cityWordCountMap, cityListingsMap, new HashMap<>(), new HashMap<>());
        while (true) {
            System.out.print("\033[1;36mEnter the city name (or type 'back' to return to the main menu, 'exit' to quit):\033[0m ");
            String city = scanner.nextLine().trim();
            if (city.matches("(?i)back")) {
                return; // Go back to the main menu
            }
            if (city.matches("(?i)exit")) {
                System.out.println("\033[1;32mClosing the app. Catch you later, property hunter! \033[0m");
                System.exit(0);
            }
            String correctedCity = spellChecker.checkSpelling(city, scanner);
            if (correctedCity.equalsIgnoreCase("None of the above")) {
                return; // Go back to the main menu
            }
            int cityFrequency = searchTracker.search(correctedCity);
            System.out.println("\033[1;32m" + correctedCity + " has been searched: " + cityFrequency + " times.\033[0m");
            FrequencyCount.displayWordFrequency(cityWordCountMap, correctedCity);

            // Display search results for the city
            List<String[]> listings = cityListingsMap.get(correctedCity);
            if (listings != null && !listings.isEmpty()) {
                for (String[] listing : listings) {
                    System.out.println("Price: " + listing[0]);
                    System.out.println("Address: " + listing[1]);
                    System.out.println("City: " + listing[2]);
                    System.out.println("Province: " + listing[3]);
                    System.out.println("Details: " + listing[4]);
                    System.out.println("URL: " + listing[5]);
                    if (listing.length > 6) {
                        System.out.println("Image File: " + listing[6]);
                    }
                    System.out.println();
                }
            } else {
                System.out.println("No listings found for city: " + correctedCity);
            }

            // Ask if user wants to apply the budget filter
            System.out.print("\033[1;36mDo you want to apply a budget filter? (yes/no):\033[0m ");
            String applyFilter = scanner.nextLine().trim().toLowerCase();
            if (applyFilter.equals("yes")) {
                FilterByPrice.filter(scanner, csvFilePath, correctedCity); // Pass the corrected city name
            }
        }
    }







    private static void displayProvinceCodes() {
        System.out.println("\033[1;36mAvailable Province Codes:\033[0m");
        for (Map.Entry<String, String> entry : provinceCodes.entrySet()) {
            System.out.println(entry.getKey() + " - " + entry.getValue());
        }
    }

    private static void handleCitySuggestions(Scanner scanner, String csvFilePath, SpellChecker spellChecker,
                                              SearchFrequencyTracker searchTracker, Map<String, Integer> cityWordCountMap,
                                              Map<String, List<String[]>> cityListingsMap) {
        while (true) {
            System.out.println("Enter a prefix for city suggestions or 'back' to return:");
            String input = scanner.nextLine().trim();
            if (input.equalsIgnoreCase("back")) {
                return;  // Return to main menu
            }

            List<String> suggestions = autocomplete.getSuggestions(input);
            if (suggestions.isEmpty()) {
                System.out.println("No suggestions found. Try a different prefix.");
                continue;
            }

            System.out.println("Top suggestions:");
            int count = 1;
            for (String suggestion : suggestions.subList(0, Math.min(suggestions.size(), 5))) {
                System.out.println(count++ + ". " + suggestion.split(" \\(")[0]);
            }

            System.out.println("Enter a number (1-5) to select a city, type a new prefix to get new suggestions, or 'back' to return:");

            while (true) {
                String selection = scanner.nextLine().trim();
                if (selection.equalsIgnoreCase("back")) {
                    return;  // Return to main menu
                }

                try {
                    int selectedIndex = Integer.parseInt(selection) - 1;
                    if (selectedIndex >= 0 && selectedIndex < Math.min(suggestions.size(), 5)) {
                        String selectedCity = suggestions.get(selectedIndex).split(" \\(")[0];
                        System.out.println("You selected: " + selectedCity);
                        searchCityDirectly(scanner, csvFilePath, searchTracker, cityWordCountMap, cityListingsMap, selectedCity); // Pass scanner
                        break;  // Break the inner loop to restart or exit
                    } else {
                        System.out.println("Invalid selection. Please enter a number (1-5), a new prefix, or 'back'.");
                    }
                } catch (NumberFormatException e) {
                    List<String> newSuggestions = autocomplete.getSuggestions(selection);
                    if (!newSuggestions.isEmpty()) {
                        suggestions = newSuggestions;
                        count = 1;
                        System.out.println("New suggestions:");
                        for (String suggestion : newSuggestions.subList(0, Math.min(newSuggestions.size(), 5))) {
                            System.out.println(count++ + ". " + suggestion.split(" \\(")[0]);
                        }
                        System.out.println("Enter a number (1-5) to select a city, type a new prefix, or 'back' to return:");
                    } else {
                        System.out.println("No suggestions found for '" + selection + "'. Try again or 'back' to return.");
                    }
                }
            }
        }
    }


    private static void searchCityDirectly(Scanner scanner, String csvFilePath,
                                           SearchFrequencyTracker searchTracker, Map<String, Integer> cityWordCountMap,
                                           Map<String, List<String[]>> cityListingsMap, String city) {
        FrequencyCount.parseCSV(csvFilePath, cityWordCountMap, cityListingsMap, new HashMap<>(), new HashMap<>());

        int cityFrequency = searchTracker.search(city);
        System.out.println("\033[1;32m" + city + " has been searched: " + cityFrequency + " times.\033[0m");
        FrequencyCount.displayWordFrequency(cityWordCountMap, city);

        // Display search results for the city
        List<String[]> listings = cityListingsMap.get(city);
        if (listings != null && !listings.isEmpty()) {
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
        } else {
            System.out.println("No listings found for city: " + city);
        }

        // Ask if user wants to apply the budget filter
        System.out.print("\033[1;36mDo you want to apply a budget filter? (yes/no):\033[0m ");
        String applyFilter = scanner.nextLine().trim().toLowerCase();
        if (applyFilter.equals("yes")) {
            FilterByPrice.filter(scanner, csvFilePath, city); // Pass the city name
        }
    }




    private static void handlePageRanking(Scanner scanner, String csvFilePath) {
        System.out.println("Enter search keywords (space-separated): ");
        String userInput = scanner.nextLine(); // Read user input
        String[] searchKeywords = userInput.split("\\s+"); // Split the input into an array of keywords

        PageRanking_BM pageRank = new PageRanking_BM(csvFilePath, searchKeywords);
        pageRank.print_TopRankedProperties(10); // Corrected method name
    }
}

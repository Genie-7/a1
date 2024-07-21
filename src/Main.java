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
            System.out.println("\033[1;32m");
            System.out.println("    ~6_.___,`P_,`P_,`P_,`P                                     |");
            System.out.println(" *  |___)  /^\\ /^\\ /^\\ /^\\      *         *   ,     *        \\   /");
            System.out.println("    ='='=`    *                      *        )            --  *   --");
            System.out.println("                   *           .-----------. ((              /   \\");
            System.out.println("        *                *     )`'`'`'`'`'`( ||     *          |");
            System.out.println("                              /`'`'`'`'`'`'`\\||                     *");
            System.out.println("    *         *      *       /`'`'`'`'`'`'`'`\\| *        *");
            System.out.println("                    ,,,,,,, /`'`'`'`'`'`'`'`'`'\\      ,");
            System.out.println("       *           .-------.`|```````````````|`  .   )       *    *");
            System.out.println("   *        *     / ,^, ,^, \\|  ,^^,   ,^^,  |  / \\ ((");
            System.out.println("                 /  |_| |_|  \\  |__|   |__|  | /   \\||   *");
            System.out.println("    *       *   /_____________\\ |  |   |  |  |/     \\|           *");
            System.out.println("                 |  __   __  |  '=='   '=='  /.......\\     *");
            System.out.println("  *     *        | (  ) (  ) |  //`_```_`\\\\  |,^, ,^,|     _    *");
            System.out.println("   ___,.,___     | |--| |--| |  ||(O)|(O)||  ||_| |_||   _|-|_");
            System.out.println("  (  ((''))  ) *  | |__| |__| |  || \" | \" ||  ||_| |_|| *  (\"')       *");
            System.out.println("   \\_('@')_/     |           |  ||   |   ||  |       |  --(_)--  *");
            System.out.println(" ***/_____\\******'==========='==''==='===''=='======='***(___)*****ldb");
            System.out.println("\033[0m");
            System.out.println("       Real Estate Scraper       ");
            System.out.println("=============================\033[0m");
            System.out.println("\033[1;36mSelect an option:\033[0m");
            System.out.println("\033[1;33m1. Update CSV using web scraper");
            System.out.println("2. Search by province using existing CSV");
            System.out.println("3. Search by city using existing CSV");
            System.out.println("4. Exit\033[0m");
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
                    searchByProvince(scanner, csvFilePath, spellChecker, searchTracker, provinceWordCountMap, provinceListingsMap);
                    break;
                case 3:
                    searchByCity(scanner, csvFilePath, spellChecker, searchTracker, cityWordCountMap, cityListingsMap);
                    break;
                case 4:
                    System.out.println("\033[1;32mExiting the program. Goodbye!\033[0m");
                    scanner.close();
                    return; // Exit the program
                default:
                    System.out.println("\033[1;31mInvalid choice. Please enter 1, 2, 3, or 4.\033[0m");
                    break;
            }
        }
    }

    private static void searchByProvince(Scanner scanner, String csvFilePath, SpellChecker spellChecker,
                                         SearchFrequencyTracker searchTracker, Map<String, Integer> provinceWordCountMap,
                                         Map<String, List<String[]>> provinceListingsMap) {
        FrequencyCount.parseCSV(csvFilePath, new HashMap<>(), new HashMap<>(), provinceWordCountMap, provinceListingsMap);
        while (true) {
            System.out.print("\033[1;36mEnter the province name (or type 'back' to return to the main menu, 'exit' to quit):\033[0m ");
            String province = scanner.nextLine().trim();
            if (province.equalsIgnoreCase("back")) {
                return; // Go back to the main menu
            }
            if (province.equalsIgnoreCase("exit")) {
                System.out.println("\033[1;32mExiting the program. Goodbye!\033[0m");
                System.exit(0);
            }
            String correctedProvince = spellChecker.checkSpelling(province, scanner);
            if (correctedProvince.equalsIgnoreCase("None of the above")) {
                return; // Go back to the main menu
            }
            int provinceFrequency = searchTracker.search(correctedProvince);
            System.out.println("\033[1;32m" + correctedProvince + " has been searched: " + provinceFrequency + " times.\033[0m");
            FrequencyCount.displayWordFrequency(provinceWordCountMap, correctedProvince);
            FrequencyCount.displayListings(provinceListingsMap, correctedProvince);
        }
    }

    private static void searchByCity(Scanner scanner, String csvFilePath, SpellChecker spellChecker,
                                     SearchFrequencyTracker searchTracker, Map<String, Integer> cityWordCountMap,
                                     Map<String, List<String[]>> cityListingsMap) {
        FrequencyCount.parseCSV(csvFilePath, cityWordCountMap, cityListingsMap, new HashMap<>(), new HashMap<>());
        while (true) {
            System.out.print("\033[1;36mEnter the city name (or type 'back' to return to the main menu, 'exit' to quit):\033[0m ");
            String city = scanner.nextLine().trim();
            if (city.equalsIgnoreCase("back")) {
                return; // Go back to the main menu
            }
            if (city.equalsIgnoreCase("exit")) {
                System.out.println("\033[1;32mExiting the program. Goodbye!\033[0m");
                System.exit(0);
            }
            String correctedCity = spellChecker.checkSpelling(city, scanner);
            if (correctedCity.equalsIgnoreCase("None of the above")) {
                return; // Go back to the main menu
            }
            int cityFrequency = searchTracker.search(correctedCity);
            System.out.println("\033[1;32m" + correctedCity + " has been searched: " + cityFrequency + " times.\033[0m");
            FrequencyCount.displayWordFrequency(cityWordCountMap, correctedCity);
            FrequencyCount.displayListings(cityListingsMap, correctedCity);
        }
    }
}

import java.util.*;

public class SearchByCity {
    public static void search(Scanner scanner, String csvFilePath, SpellChecker spellChecker,
                              SearchFrequencyTracker searchTracker, Map<String, Integer> cityWordCountMap,
                              Map<String, List<String[]>> cityListingsMap) {
        // Ensure maps are cleared before parsing CSV
        cityWordCountMap.clear();
        cityListingsMap.clear();

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
            List<String[]> listings = cityListingsMap.getOrDefault(correctedCity, new ArrayList<>());
            System.out.println("Total listings shown: " + listings.size());
            FrequencyCount.displayListings(cityListingsMap, correctedCity);
        }
    }
}

import java.util.*;

public class SearchByProvince {
    public static void search(Scanner scanner, String csvFilePath, SpellChecker spellChecker,
                              SearchFrequencyTracker searchTracker, Map<String, Integer> provinceWordCountMap,
                              Map<String, List<String[]>> provinceListingsMap) {
        // Ensure maps are cleared before parsing CSV
        provinceWordCountMap.clear();
        provinceListingsMap.clear();

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
            List<String[]> listings = provinceListingsMap.getOrDefault(correctedProvince, new ArrayList<>());
            System.out.println("Total listings shown: " + listings.size());
            FrequencyCount.displayListings(provinceListingsMap, correctedProvince);
        }
    }
}


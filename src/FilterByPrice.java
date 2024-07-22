import java.util.*;

public class FilterByPrice {
    public static void filter(Scanner scanner, String csvFilePath) {
        System.out.print("\033[1;36mEnter minimum price:\033[0m ");
        double minPrice = scanner.nextDouble();
        System.out.print("\033[1;36mEnter maximum price:\033[0m ");
        double maxPrice = scanner.nextDouble();
        scanner.nextLine(); // Consume newline

        List<String[]> filteredListings = FrequencyCount.filterByPrice(csvFilePath, minPrice, maxPrice);

        System.out.println("Total listings shown: " + filteredListings.size());
        System.out.println("\033[1;32mFiltered listings:\033[0m");
        for (String[] listing : filteredListings) {
            System.out.println("Price: " + listing[0]);
            System.out.println("Address: " + listing[1]);
            System.out.println("City: " + listing[2]);
            System.out.println("Province: " + listing[3]);
            System.out.println("Details: " + listing[4]);
            System.out.println("URL: " + listing[5]);
            System.out.println();
        }
    }
}


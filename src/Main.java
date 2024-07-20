import java.io.IOException;

public class Main {
    public static void main(String[] args) {
        RemaxWebScraper scraper = new RemaxWebScraper();
        try {
            scraper.scrape();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
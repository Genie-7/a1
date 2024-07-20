import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;
import org.apache.commons.io.FileUtils;
import org.openqa.selenium.*;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.net.URL;
import java.time.Duration;
import java.util.List;

public class RemaxWebScraper {

    private String[] cities = {"Windsor", "Toronto", "Edmonton"};
    private String outputDir = "city_listings";

    public RemaxWebScraper() {
        // Create the output directory if it doesn't exist
        File dir = new File(outputDir);
        if (!dir.exists()) {
            dir.mkdirs();
        }
    }

    public void scrape(String cityName) throws IOException {
        System.setProperty("webdriver.chrome.driver", "/opt/homebrew/bin/chromedriver");
        WebDriver driver = new ChromeDriver();
        try {
            driver.get("https://www.remax.ca/");

            WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(30));

            System.out.println("Page title is : " + driver.getTitle());

            WebElement searchBox = wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("home-search-input")));
            searchBox.sendKeys(cityName);

            WebElement firstSuggestion;
            try {
                firstSuggestion = wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("home-search-item-1")));
            } catch (Exception e) {
                System.out.println("Retrying search box interaction...");
                searchBox.clear();
                searchBox.sendKeys(cityName);
                firstSuggestion = wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("home-search-item-1")));
            }

            firstSuggestion.click();

            wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector(".search-gallery_galleryContainer__k32f5")));

            // Save the CSV file in the output directory
            FileWriter csvWriter = new FileWriter(outputDir + "/" + cityName.toLowerCase() + "_remax_listings.csv");
            try (CSVPrinter printer = new CSVPrinter(csvWriter, CSVFormat.DEFAULT.withHeader("Price", "Address", "Details", "URL", "Image File"))) {

                boolean hasNextPage = true;
                int pageCount = 0;
                while (hasNextPage && pageCount < 10) {
                    wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector(".search-gallery_galleryContainer__k32f5")));
                    List<WebElement> propertyListings = driver.findElements(By.cssSelector(".listing-card_root__RBrXm"));

                    System.out.println(propertyListings.size());

                    for (int i = 0; i < propertyListings.size(); i++) {
                        WebElement property = propertyListings.get(i);
                        boolean successful = false;
                        for (int attempts = 0; attempts < 3; attempts++) {
                            try {
                                property = driver.findElements(By.cssSelector(".listing-card_root__RBrXm")).get(i);

                                String price = property.findElement(By.cssSelector(".listing-card_price__lEBmo")).getText();
                                String address = property.findElement(By.cssSelector(".listing-address_root__g9lT5")).getText();
                                String[] addressParts = address.split(", ");
                                String city = addressParts.length > 1 ? addressParts[addressParts.length - 2] : "";
                                String province = addressParts.length > 1 ? addressParts[addressParts.length - 1] : "";
                                WebElement detailsElement = property.findElement(By.cssSelector(".listing-card_detailsRow__t1YUs"));
                                String beds = detailsElement.findElement(By.cssSelector("[data-cy='property-beds']")).getText();
                                String baths = detailsElement.findElement(By.cssSelector("[data-cy='property-baths']")).getText();
                                String details = beds + " " + baths;
                                String url = property.findElement(By.cssSelector(".listing-card_listingCard__lc4CL")).getAttribute("href");

                                String imageUrl = null;
                                try {
                                    WebElement imageElement = wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector("img.lazyloaded, img.lazyload, img.image_blurUp__uxKUD")));
                                    imageUrl = imageElement.getAttribute("src");

                                    if (imageUrl == null || imageUrl.isEmpty()) {
                                        WebElement finalImageElement = imageElement;
                                        wait.until(driver1 -> {
                                            WebElement tempImageElement = driver1.findElement(By.cssSelector("img.lazyloaded, img.lazyload, img.image_blurUp__uxKUD"));
                                            return tempImageElement.getAttribute("src") != null && !tempImageElement.getAttribute("src").isEmpty();
                                        });
                                        imageUrl = finalImageElement.getAttribute("src");
                                    }
                                } catch (Exception e) {
                                    System.out.println("No image found for this listing.");
                                }

                                String imageFileName = "No Image Available";
                                if (imageUrl != null && !imageUrl.isEmpty()) {
                                    imageFileName = address.replaceAll("[^a-zA-Z0-9]", "_") + ".jpg";
                                    FileUtils.copyURLToFile(new URL(imageUrl), new File("images/" + imageFileName));
                                }
                                printer.printRecord(price, address, details, url, "images/" + imageFileName);
                                successful = true;
                                break;
                            } catch (StaleElementReferenceException e) {
                                System.out.println("StaleElementReferenceException caught. Retrying...");
                                wait.until(ExpectedConditions.stalenessOf(property));
                            }
                        }
                        if (!successful) {
                            System.out.println("Failed to process element after multiple attempts.");
                        }
                    }

                    try {
                        WebElement nextButton = driver.findElement(By.cssSelector(".page-control_arrowButtonRoot__GNsT1[aria-label='Go to the next page of the gallery.']"));
                        if (nextButton.isEnabled() && !nextButton.getAttribute("class").contains("Mui-disabled")) {
                            ((JavascriptExecutor) driver).executeScript("arguments[0].scrollIntoView(true);", nextButton);
                            nextButton.click();
                            wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector(".search-gallery_galleryContainer__k32f5")));
                            pageCount++;
                        } else {
                            hasNextPage = false;
                        }
                    } catch (Exception e) {
                        hasNextPage = false;
                    }
                }
            }

            System.out.println("Data extraction complete. Check " + outputDir + "/" + cityName.toLowerCase() + "_remax_listings.csv for results.");
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            driver.quit();
        }
    }

    public void scrapeMultipleCities() {
        for (String city : cities) {
            try {
                scrape(city);
            } catch (IOException e) {
                System.out.println("Failed to scrape data for city: " + city);
                e.printStackTrace();
            }
        }
    }
}

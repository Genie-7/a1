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

    public void scrape() throws IOException {
        // telling system where to find chromedriver
        System.setProperty("webdriver.chrome.driver", "/Users/sushant-sharma/Downloads/chromedriver-mac-arm64/chromedriver");
        // creating an instance of chromedriver
        WebDriver driver = new ChromeDriver();
        try {
            // navigate to remax Canada website
            driver.get("https://www.remax.ca/");

            WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(20));

            // let the user see the website title
            System.out.println("Page title is : " + driver.getTitle());

            // find the search box and enter "Windsor"
            WebElement searchBox = driver.findElement(By.id("home-search-input"));
            searchBox.sendKeys("Windsor");

            WebElement firstSuggestion;

            try {
                // waiting for the first autocomplete result
                firstSuggestion = wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("home-search-item-1")));
            } catch (Exception e) {
                // Retry interaction if it fails
                System.out.println("Retrying search box interaction...");
                searchBox.clear();
                searchBox.sendKeys("Windsor");
                firstSuggestion = wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("home-search-item-1")));
            }

            // Select the first suggestion in the autocomplete list
            firstSuggestion.click();

            wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector(".search-gallery_galleryContainer__k32f5")));

            // creating a CSV file remax_listings.csv to store the results
            FileWriter csvWriter = new FileWriter("remax_listings.csv");
            // wrapping property listing data processing in a try catch for error handling
            try (CSVPrinter printer = new CSVPrinter(csvWriter, CSVFormat.DEFAULT
                    .withHeader("Price", "Address", "City", "Province", "Details", "URL", "Image File"))) {

                boolean hasNextPage = true;
                int pageCount = 0;
                while (hasNextPage && pageCount < 10) {  // Limit to first 10 pages
                    // waiting for the property listings preview grid to load
                    wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector(".search-gallery_galleryContainer__k32f5")));
                    // adding each element in the property listing preview grid to a list
                    List<WebElement> propertyListings = driver.findElements(By.cssSelector(".listing-card_root__RBrXm"));

                    // displaying num of property list preview elements for validation
                    System.out.println("Number of property listings found: " + propertyListings.size());

                    // using for loop to iterate through each property listing element in the list
                    for (int i = 0; i < propertyListings.size(); i++) {
                        WebElement property = propertyListings.get(i);

                        // retry mechanism for stale elements
                        boolean successful = false;
                        for (int attempts = 0; attempts < 3; attempts++) {
                            try {
                                // re-locate the property element
                                property = driver.findElements(By.cssSelector(".listing-card_root__RBrXm")).get(i);

                                // extracting all relevant property information
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

                                // getting image url
                                String imageUrl = null;
                                try {
                                    // broad image selector to capture all variations
                                    WebElement imageElement = wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector("img.lazyloaded, img.lazyload, img.image_blurUp__uxKUD")));
                                    imageUrl = imageElement.getAttribute("src");

                                    // re-check if image URL is empty to allow for capturing of dynamically loaded content
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

                                // define the image file name or set to "No Image Available"
                                String imageFileName = "No Image Available";
                                if (imageUrl != null && !imageUrl.isEmpty()) {
                                    // name the image file accordingly
                                    imageFileName = address.replaceAll("[^a-zA-Z0-9]", "_") + ".jpg";
                                    // Download and save the image
                                    FileUtils.copyURLToFile(new URL(imageUrl), new File("images/" + imageFileName));
                                }
                                // finally print the record to the csv file
                                printer.printRecord(price, address, city, province, details, url, "images/" + imageFileName);
                                successful = true;
                                break; // exit the retry loop on success
                            } catch (StaleElementReferenceException e) {
                                System.out.println("StaleElementReferenceException caught. Retrying...");
                                wait.until(ExpectedConditions.stalenessOf(property));
                            }
                        }
                        if (!successful) {
                            System.out.println("Failed to process element after multiple attempts.");
                        }
                    }

                    // Check if there is a next page
                    try {
                        WebElement nextButton = driver.findElement(By.cssSelector(".page-control_arrowButtonRoot__GNsT1[aria-label='Go to the next page of the gallery.']"));
                        if (nextButton.isEnabled() && !nextButton.getAttribute("class").contains("Mui-disabled")) {
                            // Scroll to the "Next" button to ensure it's visible
                            ((JavascriptExecutor) driver).executeScript("arguments[0].scrollIntoView(true);", nextButton);

                            nextButton.click();

                            // Wait for the next page of listings to load
                            wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector(".search-gallery_galleryContainer__k32f5")));

                            pageCount++;  // Increment the page count
                        } else {
                            hasNextPage = false;
                        }
                    } catch (Exception e) {
                        hasNextPage = false;
                    }
                }
            }

            // completion checkpoint
            System.out.println("Data extraction complete. Check remax_listings.csv for results.");

            //error catching
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            // Quit the application
            driver.quit();
        }
    }

    public static void main(String[] args) throws IOException {
        RemaxWebScraper scraper = new RemaxWebScraper();
        scraper.scrape();
    }
}

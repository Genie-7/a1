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
        System.setProperty("webdriver.chrome.driver", "/opt/homebrew/bin/chromedriver");
        // creating an instance of chromedriver
        WebDriver driver = new ChromeDriver();
        try {
            // navigate to remax Canada website
            driver.get("https://www.remax.ca/");

            WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(20));

            // let the user see the website title
            System.out.println("Page title is : " + driver.getTitle());

            // find the search box and enter "Toronto"
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
                    .withHeader("Price", "Address", "Details", "URL", "Image File"))) {

                boolean hasNextPage = true;
                int pageCount = 0;
                while (hasNextPage && pageCount < 10) {  // Limit to first 10 pages
                    // waiting for the property listings preview grid to load
                    wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector(".search-gallery_galleryContainer__k32f5")));
                    // adding each element in the property listing preview grid to a list
                    List<WebElement> propertyListings = driver.findElements(By.cssSelector(".listing-card_root__RBrXm"));

                    // displaying num of property list preview elements for validation
                    System.out.println(propertyListings.size());

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
                                printer.printRecord(price, address, details, url, "images/" + imageFileName);
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
}
    //*public static void main(String[] args) throws IOException {

/*Task 1

        ✓ Open the website in a web browser using Selenium.
            - Done
        ✓ Find and interact with various elements on the page (e.g., links, buttons, text boxes) using Selenium commands.
            - Location Search: Toronto
              <input aria-invalid="false" autocomplete="off" id="home-search-input" placeholder="Search by location, address,
              or MLS number" type="text" aria-label="Search by location, address, or MLS number" class="MuiInputBase-input
              MuiOutlinedInput-input autocomplete_input___RdYg MuiInputBase-inputMarginDense MuiOutlinedInput-inputMarginDense"
               value="Toronto">
            - First result list element is 0 - Places, next is always the city Ex for Toronto
                <li class="MuiListItem-root autocomplete_item__10qNs autocomplete_itemGrouped__x3bKw MuiListItem-gutters"
                role="option" aria-selected="false" id="home-search-item-1">Toronto, ON, Canada</li>

            - On search, each listing preview is in:

            <div class="search-gallery_galleryContainer__k32f5 search-page-body_galleryContainerGalleryMode__CCZyA">grid

            which contains:

                <div class="listing-card_root__RBrXm search-gallery_galleryCardRoot__dvXhP" data-testid="listing-card">
                <a class="listing-card_listingCard__lc4CL" target="_blank" data-cy="listing-card"
                href="https://www.remax.ca/on/toronto-real-estate/33-blythwood-road-wp_idm73000004-26979848-lst"><div class="listing-card_listingCardImage__ut9Eu">
                <div class="image_lazyImage__kqODu" style="width: 100%; height: 100%;"><img class="lazyloaded image_blurUp__uxKUD"
                src="https://remax-listingphotos-ca5.imgix.net/rets-images-crea-can/eb907f4514aabb3c169ffcf912e822ad33e619fa-1-large.jpeg?fit=max&amp;auto=format,compress&amp;fm=pjpg&amp;cs=srgb&amp;q=35&amp;h=215"
                alt="33 BLYTHWOOD ROAD" sizes="382.22222222222223px" style="object-fit: cover; width: 100%; height: 100%;">
                </div></div><section><div><div class="listing-card_priceRow__FIWr_ residentialDarkColour"><h2 class="listing-card_price__lEBmo">
                <span>$3,395,000</span></h2><button class="MuiButtonBase-root MuiButton-root rootSizeIcon residentialDarkColour listing-card_heartIconOnly___rW8s srp-favorite-listing MuiButton-text remax-button_buttonTextIcon__023u7"
                tabindex="0" type="button" aria-label="Favourite"><span class="MuiButton-label"><svg class="MuiSvgIcon-root srp-favorite-listing"
                focusable="false" viewBox="0 0 24 24" aria-hidden="true"><path d="M16.5 3c-1.74 0-3.41.81-4.5 2.09C10.91 3.81 9.24 3 7.5 3 4.42 3 2 5.42 2 8.5c0 3.78 3.4 6.86 8.55 11.54L12 21.35l1.45-1.32C18.6 15.36 22 12.28 22 8.5 22 5.42 19.58 3 16.5 3zm-4.4 15.55l-.1.1-.1-.1C7.14 14.24 4 11.39 4 8.5 4 6.5 5.5 5 7.5 5c1.54 0 3.04.99 3.57 2.36h1.87C13.46 5.99 14.96 5 16.5 5c2 0 3.5 1.5 3.5 3.5 0 2.89-3.14 5.74-7.9 10.05z">
                </path></svg></span></button></div><div class="listing-card_detailsRow__t1YUs"><div class="property-details_detailsWrapper__6W1XU listing-card_propertyDetailsRoot__SC_jl">
                <div class="property-details_detailsRow__AGJbD"><span class="property-details_detailSpan__aFGW5 listing-card_propertyDetail__se8jH"
                data-cy="property-beds"><span>5</span><span class=""> bed </span></span><span class="property-details_detailSpan__aFGW5 listing-card_propertyDetail__se8jH"
                 data-cy="property-baths"><span>5</span><span class=""> bath </span></span></div></div><div class="listing-address_root__g9lT5 listing-card_address__6GsHt">
                 <div data-cy="property-address"><span class="">33 BLYTHWOOD ROAD, </span><span class="">Toronto, ON</span></div></div></div></div>
                 <div class="listing-card_contentBottom__z1EjO"><div class="listing-card_courtesy__vUQ0s">ROYAL LEPAGE/J &amp; D DIVISION</div>
                 <div class="listing-card_mlsAndTagWrapper__VZcRb"><div class="listing-card_mlsNumber__syXP2" data-cy="property-mls">MLS® #: C8398442</div>
                 <span class="listing-card_tagWrapper__aFKLH"><div class="listing-tag-container_listingTags__2W1eU"><span class="listing-tag_tag__pikaK listing-tag_red___eCwi">
                 New</span></div></span></div></div></section></a></div>

            For each listing preview

        ✓ Extract data from the page using Selenium commands, such as finding and storing text, images, or other content.
        - Done
        ✓ Save the scraped data in a CSV file or other format of your choice.
         - Done
Task 2 - Students need to scrape multiple pages from the same website and combine the results
        - DONE
Task 3 -  Students need to use advanced Selenium commands, such as waiting for elements to load or handling pop-up windows.
        -DONE


         */

        // telling system where to find chromedriver
        //System.setProperty("webdriver.chrome.driver", "C:\\Users\\Brendan\\Documents\\University\\SUMMER2024\\C8547\\Assignments\\chromedriver-win64\\chromedriver.exe");
        /*System.setProperty("webdriver.chrome.driver", "/opt/homebrew/bin/chromedriver");
        /// creating an instance of chromedriver
        WebDriver driver = new ChromeDriver();
        try {
            // navigate to remax Canada website
            driver.get("https://www.remax.ca/");

            WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(20));

            // let the user see the website title
            System.out.println("Page title is : " + driver.getTitle());

            // find the search box and enter "Toronto"
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
                    .withHeader("Price", "Address", "Details", "URL", "Image File"))) {

                boolean hasNextPage = true;
                int pageCount = 0;
                while (hasNextPage && pageCount < 10) {  // Limit to first 10 pages
                    // waiting for the property listings preview grid to load
                    wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector(".search-gallery_galleryContainer__k32f5")));
                    // adding each element in the property listing preview grid to a list
                    List<WebElement> propertyListings = driver.findElements(By.cssSelector(".listing-card_root__RBrXm"));

                    // displaying num of property list preview elements for validation
                    System.out.println(propertyListings.size());

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
                                printer.printRecord(price, address, details, url, "images/" + imageFileName);
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
}*/

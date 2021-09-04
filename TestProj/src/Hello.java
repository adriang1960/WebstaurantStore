import java.util.List;

import org.openqa.selenium.*;
import org.openqa.selenium.chrome.*;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class Hello {

    public static void main(String args[])
    {
        System.setProperty("webdriver.chrome.driver", "C:\\Temp\\Java\\TestProj\\lib\\chromedriver.exe");
        WebDriver driver = new ChromeDriver();
        String url = "https://www.webstaurantstore.com";
        String searchString = "Stainless Work Table";
        String linkString = "Table";

        /*
        **  My Java build skills are weak.  So instead of learning Maven, or something, I 
        **  created a method to emulate a framework. 
        */
        TestSearchPageForSearchTerm_MockFramework(driver, url, searchString, linkString);
    }

    /* 
    ** Test search Page functionality.
    */
    @Test
    private static void TestSearchPageForSearchTerm_MockFramework(WebDriver driver, String url, String searchString, 
        String linkString) {
        
        // Get the user requested page.
        try {
            driver.get(url);
        }
        catch(Exception ex) {
            // Handle exception.
            System.out.println("URL was invalid.");
            return;
        }

        // Definitely have a timing issue.  And this "WebDriverWait" ain't working!  I even tried
        // a driver.wait and zero impact.
        long timeOutInSeconds = 10;
        WebDriverWait wait = new WebDriverWait(driver, timeOutInSeconds);
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector("div[class=page-container]")));
        // WebElement searchCtrl = wait.until(ExpectedConditions.elementToBeClickable(By.id("searchval")));

        // TODO: Timing issue
        // Find the Search Control, enter the specified text, and submit.
        WebElement searchCtrl = driver.findElement(By.id("searchval"));
        Assertions.assertNotNull(searchCtrl);
        searchCtrl.sendKeys(searchString);
        searchCtrl.submit();

        // TODO: Timing issue
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector("div[class=page-container]")));

        // Test if we ended up on the search page.
        String searchPageTitle = driver.getTitle();
        Assertions.assertTrue(searchPageTitle.contains(searchString));

        // Test if the product listings were returned.
        WebElement productListing = driver.findElement(By.id("product_listing"));
        Assertions.assertNotNull(productListing);

        List<WebElement> products = productListing.findElements(By.cssSelector("div[class='ag-item gtm-product ']"));

        for (WebElement product : products) {
            
            // Test if search result details were returned.
            List<WebElement> details = product.findElements(By.id("details"));
            Assertions.assertNotNull(details);

            // Each detail can contain multiple hrefs.  Make sure the keyword is in the link text
            // of one of them.
            for (WebElement detail: details) {
                List<WebElement> hrefs = ((WebElement)detail).findElements(By.tagName("a"));

                Boolean searchTextFound = false;
                
                for (WebElement href: hrefs) {
                    String hrefText = href.getText();
                    
                    if (hrefText.contains(linkString)) {
                            searchTextFound = true;
                            break;
                    }               
                }

                Assertions.assertTrue(searchTextFound);
            }
        }

        // Add the final product to the Cart.  
        WebElement lastProductFound = products.get((products.size()-1));
        if (lastProductFound != null) {
            WebElement addToCartButton = lastProductFound.findElement(By.cssSelector("div[class='add-to-cart']"));
            if (addToCartButton != null) {
                WebElement cartForm = addToCartButton.findElement(By.tagName("form"));
                if (cartForm != null) {
                    //
                    // We need to iterate over the input items available and find the addToCartButton
                    List<WebElement> submitBtns = cartForm.findElements(By.tagName("input"));
                    for (WebElement addToCart : submitBtns) {
                        String controlName = addToCart.getAttribute("name");
                        if (controlName.contains("addToCartButton")) {
                            addToCart.click();
                        }
                    }
                }
            }
        }

        // Clear the Cart
        boolean isCartAvailable = false;
        List<WebElement> productCarts = driver.findElements(By.tagName("a"));
        for (WebElement productCart : productCarts) {
            String tagName = productCart.getAttribute("href");
            if (tagName.contains("viewcart.cfm")) {
                isCartAvailable = true;
                productCart.click();
                break;
            }
        }

        // TODO: Timing issue
        Assertions.assertTrue(isCartAvailable);

        boolean isEmptyCartAvailable = false;
        if (isCartAvailable == true) {
            List<WebElement> emptyCarts = driver.findElements(By.linkText("Empty Cart"));
            for (WebElement emptyCart : emptyCarts) {
                String tagName = emptyCart.getAttribute("href");
                if (tagName.contains("shoppingcart:cart.empty")) {
                    isEmptyCartAvailable = true;
                    emptyCart.click();
                    break;
                }
            }
        }

        // TODO: Timing issue
        Assertions.assertTrue(isEmptyCartAvailable);
        if (isEmptyCartAvailable == true) {
            List<WebElement> modals = driver.findElements(By.cssSelector("div[class='modal-content']"));
            for (WebElement modal : modals) {
                List<WebElement> emptyBtns = modal.findElements(By.tagName("button"));
                for (WebElement emptyBtn : emptyBtns) {
                    String tagText = emptyBtn.getText();
                    if (tagText.contains("Empty Cart")) {
                        emptyBtn.click();
                        break;
                    }
                }
            }
        }
    }
}

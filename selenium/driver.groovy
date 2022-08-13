import org.openqa.selenium.support.ui.WebDriverWait
import org.openqa.selenium.*
import org.openqa.selenium.By;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait


def wait = new WebDriverWait(WDS.browser,5000);

WDS.sampleResult.sampleStart();
WDS.browser.get('http://localhost:4200/');
wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("user-menu")));
WDS.browser.findElement(By.id("user-menu")).click();
WDS.browser.findElement(By.id("logout")).click();
Boolean buttonPresent = WDS.browser.findElement(By.id("addToCart")).isEnabled();
if(buttonPresent) {
    WDS.browser.findElement(By.id("addToCart")).click();
    wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("spinner")));
    wait.until(ExpectedConditions.invisibilityOfElementLocated(By.id("spinner")));
}

WDS.sampleResult.sampleEnd();

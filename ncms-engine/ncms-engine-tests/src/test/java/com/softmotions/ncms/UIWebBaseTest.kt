package com.softmotions.ncms

import org.oneandone.qxwebdriver.QxWebDriver
import org.oneandone.qxwebdriver.interactions.Actions
import org.openqa.selenium.*
import org.openqa.selenium.chrome.ChromeDriver
import org.openqa.selenium.chrome.ChromeOptions
import org.openqa.selenium.support.ui.ExpectedConditions
import org.openqa.selenium.support.ui.WebDriverWait
import org.testng.Assert
import java.util.concurrent.TimeUnit

/**
 * @author Adamansky Anton (adamansky@gmail.com)
 */
open class UIWebBaseTest(db: String) : WebBaseTest(db) {

    protected lateinit var driver: WebDriver

    protected lateinit var driverWait: WebDriverWait

    protected lateinit var actions: Actions

    protected val xpathVersion: Int by lazy {
        try {
            By.xpath("/nobody[@attr=('A'||'')]").findElement(driver)
            return@lazy 3
        } catch (ignored: Exception) {
            try {
                By.xpath("/nobody[@attr=lower-case('A')]").findElement(driver)
                return@lazy 2;
            } catch (ignored2: Exception) {
                return@lazy 1
            }
        }
    }

    protected open fun setupUITest(cfg: String, driverType: String = "chrome") {
        System.setProperty("WEBOOT_CFG_LOCATION", cfg)
        try {
            setupWeb()
            log.info("Starting UI runner")
            runner!!.start()
            log.warn("{}", runner)
        } catch (tr: Throwable) {
            shutdownDb()
            throw tr
        }
        try {
            initDriver(driverType)
        } catch (tr: Throwable) {
            shutdown()
            throw tr
        }
    }

    protected open fun initDriver(driverType: String) {
        log.info("WebDriver initializing...")
        try {
            val options = ChromeOptions()
            options.addArguments("--start-maximized")
            val chromeDriver = ChromeDriver(options)
            if (driverType == "qx") {
                driver = QxWebDriver(chromeDriver)
            } else {
                driver = chromeDriver
            }
            driver.manage().timeouts().implicitlyWait(1, TimeUnit.SECONDS)
            driverWait = WebDriverWait(driver, 10)
            actions = Actions(chromeDriver)
            log.info("WebDriver initialized")
        } catch (tr: Throwable) {
            log.error("", tr)
            throw tr
        }
    }


    override fun shutdown() {
        quitDriver()
        super.shutdown()
    }

    protected fun quitDriver() {
        log.info("Quit WebDriver")
        try {
            driver.quit()
        } catch(e: Throwable) {
            log.error("", e)
        }
    }

    protected fun pauseDriver(seconds: Int) {
        try {
            driverWait.withTimeout(seconds.toLong(), TimeUnit.SECONDS)
                    .until({ false })
        } catch (ignored: TimeoutException) {
        }
    }

    /**
     * Fast search of elements without waiting.
     */
    protected fun isPresent(by: By): Boolean {
        driver.manage().timeouts().implicitlyWait(0, TimeUnit.SECONDS)
        try {
            driver.findElement(by)
            return true
        } catch (e: NoSuchElementException) {
            return false
        } finally {
            driver.manage().timeouts().implicitlyWait(1, TimeUnit.SECONDS)
        }
    }

    protected fun isRequiredAttr(element: WebElement): Boolean {
        return java.lang.Boolean.valueOf(element.getAttribute("required"))!!
    }

    protected fun isEditable(element: WebElement): Boolean {
        try {
            element.clear()
            return true
        } catch (e: Exception) {
            return false
        }
    }

    protected fun waitForPresence(by: By, timeout: Long = 0L) {
        try {
            if (timeout == 0L) {
                driverWait.until(ExpectedConditions.presenceOfElementLocated(by))
            } else {
                WebDriverWait(driver, timeout).until(ExpectedConditions.presenceOfElementLocated(by))
            }
        } catch (e: Exception) {
            Assert.fail("can not find element by locator: " + by)
        }
    }

    protected fun waitForAbsence(by: By, timeout: Long = 0L) {
        try {
            if (timeout == 0L) {
                driverWait.until(ExpectedConditions.invisibilityOfElementLocated(by))
            } else {
                WebDriverWait(driver, timeout).until(ExpectedConditions.invisibilityOfElementLocated(by))
            }
        } catch (e: Exception) {
            Assert.fail("element should not be present in DOM, but was found by locator: " + by)
        }
    }

    protected fun waitForClickable(element: WebElement, timeout: Long = 0L) {
        try {
            if (timeout == 0L) {
                driverWait.until(ExpectedConditions.elementToBeClickable(element))
            } else {
                WebDriverWait(driver, timeout).until(ExpectedConditions.elementToBeClickable(element))
            }
        } catch (e: Exception) {
            Assert.fail("element is not clickable: " + element)
        }
    }

    protected fun waitForVisible(by: By, timeout: Long = 0L) {
        try {
            if (timeout == 0L) {
                driverWait.until(ExpectedConditions.visibilityOfElementLocated(by))
            } else {
                WebDriverWait(driver, timeout).until(ExpectedConditions.visibilityOfElementLocated(by))
            }
        } catch (e: Exception) {
            Assert.fail("element is not visible: " + by)
        }
    }
}
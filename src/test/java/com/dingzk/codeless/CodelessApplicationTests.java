package com.dingzk.codeless;

import io.github.bonigarcia.wdm.WebDriverManager;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
class CodelessApplicationTests {

    @Test
    void contextLoads() {
    }

    @Test
    public void seleniumTest() {
        // 设置国内镜像
        System.setProperty("wdm.geckoDriverMirrorUrl", "https://registry.npmmirror.com/binary.html?path=geckodriver/");
        WebDriverManager.firefoxdriver().setup();
//        FirefoxOptions options = new FirefoxOptions();
        WebDriver webDriver = new FirefoxDriver();
        webDriver.get("https://www.baidu.com");
        Assertions.assertNotNull(webDriver.getTitle(), webDriver.getTitle());
        webDriver.quit();
    }
}

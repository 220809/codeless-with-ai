package com.dingzk.codeless.utils;

import cn.hutool.core.img.ImgUtil;
import cn.hutool.core.io.FileUtil;
import cn.hutool.core.util.StrUtil;
import com.dingzk.codeless.exception.BusinessException;
import com.dingzk.codeless.exception.ErrorCode;
import io.github.bonigarcia.wdm.WebDriverManager;
import jakarta.annotation.PreDestroy;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.RandomStringUtils;
import org.openqa.selenium.*;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.firefox.FirefoxOptions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.io.File;
import java.time.Duration;
import java.util.UUID;

/**
 * 网页截图工具类
 *
 * @author ding
 * @date 2025/12/15 23:05
 */
@Slf4j
public class ScreenshotUtil {

    private static final WebDriver webDriver;

    private static final String SCREENSHOT_DIR = System.getProperty("user.dir") + "/tmp/screenshots";

    static {
        final int SCREEN_WIDTH = 1600, SCREEN_HEIGHT = 900;
        webDriver = initFirefoxDriver(SCREEN_WIDTH, SCREEN_HEIGHT);
    }

    @PreDestroy
    public void destroy() {
        webDriver.quit();
    }

    public static String doScreenshotAndSave(String url) {
        if (StrUtil.isBlank(url)) {
            log.error("截图失败，url为空");
            return null;
        }
        try {
            final String fileSuffix = ".png";
            // 保存截图目录
            String screenshotDirPath = SCREENSHOT_DIR + File.separator + UUID.randomUUID().toString().substring(0, 7);
            FileUtil.mkdir(screenshotDirPath);
            // 原始截图保存路径
            String originalPath =
                    screenshotDirPath + File.separator + RandomStringUtils.insecure().nextAlphanumeric(8) + fileSuffix;
            webDriver.get(url);
            waitPageLoaded();
            // 获取原始截图
            byte[] shotBytes = ((TakesScreenshot) webDriver).getScreenshotAs(OutputType.BYTES);
            // 保存原始截图
            saveScreenshot(shotBytes, originalPath);
            // 压缩截图
            final String compressedSuffix = "_compressed.jpg";
            String compressedPath =
                    screenshotDirPath + File.separator + RandomStringUtils.insecure().nextAlphanumeric(8) + compressedSuffix;
            compressScreenshot(originalPath, compressedPath);
            // 删除原始截图
            FileUtil.del(originalPath);
            log.info("截图成功, 图片保存在: {}", compressedPath);
            return compressedPath;
        } catch (Exception e) {
            log.error("网页截图失败", e);
            return null;
        }
    }

    private static void waitPageLoaded() {
        try {
            WebDriverWait wait = new WebDriverWait(webDriver, Duration.ofSeconds(10));
            wait.until(webDriver -> ((JavascriptExecutor) webDriver).executeScript("return document.readyState").equals("complete"));
            // 额外等待一段时间，确保动态内容加载完成
            Thread.sleep(2000);
            log.info("页面加载完毕");
        } catch (Exception e) {
            log.error("等待页面加载失败", e);
        }
    }

    /**
     * 压缩截图
     * @param originalPath 原始截图路径
     * @param compressedPath 压缩后截图路径
     */
    private static void compressScreenshot(String originalPath, String compressedPath) {
        final float COMPRESS_QUALITY = 0.25f;
        try {
            ImgUtil.compress(new File(originalPath), new File(compressedPath), COMPRESS_QUALITY);
        } catch (Exception e) {
            log.error("压缩图片失败: {}", originalPath, e);
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "压缩图片失败");
        }
    }


    /**
     * 保存截图到文件
     */
    private static void saveScreenshot(byte[] shotBytes, String shotPath) {
        try {
            FileUtil.writeBytes(shotBytes, shotPath);
        } catch (Exception e) {
            log.error("保存图片失败: {}", shotPath, e);
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "保存图片失败");
        }
    }

    /**
     * 初始化 Firefox 浏览器驱动
     */
    private static WebDriver initFirefoxDriver(int width, int height) {
        try {
            System.setProperty("wdm.geckoDriverMirrorUrl", "https://registry.npmmirror.com/binary.html?path=geckodriver/");
            WebDriverManager.firefoxdriver().setup();
            // 配置 Firefox 选项
            FirefoxOptions options = new FirefoxOptions();
            // 无头模式
            options.addArguments("--headless");
            // 禁用GPU（在某些环境下避免问题）
            options.addArguments("--disable-gpu");
            // 禁用沙盒模式（Docker环境需要）
            options.addArguments("--no-sandbox");
            // 禁用开发者shm使用
            options.addArguments("--disable-dev-shm-usage");
            // 设置窗口大小
            options.addArguments(String.format("--window-size=%d,%d", width, height));
            // 禁用扩展
            options.addArguments("--disable-extensions");
            // 设置用户代理
            options.addArguments("--user-agent=Mozilla/5.0 (Windows NT 10.0; Win64; x64; rv:92.0) Gecko/20100101 Firefox/92.0");
            // 创建驱动
            WebDriver driver = new FirefoxDriver(options);
            // 设置页面加载超时
            driver.manage().timeouts().pageLoadTimeout(Duration.ofSeconds(30));
            // 设置隐式等待
            driver.manage().timeouts().implicitlyWait(Duration.ofSeconds(10));
            return driver;
        } catch (Exception e) {
            log.error("初始化 Firefox 浏览器失败", e);
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "初始化 Firefox 浏览器失败");
        }
    }
}

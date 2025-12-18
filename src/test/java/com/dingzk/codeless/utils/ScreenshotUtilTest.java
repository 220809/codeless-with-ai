package com.dingzk.codeless.utils;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ScreenshotUtilTest {

    @Test
    void doScreenshotAndSave() {
        String url = "https://www.bilibili.com";
        String screenshotPath = ScreenshotUtil.doScreenshotAndSave(url);
        assertNotNull(screenshotPath);
    }

    @Test
    void testDoScreenshotAndSave_MultiThread() {
        String url1 = "https://www.bilibili.com";
        String url2 = "https://www.baidu.com";

        new Thread(() -> {
            ScreenshotUtil.doScreenshotAndSave(url1);
        }).start();
        ScreenshotUtil.doScreenshotAndSave(url2);
    }

    @Test
    void testDoScreenshotAndSave_MultiThread2() throws Exception {
        String url1 = "https://www.bilibili.com";
        String url2 = "https://www.baidu.com";

        Thread thread = new Thread(() -> {
            try {
                ScreenshotUtil.doScreenshot(url1).get();
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        });
        thread.start();
        ScreenshotUtil.doScreenshot(url2).get();
        thread.join();
    }
}
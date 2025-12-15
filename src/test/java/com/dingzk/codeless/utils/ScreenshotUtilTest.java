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
}
package com.dingzk.codeless.service;

public interface ScreenshotService {

    /**
     * 对 url 网址截图并上传
     * @param url 要截图的网址
     * @return 截图的 URL
     */
    String doScreenshotAndUpload(String url);
}

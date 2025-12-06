package com.dingzk.codeless.model.enums;

import io.micrometer.common.util.StringUtils;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum GenFileTypeEnum {

    SINGLE_HTML("single_html", "单文件html代码"),
    MULTI_FILE("multi_file", "多文件代码");

    /**
     * 生成文件类型名
     */
    private final String name;
    /**
     * 生成文件类型描述
     */
    private final String text;

    public static GenFileTypeEnum getByName(String name) {
        if (StringUtils.isEmpty(name)) {
            return null;
        }
        for (GenFileTypeEnum value : values()) {
            if (value.getName().equals(name)) {
                return value;
            }
        }
        return null;
    }
}

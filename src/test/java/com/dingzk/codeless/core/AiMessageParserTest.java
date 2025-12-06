package com.dingzk.codeless.core;

import com.dingzk.codeless.ai.model.MultiFileCodeResult;
import com.dingzk.codeless.ai.model.SingleHtmlCodeResult;
import com.dingzk.codeless.core.parser.MultiFileCodeParser;
import com.dingzk.codeless.core.parser.SingleHtmlCodeParser;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class AiMessageParserTest {

    @Test
    void parseSingleHtmlCodeTest() {
        String codeContent = """
                以下是我为你生成的网页：
                ```html
                <!DOCTYPE html>
                <html>
                <head>
                    <title>测试页面</title>
                </head>
                <body>
                    <h1>Hello World!</h1>
                </body>
                </html>
                ```
                这是一段对以上网页的描述
                """;
        SingleHtmlCodeParser singleHtmlCodeParser = new SingleHtmlCodeParser();
        SingleHtmlCodeResult result = singleHtmlCodeParser.parse(codeContent);
        assertNotNull(result);
        assertNotNull(result.getHtmlCode());
    }

    @Test
    void parseMultiFileCodeTest() {
        String codeContent = """
                以下是我为你生成的网页，包含html,css,js文件：
                ```html
                <!DOCTYPE html>
                <html>
                <head>
                    <title>多文件示例</title>
                    <link rel="stylesheet" href="style.css">
                </head>
                <body>
                    <h1>欢迎使用</h1>
                    <script src="script.js"></script>
                </body>
                </html>

                ```css
                h1 {
                    color: blue;
                    text-align: center;
                }
                ```
                ```js
                console.log('页面加载完成');
                ```

                文件创建完成！
                """;
        MultiFileCodeParser multiFileCodeParser = new MultiFileCodeParser();
        MultiFileCodeResult result = multiFileCodeParser.parse(codeContent);
        assertNotNull(result);
        assertNotNull(result.getHtmlCode());
        assertNotNull(result.getCssCode());
        assertNotNull(result.getJsCode());
    }
}
package com.mostar.langchain4jtest.utils;

import org.jsoup.Jsoup;
import org.jsoup.safety.Safelist;

/**
 * XSS 过滤工具类
 * 使用 Jsoup 的 Safelist 过滤恶意 HTML 标签和属性
 */
public class XssFilter {

    private static final Safelist ALLOWED_TAGS = Safelist.relaxed()
            .addTags("strike", "s", "del")
            .addTags("h1", "h2", "h3", "h4", "h5", "h6")
            .addTags("div", "span")
            .addTags("table", "thead", "tbody", "tr", "th", "td")
            .addTags("sub", "sup")
            .addTags("code", "pre", "blockquote")
            .removeTags("script", "iframe", "object", "embed", "form", "input", "textarea")
            .removeAttributes("*", "onfocus", "onblur", "onclick", "ondblclick", "onmousedown",
                    "onmouseup", "onmouseover", "onmousemove", "onmouseout",
                    "onkeypress", "onkeydown", "onkeyup", "onload", "onerror",
                    "onsubmit", "onreset", "onselect", "onchange")
            .addProtocols("a", "href", "http", "https")
            .addProtocols("img", "src", "http", "https");

    /**
     * 过滤用户输入，移除危险的 HTML 标签和属性
     * @param input 用户输入
     * @return 过滤后的安全内容
     */
    public static String filter(String input) {
        if (input == null || input.isEmpty()) {
            return input;
        }
        return Jsoup.clean(input, ALLOWED_TAGS);
    }

    /**
     * 纯文本转义（用于不需要 HTML 格式的场景）
     * @param input 用户输入
     * @return HTML 转义后的内容
     */
    public static String escapeHtml(String input) {
        if (input == null || input.isEmpty()) {
            return input;
        }
        return Jsoup.parse(input).text();
    }
}

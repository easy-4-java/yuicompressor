package com.yahoo.platform.yui.compressor;

import java.io.StringReader;
import java.io.StringWriter;
import junit.framework.TestCase;

public class CompressionTest extends TestCase {
    private String javascript(String source, boolean munge) throws Exception {
        StringWriter output = new StringWriter();
        new JavaScriptCompressor(new StringReader(source), null)
                .compress(output, null, -1, munge, false, false, false);
        return output.toString();
    }

    public void testJavascriptCompressionMungesLocalNames() throws Exception {
        String source = "/* remove me */ function sum(first, second) { var result = first + second; return result; } sum(19, 23);";
        String compressed = javascript(source, true);
        assertTrue(compressed.length() < source.length());
        assertFalse(compressed.contains("remove me"));
        assertTrue(compressed, compressed.matches(
                "function sum\\(([a-z]),([a-z])\\)\\{var ([a-z])=\\1\\+\\2;return \\3\\}sum\\(19,23\\);"));
    }

    public void testJavascriptPreservesRegexAndStringLiterals() throws Exception {
        String source = "var url = 'https://example.com/a'; var pattern = /https?:\\/\\//; pattern.test(url) ? url : 'bad';";
        assertEquals("var url=\"https://example.com/a\";var pattern=/https?:\\/\\//;pattern.test(url)?url:\"bad\";", javascript(source, false));
    }

    private String css(String source) throws Exception {
        StringWriter output = new StringWriter();
        new CssCompressor(new StringReader(source)).compress(output, -1);
        return output.toString();
    }

    public void testCssCompressionRemovesCommentsAndWhitespace() throws Exception {
        assertEquals("body{margin:0;padding:0;color:#fff}",
                css("/* remove me */ body { margin: 0px; padding: 0px; color: #ffffff; }"));
    }

    public void testCssPreservesQuotedContentAndDataUrl() throws Exception {
        String compressed = css(".icon { background: url(data:image/png;base64,AAAA); } .label:after { content: \"a  b\"; }");
        assertTrue(compressed.contains("url(data:image/png;base64,AAAA)"));
        assertTrue(compressed.contains("\"a  b\""));
    }
}

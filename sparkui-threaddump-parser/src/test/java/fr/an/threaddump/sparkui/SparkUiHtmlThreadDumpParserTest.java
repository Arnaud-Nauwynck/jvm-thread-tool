package fr.an.threaddump.sparkui;

import fr.an.threaddump.sparkui.SparkUiHtmlThreadDumpParser;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.junit.Test;

import java.io.File;
import java.io.IOException;

public class SparkUiHtmlThreadDumpParserTest {

    SparkUiHtmlThreadDumpParser sut = new SparkUiHtmlThreadDumpParser();

    @Test
    public void test() {
        Document doc;
        try {
            doc = Jsoup.parse(new File("src/test/data/threaddump1.html"));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        sut.htmlDocToThreadDump(doc);
    }
}

package gmail.asteroster.examplesfromlesson;

import com.codeborne.pdftest.PDF;
import com.codeborne.selenide.Selenide;
import com.codeborne.xlstest.XLS;
import com.opencsv.CSVReader;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Assertions;

import static com.codeborne.selenide.Selectors.byText;
import static org.assertj.core.api.Assertions.assertThat;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipInputStream;

import static com.codeborne.selenide.Selenide.$;

public class SelenideDownloadTest {

    ClassLoader cl = SelenideDownloadTest.class.getClassLoader();

    @DisplayName("Скачивание файла")
    @Test
    void downloadTest() throws Exception {
        Selenide.open("https://github.com/junit-team/junit5/blob/main/README.md");
        File textFile = $("#raw-url").download();
        try (InputStream is = new FileInputStream(textFile)) {
            assertThat(new String(is.readAllBytes(), StandardCharsets.UTF_8))
                    .contains("This repository is the home of the next generation of JUnit");
        }
    }

    @DisplayName("Скачивание PDF")
    @Test
    void selenideParsingPdfTest() throws Exception {
        Selenide.open("https://junit.org/junit5/docs/current/user-guide/");
        File pdfDownload = $(byText("PDF download")).download();
        PDF pdf = new PDF(pdfDownload);
        assertThat(pdf.author).contains("Marc Philipp");
    }

    @DisplayName("парсинг csv")
    @Test
    void csvParseTest() throws Exception {
        try (InputStream stream = cl.getResourceAsStream("files/csvTest.csv");
             CSVReader reader = new CSVReader(new InputStreamReader(stream, StandardCharsets.UTF_8))) {

            List<String[]> content = reader.readAll();
            org.assertj.core.api.Assertions.assertThat(content).contains(
                    new String[]{"Name", "Surname"},
                    new String[]{"Dmitrii", "Tuchs"},
                    new String[]{"Artem", "Eroshenko"}
            );
        }
    }

    @DisplayName("Проверка файлов внутри zip")
    @Test
    void zipParsingTest() throws Exception {
        try (InputStream is = cl.getResourceAsStream("files/zipTestFile.zip");
             ZipInputStream zis = new ZipInputStream(is)) {
            ZipEntry entry;
            while ((entry = zis.getNextEntry()) != null) {
                if (entry.getName().equals("junit-user-guide-5.8.2")) {
                    PDF pdf = new PDF(zis);
                    Assertions.assertEquals(166, pdf.numberOfPages);
                    Assertions.assertEquals("JUnit 5 User Guide", pdf.title);

                } else if (entry.getName().equals("xlsxTest")) {
                    XLS xls = new XLS(zis);
                    String getStringCellValue = xls.excel.getSheetAt(0).getRow(0).getCell(0).getStringCellValue();
                    org.assertj.core.api.Assertions.assertThat(getStringCellValue).contains("привет QA GURU!");
                } else if (entry.getName().equals("csvTest")) {
                    CSVReader reader = new CSVReader(new InputStreamReader(zis, StandardCharsets.UTF_8)); {

                        List<String[]> content = reader.readAll();
                        org.assertj.core.api.Assertions.assertThat(content).contains(
                                new String[]{"Name","Surname"},
                                new String[]{"тест","тестовский"});
                    }
                }
            }
        }
    }
}
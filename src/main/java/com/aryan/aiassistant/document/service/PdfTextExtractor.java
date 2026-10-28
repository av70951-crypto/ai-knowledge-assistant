
package com.aryan.aiassistant.document.service;

import com.aryan.aiassistant.document.dto.ExtractedPage;
import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@Service
public class PdfTextExtractor {

    public List<ExtractedPage> extractPages(byte[] pdfBytes) {

        List<ExtractedPage> extractedPages = new ArrayList<>();

        try (PDDocument document = Loader.loadPDF(pdfBytes)) {

            PDFTextStripper stripper = new PDFTextStripper();

            for (int page = 1; page <= document.getNumberOfPages(); page++) {

                stripper.setStartPage(page);
                stripper.setEndPage(page);

                String text = stripper.getText(document).trim();

                extractedPages.add(new ExtractedPage(page, text));
            }

            return extractedPages;

        } catch (IOException e) {
            throw new IllegalArgumentException(
                    "Unable to extract text from the PDF.");
        }
    }
}

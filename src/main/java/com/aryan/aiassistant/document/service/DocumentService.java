
package com.aryan.aiassistant.document.service;

import com.aryan.aiassistant.document.dto.ExtractedPage;
import com.aryan.aiassistant.document.entity.Document;
import com.aryan.aiassistant.document.entity.DocumentChunk;
import com.aryan.aiassistant.document.repository.DocumentChunkRepository;
import com.aryan.aiassistant.document.repository.DocumentRepository;

import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
public class DocumentService {

    private final DocumentRepository documentRepository;
    private final DocumentChunkRepository documentChunkRepository;
    private final PdfTextExtractor pdfTextExtractor;
    private final Path uploadDirectory;

    public DocumentService(
            DocumentRepository documentRepository,
            DocumentChunkRepository documentChunkRepository,
            PdfTextExtractor pdfTextExtractor,
            @Value("${app.upload.dir}") String uploadDir) {

        this.documentRepository = documentRepository;
        this.documentChunkRepository = documentChunkRepository;
        this.pdfTextExtractor = pdfTextExtractor;
        this.uploadDirectory = Paths.get(uploadDir)
                .toAbsolutePath()
                .normalize();
    }

    public Document uploadPdf(MultipartFile file, Long organizationId) {

        validatePdf(file);

        if (documentRepository.countByOrganizationId(organizationId) >= 10) {
            throw new IllegalArgumentException(
                    "An organization can upload a maximum of 10 PDFs.");
        }

        String storedFileName = UUID.randomUUID() + ".pdf";
        Path destination = uploadDirectory.resolve(storedFileName);

        try {
            Files.createDirectories(uploadDirectory);

            byte[] fileBytes = file.getBytes();
            int pageCount;

            try (PDDocument pdf = Loader.loadPDF(fileBytes)) {
                pageCount = pdf.getNumberOfPages();

                if (pageCount == 0 || pageCount > 20) {
                    throw new IllegalArgumentException(
                            "PDF must contain between 1 and 20 pages.");
                }
            }

            List<ExtractedPage> pages =
                    pdfTextExtractor.extractPages(fileBytes);

            Files.write(destination, fileBytes);

            Document document = new Document();
            document.setOrganizationId(organizationId);

            String originalFileName = Paths
                    .get(file.getOriginalFilename())
                    .getFileName()
                    .toString();

            document.setFileName(originalFileName);
            document.setFilePath(destination.toString());
            document.setPageCount(pageCount);
            document.setUploadedAt(LocalDateTime.now());

            Document savedDocument = documentRepository.save(document);

            for (ExtractedPage page : pages) {

                if (page.getText() != null && !page.getText().isBlank()) {

                    DocumentChunk chunk = new DocumentChunk();
                    chunk.setDocumentId(savedDocument.getId());
                    chunk.setOrganizationId(organizationId);
                    chunk.setPageNumber(page.getPageNumber());
                    chunk.setContent(page.getText());

                    documentChunkRepository.save(chunk);
                }
            }

            return savedDocument;

        } catch (IOException e) {

            deleteFileQuietly(destination);

            throw new IllegalArgumentException(
                    "Unable to read or save the PDF file.");

        } catch (RuntimeException e) {

            deleteFileQuietly(destination);
            throw e;
        }
    }

    public void validatePdf(MultipartFile file) {

        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException(
                    "Please select a PDF file.");
        }

        if (file.getSize() > 10L * 1024 * 1024) {
            throw new IllegalArgumentException(
                    "PDF file size must not exceed 10 MB.");
        }

        String fileName = file.getOriginalFilename();

        if (fileName == null ||
                !fileName.toLowerCase().endsWith(".pdf")) {
            throw new IllegalArgumentException(
                    "Only PDF files are allowed.");
        }

        String contentType = file.getContentType();

        if (contentType != null &&
                !contentType.equalsIgnoreCase("application/pdf")) {
            throw new IllegalArgumentException(
                    "Invalid PDF content type.");
        }
    }

    public List<Document> getDocuments(Long organizationId) {
        return documentRepository.findByOrganizationId(organizationId);
    }

    public void deleteDocument(Long documentId, Long organizationId) {

        Document document = documentRepository
                .findByIdAndOrganizationId(documentId, organizationId)
                .orElseThrow(() -> new IllegalArgumentException(
                        "Document not found."));

        // Delete the extracted text chunks first.
        documentChunkRepository.deleteByDocumentIdAndOrganizationId(
                documentId, organizationId);

        // Delete the stored PDF.
        Path filePath = Paths.get(document.getFilePath());

        try {
            Files.deleteIfExists(filePath);
        } catch (IOException e) {
            throw new IllegalArgumentException(
                    "Unable to delete the stored PDF file.");
        }

        // Delete the document metadata.
        documentRepository.delete(document);
    }

    private void deleteFileQuietly(Path filePath) {
        try {
            Files.deleteIfExists(filePath);
        } catch (IOException ignored) {
            // Preserve the original error.
        }
    }
}

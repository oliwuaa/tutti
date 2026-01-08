package com.example.tutti.storage;

import com.example.tutti.music.score.Score;
import com.example.tutti.music.part.Part;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.springframework.stereotype.Service;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;

@Service
public class PdfExtractionService {

    private final String UPLOADS_DIR = "./uploads";

    public byte[] extractPartPdfBytes(Score score, Part part) throws IOException {
        String relativePath = score.getPdfPath().replace("/files/", "");
        File sourceFile = Paths.get(UPLOADS_DIR).resolve(relativePath).toFile();

        if (!sourceFile.exists()) {
            throw new IOException("Plik źródłowy PDF nie istnieje.");
        }

        try (PDDocument sourceDoc = PDDocument.load(sourceFile);
             PDDocument partDoc = new PDDocument();
             ByteArrayOutputStream baos = new ByteArrayOutputStream()) {

            int totalPages = sourceDoc.getNumberOfPages();
            int start = Math.max(0, part.getPages().getPageStart() - 1);
            int end = Math.min(totalPages - 1, part.getPages().getPageEnd() - 1);

            if (start > end) {
                throw new IllegalArgumentException("Nieprawidłowy zakres stron dla głosu.");
            }

            for (int i = start; i <= end; i++) {
                partDoc.addPage(sourceDoc.getPage(i));
            }

            partDoc.save(baos);
            return baos.toByteArray();
        }
    }
}
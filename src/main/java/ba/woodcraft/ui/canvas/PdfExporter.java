package ba.woodcraft.ui.canvas;

import ba.woodcraft.model.PointM;
import ba.woodcraft.model.ShapeModel;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.util.List;

public class PdfExporter {
    private static final Logger logger = LoggerFactory.getLogger(PdfExporter.class);

    public void export(File file, List<ShapeModel> shapes, double canvasWidthMeters, double canvasHeightMeters) {
        PDRectangle pageSize = PDRectangle.A4;
        try (PDDocument document = new PDDocument()) {
            PDPage page = new PDPage(pageSize);
            document.addPage(page);

            float margin = 36;
            float drawableWidth = pageSize.getWidth() - 2 * margin;
            float drawableHeight = pageSize.getHeight() - 2 * margin;

            double scaleX = drawableWidth / canvasWidthMeters;
            double scaleY = drawableHeight / canvasHeightMeters;
            double scale = Math.min(scaleX, scaleY);

            try (PDPageContentStream content = new PDPageContentStream(document, page)) {
                content.setLineWidth(1f);
                content.setFont(PDType1Font.HELVETICA, 9);

                for (ShapeModel shape : shapes) {
                    List<PointM> points = shape.getPoints();
                    if (points.size() < 3) {
                        continue;
                    }
                    PointM first = points.get(0);
                    float startX = (float) (margin + first.getXMeters() * scale);
                    float startY = (float) (margin + first.getYMeters() * scale);
                    content.moveTo(startX, startY);

                    for (int i = 1; i < points.size(); i++) {
                        PointM p = points.get(i);
                        float x = (float) (margin + p.getXMeters() * scale);
                        float y = (float) (margin + p.getYMeters() * scale);
                        content.lineTo(x, y);
                    }
                    content.closePath();
                    content.stroke();

                    drawDimensions(content, points, margin, scale);
                }
            }
            document.save(file);
        } catch (IOException e) {
            logger.error("PDF export failed for {}", file, e);
        }
    }

    private void drawDimensions(PDPageContentStream content, List<PointM> points, float margin, double scale) throws IOException {
        int count = points.size();
        for (int i = 0; i < count; i++) {
            PointM a = points.get(i);
            PointM b = points.get((i + 1) % count);
            double length = Math.hypot(b.getXMeters() - a.getXMeters(), b.getYMeters() - a.getYMeters());

            double midX = (a.getXMeters() + b.getXMeters()) / 2.0;
            double midY = (a.getYMeters() + b.getYMeters()) / 2.0;

            float textX = (float) (margin + midX * scale + 8);
            float textY = (float) (margin + midY * scale + 8);
            content.beginText();
            content.newLineAtOffset(textX, textY);
            content.showText(String.format("%.2f m", length));
            content.endText();
        }
    }
}

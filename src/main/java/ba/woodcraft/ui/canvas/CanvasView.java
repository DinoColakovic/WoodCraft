package ba.woodcraft.ui.canvas;

import ba.woodcraft.dao.DrawingDAO;
import ba.woodcraft.dao.MaterialDAO;
import ba.woodcraft.model.Material;
import ba.woodcraft.model.PointM;
import ba.woodcraft.model.ShapeModel;
import ba.woodcraft.ui.Session;
import javafx.geometry.Insets;
import javafx.geometry.Point2D;
import javafx.geometry.Rectangle2D;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.*;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.stage.FileChooser;
import org.apache.commons.math3.geometry.euclidean.twod.Vector2D;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class CanvasView {
    private static final double DEFAULT_SCALE = 120.0;
    private static final double NODE_RADIUS = 4.5;
    private static final double CLOSE_DISTANCE_PX = 10.0;

    private final BorderPane root = new BorderPane();
    private final Canvas canvas = new Canvas(900, 650);
    private final List<PointM> draftPoints = new ArrayList<>();
    private final List<ShapeModel> shapes = new ArrayList<>();
    private final MaterialDAO materialDAO = new MaterialDAO();
    private final DrawingDAO drawingDAO = new DrawingDAO();

    private double scale = DEFAULT_SCALE;
    private double canvasWidthMeters = 6;
    private double canvasHeightMeters = 4;

    private ShapeModel selectedShape;
    private DragState dragState;

    private final ComboBox<Material> materialCombo = new ComboBox<>();
    private final TextField thicknessField = new TextField();
    private final Label areaLabel = new Label("Area: 0.00 m²");
    private final Label priceLabel = new Label("Price: 0.00");

    private final TextField projectField = new TextField("Untitled");

    public CanvasView() {
        setupCanvas();
        root.setCenter(wrapCanvas());
        root.setRight(buildInspector());
        root.setTop(buildToolbar());
        refreshMaterials();
        redraw();
    }

    public BorderPane getRoot() {
        return root;
    }

    private HBox buildToolbar() {
        TextField widthField = new TextField(String.valueOf(canvasWidthMeters));
        TextField heightField = new TextField(String.valueOf(canvasHeightMeters));
        Button applySize = new Button("Apply Canvas Size (m)");
        Button exportPdf = new Button("Export PDF");
        Button save = new Button("Save");
        Button load = new Button("Load");

        applySize.setOnAction(event -> {
            Double width = parseDouble(widthField.getText());
            Double height = parseDouble(heightField.getText());
            if (width == null || height == null || width <= 0 || height <= 0) {
                alert("Canvas size must be positive numbers.");
                return;
            }
            canvasWidthMeters = width;
            canvasHeightMeters = height;
            canvas.setWidth(canvasWidthMeters * scale);
            canvas.setHeight(canvasHeightMeters * scale);
            redraw();
        });

        exportPdf.setOnAction(event -> {
            FileChooser chooser = new FileChooser();
            chooser.setTitle("Export PDF");
            chooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("PDF", "*.pdf"));
            File file = chooser.showSaveDialog(root.getScene().getWindow());
            if (file != null) {
                PdfExporter exporter = new PdfExporter();
                exporter.export(file, shapes, canvasWidthMeters, canvasHeightMeters);
            }
        });

        save.setOnAction(event -> handleSave());
        load.setOnAction(event -> handleLoad());

        HBox box = new HBox(10,
                new Label("Width (m)"), widthField,
                new Label("Height (m)"), heightField,
                applySize,
                new Label("Project"), projectField,
                save, load,
                exportPdf);
        box.setPadding(new Insets(10));
        return box;
    }

    private VBox buildInspector() {
        VBox panel = new VBox(10);
        panel.setPadding(new Insets(12));
        panel.setPrefWidth(260);

        Label header = new Label("Shape Properties");
        header.setStyle("-fx-font-size: 14px; -fx-font-weight: bold;");

        materialCombo.setPromptText("Select material");
        thicknessField.setPromptText("Thickness (m)");

        Button apply = new Button("Apply to Selected");
        apply.setOnAction(event -> {
            if (selectedShape == null) {
                alert("Select a shape first.");
                return;
            }
            Double thickness = parseDouble(thicknessField.getText());
            if (thickness == null || thickness <= 0) {
                alert("Thickness must be a positive number.");
                return;
            }
            Material material = materialCombo.getValue();
            if (material == null) {
                alert("Select a material.");
                return;
            }
            selectedShape.setThicknessMeters(thickness);
            selectedShape.setMaterial(material);
            updateMetrics(selectedShape);
            redraw();
        });

        Button refresh = new Button("Refresh Materials");
        refresh.setOnAction(event -> refreshMaterials());

        panel.getChildren().addAll(header, materialCombo, thicknessField, apply, refresh, areaLabel, priceLabel);
        return panel;
    }

    private BorderPane wrapCanvas() {
        BorderPane pane = new BorderPane();
        pane.setPadding(new Insets(10));
        pane.setCenter(canvas);
        return pane;
    }

    private void setupCanvas() {
        canvas.setWidth(canvasWidthMeters * scale);
        canvas.setHeight(canvasHeightMeters * scale);
        canvas.addEventHandler(MouseEvent.MOUSE_PRESSED, event -> {
            if (event.getButton() != MouseButton.PRIMARY) {
                return;
            }
            Point2D click = new Point2D(event.getX(), event.getY());
            DragState hitNode = findNodeAt(click);
            if (hitNode != null) {
                dragState = hitNode;
                event.consume();
            }
        });
        canvas.addEventHandler(MouseEvent.MOUSE_DRAGGED, event -> {
            if (dragState == null) {
                return;
            }
            Point2D point = new Point2D(event.getX(), event.getY());
            PointM moved = toMeters(point);
            if (dragState.shape == null) {
                draftPoints.set(dragState.index, moved);
            } else {
                dragState.shape.replacePoint(dragState.index, moved);
            }
            redraw();
        });
        canvas.addEventHandler(MouseEvent.MOUSE_RELEASED, event -> dragState = null);
        canvas.setOnMouseClicked(event -> {
            if (event.getButton() != MouseButton.PRIMARY) {
                return;
            }
            if (dragState != null) {
                return;
            }
            Point2D click = new Point2D(event.getX(), event.getY());
            ShapeModel hitShape = findShapeAt(click);
            if (hitShape != null) {
                selectShape(hitShape);
                redraw();
                return;
            }
            if (!draftPoints.isEmpty() && click.distance(toPixels(draftPoints.get(0))) <= CLOSE_DISTANCE_PX) {
                if (draftPoints.size() >= 3) {
                    ShapeModel shape = new ShapeModel(draftPoints);
                    shapes.add(shape);
                    draftPoints.clear();
                    selectShape(shape);
                }
                redraw();
                return;
            }
            draftPoints.add(toMeters(click));
            redraw();
        });
    }

    private void refreshMaterials() {
        if (Session.getUser() == null) {
            return;
        }
        List<Material> materials = materialDAO.listMaterialsForUser(Session.getUser().getId());
        materialCombo.getItems().setAll(materials);
    }

    private void selectShape(ShapeModel shape) {
        selectedShape = shape;
        if (shape.getMaterial() != null) {
            materialCombo.getSelectionModel().select(shape.getMaterial());
        } else {
            materialCombo.getSelectionModel().clearSelection();
        }
        if (shape.getThicknessMeters() > 0) {
            thicknessField.setText(String.format("%.3f", shape.getThicknessMeters()));
        }
        updateMetrics(shape);
    }

    private void updateMetrics(ShapeModel shape) {
        double area = computeArea(shape.getPoints());
        areaLabel.setText(String.format("Area: %.2f m²", area));
        double price = 0.0;
        if (shape.getMaterial() != null) {
            Material material = shape.getMaterial();
            price += area * material.getCostPerArea();
            if (shape.getThicknessMeters() > 0) {
                price += area * shape.getThicknessMeters() * material.getCostPerVolume();
            }
        }
        priceLabel.setText(String.format("Price: %.2f", price));
    }

    private ShapeModel findShapeAt(Point2D clickPx) {
        for (ShapeModel shape : shapes) {
            if (pointInPolygon(clickPx, shape.getPoints())) {
                return shape;
            }
        }
        return null;
    }

    private boolean pointInPolygon(Point2D pointPx, List<PointM> polygon) {
        int crossings = 0;
        int count = polygon.size();
        for (int i = 0; i < count; i++) {
            Point2D a = toPixels(polygon.get(i));
            Point2D b = toPixels(polygon.get((i + 1) % count));
            boolean cond1 = a.getY() > pointPx.getY();
            boolean cond2 = b.getY() > pointPx.getY();
            if (cond1 != cond2) {
                double slope = (b.getX() - a.getX()) / (b.getY() - a.getY());
                double intersectX = a.getX() + (pointPx.getY() - a.getY()) * slope;
                if (pointPx.getX() < intersectX) {
                    crossings++;
                }
            }
        }
        return crossings % 2 == 1;
    }

    private void redraw() {
        GraphicsContext gc = canvas.getGraphicsContext2D();
        gc.setFill(Color.web("#f7f7f7"));
        gc.fillRect(0, 0, canvas.getWidth(), canvas.getHeight());
        drawGrid(gc);

        for (ShapeModel shape : shapes) {
            boolean selected = shape == selectedShape;
            drawShape(gc, shape, selected);
        }

        drawDraft(gc);
    }

    private void drawGrid(GraphicsContext gc) {
        gc.setStroke(Color.web("#e0e0e0"));
        gc.setLineWidth(1);
        double tick = scale * 0.5;
        for (double x = 0; x <= canvas.getWidth(); x += tick) {
            gc.strokeLine(x, 0, x, canvas.getHeight());
        }
        for (double y = 0; y <= canvas.getHeight(); y += tick) {
            gc.strokeLine(0, y, canvas.getWidth(), y);
        }
        gc.setFill(Color.web("#888"));
        gc.setFont(Font.font(10));
        for (double x = 0; x <= canvas.getWidth(); x += tick) {
            gc.fillText(String.format("%.1f", x / scale), x + 2, 12);
        }
        for (double y = 0; y <= canvas.getHeight(); y += tick) {
            gc.fillText(String.format("%.1f", y / scale), 2, y - 2);
        }
    }

    private void drawShape(GraphicsContext gc, ShapeModel shape, boolean selected) {
        List<PointM> points = shape.getPoints();
        if (points.size() < 3) {
            return;
        }
        List<Rectangle2D> labelBounds = new ArrayList<>();
        double[] xs = new double[points.size()];
        double[] ys = new double[points.size()];
        for (int i = 0; i < points.size(); i++) {
            Point2D px = toPixels(points.get(i));
            xs[i] = px.getX();
            ys[i] = px.getY();
        }
        gc.setFill(selected ? Color.web("#cfe8ff") : Color.web("#d9ead3"));
        gc.setStroke(selected ? Color.web("#1565c0") : Color.web("#2e7d32"));
        gc.setLineWidth(selected ? 2 : 1.5);
        gc.fillPolygon(xs, ys, points.size());
        gc.strokePolygon(xs, ys, points.size());

        drawDimensions(gc, points, labelBounds);
    }

    private void drawDimensions(GraphicsContext gc, List<PointM> points, List<Rectangle2D> labelBounds) {
        gc.setStroke(Color.web("#424242"));
        gc.setFill(Color.web("#424242"));
        gc.setFont(Font.font(11));
        int count = points.size();
        for (int i = 0; i < count; i++) {
            Point2D a = toPixels(points.get(i));
            Point2D b = toPixels(points.get((i + 1) % count));
            double lengthMeters = a.distance(b) / scale;
            if (lengthMeters < 0.05) {
                continue;
            }
            Point2D mid = a.midpoint(b);

            Vector2D direction = new Vector2D(b.getX() - a.getX(), b.getY() - a.getY());
            Vector2D normal = new Vector2D(-direction.getY(), direction.getX());
            if (normal.getNorm() > 0) {
                normal = normal.normalize().scalarMultiply(14);
            }
            Point2D labelPos = resolveLabelPosition(mid, normal, labelBounds, String.format("%.2f m", lengthMeters));

            gc.strokeLine(a.getX(), a.getY(), b.getX(), b.getY());
            gc.fillText(String.format("%.2f m", lengthMeters), labelPos.getX(), labelPos.getY());
        }
    }

    private void drawDraft(GraphicsContext gc) {
        if (draftPoints.isEmpty()) {
            return;
        }
        gc.setStroke(Color.web("#ff8f00"));
        gc.setLineWidth(1.5);
        for (int i = 0; i < draftPoints.size() - 1; i++) {
            Point2D a = toPixels(draftPoints.get(i));
            Point2D b = toPixels(draftPoints.get(i + 1));
            gc.strokeLine(a.getX(), a.getY(), b.getX(), b.getY());
        }
        for (PointM point : draftPoints) {
            Point2D px = toPixels(point);
            gc.setFill(Color.web("#ffb74d"));
            gc.fillOval(px.getX() - NODE_RADIUS, px.getY() - NODE_RADIUS, NODE_RADIUS * 2, NODE_RADIUS * 2);
            gc.setStroke(Color.web("#ef6c00"));
            gc.strokeOval(px.getX() - NODE_RADIUS, px.getY() - NODE_RADIUS, NODE_RADIUS * 2, NODE_RADIUS * 2);
        }
    }

    private Point2D resolveLabelPosition(Point2D mid, Vector2D normal, List<Rectangle2D> labelBounds, String text) {
        double offset = 12;
        double width = text.length() * 6.5;
        double height = 12;
        for (int i = 0; i < 6; i++) {
            double dx = normal.getX() * (offset / 14.0);
            double dy = normal.getY() * (offset / 14.0);
            Point2D candidate = new Point2D(mid.getX() + dx, mid.getY() + dy);
            Rectangle2D bounds = new Rectangle2D(candidate.getX(), candidate.getY() - height, width, height);
            boolean overlaps = labelBounds.stream().anyMatch(bounds::intersects);
            if (!overlaps) {
                labelBounds.add(bounds);
                return candidate;
            }
            offset += 10;
        }
        Point2D fallback = new Point2D(mid.getX() + normal.getX(), mid.getY() + normal.getY());
        labelBounds.add(new Rectangle2D(fallback.getX(), fallback.getY() - height, width, height));
        return fallback;
    }

    private PointM toMeters(Point2D pointPx) {
        return new PointM(pointPx.getX() / scale, pointPx.getY() / scale);
    }

    private Point2D toPixels(PointM pointM) {
        return new Point2D(pointM.getXMeters() * scale, pointM.getYMeters() * scale);
    }

    private Double parseDouble(String value) {
        try {
            return Double.parseDouble(value.trim());
        } catch (Exception e) {
            return null;
        }
    }

    private double computeArea(List<PointM> points) {
        double sum = 0;
        int count = points.size();
        for (int i = 0; i < count; i++) {
            PointM a = points.get(i);
            PointM b = points.get((i + 1) % count);
            sum += a.getXMeters() * b.getYMeters() - b.getXMeters() * a.getYMeters();
        }
        return Math.abs(sum) / 2.0;
    }

    private void alert(String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private void handleSave() {
        if (Session.getUser() == null) {
            alert("Log in first.");
            return;
        }
        String name = projectField.getText().trim();
        if (name.isEmpty()) {
            alert("Project name is required.");
            return;
        }
        boolean saved = drawingDAO.saveDrawing(Session.getUser().getId(), name, canvasWidthMeters, canvasHeightMeters, shapes);
        if (!saved) {
            alert("Save failed. Check the logs for details.");
        }
    }

    private void handleLoad() {
        if (Session.getUser() == null) {
            alert("Log in first.");
            return;
        }
        String name = projectField.getText().trim();
        if (name.isEmpty()) {
            alert("Project name is required.");
            return;
        }
        var drawing = drawingDAO.loadDrawing(Session.getUser().getId(), name);
        if (drawing == null) {
            alert("No saved project found.");
            return;
        }
        canvasWidthMeters = drawing.getCanvasWidthMeters();
        canvasHeightMeters = drawing.getCanvasHeightMeters();
        canvas.setWidth(canvasWidthMeters * scale);
        canvas.setHeight(canvasHeightMeters * scale);
        shapes.clear();
        shapes.addAll(drawing.getShapes());
        draftPoints.clear();
        selectedShape = null;
        redraw();
    }

    private DragState findNodeAt(Point2D click) {
        for (int i = 0; i < draftPoints.size(); i++) {
            Point2D px = toPixels(draftPoints.get(i));
            if (px.distance(click) <= NODE_RADIUS * 1.5) {
                return new DragState(null, i);
            }
        }
        for (ShapeModel shape : shapes) {
            List<PointM> points = shape.getPoints();
            for (int i = 0; i < points.size(); i++) {
                Point2D px = toPixels(points.get(i));
                if (px.distance(click) <= NODE_RADIUS * 1.5) {
                    return new DragState(shape, i);
                }
            }
        }
        return null;
    }

    private static class DragState {
        private final ShapeModel shape;
        private final int index;

        private DragState(ShapeModel shape, int index) {
            this.shape = shape;
            this.index = index;
        }
    }
}

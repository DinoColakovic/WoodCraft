package ba.woodcraft.ui.controller;

import ba.woodcraft.model.BezierCurveShape;
import ba.woodcraft.model.CircleShape;
import ba.woodcraft.model.Drawable;
import ba.woodcraft.model.FreehandShape;
import ba.woodcraft.model.LineShape;
import ba.woodcraft.model.RectangleShape;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.geometry.Point2D;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.control.ToggleButton;
import javafx.scene.control.ToggleGroup;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.scene.shape.Rectangle;

public class CanvasController {

    private enum Tool {
        SELECT,
        FREEHAND,
        LINE,
        RECTANGLE,
        CIRCLE,
        BEZIER
    }

    @FXML private StackPane canvasHost;
    @FXML private Group zoomGroup;
    @FXML private Pane drawingPane;

    @FXML private ToggleButton lineTool;
    @FXML private ToggleButton rectangleTool;
    @FXML private ToggleButton circleTool;
    @FXML private ToggleButton bezierTool;
    @FXML private ToggleButton selectTool;

    private Tool activeTool = Tool.FREEHAND;
    private Drawable activeShape;
    private Node selectedNode;
    private SelectionOverlay selectionOverlay;

    private double zoom = 1.0;
    private static final double ZOOM_STEP = 1.1;
    private static final double ZOOM_MIN = 0.3;
    private static final double ZOOM_MAX = 4.0;

    @FXML
    public void initialize() {
        ToggleGroup toolGroup = new ToggleGroup();
        lineTool.setToggleGroup(toolGroup);
        rectangleTool.setToggleGroup(toolGroup);
        circleTool.setToggleGroup(toolGroup);
        bezierTool.setToggleGroup(toolGroup);
        selectTool.setToggleGroup(toolGroup);

        // Default: no tool selected => FREEHAND
        toolGroup.selectToggle(null);
        activeTool = Tool.FREEHAND;

        // Allow "click again to return to freehand"
        allowDeselectToFreehand(lineTool, toolGroup);
        allowDeselectToFreehand(rectangleTool, toolGroup);
        allowDeselectToFreehand(circleTool, toolGroup);
        allowDeselectToFreehand(bezierTool, toolGroup);
        allowDeselectToFreehand(selectTool, toolGroup);

        toolGroup.selectedToggleProperty().addListener((obs, oldToggle, newToggle) -> {
            if (newToggle == null) {
                activeTool = Tool.FREEHAND;
                clearSelection();
                selectionOverlay.setActive(false);
            } else if (newToggle == selectTool) {
                activeTool = Tool.SELECT;
                selectionOverlay.setActive(true);
            } else {
                clearSelection();
                selectionOverlay.setActive(false);
                if (newToggle == lineTool) activeTool = Tool.LINE;
                else if (newToggle == rectangleTool) activeTool = Tool.RECTANGLE;
                else if (newToggle == circleTool) activeTool = Tool.CIRCLE;
                else if (newToggle == bezierTool) activeTool = Tool.BEZIER;
            }
        });

        zoomGroup.setPickOnBounds(false);
        drawingPane.setPickOnBounds(true);

        Rectangle clip = new Rectangle();
        clip.widthProperty().bind(canvasHost.widthProperty());
        clip.heightProperty().bind(canvasHost.heightProperty());
        canvasHost.setClip(clip);

        selectionOverlay = new SelectionOverlay();
        selectionOverlay.attachTo(drawingPane);
        selectionOverlay.setActive(false);

        applyZoom();
    }

    private void allowDeselectToFreehand(ToggleButton btn, ToggleGroup group) {
        btn.addEventFilter(MouseEvent.MOUSE_PRESSED, e -> {
            if (btn.isSelected()) {
                // prevent default behavior (which would keep it selected)
                e.consume();
                group.selectToggle(null); // => FREEHAND
            }
        });
    }

    @FXML
    public void onZoomIn(ActionEvent event) {
        zoom = Math.min(ZOOM_MAX, zoom * ZOOM_STEP);
        applyZoom();
    }

    @FXML
    public void onZoomOut(ActionEvent event) {
        zoom = Math.max(ZOOM_MIN, zoom / ZOOM_STEP);
        applyZoom();
    }

    @FXML
    public void onZoomReset(ActionEvent event) {
        zoom = 1.0;
        applyZoom();
    }

    private void applyZoom() {
        zoomGroup.setScaleX(zoom);
        zoomGroup.setScaleY(zoom);
    }

    @FXML
    public void onClear() {
        drawingPane.getChildren().clear();
        selectionOverlay.attachTo(drawingPane);
        clearSelection();
        selectionOverlay.setActive(activeTool == Tool.SELECT);
    }

    @FXML
    public void onLogout() {
        SceneNavigator.show("view/login.fxml");
    }

    private double clamp(double v, double min, double max) {
        return Math.max(min, Math.min(v, max));
    }

    private Point2D getCanvasPoint(MouseEvent event) {
        Point2D local = drawingPane.sceneToLocal(event.getSceneX(), event.getSceneY());
        double w = drawingPane.getPrefWidth();
        double h = drawingPane.getPrefHeight();
        return new Point2D(
                clamp(local.getX(), 0, w),
                clamp(local.getY(), 0, h)
        );
    }

    @FXML
    public void onMousePressed(MouseEvent event) {
        if (activeTool == Tool.SELECT) {
            Node picked = findSelectableNode(event);
            if (picked == null) clearSelection();
            else setSelectedNode(picked);
            return;
        }

        clearSelection();
        Point2D p = getCanvasPoint(event);
        activeShape = createShape(p.getX(), p.getY());
        if (activeShape != null) drawingPane.getChildren().add(activeShape.getNode());
    }

    @FXML
    public void onMouseDragged(MouseEvent event) {
        if (activeTool == Tool.SELECT) {
            return;
        }
        if (activeShape != null) {
            Point2D p = getCanvasPoint(event);
            activeShape.update(p.getX(), p.getY());
        }
    }

    @FXML
    public void onMouseReleased(MouseEvent event) {
        if (activeTool == Tool.SELECT) {
            return;
        }
        if (activeShape != null) {
            Point2D p = getCanvasPoint(event);
            activeShape.update(p.getX(), p.getY());
            activeShape = null;
        }
    }

    private void setSelectedNode(Node node) {
        selectedNode = node;
        selectionOverlay.setTarget(node);
    }

    private void clearSelection() {
        selectedNode = null;
        selectionOverlay.clear();
    }

    private Node findSelectableNode(MouseEvent event) {
        Point2D scenePoint = new Point2D(event.getSceneX(), event.getSceneY());
        for (int i = drawingPane.getChildren().size() - 1; i >= 0; i--) {
            Node node = drawingPane.getChildren().get(i);
            if (!node.isVisible() || selectionOverlay.isOverlayNode(node)) {
                continue;
            }
            Point2D localPoint = node.sceneToLocal(scenePoint);
            if (node.contains(localPoint)) {
                return node;
            }
        }
        return null;
    }

    private Drawable createShape(double x, double y) {
        return switch (activeTool) {
            case FREEHAND -> new FreehandShape(x, y);
            case LINE -> new LineShape(x, y);
            case RECTANGLE -> new RectangleShape(x, y);
            case CIRCLE -> new CircleShape(x, y);
            case BEZIER -> new BezierCurveShape(x, y);
            case SELECT -> null;
        };
    }
}

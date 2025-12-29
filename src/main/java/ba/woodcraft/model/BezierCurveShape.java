package ba.woodcraft.model;

import javafx.scene.Node;
import javafx.scene.paint.Color;
import javafx.scene.shape.QuadCurve;

public class BezierCurveShape implements Drawable {
    private final double startX;
    private final double startY;
    private final QuadCurve curve;
    private double endX;
    private double endY;

    public BezierCurveShape(double startX, double startY) {
        this.startX = startX;
        this.startY = startY;
        this.endX = startX;
        this.endY = startY;
        this.curve = new QuadCurve();
        this.curve.setStartX(startX);
        this.curve.setStartY(startY);
        this.curve.setEndX(startX);
        this.curve.setEndY(startY);
        this.curve.setStroke(Color.DARKSLATEBLUE);
        this.curve.setStrokeWidth(2);
        this.curve.setFill(Color.TRANSPARENT);
    }

    @Override
    public Node getNode() {
        return curve;
    }

    @Override
    public void update(double x, double y) {
        setEnd(x, y);
        setControlPoints((startX + x) / 2.0, (startY + y) / 2.0);
    }

    public void setEnd(double x, double y) {
        endX = x;
        endY = y;
        curve.setStartX(startX);
        curve.setStartY(startY);
        curve.setEndX(endX);
        curve.setEndY(endY);
    }

    public void setControlPoints(double controlX, double controlY) {
        curve.setControlX(controlX);
        curve.setControlY(controlY);
    }
}

package ba.woodcraft.model;

import java.util.ArrayList;
import java.util.List;

public class Drawing {
    private final int id;
    private final String name;
    private final double canvasWidthMeters;
    private final double canvasHeightMeters;
    private final List<ShapeModel> shapes;

    public Drawing(int id, String name, double canvasWidthMeters, double canvasHeightMeters, List<ShapeModel> shapes) {
        this.id = id;
        this.name = name;
        this.canvasWidthMeters = canvasWidthMeters;
        this.canvasHeightMeters = canvasHeightMeters;
        this.shapes = new ArrayList<>(shapes);
    }

    public int getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public double getCanvasWidthMeters() {
        return canvasWidthMeters;
    }

    public double getCanvasHeightMeters() {
        return canvasHeightMeters;
    }

    public List<ShapeModel> getShapes() {
        return new ArrayList<>(shapes);
    }
}

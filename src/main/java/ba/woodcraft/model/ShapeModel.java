package ba.woodcraft.model;

import java.util.ArrayList;
import java.util.List;

public class ShapeModel {
    private final List<PointM> points;
    private double thicknessMeters;
    private Material material;

    public ShapeModel(List<PointM> points) {
        this.points = new ArrayList<>(points);
    }

    public List<PointM> getPoints() {
        return new ArrayList<>(points);
    }

    public double getThicknessMeters() {
        return thicknessMeters;
    }

    public void setThicknessMeters(double thicknessMeters) {
        this.thicknessMeters = thicknessMeters;
    }

    public Material getMaterial() {
        return material;
    }

    public void setMaterial(Material material) {
        this.material = material;
    }
}

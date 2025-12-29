package ba.woodcraft.model;

public class Material {
    private final int id;
    private final String name;
    private final double thickness;
    private final double costPerArea;
    private final double costPerVolume;

    public Material(int id, String name, double thickness, double costPerArea, double costPerVolume) {
        this.id = id;
        this.name = name;
        this.thickness = thickness;
        this.costPerArea = costPerArea;
        this.costPerVolume = costPerVolume;
    }

    public int getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public double getThickness() {
        return thickness;
    }

    public double getCostPerArea() {
        return costPerArea;
    }

    public double getCostPerVolume() {
        return costPerVolume;
    }

    @Override
    public String toString() {
        return name;
    }
}

package ba.woodcraft.model;

public class PointM {
    private final double xMeters;
    private final double yMeters;

    public PointM(double xMeters, double yMeters) {
        this.xMeters = xMeters;
        this.yMeters = yMeters;
    }

    public double getXMeters() {
        return xMeters;
    }

    public double getYMeters() {
        return yMeters;
    }
}

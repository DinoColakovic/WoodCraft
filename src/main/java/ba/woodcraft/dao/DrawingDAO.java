package ba.woodcraft.dao;

import ba.woodcraft.db.DBConnection;
import ba.woodcraft.model.Drawing;
import ba.woodcraft.model.Material;
import ba.woodcraft.model.PointM;
import ba.woodcraft.model.ShapeModel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

public class DrawingDAO {
    private static final Logger logger = LoggerFactory.getLogger(DrawingDAO.class);

    private final MaterialDAO materialDAO = new MaterialDAO();

    public Drawing loadDrawing(int userId, String name) {
        String sql = "SELECT id, canvas_width_m, canvas_height_m FROM drawings WHERE user_id = ? AND name = ?";
        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, userId);
            statement.setString(2, name);
            ResultSet rs = statement.executeQuery();
            if (!rs.next()) {
                return null;
            }
            int drawingId = rs.getInt("id");
            double width = rs.getDouble("canvas_width_m");
            double height = rs.getDouble("canvas_height_m");
            List<ShapeModel> shapes = loadShapes(connection, drawingId);
            return new Drawing(drawingId, name, width, height, shapes);
        } catch (Exception e) {
            logger.error("Failed to load drawing {} for user {}", name, userId, e);
            return null;
        }
    }

    public boolean saveDrawing(int userId, String name, double canvasWidth, double canvasHeight, List<ShapeModel> shapes) {
        String insertDrawing = """
                INSERT INTO drawings (user_id, name, canvas_width_m, canvas_height_m)
                VALUES (?, ?, ?, ?)
                ON DUPLICATE KEY UPDATE canvas_width_m = VALUES(canvas_width_m),
                                        canvas_height_m = VALUES(canvas_height_m)
                """;
        try (Connection connection = DBConnection.getConnection()) {
            connection.setAutoCommit(false);
            int drawingId;
            try (PreparedStatement statement = connection.prepareStatement(insertDrawing, Statement.RETURN_GENERATED_KEYS)) {
                statement.setInt(1, userId);
                statement.setString(2, name);
                statement.setDouble(3, canvasWidth);
                statement.setDouble(4, canvasHeight);
                statement.executeUpdate();
                ResultSet keys = statement.getGeneratedKeys();
                if (keys.next()) {
                    drawingId = keys.getInt(1);
                } else {
                    drawingId = findDrawingId(connection, userId, name);
                }
            }

            if (drawingId <= 0) {
                connection.rollback();
                return false;
            }
            clearDrawing(connection, drawingId);
            insertShapes(connection, drawingId, shapes);
            connection.commit();
            return true;
        } catch (Exception e) {
            logger.error("Failed to save drawing {} for user {}", name, userId, e);
            return false;
        }
    }

    private int findDrawingId(Connection connection, int userId, String name) throws Exception {
        String sql = "SELECT id FROM drawings WHERE user_id = ? AND name = ?";
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, userId);
            statement.setString(2, name);
            ResultSet rs = statement.executeQuery();
            if (rs.next()) {
                return rs.getInt("id");
            }
        }
        return -1;
    }

    private void clearDrawing(Connection connection, int drawingId) throws Exception {
        try (PreparedStatement deletePoints = connection.prepareStatement(
                "DELETE sp FROM shape_points sp JOIN shapes s ON sp.shape_id = s.id WHERE s.drawing_id = ?")) {
            deletePoints.setInt(1, drawingId);
            deletePoints.executeUpdate();
        }
        try (PreparedStatement deleteShapes = connection.prepareStatement("DELETE FROM shapes WHERE drawing_id = ?")) {
            deleteShapes.setInt(1, drawingId);
            deleteShapes.executeUpdate();
        }
    }

    private void insertShapes(Connection connection, int drawingId, List<ShapeModel> shapes) throws Exception {
        String insertShape = "INSERT INTO shapes (drawing_id, shape_order, thickness_m, material_id) VALUES (?, ?, ?, ?)";
        String insertPoint = "INSERT INTO shape_points (shape_id, x_m, y_m, point_order) VALUES (?, ?, ?, ?)";
        int order = 0;
        for (ShapeModel shape : shapes) {
            int shapeId;
            try (PreparedStatement statement = connection.prepareStatement(insertShape, Statement.RETURN_GENERATED_KEYS)) {
                statement.setInt(1, drawingId);
                statement.setInt(2, order++);
                statement.setDouble(3, shape.getThicknessMeters());
                Material material = shape.getMaterial();
                if (material != null) {
                    statement.setInt(4, material.getId());
                } else {
                    statement.setNull(4, java.sql.Types.INTEGER);
                }
                statement.executeUpdate();
                ResultSet keys = statement.getGeneratedKeys();
                if (!keys.next()) {
                    throw new IllegalStateException("Could not persist shape.");
                }
                shapeId = keys.getInt(1);
            }

            List<PointM> points = shape.getPoints();
            for (int i = 0; i < points.size(); i++) {
                PointM point = points.get(i);
                try (PreparedStatement statement = connection.prepareStatement(insertPoint)) {
                    statement.setInt(1, shapeId);
                    statement.setDouble(2, point.getXMeters());
                    statement.setDouble(3, point.getYMeters());
                    statement.setInt(4, i);
                    statement.executeUpdate();
                }
            }
        }
    }

    private List<ShapeModel> loadShapes(Connection connection, int drawingId) throws Exception {
        String sql = "SELECT id, thickness_m, material_id FROM shapes WHERE drawing_id = ? ORDER BY shape_order";
        List<ShapeModel> shapes = new ArrayList<>();
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, drawingId);
            ResultSet rs = statement.executeQuery();
            while (rs.next()) {
                int shapeId = rs.getInt("id");
                double thickness = rs.getDouble("thickness_m");
                int materialId = rs.getInt("material_id");
                List<PointM> points = loadPoints(connection, shapeId);
                ShapeModel shape = new ShapeModel(points);
                shape.setThicknessMeters(thickness);
                if (!rs.wasNull()) {
                    Material material = materialDAO.findById(materialId);
                    shape.setMaterial(material);
                }
                shapes.add(shape);
            }
        }
        return shapes;
    }

    private List<PointM> loadPoints(Connection connection, int shapeId) throws Exception {
        String sql = "SELECT x_m, y_m FROM shape_points WHERE shape_id = ? ORDER BY point_order";
        List<PointM> points = new ArrayList<>();
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, shapeId);
            ResultSet rs = statement.executeQuery();
            while (rs.next()) {
                points.add(new PointM(rs.getDouble("x_m"), rs.getDouble("y_m")));
            }
        }
        return points;
    }
}

package ba.woodcraft.dao;

import ba.woodcraft.db.DBConnection;
import ba.woodcraft.model.Material;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

public class MaterialDAO {
    private static final Logger logger = LoggerFactory.getLogger(MaterialDAO.class);

    public List<Material> listMaterialsForUser(int userId) {
        String sql = """
                SELECT m.id, m.name, m.cost_per_area, m.cost_per_volume
                FROM materials m
                JOIN user_materials um ON um.material_id = m.id
                WHERE um.user_id = ?
                ORDER BY m.name
                """;
        List<Material> materials = new ArrayList<>();
        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, userId);
            ResultSet rs = statement.executeQuery();
            while (rs.next()) {
                materials.add(new Material(
                        rs.getInt("id"),
                        rs.getString("name"),
                        rs.getDouble("cost_per_area"),
                        rs.getDouble("cost_per_volume")
                ));
            }
        } catch (Exception e) {
            logger.error("Failed to list materials for user {}", userId, e);
        }
        return materials;
    }

    public List<Material> listAllMaterials() {
        String sql = "SELECT id, name, cost_per_area, cost_per_volume FROM materials ORDER BY name";
        List<Material> materials = new ArrayList<>();
        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            ResultSet rs = statement.executeQuery();
            while (rs.next()) {
                materials.add(new Material(
                        rs.getInt("id"),
                        rs.getString("name"),
                        rs.getDouble("cost_per_area"),
                        rs.getDouble("cost_per_volume")
                ));
            }
        } catch (Exception e) {
            logger.error("Failed to list materials.", e);
        }
        return materials;
    }

    public Material createMaterial(String name, double costPerArea, double costPerVolume) {
        String sql = "INSERT INTO materials (name, cost_per_area, cost_per_volume) VALUES (?, ?, ?)";
        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            statement.setString(1, name);
            statement.setDouble(2, costPerArea);
            statement.setDouble(3, costPerVolume);
            if (statement.executeUpdate() != 1) {
                return null;
            }
            ResultSet keys = statement.getGeneratedKeys();
            if (keys.next()) {
                return new Material(keys.getInt(1), name, costPerArea, costPerVolume);
            }
        } catch (Exception e) {
            logger.error("Failed to create material {}", name, e);
        }
        return null;
    }

    public boolean assignMaterialToUser(int userId, int materialId) {
        String sql = "INSERT IGNORE INTO user_materials (user_id, material_id) VALUES (?, ?)";
        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, userId);
            statement.setInt(2, materialId);
            return statement.executeUpdate() >= 0;
        } catch (Exception e) {
            logger.error("Failed to assign material {} to user {}", materialId, userId, e);
            return false;
        }
    }

    public boolean removeMaterialForUser(int userId, int materialId) {
        String deleteLink = "DELETE FROM user_materials WHERE user_id = ? AND material_id = ?";
        String usage = "SELECT COUNT(*) FROM user_materials WHERE material_id = ?";
        String deleteMaterial = "DELETE FROM materials WHERE id = ?";
        try (Connection connection = DBConnection.getConnection();
             PreparedStatement deleteStmt = connection.prepareStatement(deleteLink);
             PreparedStatement usageStmt = connection.prepareStatement(usage);
             PreparedStatement deleteMaterialStmt = connection.prepareStatement(deleteMaterial)) {
            deleteStmt.setInt(1, userId);
            deleteStmt.setInt(2, materialId);
            deleteStmt.executeUpdate();

            usageStmt.setInt(1, materialId);
            ResultSet rs = usageStmt.executeQuery();
            if (rs.next() && rs.getInt(1) == 0) {
                deleteMaterialStmt.setInt(1, materialId);
                deleteMaterialStmt.executeUpdate();
            }
            return true;
        } catch (Exception e) {
            logger.error("Failed to remove material {} for user {}", materialId, userId, e);
            return false;
        }
    }

    public Material findById(int id) {
        String sql = "SELECT id, name, cost_per_area, cost_per_volume FROM materials WHERE id = ?";
        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, id);
            ResultSet rs = statement.executeQuery();
            if (rs.next()) {
                return new Material(
                        rs.getInt("id"),
                        rs.getString("name"),
                        rs.getDouble("cost_per_area"),
                        rs.getDouble("cost_per_volume")
                );
            }
        } catch (Exception e) {
            logger.error("Failed to load material {}", id, e);
        }
        return null;
    }
}

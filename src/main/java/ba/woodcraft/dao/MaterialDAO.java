package ba.woodcraft.dao;

import ba.woodcraft.model.Material;
import ba.woodcraft.util.DBConnection;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

public class MaterialDAO {

    public List<Material> findMaterialsForUser(int userId) {
        String sql = """
                SELECT m.id, m.name, m.thickness, m.cost_per_area, m.cost_per_volume
                FROM materials m
                JOIN user_materials um ON um.material_id = m.id
                WHERE um.user_id = ?
                ORDER BY m.name
                """;
        List<Material> materials = new ArrayList<>();
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, userId);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                materials.add(new Material(
                        rs.getInt("id"),
                        rs.getString("name"),
                        rs.getDouble("thickness"),
                        rs.getDouble("cost_per_area"),
                        rs.getDouble("cost_per_volume")
                ));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return materials;
    }

    public Material addMaterialForUser(int userId, Material material) {
        String insertMaterial = """
                INSERT INTO materials (name, thickness, cost_per_area, cost_per_volume)
                VALUES (?, ?, ?, ?)
                """;
        String insertJoin = "INSERT INTO user_materials (user_id, material_id) VALUES (?, ?)";

        try (Connection con = DBConnection.getConnection();
             PreparedStatement materialStatement = con.prepareStatement(insertMaterial, Statement.RETURN_GENERATED_KEYS);
             PreparedStatement joinStatement = con.prepareStatement(insertJoin)) {

            materialStatement.setString(1, material.getName());
            materialStatement.setDouble(2, material.getThickness());
            materialStatement.setDouble(3, material.getCostPerArea());
            materialStatement.setDouble(4, material.getCostPerVolume());

            int inserted = materialStatement.executeUpdate();
            if (inserted != 1) {
                return null;
            }

            ResultSet keys = materialStatement.getGeneratedKeys();
            if (!keys.next()) {
                return null;
            }
            int materialId = keys.getInt(1);

            joinStatement.setInt(1, userId);
            joinStatement.setInt(2, materialId);
            if (joinStatement.executeUpdate() != 1) {
                return null;
            }

            return new Material(materialId, material.getName(), material.getThickness(),
                    material.getCostPerArea(), material.getCostPerVolume());
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public boolean removeUserMaterial(int userId, int materialId) {
        String deleteJoin = "DELETE FROM user_materials WHERE user_id = ? AND material_id = ?";
        String checkUsage = "SELECT COUNT(*) FROM user_materials WHERE material_id = ?";
        String deleteMaterial = "DELETE FROM materials WHERE id = ?";

        try (Connection con = DBConnection.getConnection();
             PreparedStatement deleteJoinStatement = con.prepareStatement(deleteJoin);
             PreparedStatement checkStatement = con.prepareStatement(checkUsage);
             PreparedStatement deleteMaterialStatement = con.prepareStatement(deleteMaterial)) {

            deleteJoinStatement.setInt(1, userId);
            deleteJoinStatement.setInt(2, materialId);
            if (deleteJoinStatement.executeUpdate() != 1) {
                return false;
            }

            checkStatement.setInt(1, materialId);
            ResultSet rs = checkStatement.executeQuery();
            if (rs.next() && rs.getInt(1) == 0) {
                deleteMaterialStatement.setInt(1, materialId);
                deleteMaterialStatement.executeUpdate();
            }

            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }
}

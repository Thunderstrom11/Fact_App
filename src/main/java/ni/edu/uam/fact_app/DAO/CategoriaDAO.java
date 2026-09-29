package ni.edu.uam.fact_app.DAO;

import ni.edu.uam.fact_app.application.DatabaseConnection;
import ni.edu.uam.fact_app.models.Categoria;


import lombok.Data;


import java.sql.*;
import java.util.ArrayList;
import java.util.List;

@Data

public class CategoriaDAO {

    private String mensajeError = "";

    private Categoria conversionObjeto(ResultSet rs) throws SQLException {
        Categoria c = new Categoria();
        c.setId(rs.getInt("id"));
        c.setNombre(rs.getString("nombre"));
        c.setActiva(rs.getBoolean("activa"));
        return c;
    }


    public Integer guardar(Categoria categoria) {
        String query = """
            INSERT INTO categoria (nombre, activa)
            VALUES (?, ?)
            """;

        try (Connection cn = DatabaseConnection.getConnection();
             PreparedStatement ps = cn.prepareStatement(query, Statement.RETURN_GENERATED_KEYS)) {

            ps.setString(1, categoria.getNombre());
            ps.setBoolean(2, categoria.isActiva());

            int filas = ps.executeUpdate();
            if (filas == 0) {
                mensajeError = "Error: No se inserto ninguna fila.";
                return null;
            }

            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) {
                    Integer id = rs.getInt(1);
                    categoria.setId(id);
                    return id;
                }
            }
            return null;

        } catch (SQLException e) {
            mensajeError = traducirError(e);
            System.err.println("CategoriaDAO.guardar: " + mensajeError);
            return null;
        }
    }

    public List<Categoria> listar() {
        String sql = "SELECT id, nombre, activa FROM categoria ORDER BY nombre ASC";
        List<Categoria> lista = new ArrayList<>();

        try (Connection cn = DatabaseConnection.getConnection();
             PreparedStatement ps = cn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                lista.add(conversionObjeto(rs));
            }

        } catch (SQLException e) {
            mensajeError = traducirError(e);
            System.err.println("CategoriaDAO.listar: " + mensajeError);
        }
        return lista;
    }

    public Categoria buscar(int id) {
        String sql = "SELECT id, nombre, activa FROM categoria WHERE id = ?";

        try (Connection cn = DatabaseConnection.getConnection();
             PreparedStatement ps = cn.prepareStatement(sql)) {

            ps.setInt(1, id);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return conversionObjeto(rs);
                }
            }

        } catch (SQLException e) {
            mensajeError = traducirError(e);
            System.err.println("CategoriaDAO.buscar: " + mensajeError);
        }
        return null;
    }

    public boolean actualizar(Categoria categoria) {
        String sql = "UPDATE categoria SET nombre = ?, activa = ? WHERE id = ?";

        try (Connection cn = DatabaseConnection.getConnection();
             PreparedStatement ps = cn.prepareStatement(sql)) {

            ps.setString(1, categoria.getNombre());
            ps.setBoolean(2, categoria.isActiva());
            ps.setInt(3, categoria.getId());

            return ps.executeUpdate() > 0;

        } catch (SQLException e) {
            mensajeError = traducirError(e);
            System.err.println("CategoriaDAO.actualizar: " + mensajeError);
            return false;
        }
    }
    public boolean eliminar(int id) {
        String sql = "DELETE FROM categoria WHERE id = ?";

        try (Connection cn = DatabaseConnection.getConnection();
             PreparedStatement ps = cn.prepareStatement(sql)) {

            ps.setInt(1, id);
            return ps.executeUpdate() > 0;

        } catch (SQLException e) {
            mensajeError = traducirError(e);
            System.err.println("CategoriaDAO.eliminar: " + mensajeError);
            return false;
        }
    }
    private String traducirError(SQLException e) {
        return switch (e.getSQLState()) {
            case "23505" -> "(unique violation) Ya existe una categoria con este nombre.";
            case "23503" -> "(FK violation) No se pudo eliminar, existen productos que usan esta categoria.";
            case "23502" -> "(not null violation) El nombre de la categoria no puede estar vacio.";
            default -> "Error en la BD (PSQL): " + e.getMessage();
        };
    }







}

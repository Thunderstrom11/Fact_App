package ni.edu.uam.fact_app.DAO;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import ni.edu.uam.fact_app.application.DatabaseConnection;
import ni.edu.uam.fact_app.models.Categoria;
import ni.edu.uam.fact_app.models.Producto;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;


@Data
@NoArgsConstructor
@AllArgsConstructor
public class ProductoDAO {

    private String mensajeError = "";

    private Producto conversionObjeto(ResultSet rs) throws SQLException {
        Categoria c = new Categoria();
        c.setId(rs.getInt("c_id"));
        c.setNombre(rs.getString("c_nombre"));
        c.setActiva(rs.getBoolean("c_activa"));

        Producto p = new Producto();
        p.setId(rs.getInt("p_id"));
        p.setCodigo(rs.getString("p_codigo"));
        p.setNombre(rs.getString("p_nombre"));
        p.setPrecioVenta(rs.getBigDecimal("p_precio"));
        p.setExistencia(rs.getInt("p_existencia"));
        p.setRutaImagen(rs.getString("p_ruta"));
        p.setActivo(rs.getBoolean("p_activo"));
        p.setCategoria(c);   //objeto completo
        return p;
    }

    public Integer guardar(Producto p) {
        String sql = """
            INSERT INTO producto
            (
                codigo,
                nombre,
                categoria_id,
                precio_venta,
                existencia,
                ruta_imagen,
                activo
            )
            VALUES (?, ?, ?, ?, ?, ?, ?)
            """;

        try (Connection cn = DatabaseConnection.getConnection();
             PreparedStatement ps = cn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            ps.setString(1, p.getCodigo());
            ps.setString(2, p.getNombre());
            ps.setInt(3, p.getCategoria().getId());   // ← el ID, no el objeto
            ps.setBigDecimal(4, p.getPrecioVenta());  // ← BigDecimal
            ps.setInt(5, p.getExistencia());
            ps.setString(6, p.getRutaImagen());       // ← puede ser null
            ps.setBoolean(7, p.isActivo());

            if (ps.executeUpdate() == 0) {
                mensajeError = "No se inserto ninguna fila.";
                return null;
            }

            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) {
                    Integer id = rs.getInt(1);
                    p.setId(id);
                    return id;
                }
            }
            return null;

        } catch (SQLException e) {
            mensajeError = traducirError(e);
            System.err.println("ProductoDAO.guardar -> " + mensajeError);
            return null;
        }
    }

    public List<Producto> listar() {
        String sql = """
            SELECT p.id            AS p_id,
                   p.codigo        AS p_codigo,
                   p.nombre        AS p_nombre,
                   p.precio_venta  AS p_precio,
                   p.existencia    AS p_existencia,
                   p.ruta_imagen   AS p_ruta,
                   p.activo        AS p_activo,
                   c.id            AS c_id,
                   c.nombre        AS c_nombre,
                   c.activa        AS c_activa
            FROM producto p
            INNER JOIN categoria c
                ON p.categoria_id = c.id
            ORDER BY p.nombre ASC
            """;

        List<Producto> lista = new ArrayList<>();

        try (Connection cn = DatabaseConnection.getConnection();
             PreparedStatement ps = cn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                lista.add(conversionObjeto(rs));
            }

        } catch (SQLException e) {
            mensajeError = traducirError(e);
            System.err.println("ProductoDAO.listar -> " + mensajeError);
        }
        return lista;
    }

    public Producto buscarPorCodigo(String codigo) {
        String sql = """
            SELECT p.id AS p_id, p.codigo AS p_codigo, p.nombre AS p_nombre,
                   p.precio_venta AS p_precio, p.existencia AS p_existencia,
                   p.ruta_imagen AS p_ruta, p.activo AS p_activo,
                   c.id AS c_id, c.nombre AS c_nombre, c.activa AS c_activa
            FROM producto p
            INNER JOIN categoria c ON p.categoria_id = c.id
            WHERE p.codigo = ?
            """;

        try (Connection cn = DatabaseConnection.getConnection();
             PreparedStatement ps = cn.prepareStatement(sql)) {

            ps.setString(1, codigo);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return conversionObjeto(rs);
                }
            }

        } catch (SQLException e) {
            mensajeError = traducirError(e);
            System.err.println("ProductoDAO.buscarPorCodigo -> " + mensajeError);
        }
        return null;
    }

    public boolean actualizar(Producto p) {
        String sql = """
            UPDATE producto
               SET codigo       = ?,
                   nombre       = ?,
                   categoria_id = ?,
                   precio_venta = ?,
                   existencia   = ?,
                   ruta_imagen  = ?,
                   activo       = ?
             WHERE id = ?
            """;

        try (Connection cn = DatabaseConnection.getConnection();
             PreparedStatement ps = cn.prepareStatement(sql)) {

            ps.setString(1, p.getCodigo());
            ps.setString(2, p.getNombre());
            ps.setInt(3, p.getCategoria().getId());
            ps.setBigDecimal(4, p.getPrecioVenta());
            ps.setInt(5, p.getExistencia());
            ps.setString(6, p.getRutaImagen());
            ps.setBoolean(7, p.isActivo());
            ps.setInt(8, p.getId());

            return ps.executeUpdate() > 0;

        } catch (SQLException e) {
            mensajeError = traducirError(e);
            System.err.println("ProductoDAO.actualizar -> " + mensajeError);
            return false;
        }
    }

    public boolean eliminar(int id) {
        String sql = "DELETE FROM producto WHERE id = ?";

        try (Connection cn = DatabaseConnection.getConnection();
             PreparedStatement ps = cn.prepareStatement(sql)) {

            ps.setInt(1, id);
            return ps.executeUpdate() > 0;

        } catch (SQLException e) {
            mensajeError = traducirError(e);
            System.err.println("ProductoDAO.eliminar -> " + mensajeError);
            return false;
        }
    }

    public boolean desactivar(int id) {
        String sql = "UPDATE producto SET activo = FALSE WHERE id = ?";

        try (Connection cn = DatabaseConnection.getConnection();
             PreparedStatement ps = cn.prepareStatement(sql)) {

            ps.setInt(1, id);
            return ps.executeUpdate() > 0;

        } catch (SQLException e) {
            mensajeError = traducirError(e);
            System.err.println("ProductoDAO.desactivar -> " + mensajeError);
            return false;
        }
    }

    private String traducirError(SQLException e) {
        return switch (e.getSQLState()) {
            case "23505" -> "(unique violation) Ya existe un producto con ese codigo.";
            case "23503" -> "(FK violation) La categoria seleccionada no existe.";
            case "23502" -> "(not null violation) Faltan datos obligatorios del producto.";
            default -> "Error en la BD (PSQL): " + e.getMessage();
        };
    }







}

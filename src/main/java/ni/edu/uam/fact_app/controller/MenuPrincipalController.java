package ni.edu.uam.fact_app.controller;

import javafx.application.Platform;
import javafx.collections.ListChangeListener;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import ni.edu.uam.fact_app.models.Producto;
import ni.edu.uam.fact_app.util.SceneManager;
import java.io.IOException;
import java.math.BigDecimal;

public class MenuPrincipalController {
    // Tarjeta de datos
    @FXML private Label lblCantidadProductos;
    @FXML private Label lblCantidadCategorias;
    @FXML private Label lblDineroTotal;
    @FXML private Label lblCantidadEmpleados;
    @FXML private Label lblCantidadCargos;
    @FXML private Label lblCargossinEmpleados;

    @FXML
    public void initialize() {
        ProductoController.getProductos().addListener(
                (ListChangeListener<Object>) change -> actualizarTarjetas());
        EmpleadoController.getEmpleados().addListener(
                (ListChangeListener<Object>) change -> actualizarTarjetas());
        CargoController.getCargos().addListener(
                (ListChangeListener<Object>) change -> actualizarTarjetas());
        CategoriaController.getCategorias().addListener(
                (ListChangeListener<Object>) change -> actualizarTarjetas());
        actualizarTarjetas();
    }

    private void actualizarTarjetas() {
        lblCantidadProductos.setText("Cantidad de productos: "
                + ProductoController.getProductos().size());
        lblCantidadCategorias.setText("Cantidad de categorias: "
                + CategoriaController.getCategorias().size());
        lblDineroTotal.setText("Dinero total: " + formatoDinero(valorInventario()));
        lblCantidadEmpleados.setText("Cantidad empleados: "
                + EmpleadoController.getEmpleados().size());
        lblCantidadCargos.setText("Cantidad cargos: " + CargoController.getCargos().size());
        lblCargossinEmpleados.setText("Cargos sin empleados: " + contarCargosSinEmpleados());
    }
    
    private BigDecimal valorInventario() {
        BigDecimal total = BigDecimal.ZERO;
        for (Producto p : ProductoController.getProductos()) {
            if (p.getPrecioVenta() != null) {
                total = total.add(p.getPrecioVenta()
                        .multiply(BigDecimal.valueOf(p.getExistencia())));
            }
        }
        return total;
    }

    private String formatoDinero(BigDecimal monto) {
        return String.format("C$ %,.2f", monto);
    }

    private long contarCargosSinEmpleados() {
        return CargoController.getCargos().stream()
                .filter(cargo -> EmpleadoController.getEmpleados().stream()
                        .noneMatch(e -> e.getCargo() == cargo))
                .count();
    }

    @FXML
    private void abrirProductos(){
        try {
            SceneManager.abrirVentana(
                    "/ni/edu/uam/fact_app/fxml/producto-view.fxml",
                    "Gestion de Productos");
        }catch (IOException e){
            new Alert(Alert.AlertType.ERROR,"No fue posible abrir Productos").showAndWait();

        }
    }

    @FXML
    private void abrirEmpleados(){
        try {
            SceneManager.abrirVentana(
                    "/ni/edu/uam/fact_app/fxml/empleado-view.fxml",
                    "Gestion de Empleados");
        }catch (IOException e){
            new Alert(Alert.AlertType.ERROR,"No fue posible abrir Empleado").showAndWait();
        }
    }

    @FXML
    private void abrirCargos(){
        try {
            SceneManager.abrirVentana(
                    "/ni/edu/uam/fact_app/fxml/cargo-view.fxml","Ingreso de Cargos");
        }catch (IOException e){
            new Alert(Alert.AlertType.ERROR,"No fue posible abrir Cargo").showAndWait();
        }
    }

    @FXML
    private void abrirCategorias(){
        try {
            SceneManager.abrirVentana(
                    "/ni/edu/uam/fact_app/fxml/categoria-view.fxml","Ingreso de Categorias");
        }catch (IOException e){
            new Alert(Alert.AlertType.ERROR,"No fue posible abrir Categorias").showAndWait();
        }
    }

    @FXML
    private void salir(){
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION, "Desea cerrar la aplicacion ?", ButtonType.OK, ButtonType.CANCEL);
        if (alert.showAndWait().orElse(ButtonType.CANCEL) == ButtonType.OK) Platform.exit();
    }
}

package ni.edu.uam.fact_app.controller;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import ni.edu.uam.fact_app.util.SceneManager;
import java.io.IOException;

public class MenuPrincipalController {
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

package ni.edu.uam.fact_app.controller;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Stage;
import ni.edu.uam.fact_app.models.Categoria;
import ni.edu.uam.fact_app.util.AlertUtils;

public class CategoriaController {
    @FXML private TextField txtID;
    @FXML private TextField txtNombreCategoria;
    @FXML private CheckBox chxbCategoriaActiva;
    @FXML private TableView<Categoria> tblCategorias;
    @FXML private TableColumn<Categoria, Number> colID;
    @FXML private TableColumn<Categoria, String> colCategoria;
    @FXML private TableColumn<Categoria, Boolean> colActiva;

    private static final ObservableList<Categoria> categorias = FXCollections.observableArrayList(
            new Categoria(1, "Eskimo - Lacteos", true),
            new Categoria(2, "Pepsico - Sodas", true),
            new Categoria(3, "Magia Blanca - P.Limpiezas", true));

    private int idEnEdicion = -1;


    @FXML
    public void initialize() {
        tblCategorias.setItems(categorias);
        colID.setCellValueFactory(new PropertyValueFactory<>("id"));
        colCategoria.setCellValueFactory(new PropertyValueFactory<>("nombre"));
        colActiva.setCellValueFactory(new PropertyValueFactory<>("activa"));
        chxbCategoriaActiva.setSelected(true);
        refrescarID();
        txtID.setDisable(true);
    }

    @FXML
    public static ObservableList<Categoria> getCategorias() {
        return categorias;
    }

    private int proximoID() {
        int max = 0;
        for (Categoria c : categorias) {
            if (c.getId() != null && c.getId() > max) {
                max = c.getId();
            }
        }
        return max + 1;
    }

    private void refrescarID() {
        txtID.setText(String.valueOf(proximoID()));
    }

    @FXML
    private void guardar() {
        if (txtNombreCategoria.getText().isBlank()) {
            AlertUtils.showAlert("Datos inválidos",
                    "Complete el nombre de la categoría.");
            return;
        }
        int id = (idEnEdicion > 0) ? idEnEdicion : proximoID();
        Categoria existente = findCategoria(id);
        if (existente != null) {
            existente.setNombre(txtNombreCategoria.getText().trim());
            existente.setActiva(chxbCategoriaActiva.isSelected());
            categorias.set(categorias.indexOf(existente), existente);
            AlertUtils.showInfo("Categoría actualizada",
                    "Se actualizaron los datos de: " + existente.getNombre());
        } else {
            categorias.add(new Categoria(id,
                    txtNombreCategoria.getText().trim(),
                    chxbCategoriaActiva.isSelected()));
            AlertUtils.showInfo("Categoría guardada",
                    "Categoría agregada correctamente: " + txtNombreCategoria.getText().trim());
        }
        limpiar();
    }

    @FXML
    private void editar() {
        Categoria seleccionado = tblCategorias.getSelectionModel().getSelectedItem();
        if (seleccionado == null) {
            AlertUtils.showAlert("Sin selección",
                    "Seleccione la categoría de la tabla a editar.");
            return;
        }
        fillFields();
        idEnEdicion = seleccionado.getId();
    }

    @FXML
    private void eliminar() {
        Categoria seleccionado = tblCategorias.getSelectionModel().getSelectedItem();
        if (seleccionado == null) {
            AlertUtils.showAlert("Sin selección",
                    "Seleccione la categoría de la tabla a eliminar.");
            return;
        }
        if (AlertUtils.showConfirmation("Confirmar eliminación",
                "¿Desea eliminar la categoría '" + seleccionado.getNombre() + "'?")) {
            categorias.remove(seleccionado);
            limpiar();
        }
    }

    private Categoria findCategoria(int id) {
        for (Categoria c : categorias) {
            if (c.getId() != null && c.getId() == id) {
                return c;
            }
        }
        return null;
    }

    private void fillFields() {
        Categoria seleccionado = tblCategorias.getSelectionModel().getSelectedItem();
        if (seleccionado == null) {
            return;
        }
        txtID.setText(String.valueOf(seleccionado.getId()));
        txtNombreCategoria.setText(seleccionado.getNombre());
        chxbCategoriaActiva.setSelected(seleccionado.isActiva());
    }

    private void limpiar() {
        txtID.clear();
        txtNombreCategoria.clear();
        chxbCategoriaActiva.setSelected(true);
        idEnEdicion = -1;
        refrescarID();
    }

    @FXML
    private void cerrar() {
        ((Stage) txtID.getScene().getWindow()).close();
    }
}

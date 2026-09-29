package ni.edu.uam.fact_app.controller;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Stage;
import ni.edu.uam.fact_app.DAO.CategoriaDAO;
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

    private final CategoriaDAO dao = new CategoriaDAO();

    private static final ObservableList<Categoria> categorias = FXCollections.observableArrayList();

    private int idEnEdicion = -1;


    @FXML
    public void initialize() {
        tblCategorias.setItems(categorias);
        colID.setCellValueFactory(new PropertyValueFactory<>("id"));
        colCategoria.setCellValueFactory(new PropertyValueFactory<>("nombre"));
        colActiva.setCellValueFactory(new PropertyValueFactory<>("activa"));
        chxbCategoriaActiva.setSelected(true);
        txtID.setDisable(true);
        cargarBD();
    }

    private void cargarBD() {
        categorias.setAll(dao.listar());
        refrescarID();
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
                    "Complete el campo nombre de la categoría.");
            return;
        }
        boolean exito;

        if (idEnEdicion > 0){
            Categoria c = new Categoria();
            c.setId(idEnEdicion);
            c.setNombre(txtNombreCategoria.getText().trim());
            c.setActiva(chxbCategoriaActiva.isSelected());

            exito = dao.actualizar(c);
            if (exito) {
                AlertUtils.showInfo("Categoría actualizada",
                        "Se actualizaron los datos de: " + c.getNombre());
            }

        }else {
            Categoria c = new Categoria();
            c.setNombre(txtNombreCategoria.getText().trim());
            c.setActiva(chxbCategoriaActiva.isSelected());

            exito = dao.guardar(c)!=null;
            if (exito) {
                AlertUtils.showInfo("Categoría guardada",
                        "Categoría agregada correctamente: " + txtNombreCategoria.getText().trim());
            }

        }
        if (!exito) {
            AlertUtils.showAlert("No se pudo guardar", dao.getMensajeError());
            return;
        }
        cargarBD();
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
        if (!AlertUtils.showConfirmation("Confirmar eliminación",
                "¿Desea eliminar la categoría '" + seleccionado.getNombre() + "'?")) {
            return;
        }
        if (dao.eliminar(seleccionado.getId())){
            AlertUtils.showInfo("Categoria eliminada",
                    "La categoria se elimino correctamente.");
        } else {
            AlertUtils.showAlert("No se pudo eliminar", dao.getMensajeError());
        }

        cargarBD();
        limpiar();
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

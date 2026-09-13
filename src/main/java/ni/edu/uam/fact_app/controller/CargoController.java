package ni.edu.uam.fact_app.controller;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Stage;
import ni.edu.uam.fact_app.models.Cargo;
import ni.edu.uam.fact_app.util.AlertUtils;


public class CargoController {
    @FXML private TextField txtID;
    @FXML private TextField txtNombreCargo;
    @FXML private TextArea txtfDescription;
    @FXML private TableView<Cargo> tblCargos;
    @FXML private TableColumn<Cargo, Number> colID;
    @FXML private TableColumn<Cargo, String> colCargo;
    @FXML private TableColumn<Cargo, String> colDescripcion;

    private static final ObservableList<Cargo> cargos = FXCollections.observableArrayList(
            new Cargo(1, "Cajero", "Atencion de caja"),
            new Cargo(2, "Vendedor", "Atencion al cliente dentro de la tienda"),
            new Cargo(3, "Administrador", "Gestion general del negocio"));
    private boolean idAutomatico = true;

    @FXML
    public void initialize() {
        tblCargos.setItems(cargos);
        colID.setCellValueFactory(new PropertyValueFactory<>("id"));
        colCargo.setCellValueFactory(new PropertyValueFactory<>("nombres"));
        colDescripcion.setCellValueFactory(new PropertyValueFactory<>("descripcion"));
        txtID.setTextFormatter(new TextFormatter<>(change ->
                change.getControlNewText().matches("\\d*") ? change : null));
        refrescarAutoID();
        txtID.setDisable(true);
    }
    @FXML
    // para devolver la lista de cargos compartido entre las escenas
    public static ObservableList<Cargo> getCargos() {
        return cargos;
    }

    @FXML
    private void cambiarModoID() {
        idAutomatico = !idAutomatico;
        if (idAutomatico) {
            txtID.setDisable(true);
            refrescarAutoID();
        } else {
            txtID.setDisable(false);
            txtID.clear();
        }
    }

    private int proximoID() {
        int max = 0;
        for (Cargo c : cargos) {
            if (c.getId() != null && c.getId() > max) {
                max = c.getId();
            }
        }
        return max + 1;
    }

    private void refrescarAutoID() {
        if (idAutomatico) {
            txtID.setText(String.valueOf(proximoID()));
        }
    }

    @FXML
    private void guardar() {
        if (txtNombreCargo.getText().isBlank() || txtfDescription.getText().isBlank()) {
            AlertUtils.showAlert("Datos inválidos",
                    "Complete el nombre del cargo y su descripción.");
            return;
        }
        int id;
        try {
            id = txtID.getText().isBlank() ? proximoID()
                    : Integer.parseInt(txtID.getText().trim());
            if (id <= 0) {
                AlertUtils.showAlert("Datos inválidos", "El ID debe ser mayor que cero.");
                return;
            }
        } catch (NumberFormatException e) {
            AlertUtils.showAlert("Datos inválidos", "El ID debe ser numérico.");
            return;
        }
        Cargo cargo = new Cargo(id,
                txtNombreCargo.getText().trim(),
                txtfDescription.getText().trim());
        Cargo existente = findCargo(id);
        if (existente != null) {
            existente.setNombres(cargo.getNombres());
            existente.setDescripcion(cargo.getDescripcion());
            // Refresca las vistas conectadas
            cargos.set(cargos.indexOf(existente), existente);
            AlertUtils.showInfo("Cargo actualizado",
                    "Se actualizaron los datos de: " + existente.getNombres());
        } else {
            cargos.add(cargo);
            AlertUtils.showInfo("Cargo guardado",
                    "Cargo agregado correctamente: " + cargo.getNombres());
        }
        limpiar();
    }

    @FXML
    private void editar() {
        Cargo seleccionado = tblCargos.getSelectionModel().getSelectedItem();
        if (seleccionado == null) {
            AlertUtils.showAlert("Sin selección",
                    "Seleccione el cargo de la tabla a editar.");
            return;
        }
        fillFields();
    }

    @FXML
    private void eliminar() {
        Cargo seleccionado = tblCargos.getSelectionModel().getSelectedItem();
        if (seleccionado == null) {
            AlertUtils.showAlert("Sin selección",
                    "Seleccione el cargo de la tabla a eliminar.");
            return;
        }
        if (AlertUtils.showConfirmation("Confirmar eliminación",
                "Esta seguro que desea eliminar el cargo '" + seleccionado.getNombres() + "'y a todos los empleados que lo contengan ?")) {
            cargos.remove(seleccionado);
            limpiar();
        }
    }

    private Cargo findCargo(int id) {
        for (Cargo c : cargos) {
            if (c.getId() != null && c.getId() == id) {
                return c;
            }
        }
        return null;
    }

    private void fillFields() {
        Cargo seleccionado = tblCargos.getSelectionModel().getSelectedItem();
        if (seleccionado == null) {
            return;
        }
        txtID.setText(String.valueOf(seleccionado.getId()));
        txtNombreCargo.setText(seleccionado.getNombres());
        txtfDescription.setText(seleccionado.getDescripcion());
    }

    private void limpiar() {
        txtNombreCargo.clear();
        txtfDescription.clear();
        if (!idAutomatico) {
            txtID.clear();
        }
        refrescarAutoID();
    }

    @FXML
    private void cerrar() {
        ((Stage) txtID.getScene().getWindow()).close();
    }
}

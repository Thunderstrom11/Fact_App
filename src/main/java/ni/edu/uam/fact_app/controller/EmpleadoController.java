package ni.edu.uam.fact_app.controller;

import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.beans.property.SimpleStringProperty;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Stage;
import javafx.util.StringConverter;
import ni.edu.uam.fact_app.models.Cargo;
import ni.edu.uam.fact_app.models.Empleado;
import ni.edu.uam.fact_app.util.AlertUtils;
import java.time.LocalDate;


public class EmpleadoController {
    @FXML private TextField txtID;
    @FXML private TextField txtNombresEmpleado;
    @FXML private TextField txtApellidosEmpleado;
    @FXML private ComboBox<Cargo> cmbCargoEmpleado;
    @FXML private CheckBox chkEmpleadoActivo;
    @FXML private DatePicker dpFechaContratacion;
    @FXML private TableView<Empleado> tblEmpleados;
    @FXML private TableColumn<Empleado, Number> colID;
    @FXML private TableColumn<Empleado, String> colNombres;
    @FXML private TableColumn<Empleado, String> colApellidos;
    @FXML private TableColumn<Empleado, String> colCargo;
    @FXML private TableColumn<Empleado, LocalDate> colFechaContr;
    @FXML private TableColumn<Empleado, Boolean> colActivo;

    private static final ObservableList<Empleado> empleados = FXCollections.observableArrayList();

    @FXML
    public static ObservableList<Empleado> getEmpleados() {
        return empleados;
    }
    private int idEnEdicion = -1;

    @FXML
    public void initialize() {
        cmbCargoEmpleado.setItems(CargoController.getCargos());
        // Refrescar la tabla de empleados cuando la lista de cargos cambia
        CargoController.getCargos().addListener(
                (ListChangeListener<Cargo>) change -> tblEmpleados.refresh());
        cmbCargoEmpleado.setConverter(new StringConverter<Cargo>() {
            @Override public String toString(Cargo cargo) {
                return cargo == null ? "" : cargo.getNombres();
            }
            @Override public Cargo fromString(String string) {
                return null;
            }
        });
        tblEmpleados.setItems(empleados);
        chkEmpleadoActivo.setSelected(true);
        colID.setCellValueFactory(new PropertyValueFactory<>("id"));
        colNombres.setCellValueFactory(new PropertyValueFactory<>("nombres"));
        colApellidos.setCellValueFactory(new PropertyValueFactory<>("apellidos"));
        colCargo.setCellValueFactory(data ->
                new SimpleStringProperty(data.getValue().getCargo() == null
                        ? "" : data.getValue().getCargo().getNombres()));
        colFechaContr.setCellValueFactory(new PropertyValueFactory<>("fechaContratacion"));
        colActivo.setCellValueFactory(new PropertyValueFactory<>("activo"));
        refrescarID();
        txtID.setDisable(true);
    }

    @FXML
    private void asignarFechaHoy() {
        dpFechaContratacion.setValue(LocalDate.now());
    }

    private int proximoID() {
        int max = 0;
        for (Empleado e : empleados) {
            if (e.getId() != null && e.getId() > max) {
                max = e.getId();
            }
        }
        return max + 1;
    }

    private void refrescarID() {
        txtID.setText(String.valueOf(proximoID()));
    }

    @FXML
    private void guardar() {
        if (txtNombresEmpleado.getText().isBlank() || txtApellidosEmpleado.getText().isBlank()
                || cmbCargoEmpleado.getValue() == null || dpFechaContratacion.getValue() == null) {
            AlertUtils.showAlert("Datos inválidos",
                    "Complete los campos nombres, apellidos, cargo y fecha de contratación.");
            return;
        }
        int id = (idEnEdicion > 0) ? idEnEdicion : proximoID();
        Empleado empleado = new Empleado(id,
                txtNombresEmpleado.getText().trim(),
                txtApellidosEmpleado.getText().trim(),
                cmbCargoEmpleado.getValue(),
                dpFechaContratacion.getValue(),
                chkEmpleadoActivo.isSelected());
        Empleado existente = findEmpleado(id);
        if (existente != null) {
            empleados.set(empleados.indexOf(existente), empleado);
            AlertUtils.showInfo("Empleado actualizado",
                    "Se actualizaron los datos de: " + empleado.getNombres());
        } else {
            empleados.add(empleado);
            AlertUtils.showInfo("Empleado guardado",
                    "Empleado agregado correctamente: " + empleado.getNombres());
        }
        limpiar();
    }

    @FXML
    private void editar() {
        Empleado seleccionado = tblEmpleados.getSelectionModel().getSelectedItem();
        if (seleccionado == null) {
            AlertUtils.showAlert("Sin selección",
                    "Seleccione el empleado de la tabla a editar.");
            return;
        }
        fillFields();
        idEnEdicion = tblEmpleados.getSelectionModel().getSelectedItem().getId();
    }

    @FXML
    private void eliminar() {
        Empleado seleccionado = tblEmpleados.getSelectionModel().getSelectedItem();
        if (seleccionado == null) {
            AlertUtils.showAlert("Sin selección",
                    "Seleccione el empleado de la tabla a eliminar.");
            return;
        }
        if (AlertUtils.showConfirmation("Confirmar eliminación",
                "Esta seguro que desea eliminar al empleado '" + seleccionado.getNombres() + "'?")) {
            empleados.remove(seleccionado);
            limpiar();
        }
    }

    private Empleado findEmpleado(int id) {
        for (Empleado e : empleados) {
            if (e.getId() != null && e.getId() == id) {
                return e;
            }
        }
        return null;
    }

    private void fillFields() {
        Empleado seleccionado = tblEmpleados.getSelectionModel().getSelectedItem();
        if (seleccionado == null) {
            return;
        }
        txtID.setText(String.valueOf(seleccionado.getId()));
        txtNombresEmpleado.setText(seleccionado.getNombres());
        txtApellidosEmpleado.setText(seleccionado.getApellidos());
        cmbCargoEmpleado.setValue(seleccionado.getCargo());
        dpFechaContratacion.setValue(seleccionado.getFechaContratacion());
        chkEmpleadoActivo.setSelected(seleccionado.isActivo());
    }


    private void limpiar() {
        txtNombresEmpleado.clear(); txtApellidosEmpleado.clear();
        cmbCargoEmpleado.getSelectionModel().clearSelection();
        dpFechaContratacion.setValue(null);
        chkEmpleadoActivo.setSelected(true);
        idEnEdicion = -1;
        refrescarID();
    }


    @FXML
    private void cerrar() {
        ((Stage) txtID.getScene().getWindow()).close();
    }
}

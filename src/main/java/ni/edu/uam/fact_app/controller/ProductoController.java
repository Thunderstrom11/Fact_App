package ni.edu.uam.fact_app.controller;

import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import ni.edu.uam.fact_app.models.Categoria;
import ni.edu.uam.fact_app.models.Producto;
import ni.edu.uam.fact_app.util.AlertUtils;

import java.io.File;
import java.math.BigDecimal;

public class ProductoController {
    @FXML private TextField txtPrecio;
    @FXML private TextField txtNombre;
    @FXML private TextField txtCodigo;
    @FXML private TextField txtExistencia;
    @FXML private ComboBox <Categoria>  cmbCategoria;
    @FXML private CheckBox chkActivo;
    @FXML private ImageView imgProduto;
    @FXML private TableView<Producto> tblProductos;
    @FXML private TableColumn<Producto, String> colCodigo;
    @FXML private TableColumn<Producto, String> colNombre;
    @FXML private TableColumn<Producto, Categoria> colCategoria;
    @FXML private TableColumn<Producto, Number> colPrecio;
    @FXML private TableColumn<Producto, Number> colExistencia;
    @FXML private TableColumn<Producto, Boolean> colActivo;
    @FXML private TableColumn<Producto, String> colImagen;


    private final ObservableList<Producto> productos = FXCollections.observableArrayList();
    private String rutaImagen;


    @FXML
    public void initialize(){
        cmbCategoria.setItems(FXCollections.observableArrayList(
                new Categoria(1, "Alimentos", true),
                new Categoria(2, "Bebidas", true),
                new Categoria(3, "Limpiezas", true)));
        tblProductos.setItems(productos);
        chkActivo.setSelected(true);
        colCodigo.setCellValueFactory(new PropertyValueFactory<>("codigo"));
        colNombre.setCellValueFactory(new PropertyValueFactory<>("nombre"));
        colCategoria.setCellValueFactory(new PropertyValueFactory<>("categoria"));
        colPrecio.setCellValueFactory(new PropertyValueFactory<>("precioVenta"));
        colExistencia.setCellValueFactory(new PropertyValueFactory<>("existencia"));
        colActivo.setCellValueFactory(new PropertyValueFactory<>("activo"));
        txtExistencia.setTextFormatter(new TextFormatter<>(change ->
                change.getControlNewText().matches("\\d*") ? change : null));
        txtPrecio.setTextFormatter(new TextFormatter<>(change ->
                change.getControlNewText().matches("\\d*([.,]\\d*)?") ? change : null));
        colImagen.setCellValueFactory(data ->
                new SimpleObjectProperty<>(data.getValue().getRutaImagen()));
        colImagen.setCellFactory(col -> new TableCell<>() {
            private final ImageView view = new ImageView();
            {
                view.setFitWidth(64); view.setFitHeight(40); view.setPreserveRatio(true);
            }
            @Override
            protected void updateItem(String ruta, boolean empty) {
                super.updateItem(ruta, empty);
                if (empty || ruta == null) {
                    setGraphic(null);
                } else {
                    view.setImage(new Image(new File(ruta).toURI().toString()));
                    setGraphic(view);
                }
            }
        });
    }

    @FXML
    private void seleccionarImagen(){
        FileChooser chooser = new FileChooser();
        chooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Images", "*.png","*.jpg", "*.jpeg"));
        File archivo = chooser.showOpenDialog(txtPrecio.getScene().getWindow());
        if (archivo != null){
            rutaImagen = archivo.getAbsolutePath();
            imgProduto.setImage(new Image(archivo.toURI().toString()));
        }
    }

    // Al guardar si el código ya existe actualiza la fila, si no agrega una nueva
    @FXML
    private void guardar() {
        if (txtCodigo.getText().isBlank() || txtNombre.getText().isBlank()
                || txtPrecio.getText().isBlank() || txtExistencia.getText().isBlank()
                || cmbCategoria.getValue() == null) {
            AlertUtils.showAlert("Datos inválidos", "Complete los campos Nombre, .");
            return;
        }
        try {
            BigDecimal precio = new BigDecimal(txtPrecio.getText().trim().replace(',', '.'));
            int existencia = Integer.parseInt(txtExistencia.getText().trim());
            if (precio.signum() <= 0 || existencia < 0) {
                AlertUtils.showAlert("Datos inválidos",
                        "Precio mayor que cero y existencia no negativa.");
                return;
            }
            Producto producto = new Producto(null, txtCodigo.getText().trim(),
                    txtNombre.getText().trim(),  cmbCategoria.getValue(), precio,
                    existencia, rutaImagen, chkActivo.isSelected());
            Producto existente = findProducto(producto.getCodigo());
            if (existente != null) {
                productos.set(productos.indexOf(existente), producto);
                AlertUtils.showInfo("Producto actualizado",
                        "Se actualizaron los datos de: " + producto.getNombre());
            } else {
                productos.add(producto);
                AlertUtils.showInfo("Producto guardado",
                        "Producto agregado correctamente: " + producto.getNombre());
            }
            limpiar();
        } catch (NumberFormatException e) {
            AlertUtils.showAlert("Datos inválidos", "Precio o existencia no válidos.");
        }
    }

    // editar carga el producto seleccionado y bloquea el codigo
    @FXML
    private void editar() {
        Producto seleccionado = tblProductos.getSelectionModel().getSelectedItem();
        if (seleccionado == null) {
            AlertUtils.showAlert("Sin selección",
                    "Seleccione el producto de la tabla a editar.");
            return;
        }
        fillFields();
        txtCodigo.setDisable(true);
    }

    @FXML
    private void eliminar() {
        Producto seleccionado = tblProductos.getSelectionModel().getSelectedItem();
        if (seleccionado == null) {
            AlertUtils.showAlert("Sin selección",
                    "Seleccione el producto de la tabla a eliminar.");
            return;
        }
        if (AlertUtils.showConfirmation("Confirmar eliminación",
                "¿Desea eliminar el producto '" + seleccionado.getNombre() + "'?")) {
            productos.remove(seleccionado);
            limpiar();
        }
    }


    private Producto findProducto(String codigo) {
        for (Producto p : productos) {
            if (p.getCodigo().equalsIgnoreCase(codigo)) {
                return p;
            }
        }
        return null;
    }

    private void fillFields() {
        Producto seleccionado = tblProductos.getSelectionModel().getSelectedItem();
        if (seleccionado == null) {
            return;
        }
        txtCodigo.setText(seleccionado.getCodigo());
        txtNombre.setText(seleccionado.getNombre());
        txtPrecio.setText(String.valueOf(seleccionado.getPrecioVenta()));
        txtExistencia.setText(String.valueOf(seleccionado.getExistencia()));
        cmbCategoria.setValue(seleccionado.getCategoria());
        chkActivo.setSelected(seleccionado.isActivo());
        rutaImagen = seleccionado.getRutaImagen();
        imgProduto.setImage(rutaImagen == null ? null
                : new Image(new File(rutaImagen).toURI().toString()));
    }

    @FXML
    private void reiniciarImage() {
        imgProduto.setImage(null);
        rutaImagen = null;
    }

    @FXML
    private void cerrar() {
        ((Stage) txtCodigo.getScene().getWindow()).close();
    }


    private void limpiar() {
        txtCodigo.clear(); txtNombre.clear(); txtPrecio.clear(); txtExistencia.clear();
        txtCodigo.setDisable(false);
        cmbCategoria.getSelectionModel().clearSelection();
        cmbCategoria.setPromptText("Categoria");
        chkActivo.setSelected(true); imgProduto.setImage(null); rutaImagen = null;
    }
}


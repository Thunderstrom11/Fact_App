package ni.edu.uam.fact_app.controller;

import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import java.sql.SQLException;
import ni.edu.uam.fact_app.DAO.ProductoDAO;
import ni.edu.uam.fact_app.models.Categoria;
import ni.edu.uam.fact_app.models.Producto;
import ni.edu.uam.fact_app.util.AlertUtils;

import java.io.File;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

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
    @FXML private TextField txtBuscador;
    @FXML private ComboBox<Categoria> cmbFiltroCategoria;
    @FXML private CheckBox chxFiltroExistencia;
    @FXML private RadioButton rbtnFiltroTodos;
    @FXML private RadioButton rbtnFiltroActivo;
    @FXML private RadioButton rbtnFiltroInactivo;


    private static final ObservableList<Producto> productos = FXCollections.observableArrayList();
    private FilteredList<Producto> productosFiltrados;
    private final ObservableList<Categoria> categoriasFiltro = FXCollections.observableArrayList();

    @FXML
    public static ObservableList<Producto> getProductos() {
        return productos;
    }
    private String rutaImagen;
    private final ProductoDAO dao = new ProductoDAO();
    private Integer idEnEdicion = null;



    @FXML
    public void initialize(){
        cmbCategoria.setItems(CategoriaController.getCategorias());
        // Refresca el tableview de productos cuando cambia
        CategoriaController.getCategorias().addListener(
                (javafx.collections.ListChangeListener<Categoria>) change -> {tblProductos.refresh(); cargarFiltroCategorias();});
        tblProductos.setItems(productos);
        productosFiltrados = new FilteredList<>(productos, p -> true);
        tblProductos.setItems(productosFiltrados);
        tblProductos.getSelectionModel().selectedItemProperty().addListener(
                (o, anterior, seleccionado) -> {
                    if (seleccionado != null) {
                        fillFields();
                        idEnEdicion = seleccionado.getId();
                    }
                });
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
        cmbFiltroCategoria.setItems(categoriasFiltro);
        cargarFiltroCategorias();
        txtBuscador.textProperty().addListener((o, a, n) -> Filtrar());
        cmbFiltroCategoria.valueProperty().addListener((o, a, n) -> Filtrar());
        chxFiltroExistencia.selectedProperty().addListener((o, a, n) -> Filtrar());
        rbtnFiltroTodos.selectedProperty().addListener((o, a, n) -> Filtrar());
        rbtnFiltroActivo.selectedProperty().addListener((o, a, n) -> Filtrar());
        rbtnFiltroInactivo.selectedProperty().addListener((o, a, n) -> Filtrar());

        cargarBD();
        Filtrar();
    }

    private void cargarBD() {
        productos.setAll(dao.listar());
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

    // Al guardar si el código ya existe actualiza la fila actual de la db, si no agrega una nueva
    @FXML
    private void guardar() {
        try{
            Producto producto = obtenerProductoFormulario();

            if (dao.codigoExiste(producto.getCodigo(), idEnEdicion)){
                AlertUtils.showAlert("Codigo duplicado", "Ya existe un producto guardado con ese codigo.");
                return;
            }
            boolean exito = idEnEdicion == null
                    ? dao.guardar(producto) != null
                    : dao.actualizar(producto);

            if (!exito) {
                AlertUtils.showAlert("No se pudo guardar", dao.getMensajeError());
                return;
            }

            AlertUtils.showInfo("Producto guardado", "Operación realizada correctamente.");
            cargarBD();
            limpiar();

        } catch (IllegalArgumentException e) {
            AlertUtils.showAlert("Datos inválidos", e.getMessage());
        } catch (SQLException e) {
            AlertUtils.showAlert("Error de base de datos",
                    "No fue posible completar la operación.");
            System.err.println(e.getMessage());
        }
    }


    // editar carga el producto seleccionado
    @FXML
    private void editar() {
        Producto seleccionado = tblProductos.getSelectionModel().getSelectedItem();
        if (seleccionado == null) {
            AlertUtils.showAlert("Sin selección",
                    "Seleccione el producto de la tabla a editar.");
            return;
        }
        fillFields();
        idEnEdicion = seleccionado.getId();
    }

    @FXML
    private void eliminar() {
        Producto seleccionado = tblProductos.getSelectionModel().getSelectedItem();
        if (seleccionado == null) {
            AlertUtils.showAlert("Sin selección",
                    "Seleccione el producto de la tabla a eliminar.");
            return;
        }
        if (!AlertUtils.showConfirmation("Confirmar eliminación",
                "¿Desea eliminar el producto '" + seleccionado.getNombre() + "'?")) {
            return;
        }
        if(dao.eliminar(seleccionado.getId())) {
            AlertUtils.showInfo("Producto eliminado",
                    "El producto se eliminó correctamente.");
        } else {
            AlertUtils.showAlert("No se pudo eliminar", dao.getMensajeError());
        }
        cargarBD();
        limpiar();
    }

    @FXML
    private void limpiarBusqueda() {
        txtBuscador.clear();
        chxFiltroExistencia.setSelected(false);
        rbtnFiltroTodos.setSelected(true);
        if (!categoriasFiltro.isEmpty()) {
            cmbFiltroCategoria.setValue(categoriasFiltro.getFirst());
        }
        Filtrar();
    }


    private void cargarFiltroCategorias() {
        Categoria seleccionada = cmbFiltroCategoria.getValue();

        Categoria todas = new Categoria();
        todas.setNombre("Todas las categorias");

        List<Categoria> items = new ArrayList<>();
        items.add(todas);
        items.addAll(CategoriaController.getCategorias());
        categoriasFiltro.setAll(items);

        if (seleccionada != null && seleccionada.getId() != null) {
            for (Categoria c : categoriasFiltro) {
                if (seleccionada.getId().equals(c.getId())) {
                    cmbFiltroCategoria.setValue(c);
                    return;
                }
            }
        }
        cmbFiltroCategoria.setValue(todas);
    }

    private void Filtrar() {
        String producto = txtBuscador.getText() == null
                ? "" : txtBuscador.getText().trim().toLowerCase();
        Categoria categoriafiltrada = cmbFiltroCategoria.getValue();
        boolean enExistencia = chxFiltroExistencia.isSelected();
        boolean filtroActivos = rbtnFiltroActivo.isSelected();
        boolean filtroInactivos = rbtnFiltroInactivo.isSelected();

        productosFiltrados.setPredicate(p -> {
            // Texto de busqueda
            if (!producto.isEmpty()) {
                boolean okCodigo = p.getCodigo() != null
                        && p.getCodigo().toLowerCase().contains(producto);
                boolean okNombre = p.getNombre() != null
                        && p.getNombre().toLowerCase().contains(producto);
                boolean okCategoria = p.getCategoria() != null
                        && p.getCategoria().getNombre() != null
                        && p.getCategoria().getNombre().toLowerCase().contains(producto);
                if (!okCodigo && !okNombre && !okCategoria) return false;
            }
            // Estado del producto
            if (filtroActivos && !p.isActivo()) return false;
            if (filtroInactivos && p.isActivo()) return false;

            // Categoria a la que pertenece
            if (categoriafiltrada != null && categoriafiltrada.getId() != null) {
                if (p.getCategoria() == null
                        || !categoriafiltrada.getId().equals(p.getCategoria().getId())) return false;
            }
            // Estado de existencia
            if (enExistencia && p.getExistencia() == 0) return false;

            return true;
        });
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
        cmbCategoria.getSelectionModel().clearSelection();
        cmbCategoria.setPromptText("Categoria");
        chkActivo.setSelected(true);
        imgProduto.setImage(null); rutaImagen = null;
        tblProductos.getSelectionModel().clearSelection();
        idEnEdicion = null;
    }

    private Producto obtenerProductoFormulario() throws IllegalArgumentException {
        String codigo = txtCodigo.getText().trim();
        String nombre = txtNombre.getText().trim();
        Categoria categoria = cmbCategoria.getValue();

        if (codigo.isEmpty()) {
            throw new IllegalArgumentException("El código del producto es obligatorio.");
        }
        if (nombre.isEmpty()) {
            throw new IllegalArgumentException("El nombre del producto es obligatorio.");
        }
        if (categoria == null) {
            throw new IllegalArgumentException("Debe seleccionar una categoría.");
        }

        BigDecimal precio;
        try {
            precio = new BigDecimal(txtPrecio.getText().trim().replace(',', '.'));
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("El precio debe ser un valor numérico.");
        }
        if (precio.signum() <= 0) {
            throw new IllegalArgumentException("El precio de venta debe ser mayor que cero.");
        }

        int existencia;
        try {
            existencia = Integer.parseInt(txtExistencia.getText().trim());
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("La existencia debe ser un número entero.");
        }
        if (existencia < 0) {
            throw new IllegalArgumentException("La existencia no puede ser negativa.");
        }

        Producto p = new Producto();
        p.setId(idEnEdicion);
        p.setCodigo(codigo);
        p.setNombre(nombre);
        p.setCategoria(categoria);
        p.setPrecioVenta(precio);
        p.setExistencia(existencia);
        p.setRutaImagen(rutaImagen);
        p.setActivo(chkActivo.isSelected());
        return p;
    }
}


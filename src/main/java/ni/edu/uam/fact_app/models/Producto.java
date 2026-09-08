package ni.edu.uam.fact_app.models;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;

//@Data ya contiene @Getters y @Setters
@Data
@AllArgsConstructor
@NoArgsConstructor

public class Producto {
    private Integer id;
    private String nombre;
    private Caterogia caterogia;
    //Representa los decimales mejor que double
    private BigDecimal precioVenta;
    private int existencia;
    private String rutaImagen;
    private boolean activo;

}

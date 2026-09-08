package ni.edu.uam.fact_app.models;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor


public class Cargo {
    private Integer id;
    private String nombres;
    private String descripcion;

}

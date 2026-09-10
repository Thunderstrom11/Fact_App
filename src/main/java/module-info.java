module ni.edu.uam.fact_app {
    requires javafx.controls;
    requires javafx.fxml;
    requires static lombok;


    opens ni.edu.uam.fact_app.controller to javafx.fxml;
    opens ni.edu.uam.fact_app.models to javafx.base;
    exports ni.edu.uam.fact_app.application to javafx.graphics;
    exports ni.edu.uam.fact_app;
}
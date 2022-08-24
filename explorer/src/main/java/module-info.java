module de.siegmar.jmonkey.explorer {
    requires javafx.graphics;
    requires javafx.fxml;
    requires javafx.controls;
    requires java.desktop;
    requires de.siegmar.jmonkey.datarepository;
    requires de.siegmar.jmonkey.decoder;
    requires de.siegmar.jmonkey.commons;
    requires de.siegmar.jmonkey.lecscanner;
    requires de.siegmar.jmonkey.font;
    requires de.siegmar.jmonkey.index;

    exports de.siegmar.jmonkey.explorer;
    opens de.siegmar.jmonkey.explorer to javafx.fxml;
    exports de.siegmar.jmonkey.explorer.controller;
    opens de.siegmar.jmonkey.explorer.controller to javafx.fxml;
    exports de.siegmar.jmonkey.explorer.model;
    opens de.siegmar.jmonkey.explorer.model to javafx.fxml;
    exports de.siegmar.jmonkey.explorer.misc;
    opens de.siegmar.jmonkey.explorer.misc to javafx.fxml;
}

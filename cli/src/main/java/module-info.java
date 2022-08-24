module de.siegmar.jmonkey.cli {
    opens de.siegmar.jmonkey.cli;
    opens de.siegmar.jmonkey.cli.builder;
    opens de.siegmar.jmonkey.cli.decrypt;
    opens de.siegmar.jmonkey.cli.export;
    requires de.siegmar.jmonkey.index;
    requires de.siegmar.jmonkey.lecscanner;
    requires de.siegmar.jmonkey.decoder;
    requires de.siegmar.jmonkey.commons;
    requires de.siegmar.jmonkey.encoder;
    requires com.fasterxml.jackson.databind;
    requires info.picocli;
    requires java.desktop;
}

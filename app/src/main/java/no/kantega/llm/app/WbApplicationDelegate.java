package no.kantega.llm.app;

import java.io.IOException;
import java.util.List;

import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.jboss.logmanager.Logger;

import io.quarkiverse.fx.FxPostStartupEvent;
import io.quarkus.runtime.StartupEvent;
import jakarta.enterprise.event.Observes;
import jakarta.inject.Inject;
import jakarta.inject.Provider;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;
import no.hal.wb.logging.AppLoggingHandler;

public class WbApplicationDelegate {

    @Inject
    Provider<FXMLLoader> fxmlLoaderProvider;
    
    @Inject
    WbController wbController;

    public void start(@Observes FxPostStartupEvent fxStartupEvent) throws IOException {
        var fxmlLoader = fxmlLoaderProvider.get();
        fxmlLoader.setLocation(getClass().getResource("Wb.fxml"));
        Scene scene = fxmlLoader.load();
        Stage stage = fxStartupEvent.getPrimaryStage();
        stage.setScene(scene);
        stage.setWidth(1400);
        stage.setHeight(1000);
        fxStartupEvent.getPrimaryStage().show();
        fxStartupEvent.getPrimaryStage().setOnCloseRequest(event -> {
            Platform.exit();
            System.exit(0);
        });
    }

    @Inject
    AppLoggingHandler appLoggingHandler;

    @ConfigProperty(name = "wb.log.apploggers")
    List<String> appLoggers;

    void onStart(@Observes StartupEvent ev) {
        for (var logger : appLoggers) {
            Logger.getLogger(logger).addHandler(appLoggingHandler);
        }
    }
}

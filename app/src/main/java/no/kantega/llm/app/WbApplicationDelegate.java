package no.kantega.llm.app;

import java.io.IOException;

import org.jboss.logmanager.Logger;

import io.quarkiverse.fx.FxStartupEvent;
import io.quarkus.runtime.StartupEvent;
import jakarta.enterprise.event.Observes;
import jakarta.inject.Inject;
import jakarta.inject.Provider;
import javafx.application.HostServices;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;
import no.kantega.llm.logging.AppLoggingHandler;

public class WbApplicationDelegate {

  @Inject
  Provider<FXMLLoader> fxmlLoaderProvider;
  
  @Inject
  WbController wbController;

  public void start(@Observes FxStartupEvent fxStartupEvent) throws IOException {
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

  void onStart(@Observes StartupEvent ev) {
        Logger llmLogger = Logger.getLogger("no.kantega.llm");
        llmLogger.addHandler(appLoggingHandler);
    }
}

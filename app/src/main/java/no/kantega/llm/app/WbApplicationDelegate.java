package no.kantega.llm.app;

import java.io.IOException;

import io.quarkiverse.fx.FxStartupEvent;
import jakarta.enterprise.event.Observes;
import jakarta.inject.Inject;
import jakarta.inject.Provider;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

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
}

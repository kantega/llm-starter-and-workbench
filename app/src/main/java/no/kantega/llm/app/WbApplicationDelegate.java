package no.kantega.llm.app;

import java.io.IOException;

import io.quarkiverse.fx.FxStartupEvent;
import jakarta.enterprise.event.Observes;
import jakarta.inject.Inject;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class WbApplicationDelegate {

  @Inject
  FXMLLoader fxmlLoader;

  public void start(@Observes FxStartupEvent fxStartupEvent) throws IOException {
    var fxmlUrl = getClass().getResource("Wb.fxml");
    fxmlLoader.setLocation(fxmlUrl);
    Scene scene = fxmlLoader.load(fxmlUrl.openStream());
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

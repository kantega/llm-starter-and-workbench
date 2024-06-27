package no.kantega.llm.app;

import io.quarkiverse.fx.FxApplication;
import io.quarkus.runtime.Quarkus;
import io.quarkus.runtime.QuarkusApplication;
import io.quarkus.runtime.annotations.QuarkusMain;
import javafx.application.Application;

@QuarkusMain
public class WbApplication implements QuarkusApplication {

    @Override
    public int run(final String... args) {
        Application.launch(FxApplication.class, args);
        return 0;
    }

    public static void main(String[] args) {
        Quarkus.run(WbApplication.class, args); 
    }
}

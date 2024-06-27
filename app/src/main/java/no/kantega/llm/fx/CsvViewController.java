package no.kantega.llm.fx;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URI;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

import org.jboss.logging.Logger;

import jakarta.enterprise.context.Dependent;
import jakarta.enterprise.inject.Instance;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.stage.FileChooser;
import no.hal.expressions.ExpressionSupport;
import no.hal.fx.adapter.LabelAdapter;
import no.hal.fx.bindings.BindingSource;
import no.hal.fx.bindings.BindingsSource;
import no.hal.fx.util.ActionProgressHelper;
import tech.tablesaw.api.Table;
import tech.tablesaw.io.csv.CsvReadOptions;

@Dependent
public class CsvViewController extends AbstractTableViewController implements BindingsSource {

    @FXML
    TextField uriText;
    
    @FXML
    Button loadCsvAction;

    @FXML
    TableView<Integer> tableView;

    @Override
    protected TableView<Integer> getTableView() {
        return tableView;
    }

    @Inject
    Instance<LabelAdapter<?>> labelAdapters;

    private List<BindingSource<?>> bindingSources;

    @Override
    public List<BindingSource<?>> getBindingSources() {
        return bindingSources;
    }

    @Inject
    @Named("mvel")
    ExpressionSupport mvelExpressionSupport;

    @Override
    protected ExpressionSupport getExpressionSupport() {
        return mvelExpressionSupport;
    }

    @FXML
    void initialize() {
        super.initialize();
        bindingSources = List.of(createTableBindingSource());
    }

    private FileChooser fileChooser = new FileChooser();

    private boolean isFileUri(String uri) {
        return uri.startsWith("file:") || (! uri.contains(":"));
    }
    private boolean isFileUri(URI uri) {
        return "file".equals(uri.getScheme());
    }
    private String getFilePath(String uri) {
        if (uri.startsWith("file:")) {
            return URI.create(uri).getPath();
        } else if (! uri.contains(":")) {
            return uri; 
        }
        return null;
    }

    private URI getUri(String uri) {
        return (isFileUri(uri) ? Path.of(getFilePath(uri)).toUri() : URI.create(uri));
    }

    @FXML
    void browseCsvFile() {
        fileChooser.setTitle("Select csv file");
        if (isFileUri(uriText.getText())) {
            String path = getFilePath(uriText.getText());
            if (! path.isBlank()) {
                fileChooser.setInitialDirectory(new File(path).getParentFile());
            }
        }
        File selectedFile = fileChooser.showOpenDialog(null);
        if (selectedFile != null) {
            uriText.setText(selectedFile.getAbsolutePath());
            loadCsvAction.fire();
        }
    }

    @Inject
    Logger logger;

    private ActionProgressHelper buttonActionProgressHelper = new ActionProgressHelper();

    private Alert alert = null;

    @FXML
    void loadCsv(ActionEvent event) {
        var uri = getUri(uriText.getText());
        buttonActionProgressHelper.performAction(
            event, () -> {
                Supplier<InputStream> input = () -> {
                    try {
                        return uri.toURL().openStream();
                    }
                    catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                };
                return tableFromCsv(Path.of(uri).getFileName().toString(), input);
            },
            this::setTable,
            exception -> {
                if (alert == null) {
                    alert = new Alert(Alert.AlertType.ERROR);
                }
                alert.setTitle("Exception when loading Csv file");
                alert.setHeaderText(exception.getClass().getSimpleName());
                alert.setContentText(exception.getMessage());
                alert.showAndWait();        
            }
        );
    }

    private Table tableFromCsv(final String name, final Supplier<InputStream> input) throws IOException {
        char separator = ',';
        try (InputStream inputStream = input.get()) {
            separator = guessSeparator(inputStream, "\t;,", 5);
        }
        try (InputStream inputStream = input.get()) {
            CsvReadOptions options = CsvReadOptions.builder(inputStream)
                    .tableName(name)
                    .separator(separator)
                    .build();
            final Table table = Table.read().csv(options);
            return table;
        }
    }

    private char guessSeparator(final InputStream input, final String candidates, final int lineCount) throws IOException {
        final List<String> lines = new ArrayList<>(lineCount);
        try (final BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(input))) {
            String line = null;
            while ((line = bufferedReader.readLine()) != null) {
                lines.add(line);
            }
        }
        final int[][] counts = new int[candidates.length()][lines.size()];
        for (int lineNum = 0; lineNum < lines.size(); lineNum++) {
            final String s = lines.get(lineNum);
            if (s != null) {
                for (int i = 0; i < s.length(); i++) {
                    final int pos = candidates.indexOf(s.charAt(i));
                    if (pos >= 0) {
                        counts[pos][lineNum]++;
                    }
                }
            }
        }
        int best = 0;
        outer: for (int cand = 0; cand < counts.length; cand++) {
            final int firstCount = counts[cand][0];
            if (firstCount < 1) {
                continue;
            }
            for (int lineNum = 1; lineNum < lines.size(); lineNum++) {
                if (counts[cand][lineNum] != firstCount) {
                    continue outer;
                }
            }
            best = cand;
        }
        return candidates.charAt(best);
    }
}

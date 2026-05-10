package edu.cs6103.chinesename.ui;

import edu.cs6103.chinesename.db.CharacterRepository;
import edu.cs6103.chinesename.db.DatabaseManager;
import edu.cs6103.chinesename.model.NameCandidate;
import edu.cs6103.chinesename.service.EtlImporter;
import edu.cs6103.chinesename.service.NameGenerationService;
import edu.cs6103.chinesename.service.PhoneticMapper;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.RadioButton;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextField;
import javafx.scene.control.ToggleGroup;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.util.List;
import java.util.Locale;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class MainApp extends Application {
    private final ExecutorService executor = Executors.newFixedThreadPool(2);

    @Override
    public void start(Stage stage) {
        DatabaseManager db = new DatabaseManager("jdbc:sqlite:chinesename.db");
        db.initSchema();
        CharacterRepository repository = new CharacterRepository(db);
        new EtlImporter(repository).importSampleData();
        NameGenerationService service = new NameGenerationService(new PhoneticMapper());

        TextField firstNameField = new TextField();
        firstNameField.setPromptText("e.g. Elizabeth");
        TextField middleNameField = new TextField();
        TextField lastNameField = new TextField();
        lastNameField.setPromptText("e.g. Smith");
        ComboBox<Integer> resultCountBox = new ComboBox<>();
        resultCountBox.getItems().addAll(3, 5, 10);
        resultCountBox.setValue(3);

        ComboBox<String> genderBox = new ComboBox<>();
        genderBox.getItems().addAll("Neutral", "Male", "Female");
        genderBox.setValue("Neutral");

        ToggleGroup styleGroup = new ToggleGroup();
        VBox styleRadios = new VBox(4);
        String[] styleIds = {"elegant", "scholarly", "resilient", "modern", "bold", "gentle", "classic"};
        for (String id : styleIds) {
            RadioButton rb = new RadioButton(id);
            rb.setToggleGroup(styleGroup);
            styleRadios.getChildren().add(rb);
        }
        ((RadioButton) styleGroup.getToggles().get(0)).setSelected(true);

        Button generateButton = new Button("Generate");
        ScrollPane resultScroll = new ScrollPane();
        resultScroll.setFitToWidth(true);
        VBox resultBox = new VBox(10);
        resultBox.setPadding(new Insets(0, 8, 8, 0));
        resultScroll.setContent(resultBox);
        Label status = new Label("Ready");

        generateButton.setOnAction(evt -> {
            if (!hasAnyNameInput(firstNameField, middleNameField, lastNameField)) {
                status.setText("Please enter at least first name, middle name, or last name.");
                return;
            }
            generateButton.setDisable(true);
            status.setText("Generating...");
            Task<List<NameCandidate>> task = new Task<>() {
                @Override
                protected List<NameCandidate> call() {
                    return service.generate(
                            firstNameField.getText(),
                            middleNameField.getText(),
                            lastNameField.getText(),
                            selectedStyle(styleGroup),
                            genderBox.getValue(),
                            repository.findAll(),
                            resultCountBox.getValue()
                    );
                }
            };
            task.setOnSucceeded(done -> {
                List<NameCandidate> candidates = task.getValue();
                Platform.runLater(() -> {
                    resultBox.getChildren().clear();
                    for (NameCandidate c : candidates) {
                        Label headline = new Label(String.format(Locale.ROOT, "%s  ·  Pinyin: %s",
                                c.fullName(), c.pinyin()));
                        headline.setStyle("-fx-font-weight: bold;");
                        headline.setMaxWidth(Double.MAX_VALUE);
                        headline.setWrapText(true);
                        Label detail = new Label(shortenForDisplay(c.explanation(), 400));
                        detail.setWrapText(true);
                        detail.setMaxWidth(Double.MAX_VALUE);
                        VBox row = new VBox(4, headline, detail);
                        row.setMaxWidth(Double.MAX_VALUE);
                        resultBox.getChildren().add(row);
                    }
                    status.setText("Generated " + candidates.size() + " candidates");
                    generateButton.setDisable(false);
                });
            });
            task.setOnFailed(failed -> Platform.runLater(() -> {
                status.setText("Error: " + task.getException().getMessage());
                generateButton.setDisable(false);
            }));
            executor.submit(task);
        });

        VBox controls = new VBox(8,
                new Label("First name"), firstNameField,
                new Label("Middle name (optional)"), middleNameField,
                new Label("Last name (English family name)"), lastNameField,
                new Label("Result Count"), resultCountBox,
                new Label("Gender Preference"), genderBox,
                new Label("Style preference"), styleRadios,
                generateButton, status
        );
        controls.setPadding(new Insets(12));

        BorderPane root = new BorderPane();
        root.setLeft(controls);
        root.setCenter(resultScroll);
        BorderPane.setMargin(resultScroll, new Insets(12));

        stage.setTitle("ChineseNamev1");
        stage.setScene(new Scene(root, 900, 500));
        stage.show();
    }

    private String selectedStyle(ToggleGroup styleGroup) {
        var selected = styleGroup.getSelectedToggle();
        if (selected == null) {
            return "";
        }
        return ((RadioButton) selected).getText().trim().toLowerCase(Locale.ROOT);
    }

    private static boolean hasAnyNameInput(TextField first, TextField middle, TextField last) {
        return !trimOrEmpty(first).isEmpty()
                || !trimOrEmpty(middle).isEmpty()
                || !trimOrEmpty(last).isEmpty();
    }

    private static String trimOrEmpty(TextField field) {
        if (field == null || field.getText() == null) {
            return "";
        }
        return field.getText().trim();
    }

    private static String shortenForDisplay(String explanation, int maxChars) {
        if (explanation == null || explanation.isBlank()) {
            return "";
        }
        String s = explanation.trim();
        if (s.length() <= maxChars) {
            return s;
        }
        return s.substring(0, maxChars - 1).trim() + "…";
    }

    @Override
    public void stop() {
        executor.shutdownNow();
    }

    public static void main(String[] args) {
        launch(args);
    }
}

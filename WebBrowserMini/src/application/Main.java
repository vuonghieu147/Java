package application;

import java.util.ArrayList;
import java.util.List;
import javafx.application.Application;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Worker;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebHistory;
import javafx.scene.web.WebView;
import javafx.stage.Stage;

public class Main extends Application {
    private WebView webView;
    private WebEngine webEngine;
    private TextField urlField;
    private Button backButton;
    private Button reloadButton;
    private Button historyButton;
    private ListView<String> historyList;
    private ObservableList<String> historyItems;
    private List<String> urls;

    @Override
    public void start(Stage primaryStage) {
        BorderPane root = new BorderPane();
        primaryStage.setTitle("Mini Browser");
        // Create WebView and WebEngine
        webView = new WebView();
        webEngine = webView.getEngine();
        webEngine.setJavaScriptEnabled(true);
        webEngine.getLoadWorker().stateProperty().addListener(
            (obs, oldValue, newValue) -> {
                if (newValue == Worker.State.SUCCEEDED) {
                    // Update URL field and back button
                    urlField.setText(webEngine.getLocation());
                    backButton.setDisable(!(webEngine.getHistory().getCurrentIndex() > 0));

                    // Update history list
                    String url = webEngine.getLocation();
                    if (!urls.contains(url)) {
                        urls.add(url);
                        historyItems.add(url);
                    }
                }
            });

        // Create URL field
        urlField = new TextField();
        urlField.setOnAction(e -> loadURL(urlField.getText()));
        urlField.setPrefWidth(600);
        urlField.setPromptText("Enter URL here");

        // Create back button
        backButton = new Button("Back");
        backButton.setDisable(true);
        backButton.setOnAction(e -> {
            WebHistory history = webEngine.getHistory();
            int currentIndex = history.getCurrentIndex();
            if (currentIndex > 0) {
                history.go(-1);
            }
        });

        // Create reload button
        reloadButton = new Button("Reload");
        reloadButton.setOnAction(e -> webEngine.reload());

        // Create history button
        historyButton = new Button("History");
        historyButton.setOnAction(e -> {
            BorderPane historyRoot = new BorderPane();
            historyList = new ListView<String>();
            historyList.setItems(historyItems);
            historyList.getSelectionModel().selectedItemProperty().addListener(
                (obs, oldVal, newVal) -> {
                    if (newVal != null) {
                        loadURL(newVal);
                    }
                });
            historyRoot.setCenter(historyList);
            Scene historyScene = new Scene(historyRoot, 400, 600);
            Stage historyStage = new Stage();
            historyStage.setScene(historyScene);
            historyStage.setTitle("History");
            historyStage.show();
        });
        
        //Create forward button
        Button forwardButton = new Button("Forward");
        forwardButton.setOnAction(e -> {
            WebHistory history = webEngine.getHistory();
            int currentIndex = history.getCurrentIndex();
            int size = history.getEntries().size();
            if (currentIndex + 1 < size) {
                history.go(1);
            }
        });


        // Create controls bar
        BorderPane controlsBar = new BorderPane();
        controlsBar.setLeft(backButton);
        controlsBar.setCenter(urlField);
        HBox rightBox = new HBox(forwardButton,reloadButton, historyButton);
        controlsBar.setRight(rightBox);

        

        
        //Create google button
        Button googleButton = new Button("Google");
        googleButton.setOnAction(e -> {
            loadURL("https://www.google.com");
        });
        controlsBar.setBottom(googleButton);
        


        // Create scene and set to stage
        //Scene scene = new Scene(root, 800, 600);
        Scene scene = new Scene(root, 1280, 960);
        primaryStage.setScene(scene);
        primaryStage.show();
        
        // Add components to root pane
        root.setTop(controlsBar);
        root.setCenter(webView);
        

        // Initialize history items
        urls = new ArrayList<String>();
        historyItems = FXCollections.observableArrayList();
    }

    private void loadURL(String url) {
        if (!url.startsWith("http")) {
            url ="http://" + url;
        }
        webEngine.load(url);
        }
    public static void main(String[] args) {
        launch(args);
    }
}

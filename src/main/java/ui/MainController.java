package ui;

import core.ItemsLoader;
import core.ItemsSeeker;
import core.Logger;
import core.Result;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;

import java.io.IOException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.util.*;

public class MainController implements Initializable, Logger, ItemsSeeker.ResultsLoadingListener, ItemsLoader.ItemsLoadingListener {

    @FXML private TextArea inputTa;
    @FXML private TextArea consoleTa;
    @FXML private Button searchingBtn;
    @FXML private Button extractionBtn;
    @FXML private Button stopBtn;
    @FXML private Button clearBtn;
    @FXML private ComboBox<String> conditionCb;
    @FXML private Spinner<Integer> maxThreadsSpn;
    @FXML private TextField itemsLimitTf;
    @FXML private TextField categoryTf;


    @FXML private TableView<Result> table;
    @FXML private TableColumn<Result, String> queryCol;
    @FXML private TableColumn<Result, String> isSuccessCol;
    @FXML private TableColumn<Result, String> itemsProgCol;
    @FXML private TableColumn<Result, Integer> totalEntriesCol;
    @FXML private TableColumn<Result, Integer> itemsCountCol;
    @FXML private TableColumn<Result, Double> avgPriceCol;
    @FXML private TableColumn<Result, Integer> soldCountCol;
    @FXML private TableColumn<Result, Double> avgPurchasePriceCol;
    private TableContextMenu tableContextMenu;

    private ObservableList<Result> results = FXCollections.observableArrayList();
    private Set<String> resultsSet = new HashSet<>();


    private ItemsSeeker itemsSeeker;
    private ItemsLoader itemsLoader;
    private String appName;


    @Override
    public void initialize(URL location, ResourceBundle resources) {
        try {
            appName = Files.readAllLines(Paths.get("key.txt")).get(0);
        } catch (IOException e) {
            log("Unable to read app name");
        }

        queryCol.setCellValueFactory(new PropertyValueFactory<>("query"));
        isSuccessCol.setCellValueFactory(new PropertyValueFactory<>("statusString"));
        itemsProgCol.setCellValueFactory(new PropertyValueFactory<>("progressString"));
        totalEntriesCol.setCellValueFactory(new PropertyValueFactory<>("totalEntries"));
        itemsCountCol.setCellValueFactory(new PropertyValueFactory<>("itemsCount"));
        avgPriceCol.setCellValueFactory(new PropertyValueFactory<>("avgPrice"));
        soldCountCol.setCellValueFactory(new PropertyValueFactory<>("soldCount"));
        avgPurchasePriceCol.setCellValueFactory(new PropertyValueFactory<>("avgPurchasePrice"));

        queryCol.prefWidthProperty().bind(table.widthProperty().multiply(0.3));
        isSuccessCol.prefWidthProperty().bind(table.widthProperty().multiply(0.1));
        itemsProgCol.prefWidthProperty().bind(table.widthProperty().multiply(0.1));
        totalEntriesCol.prefWidthProperty().bind(table.widthProperty().multiply(0.1));
        itemsCountCol.prefWidthProperty().bind(table.widthProperty().multiply(0.1));
        avgPriceCol.prefWidthProperty().bind(table.widthProperty().multiply(0.1));
        soldCountCol.prefWidthProperty().bind(table.widthProperty().multiply(0.1));
        avgPurchasePriceCol.prefWidthProperty().bind(table.widthProperty().multiply(0.1));
        table.setItems(results);
        tableContextMenu = new TableContextMenu(table);

        searchingBtn.setTooltip(new Tooltip("Start searching for items"));
        extractionBtn.setTooltip(new Tooltip("Start detailed items information extraction"));
        clearBtn.setTooltip(new Tooltip("Clear all results"));
        maxThreadsSpn.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(1, 10, 4));

        conditionCb.setItems(FXCollections.observableArrayList("All", "New", "Used"));
        conditionCb.setValue("All");

        searchingBtn.setDisable(false);
        extractionBtn.setDisable(true);
        stopBtn.setDisable(true);
    }


    @FXML
    private void startSearching() {
        clearAll();

        if (inputTa.getText() == null || inputTa.getText().isEmpty()) {
            showAlert("Error", "Queries not specified");
            return;
        }
        List<String> queries = Arrays.asList(inputTa.getText().split("\\r?\\n"));
        itemsSeeker = new ItemsSeeker(queries, appName, getCondition(), this);
        itemsSeeker.setLogger(this);
        itemsSeeker.setMaxThreads(maxThreadsSpn.getValue());
        //Items limit
        try {
            if (itemsLimitTf.getText() != null && itemsLimitTf.getText().length() > 0)
                itemsSeeker.setItemsLimit(Integer.parseInt(itemsLimitTf.getText()));
        } catch (NumberFormatException e) {
            showAlert("Error", "Incorrect items limit!");
            return;
        }
        //Category
        if (categoryTf.getText() != null && categoryTf.getText().length() > 0)
            itemsSeeker.setCategoryId(categoryTf.getText());

        log("--- Items searching started ---");
        stopBtn.setDisable(false);
        searchingBtn.setDisable(true);
        itemsSeeker.start();
    }

    @FXML
    private void startDetailedExtraction() {
        itemsLoader = new ItemsLoader(itemsSeeker.getAllItems(), appName, this);
        itemsLoader.setLogger(this);
        searchingBtn.setDisable(true);
        extractionBtn.setDisable(true);
        stopBtn.setDisable(false);
        log("--- Detailed information extraction started ---");
        itemsLoader.start();
    }

    @FXML
    private void stop() {
        if (itemsSeeker != null && itemsSeeker.isRunning()) itemsSeeker.stop();
        if (itemsLoader != null && itemsLoader.isRunning()) itemsLoader.stop();
    }

    @FXML
    private void clearAll() {
        stop();
        resultsSet.clear();
        results.clear();
        table.refresh();
        stopBtn.setDisable(true);
        searchingBtn.setDisable(false);
        extractionBtn.setDisable(true);
    }

    @Override
    public void onItemReceived() {
        table.refresh();
    }

    @Override
    public void onAllItemsReceived() {
        log("--- Detailed information extraction is completed ---");
        searchingBtn.setDisable(true);
        extractionBtn.setDisable(true);
        stopBtn.setDisable(true);
    }

    @Override
    public void onResultReceived(Result result) {
        if (!resultsSet.contains(result.getQuery())) {
            resultsSet.add(result.getQuery());
            results.add(result);
        }
        table.refresh();
    }

    @Override
    public void onAllResultsReceived() {
        log("--- Items searching completed ---");
        stopBtn.setDisable(true);
        searchingBtn.setDisable(false);
        extractionBtn.setDisable(false);
    }

    @Override
    public void log(String message) {
        String curTime = new SimpleDateFormat("HH:mm:ss").format(new Date());
        Platform.runLater(() -> {
            consoleTa.setText(consoleTa.getText() + curTime + ": " + message +"\n");
            consoleTa.positionCaret(consoleTa.getLength());
        });
    }

    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setHeaderText(null);
        alert.setGraphic(null);
        alert.setTitle(title);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private ItemsSeeker.Condition getCondition(){
        if (conditionCb.getValue().equals("New")) return ItemsSeeker.Condition.NEW;
        if (conditionCb.getValue().equals("Used")) return ItemsSeeker.Condition.USED;
        return ItemsSeeker.Condition.ALL;
    }
}

package ui;

import core.*;
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

public class MainController implements Initializable, Logger, ItemsSeeker.ResultsLoadingListener {

    @FXML private TextArea inputTa;
    @FXML private TextArea consoleTa;
    @FXML private Button searchingBtn;
    @FXML private Button stopBtn;
    @FXML private Button clearBtn;
    @FXML private ComboBox<String> conditionCb;
    @FXML private Spinner<Integer> maxThreadsSpn;
    @FXML private TextField itemsLimitTf;
    @FXML private TextField categoryNameTf;
    @FXML private TextField categoryIdTf;
    @FXML private ComboBox<String> categoryCb;
    @FXML private Button subcategoryBtn;
    @FXML private Button parentCategoryBtn;



    @FXML private TableView<Result> table;
    @FXML private TableColumn<Result, String> queryCol;
    @FXML private TableColumn<Result, String> statusCol;
    @FXML private TableColumn<Result, Integer> itemsFoundCol;
    @FXML private TableColumn<Result, Integer> itemsListedCol;
    @FXML private TableColumn<Result, Double> avgPriceListedCol;
    @FXML private TableColumn<Result, Integer> itemsSoldCol;
    @FXML private TableColumn<Result, Double> avgPriceSoldCol;
    @FXML private TableColumn<Result, String> soldRatioCol;
    private TableContextMenu tableContextMenu;

    private ObservableList<Result> results = FXCollections.observableArrayList();
    private Set<String> resultsSet = new HashSet<>();


    private ItemsSeeker itemsSeeker;
    private String appName;
    private Category category;


    @Override
    public void initialize(URL location, ResourceBundle resources) {
        try {
            appName = Files.readAllLines(Paths.get("key.txt")).get(0);
        } catch (IOException e) {
            log("Unable to read app name");
        }
        Category.setAppName(appName);
        selectCategory("-1");

        queryCol.setCellValueFactory(new PropertyValueFactory<>("query"));
        statusCol.setCellValueFactory(new PropertyValueFactory<>("statusString"));
        itemsFoundCol.setCellValueFactory(new PropertyValueFactory<>("itemsCount"));
        itemsListedCol.setCellValueFactory(new PropertyValueFactory<>("activeItemsCount"));
        avgPriceListedCol.setCellValueFactory(new PropertyValueFactory<>("avgPriceListed"));
        itemsSoldCol.setCellValueFactory(new PropertyValueFactory<>("soldItemsCount"));
        avgPriceSoldCol.setCellValueFactory(new PropertyValueFactory<>("avgPriceSold"));
        soldRatioCol.setCellValueFactory(new PropertyValueFactory<>("soldRatio"));

        queryCol.prefWidthProperty().bind(table.widthProperty().multiply(0.3));
        statusCol.prefWidthProperty().bind(table.widthProperty().multiply(0.1));
        itemsFoundCol.prefWidthProperty().bind(table.widthProperty().multiply(0.1));
        itemsListedCol.prefWidthProperty().bind(table.widthProperty().multiply(0.1));
        avgPriceListedCol.prefWidthProperty().bind(table.widthProperty().multiply(0.1));
        soldRatioCol.prefWidthProperty().bind(table.widthProperty().multiply(0.1));
        itemsSoldCol.prefWidthProperty().bind(table.widthProperty().multiply(0.1));
        avgPriceSoldCol.prefWidthProperty().bind(table.widthProperty().multiply(0.1));
        table.setItems(results);
        tableContextMenu = new TableContextMenu(table);

        searchingBtn.setTooltip(new Tooltip("Start searching for items"));
        clearBtn.setTooltip(new Tooltip("Clear all results"));
        parentCategoryBtn.setTooltip(new Tooltip("Select parent category"));
        subcategoryBtn.setTooltip(new Tooltip("Select subcategory"));

        maxThreadsSpn.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(1, 10, 4));

        conditionCb.setItems(FXCollections.observableArrayList("All", "New", "Used"));
        conditionCb.setValue("All");

        searchingBtn.setDisable(false);
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
        if (categoryIdTf.getText() != null && categoryIdTf.getText().length() > 0)
            itemsSeeker.setCategoryId(categoryIdTf.getText());

        log("--- Items searching started ---");
        stopBtn.setDisable(false);
        searchingBtn.setDisable(true);
        itemsSeeker.start();
    }

    @FXML
    private void stop() {
        if (itemsSeeker != null && itemsSeeker.isRunning()) itemsSeeker.stop();
    }

    @FXML
    private void clearAll() {
        stop();
        resultsSet.clear();
        results.clear();
        table.refresh();
        stopBtn.setDisable(true);
        searchingBtn.setDisable(false);
    }

    @FXML
    private void selectSubcategory() {
        String categoryId = category.getChildren().get(categoryCb.getValue());
        if (categoryId != null) selectCategory(categoryId);
    }

    @FXML
    private void selectParentCategory() {
        if (category.getParentId() != null && !category.getParentId().equals("0")) selectCategory(category.getParentId());
    }

    private void selectCategory(String categoryId) {
        category = Category.findById(categoryId);
        if (category == null) {
            categoryIdTf.setText("-1");
            return;
        }
        categoryIdTf.setText(categoryId);
        categoryNameTf.setText(category.getName());
        categoryCb.getItems().clear();
        categoryCb.getItems().addAll(category.getChildren().keySet());
        if (!categoryCb.getItems().isEmpty()) categoryCb.setValue(categoryCb.getItems().get(0));
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

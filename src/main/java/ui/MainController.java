package ui;

import core.ItemsLoader;
import core.ItemsSeeker;
import core.Logger;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.TextArea;

import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.ResourceBundle;

public class MainController implements Initializable, Logger, ItemsSeeker.ResultsLoadingListener, ItemsLoader.ItemsLoadingListener {

    @FXML private TextArea inputTa;
    @FXML private TextArea consoleTa;


    @Override
    public void initialize(URL location, ResourceBundle resources) {

    }






    @Override
    public void onItemReceived() {

    }

    @Override
    public void onAllItemsReceived() {

    }

    @Override
    public void onResultReceived(String query) {

    }

    @Override
    public void onAllResultsReceived() {

    }

    @Override
    public void log(String message) {
        String curTime = new SimpleDateFormat("HH:mm:ss").format(new Date());
        Platform.runLater(() -> {
            consoleTa.setText(consoleTa.getText() + curTime + ": " + message +"\n");
            consoleTa.positionCaret(consoleTa.getLength());
        });
    }


}

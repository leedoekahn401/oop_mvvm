package project.app.humanelogistics.controller;


import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.layout.HBox;
import javafx.scene.text.Text;
import javafx.scene.layout.VBox;
import javafx.scene.image.*;

public class InformationButtonController {
    @FXML
    private Button informationButton;
    @FXML
    private VBox centerBox;
    @FXML
    public void initialize()
    {
        informationButton.setOnAction(event -> showInfo());
    }

    public void showInfo()
    {


    }


}

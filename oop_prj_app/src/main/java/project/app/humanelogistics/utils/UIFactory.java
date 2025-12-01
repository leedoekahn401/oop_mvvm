package project.app.humanelogistics.util;

import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.File;

public class UIFactory {

    public static VBox createMemberCard(String name, String role, String imagePath) {
        VBox card = new VBox(10);
        card.getStyleClass().add("card");
        card.setMaxWidth(300);

        ImageView img = new ImageView();
        try {
            img.setImage(new Image(UIFactory.class.getResourceAsStream(imagePath)));
        } catch (Exception e) {
            System.err.println("Warning: Image not found at " + imagePath);
        }
        img.setFitHeight(150);
        img.setFitWidth(150);

        Text nameTxt = new Text(name);
        nameTxt.setStyle("-fx-font-size: 18px; -fx-font-weight: bold;");

        Text roleTxt = new Text(role);
        roleTxt.setStyle("-fx-fill: #7f8c8d;");

        card.getChildren().addAll(img, nameTxt, roleTxt);
        return card;
    }

    public static VBox createChartContainer(String titleText, File chartFile) {
        ImageView chartView = new ImageView();
        try {
            chartView.setImage(new Image(new FileInputStream(chartFile)));
            chartView.setPreserveRatio(true);
            chartView.setFitWidth(800);
        } catch (FileNotFoundException e) {
            return createErrorBox("Could not load chart image.");
        }

        Text title = new Text(titleText);
        title.getStyleClass().add("section-header");

        VBox container = new VBox(20, title, chartView);
        container.setStyle("-fx-background-color: white; -fx-background-radius: 12; -fx-padding: 30; -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.1), 10, 0, 0, 2);");
        return container;
    }

    public static Text createLoadingText(String message) {
        Text loading = new Text(message);
        loading.setStyle("-fx-font-size: 18px; -fx-fill: #7f8c8d;");
        return loading;
    }

    public static VBox createErrorBox(String errorMessage) {
        Text errorText = new Text("Error: " + errorMessage);
        errorText.setStyle("-fx-fill: red; -fx-font-size: 14px;");
        return new VBox(errorText);
    }

    public static Text createSectionHeader(String text) {
        Text header = new Text(text);
        header.getStyleClass().add("section-header");
        return header;
    }
}
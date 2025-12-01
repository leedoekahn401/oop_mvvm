package project.app.humanelogistics.controller;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.VBox;
import project.app.humanelogistics.model.Developer;
import project.app.humanelogistics.utils.UIFactory;
import project.app.humanelogistics.viewmodel.DashboardViewModel;

import java.io.File;
import java.util.Arrays;
import java.util.List;

public class DashBoardController {

    @FXML private VBox mainContent;
    @FXML private ImageView imgLogo;
    @FXML private Button informationButton;
    @FXML private Button sentimentButton;
    @FXML private Button inventoryButton;
    @FXML private Button homeButton;
    @FXML private Label lblTotalPosts;
    @FXML private Label lblSentimentScore;
    @FXML private Label lblSentimentLabel;
    @FXML private Label lblTopDamage;
    @FXML private Label lblTopDamageCount;

    private DashboardViewModel viewModel;

    // This list holds the "Cards" (Dashboard Widgets) so we can restore them later
    private ObservableList<Node> defaultDashboardContent;

    private static final String CHART_FILE_PATH = "sentiment_score_chart.png";
    private static final String DAMAGE_PIE_PATH = "damage_type_pie.png";
    private static final String DAMAGE_BAR_PATH = "damage_type_bar.png";

    @FXML
    public void initialize() {
        this.viewModel = new DashboardViewModel();
        loadLogo();

        // 1. Capture the Default Content (The Dashboard Cards defined in FXML)
        if (mainContent != null) {
            this.defaultDashboardContent = FXCollections.observableArrayList(mainContent.getChildren());
        } else {
            this.defaultDashboardContent = FXCollections.emptyObservableList();
        }

        // 2. Bind UI Elements to ViewModel
        if (lblTotalPosts != null) lblTotalPosts.textProperty().bind(viewModel.totalPostsProperty());
        if (lblSentimentScore != null) lblSentimentScore.textProperty().bind(viewModel.sentimentScoreProperty());
        if (lblTopDamage != null) lblTopDamage.textProperty().bind(viewModel.topDamageProperty());
        if (lblTopDamageCount != null) lblTopDamageCount.textProperty().bind(viewModel.topDamageCountProperty());

        if (lblSentimentLabel != null) {
            lblSentimentLabel.textProperty().bind(viewModel.sentimentLabelProperty());
            lblSentimentLabel.styleProperty().bind(viewModel.sentimentStyleProperty());
        }

        // 3. Listen for Logic Changes

        // Show loading text if status changes (but ignore empty strings)
        viewModel.statusMessageProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null && !newVal.isEmpty()) {
                showLoading(newVal);
            }
        });

        // Show Chart when ViewModel has a file ready
        viewModel.chartFileProperty().addListener((obs, oldVal, newFile) -> {
            if (newFile != null) {
                showChart("Sentiment Analysis Result", newFile);
            }
        });

        // Show Gallery when ViewModel has multiple files ready
        viewModel.galleryFilesProperty().addListener((obs, oldVal, fileList) -> {
            if (fileList != null && !fileList.isEmpty()) {
                showChartGallery("Damage Analysis Report", fileList);
            }
        });

        setupNavigation();
        viewModel.loadDashboardStats();
    }

    // --- Navigation Logic ---

    private void showDefault() {
        // Restore the dashboard cards
        mainContent.getChildren().clear();
        mainContent.getChildren().addAll(defaultDashboardContent);
    }

    private void showLoading(String message) {
        mainContent.getChildren().clear();
        mainContent.getChildren().add(UIFactory.createLoadingText(message));
    }

    private void showChart(String title, File chartFile) {
        VBox chartBox = UIFactory.createChartContainer(title, chartFile);
        mainContent.getChildren().setAll(chartBox);
    }

    private void showChartGallery(String mainTitle, List<File> chartFiles) {
        mainContent.getChildren().clear();
        mainContent.getChildren().add(UIFactory.createSectionHeader(mainTitle));
        for (int i = 0; i < chartFiles.size(); i++) {
            VBox chartBox = UIFactory.createChartContainer("Analysis Chart " + (i + 1), chartFiles.get(i));
            mainContent.getChildren().add(chartBox);
        }
    }

    private void showDevelopers(List<Developer> developers) {
        mainContent.getChildren().clear();
        mainContent.getChildren().add(UIFactory.createSectionHeader("About Developers"));

        for (Developer dev : developers) {
            mainContent.getChildren().add(
                    UIFactory.createMemberCard(dev.getName(), dev.getRole(), dev.getImagePath())
            );
        }
    }

    private void setupNavigation() {
        homeButton.setOnAction(e -> {
            updateActiveButton(homeButton);
            showDefault();
            // Refresh stats to ensure numbers are up to date
            viewModel.loadDashboardStats();
        });

        sentimentButton.setOnAction(e -> {
            updateActiveButton(sentimentButton);
            // Trigger generation in ViewModel
            viewModel.generateSentimentChart(CHART_FILE_PATH);
        });

        inventoryButton.setOnAction(e -> {
            updateActiveButton(inventoryButton);
            // Trigger generation in ViewModel
            viewModel.generateDamageCharts(DAMAGE_PIE_PATH, DAMAGE_BAR_PATH);
        });

        informationButton.setOnAction(e -> {
            updateActiveButton(informationButton);
            List<Developer> devs = Arrays.asList(
                    new Developer("Backend Lead", "Data & Analysis", "/project/app/humanelogistics/picture1.jpg"),
                    new Developer("Frontend Lead", "UI & UX", "/project/app/humanelogistics/picture2.jpg")
            );
            showDevelopers(devs);
        });
    }

    private void loadLogo() {
        try {
            if (getClass().getResource("/project/app/humanelogistics/logo.png") != null) {
                imgLogo.setImage(new Image(getClass().getResourceAsStream("/project/app/humanelogistics/logo.png")));
            }
        } catch (Exception ignored) {}
    }

    private void updateActiveButton(Button clicked) {
        if (homeButton != null) homeButton.getStyleClass().remove("active");
        if (sentimentButton != null) sentimentButton.getStyleClass().remove("active");
        if (informationButton != null) informationButton.getStyleClass().remove("active");
        if (inventoryButton != null) inventoryButton.getStyleClass().remove("active");
        if (clicked != null) clicked.getStyleClass().add("active");
    }
}
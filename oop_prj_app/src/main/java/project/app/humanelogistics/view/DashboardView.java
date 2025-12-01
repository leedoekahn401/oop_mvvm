package project.app.humanelogistics.view;

import javafx.collections.ObservableList;
import javafx.scene.Node;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import project.app.humanelogistics.model.Developer;
import project.app.humanelogistics.util.UIFactory;

import java.io.File;
import java.util.List;

public class DashboardView {

    private final VBox container;
    private final ObservableList<Node> defaultContent;

    public DashboardView(VBox container, ObservableList<Node> defaultContent) {
        this.container = container;
        this.defaultContent = defaultContent;
    }

    public void showDefault() {
        if (defaultContent != null) {
            container.getChildren().setAll(defaultContent);
        }
    }

    public void showLoading(String message) {
        container.getChildren().clear();
        container.getChildren().add(UIFactory.createLoadingText(message));
    }

    public void showError(String message) {
        container.getChildren().setAll(UIFactory.createErrorBox(message));
    }

    public void showChart(String title, File chartFile) {
        VBox chartBox = UIFactory.createChartContainer(title, chartFile);
        container.getChildren().setAll(chartBox);
    }

    // NEW METHOD: Shows two charts vertically
    public void showDualCharts(String mainTitle, File chart1, File chart2) {
        container.getChildren().clear();

        Text header = UIFactory.createSectionHeader(mainTitle);

        VBox box1 = UIFactory.createChartContainer("Distribution Analysis", chart1);
        VBox box2 = UIFactory.createChartContainer("Frequency Analysis", chart2);

        // Add all to the main container
        container.getChildren().addAll(header, box1, box2);
    }

    // Handles any number of charts dynamically
    public void showChartGallery(String mainTitle, List<File> chartFiles) {
        container.getChildren().clear();

        Text header = UIFactory.createSectionHeader(mainTitle);
        container.getChildren().add(header);

        for (int i = 0; i < chartFiles.size(); i++) {
            // Create a title like "Chart 1", "Chart 2" or handle naming in the file list object wrapper if preferred
            String subTitle = "Analysis Chart " + (i + 1);
            VBox chartBox = UIFactory.createChartContainer(subTitle, chartFiles.get(i));
            container.getChildren().add(chartBox);
        }
    }

    public void showDevelopers(List<Developer> developers) {
        container.getChildren().clear();
        container.getChildren().add(UIFactory.createSectionHeader("About Developers"));

        for (Developer dev : developers) {
            container.getChildren().add(
                    UIFactory.createMemberCard(dev.getName(), dev.getRole(), dev.getImagePath())
            );
        }
    }
}
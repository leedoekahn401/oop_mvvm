module project.app.humanelogistics {
    requires javafx.controls;
    requires javafx.fxml;
    requires java.desktop;
    requires javafx.graphics;
    requires org.jfree.jfreechart;
    requires org.mongodb.driver.sync.client;
    requires org.mongodb.bson;
    requires org.mongodb.driver.core;
    requires java.net.http;
    requires org.jsoup;
    requires io.github.cdimascio.dotenv.java;

    requires google.genai;

    opens project.app.humanelogistics to javafx.fxml;
    exports project.app.humanelogistics;

    exports project.app.humanelogistics.controller;
    opens project.app.humanelogistics.controller to javafx.fxml;

    exports project.app.humanelogistics.model;
    opens project.app.humanelogistics.model to javafx.fxml;

    exports project.app.humanelogistics.preprocessing;
    opens project.app.humanelogistics.preprocessing to javafx.fxml;

    exports project.app.humanelogistics.db;
    opens project.app.humanelogistics.db to javafx.fxml;
}
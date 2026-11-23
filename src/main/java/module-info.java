module com.recipebook {
    requires javafx.controls;
    requires javafx.fxml;
    requires java.logging;
    requires com.fasterxml.jackson.databind;
    requires jakarta.xml.bind;

    exports com.recipebook;
    exports com.recipebook.model;
    exports com.recipebook.repository;
    exports com.recipebook.service;
    exports com.recipebook.controller;

    opens com.recipebook to javafx.fxml;
    opens com.recipebook.model to javafx.base, com.fasterxml.jackson.databind, jakarta.xml.bind;
    opens com.recipebook.repository to javafx.base, com.fasterxml.jackson.databind, jakarta.xml.bind;
    opens com.recipebook.controller to javafx.fxml;
}
package com.jolly_hotdogs.jollyhotdog;

import javafx.application.Application;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

public class Main extends Application {

    // UI components
    private TableView<Item> inventoryTable;
    private ComboBox<String> filterTypeComboBox, typeComboBox;
    private TextField nameField, quantityField, priceField;
    private Label statusLabel, searchLabel;

    // inventory data
    private final ObservableList<Item> inventoryData = FXCollections.observableArrayList();
    private final List<Item> inventoryList = new ArrayList<>();
    private String searchText = "";

    // file handling
    private File inventoryFile;

    @Override
    public void start(Stage stage) {
        // create UI components
        Label titleLabel = new Label("Jolly Hotdogs POS");
        titleLabel.setStyle("-fx-font-size: 24px; -fx-font-weight: bold;");
        titleLabel.setAlignment(Pos.CENTER);

        HBox filterBox = new HBox();
        filterBox.setSpacing(10);
        filterBox.setAlignment(Pos.CENTER_LEFT);

        filterTypeComboBox = new ComboBox<>();
        filterTypeComboBox.setValue("All");
        filterTypeComboBox.setPromptText("Filter by Type");
        filterTypeComboBox.getItems().addAll("All", "Drink", "Sides", "Mini Dog", "Hotdog", "Hotdog Sandwich");
        filterTypeComboBox.setOnAction(event -> filterInventory());

        TextField searchField = new TextField();
        searchField.setPromptText("Search by Name");
        searchField.setOnKeyReleased(event -> setSearchGlobal(searchField));

        Button removeButton = new Button("Remove Selected");
        removeButton.setOnAction(event -> removeItem());
        removeButton.setAlignment(Pos.CENTER_RIGHT);

        Button increaseButton = new Button("Add Selected");
        increaseButton.setOnAction(event -> increaseItem());
        increaseButton.setAlignment(Pos.CENTER_RIGHT);

        Button priceButton = new Button("Set Price Selected");
        priceButton.setOnAction(event -> changePrice());
        priceButton.setAlignment(Pos.CENTER_RIGHT);

        searchLabel = new Label();
        searchLabel.setStyle("-fx-font-size: 12px;");
        searchLabel.setAlignment(Pos.CENTER_LEFT);

        filterBox.getChildren().addAll(filterTypeComboBox, searchField, removeButton, increaseButton, priceButton);

        inventoryTable = new TableView<>();
        inventoryTable.setColumnResizePolicy(TableView.UNCONSTRAINED_RESIZE_POLICY);
        inventoryTable.getColumns().forEach(column -> {
            column.setResizable(false);
            column.setMinWidth(50);
        });
        inventoryTable.setEditable(false);

        TableColumn<Item, String> nameColumn = new TableColumn<>("Name");
        nameColumn.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getName()));

        TableColumn<Item, String> quantityColumn = new TableColumn<>("Quantity");
        quantityColumn.setCellValueFactory(cellData -> new SimpleStringProperty(Integer.toString(cellData.getValue().getQuantity())));

        TableColumn<Item, String> typeColumn = new TableColumn<>("Type");
        typeColumn.setCellValueFactory(cellData -> {
            Item item = cellData.getValue();
            return new SimpleStringProperty(cellData.getValue().getType().getDisplayName());
        });

        TableColumn<Item, String> priceColumn = new TableColumn<>("Price");
        priceColumn.setCellValueFactory(cellData -> new SimpleStringProperty(Double.toString(cellData.getValue().getPrice())));

        TableColumn<Item, String> lastTransactionColumn = new TableColumn<>("Last Transaction");
        lastTransactionColumn.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getLastTransaction().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"))));

        nameColumn.prefWidthProperty().bind(inventoryTable.widthProperty().multiply(0.3)); // 60% of the table width
        quantityColumn.prefWidthProperty().bind(inventoryTable.widthProperty().multiply(0.1)); // 40% of the table width
        typeColumn.prefWidthProperty().bind(inventoryTable.widthProperty().multiply(0.2)); // 60% of the table width
        priceColumn.prefWidthProperty().bind(inventoryTable.widthProperty().multiply(0.1)); // 40% of the table width
        lastTransactionColumn.prefWidthProperty().bind(inventoryTable.widthProperty().multiply(0.3)); // 40% of the table width

        inventoryTable.getColumns().addAll(Arrays.asList(nameColumn, quantityColumn, typeColumn, priceColumn, lastTransactionColumn));
        inventoryTable.setItems(inventoryData);

        HBox inputBox = new HBox();
        inputBox.setSpacing(10);
        inputBox.setAlignment(Pos.CENTER);

        nameField = new TextField();
        nameField.setPromptText("Name");

        quantityField = new TextField();
        quantityField.setPromptText("Quantity");

        typeComboBox = new ComboBox<>();
        typeComboBox.setPromptText("Type");
        typeComboBox.getItems().addAll("Drink", "Sides", "Mini Dog", "Hotdog", "Hotdog Sandwich");

        priceField = new TextField();
        priceField.setPromptText("Price");

        Button addButton = new Button("Add New Item");
        addButton.setOnAction(event -> addItem());

        inputBox.getChildren().addAll(nameField, quantityField, typeComboBox, priceField, addButton);

        Button saveButton = new Button("Save");
        saveButton.setOnAction(event -> saveInventory());

        Button importButton = new Button("Import");
        importButton.setOnAction(event -> importInventory(stage));

        Button exportButton = new Button("Export");
        exportButton.setOnAction(event -> exportInventory(stage));

        HBox fileBox = new HBox();
        fileBox.setSpacing(10);
        fileBox.setAlignment(Pos.CENTER_LEFT);
        fileBox.getChildren().addAll(saveButton, importButton, exportButton);

        statusLabel = new Label();
        statusLabel.setStyle("-fx-font-size: 12px;");
        statusLabel.setAlignment(Pos.CENTER);

        VBox root = new VBox();
        root.setSpacing(10);
        root.setPadding(new Insets(10));
        root.setAlignment(Pos.TOP_CENTER);
        root.getChildren().addAll(titleLabel, filterBox, searchLabel, inventoryTable, inputBox, fileBox, statusLabel);

        Scene scene = new Scene(root, 800, 600);
        inventoryTable.prefHeightProperty().bind(scene.heightProperty());

        stage.setScene(scene);
        stage.setTitle("Inventory Manager");
        stage.show();
    }

    private void filterInventory() {
        filterInventoryByType();
    }

    // filters the inventory table based on the type
    private void filterInventoryByType() {
        String filterType = filterTypeComboBox.getValue() == null ? "All" : filterTypeComboBox.getValue();
        ObservableList<Item> filteredData = FXCollections.observableArrayList();

        if (filterType.equals("All") && searchText.isEmpty()) {
            filteredData.addAll(inventoryData);
        }
        else {
            filteredData.addAll(inventoryData.stream()
                    .filter(item -> (filterType.equals("All") || item.getType().toString().equals(filterType.replace(" ", "_").toUpperCase())) &&
                            (searchText.isEmpty() || item.getName().toLowerCase().contains(searchText.toLowerCase())))
                    .toList());

            if (!searchText.isEmpty()) {
                searchLabel.setText("Results for: " + searchText);
            } else {
                searchLabel.setText("");
            }
        }

        inventoryTable.setItems(filteredData);
    }

    private void changePrice() {
        Item selectedItem = inventoryTable.getSelectionModel().getSelectedItem();
        if (selectedItem != null) {
            Dialog<String> dialog = new Dialog<>();
            dialog.setTitle("Change Price");
            dialog.setHeaderText("Enter new price for " + selectedItem.getName());

            ButtonType confirmButton = new ButtonType("Confirm", ButtonBar.ButtonData.OK_DONE);
            dialog.getDialogPane().getButtonTypes().addAll(confirmButton, ButtonType.CANCEL);

            TextField priceField = new TextField(String.valueOf(selectedItem.getPrice()));
            priceField.setPromptText("Price");

            VBox vbox = new VBox();
            vbox.getChildren().addAll(new Label("New Price:"), priceField);
            dialog.getDialogPane().setContent(vbox);

            dialog.setResultConverter(dialogButton -> {
                if (dialogButton == confirmButton) {
                    return priceField.getText();
                }
                return null;
            });

            Optional<String> result = dialog.showAndWait();
            result.ifPresent(price -> {
                try {
                    double newPrice = Double.parseDouble(price);
                    selectedItem.setPrice(newPrice);
                    inventoryTable.refresh();
                    statusLabel.setText("Price for " + selectedItem.getName() + " changed to " + newPrice);
                    selectedItem.setLastTransaction(LocalDateTime.now());
                } catch (NumberFormatException e) {
                    statusLabel.setText("Error: Invalid input for price");
                }
            });
        } else {
            statusLabel.setText("Error: No item selected");
        }
    }


    private void setSearchGlobal(TextField searchField){
        searchText = searchField.getText();
        filterInventory();
    }

    // adds a new item to the inventory
    private void addItem() {
        String name = nameField.getText().trim();
        if (name.isEmpty()) {
            statusLabel.setText("Error: Name cannot be empty");
            return;
        }

        ItemType type = ItemType.valueOf(typeComboBox.getValue().replace(" ", "_").toUpperCase());
        if (type == null) {
            statusLabel.setText("Error: Type cannot be empty");
            return;
        }

        int quantity;
        try {
            quantity = Integer.parseInt(quantityField.getText().trim());
            if (quantity < 1) {
                statusLabel.setText("Error: Quantity must be at least 1");
                return;
            }
        } catch (NumberFormatException e) {
            statusLabel.setText("Error: Invalid quantity");
            return;
        }

        double price;
        try {
            price = Double.parseDouble(priceField.getText().trim());
            if (price < 0) {
                statusLabel.setText("Error: Price cannot be negative");
                return;
            }
        } catch (NumberFormatException e) {
            statusLabel.setText("Error: Invalid price");
            return;
        }

        Item newItem = new Item(name, quantity, type, price, LocalDateTime.now());
        inventoryList.add(newItem);
        inventoryData.add(newItem);
        statusLabel.setText("Added " + name);
        filterInventory();
    }

    // removes an item from the inventory
    private void removeItem() {
        Item selectedItem = inventoryTable.getSelectionModel().getSelectedItem();
        if (selectedItem != null) {
            int selectedQuantity = selectedItem.getQuantity();
            TextInputDialog dialog = new TextInputDialog(String.valueOf(selectedQuantity));
            dialog.setTitle("Remove Item");
            dialog.setHeaderText("Remove " + selectedItem.getName() + " - Quantity: " + selectedQuantity);
            dialog.setContentText("Enter quantity to remove:");

            Optional<String> result = dialog.showAndWait();
            if (result.isPresent()) {
                String input = result.get();
                try {
                    int quantityToRemove = Integer.parseInt(input);
                    if (quantityToRemove > 0 && quantityToRemove <= selectedQuantity) {
                        if (quantityToRemove == selectedQuantity) {
                            inventoryList.remove(selectedItem);
                            inventoryData.remove(selectedItem);
                            inventoryList.addAll(inventoryData);
                            inventoryTable.setItems(inventoryData);
                            statusLabel.setText("Removed " + selectedItem.getName() + " - Quantity: " + selectedQuantity);
                            filterInventory();
                        } else {
                            selectedItem.setQuantity(selectedQuantity - quantityToRemove);
                            selectedItem.setLastTransaction(LocalDateTime.now());
                            inventoryTable.refresh();
                            statusLabel.setText("Removed " + quantityToRemove + " " + selectedItem.getName() + " - Quantity: " + selectedQuantity);
                        }
                    } else {
                        Alert alert = new Alert(Alert.AlertType.WARNING);
                        alert.setTitle("Invalid Quantity");
                        alert.setHeaderText(null);
                        alert.setContentText("Please enter a valid quantity to remove.");
                        alert.showAndWait();
                    }
                } catch (NumberFormatException e) {
                    Alert alert = new Alert(Alert.AlertType.WARNING);
                    alert.setTitle("Invalid Input");
                    alert.setHeaderText(null);
                    alert.setContentText("Please enter a valid number.");
                    alert.showAndWait();
                }
            }
        }
    }

    private void increaseItem() {
        Item selectedItem = inventoryTable.getSelectionModel().getSelectedItem();
        if (selectedItem != null) {
            TextInputDialog dialog = new TextInputDialog();
            dialog.setTitle("Increase Quantity");
            dialog.setHeaderText("Enter quantity to add:");
            Optional<String> result = dialog.showAndWait();
            if (result.isPresent()) {
                int quantityToAdd = Integer.parseInt(result.get());
                int newQuantity = selectedItem.getQuantity() + quantityToAdd;
                selectedItem.setLastTransaction(LocalDateTime.now());
                selectedItem.setQuantity(newQuantity);
                inventoryTable.refresh();
                statusLabel.setText("Increased quantity of " + selectedItem.getName() + " by " + quantityToAdd);
            }
        }
    }

    // saves the inventory to a CSV file
    private void saveInventory() {
        if (inventoryFile != null) {
            try (PrintWriter writer = new PrintWriter(new FileOutputStream(inventoryFile), true, StandardCharsets.UTF_8)) {
                writer.println("Name,Type,Quantity,Price,Last Transaction");
                for (Item item : inventoryList) {
                    writer.println(item.toCsvString());
                }
                statusLabel.setText("Inventory saved to " + inventoryFile.getName());
            } catch (IOException e) {
                statusLabel.setText("Error saving inventory to " + inventoryFile.getName());
            }
        } else {
            statusLabel.setText("No inventory file selected");
        }
    }

    // imports items to the inventory from a CSV file
    private void importInventory(Stage stage) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Select Inventory File");
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("CSV Files", "*.csv"));
        File selectedFile = fileChooser.showOpenDialog(stage);
        if (selectedFile != null) {
            try (Scanner scanner = new Scanner(selectedFile, StandardCharsets.UTF_8)) {
                List<Item> importedItems = new ArrayList<>();
                scanner.nextLine(); // skip header
                while (scanner.hasNextLine()) {
                    String line = scanner.nextLine();
                    String[] parts = line.split(",");
                    String name = parts[0].trim();
                    int quantity = Integer.parseInt(parts[1].trim());
                    ItemType type = ItemType.valueOf(parts[2].trim());
                    double price = Double.parseDouble(parts[3].trim());
                    LocalDateTime lastTransaction = LocalDateTime.parse(parts[4].trim());

                    Item newItem = new Item(name, quantity, type, price, lastTransaction);
                    importedItems.add(newItem);
                }
                inventoryList.addAll(importedItems);
                inventoryData.addAll(importedItems);
                statusLabel.setText("Imported " + importedItems.size() + " items from " + selectedFile.getName());
            } catch (IOException e) {
                statusLabel.setText("Error importing items from " + selectedFile.getName());
            }
        }
        filterInventory();
    }

    // exports the inventory to a CSV file
    private void exportInventory(Stage stage) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Save Inventory File");
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("CSV Files", "*.csv"));
        File selectedFile = fileChooser.showSaveDialog(stage);
        if (selectedFile != null) {
            try (PrintWriter writer = new PrintWriter(new FileOutputStream(selectedFile), true, StandardCharsets.UTF_8)) {
                writer.println("Name,Quantity,Type,Price,Last Transaction");
                for (Item item : inventoryList) {
                    String name = item.getName();
                    ItemType type = item.getType();
                    LocalDateTime lastTransaction = item.getLastTransaction();
                    writer.println(name + "," + item.getQuantity() + "," + type + "," + item.getPrice() + "," + lastTransaction);
                }
                statusLabel.setText("Inventory exported to " + selectedFile.getName());
            } catch (IOException e) {
                statusLabel.setText("Error exporting inventory to " + selectedFile.getName());
            }
        }
    }


    public static void main(String[] args) {
        launch(args);
    }
}
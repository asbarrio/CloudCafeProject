package application;

import application.model.Cafe;
import application.model.User;
import javafx.collections.FXCollections;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

public class CommunityScreen {
    
    private Cafe cafe;

    public CommunityScreen(Cafe cafe) {
        this.cafe = cafe;
    }

    public void start(Stage stage) {
        VBox root = new VBox(20);
        root.setPadding(new Insets(20));
        root.getStyleClass().add("root-pane");

        Label title = new Label("👥 Community Rewards Management");
        title.getStyleClass().add("title-label");

        TableView<User> userTable = new TableView<>();
        userTable.setItems(FXCollections.observableArrayList(cafe.getUserManager().getUsers().values()));
        userTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);

        TableColumn<User, String> idCol = new TableColumn<>("ID");
        idCol.setCellValueFactory(new PropertyValueFactory<>("id"));

        TableColumn<User, String> nameCol = new TableColumn<>("Name");
        nameCol.setCellValueFactory(new PropertyValueFactory<>("name"));

        TableColumn<User, String> roleCol = new TableColumn<>("Membership Tier");
        roleCol.setCellValueFactory(new PropertyValueFactory<>("role"));

        TableColumn<User, Integer> pointsCol = new TableColumn<>("Cloud Points");
        pointsCol.setCellValueFactory(new PropertyValueFactory<>("points"));

        userTable.getColumns().addAll(idCol, nameCol, roleCol, pointsCol);

        Button backBtn = new Button("⬅ Back");
        backBtn.setOnAction(e -> stage.close());

        root.getChildren().addAll(title, userTable, backBtn);

        Scene scene = new Scene(root, 800, 500);
        if(getClass().getResource("application.css") != null)
             scene.getStylesheets().add(getClass().getResource("application.css").toExternalForm());

        stage.setScene(scene);
        stage.setTitle("Community Rewards");
        stage.show();
    }
}
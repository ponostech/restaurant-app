package restaurant.controllers;

import java.io.IOException;
import java.net.URL;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;
import java.util.StringJoiner;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

import com.jfoenix.controls.JFXDatePicker;
import com.jfoenix.controls.JFXTextField;

import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.Label;
import javafx.scene.control.MenuItem;
import javafx.scene.control.Pagination;
import javafx.scene.control.SelectionMode;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableColumn.CellDataFeatures;
import javafx.scene.control.TableView;
import javafx.scene.image.Image;
import javafx.scene.input.KeyCode;
import javafx.scene.input.MouseButton;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.util.Callback;
import restaurant.dao.ExpenditureDAO;
import restaurant.dao.UserDAO;
import restaurant.helpers.DateTimeHelper;
import restaurant.helpers.Message;
import restaurant.helpers.PopupHelper;
import restaurant.helpers.ScreenManager;
import restaurant.helpers.Settings;
import restaurant.models.Expenditure;
import restaurant.models.User;

public class ExpenditureController implements Initializable {

	@FXML
	private VBox tableWrapper;

	@FXML
	private JFXDatePicker fromDateSelect;

	@FXML
	private JFXDatePicker toDateSelect;

	@FXML
	private Label totalExpenseLabel;

	@FXML
	private JFXTextField searchText;

	@FXML
	private Button addButton;

	@FXML
	private Button editButton;

	@FXML
	private Button deleteButton;

	@FXML
	private Button refreshButton;

	// User Table
	private static TableView<Expenditure> table;
	private static TableColumn<Expenditure, String> descCol;
	private static TableColumn<Expenditure, String> amountCol;
	private static TableColumn<Expenditure, String> datetimeCol;
	private static TableColumn<Expenditure, String> userCol;
	private static StackPane paginationPane;
	private static Pagination pagination;
	private static ObservableList<Expenditure> expenditures = FXCollections.observableArrayList();
	private static IntegerProperty limit = new SimpleIntegerProperty(Settings.ROWS_PER_PAGE_1);

	private ExpenditureDAO expenditureDAO = new ExpenditureDAO();
	private UserDAO userDAO = new UserDAO();
	private Executor executor;
	PopupHelper ph = new PopupHelper();

	@Override
	public void initialize(URL location, ResourceBundle resources) {

		// Create Executor thread
		executor = Executors.newCachedThreadPool(runnable -> {
			Thread t = new Thread(runnable);
			t.setDaemon(true);
			return t;
		});

		// Initialize Table and Pagination
		initTablePagination();
		populateTable();

		// Bind Table Columns with KeyPress Action Event
		table.setOnKeyPressed(e -> {

			if (e.getCode() == KeyCode.E) {
				editAction();
			}

			if (e.getCode() == KeyCode.DELETE) {
				try {
					deleteAction();
				} catch (Exception e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
			}
		});

		table.setOnMouseClicked(e -> {

			if (e.getButton().equals(MouseButton.SECONDARY)) {
				final ContextMenu menu = new ContextMenu();
				MenuItem edit = new MenuItem("Edit Expenditure");
				MenuItem delete = new MenuItem("Delete Expenditure");

				edit.setOnAction(e1 -> {
					editAction();
				});

				delete.setOnAction(e2 -> {
					try {
						deleteAction();
					} catch (Exception e4) {
						e4.printStackTrace();
					}
				});

				menu.getItems().addAll(edit, delete);
				table.setContextMenu(menu);
			}
		});

		// Add Listener to Pagination
		pagination.currentPageIndexProperty().addListener(new ChangeListener<Number>() {
			@Override
			public void changed(ObservableValue<? extends Number> observable, Number oldValue, Number newValue) {
				setTablePagination(newValue.intValue(), limit.get());
			}
		});

		// Search File Action
		searchText.setOnKeyPressed(e -> {

			if (searchText.getText().length() >= 2) {

				Task<ArrayList<Expenditure>> task = new Task<ArrayList<Expenditure>>() {

					@Override
					protected ArrayList<Expenditure> call() throws Exception {
						return expenditureDAO.search(searchText.getText().trim());
					}
				};

				task.setOnSucceeded(e1 -> {
					float total = 0f;
					expenditures.clear();
					for (Expenditure expenditure : task.getValue()) {
						expenditures.add(expenditure);
						total += expenditure.getAmount();
					}
					totalExpenseLabel.setText(" Rs " + String.format("%.02f", total));

					int pageCount = (int) (Math.ceil(expenditures.size() * 1.0 / limit.get()));
					pagination.setPageCount(pageCount < 1 ? 1 : pageCount);
					pagination.setCurrentPageIndex(0);
					setTablePagination(0, limit.get());
				});

				executor.execute(task);

			} else {
				refreshAction();
			}

		});
	}

	// Initialize Table and Pagination
	@SuppressWarnings("unchecked")
	public void initTablePagination() {

		table = new TableView<>();
		descCol = new TableColumn<>("Description");
		amountCol = new TableColumn<>("Amount");
		datetimeCol = new TableColumn<>("Date");
		userCol = new TableColumn<>("Added By");
		paginationPane = new StackPane();
		pagination = new Pagination();

		table.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
		table.setFixedCellSize(Settings.CELL_SIZE);
		table.setMinHeight(Settings.CELL_SIZE * (limit.get() + 1.01));
		table.setPrefHeight(Settings.CELL_SIZE * (limit.get() + 1.01));

		paginationPane.getChildren().add(pagination);
		pagination.setPageCount(1);
		tableWrapper.getChildren().addAll(table, paginationPane);

		// Set Table Columns

		descCol.setPrefWidth(220);
		descCol.setMinWidth(220);
		descCol.setResizable(true);
		descCol.setCellValueFactory(cellData -> cellData.getValue().descriptionProperty());

		amountCol.setPrefWidth(100);
		amountCol.setMinWidth(100);
		amountCol.setResizable(true);
		amountCol.setCellValueFactory(
				new Callback<TableColumn.CellDataFeatures<Expenditure, String>, ObservableValue<String>>() {

					@Override
					public ObservableValue<String> call(CellDataFeatures<Expenditure, String> param) {
						Float amt = param.getValue().getAmount();
						StringProperty amount = new SimpleStringProperty(amt.toString());
						return amount;
					}
				});

		datetimeCol.setPrefWidth(200);
		datetimeCol.setMinWidth(200);
		datetimeCol.setResizable(true);
		datetimeCol.setCellValueFactory(
				new Callback<TableColumn.CellDataFeatures<Expenditure, String>, ObservableValue<String>>() {

					@Override
					public ObservableValue<String> call(CellDataFeatures<Expenditure, String> param) {
						String date = DateTimeHelper.formattedDateTime(param.getValue().getDatetime());
						StringProperty formattedDate = new SimpleStringProperty(date);
						return formattedDate;
					}
				});

		userCol.setPrefWidth(120);
		userCol.setMinWidth(120);
		userCol.setResizable(true);
		userCol.setCellValueFactory(
				new Callback<TableColumn.CellDataFeatures<Expenditure, String>, ObservableValue<String>>() {

					@Override
					public ObservableValue<String> call(CellDataFeatures<Expenditure, String> param) {
						User user = userDAO.get(param.getValue().getUserId());
						StringProperty username = new SimpleStringProperty(user.getUsername());
						return username;
					}
				});

		table.getColumns().addAll(descCol, amountCol, datetimeCol, userCol);
		table.setMinWidth(600);

	}

	// Populate Customer Table
	public void populateTable() {
		Task<ArrayList<Expenditure>> task = new Task<ArrayList<Expenditure>>() {

			@Override
			protected ArrayList<Expenditure> call() throws Exception {
				return expenditureDAO.getExpenditures();
			}
		};

		task.setOnSucceeded(e -> {
			if (task.getValue() != null) {
				float total = 0f;
				expenditures.clear();
				for (Expenditure expenditure : task.getValue()) {
					expenditures.add(expenditure);
					total += expenditure.getAmount();
				}
				totalExpenseLabel.setText(" Rs " + String.format("%.02f", total));
				
				pagination.setPageCount((int) (Math.ceil(expenditures.size() * 1.0 / limit.get())));
				pagination.setCurrentPageIndex(0);
				setTablePagination(0, limit.get());
			}

		});

		executor.execute(task);
	}

	// Set current Table page to current index and set limit
	public static void setTablePagination(int index, int limit) {
		int newIndex = index * limit;

		List<Expenditure> expSubList = expenditures.subList(Math.min(newIndex, expenditures.size()),
				Math.min(expenditures.size(), newIndex + limit));
		table.getItems().clear();
		table.setItems(null);
		ObservableList<Expenditure> f = FXCollections.observableArrayList();
		table.setItems(f);
		for (Expenditure exp : expSubList) {
			f.add(exp);
		}
	}

	// Refresh Table
	public static void refreshTable(int pageNo) {
		pagination.setPageCount((int) (Math.ceil(expenditures.size() * 1.0 / limit.get())));
		if (pageNo < 0) {
			setTablePagination(pagination.getCurrentPageIndex(), limit.get());
		} else {
			pagination.setCurrentPageIndex(pageNo);
			setTablePagination(pageNo, limit.get());
		}
	}

	// Add new item to Observable List
	public static void addItem(Expenditure exp) {
		expenditures.add(0, exp);
		refreshTable(0);
	}

	// Update items in Observable List
	public static void updateItem(Expenditure exp, int index) {
		expenditures.set(index, exp);
		refreshTable(-1);
	}

	@FXML
	public void addAction() {
		FXMLLoader loader = new FXMLLoader(getClass().getResource("/restaurant/views/Expense-Add.fxml"));
		try {
			Pane root = loader.load();
			Stage window = new Stage();
			Scene scene = new Scene(root);
			scene.getStylesheets().add(getClass().getResource("/restaurant/application.css").toExternalForm());
			window.setScene(scene);
			window.setTitle("Add Expenditure");
			window.initModality(Modality.APPLICATION_MODAL);
			window.getIcons().add(new Image("/restaurant/images/icon_add.png"));
			window.setResizable(false);
			window.showAndWait();
			
			float total = 0f;
			for (Expenditure expenditure : expenditures) {
				total += expenditure.getAmount();
			}
			totalExpenseLabel.setText(" Rs " + String.format("%.02f", total));
		
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	@FXML
	public void deleteAction() throws IOException {
		ObservableList<Expenditure> selectedExpenses = table.getSelectionModel().getSelectedItems();
		if (!selectedExpenses.isEmpty()) {

			StringJoiner expDelete = new StringJoiner(", ");

			for (Expenditure exp : selectedExpenses) {
				expDelete.add(exp.getDescription());
			}

			// Delete permission denied for Employee
			if (ScreenManager.getCurrentUser().getRole() != 1) {
				ph.showError(Message.PERMISSION_DENIED);

			} else {

				if (ph.showConfirm(Message.DELETE_WARNING + "Expenditure(s): " + expDelete.toString() + " ?")) {

					// Loop through selected users
					for (Expenditure exp : selectedExpenses) {
						Task<Boolean> task = new Task<Boolean>() {

							@Override
							protected Boolean call() throws Exception {
								return expenditureDAO.delete(exp.getId());
							}
						};

						task.setOnSucceeded(e -> {
							if (task.getValue()) {
								expenditures.remove(exp);
								refreshTable(-1);
								float total = 0f;
								for (Expenditure expenditure : expenditures) {
									total += expenditure.getAmount();
								}
								totalExpenseLabel.setText(" Rs " + String.format("%.02f", total));
							} else {
								ph.showError(Message.ERROR);
							}

						});

						executor.execute(task);
					}
				}
			}

		} else {
			ph.showError(Message.ITEM_SELECT_ERROR);
		}
	}

	@FXML
	public void editAction() {
		ObservableList<Expenditure> selectedExpenses = table.getSelectionModel().getSelectedItems();
		if (!selectedExpenses.isEmpty()) {
			int i = 0;
			for (Expenditure exp : selectedExpenses) {
				FXMLLoader loader = new FXMLLoader(getClass().getResource("/restaurant/views/Expense-Edit.fxml"));
				try {
					Pane root = loader.load();
					Stage window = new Stage();
					Scene scene = new Scene(root);

					// Pass selected user and position/index in ObservableList
					// to
					// Edit User Controller
					ExpenditureEditController expenditureEditController = loader
							.<ExpenditureEditController> getController();
					int index = table.getSelectionModel().getSelectedIndices().get(i)
							+ (pagination.getCurrentPageIndex() * limit.get());
					expenditureEditController.populateExpenditureData(exp, index);

					scene.getStylesheets().add(getClass().getResource("/restaurant/application.css").toExternalForm());
					window.setScene(scene);
					window.setTitle("Edit Expenditure");
					window.initModality(Modality.APPLICATION_MODAL);
					window.getIcons().add(new Image("/restaurant/images/icon_edit.png"));
					window.setResizable(false);
					window.show();
					
				} catch (IOException e) {
					e.printStackTrace();
				}
				i++;
			}

		} else {
			ph.showError(Message.ITEM_SELECT_ERROR);
		}
	}

	@FXML
	public void refreshAction() {
		populateTable();
	}

	@FXML
	public void searchByDateAction() {
		LocalDateTime fromDateTime = LocalDateTime.of(fromDateSelect.getValue(), LocalTime.MIDNIGHT);
		LocalDateTime toDateTime = LocalDateTime.of(toDateSelect.getValue(), LocalTime.MAX);

		if ((fromDateTime != null) && (toDateTime != null)) {
			if (fromDateTime.isAfter(toDateTime)) {
				ph.showError(Message.DATE_DIFF_ERROR);
			} else {
				Task<ArrayList<Expenditure>> task = new Task<ArrayList<Expenditure>>() {

					@Override
					protected ArrayList<Expenditure> call() throws Exception {
						return expenditureDAO.getExpenditures(fromDateTime.toString(), toDateTime.toString());
					}
				};

				task.setOnSucceeded(e -> {
					if (task.getValue() != null) {
						float total = 0f;
						expenditures.clear();
						for (Expenditure expenditure : task.getValue()) {
							expenditures.add(expenditure);
							total += expenditure.getAmount();
						}
						totalExpenseLabel.setText(" Rs " + String.format("%.02f", total));
						pagination.setPageCount((int) (Math.ceil(expenditures.size() * 1.0 / limit.get())));
						pagination.setCurrentPageIndex(0);
						setTablePagination(0, limit.get());
					}

				});

				executor.execute(task);
			}
		} else {
			ph.showError(Message.DATE_SELECT_ERROR);
		}

	}

}

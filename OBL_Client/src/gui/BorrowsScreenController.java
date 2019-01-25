package gui;

import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.Map;

import com.jfoenix.controls.JFXTextField;

import entities.Book;
import entities.BorrowACopyOfBook;
import entities.DBMessage;
import entities.ObservableBook;
import entities.ObservableBorrow;
import entities.ObservableEmployee;
import entities.User;
import javafx.beans.InvalidationListener;
import javafx.beans.Observable;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableRow;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.paint.Color;
import javafx.util.Callback;

public class BorrowsScreenController implements IClientUI
{
	//NEED TO ADD REFRESH FUNCTION
	
	User userLoggedIn;
	@FXML
	private TableView<ObservableBorrow> borrowsTable;

	@FXML
	private TableColumn<ObservableBorrow, String> borrowNumberColumn;

	@FXML
	private TableColumn<ObservableBorrow, String> borrowDateColumn;

	@FXML
	private TableColumn<ObservableBorrow, String> returnDateColumn;

	@FXML
	private TableColumn<ObservableBorrow, String> copyNumberColumn;

	@FXML
	private TableColumn<ObservableBorrow, String> catalogNumberColumn;

	@FXML
	private TableColumn<ObservableBorrow, String> subscriberIDColumn;

	private ObservableList<ObservableBorrow> observableBorrowsList;// for table view...
    
	@FXML
    private JFXTextField searchTextField;
	//this is the search function, it is in listener for text inside the textfield
	private InvalidationListener onSearchStart = new InvalidationListener()
	{
		
		@Override
		public void invalidated(Observable arg0)
		{

            if(searchTextField.textProperty().get().isEmpty()) 
            {
            	borrowsTable.setItems(observableBorrowsList);
                return;
            }

            ObservableList<ObservableBorrow> tableItems = FXCollections.observableArrayList();

            ObservableList<TableColumn<ObservableBorrow, ?>> cols = borrowsTable.getColumns();

            for(int i=0; i<observableBorrowsList.size(); i++) 
            {
                for(int j=0; j<cols.size(); j++) 
                {

                    TableColumn col = cols.get(j);
					String cellValue = null;
                    try
					{
						cellValue = col.getCellData(observableBorrowsList.get(i)).toString();
					} 
                    catch (NullPointerException ex)
					{
						break;
					}

                    cellValue = cellValue.toLowerCase();

                    if(cellValue.contains(searchTextField.textProperty().get().toLowerCase())) 
                    {

                        tableItems.add(observableBorrowsList.get(i));

                        break;
                    }                        
                }
            }
            borrowsTable.setItems(tableItems);	
		}
	};
	
	@Override
	public User getUserLogedIn()
	{
		return userLoggedIn;
	}

	@Override
	public void setUserLogedIn(User userLoged)
	{
		userLoggedIn = userLoged;
		searchTextField.textProperty().addListener(onSearchStart);
		switch (userLoggedIn.getType())
		{
		case "subscriber":
			initialSubscriberView();
			break;
		case "librarian":
		case "library manager":
			 initialLibrarianView();
			break;
		}
	}


	private void initialLibrarianView()
	{
		borrowNumberColumn.setCellValueFactory(new PropertyValueFactory<>("borrowId"));
		borrowDateColumn.setCellValueFactory(new PropertyValueFactory<>("borrowDate"));
		returnDateColumn.setCellValueFactory(new PropertyValueFactory<>("returnDate"));
		copyNumberColumn.setCellValueFactory(new PropertyValueFactory<>("copyId"));
		catalogNumberColumn.setCellValueFactory(new PropertyValueFactory<>("catalogNumber"));
		subscriberIDColumn.setCellValueFactory(new PropertyValueFactory<>("subscriberId"));
		updateReturnDatesColors(returnDateColumn);
		observableBorrowsList = FXCollections.observableArrayList();
				
		borrowsTable.setRowFactory(tv -> { // press on row in borrow table to do what ever we want
			TableRow<ObservableBorrow> row = new TableRow<>();
			row.setOnMouseClicked(event -> {
				if (event.getClickCount() == 2 && (!row.isEmpty()))
				{
					ObservableBorrow rowData = row.getItem();
					System.out.println("Librarian pressed on: "+ rowData.getBorrowId()+" Borrow ID");
					
				}
			});
			return row;
		});

		GuiManager.client.getAllCurrentBorrows();		
	}

	private void initialSubscriberView()
	{
		borrowNumberColumn.setCellValueFactory(new PropertyValueFactory<>("borrowId"));
		borrowDateColumn.setCellValueFactory(new PropertyValueFactory<>("borrowDate"));
		returnDateColumn.setCellValueFactory(new PropertyValueFactory<>("returnDate"));
		copyNumberColumn.setCellValueFactory(new PropertyValueFactory<>("copyId"));
		catalogNumberColumn.setCellValueFactory(new PropertyValueFactory<>("catalogNumber"));
		subscriberIDColumn.setVisible(false);
		borrowsTable.setRowFactory(tv -> { // press on row in borrow table to do what ever we want
			TableRow<ObservableBorrow> row = new TableRow<>();
			row.setOnMouseClicked(event -> {
				if (event.getClickCount() == 2 && (!row.isEmpty()))
				{
					ObservableBorrow rowData = row.getItem();
					System.out.println("subscriber pressed on: "+ rowData.getBorrowId()+" Borrow ID");
					
				}
			});
			return row;
		});
		updateReturnDatesColors(returnDateColumn);
		observableBorrowsList = FXCollections.observableArrayList();
		GuiManager.client.getCurrentBorrowsForSubscriberID(userLoggedIn.getId());
	}

	@Override
	public void getMessageFromServer(DBMessage msg)
	{
		switch (msg.Action)
		{
		case GetCurrentBorrowsForSubID:case GetCurrentBorrows:
		{
			updateBorrowTable((List<BorrowACopyOfBook>)msg.Data);
		}
		default:
			break;
		}
	}

	private void updateBorrowTable(List<BorrowACopyOfBook> borrowList)
	{
		for(BorrowACopyOfBook borrow: borrowList)
		{
			ObservableBorrow temp = new ObservableBorrow(borrow.getId(), borrow.getBorrowDate(),
					borrow.getExpectedReturnDate(), borrow.getCopyId(), borrow.getBookCatalogNumber(),
					borrow.getSubscriberId());
			observableBorrowsList.add(temp);
		}
		
		borrowsTable.setItems(observableBorrowsList);
	}
	
	private void updateReturnDatesColors(TableColumn<ObservableBorrow, String> column) 
	{
		returnDateColumn.setCellFactory(
				new Callback<TableColumn<ObservableBorrow, String>, TableCell<ObservableBorrow, String>>() 
				{
					public TableCell call(TableColumn param) 
					{
						return new TableCell<ObservableBorrow, String>() 
						{
							@Override
							public void updateItem(String item, boolean empty) 
							{
								super.updateItem(item, empty);

								if (item != null) 
								{

									if (item.length() == 10) // expected return date column
									{
										if ((LocalDate.parse(getCurrentDateAsString())
												.isAfter(LocalDate.parse(item)))) 
										{
											setTextFill(Color.RED);
											setStyle("-fx-font-weight: bold");
											setText(item);
										} 
										else if ((LocalDate.parse(getCurrentDateAsString())
												.isEqual(LocalDate.parse(item))) // return day
												|| (LocalDate.parse(getCurrentDateAsString()).plusDays(1))
														.isEqual(LocalDate.parse(item))) // one day before return date
										{
											setTextFill(Color.GREEN);
											setStyle("-fx-font-weight: bold");
											setText(item);
										} 
										else 
										{
											setTextFill(Color.BLACK);
											setStyle("-fx-font-weight: lighter");
											setText(item);
										}

									}
								}
							}
						};
					}
				});
	}
	
	public static String getCurrentDateAsString()
	{
		GregorianCalendar calendar = new GregorianCalendar();
		SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
		String string = format.format(calendar.getTime());
		return string;
	}
	
}

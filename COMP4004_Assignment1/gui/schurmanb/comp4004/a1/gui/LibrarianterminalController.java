package schurmanb.comp4004.a1.gui;

import java.net.URL;
import java.util.ArrayList;
import java.util.Optional;
import java.util.ResourceBundle;

import org.controlsfx.control.action.AbstractAction;
import org.controlsfx.control.action.Action;
import org.controlsfx.dialog.Dialog;
import org.controlsfx.dialog.Dialogs;
import org.controlsfx.samples.HelloValidation;
import org.joda.time.LocalDate;

import schurmanb.comp4004.a1.src.Item;
import schurmanb.comp4004.a1.src.ItemNotFoundException;
import schurmanb.comp4004.a1.src.Librarian;
import schurmanb.comp4004.a1.src.Library;
import schurmanb.comp4004.a1.src.Loan;
import schurmanb.comp4004.a1.src.Title;
import schurmanb.comp4004.a1.src.User;
import schurmanb.comp4004.a1.src.UserNotFoundException;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.image.ImageView;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Priority;
import javafx.stage.Stage;

public class LibrarianterminalController extends Application implements Initializable
{
	private Library library;
	private Librarian librarian;
	private User user;
	private Title selection;
	
	@FXML private BorderPane bp;
	@FXML private Button addUserButton;
	@FXML private Button addTitleButton;
	@FXML private Button addItemButton;
	@FXML private Button borrowLoancopyButton;
	@FXML private Button returnLoancopyButton;
	@FXML private Button renewLoanButton;
	@FXML private Button removeTitleButton;
	@FXML private Button removeItemButton;
	@FXML private Button removeUserButton;
	@FXML private Button removeLibrarianButton;
	@FXML private Button monitorSystemButton;
	@FXML private Button addLibrarianButton;
	@FXML private Button collectFinesButton;
	@FXML private TableView<Title> titlesTable;
	@FXML private TableColumn<Title,String> titlesCol;
	@FXML private TableColumn<Title,String> authorsCol;
	@FXML private TableColumn<Title,String> isbnsCol;

	private Stage stage;
	
	/*
	 * ADD USER
	 */
	@FXML private void addUserButtonPressed( ActionEvent ae ){
		// prompt Librarian for iD 
		Optional<String> response = Dialogs.create()
		        .owner(stage)
		        .title("Librarian Login")
		        .masthead("Enter Librarian ID")
		        .message("ID#")
		        .showTextInput();

		// get response 
		if (response.isPresent()) {
	        try {
	        	int lID = Integer.parseInt(response.get());
	        	library = Library.getInstance();
	        	librarian = library.findLibrarian(lID);
			} catch (NumberFormatException | UserNotFoundException e) {
				Dialogs.create()
		        .owner(stage)
		        .title("Error")
		        .masthead("Librarian Not Found")
		        .message(e.toString())
		        .showError();
			}
		} else {
			return;
		}
		
		if( librarian == null ) return;
		
		Dialog dlg = new Dialog(null, "Add User");
		dlg.setMasthead("Enter User Information");
	       
	     // listen to user input on dialog (to enable / disable the login button)
	     ChangeListener<String> changeListener = new ChangeListener<String>() {
			@Override
			public void changed(ObservableValue<? extends String> observable,
					String oldValue, String newValue) {
				validateAddUser();
			}
	     };
	     fNameTxt.textProperty().addListener(changeListener);
	     lNameTxt.textProperty().addListener(changeListener);
	       
	     // layout a custom GridPane containing the input fields and labels
	     final GridPane content = new GridPane();
	     content.setHgap(10);
	     content.setVgap(10);
	       
	     content.add(new Label("First Name:"), 0, 0);
	     content.add(fNameTxt, 1, 0);
	     GridPane.setHgrow(fNameTxt, Priority.ALWAYS);
	     content.add(new Label("Last Name:"), 0, 1);
	     content.add(lNameTxt, 1, 1);
	     GridPane.setHgrow(fNameTxt, Priority.ALWAYS);
	       
	     // create the dialog with a custom graphic and the gridpane above as the
	     // main content region
	     dlg.setResizable(false);
	     dlg.setIconifiable(false);
	     dlg.setGraphic(new ImageView(HelloValidation.class.getResource("apertureLogo.png").toString()));
	     dlg.setContent(content);
	     dlg.getActions().addAll(actionAddUser, Dialog.Actions.CANCEL);
	     validateAddUser();
	       
	     Platform.runLater(new Runnable() {
	         public void run() {
	             fNameTxt.requestFocus();
	         }
	     });

	     dlg.show();
	}
	final TextField fNameTxt = new TextField();
	final TextField lNameTxt = new TextField();
	final Action actionAddUser = new AbstractAction("Add User") {
	    // This method is called when the login button is clicked ...
	    public void handle(ActionEvent ae) {
	        Dialog d = (Dialog) ae.getSource();
	        // Do the login here.
	        
	        String fName = fNameTxt.getText().trim();
	        String lName = lNameTxt.getText().trim();
	        User u = null;
	        try {
	        	u = library.addUser(fName, lName);
	        } catch( Exception e ){
	        	Dialogs.create()
		        .owner(stage)
		        .title("Error")
		        .masthead("Problem Adding User")
		        .message(e.toString())
		        .showError();
	        }
	        if( u != null ){
	        	Dialogs.create()
		        .owner(stage)
		        .title("Success")
		        .masthead("User Added")
		        .message(u.toString())
		        .showInformation();
	        }
	        librarian = null;
	        d.hide();
	    }
	};
	 // This method is called when the user types into the text fields  
	 private void validateAddUser() {
	     actionAddUser.disabledProperty().set( 
	           fNameTxt.getText().trim().isEmpty() || lNameTxt.getText().trim().isEmpty());
	 }
	
	 	/*
		 * ADD LIBRARIAN
		 */
		@FXML private void addLibrarianButtonPressed( ActionEvent ae ){
			// prompt Librarian for iD 
			Optional<String> response = Dialogs.create()
			        .owner(stage)
			        .title("Librarian Login")
			        .masthead("Enter Librarian ID")
			        .message("ID#")
			        .showTextInput();

			// get response 
			if (response.isPresent()) {
		        try {
		        	int lID = Integer.parseInt(response.get());
		        	library = Library.getInstance();
		        	librarian = library.findLibrarian(lID);
				} catch (NumberFormatException | UserNotFoundException e) {
					Dialogs.create()
			        .owner(stage)
			        .title("Error")
			        .masthead("Librarian Not Found")
			        .message(e.toString())
			        .showError();
				}
			} else {
				return;
			}
			
			if( librarian == null ) return;
			
			Dialog dlg = new Dialog(null, "Add Librarian");
			dlg.setMasthead("Enter User Information");
		       
		     // listen to user input on dialog (to enable / disable the login button)
		     ChangeListener<String> changeListener = new ChangeListener<String>() {
				@Override
				public void changed(ObservableValue<? extends String> observable,
						String oldValue, String newValue) {
					validateAddLibrarian();
				}
		     };
		     libfNameTxt.textProperty().addListener(changeListener);
		     liblNameTxt.textProperty().addListener(changeListener);
		       
		     // layout a custom GridPane containing the input fields and labels
		     final GridPane content = new GridPane();
		     content.setHgap(10);
		     content.setVgap(10);
		       
		     content.add(new Label("First Name:"), 0, 0);
		     content.add(libfNameTxt, 1, 0);
		     GridPane.setHgrow(libfNameTxt, Priority.ALWAYS);
		     content.add(new Label("Last Name:"), 0, 1);
		     content.add(liblNameTxt, 1, 1);
		     GridPane.setHgrow(libfNameTxt, Priority.ALWAYS);
		       
		     // create the dialog with a custom graphic and the gridpane above as the
		     // main content region
		     dlg.setResizable(false);
		     dlg.setIconifiable(false);
		     dlg.setGraphic(new ImageView(HelloValidation.class.getResource("apertureLogo.png").toString()));
		     dlg.setContent(content);
		     dlg.getActions().addAll(actionAddLibrarian, Dialog.Actions.CANCEL);
		     validateAddUser();
		       
		     Platform.runLater(new Runnable() {
		         public void run() {
		             libfNameTxt.requestFocus();
		         }
		     });

		     dlg.show();
		}
		final TextField libfNameTxt = new TextField();
		final TextField liblNameTxt = new TextField();
		final Action actionAddLibrarian = new AbstractAction("Add Librarian") {
		    // This method is called when the login button is clicked ...
		    public void handle(ActionEvent ae) {
		        Dialog d = (Dialog) ae.getSource();
		        // Do the login here.
		        
		        String fName = libfNameTxt.getText().trim();
		        String lName = liblNameTxt.getText().trim();
		        Librarian u = null;
		        try {
		        	u = library.addLibrarian(fName, lName);
		        } catch( Exception e ){
		        	Dialogs.create()
			        .owner(stage)
			        .title("Error")
			        .masthead("Problem Adding Librarian")
			        .message(e.toString())
			        .showError();
		        }
		        if( u != null ){
		        	Dialogs.create()
			        .owner(stage)
			        .title("Success")
			        .masthead("Librarian Added")
			        .message(u.toString())
			        .showInformation();
		        }
		        librarian = null;
		        d.hide();
		    }
		};
		 // This method is called when the user types into the text fields  
		 private void validateAddLibrarian() {
		     actionAddUser.disabledProperty().set( 
		           libfNameTxt.getText().trim().isEmpty() || liblNameTxt.getText().trim().isEmpty());
		 }
	 
	 /*
	  * Add Title
	  */
	@FXML private void addTitleButtonPressed( ActionEvent ae ){
			// prompt Librarian for iD 
			Optional<String> response = Dialogs.create()
			        .owner(stage)
			        .title("Librarian Login")
			        .masthead("Enter Librarian ID")
			        .message("ID#")
			        .showTextInput();

			// get response 
			if (response.isPresent()) {
		        try {
		        	int lID = Integer.parseInt(response.get());
		        	library = Library.getInstance();
		        	librarian = library.findLibrarian(lID);
				} catch (NumberFormatException | UserNotFoundException e) {
					Dialogs.create()
			        .owner(stage)
			        .title("Error")
			        .masthead("Librarian Not Found")
			        .message(e.toString())
			        .showError();
				}
			} else {
				return;
			}

			if( librarian == null ) return;
			
			Dialog dlg = new Dialog(null, "Add Title");
			dlg.setMasthead("Enter Title Information");
		       
		     // listen to user input on dialog (to enable / disable the login button)
		     ChangeListener<String> changeListener = new ChangeListener<String>() {
				@Override
				public void changed(ObservableValue<? extends String> observable,
						String oldValue, String newValue) {
					validateAddTitle();
				}
		     };
		     titleTitleTxt.textProperty().addListener(changeListener);
		     authorTitleTxt.textProperty().addListener(changeListener);
		     isbnTitleTxt.textProperty().addListener(changeListener);
		       
		     // layout a custom GridPane containing the input fields and labels
		     final GridPane content = new GridPane();
		     content.setHgap(10);
		     content.setVgap(10);
		       
		     content.add(new Label("Title:"), 0, 0);
		     content.add(titleTitleTxt, 1, 0);
		     GridPane.setHgrow(titleTitleTxt, Priority.ALWAYS);
		     content.add(new Label("Author:"), 0, 1);
		     content.add(authorTitleTxt, 1, 1);
		     GridPane.setHgrow(authorTitleTxt, Priority.ALWAYS);
		     content.add(new Label("ISBN:"), 0, 2);
		     content.add(isbnTitleTxt, 1, 2);
		     GridPane.setHgrow(isbnTitleTxt, Priority.ALWAYS);
		       
		     // create the dialog with a custom graphic and the gridpane above as the
		     // main content region
		     dlg.setResizable(false);
		     dlg.setIconifiable(false);
		     dlg.setGraphic(new ImageView(HelloValidation.class.getResource("apertureLogo.png").toString()));
		     dlg.setContent(content);
		     dlg.getActions().addAll(actionAddTitle, Dialog.Actions.CANCEL);
		     validateAddTitle();
		       
		     Platform.runLater(new Runnable() {
		         public void run() {
		             titleTitleTxt.requestFocus();
		         }
		     });

		     dlg.show();
		}
		final TextField titleTitleTxt = new TextField();
		final TextField authorTitleTxt = new TextField();
		final TextField isbnTitleTxt = new TextField();
		final Action actionAddTitle = new AbstractAction("Add Title") {
		    // This method is called when the login button is clicked ...
		    public void handle(ActionEvent ae) {
		        Dialog d = (Dialog) ae.getSource();
		        // Do the login here.
		        
		        Title t = null;
		        try {
		        	int isbn = Integer.parseInt(isbnTitleTxt.getText().trim());
			        String title = titleTitleTxt.getText().trim();
			        String author = authorTitleTxt.getText().trim();
		        	System.out.println(isbn);
		        	System.out.println(title);
		        	System.out.println(author);
		        	t = library.addTitle(title, author, isbn, librarian.getLibrarianID());
		        } catch( Exception e ){
		        	Dialogs.create()
			        .owner(stage)
			        .title("Error")
			        .masthead("Problem Adding Title")
			        .message(e.toString())
			        .showError();
		        }
		        if( t != null ){
		        	Dialogs.create()
			        .owner(stage)
			        .title("Success")
			        .masthead("Title Added")
			        .message(t.toString())
			        .showInformation();
		        }
		        d.hide();
		        librarian = null;
		        updateList();
		    }
		};
		// This method is called when the user types into the text fields  
		private void validateAddTitle() {
			actionAddTitle.disabledProperty().set( 
	           titleTitleTxt.getText().trim().isEmpty()
	           || isbnTitleTxt.getText().trim().isEmpty()
	           || authorTitleTxt.getText().trim().isEmpty());
		}

		 /*
		  * Add Item
		  */
		@FXML private void addItemButtonPressed( ActionEvent ae ){
	        if( selection != null ){
	        	titleItemTxt.setText(selection.getTitle());
	        	authorItemTxt.setText(selection.getAuthor());
	        	isbnItemTxt.setText(""+selection.getISBN());
	        }
				// prompt Librarian for iD 
				Optional<String> response = Dialogs.create()
				        .owner(stage)
				        .title("Librarian Login")
				        .masthead("Enter Librarian ID")
				        .message("ID#")
				        .showTextInput();

				// get response 
				if (response.isPresent()) {
			        try {
			        	int lID = Integer.parseInt(response.get());
			        	library = Library.getInstance();
			        	librarian = library.findLibrarian(lID);
					} catch (NumberFormatException | UserNotFoundException e) {
						Dialogs.create()
				        .owner(stage)
				        .title("Error")
				        .masthead("Librarian Not Found")
				        .message(e.toString())
				        .showError();
						librarian = null;
					}
				} else {
					return;
				}
				
				if( librarian == null ) return;
				
				Dialog dlg = new Dialog(null, "Add Item");
				dlg.setMasthead("Enter Title Information");
			       
			     // listen to user input on dialog (to enable / disable the login button)
			     ChangeListener<String> changeListener = new ChangeListener<String>() {
					@Override
					public void changed(ObservableValue<? extends String> observable,
							String oldValue, String newValue) {
						validateAddItem();
					}
			     };
			     titleItemTxt.textProperty().addListener(changeListener);
			     authorItemTxt.textProperty().addListener(changeListener);
			     isbnItemTxt.textProperty().addListener(changeListener);
			       
			     // layout a custom GridPane containing the input fields and labels
			     final GridPane content = new GridPane();
			     content.setHgap(10);
			     content.setVgap(10);
			       
			     content.add(new Label("Title:"), 0, 0);
			     content.add(titleItemTxt, 1, 0);
			     GridPane.setHgrow(titleItemTxt, Priority.ALWAYS);
			     content.add(new Label("Author:"), 0, 1);
			     content.add(authorItemTxt, 1, 1);
			     GridPane.setHgrow(authorItemTxt, Priority.ALWAYS);
			     content.add(new Label("ISBN:"), 0, 2);
			     content.add(isbnItemTxt, 1, 2);
			     GridPane.setHgrow(isbnItemTxt, Priority.ALWAYS);
			       
			     // create the dialog with a custom graphic and the gridpane above as the
			     // main content region
			     dlg.setResizable(false);
			     dlg.setIconifiable(false);
			     dlg.setGraphic(new ImageView(HelloValidation.class.getResource("apertureLogo.png").toString()));
			     dlg.setContent(content);
			     dlg.getActions().addAll(actionAddItem, Dialog.Actions.CANCEL);
			     validateAddTitle();
			       
			     Platform.runLater(new Runnable() {
			         public void run() {
			             titleTitleTxt.requestFocus();
			         }
			     });

			     dlg.show();
			}
			final TextField titleItemTxt = new TextField();
			final TextField authorItemTxt = new TextField();
			final TextField isbnItemTxt = new TextField();
			final Action actionAddItem = new AbstractAction("Add Item") {
			    // This method is called when the login button is clicked ...
			    public void handle(ActionEvent ae) {
			        Dialog d = (Dialog) ae.getSource();
			        // Do the login here.
			        
			        Title t = null;
			        Item i = null;
			        try {
			        	library = Library.getInstance();
			        	int isbn = Integer.parseInt(isbnItemTxt.getText().trim());
				        String title = titleItemTxt.getText().trim();
				        String author = authorItemTxt.getText().trim();
			        	System.out.println(isbn);
			        	System.out.println(title);
			        	System.out.println(author);
			        	t = library.findTitle(isbn);
			        	i = library.addItem(t, librarian);
			        } catch( Exception e ){
			        	Dialogs.create()
				        .owner(stage)
				        .title("Error")
				        .masthead("Problem Adding Title")
				        .message(e.toString())
				        .showError();
			        }
			        if( i != null ){
			        	Dialogs.create()
				        .owner(stage)
				        .title("Success")
				        .masthead("Item Added")
				        .message(i.toString())
				        .showInformation();
			        }
			        d.hide();
			        librarian = null;
			        updateList();
			    }
			};
			// This method is called when the user types into the text fields  
			private void validateAddItem() {
				actionAddTitle.disabledProperty().set( 
		           titleItemTxt.getText().trim().isEmpty()
		           || isbnItemTxt.getText().trim().isEmpty()
		           || authorItemTxt.getText().trim().isEmpty());
			}
		
		

		/*
		 * Borrow Loan copy
		 */
		@FXML private void borrowLoancopyButtonPressed( ActionEvent ae ){
			userLogin();
			if( user == null ){
				return;
			}
			if( selection != null ){
	        	ArrayList<String> choices = new ArrayList<String>();
	        	choices.add("7 days");
	        	choices.add("14 days");
	        	choices.add("28 days");
	        	choices.add("-1 (for debugging)");

	        	Optional<String> response = Dialogs.create()
	        	        .owner(stage)
	        	        .title("Select Loan Duration")
	        	        .masthead("How long would you like to borrow "+selection.getTitle()+" by "+selection.getAuthor()+"?")
	        	        .message("Duration:")
	        	        .showChoices(choices);

	        	// One way to get the response value.
	        	if (response.isPresent()) {
	        	    int days = 0;
	        	    String s = response.get();
	        	    System.out.println(s);
	        	    if( s.equals("7 days") ) days = 7;
	        	    else if( s.equals("14 days") ) days = 14;
	        	    else if( s.equals("28 days") ) days = 28;
	        	    else if( s.equals("-1 (for debugging)") ) days = 1;
	        	    Loan l = null;
	        	    try {
	        	    	library = Library.getInstance();
	        	    	Item item = library.findSomeItem(selection.getISBN());
	        	    	if( days > 0 ){
	        	    		l = library.borrowLoancopy(item, user, new LocalDate().plusDays(days));
	        	    	} else {
	        	    		l = library.borrowLoancopy(item, user, new LocalDate().minusDays(days));
	        	    	}
	        	    } catch( Exception e ){
						Dialogs.create()
				        .owner(stage)
				        .title("Error")
				        .masthead("Could Not Borrow Item")
				        .message(e.toString())
				        .showError();
	        	    }
	        	    if( l != null ){
		            	Dialogs.create()
		    	        .owner(stage)
		    	        .title("Success")
		    	        .masthead(days+" day Loan generated")
		    	        .message("Item#"+l.getItem().getItemID()+" is due by "+l.getDueDate())
		    	        .showInformation();
	        	    }
	        	} 
			} else {
				Dialogs.create()
		        .owner(stage)
		        .title("Error")
		        .masthead("Could Not Borrow Item")
		        .message("Please select an Item from the table.")
		        .showError();
			}
			user = null;
		}
		
		/*
		 * renew Loan copy
		 */
		@SuppressWarnings("unused")
		@FXML private void renewLoanButtonPressed( ActionEvent ae ){
			userLogin();
			if( user == null ){
				return;
			}
				// prompt Librarian for iD 
				Optional<String> response2 = Dialogs.create()
				        .owner(stage)
				        .title("Renew Loan")
				        .masthead("Enter Item ID of Loancopy")
				        .message("ID#")
				        .showTextInput();

				// get response 
				Item i = null;
				if (response2.isPresent()) {
			        try {
			        	int iID = Integer.parseInt(response2.get());
			        	library = Library.getInstance();
			        	i = library.findItem(iID);
					} catch (Exception e) {
						Dialogs.create()
				        .owner(stage)
				        .title("Error")
				        .masthead("Item Not Found")
				        .message(e.toString())
				        .showError();
						librarian = null;
					}
				} else {
					return;
				}
				try {
					Loan loan = library.findLoan(user, i);
				} catch (ItemNotFoundException e1) {
					Dialogs.create()
			        .owner(stage)
			        .title("Error")
			        .masthead("Could Not Find Loan")
			        .message(e1.toString())
			        .showError();
					user = null; return;
				}
	        	ArrayList<String> choices = new ArrayList<String>();
	        	choices.add("7 days");
	        	choices.add("14 days");
	        	choices.add("28 days");

	        	Optional<String> response = Dialogs.create()
	        	        .owner(stage)
	        	        .title("Select Loan Duration")
	        	        .masthead("How long would you like to renew "+selection.getTitle()+" by "+selection.getAuthor()+" for?")
	        	        .message("Duration:")
	        	        .showChoices(choices);

	        	// One way to get the response value.
	        	if (response.isPresent()) {
	        	    int days = 0;
	        	    String s = response.get();
	        	    System.out.println(s);
	        	    if( s.equals("7 days") ) days = 7;
	        	    else if( s.equals("14 days") ) days = 14;
	        	    else if( s.equals("28 days") ) days = 28;
	        	    Loan l = null;
	        	    try {
	        	    	library = Library.getInstance();
	        	    	l = library.findLoan(user, i);
	        	    	library.renewLoan(l, user, days);
	        	    } catch( Exception e ){
						Dialogs.create()
				        .owner(stage)
				        .title("Error")
				        .masthead("Could Not Renew Item")
				        .message(e.toString())
				        .showError();
	        	    }
	        	    if( l != null ){
		            	Dialogs.create()
		    	        .owner(stage)
		    	        .title("Success")
		    	        .masthead("Loan renewed for "+days+" days")
		    	        .message("Item#"+l.getItem().getItemID()+" is due by "+l.getDueDate())
		    	        .showInformation();
	        	    }
	        	} 
			user = null;
		}
		
		 /*
		  * User Login
		  */
		private void userLogin(){	
				Dialog dlg = new Dialog(null, "User Login");
				dlg.setMasthead("Enter User Information");
			       
			     // listen to user input on dialog (to enable / disable the login button)
			     ChangeListener<String> changeListener = new ChangeListener<String>() {
					@Override
					public void changed(ObservableValue<? extends String> observable,
							String oldValue, String newValue) {
						validateUserLogin();
					}
			     };
			     userFNameLoginTxt.textProperty().addListener(changeListener);
			     userLNameLoginTxt.textProperty().addListener(changeListener);
			       
			     // layout a custom GridPane containing the input fields and labels
			     final GridPane content = new GridPane();
			     content.setHgap(10);
			     content.setVgap(10);
			       
			     content.add(new Label("First Name:"), 0, 0);
			     content.add(userFNameLoginTxt, 1, 0);
			     GridPane.setHgrow(userFNameLoginTxt, Priority.ALWAYS);
			     content.add(new Label("Last Name:"), 0, 1);
			     content.add(userLNameLoginTxt, 1, 1);
			     GridPane.setHgrow(userLNameLoginTxt, Priority.ALWAYS);
			       
			     // create the dialog with a custom graphic and the gridpane above as the
			     // main content region
			     dlg.setResizable(false);
			     dlg.setIconifiable(false);
			     dlg.setGraphic(new ImageView(HelloValidation.class.getResource("apertureLogo.png").toString()));
			     dlg.setContent(content);
			     dlg.getActions().addAll(actionUserLogin, Dialog.Actions.CANCEL);
			     validateUserLogin();
			       
			     Platform.runLater(new Runnable() {
			         public void run() {
			             titleTitleTxt.requestFocus();
			         }
			     });

			     dlg.show();
			}
			final TextField userFNameLoginTxt = new TextField();
			final TextField userLNameLoginTxt = new TextField();
			final Action actionUserLogin = new AbstractAction("User Login") {
			    // This method is called when the login button is clicked ...
			    public void handle(ActionEvent ae) {
			        Dialog d = (Dialog) ae.getSource();
			        try {
				        String fName = userFNameLoginTxt.getText().trim();
				        String lName = userLNameLoginTxt.getText().trim();
			        	System.out.println(fName);
			        	System.out.println(lName);
			        	user = library.findUser(fName,lName);
			        } catch( Exception e ){
			        	Dialogs.create()
				        .owner(stage)
				        .title("Error")
				        .masthead("Problem Signing In")
				        .message(e.toString())
				        .showError();
			        	user = null;
			        }
			        d.hide();
			    }			    
			};
			// This method is called when the user types into the text fields  
			private void validateUserLogin() {
				actionUserLogin.disabledProperty().set( 
		           userFNameLoginTxt.getText().trim().isEmpty()
		           || userLNameLoginTxt.getText().trim().isEmpty());
			}
			
			/*
			 * return loancopy
			 */
			@FXML private void returnLoancopyButtonPressed( ActionEvent ae ){
					userLogin();
					Optional<String> response = Dialogs.create()
					        .owner(stage)
					        .title("Return Loancopy")
					        .masthead("Enter Item ID")
					        .message("ID#")
					        .showTextInput();

					Loan l = null;
					// get response 
					if (response.isPresent()) {
				        try {
				        	int iID = Integer.parseInt(response.get());
				        	library = Library.getInstance();
				        	Item i = library.findItem(iID);
				        	l = library.findLoan(user, i);
				        	library.returnLoancopy(user, i);
						} catch (Exception e) {
							Dialogs.create()
					        .owner(stage)
					        .title("Error")
					        .masthead("There was problem returning your Item")
					        .message(e.toString())
					        .showError();
						}
					}
					if( l != null ){
		            	Dialogs.create()
		    	        .owner(stage)
		    	        .title("Return Loancopy")
		    	        .masthead("Item returned")
		    	        .message("Thank you for returning your copy of "+l.getItem().getReferencingTitle().getTitle())
		    	        .showInformation();
					}
					user = null;
			}
			
			
			/*
			 * collect fines
			 */
			@FXML private void collectFinesButtonPressed( ActionEvent ae ){
					userLogin();
					if( user == null )return;
					if( !library.hasOverdueLoan(user) ){
						Dialogs.create()
		    	        .owner(stage)
		    	        .title("Pay Fines")
		    	        .masthead("You're in luck!")
		    	        .message("It appears that you do not have any overdue Loans.")
		    	        .showInformation();
						user = null;
						return;
					}
					Optional<String> response = Dialogs.create()
					        .owner(stage)
					        .title("Pay Fines")
					        .masthead("Enter Item ID")
					        .message("ID#")
					        .showTextInput();

					Loan l = null;
					// get response 
					if (response.isPresent()) {
				        try {
				        	int iID = Integer.parseInt(response.get());
				        	library = Library.getInstance();
				        	Item i = library.findItem(iID);
				        	l = library.findLoan(user, i);
				        	library.collectFine(user, i);
						} catch (Exception e) {
							Dialogs.create()
					        .owner(stage)
					        .title("Error")
					        .masthead("There was problem returning your Item")
					        .message(e.toString())
					        .showError();
						}
					}
					if( l != null ){
		            	Dialogs.create()
		    	        .owner(stage)
		    	        .title("Pay Fines")
		    	        .masthead("Loan priveleges returned")
		    	        .message("Thank you for paying your overdue Loans")
		    	        .showInformation();
					}
					user = null;
			}
			
			/*
			 * removeTitle
			 */
			@FXML private void removeTitleButtonPressed( ActionEvent ae ){
				Optional<String> response = Dialogs.create()
				        .owner(stage)
				        .title("Librarian Login")
				        .masthead("Enter Librarian ID")
				        .message("ID#")
				        .showTextInput();

				// get response 
				if (response.isPresent()) {
			        try {
			        	int lID = Integer.parseInt(response.get());
			        	library = Library.getInstance();
			        	librarian = library.findLibrarian(lID);
					} catch (NumberFormatException | UserNotFoundException e) {
						Dialogs.create()
				        .owner(stage)
				        .title("Error")
				        .masthead("Librarian Not Found")
				        .message(e.toString())
				        .showError();
					}
				} else {
					return;
				}

				if( librarian == null ) return;
					
					Optional<String> response2 = Dialogs.create()
					        .owner(stage)
					        .title("Remove Title")
					        .masthead("Enter isbn")
					        .message("isbn#")
					        .showTextInput();

					Title t = null;
					// get response 
					if (response.isPresent()) {
				        try {
				        	int isbn = Integer.parseInt(response2.get());
				        	library = Library.getInstance();
				        	t = library.findTitle(isbn);
				        	library.removeTitle(isbn);
						} catch (Exception e) {
							Dialogs.create()
					        .owner(stage)
					        .title("Error")
					        .masthead("There was problem returning your Item")
					        .message(e.toString())
					        .showError();
						}
					}
					if( t != null ){
		            	Dialogs.create()
		    	        .owner(stage)
		    	        .title("Success")
		    	        .masthead("Title Removed")
		    	        .message(t+" was removed from the library database")
		    	        .showInformation();
					}
					user = null;
					updateList();
			}
			
			/*
			 * remove item
			 */
			@FXML private void removeItemButtonPressed( ActionEvent ae ){
				Optional<String> response = Dialogs.create()
				        .owner(stage)
				        .title("Librarian Login")
				        .masthead("Enter Librarian ID")
				        .message("ID#")
				        .showTextInput();

				// get response 
				if (response.isPresent()) {
			        try {
			        	int lID = Integer.parseInt(response.get());
			        	library = Library.getInstance();
			        	librarian = library.findLibrarian(lID);
					} catch (NumberFormatException | UserNotFoundException e) {
						Dialogs.create()
				        .owner(stage)
				        .title("Error")
				        .masthead("Librarian Not Found")
				        .message(e.toString())
				        .showError();
					}
				} else {
					return;
				}

				if( librarian == null ) return;
					
					Optional<String> response2 = Dialogs.create()
					        .owner(stage)
					        .title("Remove Item")
					        .masthead("Enter Item ID")
					        .message("ID#")
					        .showTextInput();

					Item i = null;
					// get response 
					if (response2.isPresent()) {
				        try {
				        	int iID = Integer.parseInt(response.get());
				        	library = Library.getInstance();
				        	i = library.findItem(iID);
				        	library.removeItem(iID, librarian);
						} catch (Exception e) {
							Dialogs.create()
					        .owner(stage)
					        .title("Error")
					        .masthead("There was problem returning your Item")
					        .message(e.toString())
					        .showError();
						}
					}
					if( i != null ){
		            	Dialogs.create()
		    	        .owner(stage)
		    	        .title("Success")
		    	        .masthead("Title Removed")
		    	        .message(i+" was removed from the library database")
		    	        .showInformation();
					}
					user = null;
			}
			
			/*
			 * remove user
			 */
			@FXML private void removeUserButtonPressed( ActionEvent ae ){
				Optional<String> response = Dialogs.create()
				        .owner(stage)
				        .title("Librarian Login")
				        .masthead("Enter Librarian ID")
				        .message("ID#")
				        .showTextInput();

				// get response 
				if (response.isPresent()) {
			        try {
			        	int lID = Integer.parseInt(response.get());
			        	library = Library.getInstance();
			        	librarian = library.findLibrarian(lID);
					} catch (NumberFormatException | UserNotFoundException e) {
						Dialogs.create()
				        .owner(stage)
				        .title("Error")
				        .masthead("Librarian Not Found")
				        .message(e.toString())
				        .showError();
					}
				} else {
					return;
				}

				if( librarian == null ) return;
					
				Dialog dlg = new Dialog(null, "Remove User");
				dlg.setMasthead("Enter User Information");
			       
			     // listen to user input on dialog (to enable / disable the login button)
			     ChangeListener<String> changeListener = new ChangeListener<String>() {
					@Override
					public void changed(ObservableValue<? extends String> observable,
							String oldValue, String newValue) {
						validateRemoveUser();
					}
			     };
			     fNameRemTxt.textProperty().addListener(changeListener);
			     lNameRemTxt.textProperty().addListener(changeListener);
			       
			     // layout a custom GridPane containing the input fields and labels
			     final GridPane content = new GridPane();
			     content.setHgap(10);
			     content.setVgap(10);
			       
			     content.add(new Label("First Name:"), 0, 0);
			     content.add(fNameRemTxt, 1, 0);
			     GridPane.setHgrow(fNameRemTxt, Priority.ALWAYS);
			     content.add(new Label("Last Name:"), 0, 1);
			     content.add(lNameRemTxt, 1, 1);
			     GridPane.setHgrow(fNameRemTxt, Priority.ALWAYS);
			       
			     // create the dialog with a custom graphic and the gridpane above as the
			     // main content region
			     dlg.setResizable(false);
			     dlg.setIconifiable(false);
			     dlg.setGraphic(new ImageView(HelloValidation.class.getResource("apertureLogo.png").toString()));
			     dlg.setContent(content);
			     dlg.getActions().addAll(actionAddUser, Dialog.Actions.CANCEL);
			     validateRemoveUser();
			       
			     Platform.runLater(new Runnable() {
			         public void run() {
			             fNameRemTxt.requestFocus();
			         }
			     });

			     dlg.show();
			}
			final TextField fNameRemTxt = new TextField();
			final TextField lNameRemTxt = new TextField();
			final Action actionRemUser = new AbstractAction("Remove User") {
			    // This method is called when the login button is clicked ...
			    public void handle(ActionEvent ae) {
			        Dialog d = (Dialog) ae.getSource();
			        // Do the login here.
			        
			        String fName = fNameRemTxt.getText().trim();
			        String lName = lNameRemTxt.getText().trim();
			        try {			        	
			        	library.removeUser(fName, lName);
			        	Dialogs.create()
				        .owner(stage)
				        .title("Success")
				        .masthead("User Removed")
				        .message(fName+" "+lName+" was removed from the library database")
				        .showInformation();
			        } catch( Exception e ){
			        	Dialogs.create()
				        .owner(stage)
				        .title("Error")
				        .masthead("Problem Removing User")
				        .message(e.toString())
				        .showError();
			        }
			        librarian = null;
			        d.hide();
			    }
			};
			 // This method is called when the user types into the text fields  
			 private void validateRemoveUser() {
			     actionAddUser.disabledProperty().set( 
			           fNameRemTxt.getText().trim().isEmpty() || lNameRemTxt.getText().trim().isEmpty());
			 }
			 
				/*
				 * remove librarian
				 */
				@FXML private void removeLibrarianButtonPressed( ActionEvent ae ){
					Optional<String> response = Dialogs.create()
					        .owner(stage)
					        .title("Librarian Login")
					        .masthead("Enter Librarian ID")
					        .message("ID#")
					        .showTextInput();

					// get response 
					if (response.isPresent()) {
				        try {
				        	int lID = Integer.parseInt(response.get());
				        	library = Library.getInstance();
				        	librarian = library.findLibrarian(lID);
						} catch (NumberFormatException | UserNotFoundException e) {
							Dialogs.create()
					        .owner(stage)
					        .title("Error")
					        .masthead("Librarian Not Found")
					        .message(e.toString())
					        .showError();
						}
					} else {
						return;
					}

					if( librarian == null ) return;
						
						Optional<String> response2 = Dialogs.create()
						        .owner(stage)
						        .title("Remove Librarian")
						        .masthead("Enter Librarian ID")
						        .message("ID#")
						        .showTextInput();

						// get response 
						if (response2.isPresent()) {
					        try {
					        	int lID = Integer.parseInt(response.get());
					        	library = Library.getInstance();
					        	Librarian l = library.findLibrarian(lID);
					        	library.removeLibrarian(lID);
				            	Dialogs.create()
				    	        .owner(stage)
				    	        .title("Success")
				    	        .masthead("Librarian Removed")
				    	        .message(l+" was removed from the library database")
				    	        .showInformation();
							} catch (Exception e) {
								Dialogs.create()
						        .owner(stage)
						        .title("Error")
						        .masthead("There was problem removing the Libraian")
						        .message(e.toString())
						        .showError();
							}
						}
						user = null;
						librarian = null;
				}
			
				/*
				 * monitor system
				 */
				@FXML private void monitorSystemButtonPressed( ActionEvent ae ){
					Optional<String> response = Dialogs.create()
					        .owner(stage)
					        .title("Librarian Login")
					        .masthead("Enter Librarian ID")
					        .message("ID#")
					        .showTextInput();

					// get response 
					if (response.isPresent()) {
				        try {
				        	int lID = Integer.parseInt(response.get());
				        	library = Library.getInstance();
				        	librarian = library.findLibrarian(lID);
						} catch (NumberFormatException | UserNotFoundException e) {
							Dialogs.create()
					        .owner(stage)
					        .title("Error")
					        .masthead("Librarian Not Found")
					        .message(e.toString())
					        .showError();
						}
					} else {
						return;
					}

					if( librarian == null ) return;

					library = Library.getInstance();
					ArrayList<User> users = library.getAllUsers();
					
	            	Dialogs.create()
	    	        .owner(stage)
	    	        .title("Monitor System")
	    	        .masthead("User List")
	    	        .message(users.toString())
	    	        .showInformation();
	            	
	            	ArrayList<Item> items = library.getAllItems();
	            	Dialogs.create()
	    	        .owner(stage)
	    	        .title("Monitor System")
	    	        .masthead("Item List")
	    	        .message(items.toString())
	    	        .showInformation();
	            	
	            	ArrayList<Loan> loans = library.getAllLoans();
	            	Dialogs.create()
	    	        .owner(stage)
	    	        .title("Monitor System")
	    	        .masthead("Loan List")
	    	        .message(loans.toString())
	    	        .showInformation();
	            	
					librarian = null;
				}

	@Override
	public void start(Stage stage) throws Exception {
		this.stage = stage;
		this.library = Library.getInstance();
		this.librarian = null;
		this.user = null;
		
		Parent root = FXMLLoader.load(getClass().getResource("Librarianterminal.fxml"));
		Scene scene = new Scene(root);
		
		stage.setScene(scene);
		stage.setTitle("COMP4004 Library");
		stage.show();
	}

	public static void main(String[] args) {
		launch(args);
	}


	@Override
	public void initialize(URL location, ResourceBundle resources) {
		// setup listview
		library = Library.getInstance();
		ArrayList<Title> temp = library.getAllTitles();
		ObservableList<Title> titles = FXCollections.observableArrayList();
		for( Title t : temp ){
			titles.add(t);
		}
		titlesCol.setCellValueFactory(
			    new PropertyValueFactory<Title,String>("_title")
		);
		authorsCol.setCellValueFactory(
			    new PropertyValueFactory<Title,String>("_author")
		);
		isbnsCol.setCellValueFactory(
			    new PropertyValueFactory<Title,String>("_isbn")
		);
		titlesTable.setItems(titles);
        //Add change listener
        titlesTable.getSelectionModel().selectedItemProperty().addListener((observableValue, oldValue, newValue) -> {
            if (titlesTable.getSelectionModel().getSelectedItem() != null) {
                this.selection = newValue;
            }
        });
	}
	
	private void updateList(){
		library = Library.getInstance();
		ArrayList<Title> temp = library.getAllTitles();
		ObservableList<Title> titles = FXCollections.observableArrayList();
		for( Title t : temp ){
			titles.add(t);
		}
		titlesCol.setCellValueFactory(
			    new PropertyValueFactory<Title,String>("_title")
		);
		authorsCol.setCellValueFactory(
			    new PropertyValueFactory<Title,String>("_author")
		);
		isbnsCol.setCellValueFactory(
			    new PropertyValueFactory<Title,String>("_isbn")
		);
		titlesTable.setItems(titles);
	}
}
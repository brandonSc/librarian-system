package schurmanb.comp4004.a1.src;

import java.sql.*;
import java.util.ArrayList;

import org.joda.time.LocalDate;

public class Library
{
	private static Library uniqueInstance; 	// only one Library instance may exist
	private Connection db; 					// library back-end database
	
	/**
	 * obtain the single instance of Library
	 * @return unique instance of Library class
	 */
	public static Library getInstance(){
		if( uniqueInstance == null ){
			uniqueInstance = new Library();
		}
		return uniqueInstance;
	}

	/**
	 * Cannot call constructor explicitly (private). See getInstance()
	 */
	private Library(){
		try {
			// init database
			Class.forName("org.sqlite.JDBC");
			db = DriverManager.getConnection("jdbc:sqlite:LIB.db");
			db.setTransactionIsolation(Connection.TRANSACTION_SERIALIZABLE);
			db.setAutoCommit(false);
		} catch( SQLException e ){
			e.printStackTrace();
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
			return;
		}
		// create database tables
		createTables();
	}
	
	public Loan renewLoan( Loan loan, User user, int numDays ){
		Loan newLoan = new Loan(user,loan.getItem(),new LocalDate(),new LocalDate().plusDays(numDays));
		try {
			Statement stmnt = db.createStatement();
			String sql = "update loans set dueDate="+newLoan.getDueDate()+" where iID="+loan.getItem().getItemID()+";";
			stmnt.executeUpdate(sql);
			stmnt.close();
			db.commit();
		} catch( SQLException e ){
			e.printStackTrace();
			return null;
		}
		return newLoan;
	}
	
	/**
	 * Remove an overdue Loan from the user
	 * @param user with overdue loan
	 * @param item that is overdue
	 * @throws TitleNotFoundException
	 * @throws ItemNotFoundException
	 */
	public void collectFine( User user, Item item ) throws TitleNotFoundException, ItemNotFoundException {
		// double check the item exists
		if( findItem(item.getItemID()) != null ){
			try {
				// some stuff with money should be done here, realistically.
				Statement stmnt = db.createStatement();
				String sql = "delete from loans where iID="+item.getItemID()+";";
				stmnt.executeUpdate(sql);
				stmnt.close();
				db.commit();
			} catch( SQLException e ){
				e.printStackTrace();
			}
		}
	}
	
	/**
	 * return a loaned item
	 * @param user
	 * @param item
	 * @throws ItemNotFoundException  
	 * @throws TitleNotFoundException 
	 */
	public void returnLoancopy( User user, Item item ) throws TitleNotFoundException, ItemNotFoundException {
		// double check the item exists
		if( findItem(item.getItemID()) != null ){
			try {
				// some stuff with money should be done here, realistically.
				Statement stmnt = db.createStatement();
				String sql = "delete from loans where iID="+item.getItemID()+";";
				stmnt.executeUpdate(sql);
				stmnt.close();
				db.commit();
			} catch( SQLException e ){
				e.printStackTrace();
			}
		}
	}
	
	/**
	 * finds a Loan by Item
	 * @param item
	 * @return 
	 */
	public Loan findLoan( User user, Item item ) throws ItemNotFoundException {
		Loan l = null;
		try {
			Statement stmnt = db.createStatement();
			String sql = "select * from loans where iID="+item.getItemID()+";";
			ResultSet rs = stmnt.executeQuery(sql);
			if( rs.next() ){
				LocalDate startDate = LocalDate.parse(rs.getString("startDate"));
				LocalDate dueDate = LocalDate.parse(rs.getString("dueDate"));
				l = new Loan(user, item, startDate, dueDate);
			} else {
				rs.close();
				stmnt.close();				
				db.commit();
				throw new ItemNotFoundException("Item "+item+" is not Loaned by "+user);
			}
			rs.close();
			stmnt.close();
			db.commit();
		} catch( SQLException e ){
			e.printStackTrace();
		}
		return l;
	}

	/**
	 * allows a User to borrow an Item, 
	 * if the borrower does not have more than 10 loans,
	 * if they do not have an overdue loan.
	 * @param borrower the User that is borrowing the Item
	 * @param item the Item the User wishes to borrow
	 * @return new Loan object containing start and due dates
	 * @throws TitleNotFoundException  
	 */
	public Loan borrowLoancopy( Item item, User borrower, LocalDate dueDate ) throws TitleNotFoundException, CannotLoanException {
		Loan loan = null;
		// check Title exists first
		Title t = findTitle(item.getReferencingTitle().getISBN());
		// check user is able to borrower book (ie they have less than 10 loans and none are overdue)
		ArrayList<Loan> loans = getAllLoans(borrower);
		if( loans.size() >= 10 ){
			throw new CannotLoanException(borrower+" cannot borrow more than 10 Items.");
		} else {
			for( Loan l : loans ){
				if( l.isOverDue() ){
					throw new CannotLoanException(borrower+" has an overdue Item. Please collect this fine first.");
				}
			}
		}
		if( t != null ){
			// check if the copy is available for loan
			try {
				Statement stmnt = db.createStatement();
				String sql = "select count(*) from loans where iID="+item.getItemID();
				ResultSet rs = stmnt.executeQuery(sql);
				rs.next();
				int count = rs.getInt(1);
				rs.close();
				stmnt.close();
				db.commit();
				if( count == 0 ){
					// the Item is not currently on loan, OK to loan to borrower
					LocalDate curDate = new LocalDate();
					stmnt = db.createStatement();
					sql = "insert into loans(iID, uID, startDate, dueDate) values("
							+ item.getItemID()+","+borrower.getUserID()+",'"+curDate+"','"+dueDate+"');";
					loan = new Loan(borrower,item,curDate,dueDate);
					stmnt.executeUpdate(sql);
					stmnt.close();
					db.commit();
				} else {
					throw new CannotLoanException("The Item with ID#"+item.getItemID()+" is currently being loaned.");
				}
			} catch( SQLException e ){
				e.printStackTrace();
			}
		} else {
			throw new TitleNotFoundException("corresponding Title was not found");
		}
		return loan;
	}
	
	/**
	 * gets all Loans a User currently owes
	 * @param user User to query
	 * @return an ArrayList of loans containing start and due dates
	 * @throws TitleNotFoundException  
	 */
	public ArrayList<Loan> getAllLoans( User user ){
		ArrayList<Loan> loans = new ArrayList<Loan>();
		try {
			Statement stmnt = db.createStatement();
			String sql = "select * from loans where uID="+user.getUserID()+";";
			ResultSet rs = stmnt.executeQuery(sql);
			while( rs.next() ){
				LocalDate startDate = LocalDate.parse(rs.getString("startDate"));
				LocalDate dueDate = LocalDate.parse(rs.getString("dueDate"));
				int iID = rs.getInt("iID");
				Item item;
				try {
					item = findItem(iID);
				} catch( TitleNotFoundException | ItemNotFoundException e ){
					e.printStackTrace();
					rs.close();
					stmnt.close();
					db.commit();
					return null;
				}
				loans.add(new Loan(user,item,startDate,dueDate));
			}
			rs.close();
			stmnt.close();
			db.commit();
		} catch( SQLException e ){
			e.printStackTrace();
		}
		return loans;
	}
	
	/**
	 * gets all Loans
	 * @return an ArrayList of loans containing start and due dates
	 * @throws TitleNotFoundException  
	 */
	public ArrayList<Loan> getAllLoans(){
		ArrayList<Loan> loans = new ArrayList<Loan>();
		try {
			Statement stmnt = db.createStatement();
			String sql = "select * from loans;";
			ResultSet rs = stmnt.executeQuery(sql);
			while( rs.next() ){
				LocalDate startDate = LocalDate.parse(rs.getString("startDate"));
				LocalDate dueDate = LocalDate.parse(rs.getString("dueDate"));
				int iID = rs.getInt("iID");
				int uID = rs.getInt("uID");
				Item item;
				User user;
				try {
					item = findItem(iID);
					user = findUser(uID);
				} catch( TitleNotFoundException | ItemNotFoundException | UserNotFoundException e ){
					e.printStackTrace();
					rs.close();
					stmnt.close();
					db.commit();
					return null;
				}
				loans.add(new Loan(user,item,startDate,dueDate));
			}
			rs.close();
			stmnt.close();
			db.commit();
		} catch( SQLException e ){
			e.printStackTrace();
		}
		return loans;
	}
	
	
	/**
	 * get all items in loans database table
	 * @return arraylist of items
	 */ 
	private ArrayList<Item> getLoanedItems(){
		ArrayList<Item> items = new ArrayList<Item>();
		try {
			Statement stmnt = db.createStatement();
			String sql = "select * from loans;";
			ResultSet rs = stmnt.executeQuery(sql);
			while( rs.next() ){
				int iID = rs.getInt("iID");
				Item item;
				try {
					item = findItem(iID);
				} catch( TitleNotFoundException | ItemNotFoundException e ){
					rs.close();
					stmnt.close();
					db.commit();
					e.printStackTrace();
					return null;
				}
				if( item != null ){
					items.add(item);
				}
			}
			rs.close();
			stmnt.close();
			db.commit();
		} catch( SQLException e ){
			e.printStackTrace();
		}
		return items;
	}
	
	/**
	 * checks if a user has an overdue loan. 
	 * a user cannot borrow another book if they have an overdue loan
	 */
	public boolean hasOverdueLoan( User user ){
		ArrayList<Loan> loans = getAllLoans(user);
		for( Loan l : loans ){
			if( l.isOverDue() ){
				return true;
			}
		}
		return false;
	}

	
	/**
	 * removes Item using it's unique item ID
	 * @param iID unique ID of book item
	 * @param l Librarian removing book (must be a librarian to perform this action)
	 */
	public void removeItem( int iID, Librarian l ){
		try {
			Statement stmnt = db.createStatement();
			String sql = "delete from items where iID="+iID;
			stmnt.executeUpdate(sql);
			stmnt.close();
			db.commit();
		} catch( SQLException e ){
			e.printStackTrace();
		}
	}
	
	/**
	 * get all items in database items table
	 * @return new arraylist 
	 */
	public ArrayList<Item> getAllItems(){
		ArrayList<Item> items = new ArrayList<Item>();
		try {
			Statement stmnt = db.createStatement();
			String sql = "select * from items;";
			ResultSet rs = stmnt.executeQuery(sql);
			while( rs.next() ){
				int iID = rs.getInt("iID");
				int isbn = rs.getInt("isbn");
				Title t = null;
				try {
					t = findTitle(isbn);
				} catch( TitleNotFoundException e ){
					e.printStackTrace();
				}
				items.add(new Item(iID, t));
			}
			rs.close();
			stmnt.close();
			db.commit();
		} catch( SQLException e ){
			e.printStackTrace();
		}
		return items;
	}
	

	/**
	 * finds some arbitrary copy of an Item with corresponding isbn
	 * such that the Item is not currently being Loaned to a User. 
	 * @param isbn
	 * @return new Item object, or null if there are no copies left
	 * @throws TitleNotFoundException  
	 * @throws ItemNotFoundException 
	 */
	public Item findSomeItem( int isbn ) throws ItemNotFoundException {
		ArrayList<Item> items = getAllItems();
		ArrayList<Item> loaned = getLoanedItems();
		for( Item i : items ){
			if( i.getReferencingTitle().getISBN().equals((Integer)isbn) ){
				if( loaned.contains(i) == false ){
					return i;
				}
			}
		}
		throw new ItemNotFoundException("There are no copies (Items) of isbn#"+isbn+".");
	}
	
	/**
	 * Find the Item object corresponding to the item ID 
	 * @param iID item ID#
	 * @return new Item object
	 * @throws TitleNotFoundException
	 * @throws ItemNotFoundException
	 */
	public Item findItem( int iID ) throws TitleNotFoundException, ItemNotFoundException {
		Item item = null;
		try {
			Statement stmnt = db.createStatement();
			String sql = "select * from items where iID="+iID+";";
			ResultSet rs = stmnt.executeQuery(sql);
			if( rs.next() ){
				int isbn = rs.getInt("isbn");
				Title t = findTitle(isbn);
				item = new Item(iID, t);
			} else {
				rs.close();
				stmnt.close();
				db.commit();
				throw new ItemNotFoundException("The Item with ID#"+iID+" was not found");
			}
			rs.close();
			stmnt.close();
			db.commit();
		} catch( SQLException e ){
			e.printStackTrace();
		}
		return item;
	}
	
	/**
	 * adds an Item to the database using Title information. 
	 * the corresponding Title must already be entered. 
	 * @param isbn product code (must be same as in Title)
	 * @param lID ID number of librarian adding the Item
	 * @return new Item object
	 * @throws TitleNotFoundException
	 */
	public Item addItem( int isbn, Librarian l ) throws TitleNotFoundException {
		Title t = findTitle(isbn);
		return addItem(t,l);
	}
	
	/**
	 * adds an Item using a corresponding Title object
	 * @param t corresponding Title object 
	 * @param lID ID number of librarian adding Item
	 * @return new Item object
	 * @throws TitleNotFoundException
	 */
	public Item addItem( Title t, Librarian l ) throws TitleNotFoundException {
		Item item = null;
		if( findTitle(t.getISBN()) != null ){
			try {
				// get count first
				Statement stmnt = db.createStatement();
				String sql = "select count(*) from items where isbn="+t.getISBN()+";";
				ResultSet rs = stmnt.executeQuery(sql);
				rs.next();
				int count = rs.getInt(1) + 1;
				rs.close();
				stmnt.close();
				db.commit();
				// add new item with it's copy number
				stmnt = db.createStatement();
				sql = "insert into items(isbn,lID,copyNum) values("
						+ t.getISBN()+","+l.getLibrarianID()+","+count+");";
				stmnt.executeUpdate(sql);
				stmnt.close();
				db.commit();
				// get the unique item ID 
				stmnt = db.createStatement();
				sql = "select * from items where isbn="+t.getISBN()+" and copyNum="+count+";";
				rs = stmnt.executeQuery(sql);
				rs.next();
				int iID = rs.getInt(1);
				rs.close();
				stmnt.close();
				db.commit();
				item = new Item(iID, t);
			} catch (SQLException e) {
				throw new TitleNotFoundException("Could not add "+t.getISBN()
						+ ". Please ensure that the Title (ie it's isbn) is not already in the system.");
			}
		} else {
			throw new TitleNotFoundException("A Title with isbn "+t.getISBN()+" was not found.");
		}
		return item;
	}
	
	/**
	 * adds a title to the database 
	 * @param title name of the book
	 * @param author writer of the book
	 * @param isbn unique product code 
	 * @param lID librarian ID of participant
	 * @return new Title object
	 */
	public Title addTitle( String title, String author, int isbn, int lID ) throws AddTitleException {
		try {
			Statement stmnt = db.createStatement();
			String sql = "insert into titles(title,author,isbn,lID) values("
					+ "'"+title+"',"+"'"+author+"',"+isbn+","+lID+");";
			stmnt.executeUpdate(sql);
			stmnt.close();
			db.commit();
		} catch( SQLException e ){
			throw new AddTitleException("Could not add "+title+" by "+author
					+ ". Please ensure that the Title (ie it's isbn) is not already in the system.");
		}
		return new Title(title,author,isbn,lID);
	}
	
	/**
	 * get all titles in database
	 * @return new ArrayList of Titles
	 */
	public ArrayList<Title> getAllTitles(){
		ArrayList<Title> titles = new ArrayList<Title>();
		try { 
			Statement stmnt = db.createStatement();
			String sql = "select * from titles;";
			ResultSet rs = stmnt.executeQuery(sql);
			while( rs.next() ){
				int isbn = rs.getInt("isbn");
				String title = rs.getString("title");
				String author = rs.getString("author");
				int lID = rs.getInt("lID");
				titles.add(new Title(title,author,isbn,lID));
			}
			rs.close();
			stmnt.close();
			db.commit();
		} catch( SQLException e ){
			e.printStackTrace();
			return null;
		}
		return titles;
	}
	
	/**
	 * find a Title by name
	 * @param title of book
	 * @param author of book
	 * @return new Title object
	 * @throws TitleNotFoundException
	 */
	public Title findTitle( String title, String author ) throws TitleNotFoundException {
		Title t = null;
		try { 
			Statement stmnt = db.createStatement();
			String sql = "select * from title where title='"+title+"' and lName='"+author+"';";
			ResultSet rs = stmnt.executeQuery(sql);
			if( rs.next() ){
				int isbn = rs.getInt("isbn");
				int uID = rs.getInt("uID");
				t = new Title(title,author,isbn,uID);
			} else {
				throw new TitleNotFoundException(title+" by "+author+" cannot be found.");
			}
			stmnt.close();
			rs.close();
			db.commit();
		} catch( SQLException e ){
			throw new TitleNotFoundException("a database error has occurred. Please try the operation again."
					+ "If the problem persists, please report to the administrator, or contact <developer name>");
		}
		return t;
	}
	
	/**
	 * find a Title by isbn code
	 * @param isbn unique product code
	 * @return new Title object
	 * @throws TitleNotFoundException
	 */
	public Title findTitle( int isbn ) throws TitleNotFoundException {
		Title t = null;
		try { 
			Statement stmnt = db.createStatement();
			String sql = "select * from titles where isbn="+isbn+";";
			ResultSet rs = stmnt.executeQuery(sql);
			if( rs.next() ){
				String title = rs.getString("title");
				String author = rs.getString("author");
				int uID = rs.getInt("lID");
				t = new Title(title,author,isbn,uID);
			} else {
				throw new TitleNotFoundException(isbn+" cannot be found.");
			}
			stmnt.close();
			rs.close();
			db.commit();
		} catch( SQLException e ){
			throw new TitleNotFoundException("a database error has occurred. Please try the operation again."
					+ "If the problem persists, please report to the administrator, or contact <developer name>");
		}
		return t;
	}
	
	/**
	 * removes a Title from the database using name and author of book
	 * @param title name of book 
	 * @param author author of book
	 * @throws TitleNotFoundException
	 */
	public void removeTile( String title, String author ) throws TitleNotFoundException {
		Title t = findTitle(title,author);
		if( t != null ){
			try {
				Statement stmnt = db.createStatement();
				String sql = "delete from titles where isbn="+t.getISBN()+";";
				stmnt.executeUpdate(sql);
				stmnt.close();
				db.commit();
			} catch( SQLException e ){
				e.printStackTrace();
			}
		} else { 
			throw new TitleNotFoundException("Title "+title+" by "+author+" was not found.");
		}
	}
	
	/**
	 * removes a title from the databse using isbn#
	 * @param isbn unique code for title
	 * @throws TitleNotFoundException
	 */
	public void removeTitle( int isbn ) throws TitleNotFoundException {
		if( findTitle(isbn) != null ){
			try {
				Statement stmnt = db.createStatement();
				String sql = "delete from titles where isbn="+isbn+";";
				stmnt.executeUpdate(sql);
				stmnt.close();
				db.commit();
				stmnt = db.createStatement();
				sql = "delete from items where isbn="+isbn+";";
				stmnt.executeUpdate(sql);
				stmnt.close();
				db.commit();
			} catch( SQLException e ){
				e.printStackTrace();
			}
		} else { 
			throw new TitleNotFoundException("Title "+isbn+" was not found.");
		}
	}
	
	/**
	 * removes a title from the database
	 * @param title to remove
	 * @throws TitleNotFoundException
	 */
	public void removeTitle( Title title ) throws TitleNotFoundException {
		removeTitle(title.getISBN());
	}
	
	/**
	 * add a librarian to the database.
	 * if the librarian is not already a user, then they are automatically added as a user first
	 * @param fName first name of librarian
	 * @param lName last name of librarian
	 * @return new Librarian object
	 * @throws AddUserException
	 */
	public Librarian addLibrarian( String fName, String lName ) throws AddUserException {
		User u = null;
		int lID = -1;
		try { 
			u = findUser(fName, lName);
		} catch( UserNotFoundException e ){
			u = addUser(fName, lName);
		}
		if( u == null || u.getUserID() < 0 ){
			throw new AddUserException("there was a problem adding the Librarian as a User "+fName+" "+lName);
		} else {
			try {
				// add user to database
				Statement stmnt = db.createStatement();
				String sql = "insert into librarians(uID)"
						+ "values("+u.getUserID()+");";
				stmnt.executeUpdate(sql);
				// retrieve uID from database
				stmnt = db.createStatement();
				sql = "select * from librarians where "
						+ "uID="+u.getUserID()+";";
				stmnt.close();
				db.commit();
				ResultSet rs = stmnt.executeQuery(sql);
				if( rs.next() ){
					lID = rs.getInt("lID");
				}				stmnt.close();
				rs.close();
				db.commit();
			} catch( SQLException e ){
				throw new AddUserException("there was a problem adding Librarian "+fName+" "+lName);
			}
		}
		return new Librarian(fName,lName,u.getUserID(),lID);
	}
	
	/**
	 * adds a user to the library database.
	 * the candidate key (fName, lName) must be unique for each user
	 * @param fName first name of user
	 * @param lName last name of user
	 * @return a new User object
	 */
	public User addUser( String fName, String lName ) throws AddUserException {
		User user = null;
		try {
			// add user to database
			Statement stmnt = db.createStatement();
			String sql = "insert into users(fName,lName)"
					+ "values('"+fName+"','"+lName+"');";
			stmnt.executeUpdate(sql);
			// retrieve uID from database
			stmnt = db.createStatement();
			sql = "select * from users where "
					+ "fName='"+fName+"' and lName='"+lName+"';";
			stmnt.close();
			db.commit();
			ResultSet rs = stmnt.executeQuery(sql);
			if( rs.next() ){
				int uID = rs.getInt("uID");
				user = new User(fName,lName,uID);
			}
			stmnt.close();
			rs.close();
			db.commit();
		} catch( SQLException e ){
			throw new AddUserException("there was a problem adding user '"
					+ fName+" "+lName+"'. Please ensure this first-last name combination is unique.");
		}
		return user;
	}
	
	/**
	 * get all users in database
	 * @return new ArrayList of Users
	 */
	public ArrayList<User> getAllUsers(){
		ArrayList<User> users = new ArrayList<User>();
		try {
			Statement stmnt = db.createStatement();
			String sql = "select * from users;";
			ResultSet rs = stmnt.executeQuery(sql);
			while( rs.next() ){
				int uID = rs.getInt("uID");
				String fName = rs.getString("fName");
				String lName = rs.getString("lName");
				users.add(new User(fName,lName, uID));
			}
			rs.close();
			stmnt.close();
			db.commit();
		} catch( SQLException e ){
			e.printStackTrace();
			return null;
		}
		return users;
	}
	
	/**
	 * locates a user's id in the database using their name
	 * @param fName first name of user 
	 * @param lName last name of user
	 * @return new User object
	 */ 
	public User findUser( String fName, String lName ) throws UserNotFoundException {
		User user = null;
		try { 
			Statement stmnt = db.createStatement();
			String sql = "select * from users where fName='"+fName+"' and lName='"+lName+"';";
			ResultSet rs = stmnt.executeQuery(sql);
			if( rs.next() ){
				int uID = rs.getInt("uID");
				user = new User(fName,lName,uID);
			} else {
				throw new UserNotFoundException("the user '"+fName+" "+lName+"' cannot be found. Please add the User first.");
			}
			stmnt.close();
			rs.close();
			db.commit();
		} catch( SQLException e ){
			throw new UserNotFoundException("a database error has occurred. Please try the operation again."
					+ "If the problem persists, please report to the administrator, or contact <developer name>");
		}
		return user;
	}
	
	/**
	 * Finds librarian using user information
	 * note that this happens automatically through findUser()
	 * @param uID
	 * @return a new Librarian object
	 * @throws UserNotFoundException 
	 */
	public Librarian findLibrarian( User user ) throws UserNotFoundException {
		Librarian librarian = null;
		try { 
			Statement stmnt = db.createStatement();
			String sql = "select * from librarians where uID="+user.getUserID()+";";
			ResultSet rs = stmnt.executeQuery(sql);
			if( rs.next() ){
				int lID = rs.getInt("lID");
				librarian = new Librarian(user.getFirstName(),user.getLastName(),user.getUserID(),lID);
			} else {
				stmnt.close();
				rs.close();
				db.commit();
				throw new UserNotFoundException("the user "+user+" cannot be found as a Librarian.");
			}
			stmnt.close();
			rs.close();
			db.commit();
		} catch( SQLException e ){
			throw new UserNotFoundException("a database error has occurred. Please try the operation again."
					+ "If the problem persists, please report to the administrator, or contact <developer name>");
		}
		return librarian;
	}
	
	/**
	 * Finds librarian using id
	 * note that this happens automatically through findUser()
	 * @param uID
	 * @return a new Librarian object
	 * @throws UserNotFoundException 
	 */
	public Librarian findLibrarian( int lID ) throws UserNotFoundException {
		Librarian librarian = null;
		try { 
			Statement stmnt = db.createStatement();
			String sql = "select * from librarians where lID="+lID+";";
			ResultSet rs = stmnt.executeQuery(sql);
			int uID = -1;
			if( rs.next() ){
				uID = rs.getInt("uID");
			} else {
				stmnt.close();
				rs.close();
				db.commit();
				throw new UserNotFoundException("the librarian ID cannot be found as a Librarian.");
			}
			stmnt.close();
			rs.close();
			db.commit();
			User user = findUser(uID);
			librarian = new Librarian(user.getFirstName(),user.getLastName(),user.getUserID(),lID);
		} catch( SQLException e ){
			throw new UserNotFoundException("a database error has occurred. Please try the operation again."
					+ "If the problem persists, please report to the administrator, or contact <developer name>");
		}
		return librarian;
	}
	
	/**
	 * locates a user's info in the database using their ID#
	 * @param uID id of the user to find
	 * @return a new User object
	 */ 
	public User findUser( int uID ) throws UserNotFoundException {
		User user = null;
		try { 
			Statement stmnt = db.createStatement();
			String sql = "select * from users where uID="+uID+";";
			ResultSet rs = stmnt.executeQuery(sql);
			if( rs.next() ){
				String fName = rs.getString("fName");
				String lName = rs.getString("lName");
				user = new User(fName,lName,uID);
			} else {
				throw new UserNotFoundException("the user ID#="+uID+" was not found.");
			}
			stmnt.close();
			rs.close();
			db.commit();
		} catch( SQLException e ){
			throw new UserNotFoundException("a database error has occurred. Please try the operation again."
					+ "If the problem persists, please report to the administrator, or contact <developer name>");
		}
		return user;
	}
	
	/**
	 * removes a Librarian by their ID#
	 * @param lID use .getLibraianID()
	 */
	public void removeLibrarian( int lID ){
		try {
			Statement stmnt = db.createStatement();
			String sql = "delete from librarians where lID="+lID+";";
			stmnt.executeUpdate(sql);
			stmnt.close();
			db.commit();
		} catch( SQLException e ){
			e.printStackTrace();
		}
	}
	
	/**
	 * removes a user by id
	 * @param uID id# of user to remove
	 */
	public void removeUser( int uID ) throws UserNotFoundException {
		User user = findUser(uID);
		if( user != null ){
			try {
				removeLibrarian(uID);
				Statement stmnt = db.createStatement();
				String sql = "delete from users where uID="+user.getUserID()+";";
				stmnt.executeUpdate(sql);
				stmnt.close();
				db.commit();
			} catch( SQLException e ){
				e.printStackTrace();
			}
		} else { 
			throw new UserNotFoundException("user was not found.");
		}
	}
	
	/**
	 * removes a user by name
	 * @param fName first name of user to remove
	 * @param lName last name of user to remove
	 */
	public void removeUser( String fName, String lName ) throws UserNotFoundException {
		User user = findUser(fName, lName);
		removeUser(user.getUserID());
	}
	
	/**
	 * removes a User
	 * @param user User object to remove
	 * @throws UserNotFoundException
	 */
	public void removeUser( User user ) throws UserNotFoundException {
		removeUser(user.getUserID());
	}
	
	/**
	 * create database tables
	 */
	public void createTables(){
		try {
			// create user table
			Statement stmnt = db.createStatement();
			String sql = "create table if not exists users("
					+ "uID integer primary key autoincrement,"
					+ "fName text not null,"
					+ "lName text not null,"
					+ "unique(fName, lName));";
			stmnt.executeUpdate(sql);
			stmnt.close();
			db.commit();
			
			// create librarians table
			stmnt = db.createStatement();
			sql = "create table if not exists librarians("
					+ "lID integer primary key autoincrement,"
					+ "uID integer not null,"
					+ "foreign key(uID) references users(uID));";
			stmnt.executeUpdate(sql);
			stmnt.close();
			db.commit();
			
			// create titles table
			stmnt = db.createStatement();
			sql = "create table if not exists titles("
					+ "isbn integer primary key,"
					+ "title text not null,"
					+ "author text not null,"
					+ "lID integer not null,"
					+ "foreign key(lID) references librarians(lID)"
					+ "unique(title, author) on conflict ignore);";
			stmnt.executeUpdate(sql);
			stmnt.close();
			db.commit();
			
			// create items table
			stmnt = db.createStatement();
			sql = "create table if not exists items("
					+ "iID integer primary key autoincrement,"
					+ "isbn integer not null,"
					+ "lID integer not null,"
					+ "copyNum integer not null,"
					+ "foreign key(isbn) references titles(isbn),"
					+ "foreign key(lID) references librarians(lID));";
			stmnt.executeUpdate(sql);
			stmnt.close();
			db.commit();
			
			// create loans table
			stmnt = db.createStatement();
			sql = "create table if not exists loans("
					+ "iID integer primary key,"
					+ "uID integer not null,"
					+ "startDate text not null,"
					+ "dueDate text not null,"
					+ "unique(iID,uID));";
			stmnt.executeUpdate(sql);
			stmnt.close();
			db.commit();	
		} catch( SQLException e ){
			e.printStackTrace();
		}
	}
	
	/**
	 * For Debugging purposes, 
	 * this will erase and recreate the entire database.
	 */
	public void clearDB(){
		try {
			// drop users
			Statement stmnt = db.createStatement();
			String sql = "drop table if exists users;";
			stmnt.executeUpdate(sql);
			stmnt.close();
			db.commit();
			
			// drop librarians
			stmnt = db.createStatement();
			sql = "drop table if exists librarians;";
			stmnt.executeUpdate(sql);
			stmnt.close();
			db.commit();
			
			// drop items
			stmnt = db.createStatement();
			sql = "drop table if exists items;";
			stmnt.executeUpdate(sql);
			stmnt.close();
			db.commit();
			
			// drop titles
			stmnt = db.createStatement();
			sql = "drop table if exists titles;";
			stmnt.executeUpdate(sql);
			stmnt.close();
			db.commit();
			
			// drop loans
			stmnt = db.createStatement();
			sql = "drop table if exists loans;";
			stmnt.executeUpdate(sql);
			stmnt.close();
			db.commit();
		} catch( Exception e ){
			e.printStackTrace();
		}
		
		createTables();
	}
}

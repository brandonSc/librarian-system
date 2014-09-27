package schurmanb.comp4004.a1.src;

import java.sql.*;

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
					+ "copyNum integer not null,"
					+ "lID integer not null,"
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
					+ "startData data not null,"
					+ "dueDate data not null,"
					+ "foreign key(iID) references items(iID),"
					+ "foreign key(uID) references users(uID));";
			stmnt.executeUpdate(sql);
			stmnt.close();
			db.commit();
		} catch( Exception e ){
			e.printStackTrace();
		}
	}
	
	/**
	 * 
	 * @param borrower the User that is borrowing the Item
	 * @param item the Item the User wishes to borrow
	 * @return true on successful loan, false if there was a problem (ie outstanding fines)
	 * @throws TitleNotFoundException  
	 */
	public boolean borrowLoancopy( Item item, User borrower ) throws TitleNotFoundException {
		Title t = findTitle(item.getReferencingTitle().getISBN()); // check Title exists first
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
					stmnt = db.createStatement();
					sql = "insert into loans(iID, uID) values("
							+ item.getItemID()+", "+borrower.getUserID()+");";
					return true;
				}
			} catch( SQLException e ){
				e.printStackTrace();
				return false;
			}
		} else {
			return false;
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
	 * finds a copy of an Item with corresponding isbn. 
	 * The copy returned will be the most recent one added.
	 * @param isbn
	 * @return new Item object
	 * @throws TitleNotFoundException  
	 */
	public Item findItem( int isbn ) throws TitleNotFoundException {
		Item item = null;
		Title t = findTitle(isbn);
		try {
			Statement stmnt = db.createStatement();
			String sql = "select count(*) from items where isbn="+isbn+";";
			ResultSet rs = stmnt.executeQuery(sql);
			rs.next();
			int copyNum = rs.getInt(1);
			rs.close();
			stmnt.close();
			db.commit();
			stmnt = db.createStatement();
			sql = "select * from items where isbn="+isbn+" and copyNum="+copyNum+";";
			rs = stmnt.executeQuery(sql);
			if( rs.next() ){
				item = new Item(copyNum, t);
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
	
	public void removeTitle( int isbn ) throws TitleNotFoundException {
		if( findTitle(isbn) != null ){
			try {
				Statement stmnt = db.createStatement();
				String sql = "delete from titles where isbn="+isbn+";";
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
				throw new UserNotFoundException("the user '"+fName+" "+lName+"' cannot be found.");
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
	
	public void removeLibrarian( int uID ){
		try {
			Statement stmnt = db.createStatement();
			String sql = "delete from librarians where uID="+uID+";";
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
			
			// create user table
			stmnt = db.createStatement();
			sql = "create table if not exists users("
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
					+ "startData data not null,"
					+ "dueDate data not null,"
					+ "foreign key(iID) references items(iID),"
					+ "foreign key(uID) references users(uID));";
			stmnt.executeUpdate(sql);
			stmnt.close();
			db.commit();
		} catch( Exception e ){
			e.printStackTrace();
		}
	}
}

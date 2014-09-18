package schurmanb.comp4004.a1.src;

import java.sql.*;

public class Library
{
	public static Library uniqueInstance; 	// only one Library instance may exist
	
	private Connection db; 	// library back-end database

	/**
	 * Cannot call constructor explicitly. See getInstance()
	 */
	private Library(){
		// init database
		try {
			Class.forName("org.sqlite.JDBC");
			db = DriverManager.getConnection("jdbc:sqlite:LIB.db");
			db.setTransactionIsolation(Connection.TRANSACTION_SERIALIZABLE);
			db.setAutoCommit(false);
			Statement stmt = db.createStatement();
			String sql = "create table if not exists users("
					+ "uID integer primary key autoincrement,"
					+ "fName not null,"
					+ "lName not null,"
					+ "unique(fName, lName) on conflict ignore);";
			stmt.executeUpdate(sql);
			stmt.close();
			db.commit();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * @return unique instance of Library class
	 */
	public static Library getInstance(){
		if( uniqueInstance == null ){
			uniqueInstance = new Library();
		}
		return uniqueInstance;
	}
	
	/**
	 * adds a user to the library database
	 * @param fName first name of user
	 * @param lName last name of user
	 */
	public int addUser( String fName, String lName ){
		int uID = -1;
		try {
			// add user to database
			Statement stmnt = db.createStatement();
			String sql = "insert into users(fName,lName)"
					+ "values('"+fName+"','"+lName+"');";
			stmnt.executeUpdate(sql);
			db.commit();
			// retrieve uID from database
			stmnt = db.createStatement();
			sql = "select * from users where "
					+ "fName='"+fName+"' and lName='"+lName+"';";
			ResultSet rs = stmnt.executeQuery(sql);
			if( rs.next() ){
				uID = rs.getInt("uID");
			}
			db.commit();
		} catch( Exception e ){
			e.printStackTrace();
		}
		return uID;
	}
	
	public void removeUser( int uID ){
		try {
			Statement stmnt = db.createStatement();
			String sql = "delete from users where uID="+uID+";";
			stmnt.executeUpdate(sql);
			db.commit();
		} catch( SQLException e ){
			e.printStackTrace();
		}
	}
	
	public void removeUser( String fName, String lName ){
		try {
			Statement stmnt = db.createStatement();
			String sql = "delete from users where fName='"+fName+"' and lName='"+lName+"';";
			stmnt.executeUpdate(sql);
			db.commit();
		} catch( SQLException e ){
			e.printStackTrace();
		}
	}
}

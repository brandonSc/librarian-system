package schurmanb.comp4004.a1.src;

import java.util.ArrayList;

public class User
{
	private String fName; 	// first name of user
	private String lName; 	// last name of user
	private Integer uID; 	// id# generated for user by Library database
	
	public User( String fName, String lName, int uID ){
		this.fName = fName;
		this.lName = lName;
		this.uID = uID;
	}
	
	public String getFirstName(){
		return this.fName;
	}
	
	public String getLastName(){
		return this.lName;
	}
	
	public Integer getUserID(){
		return this.uID;
	}
	
	@Override
	public String toString(){
		return this.fName + " " + this.lName + ", ID#" + uID;
	}
}

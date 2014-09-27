package schurmanb.comp4004.a1.src;

public class Librarian extends User
{
	private Integer lID; 	// unique ID of librarian. Librarians also have a user uID
	
	public Librarian( String fName, String lName, int uID, int lID ){
		super(fName, lName, uID);
		this.lID = lID;
	}
	
	public Integer getLibrarianID(){
		return this.lID;
	}
	
	@Override
	public String toString(){
		return super.toString() + ", librarian: " + lID;
	}
}

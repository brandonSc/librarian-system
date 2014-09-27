package schurmanb.comp4004.a1.src;

public class Title
{
	String title;	// title of book 
	String author;	// author of book
	Integer isbn;	// unique product code
	Integer lID; 	// id of librarian which added the book
	
	public Title( String title, String author, int isbn ){
		this.title = title;
		this.author = author;
		this.isbn = isbn;
	}
	
	public Title( String title, String author, int isbn, int lID ){
		this.title = title;
		this.author = author;
		this.isbn = isbn;
		this.lID = lID;
	}

	public String getTitle(){
		return this.title;
	}
	
	public String getAuthor(){
		return this.author;
	}
	
	public Integer getISBN(){
		return this.isbn;
	}
	
	public Integer getLibrarianID(){
		return this.lID;
	}
	
	@Override
	public String toString(){
		return title+" by "+author+". isbn#"+isbn+". Added by Librarian#"+lID;
				
	}
}

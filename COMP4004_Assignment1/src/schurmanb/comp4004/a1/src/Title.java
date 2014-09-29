package schurmanb.comp4004.a1.src;

import javafx.beans.property.SimpleStringProperty;

public class Title implements Comparable<Title>
{
	private String title;	// title of book 
	private String author;	// author of book
	private Integer isbn;	// unique product code
	private Integer lID; 	// id of librarian which added the book
	
	// for javaFX .... 
	private final SimpleStringProperty _title;
	private final SimpleStringProperty _author;
	private final SimpleStringProperty _isbn;
	
	public Title( String title, String author, int isbn ){
		this.title = title;
		this.author = author;
		this.isbn = isbn;
		this._title = new SimpleStringProperty(title);
		this._author = new SimpleStringProperty(author);
		this._isbn = new SimpleStringProperty(""+isbn);
		
	}
	
	public Title( String title, String author, int isbn, int lID ){
		this.title = title;
		this.author = author;
		this.isbn = isbn;
		this.lID = lID;
		this._title = new SimpleStringProperty(title);
		this._author = new SimpleStringProperty(author);
		this._isbn = new SimpleStringProperty(""+isbn);
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
	
	public String get_title(){
		return this._title.get();
	}
	
	public String get_author(){
		return this._author.get();
	}
	
	public String get_isbn(){
		return this._isbn.get();
	}
	
	@Override
	public String toString(){
		return title+" by "+author+". isbn#"+isbn+". Added by Librarian#"+lID;
				
	}
	
	@Override
	public int compareTo( Title other ){
		return this.getISBN().compareTo(other.getISBN());
	}
}

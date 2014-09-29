package schurmanb.comp4004.a1.tests;

import org.junit.Test;

import schurmanb.comp4004.a1.src.AddUserException;
import schurmanb.comp4004.a1.src.Librarian;
import schurmanb.comp4004.a1.src.Library;

/**
 * This test file simply prepares the Library database for GUI testing.
 * To test Iteration4, under project folder, select gui > * > LibraianTerminalController.java and run.
 * 
 * 
 * NOTE::: user librarian ID '1' in GUI when prompted for Librarian ID!
 * 
 * NOTE::: running this takes a little while 
 * 
 * @author brandon
 *
 */
public class Iteration4
{
	@Test
	public void setupDB(){
		System.out.println("populating databse...");
		Library library = Library.getInstance();
		library.clearDB();
		Librarian lib = null;
		try {
			lib = library.addLibrarian("Super", "User");
		} catch (AddUserException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		try {
			library.addTitle("Moby Dick", "Herman Melville", 1443392, lib.getLibrarianID());
			library.addTitle("Hitchhikers Guide To The Galaxy", "Douglas Adams", 8574331, lib.getLibrarianID());
			library.addTitle("Hamlet", "Shakespeare", 3443400, lib.getLibrarianID());
			library.addTitle("Edge of Eternity", "Ken Follett", 3441101, lib.getLibrarianID());
			library.addTitle("You Are Here", "Chris Hadfield", 8847341, lib.getLibrarianID());
			library.addTitle("The Story Hour", "Thrity Umigar", 4583100, lib.getLibrarianID());
			library.addTitle("Adultery", "Paulo Coelho", 1044812, lib.getLibrarianID());
			library.addTitle("Lone Wolf", "Jodi Picoult", 5011201, lib.getLibrarianID());
			library.addTitle("Taken", "Robert Crais", 1022345, lib.getLibrarianID());
			library.addTitle("The Infinite Sea", "Someone", 8776771, lib.getLibrarianID());
			library.addTitle("Being Mortal", "Atul Gawande", 9889112, lib.getLibrarianID());
			
			for( int i=0; i<10; i++ ){
				library.addItem(1443392, lib);
			}
			for( int i=0; i<10; i++ ){
				library.addItem(8574331, lib);
			}
			for( int i=0; i<10; i++ ){
				library.addItem(3443400, lib);
			}
			for( int i=0; i<10; i++ ){
				library.addItem(3441101, lib);
			}
			for( int i=0; i<10; i++ ){
				library.addItem(8847341, lib);
			}
			for( int i=0; i<10; i++ ){
				library.addItem(4583100, lib);
			}
			for( int i=0; i<10; i++ ){
				library.addItem(1044812, lib);
			}
			for( int i=0; i<10; i++ ){
				library.addItem(1044812, lib);
			}
			for( int i=0; i<10; i++ ){
				library.addItem(5011201, lib);
			}
			for( int i=0; i<10; i++ ){
				library.addItem(8776771, lib);
			}
			for( int i=0; i<10; i++ ){
				library.addItem(9889112, lib);
			}
			
			library.addUser("Homer", "Simpson");
		} catch( Exception e ){
			e.printStackTrace();
		}
		System.out.println("Done.");
	}
}

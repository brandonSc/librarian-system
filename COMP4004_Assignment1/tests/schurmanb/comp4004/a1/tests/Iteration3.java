package schurmanb.comp4004.a1.tests;

import static org.junit.Assert.*;

import java.util.ArrayList;

import org.joda.time.LocalDate;
import org.junit.After;
import org.junit.Test;

import schurmanb.comp4004.a1.src.AddTitleException;
import schurmanb.comp4004.a1.src.AddUserException;
import schurmanb.comp4004.a1.src.CannotLoanException;
import schurmanb.comp4004.a1.src.Item;
import schurmanb.comp4004.a1.src.ItemNotFoundException;
import schurmanb.comp4004.a1.src.Librarian;
import schurmanb.comp4004.a1.src.Library;
import schurmanb.comp4004.a1.src.Loan;
import schurmanb.comp4004.a1.src.Title;
import schurmanb.comp4004.a1.src.TitleNotFoundException;
import schurmanb.comp4004.a1.src.User;
import schurmanb.comp4004.a1.src.UserNotFoundException;

public class Iteration3 
{
	Library library = Library.getInstance();
	
	/** 
	 * Tests for uses cases "Borrow Loancopy", "Return Loancopy",
	 * "Collect Fine", "Renew Loan", and "Monitor System"
	 * from Appendix B of TOTEM paper
	 */
	@After
	public void testLoans(){
		System.out.println("-------------------------------");
		
		library.clearDB();
		
		Librarian lib = null;
		try { 
			lib = library.addLibrarian("Librarian", "01");
		} catch( AddUserException e ){
			e.printStackTrace();
			fail();
		}
		
		// add some Titles
		Title t1 = null, t2 = null, t3 = null;
		try {
			t1 = library.addTitle("Moby Dick", "Herman Melville", 1443392, lib.getUserID());
			assert(t1 != null);
			System.out.println("[Title added] "+t1);
			
			t2 = library.addTitle("Hitchhikers Guide To The Galaxy", "Douglas Adams", 8574331, lib.getUserID());
			assert(t2 != null);
			System.out.println("[Title added] "+t2);
			
			t3 = library.addTitle("Hamlet", "Shakespeare", 3443400, lib.getUserID());
			assert(t3 != null);
			System.out.println("[Title added] "+t3);
		} catch( AddTitleException e ){
			e.printStackTrace();
			fail();
		}
		
		// add Items for above Titles
		Item i1[] = new Item[10], i2 = null, i3 = null;
		try {
			// add ten of item 1
			for( int i=0; i<10; i++ ){
				i1[i] = library.addItem(1443392, lib);
				System.out.println("[Item added] "+i1[i]);
			}
			// add items using Title objects
			i2 = library.addItem(t2, lib);
			System.out.println("[Item added] "+i2);
			
			i3 = library.addItem(t3, lib);
			System.out.println("[Item added] "+i3);
		} catch( TitleNotFoundException e ){
			e.printStackTrace();
			fail();
		}
		
		// create a user to loan some books to
		User u1 = null;
		try {
			u1 = library.addUser("Homer", "Simpson");
			assert(u1 != null);
		} catch( AddUserException e ){
			e.printStackTrace();
			fail();
		}
		
		Loan l1 = null;
		// test borrowLoancopy
		try {
			// generate a couple loans
			LocalDate dueDate = new LocalDate().plusDays(7);
			l1 = library.borrowLoancopy(i2, u1, dueDate);
			assert(l1 != null);
			System.out.println("[Loan Generated] "+l1);
			
			dueDate = new LocalDate().plusDays(14);
			l1 = library.borrowLoancopy(i3, u1, dueDate);
			assert(l1 != null);
			System.out.println("[Loan Generated] "+l1);
			
			// test getAllLoans() for a user
			System.out.println("printing all loans for user "+u1+" using library.getAllLoans()");
			ArrayList<Loan> loans = library.getAllLoans(u1);
			for( Loan lo : loans ){
				System.out.println(lo);
			}
		} catch( TitleNotFoundException e ){
			e.printStackTrace();
			fail();
		} catch( CannotLoanException e ){
			e.printStackTrace();
			fail();
		}
		
		// test loaning the same item twice
		try {
			library.borrowLoancopy(i3, u1, new LocalDate().plusDays(14));
		} catch( CannotLoanException e ){
			// this exception should be thrown here.
			System.err.println(e);
		} catch (TitleNotFoundException e) {
			// but not this one!
			e.printStackTrace();
			fail();
		}
		
		// test borrowing when an Item is overdue
		try {
			Loan l = library.borrowLoancopy(i1[9], u1, new LocalDate().minusDays(1));
			System.out.println("[Loan Generated] "+l);
			library.borrowLoancopy(i1[8], u1, new LocalDate().plusDays(3));
		} catch( CannotLoanException e ){
			System.err.println(e);
		} catch( TitleNotFoundException e ){
			e.printStackTrace();
			fail();
		}
		
		// test collectFine on the overdue item from above
		try { 
		 	library.collectFine(u1,i1[9]);
		 	System.out.println("[Fine Collected!]");
		} catch( TitleNotFoundException | ItemNotFoundException e ){
			e.printStackTrace();
			fail();
		}
		
		// test loaning over 10 items
		try {
			// should see some exceptions here
			for( int i=0 ;i<10; i++ ){
				Loan loan = library.borrowLoancopy(i1[i], u1, new LocalDate().plusDays(7));
				System.out.println("[Loan Generated] "+loan);
			}
		} catch( CannotLoanException e ){
			System.err.println(e);
		} catch( TitleNotFoundException e ){
			e.printStackTrace();
			fail();
		}
		
		// return some copies
		try {
			for( int i=0 ;i<5; i++ ){
				library.returnLoancopy(u1, i1[i]);
				System.out.println("[Loaned Item Returned] "+i1[i]);
			}
		} catch( TitleNotFoundException | ItemNotFoundException e ){
			e.printStackTrace();
			fail();
		}
		
		// renew a loan
		System.out.println("[Loan before renewal] "+l1);
		l1 = library.renewLoan(l1, u1, 21);
		System.out.println("[Loan after renewal] "+l1);
		
		// lastly test Monitor System use case
		System.out.println(library.getAllUsers());
		System.out.println(library.getAllTitles());
		
		System.out.println("-------------------------------");
	}
	
	/**
	 * Tests for use cases "Add Item" and "Remove Item"
	 * from Appendix B of TOTEM paper
	 */
	@Test
	public void testAddRemoveItem(){
		System.out.println("-------------------------------");
		library.clearDB();
		
		Librarian lib = null;
		try { 
			lib = library.addLibrarian("Librarian", "01");
		} catch( AddUserException e ){
			e.printStackTrace();
			fail();
		}
		
		// add some Titles
		Title t1 = null, t2 = null, t3 = null;
		try {
			t1 = library.addTitle("Moby Dick", "Herman Melville", 1443392, lib.getUserID());
			assert(t1 != null);
			System.out.println("[Title added] "+t1);
			
			t2 = library.addTitle("Hitchhikers Guide To The Galaxy", "Douglas Adams", 8574331, lib.getUserID());
			assert(t2 != null);
			System.out.println("[Title added] "+t2);
			
			t3 = library.addTitle("Hamlet", "Shakespeare", 3443400, lib.getUserID());
			assert(t3 != null);
			System.out.println("[Title added] "+t3);
		} catch( AddTitleException e ){
			e.printStackTrace();
			fail();
		}
		
		// add Items for above Titles
		Item i1[] = new Item[10], i2 = null, i3 = null;
		try {
			// add ten of item 1
			for( int i=0; i<10; i++ ){
				i1[i] = library.addItem(1443392, lib);
				System.out.println("[Item added] "+i1[i]);
			}
			// add items using Title objects
			i2 = library.addItem(t2, lib);
			System.out.println("[Item added] "+i2);
			
			i3 = library.addItem(t3, lib);
			System.out.println("[Item added] "+i3);
		} catch( TitleNotFoundException e ){
			e.printStackTrace();
			fail();
		}
		
		// test removing an Item
		library.removeItem(i3.getItemID(), lib);
		System.out.println("[Item removed] "+i3);
		
		// test finding an arbitrary Item copy of a Title
		try {
			Item i4 = library.findSomeItem(t1.getISBN());
			System.out.println("[Item found] "+i4);
		} catch( ItemNotFoundException e ){
			e.printStackTrace();
			fail();
		}
		
		System.out.println("-------------------------------");
	}
	
	/**
	 * Tests for use cases "Add Title" and "Remove Title"
	 * from Appendix B of TOTEM paper
	 */
	@Test
	public void testAddRemoveTitle(){
		System.out.println("-------------------------------");
		library.clearDB();
		
		// first create a user to add titles with
		Librarian l1 = null;
		try {
			l1 = library.addLibrarian("Super", "User");
			System.out.println("[Librarian added] "+l1);
		} catch( AddUserException e ){
			e.printStackTrace();
			fail();
		}
		
		// add some titles
		Title t1 = null, t2 = null, t3 = null;
		try {
			t1 = library.addTitle("Moby Dick", "Herman Melville", 1443392, l1.getUserID());
			assert(t1 != null);
			System.out.println("[Title added] "+t1);
			
			t2 = library.addTitle("Hitchhikers Guide To The Galaxy", "Douglas Adams", 8574331, l1.getUserID());
			assert(t2 != null);
			System.out.println("[Title added] "+t2);
			
			t3 = library.addTitle("Hamlet", "Shakespeare", 3443400, l1.getUserID());
			assert(t3 != null);
			System.out.println("[Title added] "+t3);
		} catch( AddTitleException e ){
			e.printStackTrace();
			fail();
		}
		
		// remove a title
		try {
			library.removeTitle(t2);
			System.out.println("[Title removed] "+t2);
		} catch( TitleNotFoundException e ){
			e.printStackTrace();
			fail();
		}
		
		// remove that title again to generate an exception
		try {
			library.removeTitle(t2);
		} catch( TitleNotFoundException e ){
			System.err.println(e);
		}
		
		// add the same title twice to generate an exception
		try {
			library.addTitle("Moby Dick", "Hermen Melville", 1443392, l1.getUserID());
		} catch( AddTitleException e ){
			System.err.println(e);
		}
			
		System.out.println("-------------------------------");
	}
	
	/**
	 * Tests for use cases "Add User" and "Remove User" 
	 * these tests also test the "Find User" use case
	 * from Appendix B of TOTEM paper
	 */
	@Test
	public void testAddRemoveUser(){
		System.out.println("-------------------------------");
		library.clearDB();
		User u1 = null, u2 = null, u3 = null, u4 = null;
		
		// add example users
		try {			
			System.out.println("Adding user Robin Banks...");
			u1 = library.addUser("Robin", "Banks");
			assertTrue(u1.getUserID() >= 0);
			System.out.println("added "+u1+"\n");
			
			System.out.println("Adding user Hugh Jarms...");
			u2 = library.addUser("Hugh", "Jarms");
			assertTrue(u2.getUserID() >= 0);
			System.out.println("added "+u2+"\n");
			
			System.out.println("Adding user Ilene Dover...");
			u3 = library.addUser("Ilene", "Dover");
			assertTrue(u3.getUserID() >= 0);
			System.out.println("added "+u3+"\n");
		} catch( AddUserException e ){
			// should be no exceptions here.
			e.printStackTrace();
			fail();
		}
		
		// add a user also as a librarian
		try {
			u1 = library.addLibrarian(u1.getFirstName(), u1.getLastName());
			System.out.println(u1);
		} catch( AddUserException e ){
			e.printStackTrace();
			fail();
		}
		
		// try findUser again for user/librarian
		try {
			u1 = library.findUser(u1.getUserID());
			System.out.println(u1);
		} catch( UserNotFoundException e ){
			fail();
		}
		
		// try findLibrarian
		try { 
			System.out.println(library.findLibrarian(u1));
		} catch( UserNotFoundException e ){
			fail();
		}
		
		// remove that user that is also a librarian
		try {
			library.removeUser(u1);
		} catch( UserNotFoundException e ){
			fail();
		}
		
		// try to find that user as a librarian
		// this should generate an exception!
		try {
			library.findLibrarian(u1);
		} catch( UserNotFoundException e ){
			System.err.println(e);
		}
			
		// remove a user using uID
		try { 
			System.out.println("Removing user "+u2);
			library.removeUser(u2);
			System.out.println(u2+" removed!\n");
		} catch( UserNotFoundException e ) {
			System.err.println(e);
			fail();
		}
		
		// re add that same user
		try {
			System.out.println("Adding user Hugh Jarms...");
			u4 = library.addUser("Hugh", "Jarms");
			assertTrue(u4.getUserID() >= 0);
			assertTrue(u4.getUserID() != u2.getUserID());
			System.out.println("added "+u4+"\n");
		} catch( AddUserException e ) {
			System.err.println(e);
			fail();
		}
		
		// attempt to remove user that doesn't exist
		// this will generate an exception
		try {
			System.out.println("Removing some user that does not exist...");
			library.removeUser("IDont", "ExistYet");
		} catch( UserNotFoundException e ) {
			System.err.println(e);
			System.out.println();
		}
		
		// remove user by name	
		try {
			System.out.println("Removing user Hugh Jarms by first and last name...");
			library.removeUser("Hugh", "Jarms");
		} catch( UserNotFoundException e ) {
			System.err.println(e);
			fail();
		}
		System.out.println("Robin Banks removed.\n");
		
		// add a user that already exists.
		// this will generate an exception that should be handled 
		// and presented to the user
		try {
			System.out.println("Adding Ilene Dover which already exists... This should generate an exception!");
			library.addUser("Ilene", "Dover");
		} catch( AddUserException e ) {
			System.err.println(e+"\n");
		}
		System.out.println("-------------------------------");
	}	
}

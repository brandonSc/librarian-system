package schurmanb.comp4004.a1.tests;

import static org.junit.Assert.*;

import org.junit.Test;

import schurmanb.comp4004.a1.src.AddUserException;
import schurmanb.comp4004.a1.src.Library;
import schurmanb.comp4004.a1.src.User;
import schurmanb.comp4004.a1.src.UserNotFoundException;

public class Iteration1 
{
	Library library = Library.getInstance();
	
	/**
	 * Tests for use cases "Add User" and "Remove User" 
	 * these tests also test the "Find User" use case
	 * from Appendix B of TOTEM paper
	 */
	@Test
	public void testAddRemoveUser(){
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
	}	
}

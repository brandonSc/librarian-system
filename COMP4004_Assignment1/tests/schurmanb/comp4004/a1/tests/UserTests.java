package schurmanb.comp4004.a1.tests;

import static org.junit.Assert.*;

import org.junit.Test;

import schurmanb.comp4004.a1.src.Library;

public class UserTests 
{
	Library library = Library.getInstance();
	
	/**
	 * Tests for use cases "AddUser" and "RemoveUser" from Appendix B of TOTEM paper
	 */
	@Test
	public void testAddRemoveUser(){
		// add example users
		int uID1 = library.addUser("Robin", "Banks");
		assertTrue(uID1 >= 0);
		System.out.println("Robin Banks added with ID# = "+uID1);
		
		int uID2 = library.addUser("Hugh", "Jarms");
		assertTrue(uID2 >= 0);
		System.out.println("Hugh Jharms added with ID# = "+uID2);
		
		int uID3 = library.addUser("Ilene", "Dover");
		assertTrue(uID3 >= 0);
		System.out.println("Ilene Dover added with ID# = "+uID3);
		
		// remove a user using uID
		library.removeUser(uID2);
		System.out.println("Removed user Hugh Jarms ID#"+uID2);
		
		// re add that same user
		int uID4 = library.addUser("Hugh", "Jarms");
		assertTrue(uID4 >= 0);
		assertTrue(uID4 != uID2);
		System.out.println("Hugh Jarms added with ID# = "+uID4);
		
		// attempt to remove user that doesn't exist
		library.removeUser("IDont", "ExistYet");
		
		// remove user by name
		library.removeUser("Robin", "Banks");
	}
}

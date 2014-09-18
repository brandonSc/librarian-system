package schurmanb.comp4004.a1.src;

public class Library
{
	public static Library uniqueInstance; // only one Library instance may exist
	
	/**
	 * Cannot call constructor explicitly.
	 * See getInstance()
	 */
	private Library(){
		// add DB initialization
	}
	
	public static Library getInstance(){
		
		return uniqueInstance;
	}
}

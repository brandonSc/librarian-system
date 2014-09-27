package schurmanb.comp4004.a1.src;

/**
 * Throw if there is a problem locating a user
 * @author Brandon Schurman
 */
public class UserNotFoundException extends Exception
{
	private static final long serialVersionUID = 1399349208554197618L;

	UserNotFoundException( String msg ){
		super(msg);
	}
	
	@Override
	public String toString(){
		return "UserNotFoundException: "+getMessage();
	}
}

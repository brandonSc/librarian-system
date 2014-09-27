package schurmanb.comp4004.a1.src;

/**
 * Throw if there is a problem adding the user
 * @author Brandon Schurman
 */
public class AddUserException extends Exception
{
	private static final long serialVersionUID = -7916847224259600781L;

	AddUserException( String msg ){
		super(msg);
	}
	
	@Override
	public String toString(){
		return "AddUserException: "+getMessage();
	}
}

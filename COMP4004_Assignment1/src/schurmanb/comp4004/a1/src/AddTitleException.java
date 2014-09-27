package schurmanb.comp4004.a1.src;

public class AddTitleException extends Exception
{
	private static final long serialVersionUID = -205046458575942312L;

	public AddTitleException( String msg ){
		super(msg);
	}
	
	@Override
	public String toString(){
		return "AddTitleException: "+this.getMessage();
	}
}
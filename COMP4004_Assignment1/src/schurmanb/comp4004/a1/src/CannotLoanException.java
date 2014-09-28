package schurmanb.comp4004.a1.src;

public class CannotLoanException extends Exception 
{
	private static final long serialVersionUID = -9089695317924239920L;

	public CannotLoanException( String msg ){
		super(msg);
	}
	
	@Override
	public String toString(){
		return "CannotLoanException: "+getMessage();
	}
}

package schurmanb.comp4004.a1.src;

public class TitleNotFoundException extends Exception
{
	private static final long serialVersionUID = -704730603156833724L;

	public TitleNotFoundException( String msg ){
		super(msg);
	}
	
	@Override
	public String toString(){
		return "TitleNotFoundException: "+getMessage();
	}
}

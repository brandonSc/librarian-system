package schurmanb.comp4004.a1.src;

public class ItemNotFoundException extends Exception
{
	private static final long serialVersionUID = 8117624033093927236L;

	public ItemNotFoundException( String msg ){
		super(msg);
	}
	
	@Override
	public String toString(){
		return "ItemNotFoundException: "+getMessage();
	}
}

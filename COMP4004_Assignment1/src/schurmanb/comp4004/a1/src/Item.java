package schurmanb.comp4004.a1.src;

public class Item 
{
	private Integer iID;	// item ID generated by library database
	private Title title; 	// referencing Title object
	
	public Item( int iID, Title title ){
		this.iID = iID;
		this.title = title;
	}
	
	public Integer getItemID(){
		return this.iID;
	}
	
	public Title getReferencingTitle(){
		return this.title;
	}
	
	@Override 
	public String toString(){
		return title + ". ItemID#"+iID;
	}
	
	@Override 
	public boolean equals( Object other ){
		if( other != null ){
			if( other instanceof Item ){
				Item i = (Item)other;
				if( i.getItemID().equals(this.getItemID()) ){
					return true;
				}
			}
		}
		return false;
	}
}

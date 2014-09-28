package schurmanb.comp4004.a1.src;

import org.joda.time.Days;
import org.joda.time.LocalDate;

public class Loan 
{
	private User user;				// User owing loan
	private Item item;				// Item being loaned
	private LocalDate startDate;	// date rented
	private LocalDate dueDate; 		// date due
	
	public Loan( User user, Item item, LocalDate startDate, LocalDate dueDate ){
		this.user = user;
		this.item = item;
		this.startDate = startDate;
		this.dueDate = dueDate;
	}
	
	public int daysLeft(){
		return Days.daysBetween(startDate, dueDate).getDays();
	}
	
	public boolean isOverDue(){
		LocalDate curDate = new LocalDate();
		return curDate.isAfter(dueDate);
	}
	
	public User getUser(){
		return user;
	}
	
	public Item getItem(){
		return item;
	}
	
	public LocalDate getStartDate(){
		return startDate;
	}
	
	public LocalDate getDueDate(){
		return dueDate;
	}
	
	public String toString(){
		return "Item ID#"+item.getItemID()+". Date Loaned: "+startDate+", Date Due: "+dueDate
				+". OverDue? "+isOverDue()+". Days Left: "+daysLeft();
	}
}

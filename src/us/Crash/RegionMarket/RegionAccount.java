package us.Crash.RegionMarket;

public class RegionAccount {
	
	private String name;
	
	public RegionAccount(String user){
		
		name = user;
		
	}
	
	public String addMoney(double amt){
		
		return RegionMarket.giveMoneyTo(name, amt);
		
	}
	
	public String subtractMoney(double amt){
		
		return RegionMarket.takeMoneyFrom(name, amt);
		
	}
	
	public boolean hasEnough(double amt){
		
		return RegionMarket.canAfford(name, amt);
		
	}
	
	public String getName(){ return name; }
	
	public double getAmount(){ return RegionMarket.getAmount(name); }
	
}

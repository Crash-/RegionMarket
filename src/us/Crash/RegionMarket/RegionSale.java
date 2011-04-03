package us.Crash.RegionMarket;

public class RegionSale {

	private int price;
	private String seller, region;
	private boolean instantTrade;
	
	public RegionSale(String sellername, String regionname, int saleprice, boolean instant){
		
		seller = sellername;
		region = regionname;
		price = saleprice;
		instantTrade = instant;
		
	}
	
	public int getPrice(){ return price; }
	public String getSeller(){ return seller; }
	public String getRegion(){ return region; }
	public boolean isInstant(){ return instantTrade; }
	
}

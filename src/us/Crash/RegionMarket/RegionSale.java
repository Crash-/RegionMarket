package us.Crash.RegionMarket;

public class RegionSale {

	private int price;
	private String seller, region;
	
	public RegionSale(String sellername, String regionname, int saleprice){
		
		seller = sellername;
		region = regionname;
		price = saleprice;
		
	}
	
	public int getPrice(){ return price; }
	public String getSeller(){ return seller; }
	public String getRegion(){ return region; }
	
}

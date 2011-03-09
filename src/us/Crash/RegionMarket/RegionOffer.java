package us.Crash.RegionMarket;

public class RegionOffer {

	private String sellerName, offererName, regionName;
	private int offerPrice;
	
	public RegionOffer(String owner, String offerer, String region, int price){
		
		sellerName = owner;
		offererName = offerer;
		regionName = region;
		offerPrice = price;
		
	}
	
	public String getSeller(){ return sellerName; }
	public String getRegion(){ return regionName; }
	public String getOfferer(){ return offererName; }
	public int getPrice(){ return offerPrice; }
	
}

package us.Crash.RegionMarket;

import net.minecraft.server.ItemInWorldManager;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.World;

import org.bukkit.Location;

public class RegionAgent {

	private Location myLoc;
	private String myName, sellerName, regionSale;
	private AgentBase self;
	private int sellPrice, myType;

	public RegionAgent(String name, String seller, String region, Location loc, int price, int type){

		myName = name;
		if(type == 0)
			myLoc = new Location(loc.getWorld(), loc.getX(), loc.getY(), loc.getZ(), loc.getYaw(), loc.getPitch());
		else
			myLoc = new Location(loc.getWorld(), loc.getBlockX(), loc.getBlockY(), loc.getBlockZ());
		sellerName = seller;
		regionSale = region;
		sellPrice = price;
		myType = type;
		
	}
	
	public void makeAgentBase(MinecraftServer ms, World world, ItemInWorldManager iwm, AgentManager mgr){
		
		self = new AgentBase(ms, world, this.getName(), iwm, mgr, this);
		
	}

	public Location getLocation(){ return myLoc; }
	public String getName(){ return myName; }
	public String getSeller(){ return sellerName; }
	public String getRegion(){ return regionSale; }
	public AgentBase getAgent(){ return self; }
	public int getPrice(){ return sellPrice; }
	public int getType(){ return myType; }
	public void moveTo(Location loc){
		
		if(myType != 0)
			return;
		
		myLoc = loc;
		//if(self != null)
			//self.c(myLoc.getX(), myLoc.getY(), myLoc.getZ(), myLoc.getYaw(), myLoc.getPitch());
		
	}
	
}

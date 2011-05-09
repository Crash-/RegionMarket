package us.Crash.RegionMarket;

import java.io.IOException;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;

import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Server;
import org.bukkit.World;
import org.bukkit.block.Sign;
import org.bukkit.craftbukkit.CraftServer;
import org.bukkit.craftbukkit.CraftWorld;
import org.bukkit.entity.Player;

import com.sk89q.worldguard.protection.regions.*;

import net.minecraft.server.*;


public class AgentManager {

	private ArrayList<RegionAgent> agentList = new ArrayList<RegionAgent>();
	private HashMap<String, RegionAgent> confirmList = new HashMap<String, RegionAgent>(); 
	private HashMap<String, Long> cooldownList = new HashMap<String, Long>();
	private RegionMarketManager marketManager;

	public AgentManager(RegionMarketManager mgr){

		marketManager = mgr;

	}
	
	public ArrayList<RegionAgent> getAgentList(){ return agentList; }
	
	public RegionAgent makeNewAgent(Location loc, String adder, String region, int price){
		
		RegionAgent r = new RegionAgent(adder + "'s Agent - " + region, adder, region, loc, price, 0);
		
		agentList.add(r);
		
		return r;
		
	}
	
	/*public void addAgentToWorld(RegionAgent r) {

		WorldServer w = getWorldServer(r.getLocation().getWorld());
		MinecraftServer s = getMinecraftServer(RegionMarket.server);

		r.makeAgentBase(s, w, new ItemInWorldManager(w), this);
		
		r.getAgent().c(r.getLocation().getX(), r.getLocation().getY(), r.getLocation().getZ(), r.getLocation().getYaw(), r.getLocation().getPitch());

		w.c((int)MathHelper.b(r.getAgent().locX / 16.0D), (int)MathHelper.b(r.getAgent().locZ / 16.0D)).a(r.getAgent());
		w.b.add(r.getAgent());
		
		try {

			Method method;
			method = net.minecraft.server.World.class.getDeclaredMethod("b", Entity.class);
			method.setAccessible(true);
			method.invoke(w, r.getAgent());

		} catch(Exception e){ return; }

	}*/
	
	public void addSignToWorld(Location loc, String adder, String region, int price){

		if(loc.getBlock().getTypeId() != 68 && loc.getBlock().getTypeId() != 63){
			
			RegionMarket.outputConsole("Error : " + adder + "'s agent for " + region + " is not a sign!");
			return;
			
		}
		
		Sign s = (Sign)loc.getBlock().getState();
		
		s.setLine(0, "[AGENT]");
		s.setLine(1, region);
		s.setLine(2, "" + price);
		
		RegionAgent agent = new RegionAgent("", adder, region, loc, price, 1); 
		
		agentList.add(agent);
		
	}

	public void deleteAllAgents(){
		
		for(Iterator<RegionAgent>i = agentList.iterator(); i.hasNext();){
			
			//RegionAgent a = i.next();
			//if(a.getType() == 0)
				//a.getAgent().world.e(a.getAgent());
			i.remove();
			
		}
		
	}
	
	public boolean deleteAgentFromWorld(String seller, String region, boolean... changeSign){

		RegionAgent agent = getAgent(seller, region);
		
		if(agent.getType() == 0)
			return false;
		
		if(changeSign.length == 1 && changeSign[0] && agent.getType() == 1)
			agent.getLocation().getBlock().setTypeId(0);
		
		agentList.remove(agent);

		/*if(agentList.remove(agent)){

			try {

				if(agent.getType() == 0)
					agent.getAgent().world.e(agent.getAgent());
				
			} catch(Exception e){ return false; }

			return true;

		}*/

		return false;

	}

	public void deleteAgentFromWorld(RegionAgent agent){

		/*if(agentList.remove(agent)){

			try {

				if(agent.getType() == 0)
					agent.getAgent().world.e(agent.getAgent());
				
			} catch(Exception e){ }

		}*/
		
		agentList.remove(agent);

	}

	public boolean hasAddedAgent(String seller, String region){

		return getAgent(seller, region) != null;

	}

	public RegionAgent getAgent(Location loc){
		
		if(loc == null)
			return null;
		
		loc = new Location(loc.getWorld(), loc.getBlockX(), loc.getBlockY(), loc.getBlockZ());
	
		for(RegionAgent a : agentList)
			if(a.getType() == 1 && loc.getBlockX() == a.getLocation().getBlockX() && loc.getBlockY() == a.getLocation().getBlockY() && loc.getBlockZ() == a.getLocation().getBlockZ() && loc.getWorld().equals(a.getLocation().getWorld()))
				return a;
		
		return null;
		
	}
	
	public RegionAgent getAgent(String seller, String region){

		for(RegionAgent a : agentList)
			if(a.getSeller().equalsIgnoreCase(seller) && a.getRegion().equals(region))
				return a;

		return null;

	}
	
	public RegionAgent getAgent(org.bukkit.entity.Entity entity){
		
		if(entity == null)
			return null;
		
		for(RegionAgent a : agentList){
			if(a.getType() == 0 && a.getAgent().getBukkitEntity().getEntityId() == entity.getEntityId())
				return a;
			
		}
		
		return null;
		
	}

	@SuppressWarnings("unused")
	private WorldServer getWorldServer(World world) {

		try {
			CraftWorld w = (CraftWorld) world;
			Field f;
			f = CraftWorld.class.getDeclaredField("world");

			f.setAccessible(true);
			return (WorldServer) f.get(w);

		} catch (Exception e) {
			return null;
		}

	}

	@SuppressWarnings("unused")
	private MinecraftServer getMinecraftServer(Server server) {

		if (server instanceof CraftServer) {

			try {

				CraftServer cs = (CraftServer) server;
				Field f;
				f = CraftServer.class.getDeclaredField("console");
				MinecraftServer ms;
				f.setAccessible(true);
				ms = (MinecraftServer) f.get(cs);
				return ms;

			} catch(Exception e){ return null; }

		}

		return null;
	}

	public void rigthClickCallback(EntityHuman e, RegionAgent agent){

		Player p = RegionMarket.getPlayer(e.name);

		if(p != null){

			if(agent.getSeller().equalsIgnoreCase(p.getName())){

				p.sendMessage(ChatColor.YELLOW + "[NPC]Selling the region, " + agent.getRegion() + " for " + agent.getPrice() + ". Type /rm agent -r " + agent.getRegion() + " to remove me!");

			} else {

				if(!confirmList.containsKey(p.getName()))
					p.sendMessage(ChatColor.YELLOW + "[NPC]Hello, I am selling " + agent.getSeller() + "'s region, " + agent.getRegion() + " for " + agent.getPrice() + ". Attack me to buy the region!");
				else {
					
					p.sendMessage(ChatColor.YELLOW + "[NPC]You canceled the offer.");
					confirmList.remove(p.getName());
					return;
					
				}

			}

		}

	}

	public void leftClickCallback(org.bukkit.entity.Entity e, RegionAgent agent){

		Player p = (Player)e;

		RegionAccount buyAcc = new RegionAccount(p.getName()), sellAcc = new RegionAccount(agent.getSeller());
		ProtectedRegion region = marketManager.getRegion(e.getWorld(), agent.getRegion());
		
		if(agent.getSeller().equalsIgnoreCase(p.getName())){
			
			p.sendMessage(ChatColor.YELLOW + "[NPC]I'm your agent.");
			return;
			
		}

		if(buyAcc == null || sellAcc == null || region == null)
			return;

		if(!buyAcc.hasEnough(agent.getPrice())){

			p.sendMessage(ChatColor.YELLOW + "[NPC]Sorry, you don't have enough money to buy this region.");
			return;

		}
		
		if(marketManager.hasReachedMaxRegionsAllowed(p)){
			
			p.sendMessage(ChatColor.YELLOW + "[NPC]Sorry, you've reached the max amount of regions that you can own.");
			return;
			
		}
		
		
		if(!confirmList.containsKey(p.getName()) || !confirmList.get(p.getName()).getRegion().equals(agent.getRegion())){
			
			confirmList.put(p.getName(), agent);
			cooldownList.put(p.getName(), System.currentTimeMillis());
			p.sendMessage(ChatColor.YELLOW + "[NPC]Left click me again to confirm the offer and right click to cancel.");
			return;
			
		}
		
		Long coolTime = cooldownList.get(p.getName());
		
		if(coolTime != null){
			
			if(System.currentTimeMillis() - coolTime < 1000)
				return;
			
			cooldownList.remove(p.getName());
			
		}
		
		confirmList.remove(p.getName());
		
		buyAcc.subtractMoney(agent.getPrice());
		sellAcc.addMoney(agent.getPrice());
		region.getOwners().addPlayer(p.getName());
		region.getOwners().removePlayer(agent.getSeller());

		marketManager.removeRegionSale(region, agent.getSeller());
		
		try {
			RegionMarket.WorldGuard.getGlobalRegionManager().get(p.getWorld()).save();
		} catch (IOException e1) {
			e1.printStackTrace();
		}
		
		Player player = RegionMarket.getPlayer(agent.getSeller());
		if(player != null)
			RegionMarket.outputDebug(player, p.getName() + " has bought your region, " + agent.getRegion() + "!");
		
		p.sendMessage(ChatColor.YELLOW + "[NPC]Congratulations, you now own " + agent.getRegion() + "!");

	}

}

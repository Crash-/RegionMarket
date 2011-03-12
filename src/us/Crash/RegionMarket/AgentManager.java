package us.Crash.RegionMarket;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Iterator;

import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Server;
import org.bukkit.World;
import org.bukkit.craftbukkit.CraftServer;
import org.bukkit.craftbukkit.CraftWorld;
import org.bukkit.entity.Player;

import com.nijiko.coelho.iConomy.iConomy;
import com.nijiko.coelho.iConomy.system.Account;
import com.sk89q.worldguard.protection.ProtectedRegion;

import net.minecraft.server.*;

public class AgentManager {

	private ArrayList<RegionAgent> agentList = new ArrayList<RegionAgent>();
	private RegionMarketManager marketManager;

	public AgentManager(RegionMarketManager mgr){

		marketManager = mgr;

	}

	@SuppressWarnings("unchecked")
	public void addAgentToWorld(World world, Location loc, String adder, String region, int price) {

		WorldServer w = getWorldServer(world);
		MinecraftServer s = getMinecraftServer(RegionMarket.server);

		RegionAgent r = new RegionAgent(adder + "'s Agent - " + region, adder, region, loc, price, 0);

		r.makeAgentBase(s, w, new ItemInWorldManager(w), this);

		r.getAgent().c(loc.getX(), loc.getY(), loc.getZ(), loc.getYaw(), loc.getPitch());

		w.c(MathHelper.b((int)r.getAgent().locX / 16.0D), (int)MathHelper.b(r.getAgent().locX / 16.0D)).a(r.getAgent());
		w.b.add(r.getAgent());

		try {

			Method method;
			method = net.minecraft.server.World.class.getDeclaredMethod("b", Entity.class);
			method.setAccessible(true);
			method.invoke(w, r.getAgent());

		} catch(Exception e){ }

		agentList.add(r);

	}
	
	public void addSignToWorld(Location loc, String adder, String region, int price){

		agentList.add(new RegionAgent("", adder, region, loc, price, 1));
		
	}

	public void removeAllAgents(){
		
		for(Iterator<RegionAgent>i = agentList.iterator(); i.hasNext();){
			
			RegionAgent a = i.next();
			if(a.getType() == 0)
				a.getAgent().world.e(a.getAgent());
			i.remove();
			
		}
		
	}
	
	public boolean removeAgentFromWorld(String seller, String region, boolean... changeSign){

		RegionAgent agent = getAgent(seller, region);
		if(changeSign.length == 1 && changeSign[0] && agent.getType() == 1)
			agent.getLocation().getBlock().setTypeId(0);

		if(agentList.remove(agent)){

			try {

				if(agent.getType() == 0)
					agent.getAgent().world.e(agent.getAgent());
				
			} catch(Exception e){ return false; }

			return true;

		}

		return false;

	}

	public void removeAgentFromWorld(RegionAgent agent){

		if(agentList.remove(agent)){

			try {

				if(agent.getType() == 0)
					agent.getAgent().world.e(agent.getAgent());
				
			} catch(Exception e){ }

		}

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

				p.sendMessage(ChatColor.YELLOW + "[NPC]Hello, I am selling " + agent.getSeller() + "'s region, " + agent.getRegion() + " for " + agent.getPrice() + ". Attack me to buy the region!");

			}

		}

	}

	public void leftClickCallback(org.bukkit.entity.Entity e, RegionAgent agent){

		Player p = (Player)e;

		Account buyAcc = iConomy.getBank().getAccount(p.getName()), sellAcc = iConomy.getBank().getAccount(agent.getSeller());
		ProtectedRegion region = marketManager.getRegion(agent.getRegion());
		
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

		buyAcc.subtract(agent.getPrice());
		sellAcc.add(agent.getPrice());
		buyAcc.save();
		sellAcc.save();
		region.getOwners().addPlayer(p.getName());
		region.getOwners().removePlayer(agent.getSeller());

		marketManager.removeRegionSale(region, agent.getSeller());
		
		Player player = RegionMarket.getPlayer(agent.getSeller());
		if(player != null)
			RegionMarket.outputDebug(player, p.getName() + " has bought your region, " + agent.getRegion() + "!");
		
		p.sendMessage(ChatColor.YELLOW + "[NPC]Congratulations, you now own " + agent.getRegion() + "!");

	}

}

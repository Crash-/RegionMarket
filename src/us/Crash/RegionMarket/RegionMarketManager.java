package us.Crash.RegionMarket;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;

import org.bukkit.ChatColor;
import org.bukkit.World;
import org.bukkit.entity.Player;
import com.sk89q.worldguard.bukkit.BukkitPlayer;
import com.sk89q.worldguard.protection.regions.*;

public class RegionMarketManager {

	private HashMap<String, ArrayList<RegionSale>> regionList = new HashMap<String, ArrayList<RegionSale>>();
	private HashMap<String, ArrayList<RegionOffer>> offerList = new HashMap<String, ArrayList<RegionOffer>>();
	private AgentManager AgentMgr = new AgentManager(this);
	
	public HashMap<String, ArrayList<RegionSale>> getRegionsForSale(){

		return regionList;

	}

	public HashMap<String, ArrayList<RegionOffer>> getOffersForRegions(){

		return offerList;

	}
	
	public AgentManager getAgentManager(){ return AgentMgr; }

	public boolean hasReachedMaxRegionsAllowed(Player p){
		
		if(RegionMarket.useMaxRegions)
			return RegionMarket.WorldGuard.getGlobalRegionManager().get(p.getWorld()).getRegionCountOfPlayer(new BukkitPlayer(RegionMarket.WorldGuard, p)) >= RegionMarket.WorldGuard.getGlobalConfiguration().get(p.getWorld()).maxRegionCountPerPlayer;
		else
			return false;
		
	}
	
	public RegionOffer findOffer(ProtectedRegion region, String seller, String buyer){

		if(region == null)
			return null;

		if(offerList.get(region.getId()) == null)
			return null;

		for(RegionOffer o : offerList.get(region.getId())){

			if(o.getSeller().equalsIgnoreCase(seller) && o.getOfferer().equalsIgnoreCase(buyer))
				return o;

		}		

		return null;

	}

	public RegionSale findSale(ProtectedRegion region, String seller){

		if(region == null)
			return null;

		if(regionList.get(region.getId()) == null)
			return null;

		for(RegionSale s : regionList.get(region.getId())){

			if(s.getSeller().equalsIgnoreCase(seller))
				return s;

		}

		return null;

	}

	public RegionSale findSale(String region, String seller){

		if(regionList.get(region) == null)
			return null;

		for(RegionSale s : regionList.get(region)){

			if(s.getSeller().equalsIgnoreCase(seller))
				return s;

		}

		return null;

	}

	public ProtectedRegion getRegion(World w, String name){

		return RegionMarket.WorldGuard.getGlobalRegionManager().get(w).getRegion(name);

	}

	public boolean isOnMarket(ProtectedRegion region){

		if(region == null)
			return false;

		for(String key : regionList.keySet()){

			if(key.equalsIgnoreCase(region.getId()))
				return true;

		}

		return false;

	}

	public boolean isOnMarket(String name){

		for(String key : regionList.keySet()){

			if(key.equalsIgnoreCase(name))
				return true;

		}

		return false;


	}

	public boolean isPlayerSelling(ProtectedRegion region, String playername){

		if(region == null)
			return false;

		if(regionList.get(region.getId()) == null)
			return false;

		for(RegionSale key : regionList.get(region.getId())){

			if(key.getSeller().equalsIgnoreCase(playername))
				return true;

		}

		return false;


	}

	public boolean isPlayerSelling(String name, String playername){

		if(regionList.get(name) == null)
			return false;

		for(RegionSale key : regionList.get(name)){

			if(key.getSeller().equalsIgnoreCase(playername))
				return true;

		}

		return false;

	}

	public boolean hasOfferedOnRegion(String name, String seller, Player player){
		
		if(offerList.get(name) == null)
			return false;

		for(int i = 0; i < offerList.get(name).size(); i++){

			if(offerList.get(name).get(i).getOfferer().equalsIgnoreCase(player.getName()))
				return true;

		}

		return false;

	}

	public boolean hasOfferedOnRegion(ProtectedRegion region, String seller, Player player){

		if(region == null)
			return false;
		
		if(offerList.get(region.getId()) == null)
			return false;

		for(int i = 0; i < offerList.get(region.getId()).size(); i++){

			if(offerList.get(region.getId()).get(i).getOfferer().equalsIgnoreCase(player.getName()) && offerList.get(region.getId()).get(i).getSeller().equalsIgnoreCase(seller))
				return true;

		}

		return false;

	}

	public boolean hasOfferedOnRegion(ProtectedRegion region, String seller, String player){

		if(region == null)
			return false;
		
		if(offerList.get(region.getId()) == null)
			return false;

		for(int i = 0; i < offerList.get(region.getId()).size(); i++){

			if(offerList.get(region.getId()).get(i).getOfferer().equalsIgnoreCase(player) && offerList.get(region.getId()).get(i).getSeller().equalsIgnoreCase(seller))
				return true;

		}

		return false;

	}


	public void addOffer(ProtectedRegion region, String seller, Player player, int price){

		if(region == null)
			return;
		
		if(!offerList.containsKey(region.getId()))
			offerList.put(region.getId(), new ArrayList<RegionOffer>());

		offerList.get(region.getId()).add(new RegionOffer(seller, player.getName(), region.getId(), price));

		Player p = RegionMarket.getPlayer(seller);
		if(p != null)
			p.sendMessage(ChatColor.AQUA + "[RegionMarket] " + ChatColor.YELLOW + player.getName() + " has offered on your region, " + region.getId());

	}

	public void addOffer(String region, String seller, String player, int price){

		if(!offerList.containsKey(region))
			offerList.put(region, new ArrayList<RegionOffer>());

		offerList.get(region).add(new RegionOffer(seller, player, region, price));

	}

	public void removeOffer(ProtectedRegion region, String seller, Player player){

		if(region == null)
			return;
		
		if(offerList.get(region.getId()) == null)
			return;

		HashMap<String, ArrayList<RegionOffer>>tempList = offerList;

		for(RegionOffer o : tempList.get(region.getId())){

			if(o.getSeller().equalsIgnoreCase(seller) && o.getOfferer().equalsIgnoreCase(player.getName())){

				offerList.get(region.getId()).remove(o);
				return;

			}

		}

	}

	public void addRegionSale(ProtectedRegion region, Player seller, int price, boolean instant){
		
		if(region == null)
			return;
		
		if(!isOnMarket(region))
			regionList.put(region.getId(), new ArrayList<RegionSale>());

		regionList.get(region.getId()).add(new RegionSale(seller.getName(), region.getId(), price, instant));

	}

	public void removeRegionSale(ProtectedRegion region, Player seller){

		if(region == null)
			return;

		if(regionList.get(region.getId()) == null)
			return;

		for(Iterator<RegionSale> i = regionList.get(region.getId()).iterator(); i.hasNext();)
			if(i.next().getSeller().equalsIgnoreCase(seller.getName()))
				i.remove();

		if(offerList.get(region.getId()) != null && offerList.get(region.getId()).size() != 0){

			for(Iterator<RegionOffer> i = offerList.get(region.getId()).iterator(); i.hasNext();)
				if(i.next().getSeller().equalsIgnoreCase(seller.getName()))
					i.remove();

		}
		
		RegionAgent a = AgentMgr.getAgent(seller.getName(), region.getId());
		if(a != null)
			AgentMgr.deleteAgentFromWorld(a);

	}

	public void removeRegionSale(ProtectedRegion region, String seller){

		if(region == null)
			return;

		if(regionList.get(region.getId()) == null)
			return;

		for(Iterator<RegionSale> i = regionList.get(region.getId()).iterator(); i.hasNext();)
			if(i.next().getSeller().equalsIgnoreCase(seller))
				i.remove();

		if(offerList.get(region.getId()) != null && offerList.get(region.getId()).size() != 0){

			for(Iterator<RegionOffer> i = offerList.get(region.getId()).iterator(); i.hasNext();)
				if(i.next().getSeller().equalsIgnoreCase(seller))
					i.remove();

		}
		
		RegionAgent a = AgentMgr.getAgent(seller, region.getId());
		if(a != null)
			AgentMgr.deleteAgentFromWorld(a);

	}

	public void addRegionSale(String region, String seller, int price, boolean instant){

		if(!isOnMarket(region))
			regionList.put(region, new ArrayList<RegionSale>());

		regionList.get(region).add(new RegionSale(seller, region, price, instant));

	}

	public boolean acceptOffer(ProtectedRegion region, String seller, String buyer){

		RegionOffer o = findOffer(region, seller, buyer);
		if(o == null){

			RegionMarket.outputConsole("Error getting offer on " + region.getId() + "," + seller + " for " + buyer + ".");
			return false;

		}

		RegionAccount sellAcc = new RegionAccount(seller), buyAcc = new RegionAccount(buyer);

		if(buyAcc.hasEnough(o.getPrice())){

			region.getOwners().removePlayer(seller);
			region.getOwners().addPlayer(buyer);
			sellAcc.addMoney(o.getPrice());
			buyAcc.subtractMoney(o.getPrice());
			Player p = RegionMarket.getPlayer(buyer);
			
			try {
				RegionMarket.WorldGuard.getGlobalRegionManager().get(p.getWorld()).save();
			} catch (IOException e1) {
				e1.printStackTrace();
			}
			
			if(p != null)
				p.sendMessage(ChatColor.AQUA + "[RegionMarket] " + ChatColor.YELLOW + seller + " has sold you their region " + region.getId() + "!");

			removeRegionSale(region, seller);

			return true;

		} else {

			Player p = RegionMarket.getPlayer(seller);
			if(p != null)
				RegionMarket.outputError(p, "The buyer doesn't have enough money!");

			return false;

		}

	}

	public int getPrice(String regionname, String playername){

		RegionSale sale = findSale(regionname, playername);
		if(sale == null)
			return -1;

		Integer price = sale.getPrice();

		return price == null ? -1 : price;

	}

	public int getPrice(ProtectedRegion region, String playername){

		if(region == null)
			return -1;
		
		RegionSale sale = findSale(region.getId(), playername);
		if(sale == null)
			return -1;

		Integer price = sale.getPrice();

		return price == null ? -1 : price;

	}

	public ArrayList<RegionOffer> getOffersFor(String region, String seller){

		ArrayList<RegionOffer> offers = new ArrayList<RegionOffer>();

		if(offerList.get(region) == null)
			return offers;

		for(int i = 0; i < offerList.get(region).size(); i++){

			RegionOffer o = offerList.get(region).get(i);
			if(o.getSeller().equalsIgnoreCase(seller))
				offers.add(o);

		}

		return offers;

	}

	public boolean isOwner(Player p, ProtectedRegion region){

		if(region == null)
			return false;

		return region.isOwner(new BukkitPlayer(RegionMarket.WorldGuard, p));

	}

	@SuppressWarnings("unused")
	public void listRegionsForSale(Player p, int page, String... etc){

		page -= 1;

		if(page < 0){

			RegionMarket.outputError(p, "You need to enter a page greater than 0!");
			return;

		}
		if(regionList.size() == 0){

			RegionMarket.outputError(p, "No regions are for sale!");
			return;

		}

		if(etc.length == 0){

			int size = 0;

			for(String key : regionList.keySet())
				for(RegionSale s : regionList.get(key))
					size++;

			if(size == 0){
				
				RegionMarket.outputDebug(p, "There are no regions on the market.");
				return;
				
			}
			
			if(page + 1 > (int)Math.ceil(size / 7.0)){

				RegionMarket.outputError(p, (page + 1) + " is greater than the max, " + (int)Math.ceil(size / 7.0) + ".");
				return;

			}

			p.sendMessage(ChatColor.GREEN + "Showing all regions for sale; Page " + ChatColor.WHITE + (page + 1) + ChatColor.GREEN + " of " + ChatColor.WHITE + (int)Math.ceil(size / 7.0));
			p.sendMessage(ChatColor.GREEN + "<" + ChatColor.WHITE + "region" + ChatColor.GREEN + ">" + ChatColor.YELLOW + " - " + ChatColor.GREEN + "<" + ChatColor.WHITE + "seller" + ChatColor.GREEN + ">" + ChatColor.YELLOW + " - " + ChatColor.GREEN + "<" + ChatColor.WHITE + "price" + ChatColor.GREEN + ">" + ChatColor.YELLOW + " - " + ChatColor.GREEN + "<" + ChatColor.WHITE + "instant" + ChatColor.GREEN + ">");

			int i = 0;
			boolean passed = false;

			for(String key : regionList.keySet()){

				if(i >= page * 7 && i < (page + 1) * 7){

					for(int j = 0; i >= page * 7 && i < (page + 1) * 7 && j < regionList.get(key).size(); j++){

						RegionSale s = regionList.get(key).get(j);
						p.sendMessage(ChatColor.GREEN + key + ChatColor.YELLOW + " - " + ChatColor.GREEN + s.getSeller() + ChatColor.YELLOW + " - " + ChatColor.GREEN + s.getPrice() + ChatColor.YELLOW + " - " + ChatColor.GREEN + s.isInstant());
						i++;

					}
					passed = true;

				} else {
					if(passed)
						break;
					else
						i++;

				}

			}

		} else {

			if(etc[0].equalsIgnoreCase("p")){

				String player = etc[1];

				ArrayList<RegionSale> sales = new ArrayList<RegionSale>();

				for(String key : regionList.keySet())
					for(RegionSale s : regionList.get(key))
						if(s.getSeller().equalsIgnoreCase(player))
							sales.add(s);
				
				if(sales.size() == 0){

					RegionMarket.outputDebug(p, player + " isn't selling any regions.");
					return;

				}
				if(page + 1 > (int)Math.ceil(sales.size() / 7.0)){

					RegionMarket.outputError(p, (page + 1) + " is greater than the max, " + (int)Math.ceil(sales.size() / 7.0));
					return;

				}

				p.sendMessage(ChatColor.GREEN + "Showing " + player + "'s regions on the market; Page " + ChatColor.WHITE + (page + 1) + ChatColor.GREEN + " of " + ChatColor.WHITE + (int)Math.ceil(sales.size() / 7.0));
				p.sendMessage(ChatColor.GREEN + "<" + ChatColor.WHITE + "region" + ChatColor.GREEN + ">" + ChatColor.YELLOW + " - " + ChatColor.GREEN + "<" + ChatColor.WHITE + "price" + ChatColor.GREEN + ">" + ChatColor.YELLOW + " - " + ChatColor.GREEN + "<" + ChatColor.WHITE + "instant" + ChatColor.GREEN + ">");

				for(int j = 0, i = page * 7; j < 7 && i < sales.size(); j++, i++){

					RegionSale s = sales.get(i);
					p.sendMessage(ChatColor.GREEN + s.getRegion() + ChatColor.YELLOW + " - " + ChatColor.GREEN + s.getPrice() + ChatColor.YELLOW + " - " + s.isInstant());

				}

				sales = null;

			} else if(etc[0].equalsIgnoreCase("r")){

				String region = etc[1];

				for(String key : regionList.keySet()){

					if(key.equalsIgnoreCase(region))
						region = key;
				}

				ArrayList<RegionSale> sales = regionList.get(region);

				if(sales == null){

					RegionMarket.outputError(p, "There are no sales for that region!");
					return;

				}
				
				if(sales.size() == 0){

					RegionMarket.outputError(p, "There are no sales for that region!");
					return;

				}

				if(page + 1 > (int)Math.ceil(sales.size() / 7.0)){

					RegionMarket.outputError(p, (page + 1) + " is greater than the max, " + (int)Math.ceil(sales.size() / 7.0));
					return;

				}

				p.sendMessage(ChatColor.GREEN + "Showing sales for region " + region + "; Page " + ChatColor.WHITE + (page + 1) + ChatColor.GREEN + " of " + ChatColor.WHITE + (int)Math.ceil(sales.size() / 7.0));
				p.sendMessage(ChatColor.GREEN + "<" + ChatColor.WHITE + "seller" + ChatColor.GREEN + ">" + ChatColor.YELLOW + " - " + ChatColor.GREEN + "<" + ChatColor.WHITE + "price" + ChatColor.GREEN + ">" + ChatColor.YELLOW + " - " + ChatColor.GREEN + "<" + ChatColor.WHITE + "instant" + ChatColor.GREEN + ">");

				for(int j = 0, i = page * 7; j < 7 && i < sales.size(); j++, i++){

					RegionSale s = sales.get(i);
					p.sendMessage(ChatColor.GREEN + s.getSeller() + ChatColor.YELLOW + " - " + ChatColor.GREEN + s.getPrice() + ChatColor.YELLOW + " - " + ChatColor.GREEN + s.isInstant());

				}

				sales = null;

			} else if(etc[0].equalsIgnoreCase("o")){

				String region = etc[1];

				for(String key : offerList.keySet()){

					if(key.equalsIgnoreCase(region))
						region = key;

				}

				ArrayList<RegionOffer> offers = offerList.get(region);

				if(offers == null){

					p.sendMessage(ChatColor.AQUA + "[RegionMarket] " + ChatColor.YELLOW + "There are no offers for the region.");
					return;

				}
				
				if(offers.size() == 0){

					p.sendMessage(ChatColor.AQUA + "[RegionMarket] " + ChatColor.YELLOW + "There are no offers for the region.");
					return;

				}

				if(page + 1 > (int)Math.ceil(offers.size() / 7.0)){

					RegionMarket.outputError(p, (page + 1) + " is greater than the max, " + (int)Math.ceil(offers.size() / 7.0));
					return;

				}

				p.sendMessage(ChatColor.GREEN + "Showing offers for region " + region + "; Page " + ChatColor.WHITE + (page + 1) + ChatColor.GREEN + " of " + ChatColor.WHITE + (int)Math.ceil(offers.size() / 7.0));
				p.sendMessage(ChatColor.GREEN + "<" + ChatColor.WHITE + "offerer" + ChatColor.GREEN + ">" + ChatColor.YELLOW + " - " + ChatColor.GREEN + "<" + ChatColor.WHITE + "price" + ChatColor.GREEN + ">");

				for(int j = 0, i = page * 7; j < 7 && i < offers.size(); j++, i++){

					RegionOffer o = offers.get(i);
					if(o.getSeller().equalsIgnoreCase(p.getName()))
						p.sendMessage(ChatColor.GREEN + o.getOfferer() + ChatColor.YELLOW + " - " + ChatColor.GREEN + o.getPrice());

				}

				offers = null;

			} else if(etc[0].equals("i")){
				
				ArrayList<RegionSale> sales = new ArrayList<RegionSale>();
				
				for(String key : regionList.keySet())
					for(RegionSale s : regionList.get(key))
						if(s.isInstant())
							sales.add(s);
				
				if(sales.size() == 0){

					RegionMarket.outputDebug(p, "There are no instant sales available.");
					return;

				}
				if(page + 1 > (int)Math.ceil(sales.size() / 7.0)){

					RegionMarket.outputError(p, (page + 1) + " is greater than the max, " + (int)Math.ceil(sales.size() / 7.0));
					return;

				}

				p.sendMessage(ChatColor.GREEN + "Showing all instant sales; Page " + ChatColor.WHITE + (page + 1) + ChatColor.GREEN + " of " + ChatColor.WHITE + (int)Math.ceil(sales.size() / 7.0));
				p.sendMessage(ChatColor.GREEN + "<" + ChatColor.WHITE + "region" + ChatColor.GREEN + ">" + ChatColor.YELLOW + " - " + ChatColor.GREEN + "<" + ChatColor.WHITE + "seller" + ChatColor.GREEN + ">" + ChatColor.YELLOW + " - " + ChatColor.GREEN + "<" + ChatColor.WHITE + "price" + ChatColor.GREEN + ">");

				for(int j = 0, i = page * 7; j < 7 && i < sales.size(); j++, i++){

					RegionSale s = sales.get(i);
					p.sendMessage(ChatColor.GREEN + s.getRegion() + ChatColor.YELLOW + " - " + ChatColor.GREEN + s.getSeller() + ChatColor.YELLOW + " - " + ChatColor.GREEN + s.getPrice());

				}

				sales = null;

				
			}

		}

	}

}

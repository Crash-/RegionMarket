package us.Crash.RegionMarket;

import org.bukkit.ChatColor;
import org.bukkit.Server;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.Sign;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockListener;
import org.bukkit.event.block.SignChangeEvent;
import org.bukkit.event.entity.*;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerListener;
import org.bukkit.event.server.PluginEnableEvent;
import org.bukkit.event.server.ServerListener;
import org.bukkit.plugin.java.JavaPlugin;

import com.earth2me.essentials.User;
import com.nijiko.coelho.iConomy.iConomy;
import com.nijiko.coelho.iConomy.system.Account;
import com.nijikokun.bukkit.Permissions.*;
import com.sk89q.worldguard.bukkit.WorldGuardPlugin;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;

public class RegionMarket extends JavaPlugin {

	public static Server server;
	public static WorldGuardPlugin WorldGuard = null;
	public static iConomy iConomy = null;
	public static Permissions Permissions = null;
	public static int ERR_ARG = 0, ERR_NAME = 1, ERR_NOOWN = 2, ERR_MONEY = 3, ERR_NOSALE = 4, ERR_OWN = 5;
	public static boolean useMaxRegions = false, useiConomy = true;
	private RegionMarketManager marketManager = new RegionMarketManager();
	private FileManager FileMgr;
	private SListener serverListener = new SListener();
	private EListener entityListener = new EListener(this);
	private BListener blockListener = new BListener(this);
	private PListener playerListener = new PListener(this);

	public RegionMarketManager getMarketManager(){

		return marketManager;

	}

	public FileManager getFileManager(){

		return FileMgr;

	}
	
	public static String formatMoney(double amount){

		if(useiConomy){

			return com.nijiko.coelho.iConomy.iConomy.getBank().format(amount);

		} else {

			return "$" + amount;

		}

	}

	public static boolean accountExists(String player){

		if(useiConomy){

			Account account = com.nijiko.coelho.iConomy.iConomy.getBank().getAccount(player);
			if(account == null)
				return false;

		} else {

			if(player == null || player.isEmpty())
				return false;

			Player p;
			try {
				
				p = server.matchPlayer(player).get(0);
				
			} catch(Exception e){
				
				return false;
				
			}
			User account = User.get(p);
			if(account == null)
				return false;

		}

		return true;

	}

	public static double getAmount(String player){

		if(useiConomy){

			Account account = com.nijiko.coelho.iConomy.iConomy.getBank().getAccount(player);
			if(account == null)
				return 0;

			return account.getBalance();

		} else {

			Player p;
			
			try {
				
				p = server.matchPlayer(player).get(0);
				
			} catch(Exception e){
				
				return 0;
				
			}
			User account = User.get(p);

			return account.getMoney();

		}

	}

	public static boolean canAfford(String player, double amount){

		if(useiConomy){

			Account account = com.nijiko.coelho.iConomy.iConomy.getBank().getAccount(player);
			if(account == null)
				return false;

			return account.hasEnough(amount);

		} else {

			Player p;
			
			try {
				
				p = server.matchPlayer(player).get(0);
				
			} catch(Exception e){
				
				return false;
				
			}
			User account = User.get(p);

			return account.canAfford(amount);

		}

	}

	public static String takeMoneyFrom(String player, double amount){

		if(useiConomy){

			Account account = com.nijiko.coelho.iConomy.iConomy.getBank().getAccount(player);
			if(account == null)
				return "Could not find an iConomy account for you.";

			account.subtract(amount);

		} else {

			Player p;
			
			try {
				
				p = server.matchPlayer(player).get(0);
				
			} catch(Exception e){
				
				return "Could not get the player, " + player;
				
			}
			User account = User.get(p);

			account.takeMoney(amount);

		}

		return "";

	}

	public static String giveMoneyTo(String player, double amount){

		if(useiConomy){

			Account account = com.nijiko.coelho.iConomy.iConomy.getBank().getAccount(player);
			if(account == null)
				return "Could not find an iConomy account for you.";

			account.add(amount);

		} else {

			Player p;
			
			try {
				
				p = server.matchPlayer(player).get(0);
				
			} catch(Exception e){
				
				return "Could not get the player, " + player;
				
			}
			User account = User.get(p);

			account.giveMoney(amount);

		}

		return "";

	}

	public static boolean canUseCommand(String command, Player player){

		if(Permissions == null)
			return true;

		command = command.toLowerCase();

		if(command.equals("sell") || command.equals("s"))
			return Permissions.getHandler().permission(player, "regionmarket.sell");
		if(command.equals("remove") || command.equals("r"))
			return Permissions.getHandler().permission(player, "regionmarket.remove");
		if(command.equals("offer") || command.equals("o"))
			return Permissions.getHandler().permission(player, "regionmarket.offer");
		if(command.equals("list") || command.equals("l"))
			return Permissions.getHandler().permission(player, "regionmarket.list");
		if(command.equals("accept") || command.equals("a"))
			return Permissions.getHandler().permission(player, "regionmarket.accept");
		if(command.equals("agent"))
			return Permissions.getHandler().permission(player, "regionmarket.agent");
		if(command.equals("reload"))
			return Permissions.getHandler().permission(player, "regionmarket.reload");
		if(command.equals("save"))
			return Permissions.getHandler().permission(player, "regionmarket.save");

		return true;

	}

	public static boolean outputError(Player p, int errorID){

		if(errorID == ERR_ARG)
			return outputError(p, "Wrong amount of arguments!");
		else if(errorID == ERR_NAME)
			return outputError(p, "No region exists with that name!");
		else if(errorID == ERR_NOOWN)
			return outputError(p, "You do not own that region!");
		else if(errorID == ERR_MONEY)
			return outputError(p, "You do not have enough money!");
		else if(errorID == ERR_NOSALE)
			return outputError(p, "That region is not for sale!");
		else if(errorID == ERR_OWN)
			return outputError(p, "You own that region!");

		return false;

	}

	public static boolean outputError(Player p, String s){

		p.sendMessage(ChatColor.AQUA + "[RegionMarket] " + ChatColor.RED + s);
		return false;

	}

	public static void outputDebug(Player p, String s){

		p.sendMessage(ChatColor.AQUA + "[RegionMarket] " + ChatColor.YELLOW + s);

	}

	public static Player getPlayer(String name){

		for(int i = 0; i < server.getOnlinePlayers().length; i++){

			if(server.getOnlinePlayers()[i].getName().equalsIgnoreCase(name))
				return server.getOnlinePlayers()[i];

		}

		return null;

	}

	public static void outputConsole(String output){

		System.out.println("[RegionMarket] " + output);

	}

	@Override
	public void onDisable() {

		if(FileMgr != null){

			FileMgr.saveData();
			outputConsole("Finished saving, v" + getDescription().getVersion() + " disabled.");

		} else {

			outputConsole("v" + getDescription().getVersion() + " disabled.");

		}

		try {
			for(World w : getServer().getWorlds())
				WorldGuard.getGlobalRegionManager().get(w).save();
		}catch(Exception e){
			outputConsole("Error : Could not save new WorldGuard data.");
		}

	}

	@Override
	public void onEnable() {

		server = getServer();

		WorldGuard = (WorldGuardPlugin)server.getPluginManager().getPlugin("WorldGuard");
		iConomy = (iConomy)server.getPluginManager().getPlugin("iConomy");
		Permissions = (Permissions)server.getPluginManager().getPlugin("Permissions");
		
		Object Essentials = server.getPluginManager().getPlugin("Essentials");

		if(WorldGuard == null){

			outputConsole("Unable to find WorldGuard plugin.");
			server.getPluginManager().disablePlugin(this);
			return;

		} else if(iConomy == null && Essentials == null){

			outputConsole("Unable to find an economy plugin.");
			server.getPluginManager().disablePlugin(this);
			return;

		} else if(Permissions == null){

			outputConsole("Unable to find Permissions plugin.");
			outputConsole("CAUTION : There are no restrictions on commands!");

		} else if(WorldGuard != null && (iConomy != null || Essentials != null) && Permissions != null)
			outputConsole("Found and hooked all plugins successfully.");
		
		if(iConomy != null)
			useiConomy = true;
		else if(Essentials != null)
			useiConomy = false;

		FileMgr = new FileManager("plugins/RegionMarket/", "saveData.txt", this);

		server.getPluginManager().registerEvent(Event.Type.PLUGIN_ENABLE, serverListener, Event.Priority.Normal, this);
		server.getPluginManager().registerEvent(Event.Type.ENTITY_DAMAGE, entityListener, Event.Priority.Normal, this);
		server.getPluginManager().registerEvent(Event.Type.BLOCK_BREAK, blockListener, Event.Priority.Normal, this);
		server.getPluginManager().registerEvent(Event.Type.PLAYER_INTERACT, playerListener, Event.Priority.Normal, this);
		server.getPluginManager().registerEvent(Event.Type.SIGN_CHANGE, blockListener, Event.Priority.Normal, this);
		
		outputConsole("Loaded v" + getDescription().getVersion() + ", by Crash");

	}
		
	@Override
	public boolean onCommand(CommandSender sender, Command command, String commandLabel, String[] args) {

		if(!(sender instanceof Player))
			return false;

		Player p = (Player)sender;

		if(command.getName().equalsIgnoreCase("rm")){

			if(args.length < 1)
				return outputError(p, ERR_ARG);

			if(!canUseCommand(args[0], p))
				return outputError(p, "You cannot use this command!");

			if(args[0].equalsIgnoreCase("sell") || args[0].equalsIgnoreCase("s")){

				if(args.length < 3)
					return outputError(p, ERR_ARG);

				ProtectedRegion region = marketManager.getRegion(p.getWorld(), args[1]);

				if(region == null)
					return outputError(p, ERR_NAME);

				if(!marketManager.isOwner(p, region))
					return outputError(p, ERR_NOOWN);

				if(marketManager.isPlayerSelling(region, p.getName()))
					return outputError(p, "You are already selling this region!");

				Integer price = null;

				try {

					price = Integer.parseInt(args[2]);

				} catch(Exception e){

					return outputError(p, "Unknown number, \"" + args[2] + "\".");

				}

				if(price < 1)
					return outputError(p, "Enter a price greater than 0!");
				
				boolean isInstant = false;
				
				if(args.length >= 4)
					if(args[3].equalsIgnoreCase("true"))
						isInstant = true;

				marketManager.addRegionSale(region, p, price, isInstant);
				outputDebug(p, "You added your region to the market.");


			} else if(args[0].equalsIgnoreCase("offer") || args[0].equalsIgnoreCase("o")){

				if(args.length < 4)
					return outputError(p, ERR_ARG);

				ProtectedRegion region = marketManager.getRegion(p.getWorld(), args[1]);

				if(region == null)
					return outputError(p, ERR_NAME);

				if(!marketManager.isOnMarket(region))
					return outputError(p, ERR_NOSALE);

				if(marketManager.isOwner(p, region))
					return outputError(p, ERR_OWN);

				if(!marketManager.isPlayerSelling(region, args[2]))
					return outputError(p, args[2] + " isn't selling the region, " + args[1] + "!");

				if(marketManager.hasOfferedOnRegion(region, args[2], p))
					return outputError(p, "You've already offered on that region!");
				
				if(marketManager.hasReachedMaxRegionsAllowed(p))
					return outputError(p, "You've reached the max amount of allowed regions!");
				
					
				Integer price = null;

				try {

					price = Integer.parseInt(args[3]);

				} catch(Exception e){

					return outputError(p, "Unknown number, \"" + args[3] + "\".");

				}

				int minPrice = marketManager.getPrice(region, args[2]);

				if(!new RegionAccount(p.getName()).hasEnough(price))
					return outputError(p, "You don't have that much money!");

				if(price < minPrice)
					return outputError(p, "Enter an offer higher than the minimum!");

				if(marketManager.findSale(region, args[2]).isInstant()){
					
					RegionAccount sellAcc = new RegionAccount(args[2]), buyAcc = new RegionAccount(p.getName());
					
					region.getOwners().removePlayer(args[2]);
					region.getOwners().addPlayer(p.getName());
					sellAcc.addMoney(price);
					buyAcc.subtractMoney(price);
					marketManager.removeRegionSale(region, args[2]);
					
					outputDebug(p, "You bought the region succesfully!");
					Player player = getPlayer(args[2]);
					if(player != null)
						player.sendMessage(ChatColor.AQUA + "[RegionMarket] " + ChatColor.YELLOW + p + " has bought your region, " + region.getId() + "!");
						
				} else {

					marketManager.addOffer(region, args[2], p, price);
					outputDebug(p, "You've successfully offered on " + args[1] + ".");
					
				}
				
				return true;

			} else if(args[0].equalsIgnoreCase("list") || args[0].equalsIgnoreCase("l")){

				if(args.length == 1)
					marketManager.listRegionsForSale(p, 1);
				else {

					if(!args[1].contains("-")){

						Integer page = null;

						try {

							page = Integer.parseInt(args[1]);

						} catch(Exception e){

							return outputError(p, "Unknown page \"" + args[1] + "\".");

						}

						marketManager.listRegionsForSale(p, page);


					} else {

						Integer page = null;

						if(args[1].equalsIgnoreCase("-a")){

							if(args.length < 2)
								return outputError(p, "Correct usage : /rm list -a <page>");

							if(args.length == 2)
								page = 1;
							else {

								try {

									page = Integer.parseInt(args[2]);

								} catch(Exception e){

									return outputError(p, "Unknown page \"" + args[2] + "\".");

								}

							}

							marketManager.listRegionsForSale(p, page);

						} else if(args[1].equalsIgnoreCase("-p")){

							if(args.length < 3)
								return outputError(p, "Correct usage : /rm list -p <player> <page>");

							if(args.length == 3)
								page = 1;
							else {

								try {

									page = Integer.parseInt(args[3]);

								} catch(Exception e){

									return outputError(p, "Unknown page \"" + args[3] + "\".");

								}

							}

							marketManager.listRegionsForSale(p, page, "p", args[2]);

						} else if(args[1].equalsIgnoreCase("-r")){

							if(args.length < 3)
								return outputError(p, "Correct usage : /rm list -r <region> <page>");

							if(args.length == 3)
								page = 1;
							else {

								try {

									page = Integer.parseInt(args[3]);

								} catch(Exception e){

									return outputError(p, "Unknown page \"" + args[3] + "\".");

								}

							}

							marketManager.listRegionsForSale(p, page, "r", args[2]);

						} else if(args[1].equalsIgnoreCase("-o")){

							if(args.length < 3)
								return outputError(p, "Correct usage : /rm list -o <region> <page>");

							if(args.length == 3)
								page = 1;
							else {

								try {

									page = Integer.parseInt(args[3]);

								} catch(Exception e){

									return outputError(p, "Unknown page \"" + args[3] + "\".");

								}

							}

							ProtectedRegion region = marketManager.getRegion(p.getWorld(), args[2]);

							if(region == null)
								return outputError(p, ERR_NAME);

							if(!marketManager.isOwner(p, region))
								return outputError(p, ERR_NOOWN);

							if(!marketManager.isPlayerSelling(region, p.getName()))
								return outputError(p, "You aren't selling that region!");

							marketManager.listRegionsForSale(p, page, "o", args[2]);

						} else if(args[1].equalsIgnoreCase("-i")){
							
							if(args.length < 2)
								return outputError(p, "Correct usage : /rm list -i <page>");
							
							if(args.length == 2)
								page = 1;
							else {

								try {

									page = Integer.parseInt(args[3]);

								} catch(Exception e){

									return outputError(p, "Unknown page \"" + args[2] + "\".");

								}

							}
							
							marketManager.listRegionsForSale(p, page, "i");
							
						}

					}

				}

			} else if(args[0].equalsIgnoreCase("remove") || args[0].equalsIgnoreCase("r")){

				if(args.length < 3)
					return outputError(p, ERR_ARG);
				else {

					if(!args[1].contains("-"))
						return outputError(p, "Use the flag -o or -r!");
					else if(args[1].equalsIgnoreCase("-o")){

						if(args.length < 4)
							return outputError(p, ERR_ARG);

						ProtectedRegion region = marketManager.getRegion(p.getWorld(), args[2]);

						if(region == null)
							return outputError(p, ERR_NAME);

						if(!marketManager.hasOfferedOnRegion(region, args[3], p))
							return outputError(p, "You haven't offered on that region!");

						marketManager.removeOffer(region, args[3], p);
						outputDebug(p, "You have removed your offer.");

					} else if(args[1].equalsIgnoreCase("-r")){

						ProtectedRegion region = marketManager.getRegion(p.getWorld(), args[2]);

						if(region == null)
							return outputError(p, ERR_NAME);

						if(!marketManager.isOwner(p, region))
							return outputError(p, ERR_NOOWN); 

						if(!marketManager.isPlayerSelling(region, p.getName()))
							return outputError(p, "You aren't selling that region!");

						marketManager.removeRegionSale(region, p);
						outputDebug(p, "You removed your region from the market.");

					} else {

						return outputError(p, "Unknown flag " + args[1] + ".");

					}

				}

			} else if(args[0].equalsIgnoreCase("accept") || args[0].equalsIgnoreCase("a")){

				if(args.length < 3)
					return outputError(p, ERR_ARG);

				ProtectedRegion region = marketManager.getRegion(p.getWorld(), args[1]);

				if(!marketManager.isPlayerSelling(region, p.getName()))
					return outputError(p, "You aren't selling that region!");

				if(!marketManager.hasOfferedOnRegion(region, p.getName(), args[2]))
					return outputError(p, args[2] + " hasn't offered on your region!");

				if(marketManager.acceptOffer(region, p.getName(), args[2]))
					outputDebug(p, "You have sold your ownership to " + args[2] + ".");

			} else if(args[0].equalsIgnoreCase("agent")){ 

				if(args.length < 3)
					return outputError(p, ERR_ARG);

				if(!args[1].contains("-")){

					outputError(p, "Use the flag -a, -m, or -r!");

				} else {

					args[1] = args[1].toLowerCase();

					if(args[1].equals("-a")){
						
						return outputError(p, "NPC Agents have been disabled for RM v0.5");

						/*if(args.length < 4)
							return outputError(p, "Correct usage : /rm agent -a <region> <price>");

						ProtectedRegion region = marketManager.getRegion(p.getWorld(), args[2]);

						if(region == null)
							return outputError(p, ERR_NAME);

						if(!marketManager.isOwner(p, region))
							return outputError(p, ERR_NOOWN);

						if(!marketManager.isPlayerSelling(region, p.getName()))
							return outputError(p, "You aren't selling that region!");

						if(marketManager.getAgentManager().hasAddedAgent(p.getName(), region.getId()))
							return outputError(p, "You've already added an agent to the region!");

						Integer price = null;

						try {

							price = Integer.parseInt(args[3]);

						} catch(Exception e){

							return outputError(p, "Unknown price \"" + args[3] + "\".");

						}

						if(price < 1)
							return outputError(p, "Put in a price greater than 0!");

						RegionAgent agent = marketManager.getAgentManager().makeNewAgent(p.getLocation(), p.getName(), region.getId(), price);
						marketManager.getAgentManager().addAgentToWorld(agent);
						outputDebug(p, "You've added your agent!");*/

					} else if(args[1].equals("-r")){

						ProtectedRegion region = marketManager.getRegion(p.getWorld(), args[2]);

						if(region == null)
							return outputError(p, ERR_NAME);

						if(!marketManager.isOwner(p, region))
							return outputError(p, ERR_NOOWN);

						if(!marketManager.isPlayerSelling(region, p.getName()))
							return outputError(p, "You aren't selling that region!");

						if(!marketManager.getAgentManager().hasAddedAgent(p.getName(), region.getId()))
							return outputError(p, "You haven't added an agent to the region!");

						if(marketManager.getAgentManager().deleteAgentFromWorld(p.getName(), region.getId(), true)){

							outputDebug(p, "You removed the agent.");
							return true;

						} else
							return outputError(p, "An error occured when trying to remove the agent.");

					} else if(args[1].equals("-m")){

						return outputError(p, "NPC Agents have been disabled for RM v0.5");
						
						/*ProtectedRegion region = marketManager.getRegion(p.getWorld(), args[2]);

						if(region == null)
							return outputError(p, ERR_NAME);

						if(!marketManager.isOwner(p, region))
							return outputError(p, ERR_NOOWN);

						if(!marketManager.isPlayerSelling(region, p.getName()))
							return outputError(p, "You aren't selling that region!");

						if(!marketManager.getAgentManager().hasAddedAgent(p.getName(), region.getId()))
							return outputError(p, "You haven't added an agent to that region!");

						RegionAgent agent = marketManager.getAgentManager().getAgent(p.getName(), region.getId());
						agent.moveTo(p.getLocation());
						outputDebug(p, "You've moved your agent.");*/

					} else {

						return outputError(p, "Unknown flag.");

					}

				}

			} else if(args[0].equalsIgnoreCase("reload")){

				marketManager.getAgentManager().deleteAllAgents();
				marketManager = new RegionMarketManager();

				if(FileMgr.loadData(p)){

					outputDebug(p, "The data was reloaded successfully.");

				} else {

					outputError(p, "There was an error when reloading the save file.");

				}

			} else if(args[0].equalsIgnoreCase("save")){

				FileMgr.saveData();
				outputDebug(p, "The data was saved successfully.");

			} else if(args[0].equalsIgnoreCase("help") || args[0].equalsIgnoreCase("?")){

				if(args.length == 1){

					p.sendMessage(ChatColor.YELLOW + "/============= RegionMarket Help ================\\");
					p.sendMessage(ChatColor.YELLOW + "      Use /rm help <command> for more information");
					p.sendMessage(ChatColor.YELLOW + "         <> arguments are required, () are not");
					p.sendMessage(ChatColor.YELLOW + "  /rm sell/s <" + ChatColor.WHITE + "region" + ChatColor.YELLOW + "> <" + ChatColor.WHITE + "price" + ChatColor.YELLOW + "> (" + ChatColor.WHITE + "instant" + ChatColor.YELLOW + ")");
					p.sendMessage(ChatColor.YELLOW + "  /rm offer/o <" + ChatColor.WHITE + "region" + ChatColor.YELLOW + "> <" + ChatColor.WHITE + "player" + ChatColor.YELLOW + "> <" + ChatColor.WHITE + "offerprice" + ChatColor.YELLOW + ">");
					p.sendMessage(ChatColor.YELLOW + "  /rm remove/r <" + ChatColor.WHITE + "flag" + ChatColor.YELLOW + "> <"  + ChatColor.WHITE + "region" + ChatColor.YELLOW + "> <"  + ChatColor.WHITE + "seller" + ChatColor.YELLOW + ">");
					p.sendMessage(ChatColor.YELLOW + "  /rm accept/a <" + ChatColor.WHITE + "region" + ChatColor.YELLOW + "> <"  + ChatColor.WHITE +  "buyer"  + ChatColor.YELLOW +  ">");
					p.sendMessage(ChatColor.YELLOW + "  /rm list/l (" + ChatColor.WHITE + "flag" + ChatColor.YELLOW + ") ... ("  + ChatColor.WHITE + "page" + ChatColor.YELLOW + ")");
					p.sendMessage(ChatColor.YELLOW + "  /rm agent <" + ChatColor.WHITE + "flag" + ChatColor.YELLOW + "> <"  + ChatColor.WHITE + "region" + ChatColor.YELLOW + "> ...");
					p.sendMessage(ChatColor.YELLOW + "  /rm reload | /rm save");

				} else {

					args[1] = args[1].toLowerCase();
					if(args.length == 3)
						args[2] = args[2].toLowerCase();

					if(args[1].equals("sell") || args[1].equals("s")){

						p.sendMessage(ChatColor.YELLOW + "/rm sell/s <" + ChatColor.WHITE + "region" + ChatColor.YELLOW + "> <" + ChatColor.WHITE + "price" + ChatColor.YELLOW + "> (" + ChatColor.WHITE + "instant" + ChatColor.YELLOW + ") - " + ChatColor.WHITE + "Puts your region ownership on the market with the set minimum price. If instant is \"true\" the first buyer will get the region no matter what, if nothing is put for the instant argument, it is set to false.");

					} else if(args[1].equals("offer") || args[1].equals("o")){

						p.sendMessage(ChatColor.YELLOW + "/rm offer/o <" + ChatColor.WHITE + "region" + ChatColor.YELLOW + "> <" + ChatColor.WHITE + "player" + ChatColor.YELLOW + "> <" + ChatColor.WHITE + "offerprice" + ChatColor.YELLOW + "> - " + ChatColor.WHITE + "Places your offer on the player's sale of the region.");

					} else if(args[1].equals("remove") || args[1].equals("r")){

						p.sendMessage(ChatColor.YELLOW + "/rm remove/r <" + ChatColor.WHITE + "flag" + ChatColor.YELLOW + "> <"  + ChatColor.WHITE + "region" + ChatColor.YELLOW + "> - " + ChatColor.WHITE + "Removes either an offer you made on the seller's sale of the region using the flag -o or the region from the market with the flag -r.(Seller isn't used for -r)");

					} else if(args[1].equals("accept") || args[1].equals("a")){

						p.sendMessage(ChatColor.YELLOW + "/rm accept/a <" + ChatColor.WHITE + "region" + ChatColor.YELLOW + "> <"  + ChatColor.WHITE +  "buyer"  + ChatColor.YELLOW +  "> - " + ChatColor.WHITE + "Accepts the buyer's offer on your sale of the region. (The player must have enough money at the time you accept the offer)" );

					} else if(args[1].equals("list") || args[1].equals("l")){

						if(args.length == 2)
							p.sendMessage(ChatColor.YELLOW + "/rm list/l (" + ChatColor.WHITE + "flag" + ChatColor.YELLOW + ") ... ("  + ChatColor.WHITE + "page" + ChatColor.YELLOW + ") - " + ChatColor.WHITE + "Lists different info, the default flag is a and default pages for all lists are 1. To learn about a specific flag, use /rm help list <flag>. The flags are a, p, r, o, and i.");
						else if(args[2].equals("a"))
							p.sendMessage(ChatColor.YELLOW + "/rm list/l -a (page) - " + ChatColor.WHITE + "This is used to list all regions on the market.");
						else if(args[2].equals("p"))
							p.sendMessage(ChatColor.YELLOW + "/rm list/l -p <player> (page) - " + ChatColor.WHITE + "This is used to list the regions a player is selling.");
						else if(args[2].equals("r"))
							p.sendMessage(ChatColor.YELLOW + "/rm list/l -r <region> (page) - " + ChatColor.WHITE + "This is used to list all sales for a region.");
						else if(args[2].equals("o"))
							p.sendMessage(ChatColor.YELLOW + "/rm list/l -o <region> (page) - " + ChatColor.WHITE + "This is used to list all offers on your sale of a region");
						else if(args[2].equals("i"))
							p.sendMessage(ChatColor.YELLOW + "/rm list/l -i (page) - " + ChatColor.WHITE + "This is used to list all regions that are instant trades, meaning you auto-buy it when you offer.");
						
					} else if(args[1].equals("agent")){

						p.sendMessage(ChatColor.YELLOW + "/rm agent <" + ChatColor.WHITE + "flag" + ChatColor.YELLOW + "> <"  + ChatColor.WHITE + "region" + ChatColor.YELLOW + "> ... - " + ChatColor.WHITE + "Agents sell regions for you. When adding an agent to a region use the flag -a, the region name to sell, and the price. To remove an agent use -r and the region name. Use -m and the region name to move the agent to your position.");

					} else if(args[1].equals("reload")){

						p.sendMessage(ChatColor.YELLOW + "/rm reload - " + ChatColor.WHITE + "Reloads the data from the save file. (Doesn't save before reloading!");

					} else if(args[1].equals("save")){

						p.sendMessage(ChatColor.YELLOW + "/rm save - " + ChatColor.WHITE + "Saves the current market data into the save file.");

					}

				}

			} else {

				return outputError(p, "Unknown command \"" + args[0] + "\", try /rm ? or /rm help");

			}

		}

		return true;

	}

}

class PListener extends PlayerListener {

	RegionMarket plugin;
	
	public PListener(RegionMarket p){
		
		plugin = p;
		
	}
	
	@Override
	public void onPlayerInteract(PlayerInteractEvent event){

		if(event.getAction().equals(Action.RIGHT_CLICK_BLOCK)){

			Block b = event.getClickedBlock();

			if(b.getTypeId() == 63 || b.getTypeId() == 68){

				Sign sign = (Sign)b.getState();

				if(sign.getLine(0).equals("[AGENT]")){

					RegionAgent agent = plugin.getMarketManager().getAgentManager().getAgent(b.getLocation());
					
					if(agent == null)
						return;

					if(agent.getSeller().equalsIgnoreCase(event.getPlayer().getName())){

						event.getPlayer().sendMessage(ChatColor.YELLOW + "[NPC]Destroy me to stop me from selling.");
						return;

					}

					RegionAccount buyAcc = new RegionAccount(event.getPlayer().getName()), sellAcc = new RegionAccount(agent.getSeller());
					ProtectedRegion region = plugin.getMarketManager().getRegion(event.getPlayer().getWorld(), agent.getRegion());

					if(buyAcc == null || sellAcc == null || region == null)
						return;

					if(!buyAcc.hasEnough(agent.getPrice())){

						event.getPlayer().sendMessage(ChatColor.YELLOW + "[NPC]Sorry, you don't have enough money.");
						return;

					}
					
					if(plugin.getMarketManager().hasReachedMaxRegionsAllowed(event.getPlayer())){
						
						event.getPlayer().sendMessage(ChatColor.YELLOW + "[NPC]Sorry, you've reached the max amount of regions that you can own.");
						return;
						
					}
					
					region.getOwners().addPlayer(event.getPlayer().getName());
					region.getOwners().removePlayer(agent.getSeller());
					buyAcc.subtractMoney(agent.getPrice());
					sellAcc.addMoney(agent.getPrice());

					event.getPlayer().sendMessage(ChatColor.YELLOW + "Congratulations, you now own " + agent.getRegion() + "!");
					Player p = RegionMarket.getPlayer(agent.getSeller());
					if(p != null)
						RegionMarket.outputDebug(p, event.getPlayer().getName() + " has bought your region, " + agent.getRegion() + "!");
					plugin.getMarketManager().removeRegionSale(region, agent.getSeller());

				}

			}

		}

	}

}

class SListener extends ServerListener {

	@Override
	public void onPluginEnable(PluginEnableEvent event){

		String name = event.getPlugin().getDescription().getName().toLowerCase();

		if(name.equals("permissions") && RegionMarket.Permissions == null){

			RegionMarket.Permissions = (Permissions)event.getPlugin();
			RegionMarket.outputConsole("Secondary hook on Permissions.");

		}
		if(name.equals("worldguard") && RegionMarket.WorldGuard == null){

			RegionMarket.WorldGuard = (WorldGuardPlugin)event.getPlugin();
			RegionMarket.outputConsole("Secondary hook on WorldGuard.");

		}
		if(name.equals("iconomy") && RegionMarket.iConomy == null){

			RegionMarket.iConomy = (iConomy)event.getPlugin();
			RegionMarket.outputConsole("Secondary hook on iConomy.");

		}



	}


}

class BListener extends BlockListener {

	RegionMarket plugin;

	public BListener(RegionMarket p){

		plugin = p;

	}

	@Override
	public void onSignChange(SignChangeEvent event){

		if(event.getLine(0).equals("[AGENT]")){

			String[] info = { event.getLine(1), event.getLine(2) };

			if(info.length < 2){

				RegionMarket.outputError(event.getPlayer(), "Unable to get region name/price.");
				return;

			}

			int price = 0;

			try {

				price = Integer.parseInt(info[1]);

			} catch(Exception e){

				RegionMarket.outputError(event.getPlayer(), "Unknown price \"" + info[1] + "\".");
				return;

			}

			if(price < 1){

				RegionMarket.outputError(event.getPlayer(), "Put a price in that is greater than 0!");

			}

			ProtectedRegion region = plugin.getMarketManager().getRegion(event.getPlayer().getWorld(), info[0]);
			Player p = event.getPlayer();

			if(region == null){

				RegionMarket.outputError(p, RegionMarket.ERR_NAME);
				event.setCancelled(true);
				return;

			}

			if(!plugin.getMarketManager().isOwner(p, region)){

				RegionMarket.outputError(p, RegionMarket.ERR_NOOWN);
				event.setCancelled(true);
				return;

			}

			if(!plugin.getMarketManager().isPlayerSelling(region, p.getName())){

				RegionMarket.outputError(p, "You aren't selling that region!");
				event.setCancelled(true);
				return;

			}

			if(plugin.getMarketManager().getAgentManager().hasAddedAgent(p.getName(), region.getId())){

				RegionMarket.outputError(p, "You've already added an agent to the region!");
				event.setCancelled(true);
				return;

			}

			plugin.getMarketManager().getAgentManager().addSignToWorld(event.getBlock().getLocation(), event.getPlayer().getName(), info[0], price);
			RegionMarket.outputDebug(event.getPlayer(), "You've successfully made your agent!");

		}

	}

	@Override
	public void onBlockBreak(BlockBreakEvent event){
		Block b = event.getBlock();
		if(b.getTypeId() == 63 || b.getTypeId() == 68){

			Sign sign = (Sign)b.getState();

			if(sign.getLine(0).equals("[AGENT]")){

				RegionAgent agent = plugin.getMarketManager().getAgentManager().getAgent(b.getLocation());

				if(agent == null)
					return;

				if(!agent.getSeller().equalsIgnoreCase(event.getPlayer().getName())){

					event.setCancelled(true);
					sign.update();
					return;

				}

				plugin.getMarketManager().getAgentManager().deleteAgentFromWorld(agent);
				RegionMarket.outputDebug(event.getPlayer(), "You have removed your agent.");

			}

		}

	}

}

class EListener extends EntityListener {

	private RegionMarket plugin;

	public EListener(RegionMarket p){

		plugin = p;

	}

	@Override
	public void onEntityDamage(EntityDamageEvent event){

		if(!(event instanceof EntityDamageByEntityEvent))
			return;

		EntityDamageByEntityEvent e = (EntityDamageByEntityEvent)event;

		if(!(e.getDamager() instanceof Player))
			return;

		if(e.getCause().equals(DamageCause.ENTITY_ATTACK)){

			RegionAgent agent = plugin.getMarketManager().getAgentManager().getAgent(e.getEntity());

			if(agent == null)
				return;

			plugin.getMarketManager().getAgentManager().leftClickCallback(e.getDamager(), agent);

		}

	}

}

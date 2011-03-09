package us.Crash.RegionMarket;

import org.bukkit.ChatColor;
import org.bukkit.Server;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import com.nijiko.coelho.iConomy.iConomy;
import com.nijikokun.bukkit.Permissions.*;
import com.sk89q.worldguard.bukkit.WorldGuardPlugin;
import com.sk89q.worldguard.protection.ProtectedRegion;

public class RegionMarket extends JavaPlugin {

	public static Server server;
	public static WorldGuardPlugin worldguard = null;
	public static iConomy iConomy = null;
	public static Permissions Permissions = null;
	public static int ERR_ARG = 0, ERR_NAME = 1, ERR_NOOWN = 2, ERR_MONEY = 3, ERR_NOSALE = 4, ERR_OWN = 5;
	private RegionMarketManager marketManager = new RegionMarketManager();
	private FileManager FileMgr;

	public RegionMarketManager getMarketManager(){

		return marketManager;

	}

	public FileManager getFileManager(){

		return FileMgr;

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
			outputConsole("Finished saving, v0.1 disabled.");
			
		} else {
			
			outputConsole("v0.1 disabled.");
			
		}

	}

	@Override
	public void onEnable() {

		server = getServer();

		worldguard = (WorldGuardPlugin)server.getPluginManager().getPlugin("WorldGuard");
		iConomy = (iConomy)server.getPluginManager().getPlugin("iConomy");
		Permissions = (Permissions)server.getPluginManager().getPlugin("Permissions");

		if(worldguard == null){

			outputConsole("Unable to find WorldGuard plugin.");
			server.getPluginManager().disablePlugin(this);
			return;

		} else if(iConomy == null){

			outputConsole("Unable to find iConomy plugin.");
			server.getPluginManager().disablePlugin(this);
			return;

		} else if(Permissions == null){

			outputConsole("Unable to find Permissions plugin.");
			outputConsole("CAUTION : There are no restrictions on commands!");

		} else if(worldguard != null && iConomy != null && Permissions != null)
			outputConsole("Found and hooked all plugins successfully.");


		FileMgr = new FileManager("plugins/RegionMarket/", "saveData.txt", this);

		outputConsole("Loaded version v0.1, by Crash");

	}

	@SuppressWarnings("static-access")
	@Override
	public boolean onCommand(CommandSender sender, Command command, String commandLabel, String[] args) {
		
		if(!(sender instanceof Player))
			return false;

		if(!getServer().getPluginManager().isPluginEnabled("RegionMarket"))
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

				ProtectedRegion region = marketManager.getRegion(args[1]);

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

				marketManager.addRegionSale(region, p, price);
				outputDebug(p, "You added your region to the market.");


			} else if(args[0].equalsIgnoreCase("offer") || args[0].equalsIgnoreCase("o")){

				if(args.length < 4)
					return outputError(p, ERR_ARG);

				ProtectedRegion region = marketManager.getRegion(args[1]);

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

				Integer price = null;

				try {

					price = Integer.parseInt(args[3]);

				} catch(Exception e){

					return outputError(p, "Unknown number, \"" + args[3] + "\".");

				}

				int minPrice = marketManager.getPrice(region, args[2]);

				if(!iConomy.getBank().getAccount(p.getName()).hasEnough(price))
					return outputError(p, "You don't have that much money!");

				if(price < minPrice)
					return outputError(p, "Enter an offer higher than the minimum!");

				marketManager.addOffer(region, args[2], p, price);
				outputDebug(p, "You've successfully offered on " + args[1] + ".");

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

							ProtectedRegion region = marketManager.getRegion(args[2]);

							if(region == null)
								return outputError(p, ERR_NAME);

							if(!marketManager.isOwner(p, region))
								return outputError(p, ERR_NOOWN);

							if(!marketManager.isPlayerSelling(region, p.getName()))
								return outputError(p, "You aren't selling that region!");

							marketManager.listRegionsForSale(p, page, "o", args[2]);

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

						ProtectedRegion region = marketManager.getRegion(args[2]);

						if(region == null)
							return outputError(p, ERR_NAME);

						if(!marketManager.hasOfferedOnRegion(region, args[3], p))
							return outputError(p, "You haven't offered on that region!");

						marketManager.removeOffer(region, args[3], p);
						outputDebug(p, "You have removed your offer.");

					} else if(args[1].equalsIgnoreCase("-r")){
						
						ProtectedRegion region = marketManager.getRegion(args[2]);

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

				ProtectedRegion region = marketManager.getRegion(args[1]);

				if(!marketManager.isPlayerSelling(region, p.getName()))
					return outputError(p, "You aren't selling that region!");

				if(!marketManager.hasOfferedOnRegion(region, p.getName(), args[2]))
					return outputError(p, args[2] + " hasn't offered on your region!");

				if(marketManager.acceptOffer(region, p.getName(), args[2]))
					outputDebug(p, "You have sold your ownership to " + args[2] + ".");

			} else if(args[0].equalsIgnoreCase("reload")){

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
					p.sendMessage(ChatColor.YELLOW + "  /rm sell/s <" + ChatColor.WHITE + "region" + ChatColor.YELLOW + "> <" + ChatColor.WHITE + "price" + ChatColor.YELLOW + ">");
					p.sendMessage(ChatColor.YELLOW + "  /rm offer/o <" + ChatColor.WHITE + "region" + ChatColor.YELLOW + "> <" + ChatColor.WHITE + "player" + ChatColor.YELLOW + "> <" + ChatColor.WHITE + "offerprice" + ChatColor.YELLOW + ">");
					p.sendMessage(ChatColor.YELLOW + "  /rm remove/r <" + ChatColor.WHITE + "flag" + ChatColor.YELLOW + "> <"  + ChatColor.WHITE + "region" + ChatColor.YELLOW + "> <"  + ChatColor.WHITE + "seller" + ChatColor.YELLOW + ">");
					p.sendMessage(ChatColor.YELLOW + "  /rm accept/a <" + ChatColor.WHITE + "region" + ChatColor.YELLOW + "> <"  + ChatColor.WHITE +  "player"  + ChatColor.YELLOW +  ">");
					p.sendMessage(ChatColor.YELLOW + "  /rm list/l (" + ChatColor.WHITE + "flag" + ChatColor.YELLOW + ") ... ("  + ChatColor.WHITE + "page" + ChatColor.YELLOW + ")");
					p.sendMessage(ChatColor.YELLOW + "  /rm reload | /rm save");

				} else {
					
					args[1] = args[1].toLowerCase();
					
					if(args[1].equals("sell") || args[1].equals("s")){
						
						p.sendMessage("/rm sell/s <" + ChatColor.WHITE + "region" + ChatColor.YELLOW + "> <" + ChatColor.WHITE + "price" + ChatColor.YELLOW + "> - " + ChatColor.WHITE + "Puts your region ownership on the market with the set minimum price.");
						
					} else if(args[1].equals("offer") || args[1].equals("o")){
						
						p.sendMessage(ChatColor.YELLOW + "/rm offer/o <" + ChatColor.WHITE + "region" + ChatColor.YELLOW + "> <" + ChatColor.WHITE + "player" + ChatColor.YELLOW + "> <" + ChatColor.WHITE + "offerprice" + ChatColor.YELLOW + "> - " + ChatColor.WHITE + "Places your offer on thplayer's sale of the region.");
						
					} else if(args[1].equals("remove") || args[1].equals("r")){
						
						p.sendMessage(ChatColor.YELLOW + "  /rm remove/r <" + ChatColor.WHITE + "flag" + ChatColor.YELLOW + "> <"  + ChatColor.WHITE + "region" + ChatColor.YELLOW + "> - " + ChatColor.WHITE + "Removes either an offer you made on the region using the flag -o or the region from the market with the flag -r");
						
					} else if(args[1].equals("accept") || args[1].equals("a")){
						
						p.sendMessage(ChatColor.YELLOW + "  /rm accept/a <" + ChatColor.WHITE + "region" + ChatColor.YELLOW + "> <"  + ChatColor.WHITE +  "player"  + ChatColor.YELLOW +  "> - " + ChatColor.WHITE + "Accepts player's offer on your sale of the region. (The player must have enough money at the time you accept the offer)" );
						
					} else if(args[1].equals("list") || args[1].equals("l")){
						
						p.sendMessage(ChatColor.YELLOW + "  /rm list/l (" + ChatColor.WHITE + "flag" + ChatColor.YELLOW + ") ... ("  + ChatColor.WHITE + "page" + ChatColor.YELLOW + ") - " + ChatColor.WHITE + "Lists different info. The flag -a is used to list all regions on the market, the flag -p is used to list the regions a player is selling(... is the player name), the flag -r is used to list all sales for a region(... is the region name), the flag -o is used to list all offers on your sale of a region(... is the region name). The default page is 1 and the default flag is -a if either one is not specified.");
						
					} else if(args[1].equals("reload")){
						
						p.sendMessage(ChatColor.YELLOW + "  /rm reload - " + ChatColor.WHITE + "Reloads the data from the save file. (Doesn't save before reloading!");
						
					} else if(args[1].equals("save")){
						
						p.sendMessage(ChatColor.YELLOW + "  /rm save - " + ChatColor.WHITE + "Saves the current market data into the save file.");
						
					}
					
				}

			} else {

				return outputError(p, "Unknown command \"" + args[0] + "\", try /rm ? or /rm help");

			}

		}

		return true;

	}

}


package us.Crash.RegionMarket;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;

import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.util.config.Configuration;

public class FileManager {

	private final String fileName, pathDir, configPath;
	private final RegionMarket plugin;
	BufferedWriter out = null;

	public FileManager(String dataPath, String dataFile, RegionMarket main){

		fileName = dataFile;
		pathDir = dataPath;
		plugin = main;

		configPath = "plugins/RegionMarket/config.yml";
		
		loadData();

	}

	public String getDataFileName(){ return fileName; }

	public String getFullPath(){ return pathDir + fileName; }

	public void saveData(){

		Configuration config = new Configuration(new File(configPath));
		config.load();
		
		config.setProperty("use-max-regions", RegionMarket.useMaxRegions);

		config.save();
		
		openSaveFile();
		
		writeLine("nr=" + plugin.getMarketManager().getRegionsForSale().size());

		for(String region : plugin.getMarketManager().getRegionsForSale().keySet()){

			ArrayList<RegionSale> sales = plugin.getMarketManager().getRegionsForSale().get(region);

			writeLine("n=" + region + "," + sales.size());
			for(int i = 0; i < sales.size(); i++){

				ArrayList<RegionOffer> offers = plugin.getMarketManager().getOffersFor(region, sales.get(i).getSeller());
				
				RegionAgent agent = plugin.getMarketManager().getAgentManager().getAgent(sales.get(i).getSeller(), region);
				
				writeLine(sales.get(i).getSeller() + "=" + sales.get(i).getPrice() + "," + offers.size() + "," + (agent == null ? -1 : agent.getType()) + "," + (sales.get(i).isInstant() ? "1" : "0"));
				if(agent != null){
				
					if(agent.getType() == 0)
						writeLine(plugin.getServer().getWorlds().indexOf(agent.getLocation().getWorld()) + "," + agent.getLocation().getX() + "," + agent.getLocation().getY() + "," + agent.getLocation().getZ() + "," + Math.round(agent.getLocation().getYaw()) + "," + Math.round(agent.getLocation().getPitch()) + "," + agent.getPrice());
					if(agent.getType() == 1)
						writeLine(plugin.getServer().getWorlds().indexOf(agent.getLocation().getWorld()) + "," + agent.getLocation().getX() + "," + agent.getLocation().getY() + "," + agent.getLocation().getZ() + "," + agent.getPrice());
				}
				String offerLine = "";
				for(int j = 0; j < offers.size(); j++){

					if(j == offers.size() - 1)
						offerLine += offers.get(i).getOfferer() + "," + offers.get(i).getPrice();
					else
						offerLine += offers.get(i).getOfferer() + "," + offers.get(i).getPrice() + ",";

				}
				writeLine(offerLine);

			}


		}

		closeSaveFile();

	}

	public boolean loadData(Player... reloader){

		BufferedReader in = null;
		int numTimes = 0, completed = 0;

		if(new File(pathDir).mkdir()){

			try {
				new File(pathDir + fileName).createNewFile();
				openSaveFile();
				writeLine("nr=0");
				closeSaveFile();
			} catch (IOException e) { 
				RegionMarket.outputConsole("Error when generating save file.");
				return false;
			}
			try {
				new File(configPath).createNewFile();
			} catch (IOException e) { 
				RegionMarket.outputConsole("Error when generating config file.");
				return false;
			}
			if(reloader.length == 1)
				reloader[0].sendMessage(ChatColor.AQUA + "[RegionMarket] " + ChatColor.YELLOW + "Created RM directory and save file.");
			RegionMarket.outputConsole("Created RM directory and generated save data file and config file.");

		} else {

			try {
				if(!new File(pathDir + fileName).exists()){

					new File(pathDir + fileName).createNewFile();
					RegionMarket.outputConsole("Created save data file.");
					openSaveFile();
					writeLine("nr=0");
					closeSaveFile();

				}
			} catch (IOException e) {
				RegionMarket.outputConsole("Error when generating save file.");
				return false;
			}
			
			try {
				if(!new File(configPath).exists()){

					new File(configPath).createNewFile();
					RegionMarket.outputConsole("Created config file.");
					Configuration config = new Configuration(new File(configPath));
					config.setProperty("use-max-regions", false);
					config.save();

				}
			} catch (IOException e) {
				RegionMarket.outputConsole("Error when generating config file.");
				return false;
			}
			
		}

		try {

			in = new BufferedReader(new FileReader(pathDir + fileName));

		} catch(Exception e){

			RegionMarket.outputConsole("Error when opening data file.");
			return false;

		}
		
		Configuration config = new Configuration(new File(configPath));
		config.load();
		
		RegionMarket.useMaxRegions = config.getBoolean("use-max-regions", false);

		boolean regionReadError = false;

		try {

			String line = in.readLine();

			if(line == null){

				RegionMarket.outputConsole("Unable to find number of regions on first line.");
				in.close();
				return false;

			}
			numTimes = Integer.parseInt(line.split("=")[1]);

			String region = "";
			int sellerAmt, offerAmt;
			String[] sellers, offerers;
			int[] prices, offerprices;
			boolean[] instant;

			for(int i = 0; i < numTimes; i++){

				if(regionReadError){

					String nextRegion = in.readLine();
					if(nextRegion == null)
						nextRegion = "";
					try {

						while(!nextRegion.contains("n=") && in.ready()){

							nextRegion = in.readLine();
							if(nextRegion == null)
								nextRegion = "";

						}
						if(!in.ready())
							break;

						region = nextRegion;

					} catch (Exception e){

					}

				}

				try {

					if(regionReadError){

						regionReadError = false;

					} else {

						region = in.readLine();

					}

					sellerAmt = Integer.parseInt(region.split("=")[1].split(",")[1]);
					region = region.split("=")[1].split(",")[0];
					sellers = new String[sellerAmt];
					prices = new int[sellerAmt];
					instant = new boolean[sellerAmt];

					for(int j = 0; j < sellerAmt; j++){

						int agentType = -1;
						sellers[j] = in.readLine();
						prices[j] = Integer.parseInt(sellers[j].split("=")[1].split(",")[0]);
						offerAmt = Integer.parseInt(sellers[j].split("=")[1].split(",")[1]);
						try {
							agentType = Integer.parseInt(sellers[j].split("=")[1].split(",")[2]);
						} catch(ArrayIndexOutOfBoundsException e){
							agentType = -1;
						}
						try{
							instant[j] = (sellers[j].split("=")[1].split(",")[3].equals("1") ? true : false);
						} catch(ArrayIndexOutOfBoundsException e){
							instant[j] = false;
						}
						sellers[j] = sellers[j].split("=")[0];
						offerers = new String[offerAmt];
						offerprices = new int[offerAmt];
						
						line = in.readLine();
						
						if(agentType == 0){
							
							String[] args = line.split(",");
							int w = Integer.parseInt(args[0]), price = Integer.parseInt(args[6]);
							double x = Double.parseDouble(args[1]), y = Double.parseDouble(args[2]), z = Double.parseDouble(args[3]);
							float yaw = Float.parseFloat(args[4]), pitch = Float.parseFloat(args[5]);
							World world = RegionMarket.server.getWorlds().get(w);
							Location loc = new Location(world, x, y, z, yaw, pitch);
							plugin.getMarketManager().getAgentManager().makeNewAgent(loc, sellers[j], region, price);
							line = in.readLine();
							
						} else if(agentType == 1){
							
							String[] args = line.split(",");
							int w = Integer.parseInt(args[0]), price = Integer.parseInt(args[4]);
							double x = Double.parseDouble(args[1]), y = Double.parseDouble(args[2]), z = Double.parseDouble(args[3]);
							World world = RegionMarket.server.getWorlds().get(w);
							Location loc = new Location(world, x, y, z);
							plugin.getMarketManager().getAgentManager().addSignToWorld(loc, sellers[j], region, price);
							line = in.readLine();
							
						}
						
						if(line != null){
							
							String[] offerline = line.split(",");
							plugin.getMarketManager().addRegionSale(region, sellers[j], prices[j], instant[j]);
							for(int k = 0, l = 0, p1 = 0, p2 = 1; k < offerAmt; k++, l++, p1+=2, p2+=2){

								offerers[k] = offerline[p1];
								offerprices[l] = Integer.parseInt(offerline[p2]);
								plugin.getMarketManager().addOffer(region, sellers[j], offerers[k], offerprices[k]);

							}

						}

					}

					completed++;

				} catch(Exception e){

					RegionMarket.outputConsole("Error while loading region " + region);
					regionReadError = true;

				}

			}

		} catch(Exception e){

			RegionMarket.outputConsole("Error while loading was initializing.");
			return false;

		}

		if(reloader.length == 1)
			reloader[0].sendMessage(ChatColor.AQUA + "[RegionMarket] " + ChatColor.YELLOW + "Loaded regions : [" + completed + "/" + numTimes + "]");
		RegionMarket.outputConsole("Successfully loaded regions : [" + completed + "/" + numTimes + "]");

		return true;

	}

	public void openSaveFile(){

		try {
			out = new BufferedWriter(new FileWriter(pathDir + fileName));
		} catch (IOException e) {
			RegionMarket.outputConsole("Error when opening the save file.");
		}

	}

	public void writeLine(String str){

		try {

			out.write(str + "\r\n");

		} catch(Exception e){

			RegionMarket.outputConsole("Error while writing line" + str);

		}

	}

	public void closeSaveFile(){

		try {
			out.close();
		} catch (IOException e) {
			RegionMarket.outputConsole("Error when closing the save file.");
		}

	}

}

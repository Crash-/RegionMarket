package us.Crash.RegionMarket;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

public class FileManager {

	private final String fileName, pathDir;
	private final RegionMarket plugin;
	BufferedWriter out = null;

	public FileManager(String dataPath, String dataFile, RegionMarket main){

		fileName = dataFile;
		pathDir = dataPath;
		plugin = main;

		loadData();

	}

	public String getDataFileName(){ return fileName; }

	public String getFullPath(){ return pathDir + fileName; }

	public void saveData(){

		openSaveFile();

		writeLine("nr=" + plugin.getMarketManager().getRegionsForSale().size());

		for(String region : plugin.getMarketManager().getRegionsForSale().keySet()){

			ArrayList<RegionSale> sales = plugin.getMarketManager().getRegionsForSale().get(region);

			writeLine("n=" + region + "," + sales.size());
			for(int i = 0; i < sales.size(); i++){

				ArrayList<RegionOffer> offers = plugin.getMarketManager().getOffersFor(region, sales.get(i).getSeller());

				writeLine(sales.get(i).getSeller() + "=" + sales.get(i).getPrice() + "," + offers.size());
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
			if(reloader.length == 1)
				reloader[0].sendMessage(ChatColor.AQUA + "[RegionMarket] " + ChatColor.YELLOW + "Created RM directory and save file.");
			RegionMarket.outputConsole("Created RM directory and generated save data file.");

		} else {

			try {
				if(new File(pathDir + fileName).createNewFile()){

					RegionMarket.outputConsole("Created save data file.");
					openSaveFile();
					writeLine("nr=0");
					closeSaveFile();

				}
			} catch (IOException e) {
				RegionMarket.outputConsole("Error when generating save file.");
				return false;
			}

		}

		try {

			in = new BufferedReader(new FileReader(pathDir + fileName));

		} catch(Exception e){

			RegionMarket.outputConsole("Error when opening data file.");
			return false;

		}

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

					for(int j = 0; j < sellerAmt; j++){

						sellers[j] = in.readLine();
						prices[j] = Integer.parseInt(sellers[j].split("=")[1].split(",")[0]);
						offerAmt = Integer.parseInt(sellers[j].split("=")[1].split(",")[1]);
						sellers[j] = sellers[j].split("=")[0];
						offerers = new String[offerAmt];
						offerprices = new int[offerAmt];
						line = in.readLine();
						if(line != null){

							String[] offerline = line.split(",");
							plugin.getMarketManager().addRegionSale(region, sellers[j], prices[j]);
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

import org.tbot.bot.TBot;
import org.tbot.internal.AbstractScript;
import org.tbot.internal.Manifest;
import org.tbot.internal.handlers.LogHandler;
import org.tbot.internal.handlers.RandomHandler;
import org.tbot.methods.Game;
import org.tbot.methods.Players;
import org.tbot.methods.Random;
import org.tbot.util.botcontrol.BotControlConnection;
import org.tbot.util.botcontrol.BotControlMessageEvent;
import org.tbot.util.botcontrol.BotControlMessageListener;
import org.tbot.wrappers.Player;

import java.io.IOException;

@Manifest(name = "AutoScouter1", authors = "sudo_", version = 0.1, description = "Auto Scouter")
public class AutoScouter extends AbstractScript implements BotControlMessageListener {

	String server = "irc.swiftirc.net";
	String nickname = "AutoScouter";
	String channel = "##sudo";
	
	Player localclient = null;
	private final BotControlConnection connection;

	public AutoScouter() {
		this.connection = new BotControlConnection(server, nickname);
		this.connection.setChannelName(channel);
		try {
			this.connection.connect();
			if (!this.connection.isClosed()) {

				this.connection.joinChannel();
				this.connection.addListener(this);
				this.connection.startListening();
			}
		} catch (IOException e) {
			e.printStackTrace();
		}

	}

	@Override
	public boolean onStart() {
		return true;
	}

	private boolean messageSend = false;
	private boolean hopping;

	@Override
	public int loop() {
		if (hopping) {
			TBot.getBot().getScriptHandler().getRandomHandler().get(RandomHandler.AUTO_LOGIN).enable();
		} else {
			TBot.getBot().getScriptHandler().getRandomHandler().get(RandomHandler.AUTO_LOGIN).disable();
		}
		if (!Game.isLoggedIn()) {
			return 600;
		}

		Player localclient = Players.getLocal();
		if (messageSend == true) {
			Player[] players = Players.getLoaded();

			if (players.length > 1) {
				for (Player p : players) {
					if (p != null && !localclient.equals(p)) {
		
						this.connection.sendMessage("[+] Found " + p.getName() + " (Level: " + p.getCombatLevel() + ")");
						sleep(500,800);

					}
				}
			} else {
				int currentWorld = Game.getCurrentWorld();
				this.connection.sendMessage("No players logged in World "+ currentWorld + "...");
			}
			messageSend = false;
		}
		randhop();

		return 600;
	}
	public void randhop() {
		if(hopping) {
			//Game.instaHopRandomF2P();
			sleep(3000,6000);
			Game.instaHopNextP2P();
			int currentWorld = Game.getCurrentWorld();
			this.connection.sendMessage("Loading World " + currentWorld + "...");
			messageSend = true;
			
		} else {
			Game.logout();
		}
		
	}

	@Override
	public void messageReceived(BotControlMessageEvent bcme) {
		LogHandler.log(bcme.getMessage());
		if (bcme.getMessage().equalsIgnoreCase("!logout")) {
			Game.logout();
		}
		if (bcme.getMessage().startsWith("!world ")) {
			int world = Integer.parseInt(bcme.getMessage().toLowerCase()
					.replace("!world ", ""));
			Game.instaHop(world);
			this.connection.sendMessage(bcme.getSender() + ": New World "
					+ world + ".");
		}
		if (bcme.getMessage().equalsIgnoreCase("!hop")) {
			int world = Random.nextInt(302, 386);
			Game.instaHop(world);
			this.connection.sendMessage(bcme.getSender() + ": New World "
					+ world + ".");
		}
		if (bcme.getMessage().equalsIgnoreCase("!scout")) {
			Game.instaHopRandomF2P();
			int currentWorld = Game.getCurrentWorld();
			this.connection.sendMessage(bcme.getSender() + ": New World "
					+ currentWorld + ".");
			messageSend = true;
		}
		if (bcme.getMessage().equalsIgnoreCase("!starthop")) {
			hopping = true;
			this.connection.sendMessage("AutoScouter has successfully started. Please wait a few seconds while the scout sets itself up.");
		}
		if (bcme.getMessage().equalsIgnoreCase("!stophop")) {
			hopping = false;
			this.connection.sendMessage("AutoScouter has successfully stopped. Thanks for using me :)");
		}
	}

}
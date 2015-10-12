package hexane.teammod;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import net.minecraft.command.ICommand;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.scoreboard.ScorePlayerTeam;
import net.minecraft.scoreboard.Scoreboard;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.ChatStyle;
import net.minecraft.util.EnumChatFormatting;
import net.minecraft.util.IChatComponent;
import net.minecraft.world.World;

public class MyTeamCommand implements ICommand
{
	public MyTeamCommand() {
	}
	private final String dataTeamKey = "<t>";
	private static final Map<String, String> colorMap;
   static
   {
       colorMap = new HashMap<String, String>();
       colorMap.put("black", "\u00A70");
       colorMap.put("dark_blue", "\u00A71");
       colorMap.put("dark_green", "\u00A72");
       colorMap.put("dark_aqua", "\u00A73");
       colorMap.put("dark_red", "\u00A74");
       colorMap.put("dark_purple", "\u00A75");
       colorMap.put("gold", "\u00A76");
       colorMap.put("gray", "\u00A77");
       colorMap.put("dark_gray", "\u00A78");
       colorMap.put("blue", "\u00A79");
       colorMap.put("green", "\u00A7a");
       colorMap.put("aqua", "\u00A7b");
       colorMap.put("red", "\u00A7c");
       colorMap.put("light_purple", "\u00A7d");
       colorMap.put("yellow", "\u00A7e");
       colorMap.put("white", "\u00A7f");
       colorMap.put("reset", "\u00A7r");
       colorMap.put("god", "\u00A7k");
       colorMap.put("bold", "\u00A7l");
       colorMap.put("italic", "\u00A7o");
   }
   private static final Map<String, String> colorDisplayMap;
   static
   {
   	 colorDisplayMap = new HashMap<String, String>();
   	 colorDisplayMap.put("\u00A70", "\u00A70black");
   	 colorDisplayMap.put("\u00A71","\u00A71dark_blue");
       colorDisplayMap.put("\u00A72","\u00A72dark_green");
       colorDisplayMap.put("\u00A73", "\u00A73dark_aqua");
       colorDisplayMap.put("\u00A74", "\u00A74dark_red");
       colorDisplayMap.put("\u00A75", "\u00A75dark_purple");
       colorDisplayMap.put("\u00A76", "\u00A76gold");
       colorDisplayMap.put("\u00A77", "\u00A77gray");
       colorDisplayMap.put("\u00A78", "\u00A78dark_gray");
       colorDisplayMap.put("\u00A79", "\u00A79blue");
       colorDisplayMap.put("\u00A7a", "\u00A7agreen");
       colorDisplayMap.put("\u00A7b", "\u00A7baqua");
       colorDisplayMap.put("\u00A7c", "\u00A7cred");
       colorDisplayMap.put("\u00A7d", "\u00A7dlight_purple");
       colorDisplayMap.put("\u00A7e", "\u00A7eyellow");
       colorDisplayMap.put("\u00A7f", "\u00A7fwhite");
       colorDisplayMap.put("\u00A7r", "\u00A7rNone");
       colorDisplayMap.put("", "\u00A7rNone");
   }
   
	
	@Override
	public String getCommandName()
	{
		return "myteam";
	}

	@Override
	public String getCommandUsage(ICommandSender icommandsender)
	{
		return "myteam [add <player>...|disband|leave|color [color]|create <team>|list|help]";
	}

	@Override
	public List<String> getCommandAliases()
	{
		return null;
	}

	@SuppressWarnings("unchecked")
	@Override
	public void processCommand(ICommandSender sender, String[] args)
	{
		try {
			EntityPlayer player = (EntityPlayer)sender;
			MinecraftServer server = MinecraftServer.getServer();
			World world = server.getEntityWorld();
			Scoreboard scoreboard = world.getScoreboard();
			ScorePlayerTeam dataTeam = getDataTeam(scoreboard);
			String commandString = "/myteam";
			for (String arg : args) {
				commandString += " " + arg;
			}
			sendChatToConsole(server, "Sender " + sender.getCommandSenderName() + " used command " + commandString);
			ScorePlayerTeam team = scoreboard.getPlayersTeam(player.getCommandSenderName());
			if(args.length == 0) {
				if (team == null) { throw new NoTeamException(); }
				String teamName = team.getRegisteredName();
				String teamColorCode = getTeamColor(team);
				sendChat(sender, "Your team: " + teamName);
				if (colorDisplayMap.containsKey(teamColorCode)) {
					sendChat(sender, "Team color: " + colorDisplayMap.get(teamColorCode));
				}
				
				Collection<String> teamCollection = team.getMembershipCollection();
				sendChat(sender, "Team members:");
				sendChat(sender, teamCollection);
				return;
			}
			if ("help".equalsIgnoreCase(args[0])) { //ignore args
				sendChat(sender, this.getCommandUsage(sender));
				return;
			}
			if ("list".equalsIgnoreCase(args[0])) { //ignore args
				Collection<ScorePlayerTeam> teamCollection = scoreboard.getTeamNames(); 
				if (teamCollection.size() <= 1) { sendChat(sender, "No teams to list"); }
				else {
					for (ScorePlayerTeam teamToList : (Collection<ScorePlayerTeam>)scoreboard.getTeams()) {
						if (teamToList.getRegisteredName().equals(dataTeamKey)) { continue; }
						sendChat(sender, "Team name: " + teamToList.getRegisteredName());
						Collection<String> playerCollection = teamToList.getMembershipCollection();
						if (playerCollection.size() == 0) { sendChat(sender, "No players"); }
						else {
							sendChat(sender, "Player list:");
							for (String playerToList : playerCollection) {
								sendChat(sender, playerToList);
							}
						}
					}
				}
				return;
			} 

			if ("create".equalsIgnoreCase(args[0])) { //myteam create <team> [playerlist]
				if (team != null) {
					sendErrorChat(sender, "You already belong to a team. Leave or disband before creating a new one.");
					throw new InvalidArgumentsException();
				}
				String newTeamName;
				try { newTeamName = args[1]; }
				catch (ArrayIndexOutOfBoundsException e) {
					sendErrorChat(sender, "Missing team name.");
					sendErrorChat(sender, "Correct syntax: /myteam create <team> [player1] [player2] ...");
					throw new InvalidArgumentsException();
				}
				if (newTeamName == dataTeamKey) {
					sendErrorChat(sender, "Invalid team name. Please choose a different name");
					throw new InvalidArgumentsException();
				}
				try { team = scoreboard.createTeam(newTeamName); }
				catch (IllegalArgumentException e) {
					sendChat(sender, e.getMessage());
					throw new InvalidArgumentsException();
				}
				sendChat(sender, "Created new team " + newTeamName);
				team.setNamePrefix("\u00A7r\u00A7r");
				scoreboard.func_151392_a(sender.getCommandSenderName(), newTeamName);
				updatePrefix(server, dataTeam, team);
				if (args.length > 2) {
					for (EntityPlayer playerToAdd : parsePlayerList(world, (String[])Arrays.copyOfRange(args, 2, args.length))) {
						scoreboard.func_151392_a(playerToAdd.getCommandSenderName(), newTeamName);
					}
				}
				return;
			}
			if (team == null) {
				throw new NoTeamException();
			}
			String teamName = team.getRegisteredName();
			if ("add".equalsIgnoreCase(args[0])) { //myteam add <playerlist>
				if (args.length > 1) {
					for (EntityPlayer playerToAdd : parsePlayerList(world, (String[])Arrays.copyOfRange(args, 1, args.length))) {
						sendChat(sender, playerToAdd.toString());
						scoreboard.func_151392_a(playerToAdd.getCommandSenderName(), teamName);
						sendChat(sender, "Added player " + playerToAdd.getCommandSenderName() + " to your team");
					}
				}
				else {
					sendErrorChat(sender, "Missing player to add");
					sendErrorChat(sender, "Correct syntax: /myteam add <player1> [player2] ...");
					throw new InvalidArgumentsException();
				}
				return;
			}
			if ("remove".equalsIgnoreCase(args[0])) { //myteam remove <playerlist>
				if (args.length > 1) {
					for (String playerToRemove : (String[])Arrays.copyOfRange(args, 1, args.length)) {
						if (team.getMembershipCollection().contains(playerToRemove)) {
							scoreboard.removePlayerFromTeams(playerToRemove);
							sendChat(sender, "Removed player " + playerToRemove + " from your team");
						}
						else {
							sendErrorChat(sender, "Cannot remove. Player " + playerToRemove + " is not on your team.");
						}
					}
				}
				else {
					sendErrorChat(sender, "Missing player to remove");
					sendErrorChat(sender, "Correct syntax: /myteam remove <player1> [player2] ...");
					throw new InvalidArgumentsException();
				}
				return;
			}
			if ("leave".equalsIgnoreCase(args[0])) { //ignore args
				sendChat(sender, "Removed you from team " + team.getRegisteredName());
				scoreboard.removePlayerFromTeams(player.getCommandSenderName());
				return;
			}
			if ("disband".equalsIgnoreCase(args[0])) { //ignore args
				sendChat(sender, "Disbanded team " + team.getRegisteredName());
				scoreboard.removeTeam(team);
			}
			if ("color".equalsIgnoreCase(args[0])) { //myteam color <colorname>
				if (args.length > 1) {
					if (colorMap.containsKey(args[1]))
					{
						String colorCode = colorMap.get(args[1]);
						sendChat(sender, "Set team color to " + args[1]);
						
						setTeamColor(team, colorCode);
						updatePrefix(server, dataTeam, team);
					}
					else {
						sendErrorChat(sender, "Unknown color " + args[1]);
						sendChat(sender,
								"Acceptable colors: \u00A70Black, \u00A71Dark Blue, \u00A72Dark Green, \u00A73Dark Aqua, \u00A74Dark Red, " +
								"\u00A75Dark Purple, \u00A76Gold, \u00A77Gray, \u00A78Dark Gray, \u00A79Blue, \u00A7aGreen, \u00A7bAqua, \u00A7cRed, \u00A7dLight Purple, " +
								"\u00A7eYellow, \u00A7fWhite");
						throw new InvalidArgumentsException();
					}
				}
				else {
					String colorCode = getTeamColor(team);
					if (colorDisplayMap.containsKey(colorCode)) {
						String displayName = colorDisplayMap.get(team.getColorPrefix());
						sendChat(sender, "Team color: " + displayName);
					}
					else
					{
						sendErrorChat(sender, "Could not parse team color");
						sendChatToConsole(server, "Error processing command. Team name " + teamName);
					}
				}
				return;
			}
			sendErrorChat(sender, "Unknown argument \"" + args[0] + "\"");
			throw new InvalidArgumentsException();
		}
		catch (NoTeamException e) {
			sendErrorChat(sender, "You do not belong to a team.");
			sendErrorChat(sender, "Make a team with /myteam create.");
		}
		catch (NoPlayerException e) {
			sendErrorChat(sender, "Could not find player " + e.getMessage());
		}
		catch (InvalidArgumentsException e) {
			return;
		}
	}

	private void updatePrefix(MinecraftServer server, ScorePlayerTeam dataTeam, ScorePlayerTeam team) {
		String teamName = team.getRegisteredName();
		
		String prefix = dataTeam.getColorPrefix();
		if (prefix == null) { prefix = ""; }
		String suffix = dataTeam.getColorSuffix();
		if (suffix == null) { suffix = ""; }
		String teamColor = getTeamColor(team);
		sendChatToConsole(server, "Got prefix format " + prefix);
		prefix = prefix.replaceAll("<t>", teamName).replaceAll("<c>", teamColor);
		suffix = suffix.replaceAll("<t>", teamName).replaceAll("<c>", teamColor);
		for (Map.Entry<String, String> colorPair :colorMap.entrySet()) {
			String key = colorPair.getKey();
			String value = colorPair.getValue();
			prefix = prefix.replaceAll("<" + key + ">" , value);
			suffix = suffix.replaceAll("<" + key + ">" , value);
		}
		sendChatToConsole(server, "Formatted to " + teamColor + "\u00A7r" + prefix);
		sendChatToConsole(server, "Got suffix format " + suffix);
		sendChatToConsole(server, "Formatted to " + suffix);
		team.setNamePrefix(teamColor + "\u00A7r" + prefix);
		team.setNameSuffix(suffix);
	}
	private String getTeamColor(ScorePlayerTeam team) {
		return team.getColorPrefix().substring(0,2);
	}
	private void setTeamColor(ScorePlayerTeam team, String newTeamColor) {
		String prefix = team.getColorPrefix();
		String prefixSplit;
		try {
			prefixSplit = prefix.substring(4);
		}
		catch (NullPointerException e) {
			prefix = "\u00A7r\u00A7r";
			prefixSplit = "";
		}
		catch (StringIndexOutOfBoundsException e) {
			prefixSplit = "";
		}
		team.setNamePrefix(newTeamColor + prefixSplit);
	}
	private ScorePlayerTeam getDataTeam(Scoreboard scoreboard) {
		ScorePlayerTeam dataTeam = scoreboard.getTeam(dataTeamKey);
		if (dataTeam == null) {
			scoreboard.createTeam(dataTeamKey);
			dataTeam = scoreboard.getTeam(dataTeamKey);
		}
		if (dataTeam.getColorPrefix() == null) {
			dataTeam.setNamePrefix("<c>");
		}
		if (dataTeam.getColorSuffix() == null) {
			dataTeam.setNameSuffix("\u00A7r");
		}
		return dataTeam;
	}
	
	private void sendChatToConsole(MinecraftServer server, String line) {
		IChatComponent component = new ChatComponentText(line);
		server.addChatMessage(component);
	}
	private void sendErrorChat(ICommandSender sender, String line) {
		IChatComponent component = new ChatComponentText(line);
		ChatStyle chatStyle = new ChatStyle();
		chatStyle.setColor(EnumChatFormatting.RED);
		component.setChatStyle(chatStyle);
		sender.addChatMessage(component);
	}
	private void sendChat(ICommandSender sender, String line)
	{
		IChatComponent component = new ChatComponentText(line);
		sender.addChatMessage(component);
	}
	private void sendChat(ICommandSender sender, String[] lines)
	{
		for (String line : lines) {
			IChatComponent component = new ChatComponentText(line);
			sender.addChatMessage(component);
		}
	}
	private void sendChat(ICommandSender sender, Collection<String> lines) {
		for (String line : lines) {
			IChatComponent component = new ChatComponentText(line);
			sender.addChatMessage(component);
		}
	}
	private List<EntityPlayer> parsePlayerList(World world, String[] playerNames) {
		List<EntityPlayer> playerList = new ArrayList<EntityPlayer>();
		for (String playerName : playerNames) {
			EntityPlayer player = world.getPlayerEntityByName(playerName);
			if (player == null) {throw new NoPlayerException(playerName);}
			playerList.add(player);
		}
		return playerList;
	}
	private class NoTeamException extends NullPointerException {
		/**
		 * 
		 */
		private static final long serialVersionUID = 1L;
		private NoTeamException() {
			super();
		}
		private NoTeamException(String message) {
			super(message);
		}
	}
	private class NoPlayerException extends NullPointerException {
		/**
		 * 
		 */
		private static final long serialVersionUID = 1L;
		private NoPlayerException() {
			super();
		}
		private NoPlayerException(String message) {
			super(message);
		}
	}
	private class InvalidArgumentsException extends Exception {
		/**
		 * 
		 */
		private static final long serialVersionUID = 1L;
		private InvalidArgumentsException() {
			super();
		}
		private InvalidArgumentsException(String message) {
			super(message);
		}
	}

	@Override
	public boolean canCommandSenderUseCommand(ICommandSender sender)
	{
		return (sender instanceof EntityPlayer);
	}

	@Override
	public List<String> addTabCompletionOptions(ICommandSender icommandsender,
			String[] astring)
	{
		return null;
	}

	@Override
	public boolean isUsernameIndex(String[] astring, int i)
	{
		return false;
	}

	@Override
	public int compareTo(Object o)
	{
		return 0;
	}
}
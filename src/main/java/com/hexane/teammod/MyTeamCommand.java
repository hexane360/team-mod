package com.hexane.teammod;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import net.minecraft.command.CommandException;
import net.minecraft.command.ICommand;
import net.minecraft.command.ICommandSender;
import net.minecraft.command.NumberInvalidException;
import net.minecraft.command.PlayerNotFoundException;
import net.minecraft.command.SyntaxErrorException;
import net.minecraft.command.WrongUsageException;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.scoreboard.ScorePlayerTeam;
import net.minecraft.scoreboard.Scoreboard;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.util.text.Style;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.common.command.SelectorHandlerManager;

//todo: new color storage & /team

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
		colorDisplayMap.put("\u00A7r", "\u00A7rreset");
		colorDisplayMap.put("\u00A7k", "\u00A7kgod");
		colorDisplayMap.put("\u00A7l", "\u00A7lbold");
		colorDisplayMap.put("\u00A7o", "\u00A7oitalic");
		colorDisplayMap.put("\u00A7r", "\u00A7rNone");
		colorDisplayMap.put("", "\u00A7rNone");
	}

	@Override
	public String getName() {
		return "myteam";
	}

	@Override
	public String getUsage(ICommandSender icommandsender) {
		return "/myteam [add <player>...|remove <player>...|disband|leave|color [color]|create <team> [player]...|list|help]";
	}

	@Override
	public List<String> getAliases() {
		return Collections.<String>emptyList();
	}

	@Override
	public void execute(MinecraftServer server, ICommandSender sender, String[] args) throws CommandException {
		System.out.println("/myteam " + String.join(" ", args));
		try {
			EntityPlayer player = (EntityPlayer)sender;
			World world = server.getEntityWorld();
			Scoreboard scoreboard = world.getScoreboard();
			ScorePlayerTeam dataTeam = getDataTeam(scoreboard);
			ScorePlayerTeam team = scoreboard.getPlayersTeam(player.getCommandSenderEntity().getName());
			if(args.length == 0) {
				if (team == null) { throw new NoTeamException(); }
				String teamName = team.getName();
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
				sendChat(sender, this.getUsage(sender));
				return;
			}
			if ("list".equalsIgnoreCase(args[0])) { //ignore args
				Collection<ScorePlayerTeam> teamCollection = scoreboard.getTeams(); 
				if (teamCollection.size() <= 1) { sendChat(sender, "No teams to list"); }
				else {
					for (ScorePlayerTeam teamToList : (Collection<ScorePlayerTeam>)scoreboard.getTeams()) {
						if (teamToList.getName().equals(dataTeamKey)) { continue; }
						sendChat(sender, "Team name: " + teamToList.getName());
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
					//sendErrorChat(sender, "You already belong to a team. Leave or disband before creating a new one.")
					throw new WrongUsageException("You already belong to a team. Leave or disband before creating a new one.", new Object[0]);
				}
				String newTeamName;
				try { newTeamName = args[1]; }
				catch (ArrayIndexOutOfBoundsException e) {
					/*sendErrorChat(sender, "Missing team name.");
					sendErrorChat(sender, "Correct syntax: /myteam create <team> [player1] [player2] ...");*/
					throw new WrongUsageException("Missing team name. ", "/myteam create <team> [player1] [player2]");
				}
				if (newTeamName == dataTeamKey) {
					sendErrorChat(sender, "Invalid team name. Please choose a different name");
					throw new NumberInvalidException();
				}
				try { team = scoreboard.createTeam(newTeamName); }
				catch (IllegalArgumentException e) {
					sendChat(sender, e.getMessage());
					throw new NumberInvalidException();
				}
				sendChat(sender, "Created new team " + newTeamName);
				team.setPrefix("\u00A7r\u00A7r");
				scoreboard.addPlayerToTeam(sender.getCommandSenderEntity().getName(), newTeamName);
				updatePrefix(server, dataTeam, team);
				if (args.length > 2) {
					for (EntityPlayer playerToAdd : parsePlayerList(sender, Arrays.asList(args).subList(2, args.length))) {
						if (playerToAdd != player) {
							scoreboard.addPlayerToTeam(playerToAdd.getCommandSenderEntity().getName(), newTeamName);
							sendChat(sender, "Added player " + playerToAdd.getCommandSenderEntity().getName() + " to your team");
						}
					}
				}
				return;
			}
			if (team == null) {
				throw new NoTeamException();
			}
			String teamName = team.getName();
			if ("add".equalsIgnoreCase(args[0])) { //myteam add <playerlist>
				if (args.length > 1) {
					for (EntityPlayer playerToAdd : parsePlayerList(sender, Arrays.asList(args).subList(1, args.length))) {
						if (playerToAdd != player) {
							scoreboard.addPlayerToTeam(playerToAdd.getCommandSenderEntity().getName(), teamName);
							sendChat(sender, "Added player " + playerToAdd.getCommandSenderEntity().getName() + " to your team");
						}
					}
				}
				else {
					sendErrorChat(sender, "Missing player to add");
					sendErrorChat(sender, "Correct syntax: /myteam add <player1> [player2] ...");
					throw new NumberInvalidException();
				}
				return;
			}
			if ("remove".equalsIgnoreCase(args[0])) { //myteam remove <playerlist>
				if (args.length > 1) {
					for (EntityPlayer playerToRemove : parsePlayerList(sender, Arrays.asList(args).subList(1, args.length))) {
						String name = playerToRemove.getName();
						if (playerToRemove.isOnSameTeam(player)) {
							if (playerToRemove != player) {
								scoreboard.removePlayerFromTeam(name, team);
								sendChat(sender, "Removed player " + name + " from your team");
							}
							//sendErrorChat(sender, "Cannot remove yourself."); //not sure about this yet
						}
						else {
							sendErrorChat(sender, "Cannot remove. Player " + playerToRemove + " is not on your team.");
						}
					}
				}
				else {
					sendErrorChat(sender, "Missing player to remove");
					sendErrorChat(sender, "Correct syntax: /myteam remove <player1> [player2] ...");
					throw new NumberInvalidException();
				}
				return;
			}
			if ("leave".equalsIgnoreCase(args[0])) { //ignore args
				sendChat(sender, "Removed you from team " + team.getName());
				scoreboard.removePlayerFromTeams(player.getCommandSenderEntity().getName());
				return;
			}
			if ("disband".equalsIgnoreCase(args[0])) { //ignore args
				sendChat(sender, "Disbanded team " + team.getName());
				scoreboard.removeTeam(team);
				return;
			}
			if ("color".equalsIgnoreCase(args[0])) { //myteam color <colorname>
				if (args.length > 1) {
					if (colorMap.containsKey(args[1]))
					{
						String colorCode = colorMap.get(args[1]);
						sendChat(sender, "Set team color to " + colorDisplayMap.get(colorCode));

						setTeamColor(team, colorCode);
						updatePrefix(server, dataTeam, team);
					} else {
						sendErrorChat(sender, "Unknown color " + args[1]);
						sendChat(sender, "Acceptable colors: " + String.join(", ",colorDisplayMap.values()));
						/*sendChat(sender,
								"Acceptable colors: \u00A70black, \u00A71dark_blue, \u00A72dark_green, \u00A73dark_aqua, \u00A74dark_red, " +
								"\u00A75dark_purple, \u00A76Gold, \u00A77gray, \u00A78dark_gray, \u00A79blue, \u00A7agreen, \u00A7baqua, " +
								"\u00A7cred, \u00A7dlight_purple, \u00A7eyellow, \u00A7fwhite, \u00A7oitalic, \u00A7lbold");*/
						throw new NumberInvalidException();
					}
				} else {
					String colorCode = getTeamColor(team);
					if (colorDisplayMap.containsKey(colorCode)) {
						String displayName = colorDisplayMap.get(colorCode);
						sendChat(sender, "Team color: " + displayName);
					} else {
						sendErrorChat(sender, "Could not parse team color");
						sendChat(server, "Error processing command. Team name " + teamName);
					}
				}
				return;
			}
			sendErrorChat(sender, "Unknown argument \"" + args[0] + "\"");
			throw new NumberInvalidException();
		}
		catch (NoTeamException e) {
			sendErrorChat(sender, "You do not belong to a team.");
			sendErrorChat(sender, "Make a team with /myteam create.");
		}
		/*catch (PlayerNotFoundException e) {
			sendErrorChat(sender, "Could not find player " + e.getMessage());
		}*/
	}

	private void updatePrefix(MinecraftServer server, ScorePlayerTeam dataTeam, ScorePlayerTeam team) {
		String teamName = team.getName();

		String prefix = dataTeam.getPrefix();
		if (prefix == null) { prefix = ""; }
		String suffix = dataTeam.getSuffix();
		if (suffix == null) { suffix = ""; }
		String teamColor = getTeamColor(team);
		sendChat(server, "Got prefix format " + prefix);
		prefix = prefix.replaceAll("<t>", teamName).replaceAll("<c>", teamColor);
		suffix = suffix.replaceAll("<t>", teamName).replaceAll("<c>", teamColor);
		for (Map.Entry<String, String> colorPair : colorMap.entrySet()) {
			String key = colorPair.getKey();
			String value = colorPair.getValue();
			prefix = prefix.replaceAll("<" + key + ">" , value);
			suffix = suffix.replaceAll("<" + key + ">" , value);
		}
		sendChat(server, "Formatted to " + teamColor + "\u00A7r" + prefix);
		sendChat(server, "Got suffix format " + suffix);
		sendChat(server, "Formatted to " + suffix);
		team.setPrefix(teamColor + "\u00A7r" + prefix);
		team.setSuffix(suffix);
	}
	private String getTeamColor(ScorePlayerTeam team) {
		return team.getPrefix().substring(0,2);
	}
	private void setTeamColor(ScorePlayerTeam team, String newTeamColor) {
		String prefix = team.getPrefix();
		String prefixSplit;
		try {
			prefixSplit = prefix.substring(4);
		} catch (NullPointerException e) {
			prefix = "\u00A7r\u00A7r";
			prefixSplit = "";
		} catch (StringIndexOutOfBoundsException e) {
			prefixSplit = "";
		}
		team.setPrefix(newTeamColor + prefixSplit);
	}
	private ScorePlayerTeam getDataTeam(Scoreboard scoreboard) {
		ScorePlayerTeam dataTeam = scoreboard.getTeam(dataTeamKey);
		if (dataTeam == null) {
			scoreboard.createTeam(dataTeamKey);
			dataTeam = scoreboard.getTeam(dataTeamKey);
		}
		if (dataTeam.getPrefix() == null) {
			dataTeam.setPrefix("<c>");
		}
		if (dataTeam.getSuffix() == null) {
			dataTeam.setSuffix("\u00A7r");
		}
		return dataTeam;
	}

	private void sendErrorChat(ICommandSender sender, String line) {
		ITextComponent component = new TextComponentString(line);
		Style textStyle = new Style();
		textStyle.setColor(TextFormatting.RED);
		component.setStyle(textStyle);
		sender.sendMessage(component);
	}
	private void sendChat(ICommandSender sender, String line) {
		ITextComponent component = new TextComponentString(line);
		sender.sendMessage(component);
	}
	private <T extends Iterable<String>> void sendChat(ICommandSender sender, T lines) {
		for (String line : lines) {
			sendChat(sender, line);
		}
	}
	private <T extends Iterable<String>> Set<EntityPlayer> parsePlayerList(ICommandSender sender, T playerNames) throws CommandException {
		World world = sender.getServer().getEntityWorld();
		Set<EntityPlayer> players = new HashSet<EntityPlayer>();
		for (String playerName : playerNames) {
			EntityPlayer player = world.getPlayerEntityByName(playerName);
			if (player == null) {
				//handle with FML selectors
				players.addAll(SelectorHandlerManager.matchEntities(sender, playerName, EntityPlayer.class));
			} else {
				players.add(player);
			}
		}
		return players;
	}
	private class NoTeamException extends Exception {
		private static final long serialVersionUID = 1L;
		private NoTeamException() {
			super();
		}
	}

	@Override
	public boolean checkPermission(MinecraftServer server, ICommandSender sender) {
		return (sender instanceof EntityPlayer);
	}

	@Override
	public List<String> getTabCompletions(MinecraftServer server, ICommandSender sender, String[] args, BlockPos targetPos) {
		String partial = args[args.length-1];
		
		return possibleCompletions(server, sender, args).stream()
		                                                .filter(c -> c.startsWith(partial))
		                                                .collect(Collectors.toList());
	}
	
	public List<String> possibleCompletions(MinecraftServer server, ICommandSender sender, String[] args) {
		if (args.length < 2) {
			//base commands
			return Arrays.asList("add", "disband", "leave", "color", "create", "list", "help");
		}
		if (args.length == 2 && args[0].equalsIgnoreCase("color")) {
			//return color list
			return new ArrayList<>(colorMap.keySet());
		}
		boolean create = args[0].equalsIgnoreCase("create");
		boolean add = args[0].equalsIgnoreCase("add");
		if (args.length == 2 && add || args.length == 3 && create) {
			if (sender.canUseCommand(1, "@")) {
				//return players plus player selectors
				List<String> options = new LinkedList<>(Arrays.asList(server.getOnlinePlayerNames()));				
				for (String sel : SelectorHandlerManager.selectorHandlers.keySet()) {
					options.add(sel);
				}
				return options;
			} else {
				//return all player names
				return Arrays.asList(server.getOnlinePlayerNames());
			}
		}
		if (args.length > 2 && add || args.length > 3 && create) {
			//return all unentered player names
			List<String> newNames = new LinkedList<>(Arrays.asList(server.getOnlinePlayerNames()));
			for (int i = create ? 2 : 1; i < args.length-1; i++) {
				newNames.remove(args[i]);
			}
			return newNames;
		}
		return Collections.<String>emptyList();
	}

	@Override
	public boolean isUsernameIndex(String[] args, int i) {
		/*//supports selectors for third argument of create or second argument of add/remove
		if (i == 1 && args.length == 2 && (args[0].equalsIgnoreCase("add") || args[0].equalsIgnoreCase("remove"))
		 || i == 2 && args.length == 3 && args[0].equalsIgnoreCase("create")) {
			return !(args[i].startsWith("@e") || args[i].startsWith("@s")); //disallows entity selection
		}*/
		return false;
	}

	@Override
	public int compareTo(ICommand o) {
		return getName().compareToIgnoreCase(o.getName());
	}
}
package com.hexane.teammod;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import net.minecraft.command.ICommand;
import net.minecraft.command.ICommandSender;
import net.minecraft.scoreboard.ScorePlayerTeam;
import net.minecraft.scoreboard.Scoreboard;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class TeamPrefixCommand implements ICommand
{
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

	public TeamPrefixCommand() {
	}


	@Override
	public String getName() {
		return "teamprefix";
	}

	@Override
	public String getUsage(ICommandSender icommandsender) {
		return "teamprefix [-s|--suffix] [prefix/suffix]";
	}

	@Override
	public List<String> getAliases() {
		return new ArrayList<String>();
	}

	@Override
	public void execute(MinecraftServer server, ICommandSender sender, String[] args) {
		World world = sender.getEntityWorld();
		Scoreboard scoreboard = world.getScoreboard();
		ScorePlayerTeam dataTeam = getDataTeam(scoreboard);
		Boolean bSuffix = false;

		if (args.length == 0) {
			String prefix = dataTeam.getPrefix().replaceAll(" ", "\\\\_");
			sendChat(sender, "Generic prefix form: " + prefix);
			return;
		}
		if (args[0].equals("help")) {
			sendChat(sender, this.getUsage(sender));
			sendChat(sender, new String[] {
					"Prefix/Suffix Syntax: ",
					"<c>: Replace with team color",
					"<t>: Replace with team name",
					"<colorname>: Replace with color colorname",
					"\\c: Replace with color symbol",
					"\\_: Replace with space"
			});
		}
		if (args[0].equals("-s") || args[0].equals("--suffix")) {
			bSuffix = true;
			args = (String[])Arrays.copyOfRange(args, 1, args.length);
		}
		if (args.length == 0) {
			String suffix = dataTeam.getSuffix().replaceAll(" ", "\\\\_");
			sendChat(sender, "Generic suffix form: " + suffix);
			return;
		}
		String input = "";
		for (String part : args) {
			input += part + " ";
		}
		input = input.replaceAll("\\\\c", "\u00A7");
		input = input.replaceAll("\\\\_", " ");
		for (Map.Entry<String, String> colorPair : colorMap.entrySet()) {
			String key = colorPair.getKey();
			String value = colorPair.getValue();
			input = input.replaceAll("<" + key + ">" , value);
		}
		input = input.substring(0, input.length() - 1);
		if (bSuffix) {
			dataTeam.setSuffix(input);
		} else {
			dataTeam.setPrefix(input);
		}

		sendChat(sender, "Set to " + input);
		for (ScorePlayerTeam team : (Collection<ScorePlayerTeam>)scoreboard.getTeams()) {
			if (team.getName().equals(dataTeamKey)) { continue; }
			String color = getTeamColor(team);
			String teamPrefix = input.replaceAll("<c>", color);
			teamPrefix = teamPrefix.replaceAll("<t>", team.getName());
			if (bSuffix) {
				team.setSuffix(teamPrefix);
			} else {
				team.setPrefix(color + "\u00A7r" + teamPrefix);
			}
			System.out.println("Changed team " + team.getName() + " to " + teamPrefix);
		}
	}
	private String getTeamColor(ScorePlayerTeam team) {
		return team.getPrefix().substring(0,2);
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

	@Override
	public boolean checkPermission(MinecraftServer server, ICommandSender sender) {
		//return sender.getCommandSenderName().equalsIgnoreCase("Rcon");
		return true;
	}
	public int getRequiredPermissionLevel() {
		return 3;
	}

	@Override
	public List<String> getTabCompletions(MinecraftServer server, ICommandSender sender, String[] args, BlockPos targetPos) {
		return null;
	}

	@Override
	public boolean isUsernameIndex(String[] astring, int i) {
		return false;
	}


	private void sendChat(ICommandSender sender, String line) {
		ITextComponent component = new TextComponentString(line);
		sender.sendMessage(component);
	}
	private void sendChat(ICommandSender sender, String[] lines) {
		for (String line : lines) {
			sendChat(sender, line);
		}
	}

	@Override
	public int compareTo(ICommand o) {
		return getName().compareToIgnoreCase(o.getName());
	}
}
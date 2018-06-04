package com.hexane.teammod;

import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventHandler;
import net.minecraftforge.fml.common.Mod.Instance;
import net.minecraftforge.fml.common.event.FMLServerStartingEvent;

@Mod(modid = TeamMod.ID, name = TeamMod.NAME, version = TeamMod.VERSION, acceptableRemoteVersions="*")
public class TeamMod {
	public final static String ID = "teammod";
	public final static String NAME = "Server team commands";
	public final static String VERSION = "2.0.0";
	
	@Instance(ID)
	public static TeamMod instance;
	
	@EventHandler
	public void serverLoad(FMLServerStartingEvent event) {
		event.registerServerCommand(new MyTeamCommand());
		event.registerServerCommand(new TeamPrefixCommand());
	}
}

package hexane.teammod;

import cpw.mods.fml.common.Mod;
import cpw.mods.fml.common.Mod.EventHandler;
import cpw.mods.fml.common.Mod.Instance;
import cpw.mods.fml.common.event.FMLServerStartingEvent;

@Mod(modid = TeamMod.ID, name = TeamMod.NAME, version = TeamMod.VERSION, acceptableRemoteVersions="*")
public class TeamMod
{
  public final static String ID = "TeamMod";
  public final static String NAME = "Server team commands";
  public final static String VERSION = "1.1.1";
  
  @Instance(ID)
  public static TeamMod instance;
  
  @EventHandler
  public void serverLoad(FMLServerStartingEvent event)
  {
    event.registerServerCommand(new MyTeamCommand());
    event.registerServerCommand(new TeamPrefixCommand());
  }
}

package me.jd1992.playermention.commands;

import me.jd1992.playermention.PlayerMention;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class PlayerMentionCommand implements CommandExecutor {
	
	private final PlayerMention PLUGIN;
	
	public PlayerMentionCommand (PlayerMention plugin) {
		
		this.PLUGIN = plugin;
	}
	
	@Override
	public boolean onCommand( CommandSender sender, Command command, String label, String[] args ) {
		
		if( !( sender.hasPermission( "playermention.admin" ) )
		    || !( sender instanceof Player ) ) { return true; }
		
		Player player = ( ( Player )sender );
		
		if( args.length == 1 ) {
			if( args[0].equalsIgnoreCase( "reload" ) ) {
				PLUGIN.reload( player );
			} else if( args[0].equalsIgnoreCase( "mute" ) ) {
				if( PLUGIN.getMuteList().contains( player ) ) {
					PLUGIN.getMuteList().remove( player );
					PLUGIN.sendConfigMessage( player, "messages.onSelfUnmute" );
				} else {
					PLUGIN.getMuteList().add( player );
					PLUGIN.sendConfigMessage( player, "messages.onSelfMute" );
				}
			}
		}
		return true;
	}
	
}

package me.jd1992.playermention.listener;

import me.jd1992.playermention.PlayerMention;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerChatTabCompleteEvent;

import java.util.regex.Pattern;

public class PlayerChatTabCompleteListener implements Listener {
	
	private final PlayerMention PLUGIN;
	
	public PlayerChatTabCompleteListener (PlayerMention plugin) {
		
		this.PLUGIN = plugin;
	}
	
	/**
	 * Sets the tab completions to the matching players if the message contains a @ symbol
	 * Example: @J or @j would be -> @JD1992 or other playernames which matches the given letters after the @
	 */
	@EventHandler
	public void onChatComplete( PlayerChatTabCompleteEvent event ) {
		
		// check if a mention is in the chatmessage
		if( !event.getChatMessage().contains( "@" ) ) { return; }
		
		// values for the several checks
		String regexCheckName = getRegexCheckName( event.getChatMessage() );
		double radius = PLUGIN.getConfig().getInt( "PLUGIN.radius" );
		
		event.getTabCompletions().clear();
		Player executor = event.getPlayer();
		
		// loop through all players and add matching players to tabcompletion
		for( Player p : Bukkit.getOnlinePlayers() ) {
			// not for same player or players in vanish or player on mutelist
			if( p == executor
			    || PLUGIN.isVanished( executor, p )
			    || PLUGIN.getMuteList().contains( p ) ) { continue; }
			// compare looped players name fits the tabcompletion request with regex
			if( Pattern.matches( regexCheckName.toLowerCase(), p.getName().toLowerCase() ) ) {
				// do chatradius check
				if( PLUGIN.getConfig().getBoolean( "PLUGIN.useChatRadius" )
				    && executor.getLocation().distance( p.getLocation() ) > radius ) { continue; }
				
				// add player to tabcompletion
				event.getTabCompletions().add( "@" + p.getName() );
			}
		}
	}
	
	/**
	 * Extract the name from message and prepare it for regex check
	 *
	 * @param message Message to extract the name from
	 *
	 * @return The extracted name with preparation for thr regex check
	 */
	private String getRegexCheckName( String message ) {
		
		return message.substring( message.lastIndexOf( '@' ) + 1 ) + ".*";
	}
	
}

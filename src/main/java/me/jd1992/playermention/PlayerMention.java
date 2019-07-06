package me.jd1992.playermention;

import me.jd1992.playermention.listener.PlayerChatTabCompleteListener;
import me.jd1992.playermention.commands.PlayerMentionCommand;
import me.jd1992.playermention.listener.AsyncPlayerChatListener;
import com.earth2me.essentials.Essentials;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.metadata.MetadataValue;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.*;
import java.util.logging.Level;

public final class PlayerMention extends JavaPlugin implements Listener {
	
	private FileConfiguration CONFIGURATION;
	private String            PREFIX_CONSOLE;
	private String            PREFIX_INGAME;
	
	private HashMap< Player, Long > delayList;
	private ArrayList< Player > muteList;
	
	private Essentials essentials;
	
	@Override
	public void onEnable() {
		
		init();
		this.getLogger().log(Level.INFO, PREFIX_CONSOLE + " Plugin wurde erfolgreich aktiviert.");
	}
	
	@Override
	public void onDisable() {
		
		this.getLogger().log(Level.INFO, PREFIX_CONSOLE + " Plugin wurde erfolgreich deaktiviert.");
	}
	
	/**
	 * Initialise all plugin related values
	 */
	private void init() {
		
		initConfig();
		PREFIX_CONSOLE = CONFIGURATION.getString("plugin.PREFIX_CONSOLE");
		PREFIX_INGAME = ChatColor.translateAlternateColorCodes('&', CONFIGURATION.getString("plugin.PREFIX_INGAME"));
		
		initCommands();
		initListener();
		
		delayList = new HashMap<>();
		muteList = new ArrayList<>();
		
		if( getServer().getPluginManager().getPlugin( "Essentials" ) != null ) {
			// Plugin is loaded
			essentials = ( Essentials )getServer().getPluginManager().getPlugin( "Essentials" );
		}
	}
	
	/**
	 * Load the CONFIGURATION and set the default values
	 */
	private void initConfig() {
		
		CONFIGURATION = this.getConfig();
		
		// permissions for commands
		CONFIGURATION.addDefault( "permission.admin", "playermention.admin" );
		
		// standard plugin options and values
		CONFIGURATION.addDefault( "plugin.PREFIX_CONSOLE", "[PlayerMention]" );
		CONFIGURATION.addDefault( "plugin.PREFIX_INGAME", "&6&o&PlayerMention&0>&r" );
		CONFIGURATION.addDefault( "plugin.useChatRadius", false );
		CONFIGURATION.addDefault( "plugin.radius", 100 );
		CONFIGURATION.addDefault( "plugin.useDelay", true );
		CONFIGURATION.addDefault( "plugin.messageDelayInSec", 10 );
		
		// mention affecting options and values
		CONFIGURATION.addDefault( "mention.color", "&3" );
		CONFIGURATION.addDefault( "mention.volume", 1 );
		CONFIGURATION.addDefault( "mention.pitch", 1 );
		CONFIGURATION.addDefault( "mention.sound", "ENTITY_EXPERIENCE_ORB_PICKUP" );
		
		// texts for plugin messages
		CONFIGURATION.addDefault( "messages.onDelay", "&4Du warst zu schnell, warte kurz bis du wieder jemanden markieren darfst." );
		CONFIGURATION.addDefault( "messages.onSelfMute", "&4Du hast dich selber gemutet und kannst somit nicht mehr markiert werden." );
		CONFIGURATION.addDefault( "messages.onSelfUnmute", "&4Du hast dich selber von der Muteliste entfernt und kannst wieder markiert werden." );
		
		CONFIGURATION.options().copyDefaults( true );
		this.saveConfig();
	}
	
	/**
	 * Register the commands
	 */
	private void initCommands() {
		
		this.getCommand( "playermention" ).setExecutor( new PlayerMentionCommand(this ));
	}
	
	/**
	 * Register the event listeners
	 */
	private void initListener() {
		
		this.getServer().getPluginManager().registerEvents(new AsyncPlayerChatListener(this ), this);
		this.getServer().getPluginManager().registerEvents(new PlayerChatTabCompleteListener(this ), this);
	}
	
	/**
	 * Reload all plugin related informations and send indicator messages
	 *
	 * @param sender The CommandSender which executed the reload
	 */
	public void reload( CommandSender sender ) {
		
		this.sendPluginMessage( sender, "&4Config wird neugeladen." );
		this.reloadConfig();
		this.sendPluginMessage( sender, "&4Config wurde erfolgreich neugeladen." );
	}
	
	/**
	 * Get a message from the plugin CONFIGURATION and call the method to send a plugin related message
	 *
	 * @param sender CommandSender or Player which the message is going to
	 * @param node Path to the message in the CONFIGURATION file
	 */
	public void sendConfigMessage( CommandSender sender, String node ) {
		
		sendPluginMessage( sender, this.getConfig().getString( node ) );
	}
	
	/**
	 * Send a message to the CommandSender(Player/Console) with the plugin message layout
	 *
	 * @param sender CommandSender or Player which the message is going to
	 * @param msg The message that will be send
	 */
	private void sendPluginMessage( CommandSender sender, String msg ) {
		
		String message;
		StringJoiner joiner = new StringJoiner( " " );
		if( sender instanceof Player ) {
			message = ChatColor.translateAlternateColorCodes( '&', msg );
			joiner.add(PREFIX_INGAME);
		} else {
			message = ChatColor.stripColor( msg );
			joiner.add(PREFIX_CONSOLE);
		}
		sender.sendMessage( joiner.add( message ).toString() );
	}
	
	/**
	 * Check if a player is vanished in general or to specific player
	 *
	 * @param playerWhoCanSee player to check if he can see the other player
	 * @param playerToCheck player to check if vanished
	 *
	 * @return true if player isn't visible toe the other player/players
	 */
	public boolean isVanished( Player playerWhoCanSee, Player playerToCheck ) {
		
		// Essentials vanish check
		if( essentials != null && essentials.getUser( playerToCheck.getUniqueId() ).isVanished() ) { return true; }
		
		// Vanilla vanish check
		if( !( playerWhoCanSee.canSee( playerToCheck ) ) ) { return true; }
		
		// Plugin vanish check (SuperVanish, PremiumVanish, VanishNoPacket and a few more vanish plugins)
		for( MetadataValue meta : playerToCheck.getMetadata( "vanished" ) ) {
			if( meta.asBoolean() ) { return true; }
		}
		
		return false;
	}
	
	/**
	 * Get the list with the Player which mentioned a player
	 *
	 * @return The list with the current Player delays
	 */
	public Map< Player, Long > getDelayList() {
		
		return delayList;
	}
	
	/**
	 * Get the list with the muted Player which cannot be mentioned
	 *
	 * @return The list with the current muted players
	 */
	public List< Player > getMuteList() {
		
		return muteList;
	}
	
}

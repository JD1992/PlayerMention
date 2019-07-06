package me.jd1992.playermention.listener;

import me.jd1992.playermention.PlayerMention;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;

import java.util.Set;

public class AsyncPlayerChatListener implements Listener {

    private final PlayerMention PLUGIN;
    private final String        mentionSymbol;
    private final String        mentionColor;


    public AsyncPlayerChatListener (PlayerMention plugin) {

        this.PLUGIN = plugin;
        this.mentionSymbol = "@";
        this.mentionColor = getMentionColor();

    }

    /**
     * Get the mention color out of the config and convert it to a valid colorcode
     *
     * @return The extracted and converted colorcode
     */
    private String getMentionColor () {

        String configValue = PLUGIN.getConfig().getString("mention.color");
        return ChatColor.translateAlternateColorCodes('&', configValue);
    }

    @EventHandler (priority = EventPriority.HIGHEST)
    public void onPlayerChatMention (AsyncPlayerChatEvent event) {

        if (event.getMessage().contains(mentionSymbol)) {
            Player sender = event.getPlayer();
            // Check if player mentioned a player before and if there is enough time between the mentions
            if (PLUGIN.getDelayList().containsKey(sender) && PLUGIN.getDelayList().get(sender) >= System.currentTimeMillis() &&
                ! sender.hasPermission(PLUGIN.getConfig().getString("permission.admin"))) {

                PLUGIN.sendConfigMessage(sender, "messages.onDelay");
                event.setCancelled(true);
                return;
            }

            String message = event.getMessage();
            // Get the extracted mentioned players name and check if its valid
            String playerName = extractPlayername(message);
            if (playerName.length() <= 1) {
                return;
            }

            // Clean the Playername from special characters at the end
            String cleanName = cleanPlayername(playerName);

            // Check if extracted and cleaned name is equal to a online player
            if (Bukkit.getServer().getPlayerExact(cleanName) == null) {
                return;
            }

            // Check if mentioned player is the sender
            // or mentioned player is vanished
            // or mentioned player is on mutelist
            Player mentionedPlayer = Bukkit.getServer().getPlayerExact(cleanName);
            if (sender == mentionedPlayer || PLUGIN.isVanished(sender, mentionedPlayer) || PLUGIN.getMuteList().contains(mentionedPlayer)) {
                return;
            }

            // Extract the message color if exists or set do default
            String mainColor = extractMessageColor(message);

            String        mention   = mentionSymbol + mentionedPlayer.getName() + mainColor;
            Set< Player > recipents = event.getRecipients();

            // do when chatradius check is enabled
            if (PLUGIN.getConfig().getBoolean("PLUGIN.useChatRadius")) {

                // check if the mentionedplayer ist outside the radius
                double radius = PLUGIN.getConfig().getInt("PLUGIN.radius");
                if (sender.getLocation().distance(mentionedPlayer.getLocation()) > radius) {
                    return;
                }

                // clear recipents and fill it with players inside the radius
                for (Player p : Bukkit.getOnlinePlayers()) {
                    if (sender.getLocation().distance(p.getLocation()) > radius) {
                        recipents.remove(p);
                    }
                }
                mention = mentionColor + mention;

            } else if (recipents.contains(mentionedPlayer)) {
                mention = mentionColor + mention;
            } else {
                return;
            }

            // play a sound for the mentionedplayer and set the changed message
            mentionedPlayer.playSound(mentionedPlayer.getLocation(), Sound.valueOf(PLUGIN.getConfig().getString("mention.sound")),
                                      ((float) PLUGIN.getConfig().getDouble("mention.volume")),
                                      ((float) PLUGIN.getConfig().getDouble("mention.pitch")));
            event.setMessage(message.replace(mentionSymbol + cleanName, mention));
            if (PLUGIN.getConfig().getBoolean("PLUGIN.useDelay")) {
                PLUGIN.getDelayList()
                      .put(sender, System.currentTimeMillis() + PLUGIN.getConfig().getInt("PLUGIN.messageDelayInSec") * 1000);
            }

        }
    }

    /**
     * Extract the mention from the message
     *
     * @param message Message to extract from
     *
     * @return The extracted mention with the playername
     */
    private String extractPlayername (String message) {

        String   msg      = message.substring(message.indexOf(mentionSymbol));
        String[] sentence = msg.split(" ");
        return sentence[0];
    }

    /**
     * Clean the name from trailing sepcial characters
     *
     * @param playerName Name wich should be cleaned
     *
     * @return The playername or the playername where the last character(special character) is cut off
     */
    private String cleanPlayername (String playerName) {

        String check = "[!\"#$%&'*+,-./:;<=>?^@`|~]";
        if (check.contains(playerName.substring(playerName.length() - 1))) {
            return playerName.substring(1, playerName.length() - 1);
        } else {
            return playerName.substring(1);
        }
    }

    /**
     * Extract the Chatcolor used in the message
     *
     * @param message Message that will be send
     *
     * @return The extracted colorcode from the message or the default value
     */
    private String extractMessageColor (String message) {

        if (message.charAt(0) == '§') {
            return message.substring(0, 2);
        }
        return "§r";
    }

}

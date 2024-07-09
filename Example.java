import me.croabeast.lib.advancement.AdvancementInfo;
import org.bukkit.ChatColor;
import org.bukkit.advancement.Advancement;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerAdvancementDoneEvent;
import org.bukkit.inventory.ItemStack;

public class Example implements Listener {

    private String colorize(String input) {
        return ChatColor.translateAlternateColorCodes('&', input);
    }

    private void sendToPlayer(Player player, String... lines) {
        for (String line : lines)
            if (line != null) player.sendMessage(colorize(line));
    }

    @EventHandler
    private void onPlayerAdvancement(PlayerAdvancementDoneEvent event) {
        Advancement adv = event.getAdvancement();
        Player player = event.getPlayer();

        AdvancementInfo info = AdvancementInfo.from(adv);

        String title = info.getTitle(), description = info.getDescription();
        ItemStack item = info.getItem();

        boolean announce = info.doesAnnounceToChat(), hidden = info.isHidden();

        sendToPlayer(player,
                "&7",
                "&7 &6&nAdvancement: &e" + title,
                "&8  • &7Description: &f" + description,
                (item != null ? "&8  • &7Item Icon: &f" + item : null),
                "&8  • &7Is Hidden?: &f" + hidden,
                "&8  • &7Can Announce to Chat?: &f" + announce,
                "&7"
        );
    }
}
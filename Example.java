import me.croabeast.advancementinfo.AdvancementInfo;
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

        AdvancementInfo info = new AdvancementInfo(adv);

        String title = info.getTitle();
        if (title == null) title = "";

        String description = info.getDescription();
        ItemStack item = info.getItem();

        String parent = info.getParent();

        boolean announce = info.announceToChat(),
                isHidden = info.isHidden();

        sendToPlayer(player,
                "&7",
                "&7 &6&nAdvancement: &e" + title,
                "&8  • &7Description: &f" + description,
                "&8  • &7Parent: &f" + parent,
                (item != null ? "&8  • &7Item Icon: &f" + item : null),
                "&8  • &7Is Hidden?: &f" + isHidden,
                "&8  • &7Can Announce to Chat?: &f" + announce,
                "&7"
        );
    }
}
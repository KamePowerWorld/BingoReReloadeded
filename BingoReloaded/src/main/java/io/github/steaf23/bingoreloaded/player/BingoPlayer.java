package io.github.steaf23.bingoreloaded.player;

import io.github.steaf23.bingoreloaded.BingoReloaded;
import io.github.steaf23.bingoreloaded.cards.TaskCard;
import io.github.steaf23.bingoreloaded.data.BingoMessage;
import io.github.steaf23.bingoreloaded.data.BingoStatType;
import io.github.steaf23.bingoreloaded.gameloop.BingoSession;
import io.github.steaf23.bingoreloaded.gameloop.phase.BingoGame;
import io.github.steaf23.bingoreloaded.gui.inventory.EffectOptionFlags;
import io.github.steaf23.bingoreloaded.item.ItemCooldownManager;
import io.github.steaf23.bingoreloaded.player.team.BingoTeam;
import io.github.steaf23.bingoreloaded.settings.PlayerKit;
import io.github.steaf23.bingoreloaded.tasks.GameTask;
import io.github.steaf23.playerdisplay.inventory.item.ItemTemplate;
import io.github.steaf23.playerdisplay.util.ConsoleMessenger;
import io.github.steaf23.playerdisplay.util.PDCHelper;
import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.OfflinePlayer;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.MapMeta;
import org.bukkit.map.MapRenderer;
import org.bukkit.map.MapView;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

/**
 * This class describes a player in a single bingo session.
 * This class will still exist if the player leaves the game/world.
 * This instance will be removed when the session gets destroyed.
 */
public class BingoPlayer implements BingoParticipant
{
    public final String playerName;
    private final BingoSession session;
    private final UUID playerId;
    private final Component displayName;
    private final ItemCooldownManager itemCooldowns;

    private final int POTION_DURATION = 1728000; // 24 Hours

    public BingoPlayer(Player player, BingoSession session) {
        this.playerId = player.getUniqueId();
        this.session = session;
        this.playerName = player.getName();
        this.displayName = player.displayName();
        this.itemCooldowns = new ItemCooldownManager();
    }

    /**
     * @return the player that belongs to this BingoPlayer, if this player is in a session world, otherwise returns null
     */
    public Optional<Player> sessionPlayer() {
        if (!offline().isOnline())
            return Optional.empty();

        Player player = Bukkit.getPlayer(playerId);
        if (player == null || !session.hasPlayer(player)) {
            return Optional.empty();
        }
        return Optional.of(player);
    }

    @Override
    public String getName() {
        return playerName;
    }

    @Override
    public UUID getId() {
        return playerId;
    }

    @Override
    public Component getDisplayName() {
        return displayName;
    }

    public OfflinePlayer offline() {
        return Bukkit.getOfflinePlayer(playerId);
    }

    @Override
    public void giveKit(PlayerKit kit) {
        if (sessionPlayer().isEmpty())
            return;

        Player player = sessionPlayer().get();

        var items = kit.getItems(getTeam().getColor());
        player.closeInventory();
        Inventory inv = player.getInventory();
        inv.clear();
        items.forEach(i ->
        {
            var meta = i.stack().getItemMeta();

            // Show enchantments except on the wand
            if (!PlayerKit.WAND_ITEM.isCompareKeyEqual(i.stack())) {
                meta.removeItemFlags(ItemFlag.values());
            }
            var pdc = meta.getPersistentDataContainer();
            pdc.set(PDCHelper.createKey("kit.kit_item"), PersistentDataType.BOOLEAN, true);

            i.stack().setItemMeta(meta);
            inv.setItem(i.slot(), i.stack());
        });
    }

    @Override
    public void giveBingoCard(int cardSlot, @Nullable MapRenderer mapRenderer) {
        if (sessionPlayer().isEmpty())
            return;

        Player player = sessionPlayer().get();

        ItemTemplate cardItem = mapRenderer == null ? PlayerKit.CARD_ITEM : PlayerKit.CARD_ITEM_RENDERABLE;

        BingoReloaded.scheduleTask(task -> {
            for (ItemStack itemStack : player.getInventory()) {
                if (cardItem.isCompareKeyEqual(itemStack)) {
                    player.getInventory().remove(itemStack);
                    break;
                }
            }
            ItemStack existingItem = player.getInventory().getItem(cardSlot);
            ItemStack card;
            if (mapRenderer == null) {
                card = cardItem.buildItem();
            } else {
                ItemTemplate map = cardItem.copy().addMetaModifier(meta -> {
                    if (meta instanceof MapMeta mapMeta) {
                        MapView view = Bukkit.createMap(player.getWorld());
                        for (MapRenderer renderer : new ArrayList<>(view.getRenderers())) {
                            view.removeRenderer(renderer);
                        }

                        view.addRenderer(mapRenderer);
                        mapMeta.setMapView(view);
                        return mapMeta;
                    }
                    ConsoleMessenger.bug("No valid map item found to render texture to.", this);
                    return meta;
                });
                card = map.buildItem();
            }

            player.getInventory().setItem(cardSlot, card);
            if (existingItem != null && !existingItem.getType().isAir()) {
                Map<Integer, ItemStack> leftOver = player.getInventory().addItem(existingItem);
                for (ItemStack stack : leftOver.values()) {
                    player.getWorld().dropItem(player.getLocation(), stack);
                }
            }
        });
    }

    @Override
    public void giveEffects(EnumSet<EffectOptionFlags> effects, int gracePeriod) {
        if (sessionPlayer().isEmpty())
            return;

        takeEffects(false);
        Player player = sessionPlayer().get();

        BingoReloaded.scheduleTask(task -> {
            if (effects.contains(EffectOptionFlags.NIGHT_VISION))
                player.addPotionEffect(new PotionEffect(PotionEffectType.NIGHT_VISION, POTION_DURATION, 1, false, false));
            if (effects.contains(EffectOptionFlags.WATER_BREATHING))
                player.addPotionEffect(new PotionEffect(PotionEffectType.WATER_BREATHING, POTION_DURATION, 1, false, false));
            if (effects.contains(EffectOptionFlags.FIRE_RESISTANCE))
                player.addPotionEffect(new PotionEffect(PotionEffectType.FIRE_RESISTANCE, POTION_DURATION, 1, false, false));
            if (effects.contains(EffectOptionFlags.SPEED))
                player.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, POTION_DURATION, 1, false, false));
            player.addPotionEffect(new PotionEffect(PotionEffectType.SATURATION, 2, 100, false, false));
            player.addPotionEffect(new PotionEffect(PotionEffectType.REGENERATION, 2, 100, false, false));
            player.addPotionEffect(new PotionEffect(PotionEffectType.RESISTANCE, BingoReloaded.ONE_SECOND * gracePeriod, 100, false, false));
        });
    }

    /**
     * @param force ignore if the player is actually in the world playing the game at this moment.
     */
    @Override
    public void takeEffects(boolean force) {
        if (force) {
            Player p = Bukkit.getPlayer(playerId);
            if (p != null) {
                p.clearActivePotionEffects();
            }
        } else {
            if (sessionPlayer().isEmpty())
                return;

            sessionPlayer().get().clearActivePotionEffects();
        }
    }

    public void showDeathMatchTask(GameTask task) {
        if (sessionPlayer().isEmpty())
            return;

        String itemKey = task.material.isBlock() ? "block" : "item";
        itemKey += ".minecraft." + task.material.getKey().getKey();
        sessionPlayer().get()
                .sendMessage(BingoMessage.DEATHMATCH_ITEM.asPhrase(Component.translatable(itemKey))
                        .color(NamedTextColor.GOLD));
    }

    @Override
    public void showCard(GameTask deathMatchTask) {
        BingoTeam playerTeam = getTeam();
        if (playerTeam == null) {
            ConsoleMessenger.bug("Invalid team for player " + playerName + "!", this);
            return;
        }
        Optional<TaskCard> card = playerTeam.getCard();

        sessionPlayer().ifPresent(player -> {
            if (deathMatchTask != null) {
                showDeathMatchTask(deathMatchTask);
                return;
            }

            // if the player is actually participating, show it
            card.ifPresentOrElse(c -> c.showInventory(player), () -> BingoMessage.NO_PLAYER_CARD.sendToAudience(player));
        });
    }

    @Override
    public boolean alwaysActive() {
        return false;
    }

    public void useGoUpWand(ItemStack wand, double wandCooldownSeconds, int downDistance, int upDistance, int platformLifetimeSeconds) {
        if (sessionPlayer().isEmpty())
            return;

        Player player = sessionPlayer().get();
        if (!PlayerKit.WAND_ITEM.isCompareKeyEqual(wand))
            return;

        if (!itemCooldowns.isCooldownOver(wand.getType())) {
            double timeLeft = itemCooldowns.getTimeLeft(wand.getType()) / 1000.0;
            player.sendMessage(BingoMessage.COOLDOWN.asPhrase(Component.text(String.format("%.2f", timeLeft)))
                    .color(NamedTextColor.RED));
            return;
        }

        BingoReloaded.scheduleTask(task -> {
            itemCooldowns.addCooldown(wand.getType(), (int) (wandCooldownSeconds * 1000));

            double distance;
            double fallDistance;
            // Use the wand
            if (sessionPlayer().isPresent() && sessionPlayer().get().isSneaking()) {
                distance = -downDistance;
                fallDistance = 0.0;
            } else {
                distance = upDistance;
                fallDistance = 2.0;
            }

            Location teleportLocation = player.getLocation();
            Location platformLocation = teleportLocation.clone();
            teleportLocation.setY(teleportLocation.getY() + distance + fallDistance);
            platformLocation.setY(platformLocation.getY() + distance);

            BingoGame.spawnPlatform(platformLocation, 1, true);
            BingoReloaded.scheduleTask(laterTask -> {
                BingoGame.removePlatform(platformLocation, 1);
            }, (long) Math.max(0, platformLifetimeSeconds) * BingoReloaded.ONE_SECOND);

            player.teleport(teleportLocation);
            player.playSound(player, Sound.ENTITY_SHULKER_TELEPORT, 0.8f, 1.0f);
            player.addPotionEffect(new PotionEffect(PotionEffectType.RESISTANCE, BingoReloaded.ONE_SECOND * 10, 100, false, false));

            BingoReloaded.incrementPlayerStat(player, BingoStatType.WAND_USES);
        });
    }

    @Override
    public BingoSession getSession() {
        return session;
    }

    @Override
    public @Nullable BingoTeam getTeam() {
        return session.teamManager
                .getActiveTeams()
                .getTeams()
                .stream()
                .filter(team -> team.hasMember(this.playerId))
                .findAny()
                .orElse(null);
    }

    @Override
    public void setTeam(@Nullable BingoTeam team) {
        if(team == null) {
            session.teamManager
                    .getActiveTeams()
                    .getTeams()
                    .forEach(t -> t.removeMember(this));
        } else {
            team.addMember(this);
        }
    }

    @Override
    public String toString() {
        return playerName;
    }

    @Override
    public @NotNull Audience audience() {
        return sessionPlayer().isPresent() ? sessionPlayer().get() : Audience.empty();
    }
}

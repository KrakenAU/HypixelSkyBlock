package net.swofty.gui.inventory.inventories;

import lombok.SneakyThrows;
import net.minestom.server.event.inventory.InventoryCloseEvent;
import net.minestom.server.event.inventory.InventoryPreClickEvent;
import net.minestom.server.inventory.Inventory;
import net.minestom.server.inventory.InventoryType;
import net.minestom.server.item.ItemStack;
import net.minestom.server.item.Material;
import net.swofty.data.DataHandler;
import net.swofty.data.datapoints.DatapointDouble;
import net.swofty.enchantment.EnchantmentType;
import net.swofty.gui.inventory.ItemStackCreator;
import net.swofty.gui.inventory.SkyBlockInventoryGUI;
import net.swofty.gui.inventory.item.GUIClickableItem;
import net.swofty.gui.inventory.item.GUIItem;
import net.swofty.item.ItemLore;
import net.swofty.item.Rarity;
import net.swofty.item.ReforgeType;
import net.swofty.item.SkyBlockItem;
import net.swofty.item.impl.Reforgable;
import net.swofty.item.updater.PlayerItemUpdater;
import net.swofty.user.SkyBlockPlayer;
import net.swofty.utility.MathUtility;
import net.swofty.utility.StringUtility;

import java.util.HashMap;
import java.util.Map;

public class GUIReforge extends SkyBlockInventoryGUI {
    private static final Map<Rarity, Integer> COST_MAP = new HashMap<>();
    private final int[] borderSlots = {
            0, 8, 9, 17, 18, 26, 27, 35, 36, 44
    };

    static {
        COST_MAP.put(Rarity.COMMON, 250);
        COST_MAP.put(Rarity.UNCOMMON, 500);
        COST_MAP.put(Rarity.RARE, 1000);
        COST_MAP.put(Rarity.EPIC, 2500);
        COST_MAP.put(Rarity.LEGENDARY, 5000);
        COST_MAP.put(Rarity.MYTHIC, 10000);
    }

    public GUIReforge() {
        super("Reforge Item", InventoryType.CHEST_5_ROW);
    }

    @Override
    public void onOpen(InventoryGUIOpenEvent e) {
        fill(Material.BLACK_STAINED_GLASS_PANE, "");
        set(GUIClickableItem.getCloseItem(40));

        updateFromItem(null);
    }

    public void updateFromItem(SkyBlockItem item) {
        border(ItemStackCreator.createNamedItemStack(Material.RED_STAINED_GLASS_PANE));

        if (item == null) {
            set(new GUIClickableItem() {
                @Override
                public void run(InventoryPreClickEvent e, SkyBlockPlayer player) {
                    ItemStack stack = e.getCursorItem();

                    if (stack.getDisplayName() == null) return;

                    SkyBlockItem item = new SkyBlockItem(stack);
                    updateFromItem(item);
                }

                @Override
                public boolean canPickup() {
                    return true;
                }

                @Override
                public int getSlot() {
                    return 13;
                }

                @Override
                public ItemStack.Builder getItem(SkyBlockPlayer player) {
                    return ItemStack.builder(Material.AIR);
                }
            });
            set(new GUIClickableItem() {
                @Override
                public void run(InventoryPreClickEvent e, SkyBlockPlayer player) {
                    player.sendMessage("§cPlace an item in the empty slot above to reforge it!");
                }

                @Override
                public int getSlot() {
                    return 22;
                }

                @Override
                public ItemStack.Builder getItem(SkyBlockPlayer player) {
                    return ItemStackCreator.getStack(
                            "§eReforge Item", Material.ANVIL, (short) 0, 1,
                            "§7Place an item above to reforge it!",
                            "§7Reforging items adds a random",
                            "§7modifier to the item that grants stat",
                            "§7boosts."
                    );
                }
            });
            updateItemStacks(getInventory(), getPlayer());
            return;
        }

        set(new GUIClickableItem() {
            @Override
            public void run(InventoryPreClickEvent e, SkyBlockPlayer player) {
                ItemStack stack = e.getCursorItem();

                if (stack.getDisplayName() == null) {
                    updateFromItem(null);
                    return;
                }

                SkyBlockItem item = new SkyBlockItem(stack);
                updateFromItem(item);
            }

            @Override
            public boolean canPickup() {
                return true;
            }

            @Override
            public int getSlot() {
                return 13;
            }

            @Override
            public ItemStack.Builder getItem(SkyBlockPlayer player) {
                return PlayerItemUpdater.playerUpdate(player, null, item.getItemStack());
            }
        });

        if (item.getAmount() > 1 ||
                item.getGenericInstance() == null ||
                !(item.getGenericInstance() instanceof Reforgable)) {
            set(new GUIItem() {
                @Override
                public int getSlot() {
                    return 22;
                }

                @Override
                public ItemStack.Builder getItem(SkyBlockPlayer player) {
                    return ItemStackCreator.getStack(
                            "§cError!", Material.BARRIER, (short) 0, 1,
                            "§7You cannot reforge this item!"
                    );
                }
            });
            updateItemStacks(getInventory(), getPlayer());
            return;
        }

        border(ItemStackCreator.createNamedItemStack(Material.LIME_STAINED_GLASS_PANE));
        set(new GUIClickableItem() {
            @Override
            public void run(InventoryPreClickEvent e, SkyBlockPlayer player) {
                DatapointDouble coins = player.getDataHandler().get(DataHandler.Data.COINS, DatapointDouble.class);
                int cost = COST_MAP.get(item.getAttributeHandler().getRarity());

                if (coins.getValue() - cost < 0) {
                    player.sendMessage("§cYou don't have enough Coins!");
                    return;
                }

                coins.setValue(coins.getValue() - cost);

                ReforgeType reforgeType = ((Reforgable) item.getGenericInstance()).getReforgeType();
                ReforgeType.Reforge reforge = reforgeType.getReforges().get(MathUtility.random(0, reforgeType.getReforges().size() - 1));
                String oldPrefix = item.getAttributeHandler().getReforge() == null ? "" :
                        " " + item.getAttributeHandler().getReforge().prefix();

                item.getAttributeHandler().setReforge(reforge);

                String itemName = ItemLore.getBaseName(item.getItemStack());

                player.sendMessage("§aYou reforged your" +
                        item.getAttributeHandler().getRarity().getColor() + oldPrefix + " " + itemName + "§a into a " +
                        item.getAttributeHandler().getRarity().getColor() + reforge.prefix() + " " + itemName + "§a!");

                updateFromItem(item);
            }

            @Override
            public int getSlot() {
                return 22;
            }

            @Override
            public ItemStack.Builder getItem(SkyBlockPlayer player) {
                return ItemStackCreator.getStack(
                        "§eReforge Item", Material.ANVIL, (short) 0, 1,
                        "§7Reforges the above item, giving it a",
                        "§7random stat modifier that boosts its",
                        "§7stats.",
                        "§2 ",
                        "§7Cost",
                        "§6" + COST_MAP.get(item.getAttributeHandler().getRarity()) + " Coins",
                        "§2 ",
                        "§eClick to reforge!"
                );
            }
        });
        updateItemStacks(getInventory(), getPlayer());
    }

    @Override
    public boolean allowHotkeying() {
        return false;
    }

    @Override
    public void onClose(InventoryCloseEvent e, CloseReason reason) {
        e.getPlayer().getInventory().addItemStack(e.getInventory().getItemStack(13));
    }

    @Override
    public void suddenlyQuit(Inventory inventory, SkyBlockPlayer player) {
        player.getInventory().addItemStack(inventory.getItemStack(13));
    }

    @Override
    public void border(ItemStack.Builder stack) {
        for (int i : borderSlots) {
            set(i, stack);
        }
        updateItemStacks(getInventory(), getPlayer());
    }

    @Override
    public void onBottomClick(InventoryPreClickEvent e) {

    }
}

package net.swofty.item;

import lombok.Getter;
import lombok.SneakyThrows;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextDecoration;
import net.minestom.server.item.ItemStack;
import net.swofty.item.impl.Enchantable;
import net.swofty.item.impl.Reforgable;
import net.swofty.user.SkyBlockPlayer;
import net.swofty.utility.StringUtility;
import net.swofty.item.attribute.AttributeHandler;
import net.swofty.item.impl.CustomSkyBlockAbility;
import net.swofty.item.impl.CustomSkyBlockItem;
import net.swofty.user.statistics.ItemStatistic;
import net.swofty.user.statistics.ItemStatistics;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.stream.Collectors;

public class ItemLore {
    private final ArrayList<Component> loreLines = new ArrayList<>();

    @Getter
    private ItemStack stack;

    public ItemLore(ItemStack stack) {
        this.stack = stack;
    }

    @SneakyThrows
    public void updateLore(@Nullable SkyBlockPlayer player) {
        SkyBlockItem item = new SkyBlockItem(stack);
        AttributeHandler handler = item.getAttributeHandler();

        Rarity rarity = handler.getRarity();
        String type = handler.getItemType();
        boolean recombobulated = handler.isRecombobulated();
        ItemStatistics statistics = handler.getStatistics();
        Class<?> clazz = item.clazz;

        if (recombobulated) {
            rarity = rarity.upgrade();
        }

        String displayName = StringUtility.toNormalCase(type);

        if (clazz != null) {
            // Handle Item Statistics
            if (handler.isMiningTool()) {
                addLoreLine("§8Breaking Power " + handler.getBreakingPower());
                addLoreLine(null);
            }
            boolean damage = addPossiblePropertyInt(ItemStatistic.DAMAGE, statistics.get(ItemStatistic.DAMAGE),
                    handler.getReforge(), rarity);
            boolean defence = addPossiblePropertyInt(ItemStatistic.DEFENSE, statistics.get(ItemStatistic.DEFENSE),
                    handler.getReforge(), rarity);
            boolean health = addPossiblePropertyInt(ItemStatistic.HEALTH, statistics.get(ItemStatistic.HEALTH),
                    handler.getReforge(), rarity);
            boolean strength = addPossiblePropertyInt(ItemStatistic.STRENGTH, statistics.get(ItemStatistic.STRENGTH),
                    handler.getReforge(), rarity);
            boolean intelligence = addPossiblePropertyInt(ItemStatistic.INTELLIGENCE, statistics.get(ItemStatistic.INTELLIGENCE),
                    handler.getReforge(), rarity);
            boolean miningSpeed = addPossiblePropertyInt(ItemStatistic.MINING_SPEED, statistics.get(ItemStatistic.MINING_SPEED),
                    handler.getReforge(), rarity);
            boolean speed = addPossiblePropertyInt(ItemStatistic.SPEED, statistics.get(ItemStatistic.SPEED),
                    handler.getReforge(), rarity);
            if (damage || defence || health || strength || intelligence || miningSpeed || speed) addLoreLine(null);

            // Handle Item Enchantments
            if (clazz.newInstance() instanceof Enchantable) {
                long enchantmentCount = handler.getEnchantments().toList().size();
                if (enchantmentCount < 4) {
                    handler.getEnchantments().forEach((enchantment) -> {
                        addLoreLine("§9" + enchantment.type().getName() + " " + StringUtility.getAsRomanNumeral(enchantment.level()));
                        StringUtility.splitByWordAndLength("§7" + enchantment.type().getDescription(enchantment.level()), 34, " ")
                                .forEach(this::addLoreLine);
                    });
                } else {
                    String enchantmentNames = handler.getEnchantments().toList().stream()
                            .map(enchantment1 -> "§9" + enchantment1.type().getName() + " " + StringUtility.getAsRomanNumeral(enchantment1.level()))
                            .collect(Collectors.joining(", "));
                    StringUtility.splitByWordAndLength(enchantmentNames, 34, ",").forEach(this::addLoreLine);
                }

                if (enchantmentCount != 0) {
                    addLoreLine(null);
                }
            }

            // Handle Custom Item Lore
            CustomSkyBlockItem skyBlockItem = (CustomSkyBlockItem) item.getGenericInstance();
            if (skyBlockItem.getLore(player, item) != null) {
                skyBlockItem.getLore(player, item).forEach(line -> addLoreLine("§7" + line));
                addLoreLine(null);
            }

            // Handle Custom Item Ability
            if (item.getGenericInstance() instanceof CustomSkyBlockAbility ability) {
                addLoreLine("§6Ability: " + ability.getAbilityName() + "  §e§l" +
                        ability.getAbilityActivation().getDisplay());
                for (String line : StringUtility.splitByWordAndLength(ability.getAbilityDescription(), 34, "\\s"))
                    addLoreLine("§7" + line);
                if (ability.getManaCost() > 0)
                    addLoreLine("§8Mana Cost: §3" + ability.getManaCost());
                if (ability.getAbilityCooldownTicks() > 20)
                    addLoreLine("§8Cooldown: §a" + StringUtility.commaify((double) ability.getAbilityCooldownTicks() / 20) + "s");

                addLoreLine(null);
            }

            if (item.getGenericInstance() instanceof Reforgable) {
                addLoreLine("§8This item can be reforged!");

                if (handler.getReforge() != null) {
                    displayName = handler.getReforge().prefix() + " " + displayName;
                }
            }
        }

        if (recombobulated) {
            addLoreLine(rarity.getColor() + "&kL " + rarity.getDisplay() + " &kL");
        } else {
            addLoreLine(rarity.getDisplay());
        }

        displayName = rarity.getColor() + displayName;
        this.stack = stack.withLore(loreLines)
                .withDisplayName(Component.text(displayName)
                        .decoration(TextDecoration.ITALIC, false));
    }

    public static String getBaseName(ItemStack stack) {
        return StringUtility.toNormalCase(new SkyBlockItem(stack).getAttributeHandler().getItemType());
    }

    private boolean addPossiblePropertyInt(ItemStatistic statistic,
                                           int overallValue,
                                           ReforgeType.Reforge reforge,
                                           Rarity rarity) {
        int reforgeValue = 0;
        if (reforge != null) {
            overallValue += reforge.getBonusCalculation(statistic, rarity.ordinal() + 1);
            reforgeValue = reforge.getBonusCalculation(statistic, rarity.ordinal() + 1);
        }

        if (overallValue == 0) return false;

        String color = statistic.isRed() ? "&c" : "&a";
        String line = "§7" + StringUtility.toNormalCase(statistic.getDisplayName()) + ": " +
                color + statistic.getPrefix() + overallValue + statistic.getSuffix() + " ";

        if (reforgeValue != 0) {
            line += "§9(" + (reforgeValue > 0 ? "+" : "") + reforgeValue + ")";
        }

        addLoreLine(line);
        return true;
    }

    private void addLoreLine(String line) {
        if (line == null) {
            loreLines.add(Component.empty());
            return;
        }
        line = line.replace("&", "§");
        loreLines.add(Component.text("§r" + line)
                .decorations(Collections.singleton(TextDecoration.ITALIC), false));
    }
}

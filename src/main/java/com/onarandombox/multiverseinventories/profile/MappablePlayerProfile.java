package com.onarandombox.multiverseinventories.profile;

import com.onarandombox.multiverseinventories.data.DataStrings;
import com.onarandombox.multiverseinventories.item.ItemWrapper;
import com.onarandombox.multiverseinventories.item.SimpleItemWrapper;
import com.onarandombox.multiverseinventories.util.MVILog;
import com.onarandombox.multiverseinventories.util.MinecraftTools;
import com.onarandombox.multiverseinventories.util.PlayerStats;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.inventory.ItemStack;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * @author dumptruckman
 */
public class MappablePlayerProfile implements PlayerProfile {

    private ItemStack[] inventoryContents = new ItemStack[PlayerStats.INVENTORY_SIZE];
    private ItemStack[] armorContents = new ItemStack[PlayerStats.ARMOR_SIZE];
    private Integer health = PlayerStats.HEALTH;
    private Float exp = PlayerStats.EXPERIENCE;
    private Integer totalExperience = PlayerStats.TOTAL_EXPERIENCE;
    private Integer level = PlayerStats.LEVEL;
    private Integer foodLevel = PlayerStats.FOOD_LEVEL;
    private Float exhaustion = PlayerStats.EXHAUSTION;
    private Float saturation = PlayerStats.SATURATION;

    private OfflinePlayer player;

    public MappablePlayerProfile(OfflinePlayer player) {
        this.player = player;
    }

    public MappablePlayerProfile(String playerName, Map<String, Object> playerData) {
        this(Bukkit.getOfflinePlayer(playerName));
        if (playerData.containsKey("stats")) {
            this.parsePlayerStats(playerData.get("stats").toString().split(DataStrings.GENERAL_DELIMITER));
        }
        if (playerData.containsKey("inventoryContents")) {
            this.parsePlayerInventory(playerData.get("inventoryContents").toString().split(DataStrings.ITEM_DELIMITER));
        }
        if (playerData.containsKey("armorContents")) {
            this.parsePlayerArmor(playerData.get("armorContents").toString().split(DataStrings.ITEM_DELIMITER));
        }
    }

    /**
     * @param statsArray Parses these values to fill out this Profile.
     */
    protected void parsePlayerStats(String[] statsArray) {
        for (String stat : statsArray) {
            try {
                String[] statValues = DataStrings.splitEntry(stat);
                if (statValues[0].equals(DataStrings.PLAYER_HEALTH)) {
                    this.setHealth(Integer.valueOf(statValues[1]));
                } else if (statValues[0].equals(DataStrings.PLAYER_EXPERIENCE)) {
                    this.setExp(Float.valueOf(statValues[1]));
                } else if (statValues[0].equals(DataStrings.PLAYER_TOTAL_EXPERIENCE)) {
                    this.setTotalExperience(Integer.valueOf(statValues[1]));
                } else if (statValues[0].equals(DataStrings.PLAYER_LEVEL)) {
                    this.setLevel(Integer.valueOf(statValues[1]));
                } else if (statValues[0].equals(DataStrings.PLAYER_FOOD_LEVEL)) {
                    this.setFoodLevel(Integer.valueOf(statValues[1]));
                } else if (statValues[0].equals(DataStrings.PLAYER_EXHAUSTION)) {
                    this.setExhaustion(Float.valueOf(statValues[1]));
                } else if (statValues[0].equals(DataStrings.PLAYER_SATURATION)) {
                    this.setSaturation(Float.valueOf(statValues[1]));
                }
            } catch (Exception e) {
                if (!stat.isEmpty()) {
                    MVILog.debug("Could not parse stat: '" + stat + "'");
                    MVILog.debug(e.getMessage());
                }
            }
        }
    }

    /**
     * @param inventoryArray these values to fill out this Profile.
     */
    protected void parsePlayerInventory(String[] inventoryArray) {
        ItemStack[] invContents = MinecraftTools.fillWithAir(new ItemStack[PlayerStats.INVENTORY_SIZE]);
        for (String itemString : inventoryArray) {
            String[] itemValues = DataStrings.splitEntry(itemString);
            try {
                MVILog.debug("Unwrapping item from string: " + itemString);
                ItemWrapper itemWrapper = new SimpleItemWrapper(itemValues[1]);
                MVILog.debug("Unwrapped item: " + itemWrapper.getItem().toString());
                invContents[Integer.valueOf(itemValues[0])] = itemWrapper.getItem();
            } catch (Exception e) {
                if (!itemString.isEmpty()) {
                    MVILog.debug("Could not parse item string: " + itemString);
                    MVILog.debug(e.getMessage());
                }
            }
        }
        this.setInventoryContents(invContents);
    }

    /**
     * @param armorArray Parses these values to fill out this Profile.
     */
    protected void parsePlayerArmor(String[] armorArray) {
        ItemStack[] armContents = MinecraftTools.fillWithAir(new ItemStack[PlayerStats.ARMOR_SIZE]);
        for (String itemString : armorArray) {
            String[] itemValues = DataStrings.splitEntry(itemString);
            try {
                armContents[Integer.valueOf(itemValues[0])] = new SimpleItemWrapper(itemValues[1]).getItem();
            } catch (Exception e) {
                if (!itemString.isEmpty()) {
                    MVILog.debug("Could not parse armor string: " + itemString);
                    MVILog.debug(e.getMessage());
                }
            }
        }
        this.setArmorContents(armContents);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Map<String, Object> serialize() {
        Map<String, Object> playerData = new LinkedHashMap<String, Object>();
        StringBuilder builder = new StringBuilder();

        builder.append(DataStrings.createEntry(DataStrings.PLAYER_HEALTH, this.getHealth()));
        builder.append(DataStrings.GENERAL_DELIMITER);
        builder.append(DataStrings.createEntry(DataStrings.PLAYER_EXPERIENCE, this.getExp()));
        builder.append(DataStrings.GENERAL_DELIMITER);
        builder.append(DataStrings.createEntry(DataStrings.PLAYER_TOTAL_EXPERIENCE, this.getTotalExperience()));
        builder.append(DataStrings.GENERAL_DELIMITER);
        builder.append(DataStrings.createEntry(DataStrings.PLAYER_LEVEL, this.getLevel()));
        builder.append(DataStrings.GENERAL_DELIMITER);
        builder.append(DataStrings.createEntry(DataStrings.PLAYER_FOOD_LEVEL, this.getFoodLevel()));
        builder.append(DataStrings.GENERAL_DELIMITER);
        builder.append(DataStrings.createEntry(DataStrings.PLAYER_EXHAUSTION, this.getExhaustion()));
        builder.append(DataStrings.GENERAL_DELIMITER);
        builder.append(DataStrings.createEntry(DataStrings.PLAYER_SATURATION, this.getSaturation()));

        playerData.put("stats", builder.toString());

        builder = new StringBuilder();
        boolean first = true;
        for (Integer i = 0; i < PlayerStats.INVENTORY_SIZE; i++) {
            if (this.getInventoryContents()[i] != null && this.getInventoryContents()[i].getTypeId() != 0) {
                if (first) {
                    first = false;
                } else {
                    builder.append(DataStrings.ITEM_DELIMITER);
                }
                builder.append(DataStrings.createEntry(i, new SimpleItemWrapper(this.getInventoryContents()[i]).toString()));
            }
        }
        playerData.put("inventoryContents", builder.toString());

        builder = new StringBuilder();
        first = true;
        for (Integer i = 0; i < PlayerStats.ARMOR_SIZE; i++) {
            if (this.getArmorContents()[i] != null && this.getArmorContents()[i].getTypeId() != 0) {
                if (first) {
                    first = false;
                } else {
                    builder.append(DataStrings.ITEM_DELIMITER);
                }
                builder.append(DataStrings.createEntry(i, new SimpleItemWrapper(this.getArmorContents()[i]).toString()));
            }
        }
        playerData.put("armorContents", builder.toString());
        return playerData;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public OfflinePlayer getPlayer() {
        return this.player;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ItemStack[] getInventoryContents() {
        return this.inventoryContents;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setInventoryContents(ItemStack[] inventoryContents) {
        this.inventoryContents = inventoryContents;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ItemStack[] getArmorContents() {
        return this.armorContents;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setArmorContents(ItemStack[] armorContents) {
        this.armorContents = armorContents;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Integer getHealth() {
        return this.health;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setHealth(Integer health) {
        this.health = health;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Float getExp() {
        return this.exp;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setExp(Float exp) {
        this.exp = exp;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Integer getTotalExperience() {
        return this.totalExperience;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setTotalExperience(Integer totalExperience) {
        this.totalExperience = totalExperience;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Integer getLevel() {
        return this.level;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setLevel(Integer level) {
        this.level = level;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Integer getFoodLevel() {
        return this.foodLevel;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setFoodLevel(Integer foodLevel) {
        this.foodLevel = foodLevel;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Float getExhaustion() {
        return this.exhaustion;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setExhaustion(Float exhaustion) {
        this.exhaustion = exhaustion;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Float getSaturation() {
        return this.saturation;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setSaturation(Float saturation) {
        this.saturation = saturation;
    }
}

package io.github.some_example_name;

import com.badlogic.gdx.graphics.Color;
import io.github.some_example_name.character.Player;

public class Upgrade {
    public enum Type {
        ATTACK_RANGE("攻击距离", "攻击范围 +20%", Color.ORANGE),
        ATTACK_SPEED("攻击速度", "攻击冷却 -15%", Color.RED),
        PIERCE("弹射穿透", "投射物可穿透 +1 个敌人", Color.YELLOW),
        EXTRA_TARGET("多重射击", "同时攻击目标 +1", Color.PURPLE),
        MOVE_SPEED("移动速度", "移动速度 +15%", Color.GREEN),
        ATTACK_DAMAGE("攻击力", "投射物伤害 +25%", Color.BLUE),
        CRIT_RATE("暴击率", "暴击概率 +8%", Color.PINK),
        CRIT_DAMAGE("暴击伤害", "暴击伤害倍数 +30%", Color.MAGENTA),
        MAX_HP("最大血量", "血量上限 +20", Color.FIREBRICK),
        HP_REGEN("生命回复", "每秒恢复 +1 生命", Color.LIME),
        DAMAGE_REDUCTION("坚韧", "受到伤害 -10%", Color.GRAY),
        KILL_HEAL("击杀回血", "击杀敌人恢复 +3 生命", Color.CORAL),
        LIFE_STEAL("吸血", "造成伤害的 5% 转化为生命", Color.MAROON),
        DEATH_EXPLOSION("死亡爆炸", "敌人死亡时造成范围伤害 +10", Color.GOLD),
        SLOW_AURA("减速光环", "附近敌人移速降低 20%", Color.CYAN),
        THORNS("反伤", "反弹受到伤害的 15%", Color.TEAL),
        REVIVE("复活", "死亡时以 30% 血量复活一次", Color.VIOLET),
        EXP_GAIN("智慧", "经验获取 +20%", Color.SKY),
        MAGNET_RANGE("引力", "拾取范围 +30%", Color.SALMON),
        KNOCKBACK("击退", "击退效果 +25%", Color.OLIVE),
        SPLIT_COUNT("分裂", "子弹击中后分裂 +1", Color.CHARTREUSE),
        COIN_DROP_RATE("财运", "金币掉落几率 +15%", Color.GOLD),
        COIN_MULTIPLIER("贪婪", "获得金币数量 +25%", Color.YELLOW);

        private final String name;
        private final String description;
        private final Color color;

        Type(String name, String description, Color color) {
            this.name = name;
            this.description = description;
            this.color = color;
        }

        public String getName() {
            return name;
        }

        public String getDescription() {
            return description;
        }

        public Color getColor() {
            return color;
        }
    }

    private Type type;

    public Upgrade(Type type) {
        this.type = type;
    }

    public void apply(Player player) {
        switch (type) {
            case ATTACK_RANGE:
                player.setAttackRange(player.getAttackRange() * 1.2f);
                break;
            case ATTACK_SPEED:
                player.setAttackCooldown(player.getAttackCooldown() * 0.85f);
                break;
            case PIERCE:
                player.setPierceCount(player.getPierceCount() + 1);
                break;
            case EXTRA_TARGET:
                player.setExtraTargets(player.getExtraTargets() + 1);
                break;
            case MOVE_SPEED:
                player.setSpeed(player.getSpeed() * 1.15f);
                break;
            case ATTACK_DAMAGE:
                player.setAttackDamage((int) (player.getAttackDamage() * 1.25f));
                break;
            case CRIT_RATE:
                player.setCritRate(player.getCritRate() + 0.08f);
                break;
            case CRIT_DAMAGE:
                player.setCritDamage(player.getCritDamage() + 0.3f);
                break;
            case MAX_HP:
                player.setMaxHp(player.getMaxHp() + 20);
                player.setHp(player.getHp() + 20);
                break;
            case HP_REGEN:
                player.setHpRegen(player.getHpRegen() + 1);
                break;
            case DAMAGE_REDUCTION:
                player.setDamageReduction(Math.min(player.getDamageReduction() + 0.1f, 0.8f));
                break;
            case KILL_HEAL:
                player.setKillHeal(player.getKillHeal() + 3);
                break;
            case LIFE_STEAL:
                player.setLifeSteal(player.getLifeSteal() + 0.05f);
                break;
            case DEATH_EXPLOSION:
                player.setDeathExplosionDamage(player.getDeathExplosionDamage() + 10);
                break;
            case SLOW_AURA:
                player.setSlowAuraRange(player.getSlowAuraRange() + 60);
                break;
            case THORNS:
                player.setThornsRatio(player.getThornsRatio() + 0.15f);
                break;
            case REVIVE:
                player.setReviveCount(player.getReviveCount() + 1);
                break;
            case EXP_GAIN:
                player.setExpMultiplier(player.getExpMultiplier() + 0.2f);
                break;
            case MAGNET_RANGE:
                player.setMagnetRange(player.getMagnetRange() + 45);
                break;
            case KNOCKBACK:
                player.setKnockbackForce(player.getKnockbackForce() + 0.25f);
                break;
            case SPLIT_COUNT:
                player.setSplitCount(player.getSplitCount() + 1);
                break;
            case COIN_DROP_RATE:
                player.setCoinDropRate(player.getCoinDropRate() + 0.15f);
                break;
            case COIN_MULTIPLIER:
                player.setCoinMultiplier(player.getCoinMultiplier() + 0.25f);
                break;
        }
    }

    public Type getType() {
        return type;
    }

    public String getName() {
        return type.getName();
    }

    public String getDescription() {
        return type.getDescription();
    }

    public Color getColor() {
        return type.getColor();
    }
}

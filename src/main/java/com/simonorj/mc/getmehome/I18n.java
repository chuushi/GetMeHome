package com.simonorj.mc.getmehome;

/**
 * Not an interface
 */
public enum I18n {
    LANGUAGE_NAME("language.name"),
    LANGUAGE_REGION("language.region"),
    LANGUAGE_CODE("language.code"),
    LANGUAGE_TRANSLATED_BY("language.translatedBy"),

    CMD_GENERIC_PLAYER_NOT_FOUND("commands.generic.player.notFound"),
    CMD_GENERIC_HOME_FAILURE("commands.generic.home.failure"),
    CMD_GENERIC_HOME_OTHER_FAILURE("commands.generic.home.other.failure"),

    CMD_META_HEADING("commands.meta.heading"),
    CMD_META_TRANSLATED("commands.meta.translated"),
    CMD_HOME_SUCCESS("commands.home.success"),
    CMD_HOME_UNABLE("commands.home.unable"),
    CMD_HOME_WARMUP("commands.home.warmup"),
    CMD_HOME_WARMUP_STILL("commands.home.warmup.still"),
    CMD_HOME_WARMUP_CANCEL("commands.home.warmup.cancel"),
    CMD_HOME_COOLDOWN("commands.home.cooldown"),
    CMD_HOME_OTHER_SUCCESS("commands.home.other.success"),
    CMD_SETHOME_BAD_LOCATION("commands.sethome.badLocation"),
    CMD_SETHOME_REACHED_LIMIT("commands.sethome.reachedLimit"),
    CMD_SETHOME_NEW("commands.sethome.new"),
    CMD_SETHOME_NEW_OTHER("commands.sethome.new.other"),
    CMD_SETHOME_RELOCATE("commands.sethome.relocate"),
    CMD_SETHOME_RELOCATE_OTHER("commands.sethome.relocate.other"),
    CMD_SETDEFAULTHOME("commands.setdefaulthome"),
    CMD_DELHOME("commands.delhome"),
    CMD_DELHOME_OTHER("commands.delhome.other"),
    CMD_LISTHOMES_SELF("commands.listhomes.self"),
    CMD_LISTHOMES_OTHER("commands.listhomes.other"),
    CMD_LISTHOMES_OTHER_OFFLINE("commands.listhomes.other.offline"),
    CMD_LISTHOMES_NONE("commands.listhomes.none");

    private final String node;

    I18n(String node) {
        this.node = node;
    }

    @Override
    public String toString() {
        return node;
    }
}

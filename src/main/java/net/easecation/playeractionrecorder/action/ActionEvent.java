package net.easecation.playeractionrecorder.action;

public enum ActionEvent {

    AUTH_LOGIN(ActionCategory.AUTH),
    AUTH_NUKKIT_JOIN(ActionCategory.AUTH),
    AUTH_NUKKIT_TRANSFER(ActionCategory.AUTH),
    AUTH_NUKKIT_QUIT(ActionCategory.AUTH),
    AUTH_LOGOUT(ActionCategory.AUTH),
    AUTH_KICK(ActionCategory.AUTH),

    GAMING_LOBBY_JOIN(ActionCategory.GAMING),
    GAMING_LOBBY_LEAVE(ActionCategory.GAMING),
    GAMING_STAGE_JOIN(ActionCategory.GAMING),
    GAMING_STAGE_QUIT(ActionCategory.GAMING),
    GAMING_STAGE_DISCONNECT(ActionCategory.GAMING),
    GAMING_STAGE_RECONNECT(ActionCategory.GAMING),

    WORLD_SWITCH(ActionCategory.WORLD),
    WORLD_TELEPORT(ActionCategory.WORLD),
    WORLD_ENTITY_ATTACK(ActionCategory.WORLD),
    WORLD_ENTITY_INTERACT(ActionCategory.WORLD),

    CHAT_COMMAND(ActionCategory.CHAT),
    CHAT_CHAT(ActionCategory.CHAT),
    CHAT_PARTY(ActionCategory.CHAT),
    CHAT_BUGLET(ActionCategory.CHAT),

    INTERACTION_NPC(ActionCategory.INTERACTION),
    INTERACTION_CHESTUI_SHOW(ActionCategory.INTERACTION),
    INTERACTION_CHESTUI_CLOSE(ActionCategory.INTERACTION),
    INTERACTION_GUIITEM_USE(ActionCategory.INTERACTION),
    INTERACTION_FORMUI_SHOW(ActionCategory.INTERACTION),
    INTERACTION_FORMUI_RESPONSE(ActionCategory.INTERACTION),
    INTERACTION_FORMUI_CLICK(ActionCategory.INTERACTION),
    INTERACTION_FORMUI_CLOSE(ActionCategory.INTERACTION),

    PARTY_INVITE(ActionCategory.PARTY),
    PARTY_REQUEST(ActionCategory.PARTY),
    PARTY_JOIN(ActionCategory.PARTY),
    PARTY_LEAVE(ActionCategory.PARTY),
    PARTY_KICK(ActionCategory.PARTY),
    ;

    ActionCategory category;

    ActionEvent(ActionCategory category) {
        this.category = category;
    }

    public ActionCategory getCategory() {
        return category;
    }
}

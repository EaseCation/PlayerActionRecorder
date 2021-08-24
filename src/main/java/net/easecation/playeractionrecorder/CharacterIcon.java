package net.easecation.playeractionrecorder;

import java.util.regex.Pattern;

public enum CharacterIcon {
    GREEN_A_BUTTON('\uE000'),
    RED_B_BUTTON('\uE001'),
    BLUE_X_BUTTON('\uE002'),
    YELLOW_Y_BUTTON('\uE003'),
    WHITE_LB_BUTTON('\uE004'),
    WHITE_RB_BUTTON('\uE005'),
    WHITE_LT_ROUND_BUTTON('\uE006'),
    WHITE_RT_ROUND_BUTTON('\uE007'),
    WHITE_WINDOWED_BUTTON('\uE008'),
    WHITE_MENU_BUTTON('\uE009'),
    WHITE_LS_BUTTON('\uE00A'),
    WHITE_RS_BUTTON('\uE00B'),
    WHITE_UP_CROSS_BUTTON('\uE00C'),
    WHITE_LEFT_CROSS_BUTTON('\uE00D'),
    WHITE_DOWN_CROSS_BUTTON('\uE00E'),
    WHITE_RIGHT_CROSS_BUTTON('\uE00F'),

    GREEN_A_BUTTON_2('\uE010'),
    RED_B_BUTTON_2('\uE011'),
    BLUE_X_BUTTON_2('\uE012'),
    YELLOW_Y_BUTTON_2('\uE013'),

    BLUE_CLOSE_BUTTON('\uE020'),
    RED_CIRCLE_BUTTON('\uE021'),
    PINK_SQUARE_BUTTON('\uE022'),
    CYAN_TRIANGLE_BUTTON('\uE023'),
    WHITE_L1_BUTTON('\uE024'),
    WHITE_R1_BUTTON('\uE025'),
    WHITE_L2_BUTTON('\uE026'),
    WHITE_R2_BUTTON('\uE027'),
    GRAY_PREVIOUS_BUTTON('\uE028'),
    GRAY_NEXT_BUTTON('\uE029'),
    WHITE_L3_BUTTON('\uE02A'),
    WHITE_R3_BUTTON('\uE02B'),
    WHITE_UP_OPPOSITE_BUTTON('\uE02C'),
    WHITE_LEFT_OPPOSITE_BUTTON('\uE02D'),
    WHITE_DOWN_OPPOSITE_BUTTON('\uE02E'),
    WHITE_RIGHT_OPPOSITE_BUTTON('\uE02F'),

    WHITE_A_BUTTON('\uE040'),
    WHITE_B_BUTTON('\uE041'),
    WHITE_X_BUTTON('\uE042'),
    WHITE_Y_BUTTON('\uE043'),
    WHITE_L_BUTTON('\uE044'),
    WHITE_R_BUTTON('\uE045'),
    WHITE_ZL_BUTTON('\uE046'),
    WHITE_ZR_BUTTON('\uE047'),
    WHITE_MINUS_BUTTON('\uE048'),
    WHITE_PLUS_BUTTON('\uE049'),
    WHITE_L_BUTTON_MIDDLE('\uE04A'),
    WHITE_R_BUTTON_MIDDLE('\uE04B'),
    WHITE_UP_BUTTON('\uE04C'),
    WHITE_LEFT_BUTTON('\uE04D'),
    WHITE_DOWN_BUTTON('\uE04E'),
    WHITE_RIGHT_BUTTON('\uE04F'),

    LEFT_MOUSE_BUTTON('\uE060'),
    RIGHT_MOUSE_BUTTON('\uE061'),
    MIDDLE_MOUSE_BUTTON('\uE062'),
    MIDDLE_BUTTON('\uE063'),

    FORWARD_BUTTON('\uE080'),
    BACKWARD_BUTTON('\uE081'),
    LEFT_BUTTON('\uE082'),
    RIGHT_BUTTON('\uE083'),
    SNEAKING_BUTTON('\uE084'),
    SNEAKING_BUTTON_PRESSED('\uE085'),
    UP_BUTTON('\uE086'),
    DOWN_BUTTON('\uE087'),

    CRAFTABLE_BUTTON_ON('\uE0A0'),
    CRAFTABLE_BUTTON_OFF('\uE0A1'),

    WHITE_LG_BUTTON('\uE0C0'),
    WHITE_RG_BUTTON('\uE0C1'),
    GRAY_MENU_BUTTON('\uE0C2'),
    WHITE_LS_BUTTON_2('\uE0C3'),
    WHITE_RS_BUTTON_2('\uE0C4'),
    WHITE_L_BUTTON_BIG('\uE0C5'),
    WHITE_L_BUTTON_BIG_LEFT_RIGHT('\uE0C6'),
    WHITE_L_BUTTON_BIG_UP_DOWN('\uE0C7'),
    WHITE_R_BUTTON_BIG('\uE0C8'),
    WHITE_R_BUTTON_BIG_LEFT_RIGHT('\uE0C9'),
    WHITE_R_BUTTON_BIG_UP_DOWN('\uE0CA'),
    WHITE_LT_BUTTON('\uE0CB'),
    WHITE_RT_BUTTON('\uE0CC'),
    GRAY_WINDOWS_BUTTON('\uE0CD'),

    GRAY_0_BUTTON('\uE0E0'),
    GRAY_A_BUTTON('\uE0E1'),
    GRAY_B_BUTTON('\uE0E2'),
    WHITE_LG_BUTTON_2('\uE0E3'),
    WHITE_RG_BUTTON_2('\uE0E4'),
    WHITE_LS_BUTTON_3('\uE0E5'),
    WHITE_RS_BUTTON_3('\uE0E6'),
    WHITE_LT_ROUND_BUTTON_2('\uE0E7'),
    WHITE_RT_ROUND_BUTTON_2('\uE0E8'),
    GRAY_X_BUTTON('\uE0E9'),
    GRAY_Y_BUTTON('\uE0EA'),

    HUNGER_VALUE('\uE100'),
    ARMOR_VALUE('\uE101'),
    MINE_COIN('\uE102'),
    AGENT('\uE103'),
    READING('\uE104'),
    T_OVAL('\uE105'),
    ;

    public static final Pattern CLEAN_PATTERN;

    static {
        StringBuilder regex = new StringBuilder("[");

        for (CharacterIcon icon : values()) {
            regex.append(icon.getChar());
        }

        CLEAN_PATTERN = Pattern.compile(regex.append("]").toString());
    }

    private final char character;

    CharacterIcon(char character) {
        this.character = character;
    }

    public char getChar() {
        return this.character;
    }

    @Override
    public String toString() {
        return String.valueOf(this.character);
    }
}

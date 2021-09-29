package dev.cerus.maps.api;

import java.awt.Color;
import java.util.Arrays;
import java.util.Comparator;

public enum MapColor {

    TRANSPARENT_0(0, null),
    TRANSPARENT_1(1, null),
    TRANSPARENT_2(2, null),
    TRANSPARENT_3(3, null),

    GREEN_0(4, new Color(88, 124, 39)),
    GREEN_1(5, new Color(108, 151, 47)),
    GREEN_2(6, new Color(125, 176, 55)),
    GREEN_3(7, new Color(66, 93, 29)),

    BEIGE_0(8, new Color(172, 162, 114)),
    BEIGE_1(9, new Color(210, 199, 138)),
    BEIGE_2(10, new Color(244, 230, 161)),
    BEIGE_3(11, new Color(128, 122, 85)),

    GRAY_0(12, new Color(138, 138, 138)),
    GRAY_1(13, new Color(169, 169, 169)),
    GRAY_2(14, new Color(197, 197, 197)),
    GRAY_3(15, new Color(104, 104, 104)),

    RED_0(16, new Color(178, 0, 0)),
    RED_1(17, new Color(217, 0, 0)),
    RED_2(18, new Color(252, 0, 0)),
    RED_3(19, new Color(133, 0, 0)),

    INDIGO_0(20, new Color(111, 111, 178)),
    INDIGO_1(21, new Color(136, 136, 217)),
    INDIGO_2(22, new Color(158, 158, 252)),
    INDIGO_3(23, new Color(83, 83, 133)),

    DARK_GRAY_0(24, new Color(116, 116, 116)),
    DARK_GRAY_1(25, new Color(142, 142, 142)),
    DARK_GRAY_2(26, new Color(165, 165, 165)),
    DARK_GRAY_3(27, new Color(87, 87, 87)),

    DARK_GREEN_0(28, new Color(0, 86, 0)),
    DARK_GREEN_1(29, new Color(0, 105, 0)),
    DARK_GREEN_2(30, new Color(0, 123, 0)),
    DARK_GREEN_3(31, new Color(0, 64, 0)),

    WHITE_0(32, new Color(178, 178, 178)),
    WHITE_1(33, new Color(217, 217, 217)),
    WHITE_2(34, new Color(252, 252, 252)),
    WHITE_3(35, new Color(133, 133, 133)),

    BLUEISH_GRAY_0(36, new Color(114, 117, 127)),
    BLUEISH_GRAY_1(37, new Color(139, 142, 156)),
    BLUEISH_GRAY_2(38, new Color(162, 166, 182)),
    BLUEISH_GRAY_3(39, new Color(85, 87, 96)),

    BROWN_0(40, new Color(105, 75, 53)),
    BROWN_1(41, new Color(128, 93, 65)),
    BROWN_2(42, new Color(149, 108, 76)),
    BROWN_3(43, new Color(78, 56, 39)),

    DARK_GRAY2_0(44, new Color(78, 78, 78)),
    DARK_GRAY2_1(45, new Color(95, 95, 95)),
    DARK_GRAY2_2(46, new Color(111, 111, 111)),
    DARK_GRAY2_3(47, new Color(58, 58, 58)),

    BLUE_0(48, new Color(44, 44, 178)),
    BLUE_1(49, new Color(54, 54, 217)),
    BLUE_2(50, new Color(63, 63, 252)),
    BLUE_3(51, new Color(33, 33, 133)),

    BROWN2_0(52, new Color(99, 83, 49)),
    BROWN2_1(53, new Color(122, 101, 61)),
    BROWN2_2(54, new Color(141, 118, 71)),
    BROWN2_3(55, new Color(74, 62, 38)),

    WHITE2_0(56, new Color(178, 175, 170)),
    WHITE2_1(57, new Color(217, 214, 208)),
    WHITE2_2(58, new Color(252, 249, 242)),
    WHITE2_3(59, new Color(133, 131, 127)),

    DARK_ORANGE_0(60, new Color(150, 88, 36)),
    DARK_ORANGE_1(61, new Color(184, 108, 43)),
    DARK_ORANGE_2(62, new Color(213, 125, 50)),
    DARK_ORANGE_3(63, new Color(113, 66, 27)),

    MAGENTA_0(64, new Color(124, 52, 150)),
    MAGENTA_1(65, new Color(151, 64, 184)),
    MAGENTA_2(66, new Color(176, 75, 213)),
    MAGENTA_3(67, new Color(93, 39, 113)),

    LIGHT_BLUE_0(68, new Color(71, 107, 150)),
    LIGHT_BLUE_1(69, new Color(87, 130, 184)),
    LIGHT_BLUE_2(70, new Color(101, 151, 213)),
    LIGHT_BLUE_3(71, new Color(53, 80, 113)),

    YELLOW_0(72, new Color(159, 159, 36)),
    YELLOW_1(73, new Color(195, 195, 43)),
    YELLOW_2(74, new Color(226, 226, 50)),
    YELLOW_3(75, new Color(120, 120, 27)),

    LIME_0(76, new Color(88, 142, 17)),
    LIME_1(77, new Color(108, 174, 21)),
    LIME_2(78, new Color(125, 202, 25)),
    LIME_3(79, new Color(66, 107, 13)),

    PINK_0(80, new Color(168, 88, 115)),
    PINK_1(81, new Color(206, 108, 140)),
    PINK_2(82, new Color(239, 125, 163)),
    PINK_3(83, new Color(126, 66, 86)),

    LIGHT_BLACK_0(84, new Color(52, 52, 52)),
    LIGHT_BLACK_1(85, new Color(64, 64, 64)),
    LIGHT_BLACK_2(86, new Color(75, 75, 75)),
    LIGHT_BLACK_3(87, new Color(39, 39, 39)),

    GRAY3_0(88, new Color(107, 107, 107)),
    GRAY3_1(89, new Color(130, 130, 130)),
    GRAY3_2(90, new Color(151, 151, 151)),
    GRAY3_3(91, new Color(80, 80, 80)),

    CYAN_0(92, new Color(52, 88, 107)),
    CYAN_1(93, new Color(64, 108, 130)),
    CYAN_2(94, new Color(75, 125, 151)),
    CYAN_3(95, new Color(39, 66, 80)),

    VIOLET_0(96, new Color(88, 43, 124)),
    VIOLET_1(97, new Color(108, 53, 151)),
    VIOLET_2(98, new Color(125, 62, 176)),
    VIOLET_3(99, new Color(66, 33, 93)),

    BLUE2_0(100, new Color(36, 52, 124)),
    BLUE2_1(101, new Color(43, 64, 151)),
    BLUE2_2(102, new Color(50, 75, 176)),
    BLUE2_3(103, new Color(27, 39, 93)),

    DARK_BROWN_0(104, new Color(71, 52, 36)),
    DARK_BROWN_1(105, new Color(87, 64, 43)),
    DARK_BROWN_2(106, new Color(101, 75, 50)),
    DARK_BROWN_3(107, new Color(53, 39, 27)),

    GREEN2_0(108, new Color(71, 88, 36)),
    GREEN2_1(109, new Color(87, 108, 43)),
    GREEN2_2(110, new Color(101, 125, 50)),
    GREEN2_3(111, new Color(53, 66, 27)),

    DARK_RED_0(112, new Color(107, 36, 36)),
    DARK_RED_1(113, new Color(130, 43, 43)),
    DARK_RED_2(114, new Color(151, 50, 50)),
    DARK_RED_3(115, new Color(80, 27, 27)),

    BLACK_0(116, new Color(17, 17, 17)),
    BLACK_1(117, new Color(21, 21, 21)),
    BLACK_2(118, new Color(25, 25, 25)),
    BLACK_3(119, new Color(13, 13, 13)),

    GOLD_0(120, new Color(174, 166, 53)),
    GOLD_1(121, new Color(212, 203, 65)),
    GOLD_2(122, new Color(247, 235, 76)),
    GOLD_3(123, new Color(130, 125, 39)),

    LIGHT_CYAN_0(124, new Color(63, 152, 148)),
    LIGHT_CYAN_1(125, new Color(78, 186, 181)),
    LIGHT_CYAN_2(126, new Color(91, 216, 210)),
    LIGHT_CYAN_3(127, new Color(47, 114, 111)),

    LIGHT_BLUE2_0(128, new Color(51, 89, 178)),
    LIGHT_BLUE2_1(129, new Color(62, 109, 217)),
    LIGHT_BLUE2_2(130, new Color(73, 129, 252)),
    LIGHT_BLUE2_3(131, new Color(39, 66, 133)),

    EMERALD_0(132, new Color(0, 151, 39)),
    EMERALD_1(133, new Color(0, 185, 49)),
    EMERALD_2(134, new Color(0, 214, 57)),
    EMERALD_3(135, new Color(0, 113, 30)),

    BROWN3_0(136, new Color(90, 59, 34)),
    BROWN3_1(137, new Color(110, 73, 41)),
    BROWN3_2(138, new Color(127, 85, 48)),
    BROWN3_3(139, new Color(67, 44, 25)),

    DARK_RED2_0(140, new Color(78, 1, 0)),
    DARK_RED2_1(141, new Color(95, 1, 0)),
    DARK_RED2_2(142, new Color(111, 2, 0)),
    DARK_RED2_3(143, new Color(58, 1, 0)),

    CREME_0(144, new Color(148, 124, 114)),
    CREME_1(145, new Color(180, 153, 139)),
    CREME_2(146, new Color(209, 177, 161)),
    CREME_3(147, new Color(111, 94, 85)),

    DARK_ORANGE2_0(148, new Color(112, 58, 25)),
    DARK_ORANGE2_1(149, new Color(137, 71, 31)),
    DARK_ORANGE2_2(150, new Color(159, 82, 36)),
    DARK_ORANGE2_3(151, new Color(84, 43, 19)),

    MAGENTA2_0(152, new Color(105, 61, 76)),
    MAGENTA2_1(153, new Color(129, 75, 93)),
    MAGENTA2_2(154, new Color(149, 87, 108)),
    MAGENTA2_3(155, new Color(79, 46, 57)),

    GRAYISH_BLUE_0(156, new Color(79, 76, 97)),
    GRAYISH_BLUE_1(157, new Color(97, 93, 119)),
    GRAYISH_BLUE_2(158, new Color(112, 108, 138)),
    GRAYISH_BLUE_3(159, new Color(59, 57, 73)),

    GOLD2_0(160, new Color(131, 94, 25)),
    GOLD2_1(161, new Color(160, 115, 31)),
    GOLD2_2(162, new Color(186, 133, 36)),
    GOLD2_3(163, new Color(98, 70, 19)),

    LIGHT_GREEN2_0(164, new Color(73, 83, 37)),
    LIGHT_GREEN2_1(165, new Color(89, 101, 46)),
    LIGHT_GREEN2_2(166, new Color(103, 117, 53)),
    LIGHT_GREEN2_3(167, new Color(55, 62, 28)),

    DARK_PINK_0(168, new Color(113, 54, 55)),
    DARK_PINK_1(169, new Color(138, 66, 67)),
    DARK_PINK_2(170, new Color(160, 77, 78)),
    DARK_PINK_3(171, new Color(85, 41, 41)),

    VERY_DARK_BROWN_0(172, new Color(40, 29, 25)),
    VERY_DARK_BROWN_1(173, new Color(49, 35, 30)),
    VERY_DARK_BROWN_2(174, new Color(57, 41, 35)),
    VERY_DARK_BROWN_3(175, new Color(30, 22, 19)),

    DARK_CREME_0(176, new Color(95, 76, 69)),
    DARK_CREME_1(177, new Color(116, 92, 85)),
    DARK_CREME_2(178, new Color(135, 107, 98)),
    DARK_CREME_3(179, new Color(71, 57, 52)),

    GRAYISH_BLUE2_0(180, new Color(61, 65, 65)),
    GRAYISH_BLUE2_1(181, new Color(75, 79, 79)),
    GRAYISH_BLUE2_2(182, new Color(87, 92, 92)),
    GRAYISH_BLUE2_3(183, new Color(46, 49, 49)),

    DARK_PINK2_0(184, new Color(86, 52, 62)),
    DARK_PINK2_1(185, new Color(105, 63, 76)),
    DARK_PINK2_2(186, new Color(122, 73, 88)),
    DARK_PINK2_3(187, new Color(65, 39, 47)),

    VIOLET2_0(188, new Color(54, 44, 65)),
    VIOLET2_1(189, new Color(66, 53, 79)),
    VIOLET2_2(190, new Color(76, 62, 92)),
    VIOLET2_3(191, new Color(40, 33, 49)),

    DARK_BROWN2_0(192, new Color(54, 35, 25)),
    DARK_BROWN2_1(193, new Color(66, 43, 30)),
    DARK_BROWN2_2(194, new Color(76, 50, 35)),
    DARK_BROWN2_3(195, new Color(40, 26, 19)),

    DARK_GREEN3_0(196, new Color(54, 58, 30)),
    DARK_GREEN3_1(197, new Color(66, 71, 36)),
    DARK_GREEN3_2(198, new Color(76, 82, 42)),
    DARK_GREEN3_3(199, new Color(40, 43, 22)),

    RED2_0(200, new Color(100, 42, 32)),
    RED2_1(201, new Color(123, 52, 40)),
    RED2_2(201, new Color(142, 60, 46)),
    RED2_3(203, new Color(75, 32, 24)),

    BLACK2_0(204, new Color(26, 16, 11)),
    BLACK2_1(205, new Color(32, 19, 14)),
    BLACK2_2(206, new Color(37, 22, 16)),
    BLACK2_3(207, new Color(20, 12, 8));

    private final int id;
    private final Color color;

    MapColor(final int id, final Color color) {
        this.id = id;
        this.color = color;
    }

    public static MapColor fromId(int id) {
        if (id < 0) {
            id += 256;
        }

        for (final MapColor value : values()) {
            if (value.getId() == id) {
                return value;
            }
        }
        return null;
    }

    public static Color mapColorToRgb(final byte color) {
        final MapColor mapColor = fromId(color);
        return mapColor == null ? TRANSPARENT_0.color : mapColor.color;
    }

    public static MapColor rgbToMapColor(final int r, final int g, final int b) {
        return Arrays.stream(values())
                .filter(mapPalette -> mapPalette.getId() > 3)
                .min(Comparator.comparingDouble(value -> calcDist(value, r, g, b)))
                .orElse(TRANSPARENT_0);
    }

    private static double calcDist(final MapColor mapColor, final int r, final int g, final int b) {
        final Color color = mapColor.getColor();
        return Math.sqrt(Math.pow(r - color.getRed(), 2) + Math.pow(g - color.getGreen(), 2) + Math.pow(b - color.getBlue(), 2));
    }

    public int getId() {
        return this.id;
    }

    public Color getColor() {
        return this.color;
    }

}

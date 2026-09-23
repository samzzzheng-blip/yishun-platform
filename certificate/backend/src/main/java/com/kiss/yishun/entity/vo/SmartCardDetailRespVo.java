package com.kiss.yishun.entity.vo;

import lombok.Data;

@Data
public class SmartCardDetailRespVo {

    private long id;

    private String frontUrl;

    private String backUrl;

    private String edge;

    private String corner;

    private String center;

    private String surface;

    private String score;

    private String certNumber;

    private PokemonVo cardInfo;

    private int status;

    @Data
    public static class PokemonVo {
        private String cardType;
        private String brand;
        private String title;
        private String rarity;
        private String name;
    }
}

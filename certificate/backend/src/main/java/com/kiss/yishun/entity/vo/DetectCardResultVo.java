package com.kiss.yishun.entity.vo;

import lombok.Data;

@Data
public class DetectCardResultVo {
    private int resultCode;
    private String msg;
    private String brand;
    private String cardNum;
    private String edge;
    private String corner;
    private String surface;
    private String center;
    private String totalScore;
    private String pokemonName;
    private String pokemonAttribute;
    private String rarity;
    private String parentSerial;
    private String childSerial;
    private int language;

}

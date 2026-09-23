package com.kiss.yishun.common;

import lombok.Data;

import java.io.Serializable;

@Data
public class Result<T> implements Serializable {

    private int code;

    private String msg;

    private T data;
}

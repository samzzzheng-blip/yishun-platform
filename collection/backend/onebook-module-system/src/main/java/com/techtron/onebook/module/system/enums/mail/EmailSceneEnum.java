package com.techtron.onebook.module.system.enums.mail;

import cn.hutool.core.util.ArrayUtil;
import com.techtron.onebook.framework.common.core.ArrayValuable;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Arrays;

/**
 * 用户短信验证码发送场景的枚举
 *
 * @author 芋道源码
 */
@Getter
@AllArgsConstructor
public enum EmailSceneEnum implements ArrayValuable<Integer> {

    MEMBER_REGISTER(1, "user-email-register", "用户注册-邮箱注册");

    public static final Integer[] ARRAYS = Arrays.stream(values()).map(EmailSceneEnum::getScene).toArray(Integer[]::new);

    /**
     * 验证场景的编号
     */
    private final Integer scene;
    /**
     * 模版编码
     */
    private final String templateCode;
    /**
     * 描述
     */
    private final String description;

    @Override
    public Integer[] array() {
        return ARRAYS;
    }

    public static EmailSceneEnum getCodeByScene(Integer scene) {
        return ArrayUtil.firstMatch(sceneEnum -> sceneEnum.getScene().equals(scene),
                values());
    }

}

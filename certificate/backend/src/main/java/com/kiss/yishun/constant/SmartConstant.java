package com.kiss.yishun.constant;

public class SmartConstant {

    public static String SMART_USER = "smartUser";

    public static String SMART_PHONE_CODE_NAME = "smart_phone_code_name:%s:%s";

    public static String SMART_PHONE_CODE_MAX_TRY_NAME = "smart_phone_code_max_try_name:%s:%s";

    public static int SMART_PHONE_CODE_MAX_TRY_TIMES = 5;

    // 验证码失败次数过多，限制再发送时间: 5分钟
    public static int PHONE_CODE_MAX_TRY_EXPIRE_TIME = 5 * 60 * 1000;

    // config key
    public static String CONFIG_KEY_AI_SYSTEM_SET = "aiSystemSet";
    public static String CONFIG_KEY_AI_QUESTION = "aiQuestion";
    public static String CONFIG_KEY_AI_ASK_URL = "aiAskUrl";
    public static String CONFIG_KEY_AI_API_KEY = "aiApiKey";
    public static String CONFIG_KEY_AI_MODEL = "aiModel";
    public static String CONFIG_KEY_AI_UPLOAD_TMP_DIR = "aiUploadTmpDir";
}

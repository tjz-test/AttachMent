package com.inspur.gsxm.attachmentex.entity;

/**
 * @author tianjinzan01
 * @description
 * @date
 */
public class RedisKeyCou {

    /**
     * 非公共构造函数创建
     */
    private RedisKeyCou() {
        throw new IllegalStateException("Utility class");
    }

    /**
     * acesskey
     */
    public static final String ACESS_KEY = "gsdfsacesskey";
    /**
     * secretkey
     */
    public static final String SECRET_KEY = "gsdfssecretkey";
    /**
     * ip
     */
    public static final String AMAZONS3_IP = "gsdfsamazons3ip";
    /**
     * maxconnections
     */
    public static final String MAX_CONNS = "gsdfsamazons3maxconnections";
    /**
     * maxerrorretry
     */
    public static final String MAX_ERRORS = "gsdfsamazons3maxerrorretry";
    /**
     * protocol
     */
    public static final String PROTOCOL = "gsdfsamazons3protocol";
    /**
     * sockettimeout
     */
    public static final String SOCKET_TIMEOUT = "gsdfsamazons3sockettimeout";
    /**
     * bucket
     */
    public static final String BUCKET_NAME = "gsdfsamazons3bucketname";
}

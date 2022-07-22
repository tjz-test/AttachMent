package com.inspur.gsxm.attachmentex.entity;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * @author tianjinzan01
 * @description AmazonS3ymal配置信息
 * @date 2021-11-30
 */

@ConfigurationProperties("gsxmdfs.amazons3")
@Component("amazonParamInfo")
@Setter
@Getter
public class AmazonParamInfo {

    /**
     * 用户名
     */
    private String accessKey;

    /**
     * 密钥
     */
    private String secretKey;

    /**
     * ip
     */
    private String amazons3ip;

    /**
     * 连接协议
     */
    private String amazonS3Protocol;

    /**
     * 失败请求重试次数
     */
    private Integer amazonS3MaxErrorRetry;

    /**
     * 超时时间
     */
    private Integer amazonS3SocketTimeout;

    /**
     * 最大连接数
     */
    private Integer amazonS3MaxConnections;

    /**
     * 桶名称
     */
    private String bucketName;
}

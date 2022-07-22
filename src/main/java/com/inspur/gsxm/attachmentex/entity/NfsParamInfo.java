package com.inspur.gsxm.attachmentex.entity;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * @author tianjinzan01
 * @description nfs连接参数
 * @date 2021-12-04
 */
@ConfigurationProperties("gsxmdfs.nfs")
@Component
@Setter
@Getter
public class NfsParamInfo {
    /**
     * 存储服务器域名
     */
    private String nfsIp;

    /**
     * 基路径
     */
    private String nfsRoot;

    /**
     * UID
     */
    private Integer nfsUid;

    /**
     * GID
     */
    private Integer nfsGid;
}

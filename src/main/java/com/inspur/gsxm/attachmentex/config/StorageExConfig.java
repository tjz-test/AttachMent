package com.inspur.gsxm.attachmentex.config;

import com.inspur.edp.svc.document.storage.api.DocStoragePolicy;
import com.inspur.gsxm.attachmentex.domain.AmazonS3StorageImpl;
import com.inspur.gsxm.attachmentex.domain.NfsStorageImpl;
import com.inspur.gsxm.attachmentex.domain.ServerStorageImpl;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @author tianjinzan01
 * @description 配置发布自定义存储方式
 * @date 2021-11-30
 */
@Configuration
public class StorageExConfig {

    @Bean(name = "amazons3storage")
    public DocStoragePolicy AmazonS3Storage() {
        return new AmazonS3StorageImpl();
    }

    @Bean(name = "nfs3storage")
    public DocStoragePolicy Nfs3Storage() {
        return new NfsStorageImpl();
    }

    @Bean(name = "serverstorage")
    public DocStoragePolicy ServerStorage() {
        return new ServerStorageImpl();
    }
}

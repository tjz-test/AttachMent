package com.inspur.gsxm.attachmentex.cache;

import com.inspur.gsxm.attachmentex.entity.AmazonParamInfo;
import com.inspur.gsxm.attachmentex.entity.RedisKeyCou;
import io.iec.edp.caf.commons.layeringcache.cache.Cache;
import io.iec.edp.caf.commons.layeringcache.manager.CacheManager;
import io.iec.edp.caf.commons.layeringcache.setting.LayeringCacheSetting;
import io.iec.edp.caf.commons.layeringcache.support.ExpireMode;
import io.iec.edp.caf.commons.utils.SpringBeanUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.stereotype.Component;
import javax.annotation.Resource;
import java.util.concurrent.TimeUnit;

/**
 * @author tianjinzan01
 * @description amazons3环境配置存储
 * @date 2021-11-20
 */

@Slf4j
@Component("amazonS3Cache")
@EnableConfigurationProperties({AmazonParamInfo.class})
public class AmazonS3Cache {

    /**
     * 注入amazons3配置信息
     */
    @Resource(name = "amazonParamInfo")
    private AmazonParamInfo amazonParamInfo;

    /**
     * 配置参数获取
     * @return 返回参数配置类
     */
    public AmazonParamInfo getAmazonS3Conf(){
        //获取cacheManager的Bean并注入
        CacheManager cacheManager = SpringBeanUtils.getBean(CacheManager.class);

        //声明当前要使用的缓存配置
        LayeringCacheSetting layeringCacheSetting = new LayeringCacheSetting.Builder().enableFirstCache().firstCacheExpireMode(ExpireMode.WRITE).firstCacheExpireTime(60).firstCacheTimeUnit(TimeUnit.MINUTES).disableSecondCache().depict("").build();

        //获取缓存实例,根据部署模式名称
        Cache cache = cacheManager.getCache("default", layeringCacheSetting);

        //实例化配置参数类
        AmazonParamInfo amazomConf = new AmazonParamInfo();

        //查看缓存是否失效
        if ("".equals(cache.get(RedisKeyCou.ACESS_KEY, String.class)) || cache.get(RedisKeyCou.ACESS_KEY, String.class) == null) {
            cache.put(RedisKeyCou.ACESS_KEY, amazonParamInfo.getAccessKey());
            cache.put(RedisKeyCou.SECRET_KEY, amazonParamInfo.getSecretKey());
            cache.put(RedisKeyCou.AMAZONS3_IP, amazonParamInfo.getAmazons3ip());
            cache.put(RedisKeyCou.MAX_CONNS, amazonParamInfo.getAmazonS3MaxConnections());
            cache.put(RedisKeyCou.MAX_ERRORS, amazonParamInfo.getAmazonS3MaxErrorRetry());
            cache.put(RedisKeyCou.PROTOCOL, amazonParamInfo.getAmazonS3Protocol());
            cache.put(RedisKeyCou.SOCKET_TIMEOUT, amazonParamInfo.getAmazonS3SocketTimeout());
            cache.put(RedisKeyCou.BUCKET_NAME, amazonParamInfo.getBucketName());
        }

        //参数获取
        amazomConf.setAccessKey(cache.get(RedisKeyCou.ACESS_KEY, String.class));
        amazomConf.setSecretKey(cache.get(RedisKeyCou.SECRET_KEY, String.class));
        amazomConf.setAmazons3ip(cache.get(RedisKeyCou.AMAZONS3_IP, String.class));
        amazomConf.setAmazonS3MaxConnections(cache.get(RedisKeyCou.MAX_CONNS, Integer.class));
        amazomConf.setAmazonS3MaxErrorRetry(cache.get(RedisKeyCou.MAX_ERRORS, Integer.class));
        amazomConf.setAmazonS3Protocol(cache.get(RedisKeyCou.PROTOCOL, String.class));
        amazomConf.setAmazonS3SocketTimeout(cache.get(RedisKeyCou.SOCKET_TIMEOUT, Integer.class));
        amazomConf.setBucketName(cache.get(RedisKeyCou.BUCKET_NAME, String.class));

        return amazomConf;
    }
}

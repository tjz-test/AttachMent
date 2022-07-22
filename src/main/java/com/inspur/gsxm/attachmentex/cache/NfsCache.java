package com.inspur.gsxm.attachmentex.cache;

import com.inspur.gsxm.attachmentex.entity.NfsParamInfo;
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
 * @description
 * @date
 */
@Slf4j
@Component
@EnableConfigurationProperties(NfsParamInfo.class)
public class NfsCache {

    /**
     * 注入nfs配置信息
     */
    @Resource(name = "nfsParamInfo")
    private NfsParamInfo nfsParamInfo;

    /**
     * 获取配置参数
     * @return 返回
     */
    public NfsParamInfo getNfsParamInfo(){

        //获取cacheManager的Bean并注入
        CacheManager cacheManager = SpringBeanUtils.getBean(CacheManager.class);

        //声明当前要使用的缓存配置
        LayeringCacheSetting layeringCacheSetting = new LayeringCacheSetting.Builder()
                //开启一级缓存
                .enableFirstCache()
                //一级缓存过期策略
                .firstCacheExpireMode(ExpireMode.WRITE)
                //一级缓存过期时间
                .firstCacheExpireTime(10)
                //一级缓存过期时间单位
                .firstCacheTimeUnit(TimeUnit.MINUTES)
                //开启二级缓存
                .disableSecondCache()
                .depict("").build();

        //获取缓存实例,根据部署模式名称
        Cache cache = cacheManager.getCache("default", layeringCacheSetting);
        //实例化配置参数类
        NfsParamInfo nfsParamConf = new NfsParamInfo();
        //查看缓存是否失效
        if("".equals(cache.get("gsdfsnfsip",String.class)) || cache.get("gsdfsnfsip",String.class) == null){
            log.error("nfs缓存赋值");
            cache.put("gsdfsnfsip",nfsParamInfo.getNfsIp());
            cache.put("gsdfsnfsrootid",nfsParamInfo.getNfsRoot());
            cache.put("gsdfsnfsuid",nfsParamInfo.getNfsUid());
            cache.put("gsdfsnfsgid",nfsParamInfo.getNfsGid());
        }
        nfsParamConf.setNfsIp(cache.get("gsdfsnfsip",String.class));
        nfsParamConf.setNfsRoot(cache.get("gsdfsnfsrootid",String.class));
        nfsParamConf.setNfsUid(cache.get("gsdfsnfsuid",Integer.class));
        nfsParamConf.setNfsGid(cache.get("gsdfsnfsgid",Integer.class));

        return nfsParamConf;
    }
}

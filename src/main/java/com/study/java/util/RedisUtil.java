package com.study.java.util;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;

import java.util.concurrent.TimeUnit;

/**
 *8月27日
 * 封装了一个redisTemplate工具类
 * 后续应该多重载几个方法。不然每次4个参数太累。
 */
@Component
public class RedisUtil {

    @Autowired
    private RedisTemplate redisTemplate;

    public void setString(String key,Object value,Long time,TimeUnit timeUtil){
        Assert.isTrue(value instanceof String,"Type conversion exception");
        if(time==null)
            setString(key,value);
        if(timeUtil==null)
            setString(key,value,time);
        else
            redisTemplate.opsForValue().set(key,value,time, timeUtil);
    }

    private void setString(String key, Object value) {
        redisTemplate.opsForValue().set(key,value);
    }

    private void setString(String key, Object value,Long time) {
        Assert.isTrue(time instanceof Long,"Type conversion exception");
        redisTemplate.opsForValue().set(key,value,time, TimeUnit.SECONDS);
    }


    public Object  get(String key){
       return redisTemplate.opsForValue().get(key);
    }

    public Boolean hasKey(String key){
       return redisTemplate.hasKey(key);
    }


}

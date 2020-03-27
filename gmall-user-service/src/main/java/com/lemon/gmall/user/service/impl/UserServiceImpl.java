package com.lemon.gmall.user.service.impl;


import com.alibaba.dubbo.config.annotation.Service;
import com.alibaba.fastjson.JSON;
import com.fasterxml.jackson.databind.jsonFormatVisitors.JsonAnyFormatVisitor;
import com.lemon.gmall.bean.UmsMember;
import com.lemon.gmall.bean.UmsMemberReceiveAddress;
import com.lemon.gmall.service.UserService;

import com.lemon.gmall.user.mapper.UmsMemberMapper;
import com.lemon.gmall.user.mapper.UmsMemberReceiveAddressMapper;
import com.lemon.gmall.util.RedisUtil;
import com.sun.org.apache.bcel.internal.generic.NEW;
import org.springframework.beans.factory.annotation.Autowired;
import redis.clients.jedis.Jedis;


import java.util.List;

/**
 * UserServiceImpl
 *
 * @Author: 李蒙
 * @CreateTime: 2020-03-01
 * @Description:
 */
@Service
public class UserServiceImpl implements UserService {

    @Autowired
    private UmsMemberMapper umsMemberMapper;

    @Autowired
    UmsMemberReceiveAddressMapper umsMemberReceiveAddressMapper;

    @Autowired
    RedisUtil redisUtil;

    @Override
    public List<UmsMember> getAllUser() {

        List<UmsMember> list = umsMemberMapper.selectAll();
        return list;
    }

    @Override
    public UmsMember checkUser(UmsMember user) {
        UmsMember umsMember = umsMemberMapper.selectOne(user);
        return umsMember;
    }

    @Override
    public List<UmsMemberReceiveAddress> getAddressListByUser(String userId) {
        UmsMemberReceiveAddress umsMemberReceiveAddress = new UmsMemberReceiveAddress();
        umsMemberReceiveAddress.setMemberId(userId);
        List<UmsMemberReceiveAddress> umsMemberReceiveAddressList = umsMemberReceiveAddressMapper.select(umsMemberReceiveAddress);
        return umsMemberReceiveAddressList;
    }

    @Override
    public void putUserToken(UmsMember user, String newToken) {
        Jedis jedis = null;
        try{
            jedis = redisUtil.getJedis();
            //将newtoken放入redis中
            jedis.setex("user:"+user.getId()+":token",60*60,newToken);
            UmsMember umsMember = new UmsMember();
            umsMember.setId(user.getId());
            UmsMember umsMember1 = umsMemberMapper.selectOne(umsMember);
            jedis.setex("token:"+newToken+":user",60*60, JSON.toJSONString(umsMember1));
        }catch (Exception e){
            e.getMessage();
        }finally {
            jedis.close();
        }
    }

    @Override
    public UmsMember checkUserToken(String token) {
        Jedis jedis=null;
        UmsMember umsMember =null;
        try{
            jedis =redisUtil.getJedis();
            String s = jedis.get("token:" + token + ":user");
            if (s!=null){
                umsMember = JSON.parseObject(s, UmsMember.class);
            }
        }catch (Exception e){
            e.getMessage();
        }finally {
            jedis.close();
        }
        return umsMember;
    }
}
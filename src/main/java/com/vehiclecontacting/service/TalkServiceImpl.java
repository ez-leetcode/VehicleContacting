package com.vehiclecontacting.service;

import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.vehiclecontacting.mapper.TalkMessageMapper;
import com.vehiclecontacting.mapper.TalkUserMapper;
import com.vehiclecontacting.mapper.UserMapper;
import com.vehiclecontacting.pojo.TalkMessage;
import com.vehiclecontacting.pojo.TalkUser;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@Service
public class TalkServiceImpl implements TalkService{

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private TalkUserMapper talkUserMapper;

    @Autowired
    private TalkMessageMapper talkMessageMapper;



    @Override
    public JSONObject getTalkList(Long id, Long cnt, Long page) {
        return null;
    }


    @Override
    public String allRead(Long id) {
        //先获取所有的聊天
        QueryWrapper<TalkUser> wrapper = new QueryWrapper<>();
        wrapper.eq("id1",id)
                .eq("id1_read",0)
                .or()
                .eq("id2",id)
                .eq("id1_read",0);
        List<TalkUser> talkUserList = talkUserMapper.selectList(wrapper);
        for(TalkUser x:talkUserList){
            if(x.getId1().equals(id)){
                //id2是别人
                x.setId1Read(1);
                talkUserMapper.updateById(x);
                //获取语句列表
                QueryWrapper<TalkMessage> wrapper1 = new QueryWrapper<>();
                wrapper1.eq("to_id",id)
                        .eq("from_id",x.getId2());
                List<TalkMessage> talkMessageList = talkMessageMapper.selectList(wrapper1);
                for(TalkMessage x1:talkMessageList){
                    x1.setIsRead(1);
                    //更新状态
                    talkMessageMapper.updateById(x1);
                }
            }else{
                x.setId2Read(1);
                talkUserMapper.updateById(x);
                //获取语句列表
                QueryWrapper<TalkMessage> wrapper1 = new QueryWrapper<>();
                wrapper1.eq("to_id",id)
                        .eq("from_id",x.getId1());
                List<TalkMessage> talkMessageList = talkMessageMapper.selectList(wrapper1);
                for(TalkMessage x1:talkMessageList){
                    x1.setIsRead(1);
                    //更新状态
                    talkMessageMapper.updateById(x1);
                }
            }
        }
        log.info("添加所有已读成功");
        return "success";
    }

}
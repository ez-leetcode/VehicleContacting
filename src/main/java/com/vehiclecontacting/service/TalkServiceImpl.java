package com.vehiclecontacting.service;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.vehiclecontacting.mapper.TalkMessageMapper;
import com.vehiclecontacting.mapper.TalkUserMapper;
import com.vehiclecontacting.mapper.UserMapper;
import com.vehiclecontacting.msg.TalkHeadMsg;
import com.vehiclecontacting.msg.TalkP2PMsg;
import com.vehiclecontacting.msg.TalkUserMsg;
import com.vehiclecontacting.pojo.TalkMessage;
import com.vehiclecontacting.pojo.TalkUser;
import com.vehiclecontacting.pojo.User;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.LinkedList;
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

    @Autowired
    private WebsocketService websocketService;

    @Override
    public JSONObject getTalkList(Long id, Long cnt, Long page) {
        JSONObject jsonObject = new JSONObject();
        QueryWrapper<TalkUser> wrapper = new QueryWrapper<>();
        wrapper.eq("id1",id)
                //未删除
                .eq("id1_deleted",0)
                .orderByDesc("update_time")
                .or()
                .eq("id2",id)
                .eq("id2_deleted",0)
                .orderByDesc("update_time");
        Page<TalkUser> page1 = new Page<>(page,cnt);
        talkUserMapper.selectPage(page1,wrapper);
        List<TalkUser> talkMessageList = page1.getRecords();
        List<TalkUserMsg> talkUserMsgList = new LinkedList<>();
        for(TalkUser x:talkMessageList){
            if(id.equals(x.getId1())){
                User user = userMapper.selectById(x.getId2());
                talkUserMsgList.add(new TalkUserMsg(user.getId(),user.getUsername(),user.getVip(),user.getPhoto(),x.getId1Read(),x.getLastMessage(),x.getUpdateTime()));
            }else{
                User user = userMapper.selectById(x.getId1());
                talkUserMsgList.add(new TalkUserMsg(user.getId(),user.getUsername(),user.getVip(),user.getPhoto(),x.getId2Read(), x.getLastMessage(), x.getUpdateTime()));
            }
        }
        jsonObject.put("talkMsgList",talkUserMsgList);
        jsonObject.put("counts",page1.getTotal());
        jsonObject.put("pages",page1.getPages());
        log.info("获取聊天列表成功");
        log.info(jsonObject.toString());
        return jsonObject;
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


    @Override
    public String deletedTalk(Long fromId, Long toId) {
        QueryWrapper<TalkUser> wrapper = new QueryWrapper<>();
        wrapper.eq("id1",fromId)
                .eq("id2",toId)
                .or()
                .eq("id1",toId)
                .eq("id2",fromId);
        TalkUser talkUser = talkUserMapper.selectOne(wrapper);
        if(talkUser == null){
            log.error("删除用户聊天失败，聊天不存在");
            return "existWrong";
        }
        if(talkUser.getId1().equals(fromId) && talkUser.getId1Deleted() == 0){
            talkUser.setId1Deleted(1);
            //更新删除
            talkUserMapper.update(talkUser,wrapper);
        }else if(talkUser.getId2().equals(fromId) && talkUser.getId2Deleted() == 0){
            talkUser.setId2Deleted(1);
            //更新删除
            talkUserMapper.update(talkUser,wrapper);
        }else{
            log.error("删除用户聊天失败，聊天已被删除");
            return "existWrong";
        }
        log.info("删除用户聊天成功");
        return "success";
    }


    @Override
    public String isRead(Long fromId, Long toId) {
        QueryWrapper<TalkUser> wrapper = new QueryWrapper<>();
        wrapper.eq("id1",fromId)
                .eq("id2",toId)
                .or()
                .eq("id1",toId)
                .eq("id2",fromId);
        TalkUser talkUser = talkUserMapper.selectOne(wrapper);
        if(talkUser == null){
            log.error("标记为已读失败，聊天不存在");
            return "existWrong";
        }
        if(talkUser.getId1().equals(fromId)){
            //未读为0
            talkUser.setId1Read(0);
        }else{
            talkUser.setId2Read(0);
        }
        talkUserMapper.update(talkUser,wrapper);
        //找所有聊天标记
        QueryWrapper<TalkMessage> wrapper1 = new QueryWrapper<>();
        wrapper1.eq("from_id",toId)
                .eq("to_id",fromId)
                .eq("is_read",0);
        List<TalkMessage> talkMessageList = talkMessageMapper.selectList(wrapper1);
        for(TalkMessage x:talkMessageList){
            x.setIsRead(1);
            talkMessageMapper.updateById(x);
        }
        log.info("标记为已读成功");
        return "success";
    }


    @Override
    public String deleteTalkMsg(Long fromId, Long toId, List<Long> numbers) {
        for(Long x:numbers){
            TalkMessage talkMessage = talkMessageMapper.selectById(x);
            if(talkMessage == null){
                log.error("批量删除用户聊天信息失败，信息不存在");
                return "existWrong";
            }else{
                if(talkMessage.getFromId().equals(fromId) && talkMessage.getToId().equals(toId)){
                    talkMessage.setFromDeleted(1);
                }else if(talkMessage.getFromId().equals(toId) && talkMessage.getToId().equals(fromId)){
                    talkMessage.setToDeleted(1);
                }else{
                    log.error("批量删除用户聊天信息失败，用户信息有误");
                    return "userWrong";
                }
                talkMessageMapper.updateById(talkMessage);
            }
        }
        log.info("批量删除用户聊天信息成功");
        return "success";
    }

    @Override
    public JSONObject getP2PTalkList(Long fromId, Long toId, Long page, Long cnt) {
        JSONObject jsonObject = new JSONObject();
        QueryWrapper<TalkMessage> wrapper = new QueryWrapper<>();
        wrapper.eq("from_id",fromId)
                .eq("to_id",toId)
                .eq("from_deleted",0)
                .orderByDesc("create_time")
                .or()
                .eq("to_id",fromId)
                .eq("from_id",toId)
                .eq("to_deleted",0)
                .orderByDesc("create_time");
        Page<TalkMessage> page1 = new Page<>(page,cnt);
        talkMessageMapper.selectPage(page1,wrapper);
        List<TalkMessage> talkMessageList = page1.getRecords();
        List<TalkP2PMsg> talkP2PMsgList = new LinkedList<>();
        for(TalkMessage x:talkMessageList){
            talkP2PMsgList.add(new TalkP2PMsg(x.getNumber(),x.getFromId(),x.getToId(),x.getMessage(),x.getCreateTime()));
        }
        //标记已读
        QueryWrapper<TalkMessage> wrapper1 = new QueryWrapper<>();
        wrapper1.eq("from_id",toId)
                .eq("to_id",fromId)
                .eq("to_deleted",0)
                .eq("is_read",0);
        List<TalkMessage> talkMessageList1 = talkMessageMapper.selectList(wrapper1);
        for(TalkMessage x:talkMessageList1){
            x.setIsRead(1);
            talkMessageMapper.updateById(x);
        }
        log.info("标记已读成功");
        //更新TalkUser
        QueryWrapper<TalkUser> wrapper2 = new QueryWrapper<>();
        wrapper2.eq("id1",fromId)
                .eq("id2",toId)
                .or()
                .eq("id2",fromId)
                .eq("id1",toId);
        TalkUser talkUser = talkUserMapper.selectOne(wrapper2);
        if(talkUser != null){
            if(talkUser.getId1().equals(fromId)){
                talkUser.setId1Read(0);
            }else{
                talkUser.setId2Read(0);
            }
            talkUserMapper.update(talkUser,wrapper2);
            log.info("talkUser更新成功");
        }
        jsonObject.put("talkList",talkP2PMsgList);
        jsonObject.put("counts",page1.getTotal());
        jsonObject.put("pages",page1.getPages());
        log.info("获取P2P聊天列表成功");
        log.info(jsonObject.toString());
        return jsonObject;
    }



    @Override
    public JSONObject judgeTalkHead(Long fromId, Long toId) {
        JSONObject jsonObject = new JSONObject();
        boolean judge = websocketService.judgeOnline(toId);
        User user = userMapper.selectById(toId);
        TalkHeadMsg talkHeadMsg = new TalkHeadMsg(toId,user.getUsername(),null);
        if(judge){
            talkHeadMsg.setIsOnline(1);
        }else{
            talkHeadMsg.setIsOnline(0);
        }
        jsonObject.put("userMsg",talkHeadMsg);
        log.info("获取用户在线信息成功");
        log.info(jsonObject.toString());
        return jsonObject;
    }



}
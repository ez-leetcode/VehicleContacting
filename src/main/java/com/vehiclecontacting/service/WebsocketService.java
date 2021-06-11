package com.vehiclecontacting.service;


import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.vehiclecontacting.config.RabbitmqWebsocketProductConfig;
import com.vehiclecontacting.mapper.BoxMessageMapper;
import com.vehiclecontacting.mapper.TalkMessageMapper;
import com.vehiclecontacting.mapper.TalkUserMapper;
import com.vehiclecontacting.mapper.UserMapper;
import com.vehiclecontacting.msg.SystemMsg;
import com.vehiclecontacting.msg.TalkMsg;
import com.vehiclecontacting.msg.WebsocketMsg;
import com.vehiclecontacting.pojo.BoxMessage;
import com.vehiclecontacting.pojo.TalkMessage;
import com.vehiclecontacting.pojo.TalkUser;
import com.vehiclecontacting.pojo.User;
import com.vehiclecontacting.utils.TalkResultUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.websocket.*;
import javax.websocket.server.PathParam;
import javax.websocket.server.ServerEndpoint;
import java.util.concurrent.ConcurrentHashMap;

//总线上的连接
@Component
@Slf4j
@ServerEndpoint(value = "/websocket/{id}")
public class WebsocketService {

    @Autowired
    private TalkUserMapper talkUserMapper;

    @Autowired
    private TalkMessageMapper talkMessageMapper;

    @Autowired
    private BoxMessageMapper boxMessageMapper;

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private BoxService boxService;

    private static RabbitmqWebsocketProductConfig rabbitmqWebsocketProductConfig;

    @Autowired
    public void setRabbitmqWebsocketProductConfig(RabbitmqWebsocketProductConfig rabbitmqWebsocketProductConfig){
        WebsocketService.rabbitmqWebsocketProductConfig = rabbitmqWebsocketProductConfig;
    }

    //与某个客户端连接会话，以此来给客户端发送数据
    private Session session;

    //线程安全hashmap，存放每个客户端对应的websocket对象
    private static ConcurrentHashMap<String,WebsocketService> websocketServiceConcurrentHashMap = new ConcurrentHashMap<>();

    //建立连接会调用这个方法
    @OnOpen
    public void onOpen(Session session, @PathParam("id") String id){
        log.info("正在建立与app的连接，用户id：" + id);
        //建立连接
        this.session = session;
        //放入hashmap中
        websocketServiceConcurrentHashMap.put(id,this);
        log.info("有新的连接建立，当前总连接数：" + websocketServiceConcurrentHashMap.size());
    }

    //关闭连接时调用这个方法
    @OnClose
    public void onClose(@PathParam("id") String id){
        log.info("正在关闭与app的连接，用户：" + id);
        websocketServiceConcurrentHashMap.remove(id);
        log.info("连接已断开，当前连接数：" + websocketServiceConcurrentHashMap.size());
    }

    //聊天出错时调用
    @OnError
    public void onEorror(Session session, Throwable error){
        log.info("app连接出现错误：" + session.getId());
        error.printStackTrace();
    }

    //有消息从客户端发送进来，发给消息队列
    @OnMessage
    public void onMessage(Session session,String message){
        log.info("有消息从客户端发送进来");
        log.info(message);
        log.info(session.toString());
        //把message转成talkMsg
        rabbitmqWebsocketProductConfig.sendMessageToFanoutExchange(message);
        log.info("rabbitmq发送信息成功");
    }

    //判断是否在线  true：在线  false：不在线
    public boolean judgeOnline(Long id){
        WebsocketService websocketService = websocketServiceConcurrentHashMap.get(id.toString());
        return websocketService != null;
    }

    public void sendBoxMsg(BoxMessage boxMessage){
        log.info("正在向别的用户发送消息盒子消息");
        log.info(boxMessage.toString());
        WebsocketService websocketService = websocketServiceConcurrentHashMap.get(boxMessage.getId().toString());
        User user = userMapper.selectById(boxMessage.getId());
        if(websocketService != null && user.getIsNoDisturb() == 0){
            log.info("正在给用户实时推送消息盒子消息");
            SystemMsg systemMsg = new SystemMsg(boxMessage.getTitle(),boxMessage.getMessage());
            websocketService.session.getAsyncRemote().sendText(TalkResultUtils.getResult(JSON.parseObject(systemMsg.toString()),"systemInfoSuccess").toString());
            log.info("消息盒子消息推送成功");
        }
        boxService.addBoxMessage(boxMessage.getId(),boxMessage.getMessage(),boxMessage.getTitle());
        //存到消息盒子里
        log.info("消息盒子更新成功");
    }


    //发送聊天信息
    public void sendMsg(TalkMsg talkMsg){
        log.info("正在向别的用户发送消息");
        log.info(talkMsg.toString());
        //获取用户消息实体
        WebsocketService websocketService = websocketServiceConcurrentHashMap.get(talkMsg.getToId().toString());
        //如果用户在线，直接推送
        User user1 = userMapper.selectById(talkMsg.getToId());
        if(websocketService != null && user1.getIsNoDisturb() == 0){
            log.info("正在给用户实时推送消息");
            User user = userMapper.selectById(talkMsg.getFromId());
            WebsocketMsg websocketMsg = new WebsocketMsg(talkMsg.getFromId(),user.getPhoto(),user.getUsername(),talkMsg.getInfo());
            websocketService.session.getAsyncRemote().sendText(TalkResultUtils.getResult(JSON.parseObject(websocketMsg.toString()),"receiveInfoSuccess").toString());
            log.info("推送消息成功");
        }
        //保存信息在数据库中
        TalkMessage talkMessage = new TalkMessage(null,talkMsg.getFromId(),talkMsg.getToId(),talkMsg.getInfo(),0,0,0,0,null);
        talkMessageMapper.insert(talkMessage);
        //更新talkUser
        QueryWrapper<TalkUser> wrapper = new QueryWrapper<>();
        wrapper.eq("id1",talkMsg.getFromId())
                .eq("id2",talkMsg.getToId())
                .or()
                .eq("id1",talkMsg.getToId())
                .eq("id2",talkMsg.getFromId());
        TalkUser talkUser = talkUserMapper.selectOne(wrapper);
        if(talkUser == null){
            TalkUser talkUser1 = new TalkUser(talkMsg.getFromId(),talkMsg.getToId(),0,1,0,0,talkMsg.getInfo(),null,null);
            talkUserMapper.insert(talkUser1);
            log.info("已创建talkUser");
        }else{
            //更新talkUser
            if(talkUser.getId1().equals(talkMsg.getFromId())){
                talkUser.setId2Read(talkUser.getId2Read() + 1);
            }else{
                talkUser.setId1Read(talkUser.getId1Read() + 1);
            }
            talkUser.setLastMessage(talkMsg.getInfo());
            talkUserMapper.update(talkUser,wrapper);
            log.info("talkUser更新成功");
        }
        //返回一个回执
        WebsocketService websocketService1 = websocketServiceConcurrentHashMap.get(talkMsg.getFromId().toString());
        if(websocketService1 != null){
            log.info("正在给用户同步发送回执");
            websocketService1.session.getAsyncRemote().sendText(TalkResultUtils.getResult(new JSONObject(),"sendInfoSuccess").toString());
            log.info("给用户同步发送回执成功");
        }
    }
}

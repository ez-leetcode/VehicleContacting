package com.vehiclecontacting.service;


import com.alibaba.fastjson.JSONObject;
import com.vehiclecontacting.config.RabbitmqWebsocketProductConfig;
import com.vehiclecontacting.msg.TalkMsg;
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


    public void sendMsg(TalkMsg talkMsg){
        log.info("正在向别的用户发送消息");
        log.info(talkMsg.toString());
        //获取用户消息实体
        WebsocketService websocketService = websocketServiceConcurrentHashMap.get(talkMsg.getToId().toString());
        //如果用户存在，直接推送
        if(websocketService != null){
            log.info("正在给用户实时推送消息");
            websocketService.session.getAsyncRemote().sendText(talkMsg.toString());
            log.info("推送消息成功");
        }

    }

}

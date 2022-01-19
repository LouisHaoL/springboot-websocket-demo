package com.louis.websocket;
import com.alibaba.fastjson.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import javax.websocket.*;
import javax.websocket.server.PathParam;
import javax.websocket.server.ServerEndpoint;
import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * WebSocket使用测试
 *
 * @author liuh
 * @date 2022年01月19日 10:59
 */
@Component
@ServerEndpoint("/myWebSocket/{username}")
public class MyWebSocket {

    private final static Logger log = LoggerFactory.getLogger(MyWebSocket.class);
    private static volatile int onlineCount = 0;
    private static Map<String, MyWebSocket> clients = new ConcurrentHashMap<>();
    private Session session;
    private String username;

    @OnOpen
    public void onOpen(@PathParam("username") String username, Session session) {
        this.username = username;
        this.session = session;

        addOnlineCount();
        clients.put(username, this);
        log.info("{}-已建立连接", username);
    }

    @OnClose
    public void onClose() {
        clients.remove(username);
        log.info("{}-已断开连接", username);
        subOnlineCount();
    }

    /**
     * 带有@OnMessage注解的方法入参必须为
     *
     * @param message 消息JSON String
     */
    @OnMessage
    public void onMessage(String message) {
        session.getAsyncRemote().sendText(message);
    }

    @OnError
    public void onError(Session session, Throwable error) {
        error.printStackTrace();
    }

    public static void sendMsg(String id, String msg) {
        if ("all".equals(id)) {
            clients.values().forEach(session -> session.onMessage(String.format("消息：%s，在线人数：%d", msg, getOnlineCount())));
        } else {
            if (clients.containsKey(id)) {
                clients.get(id).onMessage(msg);
            }
        }
    }

    public static synchronized int getOnlineCount() {
        return onlineCount;
    }

    public static synchronized void addOnlineCount() {
        MyWebSocket.onlineCount++;
    }

    public static synchronized void subOnlineCount() {
        MyWebSocket.onlineCount--;
    }

    public static synchronized Map<String, MyWebSocket> getClients() {
        return clients;
    }
}

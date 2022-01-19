# README

## 1、WebSocket演示项目

本项目用于演示WebSocket-SpringBoot的搭建以及使用。

## 2、后端搭建过程

### 1)、pom引入

```xml
    <dependencies>
        <!--添加Web依赖 -->
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-web</artifactId>
            <version>2.6.2</version>
        </dependency>
        <!-- https://mvnrepository.com/artifact/org.springframework.boot/spring-boot-starter-websocket -->
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-websocket</artifactId>
            <version>2.6.2</version>
        </dependency>

        <!-- https://mvnrepository.com/artifact/com.google.code.gson/gson -->
        <dependency>
            <groupId>com.google.code.gson</groupId>
            <artifactId>gson</artifactId>
            <version>2.8.5</version>
        </dependency>
        <!-- https://mvnrepository.com/artifact/com.alibaba/fastjson -->
        <dependency>
            <groupId>com.alibaba</groupId>
            <artifactId>fastjson</artifactId>
            <version>1.2.76</version>
        </dependency>
    </dependencies>
```

### 2)、配置类

```java
@Configuration
public class WebSocketConfig {

    @Bean
    public ServerEndpointExporter serverEndpointExporter() {
        return new ServerEndpointExporter();
    }
}
```

### 3)、WebSocketServer类

```java

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
```

### 4)、调用

这里使用的是接口调用

```java
@RestController
public class MyController {

    @ResponseBody
    @GetMapping("/webSocketTest")
    public String webSocketTest(@RequestParam String id, @RequestParam String msg) throws IOException {
        MyWebSocket.sendMsg(id, msg);
        return "It's Ok";
    }
}
```

## 3、前端调用

```html
<!DOCTYPE html>
<html lang="ch-ZN">
<head>
    <meta charset="UTF-8">
    <title>webSocketId1</title>
</head>
<body>
<div id="msgBox"></div>
</body>
<script>
    var websocket = null;
    var host = document.location.host;
    var username = "0001"; // 获得当前登录人员的userName
    // alert(username)
    //判断当前浏览器是否支持WebSocket 
    if ('WebSocket' in window) {
        setMessageInnerHTML("浏览器支持Websocket");
        // wss需要证书
        websocket = new WebSocket('ws://'+host+'/websocket/myWebSocket/' + username);
    } else {
        setMessageInnerHTML('当前浏览器 Not support websocket');
    }

    //连接发生错误的回调方法 
    websocket.onerror = function () {
        setMessageInnerHTML("WebSocket连接发生错误");
    };

    //连接成功建立的回调方法 
    websocket.onopen = function () {
        setMessageInnerHTML("WebSocket连接成功");
    }

    //接收到消息的回调方法 
    websocket.onmessage = function (event) {
        setMessageInnerHTML("接收到消息的回调方法")
        setMessageInnerHTML("这是后台推送的消息：" + event.data);
    }

    //连接关闭的回调方法 
    websocket.onclose = function () {
        setMessageInnerHTML("WebSocket连接关闭");
    }

    //监听窗口关闭事件，当窗口关闭时，主动去关闭websocket连接，防止连接还没断开就关闭窗口，server端会抛异常。 
    window.onbeforeunload = function () {
        closeWebSocket();
    }

    //关闭WebSocket连接 
    function closeWebSocket() {
        websocket.close();
    }

    //将消息显示在网页上
    function setMessageInnerHTML(innerHTML) {
        document.getElementById('msgBox').innerHTML += innerHTML + '<br/>';
    }
</script>
</html>
```




package com.louis.controller;

import com.google.gson.JsonObject;
import com.louis.websocket.MyWebSocket;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;

/**
 * 自定义控制器
 *
 * @author Louis
 * @date 2021年3月24日 11:05:48
 */
@RestController
public class MyController {

    @ResponseBody
    @GetMapping("/webSocketTest")
    public String webSocketTest(@RequestParam String id, @RequestParam String msg) throws IOException {
        MyWebSocket.sendMsg(id, msg);
        return "It's Ok";
    }

}

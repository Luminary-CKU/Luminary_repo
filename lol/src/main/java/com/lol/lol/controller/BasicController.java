package com.lol.lol.controller;


import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
public class BasicController {
//    @GetMapping("/")
////    @ResponseBody // 이건 return 옆에있는 문자 그대로 출력하라는 어노테이션임
//    String search(){
//        return "index.html";
//    }

    @GetMapping("/rank")
    @ResponseBody
    String ranking(){
        return "롤 유저 랭킹이될 페이지";
    }
}

package com.codegym.socialmedia.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class ChatController {

    @GetMapping("/chat-window")
    public String getChatWindow(@RequestParam String chatId,
                                @RequestParam String chatName,
                                @RequestParam String chatType,@RequestParam String avatar,
                                Model model) {
        model.addAttribute("chatId", chatId);
        model.addAttribute("chatName", chatName);
        model.addAttribute("chatType", chatType);
        model.addAttribute("avatar", avatar);
        return "fragments/chat-window :: chatWindow";
    }
}

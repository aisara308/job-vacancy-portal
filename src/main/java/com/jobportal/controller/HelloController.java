package com.jobportal.controller;


import com.jobportal.model.Resumes;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;

@Controller
public class HelloController {

    @GetMapping("/")
    public String greet() {
        return "login";
    }

    @GetMapping("/login")
    public String loginPage() {
        return "login";
    }

    @GetMapping("/register")
    public String registerPage() {
        return "register";
    }

    @GetMapping("/home")
    public String homePage() {
        return "home";
    }

    @GetMapping("/response")
    public String responsePage() {
        return "response";
    }

    @GetMapping("/chats")
    public String chatPage() {
        return "chats";
    }

    @GetMapping("/profile")
    public String profilePage() {
        return "profile";
    }

    @GetMapping("/homeem")
    public String homeemployerPage() {
        return "homeem";
    }

    @GetMapping("/responseem")
    public String responseemployerPage() {
        return "responseem";
    }

    @GetMapping("/chatsem")
    public String chatemployerPage() {
        return "chatsem";
    }

    @GetMapping("/profileem")
    public String profileemployerPage() {
        return "profileem";
    }

    @GetMapping("/vacancy")
    public String vacancyPage() {
        return "vacancy";
    }

    @GetMapping("/information")
    public String informationPage() {
        return "information";
    }
    @GetMapping("/enter-code")
    public String enterCodePage() {
        return "enter-code";
    }

    @GetMapping("/chat/{id}")
    public String openChatPage(@PathVariable Long id, Model model) {
        model.addAttribute("chatId", id);
        return "chat";
    }

}

package com.jobportal.controller;


import com.jobportal.model.Resumes;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
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

    @GetMapping("/chat")
    public String chatPage() {
        return "chat";
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

    @GetMapping("/chatem")
    public String chatemployerPage() {
        return "chatem";
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

}

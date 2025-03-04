package com.vladislav.nto_webapi.controllers.Site;

import lombok.extern.java.Log;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;

@Controller
@RequestMapping
public class HomeController {

    @GetMapping("/home")
    public String home() {
        return "home/index.html";
    }

    @GetMapping("/book")
    public String book(Model model) {
        model.addAttribute("title", "Book");
        return "booking/booking.html";
    }

    @GetMapping("/order")
    public String order(Model model) {
        return "order/order.html";
    }
}

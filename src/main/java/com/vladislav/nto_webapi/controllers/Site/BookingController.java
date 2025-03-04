package com.vladislav.nto_webapi.controllers.Site;

import com.vladislav.nto_webapi.Schemes.Requests.Seat;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/booking")
public class BookingController {

    @GetMapping
    public String successBook() {
        return "book/order.html";
    }

    @PostMapping
    public void book(@RequestBody Seat seat) {

    }
}

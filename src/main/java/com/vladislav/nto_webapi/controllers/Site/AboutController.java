package com.vladislav.nto_webapi.controllers.Site;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("about")
class AboutController {

  @GetMapping
  public String about() {
    return "laba.html";
  } 
}

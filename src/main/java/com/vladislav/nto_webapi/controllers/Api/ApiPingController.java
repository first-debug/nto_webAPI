package com.vladislav.nto_webapi.controllers.Api;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("api/ping")
public class ApiPingController {
    @GetMapping
    @ResponseStatus(HttpStatus.OK)
    public void ping() {
    }
}
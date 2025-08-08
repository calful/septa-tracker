package com.calful.septatracker;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

// @RestController tells Spring: "This class handles web requests and returns data, not HTML."
@RestController
public class HelloController {

    // @GetMapping tells Spring: "If someone does a GET request to /hello, call this method."
    @GetMapping("/hello")
    public String sayHello() {
        // This String is sent back as the HTTP response body
        return "Hello from SEPTA Tracker!";
    }
}

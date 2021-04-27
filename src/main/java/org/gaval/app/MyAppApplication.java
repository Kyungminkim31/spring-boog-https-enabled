package org.gaval.app;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@SpringBootApplication
public class MyAppApplication {
    public static void main (String [] args){
        SpringApplication.run(MyAppApplication.class, args);
    }
}

@RestController
class WelcomeController {
    @GetMapping(path="/welcome")
    public ResponseEntity<String> getWelcome(){
        return ResponseEntity.ok("Hello, HTTPs WORLD");
    }
}

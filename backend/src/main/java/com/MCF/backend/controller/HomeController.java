package com.MCF.backend.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * [Brief one-sentence description of what this class does.
 *
 * @author Drew "Dr.C" Clinkenbeard
 * @oversion 0.1.0
 * @since
 */
@RestController
public class HomeController {

    @GetMapping("/")
    public String home() {
        return "Backend is running";
    }
}

package com.qrupload.wedding.controller;

import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/photos")
public class PhotoController {

    @GetMapping
    public String getPhotos(Authentication auth){
        return "Juten Tag " + auth.getName() + " - hier sind deine Fotos";
    }
}

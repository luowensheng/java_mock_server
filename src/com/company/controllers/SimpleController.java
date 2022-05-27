package com.company.controllers;

import com.company.annotations.*;


@Controller
public class SimpleController {

    @GetMapping(value="/index")
    public String index(){

        return "Hello Index";
    }

    @GetMapping(value="/home/users")
    public String home(@RequestParam String car){

        return "Hello Home and ? "+car;
    }
}

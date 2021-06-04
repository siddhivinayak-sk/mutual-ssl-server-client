package com.sk.server.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(value = "/poll")
public class PollController {
	
	@GetMapping()
	public String poll() {
		return "Yes";
	}
}
package com.heigraduate.app.graduate.controller;

import java.util.UUID;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@Controller
public class PageController {

  @GetMapping({"/", "/promotions"})
  public String promotions() {
    return "promotions";
  }

  @GetMapping("/promotions/{id}")
  public String promotionDetail(@PathVariable UUID id, Model model) {
    model.addAttribute("promotionId", id.toString());
    return "promotion-detail";
  }

  @GetMapping("/login")
  public String login() {
    return "login";
  }

  @GetMapping("/notes")
  public String notes() {
    return "notes";
  }

  @GetMapping("/releves")
  public String releves() {
    return "releves";
  }
}

package com.heigraduate.app.graduate.controller;

import java.util.UUID;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@Controller
public class WebViewController {

  @GetMapping(value = "/login", produces = MediaType.TEXT_HTML_VALUE)
  public String login() {
    return "login";
  }

  @GetMapping(value = "/promotions", produces = MediaType.TEXT_HTML_VALUE)
  public String promotions() {
    return "promotions";
  }

  @GetMapping(value = "/promotions/{promotionId}", produces = MediaType.TEXT_HTML_VALUE)
  public String promotionDetail(@PathVariable UUID promotionId, Model model) {
    model.addAttribute("promotionId", promotionId);
    return "promotion-detail";
  }

  @GetMapping(value = "/notes", produces = MediaType.TEXT_HTML_VALUE)
  public String notes() {
    return "notes";
  }

  @GetMapping(value = "/releves", produces = MediaType.TEXT_HTML_VALUE)
  public String releves() {
    return "releves";
  }
}

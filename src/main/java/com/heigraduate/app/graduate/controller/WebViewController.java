package com.heigraduate.app.graduate.controller;

import java.util.UUID;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

/**
 * Serves the Thymeleaf web UI (§16). These pages don't touch the database directly - they render a
 * static shell and let client-side JS (see /js/api.js) call the existing JSON API with the bearer
 * token stored at login. This keeps a single source of truth for the data (the REST API) instead of
 * duplicating query/mapping logic here.
 */
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
}

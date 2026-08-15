package com.heigraduate.app.graduate.controller;

import com.heigraduate.app.graduate.dto.GroupRequest;
import com.heigraduate.app.graduate.dto.GroupResponse;
import com.heigraduate.app.graduate.service.GroupService;
import jakarta.validation.Valid;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/groups")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class GroupController {

  private final GroupService groupService;

  @GetMapping
  public List<GroupResponse> findAll() {
    return groupService.findAll();
  }

  @GetMapping("/{id}")
  public GroupResponse findById(@PathVariable UUID id) {
    return groupService.findById(id);
  }

  @PostMapping
  @ResponseStatus(HttpStatus.CREATED)
  public GroupResponse create(@Valid @RequestBody GroupRequest request) {
    return groupService.create(request);
  }

  @PutMapping("/{id}")
  public GroupResponse update(@PathVariable UUID id, @Valid @RequestBody GroupRequest request) {
    return groupService.update(id, request);
  }

  @DeleteMapping("/{id}")
  @ResponseStatus(HttpStatus.NO_CONTENT)
  public void deactivate(@PathVariable UUID id) {
    groupService.deactivate(id);
  }
}

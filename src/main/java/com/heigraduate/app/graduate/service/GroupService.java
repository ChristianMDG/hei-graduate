package com.heigraduate.app.graduate.service;

import com.heigraduate.app.graduate.dto.GroupRequest;
import com.heigraduate.app.graduate.dto.GroupResponse;
import com.heigraduate.app.graduate.exception.ConflictException;
import com.heigraduate.app.graduate.exception.ResourceNotFoundException;
import com.heigraduate.app.graduate.mapper.GroupMapper;
import com.heigraduate.app.graduate.model.Group;
import com.heigraduate.app.graduate.repository.GroupRepository;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class GroupService {

  private final GroupRepository groupRepository;

  @Transactional(readOnly = true)
  public List<GroupResponse> findAll() {
    return groupRepository.findAll().stream().map(GroupMapper::toResponse).toList();
  }

  @Transactional(readOnly = true)
  public GroupResponse findById(UUID id) {
    return groupRepository
        .findById(id)
        .map(GroupMapper::toResponse)
        .orElseThrow(() -> new ResourceNotFoundException("Group not found with id: " + id));
  }

  @Transactional
  public GroupResponse create(GroupRequest request) {
    if (groupRepository.existsByReference(request.reference())) {
      throw new ConflictException(
          "A group with reference '" + request.reference() + "' already exists");
    }
    Group group =
        Group.builder()
            .reference(request.reference())
            .capacity(request.capacity())
            .active(true)
            .build();
    return GroupMapper.toResponse(groupRepository.save(group));
  }

  @Transactional
  public GroupResponse update(UUID id, GroupRequest request) {
    Group group =
        groupRepository
            .findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Group not found with id: " + id));
    group.setReference(request.reference());
    group.setCapacity(request.capacity());
    return GroupMapper.toResponse(groupRepository.save(group));
  }

  @Transactional
  public void deactivate(UUID id) {
    Group group =
        groupRepository
            .findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Group not found with id: " + id));
    group.setActive(false);
    groupRepository.save(group);
  }
}

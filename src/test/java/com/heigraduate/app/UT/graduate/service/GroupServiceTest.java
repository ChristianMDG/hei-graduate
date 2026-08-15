package com.heigraduate.app.graduate.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import com.heigraduate.app.graduate.dto.GroupRequest;
import com.heigraduate.app.graduate.dto.GroupResponse;
import com.heigraduate.app.graduate.exception.ConflictException;
import com.heigraduate.app.graduate.exception.ResourceNotFoundException;
import com.heigraduate.app.graduate.model.Group;
import com.heigraduate.app.graduate.repository.GroupRepository;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class GroupServiceTest {

  @Mock private GroupRepository groupRepository;

  @InjectMocks private GroupService groupService;

  private Group group;
  private UUID groupId;

  @BeforeEach
  void setUp() {
    groupId = UUID.randomUUID();
    group = Group.builder().id(groupId).reference("K1").capacity(30).active(true).build();
  }

  @Test
  void findAll_shouldReturnAllGroups() {
    when(groupRepository.findAll()).thenReturn(List.of(group));

    List<GroupResponse> result = groupService.findAll();

    assertThat(result).hasSize(1);
    assertThat(result.get(0).reference()).isEqualTo("K1");
  }

  @Test
  void findById_shouldReturnGroup_whenExists() {
    when(groupRepository.findById(groupId)).thenReturn(Optional.of(group));

    GroupResponse result = groupService.findById(groupId);

    assertThat(result.id()).isEqualTo(groupId);
    assertThat(result.reference()).isEqualTo("K1");
  }

  @Test
  void findById_shouldThrow_whenNotFound() {
    UUID unknownId = UUID.randomUUID();
    when(groupRepository.findById(unknownId)).thenReturn(Optional.empty());

    assertThatThrownBy(() -> groupService.findById(unknownId))
        .isInstanceOf(ResourceNotFoundException.class)
        .hasMessageContaining(unknownId.toString());
  }

  @Test
  void create_shouldSaveGroup_whenReferenceIsUnique() {
    GroupRequest request = new GroupRequest("K2", 25);
    when(groupRepository.existsByReference("K2")).thenReturn(false);
    when(groupRepository.save(any(Group.class)))
        .thenAnswer(invocation -> invocation.getArgument(0));

    GroupResponse result = groupService.create(request);

    assertThat(result.reference()).isEqualTo("K2");
    assertThat(result.capacity()).isEqualTo(25);
    assertThat(result.active()).isTrue();
    verify(groupRepository).save(any(Group.class));
  }

  @Test
  void create_shouldThrowConflict_whenReferenceAlreadyExists() {
    GroupRequest request = new GroupRequest("K1", 30);
    when(groupRepository.existsByReference("K1")).thenReturn(true);

    assertThatThrownBy(() -> groupService.create(request))
        .isInstanceOf(ConflictException.class)
        .hasMessageContaining("K1");

    verify(groupRepository, never()).save(any());
  }

  @Test
  void update_shouldModifyExistingGroup() {
    GroupRequest request = new GroupRequest("K1-BIS", 35);
    when(groupRepository.findById(groupId)).thenReturn(Optional.of(group));
    when(groupRepository.save(any(Group.class)))
        .thenAnswer(invocation -> invocation.getArgument(0));

    GroupResponse result = groupService.update(groupId, request);

    assertThat(result.reference()).isEqualTo("K1-BIS");
    assertThat(result.capacity()).isEqualTo(35);
  }

  @Test
  void update_shouldThrow_whenGroupNotFound() {
    UUID unknownId = UUID.randomUUID();
    GroupRequest request = new GroupRequest("K1", 30);
    when(groupRepository.findById(unknownId)).thenReturn(Optional.empty());

    assertThatThrownBy(() -> groupService.update(unknownId, request))
        .isInstanceOf(ResourceNotFoundException.class);

    verify(groupRepository, never()).save(any());
  }

  @Test
  void deactivate_shouldSetActiveToFalse() {
    when(groupRepository.findById(groupId)).thenReturn(Optional.of(group));
    when(groupRepository.save(any(Group.class)))
        .thenAnswer(invocation -> invocation.getArgument(0));

    groupService.deactivate(groupId);

    assertThat(group.getActive()).isFalse();
    verify(groupRepository).save(group);
  }

  @Test
  void deactivate_shouldThrow_whenGroupNotFound() {
    UUID unknownId = UUID.randomUUID();
    when(groupRepository.findById(unknownId)).thenReturn(Optional.empty());

    assertThatThrownBy(() -> groupService.deactivate(unknownId))
        .isInstanceOf(ResourceNotFoundException.class);
  }
}

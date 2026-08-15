package com.heigraduate.app.graduate.mapper;

import com.heigraduate.app.graduate.dto.GroupResponse;
import com.heigraduate.app.graduate.model.Group;

public class GroupMapper {

  private GroupMapper() {}

  public static GroupResponse toResponse(Group group) {
    return new GroupResponse(
        group.getId(), group.getReference(), group.getCapacity(), group.getActive());
  }
}

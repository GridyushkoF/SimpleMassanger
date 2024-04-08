package net.study.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
@AllArgsConstructor
@NoArgsConstructor
public @Data class MessageIdListDto {
    private List<Integer> messageIdList;
}

package ru.practicum.ewm.request.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;

@Data
@AllArgsConstructor
public class EventRequestStatusUpdateResult {
    private List<RequestDto> confirmedRequests;
    private List<RequestDto> rejectedRequests;

    public static EventRequestStatusUpdateResult empty() {
        return new EventRequestStatusUpdateResult(List.of(), List.of());
    }
}

package ru.practicum.ewm.request.dto;


import lombok.AllArgsConstructor;
import lombok.Data;
import ru.practicum.ewm.request.model.RequestStatus;

@Data
@AllArgsConstructor
public class RequestDto {
    private Long id;
    private Long event;
    private Long requester;
    private RequestStatus status;
    private String created;
}

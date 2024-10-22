package ru.practicum.ewm.client;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.apache.tomcat.util.buf.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.util.DefaultUriBuilderFactory;
import ru.practicum.ewm.dto.GetStatsRequest;
import ru.practicum.ewm.dto.NewEndpointHitRequestDto;
import ru.practicum.ewm.dto.ViewStatsDto;
import ru.practicum.ewm.exception.InternalServerException;
import ru.practicum.ewm.utils.DateMapper;

import java.util.List;
import java.util.Map;

@Slf4j
@Service
public class StatsClient extends BaseClient {
    private static final String API_PREFIX = "";
    private final DateMapper dateMapper;
    private final ObjectMapper objectMapper;

    @Autowired
    public StatsClient(
            @Value("${stats-server.url}") String serverUrl,
            RestTemplateBuilder builder,
            DateMapper dateMapper,
            ObjectMapper objectMapper
    ) {
        super(
                builder
                        .uriTemplateHandler(new DefaultUriBuilderFactory(serverUrl + API_PREFIX))
                        .requestFactory(() -> new HttpComponentsClientHttpRequestFactory())
                        .build()
        );
        this.dateMapper = dateMapper;
        this.objectMapper = objectMapper;
    }

    public List<ViewStatsDto> getStats(GetStatsRequest request) {
        String start = dateMapper.toString(request.getStart());
        String end = dateMapper.toString(request.getEnd());

        Map<String, Object> parameters = Map.of(
                "start", start,
                "end", end,
                "uris", StringUtils.join(request.getUris(), ','),
                "unique", request.getUnique()
        );
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("/stats?start={start}&end={end}");
        if (!request.getUris().isEmpty()) {
            stringBuilder.append("&uris={uris}");
        }
        stringBuilder.append("&unique={unique}");

        ResponseEntity<String> response = get(stringBuilder.toString(), parameters);
        String responseBody = getResponseBody(response);

        try {
            return objectMapper.readValue(responseBody, new TypeReference<>() {});
        } catch (Exception e) {
            throw createParseResponseException(responseBody, e);
        }
    }

    public ViewStatsDto create(NewEndpointHitRequestDto request) {
        ResponseEntity<String> response = post("/hit", request);
        String responseBody = getResponseBody(response);
        try {
            return objectMapper.readValue(responseBody, ViewStatsDto.class);
        } catch (Exception e) {
            throw createParseResponseException(responseBody, e);
        }
    }

    private String getResponseBody(ResponseEntity<String> response) {
        if (!response.getStatusCode().is2xxSuccessful()) {
            String message = String.format("Stats returned wrong status code %s", response.getStatusCode());
            log.error(message);
            throw new InternalServerException(message);
        }
        return response.getBody();
    }

    private InternalServerException createParseResponseException(String responseBody, Exception exception) {
        String message = String.format("Could not parse request body %s", responseBody);
        log.error(message, exception);
        return new InternalServerException(message);
    }
}
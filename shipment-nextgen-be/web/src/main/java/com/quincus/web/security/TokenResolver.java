package com.quincus.web.security;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.io.InputStream;
import java.util.Optional;

@Component
public class TokenResolver {

    private static final String AUTHORIZATION_HEADER = "Authorization";
    private static final String BEARER = "Bearer ";
    private static final String X_API_AUTHORIZATION_HEADER = "X-API-AUTHORIZATION";
    private static final String ORGANIZATION_ID_HEADER = "X-ORGANISATION-ID";
    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();
    public static final String USER_ID = "user_id";
    public static final String DATA = "data";


    public String resolveToken(HttpServletRequest request) {
        String bearerToken = request.getHeader(AUTHORIZATION_HEADER);
        if (bearerToken != null && bearerToken.startsWith(BEARER)) {
            return bearerToken.substring(7);
        }
        return null;
    }

    public String resolveS2SToken(HttpServletRequest request) {
        return request.getHeader(X_API_AUTHORIZATION_HEADER);
    }

    public String resolveOrganizationId(HttpServletRequest request) {
        return request.getHeader(ORGANIZATION_ID_HEADER);
    }

    public String resolveUserId(HttpServletRequest request) {
        try (InputStream inputStream = request.getInputStream()) {
            JsonNode rootJsonNode = OBJECT_MAPPER.readTree(inputStream);
            return Optional.ofNullable(rootJsonNode)
                    .map(rootNode -> rootNode.get(DATA))
                    .map(node -> node.get(USER_ID))
                    .map(JsonNode::textValue)
                    .orElse(StringUtils.EMPTY);
        } catch (IOException e) {
            // do nothing
        }
        return null;
    }


}

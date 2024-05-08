package com.quincus.web.common.web;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.jayway.jsonpath.JsonPath;
import com.quincus.web.common.config.JacksonConfiguration;
import com.quincus.web.common.exception.CommonExceptionHandler;
import com.quincus.web.common.model.Request;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.RequestBuilder;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.setup.DefaultMockMvcBuilder;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.test.web.servlet.setup.MockMvcConfigurer;
import org.springframework.web.context.WebApplicationContext;

import java.io.UnsupportedEncodingException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;


@ExtendWith({MockitoExtension.class})
@ContextConfiguration(classes = {
        CommonExceptionHandler.class,
        JacksonConfiguration.class
})
@EnableAutoConfiguration(exclude = SecurityAutoConfiguration.class)
public class BaseControllerWebIT {

    @Autowired
    protected MockMvc mvc;
    @Autowired
    protected WebApplicationContext context;
    @Autowired
    protected ObjectMapper objectMapper;

    @BeforeEach
    void setUpMvcContext() {
        DefaultMockMvcBuilder mockMvcBuilder = MockMvcBuilders
                .webAppContextSetup(context)
                .alwaysDo(print());

        final MockMvcConfigurer mmc = applySpringSecurity();
        if (mmc != null) {
            mockMvcBuilder.apply(mmc);
        }
        mvc = mockMvcBuilder.build();
    }

    protected MockMvcConfigurer applySpringSecurity() {
        return null;
    }

    protected MvcResult performGetRequest(String url) throws Exception {
        return mvc.perform(buildRequest(HttpMethod.GET, url, null)).andReturn();
    }

    protected MvcResult performDeleteRequest(String url) throws Exception {
        return mvc.perform(buildRequest(HttpMethod.DELETE, url, null)).andReturn();
    }

    protected <T> MvcResult performPostRequest(String url, Request<T> payload) throws Exception {
        return mvc.perform(buildRequest(HttpMethod.POST, url, objectMapper.writeValueAsString(payload))).andReturn();
    }

    protected MvcResult performPostRequest(String url, String payload) throws Exception {
        return mvc.perform(buildRequest(HttpMethod.POST, url, payload)).andReturn();
    }

    protected MvcResult performPostFileRequest(String url, String multipartName, String fileContents) throws Exception {
        return mvc.perform(buildMultipartRequest(HttpMethod.POST, url, multipartName, fileContents)).andReturn();
    }

    protected <T> MvcResult performPutRequest(String url, Request<T> payload) throws Exception {
        return mvc.perform(buildRequest(HttpMethod.PUT, url, objectMapper.writeValueAsString(payload))).andReturn();
    }

    protected MvcResult performPutRequest(String url, String payload) throws Exception {
        return mvc.perform(buildRequest(HttpMethod.PUT, url, payload)).andReturn();
    }

    protected MvcResult performPatchRequest(String url) throws Exception {
        return mvc.perform(buildRequest(HttpMethod.PATCH, url, null)).andReturn();
    }

    protected  <T> MvcResult performPatchRequest(String url,  Request<T> payload) throws Exception {
        return mvc.perform(buildRequest(HttpMethod.PATCH, url, objectMapper.writeValueAsString(payload))).andReturn();
    }

    private RequestBuilder buildRequest(HttpMethod method, String url, String payload) {
        final MockHttpServletRequestBuilder builder = MockMvcRequestBuilders.request(method, url)
                .with(SecurityMockMvcRequestPostProcessors.csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON);
        if (payload != null) {
            builder.content(payload);
        }
        return builder;
    }

    private RequestBuilder buildMultipartRequest(HttpMethod method, String url, String multipartName, String fileContent) {
        MockMultipartFile mockFile = new MockMultipartFile(multipartName, "a-file", "application/json", fileContent.getBytes());
        return MockMvcRequestBuilders.multipart(method, url)
                .file(mockFile)
                .with(SecurityMockMvcRequestPostProcessors.csrf());
    }

    protected void assertThatErrorsContainMessages(
            final MvcResult actualResult,
            final String[] expectedErrorMessages,
            final int expectedLength)
            throws UnsupportedEncodingException {
        final String responseContent = actualResult.getResponse().getContentAsString();

        assertThatHttpStatusIsExpected(actualResult, HttpStatus.BAD_REQUEST);
        assertThat(JsonPath.parse(responseContent).read("$.errors", String[].class)) // NOSONAR
                .contains(expectedErrorMessages);
        assertThat(JsonPath.parse(responseContent).read("$.errors.length()", Integer.class)) // NOSONAR
                .isEqualTo(expectedLength);
    }

    protected void assertThatHttpStatusIsExpected(final MvcResult actualResult, final HttpStatus expectedHttpStatus) {
        assertThat(actualResult.getResponse().getStatus()).isEqualTo(expectedHttpStatus.value()); // NOSONAR
    }

}

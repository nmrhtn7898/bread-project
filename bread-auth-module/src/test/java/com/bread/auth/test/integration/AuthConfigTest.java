package com.bread.auth.test.integration;

import com.bread.auth.base.AbstractIntegrationTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.test.web.servlet.ResultActions;

import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.restdocs.headers.HeaderDocumentation.*;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.document;
import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.post;
import static org.springframework.restdocs.payload.PayloadDocumentation.fieldWithPath;
import static org.springframework.restdocs.payload.PayloadDocumentation.responseFields;
import static org.springframework.restdocs.request.RequestDocumentation.parameterWithName;
import static org.springframework.restdocs.request.RequestDocumentation.requestParameters;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.httpBasic;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

public class AuthConfigTest extends AbstractIntegrationTest {

    @Test
    @DisplayName("인증 서버 토큰 유효성 검증 성공 200")
    public void checkToken_200() throws Exception {
        mockMvc
                .perform(
                        post("/oauth/check_token")
                                .accept(APPLICATION_JSON)
                                .queryParam("token", getTokenPasswordGrant())
                                .with(httpBasic("test", "1234"))
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("aud").exists())
                .andExpect(jsonPath("user_name").exists())
                .andExpect(jsonPath("scope").exists())
                .andExpect(jsonPath("active").exists())
                .andExpect(jsonPath("exp").exists())
                .andExpect(jsonPath("authorities").exists())
                .andExpect(jsonPath("jti").exists())
                .andExpect(jsonPath("client_id").exists())
                .andDo(print())
                .andDo(
                        document(
                                "check-token",
                                requestHeaders(
                                        headerWithName(HttpHeaders.AUTHORIZATION).description("클라이언트 ID/SECRET 인코딩 값")
                                ),
                                requestParameters(
                                        parameterWithName("token").description("인증 토큰")
                                ),
                                responseHeaders(
                                        headerWithName(HttpHeaders.CONTENT_TYPE).description("응답 본문 타입")
                                ),
                                responseFields(
                                        fieldWithPath("aud").description("토큰 발행자"),
                                        fieldWithPath("user_name").description("사용자 아이디"),
                                        fieldWithPath("scope").description("토큰의 접근 범위"),
                                        fieldWithPath("active").description("토큰 유효 여부"),
                                        fieldWithPath("exp").description("토큰 만료 시간"),
                                        fieldWithPath("authorities").description("토큰의 접근 권한"),
                                        fieldWithPath("jti").description("토큰의 고유 식별자"),
                                        fieldWithPath("client_id").description("클라이언트 ID")
                                )
                        )
                );
    }

    @Test
    @DisplayName("인증 서버 토큰 유효성 검증 토큰 잘못된 경우 실패 400")
    public void checkToken_400() throws Exception {
        mockMvc
                .perform(
                        post("/oauth/check_token")
                                .accept(APPLICATION_JSON)
                                .queryParam("token", "invalid token")
                                .with(httpBasic("test", "1234"))
                )
                .andExpect(status().isBadRequest())
                .andDo(print());
    }

    @Test
    @DisplayName("인증 서버 토큰 유효성 검증 토큰 만료된 경우 실패 400")
    public void checkToken_Expired_400() throws Exception {
        mockMvc
                .perform(
                        post("/oauth/check_token")
                                .accept(APPLICATION_JSON)
                                .queryParam("token", "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJhdWQiOlsiYXV0aCJdLCJ1c2VyX25hbWUiOiJ1c2VyIiwic2NvcGUiOlsicmVhZCJdLCJleHAiOjE1MDM3ODQ3NzksImF1dGhvcml0aWVzIjpbInVzZXIiXSwianRpIjoiMWIxNmU4MGEtZWU0OS00ODFkLTk3ZGItN2U5NmNjOWI5OTA5IiwiY2xpZW50X2lkIjoidGVzdCJ9.oSqltl_AncyFdnFBj77NjdxyG88xmDBXQnjZYy0XHgk")
                                .with(httpBasic("test", "1234"))
                )
                .andExpect(status().isBadRequest())
                .andDo(print());
    }

    @Test
    @DisplayName("인증 서버 토큰 유효성 검증 클라이언트 정보 잘못된 경우 실패 401")
    public void checkToken_401() throws Exception {
        mockMvc
                .perform(
                        post("/oauth/check_token")
                                .accept(APPLICATION_JSON)
                                .queryParam("token", getTokenPasswordGrant())
                                .with(httpBasic("invalid clientId", "invalid clientSecret"))
                )
                .andExpect(status().isUnauthorized())
                .andDo(print());
    }

    @Test
    @DisplayName("인증 서버 PASSWORD 방식 토큰 발급 성공 200")
    public void getToken_PasswordGrant_200() throws Exception {
        getTokenPasswordGrantResponse()
                .andExpect(status().isOk())
                .andExpect(jsonPath("access_token").exists())
                .andExpect(jsonPath("refresh_token").exists())
                .andExpect(jsonPath("token_type").value("bearer"))
                .andExpect(jsonPath("expires_in").exists())
                .andExpect(jsonPath("scope").exists())
                .andExpect(jsonPath("jti").exists())
                .andDo(print())
                .andDo(
                        document(
                                "token-password-grant",
                                requestHeaders(
                                        headerWithName(HttpHeaders.AUTHORIZATION).description("클라이언트 ID/SECRET 인코딩 값")
                                ),
                                requestParameters(
                                        parameterWithName("username").description("사용자 아이디"),
                                        parameterWithName("password").description("사용자 패스워드"),
                                        parameterWithName("grant_type").description("인증 토큰 발급 방식"),
                                        parameterWithName("scope").description("토큰의 접근 범위").optional()
                                ),
                                responseHeaders(
                                        headerWithName(HttpHeaders.CONTENT_TYPE).description("응답 본문 타입")
                                ),
                                responseFields(
                                        fieldWithPath("access_token").description("인증 토큰"),
                                        fieldWithPath("refresh_token").description("재발급 토큰"),
                                        fieldWithPath("token_type").description("토큰 타입"),
                                        fieldWithPath("expires_in").description("토큰 유효 시간, 초 단위"),
                                        fieldWithPath("scope").description("토큰의 접근 범위"),
                                        fieldWithPath("jti").description("토큰의 고유 식별자")
                                )
                        )
                );
    }

    @Test
    @DisplayName("인증 서버 PASSWORD 방식 토큰 발급 사용자 정보 잘못된 경우 실패 400")
    public void getToken_PasswordGrant_400() throws Exception {
        mockMvc
                .perform(
                        post("/oauth/token")
                                .accept(APPLICATION_JSON)
                                .queryParam("username", "invalid user")
                                .queryParam("password", "invalid password")
                                .queryParam("grant_type", "password")
                                .queryParam("scope", "read")
                                .with(httpBasic("test", "1234"))
                )
                .andExpect(status().isBadRequest())
                .andDo(print());
    }

    @Test
    @DisplayName("인증 서버 PASSWORD 방식 토큰 발급 클라이언트 정보 잘못된 경우 실패 401")
    public void getToken_PasswordGrant_401() throws Exception {
        mockMvc
                .perform(
                        post("/oauth/token")
                                .accept(APPLICATION_JSON)
                                .queryParam("username", "user")
                                .queryParam("password", "user")
                                .queryParam("grant_type", "password")
                                .queryParam("scope", "read")
                                .with(httpBasic("invalid clientId", "invalid clientSecret"))
                )
                .andExpect(status().isUnauthorized())
                .andDo(print());
    }

    @Test
    @DisplayName("인증 서버 REFRESH TOKEN 방식 토큰 발급 성공 200")
    public void getToken_RefreshTokenGrant_200() throws Exception {
        getTokenRefreshTokenGrantResponse()
                .andExpect(status().isOk())
                .andExpect(jsonPath("access_token").exists())
                .andExpect(jsonPath("refresh_token").exists())
                .andExpect(jsonPath("token_type").value("bearer"))
                .andExpect(jsonPath("expires_in").exists())
                .andExpect(jsonPath("scope").exists())
                .andExpect(jsonPath("jti").exists())
                .andDo(print())
                .andDo(
                        document(
                                "token-refresh-token-grant",
                                requestHeaders(
                                        headerWithName(HttpHeaders.AUTHORIZATION).description("클라이언트 ID/SECRET 인코딩 값")
                                ),
                                requestParameters(
                                        parameterWithName("refresh_token").description("재발급 토큰"),
                                        parameterWithName("grant_type").description("인증 토큰 발급 방식"),
                                        parameterWithName("scope").description("토큰의 접근 범위").optional()
                                ),
                                responseHeaders(
                                        headerWithName(HttpHeaders.CONTENT_TYPE).description("응답 본문 타입")
                                ),
                                responseFields(
                                        fieldWithPath("access_token").description("인증 토큰"),
                                        fieldWithPath("refresh_token").description("재발급 토큰"),
                                        fieldWithPath("token_type").description("토큰 타입"),
                                        fieldWithPath("expires_in").description("토큰 유효 시간, 초 단위"),
                                        fieldWithPath("scope").description("토큰의 접근 범위"),
                                        fieldWithPath("jti").description("토큰의 고유 식별자")
                                )
                        )
                );
    }

    @Test
    @DisplayName("인증 서버 REFRESH TOKEN 방식 토큰 발급 재발급 토큰 기한 만료된 경우 실패 400")
    public void getToken_RefreshTokenGrant_Expired_400() throws Exception {
        mockMvc
                .perform(
                        post("/oauth/token")
                                .accept(APPLICATION_JSON)
                                .queryParam("refresh_token", "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJhdWQiOlsiYXV0aCJdLCJ1c2VyX25hbWUiOiJ1c2VyIiwic2NvcGUiOlsicmVhZCJdLCJhdGkiOiIyNTNhNmMxMC0wYTM3LTQyODktODRiNy01OGI3MjVjNWUyNzUiLCJleHAiOjE1MDQ2MjEwOTcsImF1dGhvcml0aWVzIjpbInVzZXIiXSwianRpIjoiZWVhZTIyYTAtNWJmZS00ODA2LTg0MGMtODU2NTAzYTNlNjBhIiwiY2xpZW50X2lkIjoidGVzdCJ9.VlIahUnYKyMdXVkcL9uhcw7QxWzJdGm8n5y5zbqpyAs")
                                .queryParam("grant_type", "refresh_token")
                                .queryParam("scope", "read write")
                                .with(httpBasic("test", "1234"))
                )
                .andExpect(status().isUnauthorized())
                .andDo(print());
    }

    @Test
    @DisplayName("인증 서버 REFRESH TOKEN 방식 토큰 발급 재발급 토큰 정보 잘못된 경우 실패 400")
    public void getToken_RefreshTokenGrant_Invalid_401() throws Exception {
        mockMvc
                .perform(
                        post("/oauth/token")
                                .accept(APPLICATION_JSON)
                                .queryParam("refresh_token", "invalid refresh token")
                                .queryParam("grant_type", "refresh_token")
                                .queryParam("scope", "read write")
                                .with(httpBasic("test", "1234"))
                )
                .andExpect(status().isUnauthorized())
                .andDo(print());
    }

    @Test
    @DisplayName("인증 서버 REFRESH TOKEN 방식 토큰 발급 클라이언트 정보 잘못된 경우 실패 401")
    public void getToken_RefreshTokenGrant_401() throws Exception {
        mockMvc
                .perform(
                        post("/oauth/token")
                                .accept(APPLICATION_JSON)
                                .queryParam("refresh_token", getRefreshTokenGrant())
                                .queryParam("grant_type", "refresh_token")
                                .queryParam("scope", "read write")
                                .with(httpBasic("invalid clientId", "invalid clientSecret"))
                )
                .andExpect(status().isUnauthorized())
                .andDo(print());
    }

    private ResultActions getTokenPasswordGrantResponse() throws Exception {
        return mockMvc
                .perform(
                        post("/oauth/token")
                                .accept(APPLICATION_JSON)
                                .queryParam("username", "user")
                                .queryParam("password", "user")
                                .queryParam("grant_type", "password")
                                .queryParam("scope", "read write")
                                .with(httpBasic("test", "1234"))
                );
    }

    private ResultActions getTokenRefreshTokenGrantResponse() throws Exception {
        return mockMvc
                .perform(
                        post("/oauth/token")
                                .accept(APPLICATION_JSON)
                                .queryParam("refresh_token", getRefreshTokenGrant())
                                .queryParam("grant_type", "refresh_token")
                                .queryParam("scope", "read write")
                                .with(httpBasic("test", "1234"))
                );
    }

    private String getTokenPasswordGrant() throws Exception {
        String responseBody = getTokenPasswordGrantResponse()
                .andReturn()
                .getResponse()
                .getContentAsString();
        return (String) parser
                .parseMap(responseBody)
                .get("access_token");
    }

    private String getRefreshTokenGrant() throws Exception {
        String responseBody = getTokenPasswordGrantResponse()
                .andReturn()
                .getResponse()
                .getContentAsString();
        return (String) parser
                .parseMap(responseBody)
                .get("refresh_token");
    }

}
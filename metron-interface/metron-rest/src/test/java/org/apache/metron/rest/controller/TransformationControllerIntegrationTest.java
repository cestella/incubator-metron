/**
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.metron.rest.controller;

import org.adrianwalker.multilinestring.Multiline;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import static org.hamcrest.Matchers.greaterThan;
import static org.hamcrest.Matchers.hasSize;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.httpBasic;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment= SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
public class TransformationControllerIntegrationTest {

    private String valid = "TO_LOWER(test)";
    private String invalid = "BAD_FUNCTION(test)";
    private String rulesJson = "[\"" + valid + "\",\"" + invalid + "\"]";

    /**
     {
     "sensorParserConfig": { "fieldTransformations" : [{"transformation" : "STELLAR","output" : ["url_host"],"config" : {"url_host" : "TO_LOWER(URL_TO_HOST(url))"}}]},
     "sampleData": {"url": "https://caseystella.com/blog"}
     }
     */
    @Multiline
    public static String sensorParseConfigJson;


    @Autowired
    private WebApplicationContext wac;

    private MockMvc mockMvc;

    private String user = "user";
    private String password = "password";

    @Before
    public void setup() throws Exception {
        this.mockMvc = MockMvcBuilders.webAppContextSetup(this.wac).apply(springSecurity()).build();
    }

    @Test
    public void testSecurity() throws Exception {
        this.mockMvc.perform(post("/api/v1/transformation/validate/rules").with(csrf()).contentType(MediaType.parseMediaType("application/json;charset=UTF-8")).content(rulesJson))
                .andExpect(status().isUnauthorized());

        this.mockMvc.perform(post("/api/v1/transformation/validate").with(csrf()).contentType(MediaType.parseMediaType("application/json;charset=UTF-8")).content(sensorParseConfigJson))
                .andExpect(status().isUnauthorized());

        this.mockMvc.perform(get("/api/v1/transformation/list"))
                .andExpect(status().isUnauthorized());

        this.mockMvc.perform(get("/api/v1/transformation/list/functions"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    public void test() throws Exception {
        this.mockMvc.perform(post("/api/v1/transformation/validate/rules").with(httpBasic(user,password)).with(csrf()).contentType(MediaType.parseMediaType("application/json;charset=UTF-8")).content(rulesJson))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.parseMediaType("application/json;charset=UTF-8")))
                .andExpect(jsonPath("$.['" + valid + "']").value(Boolean.TRUE))
                .andExpect(jsonPath("$.['" + invalid + "']").value(Boolean.FALSE));

        this.mockMvc.perform(post("/api/v1/transformation/validate").with(httpBasic(user,password)).with(csrf()).contentType(MediaType.parseMediaType("application/json;charset=UTF-8")).content(sensorParseConfigJson))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.parseMediaType("application/json;charset=UTF-8")))
                .andExpect(jsonPath("$.url").value("https://caseystella.com/blog"))
                .andExpect(jsonPath("$.url_host").value("caseystella.com"));

        this.mockMvc.perform(get("/api/v1/transformation/list").with(httpBasic(user,password)))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.parseMediaType("application/json;charset=UTF-8")))
                .andExpect(jsonPath("$", hasSize(greaterThan(0))));

        this.mockMvc.perform(get("/api/v1/transformation/list/functions").with(httpBasic(user,password)))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.parseMediaType("application/json;charset=UTF-8")))
                .andExpect(jsonPath("$", hasSize(greaterThan(0))));

        this.mockMvc.perform(get("/api/v1/transformation/list/simple/functions").with(httpBasic(user,password)))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.parseMediaType("application/json;charset=UTF-8")))
                .andExpect(jsonPath("$", hasSize(greaterThan(0))));
    }
}
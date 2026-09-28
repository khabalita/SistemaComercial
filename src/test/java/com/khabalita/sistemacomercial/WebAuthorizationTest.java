package com.khabalita.sistemacomercial;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

@SpringBootTest
@TestPropertySource(properties = "app.security.initial-admin-password=")
class WebAuthorizationTest {

    @Autowired
    private WebApplicationContext webApplicationContext;

    private MockMvc mockMvc;

    @org.junit.jupiter.api.BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext)
                .apply(springSecurity())
                .build();
    }

    @Test
    void anonymousUser_isRedirectedToLogin() throws Exception {
        mockMvc.perform(get("/ui/products"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/login"));
    }

    @Test
    void seller_cannotAccessAdminImport() throws Exception {
        mockMvc.perform(get("/ui/products/import")
                        .with(SecurityMockMvcRequestPostProcessors.user("seller").roles("VENDEDOR")))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/error/403"));
    }

    @Test
    void seller_canAccessSales() throws Exception {
        mockMvc.perform(get("/ui/sales")
                        .with(SecurityMockMvcRequestPostProcessors.user("seller").roles("VENDEDOR")))
                .andExpect(status().isOk())
                .andExpect(view().name("sales/list"))
                .andExpect(model().attribute("currentRole", "VENDEDOR"));
    }

    @Test
    void admin_canAccessImportAndIdentityIsExposed() throws Exception {
        mockMvc.perform(get("/ui/products/import")
                        .with(SecurityMockMvcRequestPostProcessors.user("admin-test").roles("ADMIN")))
                .andExpect(status().isOk())
                .andExpect(view().name("products/import"))
                .andExpect(model().attribute("currentUsername", "admin-test"))
                .andExpect(model().attribute("currentRole", "ADMIN"))
                .andExpect(model().attribute("isAdmin", true));
    }

    @Test
    void loginPage_isRendered() throws Exception {
        mockMvc.perform(get("/login"))
                .andExpect(status().isOk())
                .andExpect(view().name("login"));
    }

    @Test
    void authenticatedPost_withoutCsrfToken_isRejected() throws Exception {
        mockMvc.perform(post("/ui/sales/new")
                        .with(SecurityMockMvcRequestPostProcessors.user("seller").roles("VENDEDOR")))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/error/403"));
    }
}

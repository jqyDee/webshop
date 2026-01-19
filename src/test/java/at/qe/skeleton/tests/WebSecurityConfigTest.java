package at.qe.skeleton.tests;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

@SpringBootTest
@AutoConfigureMockMvc
class WebSecurityConfigTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void testAdminEndpointIsProtected() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.get("/api/admin/users"))
               .andExpect(MockMvcResultMatchers.status().isUnauthorized());
    }

    @Test
    @WithMockUser(authorities = "CUSTOMER")
    void testAdminEndpointForbiddenForCustomer() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.get("/api/admin/users"))
               .andExpect(MockMvcResultMatchers.status().isForbidden());
    }
}
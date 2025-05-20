package example.controller;

import com.example.entity.User;
import com.example.service.AdminBookingService;
import com.example.service.interfaces.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringBootConfiguration;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Bean;
import org.springframework.http.HttpStatus;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.server.ResponseStatusException;
import com.example.controller.AdminOperateUserInfoController;
import org.springframework.test.context.ContextConfiguration;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(AdminOperateUserInfoController.class)
@ContextConfiguration(classes = AdminOperateUserInfoControllerTest.TestConfig.class)
class AdminOperateUserInfoControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private UserService userService;

    @MockBean
    private AdminBookingService bookingService;

    private MockHttpSession session;

    @BeforeEach
    void setUp() {
        // Set up session with admin role
        session = new MockHttpSession();
        session.setAttribute("userId", 1);
        session.setAttribute("role", 1);  // Admin role
    }

    @Test
    void testGetAllUsers() throws Exception {
        // 创建一个真实的User对象列表，而不是mock对象
        User user = new User();
        List<User> users = Arrays.asList(user);
        when(userService.getAllUsers()).thenReturn(users);

        mockMvc.perform(get("/login/admin/operate_users")
                .session(session))
                .andExpect(status().isOk());

        verify(userService, times(1)).getAllUsers();
    }

    @Test
    void testGetUsersByRoleId() throws Exception {
        // 创建一个真实的User对象列表，而不是mock对象
        User user = new User();
        List<User> users = Arrays.asList(user);
        when(userService.getUsersByRoleId(1)).thenReturn(users);

        mockMvc.perform(get("/login/admin/operate_users/role/1")
                .session(session))
                .andExpect(status().isOk());

        verify(userService, times(1)).getUsersByRoleId(1);
    }

    @Test
    void testGetUsersByStatus() throws Exception {
        // 创建一个真实的User对象列表，而不是mock对象
        User user = new User();
        List<User> users = Arrays.asList(user);
        when(userService.getUsersByStatus(User.Validity.unblocked, User.Status.active))
                .thenReturn(users);

        mockMvc.perform(get("/login/admin/operate_users/status/unblocked/active")
                .session(session))
                .andExpect(status().isOk());

        verify(userService, times(1)).getUsersByStatus(User.Validity.unblocked, User.Status.active);
    }

    @Test
    void testSearchUsers() throws Exception {
        // 创建一个真实的User对象列表，而不是mock对象
        User user = new User();
        List<User> users = Arrays.asList(user);
        when(userService.searchUsers(anyInt(), anyString())).thenReturn(users);

        mockMvc.perform(get("/login/admin/operate_users/search")
                .param("userId", "1")
                .param("username", "testuser")
                .session(session))
                .andExpect(status().isOk());

        verify(userService, times(1)).searchUsers(anyInt(), anyString());
    }

    @Test
    void testDeleteUser_Success() throws Exception {
        when(userService.userExists(1)).thenReturn(true);

        mockMvc.perform(delete("/login/admin/operate_users/1")
                .session(session))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("User deleted successfully, ID: 1"));

        verify(userService, times(1)).userExists(1);
        verify(userService, times(1)).deleteUser(1);
    }

    @Test
    void testDeleteUser_Failure() throws Exception {
        when(userService.userExists(1)).thenReturn(false);

        mockMvc.perform(delete("/login/admin/operate_users/1")
                .session(session))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("User does not exist, ID: 1"));

        verify(userService, times(1)).userExists(1);
    }

    @Test
    void testExportUsersToCSV() throws Exception {
        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("message", "mock-csv-data");
        when(userService.exportUsersToCSV()).thenReturn(response);

        mockMvc.perform(get("/login/admin/operate_users/export/csv")
                .session(session))
                .andExpect(status().isOk())
                .andExpect(header().string("Content-Disposition", "attachment; filename=users.csv"))
                .andExpect(content().string("mock-csv-data"));

        verify(userService, times(1)).exportUsersToCSV();
    }

    @Test
    void testLogout() throws Exception {
        mockMvc.perform(post("/login/admin/operate_users/logout")
                .session(session))
                .andExpect(status().isOk())
                .andExpect(content().string("Logout successfully."));
    }

    @Test
    void testLockUserAccount_Success() throws Exception {
        when(userService.lockUserAccount(1)).thenReturn(true);

        mockMvc.perform(post("/login/admin/operate_users/1/lock")
                .session(session))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("User account locked successfully"));

        verify(userService, times(1)).lockUserAccount(1);
    }

    @Test
    void testLockUserAccount_Failure() throws Exception {
        when(userService.lockUserAccount(1)).thenReturn(false);

        mockMvc.perform(post("/login/admin/operate_users/1/lock")
                .session(session))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Lock user account failed, user may not exist"));

        verify(userService, times(1)).lockUserAccount(1);
    }

    @Test
    void testUnlockUserAccount_Success() throws Exception {
        when(userService.unlockUserAccount(1)).thenReturn(true);

        mockMvc.perform(post("/login/admin/operate_users/1/unlock")
                .session(session))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("User account unlocked successfully"));

        verify(userService, times(1)).unlockUserAccount(1);
    }

    @Test
    void testUnlockUserAccount_Failure() throws Exception {
        when(userService.unlockUserAccount(1)).thenReturn(false);

        mockMvc.perform(post("/login/admin/operate_users/1/unlock")
                .session(session))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Unlock user account failed, user may not exist"));

        verify(userService, times(1)).unlockUserAccount(1);
    }

    @SpringBootConfiguration
    @EnableAutoConfiguration
    static class TestConfig {
        @Bean
        public AdminOperateUserInfoController adminOperateUserInfoController(UserService userService, AdminBookingService bookingService) {
            return new AdminOperateUserInfoController(userService, bookingService);
        }
    }
}

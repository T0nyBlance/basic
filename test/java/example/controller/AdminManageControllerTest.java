package example.controller;
import com.example.controller.AdminManageController;
import com.example.entity.User;
import com.example.repository.AdminRepository;
import com.example.service.impl.AccountServiceImpl;
import com.example.service.impl.AdminServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.test.context.ContextConfiguration;
import com.meeting.MeetingApplication;

import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.hamcrest.Matchers.containsString;

// 使用普通测试扩展而非 WebMvcTest
@ExtendWith(MockitoExtension.class)
class AdminManageControllerTest {

    private MockMvc mockMvc;

    @Mock
    private AdminRepository adminRepository;

    @Mock
    private AdminServiceImpl adminService;

    @Mock
    private AccountServiceImpl accountService;

    @InjectMocks
    private AdminManageController adminManageController;

    private User user;
    
    private void setFieldValue(Object object, String fieldName, Object value) throws Exception {
        Field field = object.getClass().getDeclaredField(fieldName);
        field.setAccessible(true);
        field.set(object, value);
    }

    @BeforeEach
    void setUp() throws Exception {
        // 设置 MockMvc
        mockMvc = MockMvcBuilders.standaloneSetup(adminManageController).build();
        
        // 创建并设置模拟的用户对象
        user = new User();
        
        // 使用反射设置私有字段
        setFieldValue(user, "user_id", 1);
        setFieldValue(user, "username", "testuser");
        setFieldValue(user, "email", "test@example.com");
        setFieldValue(user, "phone_number", "1234567890");
        setFieldValue(user, "role_id", 2);
        setFieldValue(user, "end_user_status", User.Status.active);
        setFieldValue(user, "end_user_validity", User.Validity.unblocked);
    }

    @Test
    void testGetAllUsers_Success() throws Exception {
        // Arrange
        List<User> users = Arrays.asList(user);
        when(adminRepository.findAll()).thenReturn(users);

        // Act & Assert
        mockMvc.perform(get("/login/admin/users")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].user_id").value(1))
                .andExpect(jsonPath("$[0].username").value("testuser"));

        verify(adminRepository, times(1)).findAll();
    }

    @Test
    void testGetAllUsers_NoContent() throws Exception {
        // Arrange
        when(adminRepository.findAll()).thenReturn(Collections.emptyList());

        // Act & Assert
        mockMvc.perform(get("/login/admin/users")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNoContent());

        verify(adminRepository, times(1)).findAll();
    }

    @Test
    void testGetUsersByRoleId() throws Exception {
        // Arrange
        Integer roleId = 2;
        List<User> users = Arrays.asList(user);
        when(adminService.getUsersByRoleId(roleId)).thenReturn(users);

        // Act & Assert
        mockMvc.perform(get("/login/admin/users/role/{roleId}", roleId)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].user_id").value(1))
                .andExpect(jsonPath("$[0].username").value("testuser"));

        verify(adminService, times(1)).getUsersByRoleId(roleId);
    }

    @Test
    void testGetUsersByStatus() throws Exception {
        List<User> users = Arrays.asList(user);
        when(adminService.getUsersByStatus(User.Validity.unblocked, User.Status.active))
                .thenReturn(users);

        mockMvc.perform(get("/login/admin/users/status/unblocked/active")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].user_id").value(1))
                .andExpect(jsonPath("$[0].username").value("testuser"));

        verify(adminService, times(1)).getUsersByStatus(User.Validity.unblocked, User.Status.active);
    }

    @Test
    void testSearchUsers_ByUserId() throws Exception {
        // Arrange
        Integer userId = 1;
        List<User> users = Arrays.asList(user);
        when(adminService.searchUsers(userId, null)).thenReturn(users);

        // Act & Assert
        mockMvc.perform(get("/login/admin/users/search")
                .param("user_id", userId.toString())
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].user_id").value(1))
                .andExpect(jsonPath("$[0].username").value("testuser"));

        verify(adminService, times(1)).searchUsers(userId, null);
    }

    @Test
    void testSearchUsers_ByUsername() throws Exception {
        // Arrange
        String username = "testuser";
        List<User> users = Arrays.asList(user);
        when(adminService.searchUsers(null, username)).thenReturn(users);

        // Act & Assert
        mockMvc.perform(get("/login/admin/users/search")
                .param("username", username)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].user_id").value(1))
                .andExpect(jsonPath("$[0].username").value("testuser"));

        verify(adminService, times(1)).searchUsers(null, username);
    }

    @Test
    void testDeleteUser_Success() throws Exception {
        // Arrange
        Integer userId = 1;
        when(adminRepository.existsById(userId)).thenReturn(true);
        when(accountService.getRole(userId)).thenReturn(2); // Not an admin
        doNothing().when(adminRepository).deleteById(userId);

        // Act & Assert
        mockMvc.perform(delete("/login/admin/users/delete/{user_id}", userId)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().string("The user is deleted, ID: " + userId));

        verify(adminRepository, times(1)).existsById(userId);
        verify(accountService, times(1)).getRole(userId);
        verify(adminRepository, times(1)).deleteById(userId);
    }

    @Test
    void testDeleteUser_NotFound() throws Exception {
        // Arrange
        Integer userId = 1;
        when(adminRepository.existsById(userId)).thenReturn(false);

        // Act & Assert
        mockMvc.perform(delete("/login/admin/users/delete/{user_id}", userId)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound())
                .andExpect(content().string("The user does not exist, ID: " + userId));

        verify(adminRepository, times(1)).existsById(userId);
        verify(accountService, never()).getRole(anyInt());
        verify(adminRepository, never()).deleteById(anyInt());
    }

    @Test
    void testDeleteUser_Forbidden() throws Exception {
        // Arrange
        Integer userId = 1;
        when(adminRepository.existsById(userId)).thenReturn(true);
        when(accountService.getRole(userId)).thenReturn(1); // Admin

        // Act & Assert
        mockMvc.perform(delete("/login/admin/users/delete/{user_id}", userId)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isForbidden())
                .andExpect(content().string("You do not have permission to delete administrator."));

        verify(adminRepository, times(1)).existsById(userId);
        verify(accountService, times(1)).getRole(userId);
        verify(adminRepository, never()).deleteById(anyInt());
    }

    @Test
    void testExportUsersToCSV_Success() throws Exception {
        // Arrange
        List<User> users = Arrays.asList(user);
        when(adminRepository.findAll()).thenReturn(users);

        // Act & Assert
        mockMvc.perform(get("/login/admin/users/export/csv")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(header().string("Content-Disposition", "attachment; filename=users.csv"))
                .andExpect(header().string("Content-Type", containsString("text/csv")))
                .andExpect(content().string(containsString("User ID,Username,Email,Phone Number,Role,Activation Status,Lock Status")))
                .andExpect(content().string(containsString("1,testuser,test@example.com,1234567890,Regular User,Activated,Not Blocked")));

        verify(adminRepository, times(1)).findAll();
    }

    @Test
    void testExportUsersToCSV_Error() throws Exception {
        // Arrange
        when(adminRepository.findAll()).thenThrow(new RuntimeException("Database error"));

        // Act & Assert
        mockMvc.perform(get("/login/admin/users/export/csv")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isInternalServerError())
                .andExpect(content().string(containsString("Error exporting CSV: Database error")));

        verify(adminRepository, times(1)).findAll();
    }
}
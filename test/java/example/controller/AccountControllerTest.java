package example.controller;
import com.example.controller.AccountController;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpSession;
import com.example.dto.UserDTO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import com.example.dto.UserDTO;
import com.example.dto.ActivateAndResetDTO;
import com.example.entity.User;
import com.example.service.impl.AccountServiceImpl;
import com.example.service.impl.MailServiceImpl;
import com.example.controller.AccountController;
@ExtendWith(MockitoExtension.class)
class AccountControllerTest {

    @Mock
    private AccountServiceImpl accountService;

    @Mock
    private MailServiceImpl mailService;

    @Mock
    private HttpSession session;

    @InjectMocks
    private AccountController accountController;

    @Test
    void register_Success() {
        // Arrange
        UserDTO userDTO = UserDTO.builder()
            .email("test@example.com")
            .userId(123)
            .passwordHash("password")
            .build();
        
        when(accountService.register(userDTO)).thenReturn(true);
        
        // Act
        ResponseEntity<Map<String, Object>> response = accountController.register(userDTO, session);
        
        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertTrue((Boolean) response.getBody().get("success"));
        assertEquals("Email has been sent!", response.getBody().get("message"));
        verify(session).setAttribute("email", "test@example.com");
        verify(session).setAttribute("user_id", 123);
    }

    @Test
    void register_Failure() {
        // Arrange
        UserDTO userDTO = UserDTO.builder()
            .email("test@example.com")
            .build();
        
        when(accountService.register(userDTO)).thenReturn(false);
        
        // Act
        ResponseEntity<Map<String, Object>> response = accountController.register(userDTO, session);
        
        // Assert
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertFalse((Boolean) response.getBody().get("success"));
        assertEquals("Registration failed - password mismatch or email already exists", 
            response.getBody().get("message"));
    }

    @Test
    void resend_Success() {
        // Arrange
        when(session.getAttribute("user_id")).thenReturn(123);
        when(session.getAttribute("email")).thenReturn("test@example.com");
        when(mailService.generateActivationCode()).thenReturn("123456");
        
        // Act
        ResponseEntity<Map<String, Object>> response = accountController.resend(session);
        
        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertTrue((Boolean) response.getBody().get("success"));
        assertEquals("Email has been sent!", response.getBody().get("message"));
        verify(accountService).updateVerification("test@example.com", "123456");
    }

    @Test
    void resend_SessionExpired() {
        // Arrange
        when(session.getAttribute("user_id")).thenReturn(null);
        
        // Act
        ResponseEntity<Map<String, Object>> response = accountController.resend(session);
        
        // Assert
        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
        assertFalse((Boolean) response.getBody().get("success"));
        assertEquals("Session expired, please register again", response.getBody().get("message"));
    }

    @Test
    void activate_Success() {
        // Arrange
        ActivateAndResetDTO dto = new ActivateAndResetDTO();
        dto.setActivationCode("123456");
        
        when(session.getAttribute("email")).thenReturn("test@example.com");
        when(session.getAttribute("user_id")).thenReturn(123);
        when(accountService.activate("123456", "test@example.com", 123)).thenReturn(true);
        
        // Act
        ResponseEntity<Map<String, Object>> response = accountController.activicate(session, dto);
        
        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertTrue((Boolean) response.getBody().get("success"));
        assertEquals("Account has been activated!", response.getBody().get("message"));
    }

    @Test
    void login_UserSuccess() {
        // Arrange
        UserDTO userDTO = UserDTO.builder()
            .userId(123)
            .build();
        
        Map<String, Object> serviceResponse = new HashMap<>();
        serviceResponse.put("success", true);
        
        when(accountService.login(eq(userDTO), any())).thenReturn(true);
        when(accountService.getRole(eq(123))).thenReturn(2);
        
        // Act
        ResponseEntity<Map<String, Object>> response = accountController.login(userDTO, session);
        
        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertTrue((Boolean) response.getBody().get("success"));
        assertEquals("User login successful!", response.getBody().get("msg"));
        assertEquals("USER", response.getBody().get("role"));
        verify(session).setAttribute("userId", 123);
        verify(session).setAttribute("role", 2);
    }

    @Test
    void resetPassword_Success() {
        // Arrange
        when(accountService.sendResetEmail("test@example.com")).thenReturn(true);
        
        // Act
        ResponseEntity<Map<String, Object>> response = accountController.reset("test@example.com", session);
        
        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertTrue((Boolean) response.getBody().get("success"));
        assertEquals("Reset verification email has been sent!", response.getBody().get("message"));
    }

    @Test
    void verifyReset_Success() {
        // Arrange
        ActivateAndResetDTO dto = new ActivateAndResetDTO();
        dto.setActivationCode("123456");
        dto.setNewPassword("newPassword");
        
        when(accountService.resetPassword("123456", "newPassword")).thenReturn(true);
        
        // Act
        ResponseEntity<Map<String, Object>> response = accountController.verify(session, dto);
        
        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertTrue((Boolean) response.getBody().get("success"));
        assertEquals("Password has been reset!", response.getBody().get("message"));
    }

    @Test
    void showProfile_Success() {
        // Arrange
        User user = new User();
        user.setUser_id(123);
        user.setUsername("testuser");
        user.setEmail("test@example.com");
        user.setPhone_number("1234567890");
        user.setPhone_country_code("1");
        user.setAvatar_url("avatar.jpg");
        
        when(accountService.getCurrentUser(session)).thenReturn(user);
        
        // Act
        ResponseEntity<UserDTO> response = accountController.showProfile(session);
        
        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(123, response.getBody().getUserId());
        assertEquals("testuser", response.getBody().getUsername());
        assertEquals("test@example.com", response.getBody().getEmail());
    }

    @Test
    void logout_Success() {
        // Act
        ResponseEntity<String> response = accountController.logout(session);
        
        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("Logout successfully.", response.getBody());
        verify(session).invalidate();
    }

    @Test
    void getCurrentAccount_LoggedIn() {
        // Arrange
        when(session.getAttribute("userId")).thenReturn(123);
        when(session.getAttribute("role")).thenReturn(2);
        
        // Act
        Map<String, Object> result = accountController.getCurrentAccount(session);
        
        // Assert
        assertTrue((Boolean) result.get("success"));
        assertEquals(123, result.get("userId"));
        assertEquals(2, result.get("role"));
    }

    @Test
    void changePassword_Success() {
        // Arrange
        Map<String, String> params = new HashMap<>();
        params.put("currentPassword", "oldPass");
        params.put("newPassword", "newPass");
        
        when(session.getAttribute("userId")).thenReturn(123);
        when(accountService.changePassword(eq(123), eq("oldPass"), eq("newPass"), any()))
            .thenReturn(true);
        
        // Act
        Map<String, Object> result = accountController.changePassword(params, session);
        
        // Assert
        assertTrue((Boolean) result.get("success"));
        assertEquals("Password has been changed, please login again", result.get("message"));
        assertTrue((Boolean) result.get("needRelogin"));
        verify(session).invalidate();
    }
}
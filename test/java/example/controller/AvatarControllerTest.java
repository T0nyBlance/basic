package example.controller;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.util.Map;

import javax.servlet.http.HttpSession;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.multipart.MultipartFile;
import com.example.controller.AvatarController;
import com.example.service.impl.AccountServiceImpl;

@ExtendWith(MockitoExtension.class)
class AvatarControllerTest {

    @Mock
    private AccountServiceImpl accountService;

    @Mock
    private HttpSession session;

    @InjectMocks
    private AvatarController controller;

    private MultipartFile mockFile;

    @BeforeEach
    void setUp() {
        // 准备测试数据
        mockFile = new MockMultipartFile(
            "file", 
            "avatar.jpg", 
            "image/jpeg", 
            "test image content".getBytes()
        );
    }

    @Test
    void uploadAvatar_Success() {
        // Arrange
        when(session.getAttribute("userId")).thenReturn(1);
        when(accountService.updateAvatar(any(MultipartFile.class), eq(1)))
            .thenReturn("http://example.com/avatars/user1.jpg");
        
        // Act
        ResponseEntity<Map<String, Object>> response = controller.uploadAvatar(mockFile, session);
        
        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertTrue((Boolean) response.getBody().get("success"));
        assertEquals("Avatar uploaded successfully", response.getBody().get("message"));
        assertEquals("http://example.com/avatars/user1.jpg", response.getBody().get("avatarUrl"));
    }
    
    @Test
    void uploadAvatar_NotLoggedIn() {
        // Arrange
        when(session.getAttribute("userId")).thenReturn(null);
        
        // Act
        ResponseEntity<Map<String, Object>> response = controller.uploadAvatar(mockFile, session);
        
        // Assert
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertFalse((Boolean) response.getBody().get("success"));
        assertEquals("User not logged in", response.getBody().get("message"));
    }
    
    @Test
    void uploadAvatar_EmptyFile() {
        // Arrange
        when(session.getAttribute("userId")).thenReturn(1);
        MultipartFile emptyFile = new MockMultipartFile("file", "", "image/jpeg", new byte[0]);
        
        // Act
        ResponseEntity<Map<String, Object>> response = controller.uploadAvatar(emptyFile, session);
        
        // Assert
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertFalse((Boolean) response.getBody().get("success"));
        assertEquals("File cannot be empty", response.getBody().get("message"));
    }
    
    @Test
    void uploadAvatar_ServiceException() {
        // Arrange
        when(session.getAttribute("userId")).thenReturn(1);
        when(accountService.updateAvatar(any(MultipartFile.class), eq(1)))
            .thenThrow(new IllegalArgumentException("Unsupported file type"));
        
        // Act
        ResponseEntity<Map<String, Object>> response = controller.uploadAvatar(mockFile, session);
        
        // Assert
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertFalse((Boolean) response.getBody().get("success"));
        assertEquals("Unsupported file type", response.getBody().get("message"));
    }
    
    @Test
    void uploadAvatar_GeneralException() {
        // Arrange
        when(session.getAttribute("userId")).thenReturn(1);
        when(accountService.updateAvatar(any(MultipartFile.class), eq(1)))
            .thenThrow(new RuntimeException("Storage service unavailable"));
        
        // Act
        ResponseEntity<Map<String, Object>> response = controller.uploadAvatar(mockFile, session);
        
        // Assert
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        assertFalse((Boolean) response.getBody().get("success"));
        assertTrue(response.getBody().get("message").toString().contains("Avatar upload failed"));
    }
}
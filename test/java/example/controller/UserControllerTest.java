package example.controller;

import com.example.dto.UserBookingDetailDTO;
import com.example.service.StudentBookingService;
import com.example.service.interfaces.UserService;  
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.server.ResponseStatusException;

import javax.servlet.http.HttpSession;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import com.example.controller.UserController;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserControllerTest {

    @Mock
    private UserService userService;

    @Mock
    private StudentBookingService bookingService;

    @Mock
    private HttpSession session;

    @InjectMocks
    private UserController userController;

    @BeforeEach
    void setUp() {
        reset(userService, bookingService, session);
    }

    @Test
    void getUserBookingDetails_Success() {
        // Arrange
        UserBookingDetailDTO booking1 = new UserBookingDetailDTO();
        UserBookingDetailDTO booking2 = new UserBookingDetailDTO();
        List<UserBookingDetailDTO> mockBookings = Arrays.asList(booking1, booking2);
        
        when(bookingService.getUserBookingDetails(anyInt())).thenReturn(mockBookings);

        // Act
        ResponseEntity<?> response = userController.getUserBookingDetails(123);

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(2, ((List<?>) response.getBody()).size());
        verify(bookingService).getUserBookingDetails(123);
    }

    @Test
    void getUserBookingDetails_EmptyList() {
        // Arrange
        when(bookingService.getUserBookingDetails(anyInt())).thenReturn(List.of());

        // Act
        ResponseEntity<?> response = userController.getUserBookingDetails(123);

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertTrue(((List<?>) response.getBody()).isEmpty());
    }

    @Test
    void lockUserAccount_Success() {
        // Arrange
        when(session.getAttribute("userId")).thenReturn(1);
        when(session.getAttribute("role")).thenReturn(1); // Admin role
        when(userService.lockUserAccount(anyInt())).thenReturn(true);

        // Act
        ResponseEntity<Map<String, Object>> response = userController.lockUserAccount(123, session);

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertTrue((Boolean) response.getBody().get("success"));
        assertEquals("User account locked successfully", response.getBody().get("message"));
        verify(userService).lockUserAccount(123);
    }

    @Test
    void lockUserAccount_Failure() {
        // Arrange
        when(session.getAttribute("userId")).thenReturn(1);
        when(session.getAttribute("role")).thenReturn(1); // Admin role
        when(userService.lockUserAccount(anyInt())).thenReturn(false);

        // Act
        ResponseEntity<Map<String, Object>> response = userController.lockUserAccount(123, session);

        // Assert
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertFalse((Boolean) response.getBody().get("success"));
        assertEquals("User account lock failed, user may not exist", response.getBody().get("message"));
    }

    @Test
    void lockUserAccount_Unauthorized() {
        // Arrange
        when(session.getAttribute("userId")).thenReturn(null);

        // Act & Assert
        ResponseStatusException exception = assertThrows(ResponseStatusException.class, 
            () -> userController.lockUserAccount(123, session));
        
        assertEquals(HttpStatus.UNAUTHORIZED, exception.getStatus());
        assertEquals("Admin not logged in", exception.getReason());
        verifyNoInteractions(userService);
    }

    @Test
    void lockUserAccount_Forbidden() {
        // Arrange
        when(session.getAttribute("userId")).thenReturn(1);
        when(session.getAttribute("role")).thenReturn(2); // Non-admin role

        // Act & Assert
        ResponseStatusException exception = assertThrows(ResponseStatusException.class, 
            () -> userController.lockUserAccount(123, session));
        
        assertEquals(HttpStatus.FORBIDDEN, exception.getStatus());
        assertEquals("Only admins can perform this operation", exception.getReason());
        verifyNoInteractions(userService);
    }

    @Test
    void unlockUserAccount_Success() {
        // Arrange
        when(session.getAttribute("userId")).thenReturn(1);
        when(session.getAttribute("role")).thenReturn(1); // Admin role
        when(userService.unlockUserAccount(anyInt())).thenReturn(true);

        // Act
        ResponseEntity<Map<String, Object>> response = userController.unlockUserAccount(123, session);

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertTrue((Boolean) response.getBody().get("success"));
        assertEquals("User account unlocked successfully", response.getBody().get("message"));
        verify(userService).unlockUserAccount(123);
    }

    @Test
    void unlockUserAccount_Failure() {
        // Arrange
        when(session.getAttribute("userId")).thenReturn(1);
        when(session.getAttribute("role")).thenReturn(1); // Admin role
        when(userService.unlockUserAccount(anyInt())).thenReturn(false);

        // Act
        ResponseEntity<Map<String, Object>> response = userController.unlockUserAccount(123, session);

        // Assert
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertFalse((Boolean) response.getBody().get("success"));
        assertEquals("User account unlock failed, user may not exist", response.getBody().get("message"));
    }
    
    @Test
    void unlockUserAccount_Unauthorized() {
        // Arrange
        when(session.getAttribute("userId")).thenReturn(null);

        // Act & Assert
        ResponseStatusException exception = assertThrows(ResponseStatusException.class, 
            () -> userController.unlockUserAccount(123, session));
        
        assertEquals(HttpStatus.UNAUTHORIZED, exception.getStatus());
        assertEquals("Admin not logged in", exception.getReason());
        verifyNoInteractions(userService);
    }
    
    @Test
    void unlockUserAccount_Forbidden() {
        // Arrange
        when(session.getAttribute("userId")).thenReturn(1);
        when(session.getAttribute("role")).thenReturn(2); // Non-admin role

        // Act & Assert
        ResponseStatusException exception = assertThrows(ResponseStatusException.class, 
            () -> userController.unlockUserAccount(123, session));
        
        assertEquals(HttpStatus.FORBIDDEN, exception.getStatus());
        assertEquals("Only admins can perform this operation", exception.getReason());
        verifyNoInteractions(userService);
    }
}
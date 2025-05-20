package example.controller;
import com.example.controller.AdminBookingsManagementController;
import com.example.dto.BookingDTO;
import com.example.service.AdminBookingService;
import com.example.service.BookingService;
import com.example.service.interfaces.ExportService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import javax.servlet.http.HttpSession;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AdminBookingsManagementControllerTest {

    @Mock
    private AdminBookingService adminBookingService;

    @Mock
    private ExportService exportService;

    @Mock
    private BookingService bookingService;

    @Mock
    private HttpSession session;

    @InjectMocks
    private AdminBookingsManagementController controller;

    @BeforeEach
    void setUp() {
        reset(adminBookingService, exportService, bookingService, session);
    }

    @Test
    void listBookings_Success() {
        // Arrange
        when(session.getAttribute("userId")).thenReturn(1);
        when(session.getAttribute("role")).thenReturn(1);
        
        BookingDTO booking1 = new BookingDTO();
        BookingDTO booking2 = new BookingDTO();
        List<BookingDTO> mockBookings = Arrays.asList(booking1, booking2);
        
        when(bookingService.getFilteredBookings(anyLong(), anyInt())).thenReturn(mockBookings);

        // Act
        ResponseEntity<Map<String, Object>> response = controller.listBookings(123L, 456, session);

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertTrue((Boolean) response.getBody().get("success"));
        assertEquals(2, ((List<?>) response.getBody().get("data")).size());
        verify(bookingService).getFilteredBookings(123L, 456);
    }

    @Test
    void listBookings_Unauthorized() {
        // Arrange - no user session
        when(session.getAttribute("userId")).thenReturn(null);

        // Act
        ResponseEntity<Map<String, Object>> response = controller.listBookings(null, null, session);

        // Assert
        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
        assertFalse((Boolean) response.getBody().get("success"));
        assertEquals("Admin not logged in", response.getBody().get("message"));
        verifyNoInteractions(bookingService);
    }
    
    @Test
    void listBookings_Forbidden() {
        // Arrange - non-admin user
        when(session.getAttribute("userId")).thenReturn(1);
        when(session.getAttribute("role")).thenReturn(2); // Not admin

        // Act
        ResponseEntity<Map<String, Object>> response = controller.listBookings(null, null, session);

        // Assert
        assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode());
        assertFalse((Boolean) response.getBody().get("success"));
        assertEquals("Only admins can perform this operation", response.getBody().get("message"));
        verifyNoInteractions(bookingService);
    }

    @Test
    void exportReservationsCSV_Success() throws Exception {
        // Arrange
        when(session.getAttribute("userId")).thenReturn(1);
        when(session.getAttribute("role")).thenReturn(1);
        
        BookingDTO booking = new BookingDTO();
        List<BookingDTO> mockBookings = Arrays.asList(booking);
        
        when(adminBookingService.getFilteredBookings(anyLong(), any(), anyInt())).thenReturn(mockBookings);
        
        Map<String, Object> exportResult = new HashMap<>();
        exportResult.put("success", true);
        when(exportService.exportToCsv(anyList(), anyList(), anyString())).thenReturn(exportResult);

        // Act
        ResponseEntity<?> response = controller.exportReservationsCSV(123L, LocalDate.now(), 456, session);

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertTrue((Boolean) ((Map<?, ?>) response.getBody()).get("success"));
        verify(adminBookingService).getFilteredBookings(123L, LocalDate.now(), 456);
        verify(exportService).exportToCsv(anyList(), anyList(), eq("Bookings"));
    }

    @Test
    void cancelNonCompliantBookings_Success() {
        // Arrange
        when(session.getAttribute("userId")).thenReturn(1);
        when(session.getAttribute("role")).thenReturn(1);
        when(bookingService.cancelNonCompliantBookings()).thenReturn(true);

        // Act
        ResponseEntity<Map<String, Object>> response = controller.cancelNonCompliantBookings(session);

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertTrue((Boolean) response.getBody().get("success"));
        assertEquals("Non-compliant bookings have been successfully cancelled.", response.getBody().get("message"));
        verify(bookingService).cancelNonCompliantBookings();
    }

    @Test
    void cancelNonCompliantBookings_NoBookingsToCancel() {
        // Arrange
        when(session.getAttribute("userId")).thenReturn(1);
        when(session.getAttribute("role")).thenReturn(1);
        when(bookingService.cancelNonCompliantBookings()).thenReturn(false);

        // Act
        ResponseEntity<Map<String, Object>> response = controller.cancelNonCompliantBookings(session);

        // Assert
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertFalse((Boolean) response.getBody().get("success"));
        assertEquals("No non-compliant bookings need to be cancelled.", response.getBody().get("message"));
    }
    
    @Test
    void cancelNonCompliantBookings_Unauthorized() {
        // Arrange - no user session
        when(session.getAttribute("userId")).thenReturn(null);

        // Act
        ResponseEntity<Map<String, Object>> response = controller.cancelNonCompliantBookings(session);

        // Assert
        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
        assertFalse((Boolean) response.getBody().get("success"));
        assertEquals("Admin not logged in", response.getBody().get("message"));
        verifyNoInteractions(bookingService);
    }
    
    @Test
    void cancelNonCompliantBookings_Forbidden() {
        // Arrange - non-admin user
        when(session.getAttribute("userId")).thenReturn(1);
        when(session.getAttribute("role")).thenReturn(2); // Not admin

        // Act
        ResponseEntity<Map<String, Object>> response = controller.cancelNonCompliantBookings(session);

        // Assert
        assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode());
        assertFalse((Boolean) response.getBody().get("success"));
        assertEquals("Only admins can perform this operation", response.getBody().get("message"));
        verifyNoInteractions(bookingService);
    }

    @Test
    void cancelBooking_Success() {
        // Arrange
        when(session.getAttribute("userId")).thenReturn(1);
        when(session.getAttribute("role")).thenReturn(1);
        
        Map<String, Object> serviceResult = new HashMap<>();
        serviceResult.put("success", true);
        serviceResult.put("message", "Booking cancelled");
        when(bookingService.cancelBooking(isNull(), eq(123L))).thenReturn(serviceResult);

        // Act
        ResponseEntity<Map<String, Object>> response = controller.cancelBooking(123L, session);

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertTrue((Boolean) response.getBody().get("success"));
        assertEquals("Booking cancelled", response.getBody().get("message"));
        verify(bookingService).cancelBooking(null, 123L);
    }

    @Test
    void cancelBooking_Forbidden() {
        // Arrange - non-admin user
        when(session.getAttribute("userId")).thenReturn(1);
        when(session.getAttribute("role")).thenReturn(2); // Not admin

        // Act
        ResponseEntity<Map<String, Object>> response = controller.cancelBooking(123L, session);

        // Assert
        assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode());
        assertFalse((Boolean) response.getBody().get("success"));
        assertEquals("Only admins can perform this operation", response.getBody().get("message"));
        verifyNoInteractions(bookingService);
    }
}
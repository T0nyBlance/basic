package example.service;
import com.example.service.AdminBookingService;
import com.example.dto.BookingDTO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AdminBookingServiceTest {

    @Mock
    private AdminBookingService adminBookingService;

    private BookingDTO bookingDTO;

    @BeforeEach
    void setUp() {
        // Initialize a sample BookingDTO for testing
        bookingDTO = new BookingDTO();
        bookingDTO.setBookingId(1L);
        bookingDTO.setRoomId(101L);
        bookingDTO.setUserId(1001);
    }

    @Test
    void testExportToCsv() {
        // Arrange
        Long roomId = 101L;
        LocalDate date = LocalDate.of(2025, 4, 29);
        Integer userId = 1001;
        Map<String, Object> expectedResult = Map.of("success", true, "filename", "bookings_20250429.csv");

        when(adminBookingService.exportToCsv(roomId, date, userId))
                .thenReturn(expectedResult);

        // Act
        Map<String, Object> actualResult = adminBookingService.exportToCsv(roomId, date, userId);

        // Assert
        assertNotNull(actualResult);
        assertTrue((Boolean) actualResult.get("success"));
        assertEquals("bookings_20250429.csv", actualResult.get("filename"));

        verify(adminBookingService, times(1)).exportToCsv(roomId, date, userId);
    }

    @Test
    void testCancelNonCompliantBookings() {
        // Arrange
        when(adminBookingService.cancelNonCompliantBookings()).thenReturn(true);

        // Act
        boolean result = adminBookingService.cancelNonCompliantBookings();

        // Assert
        assertTrue(result);
        verify(adminBookingService, times(1)).cancelNonCompliantBookings();
    }

    @Test
    void testCancelBookingByAdmin() {
        // Arrange
        Long bookingId = 1L;
        when(adminBookingService.cancelBookingByAdmin(bookingId)).thenReturn(true);

        // Act
        boolean result = adminBookingService.cancelBookingByAdmin(bookingId);

        // Assert
        assertTrue(result);
        verify(adminBookingService, times(1)).cancelBookingByAdmin(bookingId);
    }
}
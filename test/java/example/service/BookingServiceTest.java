package example.service;
import com.example.service.BookingService;
import com.example.dto.BookingDTO;
import com.example.dto.FrequentBookingUserDTO;
import com.example.dto.UserBookingDetailDTO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import com.example.service.BookingService;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.time.LocalDateTime;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import java.time.LocalDateTime;
@ExtendWith(MockitoExtension.class)
class BookingServiceTest {

    @Mock
    private BookingService bookingService;

    private BookingDTO bookingDTO;
    private FrequentBookingUserDTO frequentBookingUserDTO;
    private UserBookingDetailDTO userBookingDetailDTO;

    @BeforeEach
    void setUp() {
        // Initialize sample DTOs for testing
        bookingDTO = new BookingDTO();
        bookingDTO.setBookingId(1L);
        bookingDTO.setRoomId(101L);
        bookingDTO.setUserId(1001);
        bookingDTO.setStartTime(LocalDateTime.of(2025, 4, 29, 0, 0));

        frequentBookingUserDTO = new FrequentBookingUserDTO();
        frequentBookingUserDTO.setUserId(1001);
        frequentBookingUserDTO.setBookingCount(10);

        userBookingDetailDTO = new UserBookingDetailDTO();
        userBookingDetailDTO.setBookingId(1L);
        userBookingDetailDTO.setRoomCode("101");
        userBookingDetailDTO.setStartTime(LocalDateTime.of(2025, 4, 29, 0, 0));
    }

    @Test
    void testGetStudentBookings() {
        // Arrange
        Integer userId = 1001;
        Integer roomId = 101;
        boolean reverseOrder = true;
        Map<String, Object> expectedResult = Map.of("bookings", Arrays.asList(bookingDTO));
        when(bookingService.getStudentBookings(userId, roomId, reverseOrder)).thenReturn(expectedResult);

        // Act
        Map<String, Object> result = bookingService.getStudentBookings(userId, roomId, reverseOrder);

        // Assert
        assertNotNull(result);
        assertTrue(result.containsKey("bookings"));
        assertEquals(1, ((List<?>) result.get("bookings")).size());
        verify(bookingService, times(1)).getStudentBookings(userId, roomId, reverseOrder);
    }

    @Test
    void testBookMeetingRoom() {
        // Arrange
        Integer userId = 1001;
        Integer roomId = 101;
        Map<String, Object> expectedResult = Map.of("success", true, "bookingId", 1L);
        when(bookingService.bookMeetingRoom(eq(userId), eq(roomId), any(BookingDTO.class))).thenReturn(expectedResult);

        // Act
        Map<String, Object> result = bookingService.bookMeetingRoom(userId, roomId, bookingDTO);

        // Assert
        assertNotNull(result);
        assertTrue((Boolean) result.get("success"));
        assertEquals(1L, result.get("bookingId"));
        verify(bookingService, times(1)).bookMeetingRoom(eq(userId), eq(roomId), any(BookingDTO.class));
    }

    @Test
    void testCancelBooking() {
        // Arrange
        Integer userId = 1001;
        Long bookingId = 1L;
        Map<String, Object> expectedResult = Map.of("success", true);
        when(bookingService.cancelBooking(userId, bookingId)).thenReturn(expectedResult);

        // Act
        Map<String, Object> result = bookingService.cancelBooking(userId, bookingId);

        // Assert
        assertNotNull(result);
        assertTrue((Boolean) result.get("success"));
        verify(bookingService, times(1)).cancelBooking(userId, bookingId);
    }

    @Test
    void testFindFrequentBookingUsers() {
        // Arrange
        LocalDate date = LocalDate.of(2025, 4, 29);
        boolean ascending = true;
        List<FrequentBookingUserDTO> expectedUsers = Arrays.asList(frequentBookingUserDTO);
        when(bookingService.findFrequentBookingUsers(date, ascending)).thenReturn(expectedUsers);

        // Act
        List<FrequentBookingUserDTO> result = bookingService.findFrequentBookingUsers(date, ascending);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(frequentBookingUserDTO.getUserId(), result.get(0).getUserId());
        assertEquals(frequentBookingUserDTO.getBookingCount(), result.get(0).getBookingCount());
        verify(bookingService, times(1)).findFrequentBookingUsers(date, ascending);
    }

    @Test
    void testGetUserBookingDetails() {
        // Arrange
        Integer userId = 1001;
        List<UserBookingDetailDTO> expectedDetails = Arrays.asList(userBookingDetailDTO);
        when(bookingService.getUserBookingDetails(userId)).thenReturn(expectedDetails);

        // Act
        List<UserBookingDetailDTO> result = bookingService.getUserBookingDetails(userId);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(bookingDTO.getBookingId(), result.get(0).getBookingId());
        verify(bookingService, times(1)).getUserBookingDetails(userId);
    }

    @Test
    void testGetFilteredBookings() {
        // Arrange
        Long roomId = 101L;
        Integer userId = 1001;
        List<BookingDTO> expectedBookings = Arrays.asList(bookingDTO);
        when(bookingService.getFilteredBookings(roomId, userId)).thenReturn(expectedBookings);

        // Act
        List<BookingDTO> result = bookingService.getFilteredBookings(roomId, userId);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(bookingDTO.getBookingId(), result.get(0).getBookingId());
        verify(bookingService, times(1)).getFilteredBookings(roomId, userId);
    }

    @Test
    void testCancelNonCompliantBookings() {
        // Arrange
        when(bookingService.cancelNonCompliantBookings()).thenReturn(true);

        // Act
        boolean result = bookingService.cancelNonCompliantBookings();

        // Assert
        assertTrue(result);
        verify(bookingService, times(1)).cancelNonCompliantBookings();
    }
}
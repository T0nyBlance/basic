package example.service;
import com.example.dto.BookingDTO;
import com.example.dto.FrequentBookingUserDTO;
import com.example.dto.NotificationDTO;
import com.example.dto.UserBookingDetailDTO;
import com.example.entity.*;
import com.example.repository.AdminBookingRepository;
import com.example.repository.MeetingRoomRepository;
import com.example.repository.NotificationRepository;
import com.example.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import javax.servlet.http.HttpSession;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.lang.reflect.Method;
import com.example.service.StudentBookingService;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import com.example.service.MailService;
import com.example.service.StudentBookingService;
@ExtendWith(MockitoExtension.class)
class StudentBookingServiceTest {

    @Mock
    private AdminBookingRepository bookingRepository;

    @Mock
    private MeetingRoomRepository meetingRoomRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private MailService mailServiceImpl;

    @Mock
    private NotificationRepository notificationRepository;

    @Mock
    private HttpSession session;

    @InjectMocks
    private StudentBookingService studentBookingService;

    private User testUser;
    private MeetingRoom testRoom;
    private Booking testBooking;
    private Notification testNotification;

    @BeforeEach
    void setUp() {
        // Set up test data
        testUser = new User();
        testUser.setUser_id(1);
        testUser.setUsername("testuser");
        testUser.setFull_name("Test User");
        testUser.setEmail("test@example.com");
        testUser.setBooking_suspended(false);

        testRoom = new MeetingRoom();
        testRoom.setRoom_id(1);
        testRoom.setRoom_code("ROOM101");
        testRoom.setDisplay_name("Conference Room 101");
        testRoom.setCapacity(10);

        testBooking = new Booking();
        testBooking.setBooking_id(1L);
        testBooking.setUser(testUser);
        testBooking.setRoom(testRoom);
        testBooking.setStart_time(LocalDateTime.now().plusHours(1));
        testBooking.setEnd_time(LocalDateTime.now().plusHours(2));
        testBooking.setStatus(Booking.Status.confirmed);
        testBooking.setTitle("Test Booking");
        testBooking.setConfirm_code("ABC123");

        testNotification = new Notification();
        testNotification.setNotification_id(1L);
        testNotification.setContent("Test notification content");
        testNotification.setType("booking");
        testNotification.setStatus("unread");

        // Set booking threshold
        ReflectionTestUtils.setField(studentBookingService, "bookingThreshold", 3);
        
        // Make Mockito more lenient for this test class
        lenient().doNothing().when(bookingRepository).save(any(Booking.class));
        lenient().doNothing().when(bookingRepository).updateStatus(anyLong(), anyString(), any());
    }

    @Test
    void getStudentBookings_ShouldReturnBookings() {
        // Arrange
        List<Booking> bookings = Collections.singletonList(testBooking);
        when(bookingRepository.findByUser(anyInt())).thenReturn(bookings);

        // Act
        Map<String, Object> result = studentBookingService.getStudentBookings(1, null, false);

        // Assert
        assertTrue((Boolean) result.get("success"));
        Map<String, Object> data = (Map<String, Object>) result.get("data");
        List<BookingDTO> bookingDTOs = (List<BookingDTO>) data.get("bookings");
        assertEquals(1, bookingDTOs.size());
        assertEquals(1L, bookingDTOs.get(0).getBookingId());
    }

    @Test
    void bookMeetingRoom_ShouldSuccessWhenValid() {
        // Arrange
        BookingDTO bookingDTO = new BookingDTO();
        bookingDTO.setStartTime(LocalDateTime.now().plusHours(1));
        bookingDTO.setEndTime(LocalDateTime.now().plusHours(2));
        bookingDTO.setTitle("Test Booking");

        when(meetingRoomRepository.findById(anyLong())).thenReturn(testRoom);
        when(userRepository.findById(anyInt())).thenReturn(Optional.of(testUser));
        when(bookingRepository.findActiveReservation(anyInt(), anyInt(), any(), anyInt()))
            .thenReturn(Optional.empty());
        when(bookingRepository.findConflictingBookings(anyInt(), any(), any()))
            .thenReturn(Collections.emptyList());
        when(notificationRepository.findByNotificationId(anyLong()))
            .thenReturn(Optional.of(testNotification));

        // Act
        Map<String, Object> result = studentBookingService.bookMeetingRoom(
            1, "testuser", 1, bookingDTO, "test@example.com", session);

        // Assert
        assertTrue((Boolean) result.get("success"));
        assertEquals("Booking successful, confirmation email sent.", result.get("message"));
        verify(mailServiceImpl).sendBookingConfirmationMail(anyString(), anyString(), anyString(), any());
    }

    @Test
    void bookMeetingRoom_ShouldFailWhenRoomNotFound() {
        // Arrange
        BookingDTO bookingDTO = new BookingDTO();
        when(meetingRoomRepository.findById(anyLong())).thenReturn(null);

        // Act & Assert
        assertThrows(RuntimeException.class, () -> {
            studentBookingService.bookMeetingRoom(1, "testuser", 1, bookingDTO, "test@example.com", session);
        });
    }

    @Test
    void bookMeetingRoom_ShouldFailWhenTimeConflict() {
        // Arrange
        BookingDTO bookingDTO = new BookingDTO();
        bookingDTO.setStartTime(LocalDateTime.now().plusHours(1));
        bookingDTO.setEndTime(LocalDateTime.now().plusHours(2));

        when(meetingRoomRepository.findById(anyLong())).thenReturn(testRoom);
        when(userRepository.findById(anyInt())).thenReturn(Optional.of(testUser));
        when(bookingRepository.findActiveReservation(anyInt(), anyInt(), any(), anyInt()))
            .thenReturn(Optional.empty());
        when(bookingRepository.findConflictingBookings(anyInt(), any(), any()))
            .thenReturn(Collections.singletonList(testBooking));

        // Act
        Map<String, Object> result = studentBookingService.bookMeetingRoom(
            1, "testuser", 1, bookingDTO, "test@example.com", session);

        // Assert
        assertFalse((Boolean) result.get("success"));
        assertEquals("The booking time conflicts with existing bookings.", result.get("message"));
    }

    @Test
    void updateBookingPermission_ShouldUpdateStatus() {
        // Arrange
        when(userRepository.findById(anyInt())).thenReturn(Optional.of(testUser));

        // Act - Suspend
        Map<String, Object> suspendResult = studentBookingService.updateBookingPermission(1, true);
        assertTrue((Boolean) suspendResult.get("success"));
        assertEquals("User booking permission has been suspended", suspendResult.get("message"));
        assertTrue(testUser.getBooking_suspended());

        // Act - Restore
        Map<String, Object> restoreResult = studentBookingService.updateBookingPermission(1, false);
        assertTrue((Boolean) restoreResult.get("success"));
        assertEquals("User booking permission has been restored", restoreResult.get("message"));
        assertFalse(testUser.getBooking_suspended());
    }

    @Test
    void cancelBooking_ShouldSuccessWhenValid() {
        // Arrange
        when(bookingRepository.findById(anyLong())).thenReturn(Optional.of(testBooking));

        // Act
        Map<String, Object> result = studentBookingService.cancelBooking(1, 1L);

        // Assert
        assertTrue((Boolean) result.get("success"));
        assertEquals("Booking has been successfully cancelled", result.get("message"));
        verify(bookingRepository).updateStatus(anyLong(), eq("cancelled"), any());
    }

    @Test
    void cancelBooking_ShouldFailWhenBookingStarted() {
        // Arrange
        testBooking.setStart_time(LocalDateTime.now().minusHours(1));
        when(bookingRepository.findById(anyLong())).thenReturn(Optional.of(testBooking));

        // Act
        Map<String, Object> result = studentBookingService.cancelBooking(1, 1L);

        // Assert
        assertFalse((Boolean) result.get("success"));
        assertEquals("Cannot cancel an already started booking", result.get("message"));
    }

    @Test
    void findFrequentBookingUsers_ShouldReturnList() {
        // Arrange
        Object[] mockResult = new Object[]{1, "testuser", "Test User", 5, LocalDate.now(), "valid"};
        when(userRepository.findFrequentBookingUsers(any(), anyInt()))
            .thenReturn(Collections.singletonList(mockResult));

        // Act
        List<FrequentBookingUserDTO> result = studentBookingService.findFrequentBookingUsers(LocalDate.now(), true);

        // Assert
        assertEquals(1, result.size());
        assertEquals("testuser", result.get(0).getUsername());
        assertEquals(5, result.get(0).getBookingCount());
    }

    @Test
    void getUserBookingDetails_ShouldReturnDetails() {
        // Arrange
        when(bookingRepository.findByUser(anyInt())).thenReturn(Collections.singletonList(testBooking));

        // Act
        List<UserBookingDetailDTO> result = studentBookingService.getUserBookingDetails(1);

        // Assert
        assertEquals(1, result.size());
        assertEquals("ROOM101", result.get(0).getRoomCode());
        assertEquals("Conference Room 101", result.get(0).getRoomName());
    }



    @Test
    void generateConfirmCode_ShouldReturn6CharCode() {
        // Use reflection to access private method
        try {
            Method method = StudentBookingService.class.getDeclaredMethod("generateConfirmCode");
            method.setAccessible(true);
            String result = (String) method.invoke(studentBookingService);

            // Assert
            assertNotNull(result);
            assertEquals(6, result.length());
            assertTrue(result.matches("[A-Z0-9]+"));
        } catch (Exception e) {
            fail("Failed to test generateConfirmCode: " + e.getMessage());
        }
    }
}
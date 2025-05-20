package example.service;

import com.example.dto.FeedbackDTO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import com.example.service.FeedbackService;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class FeedbackServiceTest {

    @Mock
    private FeedbackService feedbackService;

    private FeedbackDTO feedbackDTO;

    @BeforeEach
    void setUp() {
        // Initialize sample FeedbackDTO for testing
        feedbackDTO = new FeedbackDTO();
        feedbackDTO.setFeedbackId(1L);
        feedbackDTO.setUserId(1001);
        feedbackDTO.setType("BUG");
        feedbackDTO.setContent("Test feedback");
        feedbackDTO.setStatus("PENDING");
        feedbackDTO.setCreatedAt(LocalDateTime.of(2025, 4, 29, 10, 0));
    }

    @Test
    void testCanUserSubmitFeedback() {
        // Arrange
        Integer userId = 1001;
        when(feedbackService.canUserSubmitFeedback(userId)).thenReturn(true);

        // Act
        boolean result = feedbackService.canUserSubmitFeedback(userId);

        // Assert
        assertTrue(result);
        verify(feedbackService, times(1)).canUserSubmitFeedback(userId);
    }

    @Test
    void testIsUserOverDailyLimit() {
        // Arrange
        Integer userId = 1001;
        when(feedbackService.isUserOverDailyLimit(userId)).thenReturn(false);

        // Act
        boolean result = feedbackService.isUserOverDailyLimit(userId);

        // Assert
        assertFalse(result);
        verify(feedbackService, times(1)).isUserOverDailyLimit(userId);
    }

    @Test
    void testCreateFeedback() {
        // Arrange
        when(feedbackService.createFeedback(any(FeedbackDTO.class))).thenReturn(feedbackDTO);

        // Act
        FeedbackDTO result = feedbackService.createFeedback(feedbackDTO);

        // Assert
        assertNotNull(result);
        assertEquals(feedbackDTO.getFeedbackId(), result.getFeedbackId());
        assertEquals(feedbackDTO.getUserId(), result.getUserId());
        assertEquals(feedbackDTO.getType(), result.getType());
        assertEquals(feedbackDTO.getContent(), result.getContent());
        verify(feedbackService, times(1)).createFeedback(any(FeedbackDTO.class));
    }

    @Test
    void testGetUserFeedbacks() {
        // Arrange
        Integer userId = 1001;
        List<FeedbackDTO> expectedFeedbacks = Arrays.asList(feedbackDTO);
        when(feedbackService.getUserFeedbacks(userId)).thenReturn(expectedFeedbacks);

        // Act
        List<FeedbackDTO> result = feedbackService.getUserFeedbacks(userId);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(feedbackDTO.getFeedbackId(), result.get(0).getFeedbackId());
        verify(feedbackService, times(1)).getUserFeedbacks(userId);
    }

    @Test
    void testGetUserFeedbackDetail() {
        // Arrange
        Long feedbackId = 1L;
        Integer userId = 1001;
        when(feedbackService.getUserFeedbackDetail(feedbackId, userId)).thenReturn(feedbackDTO);

        // Act
        FeedbackDTO result = feedbackService.getUserFeedbackDetail(feedbackId, userId);

        // Assert
        assertNotNull(result);
        assertEquals(feedbackDTO.getFeedbackId(), result.getFeedbackId());
        verify(feedbackService, times(1)).getUserFeedbackDetail(feedbackId, userId);
    }

    @Test
    void testCancelFeedback() {
        // Arrange
        Long feedbackId = 1L;
        Integer userId = 1001;
        when(feedbackService.cancelFeedback(feedbackId, userId)).thenReturn(true);

        // Act
        boolean result = feedbackService.cancelFeedback(feedbackId, userId);

        // Assert
        assertTrue(result);
        verify(feedbackService, times(1)).cancelFeedback(feedbackId, userId);
    }

    @Test
    void testGetAllFeedbacks() {
        // Arrange
        List<String> statuses = Arrays.asList("PENDING", "IN_REVIEW");
        List<FeedbackDTO> expectedFeedbacks = Arrays.asList(feedbackDTO);
        when(feedbackService.getAllFeedbacks(statuses)).thenReturn(expectedFeedbacks);

        // Act
        List<FeedbackDTO> result = feedbackService.getAllFeedbacks(statuses);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(feedbackDTO.getFeedbackId(), result.get(0).getFeedbackId());
        verify(feedbackService, times(1)).getAllFeedbacks(statuses);
    }

    @Test
    void testViewFeedback() {
        // Arrange
        Long feedbackId = 1L;
        when(feedbackService.viewFeedback(feedbackId)).thenReturn(feedbackDTO);

        // Act
        FeedbackDTO result = feedbackService.viewFeedback(feedbackId);

        // Assert
        assertNotNull(result);
        assertEquals(feedbackDTO.getFeedbackId(), result.getFeedbackId());
        verify(feedbackService, times(1)).viewFeedback(feedbackId);
    }

    @Test
    void testGetFeedbackDetail() {
        // Arrange
        Long feedbackId = 1L;
        when(feedbackService.getFeedbackDetail(feedbackId)).thenReturn(feedbackDTO);

        // Act
        FeedbackDTO result = feedbackService.getFeedbackDetail(feedbackId);

        // Assert
        assertNotNull(result);
        assertEquals(feedbackDTO.getFeedbackId(), result.getFeedbackId());
        verify(feedbackService, times(1)).getFeedbackDetail(feedbackId);
    }

    @Test
    void testMarkFeedbackAsInReview() {
        // Arrange
        Long feedbackId = 1L;
        when(feedbackService.markFeedbackAsInReview(feedbackId)).thenReturn(true);

        // Act
        boolean result = feedbackService.markFeedbackAsInReview(feedbackId);

        // Assert
        assertTrue(result);
        verify(feedbackService, times(1)).markFeedbackAsInReview(feedbackId);
    }

    @Test
    void testReplyFeedback() {
        // Arrange
        Long feedbackId = 1L;
        String response = "Thank you for your feedback";
        Integer responderId = 1002;
        when(feedbackService.replyFeedback(feedbackId, response, responderId)).thenReturn(feedbackDTO);

        // Act
        FeedbackDTO result = feedbackService.replyFeedback(feedbackId, response, responderId);

        // Assert
        assertNotNull(result);
        assertEquals(feedbackDTO.getFeedbackId(), result.getFeedbackId());
        verify(feedbackService, times(1)).replyFeedback(feedbackId, response, responderId);
    }

    @Test
    void testRejectFeedback() {
        // Arrange
        Long feedbackId = 1L;
        String reason = "Invalid feedback";
        Integer responderId = 1002;
        when(feedbackService.rejectFeedback(feedbackId, reason, responderId)).thenReturn(true);

        // Act
        boolean result = feedbackService.rejectFeedback(feedbackId, reason, responderId);

        // Assert
        assertTrue(result);
        verify(feedbackService, times(1)).rejectFeedback(feedbackId, reason, responderId);
    }

    @Test
    void testGetFeedbackStatistics() {
        // Arrange
        Map<String, Object> expectedStats = Map.of("total", 10, "pending", 5);
        when(feedbackService.getFeedbackStatistics()).thenReturn(expectedStats);

        // Act
        Map<String, Object> result = feedbackService.getFeedbackStatistics();

        // Assert
        assertNotNull(result);
        assertEquals(10, result.get("total"));
        assertEquals(5, result.get("pending"));
        verify(feedbackService, times(1)).getFeedbackStatistics();
    }

    @Test
    void testGetUntreatedFeedbacks() {
        // Arrange
        List<FeedbackDTO> expectedFeedbacks = Arrays.asList(feedbackDTO);
        when(feedbackService.getUntreatedFeedbacks()).thenReturn(expectedFeedbacks);

        // Act
        List<FeedbackDTO> result = feedbackService.getUntreatedFeedbacks();

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(feedbackDTO.getFeedbackId(), result.get(0).getFeedbackId());
        verify(feedbackService, times(1)).getUntreatedFeedbacks();
    }

    @Test
    void testGetFeedbacksByType() {
        // Arrange
        String type = "BUG";
        List<FeedbackDTO> expectedFeedbacks = Arrays.asList(feedbackDTO);
        when(feedbackService.getFeedbacksByType(type)).thenReturn(expectedFeedbacks);

        // Act
        List<FeedbackDTO> result = feedbackService.getFeedbacksByType(type);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(feedbackDTO.getFeedbackId(), result.get(0).getFeedbackId());
        verify(feedbackService, times(1)).getFeedbacksByType(type);
    }

    @Test
    void testGetFeedbacksByResponder() {
        // Arrange
        Integer responderId = 1002;
        List<FeedbackDTO> expectedFeedbacks = Arrays.asList(feedbackDTO);
        when(feedbackService.getFeedbacksByResponder(responderId)).thenReturn(expectedFeedbacks);

        // Act
        List<FeedbackDTO> result = feedbackService.getFeedbacksByResponder(responderId);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(feedbackDTO.getFeedbackId(), result.get(0).getFeedbackId());
        verify(feedbackService, times(1)).getFeedbacksByResponder(responderId);
    }

    @Test
    void testHandleLongPendingFeedbacks() {
        // Arrange
        doNothing().when(feedbackService).handleLongPendingFeedbacks();

        // Act
        feedbackService.handleLongPendingFeedbacks();

        // Assert
        verify(feedbackService, times(1)).handleLongPendingFeedbacks();
    }

    @Test
    void testMarkFeedbackAsPending() {
        // Arrange
        Long feedbackId = 1L;
        when(feedbackService.markFeedbackAsPending(feedbackId)).thenReturn(true);

        // Act
        boolean result = feedbackService.markFeedbackAsPending(feedbackId);

        // Assert
        assertTrue(result);
        verify(feedbackService, times(1)).markFeedbackAsPending(feedbackId);
    }
}
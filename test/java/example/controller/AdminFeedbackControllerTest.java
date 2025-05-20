package example.controller;
import com.example.controller.AdminFeedbackController;
import com.example.dto.FeedbackDTO;
import com.example.service.FeedbackService;
import com.example.service.impl.AccountServiceImpl;
import com.example.util.ResponseUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class AdminFeedbackControllerTest {

    private MockMvc mockMvc;

    @Mock
    private FeedbackService feedbackService;

    @Mock
    private AccountServiceImpl accountService;

    @InjectMocks
    private AdminFeedbackController adminFeedbackController;

    private MockHttpSession session;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        mockMvc = MockMvcBuilders.standaloneSetup(adminFeedbackController).build();
        
        session = new MockHttpSession();
        session.setAttribute("userId", 1); // Admin user ID
    }

    @Test
    void getAllFeedbacks_WithoutStatusFilter_ShouldReturnAllFeedbacks() throws Exception {
        FeedbackDTO feedback1 = new FeedbackDTO();
        feedback1.setFeedbackId(1L);
        FeedbackDTO feedback2 = new FeedbackDTO();
        feedback2.setFeedbackId(2L);
        
        when(feedbackService.getAllFeedbacks(null)).thenReturn(Arrays.asList(feedback1, feedback2));
        
        mockMvc.perform(get("/api/admin/feedbacks"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.length()").value(2));
    }

    @Test
    void getAllFeedbacks_WithStatusFilter_ShouldReturnFilteredFeedbacks() throws Exception {
        FeedbackDTO feedback1 = new FeedbackDTO();
        feedback1.setFeedbackId(1L);
        
        when(feedbackService.getAllFeedbacks(anyList())).thenReturn(Collections.singletonList(feedback1));
        
        mockMvc.perform(get("/api/admin/feedbacks?statuses=pending,in_review"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.length()").value(1));
    }

    @Test
    void viewFeedback_WhenFeedbackExists_ShouldReturnFeedbackAndUpdateStatus() throws Exception {
        FeedbackDTO feedback = new FeedbackDTO();
        feedback.setFeedbackId(1L);
        feedback.setStatus("in_review");
        
        when(feedbackService.viewFeedback(anyLong())).thenReturn(feedback);
        
        mockMvc.perform(get("/api/admin/feedbacks/1/view"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.feedbackId").value(1))
                .andExpect(jsonPath("$.data.status").value("in_review"));
    }

    @Test
    void viewFeedback_WhenFeedbackNotExists_ShouldReturnNotFound() throws Exception {
        when(feedbackService.viewFeedback(anyLong())).thenReturn(null);
        
        mockMvc.perform(get("/api/admin/feedbacks/1/view"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("Feedback does not exist"));
    }

    @Test
    void getFeedbackDetail_WhenFeedbackExists_ShouldReturnFeedback() throws Exception {
        FeedbackDTO feedback = new FeedbackDTO();
        feedback.setFeedbackId(1L);
        
        when(feedbackService.getFeedbackDetail(anyLong())).thenReturn(feedback);
        
        mockMvc.perform(get("/api/admin/feedbacks/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.feedbackId").value(1));
    }

    @Test
    void getUntreatedFeedbacks_ShouldReturnUntreatedFeedbacks() throws Exception {
        FeedbackDTO feedback = new FeedbackDTO();
        feedback.setFeedbackId(1L);
        feedback.setStatus("pending");
        
        when(feedbackService.getUntreatedFeedbacks()).thenReturn(Collections.singletonList(feedback));
        
        mockMvc.perform(get("/api/admin/feedbacks/untreated"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data[0].status").value("pending"));
    }

    @Test
    void getFeedbacksByType_ShouldReturnTypeFilteredFeedbacks() throws Exception {
        FeedbackDTO feedback = new FeedbackDTO();
        feedback.setFeedbackId(1L);
        feedback.setType("bug");
        
        when(feedbackService.getFeedbacksByType(anyString())).thenReturn(Collections.singletonList(feedback));
        
        mockMvc.perform(get("/api/admin/feedbacks/type/bug"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data[0].type").value("bug"));
    }

    @Test
    void getFeedbacksByResponder_ShouldReturnResponderFeedbacks() throws Exception {
        FeedbackDTO feedback = new FeedbackDTO();
        feedback.setFeedbackId(1L);
        feedback.setResponderId(1);
        
        when(feedbackService.getFeedbacksByResponder(anyInt())).thenReturn(Collections.singletonList(feedback));
        
        mockMvc.perform(get("/api/admin/feedbacks/responder/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data[0].responderId").value(1));
    }

    @Test
    void markFeedbackAsInReview_WhenSuccessful_ShouldReturnSuccess() throws Exception {
        when(feedbackService.markFeedbackAsInReview(anyLong())).thenReturn(true);
        
        mockMvc.perform(post("/api/admin/feedbacks/1/mark-in-review"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Feedback has been marked as in review"));
    }

    @Test
    void replyFeedback_WithValidResponse_ShouldReturnUpdatedFeedback() throws Exception {
        FeedbackDTO feedback = new FeedbackDTO();
        feedback.setFeedbackId(1L);
        feedback.setResponse("Test response");
        
        when(feedbackService.replyFeedback(anyLong(), anyString(), anyInt())).thenReturn(feedback);
        
        mockMvc.perform(post("/api/admin/feedbacks/1/reply")
                .session(session)
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"response\":\"Test response\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Feedback has been replied"))
                .andExpect(jsonPath("$.data.response").value("Test response"));
    }

    @Test
    void rejectFeedback_WhenSuccessful_ShouldReturnSuccess() throws Exception {
        when(feedbackService.rejectFeedback(anyLong(), anyString(), anyInt())).thenReturn(true);
        
        mockMvc.perform(post("/api/admin/feedbacks/1/reject")
                .param("reason", "Invalid")
                .param("responderId", "1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Feedback has been rejected"));
    }

    @Test
    void getFeedbackStatistics_ShouldReturnStatistics() throws Exception {
        Map<String, Object> stats = new HashMap<>();
        stats.put("total", 10);
        stats.put("pending", 3);
        
        when(feedbackService.getFeedbackStatistics()).thenReturn(stats);
        
        mockMvc.perform(get("/api/admin/feedbacks/statistics"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.total").value(10))
                .andExpect(jsonPath("$.data.pending").value(3));
    }

    @Test
    void markFeedbackAsPending_WhenSuccessful_ShouldReturnSuccess() throws Exception {
        when(feedbackService.markFeedbackAsPending(anyLong())).thenReturn(true);
        
        mockMvc.perform(post("/api/admin/feedbacks/1/mark-pending"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Feedback has been marked as pending"));
    }

    @Test
    void getUserFeedbackDetail_WhenExists_ShouldReturnFeedback() throws Exception {
        FeedbackDTO feedback = new FeedbackDTO();
        feedback.setFeedbackId(1L);
        feedback.setUserId(1);
        
        when(feedbackService.getUserFeedbackDetail(anyLong(), anyInt())).thenReturn(feedback);
        
        mockMvc.perform(get("/api/admin/feedbacks/1/user/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.feedbackId").value(1));
    }
}
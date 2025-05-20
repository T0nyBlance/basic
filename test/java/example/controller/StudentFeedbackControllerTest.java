package example.controller;

import com.example.dto.FeedbackDTO;
import com.example.service.FeedbackService;
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
import org.springframework.web.servlet.HandlerInterceptor;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import com.example.controller.StudentFeedbackController;
import java.util.Arrays;
import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class StudentFeedbackControllerTest {

    private MockMvc mockMvc;

    @Mock
    private FeedbackService feedbackService;

    @InjectMocks
    private StudentFeedbackController studentFeedbackController;

    private MockHttpSession session;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        mockMvc = MockMvcBuilders.standaloneSetup(studentFeedbackController)
                .addInterceptors(new HandlerInterceptor() {
                    @Override
                    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
                        HttpSession session = request.getSession(false);
                        if (session == null || session.getAttribute("userId") == null) {
                            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                            response.getWriter().write("{\"success\":false,\"message\":\"请先登录\"}");
                            return false;
                        }
                        return true;
                    }
                })
                .build();
        
        session = new MockHttpSession();
        session.setAttribute("userId", 123);
    }

    @Test
    void createFeedback_WhenUserNotLoggedIn_ShouldReturnUnauthorized() throws Exception {
        String feedbackJson = "{\"title\":\"Test Feedback\",\"content\":\"Test content\"}";
        
        mockMvc.perform(post("/api/student/feedbacks/create")
                .contentType(MediaType.APPLICATION_JSON)
                .content(feedbackJson))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void createFeedback_WhenSubmissionTooFrequent_ShouldReturnTooManyRequests() throws Exception {
        when(feedbackService.canUserSubmitFeedback(anyInt())).thenReturn(false);
        
        String feedbackJson = "{\"title\":\"Test Feedback\",\"content\":\"Test content\"}";
        
        mockMvc.perform(post("/api/student/feedbacks/create")
                .session(session)
                .contentType(MediaType.APPLICATION_JSON)
                .content(feedbackJson))
                .andExpect(status().isTooManyRequests())
                .andExpect(jsonPath("$.message").value("Submit too frequently, please try again later"));
    }

    @Test
    void createFeedback_WhenOverDailyLimit_ShouldReturnTooManyRequests() throws Exception {
        when(feedbackService.canUserSubmitFeedback(anyInt())).thenReturn(true);
        when(feedbackService.isUserOverDailyLimit(anyInt())).thenReturn(true);
        
        String feedbackJson = "{\"title\":\"Test Feedback\",\"content\":\"Test content\"}";
        
        mockMvc.perform(post("/api/student/feedbacks/create")
                .session(session)
                .contentType(MediaType.APPLICATION_JSON)
                .content(feedbackJson))
                .andExpect(status().isTooManyRequests())
                .andExpect(jsonPath("$.message").value("Over daily submission limit"));
    }

    @Test
    void createFeedback_WhenValid_ShouldReturnCreatedFeedback() throws Exception {
        when(feedbackService.canUserSubmitFeedback(anyInt())).thenReturn(true);
        when(feedbackService.isUserOverDailyLimit(anyInt())).thenReturn(false);
        
        FeedbackDTO mockFeedback = createMockFeedback(1L, "Test Feedback", "Test content", 123);
        
        when(feedbackService.createFeedback(any(FeedbackDTO.class))).thenReturn(mockFeedback);
        
        String feedbackJson = "{\"title\":\"Test Feedback\",\"content\":\"Test content\"}";
        
        mockMvc.perform(post("/api/student/feedbacks/create")
                .session(session)
                .contentType(MediaType.APPLICATION_JSON)
                .content(feedbackJson))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Feedback submitted successfully"))
                .andExpect(jsonPath("$.data.feedbackId").value(1))
                .andExpect(jsonPath("$.data.title").value("Test Feedback"));
    }

    @Test
    void cancelFeedback_WhenUserNotLoggedIn_ShouldReturnUnauthorized() throws Exception {
        mockMvc.perform(post("/api/student/feedbacks/1/cancel"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void cancelFeedback_WhenFailed_ShouldReturnBadRequest() throws Exception {
        when(feedbackService.cancelFeedback(anyLong(), anyInt())).thenReturn(false);
        
        mockMvc.perform(post("/api/student/feedbacks/1/cancel")
                .session(session))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("Cancel feedback failed, feedback may not exist or has been processed"));
    }

    @Test
    void cancelFeedback_WhenSuccessful_ShouldReturnSuccessMessage() throws Exception {
        when(feedbackService.cancelFeedback(anyLong(), anyInt())).thenReturn(true);
        
        mockMvc.perform(post("/api/student/feedbacks/1/cancel")
                .session(session))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Feedback cancelled"));
    }

    @Test
    void getUserFeedbacks_WhenUserNotLoggedIn_ShouldReturnUnauthorized() throws Exception {
        mockMvc.perform(get("/api/student/feedbacks/user"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void getUserFeedbacks_WhenLoggedIn_ShouldReturnFeedbackList() throws Exception {
        FeedbackDTO feedback1 = createMockFeedback(1L, "Feedback 1", null, null);
        FeedbackDTO feedback2 = createMockFeedback(2L, "Feedback 2", null, null);
        
        List<FeedbackDTO> feedbacks = Arrays.asList(feedback1, feedback2);
        
        when(feedbackService.getUserFeedbacks(anyInt())).thenReturn(feedbacks);
        
        mockMvc.perform(get("/api/student/feedbacks/user")
                .session(session))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data[0].feedbackId").value(1))
                .andExpect(jsonPath("$.data[0].title").value("Feedback 1"))
                .andExpect(jsonPath("$.data[1].feedbackId").value(2))
                .andExpect(jsonPath("$.data[1].title").value("Feedback 2"));
    }

    @Test
    void getUserFeedbackDetail_WhenUserNotLoggedIn_ShouldReturnUnauthorized() throws Exception {
        mockMvc.perform(get("/api/student/feedbacks/1/user/123"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void getUserFeedbackDetail_WhenFeedbackNotFound_ShouldReturnNotFound() throws Exception {
        when(feedbackService.getUserFeedbackDetail(anyLong(), anyInt())).thenReturn(null);
        
        mockMvc.perform(get("/api/student/feedbacks/1/user/123")
                .session(session))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("Feedback not found"));
    }

    @Test
    void getUserFeedbackDetail_WhenFound_ShouldReturnFeedback() throws Exception {
        FeedbackDTO feedback = createMockFeedback(1L, "Test Feedback", "Test content", 123);
        
        when(feedbackService.getUserFeedbackDetail(anyLong(), anyInt())).thenReturn(feedback);
        
        mockMvc.perform(get("/api/student/feedbacks/1/user/123")
                .session(session))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.feedbackId").value(1))
                .andExpect(jsonPath("$.data.title").value("Test Feedback"));
    }
    
    private FeedbackDTO createMockFeedback(Long id, String title, String content, Integer userId) {
        FeedbackDTO feedback = new FeedbackDTO();
        try {
            java.lang.reflect.Field feedbackIdField = FeedbackDTO.class.getDeclaredField("feedbackId");
            feedbackIdField.setAccessible(true);
            feedbackIdField.set(feedback, id);
            
            if (title != null) {
                java.lang.reflect.Field titleField = FeedbackDTO.class.getDeclaredField("title");
                titleField.setAccessible(true);
                titleField.set(feedback, title);
            }
            
            if (content != null) {
                java.lang.reflect.Field contentField = FeedbackDTO.class.getDeclaredField("content");
                contentField.setAccessible(true);
                contentField.set(feedback, content);
            }
            
            if (userId != null) {
                java.lang.reflect.Field userIdField = FeedbackDTO.class.getDeclaredField("userId");
                userIdField.setAccessible(true);
                userIdField.set(feedback, userId);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return feedback;
    }
}
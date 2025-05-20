package example.controller;

import com.example.dto.AnnouncementDTO;
import com.example.service.impl.StudAnnouncementServiceImpl;
import com.example.util.ResponseUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import com.example.controller.StudentAnnouncementController;    
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class StudentAnnouncementControllerTest {

    private MockMvc mockMvc;

    @Mock
    private StudAnnouncementServiceImpl announcementService;

    @InjectMocks
    private StudentAnnouncementController studentAnnouncementController;

    private MockHttpSession session;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        mockMvc = MockMvcBuilders.standaloneSetup(studentAnnouncementController).build();
        
        session = new MockHttpSession();
        session.setAttribute("userId", 123);
    }

    @Test
    void getAnnouncements_WhenUserNotLoggedIn_ShouldReturnUnauthorized() throws Exception {
        mockMvc.perform(get("/api/student/announcements"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("User not logged in"));
    }

    @Test
    void getAnnouncements_WhenUserLoggedIn_ShouldReturnAnnouncements() throws Exception {
        AnnouncementDTO announcement1 = new AnnouncementDTO();
        setField(announcement1, "announcement_id", 1L);
        setField(announcement1, "content", "Test Announcement 1");
        
        AnnouncementDTO announcement2 = new AnnouncementDTO();
        setField(announcement2, "announcement_id", 2L);
        setField(announcement2, "content", "Test Announcement 2");
        
        List<AnnouncementDTO> announcements = Arrays.asList(announcement1, announcement2);
        
        when(announcementService.getAnnouncementsForUser(any(Integer.class))).thenReturn(announcements);
        
        mockMvc.perform(get("/api/student/announcements").session(session))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data[0].announcement_id").value(1))
                .andExpect(jsonPath("$.data[0].content").value("Test Announcement 1"))
                .andExpect(jsonPath("$.data[1].announcement_id").value(2))
                .andExpect(jsonPath("$.data[1].content").value("Test Announcement 2"));
    }

    @Test
    void markAsRead_WhenUserNotLoggedIn_ShouldReturnUnauthorized() throws Exception {
        mockMvc.perform(post("/api/student/announcements/1/read"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("User not logged in"));
    }

    @Test
    void markAsRead_WhenSuccessful_ShouldReturnSuccessMessage() throws Exception {
        when(announcementService.markAsRead(anyLong(), any(Integer.class))).thenReturn(true);
        
        mockMvc.perform(post("/api/student/announcements/1/read").session(session))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Announcement marked as read"));
    }

    @Test
    void markAsRead_WhenFailed_ShouldReturnBadRequest() throws Exception {
        when(announcementService.markAsRead(anyLong(), any(Integer.class))).thenReturn(false);
        
        mockMvc.perform(post("/api/student/announcements/1/read").session(session))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("Mark failed"));
    }

    @Test
    void getUnreadCount_WhenUserNotLoggedIn_ShouldReturnUnauthorized() throws Exception {
        mockMvc.perform(get("/api/student/announcements/unread/count"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("User not logged in"));
    }

    @Test
    void getUnreadCount_WhenUserLoggedIn_ShouldReturnCount() throws Exception {
        when(announcementService.getUnreadCount(any(Integer.class))).thenReturn(5);
        
        mockMvc.perform(get("/api/student/announcements/unread/count").session(session))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.unreadCount").value(5));
    }

    @Test
    void getUnreadAnnouncements_WhenUserNotLoggedIn_ShouldReturnUnauthorized() throws Exception {
        mockMvc.perform(get("/api/student/announcements/unread"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("User not logged in"));
    }

    @Test
    void getUnreadAnnouncements_WhenUserLoggedIn_ShouldReturnAnnouncements() throws Exception {
        AnnouncementDTO announcement1 = new AnnouncementDTO();
        setField(announcement1, "announcement_id", 1L);
        setField(announcement1, "content", "Unread Announcement 1");
        
        AnnouncementDTO announcement2 = new AnnouncementDTO();
        setField(announcement2, "announcement_id", 2L);
        setField(announcement2, "content", "Unread Announcement 2");
        
        List<AnnouncementDTO> unreadAnnouncements = Arrays.asList(announcement1, announcement2);
        
        when(announcementService.getUnreadAnnouncement(any(Integer.class))).thenReturn(unreadAnnouncements);
        
        mockMvc.perform(get("/api/student/announcements/unread").session(session))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data[0].announcement_id").value(1))
                .andExpect(jsonPath("$.data[0].content").value("Unread Announcement 1"))
                .andExpect(jsonPath("$.data[1].announcement_id").value(2))
                .andExpect(jsonPath("$.data[1].content").value("Unread Announcement 2"));
    }
    
    private void setField(Object obj, String fieldName, Object value) {
        try {
            java.lang.reflect.Field field = obj.getClass().getDeclaredField(fieldName);
            field.setAccessible(true);
            field.set(obj, value);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
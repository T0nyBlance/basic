package example.controller;
import com.example.controller.AdminAnnouncementController;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
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

import com.example.dto.AnnouncementCreateDTO;
import com.example.dto.AnnouncementDTO;
import com.example.service.AnnouncementService;

@ExtendWith(MockitoExtension.class)
class AdminAnnouncementControllerTest {

    @Mock
    private AnnouncementService announcementService;

    @Mock
    private HttpSession session;

    @InjectMocks
    private AdminAnnouncementController controller;

    private AnnouncementCreateDTO createDTO;
    private AnnouncementDTO announcementDTO;
    private List<AnnouncementDTO> announcementList;

    @BeforeEach
    void setUp() {
        // 准备测试数据
        createDTO = new AnnouncementCreateDTO();
        createDTO.setContent("测试公告内容");
        createDTO.setTarget_user_role(0);  // 面向所有用户
        
        announcementDTO = new AnnouncementDTO();
        announcementDTO.setAnnouncement_id(1L);
        announcementDTO.setContent("测试公告内容");
        announcementDTO.setTarget_user_role(0);
        announcementDTO.setPublisher_id(1);
        
        announcementList = new ArrayList<>();
        announcementList.add(announcementDTO);
        
        // 使用 lenient() 使得即使这个 mock 没有被使用也不会报错
        lenient().when(session.getAttribute("userId")).thenReturn(1);
    }

    @Test
    void createAnnouncement_Success() {
        // Arrange
        when(announcementService.createAnnouncement(any(AnnouncementCreateDTO.class), anyInt()))
            .thenReturn(announcementDTO);
        
        // Act
        ResponseEntity<Map<String, Object>> response = controller.createAnnouncement(createDTO, session);
        
        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertTrue((Boolean) response.getBody().get("success"));
        assertEquals("Announcement created successfully", response.getBody().get("message"));
        assertEquals(announcementDTO, response.getBody().get("data"));
    }

    @Test
    void deleteAnnouncement_Success() {
        // Arrange
        when(announcementService.deleteAnnouncement(1L, 1)).thenReturn(true);
        
        // Act
        ResponseEntity<Map<String, Object>> response = controller.deleteAnnouncement(1L, session);
        
        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertTrue((Boolean) response.getBody().get("success"));
        assertEquals("Announcement deleted successfully", response.getBody().get("message"));
    }
    
    @Test
    void deleteAnnouncement_Failure() {
        // Arrange
        when(announcementService.deleteAnnouncement(1L, 1)).thenReturn(false);
        
        // Act
        ResponseEntity<Map<String, Object>> response = controller.deleteAnnouncement(1L, session);
        
        // Assert
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertFalse((Boolean) response.getBody().get("success"));
        assertEquals("Delete failed, announcement does not exist or no permission", response.getBody().get("message"));
    }

    @Test
    void getAnnouncementsForAdmin_Success() {
        // Arrange
        when(announcementService.getAllAnnouncementsForAdmin(1)).thenReturn(announcementList);
        
        // Act
        ResponseEntity<Map<String, Object>> response = controller.getAnnouncementsForAdmin(session);
        
        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertTrue((Boolean) response.getBody().get("success"));
        assertEquals(announcementList, response.getBody().get("data"));
    }
    
    @Test
    void getAnnouncementsForAdmin_Unauthorized() {
        // Arrange
        when(session.getAttribute("userId")).thenReturn(null);
        
        // Act
        ResponseEntity<Map<String, Object>> response = controller.getAnnouncementsForAdmin(session);
        
        // Assert
        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
        assertFalse((Boolean) response.getBody().get("success"));
        assertEquals("Not logged in or session expired", response.getBody().get("message"));
    }

    @Test
    void updateAnnouncement_Success() {
        // Arrange
        when(announcementService.updateAnnouncement(anyLong(), any(AnnouncementCreateDTO.class), anyInt()))
            .thenReturn(announcementDTO);
        
        // Act
        ResponseEntity<Map<String, Object>> response = controller.updateAnnouncement(1L, createDTO, session);
        
        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertTrue((Boolean) response.getBody().get("success"));
        assertEquals("Announcement updated successfully", response.getBody().get("message"));
        assertEquals(announcementDTO, response.getBody().get("data"));
    }
    
    @Test
    void updateAnnouncement_Failure() {
        // Arrange
        when(announcementService.updateAnnouncement(anyLong(), any(AnnouncementCreateDTO.class), anyInt()))
            .thenReturn(null);
        
        // Act
        ResponseEntity<Map<String, Object>> response = controller.updateAnnouncement(1L, createDTO, session);
        
        // Assert
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertFalse((Boolean) response.getBody().get("success"));
        assertEquals("Update failed, announcement does not exist or no permission", response.getBody().get("message"));
    }

    @Test
    void getAnnouncementStatistics_Success() {
        // Arrange
        Map<String, Object> statistics = new HashMap<>();
        statistics.put("total", 10);
        statistics.put("active", 8);
        statistics.put("admin", 5);
        
        when(announcementService.getAnnouncementStatistics()).thenReturn(statistics);
        
        // Act
        ResponseEntity<Map<String, Object>> response = controller.getAnnouncementStatistics();
        
        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertTrue((Boolean) response.getBody().get("success"));
        assertEquals(statistics, response.getBody().get("data"));
    }
}
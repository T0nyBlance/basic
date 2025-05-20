package example.service;
import com.example.service.AnnouncementService;
import com.example.dto.AnnouncementCreateDTO;
import com.example.dto.AnnouncementDTO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AnnouncementServiceTest {

    @Mock
    private AnnouncementService announcementService;

    private AnnouncementDTO announcementDTO;
    private AnnouncementCreateDTO createDTO;

    @BeforeEach
    void setUp() {
        // Initialize sample DTOs for testing
        createDTO = new AnnouncementCreateDTO();
        createDTO.setContent("This is a test announcement");

        announcementDTO = new AnnouncementDTO();
        announcementDTO.setAnnouncement_id(1L);
        announcementDTO.setContent("This is a test announcement");
        announcementDTO.setPublisher_id(1001);
    }

    @Test
    void testCreateAnnouncement() {
        // Arrange
        Integer publisherId = 1001;
        when(announcementService.createAnnouncement(any(AnnouncementCreateDTO.class), eq(publisherId)))
                .thenReturn(announcementDTO);

        // Act
        AnnouncementDTO result = announcementService.createAnnouncement(createDTO, publisherId);

        // Assert
        assertNotNull(result);
        assertEquals(announcementDTO.getAnnouncement_id(), result.getAnnouncement_id());
        assertEquals(announcementDTO.getContent(), result.getContent());
        assertEquals(announcementDTO.getPublisher_id(), result.getPublisher_id());

        verify(announcementService, times(1)).createAnnouncement(any(AnnouncementCreateDTO.class), eq(publisherId));
    }

    @Test
    void testDeleteAnnouncement() {
        // Arrange
        Long announcementId = 1L;
        Integer userId = 1001;
        when(announcementService.deleteAnnouncement(announcementId, userId)).thenReturn(true);

        // Act
        boolean result = announcementService.deleteAnnouncement(announcementId, userId);

        // Assert
        assertTrue(result);
        verify(announcementService, times(1)).deleteAnnouncement(announcementId, userId);
    }

    @Test
    void testGetAllAnnouncementsForAdmin() {
        // Arrange
        Integer userId = 1001;
        List<AnnouncementDTO> expectedAnnouncements = Arrays.asList(announcementDTO);
        when(announcementService.getAllAnnouncementsForAdmin(userId)).thenReturn(expectedAnnouncements);

        // Act
        List<AnnouncementDTO> result = announcementService.getAllAnnouncementsForAdmin(userId);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(announcementDTO.getAnnouncement_id(), result.get(0).getAnnouncement_id());
        verify(announcementService, times(1)).getAllAnnouncementsForAdmin(userId);
    }

    @Test
    void testGetAnnouncementsForUser() {
        // Arrange
        Integer userId = 1002;
        List<AnnouncementDTO> expectedAnnouncements = Arrays.asList(announcementDTO);
        when(announcementService.getAnnouncementsForUser(userId)).thenReturn(expectedAnnouncements);

        // Act
        List<AnnouncementDTO> result = announcementService.getAnnouncementsForUser(userId);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(announcementDTO.getAnnouncement_id(), result.get(0).getAnnouncement_id());
        verify(announcementService, times(1)).getAnnouncementsForUser(userId);
    }

    @Test
    void testMarkAsRead() {
        // Arrange
        Long announcementId = 1L;
        Integer userId = 1002;
        when(announcementService.markAsRead(announcementId, userId)).thenReturn(true);

        // Act
        boolean result = announcementService.markAsRead(announcementId, userId);

        // Assert
        assertTrue(result);
        verify(announcementService, times(1)).markAsRead(announcementId, userId);
    }

    @Test
    void testGetUnreadCount() {
        // Arrange
        Integer userId = 1002;
        when(announcementService.getUnreadCount(userId)).thenReturn(5);

        // Act
        int result = announcementService.getUnreadCount(userId);

        // Assert
        assertEquals(5, result);
        verify(announcementService, times(1)).getUnreadCount(userId);
    }

    @Test
    void testGetUnreadAnnouncementsForUser() {
        // Arrange
        Integer userId = 1002;
        List<AnnouncementDTO> expectedAnnouncements = Arrays.asList(announcementDTO);
        when(announcementService.getUnreadAnnouncementsForUser(userId)).thenReturn(expectedAnnouncements);

        // Act
        List<AnnouncementDTO> result = announcementService.getUnreadAnnouncementsForUser(userId);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(announcementDTO.getAnnouncement_id(), result.get(0).getAnnouncement_id());
        verify(announcementService, times(1)).getUnreadAnnouncementsForUser(userId);
    }

    @Test
    void testUpdateAnnouncement() {
        // Arrange
        Long announcementId = 1L;
        Integer userId = 1001;
        when(announcementService.updateAnnouncement(eq(announcementId), any(AnnouncementCreateDTO.class), eq(userId)))
                .thenReturn(announcementDTO);

        // Act
        AnnouncementDTO result = announcementService.updateAnnouncement(announcementId, createDTO, userId);

        // Assert
        assertNotNull(result);
        assertEquals(announcementDTO.getAnnouncement_id(), result.getAnnouncement_id());
        assertEquals(announcementDTO.getContent(), result.getContent());
        verify(announcementService, times(1)).updateAnnouncement(eq(announcementId), any(AnnouncementCreateDTO.class), eq(userId));
    }

    @Test
    void testGetAnnouncementStatistics() {
        // Arrange
        Map<String, Object> expectedStats = Map.of("total", 10, "unread", 5);
        when(announcementService.getAnnouncementStatistics()).thenReturn(expectedStats);

        // Act
        Map<String, Object> result = announcementService.getAnnouncementStatistics();

        // Assert
        assertNotNull(result);
        assertEquals(10, result.get("total"));
        assertEquals(5, result.get("unread"));
        verify(announcementService, times(1)).getAnnouncementStatistics();
    }
}
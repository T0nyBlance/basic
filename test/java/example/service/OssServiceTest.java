package example.service;
import com.example.service.OssService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class OssServiceTest {

    @Mock
    private OssService ossService;

    @Test
    void testUploadAvatar() {
        // Arrange
        Integer userId = 1001;
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "avatar.jpg",
                "image/jpeg",
                "test image content".getBytes()
        );
        String expectedUrl = "https://oss.example.com/avatars/1001/avatar.jpg";
        when(ossService.uploadAvatar(file, userId)).thenReturn(expectedUrl);

        // Act
        String result = ossService.uploadAvatar(file, userId);

        // Assert
        assertNotNull(result);
        assertEquals(expectedUrl, result);
        verify(ossService, times(1)).uploadAvatar(file, userId);
    }

    @Test
    void testUploadThumbnail() {
        // Arrange
        Long roomId = 101L;
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "thumbnail.jpg",
                "image/jpeg",
                "test thumbnail content".getBytes()
        );
        String expectedUrl = "https://oss.example.com/thumbnails/101/thumbnail.jpg";
        when(ossService.uploadThumbnail(file, roomId)).thenReturn(expectedUrl);

        // Act
        String result = ossService.uploadThumbnail(file, roomId);

        // Assert
        assertNotNull(result);
        assertEquals(expectedUrl, result);
        verify(ossService, times(1)).uploadThumbnail(file, roomId);
    }

    @Test
    void testDeleteFile() {
        // Arrange
        String fileUrl = "https://oss.example.com/avatars/1001/avatar.jpg";
        when(ossService.deleteFile(fileUrl)).thenReturn(true);

        // Act
        boolean result = ossService.deleteFile(fileUrl);

        // Assert
        assertTrue(result);
        verify(ossService, times(1)).deleteFile(fileUrl);
    }
}
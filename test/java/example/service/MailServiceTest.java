package example.service;
import com.example.service.MailService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;

import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class MailServiceTest {

    @Mock
    private MailService mailService;

    @Test
    void testSendActivationMail() {
        // Arrange
        String to = "user@example.com";
        String username = "testuser";
        String activationCode = "ACT123";

        // Act
        mailService.sendActivationMail(to, username, activationCode);

        // Assert
        verify(mailService, times(1)).sendActivationMail(to, username, activationCode);
    }

    @Test
    void testSendResetPasswordMail() {
        // Arrange
        String to = "user@example.com";
        String username = "testuser";
        String resetPasswordCode = "RESET456";

        // Act
        mailService.sendResetPasswordMail(to, username, resetPasswordCode);

        // Assert
        verify(mailService, times(1)).sendResetPasswordMail(to, username, resetPasswordCode);
    }

    @Test
    void testSendBookingConfirmationMail() {
        // Arrange
        String to = "user@example.com";
        String username = "testuser";
        String roomCode = "ROOM101";
        LocalDateTime bookingTime = LocalDateTime.of(2025, 4, 29, 14, 30);

        // Act
        mailService.sendBookingConfirmationMail(to, username, roomCode, bookingTime);

        // Assert
        verify(mailService, times(1)).sendBookingConfirmationMail(to, username, roomCode, bookingTime);
    }
}
package example.service;
import com.example.service.AccountService;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import javax.servlet.http.HttpSession;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.multipart.MultipartFile;

import com.example.dto.UserDTO;
import com.example.entity.User;
import com.example.entity.User.Status;
import com.example.entity.User.Validity;
import com.example.exception.ResourceNotFoundException;
import com.example.repository.AccountRepository;
import com.example.service.impl.AccountServiceImpl;
import com.example.service.impl.MailServiceImpl;
import com.example.service.impl.OssServiceImpl;

import cn.hutool.crypto.digest.DigestUtil;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class AccountServiceTest {

    @Mock
    private AccountRepository accountRepository;

    @Mock
    private MailServiceImpl mailService;

    @Mock
    private OssServiceImpl ossService;

    @Mock
    private HttpSession session;

    @InjectMocks
    private AccountServiceImpl accountService;

    private User testUser;
    private UserDTO testUserDTO;
    private String testPassword = "Password123";
    private String testPasswordHash;

    @BeforeEach
    void setUp() {
        // 准备测试数据
        testPasswordHash = DigestUtil.md5Hex(testPassword);
        
        // 手动创建和设置 User 对象的属性，不使用 Lombok
        testUser = mock(User.class);
        when(testUser.getUser_id()).thenReturn(1234567);
        when(testUser.getUsername()).thenReturn("1234567");
        when(testUser.getEmail()).thenReturn("test@example.com");
        when(testUser.getPassword_hash()).thenReturn(testPasswordHash);
        when(testUser.getFull_name()).thenReturn("Test User");
        when(testUser.getEnd_user_status()).thenReturn(Status.active);
        when(testUser.getEnd_user_validity()).thenReturn(Validity.unblocked);
        when(testUser.getLocked()).thenReturn(0);
        when(testUser.getFailed_attempts()).thenReturn(0);
        
        // 手动创建和设置 UserDTO 对象
        testUserDTO = mock(UserDTO.class);
        when(testUserDTO.getUserId()).thenReturn(1234567);
        when(testUserDTO.getUsername()).thenReturn("1234567");
        when(testUserDTO.getEmail()).thenReturn("test@example.com");
        when(testUserDTO.getPasswordHash()).thenReturn(testPassword);
    }

    @Test
    void login_Success() {
        // Arrange
        Map<String, Object> response = new HashMap<>();
        when(accountRepository.existsById(1234567)).thenReturn(true);
        when(accountRepository.findById(1234567)).thenReturn(Optional.of(testUser));
        when(accountRepository.findByUserIdAndPassword(eq(1234567), eq(testPasswordHash)))
            .thenReturn(Optional.of(testUser));
        
        // Act
        boolean result = accountService.login(testUserDTO, response);
        
        // Assert
        assertTrue(result);
        verify(accountRepository).UpdateFailedAttemptsById(1234567, 0);
    }
    
    @Test
    void login_WrongPassword() {
        // Arrange
        Map<String, Object> response = new HashMap<>();
        // 创建一个新的模拟对象，返回错误的密码
        UserDTO wrongPasswordDTO = mock(UserDTO.class);
        when(wrongPasswordDTO.getUserId()).thenReturn(1234567);
        when(wrongPasswordDTO.getPasswordHash()).thenReturn("wrongPassword");
        
        when(accountRepository.existsById(1234567)).thenReturn(true);
        when(accountRepository.findById(1234567)).thenReturn(Optional.of(testUser));
        
        // Act
        boolean result = accountService.login(wrongPasswordDTO, response);
        
        // Assert
        assertFalse(result);
        verify(accountRepository).UpdateFailedAttemptsById(1234567, 1);
    }
    
    @Test
    void login_LockedAccount() {
        // Arrange
        Map<String, Object> response = new HashMap<>();
        // 创建一个已锁定的用户对象
        User lockedUser = mock(User.class);
        when(lockedUser.getUser_id()).thenReturn(1234567);
        when(lockedUser.getLocked()).thenReturn(1);
        
        when(accountRepository.existsById(1234567)).thenReturn(true);
        when(accountRepository.findById(1234567)).thenReturn(Optional.of(lockedUser));
        
        // Act
        boolean result = accountService.login(testUserDTO, response);
        
        // Assert
        assertFalse(result);
        assertEquals("Unable to login!", response.get("msg"));
    }

    @Test
    void register_Success() {
        // Arrange
        when(accountRepository.existsById(1234567)).thenReturn(false);
        when(accountRepository.existsByEmail("test@example.com")).thenReturn(false);
        when(mailService.generateActivationCode()).thenReturn("123456");
        doNothing().when(mailService).sendActivationMail(anyString(), anyString(), anyString());
        
        // Act
        boolean result = accountService.register(testUserDTO);
        
        // Assert
        assertTrue(result);
        verify(accountRepository).save(any(User.class));
        verify(accountRepository).updateVerificationExpires(eq("test@example.com"), any(LocalDateTime.class));
    }
    
    @Test
    void register_ExistingUser() {
        // Arrange
        when(accountRepository.existsById(1234567)).thenReturn(true);
        
        // Act & Assert
        assertThrows(RuntimeException.class, () -> accountService.register(testUserDTO));
    }
    
    @Test
    void register_InvalidPassword() {
        // Arrange
        // 创建一个带有无效密码的 DTO
        UserDTO invalidPasswordDTO = mock(UserDTO.class);
        when(invalidPasswordDTO.getUserId()).thenReturn(1234567);
        when(invalidPasswordDTO.getEmail()).thenReturn("test@example.com");
        when(invalidPasswordDTO.getPasswordHash()).thenReturn("password"); // Missing uppercase and digit
        
        when(accountRepository.existsById(1234567)).thenReturn(false);
        
        // Act & Assert
        assertThrows(RuntimeException.class, () -> accountService.register(invalidPasswordDTO));
    }

    @Test
    void getCurrentUser_Success() {
        // Arrange
        when(session.getAttribute("userId")).thenReturn(1234567);
        when(accountRepository.findById(1234567)).thenReturn(Optional.of(testUser));
        
        // Act
        User result = accountService.getCurrentUser(session);
        
        // Assert
        assertEquals(testUser, result);
    }
    
    @Test
    void getCurrentUser_NotLoggedIn() {
        // Arrange
        when(session.getAttribute("userId")).thenReturn(null);
        
        // Act & Assert
        assertThrows(ResourceNotFoundException.class, () -> accountService.getCurrentUser(session));
    }

    @Test
    void activate_Success() {
        // Arrange
        String verificationToken = "123456";
        when(testUser.getVerification_token()).thenReturn(verificationToken);
        when(testUser.getVerification_expires()).thenReturn(LocalDateTime.now().plusMinutes(5));
        
        when(accountRepository.findByVerificationToken(verificationToken)).thenReturn(Optional.of(testUser));
        when(accountRepository.verify(anyString(), any(Status.class))).thenReturn(true);
        when(accountRepository.saveUserRole(anyInt(), anyInt())).thenReturn(true);
        
        // Act
        boolean result = accountService.activate(verificationToken, "test@example.com", 1234567);
        
        // Assert
        assertTrue(result);
    }
    
    @Test
    void activate_Expired() {
        // Arrange
        String verificationToken = "123456";
        when(testUser.getVerification_token()).thenReturn(verificationToken);
        when(testUser.getVerification_expires()).thenReturn(LocalDateTime.now().minusMinutes(5));
        
        when(accountRepository.findByVerificationToken(verificationToken)).thenReturn(Optional.of(testUser));
        
        // Act & Assert
        assertThrows(RuntimeException.class, () -> 
            accountService.activate(verificationToken, "test@example.com", 1234567));
    }

    @Test
    void resetPassword_Success() {
        // Arrange
        String resetToken = "123456";
        when(testUser.getReset_token()).thenReturn(resetToken);
        when(testUser.getReset_expires()).thenReturn(LocalDateTime.now().plusMinutes(5));
        when(testUser.getEmail()).thenReturn("test@example.com");
        
        when(accountRepository.findByResetToken(resetToken)).thenReturn(Optional.of(testUser));
        when(accountRepository.findIdByEmail(anyString())).thenReturn(1234567);
        doNothing().when(accountRepository).updatePassword(anyInt(), anyString());
        
        // Act
        boolean result = accountService.resetPassword(resetToken, "NewPassword123");
        
        // Assert
        assertTrue(result);
        verify(accountRepository).updatePassword(eq(1234567), anyString());
    }
    
    @Test
    void resetPassword_Expired() {
        // Arrange
        String resetToken = "123456";
        when(testUser.getReset_token()).thenReturn(resetToken);
        when(testUser.getReset_expires()).thenReturn(LocalDateTime.now().minusMinutes(5));
        
        when(accountRepository.findByResetToken(resetToken)).thenReturn(Optional.of(testUser));
        
        // Act & Assert
        assertThrows(ResourceNotFoundException.class, () -> 
            accountService.resetPassword(resetToken, "NewPassword123"));
    }
    
    @Test
    void resetPassword_SamePassword() {
        // Arrange
        String resetToken = "123456";
        when(testUser.getReset_token()).thenReturn(resetToken);
        when(testUser.getReset_expires()).thenReturn(LocalDateTime.now().plusMinutes(5));
        when(testUser.getPassword_hash()).thenReturn(testPasswordHash);
        
        when(accountRepository.findByResetToken(resetToken)).thenReturn(Optional.of(testUser));
        
        // Act & Assert
        assertThrows(RuntimeException.class, () -> 
            accountService.resetPassword(resetToken, testPassword));
    }
}
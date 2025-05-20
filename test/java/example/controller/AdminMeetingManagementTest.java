package example.controller;
import com.example.controller.AdminMeetingManagement;
import com.example.service.MeetingRoomService;
import com.example.dto.MeetingRoomDTO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.lang.reflect.Field;
import java.util.Collections;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class) // 启用Mockito扩展
public class AdminMeetingManagementTest {

    @Mock
    private MeetingRoomService meetingRoomService;

    @InjectMocks
    private AdminMeetingManagement adminMeetingManagement;

    private MockMvc mockMvc;
    private MockHttpSession mockSession;

    @BeforeEach
    public void setUp() {
        // 初始化MockMvc
        mockMvc = MockMvcBuilders.standaloneSetup(adminMeetingManagement).build();
        
        // 创建模拟会话
        mockSession = new MockHttpSession();
        mockSession.setAttribute("userId", 1); // 模拟管理员用户ID
        mockSession.setAttribute("role", 1);   // 模拟管理员角色
    }

    @Test
    public void testGetAllMeetingRooms() throws Exception {
        // 模拟会议室服务返回的会议室列表
        MeetingRoomDTO roomDTO = new MeetingRoomDTO();
        setField(roomDTO, "roomId", 1L);
        setField(roomDTO, "displayName", "会议室1");
        
        when(meetingRoomService.getAllMeetingRooms()).thenReturn(Collections.singletonList(roomDTO));

        // 执行GET请求
        mockMvc.perform(get("/login/admin/MeetingRooms")
                .session(mockSession))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.size()").value(1))
                .andExpect(jsonPath("$[0].roomId").value(1L))
                .andExpect(jsonPath("$[0].displayName").value("会议室1"));

        verify(meetingRoomService, times(1)).getAllMeetingRooms();
    }

    @Test
    public void testAddMeetingRoom() throws Exception {
        // 创建新的会议室DTO
        MeetingRoomDTO newRoom = new MeetingRoomDTO();
        setField(newRoom, "roomId", 2L);
        setField(newRoom, "displayName", "Meeting Room 2");

        when(meetingRoomService.addMeetingRoom(any(MeetingRoomDTO.class))).thenReturn(newRoom);

        // 执行POST请求
        mockMvc.perform(post("/login/admin/MeetingRooms/addMeetingRoom")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"displayName\":\"Meeting Room 2\"}")
                .session(mockSession))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Meeting room added successfully"))
                .andExpect(jsonPath("$.data.roomId").value(2L));

        verify(meetingRoomService, times(1)).addMeetingRoom(any(MeetingRoomDTO.class));
    }

    @Test
    public void testDeleteMeetingRoom() throws Exception {
        // 模拟删除操作
        doNothing().when(meetingRoomService).deleteMeetingRoom(1L);

        // 执行DELETE请求
        mockMvc.perform(delete("/login/admin/MeetingRooms/{roomId}", 1L)
                .session(mockSession))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Meeting room deleted successfully"));

        verify(meetingRoomService, times(1)).deleteMeetingRoom(1L);
    }

    @Test
    public void testEditMeetingRoom() throws Exception {
        // 模拟更新后的会议室DTO
        MeetingRoomDTO updatedRoom = new MeetingRoomDTO();
        setField(updatedRoom, "roomId", 1L);
        setField(updatedRoom, "displayName", "Updated Meeting Room");

        when(meetingRoomService.updateMeetingRoom(eq(1L), any(MeetingRoomDTO.class))).thenReturn(updatedRoom);

        // 执行PUT请求
        mockMvc.perform(put("/login/admin/MeetingRooms/{roomId}", 1L)
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"displayName\":\"Updated Meeting Room\"}")
                .session(mockSession))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.roomId").value(1L))
                .andExpect(jsonPath("$.displayName").value("Updated Meeting Room"));

        verify(meetingRoomService, times(1)).updateMeetingRoom(eq(1L), any(MeetingRoomDTO.class));
    }

    // @Test
    // public void testUploadThumbnail() throws Exception {
    //     // 模拟文件上传返回的URL
    //     when(meetingRoomService.updateThumbnail(any(), eq(1L))).thenReturn("http://example.com/thumbnail.jpg");

    //     // 执行文件上传请求
    //     mockMvc.perform(multipart("/login/admin/MeetingRooms/{roomId}/thumbnail/upload", 1L)
    //             .file("file", "dummy content".getBytes())
    //             .session(mockSession))
    //             .andExpect(status().isOk())
    //             .andExpect(jsonPath("$.success").value(true))
    //             .andExpect(jsonPath("$.message").value("缩略图上传成功"))
    //             .andExpect(jsonPath("$.thumbnailUrl").value("http://example.com/thumbnail.jpg"));

    //     verify(meetingRoomService, times(1)).updateThumbnail(any(), eq(1L));
    // }
    
    /**
     * 使用反射设置对象的字段值
     * @param target 目标对象
     * @param fieldName 字段名
     * @param value 字段值
     */
    private void setField(Object target, String fieldName, Object value) {
        try {
            Field field = target.getClass().getDeclaredField(fieldName);
            field.setAccessible(true);
            field.set(target, value);
        } catch (Exception e) {
            throw new RuntimeException("Failed to set field value: " + fieldName, e);
        }
    }
}

package example.controller;

// This is a test comment to confirm the file is being edited
import com.example.dto.BookingDTO;
import com.example.dto.MeetingRoomDTO;
import com.example.dto.RoomScheduleDTO;
import com.example.entity.Booking;
import com.example.entity.Facility;
import com.example.repository.AdminBookingRepository;
import com.example.service.MeetingRoomService;
import com.example.service.StudentBookingService;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import com.example.controller.StudentMeetingController;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class StudentMeetingControllerTest {

    private MockMvc mockMvc;
    private ObjectMapper objectMapper;

    @Mock
    private StudentBookingService studentService;

    @Mock
    private MeetingRoomService meetingRoomService;

    @Mock
    private AdminBookingRepository bookingRepository;

    @InjectMocks
    private StudentMeetingController studentMeetingController;

    private MockHttpSession session;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        mockMvc = MockMvcBuilders.standaloneSetup(studentMeetingController).build();
        
        objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());
        
        session = new MockHttpSession();
        session.setAttribute("userId", 123);
        session.setAttribute("role", 2); // Student role
    }
    @Test
    void getAvailableRooms_WhenUnauthenticated_ShouldReturnUnauthorized() throws Exception {
        mockMvc.perform(get("/login/student/MeetingRooms"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void getAvailableRooms_WhenAuthenticated_ShouldReturnRooms() throws Exception {
        List<MeetingRoomDTO> rooms = new ArrayList<>();
        
        MeetingRoomDTO room1 = new MeetingRoomDTO();
        room1.setRoomId(1L);
        room1.setRoomCode("Room1");
        room1.setCapacity(10);
        room1.setDescription("Building A");
        
        MeetingRoomDTO room2 = new MeetingRoomDTO();
        room2.setRoomId(2L);
        room2.setRoomCode("Room2");
        room2.setCapacity(15);
        room2.setDescription("Building B");
        
        rooms.add(room1);
        rooms.add(room2);
        
        when(meetingRoomService.getAvailableRooms(any(), any())).thenReturn(rooms);
        
        mockMvc.perform(get("/login/student/MeetingRooms")
                .session(session))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].roomCode").value("Room1"))
                .andExpect(jsonPath("$[1].roomCode").value("Room2"));
    }

    @Test
    void getAllRooms_WhenAuthenticated_ShouldReturnAllRooms() throws Exception {
        List<MeetingRoomDTO> rooms = new ArrayList<>();
        
        MeetingRoomDTO room = new MeetingRoomDTO();
        room.setRoomId(1L);
        room.setRoomCode("Room1");
        room.setCapacity(10);
        room.setDescription("Building A");
        
        rooms.add(room);
        
        when(meetingRoomService.getAllRooms()).thenReturn(rooms);
        
        mockMvc.perform(get("/login/student/MeetingRooms/list")
                .session(session))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].roomCode").value("Room1"));
    }

    @Test
    void getAllFacilities_WhenAuthenticated_ShouldReturnFacilities() throws Exception {
        List<Facility> facilities = new ArrayList<>();
        
        Facility facility1 = new Facility();
        facility1.setFacility_id(1);
        facility1.setFacility_name("Projector");
        facility1.setFacility_code("AV");
        
        Facility facility2 = new Facility();
        facility2.setFacility_id(2);
        facility2.setFacility_name("Whiteboard");
        facility2.setFacility_code("General");
        
        facilities.add(facility1);
        facilities.add(facility2);
        
        when(meetingRoomService.findFacilities(anyLong())).thenReturn(facilities);
        
        mockMvc.perform(get("/login/student/MeetingRooms/list/facilities?room_id=1")
                .session(session))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].facility_name").value("Projector"));
    }

    @Test
    void getRoomScheduleData_WithDate_ShouldReturnSchedule() throws Exception {
        RoomScheduleDTO schedule = new RoomScheduleDTO();
        schedule.setRoomId(1L);
        schedule.setDate(LocalDate.now());
        
        when(meetingRoomService.getRoomSchedule(anyLong(), any())).thenReturn(schedule);
        
        mockMvc.perform(get("/login/student/MeetingRooms/list/1/schedule?date=2023-01-01")
                .session(session))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.roomId").value(1));
    }

    @Test
    void searchByRoomCode_WhenAuthenticated_ShouldReturnMatchingRooms() throws Exception {
        List<MeetingRoomDTO> rooms = new ArrayList<>();
        
        MeetingRoomDTO room = new MeetingRoomDTO();
        room.setRoomId(1L);
        room.setRoomCode("Room1");
        room.setCapacity(10);
        room.setDescription("Building A");
        
        rooms.add(room);
        
        when(meetingRoomService.searchByRoomCode(anyString())).thenReturn(rooms);
        
        mockMvc.perform(get("/login/student/MeetingRooms/search?roomCode=Room1")
                .session(session))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].roomCode").value("Room1"));
    }

    @Test
    void filterByCapacity_WhenAuthenticated_ShouldReturnFilteredRooms() throws Exception {
        List<MeetingRoomDTO> rooms = new ArrayList<>();
        
        MeetingRoomDTO room = new MeetingRoomDTO();
        room.setRoomId(1L);
        room.setRoomCode("Room1");
        room.setCapacity(10);
        room.setDescription("Building A");
        
        rooms.add(room);
        
        when(meetingRoomService.filterByCapacity(anyInt(), anyInt())).thenReturn(rooms);
        
        mockMvc.perform(get("/login/student/MeetingRooms/filter/capacity?minCapacity=5&maxCapacity=15")
                .session(session))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].capacity").value(10));
    }

    @Test
    void filterByLocation_WhenAuthenticated_ShouldReturnFilteredRooms() throws Exception {
        MeetingRoomDTO room = new MeetingRoomDTO();
        room.setRoomCode("Room1");
        room.setCapacity(10);
        room.setDescription("Building A");
        
        List<MeetingRoomDTO> rooms = new ArrayList<>();
        rooms.add(room);
        
        when(meetingRoomService.filterByLocation(anyString())).thenReturn(rooms);
        
        mockMvc.perform(get("/login/student/MeetingRooms/filter/location?location=Building")
                .session(session))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].description").value("Building A"));
    }

    @Test
    void filterByFacilities_WhenAuthenticated_ShouldReturnFilteredRooms() throws Exception {
        MeetingRoomDTO room = new MeetingRoomDTO();
        room.setRoomCode("Room1");
        room.setCapacity(10);
        room.setDescription("Building A");
        
        List<MeetingRoomDTO> rooms = new ArrayList<>();
        rooms.add(room);
        
        when(meetingRoomService.filterByFacilities(anyList())).thenReturn(rooms);
        
        mockMvc.perform(get("/login/student/MeetingRooms/filter/facilities?facilityIds=1,2")
                .session(session))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].roomCode").value("Room1"));
    }
    @Test
    void searchAndFilterRooms_WhenAuthenticated_ShouldReturnFilteredRooms() throws Exception {
        List<MeetingRoomDTO> rooms = new ArrayList<>();
        
        MeetingRoomDTO room = new MeetingRoomDTO();
        room.setRoomId(1L);
        room.setRoomCode("Room1");
        room.setCapacity(10);
        room.setDescription("Building A");
        
        rooms.add(room);
        
        when(meetingRoomService.searchAndFilterRooms(any(), any(), any(), any(), any())).thenReturn(rooms);
        
        mockMvc.perform(get("/login/student/MeetingRooms/search/filter?roomCode=Room1&minCapacity=5")
                .session(session))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].roomCode").value("Room1"));
    }

    @Test
    void bookMeetingRoom_WhenValidRequest_ShouldReturnSuccess() throws Exception {
        BookingDTO bookingDTO = new BookingDTO();
        bookingDTO.setStartTime(LocalDateTime.now().plusHours(1));
        bookingDTO.setEndTime(LocalDateTime.now().plusHours(2));
        
        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("message", "Booking successful");
        
        when(studentService.bookMeetingRoom(anyInt(), any(), anyInt(), any(), any(), any())).thenReturn(response);
        
        mockMvc.perform(post("/login/student/MeetingRooms/bookRoom/1")
                .session(session)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(bookingDTO)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }

    @Test
    void listMyBookings_WhenAuthenticated_ShouldReturnBookings() throws Exception {
        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("data", Collections.emptyList());
        
        when(studentService.getStudentBookings(anyInt(), any(), anyBoolean())).thenReturn(response);
        
        mockMvc.perform(get("/login/student/MeetingRooms/history")
                .session(session))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }

    @Test
    void updateBookingPermission_WhenValidRequest_ShouldReturnSuccess() throws Exception {
        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("message", "Permission updated");
        
        when(studentService.updateBookingPermission(anyInt(), anyBoolean())).thenReturn(response);
        
        mockMvc.perform(put("/login/student/MeetingRooms/updateBookingPermission")
                .session(session)
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"suspended\":false}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }

    @Test
    void cancelBooking_WhenValidRequest_ShouldReturnSuccess() throws Exception {
        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("message", "Booking cancelled");
        
        when(studentService.cancelBooking(anyInt(), anyLong())).thenReturn(response);
        
        mockMvc.perform(delete("/login/student/MeetingRooms/cancelBooking/1")
                .session(session))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }

    @Test
    void checkRoomAvailability_WhenAvailable_ShouldReturnTrue() throws Exception {
        List<Booking> conflictingBookings = Collections.emptyList();
        
        when(bookingRepository.findConflictingBookings(anyInt(), any(), any())).thenReturn(conflictingBookings);
        
        mockMvc.perform(get("/login/student/MeetingRooms/1/checkAvailability")
                .session(session)
                .param("startTime", "2023-01-01T10:00:00")
                .param("endTime", "2023-01-01T11:00:00"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.available").value(true));
    }
}
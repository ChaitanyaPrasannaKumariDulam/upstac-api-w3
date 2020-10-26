package org.upgrad.upstac.testrequests;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.security.test.context.support.WithUserDetails;
import org.springframework.web.server.ResponseStatusException;
import org.upgrad.upstac.testrequests.lab.CreateLabResult;
import org.upgrad.upstac.testrequests.lab.LabRequestController;
import org.upgrad.upstac.testrequests.lab.TestStatus;
import org.upgrad.upstac.users.User;
import org.upgrad.upstac.users.models.Gender;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;


@SpringBootTest
@Slf4j
class LabRequestControllerTest {


    @Autowired
    LabRequestController labRequestController;

    @Autowired
    TestRequestQueryService testRequestQueryService;

    @Autowired
    TestRequestUpdateService testRequestUpdateService;

    @Test
    @WithUserDetails(value = "tester")
    public void calling_assignForLabTest_with_valid_test_request_id_should_update_the_request_status() {
        TestRequest testRequest = getTestRequestByStatus(RequestStatus.INITIATED);
        TestRequest testRequest1 = labRequestController.assignForLabTest(testRequest.getRequestId());
        assertEquals(testRequest.getRequestId(), testRequest1.getRequestId());
        assertNotNull(testRequest1.getLabResult());
    }

    public TestRequest getTestRequestByStatus(RequestStatus status) {
        testRequestUpdateService.saveTestRequest(getMockedTestRequest(status));
        return testRequestUpdateService.saveTestRequest(getMockedTestRequest(status));
    }

    @Test
    @WithUserDetails(value = "tester")
    public void calling_assignForLabTest_with_valid_test_request_id_should_throw_exception() {
        Long InvalidRequestId = -34L;
        ResponseStatusException responseStatusException = assertThrows(ResponseStatusException.class, () -> labRequestController.assignForLabTest(InvalidRequestId));
        assertTrue(responseStatusException.getMessage().contains("Invalid ID"));
        assertNotNull(responseStatusException);
        assertEquals(HttpStatus.BAD_REQUEST, responseStatusException.getStatus());
        assertEquals("Invalid ID", responseStatusException.getReason());
    }

    @Test
    @WithUserDetails(value = "tester")
    public void calling_updateLabTest_with_valid_test_request_id_should_update_the_request_status_and_update_test_request_details() {
        TestRequest testRequest = getTestRequestByStatus(RequestStatus.LAB_TEST_IN_PROGRESS);
        CreateLabResult createLabResult = getCreateLabResult(testRequest);
        TestRequest testRequest1 = getTestRequestByStatus(RequestStatus.LAB_TEST_IN_PROGRESS);
        testRequest1.setStatus(RequestStatus.LAB_TEST_IN_PROGRESS);
        labRequestController.updateLabTest(testRequest.requestId, createLabResult);
        assertEquals(testRequest.getRequestId(), testRequest1.getRequestId());
        assertTrue(testRequest1.getStatus().equals(RequestStatus.COMPLETED));
        assertEquals(testRequest.getLabResult(), testRequest1.getLabResult());
    }


    @Test
    @WithUserDetails(value = "tester")
    public void calling_updateLabTest_with_invalid_test_request_id_should_throw_exception() {
        Long InvalidRequestId = -34L;
        TestRequest testRequest = getTestRequestByStatus(RequestStatus.LAB_TEST_IN_PROGRESS);
        CreateLabResult createLabResult = getCreateLabResult(testRequest);
        ResponseStatusException responseStatusException = assertThrows(ResponseStatusException.class, () -> labRequestController.updateLabTest(InvalidRequestId, createLabResult));
        assertNotNull(responseStatusException);
        assertEquals(HttpStatus.BAD_REQUEST, responseStatusException.getStatus());
        assertTrue(responseStatusException.getMessage().contains("Invalid ID"));

    }

    @Test
    @WithUserDetails(value = "tester")
    public void calling_updateLabTest_with_invalid_empty_status_should_throw_exception() {
        TestRequest testRequest = getTestRequestByStatus(RequestStatus.LAB_TEST_IN_PROGRESS);
        CreateLabResult createLabResult = getCreateLabResult(testRequest);
        createLabResult.setResult(null);
        ResponseStatusException responseStatusException = assertThrows(ResponseStatusException.class, () -> labRequestController.updateLabTest(testRequest.getRequestId(), createLabResult));
        assertNotNull(responseStatusException);
        assertEquals(HttpStatus.BAD_REQUEST, responseStatusException.getStatus());
        assertTrue(responseStatusException.getMessage().contains("ConstraintViolationException"));
    }

    public CreateLabResult getCreateLabResult(TestRequest testRequest) {
        CreateLabResult createLabResult = new CreateLabResult();
        createLabResult.setBloodPressure("98");
        createLabResult.setHeartBeat("101");
        createLabResult.setComments("comments");
        createLabResult.setOxygenLevel("saturated");
        createLabResult.setTemperature("98");
        createLabResult.setResult(TestStatus.NEGATIVE);
        return createLabResult;
    }

    public CreateTestRequest createTestRequest() {
        CreateTestRequest createTestRequest = new CreateTestRequest();
        createTestRequest.setAddress("some Addres");
        createTestRequest.setAge(98);
        createTestRequest.setEmail("someone" + "123456789" + "@somedomain.com");
        createTestRequest.setGender(Gender.MALE);
        createTestRequest.setName("someuser");
        createTestRequest.setPhoneNumber("123456789");
        createTestRequest.setPinCode(716768);
        return createTestRequest;
    }

    public TestRequest getMockedTestRequest(RequestStatus status) {
        CreateTestRequest createTestRequest = createTestRequest();
        TestRequest testRequest = new TestRequest();

        testRequest.setName(createTestRequest.getName());
        testRequest.setCreated(LocalDate.now());
        testRequest.setStatus(status);
        testRequest.setAge(createTestRequest.getAge());
        testRequest.setEmail(createTestRequest.getEmail());
        testRequest.setPhoneNumber(createTestRequest.getPhoneNumber());
        testRequest.setPinCode(createTestRequest.getPinCode());
        testRequest.setAddress(createTestRequest.getAddress());
        testRequest.setGender(createTestRequest.getGender());

        testRequest.setCreatedBy(createUser());

        return testRequest;
    }


    private User createUser() {
        User user = new User();
        user.setId(1L);
        user.setUserName("someuser");
        return user;
    }

}
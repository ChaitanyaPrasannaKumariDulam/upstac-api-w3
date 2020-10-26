package org.upgrad.upstac.testrequests;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.security.test.context.support.WithUserDetails;
import org.springframework.web.server.ResponseStatusException;
import org.upgrad.upstac.testrequests.consultation.Consultation;
import org.upgrad.upstac.testrequests.consultation.ConsultationController;
import org.upgrad.upstac.testrequests.consultation.ConsultationRepository;
import org.upgrad.upstac.testrequests.consultation.CreateConsultationRequest;
import org.upgrad.upstac.testrequests.consultation.DoctorSuggestion;
import org.upgrad.upstac.testrequests.lab.CreateLabResult;
import org.upgrad.upstac.testrequests.lab.LabResult;
import org.upgrad.upstac.testrequests.lab.LabResultRepository;
import org.upgrad.upstac.testrequests.lab.TestStatus;
import org.upgrad.upstac.users.User;
import org.upgrad.upstac.users.models.Gender;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;


@SpringBootTest
@Slf4j
class ConsultationControllerTest {


    @Autowired
    ConsultationController consultationController;


    @Autowired
    TestRequestQueryService testRequestQueryService;

    @Autowired
    TestRequestUpdateService testRequestUpdateService;

    @Autowired
    LabResultRepository labResultRepository;

    @Autowired
    ConsultationRepository consultationRepository;


    @Test
    @WithUserDetails(value = "doctor")
    public void calling_assignForConsultation_with_valid_test_request_id_should_update_the_request_status() {
        TestRequest testRequest = getTestRequestByStatus(RequestStatus.LAB_TEST_COMPLETED);
        TestRequest result = consultationController.assignForConsultation(testRequest.getRequestId());
        assertEquals(testRequest.getRequestId(), result.getRequestId());
        assertEquals(result.getStatus(), RequestStatus.DIAGNOSIS_IN_PROCESS);
        assertNotNull(result.getConsultation());
    }

    public TestRequest getTestRequestByStatus(RequestStatus status) {
        TestRequest testRequest = getMockedTestRequest(status);
        consultationRepository.save(testRequest.getConsultation());
        labResultRepository.save(testRequest.getLabResult());
        return testRequestUpdateService.saveTestRequest(testRequest);
    }

    @Test
    @WithUserDetails(value = "doctor")
    public void calling_assignForConsultation_with_valid_test_request_id_should_throw_exception() {
        Long InvalidRequestId = -34L;
        ResponseStatusException responseStatusException = assertThrows(ResponseStatusException.class, () -> consultationController.assignForConsultation(InvalidRequestId));
        assertTrue(responseStatusException.getMessage().contains("Invalid ID"));

    }

    @Test
    @WithUserDetails(value = "doctor")
    public void calling_updateConsultation_with_valid_test_request_id_should_update_the_request_status_and_update_consultation_details() {

        TestRequest testRequest = getTestRequestByStatus(RequestStatus.DIAGNOSIS_IN_PROCESS);
        CreateConsultationRequest createConsultationRequest = getCreateConsultationRequest(testRequest);
        TestRequest testRequest_2 = getTestRequestByStatus(RequestStatus.DIAGNOSIS_IN_PROCESS);
        testRequest_2.setStatus(RequestStatus.COMPLETED);
        consultationController.updateConsultation(testRequest.getRequestId(), createConsultationRequest);
        assertEquals(testRequest.getRequestId(), testRequest_2.getRequestId());
        assertEquals(testRequest_2.getStatus(), RequestStatus.COMPLETED);
        assertEquals(testRequest.getConsultation().getSuggestion(), testRequest_2.getConsultation().getSuggestion());
    }


    @Test
    @WithUserDetails(value = "doctor")
    public void calling_updateConsultation_with_invalid_test_request_id_should_throw_exception() {
        Long InvalidRequestId = -34L;
        TestRequest testRequest = getTestRequestByStatus(RequestStatus.DIAGNOSIS_IN_PROCESS);
        CreateConsultationRequest createConsultationRequest = getCreateConsultationRequest(testRequest);
        ResponseStatusException responseStatusException = assertThrows(ResponseStatusException.class, () -> consultationController.updateConsultation(InvalidRequestId, createConsultationRequest));
        assertNotNull(responseStatusException);
        assertEquals(HttpStatus.BAD_REQUEST, responseStatusException.getStatus());
        assertTrue(responseStatusException.getMessage().contains("Invalid ID"));
    }

    @Test
    @WithUserDetails(value = "doctor")
    public void calling_updateConsultation_with_invalid_empty_status_should_throw_exception() {

        TestRequest testRequest = getTestRequestByStatus(RequestStatus.DIAGNOSIS_IN_PROCESS);
        CreateConsultationRequest createConsultationRequest = getCreateConsultationRequest(testRequest);
        createConsultationRequest.setSuggestion(null);
        ResponseStatusException responseStatusException = assertThrows(ResponseStatusException.class, () -> consultationController.updateConsultation(testRequest.requestId, createConsultationRequest));
        assertNotNull(responseStatusException);
        assertEquals(HttpStatus.BAD_REQUEST, responseStatusException.getStatus());
    }

    public CreateConsultationRequest getCreateConsultationRequest(TestRequest testRequest) {
        CreateConsultationRequest createConsultationRequest = new CreateConsultationRequest();
        if (testRequest.getLabResult().getResult().equals(TestStatus.POSITIVE)) {
            createConsultationRequest.setComments("comments");
            createConsultationRequest.setSuggestion(DoctorSuggestion.HOME_QUARANTINE);
        } else if (testRequest.getLabResult().getResult().equals(TestStatus.NEGATIVE)) {
            createConsultationRequest.setComments("Ok");
            createConsultationRequest.setSuggestion(DoctorSuggestion.NO_ISSUES);
        }
        return createConsultationRequest;
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
        LabResult labResult = new LabResult();
        labResult.setResult(TestStatus.NEGATIVE);
        Consultation consultation = new Consultation();
        consultation.setComments("comments");
        testRequest.setConsultation(consultation);
        testRequest.setLabResult(labResult);
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
package org.upgrad.upstac.testrequests.lab;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;
import org.upgrad.upstac.testrequests.TestRequest;
import org.upgrad.upstac.users.User;

import javax.transaction.Transactional;
import java.time.LocalDate;
import java.util.Optional;

@Service
@Validated
public class LabResultService {

    private static Logger logger = LoggerFactory.getLogger(LabResultService.class);
    @Autowired
    private LabResultRepository labResultRepository;

    /**
     * Assigns a Lab Result to a tester and returns it
     *
     * @param tester The tester for whom this test request needs to be assigned to
     * @param testRequest The test request
     * @return LabResult assigned to a tester
     */
    private LabResult createLabResult(User tester, TestRequest testRequest) {
        LabResult labResult = new LabResult();
        labResult.setTester(tester);
        labResult.setRequest(testRequest);
        LabResult savedLabResult = saveLabResult(labResult);
        return savedLabResult;
    }

    @Transactional
    LabResult saveLabResult(LabResult labResult) {
        return labResultRepository.save(labResult);
    }

    public LabResult assignForLabTest(TestRequest testRequest, User tester) {
        return createLabResult(tester, testRequest);
    }

    /**
     * Fetches the existing lab result assigned to a tester and adds all testign details
     *
     * @param testRequest The test request
     * @param createLabResult The lab request with all the tests
     * @return LabResult The lab result with all the test details added
     */
    public LabResult updateLabTest(TestRequest testRequest, CreateLabResult createLabResult) {
        Optional<LabResult> optionalLabResult = labResultRepository.findByRequest(testRequest);
        LabResult labResult = optionalLabResult.get();
        labResult.setRequest(testRequest);
        labResult.setBloodPressure(createLabResult.getBloodPressure());
        labResult.setComments(createLabResult.getComments());
        labResult.setHeartBeat(createLabResult.getHeartBeat());
        labResult.setOxygenLevel(createLabResult.getOxygenLevel());
        labResult.setTemperature(createLabResult.getTemperature());
        labResult.setResult(createLabResult.getResult());
        labResult.setUpdatedOn(LocalDate.now());
        LabResult savedLabResult = saveLabResult(labResult);
        return savedLabResult;
    }
}

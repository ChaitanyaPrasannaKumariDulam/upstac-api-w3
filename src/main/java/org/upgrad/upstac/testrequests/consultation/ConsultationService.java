package org.upgrad.upstac.testrequests.consultation;

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
public class ConsultationService {

    private static Logger logger = LoggerFactory.getLogger(ConsultationService.class);
    @Autowired
    private ConsultationRepository consultationRepository;

    /**
     * Creates a new consultation by assigned the doctor and the test request and returns the consultation
     *
     * @param testRequest The test request
     * @param doctor The doctor for whom the test request needs to be assigned
     * @return Consultation The consultation assigned to a doctor
     */
    @Transactional
    public Consultation assignForConsultation(TestRequest testRequest, User doctor) {
        Consultation consultation = new Consultation();
        consultation.setDoctor(doctor);
        consultation.setRequest(testRequest);
        return consultationRepository.save(consultation);
    }

    /**
     * This method fetches existing consultation which was assigned to a doctor and updates the consultation with details provided
     *
     * @param testRequest The test request
     * @param createConsultationRequest The object with all consultation details added by doctor
     * @return Consultation The doctors consultation
     */
    public Consultation updateConsultation(TestRequest testRequest, CreateConsultationRequest createConsultationRequest) {
        Optional<Consultation> optionalConsultation = consultationRepository.findByRequest(testRequest);
        Consultation consultation = optionalConsultation.get();
        consultation.setComments(createConsultationRequest.getComments());
        consultation.setSuggestion(createConsultationRequest.getSuggestion());
        consultation.setUpdatedOn(LocalDate.now());
        return consultationRepository.save(consultation);
    }
}

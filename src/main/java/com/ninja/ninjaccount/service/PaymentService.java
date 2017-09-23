package com.ninja.ninjaccount.service;

import com.ninja.ninjaccount.domain.Payment;
import com.ninja.ninjaccount.domain.User;
import com.ninja.ninjaccount.domain.enumeration.PlanType;
import com.ninja.ninjaccount.repository.PaymentRepository;
import com.ninja.ninjaccount.service.dto.PaymentDTO;
import com.ninja.ninjaccount.service.mapper.PaymentMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Service Implementation for managing Payment.
 */
@Service
@Transactional
public class PaymentService {

    private final Logger log = LoggerFactory.getLogger(PaymentService.class);

    private final PaymentRepository paymentRepository;

    private final PaymentMapper paymentMapper;
    public PaymentService(PaymentRepository paymentRepository, PaymentMapper paymentMapper) {
        this.paymentRepository = paymentRepository;
        this.paymentMapper = paymentMapper;
    }

    /**
     * Save a payment.
     *
     * @param paymentDTO the entity to save
     * @return the persisted entity
     */
    public PaymentDTO save(PaymentDTO paymentDTO) {
        log.debug("Request to save Payment : {}", paymentDTO);
        Payment payment = paymentMapper.toEntity(paymentDTO);
        payment = paymentRepository.save(payment);
        return paymentMapper.toDto(payment);
    }

    /**
     *  Get all the payments.
     *
     *  @return the list of entities
     */
    @Transactional(readOnly = true)
    public List<PaymentDTO> findAll() {
        log.debug("Request to get all Payments");
        return paymentRepository.findAll().stream()
            .map(paymentMapper::toDto)
            .collect(Collectors.toCollection(LinkedList::new));
    }

    /**
     *  Get one payment by id.
     *
     *  @param id the id of the entity
     *  @return the entity
     */
    @Transactional(readOnly = true)
    public PaymentDTO findOne(Long id) {
        log.debug("Request to get Payment : {}", id);
        Payment payment = paymentRepository.findOne(id);
        return paymentMapper.toDto(payment);
    }

    /**
     *  Delete the  payment by id.
     *
     *  @param id the id of the entity
     */
    public void delete(Long id) {
        log.debug("Request to delete Payment : {}", id);
        paymentRepository.delete(id);
    }

    /**
     * Get payment information for one user by login
     *
     * @param login The login of the user
     * @return The payment information
     */
    public PaymentDTO findPaymentByLogin(String login) {
        Optional<Payment> payment = paymentRepository.findOneByUserLogin(login);
        if(payment.isPresent()){
            return paymentMapper.toDto(payment.get());
        }else{
            return null;
        }
    }

    /**
     * Create a payment for user when first register
     * With one month free
     *
     * @param user the user
     */
    public void createRegistrationPaymentForUser(User user){
        PaymentDTO paymentDTO = new PaymentDTO();
        paymentDTO.setPaid(false);
        paymentDTO.setPlanType(PlanType.FREE);
        paymentDTO.setSubscriptionDate(LocalDate.now().plusMonths(1));
        paymentDTO.setPrice(10);
        paymentDTO.setUserId(user.getId());
        paymentDTO.setUserLogin(user.getLogin());
        save(paymentDTO);
    }

}
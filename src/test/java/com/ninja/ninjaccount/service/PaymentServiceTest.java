package com.ninja.ninjaccount.service;

import com.ninja.ninjaccount.NinjaccountApp;
import com.ninja.ninjaccount.data.PaymentData;
import com.ninja.ninjaccount.data.UserData;
import com.ninja.ninjaccount.domain.Payment;
import com.ninja.ninjaccount.domain.User;
import com.ninja.ninjaccount.domain.enumeration.PlanType;
import com.ninja.ninjaccount.repository.PaymentRepository;
import com.ninja.ninjaccount.repository.UserRepository;
import com.ninja.ninjaccount.service.billing.PaypalService;
import com.ninja.ninjaccount.service.billing.dto.CompletePaymentDTO;
import com.ninja.ninjaccount.service.billing.dto.PaypalStatus;
import com.ninja.ninjaccount.service.billing.dto.ReturnPaymentDTO;
import com.ninja.ninjaccount.service.dto.OperationAccountType;
import com.ninja.ninjaccount.service.dto.PaymentDTO;
import com.ninja.ninjaccount.service.dto.UserDTO;
import com.ninja.ninjaccount.service.exceptions.MaxAccountsException;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.cglib.core.Local;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.temporal.ChronoField;
import java.time.temporal.ChronoUnit;
import java.time.temporal.TemporalUnit;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = NinjaccountApp.class)
@Transactional
public class PaymentServiceTest {

    @Autowired
    private PaymentService paymentService;

    @Autowired
    private UserService userService;

    @Autowired
    private PaymentRepository paymentRepository;

    @MockBean
    private PaypalService paypalService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private UserData userData;

    @Autowired
    private PaymentData paymentData;

    @Test
    public void testCreatePaymentWhenRegister() {
        User user = new User();
        user.setEmail("lol@lol.com");
        user.setLogin("lol");
        user.setActivated(true);
        user.setPassword("loooool");
        user = userService.createUser(new UserDTO(user));

        paymentService.createRegistrationPaymentForUser(user);

        Optional<Payment> payment = paymentRepository.findOneByUserLogin("lol");

        assertThat(payment.isPresent()).isTrue();
        assertThat(payment.get().getPlanType()).isEqualTo(PlanType.FREE);
        assertThat(payment.get().isPaid()).isFalse();
        assertThat(payment.get().getSubscriptionDate()).isEqualTo(LocalDate.now());
    }

    @Test
    public void testCheckReachLimitNotReached() throws MaxAccountsException {
        User user = new User();
        user.setEmail("lol@lol.com");
        user.setLogin("lol");
        user.setActivated(true);
        user.setPassword("loooool");
        user = userService.createUser(new UserDTO(user));

        paymentService.createRegistrationPaymentForUser(user);

        Integer nbAccounts = paymentService.checkReachLimitAccounts(user.getLogin(), OperationAccountType.CREATE, 1);

        assertThat(nbAccounts).isNotNull();
        assertThat(nbAccounts).isEqualTo(2);
    }

    @Test(expected = MaxAccountsException.class)
    public void testCheckReachLimitReached() throws MaxAccountsException {
        User user = new User();
        user.setEmail("lol@lol.com");
        user.setLogin("lol");
        user.setActivated(true);
        user.setPassword("loooool");
        user = userService.createUser(new UserDTO(user));

        paymentService.createRegistrationPaymentForUser(user);
        paymentService.checkReachLimitAccounts(user.getLogin(), OperationAccountType.CREATE, 1000);
    }

    @Test
    public void testCheckReachLimitUpdateDontMove() throws MaxAccountsException {
        User user = new User();
        user.setEmail("lol@lol.com");
        user.setLogin("lol");
        user.setActivated(true);
        user.setPassword("loooool");
        user = userService.createUser(new UserDTO(user));

        paymentService.createRegistrationPaymentForUser(user);

        Integer nbAccounts = paymentService.checkReachLimitAccounts(user.getLogin(), OperationAccountType.UPDATE, 1);

        assertThat(nbAccounts).isNotNull();
        assertThat(nbAccounts).isEqualTo(1);
    }

    @Test
    public void testCheckReachLimitDelete() throws MaxAccountsException {
        User user = new User();
        user.setEmail("lol@lol.com");
        user.setLogin("lol");
        user.setActivated(true);
        user.setPassword("loooool");
        user = userService.createUser(new UserDTO(user));

        paymentService.createRegistrationPaymentForUser(user);

        Integer nbAccounts = paymentService.checkReachLimitAccounts(user.getLogin(), OperationAccountType.DELETE, 2);

        assertThat(nbAccounts).isNotNull();
        assertThat(nbAccounts).isEqualTo(1);
    }

    @Test
    public void testInitPaymentOneTimeWorkflowYearShouldWork() throws MaxAccountsException {
        User user = new User();
        user.setEmail("lol@lol.com");
        user.setLogin("lol");
        user.setActivated(true);
        user.setPassword("loooool");
        user = userService.createUser(new UserDTO(user));

        user.setActivated(false);
        userRepository.save(user);

        ReturnPaymentDTO returnPaymentDTOMock = new ReturnPaymentDTO();
        returnPaymentDTOMock.setStatus("success");
        returnPaymentDTOMock.setReturnUrl("http://getrich.com");
        returnPaymentDTOMock.setPaymentId("PAY-LOL");

        Mockito.when(paypalService.createOneTimePayment(any(PlanType.class), eq(user.getLogin()))).thenReturn(returnPaymentDTOMock);

        paymentService.createRegistrationPaymentForUser(user);

        Optional<ReturnPaymentDTO> returnPaymentDTO = paymentService.initOneTimePaymentWorkflow(PlanType.PREMIUMYEAR, user.getLogin());

        Optional<Payment> payment = paymentRepository.findOneByUserLogin(user.getLogin());

        assertThat(returnPaymentDTO).isPresent();
        assertThat(payment).isPresent();
        assertThat(payment.get().getLastPaymentId()).isEqualTo(returnPaymentDTOMock.getPaymentId());
    }

    @Test
    public void testInitOneTimePaymentWorkflowYearShouldFail() {
        User user = new User();
        user.setEmail("lol@lol.com");
        user.setLogin("lol");
        user.setActivated(false);
        user.setPassword("loooool");
        user = userService.createUser(new UserDTO(user));

        user.setActivated(false);
        userRepository.save(user);

        ReturnPaymentDTO returnPaymentDTOMock = new ReturnPaymentDTO();
        returnPaymentDTOMock.setStatus("failure");

        Mockito.when(paypalService.createOneTimePayment(any(PlanType.class), eq(user.getLogin()))).thenReturn(returnPaymentDTOMock);

        paymentService.createRegistrationPaymentForUser(user);

        Optional<ReturnPaymentDTO> returnPaymentDTO = paymentService.initOneTimePaymentWorkflow(PlanType.PREMIUMYEAR, user.getLogin());

        Optional<Payment> payment = paymentRepository.findOneByUserLogin(user.getLogin());
        Optional<User> userWithAuthoritiesByLogin = userService.getUserWithAuthoritiesByLogin(user.getLogin());

        assertThat(userWithAuthoritiesByLogin).isNotPresent();
        assertThat(returnPaymentDTO).isNotPresent();
        assertThat(payment).isNotPresent();
    }

    @Test
    public void testCompleteOneTimePaymentShouldWork() {
        String paymentId = "PAY-ID";

        User user = new User();
        user.setEmail("lol@lol.com");
        user.setLogin("lol");
        user.setActivated(false);
        user.setPassword("loooool");
        user = userService.createUser(new UserDTO(user));

        user.setActivated(false);
        user = userRepository.save(user);

        PaymentDTO paymentDTO = new PaymentDTO();
        paymentDTO.setPaid(false);
        paymentDTO.setPlanType(PlanType.FREE);
        paymentDTO.setSubscriptionDate(LocalDate.now());
        paymentDTO.setPrice(PlanType.FREE.getPrice());
        paymentDTO.setUserId(user.getId());
        paymentDTO.setUserLogin(user.getLogin());
        paymentDTO.setLastPaymentId(paymentId);

        paymentService.save(paymentDTO);

        //Create the mock object for the paypal call
        ReturnPaymentDTO returnPaymentDTOMock = new ReturnPaymentDTO();
        returnPaymentDTOMock.setStatus("success");
        returnPaymentDTOMock.setPaymentId(paymentId);
        returnPaymentDTOMock.setPlanType(PlanType.PREMIUMYEAR);

        //Mock the call
        Mockito.when(paypalService.completeOneTimePaymentWorkflow(any(CompletePaymentDTO.class))).thenReturn(Optional.of(returnPaymentDTOMock));

        CompletePaymentDTO completePaymentDTO = new CompletePaymentDTO();
        completePaymentDTO.setPayerId("PAY-BLABLA");
        completePaymentDTO.setPaymentId(paymentId);

        Optional<ReturnPaymentDTO> returnPaymentDTO = paymentService.completeOneTimePaymentWorkflow(completePaymentDTO);

        Optional<Payment> payment = paymentRepository.findOneByUserLogin(user.getLogin());

        assertThat(returnPaymentDTO).isPresent();
        assertThat(payment).isPresent();
        assertThat(payment.get().getPlanType()).isEqualTo(PlanType.PREMIUMYEAR);
        assertThat(payment.get().getPrice()).isEqualTo(PlanType.PREMIUMYEAR.getPrice());
        assertThat(payment.get().getValidUntil()).isEqualTo(LocalDate.now()
            .plus(PlanType.PREMIUMYEAR.getUnitAmountValidity(), PlanType.PREMIUMYEAR.getUnit()));
    }

    @Test
    public void testCompleteOneTimePaymentShouldFail() {
        String paymentId = "PAY-ID";

        User user = new User();
        user.setEmail("lol@lol.com");
        user.setLogin("lol");
        user.setActivated(false);
        user.setPassword("loooool");
        user = userService.createUser(new UserDTO(user));

        user.setActivated(false);
        user = userRepository.save(user);

        PaymentDTO paymentDTO = new PaymentDTO();
        paymentDTO.setPaid(false);
        paymentDTO.setPlanType(PlanType.FREE);
        paymentDTO.setSubscriptionDate(LocalDate.now());
        paymentDTO.setPrice(PlanType.FREE.getPrice());
        paymentDTO.setUserId(user.getId());
        paymentDTO.setUserLogin(user.getLogin());
        paymentDTO.setLastPaymentId(paymentId);
        paymentDTO.setRecurring(false);

        paymentService.save(paymentDTO);

        //Create the mock object for the paypal call
        ReturnPaymentDTO returnPaymentDTOMock = new ReturnPaymentDTO();
        returnPaymentDTOMock.setStatus("failure");
        returnPaymentDTOMock.setPaymentId(paymentId);
        returnPaymentDTOMock.setPlanType(PlanType.PREMIUMYEAR);

        //Mock the call
        Mockito.when(paypalService.completeOneTimePaymentWorkflow(any(CompletePaymentDTO.class))).thenReturn(Optional.of(returnPaymentDTOMock));

        CompletePaymentDTO completePaymentDTO = new CompletePaymentDTO();
        completePaymentDTO.setPayerId("PAY-BLABLA");
        completePaymentDTO.setPaymentId(paymentId);

        Optional<ReturnPaymentDTO> returnPaymentDTO = paymentService.completeOneTimePaymentWorkflow(completePaymentDTO);

        Optional<Payment> payment = paymentRepository.findOneByUserLogin(user.getLogin());

        Optional<User> userWithAuthoritiesByLogin = userService.getUserWithAuthoritiesByLogin(user.getLogin());

        assertThat(userWithAuthoritiesByLogin).isNotPresent();
        assertThat(returnPaymentDTO).isNotPresent();
        assertThat(payment).isNotPresent();
    }

    @Test
    public void testInitPaymentRecurringWorkflowYearShouldWork() throws MaxAccountsException {
        User user = new User();
        user.setEmail("lol@lol.com");
        user.setLogin("lol");
        user.setActivated(true);
        user.setPassword("loooool");
        user = userService.createUser(new UserDTO(user));

        user.setActivated(false);
        userRepository.save(user);

        ReturnPaymentDTO returnPaymentDTOMock = new ReturnPaymentDTO();
        returnPaymentDTOMock.setStatus(PaypalStatus.SUCCESS.getName());
        returnPaymentDTOMock.setReturnUrl("http://getrich.com");
        returnPaymentDTOMock.setTokenForRecurring("TOKEN-LOL");
        returnPaymentDTOMock.setBillingPlanId("45456454454654");

        Mockito.when(paypalService.createRecurringPayment(any(PlanType.class), eq(user.getLogin()), any(LocalDate.class), any(Boolean.class))).thenReturn(returnPaymentDTOMock);

        paymentService.createRegistrationPaymentForUser(user);

        Optional<ReturnPaymentDTO> returnPaymentDTO = paymentService.initRecurringPaymentWorkflow(PlanType.PREMIUMYEAR, user.getLogin());

        Optional<Payment> payment = paymentRepository.findOneByUserLogin(user.getLogin());

        assertThat(returnPaymentDTO).isPresent();
        assertThat(payment).isPresent();
        assertThat(payment.get().getTokenRecurring()).isEqualTo(returnPaymentDTOMock.getTokenForRecurring());
        assertThat(payment.get().getBillingPlanId()).isEqualTo(returnPaymentDTOMock.getBillingPlanId());
    }

    @Test
    public void testInitPaymentRecurringWorkflowYearShouldFail() throws MaxAccountsException {
        User user = new User();
        user.setEmail("lol@lol.com");
        user.setLogin("lol");
        user.setActivated(true);
        user.setPassword("loooool");
        user = userService.createUser(new UserDTO(user));

        user.setActivated(false);
        userRepository.save(user);

        ReturnPaymentDTO returnPaymentDTOMock = new ReturnPaymentDTO();
        returnPaymentDTOMock.setStatus(PaypalStatus.FAILURE.getName());

        Mockito.when(paypalService.createRecurringPayment(any(PlanType.class), eq(user.getLogin()), any(LocalDate.class), any(Boolean.class))).thenReturn(returnPaymentDTOMock);

        paymentService.createRegistrationPaymentForUser(user);

        Optional<ReturnPaymentDTO> returnPaymentDTO = paymentService.initRecurringPaymentWorkflow(PlanType.PREMIUMYEAR, user.getLogin());

        assertThat(returnPaymentDTO).isNotPresent();
    }

    @Test
    @WithMockUser("lol")
    public void testCompleteRecurringPaymentShouldWork() {
        String paymentId = "PAY-ID";
        String token = "UNTOKEN";

        User user = new User();
        user.setEmail("lol@lol.com");
        user.setLogin("lol");
        user.setActivated(false);
        user.setPassword("loooool");
        user = userService.createUser(new UserDTO(user));

        user.setActivated(false);
        user = userRepository.save(user);

        PaymentDTO paymentDTO = new PaymentDTO();
        paymentDTO.setPaid(false);
        paymentDTO.setPlanType(PlanType.FREE);
        paymentDTO.setSubscriptionDate(LocalDate.now());
        paymentDTO.setPrice(PlanType.FREE.getPrice());
        paymentDTO.setUserId(user.getId());
        paymentDTO.setUserLogin(user.getLogin());
        paymentDTO.setLastPaymentId(paymentId);
        paymentDTO.setTokenRecurring(token);
        paymentDTO.setRecurring(true);

        paymentService.save(paymentDTO);

        //Create the mock object for the paypal call
        ReturnPaymentDTO returnPaymentDTOMock = new ReturnPaymentDTO();
        returnPaymentDTOMock.setStatus(PaypalStatus.SUCCESS.getName());
        returnPaymentDTOMock.setPaymentId(paymentId);
        returnPaymentDTOMock.setTokenForRecurring(token);
        returnPaymentDTOMock.setPlanType(PlanType.PREMIUMYEAR);

        //Mock the call
        Mockito.when(paypalService.completeRecurringPaymentWorkflow(anyString(), anyString())).thenReturn(returnPaymentDTOMock);

        CompletePaymentDTO completePaymentDTO = new CompletePaymentDTO();
        completePaymentDTO.setToken(token);

        Optional<ReturnPaymentDTO> returnPaymentDTO = paymentService.completeRecurringPaymentWorkflow(completePaymentDTO);

        Optional<Payment> payment = paymentRepository.findOneByUserLogin(user.getLogin());

        assertThat(returnPaymentDTO).isPresent();
        assertThat(payment).isPresent();
        assertThat(payment.get().getLastPaymentId()).isEqualTo(paymentId);
        assertThat(payment.get().getPlanType()).isEqualTo(PlanType.PREMIUMYEAR);
        assertThat(payment.get().getPrice()).isEqualTo(PlanType.PREMIUMYEAR.getPrice());
        assertThat(payment.get().getValidUntil()).isEqualTo(LocalDate.now()
            .plus(PlanType.PREMIUMYEAR.getUnitAmountValidity(), PlanType.PREMIUMYEAR.getUnit()));
        assertThat(payment.get().isRecurring()).isTrue();
    }

    @Test
    @WithMockUser("lol")
    public void testCompleteRecurringPaymentShouldFail() {
        String paymentId = "PAY-ID";
        String token = "UNTOKEN";

        User user = new User();
        user.setEmail("lol@lol.com");
        user.setLogin("lol");
        user.setActivated(false);
        user.setPassword("loooool");
        user = userService.createUser(new UserDTO(user));

        user.setActivated(false);
        user = userRepository.save(user);

        PaymentDTO paymentDTO = new PaymentDTO();
        paymentDTO.setPaid(false);
        paymentDTO.setPlanType(PlanType.FREE);
        paymentDTO.setSubscriptionDate(LocalDate.now());
        paymentDTO.setPrice(PlanType.FREE.getPrice());
        paymentDTO.setUserId(user.getId());
        paymentDTO.setUserLogin(user.getLogin());
        paymentDTO.setLastPaymentId(paymentId);
        paymentDTO.setTokenRecurring(token);
        paymentDTO.setRecurring(true);

        paymentService.save(paymentDTO);

        //Create the mock object for the paypal call
        ReturnPaymentDTO returnPaymentDTOMock = new ReturnPaymentDTO();
        returnPaymentDTOMock.setStatus(PaypalStatus.FAILURE.getName());

        //Mock the call
        Mockito.when(paypalService.completeRecurringPaymentWorkflow(anyString(), anyString())).thenReturn(returnPaymentDTOMock);

        CompletePaymentDTO completePaymentDTO = new CompletePaymentDTO();
        completePaymentDTO.setToken(token);

        Optional<ReturnPaymentDTO> returnPaymentDTO = paymentService.completeRecurringPaymentWorkflow(completePaymentDTO);

        assertThat(returnPaymentDTO).isPresent();
        assertThat(returnPaymentDTO.get().getStatus()).isEqualTo(PaypalStatus.FAILURE.getName());
    }

    @Test
    @WithMockUser("lol")
    public void testCancelRecurringPaymentShouldWork() {
        String paymentId = "PAY-ID";
        String token = "UNTOKEN";

        User user = new User();
        user.setEmail("lol@lol.com");
        user.setLogin("lol");
        user.setActivated(false);
        user.setPassword("loooool");
        user = userService.createUser(new UserDTO(user));

        user.setActivated(false);
        user = userRepository.save(user);

        PaymentDTO paymentDTO = new PaymentDTO();
        paymentDTO.setPaid(true);
        paymentDTO.setPlanType(PlanType.PREMIUMYEAR);
        paymentDTO.setSubscriptionDate(LocalDate.now());
        paymentDTO.setPrice(PlanType.PREMIUMYEAR.getPrice());
        paymentDTO.setUserId(user.getId());
        paymentDTO.setUserLogin(user.getLogin());
        paymentDTO.setLastPaymentId(paymentId);
        paymentDTO.setTokenRecurring(token);
        paymentDTO.setRecurring(true);

        paymentService.save(paymentDTO);

        //Create the mock object for the paypal call
        ReturnPaymentDTO returnPaymentDTOMock = new ReturnPaymentDTO();
        returnPaymentDTOMock.setStatus(PaypalStatus.SUCCESS.getName());
        returnPaymentDTOMock.setPaymentId(paymentId);

        //Mock the call
        Mockito.when(paypalService.cancelRecurringPayment(anyString(), anyString())).thenReturn(returnPaymentDTOMock);

        Optional<ReturnPaymentDTO> returnPaymentDTO = paymentService.cancelRecurringPaymentWorkflow(user.getLogin());

        Optional<Payment> payment = paymentRepository.findOneByUserLogin(user.getLogin());

        assertThat(returnPaymentDTO).isPresent();
        assertThat(returnPaymentDTO.get().getStatus()).isEqualTo(PaypalStatus.SUCCESS.getName());
        assertThat(returnPaymentDTO.get().getPaymentId()).isEqualTo(paymentId);
        assertThat(payment).isPresent();
        assertThat(payment.get().isRecurring()).isFalse();
    }

    @Test
    @WithMockUser("lol")
    public void testCancelRecurringPaymentShouldFail() {
        String paymentId = "PAY-ID";
        String token = "UNTOKEN";

        User user = new User();
        user.setEmail("lol@lol.com");
        user.setLogin("lol");
        user.setActivated(false);
        user.setPassword("loooool");
        user = userService.createUser(new UserDTO(user));

        user.setActivated(false);
        user = userRepository.save(user);

        PaymentDTO paymentDTO = new PaymentDTO();
        paymentDTO.setPaid(true);
        paymentDTO.setPlanType(PlanType.PREMIUMYEAR);
        paymentDTO.setSubscriptionDate(LocalDate.now());
        paymentDTO.setPrice(PlanType.PREMIUMYEAR.getPrice());
        paymentDTO.setUserId(user.getId());
        paymentDTO.setUserLogin(user.getLogin());
        paymentDTO.setLastPaymentId(paymentId);
        paymentDTO.setTokenRecurring(token);
        paymentDTO.setRecurring(true);

        paymentService.save(paymentDTO);

        //Create the mock object for the paypal call
        ReturnPaymentDTO returnPaymentDTOMock = new ReturnPaymentDTO();
        returnPaymentDTOMock.setStatus(PaypalStatus.FAILURE.getName());

        //Mock the call
        Mockito.when(paypalService.cancelRecurringPayment(anyString(), anyString())).thenReturn(returnPaymentDTOMock);

        Optional<ReturnPaymentDTO> returnPaymentDTO = paymentService.cancelRecurringPaymentWorkflow(user.getLogin());

        Optional<Payment> payment = paymentRepository.findOneByUserLogin(user.getLogin());

        assertThat(returnPaymentDTO).isPresent();
        assertThat(returnPaymentDTO.get().getStatus()).isEqualTo(PaypalStatus.FAILURE.getName());
        assertThat(returnPaymentDTO.get().getPaymentId()).isEqualTo(paymentId);
        assertThat(payment).isPresent();
        assertThat(payment.get().isRecurring()).isTrue();
    }

    @Test
    @WithMockUser("lol")
    public void testComputeStartDateShoulWorkWithoutValidateUntil() {
        String paymentId = "PAY-ID";
        String token = "UNTOKEN";

        User user = new User();
        user.setEmail("lol@lol.com");
        user.setLogin("lol");
        user.setActivated(false);
        user.setPassword("loooool");
        user = userService.createUser(new UserDTO(user));

        user.setActivated(false);
        user = userRepository.save(user);

        PaymentDTO paymentDTO = new PaymentDTO();
        paymentDTO.setPaid(true);
        paymentDTO.setPlanType(PlanType.FREE);
        paymentDTO.setSubscriptionDate(LocalDate.now());
        paymentDTO.setPrice(PlanType.FREE.getPrice());
        paymentDTO.setUserId(user.getId());
        paymentDTO.setUserLogin(user.getLogin());
        paymentDTO.setLastPaymentId(paymentId);
        paymentDTO.setTokenRecurring(token);
        paymentDTO.setRecurring(true);

        paymentService.save(paymentDTO);

        Optional<Payment> payment = paymentRepository.findOneByUserLogin(user.getLogin());

        PlanType planType = PlanType.PREMIUMYEAR;

        LocalDate startDate = paymentService.computeStartDate(payment.get(), planType);

        LocalDate now = LocalDate.now().plus(planType.getUnitAmountValidity(), planType.getUnit());

        assertThat(startDate.getDayOfMonth()).isEqualTo(now.getDayOfMonth());
        assertThat(startDate.get(ChronoField.MONTH_OF_YEAR)).isEqualTo(now.get(ChronoField.MONTH_OF_YEAR));
        assertThat(startDate.get(ChronoField.YEAR)).isEqualTo(now.get(ChronoField.YEAR));

    }

    @Test
    @WithMockUser("lol")
    public void testComputeStartDateShoulWorkWithValidateUntil() {
        String paymentId = "PAY-ID";
        String token = "UNTOKEN";

        User user = new User();
        user.setEmail("lol@lol.com");
        user.setLogin("lol");
        user.setActivated(false);
        user.setPassword("loooool");
        user = userService.createUser(new UserDTO(user));

        user.setActivated(false);
        user = userRepository.save(user);

        PaymentDTO paymentDTO = new PaymentDTO();
        paymentDTO.setPaid(true);
        paymentDTO.setPlanType(PlanType.FREE);
        paymentDTO.setSubscriptionDate(LocalDate.now());
        paymentDTO.setPrice(PlanType.FREE.getPrice());
        paymentDTO.setUserId(user.getId());
        paymentDTO.setUserLogin(user.getLogin());
        paymentDTO.setLastPaymentId(paymentId);
        paymentDTO.setTokenRecurring(token);
        paymentDTO.setRecurring(true);
        paymentDTO.setValidUntil(LocalDate.now().plus(2, ChronoUnit.MONTHS));

        paymentService.save(paymentDTO);

        Optional<Payment> payment = paymentRepository.findOneByUserLogin(user.getLogin());

        PlanType planType = PlanType.PREMIUMMONTH;

        LocalDate startDate = paymentService.computeStartDate(payment.get(), planType);

        LocalDate correctDate = LocalDate.now().plus(2, planType.getUnit());

        assertThat(startDate.getDayOfMonth()).isEqualTo(correctDate.getDayOfMonth());
        assertThat(startDate.get(ChronoField.MONTH_OF_YEAR)).isEqualTo(correctDate.get(ChronoField.MONTH_OF_YEAR));
        assertThat(startDate.get(ChronoField.YEAR)).isEqualTo(correctDate.get(ChronoField.YEAR));
    }

    @Test
    public void testCheckSubscriptionFromPaypalShouldDeactivate() {
        User user = userData.createUserJohnDoe();
        paymentData.createRegistrationPaymentForUser(user, PlanType.PREMIUMYEAR, true, LocalDate.now(), true);

        //Mock the call
        Mockito.when(paypalService.checkAgreementStillActive(anyString())).thenReturn(PaypalStatus.CANCELED);

        paymentService.checkSubscriptionFromPaypal();
        Optional<Payment> paymentToVerify = paymentRepository.findOneByUserLogin(user.getLogin());

        assertThat(paymentToVerify.get().isRecurring()).isFalse();
    }

    @Test
    public void testCheckSubscriptionFromPaypalShouldDoNothing() {
        User user = userData.createUserJohnDoe();
        paymentData.createRegistrationPaymentForUser(user, PlanType.PREMIUMYEAR, true, LocalDate.now(), true);

        //Mock the call
        Mockito.when(paypalService.checkAgreementStillActive(anyString())).thenReturn(PaypalStatus.ACTIVE);

        paymentService.checkSubscriptionFromPaypal();
        Optional<Payment> paymentToVerify = paymentRepository.findOneByUserLogin(user.getLogin());

        assertThat(paymentToVerify.get().isRecurring()).isTrue();
    }
}

package com.ninja.ninjaccount.web.rest;

import com.ninja.ninjaccount.NinjaccountApp;
import com.ninja.ninjaccount.domain.Payment;
import com.ninja.ninjaccount.domain.User;
import com.ninja.ninjaccount.domain.enumeration.PlanType;
import com.ninja.ninjaccount.repository.PaymentRepository;
import com.ninja.ninjaccount.service.PaymentService;
import com.ninja.ninjaccount.service.UserService;
import com.ninja.ninjaccount.service.dto.PaymentDTO;
import com.ninja.ninjaccount.service.dto.UserDTO;
import com.ninja.ninjaccount.service.mapper.PaymentMapper;
import com.ninja.ninjaccount.web.rest.errors.ExceptionTranslator;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.MockitoAnnotations;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.web.PageableHandlerMethodArgumentResolver;
import org.springframework.http.MediaType;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.Validator;

import javax.persistence.EntityManager;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.List;

import static com.ninja.ninjaccount.web.rest.TestUtil.createFormattingConversionService;
import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.hasItem;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
/**
 * Test class for the PaymentResource REST controller.
 *
 * @see PaymentResource
 */
@RunWith(SpringRunner.class)
@SpringBootTest(classes = NinjaccountApp.class)
public class PaymentResourceIntTest {

    private static final LocalDate DEFAULT_SUBSCRIPTION_DATE = LocalDate.ofEpochDay(0L);
    private static final LocalDate UPDATED_SUBSCRIPTION_DATE = LocalDate.now(ZoneId.systemDefault());

    private static final Integer DEFAULT_PRICE = 1;
    private static final Integer UPDATED_PRICE = 2;

    private static final PlanType DEFAULT_PLAN_TYPE = PlanType.FREE;
    private static final PlanType UPDATED_PLAN_TYPE = PlanType.PREMIUMYEAR;

    private static final Boolean DEFAULT_PAID = false;
    private static final Boolean UPDATED_PAID = true;

    private static final LocalDate DEFAULT_VALID_UNTIL = LocalDate.ofEpochDay(0L);
    private static final LocalDate UPDATED_VALID_UNTIL = LocalDate.now(ZoneId.systemDefault());

    private static final String DEFAULT_LAST_PAYMENT_ID = "AAAAAAAAAA";
    private static final String UPDATED_LAST_PAYMENT_ID = "BBBBBBBBBB";

    private static final Boolean DEFAULT_RECURRING = false;
    private static final Boolean UPDATED_RECURRING = true;

    private static final String DEFAULT_BILLING_PLAN_ID = "AAAAAAAAAA";
    private static final String UPDATED_BILLING_PLAN_ID = "BBBBBBBBBB";

    private static final String DEFAULT_TOKEN_RECURRING = "AAAAAAAAAA";
    private static final String UPDATED_TOKEN_RECURRING = "BBBBBBBBBB";

    @Autowired
    private PaymentRepository paymentRepository;

    @Autowired
    private PaymentMapper paymentMapper;

    @Autowired
    private PaymentService paymentService;

    @Autowired
    private UserService userService;

    @Autowired
    private MappingJackson2HttpMessageConverter jacksonMessageConverter;

    @Autowired
    private PageableHandlerMethodArgumentResolver pageableArgumentResolver;

    @Autowired
    private ExceptionTranslator exceptionTranslator;

    @Autowired
    private EntityManager em;

    @Autowired
    private Validator validator;

    private MockMvc restPaymentMockMvc;

    private Payment payment;

    @Before
    public void setup() {
        MockitoAnnotations.initMocks(this);
        final PaymentResource paymentResource = new PaymentResource(paymentService);
        this.restPaymentMockMvc = MockMvcBuilders.standaloneSetup(paymentResource)
            .setCustomArgumentResolvers(pageableArgumentResolver)
            .setControllerAdvice(exceptionTranslator)
            .setConversionService(createFormattingConversionService())
            .setMessageConverters(jacksonMessageConverter)
            .setValidator(validator).build();
    }

    /**
     * Create an entity for this test.
     *
     * This is a static method, as tests for other entities might also need it,
     * if they test an entity which requires the current entity.
     */
    public static Payment createEntity(EntityManager em) {
        Payment payment = new Payment()
            .subscriptionDate(DEFAULT_SUBSCRIPTION_DATE)
            .price(DEFAULT_PRICE)
            .planType(DEFAULT_PLAN_TYPE)
            .paid(DEFAULT_PAID)
            .validUntil(DEFAULT_VALID_UNTIL)
            .lastPaymentId(DEFAULT_LAST_PAYMENT_ID)
            .recurring(DEFAULT_RECURRING)
            .billingPlanId(DEFAULT_BILLING_PLAN_ID)
            .tokenRecurring(DEFAULT_TOKEN_RECURRING);
        return payment;
    }

    @Before
    public void initTest() {
        payment = createEntity(em);
    }

    @Test
    @Transactional
    public void createPayment() throws Exception {
        int databaseSizeBeforeCreate = paymentRepository.findAll().size();

        // Create the Payment
        PaymentDTO paymentDTO = paymentMapper.toDto(payment);
        restPaymentMockMvc.perform(post("/api/payments")
            .contentType(TestUtil.APPLICATION_JSON_UTF8)
            .content(TestUtil.convertObjectToJsonBytes(paymentDTO)))
            .andExpect(status().isCreated());

        // Validate the Payment in the database
        List<Payment> paymentList = paymentRepository.findAll();
        assertThat(paymentList).hasSize(databaseSizeBeforeCreate + 1);
        Payment testPayment = paymentList.get(paymentList.size() - 1);
        assertThat(testPayment.getSubscriptionDate()).isEqualTo(DEFAULT_SUBSCRIPTION_DATE);
        assertThat(testPayment.getPrice()).isEqualTo(DEFAULT_PRICE);
        assertThat(testPayment.getPlanType()).isEqualTo(DEFAULT_PLAN_TYPE);
        assertThat(testPayment.isPaid()).isEqualTo(DEFAULT_PAID);
        assertThat(testPayment.getValidUntil()).isEqualTo(DEFAULT_VALID_UNTIL);
        assertThat(testPayment.getLastPaymentId()).isEqualTo(DEFAULT_LAST_PAYMENT_ID);
        assertThat(testPayment.isRecurring()).isEqualTo(DEFAULT_RECURRING);
        assertThat(testPayment.getBillingPlanId()).isEqualTo(DEFAULT_BILLING_PLAN_ID);
        assertThat(testPayment.getTokenRecurring()).isEqualTo(DEFAULT_TOKEN_RECURRING);
    }

    @Test
    @Transactional
    public void createPaymentWithExistingId() throws Exception {
        int databaseSizeBeforeCreate = paymentRepository.findAll().size();

        // Create the Payment with an existing ID
        payment.setId(1L);
        PaymentDTO paymentDTO = paymentMapper.toDto(payment);

        // An entity with an existing ID cannot be created, so this API call must fail
        restPaymentMockMvc.perform(post("/api/payments")
            .contentType(TestUtil.APPLICATION_JSON_UTF8)
            .content(TestUtil.convertObjectToJsonBytes(paymentDTO)))
            .andExpect(status().isBadRequest());

        // Validate the Payment in the database
        List<Payment> paymentList = paymentRepository.findAll();
        assertThat(paymentList).hasSize(databaseSizeBeforeCreate);
    }

    @Test
    @Transactional
    public void checkSubscriptionDateIsRequired() throws Exception {
        int databaseSizeBeforeTest = paymentRepository.findAll().size();
        // set the field null
        payment.setSubscriptionDate(null);

        // Create the Payment, which fails.
        PaymentDTO paymentDTO = paymentMapper.toDto(payment);

        restPaymentMockMvc.perform(post("/api/payments")
            .contentType(TestUtil.APPLICATION_JSON_UTF8)
            .content(TestUtil.convertObjectToJsonBytes(paymentDTO)))
            .andExpect(status().isBadRequest());

        List<Payment> paymentList = paymentRepository.findAll();
        assertThat(paymentList).hasSize(databaseSizeBeforeTest);
    }

    @Test
    @Transactional
    public void checkPriceIsRequired() throws Exception {
        int databaseSizeBeforeTest = paymentRepository.findAll().size();
        // set the field null
        payment.setPrice(null);

        // Create the Payment, which fails.
        PaymentDTO paymentDTO = paymentMapper.toDto(payment);

        restPaymentMockMvc.perform(post("/api/payments")
            .contentType(TestUtil.APPLICATION_JSON_UTF8)
            .content(TestUtil.convertObjectToJsonBytes(paymentDTO)))
            .andExpect(status().isBadRequest());

        List<Payment> paymentList = paymentRepository.findAll();
        assertThat(paymentList).hasSize(databaseSizeBeforeTest);
    }

    @Test
    @Transactional
    public void checkPlanTypeIsRequired() throws Exception {
        int databaseSizeBeforeTest = paymentRepository.findAll().size();
        // set the field null
        payment.setPlanType(null);

        // Create the Payment, which fails.
        PaymentDTO paymentDTO = paymentMapper.toDto(payment);

        restPaymentMockMvc.perform(post("/api/payments")
            .contentType(TestUtil.APPLICATION_JSON_UTF8)
            .content(TestUtil.convertObjectToJsonBytes(paymentDTO)))
            .andExpect(status().isBadRequest());

        List<Payment> paymentList = paymentRepository.findAll();
        assertThat(paymentList).hasSize(databaseSizeBeforeTest);
    }

    @Test
    @Transactional
    public void checkPaidIsRequired() throws Exception {
        int databaseSizeBeforeTest = paymentRepository.findAll().size();
        // set the field null
        payment.setPaid(null);

        // Create the Payment, which fails.
        PaymentDTO paymentDTO = paymentMapper.toDto(payment);

        restPaymentMockMvc.perform(post("/api/payments")
            .contentType(TestUtil.APPLICATION_JSON_UTF8)
            .content(TestUtil.convertObjectToJsonBytes(paymentDTO)))
            .andExpect(status().isBadRequest());

        List<Payment> paymentList = paymentRepository.findAll();
        assertThat(paymentList).hasSize(databaseSizeBeforeTest);
    }

    @Test
    @Transactional
    public void getAllPayments() throws Exception {
        // Initialize the database
        paymentRepository.saveAndFlush(payment);

        // Get all the paymentList
        restPaymentMockMvc.perform(get("/api/payments?sort=id,desc"))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8_VALUE))
            .andExpect(jsonPath("$.[*].id").value(hasItem(payment.getId().intValue())))
            .andExpect(jsonPath("$.[*].subscriptionDate").value(hasItem(DEFAULT_SUBSCRIPTION_DATE.toString())))
            .andExpect(jsonPath("$.[*].price").value(hasItem(DEFAULT_PRICE)))
            .andExpect(jsonPath("$.[*].planType").value(hasItem(DEFAULT_PLAN_TYPE.toString())))
            .andExpect(jsonPath("$.[*].paid").value(hasItem(DEFAULT_PAID.booleanValue())))
            .andExpect(jsonPath("$.[*].validUntil").value(hasItem(DEFAULT_VALID_UNTIL.toString())))
            .andExpect(jsonPath("$.[*].lastPaymentId").value(hasItem(DEFAULT_LAST_PAYMENT_ID)))
            .andExpect(jsonPath("$.[*].recurring").value(hasItem(DEFAULT_RECURRING.booleanValue())))
            .andExpect(jsonPath("$.[*].billingPlanId").value(hasItem(DEFAULT_BILLING_PLAN_ID)))
            .andExpect(jsonPath("$.[*].tokenRecurring").value(hasItem(DEFAULT_TOKEN_RECURRING)));
    }

    @Test
    @Transactional
    public void getPayment() throws Exception {
        // Initialize the database
        paymentRepository.saveAndFlush(payment);

        // Get the payment
        restPaymentMockMvc.perform(get("/api/payments/{id}", payment.getId()))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8_VALUE))
            .andExpect(jsonPath("$.id").value(payment.getId().intValue()))
            .andExpect(jsonPath("$.subscriptionDate").value(DEFAULT_SUBSCRIPTION_DATE.toString()))
            .andExpect(jsonPath("$.price").value(DEFAULT_PRICE))
            .andExpect(jsonPath("$.planType").value(DEFAULT_PLAN_TYPE.toString()))
            .andExpect(jsonPath("$.paid").value(DEFAULT_PAID.booleanValue()))
            .andExpect(jsonPath("$.validUntil").value(DEFAULT_VALID_UNTIL.toString()))
            .andExpect(jsonPath("$.lastPaymentId").value(DEFAULT_LAST_PAYMENT_ID))
            .andExpect(jsonPath("$.recurring").value(DEFAULT_RECURRING.booleanValue()))
            .andExpect(jsonPath("$.billingPlanId").value(DEFAULT_BILLING_PLAN_ID))
            .andExpect(jsonPath("$.tokenRecurring").value(DEFAULT_TOKEN_RECURRING));
    }

    @Test
    @Transactional
    public void getNonExistingPayment() throws Exception {
        // Get the payment
        restPaymentMockMvc.perform(get("/api/payments/{id}", Long.MAX_VALUE))
            .andExpect(status().isNotFound());
    }

    @Test
    @Transactional
    public void updatePayment() throws Exception {
        // Initialize the database
        paymentRepository.saveAndFlush(payment);

        int databaseSizeBeforeUpdate = paymentRepository.findAll().size();

        // Update the payment
        Payment updatedPayment = paymentRepository.findById(payment.getId()).get();
        // Disconnect from session so that the updates on updatedPayment are not directly saved in db
        em.detach(updatedPayment);
        updatedPayment
            .subscriptionDate(UPDATED_SUBSCRIPTION_DATE)
            .price(UPDATED_PRICE)
            .planType(UPDATED_PLAN_TYPE)
            .paid(UPDATED_PAID)
            .validUntil(UPDATED_VALID_UNTIL)
            .lastPaymentId(UPDATED_LAST_PAYMENT_ID)
            .recurring(UPDATED_RECURRING)
            .billingPlanId(UPDATED_BILLING_PLAN_ID)
            .tokenRecurring(UPDATED_TOKEN_RECURRING);
        PaymentDTO paymentDTO = paymentMapper.toDto(updatedPayment);

        restPaymentMockMvc.perform(put("/api/payments")
            .contentType(TestUtil.APPLICATION_JSON_UTF8)
            .content(TestUtil.convertObjectToJsonBytes(paymentDTO)))
            .andExpect(status().isOk());

        // Validate the Payment in the database
        List<Payment> paymentList = paymentRepository.findAll();
        assertThat(paymentList).hasSize(databaseSizeBeforeUpdate);
        Payment testPayment = paymentList.get(paymentList.size() - 1);
        assertThat(testPayment.getSubscriptionDate()).isEqualTo(UPDATED_SUBSCRIPTION_DATE);
        assertThat(testPayment.getPrice()).isEqualTo(UPDATED_PRICE);
        assertThat(testPayment.getPlanType()).isEqualTo(UPDATED_PLAN_TYPE);
        assertThat(testPayment.isPaid()).isEqualTo(UPDATED_PAID);
        assertThat(testPayment.getValidUntil()).isEqualTo(UPDATED_VALID_UNTIL);
        assertThat(testPayment.getLastPaymentId()).isEqualTo(UPDATED_LAST_PAYMENT_ID);
        assertThat(testPayment.isRecurring()).isEqualTo(UPDATED_RECURRING);
        assertThat(testPayment.getBillingPlanId()).isEqualTo(UPDATED_BILLING_PLAN_ID);
        assertThat(testPayment.getTokenRecurring()).isEqualTo(UPDATED_TOKEN_RECURRING);
    }

    @Test
    @Transactional
    public void updateNonExistingPayment() throws Exception {
        int databaseSizeBeforeUpdate = paymentRepository.findAll().size();

        // Create the Payment
        PaymentDTO paymentDTO = paymentMapper.toDto(payment);

        // If the entity doesn't have an ID, it will throw BadRequestAlertException
        restPaymentMockMvc.perform(put("/api/payments")
            .contentType(TestUtil.APPLICATION_JSON_UTF8)
            .content(TestUtil.convertObjectToJsonBytes(paymentDTO)))
            .andExpect(status().isBadRequest());

        // Validate the Payment in the database
        List<Payment> paymentList = paymentRepository.findAll();
        assertThat(paymentList).hasSize(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    public void deletePayment() throws Exception {
        // Initialize the database
        paymentRepository.saveAndFlush(payment);

        int databaseSizeBeforeDelete = paymentRepository.findAll().size();

        // Delete the payment
        restPaymentMockMvc.perform(delete("/api/payments/{id}", payment.getId())
            .accept(TestUtil.APPLICATION_JSON_UTF8))
            .andExpect(status().isOk());

        // Validate the database is empty
        List<Payment> paymentList = paymentRepository.findAll();
        assertThat(paymentList).hasSize(databaseSizeBeforeDelete - 1);
    }

    @Test
    @Transactional
    public void equalsVerifier() throws Exception {
        TestUtil.equalsVerifier(Payment.class);
        Payment payment1 = new Payment();
        payment1.setId(1L);
        Payment payment2 = new Payment();
        payment2.setId(payment1.getId());
        assertThat(payment1).isEqualTo(payment2);
        payment2.setId(2L);
        assertThat(payment1).isNotEqualTo(payment2);
        payment1.setId(null);
        assertThat(payment1).isNotEqualTo(payment2);
    }

    @Test
    @Transactional
    public void dtoEqualsVerifier() throws Exception {
        TestUtil.equalsVerifier(PaymentDTO.class);
        PaymentDTO paymentDTO1 = new PaymentDTO();
        paymentDTO1.setId(1L);
        PaymentDTO paymentDTO2 = new PaymentDTO();
        assertThat(paymentDTO1).isNotEqualTo(paymentDTO2);
        paymentDTO2.setId(paymentDTO1.getId());
        assertThat(paymentDTO1).isEqualTo(paymentDTO2);
        paymentDTO2.setId(2L);
        assertThat(paymentDTO1).isNotEqualTo(paymentDTO2);
        paymentDTO1.setId(null);
        assertThat(paymentDTO1).isNotEqualTo(paymentDTO2);
    }

    @Test
    @Transactional
    public void testEntityFromId() {
        assertThat(paymentMapper.fromId(42L).getId()).isEqualTo(42);
        assertThat(paymentMapper.fromId(null)).isNull();
    }

    @Test
    @Transactional
    public void getPaymentByLogin() throws Exception {
        User user = new User();
        user.setLogin("lol");
        user.setEmail("lol@lol.com");
        user.setPassword("looooool");
        user.setActivated(true);
        User userSaved = userService.createUser(new UserDTO(user));
        // Initialize the database
        paymentRepository.saveAndFlush(payment);
        payment.setUser(userSaved);

        SecurityContext securityContext = SecurityContextHolder.createEmptyContext();
        securityContext.setAuthentication(new UsernamePasswordAuthenticationToken("lol", "looooool"));
        SecurityContextHolder.setContext(securityContext);

        // Get the payment
        restPaymentMockMvc.perform(get("/api/payments-by-login/"))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8_VALUE))
            .andExpect(jsonPath("$.id").value(payment.getId().intValue()))
            .andExpect(jsonPath("$.subscriptionDate").value(DEFAULT_SUBSCRIPTION_DATE.toString()))
            .andExpect(jsonPath("$.price").value(DEFAULT_PRICE))
            .andExpect(jsonPath("$.planType").value(DEFAULT_PLAN_TYPE.toString()));
    }

    @Test
    @Transactional
    public void getPaymentByLoginNotFound() throws Exception {
        User user = new User();
        user.setLogin("lol");
        user.setEmail("lol@lol.com");
        user.setPassword("looooool");
        user.setActivated(true);

        SecurityContext securityContext = SecurityContextHolder.createEmptyContext();
        securityContext.setAuthentication(new UsernamePasswordAuthenticationToken("lol", "looooool"));
        SecurityContextHolder.setContext(securityContext);

        // Get the payment
        restPaymentMockMvc.perform(get("/api/payments-by-login/"))
            .andExpect(status().isNotFound());
    }
}

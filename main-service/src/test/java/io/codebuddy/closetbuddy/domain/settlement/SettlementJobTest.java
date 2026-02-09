package io.codebuddy.closetbuddy.domain.settlement;

import io.codebuddy.closetbuddy.domain.catalog.category.model.entity.Category;
import io.codebuddy.closetbuddy.domain.catalog.products.model.entity.Product;
import io.codebuddy.closetbuddy.domain.catalog.products.repository.ProductJpaRepository;
import io.codebuddy.closetbuddy.domain.catalog.sellers.model.entity.Seller;
import io.codebuddy.closetbuddy.domain.catalog.sellers.repository.SellerJpaRepository;
import io.codebuddy.closetbuddy.domain.catalog.stores.model.entity.Store;
import io.codebuddy.closetbuddy.domain.catalog.stores.repository.StoreJpaRepository;
import io.codebuddy.closetbuddy.domain.orders.model.entity.Order;
import io.codebuddy.closetbuddy.domain.orders.model.entity.OrderItem;
import io.codebuddy.closetbuddy.domain.orders.repository.OrderRepository;
import io.codebuddy.closetbuddy.domain.pay.accounts.model.entity.Account;
import io.codebuddy.closetbuddy.domain.pay.accounts.repository.AccountHistoryRepository;
import io.codebuddy.closetbuddy.domain.pay.accounts.repository.AccountRepository;
import io.codebuddy.closetbuddy.domain.pay.payments.model.entity.Payment;
import io.codebuddy.closetbuddy.domain.pay.payments.repository.PaymentRepository;
import io.codebuddy.closetbuddy.domain.settlement.model.entity.Settlement;
import io.codebuddy.closetbuddy.domain.settlement.repository.SettlementDetailRepository;
import io.codebuddy.closetbuddy.domain.settlement.repository.SettlementRepository;
import io.codebuddy.closetbuddy.global.config.enumfile.OrderStatus;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.batch.core.BatchStatus;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.test.JobLauncherTestUtils;
import org.springframework.batch.test.context.SpringBatchTest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@SpringBatchTest
@ActiveProfiles("test")
@Import(TestBatchConfig.class) // JobLauncherTestUtils 빈 등록을 위한 설정
public class SettlementJobTest {

    @Autowired private JobLauncherTestUtils jobLauncherTestUtils; // Job 실행 도구

    // Repositories

    @Autowired private AccountRepository accountRepository;
    @Autowired private SellerJpaRepository sellerJpaRepository;
    @Autowired private AccountHistoryRepository accountHistoryRepository;
    @Autowired private StoreJpaRepository storeRepository;
    @Autowired private ProductJpaRepository productRepository;
    @Autowired private OrderRepository orderRepository;
    @Autowired private PaymentRepository paymentRepository;

    // 검증 및 삭제용 Repository
    @Autowired private SettlementRepository settlementRepository;
    @Autowired private SettlementDetailRepository settlementDetailRepository;

    @AfterEach
    public void tearDown() {
        // FK 제약조건 역순 삭제
        accountHistoryRepository.deleteAll();
        settlementDetailRepository.deleteAll();
        settlementRepository.deleteAll();
        paymentRepository.deleteAll();
        orderRepository.deleteAll(); // OrderItem은 Cascade로 삭제된다고 가정
        productRepository.deleteAll();
        storeRepository.deleteAll();
        sellerJpaRepository.deleteAll();
        accountRepository.deleteAll();
    }

    @Test
    @DisplayName("정산 배치 통합 테스트: Job 실행 후 정산 내역 생성 및 계좌 잔액 증가 검증")
    public void settlementJob_IntegrationTest() throws Exception {
        // [Given]
        String targetDateStr = LocalDate.now().toString();
        // 정산 기준: 배치 실행일로부터 3일 전 ~ 1달 전 데이터 조회
        // 안전하게 5일 전으로 설정
        LocalDateTime orderDate = LocalDate.now().minusDays(5).atStartOfDay();

        // 1. 가상의 Member ID 설정
        Long sellerMemberId = 100L;
        Long buyerMemberId = 200L;

        // 2. 판매자 관련 데이터 생성
        // -> 계좌와 판매자 정보가 같은 memberId(100L)를 가져야 나중에 정산금이 입금됨
        Account sellerAccount = createAccount(sellerMemberId, 100000L);
        Seller seller = createSeller(sellerMemberId);

        Store store = createStore(seller);
        Product product = createProduct(store, 50000L); // 5만원

        // 3. 구매자 관련 데이터 생성
        Order order = createOrder(buyerMemberId, product, 2, orderDate); // 10만원
        createPayment(buyerMemberId, order, orderDate);

        // [When]
        JobParameters jobParameters = new JobParametersBuilder()
                .addString("targetDate", targetDateStr)
                .addLong("time", System.currentTimeMillis())
                .toJobParameters();

        JobExecution jobExecution = jobLauncherTestUtils.launchJob(jobParameters);

        // [Then]
        // 1. Job 성공 확인
        assertThat(jobExecution.getStatus()).isEqualTo(BatchStatus.COMPLETED);

        // 2. 정산 데이터 생성 확인
        List<Settlement> settlements = settlementRepository.findAll();
        assertThat(settlements).hasSize(1);

        Settlement settlement = settlements.get(0);
        assertThat(settlement.getTotalSalesAmount()).isEqualTo(100000L);
        assertThat(settlement.getPayoutAmount()).isEqualTo(97000L); // 수수료 3% 제외

        // 3. 판매자 계좌 입금 확인 (memberId 100L로 조회)
        // AccountRepository가 findByMemberId를 지원해야 함
        Account updatedAccount = accountRepository.findByMemberId(sellerMemberId).orElseThrow();
        assertThat(updatedAccount.getBalance()).isEqualTo(197000L);
    }

    // --- Helper Methods (Member 객체 의존성 제거) ---

    private Account createAccount(Long memberId, Long balance) {
        Account account = Account.createAccount(memberId);
        account.charge(balance); // 초기 잔액 설정 필요 시
        return accountRepository.save(account);
    }

    private Seller createSeller(Long memberId) {
        return sellerJpaRepository.save(Seller.builder()
                .memberId(memberId) // 객체 대신 ID 주입
                .sellerName("사장님_" + memberId)
                .build());
    }

    private Store createStore(Seller seller) {
        return storeRepository.save(Store.builder()
                .storeName("상점_" + seller.getSellerName())
                .seller(seller)
                .build());
    }

    private Product createProduct(Store store, Long price) {
        return productRepository.save(Product.builder()
                .store(store)
                .productName("테스트상품")
                .productPrice(price)
                .productStock(100)
                .category(Category.TOP)
                .build());
    }

    private Order createOrder(Long buyerMemberId, Product product, int count, LocalDateTime date) {
        OrderItem orderItem = OrderItem.createOrderItem(product, product.getProductPrice(), count);
        List<OrderItem> items = new ArrayList<>();
        items.add(orderItem);
        
        Order order = Order.createOrder(buyerMemberId, items);

        ReflectionTestUtils.setField(order, "orderStatus", OrderStatus.COMPLETED);
        ReflectionTestUtils.setField(order, "updatedAt", date);
        ReflectionTestUtils.setField(order, "createdAt", date);

        return orderRepository.save(order);
    }

    private void createPayment(Long buyerMemberId, Order order, LocalDateTime date) {
        long totalAmount = order.getOrderItem().stream()
                .mapToLong(item -> item.getOrderPrice() * item.getOrderCount())
                .sum();

        Payment payment = Payment.builder()
                .orderId(order.getOrderId())
                .memberId(buyerMemberId) // 객체 대신 ID
                .paymentAmount(totalAmount)
                .build();

        payment.approved();

        ReflectionTestUtils.setField(payment, "createdAt", date);
        ReflectionTestUtils.setField(payment, "updatedAt", date);

        paymentRepository.save(payment);
    }
}
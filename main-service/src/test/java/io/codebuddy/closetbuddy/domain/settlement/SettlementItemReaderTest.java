package io.codebuddy.closetbuddy.domain.settlement;

import io.codebuddy.closetbuddy.domain.catalog.products.model.dto.Category;
import io.codebuddy.closetbuddy.domain.catalog.products.model.entity.Product;
import io.codebuddy.closetbuddy.domain.catalog.products.repository.ProductJpaRepository;
import io.codebuddy.closetbuddy.domain.catalog.sellers.model.entity.Seller;
import io.codebuddy.closetbuddy.domain.catalog.sellers.repository.SellerJpaRepository;
import io.codebuddy.closetbuddy.domain.catalog.stores.model.entity.Store;
import io.codebuddy.closetbuddy.domain.catalog.stores.repository.StoreJpaRepository;
import io.codebuddy.closetbuddy.domain.orders.entity.Order;
import io.codebuddy.closetbuddy.domain.orders.entity.OrderItem;
import io.codebuddy.closetbuddy.domain.orders.repository.OrderRepository;
import io.codebuddy.closetbuddy.domain.pay.accounts.model.entity.Account;
import io.codebuddy.closetbuddy.domain.pay.accounts.repository.AccountHistoryRepository;
import io.codebuddy.closetbuddy.domain.pay.accounts.repository.AccountRepository;
import io.codebuddy.closetbuddy.domain.pay.payments.model.entity.Payment;
import io.codebuddy.closetbuddy.domain.pay.payments.model.vo.PaymentStatus;
import io.codebuddy.closetbuddy.domain.pay.payments.repository.PaymentRepository;
import io.codebuddy.closetbuddy.domain.settlement.model.dto.SettlementTargetDto;
import io.codebuddy.closetbuddy.domain.settlement.repository.SettlementDetailRepository;
import io.codebuddy.closetbuddy.domain.settlement.repository.SettlementRepository;
import io.codebuddy.closetbuddy.global.config.enumfile.OrderStatus;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.core.StepExecution;
import org.springframework.batch.item.ExecutionContext;
import org.springframework.batch.item.database.JpaPagingItemReader;
import org.springframework.batch.test.MetaDataInstanceFactory;
import org.springframework.batch.test.StepScopeTestUtils;
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
@Import(TestBatchConfig.class) // 배치 설정 로드
public class SettlementItemReaderTest {

    @Autowired private JpaPagingItemReader<SettlementTargetDto> settlementItemReader;

    // Repositories
    @Autowired private AccountRepository accountRepository;
    @Autowired private AccountHistoryRepository accountHistoryRepository;
    @Autowired private SellerJpaRepository sellerJpaRepository;
    @Autowired private StoreJpaRepository storeRepository;
    @Autowired private ProductJpaRepository productRepository;
    @Autowired private OrderRepository orderRepository;
    @Autowired private PaymentRepository paymentRepository;
    @Autowired private SettlementRepository settlementRepository;
    @Autowired private SettlementDetailRepository settlementDetailRepository;

    @AfterEach
    public void tearDown() {
        // FK 제약조건을 고려하여 자식 테이블부터 삭제
        settlementDetailRepository.deleteAll();
        settlementRepository.deleteAll();
        accountHistoryRepository.deleteAll();
        paymentRepository.deleteAll();
        orderRepository.deleteAll();
        productRepository.deleteAll();
        storeRepository.deleteAll();
        sellerJpaRepository.deleteAll();
        accountRepository.deleteAll();
    }

    @Test
    @DisplayName("Reader 검증: 정산 대상 기간(3일 전 ~ 1달 전)과 완료된 주문만 조회한다")
    public void settlementItemReader_Test() throws Exception {
        // [Given]
        String targetDateStr = LocalDate.now().toString();

        // 1. [Valid] 읽혀야 하는 데이터 (5일 전, 완료됨, 결제 승인됨)
        LocalDateTime validDate = LocalDate.now().minusDays(5).atStartOfDay();
        Long expectedSellerId = createScenario(100L, validDate, OrderStatus.COMPLETED, PaymentStatus.APPROVED);

        // 2. [Invalid] 날짜가 너무 최신이라 안 읽혀야 함 (1일 전) -> 정산 대상 아님(D-3 미만)
        LocalDateTime tooRecentDate = LocalDate.now().minusDays(1).atStartOfDay();
        createScenario(200L, tooRecentDate, OrderStatus.COMPLETED, PaymentStatus.APPROVED);

        // 3. [Invalid] 날짜는 맞는데 주문 상태가 취소임
        createScenario(300L, validDate, OrderStatus.CANCELED, PaymentStatus.APPROVED);

        // 4. [Invalid] 날짜는 맞는데 결제 상태가 PENDING임
        createScenario(400L, validDate, OrderStatus.COMPLETED, PaymentStatus.PENDING);


        // [When]
        // Reader 실행 환경 설정
        // 가짜 StepExecution을 만들고 파라미터를 주입합니다.
        JobParameters jobParameters = new JobParametersBuilder()
                .addString("targetDate", targetDateStr) // Reader의 @Value로 들어갈 값
                .toJobParameters();

        StepExecution stepExecution = MetaDataInstanceFactory.createStepExecution(jobParameters);

        // [Then]
        // StepScopeTestUtils를 이용해 Reader의 read() 메서드 실행
        int readCount = StepScopeTestUtils.doInStepScope(stepExecution, () -> {

            settlementItemReader.open(new ExecutionContext());

            int count = 0;
            SettlementTargetDto item;
            try {
                while ((item = settlementItemReader.read()) != null) {
                    count++;
                    // 읽어온 데이터가 판매자의 물건인지 확인
                    assertThat(item.getSellerId()).isEqualTo(expectedSellerId);
                    System.out.println("Read Item: " + item.getProductName());
                }
            } finally {
                settlementItemReader.close();
            }
            return count;
        });

        // 검증: 총 4개의 데이터를 넣었지만, 조건에 맞는 건 1개뿐이어야 함
        assertThat(readCount).isEqualTo(1);
    }

    // --- Helper Methods ---

    // PaymentStatus 파라미터 추가
    private Long createScenario(Long idBase, LocalDateTime date, OrderStatus orderStatus, PaymentStatus paymentStatus) {
        Account account = createAccount(idBase, 0L);
        Seller seller = createSeller(idBase);
        Store store = createStore(seller);
        Product product = createProduct(store, 10000L);

        // 주문 생성 시 상태와 날짜를 파라미터로 받음
        // idBase + 1000을 하여 memberId 충돌 방지 (Account와 별개인 구매자 ID)
        Order order = createOrder(idBase + 1000, product, 1, date);

        ReflectionTestUtils.setField(order, "orderStatus", orderStatus); // 상태 강제 변경
        orderRepository.save(order);

        // paymentStatus 전달
        createPayment(idBase + 1000, order, date, paymentStatus);
        return seller.getSellerId(); // 생성된 ID 반환
    }

    private Account createAccount(Long memberId, Long balance) {
        Account account = Account.createAccount(memberId);
        if (balance > 0) {
            account.charge(balance);
        }
        return accountRepository.save(account);
    }

    private Seller createSeller(Long memberId) {
        return sellerJpaRepository.save(Seller.builder()
                .memberId(memberId)
                .sellerName("사장님_" + memberId)
                .build());
    }

    private Store createStore(Seller seller) {
        return storeRepository.save(Store.builder()
                .seller(seller)
                .storeName("상점_" + seller.getSellerName())
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

    // PaymentStatus를 받아서 처리하도록 변경
    private void createPayment(Long buyerMemberId, Order order, LocalDateTime date, PaymentStatus status) {

        long totalAmount = order.getOrderItem().stream()
                .mapToLong(item -> item.getOrderPrice() * item.getOrderCount())
                .sum();

        Payment payment = Payment.builder()
                .orderId(order.getOrderId())
                .memberId(buyerMemberId)
                .paymentAmount(totalAmount)
                .build();

        // 만약 Builder에 status가 없다면 아래 로직 사용
        // Reflection으로 덮어씀 (Builder 초기화 이슈 방지)
        ReflectionTestUtils.setField(payment, "paymentStatus", status);

        // 날짜 강제 설정
        ReflectionTestUtils.setField(payment, "createdAt", date);
        ReflectionTestUtils.setField(payment, "updatedAt", date);

        paymentRepository.save(payment);
    }
}
package io.codebuddy.closetbuddy;

import io.codebuddy.closetbuddy.domain.pay.accounts.model.entity.Account;
import io.codebuddy.closetbuddy.domain.pay.accounts.repository.AccountHistoryRepository;
import io.codebuddy.closetbuddy.domain.pay.accounts.repository.AccountRepository;
import io.codebuddy.closetbuddy.domain.common.model.dto.Role;
import io.codebuddy.closetbuddy.domain.common.model.entity.Member;
import io.codebuddy.closetbuddy.domain.common.repository.MemberRepository;
import io.codebuddy.closetbuddy.domain.orders.entity.Order;
import io.codebuddy.closetbuddy.domain.orders.entity.OrderItem;

import io.codebuddy.closetbuddy.domain.orders.repository.OrderRepository;

import io.codebuddy.closetbuddy.domain.pay.payments.model.entity.Payment;
import io.codebuddy.closetbuddy.domain.pay.payments.repository.PaymentRepository;
import io.codebuddy.closetbuddy.domain.products.model.dto.Category;
import io.codebuddy.closetbuddy.domain.products.model.entity.Product;
import io.codebuddy.closetbuddy.domain.products.repository.ProductJpaRepository;

import io.codebuddy.closetbuddy.domain.catalog.sellers.model.entity.Seller;
import io.codebuddy.closetbuddy.domain.catalog.sellers.repository.SellerJpaRepository;
import io.codebuddy.closetbuddy.domain.settlement.repository.SettlementDetailRepository;
import io.codebuddy.closetbuddy.domain.settlement.repository.SettlementRepository;
import io.codebuddy.closetbuddy.domain.catalog.stores.model.entity.Store;
import io.codebuddy.closetbuddy.domain.catalog.stores.repository.StoreJpaRepository;

import io.codebuddy.closetbuddy.global.config.enumfile.OrderStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.batch.test.JobLauncherTestUtils;
import org.springframework.batch.test.context.SpringBatchTest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@SpringBootTest
@SpringBatchTest
@ActiveProfiles("test") // application-test.yml 사용 시
public class SettlementJobTest {

    @Autowired private JobLauncherTestUtils jobLauncherTestUtils;
    @Autowired private JobLauncher jobLauncher;
    @Autowired private Job settlementJob; // JobConfig Bean 이름

    // Repositories
    @Autowired private MemberRepository memberRepository;
    @Autowired private AccountRepository accountRepository;
    @Autowired private SellerJpaRepository sellerJpaRepository;
    @Autowired private StoreJpaRepository storeRepository;
    @Autowired private ProductJpaRepository productRepository;
    @Autowired private OrderRepository ordersRepository;
    //@Autowired private OrderItemRepository orderItemRepository;
    @Autowired private PaymentRepository paymentRepository;

    // Cleanup Repositories
    @Autowired private SettlementRepository settlementRepository;
    @Autowired private SettlementDetailRepository settlementDetailRepository;
    @Autowired private AccountHistoryRepository accountHistoryRepository;

    @BeforeEach
    public void setUp() {
        // 1. 데이터 초기화 (FK 제약조건 때문에 자식 -> 부모 순으로 삭제)
        settlementDetailRepository.deleteAll();
        settlementRepository.deleteAll();
        accountHistoryRepository.deleteAll();
        paymentRepository.deleteAll();
        //orderItemRepository.deleteAll();
        ordersRepository.deleteAll();
        productRepository.deleteAll();
        storeRepository.deleteAll();
        sellerJpaRepository.deleteAll();
        accountRepository.deleteAll();
        memberRepository.deleteAll();

        // 2. [판매자] 회원 생성
        Member sellerMember = memberRepository.save(Member.builder()
                .username("판매자킴")
                .memberId("seller1")
                .email("seller@test.com")
                .password("pass")
                .role(Role.SELLER) // Role Enum 가정
                .build());

        // 3.  판매자의 계좌(Account) 생성
        accountRepository.save(Account.builder()
                .member(sellerMember)
                .balance(0L)
                .build());

        // 4. [판매자] 엔티티 생성 (Member와 연결)
        Seller seller = sellerJpaRepository.save(Seller.builder()
                .member(sellerMember)
                .sellerName("김사장")
                .build());

        // 5. 상점 생성
        Store store = storeRepository.save(Store.builder()
                .storeName("대박옷가게")
                .seller(seller)
                .build());

        // 6. 상품 생성
        Product product = productRepository.save(Product.builder()
                .store(store)
                .productName("기모 후드티")
                .productPrice(50000L)
                .productStock(100)
                .category(Category.TOP)
                .build());

        // 7. [구매자] 회원 생성
        Member buyerMember = memberRepository.save(Member.builder()
                .username("구매자이")
                .memberId("buyer1")
                .email("buyer@test.com")
                .password("pass")
                .role(Role.MEMBER)
                .build());

        // 8. 주문 상세(OrderItem) 생성
        // - 아직 DB에 저장하지 않고 객체만 만듭니다.
        OrderItem orderItem = OrderItem.createOrderItem(product, 50000L, 2); // 5만원 * 2개

        // 9.  주문(Order) 생성 및 Cascade 저장
        // - Order.createOrder() 팩토리 메서드 사용
        List<OrderItem> orderItems = new ArrayList<>();
        orderItems.add(orderItem);

        Order order = Order.createOrder(buyerMember.getId(), orderItems);

        // 10. 테스트 조건을 위한 강제 값 변경 (ReflectionTestUtils)
        // - createOrder는 CREATED 상태이므로 -> COMPLETED로 변경
        // - Reader가 읽어갈 수 있도록 날짜를 과거(10일전)로 변경
        LocalDateTime pastDate = LocalDateTime.now().minusDays(10);

        ReflectionTestUtils.setField(order, "orderStatus", OrderStatus.COMPLETED);
        ReflectionTestUtils.setField(order, "orderAmount", 100000L); // 총액 설정
        ReflectionTestUtils.setField(order, "updatedAt", pastDate);
        ReflectionTestUtils.setField(order, "createdAt", pastDate);

        // save하면 Order와 OrderItem이 같이 저장됩니다.
        ordersRepository.save(order);

        // 11. 결제 생성 (승인 완료)
        Payment payment = Payment.builder()
                .orderId(order.getOrderId())
                .memberId(buyerMember.getId())
                .paymentAmount(100000L)
                .build();
        payment.approved(); // 상태를 APPROVED로 변경
        ReflectionTestUtils.setField(payment, "createdAt", pastDate);
        ReflectionTestUtils.setField(payment, "updatedAt", pastDate);
        paymentRepository.save(payment);
    }

    @Test
    public void runJob() throws Exception {
        // Job 파라미터 설정 (Reader 쿼리 조건에 맞는 날짜)
        String targetDateStr = LocalDate.now().toString();

        JobParameters jobParameters = new JobParametersBuilder()
                .addString("targetDate", targetDateStr)
                .addLong("time", System.currentTimeMillis())
                .toJobParameters();

        jobLauncher.run(settlementJob, jobParameters);
    }
}
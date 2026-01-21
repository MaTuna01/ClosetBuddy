package io.codebuddy.closetbuddy;

import io.codebuddy.closetbuddy.domain.accounts.model.entity.Account;
import io.codebuddy.closetbuddy.domain.accounts.repository.AccountRepository;
import io.codebuddy.closetbuddy.domain.orders.entity.OrderItem;
import io.codebuddy.closetbuddy.domain.orders.repository.OrderRepository;
import io.codebuddy.closetbuddy.domain.orders.service.OrderService;
import io.codebuddy.closetbuddy.domain.payments.repository.PaymentRepository;
import io.codebuddy.closetbuddy.domain.products.model.dto.Category;
import io.codebuddy.closetbuddy.domain.products.model.entity.Product;
import io.codebuddy.closetbuddy.domain.products.repository.ProductJpaRepository;
import io.codebuddy.closetbuddy.domain.sellers.model.entity.Seller;
import io.codebuddy.closetbuddy.domain.sellers.repository.SellerJpaRepository;
import io.codebuddy.closetbuddy.domain.stores.model.entity.Store;
import io.codebuddy.closetbuddy.domain.stores.repository.StoreJpaRepository;
import io.codebuddy.closetbuddy.member.MemberClient;
import io.codebuddy.closetbuddy.member.MemberDto;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;


@ExtendWith(MockitoExtension.class)
public class OrderTest {

    // 1. 모든 리포지토리에 @Mock을 붙여야 합니다. (안 붙이면 Null입니다)
    @Mock private AccountRepository accountRepository;
    @Mock private SellerJpaRepository sellerJpaRepository;
    @Mock private StoreJpaRepository storeRepository;
    @Mock private ProductJpaRepository productRepository;
    @Mock private OrderRepository ordersRepository;
    // @Mock private PaymentRepository paymentRepository; // 필요 시 주석 해제

    // 2. 외부 통신 Client도 Mock
    @Mock private MemberClient memberClient;

    // 3. 테스트 대상 서비스
    @InjectMocks
    private OrderService orderService;

    @Test
    @DisplayName("가게와 상품을 준비하고 주문을 생성한다")
    void createStoreAndOrderTest() {
        // ---------------------------------------------------------
        // 1. [Member DTO] 준비 (DB 저장 X, 메모리 객체)
        // ---------------------------------------------------------
        MemberDto sellerDto = MemberDto.builder()
                .memberId("seller-1")
                .username("판매자킴")
                .email("seller@test.com")
                .role("SELLER")
                .build();

        MemberDto buyerDto = MemberDto.builder()
                .memberId("buyer-1")
                .username("구매자이")
                .role("MEMBER")
                .build();

        // ---------------------------------------------------------
        // 2. [Entity] 준비 (Repository.save 대신 객체를 직접 생성)
        // Mockito에서는 save()를 호출하면 null이 나오므로,
        // 그냥 객체를 만들어서 변수에 담아두는 것이 핵심입니다.
        // ---------------------------------------------------------

        // (1) 계좌 객체 생성
        Account account = Account.builder()
                .memberId(Seller.) // ID 연결
                .balance(0L)
                .build();

        // (2) 판매자 객체 생성
        Seller seller = Seller.builder()
                .memberId(seller.getMemberId()) // ID 연결
                .sellerName("김사장")
                .build();

        // (3) 상점 객체 생성 (Seller 객체 연결)
        Store store = Store.builder()
                .storeName("대박옷가게")
                .seller(seller) // 자바 객체끼리 연결
                .build();

        // (4) 상품 객체 생성 (Store 객체 연결)
        Product product = Product.builder()
                .store(store)
                .productName("기모 후드티")
                .productPrice(50000L)
                .productStock(100)
                .category(Category.TOP)
                .build();

        // ---------------------------------------------------------
        // 3. [Mocking] 리포지토리 동작 가짜 정의 (Stubbing)
        // "누군가 DB에서 데이터를 찾으면, 아까 만든 저 객체들을 줘라!"
        // ---------------------------------------------------------

        // 예: 상품 ID로 조회하면 -> 위에서 만든 product 객체 리턴
        // (만약 OrderService 내부에서 findById 등을 쓴다면 필요함)
        // given(productRepository.findById(any())).willReturn(Optional.of(product));

        // 예: 저장을 요청하면 -> 그 객체 그대로 리턴 (Null 방지)
        given(sellerJpaRepository.save(any(Seller.class))).willReturn(seller);
        given(storeRepository.save(any(Store.class))).willReturn(store);


        // ---------------------------------------------------------
        // 4. [주문 로직 실행]
        // ---------------------------------------------------------

        // 실제 테스트할 메서드 호출 (예시)
        // orderService.createOrder(buyerDto.getMemberId(), product.getId(), 2);

        // 검증 로직...
    }
}

package kitchenpos.menu.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import kitchenpos.menu.dao.MenuGroupRepository;
import kitchenpos.menu.dao.MenuProductRepository;
import kitchenpos.menu.dao.MenuRepository;
import kitchenpos.menu.dao.ProductRepository;
import kitchenpos.menu.domain.Menu;
import kitchenpos.menu.domain.MenuGroup;
import kitchenpos.menu.domain.MenuProduct;
import kitchenpos.menu.domain.Product;

@ExtendWith(MockitoExtension.class)
public class MenuServiceTest {

    @Mock
    private MenuRepository menuRepository;

    @Mock
    private MenuGroupRepository menuGroupRepository;

    @Mock
    private MenuProductRepository menuProductRepository;

    @Mock
    private ProductRepository productRepository;

    @InjectMocks
    private MenuService menuService;

    @DisplayName("메뉴를 등록할 수 있다")
    @Test
    void 메뉴_등록() {
        // given
        MenuGroup 메뉴그룹 = new MenuGroup();
        메뉴그룹.setId(1L);
        메뉴그룹.setName("중식");

        Menu 메뉴 = new Menu();
        메뉴.setId(1L);
        메뉴.setName("짜장면");
        메뉴.setPrice(new BigDecimal("6000"));
        메뉴.setMenuGroup(메뉴그룹);
        
        Product 상품 = new Product();
        상품.setId(1L);
        상품.setName("짜장면");
        상품.setPrice(new BigDecimal("6000"));

        MenuProduct 메뉴상품 = new MenuProduct();
        메뉴상품.setSeq(1L);
        메뉴상품.setMenu(메뉴);
        메뉴상품.setProduct(상품);
        메뉴상품.setQuantity(1L);

        메뉴.setMenuProducts(Arrays.asList(메뉴상품));
        given(menuGroupRepository.existsById(메뉴.getMenuGroup().getId())).willReturn(true);
        given(productRepository.findById(메뉴상품.getProduct().getId())).willReturn(Optional.of(상품));
        given(menuRepository.save(메뉴)).willReturn(메뉴);
        given(menuProductRepository.save(메뉴상품)).willReturn(메뉴상품);

        // when
        Menu 저장된_메뉴 = menuService.create(메뉴);

        // then
        assertThat(저장된_메뉴).isEqualTo(메뉴);

    }

    @DisplayName("메뉴 등록시 가격은 필수여야한다 - 예외처리")
    @Test
    void 메뉴_등록_가격_필수() {
        // given
        Menu 가격없는_메뉴 = new Menu();
        가격없는_메뉴.setId(1L);
        가격없는_메뉴.setName("짜장면");
        
        // when
        가격없는_메뉴.setPrice(null);

        // then
        assertThatThrownBy(() -> {
            menuService.create(가격없는_메뉴);
        }).isInstanceOf(IllegalArgumentException.class)
        .hasMessage("메뉴 가격은 0원 이상이어야 합니다");

    }
    
    @DisplayName("메뉴 등록시 가격은 0원 이상이어야한다 - 예외처리")
    @Test
    void 메뉴_등록_가격_0원_이상() {
        // given
        Menu 마이너스_가격_메뉴 = new Menu();
        마이너스_가격_메뉴.setId(1L);
        마이너스_가격_메뉴.setName("짜장면");
        
        // when
        마이너스_가격_메뉴.setPrice(new BigDecimal("-6000"));

        // then
        assertThatThrownBy(() -> {
            menuService.create(마이너스_가격_메뉴);
        }).isInstanceOf(IllegalArgumentException.class)
        .hasMessage("메뉴 가격은 0원 이상이어야 합니다");
    }
    
    @DisplayName("메뉴 등록시 메뉴그룹이 지정되어 있어야한다 - 예외처리")
    @Test
    void 메뉴_등록_메뉴그룹_필수() {
        // given
        Menu 메뉴그룹_미지정_메뉴 = new Menu();
        메뉴그룹_미지정_메뉴.setId(1L);
        메뉴그룹_미지정_메뉴.setName("짜장면");
        메뉴그룹_미지정_메뉴.setPrice(new BigDecimal("6000"));
        
        // when
        메뉴그룹_미지정_메뉴.setMenuGroup(new MenuGroup());

        // then
        assertThatThrownBy(() -> {
            menuService.create(메뉴그룹_미지정_메뉴);
        }).isInstanceOf(IllegalArgumentException.class)
        .hasMessage("해당하는 메뉴그룹이 없습니다");
    }
    
    @DisplayName("메뉴에 등록된 메뉴그룹은 등록된 메뉴그룹이어야한다 - 예외처리")
    @Test
    void 메뉴_등록_등록된_메뉴그룹만() {
        // given
        Menu 메뉴 = new Menu();
        메뉴.setId(1L);
        메뉴.setName("짜장면");
        메뉴.setPrice(new BigDecimal("6000"));
        MenuGroup 메뉴그룹 = new MenuGroup();
        메뉴그룹.setId(1L);
        메뉴.setMenuGroup(메뉴그룹);
        
        // when
        when(menuGroupRepository.existsById(anyLong())).thenReturn(false);

        // then
        assertThatThrownBy(() -> {
            menuService.create(메뉴);
        }).isInstanceOf(IllegalArgumentException.class)
        .hasMessage("해당하는 메뉴그룹이 없습니다");

    }
    
    @DisplayName("메뉴에 포함된 상품은 등록된 상품이어야한다 - 예외처리")
    @Test
    void 메뉴_등록_등록된_상품만() {
        // given
        Menu 메뉴 = new Menu();
        메뉴.setId(1L);
        메뉴.setName("짜장면");
        메뉴.setPrice(new BigDecimal("6000"));
        MenuGroup 메뉴그룹 = new MenuGroup();
        메뉴그룹.setId(1L);
        메뉴.setMenuGroup(메뉴그룹);
        
        MenuProduct 메뉴상품 = new MenuProduct();
        메뉴상품.setSeq(1L);
        메뉴상품.setMenu(메뉴);
        
        Product 상품 = new Product();
        상품.setId(1L);
        메뉴상품.setProduct(상품);
        메뉴.setMenuProducts(Arrays.asList(메뉴상품));
        
        given(menuGroupRepository.existsById(anyLong())).willReturn(true);
        
        // when
        when(productRepository.findById(anyLong())).thenReturn(Optional.empty());

        // then
        assertThatThrownBy(() -> {
            menuService.create(메뉴);
        }).isInstanceOf(IllegalArgumentException.class)
        .hasMessage("등록된 상품이 아닙니다");

    }

    @DisplayName("메뉴 등록시 가격은 포함된 상품들의 총 금액보다 클 수 없다- 예외처리")
    @Test
    void 메뉴_등록_금액_확인() {
        // given
        Menu 메뉴 = new Menu();
        메뉴.setId(1L);
        메뉴.setName("짜장면");
        메뉴.setPrice(new BigDecimal("10000"));
        MenuGroup 메뉴그룹 = new MenuGroup();
        메뉴그룹.setId(1L);
        메뉴.setMenuGroup(메뉴그룹);
        
        Product 상품 = new Product();
        상품.setId(1L);
        상품.setName("짜장면");
        상품.setPrice(new BigDecimal("6000"));
        
        MenuProduct 메뉴상품 = new MenuProduct();
        메뉴상품.setSeq(1L);
        메뉴상품.setMenu(메뉴);
        메뉴상품.setProduct(상품);
        메뉴상품.setQuantity(1L);
        메뉴.setMenuProducts(Arrays.asList(메뉴상품));

        given(menuGroupRepository.existsById(anyLong())).willReturn(true);
        given(productRepository.findById(anyLong())).willReturn(Optional.of(상품));

        // when, then
        assertThatThrownBy(() -> {
            menuService.create(메뉴);
        }).isInstanceOf(IllegalArgumentException.class)
        .hasMessage("메뉴 가격이 상품 가격의 합보다 큽니다");
    }
    
    @DisplayName("메뉴 목록을 조회할 수 있다")
    @Test
    void 메뉴_목록_조회() {
        // given
        Menu 첫번째_메뉴 = new Menu();
        첫번째_메뉴.setId(1L);
        첫번째_메뉴.setName("짜장면");
        첫번째_메뉴.setPrice(new BigDecimal("6000"));
        
        Menu 두번째_메뉴 = new Menu();
        두번째_메뉴.setId(2L);
        두번째_메뉴.setName("짬뽕");
        두번째_메뉴.setPrice(new BigDecimal("7000"));

        given(menuRepository.findAll()).willReturn(Arrays.asList(첫번째_메뉴, 두번째_메뉴));

        // when
        List<Menu> 메뉴_목록 = menuService.list();

        // then
        assertThat(메뉴_목록).containsExactly(첫번째_메뉴, 두번째_메뉴);
    }

}

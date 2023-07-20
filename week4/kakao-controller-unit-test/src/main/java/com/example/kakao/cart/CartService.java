package com.example.kakao.cart;

import com.example.kakao._core.errors.exception.Exception403;
import com.example.kakao.user.User;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class CartService {
    private final CartJPARepository cartJPARepository;

    public CartService(
            @Autowired CartJPARepository cartJPARepository
    ) {
        this.cartJPARepository = cartJPARepository;
    }

    @Transactional
    public CartResponse.FindAllDTO findAllByUser(User user) {
        List<Cart> carts = cartJPARepository.findAllByUser(user);
        return new CartResponse.FindAllDTO(carts);
    }

    public void save(User user, Cart cart) {
        try {
            cartJPARepository.save(cart);
        }
        catch (Exception exception) {
            throw new Exception403("이미 장바구니에 존재하는 상품입니다.");
        }
    }

    @Transactional
    public CartResponse.UpdateDTO update(User user, List<CartRequest.UpdateDTO> requests) {
        List<Cart> carts = requests
                .stream()
                .map(request -> {
                    Cart cart = cartJPARepository.findById(request.getCartId()).orElseThrow();

                    if (cart.getUser().getId() != user.getId()) {
                        throw new Exception403("현재 계정으로 해당 장바구니를 업데이트 할 수 없습니다.");
                    }

                    int quantity = request.getQuantity();
                    int price = quantity * cart.getOption().getPrice();
                    cart.update(quantity, price);

                    return cart;
                })
                .collect(Collectors.toList());

        cartJPARepository.saveAll(carts);
        List<Cart> savedCarts = cartJPARepository.findAllByUser(user);
        return new CartResponse.UpdateDTO(savedCarts);
    }

    public void deleteAllByUser(User user) {
        List<Cart> carts = cartJPARepository.findAllByUser(user);
        cartJPARepository.deleteAll(carts);
    }
}

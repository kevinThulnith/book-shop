package com.springboot.project.service;

import com.springboot.project.dto.CartItem;
import com.springboot.project.entity.Bill;
import com.springboot.project.entity.BillItem;
import com.springboot.project.entity.Item;
import com.springboot.project.entity.User;
import com.springboot.project.repository.BillRepository;
import com.springboot.project.repository.ItemRepository;
import com.springboot.project.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@Service
public class OrderService {

    @Autowired
    private BillRepository billRepository;

    @Autowired
    private ItemRepository itemRepository;

    @Autowired
    private UserRepository userRepository;

    @Transactional
    public Bill createOrder(Long customerId, Map<Long, Integer> items) {
        User customer = userRepository.findById(customerId)
                .orElseThrow(() -> new RuntimeException("Customer not found"));

        Bill bill = new Bill(customer);
        BigDecimal totalAmount = BigDecimal.ZERO;

        for (Map.Entry<Long, Integer> entry : items.entrySet()) {
            Long itemId = entry.getKey();
            Integer quantity = entry.getValue();

            Item item = itemRepository.findById(itemId)
                    .orElseThrow(() -> new RuntimeException("Item not found"));

            if (item.getStockQuantity() < quantity) {
                throw new RuntimeException("Not enough stock for item: " + item.getName());
            }

            item.setStockQuantity(item.getStockQuantity() - quantity);
            itemRepository.save(item);

            BillItem billItem = new BillItem(bill, item, quantity, item.getPrice());
            bill.getBillItems().add(billItem);

            totalAmount = totalAmount.add(item.getPrice().multiply(BigDecimal.valueOf(quantity)));
        }

        bill.setTotalAmount(totalAmount);
        // For simplicity, finalAmount is the same as totalAmount.
        // Tax and discount can be added later.
        bill.setFinalAmount(totalAmount);

        return billRepository.save(bill);
    }

    public List<Bill> getAllOrders() {
        return billRepository.findAll();
    }

    @Transactional
    public Bill placeOrder(User customer, CartService cartService) {
        Bill bill = new Bill(customer);
        BigDecimal totalAmount = BigDecimal.ZERO;

        for (CartItem cartItem : cartService.getCartItems()) {
            Item item = itemRepository.findById(cartItem.getItem().getItemCode())
                    .orElseThrow(() -> new RuntimeException("Item not found"));

            if (item.getStockQuantity() < cartItem.getQuantity()) {
                throw new RuntimeException("Not enough stock for item: " + item.getName());
            }

            item.setStockQuantity(item.getStockQuantity() - cartItem.getQuantity());
            itemRepository.save(item);

            BillItem billItem = new BillItem(bill, item, cartItem.getQuantity(), item.getPrice());
            bill.getBillItems().add(billItem);

            totalAmount = totalAmount.add(item.getPrice().multiply(BigDecimal.valueOf(cartItem.getQuantity())));
        }

        bill.setTotalAmount(totalAmount);
        bill.setFinalAmount(totalAmount);

        billRepository.save(bill);
        cartService.clearCart();

        return bill;
    }
}

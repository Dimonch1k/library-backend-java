package org.library.order;

import org.library.order.enums.OrderStatus;
import org.library.order.model.Order;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface OrderRepository extends JpaRepository<Order, Long> {
  boolean existsByBookIdAndStatus ( Long bookId, OrderStatus status );

  List<Order> findByUserId ( Long userId );

  List<Order> findAllByOrderByCreatedAtDesc();
  List<Order> findByUserIdOrderByCreatedAtDesc(Long userId);

}

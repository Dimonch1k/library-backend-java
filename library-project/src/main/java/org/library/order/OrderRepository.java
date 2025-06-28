package org.library.order;

import org.library.order.enums.OrderStatus;
import org.library.order.model.Order;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface OrderRepository extends JpaRepository<Order, UUID> {
  boolean existsByBookIdAndStatus ( UUID bookId, OrderStatus status );

  List<Order> findByUserId ( UUID userId );
}

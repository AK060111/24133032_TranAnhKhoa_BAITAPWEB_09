package vn.iotstar.repository;

import org.springframework.data.domain.*;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;
import vn.iotstar.entity.Product;

public interface ProductRepository extends JpaRepository<Product, Long> {
    @Query(value = """
            select p from Product p join fetch p.user u
            where lower(p.name) like lower(concat('%', :keyword, '%'))
               or lower(coalesce(p.description, '')) like lower(concat('%', :keyword, '%'))
            """, countQuery = """
            select count(p) from Product p
            where lower(p.name) like lower(concat('%', :keyword, '%'))
               or lower(coalesce(p.description, '')) like lower(concat('%', :keyword, '%'))
            """)
    Page<Product> search(@Param("keyword") String keyword, Pageable pageable);

    @Query(value = """
            select p from Product p join fetch p.user u
            where p.user.username = :username
              and (lower(p.name) like lower(concat('%', :keyword, '%'))
                or lower(coalesce(p.description, '')) like lower(concat('%', :keyword, '%')))
            """, countQuery = """
            select count(p) from Product p
            where p.user.username = :username
              and (lower(p.name) like lower(concat('%', :keyword, '%'))
                or lower(coalesce(p.description, '')) like lower(concat('%', :keyword, '%')))
            """)
    Page<Product> searchOwned(@Param("username") String username, @Param("keyword") String keyword, Pageable pageable);
    long countByUserId(Long userId);
}

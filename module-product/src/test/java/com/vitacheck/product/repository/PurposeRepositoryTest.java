//package com.vitacheck.product.repository;
//
//import org.junit.jupiter.api.Test;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
//import org.springframework.boot.test.context.SpringBootTest;
//
//import java.util.List;
//
//import static org.junit.jupiter.api.Assertions.*;
//
//@SpringBootTest
//class PurposeRepositoryTest {
//
//    @Autowired
//    private PurposeRepository purposeRepository;
//
//    @Test
//    void findPurposeWithLimitedSupplements() {
//        List<Object[]> results = purposeRepository.findPurposeWithLimitedSupplements(List.of(1L, 2L));
//        results.forEach(r -> System.out.println(
//                "purposeId=" + r[0] +
//                        ", purposeName=" + r[1] +
//                        ", ingredientId=" + r[2] +
//                        ", ingredientName=" + r[3] +
//                        ", supplementId=" + r[4] +
//                        ", supplementName=" + r[5] +
//                        ", coupangUrl=" + r[6] +
//                        ", imageUrl=" + r[7]
//        ));
//    }
//}

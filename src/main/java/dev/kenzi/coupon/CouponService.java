package dev.kenzi.coupon;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class CouponService {

    private final CouponRepository couponRepository;
    private final IssuedCouponRepository issuedCouponRepository;

    public CouponService(CouponRepository couponRepository, IssuedCouponRepository issuedCouponRepository) {
        this.couponRepository = couponRepository;
        this.issuedCouponRepository = issuedCouponRepository;
    }

    @Transactional
    public Long create(String name, int totalQuantity) {
        Coupon coupon = couponRepository.save(new Coupon(name, totalQuantity));
        return coupon.getId();
    }

    @Transactional
    public boolean issue(Long couponId, Long userId) {
        Coupon coupon = couponRepository.findById(couponId).orElseThrow();
        if (coupon.isSoldOut()) {
            return false;
        }
        coupon.issue();
        issuedCouponRepository.save(new IssuedCoupon(couponId, userId));
        return true;
    }
}

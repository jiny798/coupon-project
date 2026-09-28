package dev.kenzi.coupon;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class CouponService {

    private final CouponRepository couponRepository;
    private final IssuedCouponRepository issuedCouponRepository;
    private final CouponIssueRedis couponIssueRedis;

    public CouponService(CouponRepository couponRepository,
                         IssuedCouponRepository issuedCouponRepository,
                         CouponIssueRedis couponIssueRedis) {
        this.couponRepository = couponRepository;
        this.issuedCouponRepository = issuedCouponRepository;
        this.couponIssueRedis = couponIssueRedis;
    }

    @Transactional
    public Long create(String name, int totalQuantity) {
        Coupon coupon = couponRepository.save(new Coupon(name, totalQuantity));
        return coupon.getId();
    }

    @Transactional
    public IssueResult issue(Long couponId, Long userId) {
        Coupon coupon = couponRepository.findById(couponId).orElseThrow();

        if (!couponIssueRedis.addUser(couponId, userId)) {
            return IssueResult.DUPLICATE;
        }

        long order = couponIssueRedis.increment(couponId);
        if (order > coupon.getTotalQuantity()) {
            couponIssueRedis.removeUser(couponId, userId);
            return IssueResult.SOLD_OUT;
        }

        issuedCouponRepository.save(new IssuedCoupon(couponId, userId));
        return IssueResult.ISSUED;
    }

    public enum IssueResult {
        ISSUED, DUPLICATE, SOLD_OUT
    }
}

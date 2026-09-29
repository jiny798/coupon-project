package dev.kenzi.coupon;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class CouponService {

    private final CouponRepository couponRepository;
    private final CouponIssueRedis couponIssueRedis;
    private final CouponIssueProducer couponIssueProducer;

    public CouponService(CouponRepository couponRepository,
                         CouponIssueRedis couponIssueRedis,
                         CouponIssueProducer couponIssueProducer) {
        this.couponRepository = couponRepository;
        this.couponIssueRedis = couponIssueRedis;
        this.couponIssueProducer = couponIssueProducer;
    }

    @Transactional
    public Long create(String name, int totalQuantity) {
        Coupon coupon = couponRepository.save(new Coupon(name, totalQuantity));
        return coupon.getId();
    }

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

        try {
            couponIssueProducer.send(new CouponIssueMessage(couponId, userId));
        } catch (RuntimeException e) {
            couponIssueRedis.removeUser(couponId, userId);
            throw e;
        }
        return IssueResult.ISSUED;
    }

    public enum IssueResult {
        ISSUED, DUPLICATE, SOLD_OUT
    }
}

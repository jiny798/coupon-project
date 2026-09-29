package dev.kenzi.coupon;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/coupon")
public class CouponController {

    private final CouponService couponService;

    public CouponController(CouponService couponService) {
        this.couponService = couponService;
    }

    @PostMapping
    public Long create(@RequestBody CreateCouponRequest request) {
        return couponService.create(request.name(), request.totalQuantity());
    }

    @PostMapping("/{couponId}/issue/test")
    public ResponseEntity<String> issue(@PathVariable("couponId") Long couponId,
                                        @RequestBody IssueRequest request) {
        return switch (couponService.issue(couponId, request.userId())) {
            case ISSUED -> ResponseEntity.accepted().body("issued");
            case DUPLICATE -> ResponseEntity.ok("duplicate");
            case SOLD_OUT -> ResponseEntity.ok("sold out");
        };
    }

    public record CreateCouponRequest(String name, int totalQuantity) {
    }

    public record IssueRequest(Long userId) {
    }
}

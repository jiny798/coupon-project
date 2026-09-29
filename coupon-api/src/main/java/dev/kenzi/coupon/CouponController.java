package dev.kenzi.coupon;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/coupons")
public class CouponController {

    private final CouponService couponService;

    public CouponController(CouponService couponService) {
        this.couponService = couponService;
    }

    @PostMapping
    public Long create(@RequestBody CreateCouponRequest request) {
        return couponService.create(request.name(), request.totalQuantity());
    }

    @PostMapping("/{couponId}/issue")
    public ResponseEntity<String> issue(@PathVariable("couponId") Long couponId,
                                        @RequestParam("userId") Long userId) {
        return switch (couponService.issue(couponId, userId)) {
            case ISSUED -> ResponseEntity.ok("issued");
            case DUPLICATE -> ResponseEntity.status(HttpStatus.CONFLICT).body("duplicate");
            case SOLD_OUT -> ResponseEntity.status(HttpStatus.CONFLICT).body("sold out");
        };
    }

    public record CreateCouponRequest(String name, int totalQuantity) {
    }
}

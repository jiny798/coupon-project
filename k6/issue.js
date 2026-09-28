import http from 'k6/http';
import { Counter } from 'k6/metrics';

const BASE = 'http://localhost:8080';
const USERS = Number(__ENV.USERS || 1000);
const QUANTITY = Number(__ENV.QUANTITY || 100);

const issued = new Counter('issued');
const soldOut = new Counter('sold_out');

http.setResponseCallback(http.expectedStatuses(200, 409));

export const options = {
  scenarios: {
    burst: {
      executor: 'per-vu-iterations',
      vus: USERS,
      iterations: 1,
    },
  },
};

export function setup() {
  const res = http.post(
    `${BASE}/api/coupons`,
    JSON.stringify({ name: 'k6', totalQuantity: QUANTITY }),
    { headers: { 'Content-Type': 'application/json' } },
  );
  return { couponId: res.body };
}

export default function (data) {
  const res = http.post(`${BASE}/api/coupons/${data.couponId}/issue?userId=${__VU}`);
  if (res.status === 200) {
    issued.add(1);
  }
  if (res.status === 409) {
    soldOut.add(1);
  }
}

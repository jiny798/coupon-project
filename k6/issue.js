import http from 'k6/http';
import exec from 'k6/execution';
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
  const testId = (exec.test.options.tags || {}).testid || 'k6';
  const res = http.post(
    `${BASE}/api/coupons`,
    JSON.stringify({ name: testId, totalQuantity: QUANTITY }),
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

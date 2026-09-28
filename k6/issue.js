import http from 'k6/http';
import exec from 'k6/execution';
import { sleep } from 'k6';
import { Counter } from 'k6/metrics';

const BASE = 'http://localhost:8080';
const QUANTITY = Number(__ENV.QUANTITY || 500);
const VUS = Number(__ENV.VUS || 1000);
const STEP = Number(__ENV.STEP || 100);
const HOLD = __ENV.HOLD || '10s';

function stairs() {
  const stages = [];
  for (let target = STEP; target < VUS + STEP; target += STEP) {
    const level = Math.min(target, VUS);
    stages.push({ duration: '5s', target: level });
    stages.push({ duration: HOLD, target: level });
  }
  return stages;
}

const issued = new Counter('issued');
const soldOut = new Counter('sold_out');

http.setResponseCallback(http.expectedStatuses(200, 409));

export const options = {
  scenarios: {
    ramp: {
      executor: 'ramping-vus',
      startVUs: 0,
      stages: stairs(),
    },
  },
  thresholds: {
    http_req_duration: ['p(95)<3000'],
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
  sleep(1);
  const userId = exec.scenario.iterationInTest + 1;
  const res = http.post(`${BASE}/api/coupons/${data.couponId}/issue?userId=${userId}`);
  if (res.status === 200) {
    issued.add(1);
  }
  if (res.status === 409) {
    soldOut.add(1);
  }
}

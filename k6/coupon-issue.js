import http from 'k6/http';
import exec from 'k6/execution';
import { check, sleep } from 'k6';

const BASE = __ENV.BASE || 'http://localhost:8080';
const QUANTITY = Number(__ENV.QUANTITY || 100000);
const RATE = Number(__ENV.RATE || 0);

const closedModel = {
  executor: 'ramping-vus',
  startVUs: 0,
  stages: [
    { duration: '1m', target: 100 },
    { duration: '2m', target: 100 },
    { duration: '1m', target: 400 },
    { duration: '2m', target: 400 },
    { duration: '1m', target: 500 },
    { duration: '5m', target: 500 },
  ],
};

const openModel = {
  executor: 'ramping-arrival-rate',
  startRate: 0,
  timeUnit: '1s',
  preAllocatedVUs: RATE / 5,
  maxVUs: RATE * 2,
  stages: [
    [1, 0.02], [1, 0.1], [2, 0.1], [1, 0.2], [1, 0.3], [1, 0.4],
    [1, 0.5], [1, 0.6], [1, 0.7], [1, 0.8], [1, 1], [5, 1],
  ].map(([minutes, share]) => ({ duration: `${minutes * 60}s`, target: RATE * share })),
};

export const options = {
  scenarios: {
    warmup_then_load: RATE ? openModel : closedModel,
  },
  thresholds: {
    checks: ['rate>0.99'],
    http_req_duration: ['p(95)<10000'],
  },
};

export function setup() {
  const res = http.post(
    `${BASE}/api/coupon`,
    JSON.stringify({ name: 'k6', totalQuantity: QUANTITY }),
    { headers: { 'Content-Type': 'application/json' } },
  );
  return { couponId: res.body };
}

export default function (data) {
  if (!RATE) {
    sleep(1);
  }

  const userId = exec.scenario.iterationInTest + 1;
  const res = http.post(
    `${BASE}/api/coupon/${data.couponId}/issue/test`,
    JSON.stringify({ userId }),
    { headers: { 'Content-Type': 'application/json' }, timeout: '60s' },
  );

  check(res, {
    'issue request success': (r) => [200, 202].includes(r.status),
  });
}
